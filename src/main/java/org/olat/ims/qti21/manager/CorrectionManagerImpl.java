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
package org.olat.ims.qti21.manager;

import java.math.BigDecimal;
import java.util.Locale;
import java.util.NavigableSet;

import org.olat.core.id.Identity;
import org.olat.core.util.StringHelper;
import org.olat.course.assessment.AssessmentHelper;
import org.olat.course.assessment.CourseAssessmentService;
import org.olat.course.assessment.handler.AssessmentConfig;
import org.olat.course.nodes.IQTESTCourseNode;
import org.olat.course.run.environment.CourseEnvironment;
import org.olat.course.run.scoring.ScoreEvaluation;
import org.olat.course.run.userview.UserCourseEnvironment;
import org.olat.ims.qti21.AssessmentTestSession;
import org.olat.ims.qti21.CorrectionManager;
import org.olat.ims.qti21.model.xml.QtiNodesExtractor;
import org.olat.modules.assessment.Role;
import org.olat.modules.grade.GradeModule;
import org.olat.modules.grade.GradeScale;
import org.olat.modules.grade.GradeScoreRange;
import org.olat.modules.grade.GradeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import uk.ac.ed.ph.jqtiplus.node.test.AssessmentTest;

/**
 * 
 * Initial date: 10 févr. 2021<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
@Service
public class CorrectionManagerImpl implements CorrectionManager {

	@Autowired
	private CourseAssessmentService courseAssessmentService;
	@Autowired
	private GradeModule gradeModule;
	@Autowired
	private GradeService gradeService;
	
	@Override
	public void updateCourseNode(AssessmentTestSession testSession, AssessmentTest assessmentTest,
			IQTESTCourseNode courseNode, CourseEnvironment courseEnv, Identity doer, Locale locale) {
		if(testSession == null) return;
		
		Double cutValue = QtiNodesExtractor.extractCutValue(assessmentTest);

		UserCourseEnvironment assessedUserCourseEnv = AssessmentHelper
				.createAndInitUserCourseEnvironment(testSession.getIdentity(), courseEnv);
		ScoreEvaluation scoreEval = courseAssessmentService.getAssessmentEvaluation(courseNode, assessedUserCourseEnv);
		AssessmentConfig assessmentConfig = courseAssessmentService.getAssessmentConfig(courseNode);
		
		BigDecimal finalScore = testSession.getFinalScore();
		Float score = finalScore == null ? null : finalScore.floatValue();
		String grade = scoreEval.getGrade();
		String performanceClassIdent = scoreEval.getPerformanceClassIdent();
		Boolean passed = scoreEval.getPassed();
		if(testSession.getManualScore() != null && finalScore != null) {
			if (assessmentConfig.hasGrade() && gradeModule.isEnabled()) {
				if (assessmentConfig.isAutoGrade() || StringHelper.containsNonWhitespace(scoreEval.getGrade())) {
					GradeScale gradeScale = gradeService.getGradeScale(
							courseEnv.getCourseGroupManager().getCourseEntry(), courseNode.getIdent());
					NavigableSet<GradeScoreRange> gradeScoreRanges = null;gradeScoreRanges = gradeService.getGradeScoreRanges(gradeScale, locale);
					GradeScoreRange gradeScoreRange = gradeService.getGradeScoreRange(gradeScoreRanges, score);
					grade = gradeScoreRange.getGrade();
					performanceClassIdent = gradeScoreRange.getPerformanceClassIdent();
					passed = Boolean.valueOf(gradeScoreRange.isPassed());
				}
			} else if (cutValue != null) {
				boolean calculated = finalScore.compareTo(BigDecimal.valueOf(cutValue.doubleValue())) >= 0;
				passed = Boolean.valueOf(calculated);
			}
		}
		
		ScoreEvaluation manualScoreEval = new ScoreEvaluation(score, grade, performanceClassIdent, passed,
				scoreEval.getAssessmentStatus(), scoreEval.getUserVisible(),
				scoreEval.getCurrentRunStartDate(), scoreEval.getCurrentRunCompletion(),
				scoreEval.getCurrentRunStatus(), testSession.getKey());
		courseAssessmentService.updateScoreEvaluation(courseNode, manualScoreEval, assessedUserCourseEnv,
				doer, false, Role.coach);
	}

}
