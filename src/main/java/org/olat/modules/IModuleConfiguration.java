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
package org.olat.modules;

import org.olat.modules.ModuleProperty.ModulePropertyValue;

/**
 * A simple interface for dealing with sets of key/value pairs supporting the following
 * <ul>
 * <li>subsets based on a common key prefix (eg:  sendToUsers, sendToOwners --> prefix: sendTo)
 * <li>strongly typed properties (the definition of "keyName" is associated with a java type)
 * </ul>
 * 
 * <p>Initial date: May 6, 2016
 * @author lmihalkovic, http://www.frentix.com
 */
public interface IModuleConfiguration {

	/**
	 * Factory method for creating a {@link IModuleConfiguration} instance with a given 
	 * property name prefix, backed by a given instance of {@link ModuleConfiguration}.
	 * This method allows a custom prefix/name separator to be specified. Passing {@code null}
	 * as a separator is equivalent to passing an empty string {@code ""}.
	 * 
	 * @param fragmentName
	 * @param sep
	 * @param config
	 * @return
	 */
	public static IModuleConfiguration fragment(String fragmentName, String sep, ModuleConfiguration config) {
		return new ModuleconfigurationFragment(fragmentName, sep, config);
	}

	/**
	 * Factory method for creating a {@link IModuleConfiguration} instance with a given 
	 * property name prefix, backed by a given instance of {@link ModuleConfiguration}.
	 * By default the property name will follow the pattern: prefix_XXXXX, where XXXXX
	 * is the name passed as a parameter to the methods in this interface 
	 * 
	 * 
	 * @param fragmentName
	 * @param config
	 * @return
	 */
	public static IModuleConfiguration fragment(String fragmentName, ModuleConfiguration config) {
		return new ModuleconfigurationFragment(fragmentName, "_", config);
	}

	// ------------------------------------------------------------------------
	
	public default boolean has(String configKey) {
		return get(configKey) != null;
	}

	public default boolean hasAnyOf(String...configKeys) {
		for(String key : configKeys) {
			if (get(key) != null) return true;
		}
		return false;
	}

	public default boolean allTrue(String... configKeys) {
		boolean rc = false;
		for(String key : configKeys) {
			rc = rc & getBooleanSafe(key);
			if (!rc) break;
		}
		return rc;
	}

	public default boolean anyTrue(String... configKeys) {
		boolean rc = false;
		for(String key : configKeys) {
			rc = getBooleanSafe(key);
			if (rc) break;
		}
		return rc;
	}
	

	public boolean getBooleanSafe(String configKey);
	public void setBooleanEntry(String configKey, boolean value);

	public void set(String configKey, Object value);
	public Object get(String configKey);
	@SuppressWarnings("unchecked")
	default public <T> T getAs(String configKey) {
		Object val = get(configKey);
		return val != null ? (T)val : null;
	}

	
	// ------------------------------------------------------------------------
	// Strongly typed API
	
	public <X> ModulePropertyValue<X> get(ModuleProperty<X> key);
	public default <X> X val(ModuleProperty<X> key) {
		return get(key).val();
	}
	public <X> void set(ModulePropertyValue<X> value);
	public <X> void set(ModuleProperty<X> key, X value);

	public default boolean has(ModuleProperty<?> key) {
		ModulePropertyValue<?> val = get(key);
		return val.isSet();
	}

	public boolean hasAnyOf(ModuleProperty<?>...keys);

	public boolean anyTrue(ModuleProperty<Boolean> key);
	public boolean anyTrue(ModuleProperty<Boolean> key1, ModuleProperty<Boolean> key2);
	public boolean anyTrue(ModuleProperty<Boolean> key1, ModuleProperty<Boolean> key2, ModuleProperty<Boolean> key3);
	public boolean anyTrue(ModuleProperty<Boolean> key1, ModuleProperty<Boolean> key2, ModuleProperty<Boolean> key3, ModuleProperty<Boolean> key4);
	public boolean anyTrue(ModuleProperty<Boolean> key1, ModuleProperty<Boolean> key2, ModuleProperty<Boolean> key3, ModuleProperty<Boolean> key4, ModuleProperty<Boolean> key5);
	
	public boolean allTrue(ModuleProperty<Boolean> key);
	public boolean allTrue(ModuleProperty<Boolean> key1, ModuleProperty<Boolean> key2);
	public boolean allTrue(ModuleProperty<Boolean> key1, ModuleProperty<Boolean> key2, ModuleProperty<Boolean> key3);
	public boolean allTrue(ModuleProperty<Boolean> key1, ModuleProperty<Boolean> key2, ModuleProperty<Boolean> key3, ModuleProperty<Boolean> key4);
	public boolean allTrue(ModuleProperty<Boolean> key1, ModuleProperty<Boolean> key2, ModuleProperty<Boolean> key3, ModuleProperty<Boolean> key4, ModuleProperty<Boolean> key5);

}
