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

import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.olat.core.gui.translator.Translator;
import org.olat.core.id.Identity;
import org.olat.core.util.Util;
import org.olat.course.assessment.manager.UserCourseInformationsManager;
import org.olat.course.export.CourseEnvironmentMapper;
import org.olat.modules.reminder.ReminderRule;
import org.olat.modules.reminder.model.ReminderRuleImpl;
import org.olat.modules.reminder.ui.CourseLaunchRuleEditor;
import org.olat.repository.RepositoryEntry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 
 * Initial date: 09.04.2015<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
@Service
public class InitialCourseLaunchRuleSPI extends AbstractLaunchDateRuleSPI {
	
	@Autowired
	private UserCourseInformationsManager userCourseInformationsManager;
	
	@Override
	public int getSortValue() {
		return 4;
	}
	
	@Override
	public String getLabelI18nKey() {
		return "rule.initial.course.launch.date";
	}
	
	@Override
	public String getStaticText(ReminderRule rule, RepositoryEntry entry, Locale locale) {
		if (rule instanceof ReminderRuleImpl) {
			ReminderRuleImpl r = (ReminderRuleImpl)rule;
			Translator translator = Util.createPackageTranslator(CourseLaunchRuleEditor.class, locale);
			String currentUnit = r.getRightUnit();
			String currentValue = r.getRightOperand();
			
			try {
				LaunchUnit.valueOf(currentUnit);
			} catch (Exception e) {
				return null;
			}
			
			String[] args = new String[] { currentValue };
			return translator.translate("rule.initial.course.launch.date." + currentUnit, args);
		}
		return null;
	}
	
	@Override
	public ReminderRule clone(ReminderRule rule, CourseEnvironmentMapper envMapper) {
		return rule.clone();
	}

	@Override
	protected Map<Long, Date> getLaunchDates(ReminderRule rule, RepositoryEntry entry, List<Identity> identities) {
		return userCourseInformationsManager.getInitialLaunchDates(entry.getOlatResource(), identities);
	}
}