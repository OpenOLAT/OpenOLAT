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

import static org.olat.course.learningpath.ui.LearningPathNodeConfigController.CONFIG_VALUE_TRIGGER_CONFIRMED;
import static org.olat.course.learningpath.ui.LearningPathNodeConfigController.CONFIG_VALUE_TRIGGER_NODE_VISITED;
import static org.olat.course.learningpath.ui.LearningPathNodeConfigController.CONFIG_VALUE_TRIGGER_PASSED;
import static org.olat.course.learningpath.ui.LearningPathNodeConfigController.CONFIG_VALUE_TRIGGER_SCORE;
import static org.olat.course.learningpath.ui.LearningPathNodeConfigController.CONFIG_VALUE_TRIGGER_STATUS_DONE;
import static org.olat.course.learningpath.ui.LearningPathNodeConfigController.CONFIG_VALUE_TRIGGER_STATUS_IN_REVIEW;

import java.util.Date;

import org.olat.core.util.StringHelper;
import org.olat.course.learningpath.FullyAssessedTrigger;
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
	
	private static final String CONFIG_KEY_DURATION = "duration";
	private static final String CONFIG_KEY_OBLIGATION = "obligation";
	private static final String CONFIG_KEY_START = "start.date";
	private static final String CONFIG_KEY_END = "end.date";
	private static final String CONFIG_KEY_TRIGGER = "fully.assessed.trigger";
	private static final String CONFIG_KEY_SCORE_CUT_VALUE = "scoreCutValue";
	
	protected final ModuleConfiguration moduleConfiguration;
	private final boolean doneOnFullyAssessed;

	public ModuleLearningPathConfigs(ModuleConfiguration moduleConfiguration, boolean doneOnFullyAssessed) {
		this.moduleConfiguration = moduleConfiguration;
		this.doneOnFullyAssessed = doneOnFullyAssessed;
	}

	@Override
	public Boolean hasSequentialChildren() {
		return null;
	}

	@Override
	public Integer getDuration() {
		String duration = moduleConfiguration.getStringValue(CONFIG_KEY_DURATION);
		return integerOrNull(duration);
	}

	@Override
	public void setDuration(Integer duration) {
		if (duration != null) {
			moduleConfiguration.setStringValue(CONFIG_KEY_DURATION, duration.toString());
		} else {
			moduleConfiguration.remove(CONFIG_KEY_DURATION);
		}
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
		String config = moduleConfiguration.getStringValue(CONFIG_KEY_OBLIGATION);
		return StringHelper.containsNonWhitespace(config)
				? AssessmentObligation.valueOf(config)
				: OBLIGATION_DEFAULT;
	}

	@Override
	public void setObligation(AssessmentObligation obligation) {
		if (obligation != null) {
			moduleConfiguration.setStringValue(CONFIG_KEY_OBLIGATION, obligation.name());
		} else {
			moduleConfiguration.remove(CONFIG_KEY_OBLIGATION);
		}
	}

	@Override
	public Date getStartDate() {
		return moduleConfiguration.getDateValue(CONFIG_KEY_START);
	}

	@Override
	public void setStartDate(Date start) {
		moduleConfiguration.setDateValue(CONFIG_KEY_START, start);
	}

	@Override
	public Date getEndDate() {
		return moduleConfiguration.getDateValue(CONFIG_KEY_END);
	}

	@Override
	public void setEndDate(Date end) {
		moduleConfiguration.setDateValue(CONFIG_KEY_END, end);
	}
	
	@Override
	public FullyAssessedTrigger getFullyAssessedTrigger() {
		String config = getFullyAssessedTriggerConfig();
		return config != null? FullyAssessedTrigger.valueOf(config): null;
	}

	private String getFullyAssessedTriggerConfig() {
		return moduleConfiguration.getStringValue(CONFIG_KEY_TRIGGER, CONFIG_VALUE_TRIGGER_CONFIRMED);
	}

	@Override
	public void setFullyAssessedTrigger(FullyAssessedTrigger trigger) {
		if (trigger != null) {
			moduleConfiguration.setStringValue(CONFIG_KEY_TRIGGER, trigger.name());
		} else {
			moduleConfiguration.remove(CONFIG_KEY_TRIGGER);
		}
	}

	@Override
	public void setScoreTriggerValue(Integer score) {
		if (score != null) {
			moduleConfiguration.setStringValue(CONFIG_KEY_SCORE_CUT_VALUE, score.toString());
		} else {
			moduleConfiguration.remove(CONFIG_KEY_SCORE_CUT_VALUE);
		}
	}

	@Override
	public Integer getScoreTriggerValue() {
		String fullyAssessedTrigger = getFullyAssessedTriggerConfig();
		return CONFIG_VALUE_TRIGGER_SCORE.equals(fullyAssessedTrigger)
			? toInteger(moduleConfiguration.getStringValue(CONFIG_KEY_SCORE_CUT_VALUE))
			: null;
	}

	@Override
	public FullyAssessedResult isFullyAssessedOnNodeVisited() {
		String fullyAssessedTrigger = getFullyAssessedTriggerConfig();
		if (CONFIG_VALUE_TRIGGER_NODE_VISITED.equals(fullyAssessedTrigger)) {
			return LearningPathConfigs.fullyAssessed(true, true, doneOnFullyAssessed);
		}
		return LearningPathConfigs.notFullyAssessed();
	}

	@Override
	public FullyAssessedResult isFullyAssessedOnConfirmation(boolean confirmed) {
		String fullyAssessedTrigger = getFullyAssessedTriggerConfig();
		if (CONFIG_VALUE_TRIGGER_CONFIRMED.equals(fullyAssessedTrigger)) {
			return LearningPathConfigs.fullyAssessed(true, confirmed, doneOnFullyAssessed);
		}
		return LearningPathConfigs.notFullyAssessed();
	}

	@Override
	public FullyAssessedResult isFullyAssessedOnScore(Float score, Boolean userVisibility) {
		String fullyAssessedTrigger = getFullyAssessedTriggerConfig();
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
		String fullyAssessedTrigger = getFullyAssessedTriggerConfig();
		if (CONFIG_VALUE_TRIGGER_PASSED.equals(fullyAssessedTrigger)) {
			boolean fullyAssessed = Boolean.TRUE.equals(passed) && Boolean.TRUE.equals(userVisibility);
			return LearningPathConfigs.fullyAssessed(true, fullyAssessed, doneOnFullyAssessed);
		}
		return LearningPathConfigs.notFullyAssessed();
	}

	@Override
	public FullyAssessedResult isFullyAssessedOnStatus(AssessmentEntryStatus status) {
		String fullyAssessedTrigger = getFullyAssessedTriggerConfig();
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
