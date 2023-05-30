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
package org.olat.course.nodes.st.assessment;

import java.util.Locale;
import java.util.NavigableSet;

import org.olat.core.CoreSpringFactory;
import org.olat.core.id.Identity;
import org.olat.core.util.StringHelper;
import org.olat.core.util.i18n.I18nManager;
import org.olat.course.nodes.CourseNode;
import org.olat.course.run.scoring.AssessmentEvaluation;
import org.olat.course.run.scoring.RootPassedEvaluator;
import org.olat.course.run.scoring.ScoreAccounting;
import org.olat.modules.grade.GradeModule;
import org.olat.modules.grade.GradeScale;
import org.olat.modules.grade.GradeScoreRange;
import org.olat.modules.grade.GradeService;
import org.olat.repository.RepositoryEntry;

/**
 * 
 * Initial date: 30 May 2023<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class STRootGradeEvaluator implements RootPassedEvaluator {
	
	private GradeModule gradeModule;
	private GradeService gradeService;
	private I18nManager i18nManager;

	@Override
	public GradePassed getPassed(AssessmentEvaluation currentEvaluation, CourseNode courseNode,
			ScoreAccounting scoreAccounting, RepositoryEntry courseEntry, Identity assessedIdentity) {
		boolean gradeApplied = StringHelper.containsNonWhitespace(currentEvaluation.getGrade());
		if (getGradeModule().isEnabled() && gradeApplied && currentEvaluation.getScore() != null) {
			GradeScoreRange gradeScoreRange = getGradeScoreRange(currentEvaluation, courseNode, courseEntry,
					assessedIdentity);
			return GradePassed.of(
					gradeScoreRange.getGrade(),
					gradeScoreRange.getGradeSystemIdent(),
					gradeScoreRange.getPerformanceClassIdent(),
					gradeScoreRange.getPassed());
		}
		return GradePassed.none();
	}

	GradeScoreRange getGradeScoreRange(AssessmentEvaluation currentEvaluation, CourseNode courseNode,
			RepositoryEntry courseEntry, Identity assessedIdentity) {
		Locale locale = getI18nManager().getLocaleOrDefault(assessedIdentity.getUser().getPreferences().getLanguage());
		GradeScale gradeScale = getGradeService().getGradeScale(courseEntry, courseNode.getIdent());
		NavigableSet<GradeScoreRange> gradeScoreRanges = getGradeService().getGradeScoreRanges(gradeScale, locale);
		GradeScoreRange gradeScoreRange = getGradeService().getGradeScoreRange(gradeScoreRanges, currentEvaluation.getScore());
		return gradeScoreRange;
	}
	
	private GradeModule getGradeModule() {
		if (gradeModule == null) {
			gradeModule = CoreSpringFactory.getImpl(GradeModule.class);
		}
		return gradeModule;
	}
	
	// For testing only
	void setGradeModule(GradeModule gradeModule) {
		this.gradeModule = gradeModule;
	}

	private GradeService getGradeService() {
		if (gradeService == null) {
			gradeService = CoreSpringFactory.getImpl(GradeService.class);
		}
		return gradeService;
	}
	
	private I18nManager getI18nManager() {
		if (i18nManager == null) {
			i18nManager = CoreSpringFactory.getImpl(I18nManager.class);
		}
		return i18nManager;
	}

}
