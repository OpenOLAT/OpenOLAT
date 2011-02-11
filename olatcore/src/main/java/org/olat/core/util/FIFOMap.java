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
* Copyright (c) 1999-2006 at Multimedia- & E-Learning Services (MELS),<br>
* University of Zurich, Switzerland.
* <p>
*/ 

package org.olat.core.util;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Set;

/**
 * Description: <br>
 * Implementation of a first in, first out map to ensure a vague memory limit in
 * a user session
 * 
 * @author Felix Jost
 */
public class FIFOMap {
	private int maxsize;
	private LinkedHashMap lhm;

	/**
	 * Constructor for FIFOMap.
	 * 
	 * @param maxsize
	 */
	public FIFOMap(int maxsize) {
		this.maxsize = maxsize;
		lhm = new LinkedHashMap();
		//Map m = lhm;
	}

	/**
	 * put a (key,value) tupel
	 * 
	 * @param key
	 * @param value
	 */
	public void put(Object key, Object value) {
		lhm.put(key, value);
		if (lhm.size() > maxsize) {
			// removed oldest = 1. in queue
			Iterator it = lhm.keySet().iterator();
			it.next();
			it.remove();
		}
	}

	/**
   * get the value for the supplied key
	 * @param key
	 * @return the value for the supplied key
	 */
	public Object get(Object key) {
		return lhm.get(key);
	}

	/**
   * remove a (key,value) tupel
	 * @param key
	 * @return value of removed key
	 */
	public Object remove(Object key) {
		Object o = lhm.get(key);
		lhm.remove(key);
		return o;
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		FIFOMap fm = new FIFOMap(4);
		for (int i = 0; i < 9; i++) {
			fm.put("a" + i, "aa" + i);
			System.out.println(fm.lhm.entrySet());
		}
		System.out.println("-pop");
		fm.remove("a6");
		System.out.println(fm.lhm.entrySet());
		fm.remove("a8");
		System.out.println(fm.lhm.entrySet());
		fm.remove("a1");
		System.out.println(fm.lhm.entrySet());
		fm.remove("a5");
		fm.put("a5", "bb6");
		System.out.println(fm.lhm.entrySet());
		System.out.println("get " + fm.get("a7"));
		System.out.println(fm.lhm.entrySet());
	}

	/**
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		//return "FIFOMap:"+lhm.size()+":
		// "+lhm.keySet().toString()+"="+lhm.entrySet().toString()+",
		// super:"+super.toString();
		return "FIFOMap:" + lhm.size() + ": " + lhm.keySet().toString() + ", super:" + super.toString();
	}

	/**
	 * @return size of this map
	 */
	public int size() {
		return lhm.size();
	}

	/**
	 * @return ordered set of keys
	 */
	public Set getOrderedKeySet() {
		return lhm.keySet();
	}

	/**
	 * @return value iterator
	 */
	public Iterator getValueIterator() {
		return lhm.values().iterator();
	}

}

