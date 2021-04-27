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

package org.olat.group;

import java.util.Date;

import org.olat.basesecurity.Group;
import org.olat.core.id.CreateInfo;
import org.olat.core.id.ModifiedInfo;
import org.olat.core.id.OLATResourceable;
import org.olat.core.id.Persistable;
import org.olat.resource.OLATResource;

/**
 * Initial Date: Aug 2, 2004
 * 
 * @author gnaegi<br>
 *         Comment: All OLAT business group implementation share this interface.
 *         Examples are the buddygroups or the learning groups
 */
public interface BusinessGroup extends BusinessGroupShort, Persistable, CreateInfo, ModifiedInfo, OLATResourceable {

	/** regular expression to check for valid group names */
	// commas are not allowed. name is used in course conditions for weak binding
	public static final String VALID_GROUPNAME_REGEXP = "^[^,\"]*$";
	
	/** the max length for the group name.*/
	public static final int MAX_GROUP_NAME_LENGTH = 255;
	
	public static final String BUSINESS_TYPE = "business";

	/**
	 * @return The group display name (not system unique)
	 */
	public String getName();

	/**
	 * @param name The group display name. Must never be NULL
	 */
	public void setName(String name);

	/**
	 * @return The group description or NULL if none set.
	 */
	public String getDescription();
	
	/**
	 * @param description the description of this group. Might be NULL
	 */
	public void setDescription(String description);
	
	public String getTechnicalType();
	
	/**
	 * 
	 * @return An ID used by external system for example.
	 */
	public String getExternalId();
	
	public void setExternalId(String externalId);
	
	/**
	 * 
	 * @return List of flags which say what features are externally managed
	 */
	public BusinessGroupManagedFlag[] getManagedFlags();

	/**
	 * Return the list of managed flags as a string with the
	 * flags separated by comma.
	 * @return
	 */
	public String getManagedFlagsString();
	
	/**
	 * A list of flags
	 * @param flags
	 */
	public void setManagedFlagsString(String flags);

	/**
	 * BusinessGroup was active, lastUsage will be used to determine which groups
	 * should get deleted by the big administrator.
	 * 
	 * @param lastUsage
	 */
	public void setLastUsage(Date lastUsage);
	
	/**
	 * @return The associated resource
	 */
	public OLATResource getResource();
	
	public Group getBaseGroup();

	/**
	 * @return last usage of this group
	 */
	public Date getLastUsage();
	
	public boolean isOwnersVisibleIntern();

	public void setOwnersVisibleIntern(boolean visible);

	public boolean isParticipantsVisibleIntern();

	public void setParticipantsVisibleIntern(boolean visible);

	public boolean isWaitingListVisibleIntern();

	public void setWaitingListVisibleIntern(boolean visible);

	public boolean isOwnersVisiblePublic();

	public void setOwnersVisiblePublic(boolean visible);

	public boolean isParticipantsVisiblePublic();

	public void setParticipantsVisiblePublic(boolean visible);

	public boolean isWaitingListVisiblePublic();

	public void setWaitingListVisiblePublic(boolean visible);

	public boolean isDownloadMembersLists();

	public void setDownloadMembersLists(boolean downloadMembersLists);
	
	public boolean isAllowToLeave();
	
	public void setAllowToLeave(boolean allow);

	/**
	 * @return the maximal number of participants
	 */
	public Integer getMaxParticipants();

	/**
	 * @param maxParticipants the maximal number of participants
	 */
	public void setMaxParticipants(Integer maxParticipants);

	/**
	 * @return the minimal number of participants
	 */
	public Integer getMinParticipants();

	/**
	 * @param minParticipants the minimal number of participants
	 */
	public void setMinParticipants(Integer minParticipants);
	
	/**
	 * @return true: if the waiting list will automaticly close ranks to participant list 
	 */
	public Boolean getAutoCloseRanksEnabled();

	/**
	 * @param autoCloseRanksEnabled  true: enable automaticly close ranks form waiting list to participant list.
	 */	
	public void setAutoCloseRanksEnabled(Boolean autoCloseRanksEnabled);

	/**
	 * @return true: if waiting-list is enabled
	 */
	public Boolean getWaitingListEnabled();

	/**
	 * @param waitinglistEnabled  true: enable waiting list.
	 */	
	public void setWaitingListEnabled(Boolean waitingListEnabled);
}
