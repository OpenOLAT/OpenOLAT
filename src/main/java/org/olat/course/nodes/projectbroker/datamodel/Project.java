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

package org.olat.course.nodes.projectbroker.datamodel;

import java.util.List;

import org.olat.basesecurity.SecurityGroup;
import org.olat.core.id.CreateInfo;
import org.olat.core.id.Identity;
import org.olat.core.id.Persistable;
import org.olat.group.BusinessGroup;

public interface Project extends CreateInfo,Persistable{

	public static final String STATE_ASSIGNED = "state_assigned";
	// state values are used as translation-keys 
	public static final String STATE_NOT_ASSIGNED   = "state_not_assigned";
	public static final String STATE_FINAL_ENROLLED = "state_final_enrolled";
	public static final String STATE_PROV_ENROLLED  = "state_prov_enrolled";
	public static final String STATE_COMPLETE       = "state_complete";
	public static final String STATE_ENROLLED       = "state_enrolled";
	public static final String STATE_ASSIGNED_ACCOUNT_MANAGER     = "state_assigned.accountmanager";
	public static final String STATE_NOT_ASSIGNED_ACCOUNT_MANAGER = "state_not_assigned.accountmanager";
	public static final String STATE_NOT_ASSIGNED_ACCOUNT_MANAGER_NO_CANDIDATE = "state_not_assigned.accountmanager.no.candidate";

	public static final int MAX_MEMBERS_UNLIMITED = -1;
	
	public static final int MAX_CUSTOM_FIELD_VALUE_LENGTH = 250;

	public enum EventType { ENROLLMENT_EVENT, HANDOUT_EVENT; // disabled events PRESENTATION_EVENT , OTHER_EVENT
		public String getI18nKey() {
			return toString().toLowerCase();
		} 
	} 

	public String getTitle();

	public String getDescription();

	public String getAttachmentFileName();

	public String getState();
	
	public int getMaxMembers();
	
	public int getSelectedPlaces();

	/**
	 * 
	 * @return List of Identity objects
	 */
	public List<Identity> getProjectLeaders();

	/**
	 * 
	 * @return List of Identity objects
	 */
	public SecurityGroup getCandidateGroup();

	public BusinessGroup getProjectGroup();

	public void setTitle(String value);

	public void setDescription(String value);

	public void setAttachedFileName(String value);

	public void setMaxMembers(int i);

	public abstract ProjectBroker getProjectBroker();

	public void setState(String stateAssigned);

	public int getCustomFieldSize();
	
	public String getCustomFieldValue(int i);
	
	public void setCustomFieldValue(int index, String value);

	public void setProjectEvent(ProjectEvent projectEvent);

	public ProjectEvent getProjectEvent(Project.EventType eventType);
	
	public boolean isMailNotificationEnabled();

	public void setMailNotificationEnabled(boolean selected);

}