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
import org.olat.core.commons.persistence.DB;
import org.olat.core.id.Identity;
import org.olat.core.util.DateUtils;
import org.olat.modules.project.ProjArtefact;
import org.olat.modules.project.ProjNote;
import org.olat.modules.project.ProjNoteSearchParams;
import org.olat.modules.project.ProjProject;
import org.olat.modules.project.ProjectService;
import org.olat.modules.project.ProjectStatus;
import org.olat.test.JunitTestHelper;
import org.olat.test.OlatTestCase;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 12 Dez 2022<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class ProjNoteDAOTest extends OlatTestCase {

	@Autowired
	private DB dbInstance;
	@Autowired
	private ProjArtefactDAO artefactDao;
	@Autowired
	private ProjectService projectService;
	
	@Autowired
	private ProjNoteDAO sut;
	
	@Test
	public void shouldCreateNote() {
		Identity creator = JunitTestHelper.createAndPersistIdentityAsRndUser(random());
		ProjProject project = projectService.createProject(creator);
		ProjArtefact artefact = artefactDao.create(ProjNote.TYPE, project, creator);
		dbInstance.commitAndCloseSession();
		
		ProjNote note = sut.create(artefact);
		dbInstance.commitAndCloseSession();
		
		assertThat(note).isNotNull();
		assertThat(note.getCreationDate()).isNotNull();
		assertThat(note.getLastModified()).isNotNull();
		assertThat(note.getArtefact()).isEqualTo(artefact);
	}
	
	@Test
	public void shouldSaveNote() {
		ProjNote note = createRandomNote();
		dbInstance.commitAndCloseSession();
		
		String title = random();
		note.setTitle(title);
		String text = random();
		note.setText(text);
		sut.save(note);
		dbInstance.commitAndCloseSession();
		
		ProjNoteSearchParams params = new ProjNoteSearchParams();
		params.setNotes(List.of(note));
		note = sut.loadNotes(params).get(0);
		
		assertThat(note.getTitle()).isEqualTo(title);
		assertThat(note.getText()).isEqualTo(text);
	}
	
	@Test
	public void shouldDelete() {
		ProjNote note = createRandomNote();
		
		sut.delete(note);
		dbInstance.commitAndCloseSession();
		
		ProjNoteSearchParams searchParams = new ProjNoteSearchParams();
		searchParams.setProject(note.getArtefact().getProject());
		List<ProjNote> notes = sut.loadNotes(searchParams);
		assertThat(notes).isEmpty();
	}
	
	@Test
	public void shouldCount() {
		ProjNote note1 = createRandomNote();
		ProjNote note2 = createRandomNote();
		createRandomNote();
		
		ProjNoteSearchParams params = new ProjNoteSearchParams();
		params.setNotes(List.of(note1, note2));
		long count = sut.loadNotesCount(params);
		
		assertThat(count).isEqualTo(2);
	}
	
	@Test
	public void shouldLoad_filter_project() {
		ProjNote note = createRandomNote();
		createRandomNote();
		
		ProjNoteSearchParams params = new ProjNoteSearchParams();
		params.setProject(note.getArtefact().getProject());
		List<ProjNote> notes = sut.loadNotes(params);
		
		assertThat(notes).containsExactlyInAnyOrder(note);
	}
	
	@Test
	public void shouldLoad_filter_noteKeys() {
		ProjNote note1 = createRandomNote();
		ProjNote note2 = createRandomNote();
		createRandomNote();
		
		ProjNoteSearchParams params = new ProjNoteSearchParams();
		params.setNotes(List.of(note1, note2));
		List<ProjNote> notes = sut.loadNotes(params);
		
		assertThat(notes).containsExactlyInAnyOrder(note1, note2);
	}
	
	@Test
	public void shouldLoad_filter_artefacts() {
		ProjNote note1 = createRandomNote();
		ProjNote note2 = createRandomNote();
		createRandomNote();
		
		ProjNoteSearchParams params = new ProjNoteSearchParams();
		params.setArtefacts(List.of(note1.getArtefact(), note2.getArtefact()));
		List<ProjNote> notes = sut.loadNotes(params);
		
		assertThat(notes).containsExactlyInAnyOrder(note1, note2);
	}
	
	@Test
	public void shouldLoad_filter_status() {
		ProjNote note1 = createRandomNote();
		projectService.deleteNoteSoftly(note1.getArtefact().getCreator(), note1);
		ProjNote note2 = createRandomNote();
		projectService.deleteNoteSoftly(note2.getArtefact().getCreator(), note2);
		ProjNote note3 = createRandomNote();
		
		ProjNoteSearchParams params = new ProjNoteSearchParams();
		params.setNotes(List.of(note1, note2, note3));
		params.setStatus(List.of(ProjectStatus.deleted));
		List<ProjNote> notes = sut.loadNotes(params);
		
		assertThat(notes).containsExactlyInAnyOrder(note1, note2);
	}
	
	@Test
	public void shouldLoad_filter_creators() {
		Identity creator1 = JunitTestHelper.createAndPersistIdentityAsRndUser(random());
		Identity creator2 = JunitTestHelper.createAndPersistIdentityAsRndUser(random());
		Identity creator3 = JunitTestHelper.createAndPersistIdentityAsRndUser(random());
		ProjNote note1 = createRandomNote(creator1);
		ProjNote note2 = createRandomNote(creator2);
		createRandomNote(creator3);
		dbInstance.commitAndCloseSession();
		
		ProjNoteSearchParams params = new ProjNoteSearchParams();
		params.setCreators(List.of(creator1, creator2));
		List<ProjNote> notes = sut.loadNotes(params);
		
		assertThat(notes).containsExactlyInAnyOrder(note1, note2);
	}
	
	@Test
	public void shouldLoad_filter_createdAfter() {
		ProjNote note1 = createRandomNote();
		
		ProjNoteSearchParams params = new ProjNoteSearchParams();
		params.setNotes(List.of(note1));
		params.setCreatedAfter(new Date());
		sut.loadNotes(params);
		
		// Just syntax check because created date can't be modified.
	}
	
	@Test
	public void shouldLoad_numLastModified() {
		Date now = new Date();
		Identity creator = JunitTestHelper.createAndPersistIdentityAsRndUser(random());
		ProjNote note1 = createRandomNote(creator);
		ProjArtefact artefact1 = note1.getArtefact();
		artefact1.setContentModifiedDate(DateUtils.addDays(now, 3));
		artefactDao.save(artefact1);
		ProjNote note2 = createRandomNote(creator);
		ProjArtefact artefact2 = note2.getArtefact();
		artefact2.setContentModifiedDate(DateUtils.addDays(now, 2));
		artefactDao.save(artefact2);
		ProjNote note3 = createRandomNote(creator);
		ProjArtefact artefact3 = note3.getArtefact();
		artefact3.setContentModifiedDate(DateUtils.addDays(now, 4));
		artefactDao.save(artefact3);
		ProjNote note4 = createRandomNote(creator);
		ProjArtefact artefact4 = note4.getArtefact();
		artefact4.setContentModifiedDate(DateUtils.addDays(now, 1));
		artefactDao.save(artefact4);
		dbInstance.commitAndCloseSession();
		
		ProjNoteSearchParams params = new ProjNoteSearchParams();
		params.setCreators(List.of(creator));
		params.setNumLastModified(3);
		List<ProjNote> notes = sut.loadNotes(params);
		
		assertThat(notes).containsExactly(note3, note1, note2);
	}
	
	private ProjNote createRandomNote() {
		Identity creator = JunitTestHelper.createAndPersistIdentityAsRndUser(random());
		return createRandomNote(creator);
	}

	private ProjNote createRandomNote(Identity creator) {
		ProjProject project = projectService.createProject(creator);
		ProjArtefact artefact = artefactDao.create(ProjNote.TYPE, project, creator);
		ProjNote note = sut.create(artefact);
		dbInstance.commitAndCloseSession();
		return note;
	}

}
