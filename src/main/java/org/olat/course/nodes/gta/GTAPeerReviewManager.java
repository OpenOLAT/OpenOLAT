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
	
	public List<TaskReviewAssignment> getAssignmentsForTask(Task task);
	
	public List<TaskReviewAssignment> getAssignmentsForTaskList(TaskList taskList);
	
	public List<TaskReviewAssignment> getAssignmentsOfReviewer(TaskList taskList, Identity reviewer);
	
	public TaskReviewAssignment createAssignment(Task task, Identity assignee);
	
	public TaskReviewAssignment updateAssignment(TaskReviewAssignment assignment);

	public TaskReviewAssignment loadAssignment(TaskReviewAssignment assignment);
	
	public TaskReviewAssignment invalidateAssignment(TaskReviewAssignment assignment, GTACourseNode gtaNode, Identity doer);
	
	public TaskReviewAssignment reopenAssignment(TaskReviewAssignment assignment, GTACourseNode gtaNode, Identity doer);
	
	/**
	 * Special purpose query, returns all assignees of a peer review,
	 * @return
	 */
	public List<Identity> getAssigneesToRemind(TaskList taskList, GTACourseNode gtaNode);
	
	
	public Task loadOrCreateSurvey(Task task, RepositoryEntry courseEntry, GTACourseNode gtaNode);
	
	public EvaluationFormSurvey loadOrCreateSurvey(RepositoryEntry courseEntry, GTACourseNode node, Identity assessedIdentity);

	public EvaluationFormSurvey loadSurvey(Task task);
	
	public EvaluationFormParticipation loadOrCreateParticipation(EvaluationFormSurvey survey, Identity executor);
	
	public EvaluationFormParticipation loadParticipation(EvaluationFormSurvey survey, Identity executor);
	
	public EvaluationFormSession loadOrCreateSession(EvaluationFormSurvey survey, Identity executor);
	
	public EvaluationFormSession loadOrCreateSession(EvaluationFormParticipation participation);
	
	public EvaluationFormSession loadSession(EvaluationFormParticipation participation);
	
	
	public SessionParticipationListStatistics loadStatistics(Task task, List<TaskReviewAssignment> assignments,
			GTACourseNode gtaNode, List<TaskReviewAssignmentStatus> status);
	
	public SessionParticipationListStatistics loadStatistics(TaskList taskList, List<TaskReviewAssignment> assignments, Identity reviewer,
			GTACourseNode gtaNode, List<TaskReviewAssignmentStatus> status);
	

}
