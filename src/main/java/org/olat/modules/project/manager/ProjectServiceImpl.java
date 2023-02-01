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
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.olat.basesecurity.BaseSecurity;
import org.olat.basesecurity.Group;
import org.olat.basesecurity.GroupMembership;
import org.olat.basesecurity.IdentityRef;
import org.olat.basesecurity.manager.GroupDAO;
import org.olat.core.commons.services.vfs.VFSMetadata;
import org.olat.core.commons.services.vfs.VFSRepositoryService;
import org.olat.core.id.Identity;
import org.olat.core.id.Organisation;
import org.olat.core.util.StringHelper;
import org.olat.core.util.vfs.VFSConstants;
import org.olat.core.util.vfs.VFSItem;
import org.olat.core.util.vfs.VFSLeaf;
import org.olat.core.util.vfs.VFSStatus;
import org.olat.modules.project.ProjActivity;
import org.olat.modules.project.ProjActivity.Action;
import org.olat.modules.project.ProjActivitySearchParams;
import org.olat.modules.project.ProjArtefact;
import org.olat.modules.project.ProjArtefactItems;
import org.olat.modules.project.ProjArtefactRef;
import org.olat.modules.project.ProjArtefactSearchParams;
import org.olat.modules.project.ProjArtefactToArtefact;
import org.olat.modules.project.ProjArtefactToArtefactSearchParams;
import org.olat.modules.project.ProjFile;
import org.olat.modules.project.ProjFileRef;
import org.olat.modules.project.ProjFileSearchParams;
import org.olat.modules.project.ProjMemberInfo;
import org.olat.modules.project.ProjMemberInfoSearchParameters;
import org.olat.modules.project.ProjNote;
import org.olat.modules.project.ProjNoteInfo;
import org.olat.modules.project.ProjNoteRef;
import org.olat.modules.project.ProjNoteSearchParams;
import org.olat.modules.project.ProjProject;
import org.olat.modules.project.ProjProjectRef;
import org.olat.modules.project.ProjProjectSearchParams;
import org.olat.modules.project.ProjProjectToOrganisation;
import org.olat.modules.project.ProjProjectUserInfo;
import org.olat.modules.project.ProjectRole;
import org.olat.modules.project.ProjectService;
import org.olat.modules.project.ProjectStatus;
import org.olat.modules.project.model.ProjArtefactItemsImpl;
import org.olat.modules.project.model.ProjMemberInfoImpl;
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
	private ProjActivityDAO activityDao;
	@Autowired
	protected BaseSecurity securityManager;
	@Autowired
	private GroupDAO groupDao;
	
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
		if (!Objects.equals(reloadedProject.getExternalRef(), project.getExternalRef())) {
			contentChanged = true;
		}
		if (!Objects.equals(reloadedProject.getTitle(), project.getTitle())) {
			contentChanged = true;
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
			activityDao.create(Action.projectContentContent, before, after, doer, updatedProject);
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
		
		return artefacts;
	}
	
	private ProjArtefact getArtefact(ProjArtefactRef artefactRef) {
		ProjArtefactSearchParams searchParams = new ProjArtefactSearchParams();
		searchParams.setArtefacts(List.of(artefactRef));
		List<ProjArtefact> artefacts = artefactDao.loadArtefacts(searchParams);
		return !artefacts.isEmpty()? artefacts.get(0): null;
	}
	
	@Override
	public void updateMembers(Identity doer, ProjArtefactRef artefactRef, List<IdentityRef> identities) {
		ProjArtefact artefact = getArtefact(artefactRef);
		if (artefact == null) return;
		
		Group group = artefact.getBaseGroup();
		List<Identity> currentMembers = groupDao.getMembers(group, DEFAULT_ROLE_NAME);
		List<Identity> members = securityManager.loadIdentityByRefs(identities);
		
		boolean contentChange = false;
		for (Identity member : members) {
			if (!currentMembers.contains(member)) {
				groupDao.addMembershipOneWay(group, member, DEFAULT_ROLE_NAME);
				activityDao.create(Action.addMember(artefact.getType()), null, null, doer, artefact, member);
				String rolesAfterXml = ProjectXStream.rolesToXml(List.of(DEFAULT_ROLE_NAME));
				activityDao.create(Action.updateRoles(artefact.getType()), null, rolesAfterXml, doer, artefact, member);
				contentChange = true;
			}
		}
		
		for (Identity currentMember : currentMembers) {
			if (!members.contains(currentMember)) {
				groupDao.removeMembership(group, currentMember);
				activityDao.create(Action.removeMember(artefact.getType()), null, null, doer, artefact, currentMember);
				// Load from DB if more than one role possible.
				String rolesBeforeXml = ProjectXStream.rolesToXml(List.of(DEFAULT_ROLE_NAME));
				activityDao.create(Action.updateRoles(artefact.getType()), rolesBeforeXml, null, doer, artefact, currentMember);
				contentChange = true;
			}
		}
		
		if (contentChange) {
			updateContentModified(artefact, doer);
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

	
	/*
	 * Notes
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
		
		ProjActivitySearchParams searchParams = new ProjActivitySearchParams();
		searchParams.setArtefacts(List.of(reloadedNote.getArtefact()));
		List<ProjActivity> activities = activityDao.loadActivities(searchParams, 0, -1);
		activityDao.delete(activities);
		deleteArtefactPermanent(reloadedNote.getArtefact());
		noteDao.delete(note);
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
	public List<ProjNoteInfo> getNoteInfos(ProjNoteSearchParams searchParams) {
		List<ProjNote> notes = getNotes(searchParams);
		
		List<Long> groupKeys = new ArrayList<>(notes.size());
		List<ProjArtefact> artefacts = new ArrayList<>(notes.size());
		for (ProjNote note : notes) {
			groupKeys.add(note.getArtefact().getBaseGroup().getKey());
			artefacts.add(note.getArtefact());
		}
		Map<Long, Set<Identity>> groupKeyToIdentities = memberQueries.getGroupKeyToIdentities(groupKeys);
		Map<ProjArtefact, Set<ProjArtefact>> artefactToLinkedArtefacts = getArtefactToLinkedArtefacts(artefacts);
		
		List<ProjNoteInfo> infos = new ArrayList<>(notes.size());
		for (ProjNote note : notes) {
			ProjNoteInfoImpl info = new ProjNoteInfoImpl();
			info.setNote(note);
			info.setMembers(groupKeyToIdentities.get(note.getArtefact().getBaseGroup().getKey()));
			info.setNumReferences(artefactToLinkedArtefacts.getOrDefault(note.getArtefact(), Set.of()).size());
			infos.add(info);
		}
		
		return infos;
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
