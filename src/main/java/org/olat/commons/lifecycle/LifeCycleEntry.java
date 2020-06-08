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

package org.olat.commons.lifecycle;

import java.util.Date;

import org.olat.core.commons.persistence.PersistentObject;
import org.olat.core.logging.AssertException;

/**
 * An entry in LifeCycle-table
 *
 * @author Christian Guretzki
 */
public class LifeCycleEntry extends PersistentObject {

	private static final long serialVersionUID = -2919077675588017564L;
	private String persistentTypeName;
	private Long   persistentRef;
	private String action;
	private Date   lcTimestamp;
	private String userValue;
	

	protected static final int PERSISTENTTYPENAME_MAXLENGTH = 50;


	LifeCycleEntry() { 
	}

	LifeCycleEntry(Date lifeCycleDate, String persistentObjectTypeName, Long persistentObjectRef) { 
		this.setPersistentTypeName(persistentObjectTypeName);
		this.setPersistentRef(persistentObjectRef);
		if (lifeCycleDate == null) {
			this.lcTimestamp = new Date();
		} else {
			this.lcTimestamp = lifeCycleDate;
		}
	}

	
	/**
	 * @return Returns the lcTimestamp.
	 */
	public Date getLcTimestamp() {
		return lcTimestamp;
	}

	/**
	 * @return Returns the action.
	 */
	protected String getAction() {
		return action;
	}


	/**
	 * @param action The action to set.
	 */
	protected void setAction(String action) {
		this.action = action;
	}


	/**
	 * @return Returns the resourceTypeId.
	 */
	public Long getPersistentRef() {
		return persistentRef;
	}


	/**
	 * @param resourceTypeId The resourceTypeId to set.
	 */
	protected void setPersistentRef(Long persistentRef) {
		this.persistentRef = persistentRef;
	}


	/**
	 * @return Returns the resourceTypeName.
	 */
	protected String getPersistentTypeName() {
		return persistentTypeName;
	}


	/**
	 * @param resourceTypeName The resourceTypeName to set.
	 */
	protected void setPersistentTypeName(String persistentTypeName) {
		if (persistentTypeName.length() > PERSISTENTTYPENAME_MAXLENGTH) {
			throw new AssertException("persistentTypeName in o_lifecycle too long. persistentObjectTypeName=" + persistentTypeName );
		}
		this.persistentTypeName = persistentTypeName;
	}


	/**
	 * @return Returns the userValue.
	 */
	public String getUserValue() {
		return userValue;
	}


	/**
	 * @param userValue The userValue to set.
	 */
	protected void setUserValue(String userValue) {
		this.userValue = userValue;
	}




	/**
	 * @param lcTimestamp The lcTimestamp to set.
	 */
	protected void setLcTimestamp(Date lcTimestamp) {
		this.lcTimestamp = lcTimestamp;
	}

	public String toString() {
		return persistentTypeName + ":" + persistentRef + ", action=" + action + ", timestamp=" + this.lcTimestamp;
	}

}
