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
package org.olat.modules.gotomeeting.ui;

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

/**
 * 
 * Initial date: 21.03.2016<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class GoToAdminController extends BasicController implements Activateable2 {
	
	private Link accountLink, organizersLink, meetingsLink;
	private SegmentViewComponent segmentView;
	private final VelocityContainer mainVC;
	
	private GoToConfigurationController configController;
	private GoToMeetingsAdminController meetingsListCtrl;
	private GoToOrganizerListAdminController organizerListCtrl;
	
	public GoToAdminController(UserRequest ureq, WindowControl wControl) {
		super(ureq, wControl);

		mainVC = createVelocityContainer("gotomeeting_admin");
		
		segmentView = SegmentViewFactory.createSegmentView("segments", mainVC, this);
		accountLink = LinkFactory.createLink("goto.configuration", mainVC, this);
		segmentView.addSegment(accountLink, true);
		
		organizersLink = LinkFactory.createLink("organizers.title", mainVC, this);
		segmentView.addSegment(organizersLink, false);
		
		meetingsLink = LinkFactory.createLink("meetings.title", mainVC, this);
		segmentView.addSegment(meetingsLink, false);
		
		doOpenAccountSettings(ureq);
		putInitialPanel(mainVC);
	}

	@Override
	public void activate(UserRequest ureq, List<ContextEntry> entries, StateEntry state) {
		if(entries == null || entries.isEmpty()) return;
		
		String type = entries.get(0).getOLATResourceable().getResourceableTypeName();
		if("Configuration".equalsIgnoreCase(type)) {
			doOpenAccountSettings(ureq);
			segmentView.select(accountLink);
		} else if("Organizers".equalsIgnoreCase(type)) {
			List<ContextEntry> subEntries = entries.subList(1, entries.size());
			doOpenOrganizersList(ureq).activate(ureq, subEntries, entries.get(0).getTransientState());
			segmentView.select(organizersLink);
		} else if("Meetings".equalsIgnoreCase(type)) {
			doOpenMeetingsList(ureq);
			segmentView.select(meetingsLink);
		}
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		if(source == segmentView) {
			if(event instanceof SegmentViewEvent) {
				SegmentViewEvent sve = (SegmentViewEvent)event;
				String segmentCName = sve.getComponentName();
				Component clickedLink = mainVC.getComponent(segmentCName);
				if (clickedLink == accountLink) {
					doOpenAccountSettings(ureq);
				} else if (clickedLink == organizersLink){
					doOpenOrganizersList(ureq);
				} else if (clickedLink == meetingsLink){
					doOpenMeetingsList(ureq);
				}
			}
		}
	}
	
	private void doOpenAccountSettings(UserRequest ureq) {
		if(configController == null) {
			WindowControl swControl = addToHistory(ureq, OresHelper.createOLATResourceableInstance("Configuration", 0l), null);
			configController = new GoToConfigurationController(ureq, swControl);
			listenTo(configController);
		} 
		mainVC.put("segmentCmp", configController.getInitialComponent());
	}

	private GoToOrganizerListAdminController doOpenOrganizersList(UserRequest ureq) {
		if(organizerListCtrl == null) {
			WindowControl swControl = addToHistory(ureq, OresHelper.createOLATResourceableInstance("Organizers", 0l), null);
			organizerListCtrl = new GoToOrganizerListAdminController(ureq, swControl);
			listenTo(organizerListCtrl);
		} 
		mainVC.put("segmentCmp", organizerListCtrl.getInitialComponent());
		return organizerListCtrl;
	}
	
	private void doOpenMeetingsList(UserRequest ureq) {
		if(meetingsListCtrl == null) {
			WindowControl swControl = addToHistory(ureq, OresHelper.createOLATResourceableInstance("Meetings", 0l), null);
			meetingsListCtrl = new GoToMeetingsAdminController(ureq, swControl);
			listenTo(meetingsListCtrl);
		} 
		mainVC.put("segmentCmp", meetingsListCtrl.getInitialComponent());
	}

}
 