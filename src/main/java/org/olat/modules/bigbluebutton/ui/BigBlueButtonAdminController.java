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
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.gui.control.generic.dtabs.Activateable2;
import org.olat.core.id.Roles;
import org.olat.core.id.context.ContextEntry;
import org.olat.core.id.context.StateEntry;
import org.olat.core.util.resource.OresHelper;

/**
 * 
 * Initial date: 28 févr. 2019<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class BigBlueButtonAdminController extends BasicController implements Activateable2 {
	
	private Link configurationLink;
	private final Link meetingsLink;
	private final Link templatesLink;
	private final Link calendarLink;
	private final SegmentViewComponent segmentView;
	private final VelocityContainer mainVC;
	
	private final boolean configurationReadOnly;
	
	private BigBlueButtonConfigurationController configCtrl;
	private BigBlueButtonAdminMeetingsController meetingsCtrl;
	private BigBlueButtonAdminTemplatesController templatesCtrl;
	private BigBlueButtonMeetingsCalendarController calendarsCtrl;
	
	public BigBlueButtonAdminController(UserRequest ureq, WindowControl wControl) {
		super(ureq, wControl);
		
		Roles roles = ureq.getUserSession().getRoles();
		configurationReadOnly = !roles.isAdministrator() && !roles.isSystemAdmin();
		
		mainVC = createVelocityContainer("bbb_admin");
		
			segmentView = SegmentViewFactory.createSegmentView("segments", mainVC, this);
		if(!configurationReadOnly) {
			configurationLink = LinkFactory.createLink("account.configuration", mainVC, this);
			segmentView.addSegment(configurationLink, true);
		}
		templatesLink = LinkFactory.createLink("templates.title", mainVC, this);
		segmentView.addSegment(templatesLink, false);
		meetingsLink = LinkFactory.createLink("meetings.title", mainVC, this);
		segmentView.addSegment(meetingsLink, false);
		calendarLink = LinkFactory.createLink("calendar.title", mainVC, this);
		segmentView.addSegment(calendarLink, false);
		
		if(configurationReadOnly) {
			doOpenMeetings(ureq);
			segmentView.select(meetingsLink);
		} else {
			doOpenConfiguration(ureq);
		}
		
		putInitialPanel(mainVC);
	}

	@Override
	protected void doDispose() {
		//
	}

	@Override
	public void activate(UserRequest ureq, List<ContextEntry> entries, StateEntry state) {
		if(entries == null || entries.isEmpty()) return;

		String type = entries.get(0).getOLATResourceable().getResourceableTypeName();
		if("Configuration".equalsIgnoreCase(type) && !configurationReadOnly) {
			doOpenConfiguration(ureq);
			segmentView.select(configurationLink);
		} else if("Templates".equalsIgnoreCase(type)) {
			doOpenTemplates(ureq);
			segmentView.select(templatesLink);
		} else if("Meetings".equalsIgnoreCase(type)) {
			doOpenMeetings(ureq);
			segmentView.select(meetingsLink);
		} else if("Calendar".equalsIgnoreCase(type)) {
			doOpenCalendar(ureq);
			segmentView.select(calendarLink);
		}
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		if(source == segmentView) {
			if(event instanceof SegmentViewEvent) {
				SegmentViewEvent sve = (SegmentViewEvent)event;
				String segmentCName = sve.getComponentName();
				Component clickedLink = mainVC.getComponent(segmentCName);
				if (clickedLink == configurationLink) {
					doOpenConfiguration(ureq);
				} else if (clickedLink == templatesLink){
					doOpenTemplates(ureq);
				} else if (clickedLink == meetingsLink){
					doOpenMeetings(ureq);
				} else if (clickedLink == calendarLink){
					doOpenCalendar(ureq);
				}
			}
		}
	}
	
	private void doOpenConfiguration(UserRequest ureq) {
		if(configCtrl == null) {
			WindowControl bwControl = addToHistory(ureq, OresHelper.createOLATResourceableInstance("Configuration", 0l), null);
			configCtrl = new BigBlueButtonConfigurationController(ureq, bwControl);
			listenTo(configCtrl);
		} else {
			addToHistory(ureq, configCtrl);
		}
		mainVC.put("segmentCmp", configCtrl.getInitialComponent());
	}
	
	private void doOpenTemplates(UserRequest ureq) {
		if(templatesCtrl == null) {
			WindowControl bwControl = addToHistory(ureq, OresHelper.createOLATResourceableInstance("Templates", 0l), null);
			templatesCtrl = new BigBlueButtonAdminTemplatesController(ureq, bwControl, configurationReadOnly);
			listenTo(templatesCtrl);
		} else {
			addToHistory(ureq, templatesCtrl);
		}
		mainVC.put("segmentCmp", templatesCtrl.getInitialComponent());
	}
	
	private void doOpenMeetings(UserRequest ureq) {
		if(meetingsCtrl == null) {
			WindowControl bwControl = addToHistory(ureq, OresHelper.createOLATResourceableInstance("Meetings", 0l), null);
			meetingsCtrl = new BigBlueButtonAdminMeetingsController(ureq, bwControl);
			listenTo(meetingsCtrl);
		} else {
			addToHistory(ureq, meetingsCtrl);
		}
		mainVC.put("segmentCmp", meetingsCtrl.getInitialComponent());
	}
	
	private void doOpenCalendar(UserRequest ureq) {
		if(calendarsCtrl == null) {
			WindowControl bwControl = addToHistory(ureq, OresHelper.createOLATResourceableInstance("Calendar", 0l), null);
			calendarsCtrl = new BigBlueButtonMeetingsCalendarController(ureq, bwControl);
			listenTo(calendarsCtrl);
		} else {
			addToHistory(ureq, calendarsCtrl);
		}
		mainVC.put("segmentCmp", calendarsCtrl.getInitialComponent());
	}
	
}
