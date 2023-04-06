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
package org.olat.modules.todo.manager;

import static org.assertj.core.api.Assertions.assertThat;
import static org.olat.test.JunitTestHelper.random;

import java.util.List;

import org.junit.Test;
import org.olat.core.commons.persistence.DB;
import org.olat.core.commons.services.tag.Tag;
import org.olat.core.commons.services.tag.TagService;
import org.olat.core.id.Identity;
import org.olat.modules.todo.ToDoService;
import org.olat.modules.todo.ToDoTask;
import org.olat.modules.todo.ToDoTaskTag;
import org.olat.test.JunitTestHelper;
import org.olat.test.OlatTestCase;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 27 Mar 2023<br>>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class ToDoTaskTagDAOTest extends OlatTestCase {

	@Autowired
	private DB dbInstance;
	@Autowired
	private ToDoService toDoService;
	@Autowired
	private TagService tagService;
	
	@Autowired
	private ToDoTaskTagDAO sut;
	
	@Test
	public void shouldCreateTag() {
		Identity creator = JunitTestHelper.createAndPersistIdentityAsRndUser(random());
		ToDoTask toDoTask = toDoService.createToDoTask(creator, random());
		Tag tag = tagService.getOrCreateTag(random());
		dbInstance.commitAndCloseSession();
		
		ToDoTaskTag toDoTaskTag = sut.create(toDoTask, tag);
		dbInstance.commitAndCloseSession();
		
		assertThat(toDoTaskTag).isNotNull();
		assertThat(toDoTaskTag.getCreationDate()).isNotNull();
		assertThat(toDoTaskTag.getToDoTask()).isEqualTo(toDoTask);
		assertThat(toDoTaskTag.getTag()).isEqualTo(tag);
	}
	
	@Test
	public void shouldDelete() {
		ToDoTaskTag projTag = createRandomToDoTaskTag();
		
		sut.delete(projTag);
		dbInstance.commitAndCloseSession();
		
		List<String> tags = sut.loadTags(projTag.getToDoTask());
		
		assertThat(tags).isEmpty();
	}
	
	@Test
	public void shouldDeleteByToDoTask() {
		Identity creator = JunitTestHelper.createAndPersistIdentityAsRndUser(random());
		ToDoTask toDoTask = toDoService.createToDoTask(creator, random());
		createRandomToDoTaskTag(toDoTask);
		createRandomToDoTaskTag(toDoTask);
		ToDoTask toDoTask2 = toDoService.createToDoTask(creator, random());
		ToDoTaskTag toDoTaskTag = createRandomToDoTaskTag(toDoTask2);
		
		sut.deleteByToDoTask(toDoTask);
		dbInstance.commitAndCloseSession();
		
		List<String> tags = sut.loadTags(toDoTask);
		assertThat(tags).isEmpty();
		
		tags = sut.loadTags(toDoTask2);
		assertThat(tags).containsExactlyInAnyOrder(toDoTaskTag.getTag().getDisplayName());
	}
	
	private ToDoTaskTag createRandomToDoTaskTag() {
		Identity creator = JunitTestHelper.createAndPersistIdentityAsRndUser(random());
		ToDoTask toDoTask = toDoService.createToDoTask(creator, random());
		return createRandomToDoTaskTag(toDoTask);
	}
	
	private ToDoTaskTag createRandomToDoTaskTag(ToDoTask toDoTask) {
		Tag tag = tagService.getOrCreateTag(random());
		ToDoTaskTag toDoTaskTag = sut.create(toDoTask, tag);
		dbInstance.commitAndCloseSession();
		return toDoTaskTag;
	}

}
