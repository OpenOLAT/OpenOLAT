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
package org.olat.course.nodes.gta;

import java.util.List;
import java.util.Map;

import org.olat.core.id.Identity;
import org.olat.course.nodes.GTACourseNode;
import org.olat.course.nodes.gta.model.SessionParticipationListStatistics;
import org.olat.modules.forms.EvaluationFormParticipation;
import org.olat.modules.forms.EvaluationFormSession;
import org.olat.modules.forms.EvaluationFormSurvey;
import org.olat.repository.RepositoryEntry;

/**
 * 
 * Initial date: 6 juin 2024<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public interface GTAPeerReviewManager {
	
	List<TaskReviewAssignment> getAssignmentsForTask(Task task);
	
	List<TaskReviewAssignment> getAssignmentsForTaskList(TaskList taskList);
	
	List<TaskReviewAssignment> getAssignmentsOfReviewer(TaskList taskList, Identity reviewer);
	
	TaskReviewAssignment createAssignment(Task task, Identity assignee);
	
	TaskReviewAssignment updateAssignment(TaskReviewAssignment assignment);

	TaskReviewAssignment loadAssignment(TaskReviewAssignment assignment);
	
	TaskReviewAssignment invalidateAssignment(TaskReviewAssignment assignment, GTACourseNode gtaNode, Identity doer);
	
	TaskReviewAssignment reopenAssignment(TaskReviewAssignment assignment, GTACourseNode gtaNode, Identity doer);
	
	/**
	 * Special purpose query, returns all assignees of a peer review,
	 * @return
	 */
	List<Identity> getAssigneesToRemind(TaskList taskList, GTACourseNode gtaNode);
	
	
	Task loadOrCreateSurvey(Task task, RepositoryEntry courseEntry, GTACourseNode gtaNode);
	
	EvaluationFormSurvey loadOrCreateSurvey(RepositoryEntry courseEntry, GTACourseNode node, Identity assessedIdentity);

	EvaluationFormSurvey loadSurvey(Task task);
	
	EvaluationFormParticipation loadOrCreateParticipation(EvaluationFormSurvey survey, Identity executor);
	
	EvaluationFormParticipation loadParticipation(EvaluationFormSurvey survey, Identity executor);
	
	EvaluationFormSession loadOrCreateSession(EvaluationFormSurvey survey, Identity executor);
	
	EvaluationFormSession loadOrCreateSession(EvaluationFormParticipation participation);
	
	EvaluationFormSession loadSession(EvaluationFormParticipation participation);
	

	
	SessionParticipationListStatistics loadStatistics(Task task, List<TaskReviewAssignment> assignments,
			GTACourseNode gtaNode, List<TaskReviewAssignmentStatus> status);
	
	SessionParticipationListStatistics loadStatistics(TaskList taskList, List<TaskReviewAssignment> assignments, Identity reviewer,
			GTACourseNode gtaNode, List<TaskReviewAssignmentStatus> status);
	
	/**
	 * Calculate the statistics for every tasks in the task list.
	 * 
	 * @param taskList
	 * @param assignments
	 * @param gtaNode
	 * @param status
	 * @return
	 */
	Map<Task,SessionParticipationListStatistics> loadStatisticsProTask(TaskList taskList, List<TaskReviewAssignment> assignments,
			GTACourseNode gtaNode, List<TaskReviewAssignmentStatus> status);
	
	Map<Identity, SessionParticipationListStatistics> loadStatisticsProAssignee(TaskList taskList,
			List<TaskReviewAssignment> assignments, GTACourseNode gtaNode, List<TaskReviewAssignmentStatus> status);
}
