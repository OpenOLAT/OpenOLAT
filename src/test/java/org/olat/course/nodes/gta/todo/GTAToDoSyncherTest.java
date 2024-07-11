/**
 * <a href="https://www.openolat.org">
 * OpenOLAT - Online Learning and Training</a><br>
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); <br>
 * you may not use this file except in compliance with the License.<br>
 * You may obtain a copy of the License at the
 * <a href="https://www.apache.org/licenses/LICENSE-2.0">Apache homepage</a>
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
package org.olat.course.nodes.gta.todo;

import static org.assertj.core.api.Assertions.assertThat;
import static org.olat.test.JunitTestHelper.random;

import java.io.File;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import org.apache.logging.log4j.Logger;
import org.junit.Test;
import org.olat.basesecurity.GroupRoles;
import org.olat.core.commons.persistence.DB;
import org.olat.core.id.Identity;
import org.olat.core.id.IdentityEnvironment;
import org.olat.core.logging.Tracing;
import org.olat.core.util.DateUtils;
import org.olat.core.util.tree.TreeVisitor;
import org.olat.course.CourseFactory;
import org.olat.course.ICourse;
import org.olat.course.assessment.manager.UserCourseInformationsManager;
import org.olat.course.condition.ConditionNodeAccessProvider;
import org.olat.course.duedate.DueDateConfig;
import org.olat.course.duedate.DueDateService;
import org.olat.course.learningpath.LearningPathConfigs;
import org.olat.course.learningpath.LearningPathService;
import org.olat.course.learningpath.manager.LearningPathNodeAccessProvider;
import org.olat.course.nodes.CollectingVisitor;
import org.olat.course.nodes.CourseNode;
import org.olat.course.nodes.CourseNodeConfiguration;
import org.olat.course.nodes.CourseNodeFactory;
import org.olat.course.nodes.GTACourseNode;
import org.olat.course.nodes.SPCourseNode;
import org.olat.course.nodes.gta.AssignmentResponse;
import org.olat.course.nodes.gta.GTAManager;
import org.olat.course.nodes.gta.GTAType;
import org.olat.course.nodes.gta.Task;
import org.olat.course.nodes.gta.TaskProcess;
import org.olat.course.nodes.gta.model.TaskDefinition;
import org.olat.course.run.environment.CourseEnvironment;
import org.olat.course.run.userview.UserCourseEnvironment;
import org.olat.course.run.userview.UserCourseEnvironmentImpl;
import org.olat.course.todo.CourseNodeToDoHandler;
import org.olat.course.tree.CourseEditorTreeNode;
import org.olat.modules.assessment.AssessmentEntry;
import org.olat.modules.assessment.AssessmentService;
import org.olat.modules.assessment.Role;
import org.olat.modules.assessment.model.AssessmentEntryStatus;
import org.olat.modules.assessment.model.AssessmentObligation;
import org.olat.modules.todo.ToDoService;
import org.olat.modules.todo.ToDoStatus;
import org.olat.modules.todo.ToDoTask;
import org.olat.modules.todo.ToDoTaskSearchParams;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryEntryStatusEnum;
import org.olat.repository.RepositoryManager;
import org.olat.repository.manager.RepositoryEntryRelationDAO;
import org.olat.test.JunitTestHelper;
import org.olat.test.OlatTestCase;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 8 Nov 2023<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class GTAToDoSyncherTest extends OlatTestCase {

	private final Logger log = Tracing.createLoggerFor(GTAToDoSyncherTest.class);
	
	@Autowired
	private DB dbInstance;
	@Autowired
	private GTAManager gtaManager;
	@Autowired
	private RepositoryManager repositoryManager;
	@Autowired
	private RepositoryEntryRelationDAO repositoryEntryRelationDao;
	@Autowired
	private LearningPathService learningPathService;
	@Autowired
	private AssessmentService assessmentService;
	@Autowired
	private UserCourseInformationsManager userCourseInformationsManager;
	@Autowired
	private ToDoService toDoService;

	@Test
	public void shouldCreateAssignmentToDo() {
		RepositoryEntry courseEntry = createCourseEntry();
		UserCourseEnvironment userCourseEnv = addParticipant(courseEntry);
		
		// Evaluate the assessment data
		userCourseEnv.getScoreAccounting().evaluateAll(true);
		
		List<ToDoTask> toDoTasks = getToDoTasks(courseEntry);
		assertThat(toDoTasks).hasSize(1);
		ToDoTask toDoTask = toDoTasks.get(0);
		assertThat(toDoTask.getType()).isEqualTo(GTAAssignmentToDoProvider.TYPE);
		assertThat(toDoTask.getStatus()).isEqualTo(ToDoStatus.open);
	}
	
	@Test
	public void shouldNotCreateAssignmentToDo_not_todos_enabled() {
		RepositoryEntry courseEntry = createCourseEntry(false, true, true, true);
		UserCourseEnvironment userCourseEnv = addParticipant(courseEntry);
		
		userCourseEnv.getScoreAccounting().evaluateAll(true);
		assertThat(getToDoTask(courseEntry, GTAAssignmentToDoProvider.TYPE)).isNull();
	}
	
	@Test
	public void shouldNotCreateAssignmentToDo_not_learning_path() {
		RepositoryEntry courseEntry = createCourseEntry(true, false, true, true);
		UserCourseEnvironment userCourseEnv = addParticipant(courseEntry);
		
		userCourseEnv.getScoreAccounting().evaluateAll(true);
		assertThat(getToDoTask(courseEntry, GTAAssignmentToDoProvider.TYPE)).isNull();
	}
	
	@Test
	public void shouldNotCreateAssignmentToDo_excluded_obligation() {
		RepositoryEntry courseEntry = createCourseEntry();
		UserCourseEnvironment userCourseEnv = addParticipant(courseEntry);
		
		String subIdent = getGTANode(courseEntry).getIdent();
		Identity participant = userCourseEnv.getIdentityEnvironment().getIdentity();
		AssessmentEntry assessmentEntry = assessmentService.getOrCreateAssessmentEntry(participant, null, courseEntry, subIdent, Boolean.FALSE, null);
		assessmentEntry.getObligation().overrideConfig(AssessmentObligation.excluded, participant, new Date());
		assessmentService.updateAssessmentEntry(assessmentEntry);
		dbInstance.commitAndCloseSession();
		
		userCourseEnv.getScoreAccounting().evaluateAll(true);
		assertThat(getToDoTask(courseEntry, GTAAssignmentToDoProvider.TYPE)).isNull();
	}
	
	@Test
	public void shouldNotCreateAssignmentToDo_not_status_ready() {
		RepositoryEntry courseEntry = createCourseEntry();
		UserCourseEnvironment userCourseEnv = addParticipant(courseEntry);
		
		String subIdent = getSPNode(courseEntry).getIdent();
		Identity participant = userCourseEnv.getIdentityEnvironment().getIdentity();
		AssessmentEntry assessmentEntry = assessmentService.getOrCreateAssessmentEntry(participant, null, courseEntry, subIdent, Boolean.FALSE, null);
		assessmentEntry.setFullyAssessed(Boolean.FALSE);
		assessmentEntry.setAssessmentStatus(AssessmentEntryStatus.inProgress);
		assessmentService.updateAssessmentEntry(assessmentEntry);
		dbInstance.commitAndCloseSession();
		
		userCourseEnv.getScoreAccounting().evaluateAll(true);
		assertThat(getToDoTask(courseEntry, GTAAssignmentToDoProvider.TYPE)).isNull();
	}
	
	@Test
	public void shouldNotCreateAssignmentToDo_not_start_date_reached() {
		RepositoryEntry courseEntry = createCourseEntry();
		UserCourseEnvironment userCourseEnv = addParticipant(courseEntry);
		
		LearningPathConfigs learningPathConfigs = learningPathService.getConfigs(getGTANode(courseEntry), null);
		learningPathConfigs.setStartDateConfig(DueDateConfig.absolute(DateUtils.addDays(new Date(), 10)));
		
		userCourseEnv.getScoreAccounting().evaluateAll(true);
		assertThat(getToDoTask(courseEntry, GTAAssignmentToDoProvider.TYPE)).isNull();
	}
	
	@Test
	public void shouldNotCreateAssignmentToDo_not_course_start_visted() throws InterruptedException {
		RepositoryEntry courseEntry = createCourseEntry(true, true, true, false);
		UserCourseEnvironment userCourseEnv = addParticipant(courseEntry);
		
		userCourseEnv.getScoreAccounting().evaluateAll(true);
		assertThat(getToDoTask(courseEntry, GTAAssignmentToDoProvider.TYPE)).isNull();
		
		// User visits the course
		Identity participant = userCourseEnv.getIdentityEnvironment().getIdentity();
		userCourseInformationsManager.updateUserCourseInformations(courseEntry.getOlatResource(), participant);
		dbInstance.commitAndCloseSession();
		
		userCourseEnv.getScoreAccounting().evaluateAll(true);
		assertThat(getToDoTask(courseEntry, GTAAssignmentToDoProvider.TYPE)).isNotNull();
	}

	@Test
	public void shouldNotCreateAssignmentToDo_not_course_status_published() {
		RepositoryEntry courseEntry = createCourseEntry(true, true, false, true);
		UserCourseEnvironment userCourseEnv = addParticipant(courseEntry);
		
		// Evaluate the assessment data
		userCourseEnv.getScoreAccounting().evaluateAll(true);
		
		assertThat(getToDoTask(courseEntry, GTAAssignmentToDoProvider.TYPE)).isNull();
	}
	
	@Test
	public void shouldNotCreateAssignmentToDo_not_member() {
		RepositoryEntry courseEntry = createCourseEntry();
		
		Identity participant = JunitTestHelper.createAndPersistIdentityAsRndUser(random());
		dbInstance.commitAndCloseSession();
		IdentityEnvironment identityEnv = new IdentityEnvironment();
		identityEnv.setIdentity(participant);
		UserCourseEnvironment userCourseEnv = new UserCourseEnvironmentImpl(identityEnv,  CourseFactory.loadCourse(courseEntry).getCourseEnvironment());
		
		// Evaluate the assessment data
		userCourseEnv.getScoreAccounting().evaluateAll(true);
		
		assertThat(getToDoTask(courseEntry, GTAAssignmentToDoProvider.TYPE)).isNull();
	}
	
	@Test
	public void shouldNotCreateAssignmentToDo_task_done() {
		RepositoryEntry courseEntry = createCourseEntry();
		UserCourseEnvironment userCourseEnv = addParticipant(courseEntry);
		
		Task task = assignTask(userCourseEnv);
		gtaManager.updateTask(task, TaskProcess.solution, 1, getGTANode(courseEntry), false, userCourseEnv.getIdentityEnvironment().getIdentity(), Role.coach);
		dbInstance.commitAndCloseSession();
		
		// Evaluate the assessment data
		userCourseEnv.getScoreAccounting().evaluateAll(true);
		
		assertThat(getToDoTask(courseEntry, GTAAssignmentToDoProvider.TYPE)).isNull();
	}
	
	@Test
	public void shouldSetAssignmentToDoDoneWhenAssigned() {
		// Assignment to-do is created
		RepositoryEntry courseEntry = createCourseEntry();
		UserCourseEnvironment userCourseEnv = addParticipant(courseEntry);
		userCourseEnv.getScoreAccounting().evaluateAll(true);
		assertThat(getToDoTask(courseEntry, GTAAssignmentToDoProvider.TYPE).getStatus()).isEqualTo(ToDoStatus.open);
		
		// User assigns task
		assignTask(userCourseEnv);
		
		// To-do is done
		userCourseEnv.getScoreAccounting().evaluateAll(true);
		
		List<ToDoTask> toDoTasks = getToDoTasks(courseEntry);
		log.info("Task found: {} for course: {}", toDoTasks.size(), courseEntry.getKey());
		for(ToDoTask toDoTask:toDoTasks) {
			log.info("Task found: {} for course: {}, element: {} and title: {}", toDoTask.getKey(), toDoTask.getOriginId(), toDoTask.getOriginSubPath(), toDoTask.getTitle());
		}
		assertThat(toDoTasks).hasSize(1);
		assertThat(getToDoTask(courseEntry, GTAAssignmentToDoProvider.TYPE).getStatus()).isEqualTo(ToDoStatus.done);
	}
	
	@Test
	public void shouldSetAssignmentToDoDeletedWhenConfigDisabled() {
		// Assignment to-do is created
		RepositoryEntry courseEntry = createCourseEntry();
		UserCourseEnvironment userCourseEnv = addParticipant(courseEntry);
		userCourseEnv.getScoreAccounting().evaluateAll(true);
		waitMessageAreConsumed();
		
		List<ToDoTask> toDoTasks = getToDoTasks(courseEntry);
		assertThat(toDoTasks).hasSize(1);
		assertThat(getToDoTask(courseEntry, GTAAssignmentToDoProvider.TYPE).getStatus()).isEqualTo(ToDoStatus.open);
		
		// Assignment step is disabled
		enableSteps(userCourseEnv, false, false, false);
		
		// To-do is deleted
		userCourseEnv.getScoreAccounting().evaluateAll(true);
		assertThat(getToDoTask(courseEntry, GTAAssignmentToDoProvider.TYPE).getStatus()).isEqualTo(ToDoStatus.deleted);
		
		// Assignment step is enabled again
		enableSteps(userCourseEnv, true, false, false);
		
		// To-do is open again
		userCourseEnv.getScoreAccounting().evaluateAll(true);
		assertThat(getToDoTask(courseEntry, GTAAssignmentToDoProvider.TYPE).getStatus()).isEqualTo(ToDoStatus.open);
	}
	
	@Test
	public void shouldCreateSubmitToDo() {
		RepositoryEntry courseEntry = createCourseEntry();
		UserCourseEnvironment userCourseEnv = addParticipant(courseEntry);
		enableSteps(userCourseEnv, false, true, false);
		userCourseEnv.getScoreAccounting().evaluateAll(true);
		assertThat(getToDoTask(courseEntry, GTASubmitToDoProvider.TYPE).getStatus()).isEqualTo(ToDoStatus.open);
	}
	
	@Test
	public void shouldCreateSubmitToDoOnlyIfAssignmentIsDone() {
		RepositoryEntry courseEntry = createCourseEntry();
		UserCourseEnvironment userCourseEnv = addParticipant(courseEntry);
		enableSteps(userCourseEnv, true, true, false);
		userCourseEnv.getScoreAccounting().evaluateAll(true);
		assertThat(getToDoTask(courseEntry, GTASubmitToDoProvider.TYPE)).isNull();
		
		assignTask(userCourseEnv);
		
		userCourseEnv.getScoreAccounting().evaluateAll(true);
		assertThat(getToDoTask(courseEntry, GTASubmitToDoProvider.TYPE).getStatus()).isEqualTo(ToDoStatus.open);
	}
	
	@Test
	public void shouldCreateRevisionToDoAndSetToDone() {
		RepositoryEntry courseEntry = createCourseEntry();
		UserCourseEnvironment userCourseEnv = addParticipant(courseEntry);
		enableSteps(userCourseEnv, true, false, true);
		Task task = assignTask(userCourseEnv);
		
		// Task is in review
		userCourseEnv.getScoreAccounting().evaluateAll(true);
		assertThat(getToDoTask(courseEntry, GTARevisionToDoProvider.TYPE)).isNull();
		
		// Task needs revision
		gtaManager.updateTask(task, TaskProcess.revision, 1, getGTANode(courseEntry), false, userCourseEnv.getIdentityEnvironment().getIdentity(), Role.coach);
		dbInstance.commitAndCloseSession();
		
		// Revision to-do is created
		userCourseEnv.getScoreAccounting().evaluateAll(true);
		assertThat(getToDoTask(courseEntry, GTARevisionToDoProvider.TYPE).getStatus()).isEqualTo(ToDoStatus.open);
		
		// Revision is accepted
		gtaManager.updateTask(task, TaskProcess.solution, 1, getGTANode(courseEntry), false, userCourseEnv.getIdentityEnvironment().getIdentity(), Role.coach);
		dbInstance.commitAndCloseSession();
		
		// Revision to-do is done
		userCourseEnv.getScoreAccounting().evaluateAll(true);
		assertThat(getToDoTask(courseEntry, GTARevisionToDoProvider.TYPE).getStatus()).isEqualTo(ToDoStatus.done);
	}
	
	@Test
	public void shouldNotCreateRevisionToDoIfNoPreviousSteps() {
		RepositoryEntry courseEntry = createCourseEntry();
		UserCourseEnvironment userCourseEnv = addParticipant(courseEntry);
		enableSteps(userCourseEnv, false, false, true);
		
		userCourseEnv.getScoreAccounting().evaluateAll(true);
		assertThat(getToDoTask(courseEntry, GTARevisionToDoProvider.TYPE)).isNull();
	}
	
	private RepositoryEntry createCourseEntry() {
		return createCourseEntry(true, true, true, true);
	}
	
	private RepositoryEntry createCourseEntry(boolean toDosEnabled, boolean learningPath, boolean statusPublished, boolean absoluteDates) {
		Identity doer = JunitTestHelper.createAndPersistIdentityAsUser(random());
		RepositoryEntry courseEntry = JunitTestHelper.deployBasicCourse(doer);
		ICourse course = CourseFactory.loadCourse(courseEntry);
		dbInstance.commitAndCloseSession();
		
		// create the course node and a task definition
		CourseFactory.openCourseEditSession(course.getResourceableId());
		if (learningPath) {
			course.getCourseConfig().setNodeAccessType(LearningPathNodeAccessProvider.TYPE);
		} else {
			course.getCourseConfig().setNodeAccessType(ConditionNodeAccessProvider.TYPE);
		}
		CourseNodeConfiguration newNodeConfig = CourseNodeFactory.getInstance().getCourseNodeConfiguration(GTACourseNode.TYPE_INDIVIDUAL);
		GTACourseNode gtaNode = (GTACourseNode)newNodeConfig.getInstance();
		gtaNode.setShortTitle("Bake");
		gtaNode.getModuleConfiguration().setStringValue(GTACourseNode.GTASK_TYPE, GTAType.individual.name());
		gtaNode.getModuleConfiguration().setBooleanEntry(GTACourseNode.GTASK_ASSIGNMENT, true);
		gtaNode.getModuleConfiguration().setBooleanEntry(GTACourseNode.GTASK_SAMPLE_SOLUTION, true);
		if (toDosEnabled) {
			gtaNode.getModuleConfiguration().setBooleanEntry(CourseNodeToDoHandler.COURSE_NODE_TODOS_ENABLED, true);
		}
		if (!absoluteDates) {
			gtaNode.getModuleConfiguration().setBooleanEntry(GTACourseNode.GTASK_RELATIVE_DATES, true);
			gtaNode.getModuleConfiguration().setIntValue(GTACourseNode.GTASK_ASSIGNMENT_DEADLINE_RELATIVE, 10);
			gtaNode.getModuleConfiguration().setStringValue(GTACourseNode.GTASK_ASSIGNMENT_DEADLINE_RELATIVE_TO, DueDateService.TYPE_COURSE_LAUNCH);
			gtaNode.getModuleConfiguration().setIntValue(GTACourseNode.GTASK_SUBMIT_DEADLINE_RELATIVE, 10);
			gtaNode.getModuleConfiguration().setStringValue(GTACourseNode.GTASK_SUBMIT_DEADLINE_RELATIVE_TO, DueDateService.TYPE_COURSE_LAUNCH);
		}
		// This let's publish the course node without files
		gtaNode.getModuleConfiguration().setBooleanEntry(GTACourseNode.GTASK_COACH_ALLOWED_UPLOAD_TASKS, true);
		CourseNode rootNode = course.getEditorTreeModel().getCourseNode(course.getRunStructure().getRootNode().getIdent());
		course.getEditorTreeModel().addCourseNode(gtaNode, rootNode);
		
		TaskDefinition taskDefinition = new TaskDefinition();
		taskDefinition.setTitle(random());
		gtaManager.addTaskDefinition(taskDefinition, course.getCourseEnvironment(), gtaNode);
		dbInstance.commitAndCloseSession();
		
		CourseFactory.closeCourseEditSession(course.getResourceableId(), false);
		
		// publish
		RepositoryEntryStatusEnum status = statusPublished? RepositoryEntryStatusEnum.published: RepositoryEntryStatusEnum.preparation;
		CourseFactory.publishCourse(course, status, doer, Locale.ENGLISH);
		dbInstance.commitAndCloseSession();
		
		waitMessageAreConsumed();// Try it
		
		return repositoryManager.lookupRepositoryEntry(courseEntry.getKey());
	}
	
	private GTACourseNode getGTANode(RepositoryEntry courseEntry) {
		CollectingVisitor visitor = CollectingVisitor.testing(node -> node instanceof GTACourseNode);
		new TreeVisitor(visitor, CourseFactory.loadCourse(courseEntry).getRunStructure().getRootNode(), true).visitAll();
		return (GTACourseNode)visitor.getCourseNodes().get(0);
	}
	
	private SPCourseNode getSPNode(RepositoryEntry courseEntry) {
		CollectingVisitor visitor = CollectingVisitor.testing(node -> node instanceof SPCourseNode);
		new TreeVisitor(visitor, CourseFactory.loadCourse(courseEntry).getRunStructure().getRootNode(), true).visitAll();
		return (SPCourseNode)visitor.getCourseNodes().get(0);
	}
	
	private void enableSteps(UserCourseEnvironment userCourseEnv, boolean assignment, boolean submit, boolean revision) {
		RepositoryEntry courseEntry = userCourseEnv.getCourseEnvironment().getCourseGroupManager().getCourseEntry();
		GTACourseNode gtaNode = getGTANode(courseEntry);
		ICourse course = CourseFactory.loadCourse(courseEntry);
		CourseFactory.openCourseEditSession(course.getResourceableId());
		CourseEditorTreeNode editorTreeNode = course.getEditorTreeModel().getCourseEditorNodeById(gtaNode.getIdent());
		editorTreeNode.getCourseNode().getModuleConfiguration().setBooleanEntry(GTACourseNode.GTASK_ASSIGNMENT, assignment);
		editorTreeNode.getCourseNode().getModuleConfiguration().setBooleanEntry(GTACourseNode.GTASK_SUBMIT, submit);
		editorTreeNode.getCourseNode().getModuleConfiguration().setBooleanEntry(GTACourseNode.GTASK_REVIEW_AND_CORRECTION, revision);
		editorTreeNode.getCourseNode().getModuleConfiguration().setBooleanEntry(GTACourseNode.GTASK_REVISION_PERIOD, revision);
		editorTreeNode.setDirty(true);
		CourseFactory.closeCourseEditSession(course.getResourceableId(), false);
		CourseFactory.publishCourse(course, courseEntry.getEntryStatus(), userCourseEnv.getIdentityEnvironment().getIdentity(), Locale.ENGLISH);
		dbInstance.commitAndCloseSession();

		// Wait that task element are updated after publishing
		waitMessageAreConsumed();
	}

	private Task assignTask(UserCourseEnvironment userCourseEnv) {
		RepositoryEntry courseEntry = userCourseEnv.getCourseEnvironment().getCourseGroupManager().getCourseEntry();
		Identity identity = userCourseEnv.getIdentityEnvironment().getIdentity();
		CourseEnvironment courseEnv = userCourseEnv.getCourseEnvironment();
		GTACourseNode gtaNode = getGTANode(courseEntry);
		AssignmentResponse assignmentResponse = gtaManager.selectTask(identity, gtaManager.getTaskList(courseEntry, gtaNode), courseEnv, gtaNode, new File(random()));
		dbInstance.commitAndCloseSession();
		return assignmentResponse.getTask();
	}
	
	private UserCourseEnvironment addParticipant(RepositoryEntry courseEntry) {
		Identity participant = JunitTestHelper.createAndPersistIdentityAsRndUser(random());
		repositoryEntryRelationDao.addRole(participant, courseEntry, GroupRoles.participant.name());
		dbInstance.commitAndCloseSession();
		
		// First course element is a SPCourseNode
		String subIdent = getSPNode(courseEntry).getIdent();
		AssessmentEntry assessmentEntry = assessmentService.getOrCreateAssessmentEntry(participant, null, courseEntry, subIdent, Boolean.FALSE, null);
		assessmentEntry.setAssessmentStatus(AssessmentEntryStatus.done);
		assessmentEntry.setFullyAssessed(Boolean.TRUE);
		assessmentService.updateAssessmentEntry(assessmentEntry);
		dbInstance.commitAndCloseSession();
		
		IdentityEnvironment identityEnv = new IdentityEnvironment();
		identityEnv.setIdentity(participant);
		return new UserCourseEnvironmentImpl(identityEnv,  CourseFactory.loadCourse(courseEntry).getCourseEnvironment());
	}
	
	private ToDoTask getToDoTask(RepositoryEntry courseEntry, String type) {
		return getToDoTasks(courseEntry).stream().filter(toDoTask -> toDoTask.getType().equals(type)).findFirst().orElse(null);
	}

	private List<ToDoTask> getToDoTasks(RepositoryEntry courseEntry) {
		ToDoTaskSearchParams searchParams = new ToDoTaskSearchParams();
		searchParams.setOriginIds(List.of(courseEntry.getKey()));
		return toDoService.getToDoTasks(searchParams);
	}

}
