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
*/

package org.olat.modules;

import java.io.Serializable;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.Logger;
import org.olat.core.logging.Tracing;
import org.olat.core.util.Formatter;
import org.olat.core.util.StringHelper;

/**
 * Initial Date:  Dec 8, 2003
 *
 * @author gnaegi
 */
public class ModuleConfiguration implements Serializable {

	private static final Logger log = Tracing.createLoggerFor(ModuleConfiguration.class);
	private static final long serialVersionUID = 5997068149344924126L;

	/**
	 * Configuration flag for the configuration version. The configuration version
	 * is stored using an Integer in the module configuration. 
	 */
	private static final String CONFIG_VERSION = "configversion";

	private Map<String,Object> config;
	
	/**
	 * Default constructor.
	 */
	public ModuleConfiguration() {
		config = new HashMap<>();
	}
	
	public Map<String,Object> getConfigEntries(String keyFragment) {
		Map<String, Object> returnMap = new HashMap<>();
		
		for (Map.Entry<String, Object> entry : config.entrySet()) {
			if (entry == null || entry.getKey() == null) {
				continue; 
			}
			
			if (entry.getKey().contains(keyFragment)) {
				returnMap.put(entry.getKey(), entry.getValue());
			}
		}
		
		return returnMap;
	}

	/**
	 * Set a key to a value.
	 * Important: Value must be serailizable.
	 * 
	 * @param key
	 * @param value
	 */
	public void set(String key, Object value) {
		if (!(value instanceof Serializable) && value != null) throw new RuntimeException("ModuleConfiguration only accepts serializable values.");
		config.put(key, value);
	}

	/**
	 * Get value by key.
	 * @param key
	 * @return value or null if no such key
	 */
	public Object get(String key) {
		return config.get(key);
	}
	
	/**
	 * Remove key/value.
	 * @param key
	 * @return value of removed key
	 */
	public Object remove(String key) {
		return config.remove(key);
	}
	
	/**
	 * Copy all entries from the given module configuration into this module configuration.
	 * @param theConfig
	 */
	public void putAll(ModuleConfiguration theConfig) {
		config.putAll(theConfig.config);
	}
	
	/**
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return config.entrySet() +", "+super.toString();
	}

	/**
	 * return a config value as a Boolean
	 * @param config_key the key
	 * @return null if no such key, or true if there is a entry under 'key' of type string with value "true", or false otherwise
	 */
	public Boolean getBooleanEntry(String config_key) {
		// boolean are stored either as null (no val yet), "true", or "false" (Strings)
		Object val = get(config_key);
		if (val == null) return null;
		if( val instanceof Boolean) return (Boolean)val;
		return val.equals("true") ? Boolean.TRUE : Boolean.FALSE;
	}
	
	public Float getFloatEntry(String config_key) {
		Object val = get(config_key);
		Float floatValue = null;
		if (val == null) {
			floatValue = null;
		} else if( val instanceof Float) {
			floatValue = (Float)val;
		} else if( val instanceof String) {
			try {
				floatValue = Float.valueOf((String)val);
			} catch(NumberFormatException e) {
				//
			}
		}
		return floatValue;
	}

	/**
	 * 
	 * @param config_key
	 * @return false if the key is "false" or does not exist, true otherwise
	 */
	public boolean getBooleanSafe(String config_key) {
		Boolean b = getBooleanEntry(config_key);
		return (b == null? false : b.booleanValue());
	}

	public boolean getBooleanSafe(String config_key, boolean defaultvalue) {
		Boolean b = getBooleanEntry(config_key);
		return (b == null? defaultvalue : b.booleanValue());
	}
	
	public int getIntegerSafe(String configKey, int defaultValue) {
		// ints are stored as Integer
		Object val = get(configKey);
		int intVal = defaultValue;
		if(val instanceof Number) {
			intVal = ((Number)val).intValue();
		} else if(val instanceof String) {
			String stringVal = (String)val;
			if(StringHelper.containsNonWhitespace(stringVal)) {
				try {
					intVal = Integer.parseInt(stringVal);
				} catch(NumberFormatException e) {
					//
				}
			}
		}
		return intVal;
	}
	
	public void setIntValue(String config_key, int value) {
		set(config_key, Integer.valueOf(value));
	}

	/**
	 * Set a string value to the config
	 * @param config_key
	 * @param value
	 */
	public void setStringValue(String config_key, String value) {
		set(config_key, value);
	}
	
	/**
	 * Get a string value from the config. Returns false when the config key does not exist
	 * @param config_key
	 * @return
	 */
	public String getStringValue(String config_key) {
		return (String) get(config_key);
	}

	/**
	 * Get a string value from the config. Returns the defaultValue when the
	 * config key does not exist
	 * 
	 * @param config_key
	 * @param defaultValue
	 * @return
	 */
	public String getStringValue(String config_key, String defaultValue) {
		String value = getStringValue(config_key);
		if (value == null) {
			value = defaultValue;
		}
		return value;
	}

	/**
	 * @param config_key
	 * @param value
	 */
	public void setBooleanEntry(String config_key, boolean value) {
		// boolean are stored either as null (no val yet), "true", or "false" (Strings)
		String val = (value? "true" : "false");
		set(config_key, val);
	}
	
	public Date getDateValue(String config_key) {
		Object val = get(config_key);
		Date value = null;
		if(val instanceof Date) {
			value = (Date)val;
		} else if(val instanceof String) {
			try {
				if(StringHelper.containsNonWhitespace((String)val)) {
					value = Formatter.parseDatetimeFilesystemSave((String)val);
				}
			} catch (ParseException e) {
				log.warn("Cannot convert to date: " + val, e);
			}
		}
		return value;
	}
	
	public void setDateValue(String config_key, Date value) {
		if(value == null) {
			remove(config_key);
		} else {
			String val = Formatter.formatDatetimeFilesystemSave(value);
			set(config_key, val);
		}
	}
	
	public <U> List<U> getList(String config_key, @SuppressWarnings("unused") Class<U> cl) {
		@SuppressWarnings("unchecked")
		List<U> list = (List<U>)get(config_key);
		if(list == null) {
			list = new ArrayList<>();
		}
		return list;
	}
	
	public void setList(String config_key, List<?> list) {
		if(list == null) {
			remove(config_key);
		} else {
			set(config_key, list);
		}
	}
	
	public boolean has(String configKey) {
		return get(configKey) != null;
	}
	
	public boolean anyTrue(String... keys) {
		if(keys != null && keys.length > 0) {
			for(int i=keys.length; i-->0; ) {
				if(getBooleanSafe(keys[i])) {
					return true;
				}
			}
		}
		return false;
	}
	
	public boolean hasAnyOf(String... keys) {
		if(keys != null && keys.length > 0) {
			for(int i=keys.length; i-->0; ) {
				if(get(keys[i]) != null) {
					return true;
				}
			}
		}
		return false;
	}
	

	/** 
	 * Get the version of this module configuration. The version specifies which 
	 * configuration attributes are available for this course node.
	 * If no version has been set so far version=1 will be returned
	 * @return integer representing the version
	 */
	public int getConfigurationVersion() {
		Integer version = (Integer) get(CONFIG_VERSION);
		if (version == null) {
			version = Integer.valueOf(1);
			set(CONFIG_VERSION, version);
		}
		return version.intValue();
	}
	
	/**
	 * Set the configuration version to a specific value
	 * @param version
	 */
	public void setConfigurationVersion(int version) {
		set(CONFIG_VERSION, Integer.valueOf(version));
	}
}