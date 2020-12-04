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
package org.olat.core.util.cache.infinispan;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

import org.infinispan.Cache;
import org.olat.core.logging.OLATRuntimeException;
import org.olat.core.util.cache.CacheWrapper;

/**
 * Description:<br>
 * this class is threadsafe. this is the singleVM implementation of the cachewrapper.
 * it uses an ehcache as its internal cache.
 * <P>
 * Initial Date:  03.10.2007 <br>
 * @author Felix Jost, http://www.goodsolutions.ch
 */
public class InfinispanCacheWrapper<U,V> implements CacheWrapper<U,V> {

	private Cache<U,V> cache;

	/**
	 * @param cache
	 */
	protected InfinispanCacheWrapper(Cache<U,V> cache) {
		this.cache = cache;
	}
	
	@Override
	public int size() {
		return cache.size();
	}

	@Override
	public long maxCount() {
		try {
			return cache.getCacheConfiguration().memory().maxCount();
		} catch (Exception e) {
			return -1l;
		}
	}

	@Override
	public List<U> getKeys() {
		return new ArrayList<>(cache.keySet());
	}

	@Override
	public boolean containsKey(Object key) {
		return cache.containsKey(key);
	}

	// ---- cache get, set, remove
	@Override
	public V get(U key) {
		V elem;
		try {
			elem = cache.get(key);
		} catch (IllegalStateException e) {
			throw new OLATRuntimeException("cache state error for cache " + cache.getName(), e);
		} catch (Exception e) {//don't catch CacheException to be compatible with infinispan 5.2 to 6.0
			throw new OLATRuntimeException("cache error for cache " + cache.getName(), e);
		}
		return elem;
	}

	@Override
	public V remove(Object key) {
		return cache.remove(key);
	}
	
	@Override
	public V update(U key, V value) {
		V reloaded;
		if(cache.containsKey(key)) {
			reloaded = cache.replace(key, value);
		} else {
			reloaded = cache.put(key, value);
		}
		return reloaded;
	}

	@Override
	public V put(U key, V value) {
		return cache.put(key, value);
	}

	@Override
	public V put(U key, V value, int expirationTime) {
		return cache.put(key, value, expirationTime, TimeUnit.SECONDS, expirationTime, TimeUnit.SECONDS);
	}

	@Override
	public V putIfAbsent(U key, V value) {
		return cache.putIfAbsent(key, value);
	}

	@Override
	public V replace(U key, V value) {
		return cache.replace(key, value);
	}

	@Override
	public V computeIfAbsent(U key, Function<? super U, ? extends V> mappingFunction) {
		return cache.computeIfAbsent(key, mappingFunction);
	}

	@Override
	public Iterator<U> iterateKeys() {
		return cache.keySet().iterator();
	}

	@Override
	public void clear() {
		cache.clear();
	}

	@Override
	public void addListener(Object obj) {
		cache.addListener(obj);
	}
}