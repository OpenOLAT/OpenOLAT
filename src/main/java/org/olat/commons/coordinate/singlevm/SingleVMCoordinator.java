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
* <p>
*/
package org.olat.commons.coordinate.singlevm;

import org.olat.core.util.coordinate.Cacher;
import org.olat.core.util.coordinate.Coordinator;
import org.olat.core.util.coordinate.Locker;
import org.olat.core.util.coordinate.Syncer;
import org.olat.core.util.event.EventBus;

/**
 * Description:<br>
 * This is the implementation of the Coordinator for a SingleVM OLAT
 * 
 * <P>
 * Initial Date:  17.09.2007 <br>
 * @author felix
 */
public class SingleVMCoordinator implements Coordinator {

	private Syncer syncer;
	private EventBus eventBus;
	private Locker locker;
	private Cacher cacher;

	public SingleVMCoordinator() {
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
	
	public EventBus createSingleUserInstance() {
		return new SingleVMEventBus();
	}

	public Locker getLocker() {
		return locker;
	}

	public Cacher getCacher() {
		return cacher;
	}

	/**
	 * [used by spring]
	 * @param cacher
	 */
	public void setCacher(Cacher cacher) {
		this.cacher = cacher;
	}

	/**
	 * [used by spring]
	 * @param syncer
	 */
	public void setSyncer(Syncer syncer) {
		this.syncer = syncer;
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
	 * @param eventBus
	 */
	public void setEventBus(EventBus eventBus) {
		this.eventBus = eventBus;
	}

	public Integer getNodeId() {
		return new Integer(1); // single-VM: constant node-id
	}

	public boolean isClusterMode() {
		return false;
	}
}
