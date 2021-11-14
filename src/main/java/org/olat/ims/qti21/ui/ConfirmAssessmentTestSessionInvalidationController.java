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

import java.util.Collections;
import java.util.List;

import org.olat.core.commons.persistence.DB;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.id.Identity;
import org.olat.course.assessment.CourseAssessmentService;
import org.olat.course.nodes.IQTESTCourseNode;
import org.olat.course.run.userview.UserCourseEnvironment;
import org.olat.ims.qti21.AssessmentTestSession;
import org.olat.ims.qti21.QTI21Service;
import org.olat.modules.assessment.AssessmentEntry;
import org.olat.modules.assessment.Role;
import org.olat.modules.grading.GradingAssignment;
import org.olat.modules.grading.GradingAssignmentStatus;
import org.olat.modules.grading.GradingService;
import org.olat.repository.RepositoryEntry;
import org.olat.user.UserManager;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * A controller to confirm to set a test session as invalid and eventually
 * promote the results of the next last session to the assessment entry. 
 * 
 * Initial date: 27 juil. 2020<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class ConfirmAssessmentTestSessionInvalidationController extends FormBasicController {
	
	private FormLink invalidateButton;
	
	private AssessmentTestSession session;
	private RepositoryEntry testEntry;
	private IQTESTCourseNode courseNode;
	private final Identity assessedIdentity;
	private final boolean canUpdateAssessmentEntry;
	private UserCourseEnvironment assessedUserCourseEnv;
	private final GradingAssignment runningAssignment;
	
	@Autowired
	private DB dbInstance;
	@Autowired
	private QTI21Service qtiService;
	@Autowired
	private UserManager userManager;
	@Autowired
	private GradingService gradingService;
	@Autowired
	private CourseAssessmentService courseAssessmentService;
	
	/**
	 * Invalidate the assessment test session linked to a course.
	 * 
	 * @param ureq The user request
	 * @param wControl The window control
	 * @param session The assessment test session to invalidate
	 * @param lastSession If the specified test session is known to be the last session
	 * @param courseNode The course element
	 * @param assessedUserCourseEnv The user course environnment of the assessed identity
	 */
	public ConfirmAssessmentTestSessionInvalidationController(UserRequest ureq, WindowControl wControl,
			AssessmentTestSession session, boolean lastSession, IQTESTCourseNode courseNode,
			UserCourseEnvironment assessedUserCourseEnv) {
		super(ureq, wControl, "confirm_inval_test_session");
		this.session = session;
		this.courseNode = courseNode;
		this.assessedUserCourseEnv = assessedUserCourseEnv;
		assessedIdentity = assessedUserCourseEnv.getIdentityEnvironment().getIdentity();
		canUpdateAssessmentEntry = lastSession && (getNextLastSession() != null);
		runningAssignment = getRunningGradingAssignment();
		initForm(ureq);
	}
	
	/**
	 * Invalidate the assessment test session of a test entry (without course).
	 * 
	 * @param ureq The user request
	 * @param wControl The window control
	 * @param session The assessment test session to invalidate
	 * @param lastSession If the specified test session is known to be the last session
	 * @param testEntry The test repository entry
	 * @param assessedIdentity The assessed identity
	 */
	public ConfirmAssessmentTestSessionInvalidationController(UserRequest ureq, WindowControl wControl,
			AssessmentTestSession session, boolean lastSession, RepositoryEntry testEntry, Identity assessedIdentity) {
		super(ureq, wControl, "confirm_inval_test_session");
		this.session = session;
		this.testEntry = testEntry;
		this.assessedIdentity = assessedIdentity;
		canUpdateAssessmentEntry = lastSession && (getNextLastSession() != null);
		runningAssignment = null;
		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		if(formLayout instanceof FormLayoutContainer) {
			FormLayoutContainer layoutCont = (FormLayoutContainer)formLayout;
			String fullname = userManager.getUserDisplayName(session.getIdentity());
			String text = translate("invalidate.test.confirm.text", new String[]{ fullname });
			layoutCont.contextPut("msg", text);
			
			if(runningAssignment != null) {
				GradingAssignmentStatus assignmentStatus = runningAssignment.getAssignmentStatus();
				if(assignmentStatus == GradingAssignmentStatus.assigned || assignmentStatus == GradingAssignmentStatus.inProcess) {
					String warningText = translate("warning.assignment.inProcess");
					layoutCont.contextPut("assignmentMsg", warningText);
				} else if(assignmentStatus == GradingAssignmentStatus.done) {
					String warningText = translate("warning.assignment.done");
					layoutCont.contextPut("assignmentMsg", warningText);
				}
			}
		}

		uifactory.addFormCancelButton("cancel", formLayout, ureq, getWindowControl());
		if(canUpdateAssessmentEntry) {
			uifactory.addFormSubmitButton("invalidate.overwrite", formLayout);
			invalidateButton = uifactory.addFormLink("invalidate", formLayout, Link.BUTTON);
		} else {
			uifactory.addFormSubmitButton("invalidate", formLayout);
		}
	}
	
	private GradingAssignment getRunningGradingAssignment() {
		if(courseNode == null) return null;
		
		if(gradingService.isGradingEnabled(session.getTestEntry(), null)) {
			AssessmentEntry assessmentEntry = courseAssessmentService.getAssessmentEntry(courseNode, assessedUserCourseEnv);
			GradingAssignment assignment = gradingService.getGradingAssignment(session.getTestEntry(), assessmentEntry);
			if(assignment != null && session.getKey().equals(assessmentEntry.getAssessmentId())) {
				return assignment;
			}
		}
		return null;
	}

	@Override
	protected void formOK(UserRequest ureq) {
		doInvalidateSession(ureq, canUpdateAssessmentEntry);
	}
	
	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(invalidateButton == source) {
			doInvalidateSession(ureq, false);
		}
		super.formInnerEvent(ureq, source, event);
	}

	@Override
	protected void formCancelled(UserRequest ureq) {
		fireEvent(ureq, Event.CANCELLED_EVENT);
	}

	private void doInvalidateSession(UserRequest ureq, boolean updateEntryResults) {
		session.setCancelled(true);
		session = qtiService.updateAssessmentTestSession(session);
		dbInstance.commit();

		AssessmentTestSession promotedSession = getNextLastSession();
		// choose to update or not: assessment of the assessment entry, push the score to the assessment entry
		if(promotedSession != null) {
			if(courseNode == null) {
				qtiService.updateAssessmentEntry(promotedSession, updateEntryResults);
			} else {
				courseNode.promoteAssessmentTestSession(promotedSession, assessedUserCourseEnv, updateEntryResults, getIdentity(), Role.coach);
			}
		}
		
		if(runningAssignment != null) {
			GradingAssignmentStatus assignmentStatus = runningAssignment.getAssignmentStatus();
			if(assignmentStatus == GradingAssignmentStatus.assigned
					|| assignmentStatus == GradingAssignmentStatus.inProcess
					|| assignmentStatus == GradingAssignmentStatus.done) {
				if(promotedSession != null) {
					gradingService.reopenAssignment(runningAssignment, promotedSession.getFinishTime());
				} else {
					gradingService.deactivateAssignment(runningAssignment);
				}
			}
		}
		
		fireEvent(ureq, Event.CHANGED_EVENT);
	}
	
	private AssessmentTestSession getNextLastSession() {
		List<AssessmentTestSession> sessions;
		if(courseNode == null) {
			sessions = qtiService.getAssessmentTestSessions(testEntry, null, assessedIdentity, true);
		} else {
			RepositoryEntry courseEntry = assessedUserCourseEnv.getCourseEnvironment().getCourseGroupManager().getCourseEntry();
			sessions = qtiService.getAssessmentTestSessions(courseEntry, courseNode.getIdent(), assessedIdentity, true);
		}
		
		sessions.remove(session);
		if(!sessions.isEmpty()) {
			Collections.sort(sessions, new AssessmentTestSessionComparator());
			return sessions.get(0);
		}
		return null;
	}
}
