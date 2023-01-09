/**
 * <a href="http://www.openolat.org">
 * OpenOLAT - Online Learning and Training</a><br>
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); <br>
 * you may not use this file except in compliance with the License.<br>
 * You may obtain a copy of the License at the
 * <a href="http://www.apache.org/licenses/LICENSE-2.0">Apache homepage</a>
 * <p>
 * Unless required by applicable law or agreed to in writing,<br>
 * software distributed under the License is distributed on an "AS IS" BASIS, <br>
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
 * See the License for the specific language governing permissions and <br>
 * limitations under the License.
 * <p>
 * Initial code contributed and copyrighted by<br>
 * frentix GmbH, http://www.frentix.com
 * <p>
 */
package org.olat.core.configuration;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.logging.log4j.Logger;
import org.olat.core.gui.control.Event;
import org.olat.core.logging.Tracing;
import org.olat.core.util.StringHelper;
import org.olat.core.util.coordinate.CoordinatorManager;
import org.olat.core.util.event.GenericEventListener;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;

/**
 * 
 * This replace the AbstractOLATModule with an annotation
 * based module which is better loaded by Spring in cae of heavy
 * cycle in the dependency tree of the spring beans.<br>
 * To get a property from olat.properties or olat.local.properties,
 * use the @Value annotation of Spring Framework and please
 * set a default value:<br>
 * @Value("${my.prop:defaultValue}")<br> 
 * 
 * 
 * 
 * Initial date: 08.08.2014<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public abstract class AbstractSpringModule implements GenericEventListener, InitializingBean, DisposableBean {
	
	private static final Logger log = Tracing.createLoggerFor(AbstractSpringModule.class);
	
	@Value("${userdata.dir}")
	private String userDataDirectory;
	
	private final PersistedProperties moduleConfigProperties;
	
	private static final Map<Class<?>,AtomicInteger> starts = new HashMap<>();
	private static final String PROPERTIES_SALT = "A long, but constant phrase that will be used each time as the salt.";

	public AbstractSpringModule(CoordinatorManager coordinatorManager) {
		moduleConfigProperties = new PersistedProperties(coordinatorManager, this);
		if(!starts.containsKey(this.getClass())) {
			starts.put(this.getClass(), new AtomicInteger(1));
		} else {
			starts.get(this.getClass()).incrementAndGet();
		}
	}
	
	public AbstractSpringModule(CoordinatorManager coordinatorManager, String path) {
		moduleConfigProperties = new PersistedProperties(coordinatorManager, this, path, false, null);
		if(!starts.containsKey(this.getClass())) {
			starts.put(this.getClass(), new AtomicInteger(1));
		} else {
			starts.get(this.getClass()).incrementAndGet();
		}
	}
	
	public AbstractSpringModule(CoordinatorManager coordinatorManager, boolean secured) {
		moduleConfigProperties = new PersistedProperties(coordinatorManager, this, secured, PROPERTIES_SALT);
		if(!starts.containsKey(this.getClass())) {
			starts.put(this.getClass(), new AtomicInteger(1));
		} else {
			starts.get(this.getClass()).incrementAndGet();
		}
	}
	
	public AbstractSpringModule(CoordinatorManager coordinatorManager, String path, boolean secured) {
		moduleConfigProperties = new PersistedProperties(coordinatorManager, this, path, secured, PROPERTIES_SALT);
		if(!starts.containsKey(this.getClass())) {
			starts.put(this.getClass(), new AtomicInteger(1));
		} else {
			starts.get(this.getClass()).incrementAndGet();
		}
	}
	
	public static void printStats() {
		Logger logger = Tracing.createLoggerFor(AbstractSpringModule.class);
		for(Map.Entry<Class<?>, AtomicInteger> entry:starts.entrySet()) {
			if(entry.getValue().get() > 1) {
				logger.info("{} :: {}", entry.getValue().get(), entry.getKey());
			}
		}
	}

	@Override
	public void afterPropertiesSet()  {
		if (!StringHelper.containsNonWhitespace(userDataDirectory)) {
			userDataDirectory = System.getProperty("java.io.tmpdir") + "/olatdata";
		}
		moduleConfigProperties.setUserDataDirectory(userDataDirectory);
		moduleConfigProperties.init();
		initDefaultProperties();
		init();
	}

	@Override
	public void destroy() {
		moduleConfigProperties.destroy();
	}

	public abstract void init();

	/**
	 * Called during module initialization to read the default values from the
	 * configuration and set them as config properties default.
	 */
	protected void initDefaultProperties() {
		//override if needed
	}

	/**
	 * Called whenever the properties configuraton changed (e.g. on this or on another
	 * cluster node). The properties have been reloaded prior to when this method is executed.
	 */
	protected abstract void initFromChangedProperties();
	
	protected Properties createPropertiesFromPersistedProperties() {
		return moduleConfigProperties.createPropertiesFromPersistedProperties();
	}
	
	protected Set<Object> getPropertyKeys() {
		return moduleConfigProperties.getPropertyKeys();
	}

	/**
	 * Return a string value for certain propertyName-parameter.
	 * 
	 * @param propertyName
	 * @param allowEmptyString
	 *            true: empty strings are valid values; false: empty strings are
	 *            discarded
	 * @return the value from the configuration or the default value or ""/NULL
	 *         (depending on allowEmptyString flag)
	 */
	protected String getStringPropertyValue(String propertyName, boolean allowEmptyString) {
		// delegate to new property based config style
		return moduleConfigProperties.getStringPropertyValue(propertyName, allowEmptyString);
	}
	
	protected String getStringPropertyValue(String propertyName, String defaultValue) {
		// delegate to new property based config style
		String val = moduleConfigProperties.getStringPropertyValue(propertyName, true);
		if(StringHelper.containsNonWhitespace(val)) {
			return val;
		}
		return defaultValue;
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
	protected String setStringProperty(String propertyName, String value, boolean saveConfiguration) {
		// delegate to new property based config style
		moduleConfigProperties.setStringProperty(propertyName, value, saveConfiguration);
		log.info(Tracing.M_AUDIT, "change system property: {} {}", propertyName, value);
		return value;
	}
	
	/**
	 * Set a string which must not be logged
	 * @param propertyName
	 * @param value
	 * @param saveConfiguration
	 */
	protected void setSecretStringProperty(String propertyName, String value, boolean saveConfiguration) {
		// delegate to new property based config style
		moduleConfigProperties.setStringProperty(propertyName, value, saveConfiguration);
		log.info(Tracing.M_AUDIT, "change system property: {} {}", propertyName, "*********");
	}
	
	/**
	 * Return an int value for a certain property name
	 * 
	 * @param propertyName The property name
	 * @return the value from the configuration or the default value or 0
	 */
	protected int getIntPropertyValue(String propertyName) {
		// delegate to new property based config style
		return moduleConfigProperties.getIntPropertyValue(propertyName);
	}
	
	/**
	 * Return an int value for a certain property name
	 * 
	 * @param propertyName The property name
	 * @param defaultValue The default value if the property is not set
	 * @return The value from the configuration or the default value
	 */
	protected int getIntPropertyValue(String propertyName, int defaultValue) {
		// delegate to new property based config style
		return moduleConfigProperties.getIntPropertyValue(propertyName, defaultValue);
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
		log.info(Tracing.M_AUDIT, "change system property: {} {}", propertyName, Integer.valueOf(value));
	}
	
	
	/**
	 * Return a Long value for a certain property name
	 * 
	 * @param propertyName The property name
	 * @return the value from the configuration or null
	 */
	protected Long getLongProperty(String propertyName) {
		// delegate to new property based config style
		return moduleConfigProperties.getLongPropertyValue(propertyName, null);
	}
	
	/**
	 * Return a Long value for a certain property name
	 * 
	 * @param propertyName The property name
	 * @param defaultValue The default value if the property is not set
	 * @return The value from the configuration or the default value
	 */
	protected Long getLongPropertyValue(String propertyName, Long defaultValue) {
		// delegate to new property based config style
		return moduleConfigProperties.getLongPropertyValue(propertyName, defaultValue);
	}
	
	
	/**
	 * Set a Long property
	 * 
	 * @param propertyName
	 *            The key
	 * @param value
	 *            The Value
	 * @param saveConfiguration
	 *            true: will save property and fire event; false: will not save,
	 *            but set a dirty flag
	 */
	protected void setLongProperty(String propertyName, Long value, boolean saveConfiguration) {
		// delegate to new property based config style
		moduleConfigProperties.setLongProperty(propertyName, value, saveConfiguration);
		log.info(Tracing.M_AUDIT, "change system property: {} {}", propertyName, Long.valueOf(value));
	}
	
	
	protected void removeProperty(String propertyName, boolean saveConfiguration) {
		moduleConfigProperties.removeProperty(propertyName, saveConfiguration);
		log.info(Tracing.M_AUDIT, "remove system property: {}", propertyName);
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
		log.info(Tracing.M_AUDIT, "change system property: {} {}", propertyName,  value);
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

	@Override
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
