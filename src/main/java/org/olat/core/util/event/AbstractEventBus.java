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

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import org.olat.core.id.Identity;
import org.olat.core.id.OLATResourceable;
import org.olat.core.logging.OLog;
import org.olat.core.logging.Tracing;
import org.olat.core.util.event.businfo.BusListenerInfo;

/**
 * abstract class for common services of the system bus
 * @author Felix Jost
 */
public abstract class AbstractEventBus implements EventBus {

	private Map<String, EventAgency> infocenter;
	private Map<String, EventAgency> typeInfocenter;
	private OLog log = Tracing.createLoggerFor(this.getClass());

	public AbstractEventBus() {
		infocenter = new HashMap<String, EventAgency>();
		typeInfocenter = new HashMap<String, EventAgency>();
	}

	public void registerFor(GenericEventListener gel, Identity identity, OLATResourceable ores) {
		EventAgency ea = getEventAgencyFor(ores);
		ea.addListener(gel, identity);
	}

	public void deregisterFor(GenericEventListener gel, OLATResourceable ores) {
		EventAgency ea = getEventAgencyFor(ores);
		ea.removeListener(gel);
	}

	/**
	 * @return the listening names on a resource - only use for admin purposes!
	 */
	public Set<String> getListeningIdentityNamesFor(OLATResourceable ores) {
		EventAgency ea = getEventAgencyFor(ores);
		Set<String> s = ea.getListeningIdentityNames();
		return s;
	}

	public abstract int getListeningIdentityCntFor(OLATResourceable ores);
	
	public abstract void fireEventToListenersOf(MultiUserEvent event, OLATResourceable ores);
	
	/**
	 * fires events to registered listeners of generic events.
	 * To see all events set this class and also DefaultController and Component to debug.
	 * @param event
	 * @param ores
	 */
	protected void doFire(MultiUserEvent event, OLATResourceable ores) {
		Long oresId = ores.getResourceableId();
		String typeName = ores.getResourceableTypeName();

		// 1. fire to all instance listeners
		if (oresId != null) {
			// fire event to all id - listeners
			
			//try to avoid synchronize on infocenter over next two codelines
			//see OLAT-3681 -
			EventAgency ea = getEventAgencyFor(typeName, oresId);//synchronizes shortly on infocenter
			ea.fireEvent(event);//synchronizes short on listeners but only to copy
		}
		
		//try to avoid synchronize on infocenter over next two codelines
		//see OLAT-3681
		// 2. fire event to all type - listeners
		EventAgency tea = getEventAgencyFor(typeName, null);
		if (log.isDebug()) log.debug("Generic Event from: "+typeName+": of type: "+event.getClass().getName());
		tea.fireEvent(event);//synchronizes short on listeners but only to copy
	}
	
	/**
	 * used only for monitoring purposes
	 */
	public Map<String, EventAgency> getUnmodifiableInfoCenter() {
		synchronized (infocenter) {  //o_clusterOK by:fj
			Map<String, EventAgency> all = new HashMap<String, EventAgency>(infocenter.size() + typeInfocenter.size());
			all.putAll(infocenter);
			all.putAll(typeInfocenter);
			return all;
		}
	}
	
	protected BusListenerInfo createBusListenerInfo() {
		BusListenerInfo bii = new BusListenerInfo();
		synchronized(infocenter) { // o_clusterOK by:fj: extract quickly so that we can later serialize and send across the wire. data affects only one vm.
			// for all types: the name of the type + "::"+ the id (integer) is used as key
			for (Entry<String, EventAgency> entry: infocenter.entrySet()) {
				String derivedOres = entry.getKey();
				int cnt = entry.getValue().getListenerCount();
				// only add those with at least one current listener. Telling that a resource has no listeners is unneeded since we update 
				// the whole table on each clusterInfoEvent (cluster:: could be improved by only sending the delta of listeners)
				if (cnt > 0) bii.addEntry(derivedOres, cnt);
			}
			// for all types: the name of the type is used as key
			for (Entry<String, EventAgency> entry: typeInfocenter.entrySet()) {
				String derivedOres = entry.getKey();
				int cnt = entry.getValue().getListenerCount();
				if (cnt > 0) bii.addEntry(derivedOres, cnt);
			}
		}
		return bii;
	}

	private EventAgency getEventAgencyFor(OLATResourceable ores) {
		Long oresId = ores.getResourceableId();
		String typeName = ores.getResourceableTypeName();
		return getEventAgencyFor(typeName, oresId);
	}

	private EventAgency getEventAgencyFor(String typeName, Long oresId) {
		EventAgency ea;
		synchronized (infocenter) {  
			//o_clusterOK by:fj for cluster: clustereventbus is async vm to vm, and 
			// here we only the eventagency of one vm need to be synchronized
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
				String oresStr = typeName + "::" + oresId.toString();
				ea = infocenter.get(oresStr);
				if (ea == null) { // we are the first listener
					ea = new EventAgency();
					infocenter.put(oresStr, ea);
				}
			}
		}
		return ea;
	}
	
	public String toString() {
		
		if (log.isDebug()) {
			int totalListenerCount = 0;
			for (Entry<String, EventAgency> entry: getUnmodifiableInfoCenter().entrySet()) {
				EventAgency eventAgency = entry.getValue();
				int listenerCount = eventAgency.getListenerCount();
				//if (listenerCount > 0)
				//System.out.println("TEST AbstractEventBus  derivedOres(key)=" + entry.getKey() + "  listenerCount=" + listenerCount);
				totalListenerCount = totalListenerCount + listenerCount;
			}
			return "AbstractEventBus : #totalListenerCount=" + totalListenerCount + "   #EventAgency of infocenter=" + infocenter.size() + " #EventAgency of  typeInfocenter=" + typeInfocenter.size();
		} 
		return "Enable debugging to see number of listeners"; 
	}

}