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

import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import jakarta.persistence.TypedQuery;

import org.olat.basesecurity.model.OrganisationRefImpl;
import org.olat.core.commons.persistence.DB;
import org.olat.core.commons.persistence.QueryBuilder;
import org.olat.core.id.Organisation;
import org.olat.core.id.OrganisationRef;
import org.olat.modules.project.ProjProject;
import org.olat.modules.project.ProjProjectRef;
import org.olat.modules.project.ProjTemplateToOrganisation;
import org.olat.modules.project.model.ProjTemplateToOrganisationImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 
 * Initial date: 12 Jun 2023<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
@Service
public class ProjTemplateToOrganisationDAO {
	
	@Autowired
	private DB dbInstance;
	
	public ProjTemplateToOrganisation createRelation(ProjProject project, Organisation organisation) {
		ProjTemplateToOrganisationImpl relation = new ProjTemplateToOrganisationImpl();
		relation.setCreationDate(new Date());
		relation.setProject(project);
		relation.setOrganisation(organisation);
		dbInstance.getCurrentEntityManager().persist(relation);
		return relation;
	}
	
	public List<OrganisationRef> getOrganisationReferences(ProjProjectRef re) {
		StringBuilder sb = new StringBuilder(255);
		sb.append("select org.key from projtemplatetoorganisation as templateToOrganisation")
		  .append(" inner join templateToOrganisation.organisation org")
		  .append(" where templateToOrganisation.project.key=:projectKey");
		
		List<Long> keys = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), Long.class)
				.setParameter("projectKey", re.getKey())
				.getResultList();
		return keys.stream().map(OrganisationRefImpl::new)
				.collect(Collectors.toList());
	}
	
	public List<ProjTemplateToOrganisation> loadRelations(ProjProjectRef project, OrganisationRef organisation) {
		QueryBuilder sb = new QueryBuilder(255);
		sb.append("select templateToOrganisation from projtemplatetoorganisation as templateToOrganisation");
		sb.append(" inner join fetch templateToOrganisation.project project");
		sb.append(" inner join fetch templateToOrganisation.organisation org");
		if (project != null) {
			sb.and().append("project.key=:projectKey");
		}
		if (organisation != null) {
			sb.and().append("org.key=:organisationKey");
		}
		
		TypedQuery<ProjTemplateToOrganisation> query = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), ProjTemplateToOrganisation.class);
		if (project != null) {
			query.setParameter("projectKey", project.getKey());
		}
		if (organisation != null) {
			query.setParameter("organisationKey", organisation.getKey());
		}
		
		return query.getResultList();
	}
	
	public List<ProjTemplateToOrganisation> loadRelations(Collection<? extends ProjProjectRef> projects) {
		QueryBuilder sb = new QueryBuilder(255);
		sb.append("select templateToOrganisation from projtemplatetoorganisation as templateToOrganisation");
		sb.append(" inner join fetch templateToOrganisation.organisation org");
		sb.and().append("templateToOrganisation.project.key in :projectKeys");
		
		List<Long> projectKeys = projects.stream().map(ProjProjectRef::getKey).collect(Collectors.toList());
		return dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), ProjTemplateToOrganisation.class)
				.setParameter("projectKeys", projectKeys)
				.getResultList();
	}
	
	public Map<Long, List<Long>> getProjProjectKeyToOrganisations(Collection<? extends ProjProjectRef> projects) {
		return loadRelations(projects).stream()
				.collect(Collectors.groupingBy(
						oto -> oto.getProject().getKey(),
						Collectors.mapping(oto -> oto.getOrganisation().getKey(), Collectors.toList())));
	}
	
	public List<Organisation> loadOrganisations(ProjProjectRef project) {
		QueryBuilder sb = new QueryBuilder(255);
		sb.append("select templateToOrganisation.organisation from projtemplatetoorganisation as templateToOrganisation");
		if (project != null) {
			sb.and().append("templateToOrganisation.project.key=:projectKey");
		}
		
		TypedQuery<Organisation> query = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), Organisation.class);
		if (project != null) {
			query.setParameter("projectKey", project.getKey());
		}
		
		return query.getResultList();
	}
	
	public void delete(ProjProjectRef re, OrganisationRef organisation) {
		List<ProjTemplateToOrganisation> relations = loadRelations(re, organisation);
		for (ProjTemplateToOrganisation relation:relations) {
			dbInstance.getCurrentEntityManager().remove(relation);
		}
	}
	
	public void delete(ProjTemplateToOrganisation relation) {
		QueryBuilder sb = new QueryBuilder(255);
		sb.append("delete from projtemplatetoorganisation as templateToOrganisation");
		sb.and().append("templateToOrganisation.project.key=:projectKey");
		sb.and().append("templateToOrganisation.organisation.key=:organisationKey");
		
		dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString())
				.setParameter("projectKey", relation.getProject().getKey())
				.setParameter("organisationKey", relation.getOrganisation().getKey())
				.executeUpdate();
	}
	
	public int delete(ProjProjectRef project) {
		String query = "delete from projtemplatetoorganisation as templateToOrganisation where templateToOrganisation.project.key=:projectKey";
		return dbInstance.getCurrentEntityManager()
				.createQuery(query)
				.setParameter("projectKey", project.getKey())
				.executeUpdate();
	}

}
