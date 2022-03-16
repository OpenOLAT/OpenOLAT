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
package org.olat.course.nodes.iq;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.logging.activity.ThreadLocalUserActivityLogger;
import org.olat.course.assessment.AssessmentHelper;
import org.olat.course.assessment.CourseAssessmentService;
import org.olat.course.nodes.IQTESTCourseNode;
import org.olat.course.run.environment.CourseEnvironment;
import org.olat.course.run.scoring.ScoreEvaluation;
import org.olat.course.run.userview.UserCourseEnvironment;
import org.olat.ims.qti21.AssessmentTestSession;
import org.olat.ims.qti21.QTI21LoggingAction;
import org.olat.ims.qti21.QTI21Service;
import org.olat.modules.assessment.Role;
import org.olat.modules.assessment.model.AssessmentEntryStatus;
import org.olat.modules.assessment.model.AssessmentRunStatus;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 29 nov. 2017<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class ConfirmReopenController extends FormBasicController {
	
	private AssessmentTestSession testSession;
	private final IQTESTCourseNode courseNode;
	private final CourseEnvironment courseEnv;
	
	@Autowired
	private QTI21Service qtiService;
	@Autowired
	private CourseAssessmentService courseAssessmentService;
	
	public ConfirmReopenController(UserRequest ureq, WindowControl wControl,
			CourseEnvironment courseEnv, IQTESTCourseNode courseNode, AssessmentTestSession testSession) {
		super(ureq, wControl, "confirm_reopen");
		this.testSession = testSession;
		this.courseNode = courseNode;
		this.courseEnv = courseEnv;
		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		uifactory.addFormCancelButton("cancel", formLayout, ureq, getWindowControl());
		uifactory.addFormSubmitButton("reopen", formLayout);
	}
	
	@Override
	protected void formOK(UserRequest ureq) {
		testSession = qtiService.reopenAssessmentTestSession(testSession, getIdentity());
		if(testSession == null) {
			showWarning("");
		} else {
			UserCourseEnvironment assessedUserCourseEnv = AssessmentHelper
					.createAndInitUserCourseEnvironment(testSession.getIdentity(), courseEnv);

			ScoreEvaluation scoreEval = courseAssessmentService.getAssessmentEvaluation(courseNode, assessedUserCourseEnv);
			ScoreEvaluation reopenedScoreEval = new ScoreEvaluation(scoreEval.getScore(), scoreEval.getGrade(),
					scoreEval.getPerformanceClassIdent(), scoreEval.getPassed(), AssessmentEntryStatus.inProgress,
					scoreEval.getUserVisible(), scoreEval.getCurrentRunStartDate(), scoreEval.getCurrentRunCompletion(),
					AssessmentRunStatus.running, testSession.getKey());
			courseAssessmentService.updateScoreEvaluation(courseNode, reopenedScoreEval, assessedUserCourseEnv,
					getIdentity(), false, Role.coach);

			ThreadLocalUserActivityLogger.log(QTI21LoggingAction.QTI_REOPEN_IN_COURSE, getClass());
		}
		fireEvent(ureq, Event.DONE_EVENT);
	}

	@Override
	protected void formCancelled(UserRequest ureq) {
		fireEvent(ureq, Event.CANCELLED_EVENT);
	}
}
