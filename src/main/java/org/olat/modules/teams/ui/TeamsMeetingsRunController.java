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
package org.olat.modules.teams.ui;

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
import org.olat.modules.teams.TeamsMeeting;
import org.olat.repository.RepositoryEntry;

/**
 * 
 * Initial date: 20 nov. 2020<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class TeamsMeetingsRunController extends BasicController implements Activateable2 {
	
	private Link adminLink;
	private Link meetingsLink;
	private final Link backLink;
	private final VelocityContainer mainVC;
	private SegmentViewComponent segmentView;

	private TeamsMeetingController meetingCtrl;
	private TeamsMeetingsController meetingsCtrl;
	private TeamsEditMeetingsController adminCtrl;

	private final String subIdent;
	private final BusinessGroup group;
	private final RepositoryEntry entry;

	private boolean readOnly;
	private boolean moderator;
	private boolean administrator;

	public TeamsMeetingsRunController(UserRequest ureq, WindowControl wControl, RepositoryEntry entry, String subIdentifier,
			BusinessGroup group, boolean admin, boolean moderator, boolean readOnly) {
		super(ureq, wControl);
		this.subIdent = subIdentifier;
		this.group = group;
		this.entry = entry;
		
		this.readOnly = readOnly;
		this.moderator = moderator;
		this.administrator = admin;
		
		if(administrator) {
			mainVC = createVelocityContainer("run_admin");
			
			segmentView = SegmentViewFactory.createSegmentView("segments", mainVC, this);
			meetingsLink = LinkFactory.createLink("meetings.title", mainVC, this);
			meetingsLink.setElementCssClass("o_sel_teams_meetings_segment");
			segmentView.addSegment(meetingsLink, true);
			
			adminLink = LinkFactory.createLink("meetings.admin.title", mainVC, this);
			adminLink.setElementCssClass("o_sel_teams_edit_meetings_segment");
			segmentView.addSegment(adminLink, false);
			
			doOpenMeetings(ureq);
		} else {
			mainVC = createVelocityContainer("run");
			meetingsCtrl = new TeamsMeetingsController(ureq, wControl, entry, subIdent, group);
			listenTo(meetingsCtrl);
			mainVC.put("meetings", meetingsCtrl.getInitialComponent());
		}
		backLink = LinkFactory.createLinkBack(mainVC, this);
		
		putInitialPanel(mainVC);
	}

	@Override
	public void activate(UserRequest ureq, List<ContextEntry> entries, StateEntry state) {
		//
	}

	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if(meetingsCtrl == source) {
			if(event instanceof SelectTeamsMeetingEvent) {
				SelectTeamsMeetingEvent se = (SelectTeamsMeetingEvent)event;
				doSelectMeeting(ureq, se.getMeeting());
			}
		} else if(meetingCtrl == source) {
			if(event == Event.BACK_EVENT) {
				back();
			}
		}
		super.event(ureq, source, event);
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
	
	private void doOpenMeetings(UserRequest ureq) {
		if(meetingsCtrl == null) {
			WindowControl bwControl = addToHistory(ureq, OresHelper.createOLATResourceableInstance("Meetings", 0l), null);
			meetingsCtrl = new TeamsMeetingsController(ureq, bwControl, entry, subIdent, group);
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
			adminCtrl = new TeamsEditMeetingsController(ureq, bwControl, entry, subIdent, group, readOnly);
			listenTo(adminCtrl);
		} else {
			addToHistory(ureq, adminCtrl);
		}
		mainVC.put("segmentCmp", adminCtrl.getInitialComponent());
	}
	
	private void back() {
		if(meetingCtrl != null) {
			mainVC.remove(meetingCtrl.getInitialComponent());
			removeAsListenerAndDispose(meetingCtrl);
			meetingCtrl = null;
		}
	}
	
	private void doSelectMeeting(UserRequest ureq, TeamsMeeting meeting) {
		removeAsListenerAndDispose(meetingCtrl);
		meetingCtrl = null;
		
		if(meeting == null) {
			showWarning("warning.no.meeting");
		} else {
			WindowControl bwControl = addToHistory(ureq, OresHelper.createOLATResourceableInstance("Meeting", meeting.getKey()), null);
			meetingCtrl = new TeamsMeetingController(ureq, bwControl, meeting, administrator, moderator, readOnly);
			listenTo(meetingCtrl);
			mainVC.put("meeting", meetingCtrl.getInitialComponent());
		}
	}
}
