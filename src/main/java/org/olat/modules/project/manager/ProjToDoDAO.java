/**
 * <a href="http://www.openolat.org">
 * OpenOLAT - Online Learning and Training</a><br>
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); <br>
 * you may not use this toDoTask except in compliance with the License.<br>
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

import java.util.Date;
import java.util.List;

import jakarta.persistence.TypedQuery;

import org.olat.core.commons.persistence.DB;
import org.olat.core.commons.persistence.QueryBuilder;
import org.olat.modules.project.ProjArtefact;
import org.olat.modules.project.ProjToDo;
import org.olat.modules.project.ProjToDoRef;
import org.olat.modules.project.ProjToDoSearchParams;
import org.olat.modules.project.model.ProjToDoImpl;
import org.olat.modules.todo.ToDoTask;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 
 * Initial date: 27 Mar 2023<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
@Component
public class ProjToDoDAO {
	
	@Autowired
	private DB dbInstance;
	
	public ProjToDo create(ProjArtefact artefact, ToDoTask toDoTask, String identifier) {
		ProjToDoImpl toDo = new ProjToDoImpl();
		toDo.setCreationDate(new Date());
		toDo.setLastModified(toDo.getCreationDate());
		toDo.setIdentifier(identifier);
		toDo.setToDoTask(toDoTask);
		toDo.setArtefact(artefact);
		dbInstance.getCurrentEntityManager().persist(toDo);
		return toDo;
	}
	
	public ProjToDo save(ProjToDo toDo) {
		if (toDo instanceof ProjToDoImpl) {
			ProjToDoImpl impl = (ProjToDoImpl)toDo;
			impl.setLastModified(new Date());
			dbInstance.getCurrentEntityManager().merge(toDo);
		}
		return toDo;
	}

	public void delete(ProjToDoRef toDo) {
		QueryBuilder sb = new QueryBuilder();
		sb.append("delete from projtodo toDo");
		sb.and().append("toDo.key = :toDoKey");
		
		dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString())
				.setParameter("toDoKey", toDo.getKey())
				.executeUpdate();
	}

	public long loadToDosCount(ProjToDoSearchParams searchParams) {
		QueryBuilder sb = new QueryBuilder();
		sb.append("select count(*)");
		sb.append("  from projtodo toDo");
		sb.append("       inner join toDo.artefact artefact");
		sb.append("       inner join toDo.toDoTask toDoTask");
		appendQuery(sb, searchParams);
		appendOrderBy(searchParams, sb);
		
		TypedQuery<Long> query = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), Long.class);
		addParameters(query, searchParams);
		
		return query.getSingleResult().longValue();
	}

	public List<ProjToDo> loadToDos(ProjToDoSearchParams searchParams) {
		QueryBuilder sb = new QueryBuilder();
		sb.append("select toDo");
		sb.append("  from projtodo toDo");
		sb.append("       inner join fetch toDo.artefact artefact");
		sb.append("       inner join fetch artefact.project project");
		sb.append("       inner join fetch artefact.creator creator");
		sb.append("       inner join fetch artefact.contentModifiedBy modifier");
		sb.append("       inner join fetch toDo.toDoTask toDoTask");
		appendQuery(sb, searchParams);
		appendOrderBy(searchParams, sb);
		
		TypedQuery<ProjToDo> query = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), ProjToDo.class);
		addParameters(query, searchParams);
		if (searchParams.getNumLastModified() != null) {
			query.setMaxResults(searchParams.getNumLastModified().intValue());
		}
		
		return query.getResultList();
	}

	private void appendQuery(QueryBuilder sb, ProjToDoSearchParams searchParams) {
		if (searchParams.getProject() != null) {
			sb.and().append("artefact.project.key = :projectKey");
		}
		if (searchParams.getToDoKeys() != null && !searchParams.getToDoKeys().isEmpty()) {
			sb.and().append("toDo.key in :toDoKeys");
		}
		if (searchParams.getIdentifiers() != null && !searchParams.getIdentifiers().isEmpty()) {
			sb.and().append("toDo.identifier in :identifiers");
		}
		if (searchParams.getArtefactKeys() != null && !searchParams.getArtefactKeys().isEmpty()) {
			sb.and().append("artefact.key in :artefactKeys");
		}
		if (searchParams.getStatus() != null && !searchParams.getStatus().isEmpty()) {
			sb.and().append("artefact.status in :status");
		}
		if (searchParams.getCreatorKeys() != null && !searchParams.getCreatorKeys().isEmpty()) {
			sb.and().append("artefact.creator.key in :creatorKeys");
		}
		if (searchParams.getCreatedAfter() != null) {
			sb.and().append("artefact.creationDate >= :createdAfter");
		}
		if (searchParams.getToDoStatus() != null && !searchParams.getToDoStatus().isEmpty()) {
			sb.and().append("toDoTask.status in :toDoTaskStatus");
		}
		if (searchParams.getDueDateNull() != null) {
			sb.and().append("toDoTask.dueDate is ").append("not ", !searchParams.getDueDateNull().booleanValue()).append(" null");
		}
	}

	private void appendOrderBy(ProjToDoSearchParams params, QueryBuilder sb) {
		if (params.getNumLastModified() != null) {
			sb.orderBy().append("artefact.contentModifiedDate").appendAsc(false);
			sb.orderBy().append("artefact.key").appendAsc(false);
		}
	}

	private void addParameters(TypedQuery<?> query, ProjToDoSearchParams searchParams) {
		if (searchParams.getProject() != null) {
			query.setParameter("projectKey", searchParams.getProject().getKey());
		}
		if (searchParams.getToDoKeys() != null && !searchParams.getToDoKeys().isEmpty()) {
			query.setParameter("toDoKeys", searchParams.getToDoKeys());
		}
		if (searchParams.getIdentifiers() != null && !searchParams.getIdentifiers().isEmpty()) {
			query.setParameter("identifiers", searchParams.getIdentifiers());
		}
		if (searchParams.getArtefactKeys() != null && !searchParams.getArtefactKeys().isEmpty()) {
			query.setParameter("artefactKeys", searchParams.getArtefactKeys());
		}
		if (searchParams.getStatus() != null && !searchParams.getStatus().isEmpty()) {
			query.setParameter("status", searchParams.getStatus());
		}
		if (searchParams.getCreatorKeys() != null && !searchParams.getCreatorKeys().isEmpty()) {
			query.setParameter("creatorKeys", searchParams.getCreatorKeys());
		}
		if (searchParams.getCreatedAfter() != null) {
			query.setParameter("createdAfter", searchParams.getCreatedAfter());
		}
		if (searchParams.getToDoStatus() != null && !searchParams.getToDoStatus().isEmpty()) {
			query.setParameter("toDoTaskStatus", searchParams.getToDoStatus());
		}
	}

}
