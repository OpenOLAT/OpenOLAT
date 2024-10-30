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

import java.util.Collection;
import java.util.Date;
import java.util.List;

import jakarta.persistence.TypedQuery;

import org.olat.basesecurity.OrganisationEmailDomain;
import org.olat.basesecurity.OrganisationEmailDomainSearchParams;
import org.olat.basesecurity.OrganisationRoles;
import org.olat.basesecurity.model.OrganisationEmailDomainImpl;
import org.olat.basesecurity.model.OrganisationIdentityEmail;
import org.olat.core.commons.persistence.DB;
import org.olat.core.commons.persistence.QueryBuilder;
import org.olat.core.id.Organisation;
import org.olat.core.id.OrganisationRef;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 
 * Initial date: 21 Oct 2024<br>
 * @author uhensler, urs.hensler@frentix.com, https://www.frentix.com
 *
 */
@Service
public class OrganisationEmailDomainDAO {
	
	@Autowired
	private DB dbInstance;
	
	public OrganisationEmailDomain create(Organisation organisation, String domain) {
		OrganisationEmailDomainImpl emailDomain = new OrganisationEmailDomainImpl();
		emailDomain.setCreationDate(new Date());
		emailDomain.setLastModified(emailDomain.getCreationDate());
		emailDomain.setOrganisation(organisation);
		emailDomain.setDomain(domain);
		emailDomain.setEnabled(true);
		emailDomain.setSubdomainsAllowed(false);
		dbInstance.getCurrentEntityManager().persist(emailDomain);
		return emailDomain;
	}
	
	public OrganisationEmailDomain update(OrganisationEmailDomain emailDomain) {
		((OrganisationEmailDomainImpl)emailDomain).setLastModified(new Date());
		return dbInstance.getCurrentEntityManager().merge(emailDomain);
	}

	public void delete(OrganisationEmailDomain organisationEmailDomain) {
		String query = "delete from organisationemaildomain emaildomain where emaildomain.key = :organisationKey";
		
		dbInstance.getCurrentEntityManager()
				.createQuery(query)
				.setParameter("organisationKey", organisationEmailDomain.getKey())
				.executeUpdate();
	}

	public void delete(OrganisationRef organisation) {
		String query = "delete from organisationemaildomain emaildomain where emaildomain.organisation.key = :organisationKey";
		
		dbInstance.getCurrentEntityManager()
				.createQuery(query)
				.setParameter("organisationKey", organisation.getKey())
				.executeUpdate();
	}
	
	public List<OrganisationEmailDomain> loadEmailDomains(OrganisationEmailDomainSearchParams searchParams) {
		QueryBuilder sb = new QueryBuilder();
		sb.append("select emaildomain");
		sb.append("  from organisationemaildomain emaildomain");
		sb.append("       inner join fetch emaildomain.organisation");
		if (searchParams.getOrganisationKeys() != null && !searchParams.getOrganisationKeys().isEmpty()) {
			sb.and().append("emaildomain.organisation.key in :organisationKeys");
		}
		if (searchParams.getDomains() != null && !searchParams.getDomains().isEmpty()) {
			sb.and().append("emaildomain.domain in :domains");
		}
		if (searchParams.getEnabled() != null) {
			sb.and().append("emaildomain.enabled = ").append(searchParams.getEnabled());
		}
		
		TypedQuery<OrganisationEmailDomain> query = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), OrganisationEmailDomain.class);
		
		if (searchParams.getOrganisationKeys() != null && !searchParams.getOrganisationKeys().isEmpty()) {
			query.setParameter("organisationKeys", searchParams.getOrganisationKeys());
		}
		if (searchParams.getDomains() != null && !searchParams.getDomains().isEmpty()) {
			query.setParameter("domains", searchParams.getDomains());
		}
		
		return query.getResultList();
	}
	
	public List<OrganisationIdentityEmail> getOrganisationIdentityEmails(Collection<Long> organisationKeys) {
		QueryBuilder sb = new QueryBuilder();
		sb.append("select new org.olat.basesecurity.model.OrganisationIdentityEmail(");
		sb.append("       org.key");
		sb.append("     , ident.key");
		sb.append("     , user.email");
		sb.append("     )");
		sb.append("  from organisation org");
		sb.append("       inner join org.group baseGroup");
		sb.append("       inner join baseGroup.members membership");
		sb.append("       inner join membership.identity ident");
		sb.append("       inner join ident.user user");
		sb.and().append("org.key in :organisationKeys");
		sb.and().append("membership.role = :role");
		sb.and().append("user.email is not null");
		
		return dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), OrganisationIdentityEmail.class)
				.setParameter("organisationKeys", organisationKeys)
				.setParameter("role", OrganisationRoles.user.name())
				.getResultList();
	}

}
