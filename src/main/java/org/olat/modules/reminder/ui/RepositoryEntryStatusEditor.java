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
package org.olat.modules.reminder.ui;

import static org.olat.core.gui.components.util.SelectionValues.entry;

import java.util.Arrays;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.SingleSelection;
import org.olat.core.gui.components.util.SelectionValues;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.translator.Translator;
import org.olat.core.util.CodeHelper;
import org.olat.modules.reminder.ReminderRule;
import org.olat.modules.reminder.RuleEditorFragment;
import org.olat.modules.reminder.model.ReminderRuleImpl;
import org.olat.modules.reminder.rule.RepositoryEntryStatusRuleSPI;

/**
 * 
 * Initial date: 28 May 2024<br>
 * @author uhensler, urs.hensler@frentix.com, https://www.frentix.com
 *
 */
public class RepositoryEntryStatusEditor extends RuleEditorFragment {
	
	private SingleSelection statusEl;
	
	public RepositoryEntryStatusEditor(ReminderRule rule) {
		super(rule);
	}

	@Override
	public FormItem initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		Translator translator = formLayout.getTranslator();
		
		SelectionValues statusSV = new SelectionValues();
		Arrays.stream(RepositoryEntryStatusRuleSPI.ALL_STATUS).forEach(
				status -> statusSV.add(entry(
							status.name(),
							translator.translate("rule.repository.status." + status.name()))));
		statusEl = uifactory.addDropdownSingleselect("status." + CodeHelper.getRAMUniqueID(), null, formLayout,
				statusSV.keys(), statusSV.values(), null);
		
		boolean selected = false;
		if (rule instanceof ReminderRuleImpl roleRule) {
			String statusKey = roleRule.getRightOperand();
			if (Arrays.asList(statusEl.getKeys()).contains(statusKey)) {
				statusEl.select(statusKey, true);
				selected = true;
			}
		}
		
		if (!selected) {
			statusEl.select(statusEl.getKeys()[0], true);
		}
		
		return statusEl;
	}

	@Override
	public boolean validateFormLogic(UserRequest ureq) {
		boolean allOk = true;
		
		statusEl.clearError();
		if (!statusEl.isOneSelected()) {
			statusEl.setErrorKey("form.mandatory.hover");
			allOk &= false;
		}
		
		return allOk;
	}

	@Override
	public ReminderRule getConfiguration() {
		ReminderRuleImpl configuredRule = null;
		if (statusEl.isOneSelected()) {
			configuredRule = new ReminderRuleImpl();
			configuredRule.setType(RepositoryEntryStatusRuleSPI.class.getSimpleName());
			configuredRule.setOperator("=");
			configuredRule.setRightOperand(statusEl.getSelectedKey());
		}
		return configuredRule;
	}

}