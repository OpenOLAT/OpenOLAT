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
import org.olat.core.id.OLATResourceable;
import org.olat.core.id.context.ContextEntry;
import org.olat.core.id.context.StateEntry;
import org.olat.core.util.Util;
import org.olat.core.util.resource.OresHelper;
import org.olat.login.SupportsAfterLoginInterceptor;
import org.olat.modules.immunityproof.ImmunityProof;
import org.olat.modules.immunityproof.ImmunityProofModule;
import org.olat.modules.immunityproof.ui.ImmunityProofCardController;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Initial Date: Jul 14, 2005
 * 
 * @author Alexander Schneider
 * 
 *         Comment:
 */
public class ProfileAndHomePageEditController extends BasicController implements Activateable2, SupportsAfterLoginInterceptor {

	private final VelocityContainer myContent;
	private final Link profilLink, homePageLink, immunityProofLink;
	private final SegmentViewComponent segmentView;
	private ProfileFormController profileFormController;
	private HomePageSettingsController homePageController;
	private ImmunityProofCardController immunityProofUserProfileController;

	private Identity identityToModify;
	private boolean isAdministrativeUser;
	private boolean showImmunityProof;
	
	@Autowired
	private ImmunityProofModule immunityProofModule;

	public ProfileAndHomePageEditController(UserRequest ureq, WindowControl wControl) {		
		this(ureq,wControl, ureq.getIdentity(), false);
	}
	
	/**
	 * @param ureq
	 * @param wControl
	 * @param identity the identity to be changed. Can be different than current
	 *          user (usermanager that edits another users profile)
	 * @param isAdministrativeUser
	 */
	public ProfileAndHomePageEditController(UserRequest ureq, WindowControl wControl, Identity identityToModify, boolean isAdministrativeUser) {
		super(ureq, wControl);
		this.identityToModify = identityToModify;
		this.isAdministrativeUser = isAdministrativeUser;
		this.showImmunityProof = identityToModify.equals(getIdentity());
		setTranslator(UserManager.getInstance().getPropertyHandlerTranslator(getTranslator()));
		setTranslator(Util.createPackageTranslator(ImmunityProof.class, getLocale(), getTranslator()));
		
		myContent = createVelocityContainer("homepage");
		segmentView = SegmentViewFactory.createSegmentView("segments", myContent, this);
		
		profilLink = LinkFactory.createLink("tab.profile", myContent, this);
		profilLink.setElementCssClass("o_sel_usersettings_profile");
		segmentView.addSegment(profilLink, true);
		
		homePageLink = LinkFactory.createLink("tab.hp", myContent, this);
		homePageLink.setElementCssClass("o_sel_usersettings_homepage");
		segmentView.addSegment(homePageLink, false);
		
		immunityProofLink = LinkFactory.createLink("immunity.proof", myContent, this);
		immunityProofLink.setElementCssClass("o_sel_immunity_proof");
		if (immunityProofModule.isEnabled() && showImmunityProof) {
			segmentView.addSegment(immunityProofLink, false);
		}

		putInitialPanel(myContent);
		
		doOpenProfile(ureq);
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
				} else if (clickedLink == immunityProofLink) {
					selectedController = doOpenImmunityProof(ureq);
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
				resetForm(ureq, identityToModify);
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
	
	public void resetForm(UserRequest ureq, Identity identity) {
		this.identityToModify = identity;
		removeAsListenerAndDispose(profileFormController);
		removeAsListenerAndDispose(homePageController);
		profileFormController = null;
		homePageController = null;
		doOpenProfile(ureq);
	}

	@Override
	public void activate(UserRequest ureq, List<ContextEntry> entries, StateEntry state) {
		if(entries == null || entries.isEmpty()) {
			doOpenProfile(ureq);
		} else {
			String type = entries.get(0).getOLATResourceable().getResourceableTypeName();
			if("Profile".equalsIgnoreCase(type)) {
				doOpenProfile(ureq);
			} else if("HomePage".equalsIgnoreCase(type)) {
				doOpenHomePageSettings(ureq);
			} else if("CovidCertificate".equalsIgnoreCase(type) && showImmunityProof) {
				doOpenImmunityProof(ureq);
			} else {
				doOpenProfile(ureq);
			}
		}
	}

	private ProfileFormController doOpenProfile(UserRequest ureq) {
		if(profileFormController == null) {
			OLATResourceable ores = OresHelper.createOLATResourceableInstance("Profile", 0l);
            WindowControl bwControl = addToHistory(ureq, ores, null);
			profileFormController = new ProfileFormController(ureq, bwControl,
					identityToModify, isAdministrativeUser, true);
			listenTo(profileFormController);
		} 

		myContent.put("segmentCmp", profileFormController.getInitialComponent());
		myContent.contextPut("showNote", true);
		
		segmentView.select(profilLink);
		return profileFormController;
	}
	
	private HomePageSettingsController doOpenHomePageSettings(UserRequest ureq) {
		if(homePageController == null) {
			OLATResourceable ores = OresHelper.createOLATResourceableInstance("HomePage", 0l);
            WindowControl bwControl = addToHistory(ureq, ores, null);
			homePageController = new HomePageSettingsController(ureq, bwControl, identityToModify, isAdministrativeUser);
			listenTo(homePageController);
		} 

		myContent.put("segmentCmp", homePageController.getInitialComponent());
		myContent.contextPut("showNote", true);
		
		segmentView.select(homePageLink);
		return homePageController;
	}
	
	private ImmunityProofCardController doOpenImmunityProof(UserRequest ureq) {
		// Always create new controller
		OLATResourceable ores = OresHelper.createOLATResourceableInstance("CovidCertificate", 0l);
        WindowControl bwControl = addToHistory(ureq, ores, null);
		immunityProofUserProfileController = new ImmunityProofCardController(ureq, bwControl, getIdentity(), true);
		listenTo(immunityProofUserProfileController);
		
		myContent.put("segmentCmp", immunityProofUserProfileController.getInitialComponent());
		myContent.contextPut("showNote", false);
		
		segmentView.select(immunityProofLink);
		return immunityProofUserProfileController;
	}
}
