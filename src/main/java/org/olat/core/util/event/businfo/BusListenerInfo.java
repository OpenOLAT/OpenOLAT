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
package org.olat.core.util.event.businfo;

import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.olat.core.id.OLATResourceable;
import org.olat.core.util.resource.OresHelper;

/**
 * Description:<br>
 * a class which contains infos (one instance per cluster node) about the listener count for all olat resources currently being used.
 * 
 * <P>
 * Initial Date:  05.11.2007 <br>
 * @author Felix Jost, http://www.goodsolutions.ch
 */
public class BusListenerInfo implements Serializable {
	private static final long serialVersionUID = 5144991028203688442L;
	private Map<String, Integer> listenersCnt = new HashMap<>();
	
	public BusListenerInfo() {
		//
	}

	/**
	 * @param ores the channel
	 * @return the current number of listeners of this channel 
	 */
	public int getCountFor(OLATResourceable ores) {
		String derived = OresHelper.createStringRepresenting(ores);
		return getCountFor(derived);
	}

	int getCountFor(String derived) {
		synchronized(listenersCnt) {//cluster_ok /information per single vm broadcasted
			Integer cnt = listenersCnt.get(derived);
			return cnt == null? 0 : cnt;
		}
	}

	
	public void addEntry(String derivedString, int cnt) {
		synchronized(listenersCnt) {//cluster_ok
			listenersCnt.put(derivedString, new Integer(cnt));
		}
	}
	
	public Set<String> getAllDerivedStrings() {
		synchronized (listenersCnt) {//cluster_ok
			// copy to be independent (fail safe iterators)
			return new HashSet<>(listenersCnt.keySet());
		}
	}
}
