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
package org.olat.modules.catalog.manager;

import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.persistence.TypedQuery;

import org.olat.basesecurity.model.OrganisationRefImpl;
import org.olat.core.commons.persistence.DB;
import org.olat.core.commons.persistence.QueryBuilder;
import org.olat.core.id.Organisation;
import org.olat.core.id.OrganisationRef;
import org.olat.modules.catalog.CatalogLauncher;
import org.olat.modules.catalog.CatalogLauncherRef;
import org.olat.modules.catalog.CatalogLauncherToOrganisation;
import org.olat.modules.catalog.model.CatalogLauncherToOrganisationImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 
 * Initial date: 25.08.2022<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
@Service
public class CatalogLauncherToOrganisationDAO {
	
	@Autowired
	private DB dbInstance;
	
	public CatalogLauncherToOrganisation createRelation(CatalogLauncher launcher, Organisation organisation) {
		CatalogLauncherToOrganisationImpl relation = new CatalogLauncherToOrganisationImpl();
		relation.setCreationDate(new Date());
		relation.setLastModified(relation.getCreationDate());
		relation.setLauncher(launcher);
		relation.setOrganisation(organisation);
		dbInstance.getCurrentEntityManager().persist(relation);
		return relation;
	}
	
	public List<OrganisationRef> getOrganisationReferences(CatalogLauncherRef re) {
		StringBuilder sb = new StringBuilder(255);
		sb.append("select org.key from cataloglaunchertoorganisation as launcherToOrganisation")
		  .append(" inner join launcherToOrganisation.organisation org")
		  .append(" where launcherToOrganisation.launcher.key=:launcherKey");
		
		List<Long> keys = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), Long.class)
				.setParameter("launcherKey", re.getKey())
				.getResultList();
		return keys.stream().map(OrganisationRefImpl::new)
				.collect(Collectors.toList());
	}
	
	public List<CatalogLauncherToOrganisation> loadRelations(CatalogLauncherRef launcher, OrganisationRef organisation) {
		QueryBuilder sb = new QueryBuilder(255);
		sb.append("select launcherToOrganisation from cataloglaunchertoorganisation as launcherToOrganisation");
		sb.append(" inner join fetch launcherToOrganisation.launcher launcher");
		sb.append(" inner join fetch launcherToOrganisation.organisation org");
		if (launcher != null) {
			sb.and().append("launcher.key=:launcherKey");
		}
		if (organisation != null) {
			sb.and().append("org.key=:organisationKey");
		}
		
		TypedQuery<CatalogLauncherToOrganisation> query = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), CatalogLauncherToOrganisation.class);
		if (launcher != null) {
			query.setParameter("launcherKey", launcher.getKey());
		}
		if (organisation != null) {
			query.setParameter("organisationKey", organisation.getKey());
		}
		
		return query.getResultList();
	}
	
	public List<CatalogLauncherToOrganisation> loadRelations(Collection<? extends CatalogLauncherRef> launchers) {
		QueryBuilder sb = new QueryBuilder(255);
		sb.append("select launcherToOrganisation from cataloglaunchertoorganisation as launcherToOrganisation");
		sb.append(" inner join fetch launcherToOrganisation.organisation org");
		sb.and().append("launcherToOrganisation.launcher.key in :launcherKeys");
		
		List<Long> launcherKeys = launchers.stream().map(CatalogLauncherRef::getKey).collect(Collectors.toList());
		return dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), CatalogLauncherToOrganisation.class)
				.setParameter("launcherKeys", launcherKeys)
				.getResultList();
	}
	
	public Map<Long, List<Long>> getCatalogLauncherKeyToOrganisations(Collection<? extends CatalogLauncherRef> launchers) {
		return loadRelations(launchers).stream()
				.collect(Collectors.groupingBy(
						oto -> oto.getLauncher().getKey(),
						Collectors.mapping(oto -> oto.getOrganisation().getKey(), Collectors.toList())));
	}
	
	public List<Organisation> loadOrganisations(CatalogLauncherRef launcher) {
		QueryBuilder sb = new QueryBuilder(255);
		sb.append("select launcherToOrganisation.organisation from cataloglaunchertoorganisation as launcherToOrganisation");
		if (launcher != null) {
			sb.and().append("launcherToOrganisation.launcher.key=:launcherKey");
		}
		
		TypedQuery<Organisation> query = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), Organisation.class);
		if (launcher != null) {
			query.setParameter("launcherKey", launcher.getKey());
		}
		
		return query.getResultList();
	}
	
	public void delete(CatalogLauncherRef re, OrganisationRef organisation) {
		List<CatalogLauncherToOrganisation> relations = loadRelations(re, organisation);
		for(CatalogLauncherToOrganisation relation:relations) {
			dbInstance.getCurrentEntityManager().remove(relation);
		}
	}
	
	public void delete(CatalogLauncherToOrganisation relation) {
		QueryBuilder sb = new QueryBuilder(255);
		sb.append("delete from cataloglaunchertoorganisation as launcherToOrganisation");
		sb.and().append("launcherToOrganisation.launcher.key=:launcherKey");
		sb.and().append("launcherToOrganisation.organisation.key=:organisationKey");
		
		dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString())
				.setParameter("launcherKey", relation.getLauncher().getKey())
				.setParameter("organisationKey", relation.getOrganisation().getKey())
				.executeUpdate();
	}
	
	public int delete(CatalogLauncherRef launcher) {
		String query = "delete from cataloglaunchertoorganisation as launcherToOrganisation where launcherToOrganisation.launcher.key=:launcherKey";
		return dbInstance.getCurrentEntityManager()
				.createQuery(query)
				.setParameter("launcherKey", launcher.getKey())
				.executeUpdate();
	}

}
