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

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.olat.core.gui.components.date.OffsetDirection;
import org.olat.modules.curriculum.AutomationContext;
import org.olat.modules.curriculum.AutomationDependingOn;
import org.olat.modules.curriculum.AutomationType;
import org.olat.modules.curriculum.AutomationUnit;
import org.olat.modules.curriculum.CurriculumAutomationConfig;
import org.olat.modules.curriculum.CurriculumAutomationRule;
import org.olat.modules.curriculum.CurriculumElementStatus;
import org.olat.modules.curriculum.CurriculumElementType;
import org.olat.modules.curriculum.model.CurriculumAutomationConfigImpl;
import org.olat.modules.curriculum.model.CurriculumAutomationRuleImpl;
import org.olat.repository.RepositoryEntryStatusEnum;

/**
 * Initial date: 2026-06-26<br>
 * @author uhensler, urs.hensler@frentix.com, https://www.frentix.com
 */
public class CurriculumAutomationStandardRules {

	private CurriculumAutomationStandardRules() {
	}

	public static List<CurriculumAutomationConfig> createStandardConfig(CurriculumElementType type) {
		return createStandardConfig(type.isImplOnly(), type.getMaxRepositoryEntryRelations());
	}

	public static List<CurriculumAutomationConfig> createStandardConfig(boolean implOnly, int maxRepositoryEntryRelations) {
		List<CurriculumAutomationRule> rules = createStandardRules(implOnly, maxRepositoryEntryRelations);

		List<CurriculumAutomationConfig> configs = new ArrayList<>();
		for (CurriculumAutomationRule rule : rules) {
			CurriculumAutomationConfigImpl config = new CurriculumAutomationConfigImpl();
			config.setRule(rule);
			config.setEnabled(true);
			configs.add(config);
		}
		return configs;
	}

	private static List<CurriculumAutomationRule> createStandardRules(boolean implOnly, int maxRepositoryEntryRelations) {
		List<CurriculumAutomationRule> rules = new ArrayList<>();

		if (implOnly) {
			addImplementationRules(rules);
		} else {
			addElementRules(rules);
		}

		if (maxRepositoryEntryRelations != 0) {
			addContentRulesForImplementation(rules, implOnly);
		}

		return rules;
	}

	private static void addImplementationRules(List<CurriculumAutomationRule> rules) {
		CurriculumAutomationRule confirmedRule = new CurriculumAutomationRuleImpl();
		confirmedRule.setContext(AutomationContext.IMPLEMENTATION);
		confirmedRule.setAutomationType(AutomationType.STATUS_CHANGE);
		confirmedRule.setTargetStatus(CurriculumElementStatus.confirmed);
		confirmedRule.setDependingOn(AutomationDependingOn.EXECUTION_PERIOD);
		confirmedRule.setReference(CurriculumAutomationRule.REFERENCE_BEGIN);
		confirmedRule.setValue(7);
		confirmedRule.setUnit(AutomationUnit.DAYS);
		confirmedRule.setDirection(OffsetDirection.BEFORE);
		confirmedRule.setOnlyWhenStatus(Set.of("preparation", "provisional"));
		rules.add(confirmedRule);

		CurriculumAutomationRule finishedRule = new CurriculumAutomationRuleImpl();
		finishedRule.setContext(AutomationContext.IMPLEMENTATION);
		finishedRule.setAutomationType(AutomationType.STATUS_CHANGE);
		finishedRule.setTargetStatus(CurriculumElementStatus.finished);
		finishedRule.setDependingOn(AutomationDependingOn.EXECUTION_PERIOD);
		finishedRule.setReference(CurriculumAutomationRule.REFERENCE_END);
		finishedRule.setValue(null);
		finishedRule.setUnit(AutomationUnit.SAME_DAY);
		finishedRule.setDirection(OffsetDirection.AFTER);
		finishedRule.setOnlyWhenStatus(Set.of("confirmed"));
		rules.add(finishedRule);
	}

	private static void addElementRules(List<CurriculumAutomationRule> rules) {
		CurriculumAutomationRule activeRule = new CurriculumAutomationRuleImpl();
		activeRule.setContext(AutomationContext.ELEMENT);
		activeRule.setAutomationType(AutomationType.STATUS_CHANGE);
		activeRule.setTargetStatus(CurriculumElementStatus.active);
		activeRule.setDependingOn(AutomationDependingOn.EXECUTION_PERIOD);
		activeRule.setReference(CurriculumAutomationRule.REFERENCE_BEGIN);
		activeRule.setValue(null);
		activeRule.setUnit(AutomationUnit.SAME_DAY);
		activeRule.setDirection(OffsetDirection.BEFORE);
		activeRule.setOnlyWhenStatus(Set.of("preparation"));
		rules.add(activeRule);

		CurriculumAutomationRule finishedRule = new CurriculumAutomationRuleImpl();
		finishedRule.setContext(AutomationContext.ELEMENT);
		finishedRule.setAutomationType(AutomationType.STATUS_CHANGE);
		finishedRule.setTargetStatus(CurriculumElementStatus.finished);
		finishedRule.setDependingOn(AutomationDependingOn.EXECUTION_PERIOD);
		finishedRule.setReference(CurriculumAutomationRule.REFERENCE_END);
		finishedRule.setValue(null);
		finishedRule.setUnit(AutomationUnit.SAME_DAY);
		finishedRule.setDirection(OffsetDirection.AFTER);
		finishedRule.setOnlyWhenStatus(Set.of("active"));
		rules.add(finishedRule);
	}

	private static void addContentRulesForImplementation(List<CurriculumAutomationRule> rules, boolean implOnly) {
		String triggerStatus = implOnly ? "confirmed" : "active";

		CurriculumAutomationRule instantiationRule = new CurriculumAutomationRuleImpl();
		instantiationRule.setContext(AutomationContext.CONTENT);
		instantiationRule.setAutomationType(AutomationType.INSTANTIATION);
		instantiationRule.setDependingOn(AutomationDependingOn.STATUS);
		instantiationRule.setDependingOnStatus(Set.of(triggerStatus));
		rules.add(instantiationRule);

		CurriculumAutomationRule accessRule = new CurriculumAutomationRuleImpl();
		accessRule.setContext(AutomationContext.CONTENT);
		accessRule.setAutomationType(AutomationType.STATUS_CHANGE);
		accessRule.setTargetStatus(RepositoryEntryStatusEnum.coachpublished);
		accessRule.setDependingOn(AutomationDependingOn.STATUS);
		accessRule.setDependingOnStatus(Set.of(triggerStatus));
		rules.add(accessRule);

		CurriculumAutomationRule publishedRule = new CurriculumAutomationRuleImpl();
		publishedRule.setContext(AutomationContext.CONTENT);
		publishedRule.setAutomationType(AutomationType.STATUS_CHANGE);
		publishedRule.setTargetStatus(RepositoryEntryStatusEnum.published);
		publishedRule.setDependingOn(AutomationDependingOn.EXECUTION_PERIOD);
		publishedRule.setReference(CurriculumAutomationRule.REFERENCE_BEGIN);
		publishedRule.setValue(null);
		publishedRule.setUnit(AutomationUnit.SAME_DAY);
		publishedRule.setDirection(OffsetDirection.BEFORE);
		publishedRule.setOnlyWhenStatus(Set.of(triggerStatus));
		rules.add(publishedRule);

		CurriculumAutomationRule contentFinishedRule = new CurriculumAutomationRuleImpl();
		contentFinishedRule.setContext(AutomationContext.CONTENT);
		contentFinishedRule.setAutomationType(AutomationType.STATUS_CHANGE);
		contentFinishedRule.setTargetStatus(RepositoryEntryStatusEnum.closed);
		contentFinishedRule.setDependingOn(AutomationDependingOn.STATUS);
		contentFinishedRule.setDependingOnStatus(Set.of("cancelled", "finished"));
		rules.add(contentFinishedRule);
	}
}
