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
package org.olat.basesecurity.manager;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.olat.basesecurity.DeletedIdentitiesQueries;
import org.olat.basesecurity.IdentityImpl;
import org.olat.basesecurity.model.DeletedIdentity;
import org.olat.core.commons.persistence.DB;
import org.olat.core.commons.persistence.SortKey;
import org.olat.core.id.Identity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 
 * Initial date: 22 mai 2018<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
@Service
public class DeletedIdentitiesQueriesImpl implements DeletedIdentitiesQueries {
	
	@Autowired
	private DB dbInstance;

	@Override
	public int countDeletedIdentities() {
		StringBuilder sb = new StringBuilder(500);
		sb.append("select count(ident.key) from ").append(IdentityImpl.class.getCanonicalName()).append(" as ident ")
		  .append(" where ident.status=:status");
		
		List<Long> count = dbInstance.getCurrentEntityManager()
			.createQuery(sb.toString(), Long.class)
			.setParameter("status", Identity.STATUS_DELETED)
			.getResultList();
		return count == null || count.isEmpty() || count.get(0) == null ? 0 : count.get(0).intValue();
	}

	@Override
	public List<DeletedIdentity> getIdentitiesByPowerSearch(int firstResult, int maxResults, SortKey orderBy) {
		// username, firstname, lastname, deleted date, last login date, created date, deletedby, deletedRoles
		
		StringBuilder sb = new StringBuilder(500);
		sb.append("select ident.key, ident.name, ident.deletedDate, ident.lastLogin,")
		  .append(" ident.creationDate, ident.deletedRoles, ident.deletedBy,")
		  .append(" identUser.firstName, identUser.lastName")
		  .append(" from ").append(IdentityImpl.class.getCanonicalName()).append(" as ident ")
		  .append(" left join ident.user as identUser")
		  .append(" where ident.status=:status");
		orderBy(sb, orderBy);
		
		List<Object[]> rawObjects = dbInstance.getCurrentEntityManager()
			.createQuery(sb.toString(), Object[].class)
			.setParameter("status", Identity.STATUS_DELETED)
			.setFirstResult(firstResult)
			.setMaxResults(maxResults)
			.getResultList();
		
		List<DeletedIdentity> identities = new ArrayList<>(rawObjects.size());
		for(Object[] rawObject:rawObjects) {
			int pos = 0;
			Long identityKey = (Long)rawObject[pos++];
			String identityName = (String)rawObject[pos++];
			Date deletedDate = (Date)rawObject[pos++];
			Date lastLogin = (Date)rawObject[pos++];
			Date creationDate = (Date)rawObject[pos++];
			String deletedRoles = (String)rawObject[pos++];
			String deletedBy = (String)rawObject[pos++];

			String identityFirstName = (String)rawObject[pos++];
			String identityLastName = (String)rawObject[pos++];

			identities.add(new DeletedIdentity(identityKey, identityName, identityFirstName, identityLastName,
					deletedDate, lastLogin, creationDate, deletedRoles, deletedBy));
		}
		return identities;
	}
	
	private void orderBy(StringBuilder sb, SortKey orderBy) {
		if(orderBy == null) return;
		
		switch(orderBy.getKey()) {
			case "username":
				sb.append(" order by ident.name ").append(orderBy.isAsc() ? "asc" : "desc");
				break;
			case "creationDate":
				sb.append(" order by ident.creationDate ").append(orderBy.isAsc() ? "asc" : "desc");
				break;
			case "lastLogin":
				sb.append(" order by ident.lastLogin ").append(orderBy.isAsc() ? "asc" : "desc");
				break;
			case "deletedDate":
				sb.append(" order by ident.deletedDate ").append(orderBy.isAsc() ? "asc" : "desc");
				break;		
			case "firstName":
				sb.append(" order by lower(identUser.firstName), lower(identUser.lastName) ").append(orderBy.isAsc() ? "asc" : "desc");
				break;
			case "lastName":
				sb.append(" order by lower(identUser.lastName), lower(identUser.firstName) ").append(orderBy.isAsc() ? "asc" : "desc");
				break;
			case "deletedBy":
				sb.append(" order by lower(ident.deletedBy), lower(identUser.lastName) ").append(orderBy.isAsc() ? "asc" : "desc");
				break;
			default:
				sb.append(" order by ident.key ").append(orderBy.isAsc() ? "asc" : "desc");
				break;
		}
	}
}
