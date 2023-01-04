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

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

import org.apache.logging.log4j.Logger;
import org.olat.basesecurity.GroupRoles;
import org.olat.core.gui.translator.Translator;
import org.olat.core.id.Identity;
import org.olat.core.logging.Tracing;
import org.olat.core.util.Util;
import org.olat.course.CourseFactory;
import org.olat.course.export.CourseEnvironmentMapper;
import org.olat.course.reminder.manager.ReminderRuleDAO;
import org.olat.course.reminder.ui.CourseReminderListController;
import org.olat.modules.reminder.IdentitiesProviderRuleSPI;
import org.olat.modules.reminder.ReminderRule;
import org.olat.modules.reminder.RuleEditorFragment;
import org.olat.modules.reminder.model.ReminderRuleImpl;
import org.olat.modules.reminder.rule.LaunchUnit;
import org.olat.modules.reminder.ui.RepositoryEntryLifecycleAfterValidRuleEditor;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryEntryRelationType;
import org.olat.repository.manager.RepositoryEntryRelationDAO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 
 * Initial date: 4 Jan 2023<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
@Service
public class NextRecertificationDateSPI implements IdentitiesProviderRuleSPI {
	
	private static final Logger log = Tracing.createLoggerFor(NextRecertificationDateSPI.class);
	
	@Autowired
	private ReminderRuleDAO helperDao;
	@Autowired
	private RepositoryEntryRelationDAO repositoryEntryRelationDao;

	@Override
	public String getLabelI18nKey() {
		return "rule.recertificate.date";
	}

	@Override
	public int getSortValue() {
		return 9;
	}
	
	@Override
	public boolean isEnabled(RepositoryEntry entry) {
		return CourseFactory.loadCourse(entry).getCourseConfig().isCertificateEnabled();
	}

	@Override
	public String getStaticText(ReminderRule rule, RepositoryEntry entry, Locale locale) {
		if (rule instanceof ReminderRuleImpl) {
			ReminderRuleImpl r = (ReminderRuleImpl)rule;
			Translator translator = Util.createPackageTranslator(CourseReminderListController.class, locale);
			String currentUnit = r.getRightUnit();
			String currentValue = r.getRightOperand();
			
			if (currentValue == null) {
				return null;
			}
			String i18nBeforeAfter = "after.";
			if (currentValue.startsWith("-")) {
				i18nBeforeAfter = "before.";
				currentValue = currentValue.substring(1);
			}
			
			try {
				LaunchUnit.valueOf(currentUnit);
			} catch (Exception e) {
				return null;
			}
			
			String[] args = new String[] { currentValue };
			return translator.translate("rule.recertificate.date." + i18nBeforeAfter + currentUnit, args);
		}
		return null;
	}

	@Override
	public ReminderRule clone(ReminderRule rule, CourseEnvironmentMapper envMapper) {
		return rule.clone();
	}

	@Override
	public RuleEditorFragment getEditorFragment(ReminderRule rule, RepositoryEntry entry) {
		return new RepositoryEntryLifecycleAfterValidRuleEditor(rule, this.getClass().getSimpleName());
	}

	@Override
	public List<Identity> evaluate(RepositoryEntry entry, ReminderRule rule) {
		if(rule instanceof ReminderRuleImpl) {
			ReminderRuleImpl r = (ReminderRuleImpl)rule;
			if (!CourseFactory.loadCourse(entry).getCourseConfig().isCertificateEnabled()) {
				log.warn("Course {} ({}) has certificate not enabled.", entry.getKey(), entry.getDisplayname());
				return new ArrayList<>(0);
			}
			
			int distance = Integer.parseInt(r.getRightOperand());
			LaunchUnit unit = LaunchUnit.valueOf(r.getRightUnit());
			Date referenceDate = getDate(new Date(), distance, unit);
			List<Long> recertIdentityKeys = helperDao.getNextRecertificationBefore(entry, referenceDate);
			
			List<Identity> identities = repositoryEntryRelationDao.getMembers(entry, RepositoryEntryRelationType.all,
					GroupRoles.participant.name());
			for(Iterator<Identity> identityIt=identities.iterator(); identityIt.hasNext(); ) {
				Identity identity = identityIt.next();
				if(!recertIdentityKeys.contains(identity.getKey())) {
					identityIt.remove();
				}
			}
		}
		return new ArrayList<>(0);
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
