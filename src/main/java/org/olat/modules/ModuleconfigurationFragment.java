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

import java.util.Date;
import java.util.List;

import org.olat.modules.ModuleProperty.ModulePropertyValue;

/**
 * A sub-section of a standard module configuration. This is particularly 
 * useful in case a block of key/value pairs would be repeated, with a
 * different prefix.
 * 
 * <p>Initial date: May 6, 2016
 * @author lmihalkovic, http://www.frentix.com
 */
public class ModuleconfigurationFragment implements IModuleConfiguration {

	private final ModuleConfiguration config;
	private final String fragmentName;
	private final String sep; 
	
	protected ModuleconfigurationFragment(String fragmentName, String separator, ModuleConfiguration config) {
		this.config = config;
		this.fragmentName = fragmentName;
		this.sep = separator == null ? "" : separator;
	}
	
	protected final String key(String configKey) {
		return fragmentName + sep + configKey;
	}

	@Override
	public boolean getBooleanSafe(String configKey) {
		return config.getBooleanSafe(key(configKey));
	}

	@Override
	public void setBooleanEntry(String configKey, boolean value) {
		config.setBooleanEntry(key(configKey), value);
	}

	@Override
	public void set(String configKey, Object value) {
		config.set(key(configKey), value);
	}

	@Override
	public Object get(String configKey) {
		return config.get(key(configKey));
	}

	public <U> List<U> getList(String configKey, Class<U> cl) {
		return config.getList(key(configKey), cl);
	}

	@Override
	public boolean anyTrue(ModuleProperty<Boolean> key) {
		return _anyTrue(key);
	}
	@Override
	public boolean anyTrue(ModuleProperty<Boolean> key1, ModuleProperty<Boolean> key2) {
		return _anyTrue(key1, key2);
	}
	@Override
	public boolean anyTrue(ModuleProperty<Boolean> key1, ModuleProperty<Boolean> key2, ModuleProperty<Boolean> key3) {
		return _anyTrue(key1, key2, key3);
	}
	@Override
	public boolean anyTrue(ModuleProperty<Boolean> key1, ModuleProperty<Boolean> key2, ModuleProperty<Boolean> key3, ModuleProperty<Boolean> key4) {
		return _anyTrue(key1, key2, key3, key4);
	}
	@Override
	public boolean anyTrue(ModuleProperty<Boolean> key1, ModuleProperty<Boolean> key2, ModuleProperty<Boolean> key3, ModuleProperty<Boolean> key4, ModuleProperty<Boolean> key5) {
		return _anyTrue(key1, key2, key3, key4, key5);
	}
	@SafeVarargs
	protected final boolean _anyTrue(ModuleProperty<Boolean>... keys) {
		boolean rc = false;
		for(ModuleProperty<Boolean> key : keys) {
			rc = getBooleanSafe(key.name());
			if (rc) return true;
		}
		return rc;
	}
	
	
	@Override
	public boolean allTrue(ModuleProperty<Boolean> key) {
		return _allTrue(key);
	}
	@Override
	public boolean allTrue(ModuleProperty<Boolean> key1, ModuleProperty<Boolean> key2) {
		return _allTrue(key1, key2);
	}
	@Override
	public boolean allTrue(ModuleProperty<Boolean> key1, ModuleProperty<Boolean> key2, ModuleProperty<Boolean> key3) {
		return _allTrue(key1, key2, key3);
	}
	@Override
	public boolean allTrue(ModuleProperty<Boolean> key1, ModuleProperty<Boolean> key2, ModuleProperty<Boolean> key3, ModuleProperty<Boolean> key4) {
		return _allTrue(key1, key2, key3, key3);
	}
	@Override
	public boolean allTrue(ModuleProperty<Boolean> key1, ModuleProperty<Boolean> key2, ModuleProperty<Boolean> key3, ModuleProperty<Boolean> key4, ModuleProperty<Boolean> key5) {
		return _allTrue(key1, key2, key3, key4, key5);
	}
	
	@SafeVarargs
	protected final boolean _allTrue(ModuleProperty<Boolean>... keys) {
		for(ModuleProperty<Boolean> key : keys) {
			boolean rc = getBooleanSafe(key.name());
			if (!rc) return false;
		}
		return true;
	}
	
	@Override
	public <X> ModulePropertyValue<X> get(ModuleProperty<X> key) {
		ModulePropertyValue<X> value = valueOf(key);
		return value;
	}
	
	@Override
	public <X> void set(ModulePropertyValue<X> val) {
		X value = val.val();
		config.set(key(val.name()), value);
	}

	@Override
	public <X> void set(ModuleProperty<X> key, X value) {
		config.set(key(key.name()), value);
	}
	
	@Override
	public boolean hasAnyOf(ModuleProperty<?>... keys) {
		for(ModuleProperty<?> key : keys) {
			ModulePropertyValue<?> val = get(key);
			if (val.isSet()) return true;
		}
		return false;
	}
	
	protected <X> ModulePropertyValue<X> valueOf(ModuleProperty<X> key) {
		Class<X> klass = key.rawType();
		X val = null;
		String name = key.name();
		if(klass == Boolean.class) {			
			Boolean b = (key.hasDefault() ? config.getBooleanSafe(key(name), (boolean)(key.getDefault())) : config.getBooleanEntry(key(name)));
			val = klass.cast(b);
		} else if (klass == Float.class) {
			Float f = config.getFloatEntry(key(name));
			val = klass.cast(f);
		} else if (klass == Integer.class) {
			if(!key.hasDefault()) {
				throw new IllegalArgumentException("Integer keys MUST define a default value");
			}
			Integer i = config.getIntegerSafe(key(name), (int)key.getDefault());
			val = klass.cast(i);
		} else if (klass == Date.class) {
			Date d = config.getDateValue(key(name));
			val = klass.cast(d);
		} else {
			// This is no different than the normal CCE that would happen
			// in the calling code when doing
			//    SomeType val = (SomeType) config.get("keyName");
			val = klass.cast(get(name));
		}		
		return new ModulePropertyValue<X>(val, key);
	}

}
