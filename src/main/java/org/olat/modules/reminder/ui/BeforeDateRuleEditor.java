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

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.logging.log4j.Logger;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.DateChooser;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.logging.Tracing;
import org.olat.core.util.CodeHelper;
import org.olat.core.util.Formatter;
import org.olat.core.util.Util;
import org.olat.core.util.ValidationStatus;
import org.olat.modules.reminder.ReminderRule;
import org.olat.modules.reminder.RuleEditorFragment;
import org.olat.modules.reminder.model.ReminderRuleImpl;
import org.olat.modules.reminder.rule.BeforeDateRuleSPI;

/**
 * 
 * Initial date: 22.10.2018<br>
 * @author Stephan Clemenz, clemenz@vcrp.de
 *
 */
public class BeforeDateRuleEditor extends RuleEditorFragment {
	private static final Logger log = Tracing.createLoggerFor(BeforeDateRuleEditor.class);
	
	private DateChooser beforeEl;
	
	public BeforeDateRuleEditor(ReminderRule rule) {
		super(rule);
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
		
		Date before = null;
		if(rule instanceof ReminderRuleImpl) {
			ReminderRuleImpl ruleImpl = (ReminderRuleImpl)rule;
			try {
				before = Formatter.parseDatetime(ruleImpl.getRightOperand());
			} catch (ParseException e) {
				log.error("", e);
			}
		}
		
		beforeEl = uifactory.addDateChooser("ruleElement.".concat(id), null, before, ruleCont);
		beforeEl.setDateChooserTimeEnabled(true);
		beforeEl.setValidDateCheck("form.error.date");
		return ruleCont;
	}

	@Override
	public boolean validateFormLogic(UserRequest ureq) {
		boolean allOk = true;
		
		beforeEl.clearError();
		List<ValidationStatus> validationResults = new ArrayList<>();
		beforeEl.validate(validationResults);
		if(!validationResults.isEmpty()) {
			allOk &= false;
		} else if(beforeEl.getDate() == null) {
			beforeEl.setErrorKey("form.mandatory.hover", null);
			allOk &= false;
		}
		
		return allOk;
	}

	@Override
	public ReminderRule getConfiguration() {
		ReminderRuleImpl configuredRule = new ReminderRuleImpl();
		configuredRule.setType(BeforeDateRuleSPI.class.getSimpleName());
		configuredRule.setOperator(BeforeDateRuleSPI.BEFORE);
		if(beforeEl.getDate() != null) {
			configuredRule.setRightOperand(Formatter.formatDatetime(beforeEl.getDate()));
		}
		return configuredRule;
	}

}
