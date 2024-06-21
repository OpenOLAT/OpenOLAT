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

import jakarta.persistence.Query;

import org.olat.basesecurity.IdentityRef;
import org.olat.core.commons.persistence.QueryBuilder;
import org.olat.course.nodes.gta.TaskList;
import org.olat.course.nodes.gta.TaskReviewAssignmentStatus;
import org.olat.modules.forms.SessionFilter;

/**
 * 
 * Initial date: 6 juin 2024<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class AssigneeSessionFilter implements SessionFilter {
	
	private final TaskList taskList;
	private final IdentityRef assignee;
	private final List<TaskReviewAssignmentStatus> status;
	
	public AssigneeSessionFilter(TaskList taskList, IdentityRef assignee, List<TaskReviewAssignmentStatus> status) {
		this.taskList = taskList;
		this.assignee = assignee;
		this.status = status;
	}

	@Override
	public String getSelectKeys() {
		QueryBuilder sb = new QueryBuilder();
		sb.append("select sessionFilter.key");
		sb.append("  from evaluationformsession sessionFilter")
		  .append("  inner join gtatask task on (task.survey.key=sessionFilter.survey.key)")
		  .append("  inner join taskreviewasssignment assignment on (task.key=assignment.task.key)");
		
		sb.where().append(" assignment.assignee.key=:assigneeKey and task.taskList.key=:taskListKey");
		if(status != null && !status.isEmpty()) {
			sb.and().append(" assignment.status in :status");
		}
		return sb.toString();
	}

	@Override
	public void addParameters(Query query) {
		query.setParameter("assigneeKey", assignee.getKey());
		query.setParameter("taskListKey", taskList.getKey());
		if(status != null && !status.isEmpty()) {
			query.setParameter("status", status);
		}
	}
}
