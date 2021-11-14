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

/**
 * 
 * Description:<br>
 * 
 * <P>
 * Initial Date:  6 oct. 2011 <br>
 *
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
public class ViteroAdminController extends BasicController  {
	
	private final Link accountLink;
	private Link bookingsLink;
	private SegmentViewComponent segmentView;
	private final VelocityContainer mainVC;
	
	private ViteroConfigurationController configController;
	private ViteroBookingsAdminController bookingController;
	
	public ViteroAdminController(UserRequest ureq, WindowControl wControl) {
		super(ureq, wControl);
		
		mainVC = createVelocityContainer("vitero_admin");
		
		segmentView = SegmentViewFactory.createSegmentView("segments", mainVC, this);
		accountLink = LinkFactory.createLink("vitero.account", mainVC, this);
		segmentView.addSegment(accountLink, true);
		
		bookingsLink = LinkFactory.createLink("booking.title", mainVC, this);
		segmentView.addSegment(bookingsLink, false);
		
		doOpenAccountSettings(ureq);
		
		putInitialPanel(mainVC);
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
				} else if (clickedLink == bookingsLink){
					doOpenBookings(ureq);
				}
			}
		}
	}
	
	private void doOpenAccountSettings(UserRequest ureq) {
		if(configController == null) {
			configController = new ViteroConfigurationController(ureq, getWindowControl());
			listenTo(configController);
		} 
		mainVC.put("segmentCmp", configController.getInitialComponent());
	}
	
	private void doOpenBookings(UserRequest ureq) {
		if(bookingController == null) {
			bookingController = new ViteroBookingsAdminController(ureq, getWindowControl());
			listenTo(bookingController);
		}
		mainVC.put("segmentCmp", bookingController.getInitialComponent());
	}
}