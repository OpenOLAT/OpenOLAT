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
import org.olat.repository.RepositoryEntryStatusEnum;

/**
 * Initial date: 2026-06-26<br>
 * @author uhensler, urs.hensler@frentix.com, https://www.frentix.com
 */
public class CurriculumAutomationStandardRules {

	private CurriculumAutomationStandardRules() {
	}

	public static CurriculumAutomationConfig createStandardConfig(CurriculumElementType type) {
		return createStandardConfig(type.isImplOnly(), type.getMaxRepositoryEntryRelations());
	}

	public static CurriculumAutomationConfig createStandardConfig(boolean implOnly, int maxRepositoryEntryRelations) {
		CurriculumAutomationConfig config = new CurriculumAutomationConfig();

		if (implOnly) {
			addImplementationRules(config);
		} else {
			addElementRules(config);
		}

		if (maxRepositoryEntryRelations != 0) {
			addContentRulesForImplementation(config, implOnly);
		}

		return config;
	}

	private static void addImplementationRules(CurriculumAutomationConfig config) {
		CurriculumAutomationRule confirmedRule = new CurriculumAutomationRule();
		confirmedRule.setContext(AutomationContext.IMPLEMENTATION);
		confirmedRule.setAutomationType(AutomationType.STATUS_CHANGE);
		confirmedRule.setTargetStatus(CurriculumElementStatus.confirmed);
		confirmedRule.setEnabled(true);
		confirmedRule.setDependingOn(AutomationDependingOn.EXECUTION_PERIOD);
		confirmedRule.setReference(CurriculumAutomationRule.REFERENCE_BEGIN);
		confirmedRule.setValue(7);
		confirmedRule.setUnit(AutomationUnit.DAYS);
		confirmedRule.setDirection(OffsetDirection.BEFORE);
		confirmedRule.setOnlyWhenStatus(Set.of("preparation", "provisional"));
		config.addRule(confirmedRule);

		CurriculumAutomationRule finishedRule = new CurriculumAutomationRule();
		finishedRule.setContext(AutomationContext.IMPLEMENTATION);
		finishedRule.setAutomationType(AutomationType.STATUS_CHANGE);
		finishedRule.setTargetStatus(CurriculumElementStatus.finished);
		finishedRule.setEnabled(true);
		finishedRule.setDependingOn(AutomationDependingOn.EXECUTION_PERIOD);
		finishedRule.setReference(CurriculumAutomationRule.REFERENCE_END);
		finishedRule.setValue(null);
		finishedRule.setUnit(AutomationUnit.SAME_DAY);
		finishedRule.setDirection(OffsetDirection.AFTER);
		finishedRule.setOnlyWhenStatus(Set.of("confirmed"));
		config.addRule(finishedRule);
	}

	private static void addElementRules(CurriculumAutomationConfig config) {
		CurriculumAutomationRule activeRule = new CurriculumAutomationRule();
		activeRule.setContext(AutomationContext.ELEMENT);
		activeRule.setAutomationType(AutomationType.STATUS_CHANGE);
		activeRule.setTargetStatus(CurriculumElementStatus.active);
		activeRule.setEnabled(true);
		activeRule.setDependingOn(AutomationDependingOn.EXECUTION_PERIOD);
		activeRule.setReference(CurriculumAutomationRule.REFERENCE_BEGIN);
		activeRule.setValue(null);
		activeRule.setUnit(AutomationUnit.SAME_DAY);
		activeRule.setDirection(OffsetDirection.BEFORE);
		activeRule.setOnlyWhenStatus(Set.of("preparation"));
		config.addRule(activeRule);

		CurriculumAutomationRule finishedRule = new CurriculumAutomationRule();
		finishedRule.setContext(AutomationContext.ELEMENT);
		finishedRule.setAutomationType(AutomationType.STATUS_CHANGE);
		finishedRule.setTargetStatus(CurriculumElementStatus.finished);
		finishedRule.setEnabled(true);
		finishedRule.setDependingOn(AutomationDependingOn.EXECUTION_PERIOD);
		finishedRule.setReference(CurriculumAutomationRule.REFERENCE_END);
		finishedRule.setValue(null);
		finishedRule.setUnit(AutomationUnit.SAME_DAY);
		finishedRule.setDirection(OffsetDirection.AFTER);
		finishedRule.setOnlyWhenStatus(Set.of("active"));
		config.addRule(finishedRule);
	}

	private static void addContentRulesForImplementation(CurriculumAutomationConfig config, boolean implOnly) {
		String triggerStatus = implOnly ? "confirmed" : "active";

		CurriculumAutomationRule instantiationRule = new CurriculumAutomationRule();
		instantiationRule.setContext(AutomationContext.CONTENT);
		instantiationRule.setAutomationType(AutomationType.INSTANTIATION);
		instantiationRule.setEnabled(true);
		instantiationRule.setDependingOn(AutomationDependingOn.STATUS);
		instantiationRule.setDependingOnStatus(Set.of(triggerStatus));
		config.addRule(instantiationRule);

		CurriculumAutomationRule accessRule = new CurriculumAutomationRule();
		accessRule.setContext(AutomationContext.CONTENT);
		accessRule.setAutomationType(AutomationType.STATUS_CHANGE);
		accessRule.setTargetStatus(RepositoryEntryStatusEnum.coachpublished);
		accessRule.setEnabled(true);
		accessRule.setDependingOn(AutomationDependingOn.STATUS);
		accessRule.setDependingOnStatus(Set.of(triggerStatus));
		config.addRule(accessRule);

		CurriculumAutomationRule publishedRule = new CurriculumAutomationRule();
		publishedRule.setContext(AutomationContext.CONTENT);
		publishedRule.setAutomationType(AutomationType.STATUS_CHANGE);
		publishedRule.setTargetStatus(RepositoryEntryStatusEnum.published);
		publishedRule.setEnabled(true);
		publishedRule.setDependingOn(AutomationDependingOn.EXECUTION_PERIOD);
		publishedRule.setReference(CurriculumAutomationRule.REFERENCE_BEGIN);
		publishedRule.setValue(null);
		publishedRule.setUnit(AutomationUnit.SAME_DAY);
		publishedRule.setDirection(OffsetDirection.BEFORE);
		publishedRule.setOnlyWhenStatus(Set.of(triggerStatus));
		config.addRule(publishedRule);

		CurriculumAutomationRule contentFinishedRule = new CurriculumAutomationRule();
		contentFinishedRule.setContext(AutomationContext.CONTENT);
		contentFinishedRule.setAutomationType(AutomationType.STATUS_CHANGE);
		contentFinishedRule.setTargetStatus(RepositoryEntryStatusEnum.closed);
		contentFinishedRule.setEnabled(true);
		contentFinishedRule.setDependingOn(AutomationDependingOn.STATUS);
		contentFinishedRule.setDependingOnStatus(Set.of("cancelled", "finished"));
		config.addRule(contentFinishedRule);
	}
}
