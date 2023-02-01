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

import org.junit.Test;
import org.olat.core.commons.persistence.DB;
import org.olat.core.id.Identity;
import org.olat.modules.project.ProjArtefactToArtefact;
import org.olat.modules.project.ProjArtefactToArtefactSearchParams;
import org.olat.modules.project.ProjNote;
import org.olat.modules.project.ProjProject;
import org.olat.modules.project.ProjectService;
import org.olat.test.JunitTestHelper;
import org.olat.test.OlatTestCase;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 5 Jan 2023<br>>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class ProjArtefactToArtefactDAOTest extends OlatTestCase {

	@Autowired
	private DB dbInstance;
	@Autowired
	private ProjectService projectService;
	
	@Autowired
	private ProjArtefactToArtefactDAO sut;
	
	@Test
	public void shouldCreateArtefactToArtefact() {
		Identity creator = JunitTestHelper.createAndPersistIdentityAsRndUser(random());
		ProjProject project = projectService.createProject(creator);
		ProjNote note1 = projectService.createNote(creator, project);
		ProjNote note2 = projectService.createNote(creator, project);
		dbInstance.commitAndCloseSession();
		
		ProjArtefactToArtefact ata = sut.create(project, creator, note1.getArtefact(), note2.getArtefact());
		dbInstance.commitAndCloseSession();
		
		assertThat(ata).isNotNull();
		assertThat(ata.getCreationDate()).isNotNull();
		assertThat(ata.getProject()).isEqualTo(project);
		assertThat(ata.getCreator()).isEqualTo(creator);
		assertThat(ata.getArtefact1()).isEqualTo(note1.getArtefact());
		assertThat(ata.getArtefact2()).isEqualTo(note2.getArtefact());
	}
	@Test
	public void shouldDeleteAll() {
		Identity creator = JunitTestHelper.createAndPersistIdentityAsRndUser(random());
		ProjProject project = projectService.createProject(creator);
		ProjNote note1 = projectService.createNote(creator, project);
		ProjNote note2 = projectService.createNote(creator, project);
		ProjNote note3 = projectService.createNote(creator, project);
		sut.create(project, creator, note1.getArtefact(), note2.getArtefact());
		sut.create(project, creator, note1.getArtefact(), note3.getArtefact());
		ProjArtefactToArtefact ata23 = sut.create(project, creator, note2.getArtefact(), note3.getArtefact());
		sut.create(project, creator, note2.getArtefact(), note1.getArtefact());
		sut.create(project, creator, note3.getArtefact(), note1.getArtefact());
		ProjArtefactToArtefact ata32 = sut.create(project, creator, note3.getArtefact(), note2.getArtefact());
		dbInstance.commitAndCloseSession();
		
		sut.delete(note1.getArtefact());
		dbInstance.commitAndCloseSession();
		
		ProjArtefactToArtefactSearchParams params = new ProjArtefactToArtefactSearchParams();
		params.setProject(project);
		List<ProjArtefactToArtefact> artefacts = sut.loadArtefactToArtefacts(params);
		
		assertThat(artefacts).containsExactlyInAnyOrder(ata23, ata32);
	}
	
	@Test
	public void shouldDelete() {
		Identity creator = JunitTestHelper.createAndPersistIdentityAsRndUser(random());
		ProjProject project = projectService.createProject(creator);
		ProjNote note1 = projectService.createNote(creator, project);
		ProjNote note2 = projectService.createNote(creator, project);
		sut.create(project, creator, note1.getArtefact(), note2.getArtefact());
		dbInstance.commitAndCloseSession();
		
		ProjArtefactToArtefactSearchParams params = new ProjArtefactToArtefactSearchParams();
		params.setArtefact(note1.getArtefact());
		assertThat(sut.loadArtefactToArtefacts(params)).isNotEmpty();
		
		sut.delete(note1.getArtefact(), note2.getArtefact());
		dbInstance.commitAndCloseSession();
		
		assertThat(sut.loadArtefactToArtefacts(params)).isEmpty();
		
		// Inverted parameters
		sut.create(project, creator, note1.getArtefact(), note2.getArtefact());
		dbInstance.commitAndCloseSession();
		
		assertThat(sut.loadArtefactToArtefacts(params)).isNotEmpty();
		
		sut.delete(note2.getArtefact(), note1.getArtefact());
		dbInstance.commitAndCloseSession();
		
		assertThat(sut.loadArtefactToArtefacts(params)).isEmpty();
	}
	
	@Test
	public void shouldCheckIfExists() {
		Identity creator = JunitTestHelper.createAndPersistIdentityAsRndUser(random());
		ProjProject project = projectService.createProject(creator);
		ProjNote note1 = projectService.createNote(creator, project);
		ProjNote note2 = projectService.createNote(creator, project);
		dbInstance.commitAndCloseSession();
		assertThat(sut.exists(note1.getArtefact(), note2.getArtefact())).isFalse();
		assertThat(sut.exists(note2.getArtefact(), note1.getArtefact())).isFalse();
		
		sut.create(project, creator, note1.getArtefact(), note2.getArtefact());
		dbInstance.commitAndCloseSession();
		assertThat(sut.exists(note1.getArtefact(), note2.getArtefact())).isTrue();
		assertThat(sut.exists(note2.getArtefact(), note1.getArtefact())).isTrue();
	}

	@Test
	public void shouldCount() {
		Identity creator = JunitTestHelper.createAndPersistIdentityAsRndUser(random());
		createRandomArtefactToArtefact(creator);
		createRandomArtefactToArtefact(creator);
		createRandomArtefactToArtefact();
		
		ProjArtefactToArtefactSearchParams params = new ProjArtefactToArtefactSearchParams();
		params.setCreator(creator);
		long count = sut.loadArtefactToArtefactsCount(params);
		
		assertThat(count).isEqualTo(2);
	}
	
	@Test
	public void shouldLoad_filter_project() {
		ProjArtefactToArtefact ata = createRandomArtefactToArtefact();
		createRandomArtefactToArtefact();
		
		ProjArtefactToArtefactSearchParams params = new ProjArtefactToArtefactSearchParams();
		params.setProject(ata.getProject());
		List<ProjArtefactToArtefact> atas = sut.loadArtefactToArtefacts(params);
		
		assertThat(atas).containsExactlyInAnyOrder(ata);
	}
	
	@Test
	public void shouldLoad_filter_creators() {
		Identity creator1 = JunitTestHelper.createAndPersistIdentityAsRndUser(random());
		Identity creator2 = JunitTestHelper.createAndPersistIdentityAsRndUser(random());
		ProjArtefactToArtefact ata1 = createRandomArtefactToArtefact(creator1);
		ProjArtefactToArtefact ata2 = createRandomArtefactToArtefact(creator1);
		createRandomArtefactToArtefact(creator2);
		dbInstance.commitAndCloseSession();
		
		ProjArtefactToArtefactSearchParams params = new ProjArtefactToArtefactSearchParams();
		params.setCreator(creator1);
		List<ProjArtefactToArtefact> atas = sut.loadArtefactToArtefacts(params);
		
		assertThat(atas).containsExactlyInAnyOrder(ata1, ata2);
	}
	
	@Test
	public void shouldLoad_filter_artefacts() {
		Identity creator = JunitTestHelper.createAndPersistIdentityAsRndUser(random());
		ProjProject project = projectService.createProject(creator);
		ProjNote note1 = projectService.createNote(creator, project);
		ProjNote note2 = projectService.createNote(creator, project);
		ProjNote note3 = projectService.createNote(creator, project);
		ProjNote note4 = projectService.createNote(creator, project);
		ProjNote note5 = projectService.createNote(creator, project);
		ProjArtefactToArtefact ata12 = sut.create(project, creator, note1.getArtefact(), note2.getArtefact());
		ProjArtefactToArtefact ata13 = sut.create(project, creator, note1.getArtefact(), note3.getArtefact());
		sut.create(project, creator, note2.getArtefact(), note3.getArtefact());
		ProjArtefactToArtefact ata21 = sut.create(project, creator, note2.getArtefact(), note1.getArtefact());
		ProjArtefactToArtefact ata31 = sut.create(project, creator, note3.getArtefact(), note1.getArtefact());
		sut.create(project, creator, note3.getArtefact(), note2.getArtefact());
		ProjArtefactToArtefact ata45 = sut.create(project, creator, note4.getArtefact(), note5.getArtefact());
		dbInstance.commitAndCloseSession();
		
		ProjArtefactToArtefactSearchParams params = new ProjArtefactToArtefactSearchParams();
		params.setArtefacts(List.of(note1.getArtefact(), note4.getArtefact()));
		List<ProjArtefactToArtefact> atas = sut.loadArtefactToArtefacts(params);
		
		assertThat(atas).containsExactlyInAnyOrder(ata12, ata13, ata21, ata31, ata45);
	}
	
	private ProjArtefactToArtefact createRandomArtefactToArtefact() {
		Identity creator = JunitTestHelper.createAndPersistIdentityAsRndUser(random());
		return createRandomArtefactToArtefact(creator);
	}

	private ProjArtefactToArtefact createRandomArtefactToArtefact(Identity creator) {
		ProjProject project = projectService.createProject(creator);
		ProjNote note1 = projectService.createNote(creator, project);
		ProjNote note2 = projectService.createNote(creator, project);
		ProjArtefactToArtefact ata = sut.create(project, creator, note1.getArtefact(), note2.getArtefact());
		dbInstance.commitAndCloseSession();
		return ata;
	}

}
