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
 * software distributed under the License is distributed on an "AS IS" BASIS,
 * <br>
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
 * See the License for the specific language governing permissions and <br>
 * limitations under the License.
 * <p>
 * Copyright (c) since 2004 at Multimedia- & E-Learning Services (MELS),<br>
 * University of Zurich, Switzerland.
 * <p>
 */
package ch.goodsolutions.demoextension;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.olat.core.util.prefs.Preferences;

/**
 * Description:<br>
 * TODO: Felix Class Description for RamPreferences
 * 
 * <P>
 * Initial Date:  13.10.2006 <br>
 * @author Felix
 */
public class RamPreferences implements Preferences {
	private Map store = new HashMap();
	
	
	/**
	 * @see org.olat.core.util.prefs.Preferences#get(java.lang.Class, java.lang.String)
	 */
	public Object get(Class attributedClass, String key) {
		return store.get(getCompoundKey(attributedClass, key));
	}
	
	/**
	 * @see org.olat.core.util.prefs.Preferences#get(java.lang.Class, java.lang.String, java.lang.Object)
	 */
	public Object get(Class attributedClass, String key, Object defaultValue) {
		Object value = get(attributedClass, key);
		if (value == null) return defaultValue;
		return value;
	}

	/**
	 * @see org.olat.core.util.prefs.Preferences#put(java.lang.Class, java.lang.String, java.lang.Object)
	 */
	public void put(Class attributedClass, String key, Object value) {
		store.put(getCompoundKey(attributedClass, key), value);
	}

	/**
	 * @see org.olat.core.util.prefs.Preferences#putAndSave(java.lang.Class, java.lang.String, java.lang.Object)
	 */
	public void putAndSave(Class attributedClass, String key, Object value) {
		put(attributedClass, key, value);
		save();

	}

	/**
	 * @see org.olat.core.util.prefs.Preferences#save()
	 */
	public void save() {
		// nothing to do for ram

	}
	
	private String getCompoundKey(Class attributedClass, String key) {
		return attributedClass.getName()+":"+key;
	}
	
	/**
	 * 
	 * @see org.olat.core.util.prefs.Preferences#findPrefByKey(java.lang.String)
	 */
	public Object findPrefByKey(String partOfKey) {
		for (Iterator iterator = store.keySet().iterator(); iterator.hasNext();) {
			String key = (String) iterator.next();
			if (key.endsWith(partOfKey)) return store.get(key);
		}
		return null;
	}

}
