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

package org.olat.user;

import java.util.List;

import org.olat.basesecurity.BaseSecurityManager;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.segmentedview.SegmentViewComponent;
import org.olat.core.gui.components.segmentedview.SegmentViewEvent;
import org.olat.core.gui.components.segmentedview.SegmentViewFactory;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.gui.control.generic.dtabs.Activateable2;
import org.olat.core.id.Identity;
import org.olat.core.id.Roles;
import org.olat.core.id.context.ContextEntry;
import org.olat.core.id.context.StateEntry;
import org.olat.login.SupportsAfterLoginInterceptor;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Initial Date: Jul 14, 2005
 * 
 * @author Alexander Schneider
 * 
 *         Comment:
 */
public class ProfileAndHomePageEditController extends BasicController implements Activateable2, SupportsAfterLoginInterceptor {

	@Autowired
	private RolesAndDelegationsControllerFactory rolesAndDelegationsControllerFactory;

	@Autowired
	private BaseSecurityManager baseSecurityManager;

	private final Roles userRoles;
	private final VelocityContainer myContent;
	private final Link profilLink, homePageLink, rolesAndDelegationsLink;
	private final SegmentViewComponent segmentView;
	private ProfileFormController profileFormController;
	private HomePageSettingsController homePageController;
	private RolesAndDelegationsController rolesAndDelegationsController;

	private Identity identityToModify;
	private boolean isAdministrativeUser;

	public ProfileAndHomePageEditController(UserRequest ureq, WindowControl wControl) {		
		this(ureq,wControl, ureq.getIdentity(), false);
	}
	
	/**
	 * @param ureq
	 * @param wControl
	 * @param identityToModify the identity to be changed. Can be different than current
	 *          user (usermanager that edits another users profile)
	 * @param isAdministrativeUser
	 */
	public ProfileAndHomePageEditController(UserRequest ureq, WindowControl wControl, Identity identityToModify, boolean isAdministrativeUser) {
		super(ureq, wControl);
		this.identityToModify = identityToModify;
		this.isAdministrativeUser = isAdministrativeUser;
		setTranslator(UserManager.getInstance().getPropertyHandlerTranslator(getTranslator()));

		myContent = createVelocityContainer("homepage");

		// User roles
		userRoles = baseSecurityManager.getRoles(identityToModify);

		// Profile tab
		segmentView = SegmentViewFactory.createSegmentView("segments", myContent, this);
		profilLink = LinkFactory.createLink("tab.profile", myContent, this);
		profilLink.setElementCssClass("o_sel_usersettings_profile");
		segmentView.addSegment(profilLink, true);

		// Homepage tab
		homePageLink = LinkFactory.createLink("tab.hp", myContent, this);
		homePageLink.setElementCssClass("o_sel_usersettings_homepage");
		segmentView.addSegment(homePageLink, false);

		// Roles and delegations tab
		rolesAndDelegationsLink = LinkFactory.createLink("tab.roles.and.delegations", myContent, this);
		rolesAndDelegationsLink.setElementCssClass("o_sel_usersettings_rolesAndDelegations");

		// Display roles and delegations tab only in case the user has some roles (except for invitee and guestOnly)
		if (userRoles.isOLATAdmin() || userRoles.isAuthor() || userRoles.isGroupManager()
				|| userRoles.isUserManager() || userRoles.isInstitutionalResourceManager() || userRoles.isPoolAdmin()) {
			segmentView.addSegment(rolesAndDelegationsLink, false);
		} else {
			rolesAndDelegationsLink.setVisible(false);
		}

		putInitialPanel(myContent);
		
		doOpenProfile(ureq);
	}

	@Override
	protected void doDispose() {
		// controllers disposed by basic controller
	}
	
	@Override
	public boolean isUserInteractionRequired(UserRequest ureq) {
		return !(ureq.getUserSession().getRoles() == null
				|| ureq.getUserSession().getRoles().isInvitee()
				|| ureq.getUserSession().getRoles().isGuestOnly());
	}

	@Override
	public void event(UserRequest ureq, Component source, Event event) {
		if(source == segmentView) {
			if(event instanceof SegmentViewEvent) {
				SegmentViewEvent sve = (SegmentViewEvent)event;
				String segmentCName = sve.getComponentName();
				Component clickedLink = myContent.getComponent(segmentCName);
				Controller selectedController = null;
				if (clickedLink == profilLink) {
					selectedController = doOpenProfile(ureq);
				} else if (clickedLink == homePageLink){
					selectedController = doOpenHomePageSettings(ureq);
				} else if (clickedLink == rolesAndDelegationsLink) {
					selectedController = doOpenRoles(ureq);
				}
				addToHistory(ureq, selectedController);
			}
		}
	}

	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if(profileFormController == source) {
			identityToModify = profileFormController.getIdentityToModify();
			if(Event.CANCELLED_EVENT.equals(event)) {
				resetForm(ureq);
			}
			if(homePageController != null) {
				homePageController.updateIdentityToModify(ureq, identityToModify);
			}
			if(event == Event.DONE_EVENT) {
				fireEvent(ureq, event);
			}
		}
		super.event(ureq, source, event);
	}
	
	public void resetForm(UserRequest ureq) {
		removeAsListenerAndDispose(profileFormController);
		removeAsListenerAndDispose(homePageController);
		removeAsListenerAndDispose(rolesAndDelegationsController);
		profileFormController = null;
		homePageController = null;
		rolesAndDelegationsController = null;
		doOpenProfile(ureq);
	}

	@Override
	public void activate(UserRequest ureq, List<ContextEntry> entries, StateEntry state) {
		if(entries == null || entries.isEmpty()) {
			if(profileFormController == null) {
				doOpenProfile(ureq);
			}
		}
	}

	private ProfileFormController doOpenProfile(UserRequest ureq) {
		if(profileFormController == null) {
			profileFormController = new ProfileFormController(ureq, getWindowControl(),
					identityToModify, isAdministrativeUser, true);
			listenTo(profileFormController);
		}

		myContent.put("segmentCmp", profileFormController.getInitialComponent());
		return profileFormController;
	}
	
	private HomePageSettingsController doOpenHomePageSettings(UserRequest ureq) {
		if(homePageController == null) {
			homePageController = new HomePageSettingsController(ureq, getWindowControl(), identityToModify, isAdministrativeUser);
			listenTo(homePageController);
		}

		myContent.put("segmentCmp", homePageController.getInitialComponent());
		return homePageController;
	}

	private RolesAndDelegationsController doOpenRoles(UserRequest ureq) {
		if (rolesAndDelegationsController == null) {
			rolesAndDelegationsController = rolesAndDelegationsControllerFactory.create(ureq, getWindowControl(), identityToModify, userRoles);
			listenTo(rolesAndDelegationsController);
		}

		myContent.put("segmentCmp", rolesAndDelegationsController.getInitialComponent());
		return rolesAndDelegationsController;
	}
}
