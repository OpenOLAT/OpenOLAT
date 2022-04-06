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
package org.olat.modules.grade.ui.wizard;

import java.util.List;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.impl.Form;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.wizard.StepFormBasicController;
import org.olat.core.gui.control.generic.wizard.StepsEvent;
import org.olat.core.gui.control.generic.wizard.StepsRunContext;
import org.olat.core.util.StringHelper;
import org.olat.course.assessment.CourseAssessmentService;
import org.olat.course.assessment.handler.AssessmentConfig;
import org.olat.course.assessment.model.AssessmentScoreStatistic;
import org.olat.course.nodes.CourseNode;
import org.olat.course.nodes.MSCourseNode;
import org.olat.modules.grade.GradeScale;
import org.olat.modules.grade.model.GradeScaleWrapper;
import org.olat.modules.grade.ui.GradeScaleEditController;
import org.olat.repository.RepositoryEntry;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 10 Mar 2022<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class GradeScaleAdjustController extends StepFormBasicController {
	
	private GradeScaleEditController gradeScaleEditCtrl;
	
	@Autowired
	private CourseAssessmentService courseAssessmentService;
	
	
	public GradeScaleAdjustController(UserRequest ureq, WindowControl wControl, Form form, StepsRunContext runContext,
			List<AssessmentScoreStatistic> scoreStatistics) {
		super(ureq, wControl, form, runContext, LAYOUT_BAREBONE, null);
		
		RepositoryEntry courseEntry = (RepositoryEntry)runContext.get(GradeScaleAdjustCallback.KEY_COURSE_ENTRY);
		CourseNode courseNode = (CourseNode)runContext.get(GradeScaleAdjustCallback.KEY_COURSE_NODE);
		
		AssessmentConfig assessmentConfig = courseAssessmentService.getAssessmentConfig(courseNode);
		String gradeSystemKey = courseNode.getModuleConfiguration().getStringValue(MSCourseNode.CONFIG_KEY_GRADE_SYSTEM);
		Long defautGradesystemKey = StringHelper.isLong(gradeSystemKey)? Long.valueOf(gradeSystemKey): null;
		gradeScaleEditCtrl = new GradeScaleEditController(ureq, wControl, form, courseEntry, courseNode.getIdent(),
				assessmentConfig.getMinScore(), assessmentConfig.getMaxScore(), defautGradesystemKey, scoreStatistics);
		listenTo(gradeScaleEditCtrl);
		
		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		formLayout.add(gradeScaleEditCtrl.getInitialFormItem());
	}
	
	@Override
	protected void doDispose() {
		mainForm.removeSubFormListener(gradeScaleEditCtrl);
		mainForm.removeSubFormListener(this);
		super.doDispose();
	}

	@Override
	protected void formOK(UserRequest ureq) {
		GradeScale gradeScale = (GradeScale)getOrCreateFromRunContext(GradeScaleAdjustCallback.KEY_GRADE_SCALE, GradeScaleWrapper::new);
		gradeScaleEditCtrl.updateWrappers(gradeScale);
		addToRunContext(GradeScaleAdjustCallback.KEY_BREAKPOINTS, gradeScaleEditCtrl.getBreakpoints());
		
		fireEvent(ureq, StepsEvent.ACTIVATE_NEXT);
	}

}
