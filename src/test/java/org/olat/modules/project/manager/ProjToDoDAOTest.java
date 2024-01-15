/**
 * <a href="http://www.openolat.org">
 * OpenOLAT - Online Learning and Training</a><br>
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); <br>
 * you may not use this toDo except in compliance with the License.<br>
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
import org.olat.modules.project.ProjArtefact;
import org.olat.modules.project.ProjProject;
import org.olat.modules.project.ProjToDo;
import org.olat.modules.project.ProjToDoSearchParams;
import org.olat.modules.project.ProjectService;
import org.olat.modules.project.ProjectStatus;
import org.olat.modules.project.ui.ProjectBCFactory;
import org.olat.modules.todo.ToDoService;
import org.olat.modules.todo.ToDoStatus;
import org.olat.modules.todo.ToDoTask;
import org.olat.test.JunitTestHelper;
import org.olat.test.OlatTestCase;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 27 Mar 2023<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class ProjToDoDAOTest extends OlatTestCase {

	@Autowired
	private DB dbInstance;
	@Autowired
	private ProjectService projectService;
	@Autowired
	private ProjArtefactDAO artefactDao;
	@Autowired
	private ToDoService toDoService;
	
	@Autowired
	private ProjToDoDAO sut;
	
	@Test
	public void shouldCreateToDo() {
		ProjArtefact artefact = createRandomArtefact();
		ToDoTask toDoTask = createRandomToDoTask();
		String identifier = random();
		dbInstance.commitAndCloseSession();
		
		ProjToDo toDo = sut.create(artefact, toDoTask, identifier);
		dbInstance.commitAndCloseSession();
		
		assertThat(toDo).isNotNull();
		assertThat(toDo.getCreationDate()).isNotNull();
		assertThat(toDo.getLastModified()).isNotNull();
		assertThat(toDo.getIdentifier()).isEqualTo(identifier);
		assertThat(toDo.getArtefact()).isEqualTo(artefact);
		assertThat(toDo.getToDoTask()).isEqualTo(toDoTask);
	}
	
	@Test
	public void shouldSaveToDo() {
		ProjArtefact artefact = createRandomArtefact();
		ToDoTask toDoTask = createRandomToDoTask();
		ProjToDo toDo = sut.create(artefact, toDoTask, random());
		dbInstance.commitAndCloseSession();
		
		sut.save(toDo);
		dbInstance.commitAndCloseSession();
		
		// No exception
	}
	
	@Test
	public void shouldDelete() {
		ProjToDo toDo = createRandomToDo();
		
		sut.delete(toDo);
		dbInstance.commitAndCloseSession();
		
		ProjToDoSearchParams searchParams = new ProjToDoSearchParams();
		searchParams.setToDos(List.of(toDo));
		List<ProjToDo> toDos = sut.loadToDos(searchParams);
		assertThat(toDos.isEmpty());
	}
	
	@Test
	public void shouldCount() {
		ProjToDo toDo1 = createRandomToDo();
		ProjToDo toDo2 = createRandomToDo();
		createRandomToDo();
		
		ProjToDoSearchParams params = new ProjToDoSearchParams();
		params.setToDos(List.of(toDo1, toDo2));
		long count = sut.loadToDosCount(params);
		
		assertThat(count).isEqualTo(2);
	}
	
	@Test
	public void shouldLoad_filter_project() {
		ProjToDo toDo = createRandomToDo();
		createRandomToDo();
		
		ProjToDoSearchParams params = new ProjToDoSearchParams();
		params.setProject(toDo.getArtefact().getProject());
		List<ProjToDo> toDos = sut.loadToDos(params);
		
		assertThat(toDos).containsExactlyInAnyOrder(toDo);
	}
	
	@Test
	public void shouldLoad_filter_toDoKeys() {
		ProjToDo toDo1 = createRandomToDo();
		ProjToDo toDo2 = createRandomToDo();
		createRandomToDo();
		
		ProjToDoSearchParams params = new ProjToDoSearchParams();
		params.setToDos(List.of(toDo1, toDo2));
		List<ProjToDo> toDos = sut.loadToDos(params);
		
		assertThat(toDos).containsExactlyInAnyOrder(toDo1, toDo2);
	}
	
	@Test
	public void shouldLoad_filter_identifiers() {
		ProjToDo toDo1 = createRandomToDo();
		ProjToDo toDo2 = createRandomToDo();
		createRandomToDo();
		
		ProjToDoSearchParams params = new ProjToDoSearchParams();
		params.setIdentifiers(List.of(toDo1.getIdentifier(), toDo2.getIdentifier()));
		List<ProjToDo> toDos = sut.loadToDos(params);
		
		assertThat(toDos).containsExactlyInAnyOrder(toDo1, toDo2);
	}
	
	@Test
	public void shouldLoad_filter_artefacts() {
		ProjToDo toDo1 = createRandomToDo();
		ProjToDo toDo2 = createRandomToDo();
		createRandomToDo();
		
		ProjToDoSearchParams params = new ProjToDoSearchParams();
		params.setArtefacts(List.of(toDo1.getArtefact(), toDo2.getArtefact()));
		List<ProjToDo> toDos = sut.loadToDos(params);
		
		assertThat(toDos).containsExactlyInAnyOrder(toDo1, toDo2);
	}
	
	@Test
	public void shouldLoad_filter_status() {
		Identity doer = JunitTestHelper.createAndPersistIdentityAsRndUser(random());
		ProjProject project = projectService.createProject(doer, new ProjectBCFactory(), doer);
		ProjToDo toDo1 = projectService.createToDo(doer, project);
		projectService.deleteToDoSoftly(toDo1.getArtefact().getCreator(), toDo1);
		ProjToDo toDo2 =projectService.createToDo(doer, project);
		projectService.deleteToDoSoftly(toDo2.getArtefact().getCreator(), toDo2);
		ProjToDo toDo3 = projectService.createToDo(doer, project);
		
		ProjToDoSearchParams params = new ProjToDoSearchParams();
		params.setToDos(List.of(toDo1, toDo2, toDo3));
		params.setStatus(List.of(ProjectStatus.deleted));
		List<ProjToDo> toDos = sut.loadToDos(params);
		
		assertThat(toDos).containsExactlyInAnyOrder(toDo1, toDo2);
	}
	
	@Test
	public void shouldLoad_filter_doers() {
		Identity doer1 = JunitTestHelper.createAndPersistIdentityAsRndUser(random());
		Identity doer2 = JunitTestHelper.createAndPersistIdentityAsRndUser(random());
		Identity doer3 = JunitTestHelper.createAndPersistIdentityAsRndUser(random());
		ProjToDo toDo1 = createRandomToDo(doer1);
		ProjToDo toDo2 = createRandomToDo(doer2);
		createRandomToDo(doer3);
		dbInstance.commitAndCloseSession();
		dbInstance.commitAndCloseSession();
		
		ProjToDoSearchParams params = new ProjToDoSearchParams();
		params.setCreators(List.of(doer1, doer2));
		List<ProjToDo> toDos = sut.loadToDos(params);
		
		assertThat(toDos).containsExactlyInAnyOrder(toDo1, toDo2);
	}
	
	@Test
	public void shouldLoad_filter_createdAfter() {
		ProjToDo toDo1 = createRandomToDo();
		
		ProjToDoSearchParams params = new ProjToDoSearchParams();
		params.setToDos(List.of(toDo1));
		params.setCreatedAfter(new Date());
		sut.loadToDos(params);
		
		// Just syntax check because created date can't be modified.
	}
	
	@Test
	public void shouldLoad_filter_lastModified() {
		ProjToDo toDo1 = createRandomToDo();
		ProjToDo toDo2 = createRandomToDo();
		ProjToDo toDo3 = createRandomToDo();
		
		ProjToDoSearchParams params = new ProjToDoSearchParams();
		params.setToDos(List.of(toDo1, toDo2, toDo3));
		params.setNumLastModified(2);
		List<ProjToDo> toDos = sut.loadToDos(params);
		
		assertThat(toDos).containsExactlyInAnyOrder(toDo2, toDo3);
	}
	
	@Test
	public void shouldLoad_filter_todotask_status() {
		Identity doer = JunitTestHelper.createAndPersistIdentityAsRndUser(random());
		ProjProject project = projectService.createProject(doer, new ProjectBCFactory(), doer);
		ProjToDo toDo1 = projectService.createToDo(doer, project);
		ToDoTask toDoTask1 = toDo1.getToDoTask();
		toDoTask1.setStatus(ToDoStatus.done);
		toDoService.update(doer, toDoTask1, ToDoStatus.open);
		ProjToDo toDo2 = projectService.createToDo(doer, project);
		ToDoTask toDoTask2 = toDo2.getToDoTask();
		toDoTask2.setStatus(ToDoStatus.done);
		toDoService.update(doer, toDoTask2, ToDoStatus.open);
		ProjToDo toDo3 = projectService.createToDo(doer, project);
		ToDoTask toDoTask3 = toDo3.getToDoTask();
		toDoTask3.setStatus(ToDoStatus.deleted);
		toDoService.update(doer, toDoTask3, ToDoStatus.open);
		ProjToDo toDo4 = projectService.createToDo(doer, project);
		ToDoTask toDoTask4 = toDo4.getToDoTask();
		toDoTask4.setStatus(ToDoStatus.open);
		toDoService.update(doer, toDoTask4, ToDoStatus.open);
		dbInstance.commitAndCloseSession();
		
		ProjToDoSearchParams params = new ProjToDoSearchParams();
		params.setToDos(List.of(toDo1, toDo2, toDo3, toDo4));
		params.setToDoStatus(List.of(ToDoStatus.done, ToDoStatus.deleted));
		List<ProjToDo> toDos = sut.loadToDos(params);
		
		assertThat(toDos).containsExactlyInAnyOrder(toDo1, toDo2, toDo3);
	}
	
	@Test
	public void shouldLoad_filter_duedate_null() {
		Identity doer = JunitTestHelper.createAndPersistIdentityAsRndUser(random());
		ProjProject project = projectService.createProject(doer, new ProjectBCFactory(), doer);
		ProjToDo toDo1 = projectService.createToDo(doer, project);
		ProjToDo toDo2 = projectService.createToDo(doer, project);
		ProjToDo toDo3 = projectService.createToDo(doer, project);
		ToDoTask toDoTask3 = toDo3.getToDoTask();
		toDoTask3.setDueDate(new Date());
		toDoService.update(doer, toDoTask3, ToDoStatus.open);
		dbInstance.commitAndCloseSession();
		
		ProjToDoSearchParams params = new ProjToDoSearchParams();
		params.setToDos(List.of(toDo1, toDo2, toDo3));
		params.setDueDateNull(Boolean.TRUE);
		assertThat(sut.loadToDos(params)).containsExactlyInAnyOrder(toDo1, toDo2);
		
		params.setDueDateNull(Boolean.FALSE);
		assertThat(sut.loadToDos(params)).containsExactlyInAnyOrder(toDo3);
	}
	
	private ProjToDo createRandomToDo() {
		Identity doer = JunitTestHelper.createAndPersistIdentityAsRndUser(random());
		return createRandomToDo(doer);
	}
	
	private ProjToDo createRandomToDo(Identity doer) {
		ProjArtefact artefact = createRandomArtefact(doer);
		ToDoTask toDoTask = createRandomToDoTask(doer);
		ProjToDo toDo = sut.create(artefact, toDoTask, random());
		dbInstance.commitAndCloseSession();
	
		return toDo;
	}

	private ProjArtefact createRandomArtefact() {
		Identity doer = JunitTestHelper.createAndPersistIdentityAsRndUser(random());
		return createRandomArtefact(doer);
	}
	
	private ProjArtefact createRandomArtefact(Identity doer) {
		ProjProject project = projectService.createProject(doer, new ProjectBCFactory(), doer);
		ProjArtefact artefact = artefactDao.create(ProjToDo.TYPE, project, doer);
		return artefact;
	}

	private ToDoTask createRandomToDoTask() {
		Identity doer = JunitTestHelper.createAndPersistIdentityAsRndUser(random());
		return createRandomToDoTask(doer);
	}
	
	private ToDoTask createRandomToDoTask(Identity doer) {
		return toDoService.createToDoTask(doer, random());
	}

}
