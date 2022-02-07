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

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import javax.persistence.CacheRetrieveMode;
import javax.persistence.CacheStoreMode;
import javax.persistence.LockModeType;
import javax.persistence.Query;

import org.apache.logging.log4j.Logger;
import org.olat.basesecurity.GroupRoles;
import org.olat.basesecurity.IdentityRef;
import org.olat.core.commons.modules.bc.FolderConfig;
import org.olat.core.commons.persistence.DB;
import org.olat.core.commons.persistence.QueryBuilder;
import org.olat.core.commons.services.notifications.NotificationsManager;
import org.olat.core.commons.services.notifications.PublisherData;
import org.olat.core.commons.services.notifications.SubscriptionContext;
import org.olat.core.id.Identity;
import org.olat.core.logging.Tracing;
import org.olat.core.util.FileUtils;
import org.olat.core.util.StringHelper;
import org.olat.core.util.io.SystemFilenameFilter;
import org.olat.core.util.vfs.VFSContainer;
import org.olat.core.util.vfs.VFSItem;
import org.olat.core.util.vfs.VFSLeaf;
import org.olat.core.util.vfs.VFSManager;
import org.olat.core.util.xml.XStreamHelper;
import org.olat.course.CourseFactory;
import org.olat.course.ICourse;
import org.olat.course.assessment.AssessmentHelper;
import org.olat.course.assessment.CourseAssessmentService;
import org.olat.course.duedate.DueDateConfig;
import org.olat.course.duedate.DueDateService;
import org.olat.course.duedate.RelativeDueDateConfig;
import org.olat.course.nodes.GTACourseNode;
import org.olat.course.nodes.gta.AssignmentResponse;
import org.olat.course.nodes.gta.AssignmentResponse.Status;
import org.olat.course.nodes.gta.GTAManager;
import org.olat.course.nodes.gta.GTAType;
import org.olat.course.nodes.gta.IdentityMark;
import org.olat.course.nodes.gta.Task;
import org.olat.course.nodes.gta.TaskDueDate;
import org.olat.course.nodes.gta.TaskLight;
import org.olat.course.nodes.gta.TaskList;
import org.olat.course.nodes.gta.TaskProcess;
import org.olat.course.nodes.gta.TaskRef;
import org.olat.course.nodes.gta.TaskRevision;
import org.olat.course.nodes.gta.TaskRevisionDate;
import org.olat.course.nodes.gta.model.DueDate;
import org.olat.course.nodes.gta.model.Membership;
import org.olat.course.nodes.gta.model.Solution;
import org.olat.course.nodes.gta.model.SolutionList;
import org.olat.course.nodes.gta.model.TaskDefinition;
import org.olat.course.nodes.gta.model.TaskDefinitionList;
import org.olat.course.nodes.gta.model.TaskDueDateImpl;
import org.olat.course.nodes.gta.model.TaskImpl;
import org.olat.course.nodes.gta.model.TaskListImpl;
import org.olat.course.nodes.gta.model.TaskRevisionImpl;
import org.olat.course.nodes.gta.ui.events.SubmitEvent;
import org.olat.course.run.environment.CourseEnvironment;
import org.olat.course.run.scoring.AssessmentEvaluation;
import org.olat.course.run.userview.UserCourseEnvironment;
import org.olat.group.BusinessGroup;
import org.olat.group.BusinessGroupRef;
import org.olat.group.BusinessGroupService;
import org.olat.group.DeletableGroupData;
import org.olat.group.area.BGAreaManager;
import org.olat.group.manager.BusinessGroupRelationDAO;
import org.olat.group.model.BusinessGroupRefImpl;
import org.olat.modules.ModuleConfiguration;
import org.olat.modules.assessment.Role;
import org.olat.modules.assessment.model.AssessmentEntryStatus;
import org.olat.modules.edusharing.EdusharingService;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryEntryRef;
import org.olat.repository.manager.RepositoryEntryRelationDAO;
import org.olat.repository.ui.settings.LazyRepositoryEdusharingProvider;
import org.olat.resource.OLATResource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.security.ExplicitTypePermission;

/**
 * 
 * Initial date: 24.02.2015<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
@Service
public class GTAManagerImpl implements GTAManager, DeletableGroupData {
	
	private static final Logger log = Tracing.createLoggerFor(GTAManagerImpl.class);
	
	private static final XStream taskDefinitionsXstream = XStreamHelper.createXStreamInstance();
	static {
		Class<?>[] types = new Class[] {
				Solution.class, SolutionList.class, TaskDefinition.class, TaskDefinitionList.class
			};
		taskDefinitionsXstream.addPermission(new ExplicitTypePermission(types));
	}
	
	@Autowired
	private DB dbInstance;
	@Autowired
	private GTAIdentityMarkDAO gtaMarkDao;
	@Autowired
	private GTATaskRevisionDAO taskRevisionDao;
	@Autowired
	private GTATaskRevisionDateDAO taskRevisionDateDao;
	@Autowired
	private BGAreaManager areaManager;
	@Autowired
	private CourseAssessmentService courseAssessmentService;
	@Autowired
	private EdusharingService edusharingService;
	@Autowired
	private DueDateService dueDateService;
	@Autowired
	private BusinessGroupService businessGroupService;
	@Autowired
	private NotificationsManager notificationsManager;
	@Autowired
	private BusinessGroupRelationDAO businessGroupRelationDao;
	@Autowired
	private RepositoryEntryRelationDAO repositoryEntryRelationDao;

	/**
	 * Get the task folder path relative to the folder root for a specific node.
	 * 
	 * @param courseEnv
	 * @param cNode
	 * @return the task folder path relative to the folder root.
	 */
	@Override
	public VFSContainer getTasksContainer(CourseEnvironment courseEnv, GTACourseNode cNode) {
		return getContainer(courseEnv, "tasks", cNode);
	}
	
	@Override
	public File getTasksDirectory(CourseEnvironment courseEnv, GTACourseNode cNode) {
		Path path = Paths.get(FolderConfig.getCanonicalRoot(), courseEnv.getCourseBaseContainer().getRelPath(),
				"gtasks", cNode.getIdent(), "tasks");
		File file = path.toFile();
		if(!file.exists()) {
			file.mkdirs();
		}
		return file;
	}

	@Override
	public List<TaskDefinition> getTaskDefinitions(CourseEnvironment courseEnv, GTACourseNode cNode) {
		Path taskDefinitionsPath = Paths.get(FolderConfig.getCanonicalRoot(), courseEnv.getCourseBaseContainer().getRelPath(),
				"gtasks", cNode.getIdent(), TASKS_DEFINITIONS);

		List<TaskDefinition> taskDefinitions = new ArrayList<>();
		if(Files.exists(taskDefinitionsPath)) {
			TaskDefinitionList taskDefinitionsList = (TaskDefinitionList)taskDefinitionsXstream.fromXML(taskDefinitionsPath.toFile());
			if(taskDefinitionsList != null && taskDefinitionsList.getTasks() != null) {
				taskDefinitions.addAll(taskDefinitionsList.getTasks());
			}
		} else {
			syncWithTaskList(courseEnv, cNode, () -> {
				ModuleConfiguration config = cNode.getModuleConfiguration();
				TaskDefinitionList tasks = (TaskDefinitionList)config.get(GTACourseNode.GTASK_TASKS);
				if(tasks != null) {
					taskDefinitions.addAll(tasks.getTasks());
				}
				storeTaskDefinitions(taskDefinitions, courseEnv, cNode);
			});
		}
		return	taskDefinitions;
	}

	@Override
	public void addTaskDefinition(TaskDefinition newTask, CourseEnvironment courseEnv, GTACourseNode cNode) {
		syncWithTaskList(courseEnv, cNode, () -> {
			List<TaskDefinition> taskDefinitions = getTaskDefinitions(courseEnv, cNode);
			taskDefinitions.add(newTask);
			storeTaskDefinitions(taskDefinitions, courseEnv, cNode);
		});
	}

	@Override
	public void removeTaskDefinition(TaskDefinition removedTask, CourseEnvironment courseEnv, GTACourseNode cNode) {
		syncWithTaskList(courseEnv, cNode, () -> {
			List<TaskDefinition> taskDefinitions = getTaskDefinitions(courseEnv, cNode);
			boolean deleteFile = true;
			for(int i=taskDefinitions.size(); i-->0; ) {
				if(taskDefinitions.get(i).equals(removedTask)) {
					taskDefinitions.remove(i);
				}
			}

			for(int i=taskDefinitions.size(); i-->0; ) {
				if(taskDefinitions.get(i).getFilename().equals(removedTask.getFilename())) {
					deleteFile = false;
				}
			}

			if(deleteFile) {
				VFSContainer tasksContainer = getTasksContainer(courseEnv, cNode);
				VFSItem item = tasksContainer.resolve(removedTask.getFilename());
				if(item != null) {
					deleteEdusharingUsages(courseEnv, item);
					item.delete();
				}
			}
			storeTaskDefinitions(taskDefinitions, courseEnv, cNode);
		});
	}
	
	private void deleteEdusharingUsages(CourseEnvironment courseEnv, VFSItem item) {
		Long repositoryEntryKey = courseEnv.getCourseGroupManager().getCourseEntry().getKey();
		LazyRepositoryEdusharingProvider edusharingProvider = new LazyRepositoryEdusharingProvider(repositoryEntryKey);
		edusharingProvider.setSubPath(item);
		edusharingService.deleteUsages(edusharingProvider);
	}

	@Override
	public void updateTaskDefinition(String currentFilename, TaskDefinition task, CourseEnvironment courseEnv, GTACourseNode cNode) {
		syncWithTaskList( courseEnv, cNode, () -> {
			String filename = currentFilename == null ? task.getFilename() : currentFilename;
			List<TaskDefinition> taskDefinitions = getTaskDefinitions(courseEnv, cNode);
			for(int i=taskDefinitions.size(); i-->0; ) {
				if(taskDefinitions.get(i).getFilename().equals(filename)) {
					taskDefinitions.set(i, task);
					break;
				}
			}
			storeTaskDefinitions(taskDefinitions, courseEnv, cNode);
		});
	}
	
	private void storeTaskDefinitions(List<TaskDefinition> taskDefinitions, CourseEnvironment courseEnv, GTACourseNode cNode) {
		TaskDefinitionList list = new TaskDefinitionList();
		list.setTasks(taskDefinitions);
		
		Path taskDefinitionsPath = Paths.get(FolderConfig.getCanonicalRoot(), courseEnv.getCourseBaseContainer().getRelPath(),
				"gtasks", cNode.getIdent(), TASKS_DEFINITIONS);
		File taskDefinitionsFile = taskDefinitionsPath.toFile();
		if(!taskDefinitionsFile.getParentFile().exists()) {
			taskDefinitionsFile.getParentFile().mkdirs();
		}
		XStreamHelper.writeObject(taskDefinitionsXstream, taskDefinitionsFile, list);
	}

	@Override
	public File getSolutionsDirectory(CourseEnvironment courseEnv, GTACourseNode cNode) {
		Path path = Paths.get(FolderConfig.getCanonicalRoot(), courseEnv.getCourseBaseContainer().getRelPath(),
				"gtasks", cNode.getIdent(), "solutions");
		File file = path.toFile();
		if(!file.exists()) {
			file.mkdirs();
		}
		return file;
	}

	@Override
	public VFSContainer getSolutionsContainer(CourseEnvironment courseEnv, GTACourseNode cNode) {
		return getContainer(courseEnv, "solutions", cNode);
	}

	@Override
	public List<Solution> getSolutions(CourseEnvironment courseEnv, GTACourseNode cNode) {
		Path solutionDefinitionsPath = Paths.get(FolderConfig.getCanonicalRoot(), courseEnv.getCourseBaseContainer().getRelPath(),
				"gtasks", cNode.getIdent(), SOLUTIONS_DEFINITIONS);

		List<Solution> solutionsDefinitions = new ArrayList<>();
		if(Files.exists(solutionDefinitionsPath)) {
			SolutionList solutionDefinitionsList = (SolutionList)taskDefinitionsXstream.fromXML(solutionDefinitionsPath.toFile());
			if(solutionDefinitionsList != null && solutionDefinitionsList.getSolutions() != null) {
				solutionsDefinitions.addAll(solutionDefinitionsList.getSolutions());
			}
		} else {
			syncWithTaskList(courseEnv, cNode, () -> {
				ModuleConfiguration config = cNode.getModuleConfiguration();
				SolutionList solutions = (SolutionList)config.get(GTACourseNode.GTASK_SOLUTIONS);
				if(solutions != null && solutions.getSolutions() != null) {
					solutionsDefinitions.addAll(solutions.getSolutions());
				}
				storeSolutions(solutionsDefinitions, courseEnv, cNode);
			});
		}
		return solutionsDefinitions;
	}

	@Override
	public void addSolution(Solution newSolution, CourseEnvironment courseEnv, GTACourseNode cNode) {
		syncWithTaskList( courseEnv, cNode, () -> {
			List<Solution> solutions = getSolutions(courseEnv, cNode);
			solutions.add(newSolution);
			storeSolutions(solutions, courseEnv, cNode);
		});
	}

	@Override
	public void removeSolution(Solution removedSolution, CourseEnvironment courseEnv, GTACourseNode cNode) {
		syncWithTaskList( courseEnv, cNode, () -> {
			List<Solution> solutions = getSolutions(courseEnv, cNode);
			for(int i=solutions.size(); i-->0; ) {
				if(solutions.get(i).getFilename().equals(removedSolution.getFilename())) {
					solutions.remove(i);
					break;
				}
			}
			storeSolutions(solutions, courseEnv, cNode);
		});
	}
	
	@Override
	public void updateSolution(String currentFilename, Solution solution, CourseEnvironment courseEnv, GTACourseNode cNode) {
		syncWithTaskList( courseEnv, cNode, () -> {
			String filename = currentFilename == null ? solution.getFilename() : currentFilename;
			List<Solution> solutions = getSolutions(courseEnv, cNode);
			for(int i=solutions.size(); i-->0; ) {
				if(solutions.get(i).getFilename().equals(filename)) {
					solutions.set(i, solution);
					break;
				}
			}
			storeSolutions(solutions, courseEnv, cNode);
		});
	}
	
	private void storeSolutions(List<Solution> solutions, CourseEnvironment courseEnv, GTACourseNode cNode) {
		SolutionList list = new SolutionList();
		list.setSolutions(solutions);
		
		Path solutionsPath = Paths.get(FolderConfig.getCanonicalRoot(), courseEnv.getCourseBaseContainer().getRelPath(),
				"gtasks", cNode.getIdent(), SOLUTIONS_DEFINITIONS);
		XStreamHelper.writeObject(taskDefinitionsXstream, solutionsPath.toFile(), list);
	}
	
	private void syncWithTaskList(CourseEnvironment courseEnv, GTACourseNode cNode, TaskListSynched synched) {
		TaskList tasks = getTaskList(courseEnv.getCourseGroupManager().getCourseEntry(), cNode);
		if(tasks != null) {
			loadForUpdate(tasks);
			synched.sync();
			dbInstance.commit();
		} else {
			synched.sync();
		}
	}

	@Override
	public File getSubmitDirectory(CourseEnvironment courseEnv, GTACourseNode cNode, IdentityRef person) {
		Path path = Paths.get(FolderConfig.getCanonicalRoot(), courseEnv.getCourseBaseContainer().getRelPath(),
				"gtasks", cNode.getIdent(), "dropboxes", "person_" + person.getKey());
		File file = path.toFile();
		if(!file.exists()) {
			file.mkdirs();
		}
		return file;
	}

	@Override
	public File getSubmitDirectory(CourseEnvironment courseEnv, GTACourseNode cNode, BusinessGroupRef group) {
		Path path = Paths.get(FolderConfig.getCanonicalRoot(), courseEnv.getCourseBaseContainer().getRelPath(),
				"gtasks", cNode.getIdent(), "dropboxes", "bgroup_" + group.getKey());
		File file = path.toFile();
		if(!file.exists()) {
			file.mkdirs();
		}
		return file;
	}

	@Override
	public VFSContainer getSubmitContainer(CourseEnvironment courseEnv, GTACourseNode cNode, IdentityRef person) {
		VFSContainer container = getContainer(courseEnv, "dropboxes", cNode);
		String subFolder = "person_" + person.getKey();
		return VFSManager.getOrCreateContainer(container, subFolder);
	}

	@Override
	public VFSContainer getSubmitContainer(CourseEnvironment courseEnv, GTACourseNode cNode, BusinessGroupRef group) {
		VFSContainer container = getContainer(courseEnv, "dropboxes", cNode);
		String subFolder = "bgroup_" + group.getKey();
		return VFSManager.getOrCreateContainer(container, subFolder);
	}

	@Override
	public File getCorrectionDirectory(CourseEnvironment courseEnv, GTACourseNode cNode, IdentityRef person) {
		Path path = Paths.get(FolderConfig.getCanonicalRoot(), courseEnv.getCourseBaseContainer().getRelPath(),
				"gtasks", cNode.getIdent(), "corrections", "person_" + person.getKey());
		File file = path.toFile();
		if(!file.exists()) {
			file.mkdirs();
		}
		return file;
	}

	@Override
	public VFSContainer getCorrectionContainer(CourseEnvironment courseEnv, GTACourseNode cNode, IdentityRef person) {
		VFSContainer container = getContainer(courseEnv, "corrections", cNode);
		String subFolder = "person_" + person.getKey();
		return VFSManager.getOrCreateContainer(container, subFolder);
	}

	@Override
	public File getCorrectionDirectory(CourseEnvironment courseEnv, GTACourseNode cNode, BusinessGroupRef group) {
		Path path = Paths.get(FolderConfig.getCanonicalRoot(), courseEnv.getCourseBaseContainer().getRelPath(),
				"gtasks", cNode.getIdent(), "corrections", "bgroup_" + group.getKey());
		File file = path.toFile();
		if(!file.exists()) {
			file.mkdirs();
		}
		return file;
	}

	@Override
	public VFSContainer getCorrectionContainer(CourseEnvironment courseEnv, GTACourseNode cNode, BusinessGroupRef group) {
		VFSContainer container = getContainer(courseEnv, "corrections", cNode);
		String subFolder = "bgroup_" + group.getKey();
		return VFSManager.getOrCreateContainer(container, subFolder);
	}

	@Override
	public File getRevisedDocumentsDirectory(CourseEnvironment courseEnv, GTACourseNode cNode, int iteration, IdentityRef person) {
		Path path = Paths.get(FolderConfig.getCanonicalRoot(), courseEnv.getCourseBaseContainer().getRelPath(),
				"gtasks", cNode.getIdent(), "revisions_" + iteration, "person_" + person.getKey());
		File file = path.toFile();
		if(!file.exists()) {
			file.mkdirs();
		}
		return file;
	}

	@Override
	public VFSContainer getRevisedDocumentsContainer(CourseEnvironment courseEnv, GTACourseNode cNode, int iteration, IdentityRef person) {
		VFSContainer container = getContainer(courseEnv, "revisions_" + iteration, cNode);
		return VFSManager.getOrCreateContainer(container, "person_" + person.getKey());
	}

	@Override
	public File getRevisedDocumentsDirectory(CourseEnvironment courseEnv, GTACourseNode cNode, int iteration, BusinessGroupRef group) {
		Path path = Paths.get(FolderConfig.getCanonicalRoot(), courseEnv.getCourseBaseContainer().getRelPath(),
				"gtasks", cNode.getIdent(), "revisions_" + iteration, "bgroup_" + group.getKey());
		File file = path.toFile();
		if(!file.exists()) {
			file.mkdirs();
		}
		return file;
	}

	@Override
	public VFSContainer getRevisedDocumentsContainer(CourseEnvironment courseEnv, GTACourseNode cNode, int iteration, BusinessGroupRef group) {
		VFSContainer container = getContainer(courseEnv, "revisions_" + iteration, cNode);
		return VFSManager.getOrCreateContainer(container, "bgroup_" + group.getKey());
	}

	@Override
	public File getRevisedDocumentsCorrectionsDirectory(CourseEnvironment courseEnv, GTACourseNode cNode, int iteration, IdentityRef person) {
		Path path = Paths.get(FolderConfig.getCanonicalRoot(), courseEnv.getCourseBaseContainer().getRelPath(),
				"gtasks", cNode.getIdent(), "corrections_" + iteration, "person_" + person.getKey());
		File file = path.toFile();
		if(!file.exists()) {
			file.mkdirs();
		}
		return file;
	}

	@Override
	public VFSContainer getRevisedDocumentsCorrectionsContainer(CourseEnvironment courseEnv, GTACourseNode cNode, int iteration, IdentityRef person) {
		VFSContainer container = getContainer(courseEnv, "corrections_" + iteration, cNode);
		return VFSManager.getOrCreateContainer(container, "person_" + person.getKey());
	}

	@Override
	public File getRevisedDocumentsCorrectionsDirectory(CourseEnvironment courseEnv, GTACourseNode cNode, int iteration, BusinessGroupRef group) {
		Path path = Paths.get(FolderConfig.getCanonicalRoot(), courseEnv.getCourseBaseContainer().getRelPath(),
				"gtasks", cNode.getIdent(), "corrections_" + iteration, "bgroup_" + group.getKey());
		File file = path.toFile();
		if(!file.exists()) {
			file.mkdirs();
		}
		return file;
	}
	
	@Override
	public VFSContainer getRevisedDocumentsCorrectionsContainer(CourseEnvironment courseEnv, GTACourseNode cNode, int iteration, BusinessGroupRef group) {
		VFSContainer container = getContainer(courseEnv, "corrections_" + iteration, cNode);
		return VFSManager.getOrCreateContainer(container, "bgroup_" + group.getKey());
	}

	private VFSContainer getContainer(CourseEnvironment courseEnv, String folderName, GTACourseNode cNode) {
		VFSContainer courseContainer = courseEnv.getCourseBaseContainer();
		VFSContainer nodesContainer = VFSManager.getOrCreateContainer(courseContainer, "gtasks");
		VFSContainer nodeContainer = VFSManager.getOrCreateContainer(nodesContainer, cNode.getIdent());
		return VFSManager.getOrCreateContainer(nodeContainer, folderName);
	}

	@Override
	public PublisherData getPublisherData(CourseEnvironment courseEnv, GTACourseNode cNode, boolean markedOnly) {
		RepositoryEntry re = courseEnv.getCourseGroupManager().getCourseEntry();
		String businessPath = "[RepositoryEntry:" + re.getKey() + "][CourseNode:" + cNode.getIdent() + "]";
		String publisherType = markedOnly ? "MarkedGroupTask" : "GroupTask";
		return new PublisherData(publisherType, "", businessPath);
	}

	@Override
	public SubscriptionContext getSubscriptionContext(CourseEnvironment courseEnv, GTACourseNode cNode, boolean markedOnly) {
		return getSubscriptionContext(courseEnv.getCourseGroupManager().getCourseResource(), cNode, markedOnly);
	}

	@Override
	public SubscriptionContext getSubscriptionContext(OLATResource courseResource, GTACourseNode cNode, boolean markedOnly) {
		Long courseResourceableId = courseResource.getResourceableId();
		String subIdentifier = (markedOnly ? "Marked::" : "") + cNode.getIdent();
		return new SubscriptionContext("CourseModule", courseResourceableId, subIdentifier);
	}

	@Override
	public void markNews(CourseEnvironment courseEnv, GTACourseNode cNode) {
		SubscriptionContext markedCtxt = getSubscriptionContext(courseEnv, cNode, true);
		notificationsManager.markPublisherNews(markedCtxt, null, false);
		SubscriptionContext ctxt = getSubscriptionContext(courseEnv, cNode, false);
		notificationsManager.markPublisherNews(ctxt, null, false);
	}

	@Override
	public List<BusinessGroup> filterBusinessGroups(List<BusinessGroup> groups, GTACourseNode cNode) {
		if(groups == null || groups.isEmpty()) return new ArrayList<>(1);
		
		List<BusinessGroup> filteredGroups = new ArrayList<>();

		ModuleConfiguration config = cNode.getModuleConfiguration();
		List<Long> groupKeys = config.getList(GTACourseNode.GTASK_GROUPS, Long.class);
		for(BusinessGroup group:groups) {
			if(groupKeys.contains(group.getKey())) {
				filteredGroups.add(group);
			}
		}
		
		if(filteredGroups.size() < groups.size()) {
			List<Long> areaKeys = config.getList(GTACourseNode.GTASK_AREAS, Long.class);
			List<Long> groupKeysOfAreas = areaManager.findBusinessGroupKeysOfAreaKeys(areaKeys);
			
			for(BusinessGroup group:groups) {
				//don't add 2x
				if(!groupKeys.contains(group.getKey()) && groupKeysOfAreas.contains(group.getKey())) {
					filteredGroups.add(group);
				}
			}
		}
		
		return filteredGroups;
	}

	@Override
	public List<BusinessGroup> getParticipatingBusinessGroups(IdentityRef identity, GTACourseNode cNode) {
		ModuleConfiguration config = cNode.getModuleConfiguration();
		List<Long> groupKeys = config.getList(GTACourseNode.GTASK_GROUPS, Long.class);
		List<Long> areaKeys = config.getList(GTACourseNode.GTASK_AREAS, Long.class);
		return getBusinessGroups(identity, groupKeys, areaKeys, GroupRoles.participant);
	}

	@Override
	public List<BusinessGroup> getCoachedBusinessGroups(IdentityRef identity, GTACourseNode cNode) {
		ModuleConfiguration config = cNode.getModuleConfiguration();
		List<Long> groupKeys = config.getList(GTACourseNode.GTASK_GROUPS, Long.class);
		List<Long> areaKeys = config.getList(GTACourseNode.GTASK_AREAS, Long.class);
		return getBusinessGroups(identity, groupKeys, areaKeys, GroupRoles.coach);
	}
	
	private List<BusinessGroup> getBusinessGroups(IdentityRef identity, List<Long> groupKeys, List<Long> areaKeys, GroupRoles role) {
		List<Long> consolidatedGroupKeys = new ArrayList<>();
		if(groupKeys != null && !groupKeys.isEmpty()) {
			consolidatedGroupKeys.addAll(groupKeys);
		}
		consolidatedGroupKeys.addAll(areaManager.findBusinessGroupKeysOfAreaKeys(areaKeys));
		List<BusinessGroupRef> businessGroups = BusinessGroupRefImpl.toRefs(consolidatedGroupKeys);
		return businessGroupRelationDao.filterMembership(businessGroups, identity, role.name());
	}
	
	@Override
	public List<BusinessGroup> getBusinessGroups(GTACourseNode cNode) {
		List<BusinessGroup> groups;
		ModuleConfiguration config = cNode.getModuleConfiguration();
		if(GTAType.group.name().equals(config.getStringValue(GTACourseNode.GTASK_TYPE))) {
			List<Long> groupKeys = config.getList(GTACourseNode.GTASK_GROUPS, Long.class);
			List<Long> areaKeys = config.getList(GTACourseNode.GTASK_AREAS, Long.class);
			
			List<Long> consolidatedGroupKeys = new ArrayList<>();
			if(groupKeys != null && !groupKeys.isEmpty()) {
				consolidatedGroupKeys.addAll(groupKeys);
			}
			consolidatedGroupKeys.addAll(areaManager.findBusinessGroupKeysOfAreaKeys(areaKeys));
			groups = businessGroupService.loadBusinessGroups(consolidatedGroupKeys);
		} else {
			groups = Collections.emptyList();
		}
		return groups;
	}

	@Override
	public List<IdentityRef> getDuplicatedMemberships(GTACourseNode cNode) {
		List<IdentityRef> duplicates;
		
		ModuleConfiguration config = cNode.getModuleConfiguration();
		if(GTAType.group.name().equals(config.getStringValue(GTACourseNode.GTASK_TYPE))) {
			List<Long> groupKeys = config.getList(GTACourseNode.GTASK_GROUPS, Long.class);
			List<Long> areaKeys = config.getList(GTACourseNode.GTASK_AREAS, Long.class);

			List<Long> consolidatedGroupKeys = new ArrayList<>();
			if(groupKeys != null && !groupKeys.isEmpty()) {
				consolidatedGroupKeys.addAll(groupKeys);
			}
			consolidatedGroupKeys.addAll(areaManager.findBusinessGroupKeysOfAreaKeys(areaKeys));
			List<BusinessGroupRef> businessGroups = BusinessGroupRefImpl.toRefs(consolidatedGroupKeys);
			duplicates = businessGroupRelationDao.getDuplicateMemberships(businessGroups);
		} else {
			duplicates = Collections.emptyList();
		}
		
		return duplicates;
	}

	@Override
	public Membership getMembership(IdentityRef identity, RepositoryEntryRef entry, GTACourseNode cNode) {
		List<String> roles;
		ModuleConfiguration config = cNode.getModuleConfiguration();
		if(GTAType.group.name().equals(config.getStringValue(GTACourseNode.GTASK_TYPE))) {
			List<Long> groupKeys = config.getList(GTACourseNode.GTASK_GROUPS, Long.class);
			List<Long> areaKeys = config.getList(GTACourseNode.GTASK_AREAS, Long.class);

			List<Long> consolidatedGroupKeys = new ArrayList<>();
			if(groupKeys != null && !groupKeys.isEmpty()) {
				consolidatedGroupKeys.addAll(groupKeys);
			}
			consolidatedGroupKeys.addAll(areaManager.findBusinessGroupKeysOfAreaKeys(areaKeys));
			List<BusinessGroupRef> businessGroups = BusinessGroupRefImpl.toRefs(consolidatedGroupKeys);
			roles = businessGroupRelationDao.getRoles(identity, businessGroups);
		} else {
			roles = repositoryEntryRelationDao.getRoles(identity, entry);
		}
		
		boolean coach = roles.contains(GroupRoles.coach.name()) || roles.contains(GroupRoles.owner.name());
		boolean participant = roles.contains(GroupRoles.participant.name());
		return new Membership(coach, participant);
	}

	@Override
	public String getDetails(Identity assessedIdentity, RepositoryEntryRef entry, GTACourseNode cNode) {
		String details;
		if(cNode.getModuleConfiguration().getBooleanSafe(GTACourseNode.GTASK_ASSIGNMENT)) {
			List<Task> tasks = getTasks(assessedIdentity, entry, cNode);
			if(tasks == null || tasks.isEmpty()) {
				details = null;
			} else {
				StringBuilder sb = new StringBuilder();
				for(Task task:tasks) {
					if(sb.length() > 0) sb.append(", ");
					if(sb.length() > 64) {
						sb.append("...");
						break;
					}
					String taskName = task.getTaskName();
					if(StringHelper.containsNonWhitespace(taskName)) {
						sb.append(StringHelper.escapeHtml(taskName));
					}
				}
				details = sb.length() == 0 ? null : sb.toString();
			}
		} else {
			details = null;
		}
		return details;
	}

	@Override
	public boolean isTasksInProcess(RepositoryEntryRef entry, GTACourseNode cNode) {
		List<Number> numOfTasks = dbInstance.getCurrentEntityManager()
				.createNamedQuery("isTasksInProcess", Number.class)
				.setParameter("entryKey", entry.getKey())
				.setParameter("courseNodeIdent", cNode.getIdent())
				.getResultList();
		return numOfTasks != null && !numOfTasks.isEmpty() && numOfTasks.get(0) != null && numOfTasks.get(0).longValue() > 0;
	}

	@Override
	public boolean isTaskInProcess(RepositoryEntryRef entry, GTACourseNode cNode, String taskName) {
		List<Number> numOfTasks = dbInstance.getCurrentEntityManager()
				.createNamedQuery("isTaskInProcess", Number.class)
				.setParameter("entryKey", entry.getKey())
				.setParameter("courseNodeIdent", cNode.getIdent())
				.setParameter("taskName", taskName)
				.getResultList();
		return numOfTasks != null && !numOfTasks.isEmpty() && numOfTasks.get(0) != null && numOfTasks.get(0).longValue() > 0;
	}

	@Override
	public TaskList createIfNotExists(RepositoryEntry entry, GTACourseNode cNode) {
		TaskList tasks = getTaskList(entry, cNode);
		if(tasks == null) {
			TaskListImpl tasksImpl = new TaskListImpl();
			Date creationDate = new Date();
			tasksImpl.setCreationDate(creationDate);
			tasksImpl.setLastModified(creationDate);
			tasksImpl.setEntry(entry);
			tasksImpl.setCourseNodeIdent(cNode.getIdent());
			dbInstance.getCurrentEntityManager().persist(tasksImpl);
			tasks = tasksImpl;
		}
		return tasks;
	}

	@Override
	public TaskList getTaskList(RepositoryEntryRef entry, GTACourseNode cNode) {
		String q = "select tasks from gtatasklist tasks where tasks.entry.key=:entryKey and tasks.courseNodeIdent=:courseNodeIdent";
		List<TaskList> tasks = dbInstance.getCurrentEntityManager().createQuery(q, TaskList.class)
			.setParameter("entryKey", entry.getKey())
			.setParameter("courseNodeIdent", cNode.getIdent())
			.getResultList();

		return tasks.isEmpty() ? null : tasks.get(0);
	}
	
	/**
	 * Load the task list with the underlying repository
	 * entry (full).
	 * 
	 * @param task
	 * @return
	 */
	public TaskList getTaskList(TaskRef task ) {
		QueryBuilder sb = new QueryBuilder();
		sb.append("select taskList from gtatask task")
		  .append(" inner join task.taskList as taskList")
		  .append(" inner join fetch taskList.entry as v")
		  .append(" inner join fetch v.olatResource as ores")
		  .append(" inner join fetch v.statistics as statistics")
		  .append(" left join fetch v.lifecycle as lifecycle")
		  .append(" where task.key=:taskKey");
		
		List<TaskList> tasks = dbInstance.getCurrentEntityManager()
			.createQuery(sb.toString(), TaskList.class)
			.setParameter("taskKey", task.getKey())
			.getResultList();

		return tasks.isEmpty() ? null : tasks.get(0);
	}

	@Override
	public int deleteTaskList(RepositoryEntryRef entry, GTACourseNode cNode) {
		TaskList taskList = getTaskList(entry, cNode);
		
		int numOfDeletedObjects = 0;
		if(taskList != null) {
			numOfDeletedObjects += taskRevisionDateDao.deleteTaskRevisionDate(taskList);
			numOfDeletedObjects += taskRevisionDao.deleteTaskRevision(taskList);
			
			String deleteTasks = "delete from gtatask as task where task.taskList.key=:taskListKey";
			numOfDeletedObjects += dbInstance.getCurrentEntityManager().createQuery(deleteTasks)
				.setParameter("taskListKey", taskList.getKey())
				.executeUpdate();
			numOfDeletedObjects += gtaMarkDao.deleteMark(taskList);
			dbInstance.getCurrentEntityManager().remove(taskList);
			numOfDeletedObjects++;
		}
		return numOfDeletedObjects;
	}

	@Override
	public int deleteAllTaskLists(RepositoryEntryRef entry) {
		String q = "select tasks from gtatasklist tasks where tasks.entry.key=:entryKey";
		List<TaskList> taskLists = dbInstance.getCurrentEntityManager().createQuery(q, TaskList.class)
			.setParameter("entryKey", entry.getKey())
			.getResultList();
		
		String deleteTasks = "delete from gtatask as task where task.taskList.key=:taskListKey";
		Query deleteTaskQuery = dbInstance.getCurrentEntityManager().createQuery(deleteTasks);

		int numOfDeletedObjects = 0;
		for(TaskList taskList:taskLists) {
			int numOfRevisionDates = taskRevisionDateDao.deleteTaskRevisionDate(taskList);
			numOfDeletedObjects += numOfRevisionDates;
			int numOfRevisions = taskRevisionDao.deleteTaskRevision(taskList);
			numOfDeletedObjects += numOfRevisions;
			int numOfTasks = deleteTaskQuery.setParameter("taskListKey", taskList.getKey()).executeUpdate();
			numOfDeletedObjects += numOfTasks;
			int numOfMarks = gtaMarkDao.deleteMark(taskList);
			numOfDeletedObjects += numOfMarks;
		}
		
		String deleteTaskLists = "delete from gtatasklist as tasks where tasks.entry.key=:entryKey";
		numOfDeletedObjects +=  dbInstance.getCurrentEntityManager()
				.createQuery(deleteTaskLists)
				.setParameter("entryKey", entry.getKey())
				.executeUpdate();
		return numOfDeletedObjects;	
	}

	@Override
	public boolean deleteGroupDataFor(BusinessGroup group) {
		
		List<Task> groupTasks = getTasks(group);
		List<Long> taskKeys = groupTasks.stream()
				.map(Task::getKey)
				.collect(Collectors.toList());
		
		StringBuilder sb = new StringBuilder(128);
		sb.append("delete from gtataskrevisiondate as taskrev where taskrev.task.key in (:taskKeys)");
		int numOfDeletedObjects = dbInstance.getCurrentEntityManager()
			.createQuery(sb.toString())
			.setParameter("taskKeys", taskKeys)
			.executeUpdate();
		
		StringBuilder taskSb = new StringBuilder(128);
		taskSb.append("delete from gtataskrevision as taskrev where taskrev.task.key in (:taskKeys)");
		numOfDeletedObjects += dbInstance.getCurrentEntityManager()
			.createQuery(taskSb.toString())
			.setParameter("taskKeys", taskKeys)
			.executeUpdate();
		
		numOfDeletedObjects += gtaMarkDao.deleteMark(taskKeys);
		
		String deleteTasks = "delete from gtatask as task where task.key in (:taskKeys)";
		numOfDeletedObjects += dbInstance.getCurrentEntityManager().createQuery(deleteTasks)
			.setParameter("taskKeys", taskKeys)
			.executeUpdate();
		
		dbInstance.commit();

		return numOfDeletedObjects > 0;
	}

	@Override
	public List<Task> getTasks(TaskList taskList, GTACourseNode cNode) {
		StringBuilder sb = new StringBuilder();
		sb.append("select task from gtatask task ")
		  .append(" inner join task.taskList tasklist ");
		if(GTAType.group.name().equals(cNode.getModuleConfiguration().getStringValue(GTACourseNode.GTASK_TYPE))) {
			sb.append(" inner join fetch task.businessGroup bGroup ");
		} else {
			sb.append(" inner join fetch task.identity identity ");
		}
		sb.append(" where tasklist.key=:taskListKey");
		return dbInstance.getCurrentEntityManager().createQuery(sb.toString(), Task.class)
				.setParameter("taskListKey", taskList.getKey())
				.getResultList();
	}
	
	@Override
	public List<Task> getTasks(IdentityRef identity) {
		StringBuilder sb = new StringBuilder();
		sb.append("select task from gtatask task")
		  .append(" inner join fetch task.taskList as tasklist")
		  .append(" inner join fetch tasklist.entry as entry")
		  .append(" inner join fetch entry.olatResource as res")
		  .append(" where task.identity.key=:identityKey");
		return dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), Task.class)
				.setParameter("identityKey", identity.getKey())
				.getResultList();
	}

	@Override
	public List<TaskLight> getTasksLight(RepositoryEntryRef entry, GTACourseNode gtaNode) {
		StringBuilder sb = new StringBuilder();
		sb.append("select task from gtatasklight task ")
		  .append(" inner join task.taskList tasklist ")
		  .append(" where tasklist.entry.key=:entryKey and tasklist.courseNodeIdent=:courseNodeIdent");
		
		return dbInstance.getCurrentEntityManager().createQuery(sb.toString(), TaskLight.class)
				.setParameter("entryKey", entry.getKey())
				.setParameter("courseNodeIdent", gtaNode.getIdent())
				.getResultList();
	}

	@Override
	public List<Task> getTasks(IdentityRef identity, RepositoryEntryRef entry, GTACourseNode cNode) {
		StringBuilder sb = new StringBuilder();
		sb.append("select task from gtatask task ")
		  .append(" inner join task.taskList tasklist ")
		  .append(" inner join tasklist.entry rentry ")
		  .append(" where tasklist.entry.key=:entryKey and tasklist.courseNodeIdent=:courseNodeIdent and (task.identity.key=:identityKey ")
		  .append(" or task.businessGroup.key in (")
		  .append("   select bgroup.key from businessgroup as bgroup ")
		  .append("     inner join bgroup.baseGroup as baseGroup")
		  .append("     inner join baseGroup.members as membership")
		  .append("     where membership.identity.key=:identityKey and membership.role='").append(GroupRoles.participant.name()).append("'")
		  .append(" ))");

		return dbInstance.getCurrentEntityManager().createQuery(sb.toString(), Task.class)
				.setParameter("entryKey", entry.getKey())
				.setParameter("courseNodeIdent", cNode.getIdent())
				.setParameter("identityKey", identity.getKey())
				.getResultList();
	}

	@Override
	public Task getTask(TaskRef task) {
		String q = "select task from gtatask task where task.key=:taskKey";
		List<Task> tasks = dbInstance.getCurrentEntityManager().createQuery(q, Task.class)
			.setParameter("taskKey", task.getKey())
			.getResultList();

		return tasks.isEmpty() ? null : tasks.get(0);
	}

	@Override
	public TaskDueDate getDueDatesTask(TaskRef task) {
		List<TaskDueDate> tasks = dbInstance.getCurrentEntityManager()
				.createNamedQuery("dueDateTaskByTask", TaskDueDate.class)
			.setParameter("taskKey", task.getKey())
			.getResultList();
		return tasks.isEmpty() ? null : tasks.get(0);
	}

	@Override
	public Task getTask(IdentityRef identity, TaskList taskList) {
		String q = "select task from gtatask task where task.taskList.key=:taskListKey and task.identity.key=:identityKey";
		List<Task> tasks = dbInstance.getCurrentEntityManager().createQuery(q, Task.class)
			.setParameter("taskListKey", taskList.getKey())
			.setParameter("identityKey", identity.getKey())
			.getResultList();

		return tasks.isEmpty() ? null : tasks.get(0);
	}
	
	@Override
	public Task getTask(BusinessGroupRef businessGroup, TaskList taskList) {
		String q = "select task from gtatask task where task.taskList.key=:taskListKey and task.businessGroup.key=:businessGroupKey";
		List<Task> tasks = dbInstance.getCurrentEntityManager().createQuery(q, Task.class)
			.setParameter("taskListKey", taskList.getKey())
			.setParameter("businessGroupKey", businessGroup.getKey())
			.getResultList();

		return tasks.isEmpty() ? null : tasks.get(0);
	}
	
	public List<Task> getTasks(BusinessGroupRef businessGroup) {
		String q = "select task from gtatask task where task.businessGroup.key=:businessGroupKey";
		return dbInstance.getCurrentEntityManager().createQuery(q, Task.class)
			.setParameter("businessGroupKey", businessGroup.getKey())
			.getResultList();
	}

	@Override
	public List<TaskRevisionDate> getTaskRevisionsDate(Task task) {
		if(task == null || task.getKey() == null) return Collections.emptyList();
		
		String q = "select taskrev from gtataskrevisiondate taskrev where taskrev.task.key=:taskKey";
		return dbInstance.getCurrentEntityManager()
				.createQuery(q, TaskRevisionDate.class)
			.setParameter("taskKey", task.getKey())
			.getResultList();
	}
	
	@Override
	public TaskRevision updateTaskRevisionComment(Task task, TaskProcess status, int iteration, String comment, Identity commentator) {
		TaskRevision taskRevision = taskRevisionDao.getTaskRevision(task, status.name(), iteration);
		if(taskRevision != null) {
			if(!StringHelper.isSame(taskRevision.getComment(), comment)) {
				TaskRevisionImpl revision = (TaskRevisionImpl)taskRevision;
				revision.setComment(comment);
				revision.setCommentLastModified(new Date());
				revision.setCommentAuthor(commentator);
			}
			taskRevisionDao.updateTaskRevision(taskRevision);
		} else if(StringHelper.containsNonWhitespace(comment)) {
			taskRevisionDao.createTaskRevision(task, status.name(), iteration, comment, commentator, null);
		}
		dbInstance.commit();
		return taskRevision;
	}

	@Override
	public List<TaskRevision> getTaskRevisions(Task task) {
		if(task == null || task.getKey() == null) {
			return Collections.emptyList();
		}
		return taskRevisionDao.getTaskRevisions(task);
	}

	@Override
	public AssignmentResponse assignTaskAutomatically(TaskList taskList, BusinessGroup assessedGroup, CourseEnvironment courseEnv, GTACourseNode cNode, Identity doerIdentity) {
		return assignTaskAutomatically(taskList, assessedGroup, null, courseEnv, cNode, doerIdentity);
	}
	
	@Override
	public AssignmentResponse assignTaskAutomatically(TaskList taskList, Identity assessedIdentity, CourseEnvironment courseEnv, GTACourseNode cNode) {
		return assignTaskAutomatically(taskList, null, assessedIdentity, courseEnv, cNode, assessedIdentity);
	}
	
	private AssignmentResponse assignTaskAutomatically(TaskList tasks, BusinessGroup businessGroup, Identity identity,
			CourseEnvironment courseEnv, GTACourseNode cNode, Identity doerIdentity) {

		Task currentTask;
		if(businessGroup != null) {
			currentTask = getTask(businessGroup, tasks);
		} else {
			currentTask = getTask(identity, tasks);
		}

		AssignmentResponse response;
		if(currentTask == null || !StringHelper.containsNonWhitespace(currentTask.getTaskName())) {
			File tasksFolder = getTasksDirectory(courseEnv, cNode);
			String[] taskFiles = tasksFolder.list(SystemFilenameFilter.FILES_ONLY);
			
			TaskList reloadedTasks = loadForUpdate(tasks);
			List<String> assignedFilenames = getAssignedTasks(reloadedTasks);
			assignedFilenames.retainAll(List.of(taskFiles));
			
			String taskName;
			if(GTACourseNode.GTASK_SAMPLING_UNIQUE.equals(cNode.getModuleConfiguration().get(GTACourseNode.GTASK_SAMPLING))) {
				taskName = nextUnique(taskFiles, assignedFilenames);
			} else {
				taskName = nextSlotRoundRobin(taskFiles, assignedFilenames);
			}
			
			if(taskName == null) {
				response = AssignmentResponse.NO_MORE_TASKS;
			} else {
				TaskProcess nextStep = nextStep(TaskProcess.assignment, cNode);
				TaskImpl task;
				if(currentTask == null) {
					task = createTask(taskName, reloadedTasks, nextStep, businessGroup, identity, cNode);
					task.setAssignmentDate(new Date());
					dbInstance.getCurrentEntityManager().persist(task);
				} else {
					task = (TaskImpl)currentTask;
					task.setTaskName(taskName);
					task.setTaskStatus(nextStep);
					task.setAssignmentDate(new Date());
					task = dbInstance.getCurrentEntityManager().merge(task);
				}	
				dbInstance.commit();
				File taskFile = new File(getTasksDirectory(courseEnv, cNode) + File.separator + taskName);
				createSubmissionFromTask(identity, businessGroup, courseEnv, cNode, taskFile, doerIdentity, nextStep);
				syncAssessmentEntry(task, cNode, null, false, doerIdentity, Role.user);
				response = new AssignmentResponse(task, Status.ok);
			}
		} else {
			if(currentTask.getTaskStatus() == TaskProcess.assignment) {
				((TaskImpl)currentTask).setTaskStatus(TaskProcess.submit);
			}
			currentTask = dbInstance.getCurrentEntityManager().merge(currentTask);
			syncAssessmentEntry(currentTask, cNode, null, false, doerIdentity, Role.user);
			response = new AssignmentResponse(currentTask, Status.ok);
		}
		
		return response;
	}
	
	protected String nextUnique(String[] slots, List<String> usedSlots) {
		String nextSlot = null;
		
		for(String slot:slots) {
			if(!usedSlots.contains(slot)) {
				nextSlot = slot;
				break;
			}	
		}

		return nextSlot;
	}
	
	protected String nextSlotRoundRobin(String[] slots, List<String> usedSlots) {
		String nextSlot = null;
		for(String slot:slots) {
			if(!usedSlots.contains(slot)) {
				nextSlot = slot;
				break;
			}	
		}
		
		//not found an used slot
		if(nextSlot == null) {
			//statistics
			Map<String,AtomicInteger> usages = new HashMap<>();
			for(String usedSlot:usedSlots) {
				if(usages.containsKey(usedSlot)) {
					usages.get(usedSlot).incrementAndGet();
				} else {
					usages.put(usedSlot, new AtomicInteger(1));
				}
			}
			
			int minimum = Integer.MAX_VALUE;
			for(AtomicInteger slotUsage:usages.values()) {
				minimum = Math.min(minimum, slotUsage.get());	
			}
			Set<String> slotsWithMinimalUsage = new HashSet<>();
			for(Map.Entry<String, AtomicInteger> slotUsage:usages.entrySet()) {
				if(slotUsage.getValue().get() == minimum) {
					slotsWithMinimalUsage.add(slotUsage.getKey());
				}
			}
			
			//found the next slot with minimal usage
			for(String slot:slots) {
				if(slotsWithMinimalUsage.contains(slot)) {
					nextSlot = slot;
					break;
				}	
			}
		}
		
		//security
		if(nextSlot == null && slots.length > 0) {
			nextSlot = slots[0];
		}
		return nextSlot;
	}
	
	@Override
	public boolean isTaskAssigned(TaskList taskList, String taskName) {
		List<Number> tasks = dbInstance.getCurrentEntityManager()
			.createNamedQuery("countTaskByNameAndTaskList", Number.class)
			.setParameter("taskListKey", taskList.getKey())
			.setParameter("taskName", taskName)
			.getResultList();
		return tasks.isEmpty() ? false : tasks.get(0).intValue() > 0;
	}
	
	@Override
	public List<String> getAssignedTasks(TaskList taskList) {
		List<String> taskNames = dbInstance.getCurrentEntityManager()
			.createNamedQuery("tasksByTaskList", String.class)
			.setParameter("taskListKey", taskList.getKey())
			.setHint("javax.persistence.cache.retrieveMode", CacheRetrieveMode.BYPASS)
			.setHint("javax.persistence.cache.storeMode", CacheStoreMode.BYPASS)
			.getResultList();
		return taskNames.stream()
			.filter(name -> (StringHelper.containsNonWhitespace(name)))
			.collect(Collectors.toList());
	}

	@Override
	public AssignmentResponse selectTask(Identity identity, TaskList tasks, CourseEnvironment courseEnv,
			GTACourseNode cNode, File taskFile) {
		if(!GTAType.individual.name().equals(cNode.getModuleConfiguration().getStringValue(GTACourseNode.GTASK_TYPE))) {
			return AssignmentResponse.ERROR;
		}
		return selectTask(identity, null, tasks, courseEnv, cNode, taskFile, identity);
	}
	
	@Override
	public AssignmentResponse selectTask(BusinessGroup businessGroup, TaskList tasks, CourseEnvironment courseEnv,
			GTACourseNode cNode, File taskFile, Identity doerIdentity) {
		if(!GTAType.group.name().equals(cNode.getModuleConfiguration().getStringValue(GTACourseNode.GTASK_TYPE))) {
			return AssignmentResponse.ERROR;
		}
		return selectTask(null, businessGroup, tasks, courseEnv, cNode, taskFile, doerIdentity);
	}
	
	private AssignmentResponse selectTask(Identity identity, BusinessGroup businessGroup, TaskList tasks,
			CourseEnvironment courseEnv, GTACourseNode cNode, File taskFile, Identity doerIdentity) {
		Task currentTask;
		if(businessGroup != null) {
			currentTask = getTask(businessGroup, tasks);
		} else {
			currentTask = getTask(identity, tasks);
		}

		AssignmentResponse response;
		if(currentTask == null) {
			String taskName = taskFile.getName();
			TaskList reloadedTasks = loadForUpdate(tasks);
			
			String sampling = cNode.getModuleConfiguration().getStringValue(GTACourseNode.GTASK_SAMPLING);
			if(GTACourseNode.GTASK_SAMPLING_UNIQUE.equals(sampling) && isTaskAssigned(reloadedTasks, taskName)) {
				response = new AssignmentResponse(null, Status.alreadyAssigned);
			} else {
				TaskProcess nextStep = nextStep(TaskProcess.assignment, cNode);
				TaskImpl task = createTask(taskName, reloadedTasks, nextStep, businessGroup, identity, cNode);
				task.setAssignmentDate(new Date());
				dbInstance.getCurrentEntityManager().persist(task);
				createSubmissionFromTask(identity, businessGroup, courseEnv, cNode, taskFile, doerIdentity, nextStep);
				syncAssessmentEntry(task, cNode, null, false, doerIdentity, Role.user);
				response = new AssignmentResponse(task, Status.ok);
			}
			dbInstance.commit();
		} else {
			if(currentTask.getTaskStatus() == TaskProcess.assignment) {
				TaskProcess nextStep = nextStep(currentTask.getTaskStatus(), cNode);
				((TaskImpl)currentTask).setTaskStatus(nextStep);
				if(taskFile != null) {
					((TaskImpl)currentTask).setTaskName(taskFile.getName());
					createSubmissionFromTask(identity, businessGroup, courseEnv, cNode, taskFile, doerIdentity, nextStep);
				}
			}
			currentTask = dbInstance.getCurrentEntityManager().merge(currentTask);
			syncAssessmentEntry(currentTask, cNode, null, false, doerIdentity, Role.user);
			response = new AssignmentResponse(currentTask, Status.ok);
		}
		return response;
	}

	private void createSubmissionFromTask(Identity identity, BusinessGroup businessGroup, CourseEnvironment courseEnv, GTACourseNode cNode, File taskFile, Identity doerIdentity, TaskProcess nextStep) {
		if (cNode.getModuleConfiguration().getBooleanSafe(GTACourseNode.GTASK_SUBMISSION_TEMPLATE) && nextStep == TaskProcess.submit) {
			VFSContainer submitContainer = null;
			if (identity != null) {
				submitContainer = getSubmitContainer(courseEnv, cNode, identity);
			} else if (businessGroup != null) {
				submitContainer = getSubmitContainer(courseEnv, cNode, businessGroup);
			}
			if (submitContainer == null || !submitContainer.getItems().isEmpty()) {
				return;
			}
			
			VFSLeaf target = submitContainer.createChildLeaf(taskFile.getName());
			VFSManager.copyContent(taskFile, target, doerIdentity);
		}
	}

	@Override
	public Task persistTask(Task task) {
		if(task.getKey() == null) {
			if(task.getCreationDate() == null) {
				((TaskImpl)task).setCreationDate(new Date());
				((TaskImpl)task).setLastModified(task.getCreationDate());
			} else {
				((TaskImpl)task).setLastModified(new Date());
			}
			dbInstance.getCurrentEntityManager().persist(task);
		}
		return task;
	}

	@Override
	public Task createAndPersistTask(String taskName, TaskList taskList, TaskProcess status,
			BusinessGroup assessedGroup, Identity assessedIdentity, GTACourseNode cNode) {
		Task task = createTask(taskName, taskList, status, assessedGroup, assessedIdentity, cNode);
		if(task.getKey() == null) {
			dbInstance.getCurrentEntityManager().persist(task);
		} else {
			task = dbInstance.getCurrentEntityManager().merge(task);
		}
		return task;
	}

	@Override
	public TaskImpl createTask(String taskName, TaskList taskList, TaskProcess status, BusinessGroup assessedGroup, Identity assessedIdentity, GTACourseNode cNode) {
		TaskImpl task = null;
		if(assessedIdentity != null) {
			task = (TaskImpl)getTask(assessedIdentity, taskList);
		} else if(assessedGroup != null) {
			task = (TaskImpl)getTask(assessedGroup, taskList);
		}
		
		if(task == null) {
			task = new TaskImpl();
			Date creationDate = new Date();
			task.setCreationDate(creationDate);
			task.setLastModified(creationDate);
			task.setTaskList(taskList);
			task.setTaskName(taskName);

			if(GTAType.group.name().equals(cNode.getModuleConfiguration().getStringValue(GTACourseNode.GTASK_TYPE))) {
				task.setBusinessGroup(assessedGroup);
			} else {
				task.setIdentity(assessedIdentity);
			}
		} else if (StringHelper.containsNonWhitespace(taskName) && !StringHelper.containsNonWhitespace(task.getTaskName())) {
			task.setLastModified(new Date());
			task.setTaskName(taskName);
		}
		
		// only override the status if there is no status or the new status is a step further. Back is not allowed here
		if((status != null && task.getStatus() == null)
				|| (status != null && task.getStatus() != null && status.ordinal() > task.getTaskStatus().ordinal())) {
				
			task.setTaskStatus(status);//assignment is ok -> go to submit step
			task.setRevisionLoop(0);
			
			if(status == TaskProcess.graded) {
				task.setGraduationDate(new Date());
			}
		}
	
		return task;
	}

	@Override
	public Task ensureTaskExists(Task assignedTask, BusinessGroup assessedGroup, Identity assessedIdentity,
			RepositoryEntry entry, GTACourseNode gtaNode) {
		if(assignedTask == null) {
			TaskProcess firstStep = firstStep(gtaNode);
			TaskList taskList = getTaskList(entry, gtaNode);
			assignedTask = createAndPersistTask(null, taskList, firstStep, assessedGroup, assessedIdentity, gtaNode);
			dbInstance.commit();
		} else if(assignedTask.getKey() == null) {
			assignedTask = persistTask(assignedTask);
			dbInstance.commit();
		}
		return assignedTask;
	}

	public TaskRevisionDate createAndPersistTaskRevisionDate(Task task, int revisionLoop, TaskProcess status) {
		return taskRevisionDateDao.createAndPersistTaskRevisionDate(task, revisionLoop, status);
	}

	@Override
	public boolean isDueDateEnabled(GTACourseNode cNode) {
		if(cNode.getModuleConfiguration().getBooleanSafe(GTACourseNode.GTASK_RELATIVE_DATES, false)
				|| cNode.getModuleConfiguration().getBooleanSafe(GTACourseNode.GTASK_REVISION_PERIOD, false)) {
			return true;
		} else if(cNode.getModuleConfiguration().getDateValue(GTACourseNode.GTASK_ASSIGNMENT_DEADLINE) != null
				|| cNode.getModuleConfiguration().getDateValue(GTACourseNode.GTASK_SUBMIT_DEADLINE) != null
				|| cNode.getModuleConfiguration().getDateValue(GTACourseNode.GTASK_SAMPLE_SOLUTION_VISIBLE_AFTER) != null) {
			return true;
		}
		return false;
	}
	
	@Override
	public DueDate getAssignmentDueDate(TaskRef assignedTask, IdentityRef assessedIdentity, BusinessGroup assessedGroup,
			GTACourseNode cNode, RepositoryEntry courseEntry, boolean withIndividualDueDate) {
		DueDate assignmentDueDate = null;
		DueDateConfig dueDateConfig = cNode.getDueDateConfig(GTACourseNode.GTASK_ASSIGNMENT_DEADLINE);
		if(DueDateConfig.isAbsolute(dueDateConfig) && withIndividualDueDate && assignedTask != null && assignedTask.getAssignmentDueDate() != null) {
			assignmentDueDate = new DueDate(false, assignedTask.getAssignmentDueDate());
		} else if(DueDateConfig.isRelative(dueDateConfig)) {
			assignmentDueDate = getReferenceDate(dueDateConfig, assignedTask, assessedIdentity, assessedGroup, courseEntry);
		} else if(DueDateConfig.isAbsolute(dueDateConfig)) {
			assignmentDueDate = new DueDate(false, dueDateConfig.getAbsoluteDate());
		}
		return assignmentDueDate;
	}
	
	@Override
	public DueDate getSubmissionDueDate(TaskRef assignedTask, IdentityRef assessedIdentity, BusinessGroup assessedGroup,
			GTACourseNode cNode, RepositoryEntry courseEntry, boolean withIndividualDueDate) {
		DueDate submissionDueDate = null;
		DueDateConfig dueDateConfig = cNode.getDueDateConfig(GTACourseNode.GTASK_SUBMIT_DEADLINE);
		if(DueDateConfig.isAbsolute(dueDateConfig) && withIndividualDueDate && assignedTask != null && assignedTask.getSubmissionDueDate() != null) {
			submissionDueDate = new DueDate(false, assignedTask.getSubmissionDueDate());
		} else if(DueDateConfig.isRelative(dueDateConfig)) {
			submissionDueDate = getReferenceDate(dueDateConfig, assignedTask, assessedIdentity, assessedGroup, courseEntry);
		} else if(DueDateConfig.isAbsolute(dueDateConfig)) {
			submissionDueDate = new DueDate(false, dueDateConfig.getAbsoluteDate());
		}
		return submissionDueDate;
	}
	
	@Override
	public DueDate getSolutionDueDate(TaskRef assignedTask, IdentityRef assessedIdentity, BusinessGroup assessedGroup,
			GTACourseNode cNode, RepositoryEntry courseEntry, boolean withIndividualDueDate) {
		DueDate solutionDueDate = null;
		DueDateConfig dueDateConfig = cNode.getDueDateConfig(GTACourseNode.GTASK_SAMPLE_SOLUTION_VISIBLE_AFTER);
		if(DueDateConfig.isAbsolute(dueDateConfig) && withIndividualDueDate && assignedTask != null && assignedTask.getSolutionDueDate() != null) {
			solutionDueDate = new DueDate(false, assignedTask.getSolutionDueDate());
		} else if(DueDateConfig.isRelative(dueDateConfig)) {
			solutionDueDate = getReferenceDate(dueDateConfig, assignedTask, assessedIdentity, assessedGroup, courseEntry);
		} else if(DueDateConfig.isAbsolute(dueDateConfig)) {
			solutionDueDate = new DueDate(false, dueDateConfig.getAbsoluteDate());
		}

		return solutionDueDate;
	}
	
	private DueDate getReferenceDate(RelativeDueDateConfig relativeDueDateConfig, TaskRef assignedTask,
			IdentityRef assessedIdentity, BusinessGroup assessedGroup, RepositoryEntry courseEntry) {
		DueDate dueDate = null;
		if(DueDateConfig.isRelative(relativeDueDateConfig)) {
			Date referenceDate = null;
			String messageKey = null;
			String messageArg = null;
			if (relativeDueDateConfig.getRelativeToType().equals(GTACourseNode.TYPE_RELATIVE_TO_ASSIGNMENT)) {
				if(assignedTask != null) {
					referenceDate = assignedTask.getAssignmentDate();
					referenceDate = dueDateService.addNumOfDays(referenceDate, relativeDueDateConfig.getNumOfDays());
				} else {
					messageKey = "relative.to.assignment.message";
					messageArg =  Integer.toString(relativeDueDateConfig.getNumOfDays());
				}
			} else if (assessedIdentity != null) {
				referenceDate = dueDateService.getRelativeDate(relativeDueDateConfig, courseEntry, assessedIdentity);
			} else if (assessedGroup != null) {
				referenceDate = dueDateService.getRelativeDate(relativeDueDateConfig, courseEntry, assessedGroup);
			}
			
			if(referenceDate != null) {
				dueDate = new DueDate(true, referenceDate);
			} else if(messageKey != null) {
				dueDate = new DueDate(true, messageKey, messageArg);
			}
		}
		return dueDate;
	}
	
	@Override
	public Task nextStep(Task task, GTACourseNode cNode, boolean incrementUserAttempts, Identity doerIdentity, Role by) {
		TaskImpl taskImpl = (TaskImpl)task;
		TaskProcess currentStep = taskImpl.getTaskStatus();
		//cascade through the possible steps
		TaskProcess nextStep = nextStep(currentStep, cNode);
		taskImpl.setTaskStatus(nextStep);
		TaskImpl mergedTask = dbInstance.getCurrentEntityManager().merge(taskImpl);
		dbInstance.commit();//make the thing definitive
		syncAssessmentEntry(mergedTask, cNode, null, incrementUserAttempts, doerIdentity, by);
		return mergedTask;
	}
	
	@Override
	public TaskProcess firstStep(GTACourseNode cNode) {
		TaskProcess firstStep = null;
		
		if(cNode.getModuleConfiguration().getBooleanSafe(GTACourseNode.GTASK_ASSIGNMENT)) {
			firstStep = TaskProcess.assignment;
		} else if(cNode.getModuleConfiguration().getBooleanSafe(GTACourseNode.GTASK_SUBMIT)) {
			firstStep = TaskProcess.submit;
		} else if(cNode.getModuleConfiguration().getBooleanSafe(GTACourseNode.GTASK_REVIEW_AND_CORRECTION)) {
			firstStep = TaskProcess.review;
		} else if(cNode.getModuleConfiguration().getBooleanSafe(GTACourseNode.GTASK_REVISION_PERIOD)) {
			firstStep = TaskProcess.revision;
		} else if(cNode.getModuleConfiguration().getBooleanSafe(GTACourseNode.GTASK_REVISION_PERIOD)) {
			firstStep = TaskProcess.correction;
		} else if(cNode.getModuleConfiguration().getBooleanSafe(GTACourseNode.GTASK_SAMPLE_SOLUTION)) {
			firstStep = TaskProcess.solution;
		} else if(cNode.getModuleConfiguration().getBooleanSafe(GTACourseNode.GTASK_GRADING)) {
			firstStep = TaskProcess.grading;
		}
		
		return firstStep;
	}

	@Override
	public TaskProcess previousStep(TaskProcess currentStep, GTACourseNode cNode) {
		TaskProcess previousStep = null;
		switch(currentStep) {
			case graded:
			case grading: {
				if(currentStep != TaskProcess.grading && cNode.getModuleConfiguration().getBooleanSafe(GTACourseNode.GTASK_GRADING)) {
					previousStep = TaskProcess.grading;
					break;
				}
			}
			case solution: {
				if(currentStep != TaskProcess.solution && cNode.getModuleConfiguration().getBooleanSafe(GTACourseNode.GTASK_SAMPLE_SOLUTION)) {
					previousStep = TaskProcess.solution;
					break;
				}
			}
			case correction: {
				if(currentStep != TaskProcess.correction && cNode.getModuleConfiguration().getBooleanSafe(GTACourseNode.GTASK_REVISION_PERIOD)) {
					previousStep = TaskProcess.correction;
					break;
				}
			}
			case revision: {
				if(currentStep != TaskProcess.revision && cNode.getModuleConfiguration().getBooleanSafe(GTACourseNode.GTASK_REVISION_PERIOD)) {
					previousStep = TaskProcess.revision;
					break;
				}
			}
			case review: {
				if(currentStep != TaskProcess.review && cNode.getModuleConfiguration().getBooleanSafe(GTACourseNode.GTASK_REVIEW_AND_CORRECTION)) {
					previousStep = TaskProcess.review;
					break;
				}
			}
			case submit: {
				if(currentStep != TaskProcess.submit && cNode.getModuleConfiguration().getBooleanSafe(GTACourseNode.GTASK_SUBMIT)) {
					previousStep = TaskProcess.submit;
					break;
				}
			}
			case assignment: {
				if(currentStep != TaskProcess.assignment && cNode.getModuleConfiguration().getBooleanSafe(GTACourseNode.GTASK_ASSIGNMENT)) {
					previousStep = TaskProcess.assignment;
					break;
				}
			}
			default: {
				previousStep = TaskProcess.assignment;
				break;
			}
		}

		return previousStep;
	}

	@Override
	public TaskProcess nextStep(TaskProcess currentStep, GTACourseNode cNode) {
		TaskProcess nextStep = null;
		switch(currentStep) {
			case assignment:
			case submit: {
				if(currentStep != TaskProcess.submit && cNode.getModuleConfiguration().getBooleanSafe(GTACourseNode.GTASK_SUBMIT)) {
					nextStep = TaskProcess.submit;
					break;
				}
			}
			case review: {
				if(currentStep != TaskProcess.review && cNode.getModuleConfiguration().getBooleanSafe(GTACourseNode.GTASK_REVIEW_AND_CORRECTION)) {
					nextStep = TaskProcess.review;
					break;
				}
			}
			case revision: {
				if(currentStep != TaskProcess.revision && cNode.getModuleConfiguration().getBooleanSafe(GTACourseNode.GTASK_REVISION_PERIOD)) {
					nextStep = TaskProcess.revision;
					break;
				}
			}
			case correction: {
				if(currentStep != TaskProcess.correction && cNode.getModuleConfiguration().getBooleanSafe(GTACourseNode.GTASK_REVISION_PERIOD)) {
					nextStep = TaskProcess.correction;
					break;
				}
			}
			case solution: {
				if(currentStep != TaskProcess.solution && cNode.getModuleConfiguration().getBooleanSafe(GTACourseNode.GTASK_SAMPLE_SOLUTION)) {
					nextStep = TaskProcess.solution;
					break;
				}
			}
			case grading: {
				if(currentStep != TaskProcess.grading && cNode.getModuleConfiguration().getBooleanSafe(GTACourseNode.GTASK_GRADING)) {
					nextStep = TaskProcess.grading;
					break;
				}
			}
			case graded: {
				if(currentStep != TaskProcess.graded && cNode.getModuleConfiguration().getBooleanSafe(GTACourseNode.GTASK_GRADING)) {
					nextStep = TaskProcess.graded;
					break;
				}
			}
			default: {
				nextStep = TaskProcess.graded;
				break;
			}
		}

		return nextStep;
	}

	@Override
	public int updateTaskName(TaskList taskList, String currentTaskName, String newTaskName) {
		String q = "update gtatask set taskName=:newTaskName where taskList.key=:taskListKey and taskName=:oldTaskName";
		return dbInstance.getCurrentEntityManager()
				.createQuery(q)
				.setParameter("taskListKey", taskList.getKey())
				.setParameter("oldTaskName", currentTaskName)
				.setParameter("newTaskName", newTaskName)
				.executeUpdate();
	}

	@Override
	public Task collectTask(Task task, GTACourseNode cNode, int numOfDocs, Identity doerIdentity) {
		TaskProcess review = nextStep(TaskProcess.submit, cNode);
		TaskImpl taskImpl = (TaskImpl)task;
		taskImpl.setCollectionDate(new Date());
		taskImpl.setCollectionNumOfDocs(numOfDocs);
		return updateTask(task, review, cNode, true, doerIdentity, Role.coach);
	}

	@Override
	public Task submitTask(Task task, GTACourseNode cNode, int numOfDocs, Identity doerIdentity, Role by) {
		TaskProcess review = nextStep(TaskProcess.submit, cNode);
		TaskImpl taskImpl = (TaskImpl)task;
		taskImpl.setSubmissionDate(new Date());
		taskImpl.setSubmissionNumOfDocs(numOfDocs);
		taskImpl.setCollectionDate(null);
		taskImpl.setCollectionNumOfDocs(null);
		return updateTask(task, review, cNode, true, doerIdentity, by);
	}

	@Override
	public Task allowResetTask(Task task, Identity allower, GTACourseNode cNode) {
		TaskImpl taskImpl = (TaskImpl)task;
		taskImpl.setAllowResetDate(new Date());
		taskImpl.setAllowResetIdentity(allower);
		return updateTask(task, task.getTaskStatus(), cNode, false, allower, Role.coach);
	}
	
	@Override
	public Task resetTask(Task task, GTACourseNode cNode, CourseEnvironment courseEnv, Identity doerIdentity) {
		TaskImpl taskImpl = (TaskImpl)task;
		taskImpl.setTaskName(null);
		taskImpl.setAllowResetDate(null);
		Task updatedTask = updateTask(task, TaskProcess.assignment, cNode, false, doerIdentity, Role.user);
		
		File submissionDir = null;
		if(updatedTask.getBusinessGroup() != null) {
			submissionDir = getSubmitDirectory(courseEnv, cNode, updatedTask.getBusinessGroup());
		} else if(updatedTask.getIdentity() != null) {
			submissionDir = getSubmitDirectory(courseEnv, cNode, updatedTask.getIdentity());
		}
		if(submissionDir != null) {
			FileUtils.deleteDirsAndFiles(submissionDir, true, false);
		}
		return updatedTask;
	}

	@Override
	public Task resetTaskRefused(Task task, GTACourseNode cNode, Identity doerIdentity) {
		TaskImpl taskImpl = (TaskImpl)task;
		taskImpl.setAllowResetDate(null);
		taskImpl.setAllowResetIdentity(null);
		return updateTask(task, task.getTaskStatus(), cNode, false, doerIdentity, Role.user);
	}

	@Override
	public Task submitRevisions(Task task, GTACourseNode cNode, int numOfDocs, Identity doerIdentity, Role by) {
		TaskImpl taskImpl = (TaskImpl)task;
		taskImpl.setSubmissionRevisionsDate(new Date());
		taskImpl.setSubmissionRevisionsNumOfDocs(numOfDocs);
		//log the date
		createAndPersistTaskRevisionDate(taskImpl, taskImpl.getRevisionLoop(), TaskProcess.correction);
		return updateTask(taskImpl, TaskProcess.correction, cNode, true, doerIdentity, by);
	}
	
	@Override
	public Task reviewedTask(Task task, GTACourseNode cNode, Identity doerIdentity, Role by) {
		TaskProcess solution = nextStep(TaskProcess.correction, cNode);
		TaskImpl taskImpl = (TaskImpl)task;
		taskImpl.setAcceptationDate(new Date());
		return updateTask(taskImpl, solution, cNode, false, doerIdentity, by);
	}

	@Override
	public Task updateTask(Task task, TaskProcess newStatus, GTACourseNode cNode, boolean incrementUserAttempts, Identity doerIdentity, Role by) {
		TaskImpl taskImpl = (TaskImpl)task;
		taskImpl.setTaskStatus(newStatus);
		syncDates(taskImpl, newStatus);
		taskImpl = dbInstance.getCurrentEntityManager().merge(taskImpl);
		syncAssessmentEntry(taskImpl, cNode, null, incrementUserAttempts, doerIdentity, by);
		
		// mark the publishers
		OLATResource resource = taskImpl.getTaskList().getEntry().getOlatResource();
		notificationsManager.markPublisherNews(getSubscriptionContext(resource, cNode, true), null, false);
		notificationsManager.markPublisherNews(getSubscriptionContext(resource, cNode, false), null, false);
		return taskImpl;
	}
	
	private void syncDates(TaskImpl taskImpl, TaskProcess newStatus) {
		//solution date
		if(newStatus == TaskProcess.solution || newStatus == TaskProcess.grading || newStatus == TaskProcess.graded) {
			if(taskImpl.getSolutionDate() == null) {
				taskImpl.setSolutionDate(new Date());
			}
		} else {
			taskImpl.setSolutionDate(null);
		}
		
		//graduation date
		if(newStatus == TaskProcess.graded) {
			if(taskImpl.getGraduationDate() == null) {
				taskImpl.setGraduationDate(new Date());
			}
		} else {
			taskImpl.setGraduationDate(null);
		}
		
		//check submission date because of reopen
		if(newStatus == TaskProcess.assignment || newStatus == TaskProcess.submit) {
			if(taskImpl.getSubmissionDate() != null) {
				taskImpl.setSubmissionDate(null);
				taskImpl.setSubmissionNumOfDocs(null);
			}
		}
	}
	
	@Override
	public TaskDueDate updateTaskDueDate(TaskDueDate taskDueDate) {
		((TaskDueDateImpl)taskDueDate).setLastModified(new Date());
		return dbInstance.getCurrentEntityManager().merge(taskDueDate);
	}

	@Override
	public Task updateTask(Task task, TaskProcess newStatus, int iteration, GTACourseNode cNode, boolean incrementUserAttempts, Identity doerIdentity, Role by) {
		TaskImpl taskImpl = (TaskImpl)task;
		taskImpl.setTaskStatus(newStatus);
		taskImpl.setRevisionLoop(iteration);
		taskImpl = dbInstance.getCurrentEntityManager().merge(taskImpl);
		//log date
		createAndPersistTaskRevisionDate(taskImpl, iteration, newStatus);
		syncAssessmentEntry(taskImpl, cNode, null, incrementUserAttempts, doerIdentity, by);
		return taskImpl;
	}

	@Override
	public boolean toggleMark(RepositoryEntry entry, GTACourseNode gtaNode, Identity marker, Identity participant) {
		if (entry == null || gtaNode == null || marker == null || participant == null) return false;
		
		TaskList taskList = getTaskList(entry, gtaNode);
		boolean isMarked = gtaMarkDao.isMarked(taskList, marker, participant);
		if (isMarked) {
			gtaMarkDao.deleteMark(taskList, marker, participant);
		} else {
			gtaMarkDao.createAndPersisitMark(taskList, marker, participant);
		}
		return !isMarked;
		
	}

	@Override
	public List<IdentityMark> getMarks(RepositoryEntry entry, GTACourseNode gtaNode, Identity marker) {
		TaskList taskList = getTaskList(entry, gtaNode);
		return gtaMarkDao.loadMarks(taskList, marker);
	}


	@Override
	public boolean hasMarks(RepositoryEntry entry, GTACourseNode gtaNode, Identity marker) {
		TaskList taskList = getTaskList(entry, gtaNode);
		return gtaMarkDao.hasMarks(taskList, marker);
	}

	@Override
	public AssessmentEntryStatus convertToAssessmentEntryStatus(Task task, GTACourseNode cNode) {
		TaskProcess status = task.getTaskStatus();
		TaskProcess firstStep = firstStep(cNode);
		
		AssessmentEntryStatus assessmentStatus;
		if(status == firstStep) {
			if(status == TaskProcess.grading) {
				assessmentStatus = AssessmentEntryStatus.inProgress;
			} else if(status == TaskProcess.solution) {
				if(cNode.getModuleConfiguration().getBooleanSafe(GTACourseNode.GTASK_GRADING)) {
					assessmentStatus = AssessmentEntryStatus.inProgress;
				} else {
					assessmentStatus = AssessmentEntryStatus.done;
				}
			} else {
				assessmentStatus = AssessmentEntryStatus.notStarted;
			}
		} else if(status == TaskProcess.review || status == TaskProcess.correction || status == TaskProcess.grading) {
			assessmentStatus = AssessmentEntryStatus.inReview;
		} else if(status == TaskProcess.graded) {
			assessmentStatus = AssessmentEntryStatus.done;
		} else if(status == TaskProcess.solution) {
			if(cNode.getModuleConfiguration().getBooleanSafe(GTACourseNode.GTASK_GRADING)) {
				assessmentStatus = AssessmentEntryStatus.inProgress;
			} else {
				assessmentStatus = AssessmentEntryStatus.done;
			}
		} else {
			assessmentStatus = AssessmentEntryStatus.inProgress;
		}
		return assessmentStatus;
	}

	@Override
	public boolean syncAssessmentEntry(Task taskImpl, GTACourseNode cNode,
			UserCourseEnvironment assessedUserCourseEnv, boolean incrementUserAttempts, Identity doerIdentity, Role by) {
		if(taskImpl == null || taskImpl.getTaskStatus() == null || cNode == null) return false;
		
		TaskList taskList = getTaskList(taskImpl);
		RepositoryEntry courseRepoEntry = taskList.getEntry();
		ICourse course = CourseFactory.loadCourse(courseRepoEntry);
		AssessmentEntryStatus assessmentStatus = convertToAssessmentEntryStatus(taskImpl, cNode);
		AssessmentEntryStatus currentAssessmentStatus = null;
		if(GTAType.group.name().equals(cNode.getModuleConfiguration().getStringValue(GTACourseNode.GTASK_TYPE))) {
			List<Identity> assessedIdentities = businessGroupRelationDao.getMembers(taskImpl.getBusinessGroup(), GroupRoles.participant.name());
			for(Identity assessedIdentity:assessedIdentities) {
				UserCourseEnvironment userCourseEnv;
				if(assessedUserCourseEnv != null && assessedUserCourseEnv.getIdentityEnvironment().getIdentity().equals(assessedIdentity)) {
					userCourseEnv = assessedUserCourseEnv;
				} else {
					userCourseEnv = AssessmentHelper.createAndInitUserCourseEnvironment(assessedIdentity, course);
				}
				AssessmentEvaluation scoreEvaluation = courseAssessmentService.getAssessmentEvaluation(cNode, userCourseEnv);
				AssessmentEvaluation newScoreEvaluation = new AssessmentEvaluation(scoreEvaluation, assessmentStatus);
	 			courseAssessmentService.saveScoreEvaluation(cNode, doerIdentity, newScoreEvaluation, userCourseEnv, incrementUserAttempts, by);
	 			if(doerIdentity != null && doerIdentity.equals(assessedIdentity)) {
	 				currentAssessmentStatus = scoreEvaluation.getAssessmentStatus();
	 			}
			}
		} else {
			Identity assessedIdentity = taskImpl.getIdentity();
			
			UserCourseEnvironment userCourseEnv;
			if(assessedUserCourseEnv != null && assessedUserCourseEnv.getIdentityEnvironment().getIdentity().equals(assessedIdentity)) {
				userCourseEnv = assessedUserCourseEnv;
			} else {
				userCourseEnv = AssessmentHelper.createAndInitUserCourseEnvironment(assessedIdentity, course);
			}
			AssessmentEvaluation scoreEvaluation = courseAssessmentService.getAssessmentEvaluation(cNode, userCourseEnv);
			currentAssessmentStatus = scoreEvaluation.getAssessmentStatus();
			AssessmentEvaluation newScoreEvaluation = new AssessmentEvaluation(scoreEvaluation, assessmentStatus);
 			courseAssessmentService.saveScoreEvaluation(cNode, doerIdentity, newScoreEvaluation, userCourseEnv, incrementUserAttempts, by);
		}
		return currentAssessmentStatus != assessmentStatus;
	}

	private TaskList loadForUpdate(TaskList tasks) {
		dbInstance.getCurrentEntityManager().detach(tasks);
		
		String q = "select tasks from gtatasklist tasks where tasks.key=:taskListKey";
		return dbInstance.getCurrentEntityManager()
				.createQuery(q, TaskList.class)
				.setParameter("taskListKey", tasks.getKey())
				.setLockMode(LockModeType.PESSIMISTIC_WRITE)
				.getSingleResult();
	}

	@Override
	public void log(String step, String operation, Task assignedTask, Identity actor, Identity assessedIdentity, BusinessGroup assessedGroup,
			CourseEnvironment courseEnv, GTACourseNode cNode, Role by) {
		//log
		String taskName = taskToString(assignedTask);
		String msg = step + " of " + taskName + ": " + operation;
		if(GTAType.group.name().equals(cNode.getModuleConfiguration().getStringValue(GTACourseNode.GTASK_TYPE))) {
			log.info(Tracing.M_AUDIT, "{} to business group: {}", msg, assessedGroup.getName());
			courseEnv.getAuditManager()
				.appendToUserNodeLog(cNode, actor, assessedGroup, msg, by);
		} else {
			log.info(Tracing.M_AUDIT, msg);
			courseEnv.getAuditManager()
				.appendToUserNodeLog(cNode, actor, assessedIdentity, msg, by);
		}
	}

	@Override
	public void log(String step, SubmitEvent event, Task assignedTask, Identity actor, Identity assessedIdentity, BusinessGroup assessedGroup,
			CourseEnvironment courseEnv, GTACourseNode cNode, Role by) {
		String operation = event.getLogMessage();
		String file = event.getFilename();
		//log
		String taskName = taskToString(assignedTask);
		String msg = step + " of " + taskName + ": " + operation + " " + file;
		if(GTAType.group.name().equals(cNode.getModuleConfiguration().getStringValue(GTACourseNode.GTASK_TYPE))) {
			log.info(Tracing.M_AUDIT, "{} to business group: {}", msg, assessedGroup.getName());
			courseEnv.getAuditManager()
				.appendToUserNodeLog(cNode, actor, assessedGroup, msg, by);
		} else {
			log.info(Tracing.M_AUDIT, msg);
			courseEnv.getAuditManager()
				.appendToUserNodeLog(cNode, actor, assessedIdentity, msg, by);
		}
	}
	
	private String taskToString(Task assignedTask) {
		String name;
		if(assignedTask == null ) {
			name = "no assignment";
		} else if(StringHelper.containsNonWhitespace(assignedTask.getTaskName())) {
			name = assignedTask.getTaskName();
		} else if(assignedTask.getKey() != null) {
			name = assignedTask.getKey().toString();
		} else {
			name = "no assignment";
		}
		return name;	
	}
	
	private interface TaskListSynched {
		
		public void sync();
		
	}
}
