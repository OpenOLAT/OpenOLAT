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

import org.olat.core.util.event.EventBus;

/**
 * Description:<br>
 * Serves as a facade for all coordinated-related services such as
 * Syncer (synchronizing access), Locker (locking), Cacher (caching) and
 * the eventbus (application-wide events)
 * 
 * <P>
 * Initial Date:  17.09.2007 <br>
 * @author Felix Jost
 */
public interface Coordinator {
	
	/**
	 * 
	 * @return the Syncer (synchronizing access to resources)
	 */
	public Syncer getSyncer();

	/**
	 * 
	 * @return the event bus (sending application-wide events)
	 */
	public EventBus getEventBus();
	
	/**
	 * 
	 * @return the Locker (locking facility)
	 */
	public Locker getLocker();
	
	/**
	 * 
	 * @return the Cacher (for caching)
	 */
	public Cacher getCacher();
		
	/**
	 * do -not- call normally, reserved for internal calls
	 * @return
	 */
	public EventBus createSingleUserInstance();

	/**
	 * @return  Node ID of certain cluster-node, in case of single-VM return always the same id
	 * @deprecated if you really need the node id try to access it via spring. Search the spring files for ${node.id} for an example.
	 * Calling this method depends on a fully loaded OLAT and this is not what you want when doing unit testing without OLAT.
	 * If you like to have a service which only runs on one node there is a concept called singleton services.
	 * See the UpgradeManager spring config for an example.
	 */
	public Integer getNodeId();
	
	/**
	 * Try to avoid coupling your code to an either cluster or single vm version. If you have to check out the spring way by
	 * declaring an week reference to an lazy spring bean which gets resolved upon startup time and use the ${cluste.mode} property to 
	 * decide which version you need: See InstantMessagingSessionCount as an example
	 * @deprecated
	 * @return true if OLAT runs in cluster mode
	 */
	public boolean isClusterMode();

}
