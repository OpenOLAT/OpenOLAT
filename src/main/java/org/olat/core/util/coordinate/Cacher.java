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
package org.olat.core.util.coordinate;

import org.infinispan.manager.EmbeddedCacheManager;
import org.olat.core.util.cache.CacheWrapper;

/**
 * Description:<br>
 * interface to obtain caches.
 * a cache can hold key+serializable pairs of data and internally has a max time-to-live, max-idle-time, and a max-elements settings.
 * to free the developer from choosing the ttl, max-idle and max-elements settings, default values for a class should be configured in the file
 * olatcoreconfig.xml (-not- in ehcache.xml)
 * <br>
 * How to obtain the name of a cache for configuring it in the olatcoreconfig.xml file:<br>
 * The name of a cache consists of<br>
 * 1. the class name together with the subkey if given<br>
 * 2. for each subcache, the olatresourceable's type is converted to string form and appended to the cache name.<br>
 * e.g. com.mystuff.MyClass:myname@org.olat.repository.RepositoryEntry and further "@" with further subtypes for further subcaches created hereof.
 * <br><br>
 * the idea of the hierarchy is to represent the dependencies (=composition) between objects cached. e.g. a cached wikipage should be obtained using a
 * cache created from the wiki cache, since the wikipage can only exist as long as the wiki exists.
 * <P>
 * Initial Date:  15.10.2007 <br>
 * @author Felix Jost, http://www.goodsolutions.ch
 */
public interface Cacher {
	
	/**
	 * olat:::: change ownerClass to Manager, introduce Manager interface (marker interface).
	 * prevent Controllers from using this cache.
	 * <br>to set the max-time-to-live, max-idle-time and max-elements values, edit the file olatcoreconfig.xml.<br>
	 * For the exact meaning of these three parameters, consult the ehcache-1.3.0-documentation.
	 *  
	 * @param coOwnerClass the class the cache "belongs to" (the (manager) class that is responsible for managing the cache)
	 * @param name an optional name to be able to create more than one cache for the same coOwnerClass
	 * @return the CacheWrapper to use for caching and/or for creating subcaches
	 */
	public <U, V> CacheWrapper<U, V> getCache(String type, String name);
	
	public EmbeddedCacheManager getCacheContainer();
	
}
