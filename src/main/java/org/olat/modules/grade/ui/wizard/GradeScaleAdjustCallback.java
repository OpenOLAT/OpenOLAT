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
import java.util.Locale;
import java.util.NavigableSet;

import org.olat.core.CoreSpringFactory;
import org.olat.core.commons.persistence.DB;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.wizard.Step;
import org.olat.core.gui.control.generic.wizard.StepRunnerCallback;
import org.olat.core.gui.control.generic.wizard.StepsMainRunController;
import org.olat.core.gui.control.generic.wizard.StepsRunContext;
import org.olat.core.util.StringHelper;
import org.olat.course.assessment.AssessmentHelper;
import org.olat.course.assessment.CourseAssessmentService;
import org.olat.course.assessment.handler.AssessmentConfig;
import org.olat.course.assessment.handler.AssessmentConfig.Mode;
import org.olat.course.nodes.CourseNode;
import org.olat.course.run.scoring.AssessmentEvaluation;
import org.olat.course.run.scoring.ScoreEvaluation;
import org.olat.course.run.userview.UserCourseEnvironment;
import org.olat.modules.assessment.AssessmentEntry;
import org.olat.modules.assessment.AssessmentService;
import org.olat.modules.assessment.Role;
import org.olat.modules.grade.Breakpoint;
import org.olat.modules.grade.GradeScale;
import org.olat.modules.grade.GradeScoreRange;
import org.olat.modules.grade.GradeService;
import org.olat.repository.RepositoryEntry;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 10 Mar 2022<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class GradeScaleAdjustCallback implements StepRunnerCallback {
	
	public static final String KEY_COURSE_ENTRY = "courseEntry";
	public static final String KEY_COURSE_NODE = "courseNode";
	public static final String KEY_GRADE_SCALE = "gradeScale";
	public static final String KEY_BREAKPOINTS = "breakpoints";
	public static final String KEY_APPLY_GRADE = "applyGrade";
	
	private final UserCourseEnvironment coachUserCourseEnv;
	private final Locale locale;

	@Autowired
	private DB dbInstance;
	@Autowired
	private GradeService gradeService;
	@Autowired
	private AssessmentService assessmentService;
	@Autowired
	private CourseAssessmentService courseAssessmentService;
	
	public GradeScaleAdjustCallback(UserCourseEnvironment coachUserCourseEnv, Locale locale) {
		this.coachUserCourseEnv = coachUserCourseEnv;
		this.locale = locale;
		CoreSpringFactory.autowireObject(this);
	}

	@Override
	public Step execute(UserRequest ureq, WindowControl wControl, StepsRunContext runContext) {
		RepositoryEntry courseEntry = (RepositoryEntry)runContext.get(GradeScaleAdjustCallback.KEY_COURSE_ENTRY);
		CourseNode courseNode = (CourseNode)runContext.get(GradeScaleAdjustCallback.KEY_COURSE_NODE);
		GradeScale gradeScaleWrapper = (GradeScale)runContext.get(GradeScaleAdjustCallback.KEY_GRADE_SCALE);
		@SuppressWarnings("unchecked")
		List<Breakpoint> breakpointWrappers = (List<Breakpoint>)runContext.get(GradeScaleAdjustCallback.KEY_BREAKPOINTS);
		@SuppressWarnings("unchecked")
		List<Long> applyGradeToIdentities = (List<Long>)runContext.get(GradeScaleAdjustCallback.KEY_APPLY_GRADE);
		
		GradeScale gradeScale = gradeService.updateOrCreateGradeScale(courseEntry, courseNode.getIdent(), gradeScaleWrapper);
		gradeService.updateOrCreateBreakpoints(gradeScale, breakpointWrappers);
		updateGrades(courseEntry, courseNode, gradeScale, applyGradeToIdentities);
		
		return StepsMainRunController.DONE_MODIFIED;
	}
	
	private void updateGrades(RepositoryEntry courseEntry, CourseNode courseNode, GradeScale gradeScale, List<Long> applyGradeToIdentities) {
		AssessmentConfig assessmentConfig = courseAssessmentService.getAssessmentConfig(courseNode);
		boolean autoGrade = assessmentConfig.isAutoGrade();
		
		NavigableSet<GradeScoreRange> gradeScoreRanges = gradeService.getGradeScoreRanges(gradeScale, locale);
		
		List<AssessmentEntry> assessmentEntries = assessmentService.loadAssessmentEntriesBySubIdentWithStatus(courseEntry, courseNode.getIdent(), null, true);
		
		int count = 0;
		for (AssessmentEntry assessmentEntry : assessmentEntries) {
			if (autoGrade || StringHelper.containsNonWhitespace(assessmentEntry.getGrade()) || applyGradeToIdentities.contains(assessmentEntry.getIdentity().getKey())) {
				updateGrade(courseNode, assessmentEntry, gradeScoreRanges, assessmentConfig);
				if(++count % 100 == 0) {
					dbInstance.commitAndCloseSession();
				}
			}
			
		}
		dbInstance.commitAndCloseSession();
	}
	
	private void updateGrade(CourseNode courseNode, AssessmentEntry assessmentEntry,
			NavigableSet<GradeScoreRange> gradeScoreRanges, AssessmentConfig assessmentConfig) {
		AssessmentEvaluation scoreEval = AssessmentEvaluation.toAssessmentEvaluation(assessmentEntry, assessmentConfig);
		boolean hasPassed = Mode.none != assessmentConfig.getPassedMode();
		if (scoreEval != null && scoreEval.getScore() != null) {
			Boolean passed = null;
			GradeScoreRange gradeScoreRange = gradeService.getGradeScoreRange(gradeScoreRanges, scoreEval.getScore());
			String grade = gradeScoreRange.getGrade();
			String performanceClassIdent = gradeScoreRange.getPerformanceClassIdent();
			passed = hasPassed ? Boolean.valueOf(gradeScoreRange.isPassed()) : null;
			
			ScoreEvaluation doneEval = new ScoreEvaluation(scoreEval.getScore(), grade,
					performanceClassIdent, passed, scoreEval.getAssessmentStatus(), scoreEval.getUserVisible(),
					scoreEval.getCurrentRunStartDate(), scoreEval.getCurrentRunCompletion(),
					scoreEval.getCurrentRunStatus(), scoreEval.getAssessmentID());
			UserCourseEnvironment assessedUserCourseEnv = AssessmentHelper
					.createAndInitUserCourseEnvironment(assessmentEntry.getIdentity(), coachUserCourseEnv.getCourseEnvironment());
			courseAssessmentService.updateScoreEvaluation(courseNode, doneEval, assessedUserCourseEnv,
					coachUserCourseEnv.getIdentityEnvironment().getIdentity(), false, Role.coach);
		}
	}

}
