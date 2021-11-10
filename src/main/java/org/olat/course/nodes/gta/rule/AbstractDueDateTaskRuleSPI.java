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
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.olat.basesecurity.GroupRoles;
import org.olat.core.id.Identity;
import org.olat.course.nodes.CourseNode;
import org.olat.course.nodes.GTACourseNode;
import org.olat.course.nodes.gta.GTAManager;
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
import org.olat.repository.manager.RepositoryEntryRelationDAO;
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
	protected List<Identity> getPeopleToRemind(RepositoryEntry entry, CourseNode courseNode) {
		if (courseNode instanceof GTACourseNode) {
			GTACourseNode gtaCourseNode = (GTACourseNode)courseNode;
			ModuleConfiguration config = courseNode.getModuleConfiguration();
			TaskList taskList = gtaManager.getTaskList(entry, gtaCourseNode);
			if(GTAType.group.name().equals(config.getStringValue(GTACourseNode.GTASK_TYPE))) {
				return getGroupsToRemind(taskList, gtaCourseNode);
			}
			return getIndividualsToRemind(taskList, entry, gtaCourseNode);
		}
		return Collections.emptyList();
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
