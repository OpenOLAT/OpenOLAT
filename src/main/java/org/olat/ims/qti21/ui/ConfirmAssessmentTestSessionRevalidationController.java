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
 * 
 * Initial date: 27 juil. 2020<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class ConfirmAssessmentTestSessionRevalidationController extends FormBasicController {
	
	private FormLink revalidateButton;
	
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
	
	public ConfirmAssessmentTestSessionRevalidationController(UserRequest ureq, WindowControl wControl,
			AssessmentTestSession session, IQTESTCourseNode courseNode, UserCourseEnvironment assessedUserCourseEnv) {
		super(ureq, wControl, "confirm_reval_test_session");
		this.session = session;
		this.courseNode = courseNode;
		this.assessedUserCourseEnv = assessedUserCourseEnv;
		assessedIdentity = assessedUserCourseEnv.getIdentityEnvironment().getIdentity();
		canUpdateAssessmentEntry = isNextLastSession(session);
		runningAssignment = getRunningGradingAssignment();
		initForm(ureq);
	}
	
	public ConfirmAssessmentTestSessionRevalidationController(UserRequest ureq, WindowControl wControl,
			AssessmentTestSession session, RepositoryEntry testEntry, Identity assessedIdentity) {
		super(ureq, wControl, "confirm_reval_test_session");
		this.session = session;
		this.testEntry = testEntry;
		this.assessedIdentity = assessedIdentity;
		canUpdateAssessmentEntry = isNextLastSession(session);
		runningAssignment = null;
		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		if(formLayout instanceof FormLayoutContainer) {
			FormLayoutContainer layoutCont = (FormLayoutContainer)formLayout;
			String fullname = userManager.getUserDisplayName(session.getIdentity());
			String text = translate("revalidate.test.confirm.text", new String[]{ fullname });
			layoutCont.contextPut("msg", text);
			
			if(runningAssignment != null && canUpdateAssessmentEntry) {
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
			uifactory.addFormSubmitButton("revalidate.overwrite", formLayout);
			revalidateButton = uifactory.addFormLink("revalidate", formLayout, Link.BUTTON);
		} else {
			uifactory.addFormSubmitButton("revalidate", formLayout);
		}
	}

	@Override
	protected void formOK(UserRequest ureq) {
		doValidateSession(ureq, canUpdateAssessmentEntry);
	}
	
	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(revalidateButton == source) {
			doValidateSession(ureq, false);
		}
		super.formInnerEvent(ureq, source, event);
	}

	@Override
	protected void formCancelled(UserRequest ureq) {
		fireEvent(ureq, Event.CANCELLED_EVENT);
	}
	
	private GradingAssignment getRunningGradingAssignment() {
		if(courseNode == null) return null;
		
		if(gradingService.isGradingEnabled(session.getTestEntry(), null)) {
			AssessmentEntry assessmentEntry = courseAssessmentService.getAssessmentEntry(courseNode, assessedUserCourseEnv);
			GradingAssignment assignment = gradingService.getGradingAssignment(session.getTestEntry(), assessmentEntry);
			if(assignment != null) {
				return assignment;
			}
		}
		return null;
	}
	
	private void doValidateSession(UserRequest ureq, boolean updateEntryResults) {
		session.setCancelled(false);
		session = qtiService.updateAssessmentTestSession(session);
		dbInstance.commit();

		if(canUpdateAssessmentEntry) {
			if(courseNode == null) {
				qtiService.updateAssessmentEntry(session, updateEntryResults);
			} else {
				courseNode.promoteAssessmentTestSession(session, assessedUserCourseEnv, updateEntryResults, getIdentity(), Role.coach);
			}
			
			if(runningAssignment != null) {
				GradingAssignmentStatus assignmentStatus = runningAssignment.getAssignmentStatus();
				if(assignmentStatus == GradingAssignmentStatus.assigned
						|| assignmentStatus == GradingAssignmentStatus.inProcess
						|| assignmentStatus == GradingAssignmentStatus.done) {
					gradingService.reopenAssignment(runningAssignment, session.getFinishTime());
				} else if(assignmentStatus == GradingAssignmentStatus.deactivated
						|| assignmentStatus == GradingAssignmentStatus.unassigned) {
					dbInstance.commit();// if the assessment was updated before
					AssessmentEntry assessmentEntry = gradingService
							.loadFullAssessmentEntry(runningAssignment.getAssessmentEntry());
					RepositoryEntry referenceEntry = session.getTestEntry();
					gradingService.assignGrader(referenceEntry, assessmentEntry, session.getFinishTime(), true);
				}
			}
		}

		fireEvent(ureq, Event.CHANGED_EVENT);
	}
	
	private boolean isNextLastSession(AssessmentTestSession testSession) {
		List<AssessmentTestSession> sessions;
		if(courseNode == null) {
			sessions = qtiService.getAssessmentTestSessions(testEntry, null, assessedIdentity, true);
		} else {
			RepositoryEntry courseEntry = assessedUserCourseEnv.getCourseEnvironment().getCourseGroupManager().getCourseEntry();
			sessions = qtiService.getAssessmentTestSessions(courseEntry, courseNode.getIdent(), assessedIdentity, true);
		}
		if(sessions.isEmpty()) {
			return true;
		}
		
		sessions.add(testSession);
		Collections.sort(sessions, new AssessmentTestSessionComparator(false));
		return testSession.equals(sessions.get(0));
	}
}
