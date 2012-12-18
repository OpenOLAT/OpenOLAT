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
package org.olat.core.util.cache.n;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import org.infinispan.Cache;
import org.infinispan.CacheException;
import org.infinispan.configuration.cache.Configuration;
import org.infinispan.configuration.cache.ConfigurationBuilder;
import org.infinispan.eviction.EvictionStrategy;
import org.infinispan.manager.EmbeddedCacheManager;
import org.infinispan.transaction.TransactionMode;
import org.olat.core.id.OLATResourceable;
import org.olat.core.logging.OLATRuntimeException;
import org.olat.core.logging.OLog;
import org.olat.core.logging.Tracing;
import org.olat.core.util.resource.OresHelper;

/**
 * Description:<br>
 * this class is threadsafe. this is the singleVM implementation of the cachewrapper.
 * it uses an ehcache as its internal cache.
 * <P>
 * Initial Date:  03.10.2007 <br>
 * @author Felix Jost, http://www.goodsolutions.ch
 */
public class InfinispanCacheWrapper implements CacheWrapper<Object> {
	private static final OLog log = Tracing.createLoggerFor(InfinispanCacheWrapper.class);
	
	private final String cacheName;	// the fully qualified name of the cache
	private final CacheConfig config;
	private final EmbeddedCacheManager cachemanager;
	
	private Cache<Object,Serializable> cache;
	private Map<String, CacheWrapper<Object>> children = null;
	
	
	/**
	 * @param cache
	 */
	protected InfinispanCacheWrapper(String cacheName, CacheConfig config, EmbeddedCacheManager cachemanager) {
		this.cachemanager = cachemanager;
		this.cacheName = cacheName;
		this.config = config;
		try {
			// now we need a cache which has appropriate (by configuration) values for cache configs such as ttl, tti, max elements and so on.
			// next line needed since cache can also be initialized through ehcache.xml
			if(!cachemanager.cacheExists(cacheName)) {
				Configuration conf = cachemanager.getCacheConfiguration(cacheName);
				if(conf == null) {
					ConfigurationBuilder builder = new ConfigurationBuilder();
					builder.eviction().strategy(EvictionStrategy.LRU);
					builder.eviction().maxEntries(10000);
					builder.expiration().maxIdle(900000);
					builder.transaction().transactionMode(TransactionMode.NON_TRANSACTIONAL);
					builder.dataContainer().storeAsBinary().storeValuesAsBinary(false);
					Configuration configurationOverride = builder.build();
					cachemanager.defineConfiguration(cacheName, configurationOverride);
				}
			}
			cache = cachemanager.getCache(cacheName);
		} catch (Exception e) {
			log.error("", e);
		}
	}
	
	/**
	 * creates a new child instance. must be overridden by subclasses
	 * @param childName
	 * @param config
	 * @return
	 */
	protected CacheWrapper<Object> createChildCacheWrapper(String childName, CacheConfig aconfig) {
		return new InfinispanCacheWrapper(childName, aconfig, cachemanager);
	}

	/**
	 * 
	 * @return the map with the children or null
	 */
	protected Map<String, CacheWrapper<Object>> getChildren() {
		return children;
	}
	
	public CacheWrapper getOrCreateChildCacheWrapper(OLATResourceable ores) {
		String childName = OresHelper.createStringRepresenting(ores).replace(":", "_");
		String fullcacheName = cacheName + "@" + childName;
		synchronized(this) {//cluster_ok by definition of this class as used in single vm
			CacheWrapper cwChild = null;
			if (children == null) {
				children = new HashMap<String, CacheWrapper<Object>>();
			} else {
				cwChild = children.get(childName);
			}
			
			if (cwChild == null) { // not found yet
				cwChild = createChildCacheWrapper(fullcacheName, config.createConfigFor(ores));
				children.put(childName, cwChild);
			}
			return cwChild;
		}
	}
	
	@Override
	public int size() {
		return cache.size();
	}

	@Override
	public boolean containsKey(Object key) {
		return cache.containsKey(key);
	}

	// ---- cache get, set, remove
	public Serializable get(Object key) {
		Object elem;
		try {
			synchronized (cache) {//cluster_ok by definition of this class as used in single vm
				elem = cache.get(key);				
			}
		} catch (IllegalStateException e) {
			throw new OLATRuntimeException("cache state error for cache "+cacheName, e);
		} catch (CacheException e) {
			throw new OLATRuntimeException("cache error for cache "+cacheName, e);
		}
		return (Serializable)elem;
	}

	public void remove(Object key) {
		synchronized (cache) {//cluster_ok by definition of this class as used in single vm
			cache.remove(key);
		}
	}
	
	public void update(Object key, Serializable value) {
		// update is the same as put for the singlevm mode
		synchronized (cache) {//cluster_ok by definition of this class as used in single vm
			if(cache.containsKey(key)) {
				cache.replace(key, value);
			} else {
				cache.put(key, value);
			}
		}	
	}

	public void put(Object key, Serializable value) {
		// put is the same as update for the singlevm mode
		synchronized (cache) {//cluster_ok by definition of this class as used in single vm
			cache.put(key, value);
		}
	}

	
	protected String getCacheName() {
		return cacheName;
	}
	
}
