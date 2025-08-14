/**
 * <a href="https://www.openolat.org">
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
 * frentix GmbH, https://www.frentix.com
 * <p>
 */
package org.olat.course.nodes.gta.manager;

import java.util.Date;
import java.util.List;

import org.olat.core.commons.persistence.DB;
import org.olat.course.nodes.GTACourseNode;
import org.olat.course.nodes.gta.GTAType;
import org.olat.course.nodes.gta.Task;
import org.olat.course.nodes.gta.TaskList;
import org.olat.course.nodes.gta.TaskRef;
import org.olat.course.nodes.gta.model.TaskImpl;
import org.olat.group.BusinessGroupRef;
import org.olat.repository.RepositoryEntry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 
 * Initial date: 7 juin 2024<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
@Service
public class GTATaskDAO {
	
	@Autowired
	private DB dbInstance;
	

	public Task updateTask(Task task) {
		return updateTask((TaskImpl)task);
	}
	
	public TaskImpl updateTask(TaskImpl task) {
		task.setLastModified(new Date());
		return dbInstance.getCurrentEntityManager().merge(task);
	}
	
	public Task loadTask(TaskRef task) {
		String q = """
				select task from gtatask task
				left join fetch task.businessGroup as businessGroup
				left join fetch task.identity as ident
				left join fetch task.taskList as taskList
				left join fetch task.survey as survey
				where task.key=:taskKey""";
		List<Task> tasks = dbInstance.getCurrentEntityManager().createQuery(q, Task.class)
			.setParameter("taskKey", task.getKey())
			.getResultList();

		return tasks.isEmpty() ? null : tasks.get(0);
	}
	
	/**
	 * Load the task list with the underlying repository
	 * entry (full).
	 * 
	 * @param task
	 * @return
	 */
	public RepositoryEntry getEntry(TaskRef task ) {
		String query = """
			select v from repositoryentry as v
			inner join fetch v.olatResource as ores
			inner join fetch v.statistics as statistics
			left join fetch v.lifecycle as lifecycle
			where v.key in (select tlist.entry.key from gtatasklist tlist
			 inner join gtatask task on (task.taskList.key=tlist.key)
			 where task.key=:taskKey
			)""";
		
		List<RepositoryEntry> entries = dbInstance.getCurrentEntityManager()
			.createQuery(query, RepositoryEntry.class)
			.setParameter("taskKey", task.getKey())
			.getResultList();

		return entries.isEmpty() ? null : entries.get(0);
	}
	
	public List<Task> getTasks(TaskList taskList, GTACourseNode cNode) {
		StringBuilder sb = new StringBuilder();
		sb.append("select task from gtatask task")
		  .append(" inner join task.taskList tasklist");
		if(GTAType.group.name().equals(cNode.getModuleConfiguration().getStringValue(GTACourseNode.GTASK_TYPE))) {
			sb.append(" inner join fetch task.businessGroup bGroup");
		} else {
			sb.append(" inner join fetch task.identity ident")
			  .append(" inner join fetch ident.user identUser");
		}
		sb.append(" where tasklist.key=:taskListKey");
		return dbInstance.getCurrentEntityManager().createQuery(sb.toString(), Task.class)
				.setParameter("taskListKey", taskList.getKey())
				.getResultList();
	}
	
	public List<Task> getTasks(BusinessGroupRef businessGroup) {
		String q = "select task from gtatask task where task.businessGroup.key=:businessGroupKey";
		return dbInstance.getCurrentEntityManager().createQuery(q, Task.class)
			.setParameter("businessGroupKey", businessGroup.getKey())
			.getResultList();
	}
	
	public List<Long> getTasksKeys(BusinessGroupRef businessGroup) {
		String q = "select task.key from gtatask task where task.businessGroup.key=:businessGroupKey";
		return dbInstance.getCurrentEntityManager().createQuery(q, Long.class)
			.setParameter("businessGroupKey", businessGroup.getKey())
			.getResultList();
	}
	
	public int deleteTask(TaskList taskList) {
		String deleteTasks = "delete from gtatask as task where task.taskList.key=:taskListKey";
		return dbInstance.getCurrentEntityManager()
			.createQuery(deleteTasks)
			.setParameter("taskListKey", taskList.getKey())
			.executeUpdate();
	}

}
