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
package org.olat.course.nodes.gta.rule;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.olat.basesecurity.GroupRoles;
import org.olat.core.CoreSpringFactory;
import org.olat.core.id.Identity;
import org.olat.core.util.StringHelper;
import org.olat.course.CourseFactory;
import org.olat.course.ICourse;
import org.olat.course.assessment.manager.UserCourseInformationsManager;
import org.olat.course.nodes.CourseNode;
import org.olat.course.nodes.GTACourseNode;
import org.olat.course.nodes.gta.GTAManager;
import org.olat.course.nodes.gta.GTARelativeToDates;
import org.olat.course.nodes.gta.GTAType;
import org.olat.course.nodes.gta.Task;
import org.olat.course.nodes.gta.TaskList;
import org.olat.course.reminder.CourseNodeRuleSPI;
import org.olat.course.reminder.rule.AbstractDueDateRuleSPI;
import org.olat.group.BusinessGroup;
import org.olat.group.BusinessGroupService;
import org.olat.modules.ModuleConfiguration;
import org.olat.modules.reminder.ReminderRule;
import org.olat.modules.reminder.model.ReminderRuleImpl;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryEntryRelationType;
import org.olat.repository.RepositoryService;
import org.olat.repository.manager.RepositoryEntryRelationDAO;
import org.olat.repository.model.RepositoryEntryLifecycle;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 10.04.2015<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public abstract class AbstractDueDateTaskRuleSPI extends AbstractDueDateRuleSPI implements CourseNodeRuleSPI {
	
	@Autowired
	private GTAManager gtaManager;
	@Autowired
	private BusinessGroupService businessGroupService;
	@Autowired
	private RepositoryEntryRelationDAO repositoryEntryRelationDao;

	@Override
	public List<Identity> evaluate(RepositoryEntry entry, ReminderRule rule) {
		List<Identity> identities = null;
		if(rule instanceof ReminderRuleImpl) {
			ReminderRuleImpl r = (ReminderRuleImpl)rule;
			String nodeIdent = r.getLeftOperand();
	
			ICourse course = CourseFactory.loadCourse(entry);
			CourseNode courseNode = course.getRunStructure().getNode(nodeIdent);
			if(courseNode instanceof GTACourseNode) {
				identities = evaluateRule(entry, (GTACourseNode)courseNode, r);
			}
		}
		return identities == null ? Collections.<Identity>emptyList() : identities;
	}
	
	protected List<Identity> evaluateRule(RepositoryEntry entry, GTACourseNode gtaNode, ReminderRuleImpl rule) {
		List<Identity> identities = null;
		if(gtaNode.getModuleConfiguration().getBooleanSafe(GTACourseNode.GTASK_RELATIVE_DATES)) {
			identities = evaluateRelativeDateRule(entry, gtaNode, rule);
		} else {
			Date dueDate = getDueDate(gtaNode);
			if(dueDate != null && isNear(dueDate, now(), rule)) {
				identities = getPeopleToRemind(entry, gtaNode);
			}
		}
		return identities == null ? Collections.<Identity>emptyList() : identities;
	}
	
	protected abstract List<Identity> evaluateRelativeDateRule(RepositoryEntry entry, GTACourseNode gtaNode, ReminderRuleImpl r);
	
	protected abstract Date getDueDate(GTACourseNode gtaNode);
	
	protected List<Identity> getPeopleToRemind(RepositoryEntry entry, GTACourseNode gtaNode) {
		ModuleConfiguration config = gtaNode.getModuleConfiguration();
		TaskList taskList = gtaManager.getTaskList(entry, gtaNode);
		if(GTAType.group.name().equals(config.getStringValue(GTACourseNode.GTASK_TYPE))) {
			return getGroupsToRemind(taskList, gtaNode);
		} else {
			return getIndividualsToRemind(taskList, entry, gtaNode);
		}
	}
	
	protected List<Identity> getPeopleToRemindRelativeTo(RepositoryEntry entry, GTACourseNode gtaNode,
			int numOfDays, String relativeTo, ReminderRuleImpl rule) {
		List<Identity> identities = null;
		if(numOfDays >= 0 && StringHelper.containsNonWhitespace(relativeTo)) {
			GTARelativeToDates rel = GTARelativeToDates.valueOf(relativeTo);
			switch(rel) {
				case courseStart: {
					RepositoryEntryLifecycle lifecycle = entry.getLifecycle();
					if(lifecycle != null && lifecycle.getValidFrom() != null) {
						Date referenceDate = getDate(lifecycle.getValidFrom(),  numOfDays);
						if(isNear(referenceDate, now(), rule)) {
							identities = getPeopleToRemind(entry, gtaNode);
						}
					}
					break;
				}
	
				case courseLaunch: {
					UserCourseInformationsManager userCourseInformationsManager = CoreSpringFactory.getImpl(UserCourseInformationsManager.class);
					Map<Long,Date> initialLaunchDates = userCourseInformationsManager.getInitialLaunchDates(entry.getOlatResource().getResourceableId());
					Map<Long,Date> dueDates = getDueDates(initialLaunchDates, numOfDays);
					identities = getPeopleToRemindRelativeTo(entry, gtaNode, dueDates, rule);
					break;
				}
				case enrollment: {
					RepositoryService repositoryService = CoreSpringFactory.getImpl(RepositoryService.class);
					Map<Long,Date> enrollmentDates = repositoryService.getEnrollmentDates(entry);
					Map<Long,Date> dueDates = getDueDates(enrollmentDates, numOfDays);
					identities = getPeopleToRemindRelativeTo(entry, gtaNode, dueDates, rule);
					break;
				}
				default: {
					//
				}
			}	
		}
		return identities;
	}
	
	protected List<Identity> getPeopleToRemindRelativeTo(RepositoryEntry entry, GTACourseNode gtaNode,
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
	
	protected List<Identity> getGroupsToRemind(TaskList taskList, GTACourseNode gtaNode) {
		Set<BusinessGroup> doneTasks = new HashSet<>();
		if(taskList != null) {
			List<Task> tasks = gtaManager.getTasks(taskList, gtaNode);
			for(Task task:tasks) {
				if(task.getBusinessGroup() != null) {
					doneTasks.add(task.getBusinessGroup());
				}
			}
		}

		List<BusinessGroup> groups = gtaManager.getBusinessGroups(gtaNode);
		for(Iterator<BusinessGroup> groupIt=groups.iterator(); groupIt.hasNext(); ) {
			if(doneTasks.contains(groupIt.next())) {
				groupIt.remove();
			}
		}

		return businessGroupService.getMembers(groups, GroupRoles.participant.name());
	}
	
	protected List<Identity> getIndividualsToRemind(TaskList taskList, RepositoryEntry entry, GTACourseNode gtaNode) {
		List<Task> tasks = gtaManager.getTasks(taskList, gtaNode);
		Set<Identity> doneTasks = new HashSet<>();
		for(Task task:tasks) {
			if(task.getIdentity() != null) {
				doneTasks.add(task.getIdentity());
			}
		}

		List<Identity> identities = repositoryEntryRelationDao.getMembers(entry, RepositoryEntryRelationType.all, GroupRoles.participant.name());
		for(Iterator<Identity> identityIt=identities.iterator(); identityIt.hasNext(); ) {
			if(doneTasks.contains(identityIt.next())) {
				identityIt.remove();
			}
		}
		
		return identities;
	}
	
	@Override
	public String getCourseNodeIdent(ReminderRule rule) {
		if(rule instanceof ReminderRuleImpl) {
			ReminderRuleImpl r = (ReminderRuleImpl)rule;
			return r.getLeftOperand();
		}
		return null;
	}
	
}
