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

import org.olat.core.CoreSpringFactory;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.helpers.Settings;
import org.olat.social.SocialModule;

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
	private VelocityContainer shareLinkVC;
	
	/**
	 * Standard constructor for the share link controller
	 * @param ureq
	 * @param wControl
	 */
	public ShareLinkController(UserRequest ureq, WindowControl wControl) {
		super(ureq, wControl);
		// For simplicity we use only one velocity template
		this.shareLinkVC = createVelocityContainer("shareLink");
		// Add the OpenOLAT base URL from the config
		shareLinkVC.contextPut("baseURL", Settings.getServerContextPathURI());
		// Load configured share link buttons from the SocialModule configuration
		SocialModule socialModule = (SocialModule) CoreSpringFactory.getBean("socialModule");
		this.shareLinkVC.contextPut("shareLinks", socialModule.getEnabledShareLinkButtons());
		//
		putInitialPanel(this.shareLinkVC);
	}


	/* (non-Javadoc)
	 * @see org.olat.core.gui.control.DefaultController#event(org.olat.core.gui.UserRequest, org.olat.core.gui.components.Component, org.olat.core.gui.control.Event)
	 */
	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		// nothing to do
	}

	/* (non-Javadoc)
	 * @see org.olat.core.gui.control.DefaultController#doDispose()
	 */
	@Override
	protected void doDispose() {
		// nothing to do
	}

}
