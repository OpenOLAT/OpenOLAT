/**
 * <a href="https://www.openolat.org">
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
 * frentix GmbH, https://www.frentix.com
 * <p>
 */
package org.olat.modules.lecture.ui;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.modules.bigbluebutton.BigBlueButtonManager;
import org.olat.modules.bigbluebutton.BigBlueButtonMeeting;
import org.olat.modules.bigbluebutton.ui.BigBlueButtonMeetingController;
import org.olat.modules.bigbluebutton.ui.BigBlueButtonMeetingDefaultConfiguration;
import org.olat.modules.lecture.LectureBlock;
import org.olat.modules.lecture.LectureBlockRef;
import org.olat.modules.lecture.LectureService;
import org.olat.modules.teams.TeamsMeeting;
import org.olat.modules.teams.TeamsService;
import org.olat.modules.teams.ui.TeamsMeetingController;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 25 mars 2025<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class LectureBlockOnlineMeetingController extends BasicController {
	
	private final Link backLink;
	private final VelocityContainer mainVC;
	
	private TeamsMeetingController teamsMeetingCtrl;
	private BigBlueButtonMeetingController bigBlueButtonMeetingCtrl;

	@Autowired
	private TeamsService teamsService;
	@Autowired
	private LectureService lectureService;
	@Autowired
	private BigBlueButtonManager bigBlueButtonManager;
	
	public LectureBlockOnlineMeetingController(UserRequest ureq, WindowControl wControl,
			LectureBlockRef lectureBlockRef, LectureListRepositoryConfig config, LecturesSecurityCallback secCallback) {
		super(ureq, wControl);
		
		mainVC = createVelocityContainer("lecture_online_meeting");
		backLink = LinkFactory.createLinkBack(mainVC, this);
		
		String titleSize = config.getTitleSize() <= 0 ? "" : "h" + config.getTitleSize();
		mainVC.contextPut("titleSize", titleSize);
		
		LectureBlock lectureBlock = lectureService.getLectureBlock(lectureBlockRef);
		if(lectureBlock.getBBBMeeting() != null) {
			BigBlueButtonMeeting meeting = bigBlueButtonManager.getMeeting(lectureBlock.getBBBMeeting());
			BigBlueButtonMeetingDefaultConfiguration configuration = new BigBlueButtonMeetingDefaultConfiguration(false);
			bigBlueButtonMeetingCtrl = new BigBlueButtonMeetingController(ureq, getWindowControl(),
				meeting,  configuration, secCallback.isOnlineMeetingAdministrator(), secCallback.isOnlineMeetingModerator(),
				!secCallback.canEditConfiguration());
			listenTo(bigBlueButtonMeetingCtrl);
			mainVC.put("component", bigBlueButtonMeetingCtrl.getInitialComponent());
		} else if(lectureBlock.getTeamsMeeting() != null) {
			TeamsMeeting meeting = teamsService.getMeeting(lectureBlock.getTeamsMeeting());
			teamsMeetingCtrl = new TeamsMeetingController(ureq, getWindowControl(), meeting,
				secCallback.isOnlineMeetingAdministrator(), secCallback.isOnlineMeetingModerator(),
				!secCallback.canEditConfiguration());
			listenTo(teamsMeetingCtrl);
			mainVC.put("component", teamsMeetingCtrl.getInitialComponent());
		}
		putInitialPanel(mainVC);
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		if(backLink == source) {
			fireEvent(ureq, Event.BACK_EVENT);
		}
	}

}
