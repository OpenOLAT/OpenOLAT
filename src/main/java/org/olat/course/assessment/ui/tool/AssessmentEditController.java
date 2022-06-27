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
package org.olat.course.assessment.ui.tool;

import static org.olat.course.assessment.ui.tool.AssessmentParticipantViewController.gradeSystem;

import java.io.File;
import java.util.List;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.util.Util;
import org.olat.course.CourseEntryRef;
import org.olat.course.assessment.AssessmentModule;
import org.olat.course.assessment.CourseAssessmentService;
import org.olat.course.assessment.handler.AssessmentConfig;
import org.olat.course.assessment.ui.tool.AssessmentParticipantViewController.AssessmentDocumentsSupplier;
import org.olat.course.assessment.ui.tool.AssessmentParticipantViewController.PanelInfo;
import org.olat.course.nodes.CourseNode;
import org.olat.course.run.scoring.AssessmentEvaluation;
import org.olat.course.run.userview.UserCourseEnvironment;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 26 Nov 2021<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class AssessmentEditController extends BasicController implements AssessmentDocumentsSupplier {
	
	public static final PanelInfo PANEL_INFO = new PanelInfo(AssessmentEditController.class, "::assessment-tool");
	
	private final AssessmentParticipantViewController assessmentParticipantViewCtrl;
	private final AssessmentForm assessmentForm;

	private final CourseNode courseNode;
	private final UserCourseEnvironment assessedUserCourseEnv;
	
	@Autowired
	private CourseAssessmentService courseAssessmentService;

	public AssessmentEditController(UserRequest ureq, WindowControl wControl, CourseNode courseNode,
			UserCourseEnvironment coachCourseEnv, UserCourseEnvironment assessedUserCourseEnv) {
		super(ureq, wControl);
		this.courseNode = courseNode;
		this.assessedUserCourseEnv = assessedUserCourseEnv;
		setTranslator(Util.createPackageTranslator(AssessmentModule.class, getLocale(), getTranslator()));
		VelocityContainer mainVC = createVelocityContainer("assessment_edit");
		
		assessmentForm = new AssessmentForm(ureq, wControl, courseNode, coachCourseEnv, assessedUserCourseEnv);
		listenTo(assessmentForm);
		mainVC.put("form", assessmentForm.getInitialComponent());
		
		AssessmentConfig assessmentConfig = courseAssessmentService.getAssessmentConfig(new CourseEntryRef(coachCourseEnv), courseNode);
		AssessmentEvaluation assessmentEval = assessedUserCourseEnv.getScoreAccounting().evalCourseNode(courseNode);
		assessmentParticipantViewCtrl = new AssessmentParticipantViewController(ureq, getWindowControl(),
				assessmentEval, assessmentConfig, this, gradeSystem(coachCourseEnv, courseNode), PANEL_INFO);
		listenTo(assessmentParticipantViewCtrl);
		mainVC.put("participantView", assessmentParticipantViewCtrl.getInitialComponent());
		
		putInitialPanel(mainVC);
	}

	@Override
	public List<File> getIndividualAssessmentDocuments() {
		return courseAssessmentService.getIndividualAssessmentDocuments(courseNode, assessedUserCourseEnv);
	}

	@Override
	public boolean isDownloadEnabled() {
		return false;
	}
	
	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if (source == assessmentForm) {
			fireEvent(ureq, event);
		}
		super.event(ureq, source, event);
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		//
	}

}
