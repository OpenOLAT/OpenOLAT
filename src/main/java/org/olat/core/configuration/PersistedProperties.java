/**
* OLAT - Online Learning and Training<br>
* http://www.olat.org
* <p>
* Licensed under the Apache License, Version 2.0 (the "License"); <br>
* you may not use this file except in compliance with the License.<br>
* You may obtain a copy of the License at
* <p>
* http://www.apache.org/licenses/LICENSE-2.0
* <p>
* Unless required by applicable law or agreed to in writing,<br>
* software distributed under the License is distributed on an "AS IS" BASIS, <br>
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
* See the License for the specific language governing permissions and <br>
* limitations under the License.
* <p>
* Copyright (c) since 2004 at Multimedia- & E-Learning Services (MELS),<br>
* University of Zurich, Switzerland.
* <hr>
* <a href="http://www.openolat.org">
* OpenOLAT - Online Learning and Training</a><br>
* This file has been modified by the OpenOLAT community. Changes are licensed
* under the Apache 2.0 license as the original file.  
* <p>
*/
package org.olat.core.configuration;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Paths;
import java.security.SecureRandom;
import java.security.Security;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;

import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.CipherOutputStream;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

import org.apache.logging.log4j.Logger;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.olat.core.id.OLATResourceable;
import org.olat.core.logging.Tracing;
import org.olat.core.util.StringHelper;
import org.olat.core.util.WebappHelper;
import org.olat.core.util.coordinate.CoordinatorManager;
import org.olat.core.util.event.GenericEventListener;
import org.olat.core.util.resource.OresHelper;

/**
 * Description:<br>
 * The PersistedProperties features reading and writing of configuration
 * properties from and to properties files for module and application
 * configuration.
 * <p>
 * The idea is that the system can be configured with default values within the
 * module code either by a spring configuration or by using hardcoded default
 * values. Those default values are overridden by the values stored in the
 * properties files found in the user space of the system (
 * <code>olatdata/system/configuration/fully.qualified.ClassName.properties</code>
 * ). Installing updates will not overwrite those properties files, the 'old'
 * configuration is available immediately to the sytem without reconfiguration.
 * <p>
 * The developer should provide a GUI for each of those values that can be
 * configured at runtime. It is up to the programmer if the system needs a
 * reboot or not.
 * <p>
 * After constructing an instance of this class the setXXXPropertyDefault()
 * methods must be called to set the default values in case no user
 * configuration is found. This is where you can set the values you get from the
 * spring.
 * <p>
 * After the default values have been set, using the getXXXPropertyValue() you
 * will get the current configuration value. The PersistedProperties class will
 * first look in properties from the filesystem and if nothing found, use the
 * setted default values.
 * <p>
 * The class does also provide setXXXProperty() methods. Setting a property will
 * store it always in the user space properties file
 * <code>olatdata/system/configuration/fully.qualified.ClassName.properties</code>
 * . It will not modify any spring file. The setter methods
 * can be stored immediately after each set or remain transient until the
 * explicit savePropertiesAndFireChangedEvent() method is called.
 * <p>
 * To work properly in a cluster environment, the class will fire a
 * PersitedPropertiesChangedEvent at the end of each save cycle. This event must
 * be catched by the class that is using this PersistingProperties. When
 * constructing the PersistingProperties, a reference to the using class must be
 * provided. The PersistingProperties deals with registering and unregistering
 * of the event listeners for the multi-node events, this is hidden to the using
 * class.
 * <p>
 * When finished, the destroy() method must be called. In most cases this will
 * be at system shutdown time.
 * <p>
 * NOTE 1: By replacing the OLAT code with a new version all configurations made
 * will remain since the configuration is stored in the user space and not in
 * the OLAT web application directory. This greatly simplifies upgrading without
 * reconfiguring everything.
 * <p>
 * NOTE 2: When you saved a configuration using the set and save methods in this
 * class, the default configuration in your  spring
 * configuration becomes irrelevant. Changing the values in olat.local.properties or
 * your spring file will have no effect whatsoever.
 * <p>
 * NOTE 3: At any time, the configuration properties files at
 * <code>olatdata/system/configuration/</code> can be deleted. The system then
 * uses the default values provided in your olat_confix.xml, your spring
 * configuration or your hardcoded default values provided at init time.
 * <p>
 * NOTE 4: As a developer you must decide if a configuration can be reflected in
 * realtime or if you must reboot the system. In the first case you can savely
 * use the getter methods to read the configuration every time you need the
 * value, in the second case you should make a copy of the configuration value
 * at init time and operate only with the copy.
 * <p>
 * NOTE 5: In extreme conditions the get/set methods might not be cluster save.
 * Make sure you use a cluster wide GUI lock in the admin interfaces that makes
 * use of the setter methods and you are on the save side. On the other hand it
 * is very unlikely that something really bad happens, the properties files are
 * just overwritten by the next save.
 * <p>
 * <h3>Events thrown by this class:</h3>
 * <ul>
 * <li>PersistedPropertiesChangedEvent in case the configuration got changed</li>
 * </ul>
 * <p>
 * Initial Date: 01.10.2007 <br>
 * 
 * @author Florian Gnägi, http://www.frentix.com
 */
public class PersistedProperties implements Initializable, Destroyable{

	private static final Logger log = Tracing.createLoggerFor(PersistedProperties.class);
	
	// base directory where all system config files are located
	private File configurationPropertiesFile;
	// the properties loaded from disk
	private final Properties configuredProperties = new Properties();
	// the volatile default properties
	private Properties defaultProperties = new Properties();
	// flag to indicate property set operations that have not yet been saved
	private boolean propertiesDirty = false;
	private GenericEventListener propertiesChangedEventListener;

	private OLATResourceable PROPERTIES_CHANGED_EVENT_CHANNEL;
	private CoordinatorManager coordinatorManager;
	
	private final boolean secured;
	private final String filename;
	private String userDataDirectory;
	
	static {
		if(Security.getProvider(BouncyCastleProvider.PROVIDER_NAME) != null) {
			Security.removeProvider(BouncyCastleProvider.PROVIDER_NAME);
		}
		Security.insertProviderAt(new BouncyCastleProvider(), 1);
	}
	
	public PersistedProperties(GenericEventListener listener) {
		this(CoordinatorManager.getInstance(), listener);
	}
	
	public PersistedProperties(CoordinatorManager coordinatorManager, GenericEventListener listener, boolean secured) {
		this.coordinatorManager = coordinatorManager;
		this.propertiesChangedEventListener = listener;
		this.filename = propertiesChangedEventListener.getClass().getCanonicalName() + ".properties";
		this.secured = secured;
	}
	
	public PersistedProperties(CoordinatorManager coordinatorManager, GenericEventListener listener, String filename, boolean secured) {
		this.coordinatorManager = coordinatorManager;
		this.propertiesChangedEventListener = listener;
		this.filename = filename + ".properties";
		this.secured = secured;
	}
	
	/**
	 * [used by spring]
	 * @param provide coordinatorManager via DI
	 * fxdiff: needs to be public while another constructor is also public (due to spring loading)
	 */
	public PersistedProperties(CoordinatorManager coordinatorManager, GenericEventListener listener) {
		this.coordinatorManager = coordinatorManager;
		// Keep handle for dispose process
		this.propertiesChangedEventListener = listener;
		this.filename= propertiesChangedEventListener.getClass().getCanonicalName()	+ ".properties";
		this.secured = false;
	}
	
	public void setUserDataDirectory(String directory) {
		userDataDirectory = directory;
	}

	/**
	 * Constructor for a PersistedProperties object. The calling class must
	 * implement the GenericEventListener and provide a reference to itself.
	 * 
	 * @param propertiesChangedEventListener
	 */
	@Override
	public void init() {
		if(userDataDirectory == null) {
			userDataDirectory = WebappHelper.getUserDataRoot();
		}
		
		// Load configured properties from properties file
		configurationPropertiesFile = Paths.get(userDataDirectory, "system", "configuration", filename).toFile();
		loadPropertiesFromFile();
		// Finally add listener to configuration changes done in other nodes
		PROPERTIES_CHANGED_EVENT_CHANNEL = OresHelper.createOLATResourceableType(propertiesChangedEventListener.getClass().getSimpleName()
				+ ":PropertiesChangedChannel");
		coordinatorManager.getCoordinator().getEventBus().registerFor(propertiesChangedEventListener, null, PROPERTIES_CHANGED_EVENT_CHANNEL);			
	}

	/**
	 * Load the persisted properties from disk. This can be useful when your
	 * code gets a PersistedPropertiesChangedEvent and you just want to reload 
	 * the property instead of modifying the one you have already loaded.
	 */
	public void loadPropertiesFromFile() {
		// Might get an event after beeing disposed. Should not be the case, but you never know with multi user events accross nodes.
		if (propertiesChangedEventListener != null && configurationPropertiesFile.exists()) {
			InputStream is;
			try {
				is = new FileInputStream(configurationPropertiesFile);

				if(secured) {
					SecretKey key = generateKey("rk6R9pQy7dg3usJk");
					Cipher cipher = Cipher.getInstance("AES/CTR/NOPADDING");
					cipher.init(Cipher.DECRYPT_MODE, key, random);
					is =  new CipherInputStream(is, cipher);
				}
				
				configuredProperties.load(is);
				is.close();
			} catch (Exception e) {
				log.error("Could not load config file from path::" + configurationPropertiesFile.getAbsolutePath(), e);
			}
		}
	}
	
	/**
	 * [normally set by spring at startup to pass default values from olat.properties]
	 * @param defaultProperties
	 */
	public void setDefaultProperties(Properties defaultProperties) {
		this.defaultProperties = defaultProperties;
	}

	/**
	 * Call this method when the PersitedProperties is not used anymore. Will
	 * remove the event listener for change events on this class
	 */
	@Override
	public final void destroy() {
		if (propertiesChangedEventListener != null) {
			coordinatorManager.getCoordinator().getEventBus().deregisterFor(propertiesChangedEventListener, PROPERTIES_CHANGED_EVENT_CHANNEL);
			propertiesChangedEventListener = null;
		}
	}
	
	public Set<Object> getPropertyKeys() {
		Set<Object> keys = new HashSet<>();
		if(configuredProperties != null) {
			keys.addAll(configuredProperties.keySet());
		}
		if(defaultProperties != null) {
			keys.addAll(defaultProperties.keySet());
		}	
		return keys;
	}

	/**
	 * Return an int value for a certain propertyName
	 * 
	 * @param propertyName
	 * @return The value from the configuration or 0
	 */
	public int getIntPropertyValue(String propertyName) {
		return getIntPropertyValue(propertyName, 0);
	}
	
	/**
	 * Return an int value for a certain propertyName
	 * 
	 * @param propertyName The property name
	 * @param defaultValue The default value
	 * @return The value from the configuration or the default value
	 */
	public int getIntPropertyValue(String propertyName, int defaultValue) {
		// 1) Try from configuration
		String stringValue = configuredProperties.getProperty(propertyName);
		// 2) Try from default configuration
		if (stringValue == null) {
			stringValue = defaultProperties.getProperty(propertyName);
		}
		if (StringHelper.containsNonWhitespace(stringValue)) {
			try {
				return Integer.parseInt(stringValue.trim());
			} catch (Exception ex) {
				log.warn("Cannot parse to integer property::" + propertyName + ", value=" + stringValue);
			}
		}
		// 3) Not even a value found in the fallback, use 0
		if(log.isDebugEnabled()) {
			log.debug("No value found for int property::" + propertyName + ", using value=0 instead");
		}
		return defaultValue;
	}

	/**
	 * Return a string value for certain propertyName-parameter.
	 * 
	 * @param propertyName
	 * @param allowEmptyString true: empty strings are valid values; false: emtpy
	 *          strings are discarded
	 * @return the value from the configuration or the default value or ""/NULL
	 *         (depending on allowEmptyString flag)
	 */
	public String getStringPropertyValue(String propertyName, boolean allowEmptyString) {
		// 1) Try from configuration
		String stringValue = configuredProperties.getProperty(propertyName);
		// 2) Try from default configuration
		if (stringValue == null || (!allowEmptyString && !StringHelper.containsNonWhitespace(stringValue))) {
			stringValue = defaultProperties.getProperty(propertyName);
		}
		if (stringValue != null) {
			if (allowEmptyString || StringHelper.containsNonWhitespace(stringValue)) { return stringValue.trim(); }
		}
		// 3) Not even a value found in the fallback, return empty string
		stringValue = (allowEmptyString ? "" : null);
		if(log.isDebugEnabled()) {
			log.debug("No value found for string property::" + propertyName + ", using value=\"\" instead");
		}
		return stringValue;
	}

	/**
	 * Return a boolean value for certain propertyName
	 * 
	 * @param propertyName
	 * @return the value from the configuration or the default value or false
	 */
	public boolean getBooleanPropertyValue(String propertyName) {
		// 1) Try from configuration
		String stringValue = configuredProperties.getProperty(propertyName);
		// 2) Try from default configuration
		if (stringValue == null) {
			stringValue = defaultProperties.getProperty(propertyName);
		}
		if ((stringValue != null) && stringValue.trim().equalsIgnoreCase("TRUE")) { return true; }
		if ((stringValue != null) && stringValue.trim().equalsIgnoreCase("FALSE")) { return false; }
		// 3) Not even a value found in the fallback, return false
		if(log.isDebugEnabled()) {
			log.warn("No value found for boolean property::" + propertyName + ", using value=false instead");
		}
		return false;
	}

	/**
	 * Set a string property
	 * 
	 * @param propertyName The key
	 * @param value The Value
	 * @param saveConfiguration true: will save property and fire event; false:
	 *          will not save, but set a dirty flag
	 */
	public void setStringProperty(String propertyName, String value, boolean saveConfiguration) {
		synchronized (configuredProperties) { // make read/write save in VM
			String oldValue = configuredProperties.getProperty(propertyName);
			if (oldValue == null || !oldValue.equals(value)) {
				if(value == null) {
					configuredProperties.remove(propertyName);
				} else {
					configuredProperties.setProperty(propertyName, value);
				}
				propertiesDirty = true;
				if (saveConfiguration) savePropertiesAndFireChangedEvent();
			}
		}
	}

	/**
	 * Set an int property
	 * 
	 * @param propertyName The key
	 * @param value The Value
	 * @param saveConfiguration true: will save property and fire event; false:
	 *          will not save, but set a dirty flag
	 */
	public void setIntProperty(String propertyName, int value, boolean saveConfiguration) {
		synchronized (configuredProperties) { // make read/write save in VM
			String oldValue = configuredProperties.getProperty(propertyName);
			if (oldValue == null || !oldValue.equals(Integer.toString(value))) {
				configuredProperties.setProperty(propertyName, Integer.toString(value));
				propertiesDirty = true;
				if (saveConfiguration) savePropertiesAndFireChangedEvent();
			}
		}
	}

	/**
	 * Set a boolean property
	 * 
	 * @param propertyName The key
	 * @param value The Value
	 * @param saveConfiguration true: will save property and fire event; false:
	 *          will not save, but set a dirty flag
	 */
	public void setBooleanProperty(String propertyName, boolean value, boolean saveConfiguration) {
		synchronized (configuredProperties) { // make read/write save in VM
			String oldValue = configuredProperties.getProperty(propertyName);
			if (oldValue == null || !oldValue.equals(Boolean.toString(value))) {
				configuredProperties.setProperty(propertyName, Boolean.toString(value));
				propertiesDirty = true;
				if (saveConfiguration) savePropertiesAndFireChangedEvent();
			}
		}
	}

	/**
	 * Set a default value for a string property
	 * 
	 * @param propertyName
	 * @param value
	 */
	public void setStringPropertyDefault(String propertyName, String value) {
		defaultProperties.setProperty(propertyName, value);
	}

	/**
	 * Set a default value for an integer property
	 * 
	 * @param propertyName
	 * @param value
	 */
	public void setIntPropertyDefault(String propertyName, int value) {
		defaultProperties.setProperty(propertyName, Integer.toString(value));
	}

	/**
	 * Set a default value for a boolean property
	 * 
	 * @param propertyName
	 * @param value
	 */
	public void setBooleanPropertyDefault(String propertyName, boolean value) {
		defaultProperties.setProperty(propertyName, Boolean.toString(value));
	}
	
	public void removeProperty(String propertyName, boolean saveConfiguration) {
		synchronized (configuredProperties) { // make read/write save in VM
			Object removedProperty = configuredProperties.remove(propertyName);
			propertiesDirty |= removedProperty != null;
			if (saveConfiguration) {
				savePropertiesAndFireChangedEvent();
			}
		}
	}

	/**
	 * Save the properties configuration to disk and notify other nodes about
	 * change. This is only done when there are dirty changes, otherwhile the
	 * method call does nothing.
	 */
	public void savePropertiesAndFireChangedEvent() {
		// Only save when there is something to save
		synchronized (configuredProperties) { // make read/write save in VM
			if (!propertiesDirty) return;
			// Set the default language
			OutputStream fileStream = null;
			try {
				if (!configurationPropertiesFile.exists()) {
					File directory = configurationPropertiesFile.getParentFile();
					if (!directory.exists()) directory.mkdirs();
				}
				fileStream = new FileOutputStream(configurationPropertiesFile);
				
				if(secured) {
					SecretKey key = generateKey("rk6R9pQy7dg3usJk");
					Cipher cipher = Cipher.getInstance("AES/CTR/NOPADDING");
		        	cipher.init(Cipher.ENCRYPT_MODE, key, random);
		        	fileStream =  new CipherOutputStream(fileStream, cipher);	
				}
				
				configuredProperties.store(fileStream, null);
				// Flush and close before sending events to other nodes to make changes appear on other node
				fileStream.flush();
				fileStream.close();
				// Notify other cluster nodes about changed configuration
				PersistedPropertiesChangedEvent changedConfigEvent = new PersistedPropertiesChangedEvent(configuredProperties);
				coordinatorManager.getCoordinator().getEventBus().fireEventToListenersOf(changedConfigEvent, PROPERTIES_CHANGED_EVENT_CHANNEL);
			} catch (Exception e) {
				log.error("Could not write config file from path::" + configurationPropertiesFile.getAbsolutePath(), e);
			} finally {
				try {
					if (fileStream != null ) fileStream.close();
				} catch (IOException e) {
					log.error("Could not close stream after storing config to file::" + configurationPropertiesFile.getAbsolutePath(), e);
				}
			}
			// Reset for next save cycle
			propertiesDirty = false;
		}
	}

	/**
	 * Clear the properties and save the empty properties to the file system.
	 */
	public void clearAndSaveProperties() {
		synchronized (configuredProperties) { // make read/write save in VM
			if (configuredProperties.size() != 0) {
				configuredProperties.clear();
				propertiesDirty = true;
				savePropertiesAndFireChangedEvent();
			}
		}
	}

	/**
	 * Clone the persisted properties to a standard properties object.
	 * 
	 * @return
	 */
	public Properties createPropertiesFromPersistedProperties() {
		Properties tmp = new Properties();
		// first copy all the default values
		for (Object keyObject : defaultProperties.keySet()) {
			String key = (String) keyObject;
			tmp.setProperty(key, getStringPropertyValue(key, true));
		}
		// second copy the configured values
		for (Object keyObject : configuredProperties.keySet()) {
			String key = (String) keyObject;
			tmp.setProperty(key, getStringPropertyValue(key, true));
		}		
		return tmp;
	}

  
	private static final String salt = "A long, but constant phrase that will be used each time as the salt.";
	private static final int iterations = 2000;
	private static final int keyLength = 128;
	private static final SecureRandom random = new SecureRandom();
  
	private static SecretKey generateKey(String passphrase) throws Exception {
		PBEKeySpec keySpec = new PBEKeySpec(passphrase.toCharArray(), salt.getBytes(), iterations, keyLength);
		SecretKeyFactory keyFactory = SecretKeyFactory.getInstance("PBEWITHSHA256AND128BITAES-CBC-BC");
		return keyFactory.generateSecret(keySpec);
	}
}
