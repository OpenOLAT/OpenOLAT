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

import org.olat.core.util.event.MultiUserEvent;

/**
 * Description:<br>
 * represents a cache event.
 * the cache event denotes which keys of a certain cache were invalidated.
 * 
 * <P>
 * Initial Date:  23.10.2007 <br>
 * @author Felix Jost, http://www.goodsolutions.ch
 */
public class ClusterCacheWrapperEvent extends MultiUserEvent {
	private String[] keys;
	private final String cacheName;
	private final Integer sendingNodeId;

	/**
	 * @param command
	 */
	ClusterCacheWrapperEvent(Integer sendingNodeId, String cacheName, String[] keys) {
		super("clustercachewrapperevent");
		this.sendingNodeId = sendingNodeId;
		this.cacheName = cacheName;
		this.keys = keys;
	}

	public String[] getKeys() {
		return keys;
	}

	public String getCacheName() {
		return cacheName;
	}

	public Integer getSendingNodeId() {
		return sendingNodeId;
	}
	
	public String toString() {
		return super.toString()+",{#keys: "+keys.length+", cachename: "+cacheName;
	}

}
