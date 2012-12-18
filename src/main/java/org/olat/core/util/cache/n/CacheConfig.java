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

import java.util.Map;

import org.olat.core.id.OLATResourceable;
import org.olat.core.util.resource.OresHelper;

/**
 * Description:<br>
 * Holds the configuration of a cache and can create configuration for child caches.
 * the configuration of a cache can be set using spring. a cache configuration can either be valid for a certain instance of child caches,
 * or for a certain type of child caches (e.g. for all "CourseModule" caches. The latter will mostly be used to specify cache settings such as
 * max elements, time-to-idle etc. depending on the business domain and the usage pattern. See coreextconfig.xml for an example
 *  
 * 
 * <P>
 * Initial Date:  19.10.2007 <br>
 * @author Felix Jost, http://www.goodsolutions.ch
 */
public class CacheConfig {
	
	private int maxElementsInMemory = 10000;
	//private MemoryStoreEvictionPolicy memoryStoreEvictionPolicy = MemoryStoreEvictionPolicy.LRU;
	private boolean overflowToDisk = false;
	private boolean diskPersistent = false;
	private String diskStorePath;
	private boolean eternal = false;
	private int timeToLive = 120;
	private int timeToIdle = 120;
	private int diskExpiryThreadIntervalSeconds = 120;
	
	private Map<String, CacheConfig> childrenConfig;
	
	public CacheConfig() {
		// used by spring
	}

	/*
	 * for cloning
	 */
	private CacheConfig(CacheConfig config) {
		this.maxElementsInMemory = config.maxElementsInMemory;
		//this.memoryStoreEvictionPolicy = config.memoryStoreEvictionPolicy;
		this.overflowToDisk = config.overflowToDisk;
		this.diskPersistent = config.diskPersistent;
		this.diskStorePath = config.diskStorePath;
		this.eternal = config.eternal;
		this.timeToLive = config.timeToLive;
		this.timeToIdle = config.timeToIdle;
		this.diskExpiryThreadIntervalSeconds = config.diskExpiryThreadIntervalSeconds;
		
		// no need to deep clone, since read-only config.
		this.childrenConfig = config.childrenConfig;
	}

	/**
	 * Specify the maximum number of cached objects in memory.
	 * Default is 10000 elements.
	 */
	public void setMaxElementsInMemory(int maxElementsInMemory) {
		this.maxElementsInMemory = maxElementsInMemory;
	}

	/**
	 * Set the memory style eviction policy for this cache.
	 * Supported values are "LRU", "LFU" and "FIFO", according to the
	 * constants defined in EHCache's MemoryStoreEvictionPolicy class.
	 * Default is "LRU".
	 */
	/*public void setMemoryStoreEvictionPolicy(MemoryStoreEvictionPolicy memoryStoreEvictionPolicy) {
		this.memoryStoreEvictionPolicy = memoryStoreEvictionPolicy;
	}*/

	/**
	 * Set whether elements can overflow to disk when the in-memory cache
	 * has reached the maximum size limit. Default is "true".
	 */
	public void setOverflowToDisk(boolean overflowToDisk) {
		this.overflowToDisk = overflowToDisk;
	}

	/**
	 * Set the location of temporary files for the disk store of this cache.
	 * Default is the CacheManager's disk store path.
	 */
	public void setDiskStorePath(String diskStorePath) {
		this.diskStorePath = diskStorePath;
	}

	/**
	 * Set whether elements are considered as eternal. If "true", timeouts
	 * are ignored and the element is never expired. Default is "false".
	 */
	public void setEternal(boolean eternal) {
		this.eternal = eternal;
	}

	/**
	 * Set t he time in seconds to live for an element before it expires,
	 * i.e. the maximum time between creation time and when an element expires.
	 * It is only used if the element is not eternal. Default is 120 seconds.
	 */
	public void setTimeToLive(int timeToLive) {
		this.timeToLive = timeToLive;
	}

	/**
	 * Set the time in seconds to idle for an element before it expires, that is,
	 * the maximum amount of time between accesses before an element expires.
	 * This is only used if the element is not eternal. Default is 120 seconds.
	 */
	public void setTimeToIdle(int timeToIdle) {
		this.timeToIdle = timeToIdle;
	}

	/**
	 * Set whether the disk store persists between restarts of the Virtual Machine.
	 * The default is "false".
	 */
	public void setDiskPersistent(boolean diskPersistent) {
		this.diskPersistent = diskPersistent;
	}

	/**
	 * Set the number of seconds between runs of the disk expiry thread.
	 * The default is 120 seconds.
	 */
	public void setDiskExpiryThreadIntervalSeconds(int diskExpiryThreadIntervalSeconds) {
		this.diskExpiryThreadIntervalSeconds = diskExpiryThreadIntervalSeconds;
	}

	/**
	 * [used by spring]
	 * @param childrenConfig
	 */
	public void setChildrenConfig(Map<String, CacheConfig> childrenConfig) {
		this.childrenConfig = childrenConfig;
	}
	
	public CacheConfig createConfigFor(OLATResourceable ores) {
		// we will first try to use the type together with the key for the lookup. if that fails, we lookup the config for the type.
		// if that fails again (= no entries for that type in the spring .xml file) -> 
		// we take a current config (child inherit from the parent if no config is set)
		CacheConfig cc = null;
		if (childrenConfig == null) {
			cc = this;
		} else { // children are configured
			cc = childrenConfig.get(OresHelper.createStringRepresenting(ores));
			if (cc == null) {
				cc = childrenConfig.get(ores.getResourceableTypeName());
			}
			// fallback to current config
			if  (cc == null) {
				cc = this;
			}
		}
		// clone the config, so that it is independent.
		return new CacheConfig(cc);
	}

	/**
	 * Return Cache-name for given class and name
	 * @param ownerClass
	 * @param name        Cache-name 
	 * @return
	 */
	public static String getCacheName(Class<?> ownerClass, String name) {
		String cacheName = ownerClass.getName();
		if (name != null) {
			cacheName = cacheName +"_"+name;
		}
		return cacheName;
	}
}
