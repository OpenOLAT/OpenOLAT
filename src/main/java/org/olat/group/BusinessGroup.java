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

import org.olat.basesecurity.SecurityGroup;
import org.olat.core.id.CreateInfo;
import org.olat.core.id.ModifiedInfo;
import org.olat.core.id.OLATResourceable;
import org.olat.core.id.Persistable;

/**
 * Initial Date: Aug 2, 2004
 * 
 * @author gnaegi<br>
 *         Comment: All OLAT business group implementation share this interface.
 *         Examples are the buddygroups or the learning groups
 */
public interface BusinessGroup extends Persistable, CreateInfo, ModifiedInfo, OLATResourceable {

	/** group type: buddygroup * */
	//public final static String TYPE_BUDDYGROUP = "BuddyGroup";
	/** group type: learning group * */
	//public final static String TYPE_LEARNINGROUP = "LearningGroup";
	/** group type: course right group * */
	//public final static String TYPE_RIGHTGROUP = "RightGroup";
	/** regular expression to check for valid group names */
	// commas are not allowed. name is used in course conditions for weak binding
	public final static String VALID_GROUPNAME_REGEXP = "^[^,\"]*$";
	
	/** the max length for the group name.*/
	public static final int MAX_GROUP_NAME_LENGTH = 255;

	/**
	 * @return The group type identification
	 */
	public String getType();

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
	 * BusinessGroup was active, lastUsage will be used to determine which groups
	 * should get deleted by the big administrator.
	 * 
	 * @param lastUsage
	 */
	public void setLastUsage(Date lastUsage);

	/**
	 * @param description the description of this group. Might be NULL
	 */
	public void setDescription(String description);

	/**
	 * The BusinessGroup has 1..n Owners acting as <i>administrators </i>.
	 * 
	 * @return the owners
	 */
	SecurityGroup getOwnerGroup();

	/**
	 * The BusinessGroup has 0..n Partipiciants.
	 * 
	 * @return the partipiciants
	 */
	SecurityGroup getPartipiciantGroup();

	/**
	 * The BusinessGroup has 0..n people in the waiting group.
	 * 
	 * @return the waiting group
	 */
	public SecurityGroup getWaitingGroup();

	/**
	 * @return last usage of this group
	 */
	public Date getLastUsage();

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

	/**
	 * @param waitingGroup  New waiting group.
	 */	
	public void setWaitingGroup(SecurityGroup waitingGroup);
	
}
