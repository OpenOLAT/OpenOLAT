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

import java.util.Date;
import java.util.List;

import org.junit.Test;
import org.olat.basesecurity.manager.GroupDAO;
import org.olat.core.commons.persistence.DB;
import org.olat.core.id.Identity;
import org.olat.modules.project.ProjArtefact;
import org.olat.modules.project.ProjArtefactSearchParams;
import org.olat.modules.project.ProjProject;
import org.olat.modules.project.ProjectService;
import org.olat.modules.project.ProjectStatus;
import org.olat.test.JunitTestHelper;
import org.olat.test.OlatTestCase;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 5 Jan 2023<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class ProjArtefactDAOTest extends OlatTestCase {
	
	@Autowired
	private DB dbInstance;
	@Autowired
	private GroupDAO groupDao;
	@Autowired
	private ProjectService projectService;
	
	@Autowired
	private ProjArtefactDAO sut;
	
	@Test
	public void shouldCreateArtefact() {
		Identity creator = JunitTestHelper.createAndPersistIdentityAsRndUser(random());
		ProjProject project = projectService.createProject(creator);
		String type = JunitTestHelper.random();
		dbInstance.commitAndCloseSession();
		
		ProjArtefact artefact = sut.create(type, project, creator);
		dbInstance.commitAndCloseSession();
		
		assertThat(artefact).isNotNull();
		assertThat(artefact.getCreationDate()).isNotNull();
		assertThat(artefact.getLastModified()).isNotNull();
		assertThat(artefact.getBaseGroup()).isNotNull();
		assertThat(artefact.getContentModifiedDate()).isNotNull();
		assertThat(artefact.getStatus()).isEqualTo(ProjectStatus.active);
		assertThat(artefact.getCreator()).isEqualTo(creator);
		assertThat(artefact.getProject()).isEqualTo(project);
		assertThat(groupDao.countMembers(artefact.getBaseGroup())).isEqualTo(1);
	}
	
	public void shouldUpdate() {
		ProjArtefact artefact = createRandomArtefact();
		
		artefact.setContentModifiedDate(new Date());
		Identity contentModifiedBy = JunitTestHelper.createAndPersistIdentityAsRndUser(random());
		artefact.setContentModifiedBy(contentModifiedBy);
		artefact.setStatus(ProjectStatus.deleted);
		artefact = sut.save(artefact);
		
		ProjArtefactSearchParams searchParams = new ProjArtefactSearchParams();
		searchParams.setProject(artefact.getProject());
		ProjArtefact reloadedArtefact = sut.loadArtefacts(searchParams).get(0);
		
		assertThat(reloadedArtefact.getContentModifiedBy()).isEqualTo(contentModifiedBy);
		assertThat(reloadedArtefact.getStatus()).isEqualTo(ProjectStatus.deleted);
	}
	
	@Test
	public void shouldDelete() {
		ProjArtefact artefact = createRandomArtefact();
		
		sut.delete(artefact);
		dbInstance.commitAndCloseSession();
		
		ProjArtefactSearchParams searchParams = new ProjArtefactSearchParams();
		searchParams.setProject(artefact.getProject());
		List<ProjArtefact> artefacts = sut.loadArtefacts(searchParams);
		assertThat(artefacts).isEmpty();
	}
	
	@Test
	public void shouldLoad_filter_project() {
		ProjArtefact artefact = createRandomArtefact();
		createRandomArtefact();
		
		ProjArtefactSearchParams searchParams = new ProjArtefactSearchParams();
		searchParams.setProject(artefact.getProject());
		List<ProjArtefact> artefacts = sut.loadArtefacts(searchParams);
		
		assertThat(artefacts).containsExactlyInAnyOrder(artefact);
	}
	
	@Test
	public void shouldLoad_filter_artefacts() {
		ProjArtefact artefact1 = createRandomArtefact();
		ProjArtefact artefact2 = createRandomArtefact();
		createRandomArtefact();
		createRandomArtefact();
		
		ProjArtefactSearchParams searchParams = new ProjArtefactSearchParams();
		searchParams.setArtefacts(List.of(artefact1, artefact2));
		List<ProjArtefact> artefacts = sut.loadArtefacts(searchParams);
		
		assertThat(artefacts).containsExactlyInAnyOrder(artefact1, artefact2);
	}
	
	@Test
	public void shouldLoad_filter_excluded_artefacts() {
		ProjArtefact artefact1 = createRandomArtefact();
		ProjArtefact artefact2 = createRandomArtefact();
		ProjArtefact artefact3 = createRandomArtefact();
		createRandomArtefact();
		
		ProjArtefactSearchParams searchParams = new ProjArtefactSearchParams();
		searchParams.setArtefacts(List.of(artefact1, artefact2, artefact3));
		searchParams.setExcludedArtefacts(List.of(artefact3));
		List<ProjArtefact> artefacts = sut.loadArtefacts(searchParams);
		
		assertThat(artefacts).containsExactlyInAnyOrder(artefact1, artefact2);
	}

	private ProjArtefact createRandomArtefact() {
		Identity creator = JunitTestHelper.createAndPersistIdentityAsRndUser(random());
		ProjProject project = projectService.createProject(creator);
		String type = JunitTestHelper.random();
		ProjArtefact artefact = sut.create(type, project, creator);
		dbInstance.commitAndCloseSession();
		return artefact;
	}

}
