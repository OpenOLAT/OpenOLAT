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
package org.olat.modules.curriculum.manager;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.Test;
import org.olat.core.gui.components.date.OffsetDirection;
import org.olat.modules.curriculum.AutomationContext;
import org.olat.modules.curriculum.AutomationDependingOn;
import org.olat.modules.curriculum.AutomationUnit;
import org.olat.modules.curriculum.AutomationType;
import org.olat.modules.curriculum.CurriculumAutomationConfig;
import org.olat.modules.curriculum.CurriculumAutomationRule;
import org.olat.modules.curriculum.CurriculumElementType;

/**
 * Initial date: 2026-06-26<br>
 * @author uhensler, urs.hensler@frentix.com, https://www.frentix.com
 */
public class CurriculumAutomationStandardRulesTest {

	@Test
	public void testImplTypeWithContent() {
		CurriculumElementType type = mock(CurriculumElementType.class);
		when(type.isImplOnly()).thenReturn(true);
		when(type.getMaxRepositoryEntryRelations()).thenReturn(1);

		List<CurriculumAutomationConfig> configs = CurriculumAutomationStandardRules.createStandardConfig(type);

		assertThat(configs).hasSize(6);

		CurriculumAutomationRule rule0 = configs.get(0).getRule();
		assertThat(configs.get(0).isEnabled()).isTrue();
		assertThat(rule0.getContext()).isEqualTo(AutomationContext.IMPLEMENTATION);
		assertThat(rule0.getAutomationType()).isEqualTo(AutomationType.STATUS_CHANGE);
		assertThat(rule0.getTargetStatus()).isEqualTo("confirmed");
		assertThat(rule0.getDependingOn()).isEqualTo(AutomationDependingOn.EXECUTION_PERIOD);
		assertThat(rule0.getValue()).isEqualTo(7);
		assertThat(rule0.getUnit()).isEqualTo(AutomationUnit.DAYS);
		assertThat(rule0.getDirection()).isEqualTo(OffsetDirection.BEFORE);
		assertThat(rule0.getOnlyWhenStatus()).contains("preparation", "provisional");

		CurriculumAutomationRule rule1 = configs.get(1).getRule();
		assertThat(configs.get(1).isEnabled()).isTrue();
		assertThat(rule1.getContext()).isEqualTo(AutomationContext.IMPLEMENTATION);
		assertThat(rule1.getAutomationType()).isEqualTo(AutomationType.STATUS_CHANGE);
		assertThat(rule1.getTargetStatus()).isEqualTo("finished");
		assertThat(rule1.getDependingOn()).isEqualTo(AutomationDependingOn.EXECUTION_PERIOD);
		assertThat(rule1.getUnit()).isEqualTo(AutomationUnit.SAME_DAY);
		assertThat(rule1.getDirection()).isEqualTo(OffsetDirection.AFTER);
		assertThat(rule1.getOnlyWhenStatus()).contains("confirmed");

		CurriculumAutomationRule rule2 = configs.get(2).getRule();
		assertThat(rule2.getContext()).isEqualTo(AutomationContext.CONTENT);
		assertThat(rule2.getAutomationType()).isEqualTo(AutomationType.INSTANTIATION);
		assertThat(rule2.getTargetStatus()).isNull();
		assertThat(rule2.getDependingOn()).isEqualTo(AutomationDependingOn.STATUS);
		assertThat(rule2.getDependingOnStatus()).contains("confirmed");

		CurriculumAutomationRule rule3 = configs.get(3).getRule();
		assertThat(rule3.getContext()).isEqualTo(AutomationContext.CONTENT);
		assertThat(rule3.getAutomationType()).isEqualTo(AutomationType.STATUS_CHANGE);
		assertThat(rule3.getTargetStatus()).isEqualTo("coachpublished");
		assertThat(rule3.getDependingOn()).isEqualTo(AutomationDependingOn.STATUS);
		assertThat(rule3.getDependingOnStatus()).contains("confirmed");

		CurriculumAutomationRule rule4 = configs.get(4).getRule();
		assertThat(rule4.getContext()).isEqualTo(AutomationContext.CONTENT);
		assertThat(rule4.getAutomationType()).isEqualTo(AutomationType.STATUS_CHANGE);
		assertThat(rule4.getTargetStatus()).isEqualTo("published");
		assertThat(rule4.getDependingOn()).isEqualTo(AutomationDependingOn.EXECUTION_PERIOD);
		assertThat(rule4.getUnit()).isEqualTo(AutomationUnit.SAME_DAY);
		assertThat(rule4.getDirection()).isEqualTo(OffsetDirection.BEFORE);
		assertThat(rule4.getOnlyWhenStatus()).contains("confirmed");

		CurriculumAutomationRule rule5 = configs.get(5).getRule();
		assertThat(rule5.getContext()).isEqualTo(AutomationContext.CONTENT);
		assertThat(rule5.getAutomationType()).isEqualTo(AutomationType.STATUS_CHANGE);
		assertThat(rule5.getTargetStatus()).isEqualTo("closed");
		assertThat(rule5.getDependingOn()).isEqualTo(AutomationDependingOn.STATUS);
		assertThat(rule5.getDependingOnStatus()).contains("cancelled", "finished");
	}

	@Test
	public void testImplTypeWithoutContent() {
		CurriculumElementType type = mock(CurriculumElementType.class);
		when(type.isImplOnly()).thenReturn(true);
		when(type.getMaxRepositoryEntryRelations()).thenReturn(0);

		List<CurriculumAutomationConfig> configs = CurriculumAutomationStandardRules.createStandardConfig(type);

		assertThat(configs).hasSize(2);
		assertThat(configs.get(0).getRule().getContext()).isEqualTo(AutomationContext.IMPLEMENTATION);
		assertThat(configs.get(0).getRule().getTargetStatus()).isEqualTo("confirmed");
		assertThat(configs.get(1).getRule().getContext()).isEqualTo(AutomationContext.IMPLEMENTATION);
		assertThat(configs.get(1).getRule().getTargetStatus()).isEqualTo("finished");
	}

	@Test
	public void testElementTypeWithContent() {
		CurriculumElementType type = mock(CurriculumElementType.class);
		when(type.isImplOnly()).thenReturn(false);
		when(type.getMaxRepositoryEntryRelations()).thenReturn(1);

		List<CurriculumAutomationConfig> configs = CurriculumAutomationStandardRules.createStandardConfig(type);

		assertThat(configs).hasSize(6);

		CurriculumAutomationRule rule0 = configs.get(0).getRule();
		assertThat(rule0.getContext()).isEqualTo(AutomationContext.ELEMENT);
		assertThat(rule0.getAutomationType()).isEqualTo(AutomationType.STATUS_CHANGE);
		assertThat(rule0.getTargetStatus()).isEqualTo("active");
		assertThat(rule0.getDependingOn()).isEqualTo(AutomationDependingOn.EXECUTION_PERIOD);
		assertThat(rule0.getUnit()).isEqualTo(AutomationUnit.SAME_DAY);
		assertThat(rule0.getDirection()).isEqualTo(OffsetDirection.BEFORE);
		assertThat(rule0.getOnlyWhenStatus()).contains("preparation");

		CurriculumAutomationRule rule1 = configs.get(1).getRule();
		assertThat(rule1.getContext()).isEqualTo(AutomationContext.ELEMENT);
		assertThat(rule1.getAutomationType()).isEqualTo(AutomationType.STATUS_CHANGE);
		assertThat(rule1.getTargetStatus()).isEqualTo("finished");
		assertThat(rule1.getDependingOn()).isEqualTo(AutomationDependingOn.EXECUTION_PERIOD);
		assertThat(rule1.getUnit()).isEqualTo(AutomationUnit.SAME_DAY);
		assertThat(rule1.getDirection()).isEqualTo(OffsetDirection.AFTER);
		assertThat(rule1.getOnlyWhenStatus()).contains("active");

		CurriculumAutomationRule rule2 = configs.get(2).getRule();
		assertThat(rule2.getContext()).isEqualTo(AutomationContext.CONTENT);
		assertThat(rule2.getAutomationType()).isEqualTo(AutomationType.INSTANTIATION);
		assertThat(rule2.getDependingOnStatus()).contains("active");

		CurriculumAutomationRule rule3 = configs.get(3).getRule();
		assertThat(rule3.getContext()).isEqualTo(AutomationContext.CONTENT);
		assertThat(rule3.getAutomationType()).isEqualTo(AutomationType.STATUS_CHANGE);
		assertThat(rule3.getTargetStatus()).isEqualTo("coachpublished");
		assertThat(rule3.getDependingOnStatus()).contains("active");

		CurriculumAutomationRule rule4 = configs.get(4).getRule();
		assertThat(rule4.getContext()).isEqualTo(AutomationContext.CONTENT);
		assertThat(rule4.getAutomationType()).isEqualTo(AutomationType.STATUS_CHANGE);
		assertThat(rule4.getTargetStatus()).isEqualTo("published");
		assertThat(rule4.getUnit()).isEqualTo(AutomationUnit.SAME_DAY);
		assertThat(rule4.getDirection()).isEqualTo(OffsetDirection.BEFORE);
		assertThat(rule4.getOnlyWhenStatus()).contains("active");

		CurriculumAutomationRule rule5 = configs.get(5).getRule();
		assertThat(rule5.getContext()).isEqualTo(AutomationContext.CONTENT);
		assertThat(rule5.getAutomationType()).isEqualTo(AutomationType.STATUS_CHANGE);
		assertThat(rule5.getTargetStatus()).isEqualTo("closed");
		assertThat(rule5.getDependingOnStatus()).contains("cancelled", "finished");
	}

	@Test
	public void testElementTypeWithoutContent() {
		CurriculumElementType type = mock(CurriculumElementType.class);
		when(type.isImplOnly()).thenReturn(false);
		when(type.getMaxRepositoryEntryRelations()).thenReturn(0);

		List<CurriculumAutomationConfig> configs = CurriculumAutomationStandardRules.createStandardConfig(type);

		assertThat(configs).hasSize(2);
		assertThat(configs.get(0).getRule().getContext()).isEqualTo(AutomationContext.ELEMENT);
		assertThat(configs.get(0).getRule().getTargetStatus()).isEqualTo("active");
		assertThat(configs.get(1).getRule().getContext()).isEqualTo(AutomationContext.ELEMENT);
		assertThat(configs.get(1).getRule().getTargetStatus()).isEqualTo("finished");
	}
}
