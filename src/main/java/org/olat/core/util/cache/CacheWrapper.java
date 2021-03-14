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
package org.olat.core.util.cache;

import java.util.Iterator;
import java.util.List;
import java.util.function.Function;


/**
 * Description:<br>
 * Facade to the underlying cache.
 * 
 * 
 * 
 * <P>
 * Initial Date:  03.10.2007 <br>
 * @author Felix Jost, http://www.goodsolutions.ch
 */
public interface CacheWrapper<U, V> {
	
	
	public boolean containsKey(U key);

	/**
	 * 
	 * @param key the cache for the cache entry
	 * @return the cache entry or null when the element has expired, never been put into yet, or removed due to max-size, 
	 * 	or a put in a different cluster node which led to an invalidate message
	 */
	public V get(U key);
	
	/**
	 * o_clusterREVIEW :pb  review references
	 * 
	 * 
	 * puts a value in the cache. this method is thread-safe<br>
	 * Use this method if you generate new data (or change existing data) that cannot be known to other nodes yet.
	 * @see public void putSilent(String key, Serializable value);
	 * 
	 * @param key
	 * @param value
	 */
	public V update(U key, V value);	
	
	/**
	 * use this put whenever you just fill up a cache from data which is already on the db or the filesystem. e.g. use it when you simply load some properties again into cache.
	 * 
	 * e.g.
	 * 
	 * <pre>
		CacheWrapper cw = aCache.getOrCreateChildCacheWrapper(ores);
		synchronized(cw) {
			   String data = (String) cw.get(FULLUSERSET);
			if (data == null) {
				// cache entry has expired or has never been stored yet into the cache.
				// or has been invalidated in cluster mode
				data = loadDataFromDiskWeDidNotChangeAnythingButSimplyNeedTheDataAgain(...);
				cw.putSilent(FULLUSERSET, data);
			}
			return data;
		}
	 </pre>
	 * @param key
	 * @param value
	 *
	 */
	public V put(U key, V value);
	
	/**
	 * Same as above but with an explicit expiration in seconds.
	 * 
	 * @param key The key
	 * @param value	The value
	 * @param expirationTime The expiration time in seconds
	 * @return The value
	 */
	public V put(U key, V value, int expirationTime);
	
	
	public V putIfAbsent(U key, V value);
	
	public V replace(U key, V value);
	
	public V computeIfAbsent(U key, Function<? super U, ? extends V> mappingFunction);
	
	/**
	 * In the case of distributed cache, the list can be partial and
	 * you must carefully setup your cache.
	 * @return
	 */
	public List<U> getKeys();
	
	/**
	 * removes a value from the cache. this method is thread-safe
	 * @param key
	 */
	public V remove(U key);
	
	/**
	 * Return the size of the cache, in the case of a distributed
	 * cache, read carefully the documentation of the implementation
	 * of the cache.
	 * @return
	 */
	public int size();
	
	public long maxCount();
	
	/**
	 * This can be dangerous
	 * @return
	 */
	public Iterator<U> iterateKeys();
	
	public void clear();
	
	/**
	 * Annotated listener
	 * 
	 * @param obj
	 */
	public void addListener(Object obj);

}
