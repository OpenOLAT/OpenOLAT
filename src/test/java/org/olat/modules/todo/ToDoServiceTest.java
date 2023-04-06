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
package org.olat.modules.todo;

import static org.assertj.core.api.Assertions.assertThat;
import static org.olat.test.JunitTestHelper.random;

import java.util.List;

import org.junit.Test;
import org.olat.core.commons.persistence.DB;
import org.olat.core.commons.services.tag.Tag;
import org.olat.core.id.Identity;
import org.olat.test.JunitTestHelper;
import org.olat.test.OlatTestCase;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 29 Mar 2023<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class ToDoServiceTest extends OlatTestCase {
	
	@Autowired
	private DB dbInstance;
	
	@Autowired
	private ToDoService sut;
	
	@Test
	public void shouldLoadTags() {
		Identity creator = JunitTestHelper.createAndPersistIdentityAsAuthor(random());
		ToDoTask toDoTask = sut.createToDoTask(creator, random());
		
		String displayName1 = random();
		String displayName2 = random();
		sut.updateTags(toDoTask, List.of(displayName1, displayName2));
		dbInstance.commitAndCloseSession();
		
		ToDoTaskSearchParams searchParams = new ToDoTaskSearchParams();
		searchParams.setToDoTasks(List.of(toDoTask));
		List<ToDoTaskTag> toDoTaskTags = sut.getToDoTaskTags(searchParams);
		
		assertThat(toDoTaskTags).extracting(ToDoTaskTag::getTag).extracting(Tag::getDisplayName).containsExactlyInAnyOrder(displayName1, displayName2);
	}
	
	@Test
	public void shouldGetExpenditureOfWork() {
		assertExpenditureOfWork(sut.getExpenditureOfWork(1l), 0, 0, 1);
		assertExpenditureOfWork(sut.getExpenditureOfWork(8l), 0, 1, 0);
		assertExpenditureOfWork(sut.getExpenditureOfWork(10l), 0, 1, 2);
		assertExpenditureOfWork(sut.getExpenditureOfWork(20l), 0, 2, 4);
		assertExpenditureOfWork(sut.getExpenditureOfWork(40l), 1, 0, 0);
		assertExpenditureOfWork(sut.getExpenditureOfWork(70l), 1, 3, 6);
	}
	
	private void assertExpenditureOfWork(ToDoExpenditureOfWork expenditureOfWork, long weeks, long days, long hours) {
		assertThat(expenditureOfWork.getWeeks()).isEqualTo(weeks);
		assertThat(expenditureOfWork.getDays()).isEqualTo(days);
		assertThat(expenditureOfWork.getHours()).isEqualTo(hours);
	}

}
