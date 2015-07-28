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

import java.util.List;

import org.olat.core.CoreSpringFactory;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.SingleSelection;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.util.CodeHelper;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
import org.olat.group.BusinessGroup;
import org.olat.group.BusinessGroupService;
import org.olat.group.model.SearchBusinessGroupParams;
import org.olat.modules.reminder.ReminderRule;
import org.olat.modules.reminder.RuleEditorFragment;
import org.olat.modules.reminder.model.ReminderRuleImpl;
import org.olat.modules.reminder.rule.BusinessGroupRoleRuleSPI;
import org.olat.repository.RepositoryEntry;

/**
 * 
 * Initial date: 07.04.2015<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class BusinessGroupRoleEditor extends RuleEditorFragment {

	private SingleSelection groupEl;
	
	private List<BusinessGroup> businessGroups;
	
	public BusinessGroupRoleEditor(ReminderRule rule, RepositoryEntry entry) {
		super(rule);
		
		BusinessGroupService businessGroupService = CoreSpringFactory.getImpl(BusinessGroupService.class);
		SearchBusinessGroupParams params = new SearchBusinessGroupParams();
		businessGroups = businessGroupService.findBusinessGroups(params, entry, 0, -1);
	}

	@Override
	public FormItem initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		String id = Long.toString(CodeHelper.getRAMUniqueID());
		String page = Util.getPackageVelocityRoot(this.getClass()) + "/rule_1_element.html";
		FormLayoutContainer ruleCont = FormLayoutContainer
				.createCustomFormLayout(".".concat(id), formLayout.getTranslator(), page);
		ruleCont.setRootForm(formLayout.getRootForm());
		formLayout.add(ruleCont);
		ruleCont.getFormItemComponent().contextPut("id", id);
		
		String currentKey = null;
		if(rule instanceof ReminderRuleImpl) {
			ReminderRuleImpl roleRule = (ReminderRuleImpl)rule;
			currentKey = roleRule.getRightOperand();
		}
		
		String[] keys = new String[businessGroups.size()];
		String[] values = new String[businessGroups.size()];
		int count = 0;
		String selectedKey = null;
		for(BusinessGroup businessGroup:businessGroups) {
			String key = Integer.toString(count);
			if(currentKey != null && businessGroup.getKey().toString().equals(currentKey)) {
				selectedKey = key;
			}
			
			keys[count] = key;
			values[count++] = businessGroup.getName();
		}
		
		groupEl = uifactory.addDropdownSingleselect("ruleElement.".concat(id), null, ruleCont, keys, values, null);
		if(selectedKey != null) {
			groupEl.select(selectedKey, true);
		}
		if(StringHelper.containsNonWhitespace(currentKey) && selectedKey == null) {
			groupEl.setErrorKey("error.group.not.found", null);
		}
		return ruleCont;
	}

	@Override
	public boolean validateFormLogic(UserRequest ureq) {
		boolean allOk = true;
		
		groupEl.clearError();
		if(!groupEl.isOneSelected()) {
			groupEl.setErrorKey("form.mandatory.hover", null);
			allOk &= false;
		}
	
		return allOk;
	}

	@Override
	public ReminderRule getConfiguration() {
		ReminderRuleImpl configuredRule = null;
		if(groupEl.isOneSelected()) {
			
			String selectedKey = groupEl.getSelectedKey();
			int index = Integer.parseInt(selectedKey);
			if(index >= 0 && index < businessGroups.size()) {
				BusinessGroup businessGroup = businessGroups.get(index);
				
				configuredRule = new ReminderRuleImpl();
				configuredRule.setType(BusinessGroupRoleRuleSPI.class.getSimpleName());
				configuredRule.setOperator("=");
				configuredRule.setRightOperand(businessGroup.getKey().toString());
			}
		}
		return configuredRule;
	}
	
	public enum Roles {
		coach,
		participant,
		all
	}
}