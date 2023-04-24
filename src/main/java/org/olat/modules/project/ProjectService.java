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
package org.olat.modules.project;

import java.io.InputStream;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.olat.basesecurity.IdentityRef;
import org.olat.commons.calendar.model.Kalendar;
import org.olat.core.commons.services.tag.TagInfo;
import org.olat.core.id.Identity;
import org.olat.core.id.Organisation;
import org.olat.core.id.OrganisationRef;
import org.olat.core.util.vfs.VFSContainer;
import org.olat.modules.todo.ToDoPriority;
import org.olat.modules.todo.ToDoStatus;

/**
 * 
 * Initial date: 22 Nov 2022<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public interface ProjectService {
	
	public ProjProject createProject(Identity doer);

	public ProjProject updateProject(Identity doer, ProjProject project);
	
	public ProjProject setStatusDone(Identity doer, ProjProjectRef project);
	
	public ProjProject reopen(Identity doer, ProjProjectRef project);

	public ProjProject setStatusDeleted(Identity doer, ProjProjectRef project);

	public ProjProject getProject(ProjProjectRef project);
	
	public List<ProjProject> getProjects(ProjProjectSearchParams searchParams);

	public void updateProjectOrganisations(Identity doer, ProjProject project, Collection<Organisation> organisations);

	public List<Organisation> getOrganisations(ProjProjectRef project);
	
	public boolean isInOrganisation(ProjProjectRef project, Collection<OrganisationRef> organisations);

	public void updateMember(Identity doer, ProjProject project, Identity identity, Set<ProjectRole> roles);
	
	public void updateMembers(Identity doer, ProjProject project, Map<Identity, Set<ProjectRole>> identityToRoles);
	
	public void removeMembers(Identity doer, ProjProject project, Collection<Identity> identities);

	public boolean isProjectMember(IdentityRef identity);

	public List<Identity> getMembers(ProjProject project, Collection<ProjectRole> roles);

	public Map<Long, List<Identity>> getProjectGroupKeyToMembers(Collection<ProjProject> projects, Collection<ProjectRole> roles);

	public int countMembers(ProjProject project);

	public Set<ProjectRole> getRoles(ProjProject project, IdentityRef identity);

	public List<ProjMemberInfo> getMembersInfos(ProjMemberInfoSearchParameters params);

	public ProjProjectUserInfo getOrCreateProjectUserInfo(ProjProject project, Identity identity);
	
	public ProjProjectUserInfo updateProjectUserInfo(ProjProjectUserInfo projectUserInfo);
	
	
	/*
	 * Artefact
	 */
	
	public void linkArtefacts(Identity doer, ProjArtefact artefact1, ProjArtefact artefact2);
	
	public void unlinkArtefacts(Identity doer, ProjArtefact artefact1, ProjArtefact artefact2);
	
	public List<ProjArtefact> getLinkedArtefacts(ProjArtefact artefact);
	
	public ProjArtefactItems getLinkedArtefactItems(ProjArtefact artefact);
	
	public ProjArtefactItems getArtefactItems(ProjArtefactSearchParams searchParams);

	public void updateMembers(Identity doer, ProjArtefactRef artefactRef, List<IdentityRef> identities);
	
	public Map<Long, Set<Long>> getArtefactKeyToIdentityKeys(Collection<ProjArtefact> artefacts);
	
	public void updateTags(Identity doer, ProjArtefactRef artefact, List<String> displayNames);
	
	public List<TagInfo> getTagInfos(ProjProject project, ProjArtefactRef selectionArtefact);
	

	/*
	 * Files 
	 */
	
	public ProjFile createFile(Identity doer, ProjProject project, String filename, InputStream inputStream, boolean upload);

	public void updateFile(Identity doer, ProjFile file, String filename, String title, String description);
	
	public void deleteFileSoftly(Identity doer, ProjFileRef file);
	
	public boolean existsFile(ProjProjectRef project, String filename);
	
	public ProjFile getFile(ProjFileRef file);

	public long getFilesCount(ProjFileSearchParams searchParams);
	
	public List<ProjFile> getFiles(ProjFileSearchParams searchParams);
	
	public List<ProjFileInfo> getFileInfos(ProjFileSearchParams searchParams, ProjArtefactInfoParams infoParams);
	

	/*
	 * ToDos
	 */
	
	public ProjToDo createToDo(Identity doer, ProjProject project);

	public void updateToDo(Identity doer, ProjToDoRef toDo, String title, ToDoStatus status, ToDoPriority priority,
			Date startDate, Date dueDate, Long expenditureOfWork, String description);
	
	public void updateToDoStatus(Identity doer, String identifier, ToDoStatus status);
	
	public void updateMembers(Identity doer, ProjToDoRef toDo, Collection<? extends IdentityRef> assignees,
			Collection<? extends IdentityRef> delegatees);
	
	public void updateTags(Identity doer, ProjToDoRef toDo, List<String> displayNames);

	public void deleteToDoSoftly(Identity doer, ProjToDoRef toDo);
	
	public void deleteToDoPermanent(ProjToDoRef toDo);
	
	public ProjToDo getToDo(String identifier);

	public long getToDosCount(ProjToDoSearchParams searchParams);
	
	public List<ProjToDo> getToDos(ProjToDoSearchParams searchParams);
	
	public List<ProjToDoInfo> getToDoInfos(ProjToDoSearchParams searchParams, ProjArtefactInfoParams infoParams);
	
	/*
	 * Notes
	 */
	
	public ProjNote createNote(Identity doer, ProjProject project);

	public void updateNote(Identity doer, ProjNoteRef note, String editSessionIdentifier, String title, String text);
	
	public void deleteNoteSoftly(Identity doer, ProjNoteRef note);

	public void deleteNotePermanent(ProjNoteRef note);
	
	public ProjNote getNote(ProjNoteRef note);
	
	public long getNotesCount(ProjNoteSearchParams searchParams);

	public List<ProjNote> getNotes(ProjNoteSearchParams searchParams);
	
	public List<ProjNoteInfo> getNoteInfos(ProjNoteSearchParams searchParams, ProjArtefactInfoParams infoParams);

	public VFSContainer getProjectContainer(ProjProjectRef project);
	
	
	/*
	 * Appointments
	 */
	
	public ProjAppointment createAppointment(Identity doer, ProjProject project);

	public void updateAppointment(Identity doer, ProjAppointmentRef appointment, Date startDate, Date endDate,
			String subject, String description, String location, String color, boolean allDay, String recurrenceRule);
	
	public void moveAppointment(Identity doer, String identifier, Long days, Long minutes, boolean moveStartDate);

	public ProjAppointment createAppointmentOcurrence(Identity appointment, String identifier, String recurrenceId,
			Date startDate, Date endDate);
	
	public ProjAppointment createMovedAppointmentOcurrence(Identity doer, String identifier, String recurrenceId,
			Date startDate, Date endDate, Long days, Long minutes, boolean moveStartDate);
	
	public void addAppointmentExclusion(Identity doer, String identifier, Date exclusionDate, boolean single);

	public void deleteAppointmentSoftly(Identity doer, String identifier, Date occurenceDate);

	public void deleteAppointmentSoftly(Identity doer, ProjAppointmentRef appointment);

	public void deleteAppointmentPermanent(ProjAppointmentRef appointment);
	
	public ProjAppointment getAppointment(ProjAppointmentRef appointment);

	public List<ProjAppointment> getAppointments(ProjAppointmentSearchParams searchParams);
	
	public List<ProjAppointmentInfo> getAppointmentInfos(ProjAppointmentSearchParams searchParams, ProjArtefactInfoParams infoParams);
	
	public Kalendar getAppointmentsKalendar(List<ProjAppointment> appointments);
	
	
	/*
	 * Milestones
	 */
	
	public ProjMilestone createMilestone(Identity doer, ProjProject project);

	public void updateMilestone(Identity doer, ProjMilestoneRef milestone, ProjMilestoneStatus status, Date dueDate,
			String subject, String description, String color);

	public void updateMilestoneStatus(Identity doer, ProjMilestoneRef milestone, ProjMilestoneStatus status);

	public void moveMilestone(Identity doer, String identifier, Long days);

	public void deleteMilestoneSoftly(Identity doer, ProjMilestoneRef milestone);

	public void deleteMilestonePermanent(ProjMilestoneRef milestone);
	
	public ProjMilestone getMilestone(ProjMilestoneRef milestone);
	
	public List<ProjMilestone> getMilestones(ProjMilestoneSearchParams searchParams);
	
	public List<ProjMilestoneInfo> getMilestoneInfos(ProjMilestoneSearchParams searchParams, ProjArtefactInfoParams infoParams);

	public Kalendar getMilestonesKalendar(List<ProjMilestone> milestones);
	
	
	/*
	 * Activities
	 */
	
	public void createActivityRead(Identity doer, ProjProject project);
	
	public void createActivityRead(Identity doer, ProjArtefact artefact);

	public void createActivityDownload(Identity doer, ProjArtefact artefact);

	public void createActivityEdit(Identity doer, ProjFileRef file);
	
	public List<ProjActivity> getActivities(ProjActivitySearchParams searchParams, int firstResult, int maxResults);
	
	public Map<Long, ProjActivity> getProjectKeyToLastActivity(ProjActivitySearchParams searchParams);

}
