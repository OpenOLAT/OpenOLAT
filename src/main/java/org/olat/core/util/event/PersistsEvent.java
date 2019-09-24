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

import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.olat.core.id.Persistable;

/**
 * Initial Date:  Sep 8, 2005 <br>
 * @author Felix Jost
 */
public class PersistsEvent extends MultiUserEvent {

	private static final long serialVersionUID = -1076949650098640208L;
	private final Set<Long> keys;
	
	/**
	 * 
	 * @param mode
	 * @param keys
	 */
	PersistsEvent(Set<Long> keys) {
		super("persistsevent");
		this.keys = keys;	
	}
	
	/**
	 * 
	 * @param persistables a List of Persistable objects
	 * @return true if at least one of the objects in the list has the same key as the keys given.
	 */
	public boolean isAtLeastOneKeyInList(List<? extends Persistable> persistables) {
		for (Iterator<? extends Persistable> it_per = persistables.iterator(); it_per.hasNext();) {
			Persistable per = it_per.next();
			Long key = per.getKey();
			if (keys.contains(key)) return true;
		}
		return false;
	}

}
