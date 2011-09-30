//<OLATCE-103>
/**
 * 
 * BPS Bildungsportal Sachsen GmbH<br>
 * Bahnhofstrasse 6<br>
 * 09111 Chemnitz<br>
 * Germany<br>
 * 
 * Copyright (c) 2005-2010 by BPS Bildungsportal Sachsen GmbH<br>
 * http://www.bps-system.de<br>
 * 
 * All rights reserved.
 */
package com.frentix.olat.vc.provider.vitero;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;

import de.bps.course.nodes.vc.MeetingDate;
import de.bps.course.nodes.vc.provider.VCProvider;

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
	private String roomId;

	
	// data
	private List<MeetingDate> dateList = new ArrayList<MeetingDate>();
	private ViteroBookingConfiguration config;
	private MeetingDate meeting;
	private Date allBegin, allEnd;

	private VCProvider vitero;

	public ViteroDisplayController(UserRequest ureq, WindowControl wControl, String roomId, String name, String description, boolean isModerator, ViteroBookingConfiguration config, VCProvider provider) {
		super(ureq, wControl);
		this.roomId = roomId;
		this.vitero = provider;
		this.config = config;

		// The dates Table to the Course odes
		if(config.getMeetingDates() != null) dateList.addAll(config.getMeetingDates());

		// select actual meeting
		if(config.isUseMeetingDates()) {
			Date now = new Date((new Date()).getTime() + 15*60*1000); // allow to start meetings about 15 minutes before begin
			for(MeetingDate date : dateList) {
				Date begin = date.getBegin();
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
			meeting = new MeetingDate();
			meeting.setBegin(allBegin);
			meeting.setEnd(allEnd);
			meeting.setTitle(name);
			meeting.setDescription(description);
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
//</OLATCE-103>