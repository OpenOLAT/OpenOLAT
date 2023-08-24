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

import org.olat.basesecurity.Group;
import org.olat.basesecurity.manager.GroupDAO;
import org.olat.core.commons.persistence.DB;
import org.olat.core.commons.persistence.QueryBuilder;
import org.olat.core.id.Identity;
import org.olat.modules.project.ProjActivity;
import org.olat.modules.project.ProjArtefact;
import org.olat.modules.project.ProjArtefactRef;
import org.olat.modules.project.ProjArtefactSearchParams;
import org.olat.modules.project.ProjProject;
import org.olat.modules.project.ProjProjectRef;
import org.olat.modules.project.ProjectStatus;
import org.olat.modules.project.model.ProjArtefactImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 
 * Initial date: 23 Dez 2022<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
@Component
public class ProjArtefactDAO {
	
	@Autowired
	private DB dbInstance;
	@Autowired
	private GroupDAO groupDao;
	
	public ProjArtefact create(String type, ProjProject project, Identity creator) {
		Group baseGroup = groupDao.createGroup();
		groupDao.addMembershipOneWay(baseGroup, creator, ProjectServiceImpl.DEFAULT_ROLE_NAME);
		
		ProjArtefactImpl artefact = new ProjArtefactImpl();
		artefact.setCreationDate(new Date());
		artefact.setLastModified(artefact.getCreationDate());
		artefact.setContentModifiedDate(artefact.getCreationDate());
		artefact.setContentModifiedBy(creator);
		artefact.setType(type);
		artefact.setStatus(ProjectStatus.active);
		artefact.setCreator(creator);
		artefact.setProject(project);
		artefact.setBaseGroup(baseGroup);

		dbInstance.getCurrentEntityManager().persist(artefact);
		return artefact;
	}
	
	public ProjArtefact save(ProjArtefact artefact) {
		if (artefact instanceof ProjArtefactImpl) {
			ProjArtefactImpl impl = (ProjArtefactImpl)artefact;
			impl.setLastModified(new Date());
			dbInstance.getCurrentEntityManager().merge(artefact);
		}
		return artefact;
	}

	public void delete(ProjArtefactRef artefact) {
		QueryBuilder sb = new QueryBuilder();
		sb.append("delete from projartefact artefact");
		sb.and().append("artefact.key = :artefactKey");
		
		dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString())
				.setParameter("artefactKey", artefact.getKey())
				.executeUpdate();
	}
	
	public long loadArtefactsCount(ProjArtefactSearchParams searchParams) {
		QueryBuilder sb = new QueryBuilder();
		sb.append("select count(*)");
		sb.append("  from projartefact artefact");
		appendQuery(sb, searchParams);
		
		TypedQuery<Long> query = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), Long.class);
		addParameters(query, searchParams);
		
		return query.getSingleResult().longValue();
	}	

	public List<ProjArtefact> loadArtefacts(ProjArtefactSearchParams searchParams) {
		QueryBuilder sb = new QueryBuilder();
		sb.append("select artefact");
		sb.append("  from projartefact artefact");
		appendQuery(sb, searchParams);
		
		TypedQuery<ProjArtefact> query = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), ProjArtefact.class);
		addParameters(query, searchParams);
		
		return query.getResultList();
	}

	private void appendQuery(QueryBuilder sb, ProjArtefactSearchParams searchParams) {
		if (searchParams.getProject() != null) {
			sb.and().append("artefact.project.key = :projectKey");
		}
		if (searchParams.getArtefactKeys() != null && !searchParams.getArtefactKeys().isEmpty()) {
			sb.and().append("artefact.key in :artefactKeys");
		}
		if (searchParams.getExcludedArtefactKeys() != null && !searchParams.getExcludedArtefactKeys().isEmpty()) {
			sb.and().append("artefact.key not in :excludedArtefactKeys");
		}
		if (searchParams.getTypes() != null && !searchParams.getTypes().isEmpty()) {
			sb.and().append("artefact.type in :types");
		}
		if (searchParams.getStatus() != null && !searchParams.getStatus().isEmpty()) {
			sb.and().append("artefact.status in :status");
		}
	}

	private void addParameters(TypedQuery<?> query, ProjArtefactSearchParams searchParams) {
		if (searchParams.getProject() != null) {
			query.setParameter("projectKey", searchParams.getProject().getKey());
		}
		if (searchParams.getArtefactKeys() != null && !searchParams.getArtefactKeys().isEmpty()) {
			query.setParameter("artefactKeys", searchParams.getArtefactKeys());
		}
		if (searchParams.getExcludedArtefactKeys() != null && !searchParams.getExcludedArtefactKeys().isEmpty()) {
			query.setParameter("excludedArtefactKeys", searchParams.getExcludedArtefactKeys());
		}
		if (searchParams.getTypes() != null && !searchParams.getTypes().isEmpty()) {
			query.setParameter("types", searchParams.getTypes());
		}
		if (searchParams.getStatus() != null && !searchParams.getStatus().isEmpty()) {
			query.setParameter("status", searchParams.getStatus());
		}
	}
	
	public List<ProjArtefact> loadQuickSearchArtefacts(ProjProjectRef project, Identity identity) {
		QueryBuilder sb = new QueryBuilder();
		sb.append("select artefact");
		sb.append("  from projartefact artefact");
		sb.append("       inner join (");
		sb.append("         select activity.artefact.key as artefactKey");
		sb.append("              , max(activity.creationDate) as creationDate");
		sb.append("           from projactivity activity");
		sb.append("          where activity.project.key = :projectKey");
		sb.append("            and activity.artefact.key is not null");
		sb.append("            and activity.doer.key = :identityKey");
		sb.append("            and activity.action").in(ProjActivity.QUICK_START_ACTIONS);
		sb.append("          group by activity.artefact.key");
		sb.append("       ) as latestactivity on latestactivity.artefactKey = artefact.key ");
		sb.and().append("artefact.project.key = :projectKey");
		sb.and().append("artefact.status = '").append(ProjectStatus.active.name()).append("'");
		sb.and();
		sb.append("(");
		sb.append("artefact.creator.key = :identityKey");
		sb.append(" or ");
		sb.append("artefact.baseGroup.key in (");
		sb.append("select membership.group.key");
		sb.append("  from bgroupmember as membership");
		sb.append(" where membership.group.key = artefact.baseGroup.key");
		sb.append("   and membership.identity.key = :identityKey");
		sb.append(")");
		sb.append(")");
		sb.orderBy().append(" latestactivity.creationDate desc");
		
		return dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), ProjArtefact.class)
				.setParameter("projectKey", project.getKey())
				.setParameter("identityKey", identity.getKey())
				.setMaxResults(6)
				.getResultList();
	}

}
