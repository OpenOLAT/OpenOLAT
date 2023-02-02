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

import org.olat.core.util.StringHelper;
import org.olat.course.assessment.handler.AssessmentConfig;
import org.olat.course.nodes.CourseNode;
import org.olat.course.nodes.STCourseNode;
import org.olat.course.nodes.st.assessment.MaxScoreCumulator.MaxScore;
import org.olat.course.run.scoring.ScoreCalculator;
import org.olat.modules.ModuleConfiguration;
import org.olat.repository.RepositoryEntryRef;

/**
 * 
 * Initial date: 19 Aug 2019<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class STAssessmentConfig implements AssessmentConfig {
	
	private static final MaxScoreCumulator MAX_SCORE_CUMULATOR = new MaxScoreCumulator();
	
	private final RepositoryEntryRef courseEntry;
	private final CourseNode courseNode;
	private final boolean isRoot;
	private final ModuleConfiguration rootConfig;
	private final ScoreCalculator scoreCalculator;

	public STAssessmentConfig(RepositoryEntryRef courseEntry, STCourseNode courseNode, boolean isRoot, ModuleConfiguration rootConfig) {
		this.courseEntry = courseEntry;
		this.courseNode = courseNode;
		this.isRoot = isRoot;
		this.rootConfig = rootConfig;
		this.scoreCalculator = rootConfig.getBooleanSafe(STCourseNode.CONFIG_SCORE_CALCULATOR_SUPPORTED, true)
				? courseNode.getScoreCalculator()
				: null;
	}

	@Override
	public boolean isAssessable() {
		return true;
	}

	@Override
	public boolean ignoreInCourseAssessment() {
		return false;
	}

	@Override
	public void setIgnoreInCourseAssessment(boolean ignoreInCourseAssessment) {
		//
	}

	@Override
	public Mode getScoreMode() {
		if (scoreCalculator != null && StringHelper.containsNonWhitespace(scoreCalculator.getScoreExpression())) {
			return Mode.evaluated;
		} else if (rootConfig.has(STCourseNode.CONFIG_SCORE_KEY)) {
			return Mode.evaluated;
		}
		return Mode.none;
	}

	@Override
	public Float getMaxScore() {
		if (scoreCalculator == null && rootConfig.has(STCourseNode.CONFIG_SCORE_KEY)) {
			MaxScore maxScore = MAX_SCORE_CUMULATOR.getMaxScore(courseEntry, courseNode);
			String scoreKey = rootConfig.getStringValue(STCourseNode.CONFIG_SCORE_KEY);
			if (STCourseNode.CONFIG_SCORE_VALUE_SUM.equals(scoreKey)) {
				return maxScore.getSum();
			} else if (STCourseNode.CONFIG_SCORE_VALUE_AVG.equals(scoreKey)) {
				// max (not average) because the user was maybe only in one node assessed
				return maxScore.getMax();
			}
		}
		return null;
	}

	@Override
	public Float getMinScore() {
		return null;
	}
	
	@Override
	public boolean hasGrade() {
		return false;
	}
	
	@Override
	public boolean isAutoGrade() {
		return false;
	}

	@Override
	public Mode getPassedMode() {
		if (scoreCalculator != null && StringHelper.containsNonWhitespace(scoreCalculator.getPassedExpression())) {
			return Mode.evaluated;
		} else if (isEvaluatedRoot()) {
			return Mode.evaluated;
		} else if (isRoot && rootConfig.getBooleanSafe(STCourseNode.CONFIG_PASSED_MANUALLY)) {
			return Mode.setByNode;
		}
		return Mode.none;
	}

	@Override
	public boolean isPassedOverridable() {
		return scoreCalculator == null && isEvaluatedRoot();
	}

	private boolean isEvaluatedRoot() {
		return isRoot && (
				   rootConfig.has(STCourseNode.CONFIG_PASSED_PROGRESS)
				|| rootConfig.has(STCourseNode.CONFIG_PASSED_ALL)
				|| rootConfig.has(STCourseNode.CONFIG_PASSED_NUMBER)
				|| rootConfig.has(STCourseNode.CONFIG_PASSED_POINTS)
				);
	}
	
	@Override
	public Float getCutValue() {
		if (scoreCalculator != null && ScoreCalculator.PASSED_TYPE_CUTVALUE.equals(scoreCalculator.getPassedType())) {
			return Float.valueOf(scoreCalculator.getPassedCutValue());
		}
		return null;
	}
	
	@Override
	public Boolean getInitialUserVisibility(boolean done, boolean coachCanNotEdit) {
		return Boolean.TRUE;
	}

	@Override
	public Mode getCompletionMode() {
		return Mode.evaluated;
	}

	@Override
	public boolean hasAttempts() {
		return false;
	}

	@Override
	public boolean hasMaxAttempts() {
		return false;
	}

	@Override
	public Integer getMaxAttempts() {
		return null;
	}

	@Override
	public boolean hasComment() {
		return false;
	}

	@Override
	public boolean hasIndividualAsssessmentDocuments() {
		return false;
	}

	@Override
	public boolean hasStatus() {
		return false;
	}

	@Override
	public boolean isAssessedBusinessGroups() {
		return false;
	}

	@Override
	public boolean isEditable() {
		// ST nodes never editable, data generated on the fly
		return false;
	}

	@Override
	public boolean isBulkEditable() {
		return false;
	}

	@Override
	public boolean hasEditableDetails() {
		return false;
	}
	
	@Override
	public boolean isExternalGrading() {
		return false;
	}

	@Override
	public boolean isObligationOverridable() {
		return !isRoot;
	}
	
}
