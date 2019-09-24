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
import static org.olat.course.learningpath.ui.LearningPathNodeConfigController.CONFIG_KEY_TRIGGER;
import static org.olat.course.learningpath.ui.LearningPathNodeConfigController.CONFIG_VALUE_TRIGGER_NODE_VISITED;
import static org.olat.course.learningpath.ui.LearningPathNodeConfigController.CONFIG_VALUE_TRIGGER_STATUS_DONE;

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
	
	private final ModuleConfiguration moduleConfiguration;

	public ModuleLearningPathConfigs(ModuleConfiguration moduleConfiguration) {
		this.moduleConfiguration = moduleConfiguration;
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
	public FullyAssessedResult isFullyAssessedOnNodeVisited() {
		String doneTriggerName = getDoneTriggerName();
		if (CONFIG_VALUE_TRIGGER_NODE_VISITED.equals(doneTriggerName)) {
			return LearningPathConfigs.fullyAssessed(true, true);
		}
		return LearningPathConfigs.notFullyAssessed();
	}

	private String getDoneTriggerName() {
		return moduleConfiguration.getStringValue(CONFIG_KEY_TRIGGER, CONFIG_DEFAULT_TRIGGER);
	}

	@Override
	public FullyAssessedResult isFullyAssessedOnCompletion(Double completion) {
		// not implemented yet
		return LearningPathConfigs.notFullyAssessed();
	}

	@Override
	public FullyAssessedResult isFullyAssessedOnStatus(AssessmentEntryStatus status) {
		String doneTriggerName = getDoneTriggerName();
		if (AssessmentEntryStatus.done.equals(status) && CONFIG_VALUE_TRIGGER_STATUS_DONE.equals(doneTriggerName)) {
			return LearningPathConfigs.fullyAssessed(true, false);
		}
		return LearningPathConfigs.notFullyAssessed();
	}

}
