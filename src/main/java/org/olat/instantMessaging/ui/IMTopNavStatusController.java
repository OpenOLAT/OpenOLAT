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

import org.olat.core.CoreSpringFactory;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.instantMessaging.InstantMessagingService;
import org.olat.instantMessaging.model.Presence;

/**
 * 
 * Change the status
 * 
 * Initial date: 05.12.2012<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class IMTopNavStatusController extends BasicController {
	
	private final Link available, unavailable, dnd;
	private final VelocityContainer mainVC;
	
	private String status;
	
	private final InstantMessagingService imService;
	
	public IMTopNavStatusController(UserRequest ureq, WindowControl wControl, String status) {
		super(ureq, wControl);
		this.status = status;
		imService = CoreSpringFactory.getImpl(InstantMessagingService.class);
		mainVC = createVelocityContainer("status_changer");
		
		available = LinkFactory.createLink("presence.available", mainVC, this);
		available.setCustomEnabledLinkCSS("o_im_available_icon");
		dnd = LinkFactory.createLink("presence.dnd", mainVC, this);
		dnd.setCustomEnabledLinkCSS("o_im_dnd_icon");
		unavailable = LinkFactory.createLink("presence.unavailable", mainVC, this);
		unavailable.setCustomEnabledLinkCSS("o_im_unavailable_icon");

		putInitialPanel(mainVC);
	}
	
	public String getSelectedStatus() {
		return status;
	}

	@Override
	protected void doDispose() {
		//
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		if (source == available) {
			status = Presence.available.name();
			imService.updateStatus(getIdentity(), status);
			fireEvent(ureq, Event.CHANGED_EVENT);
		} else if (source == dnd) {
			status = Presence.dnd.name();
			imService.updateStatus(getIdentity(), status);
			fireEvent(ureq, Event.CHANGED_EVENT);
		} else if (source == unavailable) {
			status = Presence.unavailable.name();
			imService.updateStatus(getIdentity(), status);
			fireEvent(ureq, Event.CHANGED_EVENT);
		}
	}
}