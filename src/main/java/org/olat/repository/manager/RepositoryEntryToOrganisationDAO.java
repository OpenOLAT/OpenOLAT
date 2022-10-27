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
package org.olat.repository.manager;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.olat.basesecurity.model.OrganisationRefImpl;
import org.olat.core.commons.persistence.DB;
import org.olat.core.commons.persistence.QueryBuilder;
import org.olat.core.id.Organisation;
import org.olat.core.id.OrganisationRef;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryEntryRef;
import org.olat.repository.RepositoryEntryToOrganisation;
import org.olat.repository.model.RepositoryEntryRefImpl;
import org.olat.repository.model.RepositoryEntryToOrganisationImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 
 * Initial date: 25 avr. 2018<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
@Service
public class RepositoryEntryToOrganisationDAO {
	
	@Autowired
	private DB dbInstance;

	public RepositoryEntryToOrganisation createRelation(Organisation organisation, RepositoryEntry re, boolean master) {
		RepositoryEntryToOrganisationImpl relation = new RepositoryEntryToOrganisationImpl();
		relation.setCreationDate(new Date());
		relation.setLastModified(relation.getCreationDate());
		relation.setMaster(master);
		relation.setOrganisation(organisation);
		relation.setEntry(re);
		dbInstance.getCurrentEntityManager().persist(relation);
		return relation;
	}
	
	public List<OrganisationRef> getOrganisationReferences(RepositoryEntryRef re) {
		StringBuilder sb = new StringBuilder(255);
		sb.append("select org.key from repoentrytoorganisation as reToOrganisation")
		  .append(" inner join reToOrganisation.organisation org")
		  .append(" where reToOrganisation.entry.key=:entryKey");
		
		List<Long> keys = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), Long.class)
				.setParameter("entryKey", re.getKey())
				.getResultList();
		return keys.stream().map(OrganisationRefImpl::new)
				.collect(Collectors.toList());
	}
	
	public List<RepositoryEntryToOrganisation> getRelations(RepositoryEntryRef re, OrganisationRef organisation) {
		StringBuilder sb = new StringBuilder(255);
		sb.append("select reToOrganisation from repoentrytoorganisation as reToOrganisation")
		  .append(" inner join fetch reToOrganisation.organisation org")
		  .append(" inner join fetch reToOrganisation.entry v")
		  .append(" where v.key=:entryKey and org.key=:organisationKey");
		
		return dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), RepositoryEntryToOrganisation.class)
				.setParameter("entryKey", re.getKey())
				.setParameter("organisationKey", organisation.getKey())
				.getResultList();
	}
	
	public Map<RepositoryEntryRef, List<Organisation>> getRepositoryEntryOrganisations(Collection<? extends RepositoryEntryRef> entries) {
		StringBuilder sb = new StringBuilder(255);
		sb.append("select reToOrganisation")
		  .append(" from repoentrytoorganisation as reToOrganisation")
		  .append(" inner join fetch reToOrganisation.organisation")
		  .append(" where reToOrganisation.entry.key in :entryKeys");
		
		List<RepositoryEntryToOrganisation> reToOrgs = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), RepositoryEntryToOrganisation.class)
				.setParameter("entryKeys", entries.stream().map(RepositoryEntryRef::getKey).collect(Collectors.toList()))
				.getResultList();
		
		Map<RepositoryEntryRef, List<Organisation>> entryRefToOrganisation = new HashMap<>();
		for (RepositoryEntryToOrganisation reToOrg : reToOrgs) {
			RepositoryEntryRefImpl entryRef = new RepositoryEntryRefImpl(reToOrg.getEntry().getKey());
			entryRefToOrganisation.computeIfAbsent(entryRef, k -> new ArrayList<>()).add(reToOrg.getOrganisation());
		}
		return entryRefToOrganisation;
	}
	
	public void delete(RepositoryEntryRef re, OrganisationRef organisation) {
		List<RepositoryEntryToOrganisation> relations = getRelations(re, organisation);
		for(RepositoryEntryToOrganisation relation:relations) {
			dbInstance.getCurrentEntityManager().remove(relation);
		}
	}
	
	public void delete(RepositoryEntryToOrganisation relation) {
		QueryBuilder sb = new QueryBuilder(255);
		sb.append("delete from repoentrytoorganisation as reToOrganisation");
		sb.and().append("reToOrganisation.entry.key=:entryKey");
		sb.and().append("reToOrganisation.organisation.key=:organisationKey");
		
		dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString())
				.setParameter("entryKey", relation.getEntry().getKey())
				.setParameter("organisationKey", relation.getOrganisation().getKey())
				.executeUpdate();
	}
	
	public int delete(RepositoryEntryRef entry) {
		String query = "delete from repoentrytoorganisation as reToOrganisation where reToOrganisation.entry.key=:entryKey";
		return dbInstance.getCurrentEntityManager()
				.createQuery(query)
				.setParameter("entryKey", entry.getKey())
				.executeUpdate();
	}
	
	public int delete(OrganisationRef organisation) {
		String query = "delete from repoentrytoorganisation as reToOrganisation where reToOrganisation.organisation.key=:organisationKey";
		return dbInstance.getCurrentEntityManager()
				.createQuery(query)
				.setParameter("organisationKey", organisation.getKey())
				.executeUpdate();
	}

}
