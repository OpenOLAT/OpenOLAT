/**
 * <a href="https://www.openolat.org">
 * OpenOLAT - Online Learning and Training</a><br>
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); <br>
 * you may not use this file except in compliance with the License.<br>
 * You may obtain a copy of the License at the
 * <a href="https://www.apache.org/licenses/LICENSE-2.0">Apache homepage</a>
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
package org.olat.modules.curriculum.manager;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Set;

import org.junit.Test;
import org.olat.core.gui.components.date.OffsetDirection;
import org.olat.modules.curriculum.AutomationContext;
import org.olat.modules.curriculum.AutomationDependingOn;
import org.olat.modules.curriculum.AutomationUnit;
import org.olat.modules.curriculum.AutomationType;
import org.olat.modules.curriculum.CurriculumAutomationConfig;
import org.olat.modules.curriculum.CurriculumAutomationRule;

/**
 * Initial date: 2026-06-26<br>
 * @author uhensler, urs.hensler@frentix.com, https://www.frentix.com
 */
public class CurriculumAutomationConfigXStreamTest {

	@Test
	public void testRoundTripEmpty() {
		CurriculumAutomationConfig config = new CurriculumAutomationConfig();

		String xml = CurriculumAutomationConfigXStream.toXML(config);
		assertThat(xml).isNotBlank();

		CurriculumAutomationConfig loaded = CurriculumAutomationConfigXStream.fromXML(xml);
		assertThat(loaded).isNotNull();
		assertThat(loaded.getRules()).isNullOrEmpty();
	}

	@Test
	public void testRoundTripWithRules() {
		CurriculumAutomationConfig config = new CurriculumAutomationConfig();

		CurriculumAutomationRule rule1 = new CurriculumAutomationRule();
		rule1.setContext(AutomationContext.IMPLEMENTATION);
		rule1.setAutomationType(AutomationType.STATUS_CHANGE);
		rule1.setTargetStatus("active");
		rule1.setEnabled(true);
		rule1.setDependingOn(AutomationDependingOn.EXECUTION_PERIOD);
		rule1.setValue(7);
		rule1.setUnit(AutomationUnit.DAYS);
		rule1.setDirection(OffsetDirection.BEFORE);
		rule1.setDependingOnStatus(Set.of("confirmed"));
		rule1.setOnlyWhenStatus(Set.of("preparation", "confirmed"));
		config.addRule(rule1);

		CurriculumAutomationRule rule2 = new CurriculumAutomationRule();
		rule2.setContext(AutomationContext.CONTENT);
		rule2.setAutomationType(AutomationType.INSTANTIATION);
		rule2.setTargetStatus((String) null);
		rule2.setEnabled(false);
		rule2.setDependingOn(AutomationDependingOn.STATUS);
		rule2.setValue(null);
		rule2.setUnit(AutomationUnit.SAME_DAY);
		rule2.setDirection(OffsetDirection.AFTER);
		rule2.setDependingOnStatus(Set.of("active"));
		rule2.setOnlyWhenStatus(Set.of());
		config.addRule(rule2);

		String xml = CurriculumAutomationConfigXStream.toXML(config);
		assertThat(xml).isNotBlank();

		CurriculumAutomationConfig loaded = CurriculumAutomationConfigXStream.fromXML(xml);
		assertThat(loaded).isNotNull();
		assertThat(loaded.getRules()).hasSize(2);

		CurriculumAutomationRule loadedRule1 = loaded.getRules().get(0);
		assertThat(loadedRule1.getContext()).isEqualTo(AutomationContext.IMPLEMENTATION);
		assertThat(loadedRule1.getAutomationType()).isEqualTo(AutomationType.STATUS_CHANGE);
		assertThat(loadedRule1.getTargetStatus()).isEqualTo("active");
		assertThat(loadedRule1.isEnabled()).isTrue();
		assertThat(loadedRule1.getDependingOn()).isEqualTo(AutomationDependingOn.EXECUTION_PERIOD);
		assertThat(loadedRule1.getValue()).isEqualTo(7);
		assertThat(loadedRule1.getUnit()).isEqualTo(AutomationUnit.DAYS);
		assertThat(loadedRule1.getDirection()).isEqualTo(OffsetDirection.BEFORE);
		assertThat(loadedRule1.getDependingOnStatus()).contains("confirmed");
		assertThat(loadedRule1.getOnlyWhenStatus()).containsExactlyInAnyOrder("preparation", "confirmed");

		CurriculumAutomationRule loadedRule2 = loaded.getRules().get(1);
		assertThat(loadedRule2.getContext()).isEqualTo(AutomationContext.CONTENT);
		assertThat(loadedRule2.getAutomationType()).isEqualTo(AutomationType.INSTANTIATION);
		assertThat(loadedRule2.getTargetStatus()).isNull();
		assertThat(loadedRule2.isEnabled()).isFalse();
	}

	@Test
	public void testFromXmlNull() {
		assertThat(CurriculumAutomationConfigXStream.fromXML(null)).isNull();
	}

	@Test
	public void testFromXmlEmpty() {
		assertThat(CurriculumAutomationConfigXStream.fromXML("")).isNull();
	}
}
