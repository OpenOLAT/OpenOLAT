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

import org.junit.Test;
import org.olat.core.commons.persistence.DB;
import org.olat.core.id.Identity;
import org.olat.modules.quality.QualityAuditLog;
import org.olat.modules.quality.QualityAuditLogSearchParams;
import org.olat.modules.quality.QualityDataCollection;
import org.olat.modules.todo.ToDoService;
import org.olat.modules.todo.ToDoTask;
import org.olat.test.JunitTestHelper;
import org.olat.test.OlatTestCase;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 19 Apr 2023<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class QualityAuditLogDAOTest extends OlatTestCase {
	
	@Autowired
	private DB dbInstance;
	@Autowired
	private QualityTestHelper qualityTestHelper;
	@Autowired
	private ToDoService toDoService;
	
	@Autowired
	private QualityAuditLogDAO sut;

	@Test
	public void shouldCreate() {
		Identity doer = JunitTestHelper.createAndPersistIdentityAsRndUser(random());
		Identity identity = JunitTestHelper.createAndPersistIdentityAsRndUser(random());
		QualityDataCollection dataCollection = qualityTestHelper.createDataCollection();
		ToDoTask toDoTask = toDoService.createToDoTask(doer, random());
		dbInstance.commitAndCloseSession();
		
		QualityAuditLog.Action action = QualityAuditLog.Action.toDoCreate;
		String before = random();
		String after = random();
		QualityAuditLog activity = sut.create(action, before, after, doer, dataCollection, toDoTask, identity);
		dbInstance.commitAndCloseSession();
		
		assertThat(activity.getKey()).isNotNull();
		assertThat(activity.getCreationDate()).isNotNull();
		assertThat(activity.getAction()).isEqualTo(action);
		assertThat(activity.getBefore()).isEqualTo(before);
		assertThat(activity.getAfter()).isEqualTo(after);
		assertThat(activity.getDoer()).isEqualTo(doer);
		assertThat(activity.getDataCollection()).isEqualTo(dataCollection);
		assertThat(activity.getToDoTask()).isEqualTo(toDoTask);
		assertThat(activity.getIdentity()).isEqualTo(identity);
	}
	
	@Test
	public void shouldDelete() {
		Identity doer = JunitTestHelper.createAndPersistIdentityAsRndUser(random());
		QualityDataCollection dataCollection = qualityTestHelper.createDataCollection();
		QualityAuditLog activity1 = sut.create(QualityAuditLog.Action.toDoCreate, null, null, doer, dataCollection);
		QualityAuditLog activity2 = sut.create(QualityAuditLog.Action.toDoCreate, null, null, doer, dataCollection);
		QualityAuditLog activity3 = sut.create(QualityAuditLog.Action.toDoCreate, null, null, doer, dataCollection);
		dbInstance.commitAndCloseSession();
		
		sut.delete(List.of(activity1, activity2));
		dbInstance.commitAndCloseSession();
		
		QualityAuditLogSearchParams searchParams = new QualityAuditLogSearchParams();
		searchParams.setDataCollection(dataCollection);
		List<QualityAuditLog> activities = sut.loadAuditLogs(searchParams, 0, -1);
		assertThat(activities).containsExactlyInAnyOrder(activity3);
	}
	
	@Test
	public void shouldLoadDoers() {
		Identity doer1 = JunitTestHelper.createAndPersistIdentityAsRndUser(random());
		Identity doer2 = JunitTestHelper.createAndPersistIdentityAsRndUser(random());
		Identity doer3 = JunitTestHelper.createAndPersistIdentityAsRndUser(random());
		QualityDataCollection dataCollectio1 = qualityTestHelper.createDataCollection();
		QualityDataCollection dataCollectio2 = qualityTestHelper.createDataCollection();
		sut.create(QualityAuditLog.Action.toDoCreate, null, null, doer1, dataCollectio1);
		sut.create(QualityAuditLog.Action.toDoContentUpdate, null, null, doer1, dataCollectio1);
		sut.create(QualityAuditLog.Action.toDoCreate, null, null, doer2, dataCollectio1);
		sut.create(QualityAuditLog.Action.toDoMemberAdd, null, null, doer3, dataCollectio2);
		dbInstance.commitAndCloseSession();
		
		QualityAuditLogSearchParams searchParams = new QualityAuditLogSearchParams();
		searchParams.setDataCollection(dataCollectio1);
		List<Identity> doers = sut.loadAuditLogDoers(searchParams);
		
		assertThat(doers).hasSize(2).containsExactlyInAnyOrder(doer1, doer2);
	}
	
	@Test
	public void shouldLoad_filter_actions() {
		Identity doer = JunitTestHelper.createAndPersistIdentityAsRndUser(random());
		QualityDataCollection dataCollection = qualityTestHelper.createDataCollection();
		QualityAuditLog activity1 = sut.create(QualityAuditLog.Action.toDoCreate, null, null, doer, dataCollection);
		QualityAuditLog activity2 = sut.create(QualityAuditLog.Action.toDoContentUpdate, null, null, doer, dataCollection);
		sut.create(QualityAuditLog.Action.toDoMemberAdd, null, null, doer, dataCollection);
		dbInstance.commitAndCloseSession();
		
		QualityAuditLogSearchParams searchParams = new QualityAuditLogSearchParams();
		searchParams.setDataCollection(dataCollection);
		searchParams.setActions(List.of(QualityAuditLog.Action.toDoCreate, QualityAuditLog.Action.toDoContentUpdate));
		List<QualityAuditLog> activities = sut.loadAuditLogs(searchParams, 0, -1);
		
		assertThat(activities).containsExactlyInAnyOrder(activity1, activity2);
	}
	
	@Test
	public void shouldLoad_filter_doers() {
		Identity doer1 = JunitTestHelper.createAndPersistIdentityAsRndUser(random());
		Identity doer2 = JunitTestHelper.createAndPersistIdentityAsRndUser(random());
		QualityDataCollection dataCollection = qualityTestHelper.createDataCollection();
		QualityAuditLog activity1 = sut.create(QualityAuditLog.Action.toDoCreate, null, null, doer1, dataCollection);
		QualityAuditLog activity2 = sut.create(QualityAuditLog.Action.toDoContentUpdate, null, null, doer1, dataCollection);
		sut.create(QualityAuditLog.Action.toDoContentUpdate, null, null, doer2, dataCollection);
		dbInstance.commitAndCloseSession();
		
		QualityAuditLogSearchParams searchParams = new QualityAuditLogSearchParams();
		searchParams.setDoer(doer1);
		List<QualityAuditLog> activities = sut.loadAuditLogs(searchParams, 0, -1);
		
		assertThat(activities).containsExactlyInAnyOrder(activity1, activity2);
	}
	
	@Test
	public void shouldLoad_filter_dataCollections() {
		Identity doer = JunitTestHelper.createAndPersistIdentityAsRndUser(random());
		QualityDataCollection dataCollection1 = qualityTestHelper.createDataCollection();
		QualityDataCollection dataCollection2 = qualityTestHelper.createDataCollection();
		QualityDataCollection dataCollection3 = qualityTestHelper.createDataCollection();
		QualityAuditLog activity1 = sut.create(QualityAuditLog.Action.toDoCreate, null, null, doer, dataCollection1);
		QualityAuditLog activity2 = sut.create(QualityAuditLog.Action.toDoContentUpdate, null, null, doer, dataCollection2);
		sut.create(QualityAuditLog.Action.toDoContentUpdate, null, null, doer, dataCollection3);
		dbInstance.commitAndCloseSession();
		
		QualityAuditLogSearchParams searchParams = new QualityAuditLogSearchParams();
		searchParams.setDataCollections(List.of(dataCollection1, dataCollection2));
		List<QualityAuditLog> activities = sut.loadAuditLogs(searchParams, 0, -1);
		
		assertThat(activities).containsExactlyInAnyOrder(activity1, activity2);
	}
	
	@Test
	public void shouldLoad_filter_toDoTasks() {
		Identity doer = JunitTestHelper.createAndPersistIdentityAsRndUser(random());
		QualityDataCollection dataCollection = qualityTestHelper.createDataCollection();
		ToDoTask toDoTask1 = toDoService.createToDoTask(doer, random());
		ToDoTask toDoTask2 = toDoService.createToDoTask(doer, random());
		ToDoTask toDoTask3 = toDoService.createToDoTask(doer, random());
		QualityAuditLog activity1 = sut.create(QualityAuditLog.Action.toDoCreate, null, null, doer, dataCollection, toDoTask1, null);
		QualityAuditLog activity2 = sut.create(QualityAuditLog.Action.toDoCreate, null, null, doer, dataCollection, toDoTask2, null);
		sut.create(QualityAuditLog.Action.toDoCreate, null, null, doer, dataCollection, toDoTask3, null);
		dbInstance.commitAndCloseSession();
		
		QualityAuditLogSearchParams searchParams = new QualityAuditLogSearchParams();
		searchParams.setToDoTasks(List.of(toDoTask1, toDoTask2));
		List<QualityAuditLog> activities = sut.loadAuditLogs(searchParams, 0, -1);
		
		assertThat(activities).containsExactlyInAnyOrder(activity1, activity2);
	}
	
	@Test
	public void shouldLoad_fetch_doer() {
		Identity doer = JunitTestHelper.createAndPersistIdentityAsRndUser(random());
		QualityDataCollection dataCollection = qualityTestHelper.createDataCollection();
		sut.create(QualityAuditLog.Action.toDoCreate, null, null, doer, dataCollection);
		dbInstance.commitAndCloseSession();
		
		QualityAuditLogSearchParams searchParams = new QualityAuditLogSearchParams();
		searchParams.setDoer(doer);
		searchParams.setFetchDoer(true);
		sut.loadAuditLogs(searchParams, 0, -1);
		
		// Just a syntax check
	}
}
