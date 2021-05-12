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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

import org.apache.logging.log4j.Logger;
import org.olat.core.gui.control.Controller;
import org.olat.core.id.Identity;
import org.olat.core.id.OLATResourceable;
import org.olat.core.logging.Tracing;
import org.olat.core.logging.activity.ThreadLocalUserActivityLoggerInstaller;
import org.olat.core.logging.activity.UserActivityLoggerImpl;
import org.olat.core.util.event.businfo.BusListenerInfo;

/**
 * abstract class for common services of the system bus
 * @author Felix Jost
 */
public abstract class AbstractEventBus implements EventBus {

	private final Map<String, EventAgency> infocenter;
	private final Map<String, EventAgency> typeInfocenter;
	private final Logger log = Tracing.createLoggerFor(this.getClass());

	public AbstractEventBus() {
		infocenter = new HashMap<>();
		typeInfocenter = new HashMap<>();
	}

	@Override
	public void registerFor(GenericEventListener gel, Identity identity, OLATResourceable ores) {
		final Long oresId = ores.getResourceableId();
		final String typeName = ores.getResourceableTypeName();
		synchronized (infocenter) {
			EventAgency ea = null;
			if (oresId == null) {
				// return the eventagency which listens to all events with the type of
				// the ores
				ea = typeInfocenter.get(typeName);
				if (ea == null) { // we are the first listener -> create an agency
					ea = new EventAgency();
					typeInfocenter.put(typeName, ea);
				}
			} else {
				// type and id
				String oresStr = typeName + "::" + oresId;
				ea = infocenter.get(oresStr);
				if (ea == null) { // we are the first listener
					ea = new EventAgency();
					infocenter.put(oresStr, ea);
				}
			}
			ea.addListener(gel, identity);
		}
	}

	@Override
	public void deregisterFor(GenericEventListener gel, OLATResourceable ores) {
		final Long oresId = ores.getResourceableId();
		final String typeName = ores.getResourceableTypeName();
		synchronized (infocenter) {
			if (oresId == null) {
				EventAgency ea = typeInfocenter.get(typeName);
				if (ea != null) {
					ea.removeListener(gel);
					if(ea.getListenerCount() == 0) {
						typeInfocenter.remove(typeName);
					}
				}
			} else {
				// type and id
				String oresStr = typeName + "::" + oresId;
				EventAgency ea = infocenter.get(oresStr);
				if (ea != null) {
					ea.removeListener(gel);
					if(ea.getListenerCount() == 0) {
						infocenter.remove(typeName);
					}
				}
			}
		}
	}

	public abstract int getListeningIdentityCntFor(OLATResourceable ores);
	
	public abstract void fireEventToListenersOf(MultiUserEvent event, OLATResourceable ores);
	
	/**
	 * fires events to registered listeners of generic events.
	 * To see all events set this class and also DefaultController and Component to debug.
	 * @param event
	 * @param ores
	 */
	protected final void doFire(final MultiUserEvent event, final OLATResourceable ores) {
		final Long oresId = ores.getResourceableId();
		final String typeName = ores.getResourceableTypeName();

		GenericEventListener[] listenersArr = null;
		GenericEventListener[] listenersTypeArr = null;
		synchronized (infocenter) {  
			if (oresId != null) {
				String oresStr = typeName + "::" + oresId;
				EventAgency ea = infocenter.get(oresStr);
				if (ea != null) {
					listenersArr = ea.getListeners();
				}
			}
			EventAgency ea = typeInfocenter.get(typeName);
			if (ea != null) {
				listenersTypeArr = ea.getListeners();
			}
		}

		doFire(event, listenersArr);
		doFire(event, listenersTypeArr);
	}
	
	private final void doFire(final MultiUserEvent event, final GenericEventListener[] liArr) {
		if(liArr == null) return;
		
		for (int i = 0; i < liArr.length; i++) {
			try {
				final GenericEventListener listener = liArr[i];
				
				//make sure GenericEvents are only sent when controller is not yet disposed
				if (listener instanceof Controller) {
					Controller dCtrl = (Controller)listener;
					if (!dCtrl.isDisposed()) {
						ThreadLocalUserActivityLoggerInstaller.runWithUserActivityLogger(new Runnable() {
							@Override
							public void run() {
								listener.event(event);
							}
						}, UserActivityLoggerImpl.newLoggerForEventBus(dCtrl));
					}
				} else if(listener != null) {
					if(log.isDebugEnabled()){
						log.debug("fireEvent: Non-Controller: "+listener);
					}
					//is there a need to differ the events sent on one VM and in cluster mode?
					ThreadLocalUserActivityLoggerInstaller.runWithUserActivityLogger(new Runnable() {
						@Override
						public void run() {
							listener.event(event);
						}
					}, ThreadLocalUserActivityLoggerInstaller.createEmptyUserActivityLogger());
				}
			} catch (Exception e) {
				log.error("Error while sending generic event: {}", liArr[i], e);
			}
		}
	}
	
	protected final BusListenerInfo createBusListenerInfo() {
		BusListenerInfo bii = new BusListenerInfo();
		synchronized(infocenter) { // o_clusterOK by:fj: extract quickly so that we can later serialize and send across the wire. data affects only one vm.
			// for all types: the name of the type + "::"+ the id (integer) is used as key
			List<String> infocenterKeys = new ArrayList<>(infocenter.keySet());
			for (String derivedOres: infocenterKeys) {
				EventAgency ea = infocenter.get(derivedOres);
				int cnt = ea.getListenerCount();
				// only add those with at least one current listener. Telling that a resource has no listeners is unneeded since we update 
				// the whole table on each clusterInfoEvent (cluster:: could be improved by only sending the delta of listeners)
				if (cnt > 0) {
					bii.addEntry(derivedOres, cnt);
				} else {
					infocenter.remove(derivedOres);
				}
			}
			infocenterKeys = null;
			
			// for all types: the name of the type is used as key
			List<String> typeInfocenterKeys = new ArrayList<>(typeInfocenter.keySet());
			for (String derivedOres: typeInfocenterKeys) {
				EventAgency ea = typeInfocenter.get(derivedOres);
				int cnt = ea.getListenerCount();
				if (cnt > 0) {
					bii.addEntry(derivedOres, cnt);
				} else {
					typeInfocenter.remove(derivedOres);
				}
			}
		}
		return bii;
	}
	
	protected final int getLocalListeningIdentityCntFor(OLATResourceable ores) {
		int cnt = 0;
		final Long oresId = ores.getResourceableId();
		final String typeName = ores.getResourceableTypeName();
		synchronized (infocenter) { 
			EventAgency ea = null;
			if (oresId == null) {
				ea = typeInfocenter.get(typeName);	
			} else {
				// type and id
				String oresStr = typeName + "::" + oresId;
				ea = infocenter.get(oresStr);
			}
			if (ea != null) {
				cnt = ea.getListenerCount();
			}
		}
		return cnt;
	}
	
	/**
	 * Description: <br>
	 * The listeners map is not synchronized, but need to be. The synchronization
	 * is done by the infocenter map. Make sure that you always access instances of
	 * this class in a synchronized(infocenter).
	 * 
	 * @author Felix Jost
	 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
	 */
	private static class EventAgency {
		private WeakHashMap<GenericEventListener, Long> listeners = new WeakHashMap<>();
		
		/**
		 * @param event
		 */
		GenericEventListener[] getListeners() {
			return listeners.keySet().toArray(new GenericEventListener[listeners.size()]);
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
			if (!listeners.containsKey(gel)) {
				Long identityKey = (identity != null? identity.getKey() : null);
				listeners.put(gel, identityKey);
			}
		}

		/**
		 * @param gel
		 */
		void removeListener(GenericEventListener gel) {
			listeners.remove(gel);
		}
		
		/**
		 * 
		 * @return the current number of listeners listening to this channel/eventagency 
		 */
		int getListenerCount() {
			return listeners.size();	
		}
	}
}