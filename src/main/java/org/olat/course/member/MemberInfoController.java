/**
 * <a href="http://www.openolat.org">
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
 * frentix GmbH, http://www.frentix.com
 * <p>
 */
package org.olat.course.member;

import org.olat.NewControllerFactory;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.id.Identity;
import org.olat.user.DisplayPortraitController;

/**
 * 
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
public class MemberInfoController extends BasicController {
	
	private final Link homeLink, contactLink, assessmentLink;
	private final VelocityContainer mainVC;
	private Long identityKey;
	
	public MemberInfoController(UserRequest ureq, WindowControl wControl, Identity identity) {
		super(ureq, wControl);
	
		mainVC = createVelocityContainer("info_member");
		
		Controller dpc = new DisplayPortraitController(ureq, getWindowControl(), identity, true, false);
		listenTo(dpc); // auto dispose
		mainVC.put("image", dpc.getInitialComponent());
		
		
		homeLink = LinkFactory.createButton("home",	mainVC, this);
		homeLink.setCustomEnabledLinkCSS("b_link_left_icon b_link_to_home");
		contactLink = LinkFactory.createButton("contact",	mainVC, this);
		contactLink.setCustomEnabledLinkCSS("b_link_left_icon b_link_mail");
		assessmentLink = LinkFactory.createButton("assessment",	mainVC, this);
		assessmentLink.setCustomEnabledLinkCSS("b_link_left_icon b_link_assessment");

		putInitialPanel(mainVC);
	}
	
	@Override
	protected void doDispose() {
		//
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		if(source == homeLink) {
			String businessPath = "[Identity:" + identityKey + "]";
			NewControllerFactory.getInstance().launch(businessPath, ureq, getWindowControl());
		} else if (source == contactLink) {
			String businessPath = "[Identity:" + identityKey + "][Contact:0]";
			NewControllerFactory.getInstance().launch(businessPath, ureq, getWindowControl());
		} else if (source == assessmentLink) {
			String businessPath = "[Identity:" + identityKey + "]";
			NewControllerFactory.getInstance().launch(businessPath, ureq, getWindowControl());	
		}
	}
}