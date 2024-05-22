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
package org.olat.course.nodes.ms;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.Locale;
import java.util.NavigableSet;
import java.util.zip.ZipOutputStream;

import org.apache.logging.log4j.Logger;
import org.olat.core.CoreSpringFactory;
import org.olat.core.gui.translator.Translator;
import org.olat.core.id.Identity;
import org.olat.core.logging.Tracing;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
import org.olat.core.util.i18n.I18nManager;
import org.olat.course.auditing.UserNodeAuditManager;
import org.olat.course.nodes.CourseNode;
import org.olat.course.nodes.MSCourseNode;
import org.olat.course.run.scoring.ScoreEvaluation;
import org.olat.course.run.scoring.ScoreScalingHelper;
import org.olat.course.run.userview.UserCourseEnvironment;
import org.olat.modules.ModuleConfiguration;
import org.olat.modules.assessment.Role;
import org.olat.modules.forms.EvaluationFormManager;
import org.olat.modules.forms.EvaluationFormProvider;
import org.olat.modules.forms.EvaluationFormSession;
import org.olat.modules.forms.SessionFilter;
import org.olat.modules.forms.SessionFilterFactory;
import org.olat.modules.forms.model.xml.Form;
import org.olat.modules.forms.ui.EvaluationFormExcelExport;
import org.olat.modules.forms.ui.EvaluationFormReportsController;
import org.olat.modules.forms.ui.LegendNameGenerator;
import org.olat.modules.forms.ui.ReportHelper;
import org.olat.modules.forms.ui.ReportHelperUserColumns;
import org.olat.modules.forms.ui.SessionInformationLegendNameGenerator;
import org.olat.modules.grade.GradeModule;
import org.olat.modules.grade.GradeScale;
import org.olat.modules.grade.GradeScoreRange;
import org.olat.modules.grade.GradeService;
import org.olat.repository.RepositoryEntry;

/**
 * 
 * Initial date: 22 mai 2024<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class MSScoreEvaluationAndDataHelper {
	
	private static final Logger log = Tracing.createLoggerFor(MSScoreEvaluationAndDataHelper.class);
	
	public static ScoreEvaluation getUpdateScoreEvaluation(UserCourseEnvironment assessedUserCourseEnv, CourseNode courseNode, Locale locale, Float score) {
		GradeScoreRange gradeScoreRange = null;
		String grade = null;
		String gradeSystemIdent = null;
		String performanceClassIdent = null;
		Boolean passed = null;
		ModuleConfiguration config = courseNode.getModuleConfiguration();
		
		if (config.getBooleanSafe(MSCourseNode.CONFIG_KEY_HAS_PASSED_FIELD)) {
			if (config.getBooleanSafe(MSCourseNode.CONFIG_KEY_GRADE_ENABLED)) {
				if (CoreSpringFactory.getImpl(GradeModule.class).isEnabled() && score != null) {
					boolean applyGrade = config.getBooleanSafe(MSCourseNode.CONFIG_KEY_GRADE_AUTO);
					if (!applyGrade) {
						ScoreEvaluation currentEval = assessedUserCourseEnv.getScoreAccounting().evalCourseNode(courseNode);
						applyGrade = StringHelper.containsNonWhitespace(currentEval.getGrade());
					}
					if (applyGrade) {
						GradeService gradeService = CoreSpringFactory.getImpl(GradeService.class);
						GradeScale gradeScale = gradeService.getGradeScale(assessedUserCourseEnv.getCourseEnvironment().getCourseGroupManager().getCourseEntry(), courseNode.getIdent());
						NavigableSet<GradeScoreRange> gradeScoreRanges = gradeService.getGradeScoreRanges(gradeScale, locale);
						gradeScoreRange = gradeService.getGradeScoreRange(gradeScoreRanges, score);
						grade = gradeScoreRange.getGrade();
						gradeSystemIdent = gradeScoreRange.getGradeSystemIdent();
						performanceClassIdent = gradeScoreRange.getPerformanceClassIdent();
						passed = gradeScoreRange.getPassed();
					}
				}
			} else if (config.has(MSCourseNode.CONFIG_KEY_PASSED_CUT_VALUE)) {
				Float cutConfig = (Float) config.get(MSCourseNode.CONFIG_KEY_PASSED_CUT_VALUE);
				if (cutConfig != null && score != null) {
					boolean aboveCutValue = score.floatValue() >= cutConfig.floatValue();
					passed = Boolean.valueOf(aboveCutValue);
				}
			} else {
				ScoreEvaluation currentEval = assessedUserCourseEnv.getScoreAccounting().evalCourseNode(courseNode);
				grade = currentEval.getGrade();
				gradeSystemIdent = currentEval.getGradeSystemIdent();
				performanceClassIdent = currentEval.getPerformanceClassIdent();
				passed = currentEval.getPassed();
			}
			
		}
		BigDecimal scoreScale = ScoreScalingHelper.getScoreScale(courseNode);
		Float weightedScore = ScoreScalingHelper.getWeightedFloatScore(score, scoreScale);
		return new ScoreEvaluation(score, weightedScore, scoreScale, grade, gradeSystemIdent, performanceClassIdent, passed, null, null, null, null, null, null);
	}
	
	public static void archiveForResetUserData(UserCourseEnvironment assessedUserCourseEnv, ZipOutputStream archiveStream,
			String path, CourseNode courseNode, EvaluationFormProvider evaluationFormProvider) {
		
		RepositoryEntry courseEntry = assessedUserCourseEnv.getCourseEnvironment().getCourseGroupManager().getCourseEntry();
		
		try {
			MSService msService = CoreSpringFactory.getImpl(MSService.class);
			I18nManager i18nManager = CoreSpringFactory.getImpl(I18nManager.class);
			EvaluationFormManager evaluationFormManager = CoreSpringFactory.getImpl(EvaluationFormManager.class);
			Identity assessedIdentity = assessedUserCourseEnv.getIdentityEnvironment().getIdentity();
			Locale locale = i18nManager.getLocaleOrDefault(assessedIdentity.getUser().getPreferences().getLanguage());
			RepositoryEntry formEntry = MSCourseNode.getEvaluationForm(courseNode.getModuleConfiguration());

			if(formEntry != null) {
				EvaluationFormSession session =  msService.getSession(courseEntry, courseNode.getIdent(), evaluationFormProvider, assessedIdentity, null);
				if(session != null) {
					SessionFilter filter = SessionFilterFactory.create(session);
					Form form = evaluationFormManager.loadForm(formEntry);
					
					Translator translator = Util.createPackageTranslator(EvaluationFormReportsController.class, locale);
					LegendNameGenerator legendNameGenerator = new SessionInformationLegendNameGenerator(filter);
					ReportHelper reportHelper = ReportHelper.builder(locale).withLegendNameGenrator(legendNameGenerator).build();
					ReportHelperUserColumns userColumns = new ReportHelperUserColumns(reportHelper, translator);
					
					EvaluationFormExcelExport evaluationFormExport = new EvaluationFormExcelExport(form, filter,
							reportHelper.getComparator(), userColumns, courseNode.getShortName());
					evaluationFormExport.export(archiveStream, path);
				}
			}
		} catch (IOException e) {
			log.error("", e);
		}
	}
	
	public static void resetUserData(UserCourseEnvironment assessedUserCourseEnv, CourseNode courseNode, EvaluationFormProvider evaluationFormProvider,
			Identity identity, Role by) {
		RepositoryEntry formEntry = MSCourseNode.getEvaluationForm(courseNode.getModuleConfiguration());
		if(formEntry != null) {
			MSService msService = CoreSpringFactory.getImpl(MSService.class);
			RepositoryEntry courseEntry = assessedUserCourseEnv.getCourseEnvironment().getCourseGroupManager().getCourseEntry();
			Identity assessedIdentity = assessedUserCourseEnv.getIdentityEnvironment().getIdentity();
			
			EvaluationFormSession session =  msService.getSession(courseEntry, courseNode.getIdent(), evaluationFormProvider, assessedIdentity, null);
			if(session != null) {
				UserNodeAuditManager auditManager = assessedUserCourseEnv.getCourseEnvironment().getAuditManager();
				AuditEnv auditEnv = AuditEnv.of(auditManager, courseNode, assessedIdentity, identity, by);
				msService.deleteSession(courseEntry, courseNode.getIdent(), evaluationFormProvider, assessedIdentity, auditEnv);
			}
		}
	}
}
