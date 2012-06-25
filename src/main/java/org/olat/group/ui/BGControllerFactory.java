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

package org.olat.group.ui;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.Windows;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.dtabs.DTab;
import org.olat.core.gui.control.generic.dtabs.DTabs;
import org.olat.core.id.OLATResourceable;
import org.olat.core.id.context.BusinessControlFactory;
import org.olat.core.id.context.ContextEntry;
import org.olat.core.logging.AssertException;
import org.olat.group.BusinessGroup;
import org.olat.group.context.BGContext;
import org.olat.group.ui.edit.BusinessGroupEditController;
import org.olat.group.ui.main.BGMainController;
import org.olat.group.ui.management.BGManagementController;
import org.olat.group.ui.run.BusinessGroupMainRunController;
import org.olat.resource.OLATResource;

/**
 * Description: <BR>
 * Factory to create all controllers needed to work with business groups. The
 * methods will configure generic run, edit and management controllers to make
 * the desired featureset available
 * <P>
 * Initial Date: Aug 19, 2004
 * 
 * @author patrick
 */

public class BGControllerFactory {

	private static BGControllerFactory INSTANCE = null;
	
	static {
		INSTANCE = new BGControllerFactory();
	}

	/**
	 * Use getInstance instead
	 */
	private BGControllerFactory() {
	//
	}

	/**
	 * @return business group controller factory
	 */
	public static BGControllerFactory getInstance() {
		return INSTANCE;
	}

	//
	// 1) Group edit controllers
	//

	/**
	 * Factory method to create a configured group edit controller
	 * 
	 * @param ureq
	 * @param wControl
	 * @param businessGroup
	 * @return an edit controller for this busines group
	 */
	public BusinessGroupEditController createEditControllerFor(UserRequest ureq, WindowControl wControl, BusinessGroup businessGroup) {
		String bgTyp = businessGroup.getType();
		if (BusinessGroup.TYPE_BUDDYGROUP.equals(bgTyp)) {
			BGConfigFlags flags = BGConfigFlags.createBuddyGroupDefaultFlags();
			return new BusinessGroupEditController(ureq, wControl, businessGroup, flags);
		} else if (BusinessGroup.TYPE_LEARNINGROUP.equals(bgTyp)) {
			BGConfigFlags flags = BGConfigFlags.createLearningGroupDefaultFlags();
			return new BusinessGroupEditController(ureq, wControl, businessGroup, flags);
		} else if (BusinessGroup.TYPE_RIGHTGROUP.equals(bgTyp)) {
			BGConfigFlags flags = BGConfigFlags.createRightGroupDefaultFlags();
			return new BusinessGroupEditController(ureq, wControl, businessGroup, flags);
		}
		// else
		throw new AssertException("unknown BusinessGroupType::" + bgTyp);
	}

	//
	// 2) Group run controllers
	//

	/**
	 * Factory method to create a configured group run controller
	 * 
	 * @param ureq
	 * @param wControl
	 * @param businessGroup
	 * @param isGMAdmin true if user is group management administrator
	 * @param initialViewIdentifier
	 * @return a run controller for this business group
	 */
	public BusinessGroupMainRunController createRunControllerFor(UserRequest ureq, WindowControl wControl, BusinessGroup businessGroup,
			boolean isGMAdmin, String initialViewIdentifier) {

		
		// build up the context path
		WindowControl bwControl;
		OLATResourceable businessOres = businessGroup;
		ContextEntry ce = BusinessControlFactory.getInstance().createContextEntry(businessOres);
		//OLAT-5944: check if the current context entry is not already the group entry to avoid duplicate in the business path
		if(ce.equals(wControl.getBusinessControl().getCurrentContextEntry())) {
			bwControl = wControl;
		} else {
			bwControl = BusinessControlFactory.getInstance().createBusinessWindowControl(ce, wControl);
		}
		
		
		String bgTyp = businessGroup.getType();
		if (BusinessGroup.TYPE_BUDDYGROUP.equals(bgTyp)) {
			BGConfigFlags flags = BGConfigFlags.createBuddyGroupDefaultFlags();
			flags.setEnabled(BGConfigFlags.IS_GM_ADMIN, false);
			return new BusinessGroupMainRunController(ureq, bwControl, businessGroup, flags, initialViewIdentifier);
		} else if (BusinessGroup.TYPE_LEARNINGROUP.equals(bgTyp)) {
			BGConfigFlags flags = BGConfigFlags.createLearningGroupDefaultFlags();
			flags.setEnabled(BGConfigFlags.IS_GM_ADMIN, isGMAdmin);
			return new BusinessGroupMainRunController(ureq, bwControl, businessGroup, flags, initialViewIdentifier);
		} else if (BusinessGroup.TYPE_RIGHTGROUP.equals(bgTyp)) {
			BGConfigFlags flags = BGConfigFlags.createRightGroupDefaultFlags();
			flags.setEnabled(BGConfigFlags.IS_GM_ADMIN, isGMAdmin);
			return new BusinessGroupMainRunController(ureq, bwControl, businessGroup, flags, initialViewIdentifier);
		}
		// else
		throw new AssertException("unknown BusinessGroupType::" + bgTyp);
	}

	/**
	 * Creates a runtime environment for this business group as a tab in the top
	 * navigation bar
	 * 
	 * @param businessGroup
	 * @param ureq
	 * @param wControl
	 * @param userActivityLogger The logger used to log the user activities or
	 *          null if no logger used
	 * @param isGMAdmin
	 * @param initialViewIdentifier
	 * @return BusinessGroupMainRunController or null if already initialized
	 */
	public BusinessGroupMainRunController createRunControllerAsTopNavTab(BusinessGroup businessGroup, UserRequest ureq,
			WindowControl wControl, boolean isGMAdmin, String initialViewIdentifier) {
		String displayName = businessGroup.getName();

		BusinessGroupMainRunController bgMrc = null;

		OLATResourceable ores = businessGroup;
		DTabs dts = (DTabs)Windows.getWindows(ureq).getWindow(ureq).getAttribute("DTabs");
		//was brasato:: DTabs dts = wControl.getDTabs();
		DTab dt = dts.getDTab(ores);
		if (dt == null) {
			// does not yet exist -> create and add
			dt = dts.createDTab(ores, displayName);
			// tabs full
			if (dt == null) return null;
			bgMrc = this.createRunControllerFor(ureq, dt.getWindowControl(), businessGroup, isGMAdmin, initialViewIdentifier);
			dt.setController(bgMrc);
			dts.addDTab(dt);
		}
		dts.activate(ureq, dt, "", null); // null: do not activate to a certain view
		return bgMrc;
	}

	//
	// group management controllers
	//

	/**
	 * Factory method to create a configured buddy group main controller for the
	 * management of the users own buddygroup
	 * 
	 * @param ureq
	 * @param wControl
	 * @param initialViewIdentifier
	 * @return a configured buddy group main controller
	 */
	public BGMainController createBuddyGroupMainController(UserRequest ureq, WindowControl wControl, String initialViewIdentifier) {
		return new BGMainController(ureq, wControl, initialViewIdentifier);
	}

	/**
	 * Factory method to create a configured group management controller for
	 * learning groups and right groups.
	 * 
	 * @param ureq
	 * @param wControl
	 * @param bgContext
	 * @param useBackLink
	 * @return a business group management controller for this group context
	 */
	public BGManagementController createManagementController(UserRequest ureq, WindowControl wControl, BGContext bgContext,
			boolean useBackLink) {
		if (bgContext == null) throw new AssertException("Group context must not be null");

		if (BusinessGroup.TYPE_LEARNINGROUP.equals(bgContext.getGroupType())) {
			return createLearningGroupManagementController(ureq, wControl, bgContext, useBackLink);
		} else if (BusinessGroup.TYPE_RIGHTGROUP.equals(bgContext.getGroupType())) {
			return createRightGroupManagementController(ureq, wControl, bgContext, useBackLink);
		} else {
			throw new AssertException("Can't handle group type ::" + bgContext.getGroupType());
		}
	}
	
	/**
	 * create Controller for new business group creation
	 * @param ureq
	 * @param wControl
	 * @param ual
	 * @param flags
	 * @param bgContext
	 * @param groupManager
	 * @return
	 */
	public NewBGController createNewBGController(UserRequest ureq, WindowControl wControl, boolean minMaxEnabled, OLATResource resource){
		return createNewBGController(ureq, wControl, minMaxEnabled, resource, true, null);
	}
	/**
	 * create controller for (mass) creation of business groups (bulkmode) with
	 * a group name(s) proposition.
	 * @param ureq
	 * @param wControl
	 * @param ual
	 * @param minMaxEnabled
	 * @param bgContext
	 * @param bulkMode 
	 * @param csvGroupNames
	 * @return
	 */
	public NewBGController createNewBGController(UserRequest ureq, WindowControl wControl, boolean minMaxEnabled, OLATResource resource, boolean bulkMode, String csvGroupNames){
		if (resource == null) throw new AssertException("Group context must not be null");
		NewBGController retVal = new NewBGController(ureq, wControl, minMaxEnabled, resource, bulkMode, csvGroupNames);
		return retVal;
	}
	

	private BGManagementController createLearningGroupManagementController(UserRequest ureq, WindowControl wControl, BGContext bgContext,
			boolean useBackLink) {
		// controller configuration
		BGConfigFlags flags = BGConfigFlags.createLearningGroupDefaultFlags();
		flags.setEnabled(BGConfigFlags.BACK_SWITCH, useBackLink);
		return new BGManagementController(ureq, wControl, bgContext, flags);
	}

	private BGManagementController createRightGroupManagementController(UserRequest ureq, WindowControl wControl, BGContext bgContext,
			boolean useBackLink) {
		BGConfigFlags flags = BGConfigFlags.createRightGroupDefaultFlags();
		flags.setEnabled(BGConfigFlags.BACK_SWITCH, useBackLink);
		return new BGManagementController(ureq, wControl, bgContext, flags);
	}

	/**
	 * a new area creation controller
	 * @param ureq
	 * @param wControl
	 * @param ual
	 * @param bgContext
	 * @return
	 */
	public NewAreaController createNewAreaController(UserRequest ureq, WindowControl wControl, OLATResource resource) {
		return createNewAreaController(ureq, wControl, resource, true, null);
	}
	
	/**
	 * a new area creation controller in bulkmode
	 * @param ureq
	 * @param wControl
	 * @param ual
	 * @param bgContext
	 * @param bulkMode
	 * @param csvNames
	 * @return
	 */
	public NewAreaController createNewAreaController(UserRequest ureq, WindowControl wControl, OLATResource resource, boolean bulkMode, String csvNames) {
		if (resource == null) throw new AssertException("Group resource must not be null");
		NewAreaController nac = new NewAreaController(ureq, wControl, resource, bulkMode, csvNames);
		return nac;
	}
	
}