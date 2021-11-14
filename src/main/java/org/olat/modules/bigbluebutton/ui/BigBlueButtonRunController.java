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
package org.olat.modules.bigbluebutton.ui;

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
import org.olat.modules.bigbluebutton.BigBlueButtonManager;
import org.olat.modules.bigbluebutton.BigBlueButtonMeeting;
import org.olat.modules.bigbluebutton.BigBlueButtonMeetingTemplate;
import org.olat.modules.bigbluebutton.BigBlueButtonTemplatePermissions;
import org.olat.repository.RepositoryEntry;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 18 mars 2020<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class BigBlueButtonRunController extends BasicController implements Activateable2 {
	
	private Link adminLink;
	private Link meetingsLink;
	private final Link backLink;
	
	private final VelocityContainer mainVC;
	private SegmentViewComponent segmentView;
	
	private BigBlueButtonMeetingController meetingCtrl;
	private BigBlueButtonMeetingsController meetingsCtrl;
	private BigBlueButtonEditMeetingsController adminCtrl;
	
	private final String subIdent;
	private final BusinessGroup group;
	private final RepositoryEntry entry;
	private final BigBlueButtonMeetingDefaultConfiguration configuration;
	
	private boolean readOnly;
	private boolean moderator;
	private boolean administrator;

	@Autowired
	private BigBlueButtonManager bigBlueButtonManager;
	
	public BigBlueButtonRunController(UserRequest ureq, WindowControl wControl, RepositoryEntry entry, String subIdentifier,
			BusinessGroup group, BigBlueButtonMeetingDefaultConfiguration configuration, boolean admin, boolean moderator, boolean readOnly) {
		super(ureq, wControl);
		this.subIdent = subIdentifier;
		this.group = group;
		this.entry = entry;
		this.readOnly = readOnly;
		this.configuration = configuration;
		this.administrator = admin;
		this.moderator = moderator;
		
		if(administrator && hasAtLeastOneTemplate(ureq)) {
			mainVC = createVelocityContainer("run_admin");
			
			segmentView = SegmentViewFactory.createSegmentView("segments", mainVC, this);
			meetingsLink = LinkFactory.createLink("meetings.title", mainVC, this);
			meetingsLink.setElementCssClass("o_sel_bbb_meetings_segment");
			segmentView.addSegment(meetingsLink, true);
			
			adminLink = LinkFactory.createLink("meetings.admin.title", mainVC, this);
			adminLink.setElementCssClass("o_sel_bbb_edit_meetings_segment");
			segmentView.addSegment(adminLink, false);
			
			doOpenMeetings(ureq);
		} else {
			mainVC = createVelocityContainer("run");
			meetingsCtrl = new BigBlueButtonMeetingsController(ureq, wControl, entry, subIdent, group, admin, moderator);
			listenTo(meetingsCtrl);
			mainVC.put("meetings", meetingsCtrl.getInitialComponent());
		}
		backLink = LinkFactory.createLinkBack(mainVC, this);

		putInitialPanel(mainVC);
	}

	
	private boolean hasAtLeastOneTemplate(UserRequest ureq) {
		// TODO bbb should also apply to group config form: don't let regular students enable bbb in groups when they have no 
		// template to choose from.
		List<BigBlueButtonTemplatePermissions> permissions = bigBlueButtonManager
				.calculatePermissions(entry, group, getIdentity(), ureq.getUserSession().getRoles());
		bigBlueButtonManager.getTemplates(permissions);
	
		List<BigBlueButtonMeetingTemplate> templates = bigBlueButtonManager.getTemplates(permissions);
		return !templates.isEmpty();
	}
	
	@Override
	public void activate(UserRequest ureq, List<ContextEntry> entries, StateEntry state) {
		if(entries == null || entries.isEmpty()) return;
		
		String type = entries.get(0).getOLATResourceable().getResourceableTypeName();
		if("Meetings".equalsIgnoreCase(type)) {
			doOpenMeetings(ureq);
			if(segmentView != null) {
				segmentView.select(meetingsLink);
			}
		} else if("Administration".equalsIgnoreCase(type)) {
			if(administrator) {
				doOpenAdmin(ureq);
				if(segmentView != null) { 
					segmentView.select(adminLink);
				}
			}
		} else if("Meeting".equalsIgnoreCase(type)) {
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
			if(event instanceof SelectBigBlueButtonMeetingEvent) {
				SelectBigBlueButtonMeetingEvent se = (SelectBigBlueButtonMeetingEvent)event;
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
			meetingsCtrl = new BigBlueButtonMeetingsController(ureq, bwControl,
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
			adminCtrl = new BigBlueButtonEditMeetingsController(ureq, bwControl, entry, subIdent, group, readOnly);
			listenTo(adminCtrl);
		} else {
			addToHistory(ureq, adminCtrl);
		}
		mainVC.put("segmentCmp", adminCtrl.getInitialComponent());
	}
	
	private void doSelectMeeting(UserRequest ureq, BigBlueButtonMeeting meeting) {
		removeAsListenerAndDispose(meetingCtrl);
		meetingCtrl = null;
		
		if(meeting == null) {
			showWarning("warning.no.meeting");
		} else {
			WindowControl bwControl = addToHistory(ureq, OresHelper.createOLATResourceableInstance("Meeting", meeting.getKey()), null);
			meetingCtrl = new BigBlueButtonMeetingController(ureq, bwControl, meeting, configuration, administrator, moderator, readOnly);
			listenTo(meetingCtrl);
			mainVC.put("meeting", meetingCtrl.getInitialComponent());
		}
	}
}
