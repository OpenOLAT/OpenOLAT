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

import java.io.File;

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
import org.olat.core.id.UserConstants;
import org.olat.core.util.StringHelper;
import org.olat.core.util.UserSession;
import org.olat.core.util.coordinate.CoordinatorManager;
import org.olat.core.util.event.GenericEventListener;
import org.olat.core.util.resource.OresHelper;
import org.olat.user.propertyhandlers.GenderPropertyHandler;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Initial Date:  Sept 08, 2005
 *
 * Displays a Portrait-Image and/or Full Name of a given User (Identity).<br />
 * the portrait and fullname can be linked to the VCard (which opens in a new browser window / popup)
 * 
 * @author Alexander Schneider
 * 
 */
public class DisplayPortraitController extends BasicController implements GenericEventListener {
	
	private VelocityContainer myContent;
	private Identity portraitIdent;
	
	private final String mapperPath;
	private final UserAvatarMapper mapper;
	private final OLATResourceable listenerOres;
	
	private final boolean useLarge;
	private final boolean isAnonymous;
	private final boolean isDeletedUser;
	private final boolean displayPortraitImage;
	
	private boolean forceAnonymous;	
	
	@Autowired
	private DisplayPortraitManager displayPortraitManager;

	
	/**
	 * Use for HighscoreRunController, where choice of CSS CLASS needs to be 
	 * manipulated based on HighscoreEditController setting.
 	 * will display portrait, no username
	 *
	 * @param ureq
	 * @param wControl
	 * @param portraitIdent
	 *            the identity to display
	 * @param useLarge
	 *            if set to true, the portrait-image is displayed as "big"
	 * @param canLinkToHomePage
	 *            if set to true, the portrait is linked to the users homepage
	 * @param setAnonymous the set anonymous
	 * 			  choose ANONYMOUS CSS CLASS on loadPortrait()
	 */
	public DisplayPortraitController(UserRequest ureq, WindowControl wControl, Identity portraitIdent,
			boolean useLarge, boolean canLinkToHomePage, boolean setAnonymous) { 
		this(ureq, wControl, portraitIdent, useLarge, canLinkToHomePage, false, true);
		this.forceAnonymous = setAnonymous;
		loadPortrait();
	}

	/**
	 * most common used constructor<br />
	 * will display portrait, no username
	 * 
	 * @param ureq
	 * @param wControl
	 * @param portraitIdent
	 *            the identity to display
	 * @param useLarge
	 *            if set to true, the portrait-image is displayed as "big"
	 * @param canLinkToHomePage
	 *            if set to true, the portrait is linked to the users homepage
	 */
	public DisplayPortraitController(UserRequest ureq, WindowControl wControl, Identity portraitIdent,
			boolean useLarge, boolean canLinkToHomePage) { 
		this(ureq, wControl, portraitIdent, useLarge, canLinkToHomePage, false, true);
	}
	
	/**
	 * constructor with more config options<br />
	 * use this if you want to display the full name of the user (additionally
	 * or only)
	 * 
	 * @param ureq
	 * @param wControl
	 * @param portraitIdent
	 *            the identity to display
	 * @param useLarge
	 *            if set to true, the portrait-image is displayed as "big"
	 * @param canLinkToHomePage
	 *            if set to true, the portrait is linked to the users homepage
	 * @param displayUserFullName
	 *            if set to true, the users full name ("firstname lastname") is
	 *            displayed as well
	 * @param displayPortraitImage
	 *            if set to false, the portrait image will not be displayed
	 */
	public DisplayPortraitController(UserRequest ureq, WindowControl wControl, Identity portraitIdent,
			boolean useLarge, boolean canLinkToHomePage, boolean displayUserFullName, boolean displayPortraitImage) { 
		super(ureq, wControl);
		this.isDeletedUser = portraitIdent.getStatus().equals(Identity.STATUS_DELETED);
		myContent = createVelocityContainer("displayportrait");
		myContent.contextPut("canLinkToHomePage", (canLinkToHomePage && !isDeletedUser) ? Boolean.TRUE : Boolean.FALSE);

		this.useLarge = useLarge;
		this.portraitIdent = portraitIdent;
		this.displayPortraitImage = displayPortraitImage;
		
		UserSession usess = ureq.getUserSession();
		isAnonymous = usess != null && usess.getRoles().isGuestOnly();// export data doesn't have a session

		mapper = new UserAvatarMapper(useLarge);
		mapperPath = registerMapper(ureq, mapper);
		
		myContent.contextPut("identityKey", portraitIdent.getKey().toString());
		myContent.contextPut("displayUserFullName", displayUserFullName);
		String fullName = UserManager.getInstance().getUserDisplayName(portraitIdent);
		myContent.contextPut("fullName", fullName);		
		String altText = translate("title.homepage") + ": " + fullName;
		myContent.contextPut("altText", StringHelper.escapeHtml(altText));
		putInitialPanel(myContent);
		
		loadPortrait();

		listenerOres = OresHelper.createOLATResourceableInstance("portrait", getIdentity().getKey());
		CoordinatorManager.getInstance().getCoordinator().getEventBus().registerFor(this, portraitIdent, listenerOres);
	}

	@Override
	protected void doDispose() {
		CoordinatorManager.getInstance().getCoordinator().getEventBus().deregisterFor(this, listenerOres);
        super.doDispose();
	}
	
	private void loadPortrait() {
		File image = null;
		if(displayPortraitImage) {
			GenderPropertyHandler genderHander = (GenderPropertyHandler) UserManager.getInstance().getUserPropertiesConfig().getPropertyHandler(UserConstants.GENDER);
			String gender = "-"; // use as default
			if (genderHander != null) {
				gender = genderHander.getInternalValue(portraitIdent.getUser());
			}
			
			if (useLarge) {
				image = displayPortraitManager.getBigPortrait(portraitIdent);
				if (image != null && !forceAnonymous && !isDeletedUser) {
					myContent.contextPut("portraitCssClass", DisplayPortraitManager.AVATAR_BIG_CSS_CLASS);
				} else if (isAnonymous || forceAnonymous || isDeletedUser) {
					myContent.contextPut("portraitCssClass", DisplayPortraitManager.ANONYMOUS_BIG_CSS_CLASS);
				} else if (gender.equals("male")) {
					myContent.contextPut("portraitCssClass", DisplayPortraitManager.DUMMY_MALE_BIG_CSS_CLASS);
				} else if (gender.equals("female")) {
					myContent.contextPut("portraitCssClass", DisplayPortraitManager.DUMMY_FEMALE_BIG_CSS_CLASS);
				} else {
					myContent.contextPut("portraitCssClass", DisplayPortraitManager.DUMMY_BIG_CSS_CLASS);
				}
			} else {
				image = displayPortraitManager.getSmallPortrait(portraitIdent);
				if (image != null && !forceAnonymous && !isDeletedUser) {
					myContent.contextPut("portraitCssClass", DisplayPortraitManager.AVATAR_SMALL_CSS_CLASS);					
				} else if (isAnonymous || forceAnonymous || isDeletedUser) {
					myContent.contextPut("portraitCssClass", DisplayPortraitManager.ANONYMOUS_SMALL_CSS_CLASS);
				} else if (gender.equals("male")) {
					myContent.contextPut("portraitCssClass", DisplayPortraitManager.DUMMY_MALE_SMALL_CSS_CLASS);
				} else if (gender.equals("female")) {
					myContent.contextPut("portraitCssClass", DisplayPortraitManager.DUMMY_FEMALE_SMALL_CSS_CLASS);
				} else {
					myContent.contextPut("portraitCssClass", DisplayPortraitManager.DUMMY_SMALL_CSS_CLASS);
				}
			}
			
			if (image != null) {
				myContent.contextPut("mapperUrl", mapper.createPathFor(mapperPath, portraitIdent));
			} else {
				myContent.contextRemove("mapperUrl");
			}
		} else {
			myContent.contextRemove("mapperUrl");
		}

		myContent.contextPut("hasPortrait", (image != null && !forceAnonymous) ? Boolean.TRUE : Boolean.FALSE);
	}

	@Override
	public void event(Event event) {
		if("changed-portrait".equals(event.getCommand()) && event instanceof ProfileEvent) {
			try {
				ProfileEvent pe = (ProfileEvent)event;
				if(portraitIdent.getKey().equals(pe.getIdentityKey())) {
					loadPortrait();
					myContent.setDirty(true);
				}
			} catch (Exception e) {
				logError("", e);
			}
		}
	}

	@Override
	public void event(UserRequest ureq, Component source, Event event) {
		if (source == myContent) {
			if (event.getCommand().equals("showuserinfo")) {
				showUserInfo(ureq);
			}
		}
		// nothing to dispatch
	}

	/**
	 * Method to open the users visiting card in a new tab. Public to call it also from the patrent controller
	 * @param ureq
	 */
	public void showUserInfo(UserRequest ureq) {
		ControllerCreator ctrlCreator = new ControllerCreator() {
			@Override
			public Controller createController(UserRequest lureq, WindowControl lwControl) {
				return new UserInfoMainController(lureq, lwControl, portraitIdent, true, false);
			}					
		};
		//wrap the content controller into a full header layout
		ControllerCreator layoutCtrlr = BaseFullWebappPopupLayoutFactory.createAuthMinimalPopupLayout(ureq, ctrlCreator);
		//open in new browser window
		PopupBrowserWindow pbw = getWindowControl().getWindowBackOffice().getWindowManager().createNewPopupBrowserWindowFor(ureq, layoutCtrlr);
		pbw.open(ureq);
	}
}