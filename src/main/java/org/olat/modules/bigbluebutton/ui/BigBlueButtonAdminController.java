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
	private final Link serversLink;
	private final Link meetingsLink;
	private final Link templatesLink;
	private final Link calendarLink;
	private final SegmentViewComponent segmentView;
	private final VelocityContainer mainVC;
	
	private final boolean configurationReadOnly;

	private BigBlueButtonAdminServersController serversCtrl;
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
			configurationLink.setElementCssClass("o_sel_bbb_configuration");
			segmentView.addSegment(configurationLink, true);
		}
		serversLink = LinkFactory.createLink("servers.title", mainVC, this);
		serversLink.setElementCssClass("o_sel_bbb_servers");
		segmentView.addSegment(serversLink, false);
		templatesLink = LinkFactory.createLink("templates.title", mainVC, this);
		templatesLink.setElementCssClass("o_sel_bbb_templates");
		segmentView.addSegment(templatesLink, false);
		meetingsLink = LinkFactory.createLink("meetings.title", mainVC, this);
		meetingsLink.setElementCssClass("o_sel_bbb_meetings");
		segmentView.addSegment(meetingsLink, false);
		calendarLink = LinkFactory.createLink("calendar.title", mainVC, this);
		calendarLink.setElementCssClass("o_sel_bbb_calendar");
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
		} else if("Servers".equalsIgnoreCase(type)) {
			doOpenServers(ureq);
			segmentView.select(serversLink);
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
				} else if (clickedLink == templatesLink) {
					doOpenTemplates(ureq);
				} else if (clickedLink == meetingsLink) {
					doOpenMeetings(ureq);
				} else if (clickedLink == calendarLink) {
					doOpenCalendar(ureq);
				} else if (clickedLink == serversLink) {
					doOpenServers(ureq);
				}
			}
		}
	}
	
	private void doOpenConfiguration(UserRequest ureq) {
		removeAsListenerAndDispose(configCtrl);

		WindowControl bwControl = addToHistory(ureq, OresHelper.createOLATResourceableInstance("Configuration", 0l), null);
		configCtrl = new BigBlueButtonConfigurationController(ureq, bwControl);
		listenTo(configCtrl);

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
	
	private void doOpenServers(UserRequest ureq) {
		if(serversCtrl == null) {
			WindowControl bwControl = addToHistory(ureq, OresHelper.createOLATResourceableInstance("Servers", 0l), null);
			serversCtrl = new BigBlueButtonAdminServersController(ureq, bwControl);
			listenTo(serversCtrl);
		} else {
			addToHistory(ureq, serversCtrl);
		}
		mainVC.put("segmentCmp", serversCtrl.getInitialComponent());
	}
}
