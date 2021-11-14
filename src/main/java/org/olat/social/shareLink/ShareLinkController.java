/**
 * <a href=“http://www.openolat.org“>
 * OpenOLAT - Online Learning and Training</a><br>
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); <br>
 * you may not use this file except in compliance with the License.<br>
 * You may obtain a copy of the License at the
 * <a href="http://www.apache.org/licenses/LICENSE-2.0">Apache homepage</a>
 * <p>
 * Unless required by applicable law or agreed to in writing,<br>
 * software distributed under the License is distributed on an "AS IS" BASIS, <br>
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
 * See the License for the specific language governing permissions and <br>
 * limitations under the License.
 * <p>
 * Initial code contributed and copyrighted by<br>
 * 13.09.2012 by frentix GmbH, http://www.frentix.com
 * <p>
 **/


package org.olat.social.shareLink;

import java.util.List;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.WindowManager;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.gui.control.winmgr.JSCommand;
import org.olat.core.helpers.Settings;
import org.olat.core.id.context.BusinessControlFactory;
import org.olat.core.id.context.ContextEntry;
import org.olat.core.id.context.HistoryPoint;
import org.olat.core.util.StringHelper;
import org.olat.core.util.UserSession;
import org.olat.social.SocialModule;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * <h3>Description:</h3>
 * <p>
 * This controller displays a row of buttons to share the link of the current
 * page (perma-link/business-path link) with other people. Besides some common
 * social networks a mail button and a link copy/past button is also
 * implemented.
 * <p>
 * The list of buttons can be configured in the SocialModule and the olat.properties
 * <p>
 * <h3>Events thrown by this controller:</h3>
 * <p>
 * none
 * <p>
 * Initial Date: 13.09.2012 <br>
 * 
 * @author Florian Gnaegi, frentix GmbH, http://www.frentix.com
 */
public class ShareLinkController extends BasicController {
	private final VelocityContainer shareLinkVC;
	
	@Autowired
	private SocialModule socialModule;
	
	/**
	 * Standard constructor for the share link controller
	 * @param ureq
	 * @param wControl
	 */
	public ShareLinkController(UserRequest ureq, WindowControl wControl) {
		super(ureq, wControl);
		// For simplicity we use only one velocity template
		shareLinkVC = createVelocityContainer("shareLink");
		// Add the OpenOLAT base URL from the config
		shareLinkVC.contextPut("baseURL", Settings.getServerContextPathURI());
		// Load configured share link buttons from the SocialModule configuration
		shareLinkVC.contextPut("shareLinks", socialModule.getEnabledShareLinkButtons());
		// Tell if user is logged in
		UserSession usess = ureq.getUserSession();
		shareLinkVC.contextPut("isUser", usess.isAuthenticated() && !usess.getRoles().isGuestOnly());
		putInitialPanel(shareLinkVC);
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		UserSession usess = ureq.getUserSession();
		if (source == shareLinkVC && "setLandingPage".equals(event.getCommand()) && usess != null && usess.isAuthenticated()) {
			HistoryPoint p = usess.getLastHistoryPoint();
			if(p != null && StringHelper.containsNonWhitespace(p.getBusinessPath())) {
				List<ContextEntry> ces = p.getEntries();
				String landingPage = BusinessControlFactory.getInstance().getAsURIString(ces, true);
				int start = landingPage.indexOf("/url/");
				if (start != -1) {
					// start with / after /url
					landingPage = landingPage.substring(start + 4);
				}
				// update user prefs
				usess.getGuiPreferences().putAndSave(WindowManager.class, "landing-page", landingPage);				
				getWindowControl().getWindowBackOffice().sendCommandTo(new JSCommand("showInfoBox(\"" + translate("info.header") + "\",\"" + translate("landingpage.set.message") + "\");"));
			}
		}
	}
}
