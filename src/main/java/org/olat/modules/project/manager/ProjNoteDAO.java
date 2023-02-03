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

import java.util.Date;
import java.util.List;

import jakarta.persistence.TypedQuery;

import org.olat.core.commons.persistence.DB;
import org.olat.core.commons.persistence.QueryBuilder;
import org.olat.modules.project.ProjArtefact;
import org.olat.modules.project.ProjNote;
import org.olat.modules.project.ProjNoteRef;
import org.olat.modules.project.ProjNoteSearchParams;
import org.olat.modules.project.model.ProjNoteImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 
 * Initial date: 16 Dez 2022<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
@Component
public class ProjNoteDAO {
	
	@Autowired
	private DB dbInstance;
	
	public ProjNote create(ProjArtefact artefact) {
		ProjNoteImpl note = new ProjNoteImpl();
		note.setCreationDate(new Date());
		note.setLastModified(note.getCreationDate());
		note.setArtefact(artefact);
		dbInstance.getCurrentEntityManager().persist(note);
		return note;
	}
	
	public ProjNote save(ProjNote note) {
		if (note instanceof ProjNoteImpl) {
			ProjNoteImpl impl = (ProjNoteImpl)note;
			impl.setLastModified(new Date());
			dbInstance.getCurrentEntityManager().merge(note);
		}
		return note;
	}

	public void delete(ProjNoteRef note) {
		QueryBuilder sb = new QueryBuilder();
		sb.append("delete from projnote note");
		sb.and().append("note.key = :noteKey");
		
		dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString())
				.setParameter("noteKey", note.getKey())
				.executeUpdate();
	}
	
	public long loadNotesCount(ProjNoteSearchParams searchParams) {
		QueryBuilder sb = new QueryBuilder();
		sb.append("select count(*)");
		sb.append("  from projnote note");
		sb.append("       inner join note.artefact artefact");
		appendQuery(searchParams, sb);
		appendOrderBy(searchParams, sb);
		
		TypedQuery<Long> query = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), Long.class);
		addParameters(query, searchParams);
		
		return query.getSingleResult().longValue();
	}	

	public List<ProjNote> loadNotes(ProjNoteSearchParams searchParams) {
		QueryBuilder sb = new QueryBuilder();
		sb.append("select note");
		sb.append("  from projnote note");
		sb.append("       inner join fetch note.artefact artefact");
		sb.append("       inner join fetch artefact.project project");
		sb.append("       inner join fetch artefact.creator creator");
		sb.append("       inner join fetch artefact.contentModifiedBy modifier");
		appendQuery(searchParams, sb);
		appendOrderBy(searchParams, sb);
		
		TypedQuery<ProjNote> query = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), ProjNote.class);
		addParameters(query, searchParams);
		if (searchParams.getNumLastModified() != null) {
			query.setMaxResults(searchParams.getNumLastModified());
		}
		
		return query.getResultList();
	}

	private void appendQuery(ProjNoteSearchParams searchParams, QueryBuilder sb) {
		if (searchParams.getProject() != null) {
			sb.and().append("artefact.project.key = :projectKey");
		}
		if (searchParams.getNoteKeys() != null && !searchParams.getNoteKeys().isEmpty()) {
			sb.and().append("note.key in :noteKeys");
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
			sb.and().append("note.creationDate >= :createdAfter");
		}
	}

	private void appendOrderBy(ProjNoteSearchParams searchParams, QueryBuilder sb) {
		if (searchParams.getNumLastModified() != null) {
			sb.orderBy().append("artefact.contentModifiedDate").appendAsc(false);
			sb.orderBy().append("artefact.key").appendAsc(false);
		}
	}

	private void addParameters(TypedQuery<?> query, ProjNoteSearchParams searchParams) {
		if (searchParams.getProject() != null) {
			query.setParameter("projectKey", searchParams.getProject().getKey());
		}
		if (searchParams.getNoteKeys() != null && !searchParams.getNoteKeys().isEmpty()) {
			query.setParameter("noteKeys", searchParams.getNoteKeys());
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
	}

}
