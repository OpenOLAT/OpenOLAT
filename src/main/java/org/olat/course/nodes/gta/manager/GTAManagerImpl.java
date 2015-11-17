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
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.persistence.LockModeType;
import javax.persistence.Query;

import org.olat.basesecurity.GroupRoles;
import org.olat.basesecurity.IdentityRef;
import org.olat.core.commons.modules.bc.FolderConfig;
import org.olat.core.commons.modules.bc.vfs.OlatRootFolderImpl;
import org.olat.core.commons.persistence.DB;
import org.olat.core.commons.services.notifications.PublisherData;
import org.olat.core.commons.services.notifications.SubscriptionContext;
import org.olat.core.id.Identity;
import org.olat.core.logging.OLog;
import org.olat.core.logging.Tracing;
import org.olat.core.util.io.SystemFilenameFilter;
import org.olat.core.util.vfs.VFSContainer;
import org.olat.core.util.vfs.VFSManager;
import org.olat.course.nodes.GTACourseNode;
import org.olat.course.nodes.gta.AssignmentResponse;
import org.olat.course.nodes.gta.AssignmentResponse.Status;
import org.olat.course.nodes.gta.GTAManager;
import org.olat.course.nodes.gta.GTAType;
import org.olat.course.nodes.gta.Task;
import org.olat.course.nodes.gta.TaskLight;
import org.olat.course.nodes.gta.TaskList;
import org.olat.course.nodes.gta.TaskProcess;
import org.olat.course.nodes.gta.model.Membership;
import org.olat.course.nodes.gta.model.TaskImpl;
import org.olat.course.nodes.gta.model.TaskListImpl;
import org.olat.course.nodes.gta.ui.SubmitEvent;
import org.olat.course.run.environment.CourseEnvironment;
import org.olat.group.BusinessGroup;
import org.olat.group.BusinessGroupRef;
import org.olat.group.BusinessGroupService;
import org.olat.group.DeletableGroupData;
import org.olat.group.area.BGAreaManager;
import org.olat.group.manager.BusinessGroupRelationDAO;
import org.olat.group.model.BusinessGroupRefImpl;
import org.olat.modules.ModuleConfiguration;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryEntryRef;
import org.olat.repository.manager.RepositoryEntryRelationDAO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 
 * Initial date: 24.02.2015<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
@Service
public class GTAManagerImpl implements GTAManager, DeletableGroupData {
	
	private static final OLog log = Tracing.createLoggerFor(GTAManagerImpl.class);
	
	@Autowired
	private DB dbInstance;
	@Autowired
	private BGAreaManager areaManager;
	@Autowired
	private BusinessGroupService businessGroupService;
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
		OlatRootFolderImpl courseContainer = courseEnv.getCourseBaseContainer();
		VFSContainer nodesContainer = VFSManager.getOrCreateContainer(courseContainer, "gtasks");
		VFSContainer nodeContainer = VFSManager.getOrCreateContainer(nodesContainer, cNode.getIdent());
		return VFSManager.getOrCreateContainer(nodeContainer, folderName);
	}

	@Override
	public PublisherData getPublisherData(CourseEnvironment courseEnv, GTACourseNode cNode) {
		RepositoryEntry re = courseEnv.getCourseGroupManager().getCourseEntry();
		String businessPath = "[RepositoryEntry:" + re.getKey() + "][CourseNode:" + cNode.getIdent() + "]";
		PublisherData publisherData = new PublisherData("GroupTask", "", businessPath);
		return publisherData;
	}

	@Override
	public SubscriptionContext getSubscriptionContext(CourseEnvironment courseEnv, GTACourseNode cNode) {
		SubscriptionContext sc = new SubscriptionContext("CourseModule", courseEnv.getCourseResourceableId(), cNode.getIdent());
		return sc;
	}

	@Override
	public List<BusinessGroup> filterBusinessGroups(List<BusinessGroup> groups, GTACourseNode cNode) {
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
		if(groupKeys != null && groupKeys.size() > 0) {
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
			if(groupKeys != null && groupKeys.size() > 0) {
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
			if(groupKeys != null && groupKeys.size() > 0) {
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
			if(groupKeys != null && groupKeys.size() > 0) {
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
	public boolean isTasksInProcess(RepositoryEntryRef entry, GTACourseNode cNode) {
		List<Number> numOfTasks = dbInstance.getCurrentEntityManager()
				.createNamedQuery("isTasksInProcess", Number.class)
				.setParameter("entryKey", entry.getKey())
				.setParameter("courseNodeIdent", cNode.getIdent())
				.getResultList();
		return numOfTasks != null && !numOfTasks.isEmpty() && numOfTasks.get(0) != null && numOfTasks.get(0).intValue() > 0;
	}

	@Override
	public boolean isTaskInProcess(RepositoryEntryRef entry, GTACourseNode cNode, String taskName) {
		List<Number> numOfTasks = dbInstance.getCurrentEntityManager()
				.createNamedQuery("isTaskInProcess", Number.class)
				.setParameter("entryKey", entry.getKey())
				.setParameter("courseNodeIdent", cNode.getIdent())
				.setParameter("taskName", taskName)
				.getResultList();
		return numOfTasks != null && !numOfTasks.isEmpty() && numOfTasks.get(0) != null && numOfTasks.get(0).intValue() > 0;
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
	
	@Override
	public boolean deleteGroupDataFor(BusinessGroup group) {
		log.audit("Delete tasks of business group: " + group.getKey());
		String deleteTasks = "delete from gtatask as task where task.businessGroup.key=:groupKey";
		dbInstance.getCurrentEntityManager()
				.createQuery(deleteTasks)
				.setParameter("groupKey", group.getKey())
				.executeUpdate();
		return true;	

	}

	@Override
	public int deleteTaskList(RepositoryEntryRef entry, GTACourseNode cNode) {
		TaskList taskList = getTaskList(entry, cNode);
		
		int numOfDeletedObjects;
		if(taskList != null) {
			String deleteTasks = "delete from gtatask as task where task.taskList.key=:taskListKey";
			int numOfTasks = dbInstance.getCurrentEntityManager().createQuery(deleteTasks)
				.setParameter("taskListKey", taskList.getKey())
				.executeUpdate();
			dbInstance.getCurrentEntityManager().remove(taskList);
			numOfDeletedObjects = numOfTasks + 1;
		} else {
			numOfDeletedObjects = 0;
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
			int numOfTasks = deleteTaskQuery.setParameter("taskListKey", taskList.getKey()).executeUpdate();
			numOfDeletedObjects += numOfTasks;
		}
		
		String deleteTaskLists = "delete from gtatasklist as tasks where tasks.entry.key=:entryKey";
		numOfDeletedObjects +=  dbInstance.getCurrentEntityManager()
				.createQuery(deleteTaskLists)
				.setParameter("entryKey", entry.getKey())
				.executeUpdate();
		return numOfDeletedObjects;	
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
	
	@Override
	public AssignmentResponse assignTaskAutomatically(TaskList taskList, BusinessGroup assessedGroup, CourseEnvironment courseEnv, GTACourseNode cNode) {
		return assignTaskAutomatically(taskList, assessedGroup, null, courseEnv, cNode);
	}
	
	@Override
	public AssignmentResponse assignTaskAutomatically(TaskList taskList, Identity assessedIdentity, CourseEnvironment courseEnv, GTACourseNode cNode) {
		return assignTaskAutomatically(taskList, null, assessedIdentity, courseEnv, cNode);
	}
	
	private AssignmentResponse assignTaskAutomatically(TaskList tasks, BusinessGroup businessGroup, Identity identity,
			CourseEnvironment courseEnv, GTACourseNode cNode) {

		Task currentTask;
		if(businessGroup != null) {
			currentTask = getTask(businessGroup, tasks);
		} else {
			currentTask = getTask(identity, tasks);
		}

		AssignmentResponse response;
		if(currentTask == null) {
			TaskList reloadedTasks = loadForUpdate(tasks);

			File tasksFolder = getTasksDirectory(courseEnv, cNode);
			String[] taskFiles = tasksFolder.list(SystemFilenameFilter.FILES_ONLY);
			List<String> assignedFilenames = getAssignedTasks(reloadedTasks);
			
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
				TaskImpl task = createTask(taskName, reloadedTasks, nextStep, businessGroup, identity, cNode);
				task.setAssignmentDate(new Date());
				dbInstance.getCurrentEntityManager().persist(task);
				dbInstance.commit();
				response = new AssignmentResponse(task, Status.ok);
			}
		} else {
			if(currentTask.getTaskStatus() == TaskProcess.assignment) {
				((TaskImpl)currentTask).setTaskStatus(TaskProcess.submit);
			}
			currentTask = dbInstance.getCurrentEntityManager().merge(currentTask);
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
		//remove previous rounds
		Set<String> usedOnce = new HashSet<>();
		for(Iterator<String> usedSlotIt=usedSlots.iterator(); usedSlotIt.hasNext(); ) {
			String usedSlot = usedSlotIt.next();
			if(usedOnce.contains(usedSlot)) {
				usedSlotIt.remove();
			} else {
				usedOnce.add(usedSlot);
			}
		}
		
		//usedSlots are cleaned and contains only current round
		String nextSlot = null;
		for(String slot:slots) {
			if(!usedSlots.contains(slot)) {
				nextSlot = slot;
				break;
			}	
		}
		
		if(nextSlot == null) {
			//begin a new round
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
		return dbInstance.getCurrentEntityManager()
			.createNamedQuery("tasksByTaskList", String.class)
			.setParameter("taskListKey", taskList.getKey())
			.getResultList();
	}

	@Override
	public AssignmentResponse selectTask(Identity identity, TaskList tasks, GTACourseNode cNode, File taskFile) {
		if(!GTAType.individual.name().equals(cNode.getModuleConfiguration().getStringValue(GTACourseNode.GTASK_TYPE))) {
			return AssignmentResponse.ERROR;
		}
		return selectTask(identity, null, tasks, cNode, taskFile);
	}
	
	@Override
	public AssignmentResponse selectTask(BusinessGroup businessGroup, TaskList tasks, GTACourseNode cNode, File taskFile) {
		if(!GTAType.group.name().equals(cNode.getModuleConfiguration().getStringValue(GTACourseNode.GTASK_TYPE))) {
			return AssignmentResponse.ERROR;
		}
		return selectTask(null, businessGroup, tasks, cNode, taskFile);
	}
	
	private AssignmentResponse selectTask(Identity identity, BusinessGroup businessGroup, TaskList tasks, GTACourseNode cNode, File taskFile) {
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
				response = new AssignmentResponse(task, Status.ok);
			}
			dbInstance.commit();
		} else {
			if(currentTask.getTaskStatus() == TaskProcess.assignment) {
				TaskProcess nextStep = nextStep(currentTask.getTaskStatus(), cNode);
				((TaskImpl)currentTask).setTaskStatus(nextStep);
			}
			currentTask = dbInstance.getCurrentEntityManager().merge(currentTask);
			response = new AssignmentResponse(currentTask, Status.ok);
		}
		return response;
	}

	@Override
	public TaskImpl createTask(String taskName, TaskList taskList, TaskProcess status, BusinessGroup assessedGroup, Identity assessedIdentity, GTACourseNode cNode) {
		TaskImpl task = new TaskImpl();
		Date creationDate = new Date();
		task.setCreationDate(creationDate);
		task.setLastModified(creationDate);
		task.setTaskList(taskList);
		task.setTaskName(taskName);
		task.setTaskStatus(status);//assignment is ok -> go to submit step
		task.setRevisionLoop(0);
		
		if(GTAType.group.name().equals(cNode.getModuleConfiguration().getStringValue(GTACourseNode.GTASK_TYPE))) {
			task.setBusinessGroup(assessedGroup);
		} else {
			task.setIdentity(assessedIdentity);
		}
		return task;
	}

	@Override
	public Task nextStep(Task task, GTACourseNode cNode) {
		TaskImpl taskImpl = (TaskImpl)task;
		TaskProcess currentStep = taskImpl.getTaskStatus();
		//cascade through the possible steps
		TaskProcess nextStep = nextStep(currentStep, cNode);
		taskImpl.setTaskStatus(nextStep);
		TaskImpl mergedtask = dbInstance.getCurrentEntityManager().merge(taskImpl);
		dbInstance.commit();//make the thing definitiv
		return mergedtask;
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
	public Task updateTask(Task task, TaskProcess newStatus) {
		TaskImpl taskImpl = (TaskImpl)task;
		taskImpl.setTaskStatus(newStatus);
		return dbInstance.getCurrentEntityManager().merge(taskImpl);
	}

	@Override
	public Task updateTask(Task task, TaskProcess newStatus, int iteration) {
		TaskImpl taskImpl = (TaskImpl)task;
		taskImpl.setTaskStatus(newStatus);
		taskImpl.setRevisionLoop(iteration);
		return dbInstance.getCurrentEntityManager().merge(taskImpl);
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
			CourseEnvironment courseEnv, GTACourseNode cNode) {
		//log
		String msg = step + " of " + assignedTask.getTaskName() + ": " + operation;
		if(GTAType.group.name().equals(cNode.getModuleConfiguration().getStringValue(GTACourseNode.GTASK_TYPE))) {
			log.audit(msg + " to business group: " + assessedGroup.getName(), null);
			courseEnv.getAuditManager()
				.appendToUserNodeLog(cNode, actor, assessedGroup, msg);
		} else {
			log.audit(msg, null);
			courseEnv.getAuditManager()
				.appendToUserNodeLog(cNode, actor, assessedIdentity, msg);
		}
	}

	@Override
	public void log(String step, SubmitEvent event, Task assignedTask, Identity actor, Identity assessedIdentity, BusinessGroup assessedGroup,
			CourseEnvironment courseEnv, GTACourseNode cNode) {
		String operation = event.getLogMessage();
		String file = event.getFilename();
		//log
		String msg = step + " of " + assignedTask.getTaskName() + ": " + operation + " " + file;
		if(GTAType.group.name().equals(cNode.getModuleConfiguration().getStringValue(GTACourseNode.GTASK_TYPE))) {
			log.audit(msg + " to business group: " + assessedGroup.getName(), null);
			courseEnv.getAuditManager()
				.appendToUserNodeLog(cNode, actor, assessedGroup, msg);
		} else {
			log.audit(msg, null);
			courseEnv.getAuditManager()
				.appendToUserNodeLog(cNode, actor, assessedIdentity, msg);
		}
	}
}
