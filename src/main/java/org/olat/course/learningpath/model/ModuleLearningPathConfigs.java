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

import java.util.List;
import java.util.Set;

import org.olat.core.util.StringHelper;
import org.olat.course.duedate.DueDateConfig;
import org.olat.course.learningpath.FullyAssessedTrigger;
import org.olat.course.learningpath.LearningPathConfigs;
import org.olat.course.learningpath.obligation.ExceptionalObligation;
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
	
	private static final Set<AssessmentObligation> AVAILABLE_OBLIGATIONS = Set.of(AssessmentObligation.mandatory,
			AssessmentObligation.optional, AssessmentObligation.excluded);

	static final String CONFIG_VERSION = "lp.configversion";
	static final int VERSION_CURRENT = 2;
	static final String CONFIG_KEY_DURATION = "duration";
	static final String CONFIG_KEY_OBLIGATION = "obligation";
	static final String CONFIG_KEY_EXCEPTIONAL_OBLIGATIONS = "lp.exeptional.obligations";
	static final String CONFIG_KEY_RELATIVE_DATES = "lp.rel.dates";
	static final String CONFIG_KEY_START = "start.date";
	static final String CONFIG_KEY_START_RELATIVE = "start.date.relative";
	static final String CONFIG_KEY_START_RELATIVE_TO = "start.date.relative.to";
	static final String CONFIG_KEY_END = "end.date";
	static final String CONFIG_KEY_END_RELATIVE = "end.date.relative";
	static final String CONFIG_KEY_END_RELATIVE_TO = "end.date.relative.to";
	static final String CONFIG_KEY_TRIGGER = "fully.assessed.trigger";
	static final String CONFIG_KEY_SCORE_CUT_VALUE = "scoreCutValue";
	
	protected final ModuleConfiguration moduleConfiguration;
	private final boolean doneOnFullyAssessed;

	public ModuleLearningPathConfigs(ModuleConfiguration moduleConfiguration, boolean doneOnFullyAssessed) {
		this.moduleConfiguration = moduleConfiguration;
		this.doneOnFullyAssessed = doneOnFullyAssessed;
	}

	public void updateDefaults(boolean newNode, FullyAssessedTrigger trigger) {
		int version = moduleConfiguration.getIntegerSafe(CONFIG_VERSION, 1);
		if (newNode) {
			setFullyAssessedTrigger(trigger);
			if (isInitObligation()) {
				setObligation(LearningPathConfigs.OBLIGATION_DEFAULT);
			}
		} else if (!moduleConfiguration.has(CONFIG_KEY_TRIGGER)) {
			// Initialisation of existing course node. In the early days the configuration was not initialized
			// when a new course node was created. A null trigger indicates that the user never saved
			// the learning path configurations, so we have to initialize the old default values.
			setFullyAssessedTrigger(LEGACY_TRIGGER_DEFAULT);
			if (isInitObligation()) {
				setObligation(LearningPathConfigs.OBLIGATION_DEFAULT);
			}
		}
		
		if (version < 2) {
			moduleConfiguration.setBooleanEntry(CONFIG_KEY_RELATIVE_DATES, false);
		}
		
		moduleConfiguration.setIntValue(CONFIG_VERSION, VERSION_CURRENT);
	}

	protected boolean isInitObligation() {
		return true;
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
	public Set<AssessmentObligation> getAvailableObligations() {
		return AVAILABLE_OBLIGATIONS;
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
	public List<ExceptionalObligation> getExceptionalObligations() {
		return moduleConfiguration.getList(CONFIG_KEY_EXCEPTIONAL_OBLIGATIONS, ExceptionalObligation.class);
	}

	@Override
	public void setExceptionalObligations(List<ExceptionalObligation> exeptionalObligations) {
		moduleConfiguration.setList(CONFIG_KEY_EXCEPTIONAL_OBLIGATIONS, exeptionalObligations);
	}

	@Override
	public boolean isRelativeDates() {
		return moduleConfiguration.getBooleanSafe(CONFIG_KEY_RELATIVE_DATES);
	}

	@Override
	public void setRelativeDates(boolean relativeDates) {
		moduleConfiguration.setBooleanEntry(CONFIG_KEY_RELATIVE_DATES, relativeDates);
	}

	@Override
	public DueDateConfig getStartDateConfig() {
		return DueDateConfig.ofModuleConfiguration(moduleConfiguration, CONFIG_KEY_RELATIVE_DATES, CONFIG_KEY_START,
				CONFIG_KEY_START_RELATIVE, CONFIG_KEY_START_RELATIVE_TO);
	}

	@Override
	public void setStartDateConfig(DueDateConfig start) {
		moduleConfiguration.remove(CONFIG_KEY_START);
		moduleConfiguration.remove(CONFIG_KEY_START_RELATIVE);
		moduleConfiguration.remove(CONFIG_KEY_START_RELATIVE_TO);
		moduleConfiguration.setDateValue(CONFIG_KEY_START, start.getAbsoluteDate());
		moduleConfiguration.setIntValue(CONFIG_KEY_START_RELATIVE, start.getNumOfDays());
		moduleConfiguration.setStringValue(CONFIG_KEY_START_RELATIVE_TO, start.getRelativeToType());
	}

	@Override
	public DueDateConfig getEndDateConfig() {
		return DueDateConfig.ofModuleConfiguration(moduleConfiguration, CONFIG_KEY_RELATIVE_DATES, CONFIG_KEY_END,
				CONFIG_KEY_END_RELATIVE, CONFIG_KEY_END_RELATIVE_TO);
	}

	@Override
	public void setEndDateConfig(DueDateConfig end) {
		moduleConfiguration.remove(CONFIG_KEY_END);
		moduleConfiguration.remove(CONFIG_KEY_END_RELATIVE);
		moduleConfiguration.remove(CONFIG_KEY_END_RELATIVE_TO);
		moduleConfiguration.setDateValue(CONFIG_KEY_END, end.getAbsoluteDate());
		moduleConfiguration.setIntValue(CONFIG_KEY_END_RELATIVE, end.getNumOfDays());
		moduleConfiguration.setStringValue(CONFIG_KEY_END_RELATIVE_TO, end.getRelativeToType());
	}
	
	@Override
	public FullyAssessedTrigger getFullyAssessedTrigger() {
		String config = getFullyAssessedTriggerConfig();
		return config != null? FullyAssessedTrigger.valueOf(config): null;
	}

	private String getFullyAssessedTriggerConfig() {
		return moduleConfiguration.getStringValue(CONFIG_KEY_TRIGGER, LEGACY_TRIGGER_DEFAULT.name());
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
