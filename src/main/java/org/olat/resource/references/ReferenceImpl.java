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

package org.olat.resource.references;

import org.olat.core.commons.persistence.PersistentObject;
import org.olat.core.logging.AssertException;
import org.olat.resource.OLATResourceImpl;


/**
 * Initial Date:  May 27, 2004
 *
 * @author Mike Stock
 * 
 * Comment:  
 * 
 */
public class ReferenceImpl extends PersistentObject {
	//FIXME:ms:c (by fj) extract interface Reference
	private OLATResourceImpl source;
	private OLATResourceImpl target;
	private String userdata;
	private static final int USERDATA_MAXLENGTH = 64;
	
	protected ReferenceImpl() {
		// hibernate
	}
	
	/**
	 * @param source
	 * @param target
	 * @param userdata
	 */
	public ReferenceImpl(OLATResourceImpl source, OLATResourceImpl target, String userdata) {
		this.source = source;
		this.target = target;
		this.userdata = userdata;
	}
	
	/**
	 * @return Returns the source.
	 */
	public OLATResourceImpl getSource() {
		return source;
	}
	/**
	 * @param source The source to set.
	 */
	public void setSource(OLATResourceImpl source) {
		this.source = source;
	}
	/**
	 * @return Returns the target.
	 */
	public OLATResourceImpl getTarget() {
		return target;
	}
	/**
	 * @param target The target to set.
	 */
	public void setTarget(OLATResourceImpl target) {
		this.target = target;
	}

	/**
	 * @return Returns the userdata.
	 */
	public String getUserdata() {
		return userdata;
	}
	/**
	 * @param userdata The userdata to set.
	 */
	public void setUserdata(String userdata) {
		if (userdata != null && userdata.length() > USERDATA_MAXLENGTH)
			throw new AssertException("field userdata of table o_reference too long");
		this.userdata = userdata;
	}

}
