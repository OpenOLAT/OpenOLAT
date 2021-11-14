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
package org.olat.modules.adobeconnect.ui;

import java.util.List;

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
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.gui.control.generic.dtabs.Activateable2;
import org.olat.core.id.context.ContextEntry;
import org.olat.core.id.context.StateEntry;
import org.olat.core.util.resource.OresHelper;
import org.olat.group.BusinessGroup;
import org.olat.modules.adobeconnect.AdobeConnectMeeting;
import org.olat.repository.RepositoryEntry;

/**
 * 
 * Initial date: 1 mars 2019<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class AdobeConnectRunController extends BasicController implements Activateable2 {
	
	private Link adminLink;
	private Link meetingsLink;
	private final Link backLink;
	
	private final VelocityContainer mainVC;
	private SegmentViewComponent segmentView;
	
	private AdobeConnectMeetingController meetingCtrl;
	private AdobeConnectMeetingsController meetingsCtrl;
	private AdobeConnectEditMeetingsController adminCtrl;
	
	private final String subIdent;
	private final BusinessGroup group;
	private final RepositoryEntry entry;
	private final AdobeConnectMeetingDefaultConfiguration configuration;
	
	private final boolean canView;
	private final boolean readOnly;
	private final boolean moderator;
	private final boolean administrator;
	
	public AdobeConnectRunController(UserRequest ureq, WindowControl wControl, RepositoryEntry entry, String subIdentifier,
			BusinessGroup group, AdobeConnectMeetingDefaultConfiguration configuration, boolean admin, boolean moderator, boolean readOnly) {
		super(ureq, wControl);
		
		this.entry = entry;
		this.subIdent = subIdentifier;
		this.group = group;
		this.configuration = configuration;
		
		this.administrator = admin;
		this.moderator = moderator;
		this.readOnly = readOnly;
		
		canView = !ureq.getUserSession().getRoles().isGuestOnly();

		if(!canView) {
			//no accessible to guests
			mainVC = createVelocityContainer("run");
		} else if(administrator) {
			mainVC = createVelocityContainer("run_admin");
			
			segmentView = SegmentViewFactory.createSegmentView("segments", mainVC, this);
			meetingsLink = LinkFactory.createLink("meetings.title", mainVC, this);
			segmentView.addSegment(meetingsLink, true);
			
			adminLink = LinkFactory.createLink("meetings.admin.title", mainVC, this);
			segmentView.addSegment(adminLink, false);
			
			doOpenMeetings(ureq);
		} else {
			mainVC = createVelocityContainer("run");
			meetingsCtrl = new AdobeConnectMeetingsController(ureq, wControl, entry, subIdent, group, admin, moderator);
			listenTo(meetingsCtrl);
			mainVC.put("meetings", meetingsCtrl.getInitialComponent());
		}
		backLink = LinkFactory.createLinkBack(mainVC, this);

		putInitialPanel(mainVC);
	}

	@Override
	public void activate(UserRequest ureq, List<ContextEntry> entries, StateEntry state) {
		if(entries == null || entries.isEmpty()) return;
		
		String type = entries.get(0).getOLATResourceable().getResourceableTypeName();
		if("Meetings".equalsIgnoreCase(type)) {
			if(canView) {
				doOpenMeetings(ureq);
				if(segmentView != null) {
					segmentView.select(meetingsLink);
				}
			}
		} else if("Administration".equalsIgnoreCase(type)) {
			if(administrator) {
				doOpenAdmin(ureq);
				if(segmentView != null) { 
					segmentView.select(adminLink);
				}
			}
		} else if("Meeting".equalsIgnoreCase(type)) {
			if(canView) {
				doOpenMeetings(ureq);
				if(segmentView != null) {
					segmentView.select(meetingsLink);
				}
				Long meetingKey = entries.get(0).getOLATResourceable().getResourceableId();
				if(meetingsCtrl.hasMeetingByKey(meetingKey)) {
					doSelectMeeting(ureq, meetingsCtrl.getMeetingByKey(meetingKey));
				}
			}
		}
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		if(backLink == source) {
			back();
		} else if(source == segmentView) {
			if(event instanceof SegmentViewEvent) {
				SegmentViewEvent sve = (SegmentViewEvent)event;
				String segmentCName = sve.getComponentName();
				Component clickedLink = mainVC.getComponent(segmentCName);
				if (clickedLink == meetingsLink) {
					doOpenMeetings(ureq);
				} else if (clickedLink == adminLink){
					doOpenAdmin(ureq);
				}
			}
		}
	}
	
	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if(meetingsCtrl == source) {
			if(event instanceof SelectAdobeConnectMeetingEvent) {
				SelectAdobeConnectMeetingEvent se = (SelectAdobeConnectMeetingEvent)event;
				doSelectMeeting(ureq, se.getMeeting());
			}
		} else if(meetingCtrl == source) {
			if(event == Event.BACK_EVENT) {
				back();
			}
		}
	}
	
	private void back() {
		if(meetingCtrl != null) {
			mainVC.remove(meetingCtrl.getInitialComponent());
			removeAsListenerAndDispose(meetingCtrl);
			meetingCtrl = null;
		}
	}
	
	private void doOpenMeetings(UserRequest ureq) {
		if(meetingsCtrl == null) {
			WindowControl bwControl = addToHistory(ureq, OresHelper.createOLATResourceableInstance("Meetings", 0l), null);
			meetingsCtrl = new AdobeConnectMeetingsController(ureq, bwControl,
					entry, subIdent, group, administrator, moderator);
			listenTo(meetingsCtrl);
		} else {
			meetingsCtrl.updateModel();
			addToHistory(ureq, meetingsCtrl);
		}
		mainVC.put("segmentCmp", meetingsCtrl.getInitialComponent());
	}
	
	private void doOpenAdmin(UserRequest ureq) {
		if(adminCtrl == null) {
			WindowControl bwControl = addToHistory(ureq, OresHelper.createOLATResourceableInstance("Administration", 0l), null);
			adminCtrl = new AdobeConnectEditMeetingsController(ureq, bwControl,
					entry, subIdent, group, configuration, readOnly);
			listenTo(adminCtrl);
		} else {
			addToHistory(ureq, adminCtrl);
		}
		mainVC.put("segmentCmp", adminCtrl.getInitialComponent());
	}
	
	private void doSelectMeeting(UserRequest ureq, AdobeConnectMeeting meeting) {
		removeAsListenerAndDispose(meetingCtrl);
		meetingCtrl = null;
		
		if(meeting == null) {
			showWarning("warning.no.meeting");
		} else {
			WindowControl bwControl = addToHistory(ureq, OresHelper.createOLATResourceableInstance("Meeting", meeting.getKey()), null);
			meetingCtrl = new AdobeConnectMeetingController(ureq, bwControl, meeting, configuration, administrator, moderator, readOnly);
			listenTo(meetingCtrl);
			mainVC.put("meeting", meetingCtrl.getInitialComponent());
		}
	}
}
