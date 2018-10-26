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

package org.olat.core.commons.services.notifications;

import java.io.Serializable;

import org.olat.core.id.OLATResourceable;
import org.olat.core.logging.AssertException;

/**
 * Description: <br>
 * <P>
 * 
 * Initial Date: 25.10.2004 <br>
 * @author Felix Jost
 */
public class SubscriptionContext implements Serializable {

	private static final long serialVersionUID = 2704406819309583263L;
	
	private final String resName;
	private final Long resId;
	private final String subidentifier;

	/**
	 * Create a new subscription context
	 * 
	 * @param resName not null, unique identifier for this context use something like: OresHelper.calculateTypeName(DropboxController.class);
	 * @param resId not null, resource id like OLATResourcable.getResourceId()
	 * @param subidentifier not null, when context is from course use CourseNode.getIdent()
	 */
	public SubscriptionContext(String resName, Long resId, String subidentifier) {
		if (resName == null || resId == null || subidentifier == null) throw new AssertException("resName, resId, subident cannot be null");
		this.resName = resName;
		this.resId = resId;
		this.subidentifier = subidentifier;
	}
	
	/**
	 * Create a new subscription context.
	 * Calls the other constructor by calculating the unique name and the resource id out of the OLATResourcable
	 * 
	 * @param ores, 
	 * @param subidentifier not null, when context is from course use CourseNode.getIdent()
	 */
	public SubscriptionContext(OLATResourceable ores, String subidentifier) {
		this (ores.getResourceableTypeName(), ores.getResourceableId(), subidentifier);
	}

	/**
	 * @return resId
	 */
	public Long getResId() {
		return resId;
	}

	/**
	 * @return resName
	 */
	public String getResName() {
		return resName;
	}

	/**
	 * @return subidentifier
	 */
	public String getSubidentifier() {
		return subidentifier;
	}

	/**
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		return getResName() + "," + getResId() + "," + getSubidentifier() + "," + super.toString();
	}
}

