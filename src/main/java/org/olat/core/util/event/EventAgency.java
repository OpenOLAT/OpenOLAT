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

package org.olat.core.util.event;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.WeakHashMap;

import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.id.Identity;
import org.olat.core.logging.OLog;
import org.olat.core.logging.Tracing;
import org.olat.core.logging.activity.ThreadLocalUserActivityLoggerInstaller;
import org.olat.core.logging.activity.UserActivityLoggerImpl;

/**
 * Description: <br>
 * note: need to sync on each method since the hashmap is not synced itself and
 * also to ensure listeners.values() does not break with a
 * concurrentmodificationexception (weakhashmap has fail-fast iterators, but
 * read-only operation do not trigger removing entries from the referencequeue.
 * 
 * @author Felix Jost
 */
class EventAgency {
	private WeakHashMap<GenericEventListener, String> listeners = new WeakHashMap<GenericEventListener, String>();
	private OLog log = Tracing.createLoggerFor(this.getClass());

	//private Event latestEvent = null;
	//private long latestEventTimestamp; // ts when the latest event was fired
	//private long ttl = 0; // time-to-live in miliseconds
	
	EventAgency() {
		//
	}
	
	
	/**
	 * @param ttl The time-to-live to set, in miliseconds. the latest event fired will be kept "in store" for that period in time and will be delivered upon registering. defaults to 0, meaning that the latest event is never stored.
	 */
	/*EventAgency(long ttl) {
		this.ttl = ttl;
	}*/

	/**
	 * 
	 * 
	 * @param event
	 */
	void fireEvent(final Event event) {
		GenericEventListener[] liArr;
		synchronized (listeners) {  //o_clusterOK by:fj
			// needed to avoid concurrentmodificationexception, since the recipients may remove themselves from the list and the listeners lock is reentrant
			List<GenericEventListener> li = new ArrayList<GenericEventListener>(listeners.keySet());
			// instead synchronize looping to avoid concurrent modifcation exception
			// -> synchronize copy to array
			// -> loop over array
			liArr = new GenericEventListener[li.size()];
			liArr = li.toArray(liArr);
		}
		// -> avoid dead lock (see OLAT-3681)
		//no sync during firing to listeners (potentially "long" taking - although recommendation is to keep event methods short.
		
		for (int i = 0; i < liArr.length; i++) {
			try {
				final GenericEventListener listener = liArr[i];
				
				//make sure GenericEvents are only sent when controller is not yet disposed
				if (listener instanceof Controller) {
					Controller dCtrl = (Controller)listener;
					if (!dCtrl.isDisposed()) {
						ThreadLocalUserActivityLoggerInstaller.runWithUserActivityLogger(new Runnable() {
							public void run() {
								listener.event(event);
							}
						}, UserActivityLoggerImpl.newLoggerForEventBus(dCtrl));
					}
				} else {
					if(log.isDebug()){
						log.debug("fireEvent: Non-Controller: "+listener);
					}
					//is there a need to differ the events sent on one VM and in cluster mode?
					ThreadLocalUserActivityLoggerInstaller.runWithUserActivityLogger(new Runnable() {
						public void run() {
							listener.event(event);
						}
					}, ThreadLocalUserActivityLoggerInstaller.createEmptyUserActivityLogger());
				}
			} catch (RuntimeException e) {
				log.error("Error while sending generic event: "+liArr[i], e);
			}
		}
	}

	/**
	 * use only for administrative purposes!
	 * @return a Set of IdentitieNames (Strings) who are registered with this event agency.
	 */
	Set<String> getListeningIdentityNames() {
		synchronized (listeners) {//cluster_ok
			Collection<String> c = listeners.values();
			Set<String> distinctIds = new HashSet<String>(c);
			return distinctIds;
		}
	}

	/**
	 * impl note: the underlying impl takes a weakHashMap, so unused entries are
	 * cleared. an instance may not be added twice (make no sense anyway), since we
	 * are using a map, not a list.
	 * 
	 * @param gel the instance which wants to listen to events.
	 * @param identity the identity belonging to the listener, or null if there is
	 *          none (e.g. the LockManager = the 'System')
	 */
	void addListener(GenericEventListener gel, Identity identity) {
		synchronized (listeners) { //o_clusterOK by:fj
			if (listeners.containsKey(gel)) {
				//already registered, do nothing
				return;
			}
			
			String identityName = (identity != null? identity.getName() : null);
			listeners.put(gel, identityName);
		}
	}

	/**
	 * @param gel
	 */
	void removeListener(GenericEventListener gel) {
		synchronized (listeners) { //o_clusterOK by:fj
			listeners.remove(gel);
		}
	}
	
	/**
	 * 
	 * @return the current number of listeners listening to this channel/eventagency 
	 */
	int getListenerCount() {
		synchronized (listeners) { //o_clusterOK by:fj
			return listeners.size();
		}		
	}

}