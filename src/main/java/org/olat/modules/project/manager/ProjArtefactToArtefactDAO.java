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
import org.olat.core.id.Identity;
import org.olat.modules.project.ProjArtefact;
import org.olat.modules.project.ProjArtefactRef;
import org.olat.modules.project.ProjArtefactToArtefact;
import org.olat.modules.project.ProjArtefactToArtefactSearchParams;
import org.olat.modules.project.ProjProject;
import org.olat.modules.project.model.ProjArtefactToArtefactImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 
 * Initial date: 5 Jan 2023<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
@Component
public class ProjArtefactToArtefactDAO {
	
	@Autowired
	private DB dbInstance;
	
	public ProjArtefactToArtefact create(ProjProject project, Identity creator, ProjArtefact artefact1, ProjArtefact artefact2) {
		ProjArtefactToArtefactImpl ata = new ProjArtefactToArtefactImpl();
		ata.setCreationDate(new Date());
		ata.setProject(project);
		ata.setCreator(creator);
		ata.setArtefact1(artefact1);
		ata.setArtefact2(artefact2);
		dbInstance.getCurrentEntityManager().persist(ata);
		return ata;
	}
	
	public void delete(ProjArtefact artefact) {
		QueryBuilder sb = new QueryBuilder();
		sb.append("delete from projartefacttoartefact ata");
		sb.and().append("ata.artefact1.key = :artefactKey");
		sb.append(" or ata.artefact2.key = :artefactKey");
		
		dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString())
				.setParameter("artefactKey", artefact.getKey())
				.executeUpdate();
	}
	
	public void delete(ProjArtefactRef artefact1, ProjArtefactRef artefact2) {
		QueryBuilder sb = new QueryBuilder();
		sb.append("delete from projartefacttoartefact ata");
		sb.and().append("(ata.artefact1.key = :artefact1Key and ata.artefact2.key = :artefact2Key)");
		sb.append("or (ata.artefact1.key = :artefact2Key and ata.artefact2.key = :artefact1Key)");
		
		dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString())
				.setParameter("artefact1Key", artefact1.getKey())
				.setParameter("artefact2Key", artefact2.getKey())
				.executeUpdate();
	}
	
	public boolean exists(ProjArtefactRef artefact1, ProjArtefactRef artefact2) {
		QueryBuilder sb = new QueryBuilder();
		sb.append("select ata.key");
		sb.append("  from projartefacttoartefact ata");
		sb.and().append("(ata.artefact1.key = :artefact1Key and ata.artefact2.key = :artefact2Key)");
		sb.append("or (ata.artefact1.key = :artefact2Key and ata.artefact2.key = :artefact1Key)");
		
		return !dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), Long.class)
				.setParameter("artefact1Key", artefact1.getKey())
				.setParameter("artefact2Key", artefact2.getKey())
				.getResultList()
				.isEmpty();
	}
	
	public long loadArtefactToArtefactsCount(ProjArtefactToArtefactSearchParams searchParams) {
		QueryBuilder sb = new QueryBuilder();
		sb.append("select count(*)");
		sb.append("  from projartefacttoartefact ata");
		appendQuery(sb, searchParams);
		
		TypedQuery<Long> query = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), Long.class);
		addParameters(query, searchParams);
		
		return query.getSingleResult().longValue();
	}	

	public List<ProjArtefactToArtefact> loadArtefactToArtefacts(ProjArtefactToArtefactSearchParams searchParams) {
		QueryBuilder sb = new QueryBuilder();
		sb.append("select ata");
		sb.append("  from projartefacttoartefact ata");
		sb.append("       inner join fetch ata.artefact1");
		sb.append("       inner join fetch ata.artefact2");
		appendQuery(sb, searchParams);
		
		TypedQuery<ProjArtefactToArtefact> query = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), ProjArtefactToArtefact.class);
		addParameters(query, searchParams);
		
		return query.getResultList();
	}

	private void appendQuery(QueryBuilder sb, ProjArtefactToArtefactSearchParams searchParams) {
		if (searchParams.getProjectKey() != null) {
			sb.and().append("ata.project.key = :projectKey");
		}
		if (searchParams.getCreatorKey() != null) {
			sb.and().append("ata.creator.key = :creatorKey");
		}
		if (searchParams.getArtefactKeys() != null && !searchParams.getArtefactKeys().isEmpty()) {
			sb.and().append("(");
			sb.append("    ata.artefact1.key in :artefactKeys");
			sb.append(" or ata.artefact2.key in :artefactKeys");
			sb.append(")");
		}
	}

	private void addParameters(TypedQuery<?> query, ProjArtefactToArtefactSearchParams searchParams) {
		if (searchParams.getProjectKey() != null) {
			query.setParameter("projectKey", searchParams.getProjectKey());
		}
		if (searchParams.getCreatorKey() != null) {
			query.setParameter("creatorKey", searchParams.getCreatorKey());
		}
		if (searchParams.getArtefactKeys() != null && !searchParams.getArtefactKeys().isEmpty()) {
			query.setParameter("artefactKeys", searchParams.getArtefactKeys());
		}
	}

}
