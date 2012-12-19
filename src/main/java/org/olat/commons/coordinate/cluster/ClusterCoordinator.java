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
*/
package org.olat.commons.coordinate.cluster;

import org.olat.commons.coordinate.cluster.jms.ClusterEventBus;
import org.olat.commons.coordinate.singlevm.SingleVMEventBus;
import org.olat.core.util.cluster.ClusterConfig;
import org.olat.core.util.coordinate.Cacher;
import org.olat.core.util.coordinate.Coordinator;
import org.olat.core.util.coordinate.Locker;
import org.olat.core.util.coordinate.Syncer;
import org.olat.core.util.event.EventBus;

/**
 * Description:<br>
 * Coordinator implementation for the olat cluster mode
 * 
 * <P>
 * Initial Date:  21.09.2007 <br>
 * @author Felix Jost, http://www.goodsolutions.ch
 */
public class ClusterCoordinator implements Coordinator {
	private Syncer syncer;
	private EventBus eventBus;
	private Locker locker;
	private Cacher cacher;
	private ClusterConfig clusterConfig;
	
	/**
	 * [used by spring]
	 *
	 */
	public ClusterCoordinator() {
		//
	}
	
	/**
	 * to be used only by the cluster admin controller!
	 * @return
	 */
	public ClusterEventBus getClusterEventBus() {
		return (ClusterEventBus) eventBus;
	}
	
	/**
	 * @see org.olat.core.util.coordinate.Coordinator#getEventBus()
	 */
	public EventBus getEventBus() {
		return eventBus;
	}

	/**
	 * @see org.olat.core.util.coordinate.Coordinator#getSyncer()
	 */
	public Syncer getSyncer() {
		return syncer;
	}
	
	
	/**
	 * do not call normally, reserved for internal calls
	 * @return
	 */
	public EventBus createSingleUserInstance() {
		// take the normal singlevm event bus, since this is only 
		// for within one user-session, which is in one vm
		return new SingleVMEventBus();
	}

	public Locker getLocker() {
		return locker;
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
	 * @param locker
	 */
	public void setLocker(Locker locker) {
		this.locker = locker;
	}

	/**
	 * [used by spring]
	 * @param syncer
	 */
	public void setSyncer(Syncer syncer) {
		this.syncer = syncer;
	}

	public Cacher getCacher() {
		return cacher;
	}

	public void setCacher(Cacher cacher) {
		this.cacher = cacher;
	}

	public Integer getNodeId() {
		return clusterConfig.getNodeId();
	}
	
	/**
	 * [used by spring]
	 */
	public void setClusterConfig(ClusterConfig clusterConfig) {
		this.clusterConfig = clusterConfig;
	}
	
	/**
	 * 
	 * @see org.olat.core.util.coordinate.Coordinator#isClusterMode()
	 */
	public boolean isClusterMode() {
		return true;
	}	

}