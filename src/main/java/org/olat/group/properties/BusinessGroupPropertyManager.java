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

package org.olat.group.properties;

import org.olat.core.commons.persistence.DBFactory;
import org.olat.core.manager.BasicManager;
import org.olat.group.BusinessGroup;
import org.olat.properties.NarrowedPropertyManager;
import org.olat.properties.Property;
import org.olat.properties.PropertyConstants;

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

public class BusinessGroupPropertyManager extends BasicManager {
	private static String PROP_NAME = "displayMembers";
	private int showOwnersVal      = 1;// 0x..0001
	private int showPartipsVal     = 2;// 0x..0010
	private int showWaitingListVal = 4;// 0x..0100

	// 0x..1000
	// next used int values should be  8, 16, ....
	//
	private BusinessGroup businessGroup;
	private NarrowedPropertyManager npm;
	private Property myProperty; // local copy

	/**
	 * Create a new BusinessGroupPropertyManager which instantiates a
	 * NarrowedPropertyManager, narrowed to the supplied BusinessGroup.
	 * 
	 * @param bg BusinessGroup not null.
	 */
	public BusinessGroupPropertyManager(BusinessGroup bg) {
		this.businessGroup = bg;
		this.npm = NarrowedPropertyManager.getInstance(businessGroup);
	}

	/**
	 * Creates and persists a new Property for the Display Members configuration
	 * according to the supplied booleans.
	 * 
	 * @param showOwners
	 * @param showPartips
	 * @return The generated property
	 */
	public Property createAndPersistDisplayMembers(boolean showOwners, boolean showPartips, boolean showWaitingList) {
		long showXXX = 0;
		if (showOwners) showXXX += showOwnersVal;
		if (showPartips) showXXX += showPartipsVal;
		if (showWaitingList) showXXX += showWaitingListVal;
		Property prop = npm.createPropertyInstance(null, this.businessGroup, PropertyConstants.OLATRESOURCE_CONFIGURATION, PROP_NAME, null,
				new Long(showXXX), null, null);
		npm.saveProperty(prop);
		this.myProperty = prop;
		return prop;
	}

	/**
	 * updates an existing property, if it is not existing, i.e. called before the
	 * property was created and persisted, a null pointer exception occurs.
	 * 
	 * @param showOwners
	 * @param showPartips
	 */
	public void updateDisplayMembers(boolean showOwners, boolean showPartips, boolean showWaitingList) {
		long showXXX = 0;
		if (showOwners) showXXX += showOwnersVal;
		if (showPartips) showXXX += showPartipsVal;
		if (showWaitingList) showXXX += showWaitingListVal;
		if (this.myProperty == null) {
			this.myProperty = findProperty();
		} else {
			// reload object to prevent stale object exception
			this.myProperty = (Property) DBFactory.getInstance().loadObject(this.myProperty);
		}
		this.myProperty.setLongValue(new Long(showXXX));
		npm.updateProperty(this.myProperty);
	}

	/**
	 * delete the display members property
	 */
	public void deleteDisplayMembers() {
		if (this.myProperty == null) {
			this.myProperty = findProperty();
		}
		npm.deleteProperty(myProperty);
		this.myProperty = null;
	}

	/**
	 * true if Members can see the Owners, false otherwise. If the property is not
	 * existing, i.e. called before the property was created and persisted, a null
	 * pointer exception occurs.
	 * 
	 * @return true if group owners are configured to be displayed
	 */
	public boolean showOwners() {
		return ((getDisplayMembersValue() & showOwnersVal) == showOwnersVal);
	}

	/**
	 * true if Members can see the Partipiciants, false otherwise. If the property
	 * is not existing, i.e. called before the property was created and persisted,
	 * a null pointer exception occurs.
	 * 
	 * @return true if group participants are configured to be displayed
	 */
	public boolean showPartips() {
		return ((getDisplayMembersValue() & showPartipsVal) == showPartipsVal);
	}

	private int getDisplayMembersValue() {
		if (this.myProperty == null) {
			this.myProperty = findProperty();
		}
		//
		int showXXX = this.myProperty.getLongValue().intValue();
		return showXXX;
	}

	/**
	 * @return The group property. Either red from database or newly created.
	 */
	private Property findProperty() {
		Property prop = npm.findProperty(null, businessGroup, PropertyConstants.OLATRESOURCE_CONFIGURATION, PROP_NAME);
		// prop != null, otherwise the init of this businessGroup was incomplete
		// or the caller uses the function in the wrong way
		//
		// BugFix 986, http://bugzilla.olat.org/show_bug.cgi?id=986
		// above statement is still true, but as old groups are existing, which
		// return prop==null in this case, we decided to handle it as if both groups
		// are to show. This reproduces behaviour of OLAT before the code was added.
		// Furthermore if the property wass not existing one will be created.
		if (prop == null) {
			prop = createAndPersistDisplayMembers(true, true, true);
		}
		return prop;
	}

	/**
	 * @param sourceGroup The group from which the configuration should be copied
	 */
	public void copyConfigurationFromGroup(BusinessGroup sourceGroup) {
		BusinessGroupPropertyManager sourceGPM = new BusinessGroupPropertyManager(sourceGroup);
		updateDisplayMembers(sourceGPM.showOwners(), sourceGPM.showPartips(), sourceGPM.showWaitingList() );
	}

	/**
	 * true if Members can see the Waiting, false otherwise. If the property
	 * is not existing, i.e. called before the property was created and persisted,
	 * a null pointer exception occurs.
	 * 
	 * @return true if group participants are configured to be displayed
	 */
	public boolean showWaitingList() {
		return ((getDisplayMembersValue() & showWaitingListVal) == showWaitingListVal);
	}

}
