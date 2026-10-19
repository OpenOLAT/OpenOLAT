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
import static org.olat.test.JunitTestHelper.random;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.Test;
import org.olat.basesecurity.GroupMembership;
import org.olat.core.commons.persistence.DB;
import org.olat.core.id.Identity;
import org.olat.modules.project.ProjMemberInfoSearchParameters;
import org.olat.modules.project.ProjProject;
import org.olat.modules.project.ProjectRole;
import org.olat.modules.project.ProjectService;
import org.olat.modules.project.ui.ProjectBCFactory;
import org.olat.test.JunitTestHelper;
import org.olat.test.OlatTestCase;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 2 Dec 2022<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class ProjMemberQueriesTest extends OlatTestCase {

	@Autowired
	private DB dbInstance;
	@Autowired
	private ProjectService projectService;
	
	@Autowired
	private ProjMemberQueries sut;
	
	@Test
	public void shouldCheckIfProjectMember() {
		Identity idenity = JunitTestHelper.createAndPersistIdentityAsUser(random());
		dbInstance.commitAndCloseSession();
		assertThat(sut.isProjectMember(idenity)).isFalse();
		
		Identity creator = JunitTestHelper.createAndPersistIdentityAsUser(random());
		ProjProject project = projectService.createProject(creator, new ProjectBCFactory(), creator);
		dbInstance.commitAndCloseSession();
		assertThat(sut.isProjectMember(idenity)).isFalse();
		
		projectService.updateMember(project.getCreator(), new ProjectBCFactory(), project, idenity, Set.of(ProjectRole.client), null);
		dbInstance.commitAndCloseSession();
		assertThat(sut.isProjectMember(idenity)).isTrue();
	}
	
	@Test
	public void shouldGetMemberships() {
		Identity idenity = JunitTestHelper.createAndPersistIdentityAsUser(random());
		Identity creator = JunitTestHelper.createAndPersistIdentityAsUser(random());
		ProjProject project = projectService.createProject(creator, new ProjectBCFactory(), creator);
		projectService.updateMember(creator, new ProjectBCFactory(), project, creator, Set.of(ProjectRole.client, ProjectRole.steeringCommitee), null);
		projectService.updateMember(creator, new ProjectBCFactory(), project, idenity, Set.of(ProjectRole.client, ProjectRole.steeringCommitee), null);
		dbInstance.commitAndCloseSession();
		
		ProjMemberInfoSearchParameters params = new ProjMemberInfoSearchParameters();
		params.setProject(project);
		List<GroupMembership> memberships = sut.getProjMemberships(params);
		
		assertThat(memberships).hasSize(4);
	}

	@Test
	public void shouldGetProjectKeyToRoles() {
		Identity identity = JunitTestHelper.createAndPersistIdentityAsUser(random());
		Identity creator = JunitTestHelper.createAndPersistIdentityAsUser(random());
		ProjProject project1 = projectService.createProject(creator, new ProjectBCFactory(), creator);
		ProjProject project2 = projectService.createProject(creator, new ProjectBCFactory(), creator);
		ProjProject projectNotMember = projectService.createProject(creator, new ProjectBCFactory(), creator);
		ProjProject projectNotInList = projectService.createProject(creator, new ProjectBCFactory(), creator);
		projectService.updateMember(creator, new ProjectBCFactory(), project1, identity, Set.of(ProjectRole.client, ProjectRole.steeringCommitee), null);
		projectService.updateMember(creator, new ProjectBCFactory(), project2, identity, Set.of(ProjectRole.participant), null);
		projectService.updateMember(creator, new ProjectBCFactory(), projectNotInList, identity, Set.of(ProjectRole.leader), null);
		dbInstance.commitAndCloseSession();
		
		Map<Long, Set<ProjectRole>> projectKeyToRoles = sut.getProjectKeyToRoles(
				List.of(project1.getKey(), project2.getKey(), projectNotMember.getKey()), identity);
		
		assertThat(projectKeyToRoles).containsOnlyKeys(project1.getKey(), project2.getKey());
		assertThat(projectKeyToRoles.get(project1.getKey())).containsExactlyInAnyOrder(ProjectRole.client, ProjectRole.steeringCommitee);
		assertThat(projectKeyToRoles.get(project2.getKey())).containsExactly(ProjectRole.participant);
		assertThat(sut.getProjectKeyToRoles(List.of(), identity)).isEmpty();
	}
	
	@Test
	public void shouldGetRolesOfProjects() {
		Identity identity = JunitTestHelper.createAndPersistIdentityAsUser(random());
		Identity creator = JunitTestHelper.createAndPersistIdentityAsUser(random());
		ProjProject project1 = projectService.createProject(creator, new ProjectBCFactory(), creator);
		ProjProject project2 = projectService.createProject(creator, new ProjectBCFactory(), creator);
		ProjProject projectNotMember = projectService.createProject(creator, new ProjectBCFactory(), creator);
		projectService.updateMember(creator, new ProjectBCFactory(), project1, identity, Set.of(ProjectRole.client, ProjectRole.supplier), null);
		projectService.updateMember(creator, new ProjectBCFactory(), project2, identity, Set.of(ProjectRole.owner), null);
		dbInstance.commitAndCloseSession();
		
		List<ProjProject> projects = List.of(project1, project2, projectNotMember);
		Map<Long, Set<ProjectRole>> projectKeyToRoles = projectService.getRoles(projects, identity);
		
		// the same roles as the query per project
		for (ProjProject project : projects) {
			assertThat(projectKeyToRoles.getOrDefault(project.getKey(), Set.of()))
					.containsExactlyInAnyOrderElementsOf(projectService.getRoles(project, identity));
		}
		assertThat(projectKeyToRoles.get(project1.getKey())).containsExactlyInAnyOrder(ProjectRole.client, ProjectRole.supplier);
		assertThat(projectKeyToRoles).doesNotContainKey(projectNotMember.getKey());
		assertThat(projectService.getRoles(List.<ProjProject>of(), identity)).isEmpty();
		// the creator is owner of all three projects
		assertThat(projectService.getRoles(projects, creator)).hasSize(3);
	}

}
