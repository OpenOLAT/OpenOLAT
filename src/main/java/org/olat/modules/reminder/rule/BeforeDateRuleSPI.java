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

import org.olat.core.logging.OLog;
import org.olat.core.logging.Tracing;
import org.olat.core.util.Formatter;
import org.olat.core.util.StringHelper;
import org.olat.course.export.CourseEnvironmentMapper;
import org.olat.modules.reminder.ReminderRule;
import org.olat.modules.reminder.RuleEditorFragment;
import org.olat.modules.reminder.RuleSPI;
import org.olat.modules.reminder.model.ReminderRuleImpl;
import org.olat.modules.reminder.ui.BeforeDateRuleEditor;
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
	
	private static final OLog log = Tracing.createLoggerFor(BeforeDateRuleSPI.class);
	
	public static final String BEFORE = "<";

	@Override
	public String getLabelI18nKey() {
		return "rule.before.date";
	}

	@Override
	public String getCategory() {
		return "general";
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
			ReminderRuleImpl r = (ReminderRuleImpl)rule;
			String operator = r.getOperator();
			if(BEFORE.equals(operator) && StringHelper.containsNonWhitespace(r.getRightOperand())) {
				try {
					Date date = Formatter.parseDatetime(r.getRightOperand());
					Date now = new Date();
					allOk &= now.compareTo(date) <= 0;
				} catch (ParseException e) {
					log.error("", e);
				}
			}
		}

		return allOk;
	}

}
