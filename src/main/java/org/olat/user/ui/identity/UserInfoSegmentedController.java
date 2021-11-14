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
package org.olat.user.ui.identity;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.segmentedview.SegmentViewComponent;
import org.olat.core.gui.components.segmentedview.SegmentViewEvent;
import org.olat.core.gui.components.segmentedview.SegmentViewFactory;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.id.Identity;
import org.olat.core.util.Util;
import org.olat.user.UserInfoMainController;

/**
 * 
 * Initial date: 6 juil. 2018<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class UserInfoSegmentedController extends AbstractUserInfoMainController {

	private Link backLink;
	private Link folderLink;
	private Link contactLink;
	private Link calendarLink;
	private final Link homepageLink;
	private final VelocityContainer mainVC;
	private final SegmentViewComponent segmentView;
	
	public UserInfoSegmentedController(UserRequest ureq, WindowControl wControl,
			Identity chosenIdentity, boolean withTitle, boolean withBack) {
		super(ureq, wControl, chosenIdentity);
		setTranslator(Util.createPackageTranslator(UserInfoMainController.class, getLocale(), getTranslator()));
		
		mainVC = createVelocityContainer("infos_segment");
		mainVC.contextPut("withTitle", Boolean.valueOf(withTitle));
		mainVC.contextPut("fullName", userManager.getUserDisplayName(chosenIdentity));
		if(withBack) {
			backLink = LinkFactory.createLinkBack(mainVC, this);
		}
		segmentView = SegmentViewFactory.createSegmentView("segment", mainVC, this);
		homepageLink = LinkFactory.createLink("menu.homepage", mainVC, this);
		segmentView.addSegment(homepageLink, true);
		mainVC.put("segments", segmentView);
		doSelect(ureq, homepageLink);
		
		if (!isDeleted && !isInvitee) {
			if(calendarModule.isEnablePersonalCalendar()) {
				calendarLink = LinkFactory.createLink("menu.calendar", mainVC, this);
				calendarLink.setIconLeftCSS("o_visiting_card_calendar");
				segmentView.addSegment(calendarLink, false);
			}
			folderLink = LinkFactory.createLink("menu.folder", mainVC, this);
			folderLink.setIconLeftCSS("o_visiting_card_folder");
			segmentView.addSegment(folderLink, false);
		}
		
		if (!isDeleted) {
			contactLink = LinkFactory.createLink("menu.contact", mainVC, this);
			contactLink.setIconLeftCSS("o_visiting_card_contact");
			segmentView.addSegment(contactLink, false);
		}
		
		putInitialPanel(mainVC);
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		if(source == segmentView) {
			if(event instanceof SegmentViewEvent) {
				SegmentViewEvent sve = (SegmentViewEvent)event;
				String segmentCName = sve.getComponentName();
				Component clickedLink = mainVC.getComponent(segmentCName);
				doSelect(ureq, clickedLink);
			}
		} else if(source == backLink) {
			fireEvent(ureq, Event.BACK_EVENT);
		}
	}
	
	private void doSelect(UserRequest ureq, Component clickedLink) {
		Controller selectedCtrl = null;
		if (clickedLink == homepageLink) {
			selectedCtrl = doOpenHomepage(ureq);
		} else if(clickedLink == calendarLink) {
			selectedCtrl = doOpenCalendar(ureq);
		} else if(clickedLink == folderLink) {
			selectedCtrl = doOpenFolder(ureq);
		} else if(clickedLink == contactLink) {
			selectedCtrl = doOpenContact(ureq);
		}
		if(selectedCtrl != null) {
			addToHistory(ureq, selectedCtrl);
			mainVC.put("segmentCmp", selectedCtrl.getInitialComponent());
		}
	}
}