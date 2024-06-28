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

import org.olat.basesecurity.IdentityRef;
import org.olat.core.commons.persistence.DB;
import org.olat.core.id.Identity;
import org.olat.course.nodes.gta.Task;
import org.olat.course.nodes.gta.TaskList;
import org.olat.course.nodes.gta.TaskReviewAssignment;
import org.olat.course.nodes.gta.TaskReviewAssignmentStatus;
import org.olat.course.nodes.gta.model.TaskReviewAssignmentImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 
 * Initial date: 12 juin 2024<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
@Service
public class GTATaskReviewAssignmentDAO {

	@Autowired
	private DB dbInstance;
	
	public TaskReviewAssignment createAssignment(Task task, Identity assignee) {
		TaskReviewAssignmentImpl assignment = new TaskReviewAssignmentImpl();
		assignment.setCreationDate(new Date());
		assignment.setLastModified(assignment.getCreationDate());
		assignment.setAssigned(true);
		assignment.setStatus(TaskReviewAssignmentStatus.open);
		assignment.setAssignee(assignee);
		assignment.setTask(task);
		dbInstance.getCurrentEntityManager().persist(assignment);
		return assignment;
	}
	
	public TaskReviewAssignment loadByKey(Long assignmentKey) {
		String query = """
				select assignment from taskreviewasssignment assignment
				inner join fetch assignment.task as task
				inner join fetch assignment.assignee as assignee
				left join fetch assignment.participation as surveyParticipation
				where assignment.key=:assignmentKey
				""";
		
		List<TaskReviewAssignment> assignmentList = dbInstance.getCurrentEntityManager()
				.createQuery(query, TaskReviewAssignment.class)
				.setParameter("assignmentKey", assignmentKey)
				.getResultList();
		return assignmentList != null && !assignmentList.isEmpty() ? assignmentList.get(0) : null;
	}
	
	public List<TaskReviewAssignment> getAssignments(Task task) {
		String query = """
				select assignment from taskreviewasssignment assignment
				inner join fetch assignment.task as task
				inner join fetch assignment.assignee as assignee
				left join fetch assignment.participation as surveyParticipation
				where task.key=:taskKey
				""";
		
		return dbInstance.getCurrentEntityManager()
				.createQuery(query, TaskReviewAssignment.class)
				.setParameter("taskKey", task.getKey())
				.getResultList();
	}
	
	public List<TaskReviewAssignment> getAssignments(TaskList taskList) {
		String query = """
				select assignment from taskreviewasssignment assignment
				inner join fetch assignment.task as task
				inner join fetch assignment.assignee as assignee
				left join fetch assignment.participation as surveyParticipation
				where task.taskList.key=:taskListKey
				""";
		
		return dbInstance.getCurrentEntityManager()
				.createQuery(query, TaskReviewAssignment.class)
				.setParameter("taskListKey", taskList.getKey())
				.getResultList();
	}
	
	public List<TaskReviewAssignment> getAssignments(TaskList taskList, IdentityRef reviewer) {
		String query = """
				select assignment from taskreviewasssignment assignment
				inner join fetch assignment.task as task
				inner join fetch assignment.assignee as assignee
				left join fetch assignment.participation as surveyParticipation
				where assignee.key=:reviewerKey and task.taskList.key=:taskListKey
				""";
		
		return dbInstance.getCurrentEntityManager()
				.createQuery(query, TaskReviewAssignment.class)
				.setParameter("reviewerKey", reviewer.getKey())
				.setParameter("taskListKey", taskList.getKey())
				.getResultList();
	}
	
	
	public List<Identity> findAssignees(TaskList taskList, List<TaskReviewAssignmentStatus> status) {
		if(status == null || status.isEmpty()) {
			status = List.of(TaskReviewAssignmentStatus.values());
		}
		
		String query = """
				select assignee from taskreviewasssignment assignment
				inner join assignment.task as task
				inner join assignment.assignee as assignee
				inner join fetch assignee.user as assigneeUsr
				where task.taskList.key=:taskListKey and assignment.status in (:status)
				""";
		
		return dbInstance.getCurrentEntityManager()
				.createQuery(query, Identity.class)
				.setParameter("taskListKey", taskList.getKey())
				.setParameter("status", status)
				.getResultList();
	}
	
	public TaskReviewAssignment updateAssignment(TaskReviewAssignment assignment) {
		((TaskReviewAssignmentImpl)assignment).setLastModified(new Date());
		return dbInstance.getCurrentEntityManager().merge(assignment);
	}
}
