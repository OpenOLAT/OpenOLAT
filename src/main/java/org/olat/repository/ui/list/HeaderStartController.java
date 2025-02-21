/**
 * <a href="https://www.openolat.org">
 * OpenOLAT - Online Learning and Training</a><br>
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); <br>
 * you may not use this file except in compliance with the License.<br>
 * You may obtain a copy of the License at the
 * <a href="https://www.apache.org/licenses/LICENSE-2.0">Apache homepage</a>
 * <p>
 * Unless required by applicable law or agreed to in writing,<br>
 * software distributed under the License is distributed on an "AS IS" BASIS, <br>
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
 * See the License for the specific language governing permissions and <br>
 * limitations under the License.
 * <p>
 * Initial code contributed and copyrighted by<br>
 * frentix GmbH, https://www.frentix.com
 * <p>
 */
package org.olat.repository.ui.list;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.EscapeMode;
import org.olat.core.gui.components.link.ExternalLink;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.util.Util;
import org.olat.repository.RepositoryService;



/**
 * 
 * Initial date: Feb 7, 2025<br>
 * @author uhensler, urs.hensler@frentix.com, https://www.frentix.com
 *
 */
public class HeaderStartController extends BasicController {

	private final VelocityContainer mainVC;
	private final Link startLink;
	private final Link leaveLink;
	private final ExternalLink guestStartLink;
	
	private boolean autoBooking = false;

	protected HeaderStartController(UserRequest ureq, WindowControl wControl) {
		super(ureq, wControl);
		setTranslator(Util.createPackageTranslator(RepositoryService.class, getLocale(), getTranslator()));
		mainVC = createVelocityContainer("details_header_start");
		putInitialPanel(mainVC);
		
		startLink = LinkFactory.createCustomLink("start", "start", null, Link.BUTTON + Link.NONTRANSLATED, mainVC, this);
		startLink.setEscapeMode(EscapeMode.html);
		startLink.setIconRightCSS("o_icon o_icon_start o_icon-lg");
		startLink.setPrimary(true);
		startLink.setElementCssClass("o_start o_button_call_to_action");
		
		leaveLink = LinkFactory.createCustomLink("leave", "sign.out", null, Link.BUTTON + Link.NONTRANSLATED, mainVC, this);
		leaveLink.setElementCssClass("o_sign_out");
		leaveLink.setIconLeftCSS("o_icon o_icon_sign_out");
		leaveLink.setGhost(true);
		leaveLink.setVisible(false);
		
		guestStartLink = new ExternalLink("start.guest", "start.guest");
		guestStartLink.setName(translate("start.guest"));
		guestStartLink.setElementCssClass("btn btn-default btn-primary o_button_call_to_action");
		guestStartLink.setVisible(false);
		mainVC.put("start.guest", guestStartLink);
	}
	
	public Link getStartLink() {
		return startLink;
	}

	public Link getLeaveLink() {
		return leaveLink;
	}

	public ExternalLink getGuestStartLink() {
		return guestStartLink;
	}

	public void setWarning(String warning) {
		mainVC.contextPut("warning", warning);
	}
	
	public void setError(String error) {
		mainVC.contextPut("error", error);
	}

	public boolean isAutoBooking() {
		return autoBooking;
	}

	public void setAutoBooking(boolean autoBooking) {
		this.autoBooking = autoBooking;
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		if (source == startLink) {
			fireEvent(ureq, AbstractDetailsHeaderController.START_EVENT);
		} else if (source == leaveLink) {
			fireEvent(ureq, AbstractDetailsHeaderController.LEAVE_EVENT);
		}
	}

}
