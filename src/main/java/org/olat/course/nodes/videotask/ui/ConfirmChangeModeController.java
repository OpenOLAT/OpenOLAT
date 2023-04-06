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
package org.olat.course.nodes.videotask.ui;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.id.Identity;
import org.olat.course.assessment.AssessmentHelper;
import org.olat.course.assessment.CourseAssessmentService;
import org.olat.course.nodes.CourseNode;
import org.olat.course.run.environment.CourseEnvironment;
import org.olat.course.run.userview.UserCourseEnvironment;
import org.olat.modules.assessment.Role;
import org.olat.modules.video.VideoAssessmentService;
import org.olat.modules.video.VideoTaskSession;
import org.olat.repository.RepositoryEntry;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 19 janv. 2023<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class ConfirmChangeModeController extends FormBasicController {
	
	private final String subIdent;
	private final String currentMode;
	private final RepositoryEntry entry;
	private final long numOfParticipants;
	private final CourseNode courseNode;
	private final CourseEnvironment courseEnv;

	@Autowired
	private VideoAssessmentService videoAssessmentService;

	@Autowired
	private CourseAssessmentService courseAssessmentService;

	public ConfirmChangeModeController(UserRequest ureq, WindowControl wControl,
									   RepositoryEntry entry, String subIdent, String currentMode,
									   long numOfParticipants, CourseNode courseNode,
									   CourseEnvironment courseEnv) {
		super(ureq, wControl, "confirm_change_mode");
		this.entry = entry;
		this.subIdent = subIdent;
		this.currentMode = currentMode;
		this.numOfParticipants = numOfParticipants;
		this.courseNode = courseNode;
		this.courseEnv = courseEnv;
		initForm(ureq);
	}
	
	public String getCurrentMode() {
		return currentMode;
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		if(formLayout instanceof FormLayoutContainer layoutCont) {
			String i18nKey = numOfParticipants <= 1 ? "change.mode.descr.singular" : "change.mode.descr.plural";
			String msg = translate(i18nKey, Long.toString(numOfParticipants));
			layoutCont.contextPut("msg", msg);
		}
		uifactory.addFormCancelButton("cancel", formLayout, ureq, getWindowControl());
		uifactory.addFormSubmitButton("change.mode", formLayout);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		resetAttempts();
		videoAssessmentService.deleteTaskSessions(entry, subIdent);
		fireEvent(ureq, Event.DONE_EVENT);
	}

	private void resetAttempts() {
		List<VideoTaskSession> taskSessions = videoAssessmentService.getTaskSessions(entry, subIdent);
		Set<Identity> identities = taskSessions.stream().map(VideoTaskSession::getIdentity).collect(Collectors.toSet());
		for (Identity identity : identities) {
			UserCourseEnvironment userCourseEnv = AssessmentHelper.createAndInitUserCourseEnvironment(identity, courseEnv);
			courseAssessmentService.updateAttempts(courseNode, 0, null, userCourseEnv, getIdentity(), Role.coach);
		}
	}

	@Override
	protected void formCancelled(UserRequest ureq) {
		fireEvent(ureq, Event.CANCELLED_EVENT);
	}
}
