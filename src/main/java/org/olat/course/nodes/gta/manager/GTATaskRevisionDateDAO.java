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
package org.olat.course.nodes.gta.manager;

import java.util.Date;

import org.olat.core.commons.persistence.DB;
import org.olat.course.nodes.gta.Task;
import org.olat.course.nodes.gta.TaskList;
import org.olat.course.nodes.gta.TaskProcess;
import org.olat.course.nodes.gta.TaskRevisionDate;
import org.olat.course.nodes.gta.model.TaskRevisionDateImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 
 * Initial date: 25 oct. 2021<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
@Service
public class GTATaskRevisionDateDAO {
	
	@Autowired
	private DB dbInstance;
	
	public TaskRevisionDate createAndPersistTaskRevisionDate(Task task, int revisionLoop, TaskProcess status) {
		TaskRevisionDateImpl rev = new TaskRevisionDateImpl();
		rev.setCreationDate(new Date());
		rev.setDate(rev.getCreationDate());
		rev.setRevisionLoop(revisionLoop);
		rev.setStatus(status.name());
		rev.setTask(task);
		dbInstance.getCurrentEntityManager().persist(rev);
		return rev;
	}
	
	public int deleteTaskRevisionDate(TaskList taskList) {
		StringBuilder sb = new StringBuilder(128);
		sb.append("delete from gtataskrevisiondate as taskrev where taskrev.task.key in (")
		  .append("  select task.key from gtatask as task where task.taskList.key=:taskListKey)");
		return dbInstance.getCurrentEntityManager()
			.createQuery(sb.toString())
			.setParameter("taskListKey", taskList.getKey())
			.executeUpdate();
	}

}
