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

import static org.assertj.core.api.Assertions.assertThat;
import static org.olat.test.JunitTestHelper.random;

import java.io.InputStream;
import java.util.Date;
import java.util.List;

import org.junit.Test;
import org.olat.core.commons.persistence.DB;
import org.olat.core.commons.services.tag.TagInfo;
import org.olat.core.id.Identity;
import org.olat.modules.project.manager.ProjArtefactToArtefactDAO;
import org.olat.modules.todo.ToDoPriority;
import org.olat.modules.todo.ToDoService;
import org.olat.modules.todo.ToDoStatus;
import org.olat.modules.todo.ToDoTask;
import org.olat.modules.todo.ToDoTaskSearchParams;
import org.olat.test.JunitTestHelper;
import org.olat.test.OlatTestCase;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 11 May 2023<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class ProjectCopyServiceTest extends OlatTestCase {
	
	@Autowired
	private DB dbInstance;
	@Autowired
	private ProjectService projectService;
	@Autowired
	private ProjArtefactToArtefactDAO artefactToArtefactDao;
	@Autowired
	private ToDoService toDoService;
	
	@Autowired
	private ProjectCopyService sut;

	@Test
	public void shouldCopyArtefacts_excludeDeleted() {
		Identity doer = JunitTestHelper.createAndPersistIdentityAsUser(random());
		ProjProject project = projectService.createProject(doer, doer);
		ProjNote note1 = projectService.createNote(doer, project);
		projectService.updateNote(doer, note1, null, "1", null);
		ProjNote note2 = projectService.createNote(doer, project);
		projectService.updateNote(doer, note2, null, "2", null);
		projectService.deleteNoteSoftly(doer, note2);
		ProjNote note3 = projectService.createNote(doer, project);
		projectService.updateNote(doer, note3, null, "3", null);
		dbInstance.commitAndCloseSession();
		
		ProjProject projectCopy = projectService.createProject(doer, doer);
		sut.copyProjectArtefacts(doer, project, projectCopy);
		dbInstance.commitAndCloseSession();
		
		ProjNoteSearchParams searchParams = new ProjNoteSearchParams();
		searchParams.setProject(projectCopy);
		List<ProjNote> noteCopies = projectService.getNotes(searchParams);
		assertThat(noteCopies).extracting(ProjNote::getTitle).containsExactlyInAnyOrder("1", "3");
	}

	@SuppressWarnings("null")
	@Test
	public void shouldCopyArtefacts_initFile() {
		Identity doer = JunitTestHelper.createAndPersistIdentityAsUser(random());
		ProjProject project = projectService.createProject(doer, doer);
		ProjFile file = null;
		try (InputStream is = JunitTestHelper.class.getResourceAsStream("file_resources/page.html")) {
			file = projectService.createFile(doer, project, random(), is, false);
		} catch (Exception fnfe) {
			//
		}
		String filename = random();
		String title = random();
		String description = random();
		projectService.updateFile(doer, file, filename, title, description);
		String tag1 = random();
		String tag2 = random();
		projectService.updateTags(doer, file.getArtefact(), List.of(tag1, tag2));
		dbInstance.commitAndCloseSession();
		
		ProjProject projectCopy = projectService.createProject(doer, doer);
		sut.copyProjectArtefacts(doer, project, projectCopy);
		dbInstance.commitAndCloseSession();
		
		ProjFileSearchParams searchParams = new ProjFileSearchParams();
		searchParams.setProject(projectCopy);
		List<ProjFile> fileCopies = projectService.getFiles(searchParams);
		assertThat(fileCopies).hasSize(1);
		ProjFile fileCopy = fileCopies.get(0);
		assertThat(fileCopy.getVfsMetadata().getFilename()).isEqualTo(filename);
		assertThat(fileCopy.getVfsMetadata().getTitle()).isEqualTo(title);
		assertThat(fileCopy.getVfsMetadata().getComment()).isEqualTo(description);
		List<TagInfo> tagInfos = projectService.getTagInfos(projectCopy, fileCopy.getArtefact());
		assertThat(tagInfos).extracting(TagInfo::getDisplayName).containsExactlyInAnyOrder(tag1, tag2);
		assertThat(tagInfos).extracting(TagInfo::isSelected).containsExactlyInAnyOrder(Boolean.TRUE, Boolean.TRUE);
	}

	@Test
	public void shouldCopyArtefacts_initNote() {
		Identity doer = JunitTestHelper.createAndPersistIdentityAsUser(random());
		ProjProject project = projectService.createProject(doer, doer);
		ProjNote note = projectService.createNote(doer, project);
		String title = random();
		String text = random();
		projectService.updateNote(doer, note, null, title, text);
		String tag1 = random();
		String tag2 = random();
		projectService.updateTags(doer, note.getArtefact(), List.of(tag1, tag2));
		dbInstance.commitAndCloseSession();
		
		ProjProject projectCopy = projectService.createProject(doer, doer);
		sut.copyProjectArtefacts(doer, project, projectCopy);
		dbInstance.commitAndCloseSession();
		
		ProjNoteSearchParams searchParams = new ProjNoteSearchParams();
		searchParams.setProject(projectCopy);
		List<ProjNote> noteCopies = projectService.getNotes(searchParams);
		assertThat(noteCopies).hasSize(1);
		ProjNote noteCopy = noteCopies.get(0);
		assertThat(noteCopy.getTitle()).isEqualTo(title);
		assertThat(noteCopy.getText()).isEqualTo(text);
		List<TagInfo> tagInfos = projectService.getTagInfos(projectCopy, noteCopy.getArtefact());
		assertThat(tagInfos).extracting(TagInfo::getDisplayName).containsExactlyInAnyOrder(tag1, tag2);
		assertThat(tagInfos).extracting(TagInfo::isSelected).containsExactlyInAnyOrder(Boolean.TRUE, Boolean.TRUE);
	}

	@Test
	public void shouldCopyArtefacts_initToDo() {		Identity doer = JunitTestHelper.createAndPersistIdentityAsUser(random());
		ProjProject project = projectService.createProject(doer, doer);
		ProjToDo toDo = projectService.createToDo(doer, project);
		String title = random();
		ToDoStatus status = ToDoStatus.done;
		ToDoPriority priority = ToDoPriority.high;
		Date startDate = new Date();
		Date dueDate = new Date();
		Long expenditureOfWork = Long.valueOf(3);
		String description = random();
		projectService.updateToDo(doer, toDo, title, status, priority, startDate, dueDate, expenditureOfWork, description);
		String tag1 = random();
		String tag2 = random();
		projectService.updateTags(doer, toDo, List.of(tag1, tag2));
		dbInstance.commitAndCloseSession();
		
		ProjProject projectCopy = projectService.createProject(doer, doer);
		sut.copyProjectArtefacts(doer, project, projectCopy);
		dbInstance.commitAndCloseSession();
		
		ProjToDoSearchParams searchParams = new ProjToDoSearchParams();
		searchParams.setProject(projectCopy);
		List<ProjToDo> toDoCopies = projectService.getToDos(searchParams);
		assertThat(toDoCopies).hasSize(1);
		ProjToDo toDoCopy = toDoCopies.get(0);
		ToDoTask toDoTaskCopy = toDoCopy.getToDoTask();
		assertThat(toDoTaskCopy.getTitle()).isEqualTo(title);
		assertThat(toDoTaskCopy.getStatus()).isEqualTo(ToDoStatus.open);
		assertThat(toDoTaskCopy.getPriority()).isEqualTo(priority);
		assertThat(toDoTaskCopy.getStartDate()).isNull();
		assertThat(toDoTaskCopy.getDueDate()).isNull();
		assertThat(toDoTaskCopy.getDoneDate()).isNull();
		assertThat(toDoTaskCopy.getExpenditureOfWork()).isEqualTo(expenditureOfWork);
		assertThat(toDoTaskCopy.getDescription()).isEqualTo(description);
		List<TagInfo> tagInfos = projectService.getTagInfos(projectCopy, toDoCopy.getArtefact());
		assertThat(tagInfos).extracting(TagInfo::getDisplayName).containsExactlyInAnyOrder(tag1, tag2);
		assertThat(tagInfos).extracting(TagInfo::isSelected).containsExactlyInAnyOrder(Boolean.TRUE, Boolean.TRUE);
		ToDoTaskSearchParams toDoTaskSearchParams = new ToDoTaskSearchParams();
		toDoTaskSearchParams.setToDoTasks(List.of(toDoTaskCopy));
		List<TagInfo> taskTagInfos = toDoService.getTagInfos(toDoTaskSearchParams, toDoTaskCopy);
		assertThat(taskTagInfos).extracting(TagInfo::getDisplayName).containsExactlyInAnyOrder(tag1, tag2);
		assertThat(taskTagInfos).extracting(TagInfo::isSelected).containsExactlyInAnyOrder(Boolean.TRUE, Boolean.TRUE);
	}

	@Test
	public void shouldCopyArtefacts_copyReferences() {
		Identity doer = JunitTestHelper.createAndPersistIdentityAsUser(random());
		ProjProject project = projectService.createProject(doer, doer);
		ProjNote note1 = projectService.createNote(doer, project);
		projectService.updateNote(doer, note1, null, "1", null);
		ProjNote note2 = projectService.createNote(doer, project);
		projectService.updateNote(doer, note2, null, "2", null);
		projectService.deleteNoteSoftly(doer, note2);
		ProjNote note3 = projectService.createNote(doer, project);
		projectService.updateNote(doer, note2, null, "2", null);
		ProjAppointment appointment = projectService.createAppointment(doer, project, new Date());
		projectService.linkArtefacts(doer, note1.getArtefact(), appointment.getArtefact());
		projectService.linkArtefacts(doer, note1.getArtefact(), note2.getArtefact());
		projectService.linkArtefacts(doer, note1.getArtefact(), note3.getArtefact());
		projectService.linkArtefacts(doer, note2.getArtefact(), note3.getArtefact());
		dbInstance.commitAndCloseSession();
		
		ProjProject projectCopy = projectService.createProject(doer, doer);
		sut.copyProjectArtefacts(doer, project, projectCopy);
		dbInstance.commitAndCloseSession();
		
		ProjArtefactToArtefactSearchParams searchParams = new ProjArtefactToArtefactSearchParams();
		searchParams.setProject(projectCopy);
		List<ProjArtefactToArtefact> ataCopies = artefactToArtefactDao.loadArtefactToArtefacts(searchParams);
		assertThat(ataCopies).hasSize(1);
	}

}
