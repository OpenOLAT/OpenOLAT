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
package org.olat.instantMessaging.ui;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.id.OLATResourceable;
import org.olat.instantMessaging.InstantMessagingService;
import org.olat.instantMessaging.OpenInstantMessageEvent;

/**
 * 
 * Initial date: 06.12.2012<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
public class ChatToolController extends BasicController {
	
	private final VelocityContainer mainVC;
	private final OLATResourceable resource;
	private final Link openChatLink;

	public ChatToolController(UserRequest ureq, WindowControl wControl, OLATResourceable resource) {
		super(ureq, wControl);
		this.resource = resource;
		
		mainVC = createVelocityContainer("summary");
		mainVC.contextPut("isInAssessment", Boolean.FALSE);
		openChatLink = LinkFactory.createButton("openChat", mainVC, this);
		putInitialPanel(mainVC);
	}
	
	@Override
	protected void doDispose() {
		//
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		if(openChatLink == source) {
			OpenInstantMessageEvent e = new OpenInstantMessageEvent(ureq, resource);
			ureq.getUserSession().getSingleUserEventCenter().fireEventToListenersOf(e, InstantMessagingService.TOWER_EVENT_ORES);
		}
	}
}
