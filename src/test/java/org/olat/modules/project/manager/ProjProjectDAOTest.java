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

import static org.assertj.core.api.Assertions.assertThat;
import static org.olat.test.JunitTestHelper.miniRandom;
import static org.olat.test.JunitTestHelper.random;

import java.util.Date;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.time.DateUtils;
import org.junit.Test;
import org.olat.basesecurity.Group;
import org.olat.basesecurity.OrganisationService;
import org.olat.basesecurity.manager.GroupDAO;
import org.olat.core.commons.persistence.DB;
import org.olat.core.id.Identity;
import org.olat.core.id.Organisation;
import org.olat.modules.project.ProjProject;
import org.olat.modules.project.ProjProjectSearchParams;
import org.olat.modules.project.ProjectRole;
import org.olat.modules.project.ProjectService;
import org.olat.modules.project.ProjectStatus;
import org.olat.test.JunitTestHelper;
import org.olat.test.OlatTestCase;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 22 Nov 2022<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class ProjProjectDAOTest extends OlatTestCase {

	@Autowired
	private DB dbInstance;
	@Autowired
	private GroupDAO groupDao;
	@Autowired
	private ProjectService projectService;
	@Autowired
	private OrganisationService organisationService;
	
	@Autowired
	private ProjProjectDAO sut;
	
	@Test
	public void shouldCreateProject() {
		Identity creator = JunitTestHelper.createAndPersistIdentityAsRndUser(random());
		Group group = groupDao.createGroup();
		dbInstance.commitAndCloseSession();
		
		ProjProject project = sut.create(creator, group, null);
		dbInstance.commitAndCloseSession();
		
		assertThat(project).isNotNull();
		assertThat(project.getCreationDate()).isNotNull();
		assertThat(project.getLastModified()).isNotNull();
		assertThat(project.getStatus()).isEqualTo(ProjectStatus.active);
		assertThat(project.isTemplatePrivate()).isFalse();
		assertThat(project.isTemplatePublic()).isFalse();
		assertThat(project.getCreator()).isEqualTo(creator);
		assertThat(project.getBaseGroup()).isEqualTo(group);
	}
	
	@Test
	public void shouldSaveProject() {
		Identity deletedBy = JunitTestHelper.createAndPersistIdentityAsRndUser(random());
		ProjProject project = createRandomProject();
		
		String externalRef = random();
		project.setExternalRef(externalRef);
		String title = random();
		project.setTitle(title);
		String teaser = random();
		project.setTeaser(teaser);
		String description = random();
		project.setDescription(description);
		String avatarCssClass = miniRandom();
		project.setAvatarCssClass(avatarCssClass);
		project.setTemplatePrivate(true);
		project.setTemplatePublic(true);
		Date deletedDate = DateUtils.addDays(new Date(), 1);
		project.setDeletedDate(deletedDate);
		project.setDeletedBy(deletedBy);
		sut.save(project);
		dbInstance.commitAndCloseSession();
		
		ProjProjectSearchParams params = new ProjProjectSearchParams();
		params.setProjectKeys(List.of(project));
		project = sut.loadProjects(params).get(0);
		
		assertThat(project.getExternalRef()).isEqualTo(externalRef);
		assertThat(project.getTitle()).isEqualTo(title);
		assertThat(project.getTeaser()).isEqualTo(teaser);
		assertThat(project.getDescription()).isEqualTo(description);
		assertThat(project.getAvatarCssClass()).isEqualTo(avatarCssClass);
		assertThat(project.isTemplatePrivate()).isTrue();
		assertThat(project.isTemplatePublic()).isTrue();
		assertThat(project.getDeletedDate()).isCloseTo(deletedDate, 1000);
		assertThat(project.getDeletedBy()).isEqualTo(deletedBy);
	}
	
	@Test
	public void shouldDeleteProject() {
		Identity creator = JunitTestHelper.createAndPersistIdentityAsRndUser(random());
		Group group = groupDao.createGroup();
		ProjProject project = sut.create(creator, group, null);
		dbInstance.commitAndCloseSession();
		
		sut.delete(project);
		dbInstance.commitAndCloseSession();
		
		ProjProjectSearchParams params = new ProjProjectSearchParams();
		params.setProjectKeys(List.of(project));
		assertThat(sut.loadProjects(params)).isEmpty();
	}
	
	@Test
	public void shouldLoadProjectByGroup() {
		ProjProject project = createRandomProject();
		
		ProjProject loadedProject = sut.loadProject(project.getBaseGroup());
		
		assertThat(loadedProject).isEqualTo(project);
	}
	
	@Test
	public void shouldLoad_filter_membership() {
		Identity identity = JunitTestHelper.createAndPersistIdentityAsRndUser(random());
		ProjProject project1 = createProject(identity);
		ProjProject project2 = createRandomProject();
		projectService.updateMember(project2.getCreator(), project2, identity, Set.of(ProjectRole.steeringCommitee));
		createRandomProject();
		dbInstance.commitAndCloseSession();
		
		ProjProjectSearchParams params = new ProjProjectSearchParams();
		params.setIdentity(identity);
		List<ProjProject> projects = sut.loadProjects(params);
		
		assertThat(projects).containsExactlyInAnyOrder(project1, project2);
	}
	
	@Test
	public void shouldLoad_filter_organisations() {
		Organisation organisation1 = organisationService.createOrganisation(random(), random(), random(), null, null);
		Organisation organisation2 = organisationService.createOrganisation(random(), random(), random(), null, null);
		ProjProject project1 = createRandomProject();
		projectService.updateProjectOrganisations(project1.getCreator(), project1, List.of(organisation1));
		ProjProject project2 = createRandomProject();
		projectService.updateProjectOrganisations(project2.getCreator(), project2, List.of(organisation1, organisation2));
		ProjProject project3 = createRandomProject();
		projectService.updateProjectOrganisations(project3.getCreator(), project3, List.of(organisation2));
		createRandomProject();
		
		ProjProjectSearchParams params = new ProjProjectSearchParams();
		params.setProjectOrganisations(List.of(organisation1));
		List<ProjProject> projects = sut.loadProjects(params);
		
		assertThat(projects).containsExactlyInAnyOrder(project1, project2);
	}
	
	@Test
	public void shouldLoad_filter_projectKeys() {
		ProjProject project1 = createRandomProject();
		ProjProject project2 = createRandomProject();
		createRandomProject();
		
		ProjProjectSearchParams params = new ProjProjectSearchParams();
		params.setProjectKeys(List.of(project1, project2));
		List<ProjProject> projects = sut.loadProjects(params);
		
		assertThat(projects).containsExactlyInAnyOrder(project1, project2);
	}
	
	@Test
	public void shouldLoad_filter_Status() {
		ProjProject project1 = createRandomProject();
		project1 = projectService.setStatusDone(project1.getCreator(), project1);
		ProjProject project2 = createRandomProject();
		project2 = projectService.setStatusDone(project2.getCreator(), project2);
		ProjProject project3 = createRandomProject();
		project3 = projectService.setStatusDeleted(project3.getCreator(), project3);
		ProjProject project4 = createRandomProject();
		
		ProjProjectSearchParams params = new ProjProjectSearchParams();
		params.setProjectKeys(List.of(project1, project2, project3, project4));
		params.setStatus(List.of(ProjectStatus.done, ProjectStatus.deleted));
		List<ProjProject> projects = sut.loadProjects(params);
		
		assertThat(projects).containsExactlyInAnyOrder(project1, project2, project3);
	}
	
	@Test
	public void shouldLoad_filter_Template() {
		ProjProject project1 = createRandomProject();
		project1.setTemplatePrivate(true);
		project1.setTemplatePublic(true);
		sut.save(project1);
		ProjProject project2 = createRandomProject();
		project2.setTemplatePrivate(true);
		project2.setTemplatePublic(false);
		sut.save(project2);
		ProjProject project3 = createRandomProject();
		project3.setTemplatePrivate(false);
		project3.setTemplatePublic(true);
		sut.save(project3);
		ProjProject project4 = createRandomProject();
		project4.setTemplatePrivate(false);
		project4.setTemplatePublic(false);
		sut.save(project4);
		dbInstance.commitAndCloseSession();
		
		ProjProjectSearchParams params = new ProjProjectSearchParams();
		params.setProjectKeys(List.of(project1, project2, project3, project4));
		assertThat(sut.loadProjects(params)).containsExactlyInAnyOrder(project1, project2, project3, project4);
		
		params.setTemplate(Boolean.TRUE);
		assertThat(sut.loadProjects(params)).containsExactlyInAnyOrder(project1, project2, project3);
		
		params.setTemplate(Boolean.FALSE);
		assertThat(sut.loadProjects(params)).containsExactlyInAnyOrder(project4);
	}
	
	@Test
	public void shouldLoad_filter_ArtefactAvailable() {
		ProjProject project1 = createRandomProject();
		projectService.createNote(project1.getCreator(), project1);
		ProjProject project2 = createRandomProject();
		
		ProjProjectSearchParams params = new ProjProjectSearchParams();
		params.setProjectKeys(List.of(project1, project2));
		assertThat(sut.loadProjects(params)).containsExactlyInAnyOrder(project1, project2);
		
		params.setArtefactAvailable(Boolean.TRUE);
		assertThat(sut.loadProjects(params)).containsExactlyInAnyOrder(project1);
		
		params.setArtefactAvailable(Boolean.FALSE);
		assertThat(sut.loadProjects(params)).containsExactlyInAnyOrder(project2);
	}
	
	private ProjProject createRandomProject() {
		Identity creator = JunitTestHelper.createAndPersistIdentityAsRndUser(random());
		return createProject(creator);
	}

	private ProjProject createProject(Identity creator) {
		ProjProject project = projectService.createProject(creator, creator);
		dbInstance.commitAndCloseSession();
		return project;
	}

}
