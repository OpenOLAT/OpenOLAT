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
package org.olat.course.reminder.ui;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.SingleSelection;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.util.CodeHelper;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
import org.olat.course.reminder.rule.LearningProgressRuleSPI;
import org.olat.modules.reminder.ReminderRule;
import org.olat.modules.reminder.RuleEditorFragment;
import org.olat.modules.reminder.model.ReminderRuleImpl;

/**
 * 
 * Initial date: 3 Sep 2020<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class LearningProgressEditor extends RuleEditorFragment {
	
	private static final String[] operatorKeys = new String[]{ "<", "<=", "=", "=>", ">"};
	
	private TextElement valueEl;
	private SingleSelection operatorEl;
	
	public LearningProgressEditor(ReminderRule rule) {
		super(rule);
	}

	@Override
	public FormItem initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		String page = Util.getPackageVelocityRoot(this.getClass()) + "/learning_progress.html";
		String id = Long.toString(CodeHelper.getRAMUniqueID());
		
		FormLayoutContainer ruleCont = uifactory
				.addCustomFormLayout("attempts.".concat(id), null, page, formLayout);
		ruleCont.contextPut("id", id);
		
		String currentValue = null;
		String currentOperator = null;
		
		if(rule instanceof ReminderRuleImpl) {
			ReminderRuleImpl r = (ReminderRuleImpl)rule;
			currentOperator = r.getOperator();
			currentValue = r.getRightOperand();
		}

		operatorEl = uifactory.addDropdownSingleselect("operators.".concat(id), null, ruleCont, operatorKeys, operatorKeys, null);
		operatorEl.setDomReplacementWrapperRequired(false);
		boolean opSelected = false;
		if(currentOperator != null) {
			for(String operatorKey:operatorKeys) {
				if(currentOperator.equals(operatorKey)) {
					operatorEl.select(operatorKey, true);
					opSelected = true;
				}
			}
		}
		if(!opSelected) {
			operatorEl.select(operatorKeys[4], true);
		}

		valueEl = uifactory.addTextElement("value.".concat(id), null, 128, currentValue, ruleCont);
		valueEl.setDomReplacementWrapperRequired(false);
		valueEl.setDisplaySize(3);
		
		return ruleCont;
	}

	@Override
	public boolean validateFormLogic(UserRequest ureq) {
		boolean allOk = true;
		
		operatorEl.clearError();
		if(!operatorEl.isOneSelected()) {
			operatorEl.setErrorKey("form.mandatory.hover");
			allOk &= false;
		}
		
		allOk &= validateValue(valueEl);
		
		return allOk;
	}
	
	private boolean validateValue(TextElement el) {
		boolean allOk = true;
		
		valueEl.clearError();
		if (el.isVisible()) {
			String value = el.getValue();
			if (StringHelper.containsNonWhitespace(value)) {
				try {
					int parsedInt = Integer.parseInt(value);
					if (parsedInt < 0 || parsedInt > 100) {
						allOk = false;
						el.setErrorKey("error.int.between", "0", "100");
					}
				} catch(Exception e) {
					allOk = false;
					el.setErrorKey("error.int.between", "0", "100");
				}
			} else {
				valueEl.setErrorKey("form.mandatory.hover");
				allOk = false;
			}
		}

		return allOk;
	}

	@Override
	public ReminderRule getConfiguration() {
		ReminderRuleImpl configuredRule = new ReminderRuleImpl();
		configuredRule.setType(LearningProgressRuleSPI.class.getSimpleName());
		configuredRule.setOperator(operatorEl.getSelectedKey());
		configuredRule.setRightOperand(valueEl.getValue());
		return configuredRule;
	}
}
