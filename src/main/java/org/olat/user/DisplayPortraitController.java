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
import org.olat.core.gui.components.image.ImageComponent;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.gui.control.creator.ControllerCreator;
import org.olat.core.gui.control.generic.popup.PopupBrowserWindow;
import org.olat.core.gui.media.MediaResource;
import org.olat.core.id.Identity;
import org.olat.core.id.UserConstants;
import org.olat.core.logging.AssertException;
import org.olat.user.propertyhandlers.GenderPropertyHandler;

/**
 * Initial Date:  Sept 08, 2005
 *
 * @author Alexander Schneider
 * 
 * Comment: 
 */
public class DisplayPortraitController extends BasicController {
	
	private VelocityContainer myContent;
	private Identity portraitIdent;
	
	/**
	 * @param ureq
	 * @param wControl
	 * @param portrait
	 */
	public DisplayPortraitController(UserRequest ureq, WindowControl wControl, Identity portraitIdent, boolean useLarge, boolean canLinkToHomePage) { 
		super(ureq, wControl);
		myContent = createVelocityContainer("displayportrait");
		myContent.contextPut("canLinkToHomePage", canLinkToHomePage ? Boolean.TRUE : Boolean.FALSE);
		if (portraitIdent == null) throw new AssertException("identity can not be null!");
		this.portraitIdent = portraitIdent;
		
		ImageComponent ic = null;
		
		GenderPropertyHandler genderHander = (GenderPropertyHandler) UserManager.getInstance().getUserPropertiesConfig().getPropertyHandler(UserConstants.GENDER);
		String gender = "-"; // use as default
		if (genderHander != null) {
			gender = genderHander.getInternalValue(portraitIdent.getUser());
		}
		
		MediaResource portrait = null;
		if (useLarge){
			portrait = DisplayPortraitManager.getInstance().getPortrait(portraitIdent, DisplayPortraitManager.PORTRAIT_BIG_FILENAME);
			if (gender.equals("-")) {
				myContent.contextPut("portraitCssClass", DisplayPortraitManager.DUMMY_BIG_CSS_CLASS);
			} else if (gender.equals("male")) {
				myContent.contextPut("portraitCssClass", DisplayPortraitManager.DUMMY_MALE_BIG_CSS_CLASS);
			} else if (gender.equals("female")) {
				myContent.contextPut("portraitCssClass", DisplayPortraitManager.DUMMY_FEMALE_BIG_CSS_CLASS);
			}
		} else {
			portrait = DisplayPortraitManager.getInstance().getPortrait(portraitIdent, DisplayPortraitManager.PORTRAIT_SMALL_FILENAME);
			if (gender.equals("-")) {
				myContent.contextPut("portraitCssClass", DisplayPortraitManager.DUMMY_SMALL_CSS_CLASS);
			} else if (gender.equals("male")) {
				myContent.contextPut("portraitCssClass", DisplayPortraitManager.DUMMY_MALE_SMALL_CSS_CLASS);
			} else if (gender.equals("female")) {
				myContent.contextPut("portraitCssClass", DisplayPortraitManager.DUMMY_FEMALE_SMALL_CSS_CLASS);
			}
		}
		myContent.contextPut("hasPortrait", (portrait != null) ? Boolean.TRUE : Boolean.FALSE);
		myContent.contextPut("identityKey", portraitIdent.getKey().toString());

		if (portrait != null){
				ic = new ImageComponent("image");
				ic.setMediaResource(portrait);
				myContent.put(ic);
		}
		putInitialPanel(myContent);
	}

	/**
	 * @see org.olat.core.gui.control.DefaultController#event(org.olat.core.gui.UserRequest, org.olat.core.gui.components.Component, org.olat.core.gui.control.Event)
	 */
	@Override
	public void event(UserRequest ureq, Component source, Event event) {
		if (source == myContent) {
			if (event.getCommand().equals("showuserinfo")) {
				ControllerCreator ctrlCreator = new ControllerCreator() {
					public Controller createController(UserRequest lureq, WindowControl lwControl) {
						return new UserInfoMainController(lureq, lwControl, portraitIdent);
					}					
				};
				//wrap the content controller into a full header layout
				ControllerCreator layoutCtrlr = BaseFullWebappPopupLayoutFactory.createAuthMinimalPopupLayout(ureq, ctrlCreator);
				//open in new browser window
				PopupBrowserWindow pbw = getWindowControl().getWindowBackOffice().getWindowManager().createNewPopupBrowserWindowFor(ureq, layoutCtrlr);
				pbw.open(ureq);
				//
			}
		}
		// nothing to dispatch
	}

	/**
	 * 
	 * @see org.olat.core.gui.control.DefaultController#doDispose(boolean)
	 */
	@Override
	protected void doDispose() {
		// nothing to do yet
	}
	
}
