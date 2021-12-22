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
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.olat.core.CoreSpringFactory;
import org.olat.core.gui.translator.Translator;
import org.olat.core.id.Identity;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
import org.olat.course.CourseFactory;
import org.olat.course.ICourse;
import org.olat.course.assessment.manager.UserCourseInformationsManager;
import org.olat.course.duedate.DueDateConfig;
import org.olat.course.duedate.DueDateService;
import org.olat.course.duedate.RelativeDueDateConfig;
import org.olat.course.duedate.ui.DueDateConfigFormatter;
import org.olat.course.learningpath.manager.LearningPathNodeAccessProvider;
import org.olat.course.nodeaccess.NodeAccessType;
import org.olat.course.nodes.CourseNode;
import org.olat.course.nodes.GTACourseNode;
import org.olat.course.nodes.form.ui.FormBeforeDueDateRuleEditor;
import org.olat.course.nodes.gta.ui.GTARunController;
import org.olat.modules.assessment.AssessmentService;
import org.olat.modules.assessment.model.AssessmentObligation;
import org.olat.modules.reminder.IdentitiesProviderRuleSPI;
import org.olat.modules.reminder.ReminderRule;
import org.olat.modules.reminder.model.ReminderRuleImpl;
import org.olat.modules.reminder.rule.LaunchUnit;
import org.olat.modules.reminder.ui.ReminderAdminController;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryService;
import org.olat.repository.model.RepositoryEntryLifecycle;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 30 Apr 2021<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public abstract class AbstractDueDateRuleSPI implements IdentitiesProviderRuleSPI {
	
	@Autowired
	protected DueDateService dueDateService;
	@Autowired
	protected AssessmentService assessmentService;

	protected abstract String getStaticTextPrefix();
	
	protected abstract DueDateConfig getDueDateConfig(CourseNode courseNode);
	
	protected abstract List<Identity> getPeopleToRemind(RepositoryEntry entry, CourseNode courseNode);
	
	@Override
	public String getStaticText(ReminderRule rule, RepositoryEntry entry, Locale locale) {
		if (rule instanceof ReminderRuleImpl) {
			ReminderRuleImpl r = (ReminderRuleImpl)rule;
			Translator translator = Util.createPackageTranslator(ReminderAdminController.class, locale);
			translator = Util.createPackageTranslator(FormBeforeDueDateRuleEditor.class, locale, translator);
			translator = Util.createPackageTranslator(GTARunController.class, locale, translator);
			String currentUnit = r.getRightUnit();
			String currentValue = r.getRightOperand();
			String nodeIdent = r.getLeftOperand();
			
			try {
				LaunchUnit.valueOf(currentUnit);
			} catch (Exception e) {
				return null;
			}
			
			ICourse course = CourseFactory.loadCourse(entry);
			CourseNode courseNode = course.getRunStructure().getNode(nodeIdent);
			if (courseNode == null) {
				courseNode = course.getEditorTreeModel().getCourseNode(nodeIdent);
				if (courseNode == null) {
					return null;
				}
			}
			
			DueDateConfig dueDateConfig = getDueDateConfig(courseNode);
			
			String deadline = null;
			DueDateConfigFormatter dueDateConfigFormatter = DueDateConfigFormatter.create(translator.getLocale());
			if (DueDateConfig.isRelative(dueDateConfig)) {
				String realtiveToName = GTACourseNode.TYPE_RELATIVE_TO_ASSIGNMENT.equals(dueDateConfig.getRelativeToType())
						? translator.translate("relative.to.assignment")
						: dueDateConfigFormatter.getRelativeToTypeNameAfter(dueDateConfig.getRelativeToType());
				deadline = dueDateConfigFormatter.concatRelativeDateConfig(dueDateConfig.getNumOfDays(), realtiveToName);
			} else if (DueDateConfig.isAbsolute(dueDateConfig)) {
				deadline = dueDateConfigFormatter.formatAbsoluteDateConfig(dueDateConfig);
			}
			
			if (!StringHelper.containsNonWhitespace(deadline)) {
				deadline = translator.translate("missing.value");
			}
			String[] args = new String[] { courseNode.getShortTitle(), courseNode.getIdent(), currentValue, deadline };
			return translator.translate(getStaticTextPrefix() + currentUnit, args);
		}
		return null;
	}
	
	@Override
	public List<Identity> evaluate(RepositoryEntry entry, ReminderRule rule) {
		List<Identity> identities = null;
		if(rule instanceof ReminderRuleImpl) {
			ReminderRuleImpl r = (ReminderRuleImpl)rule;
			String nodeIdent = r.getLeftOperand();
	
			ICourse course = CourseFactory.loadCourse(entry);
			if (course != null) {
				CourseNode courseNode = course.getRunStructure().getNode(nodeIdent);
				if (courseNode != null) {
					identities = evaluateRule(entry, courseNode, r);
					
					if(LearningPathNodeAccessProvider.TYPE.equals(NodeAccessType.of(course).getType())) {
						List<Long> excludedIdentityKeys = assessmentService.getIdentityKeys(entry, courseNode.getIdent(), AssessmentObligation.EXCLUDED);
						if (!excludedIdentityKeys.isEmpty()) {
							identities.removeIf(identity -> excludedIdentityKeys.contains(identity.getKey()));
						}
					}
				}
			}
		}
		return identities == null ? Collections.emptyList() : identities;
	}
	
	public List<Identity> evaluateRule(RepositoryEntry entry, CourseNode courseNode, ReminderRuleImpl rule) {
		List<Identity> identities = null;
		DueDateConfig dueDateConfig = getDueDateConfig(courseNode);
		if(DueDateConfig.isRelative(dueDateConfig)) {
			identities = getPeopleToRemindRelativeTo(entry, courseNode, dueDateConfig, rule);
		} else if(DueDateConfig.isAbsolute(dueDateConfig) && isNear(dueDateConfig.getAbsoluteDate(), now(), rule)) {
			identities = getPeopleToRemind(entry, courseNode);
		}
		return identities == null ? Collections.emptyList() : identities;
	}

	private List<Identity> getPeopleToRemindRelativeTo(RepositoryEntry entry, CourseNode courseNode,
			RelativeDueDateConfig relativDateConfig, ReminderRuleImpl rule) {
		List<Identity> identities = null;
		if(DueDateConfig.isRelative(relativDateConfig)) {
			if (DueDateService.TYPE_COURSE_START.equals(relativDateConfig.getRelativeToType())) {
				RepositoryEntryLifecycle lifecycle = entry.getLifecycle();
				if (lifecycle != null && lifecycle.getValidFrom() != null) {
					Date referenceDate = getDate(lifecycle.getValidFrom(), relativDateConfig.getNumOfDays());
					if(isNear(referenceDate, now(), rule)) {
						identities = getPeopleToRemind(entry, courseNode);
					}
				}
			} else if (DueDateService.TYPE_COURSE_LAUNCH.equals(relativDateConfig.getRelativeToType())) {
				UserCourseInformationsManager userCourseInformationsManager = CoreSpringFactory.getImpl(UserCourseInformationsManager.class);
				Map<Long,Date> initialLaunchDates = userCourseInformationsManager.getInitialLaunchDates(entry.getOlatResource().getResourceableId());
				Map<Long,Date> dueDates = getDueDates(initialLaunchDates, relativDateConfig.getNumOfDays());
				identities = getPeopleToRemindRelativeTo(entry, courseNode, dueDates, rule);
			} else if (DueDateService.TYPE_ENROLLMENT.equals(relativDateConfig.getRelativeToType())) {
				RepositoryService repositoryService = CoreSpringFactory.getImpl(RepositoryService.class);
				Map<Long,Date> enrollmentDates = repositoryService.getEnrollmentDates(entry);
				Map<Long,Date> dueDates = getDueDates(enrollmentDates, relativDateConfig.getNumOfDays());
				identities = getPeopleToRemindRelativeTo(entry, courseNode, dueDates, rule);
			}
		}
		return identities;
	}
	
	protected List<Identity> getPeopleToRemindRelativeTo(RepositoryEntry entry, CourseNode gtaNode,
			Map<Long,Date> dates, ReminderRuleImpl rule) {
		
		Date now = now();
		Set<Long> potentialidentityKeys = new HashSet<>();
		for(Map.Entry<Long, Date> entryDate:dates.entrySet()) {
			Long identityKey = entryDate.getKey();
			Date date = entryDate.getValue();
			if(isNear(date, now, rule)) {
				potentialidentityKeys.add(identityKey);
			}	
		}

		List<Identity> identities = null;
		if(potentialidentityKeys.size() > 0) {
			List<Identity> allIdentities = getPeopleToRemind(entry, gtaNode);
			identities = new ArrayList<>();
			for(Identity identity:allIdentities) {
				if(potentialidentityKeys.contains(identity.getKey())) {
					identities.add(identity);
				}
			}
		}
		return identities;
	}
	
	private Map<Long,Date> getDueDates(Map<Long,Date> referenceDates, int numOfDays) {
		Map<Long, Date> dueDates = new HashMap<>();
		if(referenceDates != null && referenceDates.size() > 0) {
			Calendar cal = Calendar.getInstance();
			for(Map.Entry<Long, Date> referenceEntry:referenceDates.entrySet()) {
				Long identityKey = referenceEntry.getKey();
				cal.setTime(referenceEntry.getValue());
				cal.add(Calendar.DATE, numOfDays);
				dueDates.put(identityKey, cal.getTime());
			}
		}
		return dueDates;
	}
	
	private Date getDate(Date referenceDate, int numOfDays) {
		Date date = null;
		if(referenceDate != null) {
			Calendar cal = Calendar.getInstance();
			cal.setTime(referenceDate);
			cal.add(Calendar.DATE, numOfDays);
			cal.set(Calendar.SECOND, 0);
			cal.set(Calendar.MILLISECOND, 0);
			date = cal.getTime();
		}
		return date;
	}

	protected Date now() {
		Calendar cal = Calendar.getInstance();
		cal.set(Calendar.SECOND, 0);
		cal.set(Calendar.MILLISECOND, 0);
		return cal.getTime();
	}

	protected boolean isNear(Date dueDate, Date now, ReminderRuleImpl r) {
		int value = Integer.parseInt(r.getRightOperand());
		String unit = r.getRightUnit();
		return near(dueDate, now, value, LaunchUnit.valueOf(unit));
	}

	private boolean near(Date date, Date now, int distance, LaunchUnit unit) {
		double between = -1;
		switch(unit) {
			case day:
				between = daysBetween(now, date);
				break;
			case week:
				between = weeksBetween(now, date);
				break;
			case month:
				between = monthsBetween(now, date);
				break;
			case year:
				between = yearsBetween(now, date);
				break;
		}
		// 0.1 to let +- 2 hours to match
		return  between <= distance || between - 0.1 <= distance || between < 0.0;
	}

	private double daysBetween(Date d1, Date d2) {
		return ((d2.getTime() - d1.getTime()) / (1000d * 60d * 60d * 24d));
	}

	private double weeksBetween(Date d1, Date d2) {
		return ((d2.getTime() - d1.getTime()) / (1000d * 60d * 60d * 24d * 7d));
	}

	private double monthsBetween(Date d1, Date d2) {
		return ((d2.getTime() - d1.getTime()) / (1000d * 60d * 60d * 24d * 30d));
	}
	
	double yearsBetween(Date d1, Date d2) {
		return ((d2.getTime() - d1.getTime()) / (1000d * 60d * 60d * 24d * 365d));
	}

}