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
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import jakarta.persistence.TypedQuery;

import org.olat.core.commons.persistence.DB;
import org.olat.core.commons.persistence.QueryBuilder;
import org.olat.core.id.Identity;
import org.olat.core.id.Organisation;
import org.olat.modules.project.ProjActivity;
import org.olat.modules.project.ProjActivity.Action;
import org.olat.modules.project.ProjActivitySearchParams;
import org.olat.modules.project.ProjArtefact;
import org.olat.modules.project.ProjDateRange;
import org.olat.modules.project.ProjProject;
import org.olat.modules.project.model.ProjActivityImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 
 * Initial date: 16 Jan 2023<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
@Component
public class ProjActivityDAO {
	
	@Autowired
	private DB dbInstance;
	
	public ProjActivity create(Action action, String before, String after, Identity doer, ProjProject project) {
		return create(action, before, after, null, doer, project, null, null, null, null);
	}
	
	public ProjActivity create(Action action, String before, String after, Identity doer, ProjProject project, Identity memeber) {
		return create(action, before, after, null, doer, project, null, null, memeber, null);
	}
	
	public ProjActivity create(Action action, String before, String after, Identity doer, ProjProject project, Organisation organisation) {
		return create(action, before, after, null, doer, project, null, null, null, organisation);
	}
	
	public ProjActivity create(Action action, String before, String after, Identity doer, ProjArtefact artefact) {
		return create(action, before, after, null, doer, artefact.getProject(), artefact, null, null, null);
	}
	
	public ProjActivity create(Action action, String before, String after, String tempIdentifier, Identity doer, ProjArtefact artefact) {
		return create(action, before, after, tempIdentifier, doer, artefact.getProject(), artefact, null, null, null);
	}
	
	public ProjActivity create(Action action, String before, String after, Identity doer, ProjArtefact artefact, Identity memeber) {
		return create(action, before, after, null, doer, artefact.getProject(), artefact, null, memeber, null);
	}
	
	public ProjActivity create(Action action, String before, String after, Identity doer, ProjArtefact artefact, ProjArtefact artefactReference) {
		return create(action, before, after, null, doer, artefact.getProject(), artefact, artefactReference, null, null);
	}
	
	public ProjActivity create(Action action, String before, String after, String tempIdentifier, Identity doer, ProjProject project,
			ProjArtefact artefact, ProjArtefact artefactReference, Identity member, Organisation organisation) {
		return create(action, before, after, tempIdentifier, doer, project, artefact, artefactReference, member, organisation, new Date());
	}
	
	ProjActivity create(Action action, String before, String after, String tempIdentifier, Identity doer, ProjProject project,
			ProjArtefact artefact, ProjArtefact artefactReference, Identity member, Organisation organisation, Date creationDate) {
		ProjActivityImpl activity = new ProjActivityImpl();
		activity.setCreationDate(creationDate);
		activity.setAction(action);
		activity.setActionTarget(action.getTarget());
		activity.setBefore(before);
		activity.setAfter(after);
		activity.setTempIdentifier(tempIdentifier);
		activity.setDoer(doer);
		activity.setProject(project);
		activity.setArtefact(artefact);
		activity.setArtefactReference(artefactReference);
		activity.setMember(member);
		activity.setOrganisation(organisation);
		dbInstance.getCurrentEntityManager().persist(activity);
		return activity;
	}

	public List<ProjActivity> loadActivities(String tempIdentifier, Action action) {
		QueryBuilder sb = new QueryBuilder();
		sb.append("select activity");
		sb.append("  from projactivity activity");
		sb.and().append("activity.tempIdentifier = :tempIdentifier");
		sb.and().append("activity.action = :action");
		sb.orderBy().append("activity.creationDate desc");
		
		return dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), ProjActivity.class)
				.setParameter("tempIdentifier", tempIdentifier)
				.setParameter("action", action)
				.getResultList();
	}
	
	public void delete(List<ProjActivity> activities) {
		QueryBuilder sb = new QueryBuilder();
		sb.append("delete from projactivity activity");
		sb.and().append("activity.key in :activitiesKeys");
		
		dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString())
				.setParameter("activitiesKeys", activities.stream().map(ProjActivity::getKey).collect(Collectors.toList()))
				.executeUpdate();
	}
	
	public List<ProjActivity> loadActivities(ProjActivitySearchParams searchParams, int firstResult, int maxResults) {
		QueryBuilder sb = new QueryBuilder();
		sb.append("select activity");
		sb.append("  from projactivity activity");
		sb.append("       left join fetch activity.artefact artefact");
		appendQuery(searchParams, sb);
		if (maxResults > 0) {
			sb.orderBy().append("activity.creationDate desc");
		}
		
		TypedQuery<ProjActivity> query = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), ProjActivity.class);
		addParameters(query, searchParams);
		query.setFirstResult(firstResult);
		if (maxResults > 0) {
			query.setMaxResults(maxResults);
		}
		
		return query.getResultList();
	}
	
	public Map<Long, ProjActivity> loadProjectKeyToLastActivity(ProjActivitySearchParams searchParams) {
		QueryBuilder sb = new QueryBuilder();
		sb.append("select lastactivity");
		sb.append("  from projactivity lastactivity");
		sb.append(" where lastactivity.key in (");
		// Assuming newer records have a higher key. Should sufficient for the moment.
		sb.append("select max(activity.key)");
		sb.append("  from projactivity activity");
		appendQuery(searchParams, sb);
		sb.groupBy().append("activity.project.key");
		
		sb.append(")");
		
		TypedQuery<ProjActivity> query = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), ProjActivity.class);
		addParameters(query, searchParams);
		
		return query.getResultList().stream().collect(Collectors.toMap(activity -> activity.getProject().getKey(), Function.identity()));
	}

	private void appendQuery(ProjActivitySearchParams searchParams, QueryBuilder sb) {
		if (searchParams.getActions() != null && !searchParams.getActions().isEmpty()) {
			sb.and().append("activity.action in :actions");
		}
		if (searchParams.getTargets() != null && !searchParams.getTargets().isEmpty()) {
			sb.and().append("activity.actionTarget in :targets");
		}
		if (searchParams.getDoerKey() != null) {
			sb.and().append("activity.doer.key = :doerKey");
		}
		if (searchParams.getProjectKeys() != null && !searchParams.getProjectKeys().isEmpty()) {
			sb.and().append("activity.project.key in :projectKeys");
		}
		if (searchParams.getArtefactKeys() != null && !searchParams.getArtefactKeys().isEmpty()) {
			sb.and().append("activity.artefact.key in :artefactKeys");
		}
		if (searchParams.getCreatedDateRanges() != null) {
			if (!searchParams.getCreatedDateRanges().isEmpty()) {
				sb.and().append(" (");
				boolean or = false;
				for (int i = 0; i < searchParams.getCreatedDateRanges().size(); i++) {
					if (or) {
						sb.append(" or ");
					}
					or = true;
					
					sb.append("(");
					sb.append("activity.creationDate >= :createdAfter").append(i);
					sb.append(" and ");
					sb.append("activity.creationDate <= :createdBefore").append(i);
					sb.append(")");
				}
				sb.append(")");
			} else {
				// Special case: Load no activities at all.
				sb.and().append("1 = 2");
			}
		}
	}

	private void addParameters(TypedQuery<?> query, ProjActivitySearchParams searchParams) {
		if (searchParams.getActions() != null && !searchParams.getActions().isEmpty()) {
			query.setParameter("actions", searchParams.getActions());
		}
		if (searchParams.getTargets() != null && !searchParams.getTargets().isEmpty()) {
			query.setParameter("targets", searchParams.getTargets());
		}
		if (searchParams.getDoerKey() != null) {
			query.setParameter("doerKey", searchParams.getDoerKey());
		}
		if (searchParams.getProjectKeys() != null && !searchParams.getProjectKeys().isEmpty()) {
			query.setParameter("projectKeys", searchParams.getProjectKeys());
		}
		if (searchParams.getArtefactKeys() != null && !searchParams.getArtefactKeys().isEmpty()) {
			query.setParameter("artefactKeys", searchParams.getArtefactKeys());
		}
		if (searchParams.getCreatedDateRanges() != null) {
			if (!searchParams.getCreatedDateRanges().isEmpty()) {
				for (int i = 0; i < searchParams.getCreatedDateRanges().size(); i++) {
					ProjDateRange dateRange = searchParams.getCreatedDateRanges().get(i);
					query.setParameter("createdAfter" + i, dateRange.getFrom());
					query.setParameter("createdBefore" + i, dateRange.getTo());
				}
			}
		}
	}

}
