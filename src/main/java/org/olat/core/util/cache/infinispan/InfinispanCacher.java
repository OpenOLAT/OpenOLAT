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

import org.infinispan.Cache;
import org.infinispan.configuration.cache.Configuration;
import org.infinispan.configuration.cache.ConfigurationBuilder;
import org.infinispan.configuration.cache.StorageType;
import org.infinispan.eviction.EvictionStrategy;
import org.infinispan.manager.EmbeddedCacheManager;
import org.infinispan.transaction.TransactionMode;
import org.infinispan.util.concurrent.IsolationLevel;
import org.olat.core.util.cache.CacheWrapper;
import org.olat.core.util.coordinate.Cacher;

/**
 * Description:<br>
 * single java vm implementation of the cacher interface
 * 
 * <P>
 * Initial Date:  16.10.2007 <br>
 * @author Felix Jost, http://www.goodsolutions.ch
 */
public class InfinispanCacher implements Cacher {
	
	private EmbeddedCacheManager cacheManager;
	
	public InfinispanCacher(EmbeddedCacheManager cacheManager) {
		this.cacheManager = cacheManager;
	}
	
	@Override
	public EmbeddedCacheManager getCacheContainer() {
		return cacheManager;
	}

	@Override
	public <U, V> CacheWrapper<U, V> getCache(String type, String name) {
		String cacheName = type + "-" + name;
		if(!cacheManager.cacheExists(cacheName)) {
			createInfinispanConfiguration(cacheName);
		}
		
		Cache<U, V> cache = cacheManager.getCache(cacheName);
		return new InfinispanCacheWrapper<>(cache);
	}
	
	private void createInfinispanConfiguration(String cacheName) {	
		Configuration conf = cacheManager.getCacheConfiguration(cacheName);
		if(conf == null) {
			long maxEntries = 10000;
			long maxIdle = 900000l;
	
			ConfigurationBuilder builder = new ConfigurationBuilder();
			builder.memory()
				.whenFull(EvictionStrategy.REMOVE)
				.storage(StorageType.HEAP)
				.maxCount(maxEntries);
			builder.expiration()
				.maxIdle(maxIdle);
			builder.transaction()
				.transactionMode(TransactionMode.NON_TRANSACTIONAL);
			builder.locking()
				.concurrencyLevel(1000)
				.useLockStriping(false)
				.lockAcquisitionTimeout(15000)
				.isolationLevel(IsolationLevel.READ_COMMITTED);
			builder.statistics()
				.enable();
			builder.encoding()
				.mediaType("application/x-java-object");

			Configuration configurationOverride = builder.build();
			cacheManager.defineConfiguration(cacheName, configurationOverride);
		}
	}	
}