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
import java.util.Date;

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
import org.olat.modules.reminder.ReminderRule;
import org.olat.modules.reminder.RuleEditorFragment;
import org.olat.modules.reminder.model.ReminderRuleImpl;
import org.olat.modules.reminder.rule.DateRuleSPI;

/**
 * 
 * Initial date: 07.04.2015<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class DateRuleEditor extends RuleEditorFragment {
	private static final Logger log = Tracing.createLoggerFor(DateRuleEditor.class);
	
	private DateChooser afterEl;
	
	public DateRuleEditor(ReminderRule rule) {
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
		
		Date after = null;
		if(rule instanceof ReminderRuleImpl) {
			ReminderRuleImpl ruleImpl = (ReminderRuleImpl)rule;
			try {
				after = Formatter.parseDatetime(ruleImpl.getRightOperand());
			} catch (ParseException e) {
				log.error("", e);
			}
		}
		
		afterEl = uifactory.addDateChooser("ruleElement.".concat(id), null, after, ruleCont);
		afterEl.setDateChooserTimeEnabled(true);
		afterEl.setValidDateCheck("form.error.date");
		return ruleCont;
	}

	@Override
	public boolean validateFormLogic(UserRequest ureq) {
		boolean allOk = true;
		
		if(!afterEl.validate()) {
			allOk &= false;
		} else if(afterEl.getDate() == null) {
			afterEl.setErrorKey("form.mandatory.hover", null);
			allOk &= false;
		}
		
		return allOk;
	}

	@Override
	public ReminderRule getConfiguration() {
		ReminderRuleImpl configuredRule = new ReminderRuleImpl();
		configuredRule.setType(DateRuleSPI.class.getSimpleName());
		configuredRule.setOperator(DateRuleSPI.AFTER);
		if(afterEl.getDate() != null) {
			configuredRule.setRightOperand(Formatter.formatDatetime(afterEl.getDate()));
		}
		return configuredRule;
	}

}
