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

import java.util.Date;
import java.util.List;

import org.olat.core.commons.persistence.DB;
import org.olat.core.commons.persistence.QueryBuilder;
import org.olat.core.commons.services.tag.Tag;
import org.olat.modules.todo.ToDoTask;
import org.olat.modules.todo.ToDoTaskRef;
import org.olat.modules.todo.ToDoTaskTag;
import org.olat.modules.todo.model.ToDoTaskTagImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 
 * Initial date: 27 Mar 2023<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
@Component
public class ToDoTaskTagDAO {
	
	@Autowired
	private DB dbInstance;
	
	public ToDoTaskTag create(ToDoTask toDoTask, Tag tag) {
		ToDoTaskTagImpl toDoTaskTag = new ToDoTaskTagImpl();
		toDoTaskTag.setCreationDate(new Date());
		toDoTaskTag.setToDoTask(toDoTask);
		toDoTaskTag.setTag(tag);
		dbInstance.getCurrentEntityManager().persist(toDoTaskTag);
		return toDoTaskTag;
	}
	
	public void delete(ToDoTaskTag toDoTaskTag) {
		QueryBuilder sb = new QueryBuilder();
		sb.append("delete from todotasktag toDoTaskTag");
		sb.and().append("toDoTaskTag.key = :key");
		
		dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString())
				.setParameter("key", toDoTaskTag.getKey())
				.executeUpdate();
	}

	public void deleteByToDoTask(ToDoTask toDoTask) {
		QueryBuilder sb = new QueryBuilder();
		sb.append("delete from todotasktag toDoTaskTag");
		sb.and().append("toDoTaskTag.toDoTask.key = :key");
		
		dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString())
				.setParameter("key", toDoTask.getKey())
				.executeUpdate();
	}
	
	public List<ToDoTaskTag> loadToDoTaskTags(ToDoTaskRef toDoTask) {
		QueryBuilder sb = new QueryBuilder();
		sb.append("select toDoTaskTag");
		sb.append("  from todotasktag toDoTaskTag");
		sb.append("       inner join fetch toDoTaskTag.tag tag");
		sb.and().append("toDoTaskTag.toDoTask.key = :key");
		
		return dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), ToDoTaskTag.class)
				.setParameter("key", toDoTask.getKey())
				.getResultList();
	}
	
	public List<String> loadTags(ToDoTaskRef toDoTask) {
		QueryBuilder sb = new QueryBuilder();
		sb.append("select toDoTaskTag.tag.displayName");
		sb.append("  from todotasktag toDoTaskTag");
		sb.and().append("toDoTaskTag.toDoTask.key = :key");
		
		return dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), String.class)
				.setParameter("key", toDoTask.getKey())
				.getResultList();
	}
	
}
