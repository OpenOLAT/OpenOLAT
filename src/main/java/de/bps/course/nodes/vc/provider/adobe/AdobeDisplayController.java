//<OLATCE-103>
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
 * BPS Bildungsportal Sachsen GmbH, http://www.bps-system.de
 * <p>
 */
package de.bps.course.nodes.vc.provider.adobe;

import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.gui.media.RedirectMediaResource;

import de.bps.course.nodes.vc.MeetingDate;
import de.bps.course.nodes.vc.provider.VCProvider;

/**
 * 
 * Description:<br>
 * Display controller for Adobe Connect implementation, mostly used as
 * run controller in the course node.
 * 
 * <P>
 * Initial Date:  16.12.2010 <br>
 * @author skoeber
 */
public class AdobeDisplayController extends BasicController {
	
	private static String COMMAND_START_MEETING = "cmd.start.meeting";
	private static String COMMAND_START_JOIN_MEETING = "cmd.start.join.meeting";
	private static String COMMAND_JOIN_MODERATOR = "cmd.join.moderator";
	private static String COMMAND_JOIN_LEARNER = "cmd.join.learner";
	private static String COMMAND_REMOVE_MEETING = "cmd.remove.meeting";
	private static String COMMAND_SYNC_MEETING = "cmd.sync.meeting";

	// objects for run view
	private VelocityContainer runVC;
	private String roomId;
	private Link startModerator, startJoinLearner, joinModerator, joinLearner;
	private Link removeMeeting, updateMeeting;
	
	// data
	private List<MeetingDate> dateList = new ArrayList<MeetingDate>();
	private AdobeConnectConfiguration config;
	private MeetingDate meeting;
	private Date allBegin, allEnd;

	private VCProvider adobe;

	public AdobeDisplayController(UserRequest ureq, WindowControl wControl, String roomId, String name, String description,
			boolean isModerator, boolean readOnly, AdobeConnectConfiguration config, VCProvider provider) {
		super(ureq, wControl);
		this.roomId = roomId;
		this.adobe = provider;
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
		
		startModerator = LinkFactory.createButton(COMMAND_START_MEETING, runVC, this);
		startModerator.setVisible(!readOnly);
		startJoinLearner = LinkFactory.createButton(COMMAND_START_JOIN_MEETING, runVC, this);
		startJoinLearner.setVisible(!readOnly);
		joinLearner = LinkFactory.createButton(COMMAND_JOIN_LEARNER, runVC, this);
		joinLearner.setVisible(!readOnly);
		joinModerator = LinkFactory.createButton(COMMAND_JOIN_MODERATOR, runVC, this);
		joinModerator.setVisible(!readOnly);
		removeMeeting = LinkFactory.createButton(COMMAND_REMOVE_MEETING, runVC, this);
		removeMeeting.setVisible(!readOnly);
		updateMeeting = LinkFactory.createButton(COMMAND_SYNC_MEETING, runVC, this);
		updateMeeting.setVisible(!readOnly);
		// set target to be able to open new browser window on event
		startJoinLearner.setTarget("_blank");
		joinLearner.setTarget("_blank");
		joinModerator.setTarget("_blank");
		// render the right button
		boolean isUseDates = config.isUseMeetingDates();
		boolean isMeeting = !isUseDates | meeting != null;
		boolean exists = adobe.existsClassroom(roomId, config);
		boolean guestCanStart = config.isGuestStartMeetingAllowed();
		runVC.contextPut("isModerator", isModerator);
		runVC.contextPut("exists", exists);
		runVC.contextPut("guestCanStart", guestCanStart);
		runVC.contextPut("useDates", isUseDates);
		joinLearner.setEnabled(isMeeting & exists);

		putInitialPanel(runVC);
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		if(source == startModerator) {
			/*
			 * create new meeting room and prepare to join it
			 */
			boolean success = adobe.createClassroom(roomId, null, null, allBegin, allEnd, config);
			if(success) {
				runVC.contextPut("exists", true);
				runVC.setDirty(true);
			} else {
				getWindowControl().setError(translate("error.create.room"));
			}
		} else if(source == startJoinLearner) {
			/*
			 * create new meeting room and join immediately as guest
			 */
			boolean success = adobe.createClassroom(roomId, null, null, allBegin, allEnd, config);
			if(success) {
				runVC.contextPut("exists", true);
				runVC.setDirty(true);
				// join meeting as guest
				URL url = adobe.createClassroomGuestUrl(roomId, ureq.getIdentity(), config);
				RedirectMediaResource rmr = new RedirectMediaResource(url.toString());
				ureq.getDispatchResult().setResultingMediaResource(rmr);
				return;
			} else {
				getWindowControl().setError(translate("error.create.room"));
			}
		} else if(source == joinLearner) {
			/*
			 * easiest case: simply generate link to join meeting as guest
			 */
			URL url = adobe.createClassroomGuestUrl(roomId, ureq.getIdentity(), config);
			RedirectMediaResource rmr = new RedirectMediaResource(url.toString());
			ureq.getDispatchResult().setResultingMediaResource(rmr);
			return;
		} else if(source == joinModerator) {
			/*
			 * join meeting as moderator, first prepare user to have appropriate rights
			 */
			boolean success = adobe.existsClassroom(roomId, config);
			// update rights for user to moderate meeting
			if(success) {
				success = adobe.createModerator(ureq.getIdentity(), roomId);
			} else {
				// room not found, should not appear
				getWindowControl().setError(translate("error.no.room"));
				return;
			}
			// login the user as moderator
			if(success) {
				success = adobe.login(ureq.getIdentity(), null);
			} else {
				// could not create moderator or update the rights
				getWindowControl().setError(translate("error.update.rights"));
				return;
			}
			// redirect to the meeting
			if(success) {
				URL url = adobe.createClassroomUrl(roomId, ureq.getIdentity(), config);
				RedirectMediaResource rmr = new RedirectMediaResource(url.toString());
				ureq.getDispatchResult().setResultingMediaResource(rmr);
			} else {
				// login failed
				getWindowControl().setError(translate("error.no.login"));
				return;
			}
			return;
		} else if(source == removeMeeting) {
			boolean success = adobe.removeClassroom(roomId, config);
			if(success) {
				runVC.contextPut("exists", false);
				runVC.setDirty(true);
			} else {
				// removing failed
				getWindowControl().setError(translate("error.remove.room"));
			}
		} else if(source == updateMeeting) {
			boolean success = adobe.updateClassroom(roomId, null, null, allBegin, allEnd, config);
			if(success) {
				getWindowControl().setInfo(translate("success.update.room"));
			} else {
				// update failed
				getWindowControl().setError(translate("error.update.room"));
			}
		}
	}

	@Override
	protected void doDispose() {
		// nothing to dispose
	}
}
//</OLATCE-103>