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
package org.olat.course.nodes.gta;

import java.io.File;
import java.util.Date;
import java.util.List;

import org.olat.basesecurity.IdentityRef;
import org.olat.core.commons.services.notifications.PublisherData;
import org.olat.core.commons.services.notifications.SubscriptionContext;
import org.olat.core.id.Identity;
import org.olat.core.util.vfs.VFSContainer;
import org.olat.course.nodes.GTACourseNode;
import org.olat.course.nodes.gta.model.DueDate;
import org.olat.course.nodes.gta.model.Membership;
import org.olat.course.nodes.gta.model.Solution;
import org.olat.course.nodes.gta.model.TaskDefinition;
import org.olat.course.nodes.gta.ui.events.SubmitEvent;
import org.olat.course.run.environment.CourseEnvironment;
import org.olat.course.run.userview.UserCourseEnvironment;
import org.olat.group.BusinessGroup;
import org.olat.group.BusinessGroupRef;
import org.olat.modules.assessment.Role;
import org.olat.modules.assessment.model.AssessmentEntryStatus;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryEntryRef;
import org.olat.resource.OLATResource;

/**
 * 
 * Initial date: 24.02.2015<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public interface GTAManager {
	
	public static final String SOLUTIONS_DEFINITIONS = "solutionDefinitions.xml";
	public static final String TASKS_DEFINITIONS = "taskDefinitions.xml";
	String BUSY_VALUE = "busy";
	
	public VFSContainer getTasksContainer(CourseEnvironment courseEnv, GTACourseNode cNode);
	
	public File getTasksDirectory(CourseEnvironment courseEnv, GTACourseNode cNode);
	
	/**
	 * Get (and eventually upgrade) the task definitions.
	 * @param courseEnv
	 * @param cNode
	 * @return
	 */
	public List<TaskDefinition> getTaskDefinitions(CourseEnvironment courseEnv, GTACourseNode cNode);
	
	public void addTaskDefinition(TaskDefinition newTask, CourseEnvironment courseEnv, GTACourseNode cNode);

	/**
	 * 
	 * @param currentFilename the filename before the definition was updated
	 * @param task
	 * @param courseEnv
	 * @param cNode
	 */
	public void updateTaskDefinition(String currentFilename, TaskDefinition task, CourseEnvironment courseEnv, GTACourseNode cNode);
	
	/**
	 * Remove the task definition and the file (if it's not used by an other task)
	 * 
	 * @param removedTask The task definition to remove
	 * @param courseEnv The course environment
	 * @param cNode The course element
	 */
	public void removeTaskDefinition(TaskDefinition removedTask, CourseEnvironment courseEnv, GTACourseNode cNode);
	
	public File getSubmitDirectory(CourseEnvironment courseEnv, GTACourseNode cNode, IdentityRef person);
	
	public File getSubmitDirectory(CourseEnvironment courseEnv, GTACourseNode cNode, BusinessGroupRef assessedGroup);
	
	public VFSContainer getSubmitContainer(CourseEnvironment courseEnv, GTACourseNode cNode, IdentityRef person);
	
	public VFSContainer getSubmitContainer(CourseEnvironment courseEnv, GTACourseNode cNode, BusinessGroupRef group);
	
	public File getCorrectionDirectory(CourseEnvironment courseEnv, GTACourseNode cNode, IdentityRef person);
	
	public VFSContainer getCorrectionContainer(CourseEnvironment courseEnv, GTACourseNode cNode, IdentityRef person);
	
	public File getCorrectionDirectory(CourseEnvironment courseEnv, GTACourseNode cNode, BusinessGroupRef group);
	
	public VFSContainer getCorrectionContainer(CourseEnvironment courseEnv, GTACourseNode cNode, BusinessGroupRef group);
	
	
	public File getRevisedDocumentsDirectory(CourseEnvironment courseEnv, GTACourseNode cNode, int iteration, IdentityRef person);
	
	public VFSContainer getRevisedDocumentsContainer(CourseEnvironment courseEnv, GTACourseNode cNode, int iteration, IdentityRef person);
	
	public File getRevisedDocumentsDirectory(CourseEnvironment courseEnv, GTACourseNode cNode, int iteration, BusinessGroupRef group);
	
	public VFSContainer getRevisedDocumentsContainer(CourseEnvironment courseEnv, GTACourseNode cNode, int iteration, BusinessGroupRef group);

	public File getRevisedDocumentsCorrectionsDirectory(CourseEnvironment courseEnv, GTACourseNode cNode, int iteration, IdentityRef person);
	
	public VFSContainer getRevisedDocumentsCorrectionsContainer(CourseEnvironment courseEnv, GTACourseNode cNode, int iteration, IdentityRef person);
	
	public File getRevisedDocumentsCorrectionsDirectory(CourseEnvironment courseEnv, GTACourseNode cNode, int iteration, BusinessGroupRef group);
	
	public VFSContainer getRevisedDocumentsCorrectionsContainer(CourseEnvironment courseEnv, GTACourseNode cNode, int iteration, BusinessGroupRef group);
	

	public File getSolutionsDirectory(CourseEnvironment courseEnv, GTACourseNode cNode);

	public VFSContainer getSolutionsContainer(CourseEnvironment courseEnv, GTACourseNode cNode);
	
	public List<Solution> getSolutions(CourseEnvironment courseEnv, GTACourseNode cNode);
	
	public void addSolution(Solution newSolution, CourseEnvironment courseEnv, GTACourseNode cNode);
	
	public void updateSolution(String currentFilename, Solution solution, CourseEnvironment courseEnv, GTACourseNode cNode);
	
	public void removeSolution(Solution removedSolution, CourseEnvironment courseEnv, GTACourseNode cNode);
	
	/**
	 * Create a subscription context.
	 * @param courseEnv The course environment
	 * @param cNode The course element
	 * @return The subscription context for this course and course element.
	 */
	public SubscriptionContext getSubscriptionContext(CourseEnvironment courseEnv, GTACourseNode cNode, boolean markedOnly);
	
	/**
	 * Create a subscription context.
	 * @param courseRes The course resource found in the repository entry
	 * @param cNode The course element
	 * @return The subscription context for this course and course element.
	 */
	public SubscriptionContext getSubscriptionContext(OLATResource courseRes, GTACourseNode cNode, boolean markedOnly);
	
	public PublisherData getPublisherData(CourseEnvironment courseEnv, GTACourseNode cNode, boolean markedOnly);
	
	/**
	 * Set the news on the publishers for standard and marked tasks.
	 * 
	 * @param courseEnv The course environment
	 * @param cNode The course node
	 */
	public void markNews(CourseEnvironment courseEnv, GTACourseNode cNode);
	
	/**
	 * Return the list of business groups configured in the course element
	 * following the areas.
	 * 
	 * @param cNode
	 * @return
	 */
	public List<BusinessGroup> getBusinessGroups(GTACourseNode cNode);
	
	
	/**
	 * The list of groups where the specified identity is participant.
	 * 
	 * @param identity
	 * @param groupKeys
	 * @param areaKeys
	 * @return
	 */
	public List<BusinessGroup> getParticipatingBusinessGroups(IdentityRef identity, GTACourseNode gtaNode);
	
	/**
	 * The list of groups where the specified identity is coach.
	 * 
	 * @param identity
	 * @param groupKeys
	 * @param areaKeys
	 * @return
	 */
	public List<BusinessGroup> getCoachedBusinessGroups(IdentityRef identity, GTACourseNode gtaNode);
	
	/**
	 * Filter the groups selected in the course element from the specified list.
	 * 
	 * @param groups
	 * @param gtaNode
	 * @return
	 */
	public List<BusinessGroup> filterBusinessGroups(List<BusinessGroup> groups, GTACourseNode gtaNode);
	
	/**
	 * Return a list of participants which are in several groups.
	 * 
	 * @param gtaNode
	 * @return
	 */
	public List<IdentityRef> getDuplicatedMemberships(GTACourseNode gtaNode);
	
	/**
	 * Are users already processing the tasks?
	 * 
	 * @param entry
	 * @param gtaNode
	 * @return
	 */
	public boolean isTasksInProcess(RepositoryEntryRef entry, GTACourseNode gtaNode);
	
	/**
	 * Convert the status of a task to the status used by the assessment tool.
	 * @param task
	 * @param cNode
	 * @return
	 */
	public AssessmentEntryStatus convertToAssessmentEntryStatus(Task task, GTACourseNode cNode);
	
	/**
	 * Synchronize the assessment entry with the state of the task.
	 * 
	 * @param task The task
	 * @param cNode The course element
	 * @param assessedUserCourseEnv Optional: the user course environment to be directly updatet
	 * @param doerIdentity the identity which do the action
	 * @param by The role of the doer
	 * @return true if the status is done
	 */
	public boolean syncAssessmentEntry(Task task, GTACourseNode cNode, UserCourseEnvironment assessedUserCourseEnv, boolean incrementUserAttempts, Identity doerIdentity, Role by);
	
	/**
	 * Are users already processing this task?
	 * 
	 * @param entry
	 * @param gtaNode
	 * @param taskName
	 * @return
	 */
	public boolean isTaskInProcess(RepositoryEntryRef entry, GTACourseNode gtaNode, String taskName);
	
	/**
	 * Return the details, a string used by the assessment tool
	 * @return
	 */
	public String getDetails(Identity assessedIdentity, RepositoryEntryRef entry, GTACourseNode cNode);
	
	public TaskList createIfNotExists(RepositoryEntry entry, GTACourseNode cNode);
	
	public TaskList getTaskList(RepositoryEntryRef entry, GTACourseNode cNode);
	
	public int updateTaskName(TaskList taskList, String currentTaskName, String newTaskName);
	
	/**
	 * Delete the task list and the tasks of the specified course element of the course.
	 * @param entry The repository entry of the course
	 * @param cNode The course element
	 * @return
	 */
	public int deleteTaskList(RepositoryEntryRef entry, GTACourseNode cNode);
	
	/**
	 * Delete all the task list and tasks of a course specified by its repository entry.
	 * @param entry
	 * @return
	 */
	public int deleteAllTaskLists(RepositoryEntryRef entry);
	
	
	public Membership getMembership(IdentityRef identity, RepositoryEntryRef entry, GTACourseNode cNode);
	
	public Task getTask(TaskRef task);
	
	public TaskDueDate getDueDatesTask(TaskRef task);
	
	public Task getTask(IdentityRef identity, TaskList taskList);
	
	public Task getTask(BusinessGroupRef businessGroup, TaskList taskList);
	
	public Task createTask(String taskName, TaskList taskList, TaskProcess status, BusinessGroup assessedGroup, Identity assessedIdentity, GTACourseNode cNode);
	
	public Task createAndPersistTask(String taskName, TaskList taskList, TaskProcess status, BusinessGroup assessedGroup, Identity assessedIdentity, GTACourseNode cNode);
	
	/**
	 * Ensure the task exists and is persisted, if not create one with status set to the first configured step.
	 * 
	 * @param assignedTask The assigned task, if it exists
	 * @param assessedGroup The assessed group
	 * @param assessedIdentity The assessed identity
	 * @param entry The course entry
	 * @param gtaNode The course element
	 * @return A persisted task
	 */
	public Task ensureTaskExists(Task assignedTask, BusinessGroup assessedGroup, Identity assessedIdentity, RepositoryEntry entry, GTACourseNode gtaNode);
	
	/**
	 * If a temporary transient task created for coaching purpose need to be persisted,
	 * 
	 * @param task The transient task
	 * @return The persisted task
	 */
	public Task persistTask(Task task);
	
	public Task nextStep(Task task, GTACourseNode cNode, boolean incrementUserAttempts, Identity doerIdentity, Role by);
	
	

	public List<Task> getTasks(TaskList taskList, GTACourseNode gtaNode);
	
	public List<Task> getTasks(IdentityRef identity);
	
	public List<TaskLight> getTasksLight(RepositoryEntryRef entry, GTACourseNode gtaNode);
	
	public List<TaskRevisionDate> getTaskRevisionsDate(Task task);
	
	/**
	 * Update a comment for the specified revision. If the comment is empty
	 * and there is not already a revision entry, nothing will be saved.
	 * 
	 * @param task The task
	 * @param status The status of the revision
	 * @param iteration The iteration / revision loop index of the revision
	 * @param comment The comment
	 * @param commentator The identity of the commentator
	 * @return The revision if merged or persisted
	 */
	public TaskRevision updateTaskRevisionComment(Task task, TaskProcess status, int iteration, String comment, Identity commentator);
	
	public List<TaskRevision> getTaskRevisions(Task task);

	
	/**
	 * Return the tasks assigned to a person, individually or via a
	 * business group.
	 * 
	 * @param identity
	 * @param cNode
	 * @return
	 */
	public List<Task> getTasks(IdentityRef identity, RepositoryEntryRef entry, GTACourseNode cNode);
	
	public boolean isTaskAssigned(TaskList taskList, String taskName);
	
	public List<String> getAssignedTasks(TaskList taskList);
	
	/**
	 * Select a task.
	 * 
	 * @param identity The assessed identity
	 * @param taskList The course element's task
	 * @param courseEnv The course environment
	 * @param cNode The course node
	 * @param task The document's task
	 * @param doerIdentity The user which does the operation
	 * @return
	 */
	public AssignmentResponse selectTask(Identity identity, TaskList taskList, CourseEnvironment courseEnv,
			GTACourseNode cNode, File task);
	
	public AssignmentResponse selectTask(BusinessGroup group, TaskList taskList, CourseEnvironment courseEnv,
			GTACourseNode cNode, File task, Identity doerIdentity);
	
	public AssignmentResponse assignTaskAutomatically(TaskList taskList, BusinessGroup assessedGroup, CourseEnvironment courseEnv,
			GTACourseNode cNode, Identity doerIdentity);

	public AssignmentResponse assignTaskAutomatically(TaskList taskList, Identity assessedIdentity, CourseEnvironment courseEnv,
			GTACourseNode cNode);

	public boolean isDueDateEnabled(GTACourseNode cNode);
	
	/**
	 * Check if the submission was done late.
	 * 
	 * @param task Task with a submission date mandatory
	 * @param assessedIdentity The assessed identity
	 * @param assessedGroup The assessed business group
	 * @param cNode The group task course element
	 * @param courseEntry The course repository entry
	 * @param withIndividualDueDate
	 * @return If the submission was done late
	 */
	public boolean isSubmissionLate(Task task, IdentityRef assessedIdentity, BusinessGroup assessedGroup,
			GTACourseNode cNode, RepositoryEntry courseEntry, boolean withIndividualDueDate);
	
	/**
	 * 
	 * @param submissionDate
	 * @param referenceDate
	 * @param referenceLateDate
	 * @param extensionDate
	 * @return
	 */
	public TaskLateStatus evaluateSubmissionLateStatus(Date submissionDate, Date referenceDate, Date referenceLateDate,
			Date extensionDate);
	
	/**
	 * Check if the submission was done in extended time.
	 * 
	 * @param task Task with a submission date mandatory
	 * @param assessedIdentity The assessed identity
	 * @param assessedGroup The assessed business group
	 * @param cNode The group task course element
	 * @param courseEntry The course repository entry
	 * @param withIndividualDueDate
	 * @return If the submission was done during the extension of time or not
	 */
	public boolean isSubmissionExtended(Task task, IdentityRef assessedIdentity, BusinessGroup assessedGroup,
			GTACourseNode cNode, RepositoryEntry courseEntry, boolean withIndividualDueDate);
	
	public DueDate getAssignmentDueDate(TaskRef task, IdentityRef assessedIdentity, BusinessGroup assessedGroup,
			GTACourseNode gtaNode, RepositoryEntry courseEntry, boolean withIndividualDueDate);
	
	public DueDate getSubmissionDueDate(TaskRef assignedTask, IdentityRef assessedIdentity, BusinessGroup assessedGroup,
			GTACourseNode cNode, RepositoryEntry courseEntry, boolean withIndividualDueDate);
	
	public DueDate getLateSubmissionDueDate(TaskRef assignedTask, IdentityRef assessedIdentity, BusinessGroup assessedGroup,
			GTACourseNode cNode, RepositoryEntry courseEntry, boolean withIndividualDueDate);
	
	public DueDate getSolutionDueDate(TaskRef assignedTask, IdentityRef assessedIdentity, BusinessGroup assessedGroup,
			GTACourseNode cNode, RepositoryEntry courseEntry, boolean withIndividualDueDate);
	
	/**
	 * 
	 * @param dueDate The due date (can be null)
	 * @param lateDueDate The late submission date (can be null)
	 * @return The latest due date 
	 */
	public Date getDeadlineOf(DueDate dueDate, DueDate lateDueDate);
	
	public TaskProcess firstStep(GTACourseNode cNode);

	public TaskProcess previousStep(TaskProcess currentStep, GTACourseNode cNode);
	
	public TaskProcess nextStep(TaskProcess currentStep, GTACourseNode cNode);

	/**
	 * Collect the task, increment number of attempts.
	 * 
	 * @param task The task
	 * @param cNode The course node
	 * @param numOfDocs The number of documents
	 * @param doerIdentity The user which do the operation
	 * @return The updatet task
	 */
	public Task collectTask(Task task, GTACourseNode cNode, int numOfDocs, Identity doerIdentity);
	
	/**
	 * Task is reviewed and accepted.
	 * 
	 * @param task
	 * @param cNode
	 * @return The updated task
	 */
	public Task reviewedTask(Task task, GTACourseNode cNode, Identity doerIdentity, Role by);
	
	/**
	 * 
	 */
	public Task collectRevisionTask(Task task, GTACourseNode cNode, int numOfDocs, Identity doerIdentity);
	
	public Task updateTask(Task task, TaskProcess newStatus, GTACourseNode cNode, boolean incrementUserAttempts, Identity doerIdentity, Role by);
	
	public TaskDueDate updateTaskDueDate(TaskDueDate taskDueDate);
	
	/**
	 * Submit the task to coach. The operation increments the number of attempts.
	 * 
	 * @param task The task
	 * @param cNode The course node
	 * @param numOfDocs The number of documents
	 * @param doerIdentity The user who does the operation
	 * @param by The role of the user who does the operation
	 * @return The updated task
	 */
	public Task submitTask(Task task, GTACourseNode cNode, int numOfDocs, Identity doerIdentity, Role by);
	
	/**
	 * Submit a revision. The operation increments the number of attempts.
	 * 
	 * @param task The task
	 * @param cNode The course node
	 * @param numOfDocs The number of documents
	 * @param doerIdentity The user who does the operation
	 * @param by The role of the user who does the operation
	 * @return
	 */
	public Task submitRevisions(Task task, GTACourseNode cNode, int numOfDocs, Identity doerIdentity, Role by);
	
	public Task updateTask(Task task, TaskProcess newStatus, int iteration, GTACourseNode cNode, boolean incrementUserAttempts, Identity doerIdentity, Role by);
	
	public Task allowResetTask(Task task, Identity allower, GTACourseNode cNode);
	
	public Task resetTask(Task task, GTACourseNode cNode, CourseEnvironment courseEnv, Identity doerIdentity);
	
	public Task resetTaskRefused(Task task, GTACourseNode cNode, Identity doerIdentity);
	
	public boolean toggleMark(RepositoryEntry entry, GTACourseNode gtaNode, Identity marker, Identity participant);

	public List<IdentityMark> getMarks(RepositoryEntry entry, GTACourseNode gtaNode, Identity marker);
	
	public boolean hasMarks(RepositoryEntry entry, GTACourseNode gtaNode, Identity marker);

	public void log(String step, String operation, Task assignedTask, Identity actor, Identity assessedIdentity, BusinessGroup assessedGroup,
			CourseEnvironment courseEnv, GTACourseNode cNode, Role by);
	
	public void log(String step, SubmitEvent event, Task assignedTask, Identity actor, Identity assessedIdentity, BusinessGroup assessedGroup,
			CourseEnvironment courseEnv, GTACourseNode cNode, Role by);

}
