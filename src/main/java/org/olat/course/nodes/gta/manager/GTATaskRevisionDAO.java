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
import java.util.List;

import org.olat.core.commons.persistence.DB;
import org.olat.core.id.Identity;
import org.olat.core.util.StringHelper;
import org.olat.course.nodes.gta.Task;
import org.olat.course.nodes.gta.TaskList;
import org.olat.course.nodes.gta.TaskRevision;
import org.olat.course.nodes.gta.model.TaskRevisionImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 
 * Initial date: 28 nov. 2019<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
@Service
public class GTATaskRevisionDAO {
	
	@Autowired
	private DB dbInstance;
	
	public TaskRevision createTaskRevision(Task task, String status, int revisionLoop, String comment, Identity commentator, Date revisionDate) {
		TaskRevisionImpl rev = new TaskRevisionImpl();
		rev.setCreationDate(new Date());
		rev.setLastModified(rev.getCreationDate());
		rev.setDate(revisionDate);
		if(StringHelper.containsNonWhitespace(comment)) {
			rev.setComment(comment);
			rev.setCommentLastModified(new Date());
			rev.setCommentAuthor(commentator);
		}
		rev.setTask(task);
		rev.setStatus(status);
		rev.setRevisionLoop(revisionLoop);
		dbInstance.getCurrentEntityManager().persist(rev);
		return rev;
	}
	
	public TaskRevision updateTaskRevision(TaskRevision taskRevision) {
		((TaskRevisionImpl)taskRevision).setLastModified(new Date());
		return dbInstance.getCurrentEntityManager().merge(taskRevision);
	}
	
	public List<TaskRevision> getTaskRevisions(Task task) {
		String s = "select rev from gtataskrevision as rev where rev.task.key=:taskKey";
		return dbInstance.getCurrentEntityManager()
				.createQuery(s, TaskRevision.class)
				.setParameter("taskKey", task.getKey())
				.getResultList();
	}
	
	public TaskRevision getTaskRevision(Task task, String status, int revisionLoop) {
		String s = "select rev from gtataskrevision as rev where rev.task.key=:taskKey and rev.revisionLoop=:revisionLoop and rev.status=:status";
		List<TaskRevision> revisions = dbInstance.getCurrentEntityManager()
				.createQuery(s, TaskRevision.class)
				.setParameter("taskKey", task.getKey())
				.setParameter("revisionLoop", Integer.valueOf(revisionLoop))
				.setParameter("status", status)
				.getResultList();
		return revisions == null || revisions.isEmpty() ? null : revisions.get(0);
	}
	
	public int deleteTaskRevision(TaskList taskList) {
		StringBuilder taskSb = new StringBuilder(128);
		taskSb.append("delete from gtataskrevision as taskrev where taskrev.task.key in (")
		      .append("  select task.key from gtatask as task where task.taskList.key=:taskListKey)");
		return dbInstance.getCurrentEntityManager()
			.createQuery(taskSb.toString())
			.setParameter("taskListKey", taskList.getKey())
			.executeUpdate();
	}
}
