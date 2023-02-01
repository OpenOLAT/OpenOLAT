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
import java.util.Set;
import java.util.stream.Collectors;

import jakarta.persistence.TypedQuery;

import org.olat.basesecurity.Group;
import org.olat.basesecurity.IdentityRef;
import org.olat.core.commons.persistence.DB;
import org.olat.core.commons.persistence.QueryBuilder;
import org.olat.core.id.Identity;
import org.olat.core.id.OrganisationRef;
import org.olat.modules.project.ProjProject;
import org.olat.modules.project.ProjProjectRef;
import org.olat.modules.project.ProjProjectSearchParams;
import org.olat.modules.project.ProjectStatus;
import org.olat.modules.project.model.ProjProjectImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 
 * Initial date: 22 Nov 2022<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
@Component
public class ProjProjectDAO {
	
	@Autowired
	private DB dbInstance;
	
	public ProjProject create(Identity creator, Group baseGroup) {
		ProjProjectImpl project = new ProjProjectImpl();
		project.setCreationDate(new Date());
		project.setLastModified(project.getCreationDate());
		project.setStatus(ProjectStatus.active);
		project.setCreator(creator);
		project.setBaseGroup(baseGroup);
		dbInstance.getCurrentEntityManager().persist(project);
		return project;
	}
	
	public ProjProject save(ProjProject project) {
		if (project instanceof ProjProjectImpl) {
			ProjProjectImpl impl = (ProjProjectImpl)project;
			impl.setLastModified(new Date());
			dbInstance.getCurrentEntityManager().merge(project);
		}
		return project;
	}

	public void delete(ProjProjectRef project) {
		QueryBuilder sb = new QueryBuilder();
		sb.append("delete from projproject project");
		sb.and().append(" project.key = :projectKey");
		
		dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString())
				.setParameter("projectKey", project.getKey())
				.executeUpdate();
	}

	public ProjProject loadProject(Group baseGroup) {
		QueryBuilder sb = new QueryBuilder();
		sb.append("select project");
		sb.append("  from projproject project");
		sb.and().append(" project.baseGroup.key = :groupKey");
		
		 List<ProjProject> projects = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), ProjProject.class)
				.setParameter("groupKey", baseGroup.getKey())
				.getResultList();
		
		return projects != null && !projects.isEmpty()? projects.get(0): null;
	}

	public List<ProjProject> loadProjects(ProjProjectSearchParams params) {
		QueryBuilder sb = new QueryBuilder();
		sb.append("select project");
		sb.append("  from projproject project");
		appendIdentitySubSelect(sb, params.getIdentity(), params.getProjectOrganisations());
		appendQuery(sb, params);
		
		TypedQuery<ProjProject> query = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), ProjProject.class);
		addParameters(query, params);
		
		return query.getResultList();
	}

	private void appendIdentitySubSelect(QueryBuilder sb, IdentityRef identity, List<OrganisationRef> projectOrganisations) {
		if (identity == null && (projectOrganisations == null || projectOrganisations.isEmpty())) {
			return;
		}
		
		boolean or = false;
		sb.and().append("(");
		if (identity != null) {
			sb.append("project.key in (");
			sb.append("select distinct project2.key");
			sb.append("  from projproject project2");
			sb.append("       join bgroupmember as membership");
			sb.append("         on project2.baseGroup.key = membership.group.key");
			sb.append(" where membership.identity.key = :identityKey");
			sb.append(")");
			or = true;
		}
		if (projectOrganisations != null && !projectOrganisations.isEmpty()) {
			if (or) {
				sb.append(" or ");
			}
			sb.append("project.key in (");
			sb.append("select distinct projtoorg.project.key");
			sb.append("  from projprojecttoorganisation projtoorg");
			sb.append(" where projtoorg.organisation.key in :organisationKeys");
			sb.append(")");
		}
		sb.append(")");
	}

	private void appendQuery(QueryBuilder sb, ProjProjectSearchParams params) {
		if (params.getProjectKeys() != null && !params.getProjectKeys().isEmpty()) {
			sb.and().append("project.key in :projectKeys");
		}
		if (params.getStatus() != null && !params.getStatus().isEmpty()) {
			sb.and().append("project.status in :status");
		}
	}

	private void addParameters(TypedQuery<?> query, ProjProjectSearchParams params) {
		if (params.getIdentity() != null) {
			query.setParameter("identityKey", params.getIdentity().getKey());
		}
		if (params.getProjectOrganisations() != null && !params.getProjectOrganisations().isEmpty()) {
			Set<Long> organisationKeys = params.getProjectOrganisations().stream().map(OrganisationRef::getKey).collect(Collectors.toSet());
			query.setParameter("organisationKeys", organisationKeys);
		}
		if (params.getProjectKeys() != null && !params.getProjectKeys().isEmpty()) {
			query.setParameter("projectKeys", params.getProjectKeys());
		}
		if (params.getStatus() != null && !params.getStatus().isEmpty()) {
			query.setParameter("status", params.getStatus());
		}
	}

}
