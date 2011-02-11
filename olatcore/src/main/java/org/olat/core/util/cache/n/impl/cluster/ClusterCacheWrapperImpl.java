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
package org.olat.core.util.cache.n.impl.cluster;

import java.io.Serializable;
import java.util.Map;

import org.olat.core.util.cache.n.CacheConfig;
import org.olat.core.util.cache.n.CacheWrapper;
import org.olat.core.util.cache.n.impl.svm.CacheWrapperImpl;

/**
 * Description:<br>
 * cluster implementation of the cache wrapper.
 * after put or delete, it notifies the other caches which represent the 
 * same cache on the other olat cluster nodes by sending an invalidating event.
 * those other caches will then remove the invalidated key/value pairs from its cache,
 * so that an access to it forces a refetching from the primary sources. 
 * 
 * <P>
 * Initial Date:  23.10.2007 <br>
 * @author Felix Jost, http://www.goodsolutions.ch
 */
public class ClusterCacheWrapperImpl extends CacheWrapperImpl  {
	private final ClusterCacher clusterCacher;
	
	/**
	 * @param cacheName
	 * @param config
	 */
	ClusterCacheWrapperImpl(ClusterCacher cacher, String cacheName, CacheConfig config) {
		super(cacheName, config);
		this.clusterCacher = cacher;
	}
	
	@Override
	protected CacheWrapper createChildCacheWrapper(String childName, CacheConfig config) {
		return new ClusterCacheWrapperImpl(clusterCacher, childName, config);
	}
	
	private void afterChanged(String[] keys) {
		clusterCacher.sendChangedKeys(getCacheName(), keys);
	}
	
	@Override
	public void update(String key, Serializable value) {
		super.update(key, value);
		afterChanged(new String[]{key});
	}
	
	// no need to override putSilent - since no notifications are sent
	
	@Override
	public void updateMulti(String[] keys, Serializable[] values) {
		super.updateMulti(keys, values);
		afterChanged(keys);
	}

	@Override
	public void remove(String key) {
		super.remove(key);
		// always notify the other nodes, even if the key was not there - we cannot know (we would have to introduce flags per key) why it was not there (expired or invalidated)
		afterChanged(new String[]{key});
	}
	
	ClusterCacheWrapperImpl getChildWithName(String childName) {
		Map<String, CacheWrapper> children = getChildren();
		return children == null? null : (ClusterCacheWrapperImpl) children.get(childName);
	}

	void invalidateKeys(String[] keys) {
		// we simply delete the entries with the keys, so that the client of the cache 
		// will receive a null value upon new request and refill the cache if needed
		for (String key : keys) {
			// call the super implementation, because no further notification is needed (the key was removed due to a message from an other cache)
			super.remove(key);
		}
	}
	
}
