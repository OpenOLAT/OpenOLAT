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
package org.olat.course.learningpath.model;

import static org.olat.course.learningpath.ui.LearningPathNodeConfigController.CONFIG_DEFAULT_OBLIGATION;
import static org.olat.course.learningpath.ui.LearningPathNodeConfigController.CONFIG_DEFAULT_TRIGGER;
import static org.olat.course.learningpath.ui.LearningPathNodeConfigController.CONFIG_KEY_DURATION;
import static org.olat.course.learningpath.ui.LearningPathNodeConfigController.CONFIG_KEY_OBLIGATION;
import static org.olat.course.learningpath.ui.LearningPathNodeConfigController.CONFIG_KEY_SCORE_CUT_VALUE;
import static org.olat.course.learningpath.ui.LearningPathNodeConfigController.CONFIG_KEY_START;
import static org.olat.course.learningpath.ui.LearningPathNodeConfigController.CONFIG_KEY_TRIGGER;
import static org.olat.course.learningpath.ui.LearningPathNodeConfigController.CONFIG_VALUE_TRIGGER_CONFIRMED;
import static org.olat.course.learningpath.ui.LearningPathNodeConfigController.CONFIG_VALUE_TRIGGER_NODE_VISITED;
import static org.olat.course.learningpath.ui.LearningPathNodeConfigController.CONFIG_VALUE_TRIGGER_PASSED;
import static org.olat.course.learningpath.ui.LearningPathNodeConfigController.CONFIG_VALUE_TRIGGER_SCORE;
import static org.olat.course.learningpath.ui.LearningPathNodeConfigController.CONFIG_VALUE_TRIGGER_STATUS_DONE;
import static org.olat.course.learningpath.ui.LearningPathNodeConfigController.CONFIG_VALUE_TRIGGER_STATUS_IN_REVIEW;

import java.util.Date;

import org.olat.core.util.StringHelper;
import org.olat.course.learningpath.LearningPathConfigs;
import org.olat.modules.ModuleConfiguration;
import org.olat.modules.assessment.model.AssessmentEntryStatus;
import org.olat.modules.assessment.model.AssessmentObligation;

/**
 * 
 * Initial date: 30 Aug 2019<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class ModuleLearningPathConfigs implements LearningPathConfigs {
	
	protected final ModuleConfiguration moduleConfiguration;
	private final boolean doneOnFullyAssessed;

	public ModuleLearningPathConfigs(ModuleConfiguration moduleConfiguration, boolean doneOnFullyAssessed) {
		this.moduleConfiguration = moduleConfiguration;
		this.doneOnFullyAssessed = doneOnFullyAssessed;
	}

	@Override
	public Integer getDuration() {
		String duration = moduleConfiguration.getStringValue(CONFIG_KEY_DURATION);
		return integerOrNull(duration);
	}

	@Override
	public void setDuration(Integer duration) {
		moduleConfiguration.setStringValue(CONFIG_KEY_DURATION, duration.toString());
	}

	private Integer integerOrNull(String value) {
		if (StringHelper.containsNonWhitespace(value)) {
			try {
				return Integer.valueOf(value);
			} catch (Exception e) {
				// fall through
			}
		}
		return null;
	}

	@Override
	public AssessmentObligation getObligation() {
		String value = moduleConfiguration.getStringValue(CONFIG_KEY_OBLIGATION, CONFIG_DEFAULT_OBLIGATION);
		return AssessmentObligation.valueOf(value);
	}

	@Override
	public Date getStartDate() {
		return moduleConfiguration.getDateValue(CONFIG_KEY_START);
	}

	@Override
	public FullyAssessedResult isFullyAssessedOnNodeVisited() {
		String fullyAssessedTrigger = getFullyAssessedTrigger();
		if (CONFIG_VALUE_TRIGGER_NODE_VISITED.equals(fullyAssessedTrigger)) {
			return LearningPathConfigs.fullyAssessed(true, true, doneOnFullyAssessed);
		}
		return LearningPathConfigs.notFullyAssessed();
	}

	@Override
	public FullyAssessedResult isFullyAssessedOnConfirmation(boolean confirmed) {
		String fullyAssessedTrigger = getFullyAssessedTrigger();
		if (CONFIG_VALUE_TRIGGER_CONFIRMED.equals(fullyAssessedTrigger)) {
			return LearningPathConfigs.fullyAssessed(true, confirmed, doneOnFullyAssessed);
		}
		return LearningPathConfigs.notFullyAssessed();
	}

	@Override
	public FullyAssessedResult isFullyAssessedOnScore(Float score, Boolean userVisibility) {
		String fullyAssessedTrigger = getFullyAssessedTrigger();
		if (CONFIG_VALUE_TRIGGER_SCORE.equals(fullyAssessedTrigger)) {
			Integer scoreCut = toInteger(moduleConfiguration.getStringValue(CONFIG_KEY_SCORE_CUT_VALUE));
			boolean fullyAssessed = Boolean.TRUE.equals(userVisibility) && score != null && scoreCut != null
					&& score >= scoreCut.intValue();
			return LearningPathConfigs.fullyAssessed(true, fullyAssessed, doneOnFullyAssessed);
		}
		return LearningPathConfigs.notFullyAssessed();
	}

	@Override
	public FullyAssessedResult isFullyAssessedOnPassed(Boolean passed, Boolean userVisibility) {
		String fullyAssessedTrigger = getFullyAssessedTrigger();
		if (CONFIG_VALUE_TRIGGER_PASSED.equals(fullyAssessedTrigger)) {
			boolean fullyAssessed = Boolean.TRUE.equals(passed) && Boolean.TRUE.equals(userVisibility);
			return LearningPathConfigs.fullyAssessed(true, fullyAssessed, doneOnFullyAssessed);
		}
		return LearningPathConfigs.notFullyAssessed();
	}

	@Override
	public FullyAssessedResult isFullyAssessedOnStatus(AssessmentEntryStatus status) {
		String fullyAssessedTrigger = getFullyAssessedTrigger();
		if (CONFIG_VALUE_TRIGGER_STATUS_DONE.equals(fullyAssessedTrigger)) {
			boolean fullyAssessed = AssessmentEntryStatus.done.equals(status);
			return LearningPathConfigs.fullyAssessed(true, fullyAssessed, false);
		}
		if (CONFIG_VALUE_TRIGGER_STATUS_IN_REVIEW.equals(fullyAssessedTrigger)) {
			boolean fullyAssessed = AssessmentEntryStatus.done.equals(status)
					|| AssessmentEntryStatus.inReview.equals(status);
			return LearningPathConfigs.fullyAssessed(true, fullyAssessed, false);
		}
		return LearningPathConfigs.notFullyAssessed();
	}

	private String getFullyAssessedTrigger() {
		return moduleConfiguration.getStringValue(CONFIG_KEY_TRIGGER, CONFIG_DEFAULT_TRIGGER);
	}
	
	private Integer toInteger(String value) {
		if (StringHelper.containsNonWhitespace(value)) {
			try {
				return Integer.valueOf(value);
			} catch (NumberFormatException e) {
				// 
			}
		}
		return null;
	}

}
