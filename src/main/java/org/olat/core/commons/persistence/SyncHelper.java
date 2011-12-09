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

package org.olat.core.commons.persistence;

import java.util.Iterator;
import java.util.List;

import org.olat.core.id.Persistable;

/**
*  Description:<br>
*
* @author Felix Jost
*/
public class SyncHelper {

	/**
	 * @param persistables
	 * @param persistable
	 * @return Persistable
	 */
	public static Persistable findInListByKey(List persistables, Persistable persistable) {
		Long key = persistable.getKey();
		for (Iterator iter = persistables.iterator(); iter.hasNext();) {
			Persistable ppit  = (Persistable) iter.next();
			if (ppit.getKey().equals(key)) {
				return ppit;
			}
		}
		return null;
	}
	
	/**
	 * @param listOfPersistables
	 * @param persistable
	 * @return True if listOfPersistable contains persistable
	 */
	public static boolean containsPersistable(List listOfPersistables, Persistable persistable) {
		Long key = persistable.getKey();
		for (Iterator iter = listOfPersistables.iterator(); iter.hasNext();) {
			Persistable entry = (Persistable) iter.next();
			if (entry.getKey().equals(key)) {
				return true;
			} 
		}
		return false;
	}
	
	/*public static List replacePersistable(List listOfPersitables, Persistable newPersistable) {
		for (Iterator iter = listOfPersitables.iterator(); iter.hasNext();) {
			Persistable entry = (Persistable) iter.next();
			if (entry.getKey().equals(newPersistable.getKey())) {
				int index = listOfPersitables.indexOf(entry);
				listOfPersitables.add(index, newPersistable);
			}
		}		
		return listOfPersitables;
		
	}*/
}
