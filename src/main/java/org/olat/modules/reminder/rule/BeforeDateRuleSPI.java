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
package org.olat.modules.reminder.rule;

import java.text.ParseException;
import java.util.Date;
import java.util.Locale;

import org.apache.logging.log4j.Logger;
import org.olat.core.gui.translator.Translator;
import org.olat.core.logging.Tracing;
import org.olat.core.util.Formatter;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
import org.olat.course.export.CourseEnvironmentMapper;
import org.olat.modules.reminder.ReminderRule;
import org.olat.modules.reminder.RuleEditorFragment;
import org.olat.modules.reminder.RuleSPI;
import org.olat.modules.reminder.model.ReminderRuleImpl;
import org.olat.modules.reminder.ui.BeforeDateRuleEditor;
import org.olat.modules.reminder.ui.ReminderAdminController;
import org.olat.repository.RepositoryEntry;
import org.springframework.stereotype.Service;

/**
 * 
 * Initial date: 22.10.2018<br>
 * @author Stephan Clemenz, clemenz@vcrp.de
 *
 */
@Service
public class BeforeDateRuleSPI implements RuleSPI {
	
	private static final Logger log = Tracing.createLoggerFor(BeforeDateRuleSPI.class);
	
	public static final String BEFORE = "<";

	@Override
	public int getSortValue() {
		return 200;
	}
	
	@Override
	public String getLabelI18nKey() {
		return "rule.before.date";
	}
	
	@Override
	public String getStaticText(ReminderRule rule, RepositoryEntry entry, Locale locale) {
		if (rule instanceof ReminderRuleImpl) {
			ReminderRuleImpl r = (ReminderRuleImpl)rule;
			Translator translator = Util.createPackageTranslator(ReminderAdminController.class, locale);
			String operator = r.getOperator();
			
			Date dueDate = null;
			if(BEFORE.equals(operator) && StringHelper.containsNonWhitespace(r.getRightOperand())) {
				try {
					dueDate = Formatter.parseDatetime(r.getRightOperand());
				} catch (ParseException e) {
					log.error("", e);
				}
			}
			
			String formattedDate = dueDate != null
					? Formatter.getInstance(locale).formatDateAndTime(dueDate)
					: translator.translate("missing.value");
			return translator.translate("rule.before.date.text", new String[] {formattedDate} );
			
		}
		return null;
	}

	@Override
	public RuleEditorFragment getEditorFragment(ReminderRule rule, RepositoryEntry entry) {
		return new BeforeDateRuleEditor(rule);
	}
	
	@Override
	public ReminderRule clone(ReminderRule rule, CourseEnvironmentMapper envMapper) {
		return rule.clone();
	}
	
	public boolean evaluate(ReminderRule rule) {
		boolean allOk = true;
		
		if(rule instanceof ReminderRuleImpl) {
			Date now = new Date();
			Date date = getDate(rule);
			
			if (date != null) {
				allOk &= now.compareTo(date) <= 0;
			}
		}

		return allOk;
	}
	
	@Override
	public boolean isDateDependant() {
		return true;
	}

	@Override
	public Date getDate(ReminderRule rule) {
		if(rule instanceof ReminderRuleImpl) {
			ReminderRuleImpl r = (ReminderRuleImpl)rule;
			String operator = r.getOperator();
			if(BEFORE.equals(operator) && StringHelper.containsNonWhitespace(r.getRightOperand())) {
				try {
					return Formatter.parseDatetime(r.getRightOperand());
				} catch (ParseException e) {
					log.error("", e);
				}
			}
		}
		
		return null;
	}
	
	@Override
	public ReminderRule moveDate(ReminderRule rule, CourseEnvironmentMapper envMapper, long dateDifferenceMilliSeconds) {
		ReminderRule copy = clone(rule, envMapper);
		Date date = getDate(rule);
		
		if (date != null) {
			date.setTime(date.getTime() + dateDifferenceMilliSeconds);
		}
		
		ReminderRuleImpl ruleImpl = (ReminderRuleImpl) copy;
		ruleImpl.setRightOperand(Formatter.formatDatetime(date));
		
		return ruleImpl;
	}
}
