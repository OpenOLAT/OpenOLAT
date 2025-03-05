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

import org.olat.core.commons.fullWebApp.popup.BaseFullWebappPopupLayoutFactory;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.gui.control.creator.ControllerCreator;
import org.olat.core.gui.control.generic.popup.PopupBrowserWindow;
import org.olat.core.id.Identity;
import org.olat.core.id.OLATResourceable;
import org.olat.core.util.UserSession;
import org.olat.core.util.coordinate.CoordinatorManager;
import org.olat.core.util.event.GenericEventListener;
import org.olat.core.util.resource.OresHelper;
import org.olat.user.UserPortraitComponent.PortraitSize;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Initial Date:  Sept 08, 2005
 *
 * @author Alexander Schneider
 * 
 */
public class DisplayPortraitController extends BasicController implements GenericEventListener {
	
	private final VelocityContainer mainVC;
	private final UserPortraitComponent portraitComp;

	private final Identity portraitIdent;
	private final boolean isDeletedUser;
	private final String avatarBaseURL;
	private final UserAvatarMapper mapper;
	private OLATResourceable listenerOres;
	
	@Autowired
	private UserPortraitService userPortraitService;

	public DisplayPortraitController(UserRequest ureq, WindowControl wControl, Identity portraitIdent,
			PortraitSize portraitSize, boolean canLinkToHomePage) { 
		super(ureq, wControl);
		this.portraitIdent = portraitIdent;
		this.isDeletedUser = portraitIdent != null? portraitIdent.getStatus().equals(Identity.STATUS_DELETED): false;
		
		mainVC = createVelocityContainer("displayportrait");
		putInitialPanel(mainVC);
		
		// export data doesn't have a session, web catalog doesn't have roles
		UserSession usess = ureq.getUserSession();
		boolean isAnonymous = portraitIdent == null || (usess != null && usess.getRoles() != null && usess.getRoles().isGuestOnly());
		
		mainVC.contextPut("canLinkToHomePage", (canLinkToHomePage && !isDeletedUser & !isAnonymous) ? Boolean.TRUE : Boolean.FALSE);
		
		boolean useLarge = PortraitSize.medium == portraitSize || PortraitSize.large == portraitSize;
		mapper = new UserAvatarMapper(useLarge);
		avatarBaseURL = registerCacheableMapper(ureq, "avatars-members", mapper);
		
		if (portraitIdent != null && portraitIdent.getKey() != null) {
			mainVC.contextPut("identityKey", portraitIdent.getKey().toString());
		}
		
		portraitComp = UserPortraitFactory.createUserPortrait("portrait", mainVC, getLocale(), avatarBaseURL);
		portraitComp.setSize(portraitSize);
		portraitComp.setDisplayPresence(false);
		
		loadPortrait();
		
		if (portraitIdent != null && portraitIdent.getKey() != null) {
			listenerOres = OresHelper.createOLATResourceableInstance("portrait", portraitIdent.getKey());
			CoordinatorManager.getInstance().getCoordinator().getEventBus().registerFor(this, portraitIdent, listenerOres);
		}
	}
	
	@Override
	protected void doDispose() {
		if (listenerOres != null) {
			CoordinatorManager.getInstance().getCoordinator().getEventBus().deregisterFor(this, listenerOres);
		}
		super.doDispose();
	}
	
	private void loadPortrait() {
		PortraitUser portraitUser = userPortraitService.createPortraitUser(portraitIdent);
		portraitComp.setPortraitUser(portraitUser);
	}
	
	@Override
	public void event(Event event) {
		if("changed-portrait".equals(event.getCommand()) && event instanceof ProfileEvent) {
			try {
				ProfileEvent pe = (ProfileEvent)event;
				if(portraitIdent.getKey().equals(pe.getIdentityKey())) {
					loadPortrait();
					mainVC.setDirty(true);
				}
			} catch (Exception e) {
				logError("", e);
			}
		}
	}

	@Override
	public void event(UserRequest ureq, Component source, Event event) {
		if (source == mainVC) {
			if (event.getCommand().equals("showuserinfo")) {
				showUserInfo(ureq);
			}
		}
	}

	private void showUserInfo(UserRequest ureq) {
		ControllerCreator ctrlCreator = new ControllerCreator() {
			@Override
			public Controller createController(UserRequest lureq, WindowControl lwControl) {
				return new UserInfoMainController(lureq, lwControl, portraitIdent, true, false);
			}
		};
		ControllerCreator layoutCtrlr = BaseFullWebappPopupLayoutFactory.createAuthMinimalPopupLayout(ureq, ctrlCreator);
		PopupBrowserWindow pbw = getWindowControl().getWindowBackOffice().getWindowManager().createNewPopupBrowserWindowFor(ureq, layoutCtrlr);
		pbw.open(ureq);
	}
}