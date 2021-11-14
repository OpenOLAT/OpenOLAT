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
 * 12.10.2011 by frentix GmbH, http://www.frentix.com
 * <p>
 */
package org.olat.modules.vitero.ui;

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
import org.olat.core.id.OLATResourceable;
import org.olat.group.BusinessGroup;

/**
 * 
 * Description:<br>
 * 
 * <P>
 * Initial Date:  13 oct. 2011 <br>
 *
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
public class ViteroBookingsRunController extends BasicController {
	
	private Link bookingsLink;
	private Link adminLink;
	private SegmentViewComponent segmentView;
	private VelocityContainer mainVC;
	
	private ViteroBookingsController bookingsController;
	private ViteroBookingsEditController adminController;
	
	private final boolean readOnly;
	private final BusinessGroup group;
	private final OLATResourceable ores;
	private final String subIdentifier;
	private final String resourceName;
	
	public ViteroBookingsRunController(UserRequest ureq, WindowControl wControl, BusinessGroup group, OLATResourceable ores,
			String subIdentifier, String resourceName, boolean admin, boolean readOnly) {
		super(ureq, wControl);
		
		this.group = group;
		this.ores = ores;
		this.readOnly = readOnly;
		this.subIdentifier = subIdentifier;
		this.resourceName = resourceName;
		
		if(ureq.getUserSession().getRoles().isGuestOnly()) {
			//no accessible to guests
		} else if(admin) {
			mainVC = createVelocityContainer("run_admin");
			
			segmentView = SegmentViewFactory.createSegmentView("segments", mainVC, this);
			bookingsLink = LinkFactory.createLink("booking.title", mainVC, this);
			segmentView.addSegment(bookingsLink, true);
			
			adminLink = LinkFactory.createLink("booking.admin.title", mainVC, this);
			segmentView.addSegment(adminLink, false);
			
			doOpenBookings(ureq);
			
			putInitialPanel(mainVC);
		} else {
			bookingsController = new ViteroBookingsController(ureq, wControl,group, ores, subIdentifier, readOnly);
			listenTo(bookingsController);
			putInitialPanel(bookingsController.getInitialComponent());
		}
	}
	
	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		if(source == segmentView) {
			if(event instanceof SegmentViewEvent) {
				SegmentViewEvent sve = (SegmentViewEvent)event;
				String segmentCName = sve.getComponentName();
				Component clickedLink = mainVC.getComponent(segmentCName);
				if (clickedLink == bookingsLink) {
					doOpenBookings(ureq);
				} else if (clickedLink == adminLink){
					doOpenAdmin(ureq);
				}
			}
		}
	}
	
	private void doOpenBookings(UserRequest ureq) {
		if(bookingsController == null) {
			bookingsController = new ViteroBookingsController(ureq, getWindowControl(), group, ores, subIdentifier, readOnly);
			listenTo(bookingsController);
		} 
		mainVC.put("segmentCmp", bookingsController.getInitialComponent());
	}
	
	private void doOpenAdmin(UserRequest ureq) {
		if(adminController == null) {
			adminController = new ViteroBookingsEditController(ureq, getWindowControl(), group, ores, subIdentifier, resourceName, readOnly);
			listenTo(adminController);
		} 
		mainVC.put("segmentCmp", adminController.getInitialComponent());
	}
}