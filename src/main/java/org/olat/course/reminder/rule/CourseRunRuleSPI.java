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
package org.olat.course.reminder.rule;

import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.olat.core.gui.translator.Translator;
import org.olat.core.id.Identity;
import org.olat.core.util.Util;
import org.olat.course.assessment.manager.UserCourseInformationsManager;
import org.olat.course.export.CourseEnvironmentMapper;
import org.olat.course.reminder.ui.CourseRunEditor;
import org.olat.course.reminder.ui.LearningProgressEditor;
import org.olat.modules.reminder.FilterRuleSPI;
import org.olat.modules.reminder.ReminderRule;
import org.olat.modules.reminder.RuleEditorFragment;
import org.olat.modules.reminder.model.ReminderRuleImpl;
import org.olat.repository.RepositoryEntry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 
 * Initial date: 21 mars 2023<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
@Service
public class CourseRunRuleSPI implements FilterRuleSPI {

	@Autowired
	private UserCourseInformationsManager userCourseInformationsManager;
	
	@Override
	public int getSortValue() {
		return 6;
	}
	
	@Override
	public String getLabelI18nKey() {
		return "rule.course.run";
	}
	
	@Override
	public ReminderRule clone(ReminderRule rule, CourseEnvironmentMapper envMapper) {
		return rule.clone();
	}

	@Override
	public String getStaticText(ReminderRule rule, RepositoryEntry entry, Locale locale) {
		if (rule instanceof ReminderRuleImpl r) {
			Translator translator = Util.createPackageTranslator(LearningProgressEditor.class, locale);
			String operator = r.getOperator();
			String percent = r.getRightOperand();
			switch(operator) {
				case "<": return translator.translate("rule.course.run.less", percent);
				case "<=": return translator.translate("rule.course.run.less.equals", percent);
				case "=": return translator.translate("rule.course.run.equals", percent);
				case "=>": return translator.translate("rule.course.run.greater.equals", percent);
				case ">": return translator.translate("rule.course.run.greater", percent);
			}
		}
		return null;
	}

	@Override
	public RuleEditorFragment getEditorFragment(ReminderRule rule, RepositoryEntry entry) {
		return new CourseRunEditor(rule);
	}

	@Override
	public void filter(RepositoryEntry entry, List<Identity> identities, ReminderRule rule) {
		if(rule instanceof ReminderRuleImpl r) {
			String operator = r.getOperator();
			long value = Long.parseLong(r.getRightOperand());
			Map<Long,Long> courseRuns = userCourseInformationsManager.getCourseRuns(entry.getOlatResource(), identities);
	
			for(Iterator<Identity> identityIt=identities.iterator(); identityIt.hasNext(); ) {
				Identity identity = identityIt.next();
				
				long run = 1;
				Long savedRun = courseRuns.get(identity.getKey());
				if(savedRun != null) {
					run = savedRun.longValue();
				}
				
				if(!evaluateRun(run, operator, value)) {
					identityIt.remove();
				}
			}
		}
	}
	
	private boolean evaluateRun(long run, String operator, long value) {
		boolean eval = false;
		switch(operator) {
			case "<": eval = run < value; break;
			case "<=": eval = run <= value; break;
			case "=": eval = run == value; break;
			case "=>": eval = run >= value; break;
			case ">": eval = run > value;  break;
			case "!=": eval = run != value; break;
			default: eval = false; break;
		}
		return eval;
	}
}
