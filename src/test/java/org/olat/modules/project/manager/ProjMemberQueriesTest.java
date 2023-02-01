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
import java.util.Set;

import org.junit.Test;
import org.olat.basesecurity.GroupMembership;
import org.olat.core.commons.persistence.DB;
import org.olat.core.id.Identity;
import org.olat.modules.project.ProjMemberInfoSearchParameters;
import org.olat.modules.project.ProjProject;
import org.olat.modules.project.ProjectRole;
import org.olat.modules.project.ProjectService;
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
		ProjProject project = projectService.createProject(creator);
		dbInstance.commitAndCloseSession();
		assertThat(sut.isProjectMember(idenity)).isFalse();
		
		projectService.updateMember(project.getCreator(), project, idenity, Set.of(ProjectRole.client));
		dbInstance.commitAndCloseSession();
		assertThat(sut.isProjectMember(idenity)).isTrue();
	}
	
	@Test
	public void shouldGetMemberships() {
		Identity idenity = JunitTestHelper.createAndPersistIdentityAsUser(random());
		Identity creator = JunitTestHelper.createAndPersistIdentityAsUser(random());
		ProjProject project = projectService.createProject(creator);
		projectService.updateMember(creator, project, creator, Set.of(ProjectRole.client, ProjectRole.steeringCommitee));
		projectService.updateMember(creator, project, idenity, Set.of(ProjectRole.client, ProjectRole.steeringCommitee));
		dbInstance.commitAndCloseSession();
		
		ProjMemberInfoSearchParameters params = new ProjMemberInfoSearchParameters();
		params.setProject(project);
		List<GroupMembership> memberships = sut.getProjMemberships(params);
		
		assertThat(memberships).hasSize(4);
	}

}
