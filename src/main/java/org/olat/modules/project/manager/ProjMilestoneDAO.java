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

import org.jgroups.util.UUID;
import org.olat.core.commons.persistence.DB;
import org.olat.core.commons.persistence.QueryBuilder;
import org.olat.modules.project.ProjArtefact;
import org.olat.modules.project.ProjMilestone;
import org.olat.modules.project.ProjMilestoneRef;
import org.olat.modules.project.ProjMilestoneSearchParams;
import org.olat.modules.project.ProjMilestoneStatus;
import org.olat.modules.project.model.ProjMilestoneImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 
 * Initial date: 9 Feb 2023<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
@Component
public class ProjMilestoneDAO {
	
	@Autowired
	private DB dbInstance;
	
	public ProjMilestone create(ProjArtefact artefact,  Date dueDate) {
		ProjMilestoneImpl milestone = new ProjMilestoneImpl();
		milestone.setCreationDate(new Date());
		milestone.setLastModified(milestone.getCreationDate());
		milestone.setIdentifier(UUID.randomUUID().toString());
		milestone.setStatus(ProjMilestoneStatus.open);
		milestone.setDueDate(dueDate);
		milestone.setArtefact(artefact);
		dbInstance.getCurrentEntityManager().persist(milestone);
		return milestone;
	}
	
	public ProjMilestone save(ProjMilestone milestone) {
		if (milestone instanceof ProjMilestoneImpl) {
			ProjMilestoneImpl impl = (ProjMilestoneImpl)milestone;
			impl.setLastModified(new Date());
			dbInstance.getCurrentEntityManager().merge(milestone);
		}
		return milestone;
	}

	public void delete(ProjMilestoneRef milestone) {
		QueryBuilder sb = new QueryBuilder();
		sb.append("delete from projmilestone milestone");
		sb.and().append("milestone.key = :milestoneKey");
		
		dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString())
				.setParameter("milestoneKey", milestone.getKey())
				.executeUpdate();
	}
	
	public long loadMilestonesCount(ProjMilestoneSearchParams searchParams) {
		QueryBuilder sb = new QueryBuilder();
		sb.append("select count(*)");
		sb.append("  from projmilestone milestone");
		sb.append("       inner join milestone.artefact artefact");
		appendQuery(searchParams, sb);
		
		TypedQuery<Long> query = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), Long.class);
		addParameters(query, searchParams);
		
		return query.getSingleResult().longValue();
	}	

	public List<ProjMilestone> loadMilestones(ProjMilestoneSearchParams searchParams) {
		QueryBuilder sb = new QueryBuilder();
		sb.append("select milestone");
		sb.append("  from projmilestone milestone");
		sb.append("       inner join fetch milestone.artefact artefact");
		sb.append("       inner join fetch artefact.project project");
		sb.append("       inner join fetch artefact.creator creator");
		sb.append("       inner join fetch artefact.contentModifiedBy modifier");
		appendQuery(searchParams, sb);
		
		TypedQuery<ProjMilestone> query = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), ProjMilestone.class);
		addParameters(query, searchParams);
		
		return query.getResultList();
	}

	private void appendQuery(ProjMilestoneSearchParams searchParams, QueryBuilder sb) {
		if (searchParams.getProject() != null) {
			sb.and().append("artefact.project.key = :projectKey");
		}
		if (searchParams.getMilestoneKeys() != null && !searchParams.getMilestoneKeys().isEmpty()) {
			sb.and().append("milestone.key in :milestoneKeys");
		}
		if (searchParams.getIdentifiers() != null && !searchParams.getIdentifiers().isEmpty()) {
			sb.and().append("milestone.identifier in :identifiers");
		}
		if (searchParams.getArtefactKeys() != null && !searchParams.getArtefactKeys().isEmpty()) {
			sb.and().append("artefact.key in :artefactKeys");
		}
		if (searchParams.getStatus() != null && !searchParams.getStatus().isEmpty()) {
			sb.and().append("artefact.status in :status");
		}
	}

	private void addParameters(TypedQuery<?> query, ProjMilestoneSearchParams searchParams) {
		if (searchParams.getProject() != null) {
			query.setParameter("projectKey", searchParams.getProject().getKey());
		}
		if (searchParams.getMilestoneKeys() != null && !searchParams.getMilestoneKeys().isEmpty()) {
			query.setParameter("milestoneKeys", searchParams.getMilestoneKeys());
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
	}

}
