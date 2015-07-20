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

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicInteger;

import org.olat.core.gui.control.Event;
import org.olat.core.logging.LogDelegator;
import org.olat.core.logging.OLog;
import org.olat.core.logging.StartupException;
import org.olat.core.logging.Tracing;
import org.olat.core.util.StringHelper;
import org.olat.core.util.event.GenericEventListener;

/**
 * Description:<br>
 * Default/abstract class with helper methods. <br>
 * The abstract olat module features reading and writing of configuration
 * properties to config and properties files. The idea is that the system can be
 * configured with default values either in the config xml file or by providing
 * reasonable default values directly in the module code.
 * <p>
 * The developer should provide a GUI for each value that can be configured at
 * runtime without the need of rebooting the entire system. But this is up to
 * the programmer who implements the setter methods for the config values.
 * <p>
 * The getter methods will load the configuration in the following order:
 * <ol>
 * <li>
 * <code>olatdata/system/configuration/fully.qualified.ClassName.properties</code>
 * </li>
 * <li>falling back to<code>olat.local.properties</code></li>
 * <li>falling back to<code>olat.properties</code></li>
 * <li>falling back to default value defined in method call</li>
 * </ol>
 * The class does also provide save methods. Setting a config parameter will
 * store it always in the user space config file
 * <code>olatdata/system/configuration/fully.qualified.ClassName.properties</code>
 * and not in the olat.properties, those are only the default values in case no
 * other configuration.
 * <p>
 * To work properly in a cluster environment, the module will fire a
 * ModuleConfiguratoinChangedEvent at the end of each save cycle. This event is
 * catched automatically and the abstract method initFromChangedProperties() is
 * called.
 * <p>
 * For more information on how the storing mechanism of the configuration works
 * please have a look at the PersistedProperties class.
 * <p>
 * If you want to use the properties mechanism in a spring loaded class, use the
 * PersistedProperties class directly to read/write your application
 * configuration.
 * <p>
 * Initial Date: 01.10.2007 <br>
 * 
 * @author Felix Jost, http://www.goodsolutions.ch
 * @author Florian Gnägi, http://www.frentix.com
 */
public abstract class AbstractOLATModule extends LogDelegator implements GenericEventListener {
	protected PersistedProperties moduleConfigProperties;
	private Properties moduleDefaultConfig;
	
	public static final Map<Class<?>,AtomicInteger> starts = new HashMap<Class<?>,AtomicInteger>();
	
	public AbstractOLATModule() {
		if(!starts.containsKey(this.getClass())) {
			starts.put(this.getClass(), new AtomicInteger(1));
		} else {
			starts.get(this.getClass()).incrementAndGet();
		}
	}
	
	public static void printStats() {
		OLog logger = Tracing.createLoggerFor(AbstractOLATModule.class);
		for(Map.Entry<Class<?>, AtomicInteger> entry:starts.entrySet()) {
			if(entry.getValue().get() > 1) {
				logger.info(entry.getValue().get() + " :: " + entry.getKey());
			}
		}
	}

	/**
	 * Initialize the module. Called by the spring framework at startup time
	 */
	public final void init(Properties moduleConfig) {
		// Default module configuration from xml file
		this.moduleDefaultConfig = moduleConfig;
		// Let the module set the default properties
		if (moduleConfigProperties == null) throw new StartupException("PersistedProperties is null. You have to set Persisted properties in spring config when you use abstractOlatModule!");
		initDefaultProperties();
		init();
	}
	
	public abstract void init();
	
	
	public abstract void setPersistedProperties(PersistedProperties persistedProperties);

	/**
	 * Called during module initialization to read the default values from the
	 * configuration and set them as config properties default.
	 */
	protected abstract void initDefaultProperties();

	/**
	 * Called whenever the properties configuraton changed (e.g. on this or on another
	 * cluster node). The properties have been reloaded prior to when this method is executed.
	 */
	protected abstract void initFromChangedProperties();
	
	
	/**
	 * Return an int value for certain config-parameter. If the parameter does
	 * not exist, return the defaultValue.
	 * 
	 * @param parameterName
	 * @param defaultValue
	 * @param
	 * @return The int value.
	 */
	protected int getIntConfigParameter(String parameterName, int defaultValue) {
		String stringValue = moduleDefaultConfig.getProperty(parameterName);
		if (StringHelper.containsNonWhitespace(stringValue)) {
			try {
				return Integer.parseInt(stringValue.trim());
			} catch (Exception ex) {
				logWarn("Cannot parse to integer conf-parameter '" + parameterName + "', value=" + stringValue, null);
			}
		}
		logInfo("Take default value for integer conf-parameter '" + parameterName + "', value=" + defaultValue, null);
		return defaultValue;
	}

	/**
	 * Return a string value for certain config-parameter. If the parameter does
	 * not exist, return the defaultValue.
	 * 
	 * @param parameterName
	 * @param defaultValue
	 * @param
	 * @return The string value. Can be empty, but never null
	 */
	protected String getStringConfigParameter(String parameterName, String defaultValue, boolean allowEmptyString) {
		String stringValue = moduleDefaultConfig.getProperty(parameterName);
		if (stringValue != null) {
			if (allowEmptyString || StringHelper.containsNonWhitespace(stringValue)) {
				return stringValue.trim();				
			}
		}
		logInfo("Take default value for String conf-parameter '" + parameterName + "', value=" + defaultValue, null);
		return defaultValue;
	}

	/**
	 * Return a boolean value for certain config-parameter. If the paramter does
	 * not exist, return the defaultValue. 'true' and 'enabled' return <code>true</code>, 'false' and 'disabled' return <code>false</code>. 
	 * 
	 * @param parameterName
	 * @param defaultValue
	 * @return
	 */
	protected boolean getBooleanConfigParameter(String parameterName, boolean defaultValue) {
		String stringValue = moduleDefaultConfig.getProperty(parameterName);			
		if ((stringValue != null) && (stringValue.trim().equalsIgnoreCase("TRUE") || stringValue.trim().equalsIgnoreCase("ENABLED"))) {
			return true;
		}
		if ((stringValue != null) && (stringValue.trim().equalsIgnoreCase("FALSE") || stringValue.trim().equalsIgnoreCase("DISABLED"))) {
			return false;
		}
		logInfo("Take default Boolean conf-parameter '" + parameterName + "', value=" + stringValue
				+ ", only true/false supported => take default value.", null);
		return defaultValue;
	}
	
	//
	// Delegate methods used to get and set the values and default in the user
	// configuration using the persisted properties.
	//

	/**
	 * Return a string value for certain propertyName-parameter.
	 * 
	 * @param propertyName
	 * @param allowEmptyString
	 *            true: empty strings are valid values; false: emtpy strings are
	 *            discarded
	 * @return the value from the configuration or the default value or ""/NULL
	 *         (depending on allowEmptyString flag)
	 */
	protected String getStringPropertyValue(String propertyName, boolean allowEmptyString) {
		// delegate to new property based config style
		return moduleConfigProperties.getStringPropertyValue(propertyName, allowEmptyString);
	}
	/**
	 * Set a string property
	 * 
	 * @param propertyName
	 *            The key
	 * @param value
	 *            The Value
	 * @param saveConfiguration
	 *            true: will save property and fire event; false: will not save,
	 *            but set a dirty flag
	 */
	protected void setStringProperty(String propertyName, String value, boolean saveConfiguration) {
		// delegate to new property based config style
		moduleConfigProperties.setStringProperty(propertyName, value, saveConfiguration);
		logAudit("change system property: " + propertyName, value);
	}
	/**
	 * Retrun an int value for a certain propertyName
	 * 
	 * @param propertyName
	 * @return the value from the configuration or the default value or 0
	 */
	protected int getIntPropertyValue(String propertyName) {
		// delegate to new property based config style
		return moduleConfigProperties.getIntPropertyValue(propertyName);
	}
	/**
	 * Set an int property
	 * 
	 * @param propertyName
	 *            The key
	 * @param value
	 *            The Value
	 * @param saveConfiguration
	 *            true: will save property and fire event; false: will not save,
	 *            but set a dirty flag
	 */
	protected void setIntProperty(String propertyName, int value, boolean saveConfiguration) {
		// delegate to new property based config style
		moduleConfigProperties.setIntProperty(propertyName, value, saveConfiguration);
		logAudit("change system property: " + propertyName, Integer.toString(value));
	}
	/**
	 * Return a boolean value for certain propertyName
	 * 
	 * @param propertyName
	 * @return the value from the configuration or the default value or false
	 */
	protected boolean getBooleanPropertyValue(String propertyName) {
		// delegate to new property based config style
		return moduleConfigProperties.getBooleanPropertyValue(propertyName);
	}
	/**
	 * Set a boolean property
	 * 
	 * @param propertyName
	 *            The key
	 * @param value
	 *            The Value
	 * @param saveConfiguration
	 *            true: will save property and fire event; false: will not save,
	 *            but set a dirty flag
	 */
	protected void setBooleanProperty(String propertyName, boolean value, boolean saveConfiguration) {
		// delegate to new property based config style
		moduleConfigProperties.setBooleanProperty(propertyName, value, saveConfiguration);
		logAudit("change system property: " + propertyName,  Boolean.toString(value));
	}
	/**
	 * Save the properties configuration to disk and notify other nodes about
	 * change. This is only done when there are dirty changes, otherwhile the
	 * method call does nothing.
	 */
	protected void savePropertiesAndFireChangedEvent() {
		// delegate to new property based config style
		moduleConfigProperties.savePropertiesAndFireChangedEvent();
	}
	/**
	 * Clear the properties and save the empty properties to the file system.
	 */
	protected void clearAndSaveProperties() {
		// delegate to new property based config style
		moduleConfigProperties.clearAndSaveProperties();
	}
	/**
	 * Set a default value for a string property
	 * @param propertyName
	 * @param value
	 */
	protected void setStringPropertyDefault(String key, String value){
		// delegate to new property based config style
		moduleConfigProperties.setStringPropertyDefault(key, value);		
	}
	/**
	 * Set a default value for a boolean property
	 * @param propertyName
	 * @param value
	 */
	protected void setBooleanPropertyDefault(String key, boolean value){
		// delegate to new property based config style
		moduleConfigProperties.setBooleanPropertyDefault(key, value);		
	}
	/**
	 * Set a default value for an integer property
	 * @param propertyName
	 * @param value
	 */
	protected void setIntPropertyDefault(String key, int value){
		// delegate to new property based config style
		moduleConfigProperties.setIntPropertyDefault(key, value);		
	}
	
	/**
	 * @see org.olat.core.util.event.GenericEventListener#event(org.olat.core.gui.control.Event)
	 */
	public void event(Event event) {
		if (event instanceof PersistedPropertiesChangedEvent) {
			PersistedPropertiesChangedEvent persistedPropertiesEvent = (PersistedPropertiesChangedEvent) event;
			if (!persistedPropertiesEvent.isEventOnThisNode()) {
				// Reload the module configuration from disk, only when event not fired by this node
				moduleConfigProperties.loadPropertiesFromFile();
			}
			// Call abstract method to initialize after the property changed, even when changes
			// were triggered by this node. 
			initFromChangedProperties();				
		}		
	}
}
