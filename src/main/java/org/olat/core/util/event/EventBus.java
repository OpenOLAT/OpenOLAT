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
package org.olat.core.util.event;

import org.olat.core.id.Identity;
import org.olat.core.id.OLATResourceable;

/**
 * <!--**************-->
 * <h3>Multiuser events</h3>
 * This is the center distributing the multiuser events into the system.<br>
 * Classes implementing the
 * {@link org.olat.core.util.event.GenericEventListener GenericEventListener} can
 * register for an event bound to a certain
 * {@link org.olat.core.id.OLATResourceable}. A class having
 * "news" concerning an OLATResourceable may fire
 * {@link org.olat.core.util.event.MultiUserEvent Events} which are sent to all
 * listeners.<br>
 * <b>NOTE:</b> the listeners are put in a WeakHashMap, so they need to have
 * another reference than just the event center.
 * 
 * @author Felix Jost
 */
public interface EventBus {

	/**
	 * registers a GenericEventListener to listen to events concerning the
	 * OLATResourceable ores
	 * 
	 * @param gel the GenericEventListener / the class implementing it
	 * @param identity the identity to whicinfoh the listening (controller)
	 *          belongs, or null if that is not known or the olat-system itself.
	 * @param ores the OLATResourceable
	 */
	public void registerFor(GenericEventListener gel, Identity identity, OLATResourceable ores);

	/**
	 * deregisters/removes a GenericEventListener to listen to events concerning
	 * the OLATResourceable ores
	 * 
	 * @param gel
	 * @param ores
	 */
	public void deregisterFor(GenericEventListener gel, OLATResourceable ores);

	/**
	 * fires an event to all listeners interested in events concerning this
	 * OLATResourceable ores. The events may be fired and received synchronously or asynchronously, depending on the concrete implementation.
	 * 
	 * 
	 * @param event the OLATResourceableEvent (must be serializable!, for multiple
	 *          server olat installations)
	 * @param ores the OLATResourceable
	 */
	public void fireEventToListenersOf(MultiUserEvent event, OLATResourceable ores);

	/**
	 * 
	 * Note for cluster: this method is cluster-safe. in a cluster, it takes the latest counts received from all cluster nodes and sums them up.
	 * @param ores the resourceable
	 * @return the number of people currently using this resource
	 */
	public int getListeningIdentityCntFor(OLATResourceable ores);

}