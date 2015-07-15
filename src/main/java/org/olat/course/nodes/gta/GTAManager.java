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
import java.util.List;

import org.olat.basesecurity.IdentityRef;
import org.olat.core.commons.services.notifications.PublisherData;
import org.olat.core.commons.services.notifications.SubscriptionContext;
import org.olat.core.id.Identity;
import org.olat.core.util.vfs.VFSContainer;
import org.olat.course.nodes.GTACourseNode;
import org.olat.course.nodes.gta.model.Membership;
import org.olat.course.nodes.gta.ui.SubmitEvent;
import org.olat.course.run.environment.CourseEnvironment;
import org.olat.group.BusinessGroup;
import org.olat.group.BusinessGroupRef;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryEntryRef;

/**
 * 
 * Initial date: 24.02.2015<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public interface GTAManager {
	
	public VFSContainer getTasksContainer(CourseEnvironment courseEnv, GTACourseNode cNode);
	
	public File getTasksDirectory(CourseEnvironment courseEnv, GTACourseNode cNode);
	
	
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
	
	
	public SubscriptionContext getSubscriptionContext(CourseEnvironment courseEnv, GTACourseNode cNode);
	
	public PublisherData getPublisherData(CourseEnvironment courseEnv, GTACourseNode cNode);
	
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
	 * Are users already processing this task?
	 * 
	 * @param entry
	 * @param gtaNode
	 * @param taskName
	 * @return
	 */
	public boolean isTaskInProcess(RepositoryEntryRef entry, GTACourseNode gtaNode, String taskName);
	
	public TaskList createIfNotExists(RepositoryEntry entry, GTACourseNode cNode);
	
	public TaskList getTaskList(RepositoryEntryRef entry, GTACourseNode cNode);
	
	public int updateTaskName(TaskList taskList, String currentTaskName, String newTaskName);
	
	public int deleteTaskList(RepositoryEntryRef entry, GTACourseNode cNode);
	
	
	public Membership getMembership(IdentityRef identity, RepositoryEntryRef entry, GTACourseNode cNode);
	
	public Task getTask(IdentityRef identity, TaskList taskList);
	
	public Task getTask(BusinessGroupRef businessGroup, TaskList taskList);
	
	public Task createTask(String taskName, TaskList taskList, TaskProcess status, BusinessGroup assessedGroup, Identity assessedIdentity, GTACourseNode cNode);
	
	public Task nextStep(Task task, GTACourseNode cNode);
	
	

	public List<Task> getTasks(TaskList taskList, GTACourseNode gtaNode);
	
	public List<TaskLight> getTasksLight(RepositoryEntryRef entry, GTACourseNode gtaNode);

	
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
	
	public AssignmentResponse selectTask(Identity identity, TaskList taskList, GTACourseNode cNode, File task);
	
	public AssignmentResponse selectTask(BusinessGroup group, TaskList taskList, GTACourseNode cNode, File task);
	
	public AssignmentResponse assignTaskAutomatically(TaskList taskList, BusinessGroup assessedGroup, CourseEnvironment courseEnv, GTACourseNode cNode);

	public AssignmentResponse assignTaskAutomatically(TaskList taskList, Identity assessedIdentity, CourseEnvironment courseEnv, GTACourseNode cNode);

	public TaskProcess firstStep(GTACourseNode cNode);

	public TaskProcess previousStep(TaskProcess currentStep, GTACourseNode cNode);
	
	public TaskProcess nextStep(TaskProcess currentStep, GTACourseNode cNode);
	
	public Task updateTask(Task task, TaskProcess newStatus);
	
	public Task updateTask(Task task, TaskProcess newStatus, int iteration);
	
	public void log(String step, String operation, Task assignedTask, Identity actor, Identity assessedIdentity, BusinessGroup assessedGroup,
			CourseEnvironment courseEnv, GTACourseNode cNode);
	
	public void log(String step, SubmitEvent event, Task assignedTask, Identity actor, Identity assessedIdentity, BusinessGroup assessedGroup,
			CourseEnvironment courseEnv, GTACourseNode cNode);

}
