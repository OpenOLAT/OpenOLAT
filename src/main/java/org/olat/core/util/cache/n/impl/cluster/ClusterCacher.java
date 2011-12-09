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
package org.olat.core.util.cache.n.impl.cluster;

import java.util.regex.Pattern;

import org.olat.core.configuration.Initializable;
import org.olat.core.gui.control.Event;
import org.olat.core.id.OLATResourceable;
import org.olat.core.logging.AssertException;
import org.olat.core.util.cache.n.CacheConfig;
import org.olat.core.util.cache.n.CacheWrapper;
import org.olat.core.util.coordinate.Cacher;
import org.olat.core.util.event.EventBus;
import org.olat.core.util.event.GenericEventListener;
import org.olat.core.util.resource.OresHelper;
import org.olat.testutils.codepoints.server.Codepoint;

/**
 * Description:<br>
 * cluster implementation of the cacher interface.
 * it uses the event bus to asychronously send invalidating messages to the same caches
 * in the other olat cluster nodes.
 * 
 * 
 * <P>
 * Initial Date:  16.10.2007 <br>
 * @author Felix Jost, http://www.goodsolutions.ch
 */
public class ClusterCacher implements Cacher, GenericEventListener, Initializable {
	private static final OLATResourceable ORES_THIS = OresHelper.createOLATResourceableTypeWithoutCheck(ClusterCacher.class.getName());
	private static final Pattern PAT_DELIM_CACHENAME = Pattern.compile("@");
	
	private ClusterConfig clusterConfig;
	
	private EventBus eventBus;
	private ClusterCacheWrapperImpl rootCacheWrapperImpl;
	private CacheConfig rootConfig;
	
	/**
	 * [used by spring]
	 *
	 */
	public ClusterCacher() {
		//
	}
	
	/**
	 * [used by spring]
	 *
	 */
	public void init() {
		if (rootConfig == null) {
			throw new AssertException("rootConfig property must not be null!");
		}
		rootCacheWrapperImpl = new ClusterCacheWrapperImpl(this, this.getClass().getName(), rootConfig);	
		eventBus.registerFor(this, null, ORES_THIS);
	}
	
	
	public CacheWrapper getOrCreateCache(Class ownerClass, String name) {
		OLATResourceable ores = OresHelper.createOLATResourceableInstanceWithoutCheck(CacheConfig.getCacheName(ownerClass, name), new Long(0));
		return rootCacheWrapperImpl.getOrCreateChildCacheWrapper(ores);
	}



	/* (non-Javadoc)
	 * @see org.olat.core.util.event.GenericEventListener#event(org.olat.core.gui.control.Event)
	 */
	public void event(Event event) {
		Codepoint.codepoint(ClusterCacher.class, "event");
		// we only receive one type of events:
		ClusterCacheWrapperEvent ccwe = (ClusterCacheWrapperEvent)event;
		
		// important: ignore messages which stem from our cache (messages are broadcasted to all cluster nodes, that is also our own.
		// without that, a put (key) would result in that key being invalidated shortly after:
		// a put into a cache is to invalidate this key in all other cache instances
		if (ccwe.getSendingNodeId().equals(clusterConfig.getNodeId())) {
			return;
		}
		
		// cacheName is "fully qualified name" such as
		// "org.olat.core.util.cache.n.impl.svm.SingleVMCacher@org.olat.login.LoginModule_blockafterfailedattempts__0"
		String cacheName = ccwe.getCacheName();
		
		// find the matching child cache by traversing down the cache tree starting from the root.
		ClusterCacheWrapperImpl current = rootCacheWrapperImpl;
		String[] childCacheNames = PAT_DELIM_CACHENAME.split(cacheName);
		int childCnt = childCacheNames.length;
		
		int i = 1; // skip first entry since this is ourselves (rootCacheWrapperImpl) and doesn't need to be resolved
		while (i < childCnt && current != null) {
			String childName = childCacheNames[i];
			current = current.getChildWithName(childName);
			i++;
		}
		// if a matching cache was found -> invalidate those keys since their values have changed in the "same" cache in another vm and are thus dirty.
		if (current != null) {
			Codepoint.codepoint(ClusterCacher.class, "invalidateKeys");
			current.invalidateKeys(ccwe.getKeys());
		} // else no matching child cache found		
	}

	/**
	 * @param cacheName
	 * @param keys
	 */
	public void sendChangedKeys(String cacheName, String[] keys) {
		Codepoint.codepoint(ClusterCacher.class, "sendChangedKeys");
		eventBus.fireEventToListenersOf(new ClusterCacheWrapperEvent(clusterConfig.getNodeId(), cacheName, keys), ORES_THIS);
	}

	
	/**
	 * [used by spring]
	 * @param eventBus
	 */
	public void setEventBus(EventBus eventBus) {
		this.eventBus = eventBus;
	}
	
	/**
	 * [used by spring]
	 * @param rootConfig
	 */
	public void setRootConfig(CacheConfig rootConfig) {
		this.rootConfig = rootConfig;
	}

	/**
	 * [used by spring]
	 */
	public void setClusterConfig(ClusterConfig clusterConfig) {
		this.clusterConfig = clusterConfig;
	}	
}
