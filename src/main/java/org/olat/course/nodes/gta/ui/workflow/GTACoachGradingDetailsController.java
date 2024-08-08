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
package org.olat.course.nodes.gta.ui.workflow;

import static org.olat.course.assessment.ui.tool.AssessmentParticipantViewController.formEvaluation;
import static org.olat.course.assessment.ui.tool.AssessmentParticipantViewController.gradeSystem;

import java.util.List;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.impl.Form;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.ComponentWrapperElement;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.util.Util;
import org.olat.core.util.vfs.VFSLeaf;
import org.olat.course.CourseFactory;
import org.olat.course.ICourse;
import org.olat.course.assessment.AssessmentHelper;
import org.olat.course.assessment.handler.AssessmentConfig;
import org.olat.course.assessment.ui.tool.AssessmentParticipantViewController;
import org.olat.course.assessment.ui.tool.AssessmentParticipantViewController.AssessmentDocumentsSupplier;
import org.olat.course.assessment.ui.tool.AssessmentParticipantViewController.FormEvaluationSupplier;
import org.olat.course.assessment.ui.tool.AssessmentParticipantViewController.GradeSystemSupplier;
import org.olat.course.assessment.ui.tool.IdentityListCourseNodeController;
import org.olat.course.nodes.GTACourseNode;
import org.olat.course.nodes.gta.ui.GTACoachController;
import org.olat.course.nodes.gta.ui.events.AssessTaskEvent;
import org.olat.course.nodes.gta.ui.events.ReopenEvent;
import org.olat.course.run.environment.CourseEnvironment;
import org.olat.course.run.scoring.AssessmentEvaluation;
import org.olat.course.run.userview.UserCourseEnvironment;
import org.olat.modules.assessment.model.AssessmentEntryStatus;

/**
 * 
 * Initial date: 5 juil. 2024<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class GTACoachGradingDetailsController extends FormBasicController implements AssessmentDocumentsSupplier {
	
	private FormLink reopenButton;
	private FormLink assessmentButton;
	
	private final CoachedParticipantRow row;
	
	private final AssessmentParticipantViewController participantViewController;
	
	public GTACoachGradingDetailsController(UserRequest ureq, WindowControl wControl, CoachedParticipantRow row,
			AssessmentConfig assessmentConfig, CourseEnvironment courseEnv, GTACourseNode gtaNode, Form rootForm) {
		super(ureq, wControl, LAYOUT_CUSTOM, "grading_details_view", rootForm);
		setTranslator(Util.createPackageTranslator(GTACoachController.class, getLocale(),
				Util.createPackageTranslator(IdentityListCourseNodeController.class, getLocale(), getTranslator())));
		this.row = row;
		
		ICourse course = CourseFactory.loadCourse(courseEnv.getCourseResourceableId());
		UserCourseEnvironment assessedUserCourseEnv = AssessmentHelper.createAndInitUserCourseEnvironment(row.getAssessedIdentity(), course);
		AssessmentEvaluation assessmentEval = assessedUserCourseEnv.getScoreAccounting().evalCourseNode(gtaNode);
		
		GradeSystemSupplier gradeSystemSupplier = gradeSystem(assessedUserCourseEnv, gtaNode);
		FormEvaluationSupplier formEvaluationSupplier = formEvaluation(assessedUserCourseEnv, gtaNode, assessmentConfig);
		
		participantViewController = new AssessmentParticipantViewController(ureq, getWindowControl(),
				assessmentEval, assessmentConfig, this, gradeSystemSupplier, formEvaluationSupplier, null, false, false);
		listenTo(participantViewController);
		
		initForm(ureq);
	}
	
	public CoachedParticipantRow getRow() {
		return row;
	}

	@Override
	public List<VFSLeaf> getIndividualAssessmentDocuments() {
		return List.of();
	}

	@Override
	public boolean isDownloadEnabled() {
		return false;
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		ComponentWrapperElement participantViewEl = new ComponentWrapperElement(participantViewController.getInitialComponent());
		formLayout.add("participantView", participantViewEl);
		
		if(row.getAssessmentStatus() == AssessmentEntryStatus.done) {
			reopenButton = uifactory.addFormLink("tool.reopen", formLayout, Link.BUTTON);
			reopenButton.setIconLeftCSS("o_icon o_icon-fw o_icon_extra_time");
		} else {
			assessmentButton = uifactory.addFormLink("tool.assessment", formLayout, Link.BUTTON);
		}
	}
	
	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(reopenButton == source) {
			fireEvent(ureq, new ReopenEvent());
		} else if(assessmentButton == source) {
			fireEvent(ureq, new AssessTaskEvent());
		}
	}

	@Override
	protected void formOK(UserRequest ureq) {
		//
	}
}
