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

package org.olat.core.util.resource;

import java.util.Iterator;
import java.util.List;

import org.olat.core.id.OLATResourceable;
import org.olat.core.logging.AssertException;
import org.olat.core.util.event.MultiUserEvent;


/**
 *  Description:<br>
 *  event from an olatresourceable.
 *  Important: in order to be serializable, only the flat form of the OLATResoureable is taken (a String), and a method is offered to test on 
 *  contains and equals.
 *
 * @author Felix Jost
 */
public abstract class OLATResourceableEvent extends MultiUserEvent {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -5015369009092731312L;
	
	
	private String derivedOres;
	private Long oresId;
	private String oresType;

	/**
	 * Event from an olatresourceable.
	 * @param command
	 * @param ores
	 */
	public OLATResourceableEvent(String command, OLATResourceable ores) {
		super(command);
		this.derivedOres = OresHelper.createStringRepresenting(ores);
		this.oresId = ores.getResourceableId();
		this.oresType = ores.getResourceableTypeName();
	}
	
	/**
	 * @param olatResourceables
	 * @return True if target is contained list.
	 */
	public boolean targetContainedIn(List<OLATResourceable> olatResourceables) {
		boolean found = false;
		for (Iterator<OLATResourceable> it_ores = olatResourceables.iterator(); it_ores.hasNext();) {
			OLATResourceable aor = it_ores.next();
			String derived = OresHelper.createStringRepresenting(aor);
			if (derived.equals(derivedOres)) {
				found = true;
				break;
			}
		}
		return found;
	}

	/**
	 * @param ores
	 * @return True on equyality.
	 */
	public boolean targetEquals(OLATResourceable ores) {
		return targetEquals(ores, false);
	}

	/**
	 * 
	 * @param ores the OLATResourceable to test against.
	 * @param exceptIfFalse if true = throw an exception if the target does not match ores
	 * @return True on equality.
	 */
	public boolean targetEquals(OLATResourceable ores, boolean exceptIfFalse) {
		String derived = OresHelper.createStringRepresenting(ores);
		boolean res = derived.equals(derivedOres);
		if (!res && exceptIfFalse) throw new AssertException("expected ores to be the same as target, but failed: ores ="+
				ores.getResourceableTypeName()+" / "+ores.getResourceableId()+", but event was for "+derived);
		return res;
	}
	
	

	/**
	 * @return derived olatresourceable
	 */
	public String getDerivedOres() {
		return derivedOres;
	}

	/**
	 * @return Returns the oresId.
	 */
	public Long getOresId() {
		return oresId;
	}

	/**
	 * @return Returns the oresType.
	 */
	public String getOresType() {
		return oresType;
	}
}
