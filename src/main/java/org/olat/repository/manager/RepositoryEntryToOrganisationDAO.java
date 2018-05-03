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

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import org.olat.basesecurity.model.OrganisationRefImpl;
import org.olat.core.commons.persistence.DB;
import org.olat.core.id.Organisation;
import org.olat.core.id.OrganisationRef;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryEntryRef;
import org.olat.repository.RepositoryEntryToOrganisation;
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
	
	public void delete(RepositoryEntryToOrganisation relation) {
		dbInstance.getCurrentEntityManager().remove(relation);
	}
	
	public int delete(RepositoryEntryRef entry) {
		String query = "delete from repoentrytoorganisation as reToOrganisation where reToOrganisation.entry.key=:entryKey";
		return dbInstance.getCurrentEntityManager()
				.createQuery(query)
				.setParameter("entryKey", entry.getKey())
				.executeUpdate();
	}

}
