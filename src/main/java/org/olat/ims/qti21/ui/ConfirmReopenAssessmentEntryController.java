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
package org.olat.ims.qti21.ui;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.course.assessment.CourseAssessmentService;
import org.olat.course.nodes.IQTESTCourseNode;
import org.olat.course.run.scoring.ScoreEvaluation;
import org.olat.course.run.userview.UserCourseEnvironment;
import org.olat.ims.qti21.AssessmentTestSession;
import org.olat.modules.assessment.AssessmentEntry;
import org.olat.modules.assessment.AssessmentService;
import org.olat.modules.assessment.Role;
import org.olat.modules.assessment.model.AssessmentEntryStatus;
import org.olat.repository.RepositoryEntry;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 28 juil. 2020<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class ConfirmReopenAssessmentEntryController extends FormBasicController {
	
	private FormLink readOnlyButton;
	
	private Object userObject;
	private final IQTESTCourseNode courseNode;
	private final AssessmentTestSession session;
	private final UserCourseEnvironment assessedUserCourseEnv;
	
	@Autowired
	private AssessmentService assessmentService;
	@Autowired
	private CourseAssessmentService courseAssessmentService;
	
	/**
	 * Confirm reopen of the assessment in a course element.
	 * 
	 * @param ureq The user request
	 * @param wControl The window control
	 * @param assessedUserCourseEnv The user course environment of the assessed identity
	 * @param courseNode The course node
	 * @param session The assessment test session
	 */
	public ConfirmReopenAssessmentEntryController(UserRequest ureq, WindowControl wControl,
			UserCourseEnvironment assessedUserCourseEnv, IQTESTCourseNode courseNode,
			AssessmentTestSession session) {
		super(ureq, wControl, "confirm_reopen_assessment");
		this.assessedUserCourseEnv = assessedUserCourseEnv;
		this.courseNode = courseNode;
		this.session = session;
		initForm(ureq);
	}
	
	/**
	 * Confirm reopen of the assessment of test done within a test
	 * repository entry.
	 * 
	 * @param ureq The user request
	 * @param wControl The window control
	 * @param session The assessment test session
	 */
	public ConfirmReopenAssessmentEntryController(UserRequest ureq, WindowControl wControl,
			AssessmentTestSession session) {
		super(ureq, wControl, "confirm_reopen_assessment");
		assessedUserCourseEnv = null;
		courseNode = null;
		this.session = session;
		initForm(ureq);
	}
	
	public AssessmentTestSession getAssessmentTestSession() {
		return session;
	}

	public Object getUserObject() {
		return userObject;
	}

	public void setUserObject(Object userObject) {
		this.userObject = userObject;
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		
		uifactory.addFormCancelButton("cancel", formLayout, ureq, getWindowControl());
		uifactory.addFormSubmitButton("reopen.assessment", formLayout);
		readOnlyButton = uifactory.addFormLink("correction.readonly", formLayout, Link.BUTTON);
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(readOnlyButton == source) {
			fireEvent(ureq, Event.DONE_EVENT);
		}
		super.formInnerEvent(ureq, source, event);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		doReopen();
		fireEvent(ureq, Event.CHANGED_EVENT);
	}

	@Override
	protected void formCancelled(UserRequest ureq) {
		fireEvent(ureq, Event.CANCELLED_EVENT);
	}
	
	private void doReopen() {
		if(courseNode != null) {
			ScoreEvaluation scoreEval = assessedUserCourseEnv.getScoreAccounting().evalCourseNode(courseNode);
			if (scoreEval != null) {
				ScoreEvaluation reopenedEval = new ScoreEvaluation(scoreEval.getScore(), scoreEval.getPassed(),
						AssessmentEntryStatus.inReview, scoreEval.getUserVisible(),
						scoreEval.getCurrentRunStartDate(), scoreEval.getCurrentRunCompletion(),
						scoreEval.getCurrentRunStatus(), scoreEval.getAssessmentID());
				courseAssessmentService.updateScoreEvaluation(courseNode, reopenedEval, assessedUserCourseEnv,
						getIdentity(), false, Role.coach);
			}
		} else if(session != null) {
			RepositoryEntry testEntry = session.getTestEntry();
			AssessmentEntry assessmentEntry = assessmentService.loadAssessmentEntry(session.getIdentity(), testEntry, null, testEntry);
			if (assessmentEntry != null) {
				assessmentEntry.setAssessmentStatus(AssessmentEntryStatus.inReview);
				assessmentService.updateAssessmentEntry(assessmentEntry);
			}
		}
	}
}
