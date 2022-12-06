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

import java.util.ArrayList;
import java.util.List;

import org.olat.core.CoreSpringFactory;
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
import org.olat.modules.reminder.rule.UserPropertyRuleSPI;
import org.olat.user.UserManager;
import org.olat.user.propertyhandlers.UserPropertyHandler;

/**
 * 
 * Initial date: 08.04.2015<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class UserPropertyEditor extends RuleEditorFragment {
	
	private TextElement valueEl;
	private SingleSelection propEl;
	
	private final List<UserPropertyHandler> userPropertyHandlers;
	
	public UserPropertyEditor(ReminderRule rule) {
		super(rule);
		
		UserManager userManager = CoreSpringFactory.getImpl(UserManager.class);
		userPropertyHandlers = new ArrayList<>(userManager.getUserPropertyHandlersFor(UserPropertyRuleSPI.USER_PROPS_ID, false));
	}

	@Override
	public FormItem initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		String id = Long.toString(CodeHelper.getRAMUniqueID());
		
		FormLayoutContainer userPropCont = uifactory
				.addInlineFormLayout("user.prop.".concat(id), null, formLayout);
		userPropCont.contextPut("id", id);
		
		Translator trans = Util.createPackageTranslator(UserPropertyHandler.class, ureq.getLocale(), formLayout.getTranslator());
		
		int numOfProperties = userPropertyHandlers.size();
		String[] propKeys = new String[numOfProperties];
		String[] propValues = new String[numOfProperties];
		
		for(int i=0; i<numOfProperties; i++) {
			UserPropertyHandler handler = userPropertyHandlers.get(i);
			propKeys[i] = handler.getName();
			propValues[i] = trans.translate(handler.i18nFormElementLabelKey());
		}
		
		String currentPropertyName = null;
		String currentPropertyValue = null;
		if(rule instanceof ReminderRuleImpl) {
			ReminderRuleImpl r = (ReminderRuleImpl)rule;
			currentPropertyName = r.getLeftOperand();
			currentPropertyValue = r.getRightOperand();
		}
		
		propEl = uifactory.addDropdownSingleselect("user.prop.".concat(id), null, userPropCont, propKeys, propValues, null);
		propEl.setDomReplacementWrapperRequired(false);
		if(currentPropertyName != null) {
			for(String propKey:propKeys) {
				if(currentPropertyName.equals(propKey)) {
					propEl.select(propKey, true);
				}
			}
		}

		valueEl = uifactory.addTextElement("user.value.".concat(id), null, 128, currentPropertyValue, userPropCont);
		valueEl.setDomReplacementWrapperRequired(false);

		return userPropCont;
	}

	@Override
	public boolean validateFormLogic(UserRequest ureq) {
		boolean allOk = true;
		
		propEl.clearError();
		if(!propEl.isOneSelected()) {
			propEl.setErrorKey("form.mandatory.hover");
			allOk &= false;
		}
		
		valueEl.clearError();
		if(!StringHelper.containsNonWhitespace(valueEl.getValue())) {
			valueEl.setErrorKey("form.mandatory.hover");
			allOk &= false;
		}
		
		return allOk;
	}

	@Override
	public ReminderRule getConfiguration() {
		ReminderRuleImpl configuredRule = null; 
		if(propEl.isOneSelected() && StringHelper.containsNonWhitespace(valueEl.getValue())) {
			configuredRule = new ReminderRuleImpl();
			configuredRule.setType(UserPropertyRuleSPI.class.getSimpleName());
			configuredRule.setLeftOperand(propEl.getSelectedKey());
			configuredRule.setOperator("=");
			configuredRule.setRightOperand(valueEl.getValue());
		}
		return configuredRule;
	}
}