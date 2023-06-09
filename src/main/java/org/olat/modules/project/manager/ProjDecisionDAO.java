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
import org.olat.modules.project.ProjDecision;
import org.olat.modules.project.ProjDecisionRef;
import org.olat.modules.project.ProjDecisionSearchParams;
import org.olat.modules.project.model.ProjDecisionImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 
 * Initial date: 9 Jun 2023<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
@Component
public class ProjDecisionDAO {
	
	@Autowired
	private DB dbInstance;
	
	public ProjDecision create(ProjArtefact artefact) {
		ProjDecisionImpl decision = new ProjDecisionImpl();
		decision.setCreationDate(new Date());
		decision.setLastModified(decision.getCreationDate());
		decision.setArtefact(artefact);
		dbInstance.getCurrentEntityManager().persist(decision);
		return decision;
	}
	
	public ProjDecision save(ProjDecision decision) {
		if (decision instanceof ProjDecisionImpl) {
			ProjDecisionImpl impl = (ProjDecisionImpl)decision;
			impl.setLastModified(new Date());
			dbInstance.getCurrentEntityManager().merge(decision);
		}
		return decision;
	}

	public void delete(ProjDecisionRef decision) {
		QueryBuilder sb = new QueryBuilder();
		sb.append("delete from projdecision decision");
		sb.and().append("decision.key = :decisionKey");
		
		dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString())
				.setParameter("decisionKey", decision.getKey())
				.executeUpdate();
	}
	
	public long loadDecisionsCount(ProjDecisionSearchParams searchParams) {
		QueryBuilder sb = new QueryBuilder();
		sb.append("select count(*)");
		sb.append("  from projdecision decision");
		sb.append("       inner join decision.artefact artefact");
		appendQuery(searchParams, sb);
		
		TypedQuery<Long> query = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), Long.class);
		addParameters(query, searchParams);
		
		return query.getSingleResult().longValue();
	}	

	public List<ProjDecision> loadDecisions(ProjDecisionSearchParams searchParams) {
		QueryBuilder sb = new QueryBuilder();
		sb.append("select decision");
		sb.append("  from projdecision decision");
		sb.append("       inner join fetch decision.artefact artefact");
		sb.append("       inner join fetch artefact.project project");
		sb.append("       inner join fetch artefact.creator creator");
		sb.append("       inner join fetch artefact.contentModifiedBy modifier");
		appendQuery(searchParams, sb);
		appendOrderBy(searchParams, sb);
		
		TypedQuery<ProjDecision> query = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), ProjDecision.class);
		addParameters(query, searchParams);
		if (searchParams.getNumLastModified() != null) {
			query.setMaxResults(searchParams.getNumLastModified());
		}
		
		return query.getResultList();
	}

	private void appendQuery(ProjDecisionSearchParams searchParams, QueryBuilder sb) {
		if (searchParams.getProject() != null) {
			sb.and().append("artefact.project.key = :projectKey");
		}
		if (searchParams.getDecisionKeys() != null && !searchParams.getDecisionKeys().isEmpty()) {
			sb.and().append("decision.key in :decisionKeys");
		}
		if (searchParams.getArtefactKeys() != null && !searchParams.getArtefactKeys().isEmpty()) {
			sb.and().append("artefact.key in :artefactKeys");
		}
		if (searchParams.getStatus() != null && !searchParams.getStatus().isEmpty()) {
			sb.and().append("artefact.status in :status");
		}
	}
	
	private void appendOrderBy(ProjDecisionSearchParams searchParams, QueryBuilder sb) {
		if (searchParams.getNumLastModified() != null) {
			sb.orderBy().append("artefact.contentModifiedDate").appendAsc(false);
			sb.orderBy().append("artefact.key").appendAsc(false);
		}
	}

	private void addParameters(TypedQuery<?> query, ProjDecisionSearchParams searchParams) {
		if (searchParams.getProject() != null) {
			query.setParameter("projectKey", searchParams.getProject().getKey());
		}
		if (searchParams.getDecisionKeys() != null && !searchParams.getDecisionKeys().isEmpty()) {
			query.setParameter("decisionKeys", searchParams.getDecisionKeys());
		}
		if (searchParams.getArtefactKeys() != null && !searchParams.getArtefactKeys().isEmpty()) {
			query.setParameter("artefactKeys", searchParams.getArtefactKeys());
		}
		if (searchParams.getStatus() != null && !searchParams.getStatus().isEmpty()) {
			query.setParameter("status", searchParams.getStatus());
		}
	}

}
