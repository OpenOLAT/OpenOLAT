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

import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.olat.basesecurity.GroupRoles;
import org.olat.core.id.Identity;
import org.olat.course.CourseFactory;
import org.olat.course.ICourse;
import org.olat.course.nodes.CourseNode;
import org.olat.course.nodes.GTACourseNode;
import org.olat.course.nodes.gta.GTAManager;
import org.olat.course.nodes.gta.GTAType;
import org.olat.course.nodes.gta.Task;
import org.olat.course.nodes.gta.TaskList;
import org.olat.group.BusinessGroup;
import org.olat.group.BusinessGroupService;
import org.olat.modules.ModuleConfiguration;
import org.olat.modules.reminder.IdentitiesProviderRuleSPI;
import org.olat.modules.reminder.ReminderRule;
import org.olat.modules.reminder.model.ReminderRuleImpl;
import org.olat.modules.reminder.rule.LaunchUnit;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryEntryRelationType;
import org.olat.repository.manager.RepositoryEntryRelationDAO;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 10.04.2015<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public abstract class AbstractDueDateTaskRuleSPI implements IdentitiesProviderRuleSPI {
	
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
	
			ICourse course = CourseFactory.loadCourse(entry.getOlatResource());
			CourseNode courseNode = course.getRunStructure().getNode(nodeIdent);
			if(courseNode instanceof GTACourseNode) {
				identities = evaluateRule(entry, (GTACourseNode)courseNode, r);
			}
		}
		return identities == null ? Collections.<Identity>emptyList() : identities;
	}
	
	protected List<Identity> evaluateRule(RepositoryEntry entry, GTACourseNode gtaNode, ReminderRuleImpl r) {
		List<Identity> identities = null;
		Date dueDate = getDueDate(gtaNode);
		if(dueDate != null) {
			int value = Integer.parseInt(r.getRightOperand());
			String unit = r.getRightUnit();
			Date now = new Date();
			if(near(dueDate, now, value, LaunchUnit.valueOf(unit))) {
				identities = getPeopleToRemind(entry, gtaNode);
			}
		}
		return identities == null ? Collections.<Identity>emptyList() : identities;
	}
	
	protected abstract Date getDueDate(GTACourseNode gtaNode);
	
	protected List<Identity> getPeopleToRemind(RepositoryEntry entry, GTACourseNode gtaNode) {
		ModuleConfiguration config = gtaNode.getModuleConfiguration();
		TaskList taskList = gtaManager.getTaskList(entry, gtaNode);
		if(GTAType.group.name().equals(config.getStringValue(GTACourseNode.GTASK_TYPE))) {
			return getGroupsToRemind(taskList, gtaNode);
		} else {
			return getIndividualsToRemind(taskList, entry);
		}
	}
	
	protected List<Identity> getGroupsToRemind(TaskList taskList, GTACourseNode gtaNode) {
		List<Task> tasks = gtaManager.getTasks(taskList);
		Set<BusinessGroup> doneTasks = new HashSet<BusinessGroup>();
		for(Task task:tasks) {
			if(task.getBusinessGroup() != null) {
				doneTasks.add(task.getBusinessGroup());
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
	
	protected List<Identity> getIndividualsToRemind(TaskList taskList, RepositoryEntry entry) {
		List<Task> tasks = gtaManager.getTasks(taskList);
		Set<Identity> doneTasks = new HashSet<Identity>();
		for(Task task:tasks) {
			if(task.getIdentity() != null) {
				doneTasks.add(task.getIdentity());
			}
		}

		List<Identity> identities = repositoryEntryRelationDao.getMembers(entry, RepositoryEntryRelationType.both, GroupRoles.participant.name());
		for(Iterator<Identity> identityIt=identities.iterator(); identityIt.hasNext(); ) {
			if(doneTasks.contains(identityIt.next())) {
				identityIt.remove();
			}
		}
		
		return identities;
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
		return  between <= distance || between < 0.0;
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
	
	private double yearsBetween(Date d1, Date d2) {
        return ((d2.getTime() - d1.getTime()) / (1000d * 60d * 60d * 24d * 365d));
	}
	
	
}
