/**
 * OLAT - Online Learning and Training<br>
 * http://www.olat.org
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); <br>
 * you may not use this file except in compliance with the License.<br>
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing,<br>
 * software distributed under the License is distributed on an "AS IS" BASIS, <br>
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
 * See the License for the specific language governing permissions and <br>
 * limitations under the License.
 * <p>
 * Copyright (c) frentix GmbH<br>
 * http://www.frentix.com<br>
 * <p>
 */
package com.frentix.olat.course.nodes.vitero;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;

import com.frentix.olat.vitero.manager.ViteroManager;
import com.frentix.olat.vitero.model.ViteroBooking;


/**
 * 
 * Description:<br>
 * TODO: srosse Class Description for ViteroDisplayController
 * 
 * <P>
 * Initial Date:  26 sept. 2011 <br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
public class ViteroDisplayController extends BasicController {

	// objects for run view
	private VelocityContainer runVC;
	
	// data
	private List<ViteroBooking> dateList = new ArrayList<ViteroBooking>();
	private ViteroBookingConfiguration config;
	private ViteroBooking meeting;
	private Date allBegin, allEnd;

	private ViteroManager vitero;

	public ViteroDisplayController(UserRequest ureq, WindowControl wControl, String roomId, String name, String description, boolean isModerator, ViteroBookingConfiguration config, ViteroManager provider) {
		super(ureq, wControl);
		this.vitero = provider;
		this.config = config;

		// The dates Table to the Course odes
		if(config.getMeetingDates() != null) dateList.addAll(config.getMeetingDates());

		// select actual meeting
		if(config.isUseMeetingDates()) {
			Date now = new Date((new Date()).getTime() + 15*60*1000); // allow to start meetings about 15 minutes before begin
			for(ViteroBooking date : dateList) {
				Date begin = date.getStart();
				Date end = date.getEnd();
				if(now.after(begin) & now.before(end)) {
					meeting = date;
				}
				allBegin = allBegin == null ? begin : begin.before(allBegin) ? begin : allBegin;
				allEnd = allEnd == null ? end : end.after(allEnd) ? end : allEnd;
			}
		} else {
			allBegin = new Date();
			allEnd = new Date(allBegin.getTime() + 365*24*60*60*1000); // preset one year
			meeting = new ViteroBooking();
			meeting.setStart(allBegin);
			meeting.setEnd(allEnd);
		}
		
		runVC = createVelocityContainer("run");
		


		putInitialPanel(runVC);
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		//
	}

	@Override
	protected void doDispose() {
		// nothing to dispose
	}
}