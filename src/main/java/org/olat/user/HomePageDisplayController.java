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
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.olat.basesecurity.OrganisationModule;
import org.olat.basesecurity.OrganisationRoles;
import org.olat.basesecurity.OrganisationService;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.image.ImageComponent;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.helpers.Settings;
import org.olat.core.id.Identity;
import org.olat.core.id.Organisation;
import org.olat.core.id.User;
import org.olat.core.id.UserConstants;
import org.olat.core.util.StringHelper;
import org.olat.core.util.UserSession;
import org.olat.instantMessaging.ImPreferences;
import org.olat.instantMessaging.InstantMessagingModule;
import org.olat.instantMessaging.InstantMessagingService;
import org.olat.instantMessaging.OpenInstantMessageEvent;
import org.olat.instantMessaging.model.Buddy;
import org.olat.user.propertyhandlers.UserPropertyHandler;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Initial Date:  Jul 25, 2005
  *
 * @author Alexander Schneider
 * 
 * Comment: This controller displays the users visiting card
 */
public class HomePageDisplayController extends BasicController {
	private static final String usageIdentifyer = HomePageConfig.class.getCanonicalName();
	
	private Link imLink;
	
	@Autowired
	private UserModule userModule;
	@Autowired
	private UserManager userManager;
	@Autowired
	private InstantMessagingModule imModule;
	@Autowired
	private InstantMessagingService imService;
	@Autowired
	private OrganisationModule organisationModule;
	@Autowired
	private OrganisationService organisationService;
	@Autowired
	private DisplayPortraitManager displayPortraitManager;

	/**
	 * @param ureq
	 * @param wControl
	 * @param hpc
	 */
	public HomePageDisplayController(UserRequest ureq, WindowControl wControl, Identity homeIdentity, HomePageConfig hpc) {
		super(ureq, wControl);
		
		// use property handler translator for translating of user fields
		setTranslator(userManager.getPropertyHandlerTranslator(getTranslator()));
		VelocityContainer mainVC = createVelocityContainer("homepagedisplay");
		
		String fullname = StringHelper.escapeHtml(userManager.getUserDisplayName(homeIdentity));
		mainVC.contextPut("deleted", homeIdentity.getStatus().equals(Identity.STATUS_DELETED));
		mainVC.contextPut("user", homeIdentity.getUser());
		mainVC.contextPut("userFullname", fullname);
		mainVC.contextPut("locale", getLocale());
		
		// add configured property handlers and the homepage config
		// do the looping in the velocity context
		List<UserPropertyHandler> userPropertyHandlers
			= new ArrayList<>(userManager.getUserPropertyHandlersFor(usageIdentifyer, false));
		UserPropertyHandler userSearchedInterestsHandler = null;
		UserPropertyHandler userInterestsHandler = null;
		for(Iterator<UserPropertyHandler> propIt=userPropertyHandlers.iterator(); propIt.hasNext(); ) {
			UserPropertyHandler prop = propIt.next();
			if(!hpc.isEnabled(prop.getName()) && !userManager.isMandatoryUserProperty(usageIdentifyer, prop)) {
				propIt.remove();
			} else if("userInterests".equals(prop.getName())) {
				userInterestsHandler = prop;
			} else if("userSearchedInterests".equals(prop.getName())) {
				userSearchedInterestsHandler = prop;
			}
		}
		if(userInterestsHandler != null && userSearchedInterestsHandler != null) {
			userPropertyHandlers.remove(userSearchedInterestsHandler);
		}
		
		mainVC.contextPut("userPropertyHandlers", userPropertyHandlers);
		mainVC.contextPut("userSearchedInterestsHandler", userSearchedInterestsHandler);
		
		if(userModule.isUserAboutMeEnabled()) {
			String aboutMe = hpc.getTextAboutMe();
			mainVC.contextPut("aboutMe", aboutMe);	
		}
		
		// Add external link to visiting card
		StringBuilder extLink = new StringBuilder();
		extLink.append(Settings.getServerContextPathURI())
			.append("/url/HomeSite/").append(homeIdentity.getKey());
		mainVC.contextPut("extLink", extLink);

		Controller dpc = new DisplayPortraitController(ureq, getWindowControl(), homeIdentity, true, false);
		listenTo(dpc); // auto dispose
		mainVC.put("image", dpc.getInitialComponent());
		
		UserSession usess = ureq.getUserSession();
		
		exposeOrganisations(homeIdentity, mainVC);
		exposeLogo(usess, homeIdentity, mainVC);
		exposeInstantMessageLink(homeIdentity, mainVC);
		putInitialPanel(mainVC);
	}
	
	private void exposeOrganisations(Identity homeIdentity, VelocityContainer mainVC) {
		if(organisationModule.isEnabled()) {
			List<Organisation> organisations = organisationService.getOrganisations(homeIdentity, OrganisationRoles.values());
			List<String> organisationNames = new ArrayList<>(organisations.size());
			for(Organisation organisation:organisations) {
				organisationNames.add(organisation.getDisplayName());
			}
			mainVC.contextPut("organisations", organisationNames);
		}
	}
	
	private void exposeLogo(UserSession usess, Identity homeIdentity, VelocityContainer mainVC) {
		if(userModule.isLogoByProfileEnabled()) {
			File logo = displayPortraitManager.getBigLogo(homeIdentity);
			if (logo != null) {
				ImageComponent logoCmp = new ImageComponent(usess, "logo");
				logoCmp.setMedia(logo);
				logoCmp.setMaxWithAndHeightToFitWithin(200, 66);
				mainVC.put("logo", logoCmp);				
			}
		}
	}
	
	private void exposeInstantMessageLink(Identity homeIdentity, VelocityContainer mainVC) {
		if(imModule.isEnabled() && imModule.isPrivateEnabled()) {
			ImPreferences prefs = imService.getImPreferences(homeIdentity);
			if(prefs.isVisibleToOthers()) {
				User user = homeIdentity.getUser();
				String fName = StringHelper.escapeHtml(user.getProperty(UserConstants.FIRSTNAME, getLocale()));
				String lName = StringHelper.escapeHtml(user.getProperty(UserConstants.LASTNAME, getLocale()));
				imLink = LinkFactory.createCustomLink("im.link", "im.link", "im.link", Link.NONTRANSLATED, mainVC, this);
				imLink.setCustomDisplayText(translate("im.link", new String[] {fName,lName}));
				Buddy buddy = imService.getBuddyById(homeIdentity.getKey());
				
				String css = "o_icon " + (imModule.isOnlineStatusEnabled() ? getStatusCss(buddy) : "o_im_chat_icon");
				imLink.setIconLeftCSS(css);
				imLink.setUserObject(buddy);
			}
		}
	}
	
	private String getStatusCss(Buddy buddy) {
		return "o_icon_status_".concat(buddy.getStatus());
	}

	@Override
	public void event(UserRequest ureq, Component source, Event event) {
		if(imLink == source) {
			Buddy buddy = (Buddy)imLink.getUserObject();
			OpenInstantMessageEvent e = new OpenInstantMessageEvent(buddy);
			ureq.getUserSession().getSingleUserEventCenter().fireEventToListenersOf(e, InstantMessagingService.TOWER_EVENT_ORES);
		}
	}
}