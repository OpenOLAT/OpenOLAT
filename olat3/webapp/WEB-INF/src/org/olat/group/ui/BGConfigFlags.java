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
* <p>
*/ 

package org.olat.group.ui;

import java.util.HashMap;
import java.util.Map;

import org.olat.instantMessaging.InstantMessagingModule;

/**
 * Description:<BR>
 * Objects of this class contain configration flags for the various business
 * group controllers. See the BGControllerFactory to see which group type has
 * which configuration.
 * <P>
 * Initial Date: Aug 25, 2004
 * 
 * @author gnaegi
 */
public class BGConfigFlags {

	/** owner group of group enabled */
	public static final String GROUP_OWNERS = "group_owners";
	/** at least one owner is required */
	public static final String GROUP_OWNER_REQURED = "group_owner_required";
	/** group areas enabled */
	public static final String AREAS = "areas";
	/** group rights enabled */
	public static final String RIGHTS = "rights";
	/** group min / max number of participants enabled */
	public static final String GROUP_MINMAX_SIZE = "group_minmax_size";
	/** collaboration tools for goups enabled */
	public static final String GROUP_COLLABTOOLS = "group_collabtools";

	/** create group functionality enabled */
	public static final String GROUPS_CREATE = "groups_create";
	/** modify group functionality enabled */
	public static final String GROUPS_MODIFY = "groups_modify";
	/** delete group functionality enabled */
	public static final String GROUPS_DELETE = "groups_delete";

	/** create area functionality enabled */
	public static final String AREAS_CREATE = "areas_create";
	/** modify area functionality enabled */
	public static final String AREAS_MODIFY = "areas_modify";
	/** delete area functionality enabled */
	public static final String AREAS_DELETE = "areas_delete";

	/** modify rights functionality enabled */
	public static final String RIGHTS_MODIFY = "rights_modify";

	/** true if the current user is group management administrator */
	public static final String IS_GM_ADMIN = "is.gm.admin";

	/**
	 * true if the current admin user is allowed to see all userdata. If set to
	 * true the user can change the table columns of the user tables and for
	 * example see the users institutional id. If set to false, only the name,
	 * firstname, login and email is revealed
	 */
	public static final String ADMIN_SEE_ALL_USER_DATA = "admin.see.all.user.data";

	/** GUI option: show or hide back-link in tools * */
	public static final String BACK_SWITCH = "back_switch";

	/** instant messaging option: synchronize users as buddylist * */
	public static final String BUDDYLIST = "buddylist";

	/** runtime option: show associated resources / courses * */
	public static final String SHOW_RESOURCES = "show_resources";

	private Map<String, Boolean> flags;

	/**
	 * Constructor for a business group configuration flag object
	 */
	private BGConfigFlags() {
		super();
		flags = new HashMap<String, Boolean>();
	}

	/**
	 * @param flag The configuration flag name
	 * @param enabled true: enabled, flase: disabled
	 */
	public void setEnabled(String flag, boolean enabled) {
		flags.put(flag, Boolean.valueOf(enabled));
	}

	/**
	 * @param flag The configuration flag name
	 * @return true if enabled, false otherwhise
	 */
	public boolean isEnabled(String flag) {
		Boolean result = flags.get(flag);
		if (result == null) return false;
		return result.booleanValue();
	}

	/**
	 * Factory method to create the default group configuration object
	 * 
	 * @return BGConfigFlags
	 */
	public static BGConfigFlags createBuddyGroupDefaultFlags() {
		BGConfigFlags bgFlags = new BGConfigFlags();
		bgFlags.setEnabled(BGConfigFlags.BUDDYLIST, true);
		bgFlags.setEnabled(BGConfigFlags.GROUP_OWNERS, true);
		bgFlags.setEnabled(BGConfigFlags.GROUP_OWNER_REQURED, true);
		bgFlags.setEnabled(BGConfigFlags.GROUP_MINMAX_SIZE, false);
		bgFlags.setEnabled(BGConfigFlags.GROUP_COLLABTOOLS, true);
		bgFlags.setEnabled(BGConfigFlags.AREAS, false);
		bgFlags.setEnabled(BGConfigFlags.RIGHTS, false);
		bgFlags.setEnabled(BGConfigFlags.SHOW_RESOURCES, false);

		// security flags
		// TODO use callback here to make this right dependent
		bgFlags.setEnabled(BGConfigFlags.GROUPS_CREATE, true);
		bgFlags.setEnabled(BGConfigFlags.GROUPS_MODIFY, true);
		bgFlags.setEnabled(BGConfigFlags.GROUPS_DELETE, true);
		bgFlags.setEnabled(BGConfigFlags.AREAS_CREATE, false);
		bgFlags.setEnabled(BGConfigFlags.AREAS_MODIFY, false);
		bgFlags.setEnabled(BGConfigFlags.AREAS_DELETE, false);
		bgFlags.setEnabled(BGConfigFlags.RIGHTS_MODIFY, false);
		bgFlags.setEnabled(BGConfigFlags.ADMIN_SEE_ALL_USER_DATA, false);

		return bgFlags;
	}

	/**
	 * Factory method to create the default group configuration object
	 * 
	 * @return BGConfigFlags
	 */
	public static BGConfigFlags createLearningGroupDefaultFlags() {
		BGConfigFlags bgFlags = new BGConfigFlags();
		//only sync learning groups with IM server if enabled in olat config file
		bgFlags.setEnabled(BGConfigFlags.BUDDYLIST, InstantMessagingModule.isSyncLearningGroups());
		bgFlags.setEnabled(BGConfigFlags.GROUP_OWNERS, true);
		bgFlags.setEnabled(BGConfigFlags.GROUP_OWNER_REQURED, false);
		bgFlags.setEnabled(BGConfigFlags.GROUP_MINMAX_SIZE, true);
		bgFlags.setEnabled(BGConfigFlags.GROUP_COLLABTOOLS, true);
		bgFlags.setEnabled(BGConfigFlags.AREAS, true);
		bgFlags.setEnabled(BGConfigFlags.RIGHTS, false);
		bgFlags.setEnabled(BGConfigFlags.SHOW_RESOURCES, true);

		// security flags
		// TODO use callback here to make this right dependent
		bgFlags.setEnabled(BGConfigFlags.GROUPS_CREATE, true);
		bgFlags.setEnabled(BGConfigFlags.GROUPS_MODIFY, true);
		bgFlags.setEnabled(BGConfigFlags.GROUPS_DELETE, true);
		bgFlags.setEnabled(BGConfigFlags.AREAS_CREATE, true);
		bgFlags.setEnabled(BGConfigFlags.AREAS_MODIFY, true);
		bgFlags.setEnabled(BGConfigFlags.AREAS_DELETE, true);
		bgFlags.setEnabled(BGConfigFlags.RIGHTS_MODIFY, false);
		bgFlags.setEnabled(BGConfigFlags.ADMIN_SEE_ALL_USER_DATA, true);

		return bgFlags;
	}

	/**
	 * Factory method to create the default group configuration object
	 * 
	 * @return BGConfigFlags
	 */
	public static BGConfigFlags createRightGroupDefaultFlags() {
		BGConfigFlags bgFlags = new BGConfigFlags();
		bgFlags.setEnabled(BGConfigFlags.BUDDYLIST, false);
		bgFlags.setEnabled(BGConfigFlags.GROUP_OWNERS, false);
		bgFlags.setEnabled(BGConfigFlags.GROUP_OWNER_REQURED, false);
		bgFlags.setEnabled(BGConfigFlags.GROUP_MINMAX_SIZE, false);
		bgFlags.setEnabled(BGConfigFlags.GROUP_COLLABTOOLS, true);
		bgFlags.setEnabled(BGConfigFlags.AREAS, false);
		bgFlags.setEnabled(BGConfigFlags.RIGHTS, true);
		bgFlags.setEnabled(BGConfigFlags.SHOW_RESOURCES, true);

		// security flags
		// TODO use callback here to make this right dependent
		bgFlags.setEnabled(BGConfigFlags.GROUPS_CREATE, true);
		bgFlags.setEnabled(BGConfigFlags.GROUPS_MODIFY, true);
		bgFlags.setEnabled(BGConfigFlags.GROUPS_DELETE, true);
		bgFlags.setEnabled(BGConfigFlags.AREAS_CREATE, false);
		bgFlags.setEnabled(BGConfigFlags.AREAS_MODIFY, false);
		bgFlags.setEnabled(BGConfigFlags.AREAS_DELETE, false);
		bgFlags.setEnabled(BGConfigFlags.RIGHTS_MODIFY, true);
		bgFlags.setEnabled(BGConfigFlags.ADMIN_SEE_ALL_USER_DATA, false);

		return bgFlags;
	}

}
