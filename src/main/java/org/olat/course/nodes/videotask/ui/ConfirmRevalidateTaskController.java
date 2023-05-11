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

import org.olat.core.commons.persistence.DB;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.course.nodes.VideoTaskCourseNode;
import org.olat.course.run.userview.UserCourseEnvironment;
import org.olat.modules.assessment.Role;
import org.olat.modules.video.VideoAssessmentService;
import org.olat.modules.video.VideoTaskSession;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 30 janv. 2023<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class ConfirmRevalidateTaskController extends FormBasicController {

	private VideoTaskSession taskSession;
	private final VideoTaskCourseNode courseNode;
	private final UserCourseEnvironment assessedUserCourseEnv;
	private final String text;
	private final String buttonKey;
	private final boolean apply;

	@Autowired
	private DB dbInstance;
	@Autowired
	private VideoAssessmentService videoAssessmentService;
	
	public ConfirmRevalidateTaskController(UserRequest ureq, WindowControl wControl, VideoTaskSession taskSession,
										   String text, String buttonKey, boolean apply, VideoTaskCourseNode courseNode,
										   UserCourseEnvironment assessedUserCourseEnv) {
		super(ureq, wControl, "confirm_revalidate");
		this.text = text;
		this.buttonKey = buttonKey;
		this.apply = apply;
		this.taskSession = taskSession;
		this.courseNode = courseNode;
		this.assessedUserCourseEnv = assessedUserCourseEnv;

		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		uifactory.addStaticTextElement("revalidate.text", "", text, formLayout);
		uifactory.addFormCancelButton("cancel", formLayout, ureq, getWindowControl());
		uifactory.addFormSubmitButton("revalidate", buttonKey, formLayout);
	}
	
	@Override
	protected void formOK(UserRequest ureq) {
		doValidateSession(ureq);
		fireEvent(ureq, Event.DONE_EVENT);
	}

	@Override
	protected void formCancelled(UserRequest ureq) {
		fireEvent(ureq, Event.CANCELLED_EVENT);
	}
	
	private void doValidateSession(UserRequest ureq) {
		taskSession.setCancelled(false);
		taskSession = videoAssessmentService.updateTaskSession(taskSession);
		dbInstance.commit();

		if (apply) {
			courseNode.promoteTaskSession(taskSession, assessedUserCourseEnv, true, getIdentity(), Role.coach, getLocale());
		}

		fireEvent(ureq, Event.CHANGED_EVENT);
	}
}
