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
package org.olat.core.util.coordinate.util;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.olat.core.id.OLATResourceable;

/**
 * Description:<br>
 * to be used only by implementations of the syncer interface. please use 
 * @see org.olat.core.util.coordinate.Syncer
 * to synchronized on resources.
 * 
 * synchronized on a olatresourceable keeps that olatresourceable in the vm until vm restart.
 * per olatresourceable about the following space is needed: (1 Object, 1 String (around 15-70 chars), and 1 place in a HashMap = 1 
 * Entry with 3 references and 1 int for the hash.) -> (3+1)*8 + 70*2+ object overhead cluster::: make that guess more exact. = 200 bytes per ores.
 * -> 5000 need 1MByte which is fine (current olat having a couple of thousands of olatresourceables)
 * 
 * cluster:: use a HashSet instead of a Map saves all the objects (return set.get(key), internally a hashset only as -one- PRESENT object
 * 
 * <P>
 * Initial Date:  26.09.2007 <br>
 * @author Felix Jost, http://www.goodsolutions.ch
 */
public class DerivedStringSyncer {
	public static final DerivedStringSyncer INSTANCE = new DerivedStringSyncer();
	
	// keys: OLATResource-Type:ResourceId ;values: Objects
	private ConcurrentMap<String, Object> synchLockHashmap = new ConcurrentHashMap<>();

	public static DerivedStringSyncer getInstance() {
		return INSTANCE;
	}
	
	private DerivedStringSyncer() {
		// singleton
	}
	/**
	 * Only to be used by implementations of Syncer!
	 * @param ores
	 * @return
	 */
	public Object getSynchLockFor(OLATResourceable ores) {
		String key = ores.getResourceableTypeName() + ":" + ores.getResourceableId();
		Object synchLock = synchLockHashmap.get(key);
		if(synchLock == null) {
			Object newSynchLock = new Object();
			synchLock = synchLockHashmap.putIfAbsent(key, newSynchLock);
			if(synchLock == null) {
				synchLock = newSynchLock;
			}
		}
		return synchLock;
	}
}

