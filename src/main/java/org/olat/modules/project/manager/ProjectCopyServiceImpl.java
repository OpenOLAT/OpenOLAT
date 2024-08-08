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

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.olat.basesecurity.OrganisationRoles;
import org.olat.basesecurity.OrganisationService;
import org.olat.core.commons.services.vfs.VFSMetadata;
import org.olat.core.commons.services.vfs.VFSRepositoryService;
import org.olat.core.id.Identity;
import org.olat.core.id.Organisation;
import org.olat.core.util.vfs.LocalFileImpl;
import org.olat.core.util.vfs.VFSItem;
import org.olat.core.util.vfs.VFSLeaf;
import org.olat.modules.project.ProjActivity.Action;
import org.olat.modules.project.ProjAppointment;
import org.olat.modules.project.ProjArtefact;
import org.olat.modules.project.ProjArtefactItems;
import org.olat.modules.project.ProjArtefactSearchParams;
import org.olat.modules.project.ProjArtefactToArtefact;
import org.olat.modules.project.ProjArtefactToArtefactSearchParams;
import org.olat.modules.project.ProjDecision;
import org.olat.modules.project.ProjFile;
import org.olat.modules.project.ProjMilestone;
import org.olat.modules.project.ProjMilestoneStatus;
import org.olat.modules.project.ProjNote;
import org.olat.modules.project.ProjProject;
import org.olat.modules.project.ProjProjectImageType;
import org.olat.modules.project.ProjProjectRef;
import org.olat.modules.project.ProjTag;
import org.olat.modules.project.ProjTagSearchParams;
import org.olat.modules.project.ProjToDo;
import org.olat.modules.project.ProjectCopyService;
import org.olat.modules.project.ProjectService;
import org.olat.modules.project.ProjectStatus;
import org.olat.modules.project.ui.ProjectBCFactory;
import org.olat.modules.todo.ToDoStatus;
import org.olat.modules.todo.ToDoTask;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 
 * Initial date: 9 May 2023<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
@Service
public class ProjectCopyServiceImpl implements ProjectCopyService {
	
	@Autowired
	private ProjectService projectService;
	@Autowired
	private ProjArtefactToArtefactDAO artefactToArtefactDao;
	@Autowired
	private ProjTagDAO tagDao;
	@Autowired
	private OrganisationService organisationService;
	@Autowired
	private VFSRepositoryService vfsRepositoryServcie;
	@Autowired
	private ProjActivityDAO activityDao;
	
	@Override
	public ProjProject copyProjectFromTemplate(Identity doer, ProjProjectRef projectTemplate) {
		ProjProject project = projectService.getProject(projectTemplate);
		if (project == null || ProjectStatus.deleted == project.getStatus()) {
			return null;
		}
		
		// The new project a regular project but  never a template 
		ProjectBCFactory bcFactory = ProjectBCFactory.createFactoryProject();
		ProjProject projectCopy = projectService.createProject(doer, bcFactory, doer);
		projectCopy = projectService.updateProject(doer, bcFactory, projectCopy, 
				project.getExternalRef(),
				project.getTitle(),
				project.getTeaser(),
				project.getDescription(),
				false,
				false);
		
		List<Organisation> organisations = organisationService.getOrganisations(doer, OrganisationRoles.user);
		projectService.updateProjectOrganisations(doer, projectCopy, organisations);
		
		VFSLeaf backgroundImage = projectService.getProjectImage(project, ProjProjectImageType.background);
		if (backgroundImage instanceof LocalFileImpl file) {
			projectService.storeProjectImage(doer, projectCopy, ProjProjectImageType.background, file.getBasefile(), backgroundImage.getName());
		}
		VFSLeaf avatarImage = projectService.getProjectImage(project, ProjProjectImageType.avatar);
		if (avatarImage instanceof LocalFileImpl file) {
			projectService.storeProjectImage(doer, projectCopy, ProjProjectImageType.avatar, file.getBasefile(), avatarImage.getName());
		}
		
		copyProjectArtefacts(doer, project, projectCopy);
		
		return projectCopy;
	}

	@Override
	public void copyProjectArtefacts(Identity doer, ProjProjectRef project, ProjProject projectCopy) {
		ProjArtefactSearchParams sarchParams = new ProjArtefactSearchParams();
		sarchParams.setProject(project);
		sarchParams.setStatus(List.of(ProjectStatus.active));
		ProjArtefactItems artefactItems = projectService.getArtefactItems(sarchParams);
		
		ProjTagSearchParams tagSearchParams = new ProjTagSearchParams();
		tagSearchParams.setProject(project);
		tagSearchParams.setArtefactStatus(List.of(ProjectStatus.active));
		Map<ProjArtefact, List<String>> artefactToTagDisplayNames = tagDao.loadTags(tagSearchParams).stream()
				.collect(Collectors.groupingBy(
						ProjTag::getArtefact,
						Collectors.collectingAndThen(
								Collectors.toList(),
								tags -> tags.stream()
										.map(tag -> tag.getTag().getDisplayName())
										.distinct()
										.collect(Collectors.toList()))));
		
		ProjectBCFactory bcFactory = ProjectBCFactory.createFactory(projectCopy);
		Map<ProjArtefact, ProjArtefact> artefactToArtefactCopy = new HashMap<>();
		// The creation order has to be kept.
		List<ProjArtefact> artefacts = artefactItems.getArtefacts().stream()
				.sorted((a1, a2) -> a1.getCreationDate().compareTo(a2.getCreationDate()))
				.toList();
		for (ProjArtefact artefact : artefacts) {
			switch (artefact.getType()) {
			case ProjFile.TYPE: copyFile(doer, projectCopy, artefactToTagDisplayNames, artefactToArtefactCopy, artefactItems.getFile(artefact));
			case ProjNote.TYPE: copyNote(doer, projectCopy, artefactToTagDisplayNames, artefactToArtefactCopy, artefactItems.getNote(artefact));
			case ProjToDo.TYPE: copyToDo(doer, projectCopy, artefactToTagDisplayNames, artefactToArtefactCopy, artefactItems.getToDo(artefact));
			case ProjDecision.TYPE: copyDecision(doer, projectCopy, artefactToTagDisplayNames, artefactToArtefactCopy, artefactItems.getDecision(artefact));
			case ProjAppointment.TYPE: copyAppointment(doer, bcFactory, projectCopy, artefactToTagDisplayNames, artefactToArtefactCopy, artefactItems.getAppointment(artefact));
			case ProjMilestone.TYPE: copyMilestone(doer, bcFactory, projectCopy, artefactToTagDisplayNames, artefactToArtefactCopy, artefactItems.getMilestone(artefact));
			default: // do not copy 
			}
			
		}
		
		copyArtefactToArtefact(doer, project, artefactToArtefactCopy);
	}

	private void copyFile(Identity doer, ProjProject projectCopy, Map<ProjArtefact, List<String>> artefactToTagDisplayNames,
			Map<ProjArtefact, ProjArtefact> artefactToArtefactCopy,
			ProjFile file) {
		if (file == null) return;
		
		VFSMetadata vfsMetadata = file.getVfsMetadata();
		VFSItem item = vfsRepositoryServcie.getItemFor(vfsMetadata);
		if (item instanceof VFSLeaf vfsLeaf) {
			ProjFile fileCopy = projectService.createFile(doer, projectCopy, vfsMetadata.getFilename(),
					vfsLeaf.getInputStream(), false);
			activityDao.create(Action.fileCopyInitialized, null, null, doer, fileCopy.getArtefact());
			projectService.updateFile(doer, fileCopy,
					vfsMetadata.getFilename(),
					vfsMetadata.getTitle(),
					vfsMetadata.getComment());
			projectService.updateTags(doer, fileCopy.getArtefact(), artefactToTagDisplayNames.getOrDefault(file.getArtefact(), List.of()));
			artefactToArtefactCopy.put(file.getArtefact(), fileCopy.getArtefact());
		}
	}

	private void copyNote(Identity doer, ProjProject projectCopy, Map<ProjArtefact, List<String>> artefactToTagDisplayNames,
			Map<ProjArtefact, ProjArtefact> artefactToArtefactCopy,
			ProjNote note) {
		if (note == null) return;
		
		ProjNote noteCopy = projectService.createNote(doer, projectCopy);
		activityDao.create(Action.noteCopyInitialized, null, null, doer, noteCopy.getArtefact());
		projectService.updateNote(doer, noteCopy, null, note.getTitle(), note.getText());
		projectService.updateTags(doer, noteCopy.getArtefact(), artefactToTagDisplayNames.getOrDefault(note.getArtefact(), List.of()));
		artefactToArtefactCopy.put(note.getArtefact(), noteCopy.getArtefact());
	}
	
	private void copyToDo(Identity doer, ProjProject projectCopy, Map<ProjArtefact, List<String>> artefactToTagDisplayNames,
			Map<ProjArtefact, ProjArtefact> artefactToArtefactCopy,
			ProjToDo toDo) {
		if (toDo == null) return;
		
		ProjToDo toDoCopy = projectService.createToDo(doer, projectCopy);
		activityDao.create(Action.toDoCopyInitialized, null, null, doer, toDoCopy.getArtefact());
		projectService.updateMembers(doer, toDoCopy, List.of(doer), List.of());
		ToDoTask toDoTask = toDo.getToDoTask();
		projectService.updateToDo(doer, toDoCopy,
				toDoTask.getTitle(),
				ToDoStatus.open,
				toDoTask.getPriority(),
				null,
				null,
				toDoTask.getExpenditureOfWork(),
				toDoTask.getDescription());
		projectService.updateTags(doer, toDoCopy, artefactToTagDisplayNames.getOrDefault(toDo.getArtefact(), List.of()));
		artefactToArtefactCopy.put(toDo.getArtefact(), toDoCopy.getArtefact());
	}
	
	private void copyDecision(Identity doer, ProjProject projectCopy, Map<ProjArtefact, List<String>> artefactToTagDisplayNames,
			Map<ProjArtefact, ProjArtefact> artefactToArtefactCopy, ProjDecision decision) {
		if (decision == null) return;
		
		ProjDecision decisionCopy = projectService.createDecision(doer, projectCopy);
		activityDao.create(Action.decisionCopyInitialized, null, null, doer, decisionCopy.getArtefact());
		projectService.updateDecision(doer, decisionCopy, decision.getTitle(), decision.getDetails(), null);
		projectService.updateTags(doer, decisionCopy.getArtefact(), artefactToTagDisplayNames.getOrDefault(decision.getArtefact(), List.of()));
		artefactToArtefactCopy.put(decision.getArtefact(), decisionCopy.getArtefact());
	}

	private void copyAppointment(Identity doer, ProjectBCFactory bcFactory, ProjProject projectCopy,
			Map<ProjArtefact, List<String>> artefactToTagDisplayNames,
			Map<ProjArtefact, ProjArtefact> artefactToArtefactCopy, ProjAppointment appointment) {
		if (appointment == null) return;
		
		ProjAppointment appointmentCopy = projectService.createAppointment(doer, bcFactory, projectCopy, null, null);
		activityDao.create(Action.appointmentCopyInitialized, null, null, doer, appointmentCopy.getArtefact());
		projectService.updateAppointment(doer, bcFactory, appointmentCopy,
				null,
				null,
				appointment.getSubject(),
				appointment.getDescription(),
				appointment.getLocation(),
				appointment.getColor(),
				appointment.isAllDay(),
				appointment.getRecurrenceRule());
		projectService.updateTags(doer, appointmentCopy.getArtefact(), artefactToTagDisplayNames.getOrDefault(appointment.getArtefact(), List.of()));
		artefactToArtefactCopy.put(appointment.getArtefact(), appointmentCopy.getArtefact());
	}
	
	private void copyMilestone(Identity doer, ProjectBCFactory bcFactory, ProjProject projectCopy, Map<ProjArtefact, List<String>> artefactToTagDisplayNames,
			Map<ProjArtefact, ProjArtefact> artefactToArtefactCopy, ProjMilestone milestone) {
		if (milestone == null) return;
		
		ProjMilestone milestoneCopy = projectService.createMilestone(doer, bcFactory, projectCopy);
		activityDao.create(Action.milestoneCopyInitialized, null, null, doer, milestoneCopy.getArtefact());
		projectService.updateMilestone(doer, bcFactory, milestoneCopy,
				ProjMilestoneStatus.open,
				null,
				milestone.getSubject(),
				milestone.getDescription(),
				milestone.getColor());
		projectService.updateTags(doer, milestoneCopy.getArtefact(), artefactToTagDisplayNames.getOrDefault(milestone.getArtefact(), List.of()));
		artefactToArtefactCopy.put(milestone.getArtefact(), milestoneCopy.getArtefact());
	}
	
	private void copyArtefactToArtefact(Identity doer, ProjProjectRef project, Map<ProjArtefact, ProjArtefact> artefactToArtefactCopy) {
		ProjArtefactToArtefactSearchParams searchParams = new ProjArtefactToArtefactSearchParams();
		searchParams.setProject(project);
		List<ProjArtefactToArtefact> artefactToArtefacts = artefactToArtefactDao.loadArtefactToArtefacts(searchParams).stream()
				.sorted((ata1, ata2) -> ata1.getCreationDate().compareTo(ata2.getCreationDate()))
				.toList();
		
		for (ProjArtefactToArtefact artefactToArtefact : artefactToArtefacts) {
			ProjArtefact artefactCopy1 = artefactToArtefactCopy.get(artefactToArtefact.getArtefact1());
			ProjArtefact artefactCopy2 = artefactToArtefactCopy.get(artefactToArtefact.getArtefact2());
			if (artefactCopy1 != null && artefactCopy2 != null) {
				projectService.linkArtefacts(doer, artefactCopy1, artefactCopy2);
			}
		}
	}

}
