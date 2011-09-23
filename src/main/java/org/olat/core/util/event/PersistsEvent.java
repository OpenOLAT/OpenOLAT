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
* Copyright (c) 1999-2006 at Multimedia- & E-Learning Services (MELS),<br>
* University of Zurich, Switzerland.
* <p>
*/ 

package org.olat.core.util.event;

import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.olat.core.id.Persistable;

/**
 * Description:<br>
 * TODO: Felix Jost Class Description for PersistsEvent
 * 
 * <P>
 * Initial Date:  Sep 8, 2005 <br>
 * @author Felix Jost
 */
public class PersistsEvent extends MultiUserEvent {
	//public static final int MODE_AFFECTED = 1;
	//public static final int MODE_ADDED = 2;
	//public static final int MODE_REMOVED = 4;
	//public static final int MODE_UPDATED = 8;

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
	public boolean isAtLeastOneKeyInList(List persistables) {
		for (Iterator it_per = persistables.iterator(); it_per.hasNext();) {
			Persistable per = (Persistable) it_per.next();
			Long key = per.getKey();
			if (keys.contains(key)) return true;
		}
		return false;
	}

}
