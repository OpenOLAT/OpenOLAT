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

import org.assertj.core.api.SoftAssertions;
import org.junit.Test;
import org.olat.course.learningpath.FullyAssessedTrigger;
import org.olat.modules.ModuleConfiguration;
import org.olat.modules.assessment.model.AssessmentObligation;

/**
 * 
 * Initial date: 24 Aug 2021<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class ModuleLearningPathConfigsTest {
	
	private FullyAssessedTrigger TRIGGER = FullyAssessedTrigger.statusInReview;

	@Test
	public void shouldUpdateDefaultsOfNewNode() {
		ModuleConfiguration moduleConfig = new ModuleConfiguration();
		ModuleLearningPathConfigs sut = new ModuleLearningPathConfigs(moduleConfig, true);

		sut.updateDefaults(true, TRIGGER);
		
		SoftAssertions softly = new SoftAssertions();
		softly.assertThat(moduleConfig.getIntegerSafe(ModuleLearningPathConfigs.CONFIG_VERSION, 0))
				.as("Version is initialized")
				.isEqualTo(1);
		softly.assertThat(moduleConfig.getStringValue(ModuleLearningPathConfigs.CONFIG_KEY_OBLIGATION))
				.as("Obligation is initialized")
				.isEqualTo(ModuleLearningPathConfigs.OBLIGATION_DEFAULT.name());
		softly.assertThat(moduleConfig.getStringValue(ModuleLearningPathConfigs.CONFIG_KEY_TRIGGER))
				.as("Trigger is initialized")
				.isEqualTo(TRIGGER.name());
		softly.assertAll();
	}
	
	@Test
	public void shouldUpdateDefaultsOfExistingUnsavedNode() {
		ModuleConfiguration moduleConfig = new ModuleConfiguration();
		ModuleLearningPathConfigs sut = new ModuleLearningPathConfigs(moduleConfig, true);

		sut.updateDefaults(false, TRIGGER);
		
		SoftAssertions softly = new SoftAssertions();
		softly.assertThat(moduleConfig.getIntegerSafe(ModuleLearningPathConfigs.CONFIG_VERSION, 0))
				.as("Version is initialized")
				.isEqualTo(1);
		softly.assertThat(moduleConfig.getStringValue(ModuleLearningPathConfigs.CONFIG_KEY_OBLIGATION))
				.as("Obligation is initialized")
				.isEqualTo(ModuleLearningPathConfigs.OBLIGATION_DEFAULT.name());
		softly.assertThat(moduleConfig.getStringValue(ModuleLearningPathConfigs.CONFIG_KEY_TRIGGER))
				.as("Trigger is initialized")
				.isEqualTo(ModuleLearningPathConfigs.LEGACY_TRIGGER_DEFAULT.name());
		softly.assertAll();
	}
	
	@Test
	public void shouldUpdateDefaultsOfExistingSavedNode() {
		ModuleConfiguration moduleConfig = new ModuleConfiguration();
		ModuleLearningPathConfigs sut = new ModuleLearningPathConfigs(moduleConfig, true);
		// Saved configs have trigger and obligation
		sut.setFullyAssessedTrigger(FullyAssessedTrigger.nodeVisited);
		sut.setObligation(AssessmentObligation.optional);

		sut.updateDefaults(false, TRIGGER);
		
		SoftAssertions softly = new SoftAssertions();
		softly.assertThat(moduleConfig.getIntegerSafe(ModuleLearningPathConfigs.CONFIG_VERSION, 0))
				.as("Version is initialized")
				.isEqualTo(1);
		softly.assertThat(moduleConfig.getStringValue(ModuleLearningPathConfigs.CONFIG_KEY_OBLIGATION))
				.as("Obligation is initialized")
				.isEqualTo(AssessmentObligation.optional.name());
		softly.assertThat(moduleConfig.getStringValue(ModuleLearningPathConfigs.CONFIG_KEY_TRIGGER))
				.as("Trigger is initialized")
				.isEqualTo(FullyAssessedTrigger.nodeVisited.name());
		softly.assertAll();
	}

}
