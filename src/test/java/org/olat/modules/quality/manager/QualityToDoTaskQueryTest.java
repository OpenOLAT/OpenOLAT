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
package org.olat.modules.quality.manager;

import static org.assertj.core.api.Assertions.assertThat;
import static org.olat.test.JunitTestHelper.random;

import java.util.List;
import java.util.Set;

import org.junit.Test;
import org.olat.core.commons.persistence.DB;
import org.olat.core.id.Identity;
import org.olat.core.id.Organisation;
import org.olat.modules.quality.QualityDataCollection;
import org.olat.modules.todo.ToDoService;
import org.olat.modules.todo.ToDoTask;
import org.olat.modules.todo.ToDoTaskSearchParams;
import org.olat.test.JunitTestHelper;
import org.olat.test.OlatTestCase;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 24 Apr 2023<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class QualityToDoTaskQueryTest extends OlatTestCase {
	
	@Autowired
	private DB dbInstance;
	@Autowired
	private QualityTestHelper qualityTestHelper;
	
	@Autowired
	private ToDoService toDoService;
	
	@Test
	public void shouldGetTodos() {
		Identity identity = JunitTestHelper.createAndPersistIdentityAsUser(random());
		Identity member = JunitTestHelper.createAndPersistIdentityAsUser(random());
		
		// ToDo not in qm
		ToDoTask toDoTask1 = toDoService.createToDoTask(member, random());
		// General qm todo
		ToDoTask toDoTask2 = toDoService.createToDoTask(member, GeneralToDoTaskProvider.TYPE, null, null, null, null);
		// General qm todo (not member)
		ToDoTask toDoTask3 = toDoService.createToDoTask(identity, GeneralToDoTaskProvider.TYPE, null, null, null, null);
	
		// DataCollection ToDo in organisation
		Organisation organisation1 = qualityTestHelper.createOrganisation();
		QualityDataCollection dataCollection1 = qualityTestHelper.createDataCollection(organisation1);
		ToDoTask toDoTask4 = toDoService.createToDoTask(identity, DataCollectionToDoTaskProvider.TYPE, dataCollection1.getKey(), null, null, null);
		// DataCollection ToDo not in organisation
		QualityDataCollection dataCollection2 = qualityTestHelper.createDataCollection();
		ToDoTask toDoTask5 = toDoService.createToDoTask(identity, DataCollectionToDoTaskProvider.TYPE, dataCollection2.getKey(), null, null, null);
		
		// Member
		QualityDataCollection dataCollection3 = qualityTestHelper.createDataCollection();
		ToDoTask toDoTask6 = toDoService.createToDoTask(identity, DataCollectionToDoTaskProvider.TYPE, dataCollection3.getKey(), null, null, null);
		toDoService.updateMember(identity, toDoTask6, List.of(identity), List.of(member));
		
		dbInstance.commitAndCloseSession();
		
		ToDoTaskSearchParams searchParams = new ToDoTaskSearchParams();
		Set<Long> originIds = Set.of(dataCollection1.getKey());
		searchParams.setCustomQuery(new QualityToDoTaskQuery(member, true, originIds));
		List<ToDoTask> toDoTasks = toDoService.getToDoTasks(searchParams);
		
		assertThat(toDoTasks)
				.contains(
						toDoTask2,
						toDoTask3,
						toDoTask4,
						toDoTask6
				).doesNotContain(
						toDoTask1,
						toDoTask5
				);
	}
	
	@Test
	public void shouldGetTodos_excludeGeneralIfNotMaanger() {
		Identity identity = JunitTestHelper.createAndPersistIdentityAsUser(random());
		Identity member = JunitTestHelper.createAndPersistIdentityAsUser(random());
		
		toDoService.createToDoTask(identity, GeneralToDoTaskProvider.TYPE, null, null, null, null);
		dbInstance.commitAndCloseSession();
		
		ToDoTaskSearchParams searchParams = new ToDoTaskSearchParams();
		searchParams.setCustomQuery(new QualityToDoTaskQuery(member, false, List.of()));
		List<ToDoTask> toDoTasks = toDoService.getToDoTasks(searchParams);
		
		assertThat(toDoTasks).isEmpty();
	}

}
