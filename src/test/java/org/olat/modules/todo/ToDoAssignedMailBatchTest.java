/**
 * <a href="https://www.openolat.org">
 * OpenOLAT - Online Learning and Training</a><br>
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); <br>
 * you may not use this file except in compliance with the License.<br>
 * You may obtain a copy of the License at the
 * <a href="https://www.apache.org/licenses/LICENSE-2.0">Apache homepage</a>
 * <p>
 * Unless required by applicable law or agreed to in writing,<br>
 * software distributed under the License is distributed on an "AS IS" BASIS, <br>
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
 * See the License for the specific language governing permissions and <br>
 * limitations under the License.
 * <p>
 * Initial code contributed and copyrighted by<br>
 * frentix GmbH, https://www.frentix.com
 * <p>
 */
package org.olat.modules.todo;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Map;

import org.junit.Test;
import org.olat.basesecurity.model.IdentityRefImpl;
import org.olat.modules.todo.ToDoAssignedMailBatch.Entry;
import org.olat.modules.todo.model.ToDoTaskImpl;

/**
 *
 * Tests the pure collection logic of {@link ToDoAssignedMailBatch}. No Spring context:
 * the batch is created with <code>new</code>, and {@link ToDoAssignedMailBatch#sendMails()}
 * is not called here.
 *
 * Initial date: 22 Sep 2026<br>
 * @author uhensler, urs.hensler@frentix.com, https://www.frentix.com
 *
 */
public class ToDoAssignedMailBatchTest {
	
	@Test
	public void shouldKeepTwoEntriesInInsertionOrder_forOneRecipient() {
		ToDoAssignedMailBatch batch = new ToDoAssignedMailBatch();
		IdentityRefImpl recipient = new IdentityRefImpl(1L);
		ToDoTaskImpl task1 = createTask(11L, "First task");
		ToDoTaskImpl task2 = createTask(12L, "Second task");
		
		batch.onAssigned(null, recipient, task1, null);
		batch.onAssigned(null, recipient, task2, null);
		
		Map<Long, List<Entry>> assignments = batch.getAssignments();
		assertThat(assignments).hasSize(1);
		List<Entry> entries = assignments.get(1L);
		assertThat(entries).extracting(entry -> entry.toDoTask().getKey()).containsExactly(11L, 12L);
	}
	
	@Test
	public void shouldNotDuplicateTheSameToDo_forTheSameRecipient() {
		ToDoAssignedMailBatch batch = new ToDoAssignedMailBatch();
		IdentityRefImpl recipient = new IdentityRefImpl(1L);
		ToDoTaskImpl task = createTask(11L, "Task with an assignee and a delegatee");
		
		// The same recipient can be assignee and delegatee of the same to-do
		batch.onAssigned(null, recipient, task, null);
		batch.onAssigned(null, recipient, task, null);
		
		List<Entry> entries = batch.getAssignments().get(1L);
		assertThat(entries).hasSize(1);
	}
	
	@Test
	public void shouldKeepTwoRecipientsApart() {
		ToDoAssignedMailBatch batch = new ToDoAssignedMailBatch();
		ToDoTaskImpl task = createTask(11L, "Task");
		
		batch.onAssigned(null, new IdentityRefImpl(1L), task, null);
		batch.onAssigned(null, new IdentityRefImpl(2L), task, null);
		
		assertThat(batch.getAssignments()).hasSize(2);
	}
	
	@Test
	public void shouldBeEmpty_withoutAnyAssignment() {
		ToDoAssignedMailBatch batch = new ToDoAssignedMailBatch();
		
		assertThat(batch.isEmpty()).isTrue();
	}
	
	@Test
	public void shouldNotBeEmpty_afterAnAssignment() {
		ToDoAssignedMailBatch batch = new ToDoAssignedMailBatch();
		
		batch.onAssigned(null, new IdentityRefImpl(1L), createTask(11L, "Task"), null);
		
		assertThat(batch.isEmpty()).isFalse();
	}
	
	private ToDoTaskImpl createTask(Long key, String title) {
		ToDoTaskImpl task = new ToDoTaskImpl();
		task.setKey(key);
		task.setTitle(title);
		return task;
	}
	
}
