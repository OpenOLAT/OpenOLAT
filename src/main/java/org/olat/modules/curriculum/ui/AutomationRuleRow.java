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
package org.olat.modules.curriculum.ui;

import java.util.Date;
import java.util.Set;

import org.olat.core.gui.components.date.OffsetDirection;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.elements.FormToggle;
import org.olat.modules.curriculum.AutomationContext;
import org.olat.modules.curriculum.AutomationDependingOn;
import org.olat.modules.curriculum.AutomationType;
import org.olat.modules.curriculum.AutomationUnit;
import org.olat.modules.curriculum.CurriculumAutomationConfig;
import org.olat.modules.curriculum.CurriculumAutomationRule;

/**
 * Initial date: 2026-06-26<br>
 * @author uhensler, https://www.frentix.com
 */
public class AutomationRuleRow {

	private final CurriculumAutomationConfig config;
	private final CurriculumAutomationRule rule;
	private Date plannedExecution;
	private FormToggle ruleEnabledEl;
	private FormLink toolsLink;

	public AutomationRuleRow(CurriculumAutomationConfig config) {
		this.config = config;
		this.rule = config.getRule();
	}

	public CurriculumAutomationConfig getConfig() {
		return config;
	}

	public CurriculumAutomationRule getRule() {
		return rule;
	}

	public AutomationContext getContext() {
		return rule.getContext();
	}

	public AutomationType getAutomationType() {
		return rule.getAutomationType();
	}

	public Object getTargetStatus() {
		return CurriculumAutomationRule.toStatusEnum(rule.getTargetStatus());
	}

	public boolean isEnabled() {
		return config.isEnabled();
	}

	public AutomationDependingOn getDependingOn() {
		return rule.getDependingOn();
	}

	public Integer getValue() {
		return rule.getValue();
	}

	public AutomationUnit getUnit() {
		return rule.getUnit();
	}

	public OffsetDirection getDirection() {
		return rule.getDirection();
	}

	public Set<String> getDependingOnStatus() {
		return rule.getDependingOnStatus();
	}

	public Set<String> getOnlyWhenStatus() {
		return rule.getOnlyWhenStatus();
	}

	public Date getPlannedExecution() {
		return plannedExecution;
	}

	public void setPlannedExecution(Date plannedExecution) {
		this.plannedExecution = plannedExecution;
	}

	public FormToggle getRuleEnabledEl() {
		return ruleEnabledEl;
	}

	public void setRuleEnabledEl(FormToggle ruleEnabledEl) {
		this.ruleEnabledEl = ruleEnabledEl;
	}

	public FormLink getToolsLink() {
		return toolsLink;
	}

	public void setToolsLink(FormLink toolsLink) {
		this.toolsLink = toolsLink;
	}
}
