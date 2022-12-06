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
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.translator.Translator;
import org.olat.core.util.CodeHelper;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
import org.olat.modules.reminder.ReminderRule;
import org.olat.modules.reminder.RuleEditorFragment;
import org.olat.modules.reminder.model.ReminderRuleImpl;
import org.olat.modules.reminder.rule.LaunchUnit;

/**
 * 
 * Initial date: 26.05.2015<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class RepositoryEntryLifecycleAfterValidRuleEditor extends RuleEditorFragment {
	
	private static final String[] unitKeys = new String[]{
		LaunchUnit.day.name(), LaunchUnit.week.name(), LaunchUnit.month.name(), LaunchUnit.year.name()
	};
	
	private TextElement valueEl;
	private SingleSelection unitEl;
	
	private final String ruleType;
	private final String templateName;
	
	public RepositoryEntryLifecycleAfterValidRuleEditor(ReminderRule rule, String ruleType, String templateName) {
		super(rule);
		this.ruleType = ruleType;
		this.templateName = templateName;
	}

	@Override
	public FormItem initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		
		String page = Util.getPackageVelocityRoot(this.getClass()) + templateName;
		String id = Long.toString(CodeHelper.getRAMUniqueID());
		
		Translator trans = formLayout.getTranslator();
		FormLayoutContainer ruleCont = uifactory
				.addCustomFormLayout("repo.valid.".concat(id), null, page, formLayout);
		ruleCont.contextPut("id", id);
		
		String currentValue = null;
		String currentUnit = null;
		if(rule instanceof ReminderRuleImpl) {
			ReminderRuleImpl r = (ReminderRuleImpl)rule;
			currentValue = r.getRightOperand();
			currentUnit = r.getRightUnit();
		}

		valueEl = uifactory.addTextElement("launchvalue".concat(id), null, 128, currentValue, ruleCont);
		valueEl.setDomReplacementWrapperRequired(false);
		valueEl.setDisplaySize(3);
		
		String[] unitValues = new String[] {
				trans.translate(LaunchUnit.day.name()), trans.translate(LaunchUnit.week.name()),
				trans.translate(LaunchUnit.month.name()), trans.translate(LaunchUnit.year.name())
		};
		
		unitEl = uifactory.addDropdownSingleselect("launchunit".concat(id), null, ruleCont, unitKeys, unitValues, null);
		unitEl.setDomReplacementWrapperRequired(false);
		boolean unitSelected = false;
		if(currentUnit != null) {
			for(String unitKey:unitKeys) {
				if(currentUnit.equals(unitKey)) {
					unitEl.select(unitKey, true);
					unitSelected = true;
				}
			}
		}
		if(!unitSelected) {
			unitEl.select(unitKeys[1], true);	
		}

		return ruleCont;
	}

	@Override
	public boolean validateFormLogic(UserRequest ureq) {
		boolean allOk = true;
		
		unitEl.clearError();
		if(!unitEl.isOneSelected()) {
			unitEl.setErrorKey("form.mandatory.hover", null);
			allOk &= false;
		}
		
		valueEl.clearError();
		if(!StringHelper.containsNonWhitespace(valueEl.getValue())) {
			valueEl.setErrorKey("form.mandatory.hover", null);
			allOk &= false;
		} else {
			allOk &= validateInt(valueEl);
		}
		
		return allOk;
	}
	
	private boolean validateInt(TextElement el) {
		boolean allOk = true;
		
		if(el.isVisible()) {
			String value = el.getValue();
			if(StringHelper.containsNonWhitespace(value)) {
				try {
					Integer.parseInt(value);
				} catch(Exception e) {
					allOk = false;
					el.setErrorKey("error.wrong.int", null);
				}
			}
		}

		return allOk;
	}

	@Override
	public ReminderRule getConfiguration() {
		ReminderRuleImpl configuredRule = new ReminderRuleImpl();
		configuredRule.setType(ruleType);
		configuredRule.setOperator(">");
		configuredRule.setRightOperand(valueEl.getValue());
		configuredRule.setRightUnit(unitEl.getSelectedKey());
		return configuredRule;
	}
}