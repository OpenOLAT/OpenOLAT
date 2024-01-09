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

package org.olat.core.util.prefs;

import java.util.List;

/**
 * Initial Date:  05.08.2005 <br>
 * @author Felix
 */
public interface Preferences {

	/**
	 * @param attributedClass
	 * @param key
	 * @return the deserialized object from prefValue or null if none found
	 */
	public Object get(Class<?> attributedClass, String key);
	
	public <U> List<U> getList(Class<?> attributedClass, String key, Class<U> type);
	
	/**
	 * 
	 * @param attributedClass
	 * @param key
	 * @return the deserialized object from prefValue or null if none found
	 */
	public Object get(String attributedClass, String key);

	/**
	 * @param attributedClass
	 * @param key
	 * @param defaultValue the value returned if no such config exists
	 * @return the deserialized object from prefValue or defaultValue
	 */
	public Object get(Class<?> attributedClass, String key, Object defaultValue);

	/**
	 * persist new Object value with given attrClass and key
	 * value will be serialized and updated if it exists otherwise a new entry will be created
	 *  
	 * @param attributedClass
	 * @param key
	 * @param value
	 */
	public void putAndSave(Class<?> attributedClass, String key, Object value);
	
	/**
	 * persist new Object value with given attrClass and key
	 * value will be serialized and updated if it exists otherwise a new entry will be created
	 *
	 * @param attributedClass
	 * @param key
	 * @param value
	 */
	public void putAndSave(String attributedClass, String key, Object value);

	/**
	 * checks if the key is in the preferences collection
	 * use only if you do not have access to the attributed class
	 *
	 * @param string
	 * @return the deserialized pref object or null if nothing found
	 */
	public Object findPrefByKey(String string);
}
