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
package org.olat.modules.lecture.ui.addwizard;

import java.util.List;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.impl.Form;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.wizard.StepFormBasicController;
import org.olat.core.gui.control.generic.wizard.StepsEvent;
import org.olat.core.gui.control.generic.wizard.StepsRunContext;
import org.olat.modules.bigbluebutton.BigBlueButtonManager;
import org.olat.modules.bigbluebutton.BigBlueButtonMeeting;
import org.olat.modules.bigbluebutton.BigBlueButtonTemplatePermissions;
import org.olat.modules.bigbluebutton.ui.EditBigBlueButtonMeetingController;
import org.olat.modules.lecture.LectureBlock;
import org.olat.modules.teams.TeamsMeeting;
import org.olat.modules.teams.TeamsService;
import org.olat.modules.teams.ui.EditTeamsMeetingController;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 20 mars 2025<br>
 * @author srosse, stephane.rosse@frentix.com, https://www.frentix.com
 *
 */
public class OnlineMeetingController extends StepFormBasicController {

	private EditTeamsMeetingController teamsMeetingCtrl;
	private EditBigBlueButtonMeetingController bigBlueButtonMeetingCtrl;

	@Autowired
	private TeamsService teamsService;
	@Autowired
	private BigBlueButtonManager bigBlueButtonManager;
	
	public OnlineMeetingController(UserRequest ureq, WindowControl wControl, Form rootForm,
			StepsRunContext runContext, AddLectureContext addLecture) {
		super(ureq, wControl, rootForm, runContext, LAYOUT_BAREBONE, null);
		
		LectureBlock lectureBlock = addLecture.getLectureBlock();
		if(addLecture.isWithBigBlueButtonMeeting()) {
			BigBlueButtonMeeting bigBlueButtonMeeting = addLecture.getBigBlueButtonMeeting();
			if(bigBlueButtonMeeting == null) {
				bigBlueButtonMeeting = bigBlueButtonManager.createMeeting(lectureBlock.getTitle(), lectureBlock.getStartDate(), lectureBlock.getEndDate(),
						null, null, null, getIdentity());
			} else {
				bigBlueButtonMeeting = bigBlueButtonManager.getMeeting(bigBlueButtonMeeting);
			}
			addLecture.setBigBlueButtonMeeting(bigBlueButtonMeeting);
			List<BigBlueButtonTemplatePermissions> permissions = bigBlueButtonManager
					.calculatePermissions(addLecture.getEntry(), null, getIdentity(), ureq.getUserSession().getRoles());
			bigBlueButtonMeetingCtrl = new EditBigBlueButtonMeetingController(ureq, getWindowControl(), rootForm, bigBlueButtonMeeting, permissions);
			listenTo(bigBlueButtonMeetingCtrl);
			bigBlueButtonMeetingCtrl.removeDates();
		} else if(addLecture.isWithTeamsMeeting()) {
			TeamsMeeting teamsMeeting = addLecture.getTeamsMeeting();
			if(teamsMeeting == null) {
				teamsMeeting = teamsService.createMeeting(lectureBlock.getTitle(), lectureBlock.getStartDate(), lectureBlock.getEndDate(),
						null, null, null, getIdentity());
			} else {
				teamsMeeting = teamsService.getMeeting(teamsMeeting);
			}
			addLecture.setTeamsMeeting(teamsMeeting);
			teamsMeetingCtrl = new EditTeamsMeetingController(ureq, getWindowControl(), teamsMeeting);
			listenTo(teamsMeetingCtrl);
			teamsMeetingCtrl.removeDates();
		}
		
		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		if(bigBlueButtonMeetingCtrl != null) {
			formLayout.add(bigBlueButtonMeetingCtrl.getInitialFormItem());
		} else if(teamsMeetingCtrl != null) {
			formLayout.add(teamsMeetingCtrl.getInitialFormItem());
		}
	}

	@Override
	public void event(UserRequest ureq, Controller source, Event event) {
		if(bigBlueButtonMeetingCtrl == source || teamsMeetingCtrl == source) {
			if(event == Event.DONE_EVENT || event == Event.CHANGED_EVENT) {
				fireEvent(ureq, StepsEvent.INFORM_FINISHED);
			}
		}
		super.event(ureq, source, event);
	}
	
	@Override
	protected boolean validateFormLogic(UserRequest ureq) {
		boolean allOk = true;
		if(bigBlueButtonMeetingCtrl != null) {
			allOk &= bigBlueButtonMeetingCtrl.validateFormLogic(ureq);
		}
		if(teamsMeetingCtrl != null) {
			allOk &= teamsMeetingCtrl.validateFormLogic(ureq);
		}
		return allOk;
	}

	@Override
	protected void formOK(UserRequest ureq) {
		if(bigBlueButtonMeetingCtrl != null) {
			bigBlueButtonMeetingCtrl.formOK(ureq);
		}
		if(teamsMeetingCtrl != null) {
			teamsMeetingCtrl.formOK(ureq);
		}
		fireEvent(ureq, StepsEvent.INFORM_FINISHED);
	}
}
