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

import java.util.List;

import org.olat.core.commons.persistence.DB;
import org.olat.course.nodes.GTACourseNode;
import org.olat.course.nodes.gta.TaskList;
import org.olat.repository.RepositoryEntryRef;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 
 * Initial date: 3 juin 2026<br>
 * @author srosse, stephane.rosse@frentix.com, https://www.frentix.com
 *
 */
@Service
public class GTATaskListDAO {

	@Autowired
	private DB dbInstance;
	
	public TaskList getTaskList(RepositoryEntryRef entry, GTACourseNode cNode) {
		String q = "select tasks from gtatasklist tasks where tasks.entry.key=:entryKey and tasks.courseNodeIdent=:courseNodeIdent";
		List<TaskList> tasks = dbInstance.getCurrentEntityManager().createQuery(q, TaskList.class)
			.setParameter("entryKey", entry.getKey())
			.setParameter("courseNodeIdent", cNode.getIdent())
			.getResultList();

		return tasks.isEmpty() ? null : tasks.get(0);
	}
	
	public int deleteTaskList(TaskList taskList) {
		String deleteTasks = "delete from gtatasklist as taskList where taskList.key=:taskListKey";
		return dbInstance.getCurrentEntityManager()
			.createQuery(deleteTasks)
			.setParameter("taskListKey", taskList.getKey())
			.executeUpdate();
	}
	

}
