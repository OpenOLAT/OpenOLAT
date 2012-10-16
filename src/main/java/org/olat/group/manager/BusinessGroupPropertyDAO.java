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

package org.olat.group.manager;

import org.olat.core.util.StringHelper;
import org.olat.group.BusinessGroup;
import org.olat.group.BusinessGroupModule;
import org.olat.properties.Property;
import org.olat.properties.PropertyConstants;
import org.olat.properties.PropertyManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Description:<BR>
 * Handles the displayMembers property assigned to BusinessGroups. It uses the
 * float column in the property table, and stores there a bitfield.<br>
 * The DisplayMembers Property is used to switch on - off exposing the owner
 * members, partipiciant members respectively, to the partipiciants of a
 * BusinessGroup.
 * <p>
 * Initial Date: Sep 22, 2004
 * 
 * @author patrick
 */
@Service("businessGroupPropertyManager")
public class BusinessGroupPropertyDAO {
	private static final String PROP_NAME = "displayMembers";
	private static final int showOwnersVal      = 1;// 0x..0001
	private static final int showPartipsVal     = 2;// 0x..0010
	private static final int showWaitingListVal = 4;// 0x..0100

	@Autowired
	private PropertyManager propertyManager;
	@Autowired
	private BusinessGroupModule groupModule;

	/**
	 * Creates and persists a new Property for the Display Members configuration
	 * according to the supplied booleans.
	 * 
	 * @param showOwners
	 * @param showPartips
	 * @return The generated property
	 */
	public Property createAndPersistDisplayMembers(BusinessGroup group, boolean showOwners, boolean showPartips, boolean showWaitingList) {
		long showXXX = 0;
		if (showOwners) showXXX += showOwnersVal;
		if (showPartips) showXXX += showPartipsVal;
		if (showWaitingList) showXXX += showWaitingListVal;
		
		Property prop = propertyManager.createPropertyInstance(null, group, group, PropertyConstants.OLATRESOURCE_CONFIGURATION, PROP_NAME, null, new Long(showXXX), null, null);
		propertyManager.saveProperty(prop);
		return prop;
	}

	/**
	 * updates an existing property, if it is not existing, i.e. called before the
	 * property was created and persisted, a null pointer exception occurs.
	 * 
	 * @param showOwners
	 * @param showPartips
	 */
	public void updateDisplayMembers(BusinessGroup group, boolean showOwners, boolean showPartips, boolean showWaitingList,
			boolean ownrsPublic, boolean partipsPublic, boolean waitingListPublic, boolean downloadLists) {
		long showXXX = 0;
		if (showOwners) showXXX += showOwnersVal;
		if (showPartips) showXXX += showPartipsVal;
		if (showWaitingList) showXXX += showWaitingListVal;
		
		long publicXXX = 0;
		if (ownrsPublic) publicXXX += showOwnersVal;
		if (partipsPublic) publicXXX += showPartipsVal;
		if (waitingListPublic) publicXXX += showWaitingListVal;
		
		float download = downloadLists ? 1.0f : 0.0f;
		
		Property property = findProperty(group);
		property.setLongValue(new Long(showXXX));
		property.setStringValue(Long.toString(publicXXX));
		property.setFloatValue(new Float(download));
		propertyManager.updateProperty(property);
	}

	/**
	 * delete the display members property
	 */
	public void deleteDisplayMembers(BusinessGroup group) {
		Property property = findProperty(group);
		propertyManager.deleteProperty(property);
	}

	/**
	 * true if Members can see the Owners, false otherwise. If the property is not
	 * existing, i.e. called before the property was created and persisted, a null
	 * pointer exception occurs.
	 * 
	 * @return true if group owners are configured to be displayed
	 */
	public boolean showOwners(Property prop) {
		return ((getDisplayMembersValue(prop) & showOwnersVal) == showOwnersVal);
	}

	/**
	 * true if Members can see the Participants, false otherwise. If the property
	 * is not existing, i.e. called before the property was created and persisted,
	 * a null pointer exception occurs.
	 * 
	 * @return true if group participants are configured to be displayed
	 */
	public boolean showPartips(Property prop) {
		return ((getDisplayMembersValue(prop) & showPartipsVal) == showPartipsVal);
	}

	/**
	 * true if Members can see the Waiting, false otherwise. If the property
	 * is not existing, i.e. called before the property was created and persisted,
	 * a null pointer exception occurs.
	 * 
	 * @return true if group participants are configured to be displayed
	 */
	public boolean showWaitingList(Property prop) {
		return ((getDisplayMembersValue(prop) & showWaitingListVal) == showWaitingListVal);
	}
	
	public boolean isOwnersPublic(Property prop) {
		return ((getPublicMembersValue(prop) & showOwnersVal) == showOwnersVal);
	}

	public boolean isPartipsPublic(Property prop) {
		return ((getPublicMembersValue(prop) & showPartipsVal) == showPartipsVal);
	}

	public boolean isWaitingListPublic(Property prop) {
		return ((getPublicMembersValue(prop) & showWaitingListVal) == showWaitingListVal);
	}
	
	public boolean isDownloadLists(Property prop) {
		Float val = prop.getFloatValue();
		if(val == null) {
			return groupModule.isUserListDownloadDefaultAllowed();//default
		}
		return 0.5f < val.floatValue();	
	}

	private int getDisplayMembersValue(Property prop) {
		int showXXX = prop.getLongValue().intValue();
		return showXXX;
	}
	
	private long getPublicMembersValue(Property prop) {
		String publicXXX = prop.getStringValue();
		if(StringHelper.isLong(publicXXX)) {
			return Long.parseLong(publicXXX);
		}
		return 0;
	}

	/**
	 * @return The group property. Either red from database or newly created.
	 */
	public Property findProperty(BusinessGroup group) {
		Property prop = propertyManager.findProperty(null, group, group, PropertyConstants.OLATRESOURCE_CONFIGURATION, PROP_NAME);
		// prop != null, otherwise the init of this businessGroup was incomplete
		// or the caller uses the function in the wrong way
		//
		// BugFix 986, http://bugzilla.olat.org/show_bug.cgi?id=986
		// above statement is still true, but as old groups are existing, which
		// return prop==null in this case, we decided to handle it as if both groups
		// are to show. This reproduces behaviour of OLAT before the code was added.
		// Furthermore if the property wass not existing one will be created.
		if (prop == null) {
			prop = createAndPersistDisplayMembers(group, true, true, true);
		}
		return prop;
	}

	/**
	 * @param sourceGroup The group from which the configuration should be copied
	 */
	public void copyConfigurationFromGroup(BusinessGroup sourceGroup, BusinessGroup targetGroup) {
		Property sourceGPM = findProperty(sourceGroup);
		boolean showOwners = showOwners(sourceGPM);
		boolean showPartips = showPartips(sourceGPM);
		boolean showWaitingList = showWaitingList(sourceGPM);
		boolean ownersPublic = isOwnersPublic(sourceGPM);
		boolean partipsPublic = isPartipsPublic(sourceGPM);
		boolean waitingListPublic = isWaitingListPublic(sourceGPM);
		boolean downloadLists = isDownloadLists(sourceGPM);
		updateDisplayMembers(targetGroup, showOwners, showPartips, showWaitingList,
				ownersPublic, partipsPublic, waitingListPublic,
				downloadLists);
	}
}