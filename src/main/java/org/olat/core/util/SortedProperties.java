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
package org.olat.core.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;
import java.util.TreeMap;

/**
 * Description:<br>
 * This class extends the java Properties class to store the properties in a
 * sorted manner. This is usefull when the properties are saved in a CVS where
 * the ordering is needed to see meaninfull diffs between check ins.
 * <p>
 * This code is as based on the code from the findbugs project:
 * <p>
 * http://findbugs.sourceforge.net/api/edu/umd/cs/findbugs/config/
 * SortedProperties.html
 * <br>
 * http://code.google.com/p/findbugs/source/browse/trunk/findbugs
 * /src/java/edu/umd/cs/findbugs/config/SortedProperties.java?r=10575
 * 
 * <P>
 * Initial Date: 12.02.2009 <br>
 * 
 * @author gnaegi
 */
public class SortedProperties extends Properties {

	private static final long serialVersionUID = -5930211273975255180L;

	/**
	 * Override to be able to write properties sorted by keys
	 * to the disk (only Java 8)
	 * 
	 * @see java.util.Hashtable#keys()
	 */
	@SuppressWarnings({ "unchecked" })
	@Override
	public synchronized Enumeration<Object> keys() {
		// sort elements based on detector (prop key) names
		Set set = keySet();
		return sortKeys(set);
	}
	
	/**
	 * Override to be able to write properties sorted by keys
	 * to the disk (Java 11)
	 */
	@Override
	public Set<Entry<Object, Object>> entrySet() {
		TreeMap<Object,Object> map = new TreeMap<>();
		for(Object propertyName:keySet()) {
			map.put(propertyName, get(propertyName));
		}		
		return map.entrySet();
	}

	/**
	 * To be compatible with version control systems, we need to sort properties
	 * before storing them to disk. Otherwise each change may lead to problems by
	 * diff against previous version - because Property entries are randomly
	 * distributed (it's a map).
	 * 
	 * @param keySet non null set instance to sort
	 * @return non null list wich contains all given keys, sorted
	 *         lexicographically. The list may be empty if given set was empty
	 */
	public static Enumeration<?> sortKeys(Set<String> keySet) {
		List<String> sortedList = new ArrayList<>();
		sortedList.addAll(keySet);
		Collections.sort(sortedList);
		return Collections.enumeration(sortedList);
	}

}
