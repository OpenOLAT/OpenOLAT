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

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.SingleSelection;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.translator.Translator;
import org.olat.core.util.CodeHelper;
import org.olat.modules.reminder.ReminderRule;
import org.olat.modules.reminder.RuleEditorFragment;
import org.olat.modules.reminder.model.ReminderRuleImpl;
import org.olat.modules.reminder.rule.RepositoryEntryRoleRuleSPI;
import org.olat.modules.reminder.rule.RepositoryEntryRoleRuleSPI.Roles;

/**
 * 
 * Initial date: 07.04.2015<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class RepositoryEntryRoleEditor extends RuleEditorFragment {

	private static final String[] keys = new String[]{
		Roles.owner.name(), Roles.coach.name(), Roles.participant.name(),
		Roles.participantAndCoach.name(), Roles.ownerAndCoach.name(), Roles.all.name()
	};
	
	private SingleSelection roleEl;
	
	public RepositoryEntryRoleEditor(ReminderRule rule) {
		super(rule);
	}

	@Override
	public FormItem initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		Translator translator = formLayout.getTranslator();
		
		String[] values = new String[] {
				translator.translate("course.role.owner"), translator.translate("course.role.coach"),
				translator.translate("course.role.participant"), translator.translate("course.role.participantAndCoach"),
				translator.translate("course.role.ownerAndCoach"), translator.translate("course.role.all")
		};
		
		roleEl = uifactory.addDropdownSingleselect("role." + CodeHelper.getRAMUniqueID(), null, formLayout,
				keys, values, null);
		
		boolean selected = false;
		if(rule instanceof ReminderRuleImpl) {
			ReminderRuleImpl roleRule = (ReminderRuleImpl)rule;
			String role = roleRule.getRightOperand();
			for(String key:keys) {
				if(key.equals(role)) {
					roleEl.select(key, true);
					selected = true;
				}
			}
		}
		
		if(!selected) {
			roleEl.select(keys[0], true);
		}
		
		return roleEl;
	}

	@Override
	public boolean validateFormLogic(UserRequest ureq) {
		boolean allOk = true;
		
		roleEl.clearError();
		if(!roleEl.isOneSelected()) {
			roleEl.setErrorKey("form.mandatory.hover", null);
			allOk &= false;
		}
		
		return allOk;
	}

	@Override
	public ReminderRule getConfiguration() {
		ReminderRuleImpl configuredRule = null;
		if(roleEl.isOneSelected()) {
			configuredRule = new ReminderRuleImpl();
			configuredRule.setType(RepositoryEntryRoleRuleSPI.class.getSimpleName());
			configuredRule.setOperator("=");
			configuredRule.setRightOperand(roleEl.getSelectedKey());
		}
		return configuredRule;
	}


}