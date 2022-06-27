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

import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import org.hibernate.LazyInitializationException;
import org.olat.core.CoreSpringFactory;
import org.olat.core.gui.translator.Translator;
import org.olat.core.util.Util;
import org.olat.course.export.CourseEnvironmentMapper;
import org.olat.modules.reminder.ReminderRule;
import org.olat.modules.reminder.RepositoryEntryRuleSPI;
import org.olat.modules.reminder.RuleEditorFragment;
import org.olat.modules.reminder.model.ReminderRuleImpl;
import org.olat.modules.reminder.ui.RepositoryEntryLifecycleAfterValidRuleEditor;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryService;
import org.olat.repository.model.RepositoryEntryLifecycle;
import org.springframework.stereotype.Service;


/**
 * 
 * Initial date: 26.05.2015<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
@Service
public class RepositoryEntryLifecycleAfterValidFromRuleSPI implements RepositoryEntryRuleSPI  {

	@Override
	public String getLabelI18nKey() {
		return "rule.lifecycle.validfrom";
	}
	
	@Override
	public int getSortValue() {
		return 2;
	}
	
	@Override
	public ReminderRule clone(ReminderRule rule, CourseEnvironmentMapper envMapper) {
		return rule.clone();
	}
	
	@Override
	public String getStaticText(ReminderRule rule, RepositoryEntry entry, Locale locale) {
		if (rule instanceof ReminderRuleImpl) {
			ReminderRuleImpl r = (ReminderRuleImpl)rule;
			Translator translator = Util.createPackageTranslator(RepositoryEntryLifecycleAfterValidRuleEditor.class, locale);
			String currentUnit = r.getRightUnit();
			String currentValue = r.getRightOperand();
			
			try {
				LaunchUnit.valueOf(currentUnit);
			} catch (Exception e) {
				return null;
			}
			
			String[] args = new String[] { currentValue };
			return translator.translate("rule.lifecycle.validfrom." + currentUnit, args);
		}
		return null;
	}
	
	@Override
	public RuleEditorFragment getEditorFragment(ReminderRule rule, RepositoryEntry entry) {
		return new RepositoryEntryLifecycleAfterValidRuleEditor(rule, this.getClass().getSimpleName(), "/repo_valid_from.html");
	}

	@Override
	public boolean evaluate(RepositoryEntry entry, ReminderRule rule) {
		boolean allOk = true;
		if(rule instanceof ReminderRuleImpl) {
			RepositoryEntryLifecycle lifecycle = getRepositoryEntryLifecycle(entry);
			if(lifecycle != null && lifecycle.getValidFrom() != null) {
				allOk &= evaluate(lifecycle, rule);
			} else {
				allOk &= false;
			}
		}
		return allOk;
	}
	
	public boolean evaluate(RepositoryEntryLifecycle lifecycle, ReminderRule rule) {
		Date now = cleanNow();
		Date validTo = lifecycle.getValidTo();
		if(validTo != null && now.compareTo(validTo) >= 0) {
			return false;//the course is at the end
		}

		ReminderRuleImpl r = (ReminderRuleImpl)rule;
		int distance = Integer.parseInt(r.getRightOperand());
		LaunchUnit unit = LaunchUnit.valueOf(r.getRightUnit());
		Date referenceDate = getDate(lifecycle.getValidFrom(), distance, unit);
		return now.compareTo(referenceDate) >= 0;
	}
	
	private RepositoryEntryLifecycle getRepositoryEntryLifecycle(RepositoryEntry entry) {
		try {
			RepositoryEntryLifecycle lifecycle = entry.getLifecycle();
			if(lifecycle != null) {
				lifecycle.getValidTo();
			}
			return lifecycle;
		} catch (LazyInitializationException e) {
			RepositoryEntry reloadedEntry = CoreSpringFactory.getImpl(RepositoryService.class)
					.loadByKey(entry.getKey());
			return reloadedEntry.getLifecycle();
		}
	}

	private Date cleanNow() {
		Calendar cal = Calendar.getInstance();
		cal.setTime(new Date());
		cal.set(Calendar.HOUR_OF_DAY, 0);
		cal.set(Calendar.MINUTE, 0);
		cal.set(Calendar.SECOND, 0);
		cal.set(Calendar.MILLISECOND, 0);
		return cal.getTime();
	}
	
	private Date getDate(Date date, int distance, LaunchUnit unit) {
		Calendar cal = Calendar.getInstance();
		cal.setTime(date);
		switch(unit) {
			case day:
				cal.add(Calendar.DATE, distance);
				break;
			case week:
				cal.add(Calendar.DATE, 7 * distance);
				break;
			case month:
				cal.add(Calendar.MONTH, distance);
				break;
			case year:
				cal.add(Calendar.YEAR, distance);
				break;
		}
		cal.set(Calendar.HOUR_OF_DAY, 0);
		cal.set(Calendar.MINUTE, 0);
		cal.set(Calendar.SECOND, 0);
		cal.set(Calendar.MILLISECOND, 0);
		return cal.getTime();
	}
}