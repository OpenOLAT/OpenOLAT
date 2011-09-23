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
* Copyright (c) 1999-2007 at Multimedia- & E-Learning Services (MELS),<br>
* University of Zurich, Switzerland.
* <p>
*/
package org.olat.core.util.cache.n.impl.svm;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheException;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Element;

import org.olat.core.id.OLATResourceable;
import org.olat.core.logging.OLATRuntimeException;
import org.olat.core.logging.OLog;
import org.olat.core.logging.Tracing;
import org.olat.core.util.cache.n.CacheConfig;
import org.olat.core.util.cache.n.CacheWrapper;
import org.olat.core.util.resource.OresHelper;

/**
 * Description:<br>
 * this class is threadsafe. this is the singleVM implementation of the cachewrapper.
 * it uses an ehcache as its internal cache.
 * <P>
 * Initial Date:  03.10.2007 <br>
 * @author Felix Jost, http://www.goodsolutions.ch
 */
public class CacheWrapperImpl implements CacheWrapper {
	private static final OLog log = Tracing.createLoggerFor(CacheWrapperImpl.class);
	
	private final String cacheName;	// the fully qualified name of the cache
	private final CacheConfig config;
	private final CacheManager cachemanager;
	
	private Cache cache;
	private Map<String, CacheWrapper> children = null;
	
	
	/**
	 * @param cache
	 */
	protected CacheWrapperImpl(String cacheName, CacheConfig config) {
		this.cacheName = cacheName;
		this.config = config;
		this.cachemanager = CacheManager.getInstance();
		// now we need a cache which has appropriate (by configuration) values for cache configs such as ttl, tti, max elements and so on.
		// next line needed since cache can also be initialized through ehcache.xml
		if (cachemanager.cacheExists(cacheName)) {
			this.cache = cachemanager.getCache(cacheName);
			log.warn("using cache parameters from ehcache.xml for cache named '"+cacheName+"'");
		} else {
			this.cache = config.createCache(cacheName);
			try {
				cachemanager.addCache(this.cache);
			}
			catch (CacheException e) {
				throw new OLATRuntimeException("Problem when initializing the caches", e);
			}
		}
	}
	
	/**
	 * creates a new child instance. must be overridden by subclasses
	 * @param childName
	 * @param config
	 * @return
	 */
	protected CacheWrapper createChildCacheWrapper(String childName, CacheConfig aconfig) {
		return new CacheWrapperImpl(childName, aconfig);
	}

	/**
	 * 
	 * @return the map with the children or null
	 */
	protected Map<String, CacheWrapper> getChildren() {
		return children;
	}
	
	public CacheWrapper getOrCreateChildCacheWrapper(OLATResourceable ores) {
		String childName = OresHelper.createStringRepresenting(ores).replace(":", "_");
		String fullcacheName = cacheName + "@" + childName;
		synchronized(this) {//cluster_ok by definition of this class as used in single vm
			CacheWrapper cwChild = null;
			if (children == null) {
				children = new HashMap<String, CacheWrapper>();
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
	
	// ---- cache get, set, remove
	public Serializable get(String key) {
		Element elem;
		try {
			synchronized (cache) {//cluster_ok by definition of this class as used in single vm
				elem = cache.get(key);				
			}
		} catch (IllegalStateException e) {
			throw new OLATRuntimeException("cache state error for cache "+cache.getName(), e);
		} catch (CacheException e) {
			throw new OLATRuntimeException("cache error for cache "+cache.getName(), e);
		}
		return elem == null? null : elem.getValue();
	}

	public void remove(String key) {
		synchronized (cache) {//cluster_ok by definition of this class as used in single vm
			cache.remove(key);
		}
	}
	
	public void update(String key, Serializable value) {
		// update is the same as put for the singlevm mode
		doPut(key, value);
	}
	
	private void doPut(String key, Serializable value) {
		Element element = new Element(key, value);
		synchronized (cache) {//cluster_ok by definition of this class as used in single vm
			cache.put(element);
		}		
	}

	public void put(String key, Serializable value) {
		// put is the same as update for the singlevm mode
		doPut(key, value);
	}

	public void updateMulti(String[] keys, Serializable[] values) {
		int len = keys.length;
		synchronized (cache) {//cluster_ok by definition of this class as used in single vm
			for (int i = 0; i < len; i++) {
				Element element = new Element(keys[i], values[i]);				
				cache.put(element);
			}
		}
	}
	
	protected String getCacheName() {
		return cacheName;
	}
	
}
