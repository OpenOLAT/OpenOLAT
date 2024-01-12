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
package org.olat.modules.project.manager;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

import jakarta.annotation.PostConstruct;

import org.apache.logging.log4j.Logger;
import org.olat.basesecurity.BaseSecurity;
import org.olat.basesecurity.Group;
import org.olat.basesecurity.GroupMembership;
import org.olat.basesecurity.IdentityRef;
import org.olat.basesecurity.OrganisationModule;
import org.olat.basesecurity.OrganisationService;
import org.olat.basesecurity.manager.GroupDAO;
import org.olat.commons.calendar.CalendarUtils;
import org.olat.commons.calendar.model.Kalendar;
import org.olat.core.commons.persistence.DB;
import org.olat.core.commons.services.doceditor.DocEditorService;
import org.olat.core.commons.services.doceditor.DocumentSavedEvent;
import org.olat.core.commons.services.notifications.NotificationsManager;
import org.olat.core.commons.services.notifications.PublisherData;
import org.olat.core.commons.services.notifications.Subscriber;
import org.olat.core.commons.services.notifications.SubscriptionContext;
import org.olat.core.commons.services.tag.Tag;
import org.olat.core.commons.services.tag.TagInfo;
import org.olat.core.commons.services.tag.TagService;
import org.olat.core.commons.services.vfs.VFSMetadata;
import org.olat.core.commons.services.vfs.VFSRepositoryService;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.media.MediaResource;
import org.olat.core.id.Identity;
import org.olat.core.id.Organisation;
import org.olat.core.id.OrganisationRef;
import org.olat.core.logging.Tracing;
import org.olat.core.util.DateRange;
import org.olat.core.util.DateUtils;
import org.olat.core.util.FileUtils;
import org.olat.core.util.Formatter;
import org.olat.core.util.StringHelper;
import org.olat.core.util.coordinate.CoordinatorManager;
import org.olat.core.util.event.GenericEventListener;
import org.olat.core.util.vfs.VFSConstants;
import org.olat.core.util.vfs.VFSContainer;
import org.olat.core.util.vfs.VFSItem;
import org.olat.core.util.vfs.VFSLeaf;
import org.olat.core.util.vfs.VFSStatus;
import org.olat.modules.project.ProjActivity;
import org.olat.modules.project.ProjActivity.Action;
import org.olat.modules.project.ProjActivitySearchParams;
import org.olat.modules.project.ProjAppointment;
import org.olat.modules.project.ProjAppointmentInfo;
import org.olat.modules.project.ProjAppointmentRef;
import org.olat.modules.project.ProjAppointmentSearchParams;
import org.olat.modules.project.ProjArtefact;
import org.olat.modules.project.ProjArtefactInfo;
import org.olat.modules.project.ProjArtefactInfoParams;
import org.olat.modules.project.ProjArtefactItems;
import org.olat.modules.project.ProjArtefactRef;
import org.olat.modules.project.ProjArtefactSearchParams;
import org.olat.modules.project.ProjArtefactToArtefact;
import org.olat.modules.project.ProjArtefactToArtefactSearchParams;
import org.olat.modules.project.ProjDecision;
import org.olat.modules.project.ProjDecisionInfo;
import org.olat.modules.project.ProjDecisionRef;
import org.olat.modules.project.ProjDecisionSearchParams;
import org.olat.modules.project.ProjFile;
import org.olat.modules.project.ProjFileInfo;
import org.olat.modules.project.ProjFileRef;
import org.olat.modules.project.ProjFileSearchParams;
import org.olat.modules.project.ProjMemberInfo;
import org.olat.modules.project.ProjMemberInfoSearchParameters;
import org.olat.modules.project.ProjMilestone;
import org.olat.modules.project.ProjMilestoneInfo;
import org.olat.modules.project.ProjMilestoneRef;
import org.olat.modules.project.ProjMilestoneSearchParams;
import org.olat.modules.project.ProjMilestoneStatus;
import org.olat.modules.project.ProjNote;
import org.olat.modules.project.ProjNoteInfo;
import org.olat.modules.project.ProjNoteRef;
import org.olat.modules.project.ProjNoteSearchParams;
import org.olat.modules.project.ProjProject;
import org.olat.modules.project.ProjProjectImageType;
import org.olat.modules.project.ProjProjectRef;
import org.olat.modules.project.ProjProjectSearchParams;
import org.olat.modules.project.ProjProjectSecurityCallback;
import org.olat.modules.project.ProjProjectToOrganisation;
import org.olat.modules.project.ProjProjectUserInfo;
import org.olat.modules.project.ProjTag;
import org.olat.modules.project.ProjTagSearchParams;
import org.olat.modules.project.ProjTemplateToOrganisation;
import org.olat.modules.project.ProjToDo;
import org.olat.modules.project.ProjToDoInfo;
import org.olat.modules.project.ProjToDoRef;
import org.olat.modules.project.ProjToDoSearchParams;
import org.olat.modules.project.ProjWhiteboardFileType;
import org.olat.modules.project.ProjectRole;
import org.olat.modules.project.ProjectSecurityCallbackFactory;
import org.olat.modules.project.ProjectService;
import org.olat.modules.project.ProjectStatus;
import org.olat.modules.project.model.ProjAppointmentInfoImpl;
import org.olat.modules.project.model.ProjArtefactInfoImpl;
import org.olat.modules.project.model.ProjArtefactItemsImpl;
import org.olat.modules.project.model.ProjDecisionInfoImpl;
import org.olat.modules.project.model.ProjFileInfoImpl;
import org.olat.modules.project.model.ProjMemberInfoImpl;
import org.olat.modules.project.model.ProjMilestoneInfoImpl;
import org.olat.modules.project.model.ProjNoteInfoImpl;
import org.olat.modules.project.model.ProjToDoInfoImpl;
import org.olat.modules.project.ui.ProjectBCFactory;
import org.olat.modules.todo.ToDoPriority;
import org.olat.modules.todo.ToDoRight;
import org.olat.modules.todo.ToDoRole;
import org.olat.modules.todo.ToDoService;
import org.olat.modules.todo.ToDoStatus;
import org.olat.modules.todo.ToDoTask;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 
 * Initial date: 22 Nov 2022<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
@Service
public class ProjectServiceImpl implements ProjectService, GenericEventListener {

	private static final Logger log = Tracing.createLoggerFor(ProjectServiceImpl.class);
	
	static final String DEFAULT_ROLE_NAME = ProjectRole.participant.name();
	
	@Autowired
	private DB dbInstance;
	@Autowired
	private ProjProjectDAO projectDao;
	@Autowired
	private ProjProjectToOrganisationDAO projectToOrganisationDao;
	@Autowired
	private ProjTemplateToOrganisationDAO templateToOrganisationDao;
	@Autowired
	private ProjProjectUserInfoDAO projectUserInfoDao;
	@Autowired
	private ProjectStorage projectStorage;
	@Autowired
	private ProjMemberQueries memberQueries;
	@Autowired
	private ProjArtefactDAO artefactDao;
	@Autowired
	private ProjArtefactToArtefactDAO artefactToArtefactDao;
	@Autowired
	private ProjFileDAO fileDao;
	@Autowired
	private VFSRepositoryService vfsRepositoryService;
	@Autowired
	private ProjToDoDAO toDoDao;
	@Autowired
	private ProjDecisionDAO decisionDao;
	@Autowired
	private ToDoService toDoService;
	@Autowired
	private ProjNoteDAO noteDao;
	@Autowired
	private ProjAppointmentDAO appointmentDao;
	@Autowired
	private ProjMilestoneDAO milestoneDao;
	@Autowired
	private ProjCalendarHelper calendarHelper;
	@Autowired
	private ProjActivityDAO activityDao;
	@Autowired
	private OrganisationModule organisationModule;
	@Autowired
	private OrganisationService organisationService;
	@Autowired
	protected BaseSecurity securityManager;
	@Autowired
	private GroupDAO groupDao;
	@Autowired
	private ProjTagDAO tagDao;
	@Autowired
	private TagService tagService;
	@Autowired
	private NotificationsManager notificationManager;
	
	@PostConstruct
	private void init() {
		CoordinatorManager.getInstance().getCoordinator().getEventBus().registerFor(this, null, DocEditorService.DOCUMENT_SAVED_EVENT_CHANNEL);
	}
	
	@Override
	public ProjProject createProject(Identity doer, ProjectBCFactory bcFactory, Identity owner) {
		Group baseGroup = groupDao.createGroup();
		groupDao.addMembershipOneWay(baseGroup, owner, ProjectRole.owner.name());
		
		ProjProject project = projectDao.create(doer, baseGroup, ProjProjectImageType.getRandmonAvatarCssClass());
		String after = ProjectXStream.toXml(project);
		activityDao.create(Action.projectCreate, null, after, doer, project);
		markNews(project);
		
		notificationManager.subscribe(owner, getSubscriptionContext(project), getPublisherData(bcFactory, project));
		
		return project;
	}
	
	@Override
	public ProjProject updateProject(Identity doer, ProjectBCFactory bcFactory, ProjProjectRef project, String externalRef, String title,
			String teaser, String description, boolean templatePrivate, boolean templatePublic) {
		ProjProject reloadedProject = getProject(project);
		if (reloadedProject == null) {
			return null;
		}
		String before = ProjectXStream.toXml(reloadedProject);
		
		boolean contentChanged = false;
		boolean titleChanged = false;
		if (!Objects.equals(reloadedProject.getExternalRef(), externalRef)) {
			reloadedProject.setExternalRef(externalRef);
			contentChanged = true;
		}
		if (!Objects.equals(reloadedProject.getTitle(), title)) {
			reloadedProject.setTitle(title);
			contentChanged = true;
			titleChanged = true;
		}
		if (!Objects.equals(reloadedProject.getTeaser(), teaser)) {
			reloadedProject.setTeaser(teaser);
			contentChanged = true;
		}
		if (!Objects.equals(reloadedProject.getDescription(), description)) {
			reloadedProject.setDescription(description);
			contentChanged = true;
		}
		if (reloadedProject.isTemplatePrivate() != templatePrivate) {
			reloadedProject.setTemplatePrivate(templatePrivate);
			contentChanged = true;
		}
		if (reloadedProject.isTemplatePublic() != templatePublic) {
			reloadedProject.setTemplatePublic(templatePublic);
			contentChanged = true;
		}
		
		if (contentChanged) {
			reloadedProject = projectDao.save(reloadedProject);
			
			String after = ProjectXStream.toXml(reloadedProject);
			activityDao.create(Action.projectContentUpdate, before, after, doer, reloadedProject);
			markNews(reloadedProject);
			
			if (titleChanged) {
				ProjAppointmentSearchParams searchParams = new ProjAppointmentSearchParams();
				searchParams.setProject(reloadedProject);
				searchParams.setStatus(List.of(ProjectStatus.active));
				getAppointmentInfos(searchParams, ProjArtefactInfoParams.MEMBERS)
						.forEach(info -> calendarHelper.createOrUpdateEvent(bcFactory, info.getAppointment(), info.getMembers()));
				
				toDoService.updateOriginTitle(ProjToDoProvider.TYPE, project.getKey(), null, reloadedProject.getTitle(), null);
			}
		}
		
		return reloadedProject;
	}
	
	@Override
	public ProjProject setStatusDone(Identity doer, ProjProjectRef project) {
		ProjProject reloadedProject = getProject(project);
		if (ProjectStatus.active == reloadedProject.getStatus()) {
			String before = ProjectXStream.toXml(reloadedProject);
			
			reloadedProject.setDeletedDate(null);
			reloadedProject.setDeletedBy(null);
			reloadedProject.setStatus(ProjectStatus.done);
			reloadedProject = projectDao.save(reloadedProject);
			
			String after = ProjectXStream.toXml(reloadedProject);
			activityDao.create(Action.projectStatusDone, before, after, doer, reloadedProject);
			markNews(reloadedProject);
			
			// Update to-dos
			toDoService.updateOriginDeleted(ProjToDoProvider.TYPE, project.getKey(), null, false, null, null);
		}
		return reloadedProject;
	}
	
	@Override
	public ProjProject reopen(Identity doer, ProjProjectRef project) {
		ProjProject reloadedProject = getProject(project);
		if (ProjectStatus.active != reloadedProject.getStatus()) {
			String before = ProjectXStream.toXml(reloadedProject);
			
			reloadedProject.setDeletedDate(null);
			reloadedProject.setDeletedBy(null);
			reloadedProject.setStatus(ProjectStatus.active);
			reloadedProject = projectDao.save(reloadedProject);
			
			String after = ProjectXStream.toXml(reloadedProject);
			activityDao.create(Action.projectStatusActive, before, after, doer, reloadedProject);
			markNews(reloadedProject);
			
			// Update to-dos
			toDoService.updateOriginDeleted(ProjToDoProvider.TYPE, project.getKey(), null, false, null, null);
		}
		return reloadedProject;
	}
	
	@Override
	public ProjProject setStatusDeleted(Identity doer, ProjectBCFactory bcFactory, ProjProjectRef project) {
		ProjProject reloadedProject = getProject(project);
		if (ProjectStatus.deleted != reloadedProject.getStatus()) {
			// Delete all members but owners
			ProjMemberInfoSearchParameters params = new ProjMemberInfoSearchParameters();
			params.setProject(reloadedProject);
			Map<Identity,Set<ProjectRole>> memberToRoles = getMemberToRoles(params);
			for (Map.Entry<Identity,Set<ProjectRole>> entry: memberToRoles.entrySet()) {
				if (entry.getValue().contains(ProjectRole.owner)) {
					updateMember(doer, bcFactory, reloadedProject, entry.getKey(), Set.of(ProjectRole.owner));
				} else {
					updateMember(doer, bcFactory, reloadedProject, entry.getKey(), Set.of());
				}
			}
			
			// Delete all appointment events from user calendars
			ProjAppointmentSearchParams searchParams = new ProjAppointmentSearchParams();
			searchParams.setProject(reloadedProject);
			searchParams.setStatus(List.of(ProjectStatus.active));
			getAppointmentInfos(searchParams, ProjArtefactInfoParams.MEMBERS)
					.forEach(info -> calendarHelper.deleteEvent(info.getAppointment(), info.getMembers()));
			
			String before = ProjectXStream.toXml(reloadedProject);
			
			Date now = new Date();
			reloadedProject.setDeletedDate(now);
			reloadedProject.setDeletedBy(doer);
			reloadedProject.setStatus(ProjectStatus.deleted);
			reloadedProject = projectDao.save(reloadedProject);
			
			String after = ProjectXStream.toXml(reloadedProject);
			activityDao.create(Action.projectStatusDelete, before, after, doer, reloadedProject);
			markNews(reloadedProject);
			
			// Update to-dos
			toDoService.updateOriginDeleted(ProjToDoProvider.TYPE, project.getKey(), null, true, now, doer);
		}
		return reloadedProject;
	}
	
	@Override
	public ProjProject getProject(ProjProjectRef project) {
		ProjProjectSearchParams searchParams = new ProjProjectSearchParams();
		searchParams.setProjectKeys(List.of(project));
		List<ProjProject> projects = projectDao.loadProjects(searchParams);
		return projects != null && !projects.isEmpty()? projects.get(0): null;
	}

	@Override
	public List<ProjProject> getProjects(ProjProjectSearchParams searchParams) {
		return projectDao.loadProjects(searchParams);
	}
	
	@Override
	public void updateProjectOrganisations(Identity doer, ProjProject project, Collection<Organisation> organisations) {
		// If the organisation module is not enabled add the project to the default organisation
		if (!organisationModule.isEnabled()) {
			List<ProjProjectToOrganisation> currentRelations = projectToOrganisationDao.loadRelations(project, null);
			if (currentRelations.isEmpty()) {
				projectToOrganisationDao.createRelation(project, organisationService.getDefaultOrganisation());
				// No activity log
			}
			return;
		}
		
		if (organisations == null || organisations.isEmpty()) {
			// All projects have to have a relation to a organisation.
			log.warn("{} tries to remove project {} (key::{}) from all organisations", doer, project.getTitle(), project.getKey());
			return;
		}
		
		List<ProjProjectToOrganisation> currentRelations = projectToOrganisationDao.loadRelations(project, null);
		List<Organisation> currentOrganisations = currentRelations.stream()
				.map(ProjProjectToOrganisation::getOrganisation)
				.collect(Collectors.toList());
		
		// Create relation for new organisations
		for (Organisation organisation : organisations) {
			if (!currentOrganisations.contains(organisation)) {
				projectToOrganisationDao.createRelation(project, organisation);
				activityDao.create(Action.projectOrganisationAdd, null, null, doer, project, organisation);
			}
		}
		
		// Delete relation of old organisations
		for (ProjProjectToOrganisation relation : currentRelations) {
			if (!organisations.contains(relation.getOrganisation())) {
				projectToOrganisationDao.delete(relation);
				activityDao.create(Action.projectOrganisationRemove, null, null, doer, project, relation.getOrganisation());
			}
		}
	}
	
	@Override
	public List<Organisation> getOrganisations(ProjProjectRef project) {
		if (project == null) return List.of();
		
		return projectToOrganisationDao.loadOrganisations(project);
	}
	
	@Override
	public Map<Long, Set<Long>> getProjectKeyToOrganisationKey(List<? extends ProjProjectRef> projects) {
		return projectToOrganisationDao.getProjectKeyToOrganisationKeys(projects);
	}
	
	@Override
	public boolean isInOrganisation(ProjProjectRef project, Collection<OrganisationRef> organisations) {
		List<Long> projectOrganisationKeys = getOrganisations(project).stream().map(Organisation::getKey).toList();
		List<Long> organisationKeys = organisations.stream().map(OrganisationRef::getKey).toList();
		return !Collections.disjoint(projectOrganisationKeys, organisationKeys);
	}
	
	@Override
	public void updateTemplateOrganisations(Identity doer, ProjProject project, Collection<Organisation> organisations) {
		List<ProjTemplateToOrganisation> currentRelations = templateToOrganisationDao.loadRelations(project, null);
		List<Organisation> currentOrganisations = currentRelations.stream()
				.map(ProjTemplateToOrganisation::getOrganisation)
				.collect(Collectors.toList());
		
		// Create relation for new organisations
		for (Organisation organisation : organisations) {
			if (!currentOrganisations.contains(organisation)) {
				templateToOrganisationDao.createRelation(project, organisation);
				activityDao.create(Action.projectTemplateOrganisationAdd, null, null, doer, project, organisation);
			}
		}
		
		// Delete relation of old organisations
		for (ProjTemplateToOrganisation relation : currentRelations) {
			if (!organisations.contains(relation.getOrganisation())) {
				templateToOrganisationDao.delete(relation);
				activityDao.create(Action.projectTemplateOrganisationRemove, null, null, doer, project, relation.getOrganisation());
			}
		}
	}
	
	@Override
	public List<Organisation> getTemplateOrganisations(ProjProjectRef project) {
		if (project == null) return List.of();
		
		return templateToOrganisationDao.loadOrganisations(project);
	}
	
	@Override
	public void storeProjectImage(Identity doer, ProjProjectRef project, ProjProjectImageType type, File file, String filename) {
		VFSLeaf projectImage = getProjectImage(project, type);
		String filenameBefore = null;
		if (projectImage != null) {
			filenameBefore = projectImage.getName();
		}
		
		projectStorage.storeProjectImage(project, type, doer, file, filename);
		
		projectImage = getProjectImage(project, type);
		String filenameAfter = null;
		if (projectImage != null) {
			filenameAfter = projectImage.getName();
		}
		
		if (!Objects.equals(filenameBefore, filenameAfter)) {
			createProjectImageActivity(doer, project, type, filenameBefore, filenameAfter);
		}
	}
	
	@Override
	public void deleteProjectImage(Identity doer, ProjProjectRef project, ProjProjectImageType type) {
		VFSLeaf projectImage = getProjectImage(project, type);
		if (projectImage != null) {
			createProjectImageActivity(doer, project, type, projectImage.getName(), null);
		}
		
		projectStorage.deleteProjectImage(project, type);
	}
	
	@Override
	public VFSLeaf getProjectImage(ProjProjectRef project, ProjProjectImageType type) {
		return projectStorage.getProjectImage(project, type);
	}
	
	private void createProjectImageActivity(Identity doer, ProjProjectRef project, ProjProjectImageType type, String before, String after) {
		Action action = switch (type) {
			case avatar -> Action.projectImageAvatarUpdate;
			case background -> Action.projectImageBackgroundUpdate;
		};
		
		if (action != null) {
			ProjProject reloadedProject = getProject(project);
			activityDao.create(action, before, after, doer, reloadedProject);
			markNews(reloadedProject);
		}
	}

	@Override
	public void updateMember(Identity doer, ProjectBCFactory bcFactory, ProjProject project, Identity identity, Set<ProjectRole> roles) {
		Group group = project.getBaseGroup();
		
		List<ProjectRole> currentRoles = groupDao.getMemberships(group, identity).stream()
				.map(GroupMembership::getRole)
				.map(ProjectRole::valueOf)
				.collect(Collectors.toList());
		if (currentRoles.isEmpty() && roles.isEmpty()) {
			// Identity was not member and will not be member.
			return;
		}
		
		String rolesBeforeXml = ProjectXStream.rolesToXml(currentRoles.stream().map(ProjectRole::name).collect(Collectors.toList()));
		
		if (roles == null || roles.isEmpty()) {
			groupDao.removeMembership(group, identity);
			
			activityDao.create(Action.projectRolesUpdate, rolesBeforeXml, null, doer, project, identity);
			activityDao.create(Action.projectMemberRemove, null, null, doer, project, identity);
			markNews(project);
			
			Subscriber subscriber = notificationManager.getSubscriber(identity, getSubscriptionContext(project));
			if (subscriber != null) {
				notificationManager.deleteSubscriber(subscriber.getKey());
			}
			
			ProjMilestoneSearchParams searchParams = new ProjMilestoneSearchParams();
			searchParams.setProject(project);
			milestoneDao.loadMilestones(searchParams).forEach(milestone -> calendarHelper.deleteEvent(milestone, identity));
			
			return;
		}
		
		// Create membership for new roles
		boolean rolesChanged = false;
		for (ProjectRole role : roles) {
			if (!currentRoles.contains(role)) {
				groupDao.addMembershipOneWay(group, identity, role.name());
				rolesChanged = true;
			}
			
		}
		
		// Delete membership of old roles. Invites remain invites
		for (ProjectRole currentRole : currentRoles) {
			if (ProjectRole.invitee != currentRole && !roles.contains(currentRole)) {
				groupDao.removeMembership(group, identity, currentRole.name());
				rolesChanged = true;
			}
		}
		
		if (rolesChanged) {
			String rolesAfterXml = ProjectXStream.rolesToXml(roles.stream().map(ProjectRole::name).collect(Collectors.toList()));
			activityDao.create(Action.projectRolesUpdate, rolesBeforeXml, rolesAfterXml, doer, project, identity);
		}
		
		if (currentRoles.isEmpty() || (currentRoles.size() == 1 && currentRoles.get(0) == ProjectRole.invitee)) {
			activityDao.create(Action.projectMemberAdd, null, null, doer, project, identity);
			markNews(project);
				
			notificationManager.subscribe(identity, getSubscriptionContext(project), getPublisherData(bcFactory, project));
			
			ProjMilestoneSearchParams searchParams = new ProjMilestoneSearchParams();
			searchParams.setProject(project);
			searchParams.setStatus(List.of(ProjectStatus.active));
			milestoneDao.loadMilestones(searchParams).forEach(milestone -> calendarHelper.createOrUpdateEvent(bcFactory, milestone, List.of(identity)));
		}
	}
	
	@Override
	public void updateMembers(Identity doer, ProjectBCFactory bcFactory, ProjProject project, Map<Identity, Set<ProjectRole>> identityToRoles) {
		identityToRoles.entrySet().forEach(identityToRole -> updateMember(doer, bcFactory, project, identityToRole.getKey(), identityToRole.getValue()));
	}
	
	@Override
	public void removeMembers(Identity doer, ProjectBCFactory bcFactory, ProjProject project, Collection<Identity> identities) {
		identities.forEach(identity -> updateMember(doer, bcFactory, project, identity, Set.of()));
	}

	@Override
	public boolean isProjectMember(IdentityRef identity) {
		return memberQueries.isProjectMember(identity);
	}
	
	private Set<Identity> getMembers(ProjProject project) {
		Long projectGroupKey = project.getBaseGroup().getKey();
		Set<Identity> members = memberQueries.getGroupKeyToIdentities(List.of(projectGroupKey)).getOrDefault(projectGroupKey, Set.of());
		return members;
	}
	
	@Override
	public List<Identity> getMembers(ProjProject project, Collection<ProjectRole> roles) {
		return getProjMemberships(List.of(project), roles).stream()
				.map(GroupMembership::getIdentity)
				.distinct()
				.collect(Collectors.toList());
	}
	
	@Override
	public Map<Long, List<Identity>> getProjectGroupKeyToMembers(Collection<ProjProject> projects, Collection<ProjectRole> roles) {
		return getProjMemberships(projects, roles).stream()
				.collect(Collectors.groupingBy(
						membership -> membership.getGroup().getKey(),
						Collectors.collectingAndThen(
								Collectors.toList(),
								memberships -> memberships.stream()
										.map(GroupMembership::getIdentity)
										.distinct()
										.collect(Collectors.toList()))));
	}
	
	private List<GroupMembership> getProjMemberships(Collection<ProjProject> projects, Collection<ProjectRole> roles) {
		Collection<Long> groupKeys = projects.stream().map(ProjProject::getBaseGroup).map(Group::getKey).collect(Collectors.toSet());
		Collection<String> roleNames = roles.stream().map(ProjectRole::name).collect(Collectors.toSet());
		return groupDao.getMemberships(groupKeys, roleNames);
	}
	
	@Override
	public int countMembers(ProjProject project) {
		return groupDao.countMembers(project.getBaseGroup());
	}
	
	@Override
	public Set<ProjectRole> getRoles(ProjProject project, IdentityRef identity) {
		return groupDao.getMemberships(project.getBaseGroup(), identity).stream()
				.map(GroupMembership::getRole)
				.map(ProjectRole::valueOf)
				.collect(Collectors.toSet());
	}
	
	@Override
	public List<ProjMemberInfo> getMembersInfos(ProjMemberInfoSearchParameters params) {
		Map<Identity, Set<ProjectRole>> identityRoRoles = getMemberToRoles(params);
		
		Map<Long, ProjProjectUserInfo> identityKeyToInfo = projectUserInfoDao.loadProjectUserInfos(params.getProject(), identityRoRoles.keySet()).stream()
				.collect(Collectors.toMap(
						info -> info.getIdentity().getKey(),
						Function.identity()));
		
		List<ProjMemberInfo> memberInfos = new ArrayList<>(identityRoRoles.size());
		for (Entry<Identity, Set<ProjectRole>> entry : identityRoRoles.entrySet()) {
			ProjMemberInfoImpl memberInfo = new ProjMemberInfoImpl();
			Identity identity = entry.getKey();
			memberInfo.setIdentity(identity);
			memberInfo.setRoles(entry.getValue());
			
			ProjProjectUserInfo projectUserInfo = identityKeyToInfo.get(identity.getKey());
			if (projectUserInfo != null) {
				memberInfo.setLastVisitDate(projectUserInfo.getLastVisitDate());
			}
			
			memberInfos.add(memberInfo);
		}
		return memberInfos;
	}
	
	private Map<Identity, Set<ProjectRole>> getMemberToRoles(ProjMemberInfoSearchParameters params) {
		return memberQueries.getProjMemberships(params).stream()
				.collect(Collectors.groupingBy(
						GroupMembership::getIdentity, 
						Collectors.collectingAndThen(
								Collectors.toList(),
								memberships -> memberships.stream()
										.map(GroupMembership::getRole)
										.map(ProjectRole::valueOf)
										.collect(Collectors.toSet()))));
	}

	@Override
	public ProjProjectUserInfo getOrCreateProjectUserInfo(ProjProject project, Identity identity) {
		List<ProjProjectUserInfo> projectUserInfos = projectUserInfoDao.loadProjectUserInfos(project, List.of(identity));
		
		return projectUserInfos == null || projectUserInfos.isEmpty()
				? projectUserInfoDao.create(project, identity)
				: projectUserInfos.get(0);
	}

	@Override
	public ProjProjectUserInfo updateProjectUserInfo(ProjProjectUserInfo projectUserInfo) {
		return projectUserInfoDao.save(projectUserInfo);
	}


	@Override
	public VFSContainer getProjectContainer(ProjProjectRef project) {
		return projectStorage.getOrCreateFileContainer(project);
	}
	
	@Override
	public void createWhiteboard(Identity doer, ProjProject project, Locale locale) {
		VFSLeaf whiteboard = getWhiteboard(project, ProjWhiteboardFileType.board);
		if (whiteboard != null) {
			return;
		}
		
		projectStorage.createWhiteboard(doer, project, locale, ProjWhiteboardFileType.board);
		projectStorage.createWhiteboard(doer, project, locale, ProjWhiteboardFileType.preview);
		projectStorage.createWhiteboard(doer, project, locale, ProjWhiteboardFileType.previewPng);
		activityDao.create(Action.whiteboardCreate, null, null, doer, project);
	}
	
	@Override
	public void copyWhiteboardToFiles(Identity doer, ProjProject project) {
		VFSLeaf whiteboard = getWhiteboard(project, ProjWhiteboardFileType.board);
		if (whiteboard == null) {
			return;
		}
		
		activityDao.create(Action.whiteboardCopyToFiles, null, null, doer, project);
		
		try (InputStream inputStream = whiteboard.getInputStream()) {
			String filename = StringHelper.transformDisplayNameToFileSystemName(project.getTitle() + "_whiteboard_")
					+ Formatter.formatDatetimeFilesystemSave(new Date())
					+ "." + FileUtils.getFileSuffix(whiteboard.getName());
			createFile(doer, project, filename, inputStream, false);
		} catch(IOException e) {
			log.error("", e);
		}
	}
	
	@Override
	public void resetWhiteboard(Identity doer, ProjProject project) {
		copyWhiteboardToFiles(doer, project);
		
		projectStorage.deleteWhiteboard(project);
		activityDao.create(Action.whiteboardDelete, null, null, doer, project);
	}
	
	@Override
	public VFSLeaf getWhiteboard(ProjProjectRef project, ProjWhiteboardFileType type) {
		return projectStorage.getWhiteboard(project, type);
	}
	
	@Override
	public SubscriptionContext getSubscriptionContext(ProjProject project) {
		return new SubscriptionContext(ProjProject.TYPE, project.getResourceableId(), ProjProject.TYPE);
	}
	
	@Override
	public PublisherData getPublisherData(ProjectBCFactory bcFactory, ProjProject project) {
		return new PublisherData(ProjProject.TYPE, "", bcFactory.getBusinessPath(project, null, null));
	}
	
	private void markNews(ProjProject project) {
		notificationManager.markPublisherNews(getSubscriptionContext(project), null, false);
	}
	
	@Override
	public MediaResource createWordReport(Identity doer, ProjProjectRef project, Collection<String> artefactTypes,
			DateRange dateRange, boolean includeTimeline, Locale locale) {
		ProjProject reloadedProject = getProject(project);
		ProjProjectSecurityCallback secCallback = ProjectSecurityCallbackFactory.createDefaultCallback(reloadedProject,
				getRoles(reloadedProject, doer), false, false);
		ProjReportWordExport reportWordExport = new ProjReportWordExport(this, memberQueries, reloadedProject,
				secCallback, artefactTypes, dateRange, includeTimeline, locale);
		if (artefactTypes.contains(ProjNote.TYPE) && !reportWordExport.getNotes().isEmpty()
				|| artefactTypes.contains(ProjFile.TYPE) && !reportWordExport.getFiles().isEmpty()) {
			return new ProjectMediaResource(this, dbInstance, doer, reloadedProject, reportWordExport,
					reportWordExport.getFiles(), reportWordExport.getNotes(), reloadedProject.getTitle());
		}
		
		return new ProjReportWordMediaResource(reportWordExport);
	}
	
	@Override
	public MediaResource createMediaResource(Identity doer, ProjProject project, Collection<ProjFile> files,
			Collection<ProjNote> notes, String filename) {
		return new ProjectMediaResource(this, dbInstance, doer, project, null, files, notes, filename);
	}
	
	
	/*
	 * Artefacts
	 */
	@Override
	public void linkArtefacts(Identity doer, ProjArtefact artefact1, ProjArtefact artefact2) {
		if (!artefactToArtefactDao.exists(artefact1, artefact2)) {
			artefactToArtefactDao.create(artefact1.getProject(), doer, artefact1, artefact2);
			updateContentModified(artefact1, doer);
			updateContentModified(artefact2, doer);
			activityDao.create(Action.addReference(artefact1.getType()), null, null, doer, artefact1, artefact2);
			activityDao.create(Action.addReference(artefact2.getType()), null, null, doer, artefact2, artefact1);
		}
	}
	
	@Override
	public void unlinkArtefacts(Identity doer, ProjArtefact artefact1, ProjArtefact artefact2) {
		if (artefactToArtefactDao.exists(artefact1, artefact2)) {
			artefactToArtefactDao.delete(artefact1, artefact2);
			updateContentModified(artefact1, doer);
			updateContentModified(artefact2, doer);
			activityDao.create(Action.removeReference(artefact1.getType()), null, null, doer, artefact1, artefact2);
			activityDao.create(Action.removeReference(artefact2.getType()), null, null, doer, artefact2, artefact1);
		}
	}
	
	@Override
	public void updateLinkedArtefacts(Identity doer, ProjArtefact artefact, Set<ProjArtefact> linkedArtefacts) {
		List<ProjArtefact> currentLinkedArtefacts = getLinkedArtefacts(artefact);
		
		for (ProjArtefact linkedArtefact : linkedArtefacts) {
			if (!currentLinkedArtefacts.contains(linkedArtefact)) {
				linkArtefacts(doer, artefact, linkedArtefact);
			}
		}
		
		for (ProjArtefact currentLinkedArtefact : currentLinkedArtefacts) {
			if (!linkedArtefacts.contains(currentLinkedArtefact)) {
				unlinkArtefacts(doer, artefact, currentLinkedArtefact);
			}
		}
	}
	
	@Override
	public List<ProjArtefact> getLinkedArtefacts(ProjArtefact artefact) {
		ProjArtefactToArtefactSearchParams ataSearchParams = new ProjArtefactToArtefactSearchParams();
		ataSearchParams.setArtefact(artefact);
		return artefactToArtefactDao.loadArtefactToArtefacts(ataSearchParams).stream()
				.map(ata -> artefact.getKey().equals(ata.getArtefact1().getKey())? ata.getArtefact2(): ata.getArtefact1())
				.filter(linkedArtefcat -> ProjectStatus.active == linkedArtefcat.getStatus())
				.toList();
	}
	
	private Map<ProjArtefact, Set<ProjArtefact>> getArtefactToLinkedArtefacts(Collection<? extends ProjArtefactRef> artefacts) {
		ProjArtefactToArtefactSearchParams ataSearchParams = new ProjArtefactToArtefactSearchParams();
		ataSearchParams.setArtefacts(artefacts);
		
		Map<ProjArtefact, Set<ProjArtefact>> artefactToReferences = new HashMap<>();
		for (ProjArtefactToArtefact ata : artefactToArtefactDao.loadArtefactToArtefacts(ataSearchParams)) {
			ProjArtefact artefact1 = ata.getArtefact1();
			ProjArtefact artefact2 = ata.getArtefact2();
			artefactToReferences.computeIfAbsent(artefact1, key -> new HashSet<>()).add(artefact2);
			artefactToReferences.computeIfAbsent(artefact2, key -> new HashSet<>()).add(artefact1);
		}
		return artefactToReferences;
	}
	
	@Override
	public ProjArtefactItems getLinkedArtefactItems(ProjArtefact artefact) {
		if (artefact == null) {
			return new ProjArtefactItemsImpl();
		}
		
		ProjArtefactToArtefactSearchParams ataSearchParams = new ProjArtefactToArtefactSearchParams();
		ataSearchParams.setArtefact(artefact);
		Map<String, List<ProjArtefact>> typeToArtefacts = artefactToArtefactDao.loadArtefactToArtefacts(ataSearchParams).stream()
				.map(ata -> artefact.getKey().equals(ata.getArtefact1().getKey())? ata.getArtefact2(): ata.getArtefact1())
				.filter(a -> a.getStatus() != ProjectStatus.deleted)
				.collect(Collectors.groupingBy(ProjArtefact::getType));
		
		return loadArtefacts(typeToArtefacts);
	}
	
	@Override
	public ProjArtefactItems getArtefactItems(ProjArtefactSearchParams searchParams) {
		Map<String, List<ProjArtefact>> typeToArtefacts = artefactDao.loadArtefacts(searchParams).stream()
				.collect(Collectors.groupingBy(ProjArtefact::getType));
		
		return loadArtefacts(typeToArtefacts);
	}
	
	@Override
	public ProjArtefactItems getQuickStartArtefactItems(ProjProjectRef project, Identity identity) {
		Map<String, List<ProjArtefact>> typeToArtefacts = artefactDao.loadQuickSearchArtefacts(project, identity).stream()
				.collect(Collectors.groupingBy(ProjArtefact::getType));
		
		return loadArtefacts(typeToArtefacts);
	}

	private ProjArtefactItems loadArtefacts(Map<String, List<ProjArtefact>> typeToArtefacts) {
		ProjArtefactItemsImpl artefacts = new ProjArtefactItemsImpl();
		
		List<ProjArtefact> fileArtefacts = typeToArtefacts.get(ProjFile.TYPE);
		if (fileArtefacts != null) {
			ProjFileSearchParams searchParams = new ProjFileSearchParams();
			searchParams.setArtefacts(fileArtefacts);
			List<ProjFile> files = fileDao.loadFiles(searchParams);
			artefacts.setFiles(files);
		}
		
		List<ProjArtefact> toDoArtefacts = typeToArtefacts.get(ProjToDo.TYPE);
		if (toDoArtefacts != null) {
			ProjToDoSearchParams searchParams = new ProjToDoSearchParams();
			searchParams.setArtefacts(toDoArtefacts);
			List<ProjToDo> toDos = toDoDao.loadToDos(searchParams);
			artefacts.setToDos(toDos);
		}
		
		List<ProjArtefact> decisionArtefacts = typeToArtefacts.get(ProjDecision.TYPE);
		if (decisionArtefacts != null) {
			ProjDecisionSearchParams searchParams = new ProjDecisionSearchParams();
			searchParams.setArtefacts(decisionArtefacts);
			List<ProjDecision> decisions = decisionDao.loadDecisions(searchParams);
			artefacts.setDecisions(decisions);
		}
		
		List<ProjArtefact> noteArtefacts = typeToArtefacts.get(ProjNote.TYPE);
		if (noteArtefacts != null) {
			ProjNoteSearchParams searchParams = new ProjNoteSearchParams();
			searchParams.setArtefacts(noteArtefacts);
			List<ProjNote> notes = noteDao.loadNotes(searchParams);
			artefacts.setNotes(notes);
		}
		
		List<ProjArtefact> appointmentArtefacts = typeToArtefacts.get(ProjAppointment.TYPE);
		if (appointmentArtefacts != null) {
			ProjAppointmentSearchParams searchParams = new ProjAppointmentSearchParams();
			searchParams.setArtefacts(appointmentArtefacts);
			List<ProjAppointment> appointments = appointmentDao.loadAppointments(searchParams);
			artefacts.setAppointments(appointments);
		}
		
		List<ProjArtefact> milestoneArtefacts = typeToArtefacts.get(ProjMilestone.TYPE);
		if (milestoneArtefacts != null) {
			ProjMilestoneSearchParams searchParams = new ProjMilestoneSearchParams();
			searchParams.setArtefacts(milestoneArtefacts);
			List<ProjMilestone> milestones = milestoneDao.loadMilestones(searchParams);
			artefacts.setMilestones(milestones);
		}
		
		return artefacts;
	}
	
	private ProjArtefact getArtefact(ProjArtefactRef artefactRef) {
		ProjArtefactSearchParams searchParams = new ProjArtefactSearchParams();
		searchParams.setArtefacts(List.of(artefactRef));
		searchParams.setStatus(List.of(ProjectStatus.active));
		List<ProjArtefact> artefacts = artefactDao.loadArtefacts(searchParams);
		return !artefacts.isEmpty()? artefacts.get(0): null;
	}
	
	private Map<ProjArtefact, ProjArtefactInfo> getArtefactToInfo(List<ProjArtefact> artefacts, ProjArtefactInfoParams params) {
		Map<Long, Set<Identity>> groupKeyToIdentities;
		if (params.isMembers()) {
			List<Long> groupKeys = artefacts.stream().map(artefact -> artefact.getBaseGroup().getKey()).toList();
			groupKeyToIdentities = memberQueries.getGroupKeyToIdentities(groupKeys);
		} else {
			groupKeyToIdentities = Collections.emptyMap();
		}
		
		Map<ProjArtefact, Set<ProjArtefact>> artefactToLinkedArtefacts = params.isNumReferences()
				? getArtefactToLinkedArtefacts(artefacts)
				: Collections.emptyMap();
		
		List<ProjTag> projTags = params.isTags()
				? getProjTags(artefacts)
				: Collections.emptyList();
		
		Map<Long, List<Tag>> artefactKeyToTags = params.isTags()
				? getArtefactKeyToTag(projTags)
				: Collections.emptyMap();
		
		Map<ProjArtefact, ProjArtefactInfo> infos = new HashMap<>(artefacts.size());
		for (ProjArtefact artefact : artefacts) {
			ProjArtefactInfoImpl info = new ProjArtefactInfoImpl();
			info.setMembers(groupKeyToIdentities.getOrDefault(artefact.getBaseGroup().getKey(), Set.of()));
			info.setNumReferences(artefactToLinkedArtefacts.getOrDefault(artefact, Set.of()).size());
			info.setTags(artefactKeyToTags.getOrDefault(artefact.getKey(), 
					params.isTags()? new ArrayList<>(0): List.of()));
			infos.put(artefact, info);
		}
		
		return infos;
	}
	
	@Override
	public void updateMembers(Identity doer, ProjectBCFactory bcFactory, ProjArtefactRef artefactRef, List<IdentityRef> identities) {
		ProjArtefact artefact = getArtefact(artefactRef);
		if (artefact == null) return;
		
		List<Identity> members = securityManager.loadIdentityByRefs(identities);
		
		updateMembers(doer, bcFactory, artefact, members);
	}

	private void updateMembers(Identity doer, ProjectBCFactory bcFactory, ProjArtefact artefact, List<Identity> members) {
		Group group = artefact.getBaseGroup();
		List<Identity> currentMembers = groupDao.getMembers(group, DEFAULT_ROLE_NAME);
		
		List<Identity> toAdd = new ArrayList<>(members);
		toAdd.removeAll(currentMembers);
		toAdd.forEach(identity -> updateMember(doer, artefact, identity, Set.of(DEFAULT_ROLE_NAME)));
		
		List<Identity> toRemove = new ArrayList<>(currentMembers);
		toRemove.removeAll(members);
		toRemove.forEach(identity -> updateMember(doer, artefact, identity, Set.of()));
		
		onUpdateMembers(bcFactory, artefact, toAdd, toRemove);
		
		if (!toAdd.isEmpty() || !toRemove.isEmpty()) {
			updateContentModified(artefact, doer);
		}
	}
	
	private void updateMember(Identity doer, ProjArtefact artefact, Identity identity, Set<String> roles) {
		updateMember(doer, artefact, identity, roles, null, null);
	}
	
	private void updateMember(Identity doer, ProjArtefact artefact, Identity identity, Set<String> roles, List<Identity> addedIdentities, List<Identity> removedIdentities) {
		Group group = artefact.getBaseGroup();
		
		List<String> currentRoles = groupDao.getMemberships(group, identity).stream()
				.map(GroupMembership::getRole)
				.collect(Collectors.toList());
		if (currentRoles.isEmpty() && roles.isEmpty()) {
			return;
		}
		
		String rolesBeforeXml = !currentRoles.isEmpty()
				? ProjectXStream.rolesToXml(currentRoles.stream().collect(Collectors.toList()))
				: null;
		
		boolean rolesChanged = false;
		if (roles.isEmpty()) {
			groupDao.removeMembership(group, identity);
			activityDao.create(Action.updateRoles(artefact.getType()), rolesBeforeXml, null, doer, artefact, identity);
			activityDao.create(Action.removeMember(artefact.getType()), null, null, doer, artefact, identity);
			if (removedIdentities != null) {
				removedIdentities.add(identity);
			}
			rolesChanged = true;
		} else {
			// Create membership for new roles
			for (String role : roles) {
				if (!currentRoles.contains(role)) {
					groupDao.addMembershipOneWay(group, identity, role);
					rolesChanged = true;
				}
				
			}
			
			// Delete membership of old roles.
			for (String currentRole: currentRoles) {
				if (!roles.contains(currentRole)) {
					groupDao.removeMembership(group, identity, currentRole);
					rolesChanged = true;
				}
			}
		}
		
		if (rolesChanged) {
			String rolesAfterXml = ProjectXStream.rolesToXml(roles.stream().collect(Collectors.toList()));
			activityDao.create(Action.updateRoles(artefact.getType()), rolesBeforeXml, rolesAfterXml, doer, artefact, identity);
		}
		
		if (currentRoles.isEmpty()) {
			activityDao.create(Action.addMember(artefact.getType()), null, null, doer, artefact, identity);
			if (addedIdentities != null) {
				addedIdentities.add(identity);
			}
		}
	}
	
	// Maybe move individual artefact logic to a handler
	private void onUpdateMembers(ProjectBCFactory bcFactory, ProjArtefact artefact, List<Identity> membersAdded, List<Identity> membersRemoved) {
		if (ProjAppointment.TYPE.equals(artefact.getType())) {
			ProjAppointmentSearchParams searchParams = new ProjAppointmentSearchParams();
			searchParams.setArtefacts(List.of(artefact));
			List<ProjAppointment> appointments = getAppointments(searchParams);
			if (!appointments.isEmpty()) {
				ProjAppointment appointment = appointments.get(0);
				calendarHelper.createOrUpdateEvent(bcFactory, appointment, membersAdded);
				calendarHelper.deleteEvent(appointment, membersRemoved);
			}
		}
	}
	
	public void addMember(Identity doer, ProjArtefact artefact, Identity identity) {
		Group group = artefact.getBaseGroup();
		
		List<String> currentRoles = groupDao.getMemberships(group, identity).stream()
				.map(GroupMembership::getRole)
				.collect(Collectors.toList());
		
		String rolesBeforeXml = !currentRoles.isEmpty()
				? ProjectXStream.rolesToXml(currentRoles.stream().collect(Collectors.toList()))
				: null;
		
		boolean rolesChanged = false;
		if (!currentRoles.contains(DEFAULT_ROLE_NAME)) {
			groupDao.addMembershipOneWay(group, identity, DEFAULT_ROLE_NAME);
			rolesChanged = true;
		}
		
		if (rolesChanged) {
			String rolesAfterXml = ProjectXStream.rolesToXml(List.of(DEFAULT_ROLE_NAME));
			activityDao.create(Action.updateRoles(artefact.getType()), rolesBeforeXml, rolesAfterXml, doer, artefact, identity);
		}
		if (currentRoles.isEmpty()) {
			activityDao.create(Action.addMember(artefact.getType()), null, null, doer, artefact, identity);
		}
	}

	@Override
	public Map<Long, Set<Long>> getArtefactKeyToIdentityKeys(Collection<ProjArtefact> artefacts) {
		Collection<Long> groupKeys = artefacts.stream().map(artefact -> artefact.getBaseGroup().getKey()).collect(Collectors.toList());
		Map<Long, Set<Long>> groupKeyToIdentityKeys = memberQueries.getGroupKeyToIdentityKeys(groupKeys);
		
		Map<Long, Set<Long>> artefactKeyToIdentityKeys = new HashMap<>(artefacts.size());
		for (ProjArtefact artefact : artefacts) {
			artefactKeyToIdentityKeys.put(artefact.getKey(), groupKeyToIdentityKeys.getOrDefault(artefact.getBaseGroup().getKey(), Set.of()));
		}
		
		return artefactKeyToIdentityKeys;
	}

	@Override
	public void updateTags(Identity doer, ProjArtefactRef artefact, List<String> displayNames) {
		ProjArtefact reloadedArtefact = getArtefact(artefact);
		if (reloadedArtefact == null) return;
		
		ProjTagSearchParams searchParams = new ProjTagSearchParams();
		searchParams.setArtefacts(List.of(reloadedArtefact));
		List<ProjTag> projTags = tagDao.loadTags(searchParams);
		List<Tag> currentTags = projTags.stream().map(ProjTag::getTag).toList();
		List<Tag> tags = tagService.getOrCreateTags(displayNames);
		
		boolean tagsChanged = false;
		for (Tag tag : tags) {
			if (!currentTags.contains(tag)) {
				 tagDao.create(reloadedArtefact.getProject(), reloadedArtefact, tag);
				 tagsChanged = true;
			}
		}
		
		for (ProjTag projTag : projTags) {
			if (!tags.contains(projTag.getTag())) {
				tagDao.delete(projTag);
				tagsChanged = true;
			}
		}
		
		if (tagsChanged) {
			String tagsBeforeXml = ProjectXStream.tagsToXml(currentTags.stream().map(Tag::getDisplayName).toList());
			String tagsAfterXml = ProjectXStream.tagsToXml(displayNames);
			activityDao.create(Action.updateTags(reloadedArtefact.getType()), tagsBeforeXml, tagsAfterXml, doer, reloadedArtefact);
		}
	}

	@Override
	public List<TagInfo> getTagInfos(ProjProjectRef project, ProjArtefactRef selectionArtefact){
		return tagDao.loadProjectTagInfos(project, selectionArtefact);
	}
	
	private List<ProjTag> getProjTags(List<ProjArtefact> artefacts) {
		ProjTagSearchParams searchParams = new ProjTagSearchParams();
		searchParams.setArtefacts(artefacts);
		searchParams.setArtefactStatus(List.of(ProjectStatus.active));
		return tagDao.loadTags(searchParams);
	}
	
	private Map<Long, List<Tag>> getArtefactKeyToTag(List<ProjTag> projTags) {
		return projTags.stream().collect(
				Collectors.groupingBy
						(projTag -> projTag.getArtefact().getKey(),
						Collectors.collectingAndThen(
								Collectors.toList(),
								projTag -> projTag.stream()
										.map(ProjTag::getTag)
										.collect(Collectors.toList()))));
	}
	
	private void updateContentModified(ProjArtefact artefact, Identity modifiedBy) {
		artefact.setContentModifiedDate(new Date());
		artefact.setContentModifiedBy(modifiedBy);
		artefactDao.save(artefact);
	}
	
	private void deleteArtefactSoftly(Identity doer, ProjArtefact artefact) {
		if (ProjectStatus.deleted != artefact.getStatus()) {
			artefact.setDeletedDate(new Date());
			artefact.setDeletedBy(doer);
			artefact.setStatus(ProjectStatus.deleted);
			artefactDao.save(artefact);
		}
	}
	
	private void deleteArtefactPermanent(ProjArtefact artefact) {
		tagDao.delete(artefact);
		activityDao.delete(artefact);
		artefactToArtefactDao.delete(artefact);
		groupDao.removeMemberships(artefact.getBaseGroup());
		groupDao.removeGroup(artefact.getBaseGroup());
		artefactDao.delete(artefact);
	}
	
	
	/*
	 * Files
	 */
	
	@Override
	public ProjFile createFile(Identity doer, ProjProject project, String filename, InputStream inputStream, boolean upload) {
		ProjArtefact artefact = artefactDao.create(ProjFile.TYPE, project, doer);
		VFSLeaf vfsLeaf = projectStorage.storeFile(project, doer, filename, inputStream);
		ProjFile file = fileDao.create(artefact, vfsLeaf.getMetaInfo());
		Action action = upload? Action.fileUpload: Action.fileCreate;
		String after = ProjectXStream.toXml(file);
		activityDao.create(action, null, after, null, doer, artefact);
		markNews(project);
		return file;
	}

	@Override
	public void updateFile(Identity doer, ProjFile file, String filename, String title, String description) {
		ProjFile reloadedFile = getFile(file);
		if (reloadedFile == null) {
			return;
		}
		String before = ProjectXStream.toXml(reloadedFile);
		
		boolean filenameChanged = false;
		if (StringHelper.containsNonWhitespace(filename) && !filename.equals(reloadedFile.getVfsMetadata().getFilename())) {
			VFSItem vfsItem = vfsRepositoryService.getItemFor(reloadedFile.getVfsMetadata());
			if (vfsItem instanceof VFSLeaf) {
				VFSLeaf vfsLeaf = (VFSLeaf)vfsItem;
				VFSStatus renamed = vfsLeaf.rename(filename);
				if (VFSConstants.YES == renamed) {
					filenameChanged = true;
					reloadedFile = getFile(file);
				}
			}
		}
		
		boolean metadataChanged = false;
		VFSMetadata vfsMetadata = reloadedFile.getVfsMetadata();
		if (!Objects.equals(vfsMetadata.getTitle(), title)) {
			vfsMetadata.setTitle(title);
			metadataChanged = true;
		}
		if (!Objects.equals(vfsMetadata.getComment(), description)) {
			vfsMetadata.setComment(description);
			metadataChanged = true;
		}
		if (metadataChanged) {
			vfsRepositoryService.updateMetadata(vfsMetadata);
		}
		
		if (filenameChanged || metadataChanged) {
			fileDao.save(file);
			updateContentModified(file.getArtefact(), doer);
			String after = ProjectXStream.toXml(getFile(reloadedFile));
			activityDao.create(Action.fileContentUpdate, before, after, doer, file.getArtefact());
			markNews(reloadedFile.getArtefact().getProject());
			
			addMember(doer, file.getArtefact(), doer);
		}
	}
	
	@Override
	public void deleteFileSoftly(Identity doer, ProjFileRef file) {
		ProjFile reloadedFile = getFile(file);
		if (reloadedFile == null) {
			return;
		}
		String before = ProjectXStream.toXml(reloadedFile);
		
		deleteArtefactSoftly(doer, reloadedFile.getArtefact());
		
		String after = ProjectXStream.toXml(reloadedFile);
		activityDao.create(Action.fileStatusDelete, before, after, null, doer, reloadedFile.getArtefact());
		markNews(reloadedFile.getArtefact().getProject());
	}
	
	@Override
	public boolean existsFile(ProjProjectRef project, String filename) {
		return projectStorage.exists(project, filename);
	}
	
	@Override
	public ProjFile getFile(final ProjFileRef file) {
		ProjFileSearchParams searchParams = new ProjFileSearchParams();
		searchParams.setFiles(List.of(file));
		List<ProjFile> files = fileDao.loadFiles(searchParams);
		return files != null && !files.isEmpty()? files.get(0): null;
	}
	
	@Override
	public long getFilesCount(ProjFileSearchParams searchParams) {
		return fileDao.loadFilesCount(searchParams);
	}

	@Override
	public List<ProjFile> getFiles(ProjFileSearchParams searchParams) {
		return fileDao.loadFiles(searchParams);
	}
	
	@Override
	public List<ProjFileInfo> getFileInfos(ProjFileSearchParams searchParams, ProjArtefactInfoParams infoParams) {
		List<ProjFile> files = getFiles(searchParams);
		List<ProjArtefact> artefacts = files.stream().map(ProjFile::getArtefact).toList();
		Map<ProjArtefact, ProjArtefactInfo> artefactToInfo = getArtefactToInfo(artefacts, infoParams);
		
		return files.stream()
				.map(file -> new ProjFileInfoImpl(file, artefactToInfo.get(file.getArtefact())))
				.collect(Collectors.toList());
	}
	
	
	/*
	 * ToDos
	 */
	
	@Override
	public ProjToDo createToDo(Identity doer, ProjProject project) {
		String identifier = UUID.randomUUID().toString();
		ProjArtefact artefact = artefactDao.create(ProjToDo.TYPE, project, doer);
		ToDoTask toDoTask = toDoService.createToDoTask(doer, ProjToDoProvider.TYPE, project.getKey(), identifier, project.getTitle(), null, null);
		ProjToDo toDo = toDoDao.create(artefact, toDoTask, identifier);
		String after = ProjectXStream.toXml(toDo);
		activityDao.create(Action.toDoCreate, null, after, null, doer, artefact);
		markNews(project);
		return toDo;
	}

	@Override
	public void updateToDo(Identity doer, ProjToDoRef toDo, String title, ToDoStatus status, ToDoPriority priority,
			Date startDate, Date dueDate, Long expenditureOfWork, String description) {
		ProjToDo reloadedToDo = getToDo(toDo, true);
		if (reloadedToDo == null) {
			return;
		}
		updateReloadedToDo(doer, reloadedToDo, title, status, priority, startDate, dueDate, expenditureOfWork,
				description);
	}
	
	@Override
	public void updateToDoStatus(Identity doer, String identifier, ToDoStatus status) {
		// This method should not be used to set status deleted. Use deleteToDoSoftly();
		ProjToDo reloadedToDo = getToDo(identifier, true);
		if (reloadedToDo == null) {
			return;
		}
		ToDoTask toDoTask = reloadedToDo.getToDoTask();
		updateReloadedToDo(doer, reloadedToDo, toDoTask.getTitle(), status, toDoTask.getPriority(),
				toDoTask.getStartDate(), toDoTask.getDueDate(), toDoTask.getExpenditureOfWork(),
				toDoTask.getDescription());
	}

	private void updateReloadedToDo(Identity doer, ProjToDo reloadedToDo, String title, ToDoStatus status,
			ToDoPriority priority, Date startDate, Date dueDate, Long expenditureOfWork, String description) {
		String before = ProjectXStream.toXml(reloadedToDo);
		ToDoStatus previousStatus = reloadedToDo.getToDoTask().getStatus();
		
		boolean changed = false;
		ToDoTask toDoTask = reloadedToDo.getToDoTask();
		if (!Objects.equals(toDoTask.getTitle(), title)) {
			toDoTask.setTitle(title);
			changed = true;
		}
		if (!Objects.equals(toDoTask.getStatus(), status)) {
			toDoTask.setStatus(status);
			changed = true;
		}
		if (!Objects.equals(toDoTask.getPriority(), priority)) {
			toDoTask.setPriority(priority);
			changed = true;
		}
		if (!DateUtils.isSameDay(toDoTask.getStartDate(), startDate)) {
			toDoTask.setStartDate(startDate);
			changed = true;
		}
		if (!DateUtils.isSameDay(toDoTask.getDueDate(), dueDate)) {
			toDoTask.setDueDate(dueDate);
			changed = true;
		}
		if (!Objects.equals(toDoTask.getExpenditureOfWork(), expenditureOfWork)) {
			toDoTask.setExpenditureOfWork(expenditureOfWork);
			changed = true;
		}
		if (!Objects.equals(toDoTask.getDescription(), description)) {
			toDoTask.setDescription(description);
			changed = true;
		}
		if ((toDoTask.getAssigneeRights() == null || toDoTask.getAssigneeRights().length == 0)
				&& !reloadedToDo.getArtefact().getProject().isTemplatePrivate()
				&& !reloadedToDo.getArtefact().getProject().isTemplatePublic()) {
			// Assignee has no rights if template
			toDoTask.setAssigneeRights(new ToDoRight[] {ToDoRight.edit});
			changed = true;
		}
		if (changed) {
			toDoTask.setContentModifiedDate(new Date());
			toDoService.update(doer, toDoTask, previousStatus);
			updateContentModified(reloadedToDo.getArtefact(), doer);
			String after = ProjectXStream.toXml(getToDo(reloadedToDo, false));
			activityDao.create(Action.toDoContentUpdate, before, after, doer, reloadedToDo.getArtefact());
			markNews(reloadedToDo.getArtefact().getProject());
		}
	}
	
	@Override
	public void updateMembers(Identity doer, ProjToDoRef toDo, Collection<? extends IdentityRef> assignees,
			Collection<? extends IdentityRef> delegatees) {
		ProjToDo reloadedToDo = getToDo(toDo, true);
		if (reloadedToDo == null) {
			return;
		}
		
		Map<Long, Set<String>> identityKeyToRoles = new HashMap<>();
		
		for (IdentityRef identityRef : assignees) {
			Set<String> roles = identityKeyToRoles.computeIfAbsent(identityRef.getKey(), key -> new HashSet<>(1));
			roles.add(ToDoRole.assignee.name());
		}
		for (IdentityRef identityRef : delegatees) {
			Set<String> roles = identityKeyToRoles.computeIfAbsent(identityRef.getKey(), key -> new HashSet<>(1));
			roles.add(ToDoRole.delegatee.name());
		}
		
		Group group = reloadedToDo.getArtefact().getBaseGroup();
		List<Identity> currentMembers = groupDao.getMembers(group, DEFAULT_ROLE_NAME);
		for (Identity currentMember : currentMembers) {
			identityKeyToRoles.computeIfAbsent(currentMember.getKey(), key -> Set.of());
		}
		
		Map<Long, Identity> identityKeyToIdentity = securityManager.loadIdentityByKeys(identityKeyToRoles.keySet())
				.stream().collect(Collectors.toMap(Identity::getKey, Function.identity()));
		
		List<Identity> addedIdentities = new ArrayList<>(identityKeyToRoles.size());
		List<Identity> revedIdentities = new ArrayList<>(identityKeyToRoles.size());
		identityKeyToRoles.entrySet().forEach(identityToRole -> updateMember(doer, reloadedToDo.getArtefact(),
				identityKeyToIdentity.get(identityToRole.getKey()), identityToRole.getValue(), addedIdentities, revedIdentities));
		
		if (!addedIdentities.isEmpty() || !revedIdentities.isEmpty()) {
			updateContentModified(reloadedToDo.getArtefact(), doer);
		}
		
		toDoService.updateMember(doer, reloadedToDo.getToDoTask(), assignees, delegatees);
	}
	
	@Override
	public void updateTags(Identity doer, ProjToDoRef toDo, List<String> displayNames) {
		ProjToDo reloadedToDo = getToDo(toDo, true);
		if (reloadedToDo == null) {
			return;
		}
		updateTags(doer, reloadedToDo.getArtefact(), displayNames);
		toDoService.updateTags(reloadedToDo.getToDoTask(), displayNames);
	}
	
	@Override
	public void deleteToDoSoftly(Identity doer, ProjToDoRef toDo) {
		ProjToDo reloadedToDo = getToDo(toDo, true);
		if (reloadedToDo == null) {
			return;
		}
		String before = ProjectXStream.toXml(reloadedToDo);
		ToDoStatus previousStatus = reloadedToDo.getToDoTask().getStatus();
		
		ToDoTask toDoTask = reloadedToDo.getToDoTask();
		toDoTask.setStatus(ToDoStatus.deleted);
		Date now = new Date();
		toDoTask.setContentModifiedDate(now);
		toDoTask.setDeletedDate(now);
		toDoTask.setDeletedBy(doer);
		toDoService.update(doer, toDoTask, previousStatus);
		
		deleteArtefactSoftly(doer, reloadedToDo.getArtefact());
		
		reloadedToDo = getToDo(toDo, false);
		String after = ProjectXStream.toXml(reloadedToDo);
		activityDao.create(Action.toDoStatusDelete, before, after, null, doer, reloadedToDo.getArtefact());
		markNews(reloadedToDo.getArtefact().getProject());
	}
	
	public ProjToDo getToDo(ProjToDoRef toDo, boolean active) {
		ProjToDoSearchParams searchParams = new ProjToDoSearchParams();
		searchParams.setToDos(List.of(toDo));
		if (active) {
			searchParams.setStatus(List.of(ProjectStatus.active));
		}
		List<ProjToDo> toDos = toDoDao.loadToDos(searchParams);
		return toDos != null && !toDos.isEmpty()? toDos.get(0): null;
	}
	
	@Override
	public ProjToDo getToDo(String identifier) {
		return getToDo(identifier, false);
	}

	private ProjToDo getToDo(String identifier, boolean active) {
		ProjToDoSearchParams searchParams = new ProjToDoSearchParams();
		searchParams.setIdentifiers(List.of(identifier));
		if (active) {
			searchParams.setStatus(List.of(ProjectStatus.active));
		}
		List<ProjToDo> toDos = toDoDao.loadToDos(searchParams);
		return toDos != null && !toDos.isEmpty()? toDos.get(0): null;
	}
	
	@Override
	public long getToDosCount(ProjToDoSearchParams searchParams) {
		return toDoDao.loadToDosCount(searchParams);
	}

	@Override
	public List<ProjToDo> getToDos(ProjToDoSearchParams searchParams) {
		return toDoDao.loadToDos(searchParams);
	}
	
	@Override
	public List<ProjToDoInfo> getToDoInfos(ProjToDoSearchParams searchParams, ProjArtefactInfoParams infoParams) {
		List<ProjToDo> toDos = getToDos(searchParams);
		List<ProjArtefact> artefacts = toDos.stream().map(ProjToDo::getArtefact).toList();
		Map<ProjArtefact, ProjArtefactInfo> artefactToInfo = getArtefactToInfo(artefacts, infoParams);
		
		return toDos.stream()
				.map(toDo -> new ProjToDoInfoImpl(toDo, artefactToInfo.get(toDo.getArtefact())))
				.collect(Collectors.toList());
	}
	
	
	/*
	 * Decisions
	 */
	
	@Override
	public ProjDecision createDecision(Identity doer, ProjProject project) {
		ProjArtefact artefact = artefactDao.create(ProjDecision.TYPE, project, doer);
		ProjDecision decision = decisionDao.create(artefact);
		String after = ProjectXStream.toXml(decision);
		activityDao.create(Action.decisionCreate, null, after, null, doer, artefact);
		markNews(project);
		return decision;
	}
	
	@Override
	public void updateDecision(Identity doer, ProjDecisionRef decision, String title, String details, Date decisionDate) {
		ProjDecision reloadedDecision = getDecision(decision, true);
		if (reloadedDecision == null) {
			return;
		}
		
		updateReloadedDecision(doer, reloadedDecision, title, details, decisionDate);
	}
	
	private ProjDecision updateReloadedDecision(Identity doer, ProjDecision reloadedDecision, String title,
			String details, Date decisionDate) {
		String before = ProjectXStream.toXml(reloadedDecision);
		
		boolean contentChanged = false;


		String titleCleaned = StringHelper.containsNonWhitespace(title)? title: null;
		if (!Objects.equals(reloadedDecision.getTitle(), titleCleaned)) {
			reloadedDecision.setTitle(titleCleaned);
			contentChanged = true;
		}
		String detailsCleaned = StringHelper.containsNonWhitespace(details)? details: null;
		if (!Objects.equals(reloadedDecision.getDetails(), detailsCleaned)) {
			reloadedDecision.setDetails(detailsCleaned);
			contentChanged = true;
		}
		Date cleanedDecisionDate = decisionDate != null? DateUtils.truncateSeconds(decisionDate): null;
		if (!Objects.equals(reloadedDecision.getDecisionDate(), cleanedDecisionDate)) {
			reloadedDecision.setDecisionDate(cleanedDecisionDate);
			contentChanged = true;
		}
		
		if (contentChanged) {
			reloadedDecision = decisionDao.save(reloadedDecision);
			updateContentModified(reloadedDecision.getArtefact(), doer);
			
			String after = ProjectXStream.toXml(reloadedDecision);
			activityDao.create(Action.decisionContentUpdate, before, after, null, doer, reloadedDecision.getArtefact());
			markNews(reloadedDecision.getArtefact().getProject());
		}
		
		return reloadedDecision;
	}
	
	@Override
	public void deleteDecisionSoftly(Identity doer, ProjDecisionRef decision) {
		ProjDecision reloadedDecision = getDecision(decision, true);
		if (reloadedDecision == null) {
			return;
		}
		
		deleteReloadedDecision(doer, reloadedDecision);
	}

	private void deleteReloadedDecision(Identity doer, ProjDecision reloadedDecision) {
		String before = ProjectXStream.toXml(reloadedDecision);
		
		deleteArtefactSoftly(doer, reloadedDecision.getArtefact());
		
		ProjDecision deletedDecision = getDecision(reloadedDecision);
		String after = ProjectXStream.toXml(deletedDecision);
		activityDao.create(Action.decisionStatusDelete, before, after, null, doer, deletedDecision.getArtefact());
		markNews(deletedDecision.getArtefact().getProject());
	}

	@Override
	public ProjDecision getDecision(ProjDecisionRef decision) {
		return getDecision(decision, false);
	}
	
	private ProjDecision getDecision(ProjDecisionRef decision, boolean active) {
		ProjDecisionSearchParams searchParams = new ProjDecisionSearchParams();
		searchParams.setDecisions(List.of(decision));
		if (active) {
			searchParams.setStatus(List.of(ProjectStatus.active));
		}
		List<ProjDecision> decisions = decisionDao.loadDecisions(searchParams);
		return decisions != null && !decisions.isEmpty()? decisions.get(0): null;
	}
	
	@Override
	public long getDecisionsCount(ProjDecisionSearchParams searchParams) {
		return decisionDao.loadDecisionsCount(searchParams);
	}
	
	@Override
	public List<ProjDecision> getDecisions(ProjDecisionSearchParams searchParams) {
		return decisionDao.loadDecisions(searchParams);
	}
	
	@Override
	public List<ProjDecisionInfo> getDecisionInfos(ProjDecisionSearchParams searchParams, ProjArtefactInfoParams infoParams) {
		List<ProjDecision> decisions = getDecisions(searchParams);
		List<ProjArtefact> artefacts = decisions.stream().map(ProjDecision::getArtefact).toList();
		Map<ProjArtefact, ProjArtefactInfo> artefactToInfo = getArtefactToInfo(artefacts, infoParams);
		
		return decisions.stream()
				.map(decision -> new ProjDecisionInfoImpl(decision, artefactToInfo.get(decision.getArtefact())))
				.collect(Collectors.toList());
	}
	
	
	/*
	 * Files
	 */
	
	@Override
	public ProjNote createNote(Identity doer, ProjProject project) {
		ProjArtefact artefact = artefactDao.create(ProjNote.TYPE, project, doer);
		ProjNote note = noteDao.create(artefact);
		String after = ProjectXStream.toXml(note);
		activityDao.create(Action.noteCreate, null, after, null, doer, artefact);
		markNews(project);
		return note;
	}
	
	@Override
	public void updateNote(Identity doer, ProjNoteRef note, String editSessionIdentifier, String title, String text) {
		ProjNote reloadedNote = getNote(note);
		if (reloadedNote == null) {
			return;
		}
		String before = ProjectXStream.toXml(reloadedNote);
		
		boolean contentChanged = false;
		String titleCleaned = StringHelper.containsNonWhitespace(title)? title: null;
		if (!Objects.equals(reloadedNote.getTitle(), titleCleaned)) {
			reloadedNote.setTitle(titleCleaned);
			contentChanged = true;
		}
		String textCleaned = StringHelper.containsNonWhitespace(text)? text: null;
		if (!Objects.equals(reloadedNote.getText(), textCleaned)) {
			reloadedNote.setText(textCleaned);
			contentChanged = true;
		}
		
		if (contentChanged) {
			noteDao.save(reloadedNote);
			updateContentModified(reloadedNote.getArtefact(), doer);
			
			String after = ProjectXStream.toXml(reloadedNote);
			List<ProjActivity> activities =  activityDao.loadActivities(editSessionIdentifier, Action.noteContentUpdate);
			if (!activities.isEmpty()) {
				before = activities.get(0).getBefore();
				activityDao.delete(activities);
			}
			activityDao.create(Action.noteContentUpdate, before, after, editSessionIdentifier, doer, reloadedNote.getArtefact());
			markNews(reloadedNote.getArtefact().getProject());
			
			addMember(doer, reloadedNote.getArtefact(), doer);
		}
	}

	@Override
	public void deleteNoteSoftly(Identity doer, ProjNoteRef note) {
		ProjNote reloadedNote = getNote(note);
		if (reloadedNote == null) {
			return;
		}
		String before = ProjectXStream.toXml(reloadedNote);
		
		deleteArtefactSoftly(doer, reloadedNote.getArtefact());
		
		reloadedNote = getNote(note);
		String after = ProjectXStream.toXml(reloadedNote);
		activityDao.create(Action.noteStatusDelete, before, after, null, doer, reloadedNote.getArtefact());
		markNews(reloadedNote.getArtefact().getProject());
	}
	
	@Override
	public void deleteNotePermanent(ProjNoteRef note) {
		ProjNote reloadedNote = getNote(note);
		if (reloadedNote == null) {
			return;
		}
		
		ProjArtefact artefact = reloadedNote.getArtefact();
		noteDao.delete(note);
		deleteArtefactPermanent(artefact);
	}

	@Override
	public ProjNote getNote(ProjNoteRef note) {
		ProjNoteSearchParams searchParams = new ProjNoteSearchParams();
		searchParams.setNotes(List.of(note));
		List<ProjNote> notes = noteDao.loadNotes(searchParams);
		return notes != null && !notes.isEmpty()? notes.get(0): null;
	}
	
	@Override
	public long getNotesCount(ProjNoteSearchParams searchParams) {
		return noteDao.loadNotesCount(searchParams);
	}

	@Override
	public List<ProjNote> getNotes(ProjNoteSearchParams searchParams) {
		return noteDao.loadNotes(searchParams);
	}
	
	@Override
	public List<ProjNoteInfo> getNoteInfos(ProjNoteSearchParams searchParams, ProjArtefactInfoParams infoParams) {
		List<ProjNote> notes = getNotes(searchParams);
		List<ProjArtefact> artefacts = notes.stream().map(ProjNote::getArtefact).toList();
		Map<ProjArtefact, ProjArtefactInfo> artefactToInfo = getArtefactToInfo(artefacts, infoParams);
		
		return notes.stream()
				.map(note -> new ProjNoteInfoImpl(note, artefactToInfo.get(note.getArtefact())))
				.collect(Collectors.toList());
	}
	
	
	/*
	 * Appointments
	 */
	
	@Override
	public ProjAppointment createAppointment(Identity doer, ProjectBCFactory bcFactory, ProjProject project, Date startDay) {
		// Add some time to start after the creation of the activity (below)
		Date startDate = startDay != null? DateUtils.copyTime(startDay, DateUtils.addMinutes(new Date(), 1)): null;
		Date endDate = startDate != null? DateUtils.addHours(startDate, 1): null;
		return createAppointment(doer, bcFactory, true, project, startDate, endDate);
	}
	
	private ProjAppointment createAppointment(Identity doer, ProjectBCFactory bcFactory, boolean createActivity,
			ProjProject project, Date startDate, Date endDate) {
		ProjArtefact artefact = artefactDao.create(ProjAppointment.TYPE, project, doer);
		Date truncatedStartDate = startDate != null? DateUtils.truncateSeconds(startDate): null;
		Date truncatedEndate = endDate != null? DateUtils.truncateSeconds(endDate): null;
		ProjAppointment appointment = appointmentDao.create(artefact, truncatedStartDate, truncatedEndate);
		calendarHelper.createOrUpdateEvent(bcFactory, appointment, List.of(doer));
		if (createActivity) {
			String after = ProjectXStream.toXml(appointment);
			activityDao.create(Action.appointmentCreate, null, after, null, doer, artefact);
			markNews(project);
		}
		return appointment;
	}
	
	
	@Override
	public void updateAppointment(Identity doer, ProjectBCFactory bcFactory, ProjAppointmentRef appointment, Date startDate, Date endDate,
			String subject, String description, String location, String color, boolean allDay, String recurrenceRule) {
		ProjAppointment reloadedAppointment = getAppointment(appointment, true);
		if (reloadedAppointment == null) {
			return;
		}
		
		updateOccurenceIds(doer, bcFactory, reloadedAppointment, startDate, allDay);
		
		updateReloadedAppointment(doer, bcFactory, true, reloadedAppointment, reloadedAppointment.getRecurrenceId(),
				startDate, endDate, subject, description, location, color, allDay, recurrenceRule,
				reloadedAppointment.getRecurrenceExclusion());
	}
	
	private void updateOccurenceIds(Identity doer, ProjectBCFactory bcFactory, ProjAppointment reloadedAppointment, Date startDate, boolean allDay) {
		if (StringHelper.containsNonWhitespace(reloadedAppointment.getRecurrenceRule()) && reloadedAppointment.getStartDate() != null && startDate != null) {
			int beginDiff = (int)(startDate.getTime() - reloadedAppointment.getStartDate().getTime());
			if (beginDiff != 0) {
				ProjAppointmentSearchParams searchParams = new ProjAppointmentSearchParams();
				searchParams.setEventIds(List.of(reloadedAppointment.getEventId()));
				searchParams.setRecurrenceIdAvailable(Boolean.TRUE);
				List<ProjAppointment> occurenceAppointments = appointmentDao.loadAppointments(searchParams);
				for (ProjAppointment occurenceAppointment : occurenceAppointments) {
					String updateOccurenceId = calendarHelper.getUpdatedOccurenceId(occurenceAppointment.getRecurrenceId(), allDay, beginDiff);
					if (StringHelper.containsNonWhitespace(updateOccurenceId)) {
						updateReloadedAppointment(doer, bcFactory, true, occurenceAppointment, updateOccurenceId,
								occurenceAppointment.getStartDate(), occurenceAppointment.getEndDate(),
								occurenceAppointment.getSubject(), occurenceAppointment.getDescription(),
								occurenceAppointment.getLocation(), occurenceAppointment.getColor(),
								occurenceAppointment.isAllDay(), occurenceAppointment.getRecurrenceRule(),
								occurenceAppointment.getRecurrenceExclusion());
					}
				}
			}
		}
	}
	
	@Override
	public void moveAppointment(Identity doer, ProjectBCFactory bcFactory, String identifier, Long days, Long minutes, boolean moveStartDate) {
		ProjAppointment reloadedAppointment = getAppointment(identifier, true);
		if (reloadedAppointment == null) {
			return;
		}
		
		moveReloadedAppointment(doer, bcFactory, true, reloadedAppointment, days, minutes, moveStartDate);
	}
	
	@Override
	public ProjAppointment createAppointmentOcurrence(Identity doer, ProjectBCFactory bcFactory, String identifier, String recurrenceId,
			Date startDate, Date endDate) {
		ProjAppointment reloadedAppointment = getAppointment(identifier, true);
		if (reloadedAppointment == null) {
			return null;
		}
		
		ProjAppointment clonedAppointment = createAppointment(doer, bcFactory, false, reloadedAppointment.getArtefact().getProject(), startDate, endDate);
		List<Identity> currentMembers = groupDao.getMembers(reloadedAppointment.getArtefact().getBaseGroup(), DEFAULT_ROLE_NAME);
		updateMembers(doer, bcFactory, clonedAppointment.getArtefact(), currentMembers);
		
		clonedAppointment.setEventId(reloadedAppointment.getEventId());
		clonedAppointment = updateReloadedAppointment(doer, bcFactory, false, clonedAppointment, recurrenceId, startDate, endDate,
				reloadedAppointment.getSubject(), reloadedAppointment.getDescription(),
				reloadedAppointment.getLocation(), reloadedAppointment.getColor(), reloadedAppointment.isAllDay(), null,
				null);
		
		String after = ProjectXStream.toXml(clonedAppointment);
		activityDao.create(Action.appointmentCreate, null, after, null, doer, clonedAppointment.getArtefact());
		markNews(clonedAppointment.getArtefact().getProject());
		
		return clonedAppointment;
	}
	
	@Override
	public ProjAppointment createMovedAppointmentOcurrence(Identity doer, ProjectBCFactory bcFactory, String identifier,
			String recurrenceId, Date startDate, Date endDate, Long days, Long minutes, boolean moveStartDate) {
		ProjAppointment reloadedAppointment = getAppointment(identifier, true);
		if (reloadedAppointment == null) {
			return null;
		}
		
		ProjAppointment clonedAppointment = createAppointment(doer, bcFactory, false, reloadedAppointment.getArtefact().getProject(), startDate, endDate);
		List<Identity> currentMembers = groupDao.getMembers(reloadedAppointment.getArtefact().getBaseGroup(), DEFAULT_ROLE_NAME);
		updateMembers(doer, bcFactory, clonedAppointment.getArtefact(), currentMembers);
		
		clonedAppointment.setEventId(reloadedAppointment.getEventId());
		clonedAppointment = updateReloadedAppointment(doer, bcFactory, false, clonedAppointment, recurrenceId,
				new Date(startDate.getTime()), new Date(endDate.getTime()), reloadedAppointment.getSubject(),
				reloadedAppointment.getDescription(), reloadedAppointment.getLocation(), reloadedAppointment.getColor(),
				reloadedAppointment.isAllDay(), null, null);
		
		clonedAppointment = moveReloadedAppointment(doer, bcFactory, false, clonedAppointment, days, minutes, moveStartDate);
		
		String after = ProjectXStream.toXml(clonedAppointment);
		activityDao.create(Action.appointmentCreate, null, after, null, doer, clonedAppointment.getArtefact());
		markNews(clonedAppointment.getArtefact().getProject());
		
		return clonedAppointment;
	}

	private ProjAppointment updateReloadedAppointment(Identity doer, ProjectBCFactory bcFactory, boolean createActivity,
			ProjAppointment reloadedAppointment, String recurrenceId, Date startDate, Date endDate, String subject,
			String description, String location, String color, boolean allDay, String recurrenceRule,
			String recurrenceExclusion) {
		String before = ProjectXStream.toXml(reloadedAppointment);
		
		boolean contentChanged = false;
		if (!Objects.equals(reloadedAppointment.getRecurrenceId(), recurrenceId)) {
			reloadedAppointment.setRecurrenceId(recurrenceId);
			contentChanged = true;
		}
		Date cleanedStartDate = startDate != null? DateUtils.truncateSeconds(startDate): null;
		if (!Objects.equals(reloadedAppointment.getStartDate(), cleanedStartDate)) {
			reloadedAppointment.setStartDate(cleanedStartDate);
			contentChanged = true;
		}
		Date cleanedEndDate = endDate != null? DateUtils.truncateSeconds(endDate): null;
		if (!Objects.equals(reloadedAppointment.getEndDate(), cleanedEndDate)) {
			reloadedAppointment.setEndDate(cleanedEndDate);
			contentChanged = true;
		}
		String subjectCleaned = StringHelper.containsNonWhitespace(subject)? subject: null;
		if (!Objects.equals(reloadedAppointment.getSubject(), subjectCleaned)) {
			reloadedAppointment.setSubject(subjectCleaned);
			contentChanged = true;
		}
		String descriptionCleaned = StringHelper.containsNonWhitespace(description)? description: null;
		if (!Objects.equals(reloadedAppointment.getDescription(), descriptionCleaned)) {
			reloadedAppointment.setDescription(descriptionCleaned);
			contentChanged = true;
		}
		String locationCleaned = StringHelper.containsNonWhitespace(location)? location: null;
		if (!Objects.equals(reloadedAppointment.getLocation(), locationCleaned)) {
			reloadedAppointment.setLocation(locationCleaned);
			contentChanged = true;
		}
		String colorCleaned = StringHelper.containsNonWhitespace(color)? color: null;
		if (!Objects.equals(reloadedAppointment.getColor(), colorCleaned)) {
			reloadedAppointment.setColor(colorCleaned);
			contentChanged = true;
		}
		if (reloadedAppointment.isAllDay() != allDay) {
			reloadedAppointment.setAllDay(allDay);
			contentChanged = true;
		}
		String recurrenceRuleCleaned = StringHelper.containsNonWhitespace(recurrenceRule)? recurrenceRule: null;
		if (!Objects.equals(reloadedAppointment.getRecurrenceRule(), recurrenceRuleCleaned)) {
			reloadedAppointment.setRecurrenceRule(recurrenceRuleCleaned);
			contentChanged = true;
		}
		if (!Objects.equals(reloadedAppointment.getRecurrenceExclusion(), recurrenceExclusion)) {
			reloadedAppointment.setRecurrenceExclusion(recurrenceExclusion);
			contentChanged = true;
		}
		
		if (contentChanged) {
			reloadedAppointment = appointmentDao.save(reloadedAppointment);
			updateContentModified(reloadedAppointment.getArtefact(), doer);
			
			List<Identity> members = groupDao.getMembers(reloadedAppointment.getArtefact().getBaseGroup(), DEFAULT_ROLE_NAME);
			if (startDate != null && endDate != null) {
				calendarHelper.createOrUpdateEvent(bcFactory, reloadedAppointment, members);
			} else {
				calendarHelper.deleteEvent(reloadedAppointment, members);
			}
			
			if (createActivity) {
				String after = ProjectXStream.toXml(reloadedAppointment);
				activityDao.create(Action.appointmentContentUpdate, before, after, null, doer, reloadedAppointment.getArtefact());
			}
		}
		
		return reloadedAppointment;
	}

	private ProjAppointment moveReloadedAppointment(Identity doer, ProjectBCFactory bcFactory, boolean createActivity,
			ProjAppointment reloadedAppointment, Long days, Long minutes, boolean moveStartDate) {
		Date startDate = reloadedAppointment.getStartDate();
		if (moveStartDate) {
			startDate = move(reloadedAppointment.getStartDate(), days, minutes);
		}
		Date endDate = move(reloadedAppointment.getEndDate(), days, minutes);
		
		return updateReloadedAppointment(doer, bcFactory, createActivity, reloadedAppointment,
				reloadedAppointment.getRecurrenceId(), startDate, endDate, reloadedAppointment.getSubject(),
				reloadedAppointment.getDescription(), reloadedAppointment.getLocation(), reloadedAppointment.getColor(),
				reloadedAppointment.isAllDay(), reloadedAppointment.getRecurrenceRule(),
				reloadedAppointment.getRecurrenceExclusion());
	}
	
	private Date move(Date date, Long dayDelta, Long minuteDelta) {
		if (date == null) {
			return date;
		}
		
		Calendar cal = Calendar.getInstance();
		cal.setTime(date);
		if (dayDelta != null) {
			cal.add(Calendar.DATE, dayDelta.intValue());
		}
		if (minuteDelta != null) {
			cal.add(Calendar.MINUTE, minuteDelta.intValue());
		}
		return cal.getTime();
	}
	
	@Override
	public void addAppointmentExclusion(Identity doer, ProjectBCFactory bcFactory, String identifier, Date exclusionDate, boolean single) {
		ProjAppointment reloadedAppointment = getAppointment(identifier, true);
		if (reloadedAppointment == null) {
			return;
		}
		if (!StringHelper.containsNonWhitespace(reloadedAppointment.getRecurrenceRule())) {
			return;
		}
		
		if (single) {
			addAppointmentSingleExclusion(doer, bcFactory, true, reloadedAppointment, exclusionDate);
			String before = ProjectXStream.toXml(exclusionDate);
			activityDao.create(Action.appointmentOccurrenceDelete, before, null, null, doer, reloadedAppointment.getArtefact());
		} else {
			addAppointmentFutureExclusion(doer, bcFactory, reloadedAppointment, exclusionDate);
		}
	}
	
	private void addAppointmentSingleExclusion(Identity doer, ProjectBCFactory bcFactory, boolean createActivity,
			ProjAppointment reloadedAppointment, Date exclusionDate) {
		List<Date> exclisionDates = CalendarUtils.getRecurrenceExcludeDates(reloadedAppointment.getRecurrenceExclusion());
		exclisionDates.add(exclusionDate);
		String recurrenceExclusion = CalendarUtils.getRecurrenceExcludeRule(exclisionDates);
		updateReloadedAppointment(doer, bcFactory, createActivity, reloadedAppointment, reloadedAppointment.getRecurrenceId(),
				reloadedAppointment.getStartDate(), reloadedAppointment.getEndDate(),
				reloadedAppointment.getSubject(), reloadedAppointment.getDescription(),
				reloadedAppointment.getLocation(), reloadedAppointment.getColor(), reloadedAppointment.isAllDay(),
				reloadedAppointment.getRecurrenceRule(), recurrenceExclusion);
	}
	
	private void addAppointmentFutureExclusion(Identity doer, ProjectBCFactory bcFactory, ProjAppointment reloadedAppointment, Date exclusionDate) {
		String recurrenceRule = calendarHelper.getExclusionRecurrenceRule(reloadedAppointment.getRecurrenceRule(), exclusionDate);
		updateReloadedAppointment(doer, bcFactory, true, reloadedAppointment, reloadedAppointment.getRecurrenceId(),
				reloadedAppointment.getStartDate(), reloadedAppointment.getEndDate(),
				reloadedAppointment.getSubject(), reloadedAppointment.getDescription(),
				reloadedAppointment.getLocation(), reloadedAppointment.getColor(), reloadedAppointment.isAllDay(),
				recurrenceRule, reloadedAppointment.getRecurrenceExclusion());
	}
	
	@Override
	public void deleteAppointmentSoftly(Identity doer, ProjectBCFactory bcFactory, String identifier, Date occurenceDate) {
		ProjAppointment reloadedAppointment = getAppointment(identifier, true);
		if (reloadedAppointment == null) {
			return;
		}
		
		if (reloadedAppointment.getRecurrenceId() != null) {
			ProjAppointmentSearchParams searchParams = new ProjAppointmentSearchParams();
			searchParams.setProject(reloadedAppointment.getArtefact().getProject());
			searchParams.setEventIds(List.of(reloadedAppointment.getEventId()));
			searchParams.setRecurrenceIdAvailable(Boolean.FALSE);
			List<ProjAppointment> appointments = appointmentDao.loadAppointments(searchParams);
			if (appointments != null && !appointments.isEmpty()) {
				ProjAppointment rootAppointment = appointments.get(0);
				addAppointmentSingleExclusion(doer, bcFactory, false, rootAppointment, occurenceDate);
			}
			deleteReloadedAppointmentSoftly(doer, reloadedAppointment);
		} else {
			ProjAppointmentSearchParams searchParams = new ProjAppointmentSearchParams();
			searchParams.setProject(reloadedAppointment.getArtefact().getProject());
			searchParams.setEventIds(List.of(reloadedAppointment.getEventId()));
			List<ProjAppointment> appointments = appointmentDao.loadAppointments(searchParams);
			appointments.forEach(appointment -> deleteReloadedAppointmentSoftly(doer, appointment));
		}
	}

	@Override
	public void deleteAppointmentSoftly(Identity doer, ProjAppointmentRef appointment) {
		ProjAppointment reloadedAppointment = getAppointment(appointment, true);
		if (reloadedAppointment == null) {
			return;
		}
		deleteReloadedAppointmentSoftly(doer, reloadedAppointment);
	}

	private void deleteReloadedAppointmentSoftly(Identity doer, ProjAppointment reloadedAppointment) {
		String before = ProjectXStream.toXml(reloadedAppointment);
		
		deleteArtefactSoftly(doer, reloadedAppointment.getArtefact());
		
		ProjAppointment deletedAppointment = getAppointment(reloadedAppointment);
		String after = ProjectXStream.toXml(deletedAppointment);
		activityDao.create(Action.appointmentStatusDelete, before, after, null, doer, deletedAppointment.getArtefact());
		markNews(deletedAppointment.getArtefact().getProject());
		
		List<Identity> members = groupDao.getMembers(deletedAppointment.getArtefact().getBaseGroup(), DEFAULT_ROLE_NAME);
		calendarHelper.deleteEvent(deletedAppointment, members);
	}

	@Override
	public ProjAppointment getAppointment(ProjAppointmentRef appointment) {
		return getAppointment(appointment, false);
	}
	
	public ProjAppointment getAppointment(ProjAppointmentRef appointment, boolean active) {
		ProjAppointmentSearchParams searchParams = new ProjAppointmentSearchParams();
		searchParams.setAppointments(List.of(appointment));
		if (active) {
			searchParams.setStatus(List.of(ProjectStatus.active));
		}
		List<ProjAppointment> appointments = appointmentDao.loadAppointments(searchParams);
		return appointments != null && !appointments.isEmpty()? appointments.get(0): null;
	}

	private ProjAppointment getAppointment(String identifier, boolean active) {
		ProjAppointmentSearchParams searchParams = new ProjAppointmentSearchParams();
		searchParams.setIdentifiers(List.of(identifier));
		if (active) {
			searchParams.setStatus(List.of(ProjectStatus.active));
		}
		List<ProjAppointment> appointments = appointmentDao.loadAppointments(searchParams);
		return appointments != null && !appointments.isEmpty()? appointments.get(0): null;
	}
	
	@Override
	public List<ProjAppointment> getAppointments(ProjAppointmentSearchParams searchParams) {
		return appointmentDao.loadAppointments(searchParams);
	}

	@Override
	public List<ProjAppointmentInfo> getAppointmentInfos(ProjAppointmentSearchParams searchParams, ProjArtefactInfoParams infoParams) {
		List<ProjAppointment> appointments = getAppointments(searchParams);
		List<ProjArtefact> artefacts = appointments.stream().map(ProjAppointment::getArtefact).toList();
		Map<ProjArtefact, ProjArtefactInfo> artefactToInfo = getArtefactToInfo(artefacts, infoParams);
		
		return appointments.stream()
				.map(appointment -> new ProjAppointmentInfoImpl(appointment, artefactToInfo.get(appointment.getArtefact())))
				.collect(Collectors.toList());
	}
	
	@Override
	public Kalendar getAppointmentsKalendar(List<ProjAppointment> appointments) {
		// Should the key contain the project key?
		Kalendar calendar = new Kalendar(UUID.randomUUID().toString(), "Appointments");
		appointments.stream()
				.map(calendarHelper::toEvent)
				.forEach(event -> calendar.addEvent(event));
		
		return calendar;
	}
	
	
	/*
	 * Milestones
	 */
	
	@Override
	public ProjMilestone createMilestone(Identity doer, ProjectBCFactory bcFactory, ProjProject project) {
		return createMilestone(doer, bcFactory, true, project, null);
	}
	
	private ProjMilestone createMilestone(Identity doer, ProjectBCFactory bcFactory, boolean createActivity,
			ProjProject project, Date dueDate) {
		ProjArtefact artefact = artefactDao.create(ProjMilestone.TYPE, project, doer);
		Date truncateedDueDate = dueDate != null? DateUtils.truncateSeconds(dueDate): null;
		ProjMilestone milestone = milestoneDao.create(artefact, truncateedDueDate);
		Set<Identity> members = getMembers(project);
		calendarHelper.createOrUpdateEvent(bcFactory, milestone, members);
		if (createActivity) {
			String after = ProjectXStream.toXml(milestone);
			activityDao.create(Action.milestoneCreate, null, after, null, doer, artefact);
			markNews(project);
		}
		return milestone;
	}
	
	@Override
	public void updateMilestone(Identity doer, ProjectBCFactory bcFactory, ProjMilestoneRef milestone,
			ProjMilestoneStatus status, Date dueDate, String subject, String description, String color) {
		ProjMilestone reloadedMilestone = getMilestone(milestone, true);
		if (reloadedMilestone == null) {
			return;
		}
		
		updateReloadedMilestone(doer, bcFactory, reloadedMilestone, status, dueDate, subject, description, color);
	}
	
	@Override
	public void updateMilestoneStatus(Identity doer, ProjectBCFactory bcFactory, ProjMilestoneRef milestone, ProjMilestoneStatus status) {
		ProjMilestone reloadedMilestone = getMilestone(milestone, true);
		if (reloadedMilestone == null) {
			return;
		}
		
		updateReloadedMilestone(doer, bcFactory, reloadedMilestone, status, reloadedMilestone.getDueDate(),
				reloadedMilestone.getSubject(), reloadedMilestone.getDescription(), reloadedMilestone.getColor());
	}
	
	@Override
	public void moveMilestone(Identity doer, ProjectBCFactory bcFactory, String identifier, Long days) {
		ProjMilestone reloadedMilestone = getMilestone(identifier, true);
		if (reloadedMilestone == null) {
			return;
		}
		
		Date dueDate = move(reloadedMilestone.getDueDate(), days, Long.valueOf(0));
		
		updateReloadedMilestone(doer, bcFactory, reloadedMilestone, reloadedMilestone.getStatus(), dueDate,
				reloadedMilestone.getSubject(), reloadedMilestone.getDescription(), reloadedMilestone.getColor());
	}
	
	private ProjMilestone updateReloadedMilestone(Identity doer, ProjectBCFactory bcFactory, ProjMilestone reloadedMilestone,
			ProjMilestoneStatus status, Date dueDate, String subject, String description, String color) {
		String before = ProjectXStream.toXml(reloadedMilestone);
		
		boolean contentChanged = false;
		if (reloadedMilestone.getStatus() != status) {
			reloadedMilestone.setStatus(status);
			contentChanged = true;
		}
		Date cleanedDueDate = dueDate != null? DateUtils.truncateSeconds(dueDate): null;
		if (!Objects.equals(reloadedMilestone.getDueDate(), cleanedDueDate)) {
			reloadedMilestone.setDueDate(cleanedDueDate);
			contentChanged = true;
		}
		String subjectCleaned = StringHelper.containsNonWhitespace(subject)? subject: null;
		if (!Objects.equals(reloadedMilestone.getSubject(), subjectCleaned)) {
			reloadedMilestone.setSubject(subjectCleaned);
			contentChanged = true;
		}
		String descriptionCleaned = StringHelper.containsNonWhitespace(description)? description: null;
		if (!Objects.equals(reloadedMilestone.getDescription(), descriptionCleaned)) {
			reloadedMilestone.setDescription(descriptionCleaned);
			contentChanged = true;
		}
		String colorCleaned = StringHelper.containsNonWhitespace(color)? color: null;
		if (!Objects.equals(reloadedMilestone.getColor(), colorCleaned)) {
			reloadedMilestone.setColor(colorCleaned);
			contentChanged = true;
		}
		
		if (contentChanged) {
			reloadedMilestone = milestoneDao.save(reloadedMilestone);
			updateContentModified(reloadedMilestone.getArtefact(), doer);
			
			Set<Identity> members = getMembers(reloadedMilestone.getArtefact().getProject());
			if (dueDate != null) {
				calendarHelper.createOrUpdateEvent(bcFactory, reloadedMilestone, members);
			} else {
				calendarHelper.deleteEvent(reloadedMilestone, members);
			}
			
			String after = ProjectXStream.toXml(reloadedMilestone);
			activityDao.create(Action.milestoneContentUpdate, before, after, null, doer, reloadedMilestone.getArtefact());
			markNews(reloadedMilestone.getArtefact().getProject());
		}
		
		return reloadedMilestone;
	}
	
	@Override
	public void deleteMilestoneSoftly(Identity doer, ProjMilestoneRef milestone) {
		ProjMilestone reloadedMilestone = getMilestone(milestone, true);
		if (reloadedMilestone == null) {
			return;
		}
		
		deleteReloadedMilestone(doer, reloadedMilestone);
	}

	private void deleteReloadedMilestone(Identity doer, ProjMilestone reloadedMilestone) {
		String before = ProjectXStream.toXml(reloadedMilestone);
		
		deleteArtefactSoftly(doer, reloadedMilestone.getArtefact());
		
		ProjMilestone deletedMilestone = getMilestone(reloadedMilestone);
		String after = ProjectXStream.toXml(deletedMilestone);
		activityDao.create(Action.milestoneStatusDelete, before, after, null, doer, deletedMilestone.getArtefact());
		markNews(reloadedMilestone.getArtefact().getProject());
		
		Set<Identity> members = getMembers(reloadedMilestone.getArtefact().getProject());
		calendarHelper.deleteEvent(deletedMilestone, members);
	}

	@Override
	public ProjMilestone getMilestone(ProjMilestoneRef milestone) {
		return getMilestone(milestone, false);
	}
	
	private ProjMilestone getMilestone(ProjMilestoneRef milestone, boolean active) {
		ProjMilestoneSearchParams searchParams = new ProjMilestoneSearchParams();
		searchParams.setMilestones(List.of(milestone));
		if (active) {
			searchParams.setStatus(List.of(ProjectStatus.active));
		}
		List<ProjMilestone> milestones = milestoneDao.loadMilestones(searchParams);
		return milestones != null && !milestones.isEmpty()? milestones.get(0): null;
	}
	
	private ProjMilestone getMilestone(String identifier, boolean active) {
		ProjMilestoneSearchParams searchParams = new ProjMilestoneSearchParams();
		searchParams.setIdentifiers(List.of(identifier));
		if (active) {
			searchParams.setStatus(List.of(ProjectStatus.active));
		}
		List<ProjMilestone> milestones = milestoneDao.loadMilestones(searchParams);
		return milestones != null && !milestones.isEmpty()? milestones.get(0): null;
	}
	
	@Override
	public List<ProjMilestone> getMilestones(ProjMilestoneSearchParams searchParams) {
		return milestoneDao.loadMilestones(searchParams);
	}
	
	@Override
	public List<ProjMilestoneInfo> getMilestoneInfos(ProjMilestoneSearchParams searchParams, ProjArtefactInfoParams infoParams) {
		List<ProjMilestone> milestones = getMilestones(searchParams);
		List<ProjArtefact> artefacts = milestones.stream().map(ProjMilestone::getArtefact).toList();
		Map<ProjArtefact, ProjArtefactInfo> artefactToInfo = getArtefactToInfo(artefacts, infoParams);
		
		return milestones.stream()
				.map(milestone -> new ProjMilestoneInfoImpl(milestone, artefactToInfo.get(milestone.getArtefact())))
				.collect(Collectors.toList());
	}
	
	@Override
	public Kalendar getMilestonesKalendar(List<ProjMilestone> milestones) {
		Kalendar calendar = new Kalendar(UUID.randomUUID().toString(), "Milestone");
		milestones.stream()
				.map(calendarHelper::toEvent)
				.forEach(event -> calendar.addEvent(event));
		
		return calendar;
	}
	
	
	/*
	 * Activities
	 */
	
	@Override
	public void createActivityRead(Identity doer, ProjProject project) {
		activityDao.create(Action.projectRead, null, null, doer, project);
	}
	
	@Override
	public void createActivityRead(Identity doer, ProjArtefact artefact) {
		activityDao.create(Action.read(artefact.getType()), null, null, null, doer, artefact);
	}
	
	@Override
	public void createActivityDownload(Identity doer, ProjArtefact artefact) {
		activityDao.create(Action.download(artefact.getType()), null, null, null, doer, artefact);
	}

	private void createActivityDocumentEdit(Long identityKey, Long vfsMetadatKey, Long accessKey) {
		boolean created = createActivityFileEdit(identityKey, vfsMetadatKey, accessKey);
		if (created) {
			return;
		}
		
		createActivityWhiteboardEdit(identityKey, vfsMetadatKey, accessKey);
	}

	private boolean createActivityFileEdit(Long identityKey, Long vfsMetadatKey, Long accessKey) {
		ProjFileSearchParams searchParams = new ProjFileSearchParams();
		searchParams.setMetadataKeys(List.of(vfsMetadatKey));
		searchParams.setStatus(List.of(ProjectStatus.active));
		List<ProjFile> files = fileDao.loadFiles(searchParams);
		ProjFile reloadedFile = files != null && !files.isEmpty()? files.get(0): null;
		if (reloadedFile == null) {
			return false;
		}
		
		String before = ProjectXStream.toXml(reloadedFile);
		
		Identity doer = securityManager.loadIdentityByKey(identityKey);
		updateContentModified(reloadedFile.getArtefact(), doer);
		
		reloadedFile = getFile(reloadedFile);
		String after = ProjectXStream.toXml(reloadedFile);
		String editSessionIdentifier = accessKey.toString();
		List<ProjActivity> activities =  activityDao.loadActivities(editSessionIdentifier, Action.fileEdit);
		if (!activities.isEmpty()) {
			before = activities.get(0).getBefore();
			activityDao.delete(activities);
		}
		activityDao.create(Action.fileEdit, before, after, editSessionIdentifier, doer, reloadedFile.getArtefact());
		markNews(reloadedFile.getArtefact().getProject());
		
		addMember(doer, reloadedFile.getArtefact(), doer);
		
		return true;
	}

	private void createActivityWhiteboardEdit(Long identityKey, Long vfsMetadatKey, Long accessKey) {
		VFSMetadata vfsMetadata = vfsRepositoryService.getMetadata(() -> vfsMetadatKey);
		if (vfsMetadata != null 
				&& ProjWhiteboardFileType.board.getFilename().equalsIgnoreCase(vfsMetadata.getFilename())
				&& vfsMetadata.getRelativePath().endsWith(ProjectStorage.WHITEBOARD_DIR_NAME)
				&& vfsMetadata.getRelativePath().startsWith("projects/project/")) {
			String projectKeyStr = vfsMetadata.getRelativePath().substring(17, vfsMetadata.getRelativePath().length() - ProjectStorage.WHITEBOARD_DIR_NAME.length() - 1);
			if (StringHelper.isLong(projectKeyStr)) {
				Long projectKey = Long.valueOf(projectKeyStr);
				VFSLeaf whiteboard = getWhiteboard(() -> projectKey, ProjWhiteboardFileType.board);
				if (whiteboard != null) {
					VFSMetadata whiteboardMetadata = whiteboard.getMetaInfo();
					if (whiteboardMetadata != null && whiteboardMetadata.getKey().equals(vfsMetadatKey)) {
						String editSessionIdentifier = accessKey.toString();
						List<ProjActivity> activities =  activityDao.loadActivities(editSessionIdentifier, Action.whiteboardEdit);
						if (!activities.isEmpty()) {
							activityDao.delete(activities);
						}
						ProjProject project = getProject(() -> projectKey);
						if (project != null ) {
							Identity doer = securityManager.loadIdentityByKey(identityKey);
							activityDao.create(Action.whiteboardEdit, null, null, editSessionIdentifier, doer, project);
						}
					}
				}
			}
		}
	}

	@Override
	public List<ProjActivity> getActivities(ProjActivitySearchParams searchParams, int firstResult, int maxResults) {
		return activityDao.loadActivities(searchParams, firstResult, maxResults);
	}
	
	@Override
	public Map<Long, ProjActivity> getProjectKeyToLastActivity(ProjActivitySearchParams searchParams) {
		return activityDao.loadProjectKeyToLastActivity(searchParams);
	}

	@Override
	public void event(Event event) {
		if (event instanceof DocumentSavedEvent dccEvent && dccEvent.isEventOnThisNode()) {
			createActivityDocumentEdit(dccEvent.getIdentityKey(), dccEvent.getVfsMetadatKey(), dccEvent.getAccessKey());
		}
	}

}
