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
package org.olat.login;

import java.util.List;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.segmentedview.SegmentViewComponent;
import org.olat.core.gui.components.segmentedview.SegmentViewEvent;
import org.olat.core.gui.components.segmentedview.SegmentViewFactory;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.gui.control.generic.dtabs.Activateable2;
import org.olat.core.id.context.ContextEntry;
import org.olat.core.id.context.StateEntry;
import org.olat.core.util.resource.OresHelper;
import org.olat.modules.invitation.ui.InvitationAdminSettingsController;

/**
 * 
 * Initial date: 8 juil. 2022<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class GuestAndInvitationAdminController extends BasicController implements Activateable2 {
	
	private final Link guestLink;
	private final Link invitationLink;
	private final VelocityContainer mainVC;
	private final SegmentViewComponent segmentView;

	private GuestAdminController guestAdminCtrl;
	private InvitationAdminSettingsController invitationAdminCtrl;
	
	public GuestAndInvitationAdminController(UserRequest ureq, WindowControl wControl) {
		super(ureq, wControl);
		
		mainVC = createVelocityContainer("admin");
		
		segmentView = SegmentViewFactory.createSegmentView("segments", mainVC, this);
		segmentView.setReselect(true);
		
		guestLink = LinkFactory.createLink("admin.guest.settings", mainVC, this);
		segmentView.addSegment(guestLink, true);
		invitationLink = LinkFactory.createLink("admin.invitation.settings", mainVC, this);
		segmentView.addSegment(invitationLink, false);
		
		doOpenGuestSettings(ureq);
		putInitialPanel(mainVC);
	}

	@Override
	public void activate(UserRequest ureq, List<ContextEntry> entries, StateEntry state) {
		if(entries == null || entries.isEmpty()) return;
		
		String resourceTypeName = entries.get(0).getOLATResourceable().getResourceableTypeName();
		if("Guest".equalsIgnoreCase(resourceTypeName)) {
			doOpenGuestSettings(ureq);
			segmentView.select(guestLink);
		} else if("Invitation".equalsIgnoreCase(resourceTypeName)) {
			doOpenExternalUserSettings(ureq);
			segmentView.select(invitationLink);
		}
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		if(source == segmentView) {
			if(event instanceof SegmentViewEvent) {
				SegmentViewEvent sve = (SegmentViewEvent)event;
				String segmentCName = sve.getComponentName();
				Component clickedLink = mainVC.getComponent(segmentCName);
				if (clickedLink == guestLink) {
					doOpenGuestSettings(ureq);
				} else if (clickedLink == invitationLink) {
					doOpenExternalUserSettings(ureq);
				}
			}
		}
	}

	private void doOpenGuestSettings(UserRequest ureq) {
		if(guestAdminCtrl == null) {
			WindowControl bwControl = addToHistory(ureq, OresHelper.createOLATResourceableType("Guest"), null);
			guestAdminCtrl = new GuestAdminController(ureq, bwControl);
			listenTo(guestAdminCtrl);
		}
		mainVC.put("segmentCmp", guestAdminCtrl.getInitialComponent());
		addToHistory(ureq, guestAdminCtrl);
	}
	
	private void doOpenExternalUserSettings(UserRequest ureq) {
		if(invitationAdminCtrl == null) {
			WindowControl bwControl = addToHistory(ureq, OresHelper.createOLATResourceableType("Invitation"), null);
			invitationAdminCtrl = new InvitationAdminSettingsController(ureq, bwControl);
			listenTo(invitationAdminCtrl);
		}
		mainVC.put("segmentCmp", invitationAdminCtrl.getInitialComponent());
		addToHistory(ureq, invitationAdminCtrl);
	}
}