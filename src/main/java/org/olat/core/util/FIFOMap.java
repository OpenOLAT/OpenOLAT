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

package org.olat.core.util;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Description: <br>
 * Implementation of a first in, first out map to ensure a vague memory limit in
 * a user session
 * 
 * @author Felix Jost
 */
public class FIFOMap<T,U> {
	private int maxsize;
	private LinkedHashMap<T,U> lhm;

	/**
	 * Constructor for FIFOMap.
	 * 
	 * @param maxsize
	 */
	public FIFOMap(int maxsize) {
		this.maxsize = maxsize;
		lhm = new LinkedHashMap<>();
	}

	/**
	 * Put a (key,value) tuple
	 * 
	 * @param key The key
	 * @param value The value
	 * @return The evicted (key,value) tuple if the map exceeded its maximum size, or null
	 */
	public synchronized Map.Entry<T,U> put(T key, U value) {
		lhm.put(key, value);
		if (lhm.size() > maxsize) {
			// removed oldest = 1. in queue
			Iterator<Map.Entry<T,U>> it = lhm.entrySet().iterator();
			Map.Entry<T,U> eldest = it.next();
			it.remove();
			return new AbstractMap.SimpleImmutableEntry<>(eldest);
		}
		return null;
	}

	/**
   * get the value for the supplied key
	 * @param key
	 * @return the value for the supplied key
	 */
	public synchronized U get(T key) {
		return lhm.get(key);
	}

	/**
   * remove a (key,value) tupel
	 * @param key
	 * @return value of removed key
	 */
	public synchronized Map.Entry<T,U> remove(T key) {
		U o = lhm.remove(key);
		return o == null
				? null
				: new AbstractMap.SimpleImmutableEntry<>(key, o);
	}

	/**
	 * @return size of this map
	 */
	public synchronized int size() {
		return lhm.size();
	}
	
	/**
	 * 
	 * @return List of values
	 */
	public synchronized List<U> values() {
		return new ArrayList<>(lhm.values());
	}
	
	public synchronized List<T> keys() {
		return new ArrayList<>(lhm.keySet());
	}

	/**
	 * @return A copy of the entries
	 */
	public synchronized Map<T,U> copyEntries() {
		return new LinkedHashMap<>(lhm);
	}
	
	public synchronized void clear() {
		lhm.clear();
	}
	
	@Override
	public synchronized String toString() {
		return "FIFOMap:" + lhm.size() + ": " + lhm.keySet().toString() + ", super:" + super.toString();
	}

}

