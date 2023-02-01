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
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.olat.basesecurity.IdentityRef;
import org.olat.core.id.Identity;
import org.olat.core.id.Organisation;

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
	
	public List<ProjNoteInfo> getNoteInfos(ProjNoteSearchParams searchParams);
	
	
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
