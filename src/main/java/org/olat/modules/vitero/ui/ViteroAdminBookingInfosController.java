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
import org.olat.modules.vitero.model.ViteroBooking;
import org.olat.modules.vitero.model.ViteroGroup;

/**
 * 
 * Description:<br>
 * 
 * <P>
 * Initial Date:  13 oct. 2011 <br>
 *
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
public class ViteroAdminBookingInfosController extends BasicController {
	
	private final Link infosLink;
	private final Link membersLink;
	private final SegmentViewComponent segmentView;
	private final VelocityContainer mainVC;
	
	private ViteroAdminBookingRawInfosController infosController;
	private ViteroAdminBookingMembersController membersController;
	
	private final ViteroBooking booking;
	private final ViteroGroup group;
	
	public ViteroAdminBookingInfosController(UserRequest ureq, WindowControl wControl, ViteroBooking booking, ViteroGroup group) {
		super(ureq, wControl);
		
		this.booking = booking;
		this.group = group;
		
		mainVC = createVelocityContainer("vitero_admin");
		
		segmentView = SegmentViewFactory.createSegmentView("segments", mainVC, this);
		infosLink = LinkFactory.createLink("vitero.account", mainVC, this);
		segmentView.addSegment(infosLink, true);
		
		membersLink = LinkFactory.createLink("users", mainVC, this);
		segmentView.addSegment(membersLink, false);
		
		doOpenInfos(ureq);
		
		putInitialPanel(mainVC);
		
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		if(source == segmentView) {
			if(event instanceof SegmentViewEvent) {
				SegmentViewEvent sve = (SegmentViewEvent)event;
				String segmentCName = sve.getComponentName();
				Component clickedLink = mainVC.getComponent(segmentCName);
				if (clickedLink == infosLink) {
					doOpenInfos(ureq);
				} else if (clickedLink == membersLink){
					doOpenMembers(ureq);
				}
			}
		}
	}
	
	private void doOpenInfos(UserRequest ureq) {
		if(infosController == null) {
			infosController = new ViteroAdminBookingRawInfosController(ureq, getWindowControl(), booking, group);
			listenTo(infosController);
		} 
		mainVC.put("segmentCmp", infosController.getInitialComponent());
	}

	private void doOpenMembers(UserRequest ureq) {
		if(membersController == null) {
			membersController = new ViteroAdminBookingMembersController(ureq, getWindowControl(), booking);
			listenTo(membersController);
		} 
		mainVC.put("segmentCmp", membersController.getInitialComponent());
	}
}