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

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.olat.basesecurity.BaseSecurity;
import org.olat.basesecurity.Group;
import org.olat.basesecurity.GroupMembership;
import org.olat.basesecurity.IdentityRef;
import org.olat.basesecurity.manager.GroupDAO;
import org.olat.commons.calendar.CalendarUtils;
import org.olat.commons.calendar.model.Kalendar;
import org.olat.core.commons.services.tag.Tag;
import org.olat.core.commons.services.tag.TagService;
import org.olat.core.commons.services.vfs.VFSMetadata;
import org.olat.core.commons.services.vfs.VFSRepositoryService;
import org.olat.core.id.Identity;
import org.olat.core.id.Organisation;
import org.olat.core.util.DateUtils;
import org.olat.core.util.StringHelper;
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
import org.olat.modules.project.ProjProjectRef;
import org.olat.modules.project.ProjProjectSearchParams;
import org.olat.modules.project.ProjProjectToOrganisation;
import org.olat.modules.project.ProjProjectUserInfo;
import org.olat.modules.project.ProjTag;
import org.olat.modules.project.ProjTagInfo;
import org.olat.modules.project.ProjTagSearchParams;
import org.olat.modules.project.ProjectRole;
import org.olat.modules.project.ProjectService;
import org.olat.modules.project.ProjectStatus;
import org.olat.modules.project.model.ProjAppointmentInfoImpl;
import org.olat.modules.project.model.ProjArtefactInfoImpl;
import org.olat.modules.project.model.ProjArtefactItemsImpl;
import org.olat.modules.project.model.ProjFileInfoImpl;
import org.olat.modules.project.model.ProjMemberInfoImpl;
import org.olat.modules.project.model.ProjMilestoneInfoImpl;
import org.olat.modules.project.model.ProjNoteInfoImpl;
import org.olat.modules.project.model.ProjProjectImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 
 * Initial date: 22 Nov 2022<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
@Service
public class ProjectServiceImpl implements ProjectService {

	static final String DEFAULT_ROLE_NAME = ProjectRole.participant.name();
	
	@Autowired
	private ProjProjectDAO projectDao;
	@Autowired
	private ProjProjectToOrganisationDAO projectToOrganisationDao;
	@Autowired
	private ProjProjectUserInfoDAO projectUserInfoDao;
	@Autowired
	private ProjMemberQueries memberQueries;
	@Autowired
	private ProjArtefactDAO artefactDao;
	@Autowired
	private ProjArtefactToArtefactDAO artefactToArtefactDao;
	@Autowired
	private ProjFileDAO fileDao;
	@Autowired
	private ProjFileStorage fileStorage;
	@Autowired
	private VFSRepositoryService vfsRepositoryService;
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
	protected BaseSecurity securityManager;
	@Autowired
	private GroupDAO groupDao;
	@Autowired
	private ProjTagDAO tagDao;
	@Autowired
	private TagService tagService;
	
	@Override
	public ProjProject createProject(Identity doer) {
		Group baseGroup = groupDao.createGroup();
		groupDao.addMembershipOneWay(baseGroup, doer, ProjectRole.owner.name());
		ProjProject project = projectDao.create(doer, baseGroup);
		String after = ProjectXStream.toXml(project);
		activityDao.create(Action.projectCreate, null, after, doer, project);
		return project;
	}
	
	@Override
	public ProjProject updateProject(Identity doer, ProjProject project) {
		ProjProject reloadedProject = getProject(project);
		if (reloadedProject == null) {
			return project;
		}
		String before = ProjectXStream.toXml(reloadedProject);
		
		boolean contentChanged = false;
		boolean titleChanged = false;
		if (!Objects.equals(reloadedProject.getExternalRef(), project.getExternalRef())) {
			contentChanged = true;
		}
		if (!Objects.equals(reloadedProject.getTitle(), project.getTitle())) {
			contentChanged = true;
			titleChanged = true;
		}
		if (!Objects.equals(reloadedProject.getTeaser(), project.getTeaser())) {
			contentChanged = true;
		}
		if (!Objects.equals(reloadedProject.getDescription(), project.getDescription())) {
			contentChanged = true;
		}
		
		ProjProject updatedProject = project;
		if (contentChanged) {
			updatedProject = projectDao.save(project);
			
			String after = ProjectXStream.toXml(updatedProject);
			activityDao.create(Action.projectContentUpdate, before, after, doer, updatedProject);
			
			if (titleChanged) {
				ProjAppointmentSearchParams searchParams = new ProjAppointmentSearchParams();
				searchParams.setProject(reloadedProject);
				searchParams.setStatus(List.of(ProjectStatus.active));
				getAppointmentInfos(searchParams, ProjArtefactInfoParams.MEMBERS)
						.forEach(info -> calendarHelper.createOrUpdateEvent(info.getAppointment(), info.getMembers()));
			}
		}
		
		return updatedProject;
	}
	
	@Override
	public ProjProject setStatusDone(Identity doer, ProjProjectRef project) {
		ProjProject reloadedProject = getProject(project);
		if (ProjectStatus.active == reloadedProject.getStatus()) {
			String before = ProjectXStream.toXml(reloadedProject);
			
			((ProjProjectImpl)reloadedProject).setStatus(ProjectStatus.done);
			reloadedProject = projectDao.save(reloadedProject);
			
			String after = ProjectXStream.toXml(reloadedProject);
			activityDao.create(Action.projectStatusDone, before, after, doer, reloadedProject);
		}
		return reloadedProject;
	}
	
	@Override
	public ProjProject reopen(Identity doer, ProjProjectRef project) {
		ProjProject reloadedProject = getProject(project);
		if (ProjectStatus.done == reloadedProject.getStatus()) {
			String before = ProjectXStream.toXml(reloadedProject);
			
			((ProjProjectImpl)reloadedProject).setStatus(ProjectStatus.active);
			reloadedProject = projectDao.save(reloadedProject);
			
			String after = ProjectXStream.toXml(reloadedProject);
			activityDao.create(Action.projectStatusActive, before, after, doer, reloadedProject);
		}
		return reloadedProject;
	}
	
	@Override
	public ProjProject setStatusDeleted(Identity doer, ProjProjectRef project) {
		ProjProject reloadedProject = getProject(project);
		if (ProjectStatus.deleted != reloadedProject.getStatus()) {
			// Delete all members but owners
			ProjMemberInfoSearchParameters params = new ProjMemberInfoSearchParameters();
			params.setProject(reloadedProject);
			Map<Identity,Set<ProjectRole>> memberToRoles = getMemberToRoles(params);
			for (Map.Entry<Identity,Set<ProjectRole>> entry: memberToRoles.entrySet()) {
				if (entry.getValue().contains(ProjectRole.owner)) {
					updateMember(doer, reloadedProject, entry.getKey(), Set.of(ProjectRole.owner));
				} else {
					updateMember(doer, reloadedProject, entry.getKey(), Set.of());
				}
			}
			
			// Delete all appointment events from user calendars
			ProjAppointmentSearchParams searchParams = new ProjAppointmentSearchParams();
			searchParams.setProject(reloadedProject);
			searchParams.setStatus(List.of(ProjectStatus.active));
			getAppointmentInfos(searchParams, ProjArtefactInfoParams.MEMBERS)
					.forEach(info -> calendarHelper.deleteEvent(info.getAppointment(), info.getMembers()));
			
			String before = ProjectXStream.toXml(reloadedProject);
			
			((ProjProjectImpl)reloadedProject).setStatus(ProjectStatus.deleted);
			reloadedProject = projectDao.save(reloadedProject);
			
			String after = ProjectXStream.toXml(reloadedProject);
			activityDao.create(Action.projectStatusDelete, before, after, doer, reloadedProject);
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
		if (organisations == null || organisations.isEmpty()) {
			projectToOrganisationDao.delete(project);
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
	public void updateMember(Identity doer, ProjProject project, Identity identity, Set<ProjectRole> roles) {
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
		
		if (currentRoles.isEmpty()) {
			activityDao.create(Action.projectMemberAdd, null, null, doer, project, identity);
		}
	}
	
	@Override
	public void updateMembers(Identity doer, ProjProject project, Map<Identity, Set<ProjectRole>> identityToRoles) {
		identityToRoles.entrySet().forEach(identityToRole -> updateMember(doer, project, identityToRole.getKey(), identityToRole.getValue()));
	}
	
	@Override
	public void removeMembers(Identity doer, ProjProject project, Collection<Identity> identities) {
		identities.forEach(identity -> updateMember(doer, project, identity, Set.of()));
	}

	@Override
	public boolean isProjectMember(IdentityRef identity) {
		return memberQueries.isProjectMember(identity);
	}
	
	@Override
	public List<Identity> getMembers(ProjProject project, Collection<ProjectRole> roles) {
		return memberQueries.getProjMemberships(List.of(project), roles).stream()
				.map(GroupMembership::getIdentity)
				.distinct()
				.collect(Collectors.toList());
	}
	
	@Override
	public Map<Long, List<Identity>> getProjectGroupKeyToMembers(Collection<ProjProject> projects, Collection<ProjectRole> roles) {
		return memberQueries.getProjMemberships(projects, roles).stream()
				.collect(Collectors.groupingBy(
						membership -> membership.getGroup().getKey(),
						Collectors.collectingAndThen(
								Collectors.toList(),
								memberships -> memberships.stream()
										.map(GroupMembership::getIdentity)
										.distinct()
										.collect(Collectors.toList()))));
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
	public List<ProjArtefact> getLinkedArtefacts(ProjArtefact artefact) {
		ProjArtefactToArtefactSearchParams ataSearchParams = new ProjArtefactToArtefactSearchParams();
		ataSearchParams.setArtefact(artefact);
		return artefactToArtefactDao.loadArtefactToArtefacts(ataSearchParams).stream()
				.map(ata -> artefact.getKey().equals(ata.getArtefact1().getKey())? ata.getArtefact2(): ata.getArtefact1())
				.collect(Collectors.toList());
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
		ProjArtefactToArtefactSearchParams ataSearchParams = new ProjArtefactToArtefactSearchParams();
		ataSearchParams.setArtefact(artefact);
		Map<String, List<ProjArtefact>> typeToArtefacts = artefactToArtefactDao.loadArtefactToArtefacts(ataSearchParams).stream()
				.map(ata -> artefact.getKey().equals(ata.getArtefact1().getKey())? ata.getArtefact2(): ata.getArtefact1())
				.collect(Collectors.groupingBy(ProjArtefact::getType));
		
		return loadArtefacts(typeToArtefacts);
	}
	
	@Override
	public ProjArtefactItems getArtefactItems(ProjArtefactSearchParams searchParams) {
		Map<String, List<ProjArtefact>> typeToArtefacts = artefactDao.loadArtefacts(searchParams).stream()
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
		
		List<ProjTag> projTags = params.isTags() || params.isTagDisplayNames()
				? getProjTags(artefacts)
				: Collections.emptyList();
		
		Map<Long, List<Tag>> artefactKeyToTags = params.isTags()
				? getArtefactKeyToTag(projTags)
				: Collections.emptyMap();
		
		Map<Long, List<String>> artefactKeyToTagDisplayName = params.isTagDisplayNames()
				? getArtefactKeyToTagDisplayName(projTags)
				: Collections.emptyMap();
		
		Map<ProjArtefact, ProjArtefactInfo> infos = new HashMap<>(artefacts.size());
		for (ProjArtefact artefact : artefacts) {
			ProjArtefactInfoImpl info = new ProjArtefactInfoImpl();
			info.setMembers(groupKeyToIdentities.getOrDefault(artefact.getBaseGroup().getKey(), Set.of()));
			info.setNumReferences(artefactToLinkedArtefacts.getOrDefault(artefact, Set.of()).size());
			info.setTags(artefactKeyToTags.getOrDefault(artefact.getKey(), 
					params.isTags()? new ArrayList<>(0): List.of()));
			info.setTagDisplayNames(artefactKeyToTagDisplayName.getOrDefault(artefact.getKey(), 
					params.isTagDisplayNames()? new ArrayList<>(0): List.of()));
			infos.put(artefact, info);
		}
		
		return infos;
	}
	
	@Override
	public void updateMembers(Identity doer, ProjArtefactRef artefactRef, List<IdentityRef> identities) {
		ProjArtefact artefact = getArtefact(artefactRef);
		if (artefact == null) return;
		
		List<Identity> members = securityManager.loadIdentityByRefs(identities);
		
		updateMembers(doer, artefact, members);
	}

	private void updateMembers(Identity doer, ProjArtefact artefact, List<Identity> members) {
		Group group = artefact.getBaseGroup();
		List<Identity> currentMembers = groupDao.getMembers(group, DEFAULT_ROLE_NAME);
		
		List<Identity> membersAdded = new ArrayList<>();
		boolean contentChange = false;
		for (Identity member : members) {
			if (!currentMembers.contains(member)) {
				membersAdded.add(member);
				groupDao.addMembershipOneWay(group, member, DEFAULT_ROLE_NAME);
				activityDao.create(Action.addMember(artefact.getType()), null, null, doer, artefact, member);
				String rolesAfterXml = ProjectXStream.rolesToXml(List.of(DEFAULT_ROLE_NAME));
				activityDao.create(Action.updateRoles(artefact.getType()), null, rolesAfterXml, doer, artefact, member);
				contentChange = true;
				
			}
		}
		
		List<Identity> membersRemoved = new ArrayList<>();
		for (Identity currentMember : currentMembers) {
			if (!members.contains(currentMember)) {
				membersRemoved.add(currentMember);
				groupDao.removeMembership(group, currentMember);
				activityDao.create(Action.removeMember(artefact.getType()), null, null, doer, artefact, currentMember);
				// Load from DB if more than one role possible.
				String rolesBeforeXml = ProjectXStream.rolesToXml(List.of(DEFAULT_ROLE_NAME));
				activityDao.create(Action.updateRoles(artefact.getType()), rolesBeforeXml, null, doer, artefact, currentMember);
				contentChange = true;
			}
		}
		
		onUpdateMembers(artefact, membersAdded, membersRemoved);
		
		if (contentChange) {
			updateContentModified(artefact, doer);
		}
	}
	
	// Maybe move individual artefact logic to a handler
	private void onUpdateMembers(ProjArtefact artefact, List<Identity> membersAdded, List<Identity> membersRemoved) {
		if (ProjAppointment.TYPE.equals(artefact.getType())) {
			ProjAppointmentSearchParams searchParams = new ProjAppointmentSearchParams();
			searchParams.setArtefacts(List.of(artefact));
			List<ProjAppointment> appointments = getAppointments(searchParams);
			if (!appointments.isEmpty()) {
				ProjAppointment appointment = appointments.get(0);
				calendarHelper.createOrUpdateEvent(appointment, membersAdded);
				calendarHelper.deleteEvent(appointment, membersRemoved);
			}
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
	public List<ProjTagInfo> getTagInfos(ProjProject project, ProjArtefactRef selectionArtefact){
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
	
	private Map<Long, List<String>> getArtefactKeyToTagDisplayName(List<ProjTag> projTags) {
		return projTags.stream().collect(
				Collectors.groupingBy
						(projTag -> projTag.getArtefact().getKey(),
						Collectors.collectingAndThen(
								Collectors.toList(),
								projTag -> projTag.stream()
										.map(ProjTag::getTag)
										.map(Tag::getDisplayName)
										.collect(Collectors.toList()))));
	}
	
	private void updateContentModified(ProjArtefact artefact, Identity modifiedBy) {
		artefact.setContentModifiedDate(new Date());
		artefact.setContentModifiedBy(modifiedBy);
		artefactDao.save(artefact);
	}
	
	private void deleteArtefactSoftly(ProjArtefact artefact) {
		if (ProjectStatus.deleted != artefact.getStatus()) {
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
		VFSLeaf vfsLeaf = fileStorage.store(project, doer, filename, inputStream);
		ProjFile file = fileDao.create(artefact, vfsLeaf.getMetaInfo());
		Action action = upload? Action.fileUpload: Action.fileCreate;
		String after = ProjectXStream.toXml(file);
		activityDao.create(action, null, after, null, doer, artefact);
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
		}
	}
	
	@Override
	public void deleteFileSoftly(Identity doer, ProjFileRef file) {
		ProjFile reloadedFile = getFile(file);
		if (reloadedFile == null) {
			return;
		}
		String before = ProjectXStream.toXml(reloadedFile);
		
		deleteArtefactSoftly(reloadedFile.getArtefact());
		
		String after = ProjectXStream.toXml(reloadedFile);
		activityDao.create(Action.fileStatusDelete, before, after, null, doer, reloadedFile.getArtefact());
	}
	
	@Override
	public boolean existsFile(ProjProjectRef project, String filename) {
		return fileStorage.exists(project, filename);
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
	 * Files
	 */
	
	@Override
	public ProjNote createNote(Identity doer, ProjProject project) {
		ProjArtefact artefact = artefactDao.create(ProjNote.TYPE, project, doer);
		ProjNote note = noteDao.create(artefact);
		String after = ProjectXStream.toXml(note);
		activityDao.create(Action.noteCreate, null, after, null, doer, artefact);
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
		}
	}

	@Override
	public void deleteNoteSoftly(Identity doer, ProjNoteRef note) {
		ProjNote reloadedNote = getNote(note);
		if (reloadedNote == null) {
			return;
		}
		String before = ProjectXStream.toXml(reloadedNote);
		
		deleteArtefactSoftly(reloadedNote.getArtefact());
		
		reloadedNote = getNote(note);
		String after = ProjectXStream.toXml(reloadedNote);
		activityDao.create(Action.noteStatusDelete, before, after, null, doer, reloadedNote.getArtefact());
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

	@Override
	public VFSContainer getProjectContainer(ProjProjectRef project) {
		return fileStorage.getOrCreateFileContainer(project);
	}
	
	
	/*
	 * Appointments
	 */
	
	@Override
	public ProjAppointment createAppointment(Identity doer, ProjProject project) {
		// Add some time to start after the creation of the activity (below)
		Date startDate = DateUtils.addMinutes(new Date(), 1);
		Date endDate = DateUtils.addHours(startDate, 1);
		return createAppointment(doer, true, project, startDate, endDate);
	}
	
	private ProjAppointment createAppointment(Identity doer, boolean createActivity, ProjProject project, Date startDate, Date endDate) {
		ProjArtefact artefact = artefactDao.create(ProjAppointment.TYPE, project, doer);
		ProjAppointment appointment = appointmentDao.create(artefact, DateUtils.truncateSeconds(startDate), DateUtils.truncateSeconds(endDate));
		calendarHelper.createOrUpdateEvent(appointment, List.of(doer));
		if (createActivity) {
			String after = ProjectXStream.toXml(appointment);
			activityDao.create(Action.appointmentCreate, null, after, null, doer, artefact);
		}
		return appointment;
	}
	
	
	@Override
	public void updateAppointment(Identity doer, ProjAppointmentRef appointment, Date startDate, Date endDate,
			String subject, String description, String location, String color, boolean allDay, String recurrenceRule) {
		ProjAppointment reloadedAppointment = getAppointment(appointment, true);
		if (reloadedAppointment == null) {
			return;
		}
		
		updateOccurenceIds(doer, reloadedAppointment, startDate, allDay);
		
		updateReloadedAppointment(doer, true, reloadedAppointment, reloadedAppointment.getRecurrenceId(), startDate,
				endDate, subject, description, location, color, allDay, recurrenceRule,
				reloadedAppointment.getRecurrenceExclusion());
	}
	
	private void updateOccurenceIds(Identity doer, ProjAppointment reloadedAppointment, Date startDate, boolean allDay) {
		if (StringHelper.containsNonWhitespace(reloadedAppointment.getRecurrenceRule())) {
			int beginDiff = (int)(startDate.getTime() - reloadedAppointment.getStartDate().getTime());
			if (beginDiff != 0) {
				ProjAppointmentSearchParams searchParams = new ProjAppointmentSearchParams();
				searchParams.setEventIds(List.of(reloadedAppointment.getEventId()));
				searchParams.setRecurrenceIdAvailable(Boolean.TRUE);
				List<ProjAppointment> occurenceAppointments = appointmentDao.loadAppointments(searchParams);
				for (ProjAppointment occurenceAppointment : occurenceAppointments) {
					String updateOccurenceId = calendarHelper.getUpdatedOccurenceId(occurenceAppointment.getRecurrenceId(), allDay, beginDiff);
					if (StringHelper.containsNonWhitespace(updateOccurenceId)) {
						updateReloadedAppointment(doer, true, occurenceAppointment, updateOccurenceId,
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
	public void moveAppointment(Identity doer, String identifier, Long days, Long minutes, boolean moveStartDate) {
		ProjAppointment reloadedAppointment = getAppointment(identifier, true);
		if (reloadedAppointment == null) {
			return;
		}
		
		moveReloadedAppointment(doer, true, reloadedAppointment, days, minutes, moveStartDate);
	}
	
	@Override
	public ProjAppointment createAppointmentOcurrence(Identity doer, String identifier, String recurrenceId,
			Date startDate, Date endDate) {
		ProjAppointment reloadedAppointment = getAppointment(identifier, true);
		if (reloadedAppointment == null) {
			return null;
		}
		
		ProjAppointment clonedAppointment = createAppointment(doer, false, reloadedAppointment.getArtefact().getProject(), startDate, endDate);
		List<Identity> currentMembers = groupDao.getMembers(reloadedAppointment.getArtefact().getBaseGroup(), DEFAULT_ROLE_NAME);
		updateMembers(doer, clonedAppointment.getArtefact(), currentMembers);
		
		clonedAppointment.setEventId(reloadedAppointment.getEventId());
		clonedAppointment = updateReloadedAppointment(doer, false, clonedAppointment, recurrenceId, startDate, endDate,
				reloadedAppointment.getSubject(), reloadedAppointment.getDescription(),
				reloadedAppointment.getLocation(), reloadedAppointment.getColor(), reloadedAppointment.isAllDay(), null,
				null);
		
		String after = ProjectXStream.toXml(clonedAppointment);
		activityDao.create(Action.appointmentCreate, null, after, null, doer, clonedAppointment.getArtefact());
		
		return clonedAppointment;
	}
	
	@Override
	public ProjAppointment createMovedAppointmentOcurrence(Identity doer, String identifier,
			String recurrenceId, Date startDate, Date endDate, Long days, Long minutes, boolean moveStartDate) {
		ProjAppointment reloadedAppointment = getAppointment(identifier, true);
		if (reloadedAppointment == null) {
			return null;
		}
		
		ProjAppointment clonedAppointment = createAppointment(doer, false, reloadedAppointment.getArtefact().getProject(), startDate, endDate);
		List<Identity> currentMembers = groupDao.getMembers(reloadedAppointment.getArtefact().getBaseGroup(), DEFAULT_ROLE_NAME);
		updateMembers(doer, clonedAppointment.getArtefact(), currentMembers);
		
		clonedAppointment.setEventId(reloadedAppointment.getEventId());
		clonedAppointment = updateReloadedAppointment(doer, false, clonedAppointment, recurrenceId,
				new Date(startDate.getTime()), new Date(endDate.getTime()), reloadedAppointment.getSubject(),
				reloadedAppointment.getDescription(), reloadedAppointment.getLocation(), reloadedAppointment.getColor(),
				reloadedAppointment.isAllDay(), null, null);
		
		clonedAppointment = moveReloadedAppointment(doer, false, clonedAppointment, days, minutes, moveStartDate);
		
		String after = ProjectXStream.toXml(clonedAppointment);
		activityDao.create(Action.appointmentCreate, null, after, null, doer, clonedAppointment.getArtefact());
		
		return clonedAppointment;
	}

	private ProjAppointment updateReloadedAppointment(Identity doer, boolean createActivity,
			ProjAppointment reloadedAppointment, String recurrenceId, Date startDate, Date endDate, String subject,
			String description, String location, String color, boolean allDay, String recurrenceRule,
			String recurrenceExclusion) {
		String before = ProjectXStream.toXml(reloadedAppointment);
		
		boolean contentChanged = false;
		if (!Objects.equals(reloadedAppointment.getRecurrenceId(), recurrenceId)) {
			reloadedAppointment.setRecurrenceId(recurrenceId);
			contentChanged = true;
		}
		Date cleanedStartDate = DateUtils.truncateSeconds(startDate);
		if (!Objects.equals(reloadedAppointment.getStartDate(), cleanedStartDate)) {
			reloadedAppointment.setStartDate(cleanedStartDate);
			contentChanged = true;
		}
		Date cleanedEndDate = DateUtils.truncateSeconds(endDate);
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
			calendarHelper.createOrUpdateEvent(reloadedAppointment, members);
			
			if (createActivity) {
				String after = ProjectXStream.toXml(reloadedAppointment);
				activityDao.create(Action.appointmentContentUpdate, before, after, null, doer, reloadedAppointment.getArtefact());
			}
		}
		
		return reloadedAppointment;
	}

	private ProjAppointment moveReloadedAppointment(Identity doer, boolean createActivity, ProjAppointment reloadedAppointment, Long days,
			Long minutes, boolean moveStartDate) {
		Date startDate = reloadedAppointment.getStartDate();
		if (moveStartDate) {
			startDate = move(reloadedAppointment.getStartDate(), days, minutes);
		}
		Date endDate = move(reloadedAppointment.getEndDate(), days, minutes);
		
		return updateReloadedAppointment(doer, createActivity, reloadedAppointment,
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
	public void addAppointmentExclusion(Identity doer, String identifier, Date exclusionDate, boolean single) {
		ProjAppointment reloadedAppointment = getAppointment(identifier, true);
		if (reloadedAppointment == null) {
			return;
		}
		if (!StringHelper.containsNonWhitespace(reloadedAppointment.getRecurrenceRule())) {
			return;
		}
		
		if (single) {
			addAppointmentSingleExclusion(doer, true, reloadedAppointment, exclusionDate);
			String before = ProjectXStream.toXml(exclusionDate);
			activityDao.create(Action.appointmentOccurrenceDelete, before, null, null, doer, reloadedAppointment.getArtefact());
		} else {
			addAppointmentFutureExclusion(doer, reloadedAppointment, exclusionDate);
		}
	}
	
	private void addAppointmentSingleExclusion(Identity doer, boolean createActivity, ProjAppointment reloadedAppointment, Date exclusionDate) {
		List<Date> exclisionDates = CalendarUtils.getRecurrenceExcludeDates(reloadedAppointment.getRecurrenceExclusion());
		exclisionDates.add(exclusionDate);
		String recurrenceExclusion = CalendarUtils.getRecurrenceExcludeRule(exclisionDates);
		updateReloadedAppointment(doer, createActivity, reloadedAppointment, reloadedAppointment.getRecurrenceId(),
				reloadedAppointment.getStartDate(), reloadedAppointment.getEndDate(),
				reloadedAppointment.getSubject(), reloadedAppointment.getDescription(),
				reloadedAppointment.getLocation(), reloadedAppointment.getColor(), reloadedAppointment.isAllDay(),
				reloadedAppointment.getRecurrenceRule(), recurrenceExclusion);
	}
	
	private void addAppointmentFutureExclusion(Identity doer, ProjAppointment reloadedAppointment, Date exclusionDate) {
		String recurrenceRule = calendarHelper.getExclusionRecurrenceRule(reloadedAppointment.getRecurrenceRule(), exclusionDate);
		updateReloadedAppointment(doer, true, reloadedAppointment, reloadedAppointment.getRecurrenceId(),
				reloadedAppointment.getStartDate(), reloadedAppointment.getEndDate(),
				reloadedAppointment.getSubject(), reloadedAppointment.getDescription(),
				reloadedAppointment.getLocation(), reloadedAppointment.getColor(), reloadedAppointment.isAllDay(),
				recurrenceRule, reloadedAppointment.getRecurrenceExclusion());
	}
	
	@Override
	public void deleteAppointmentSoftly(Identity doer, String identifier, Date occurenceDate) {
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
				addAppointmentSingleExclusion(doer, false, rootAppointment, occurenceDate);
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
		
		deleteArtefactSoftly(reloadedAppointment.getArtefact());
		
		ProjAppointment deletedAppointment = getAppointment(reloadedAppointment);
		String after = ProjectXStream.toXml(deletedAppointment);
		activityDao.create(Action.appointmentStatusDelete, before, after, null, doer, deletedAppointment.getArtefact());
		
		List<Identity> members = groupDao.getMembers(deletedAppointment.getArtefact().getBaseGroup(), DEFAULT_ROLE_NAME);
		calendarHelper.deleteEvent(deletedAppointment, members);
	}

	@Override
	public void deleteAppointmentPermanent(ProjAppointmentRef appointment) {
		ProjAppointment reloadedAppointment = getAppointment(appointment);
		if (reloadedAppointment == null) {
			return;
		}
		
		ProjArtefact artefact = reloadedAppointment.getArtefact();
		appointmentDao.delete(appointment);
		deleteArtefactPermanent(artefact);
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
	public ProjMilestone createMilestone(Identity doer, ProjProject project) {
		Date dueDate = DateUtils.setTime(new Date(), 23, 59, 0);
		return createMilestone(doer, true, project, dueDate);
	}
	
	private ProjMilestone createMilestone(Identity doer, boolean createActivity, ProjProject project, Date dueDate) {
		ProjArtefact artefact = artefactDao.create(ProjMilestone.TYPE, project, doer);
		ProjMilestone milestone = milestoneDao.create(artefact, DateUtils.truncateSeconds(dueDate));
		calendarHelper.createOrUpdateEvent(milestone, List.of(doer));
		if (createActivity) {
			String after = ProjectXStream.toXml(milestone);
			activityDao.create(Action.milestoneCreate, null, after, null, doer, artefact);
		}
		return milestone;
	}
	
	
	@Override
	public void updateMilestone(Identity doer, ProjMilestoneRef milestone, ProjMilestoneStatus status, Date dueDate,
			String subject, String description, String color) {
		ProjMilestone reloadedMilestone = getMilestone(milestone, true);
		if (reloadedMilestone == null) {
			return;
		}
		
		updateReloadedMilestone(doer, reloadedMilestone, status, dueDate, subject, description, color);
	}
	
	@Override
	public void updateMilestoneStatus(Identity doer, ProjMilestoneRef milestone, ProjMilestoneStatus status) {
		ProjMilestone reloadedMilestone = getMilestone(milestone, true);
		if (reloadedMilestone == null) {
			return;
		}
		
		updateReloadedMilestone(doer, reloadedMilestone, status, reloadedMilestone.getDueDate(),
				reloadedMilestone.getSubject(), reloadedMilestone.getDescription(), reloadedMilestone.getColor());
	}
	
	@Override
	public void moveMilestone(Identity doer, String identifier, Long days) {
		ProjMilestone reloadedMilestone = getMilestone(identifier, true);
		if (reloadedMilestone == null) {
			return;
		}
		
		Date dueDate = move(reloadedMilestone.getDueDate(), days, Long.valueOf(0));
		
		updateReloadedMilestone(doer, reloadedMilestone, reloadedMilestone.getStatus(), dueDate,
				reloadedMilestone.getSubject(), reloadedMilestone.getDescription(), reloadedMilestone.getColor());
	}
	
	private ProjMilestone updateReloadedMilestone(Identity doer, ProjMilestone reloadedMilestone,
			ProjMilestoneStatus status, Date dueDate, String subject, String description, String color) {
		String before = ProjectXStream.toXml(reloadedMilestone);
		
		boolean contentChanged = false;
		if (reloadedMilestone.getStatus() != status) {
			reloadedMilestone.setStatus(status);
			contentChanged = true;
		}
		Date cleanedDueDate = DateUtils.truncateSeconds(dueDate);
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
			
			List<Identity> members = groupDao.getMembers(reloadedMilestone.getArtefact().getBaseGroup(), DEFAULT_ROLE_NAME);
			calendarHelper.createOrUpdateEvent(reloadedMilestone, members);
			
			String after = ProjectXStream.toXml(reloadedMilestone);
			activityDao.create(Action.milestoneContentUpdate, before, after, null, doer, reloadedMilestone.getArtefact());
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
		
		deleteArtefactSoftly(reloadedMilestone.getArtefact());
		
		ProjMilestone deletedMilestone = getMilestone(reloadedMilestone);
		String after = ProjectXStream.toXml(deletedMilestone);
		activityDao.create(Action.milestoneStatusDelete, before, after, null, doer, deletedMilestone.getArtefact());
		
		List<Identity> members = groupDao.getMembers(deletedMilestone.getArtefact().getBaseGroup(), DEFAULT_ROLE_NAME);
		calendarHelper.deleteEvent(deletedMilestone, members);
	}

	@Override
	public void deleteMilestonePermanent(ProjMilestoneRef milestone) {
		ProjMilestone reloadedMilestone = getMilestone(milestone);
		if (reloadedMilestone == null) {
			return;
		}
		
		ProjArtefact artefact = reloadedMilestone.getArtefact();
		milestoneDao.delete(milestone);
		deleteArtefactPermanent(artefact);
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
	
	@Override
	public void createActivityEdit(Identity doer, ProjFileRef file) {
		ProjFile reloadedFile = getFile(file);
		if (reloadedFile == null) {
			return;
		}
		String before = ProjectXStream.toXml(reloadedFile);
		activityDao.create(Action.fileEdit, before, null, null, doer, reloadedFile.getArtefact());
	}

	@Override
	public List<ProjActivity> getActivities(ProjActivitySearchParams searchParams, int firstResult, int maxResults) {
		return activityDao.loadActivities(searchParams, firstResult, maxResults);
	}
	
	@Override
	public Map<Long, ProjActivity> getProjectKeyToLastActivity(ProjActivitySearchParams searchParams) {
		return activityDao.loadProjectKeyToLastActivity(searchParams);
	}

}
