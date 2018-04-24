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

import javax.persistence.TypedQuery;

import org.olat.basesecurity.IdentityRef;
import org.olat.basesecurity.Organisation;
import org.olat.basesecurity.OrganisationRef;
import org.olat.basesecurity.OrganisationService;
import org.olat.basesecurity.OrganisationType;
import org.olat.basesecurity.model.OrganisationImpl;
import org.olat.basesecurity.model.OrganisationMember;
import org.olat.core.commons.persistence.DB;
import org.olat.core.id.Identity;
import org.olat.core.util.StringHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 
 * Initial date: 9 févr. 2018<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
@Service
public class OrganisationDAO {
	
	@Autowired
	private DB dbInstance;
	@Autowired
	private GroupDAO groupDao;
	
	public Organisation create(String displayName, String identifier, String description,
			Organisation parentOrganisation, OrganisationType type) {
		OrganisationImpl organisation = new OrganisationImpl();
		organisation.setCreationDate(new Date());
		organisation.setLastModified(organisation.getCreationDate());
		organisation.setDisplayName(displayName);
		organisation.setIdentifier(identifier);
		organisation.setDescription(description);
		organisation.setParent(parentOrganisation);
		if(parentOrganisation != null && parentOrganisation.getRoot() != null) {
			organisation.setRoot(parentOrganisation.getRoot());
		} else {
			organisation.setRoot(parentOrganisation);
		}
		organisation.setType(type);
		return organisation;
	}
	
	public Organisation createAndPersistOrganisation(String displayName, String identifier, String description,
			Organisation parentOrganisation, OrganisationType type) {
		OrganisationImpl organisation = (OrganisationImpl)create(displayName, identifier, description, parentOrganisation, type);
		organisation.setGroup(groupDao.createGroup());
		dbInstance.getCurrentEntityManager().persist(organisation);
		organisation.setMaterializedPathKeys(getMaterializedPathKeys(parentOrganisation, organisation));
		organisation = dbInstance.getCurrentEntityManager().merge(organisation);
		return organisation;
	}
	
	private String getMaterializedPathKeys(Organisation parent, Organisation level) {
		if(parent != null) {
			String parentPathOfKeys = parent.getMaterializedPathKeys();
			if(parentPathOfKeys == null || "/".equals(parentPathOfKeys)) {
				parentPathOfKeys = "";
			}
			return parentPathOfKeys + level.getKey() + "/";
		}
		return "/" + level.getKey() + "/";
	}
	
	public Organisation update(Organisation organisation) {
		if(organisation.getKey() == null) {
			OrganisationImpl orgImpl = (OrganisationImpl)organisation;
			if(orgImpl.getGroup() == null) {
				orgImpl.setGroup(groupDao.createGroup());
			}
			if(orgImpl.getCreationDate() == null) {
				orgImpl.setCreationDate(new Date());
			}
			if(orgImpl.getLastModified() == null) {
				orgImpl.setLastModified(orgImpl.getCreationDate());
			}
			dbInstance.getCurrentEntityManager().persist(orgImpl);
			orgImpl.setMaterializedPathKeys(getMaterializedPathKeys(orgImpl.getParent(), organisation));
			organisation = dbInstance.getCurrentEntityManager().merge(orgImpl);
		} else {
			((OrganisationImpl)organisation).setLastModified(new Date());
		}
		
		return dbInstance.getCurrentEntityManager().merge(organisation);
	}
	
	/**
	 * The method fetch the group, the organisation type and the parent
	 * organisation but not the root.
	 * 
	 * @param key the primary key of an organisation
	 * @return The organisation or null if not found
	 */
	public Organisation loadByKey(Long key) {
		StringBuilder sb = new StringBuilder(256);
		sb.append("select org from organisation org")
		  .append(" inner join fetch org.group baseGroup")
		  .append(" left join fetch org.type orgType")
		  .append(" left join fetch org.parent parentOrg")
		  .append(" where org.key=:key");
		
		List<Organisation> organisations = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), Organisation.class)
				.setParameter("key", key)
				.getResultList();
		return organisations == null || organisations.isEmpty() ? null : organisations.get(0);
	}
	
	public List<Organisation> loadDefaultOrganisation() {
		return loadByIdentifier(OrganisationService.DEFAULT_ORGANISATION_IDENTIFIER);
	}
	
	public List<Organisation> loadByIdentifier(String identifier) {
		StringBuilder sb = new StringBuilder(256);
		sb.append("select org from organisation org")
		  .append(" inner join fetch org.group baseGroup")
		  .append(" left join fetch org.type orgType")
		  .append(" left join fetch org.parent parentOrg")
		  .append(" where org.identifier=:identifier");
		return dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), Organisation.class)
				.setParameter("identifier", identifier)
				.getResultList();
	}
	
	public List<Organisation> find() {
		StringBuilder sb = new StringBuilder(256);
		sb.append("select org from organisation org")
		  .append(" inner join fetch org.group baseGroup")
		  .append(" left join fetch org.type orgType")
		  .append(" left join fetch org.parent parentOrg");
		return dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), Organisation.class)
				.getResultList();
	}
	
	public List<OrganisationMember> getMembers(OrganisationRef organisation) {
		StringBuilder sb = new StringBuilder(256);
		sb.append("select ident, membership.role from organisation org")
		  .append(" inner join org.group baseGroup")
		  .append(" inner join baseGroup.members membership")
		  .append(" inner join membership.identity ident")
		  .append(" where org.key=:organisationKey");
		List<Object[]> objects = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), Object[].class)
				.setParameter("organisationKey", organisation.getKey())
				.getResultList();
		List<OrganisationMember> members = new ArrayList<>(objects.size());
		for(Object[] object:objects) {
			Identity identity = (Identity)object[0];
			String role = (String)object[1];
			members.add(new OrganisationMember(identity, role));
		}
		return members;
	}
	
	public List<Identity> getIdentities(String organisationIdentifier, String role) {
		StringBuilder sb = new StringBuilder(256);
		sb.append("select ident from organisation org")
		  .append(" inner join org.group baseGroup")
		  .append(" inner join baseGroup.members membership")
		  .append(" inner join membership.identity ident")
		  .append(" inner join fetch ident.user user")
		  .append(" where org.identifier=:organisationIdentifier and membership.role=:role");
		return dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), Identity.class)
				.setParameter("organisationIdentifier", organisationIdentifier)
				.setParameter("role", role)
				.getResultList();
	}
	
	public List<Identity> getIdentities(String role) {
		StringBuilder sb = new StringBuilder(256);
		sb.append("select ident from organisation org")
		  .append(" inner join org.group baseGroup")
		  .append(" inner join baseGroup.members membership")
		  .append(" inner join membership.identity ident")
		  .append(" inner join fetch ident.user user")
		  .append(" where membership.role=:role");
		return dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), Identity.class)
				.setParameter("role", role)
				.getResultList();
	}
	
	public List<Organisation> getOrganisations(IdentityRef identity, List<String> roleList) {
		StringBuilder sb = new StringBuilder(256);
		sb.append("select org from organisation org")
		  .append(" inner join fetch org.group baseGroup")
		  .append(" inner join baseGroup.members membership")
		  .append(" where membership.identity.key=:identityKey and membership.role in (:roles)");
		return dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), Organisation.class)
				.setParameter("identityKey", identity.getKey())
				.setParameter("roles", roleList)
				.getResultList();
	}
	
	public boolean hasRole(IdentityRef identity, String organisationIdentifier, String... role) {
		StringBuilder sb = new StringBuilder(256);
		sb.append("select membership.key from organisation org")
		  .append(" inner join org.group baseGroup")
		  .append(" inner join baseGroup.members membership")
		  .append(" where membership.identity.key=:identityKey ");
		
		boolean hasRole = role != null && role.length > 0 && role[0] != null;
		if(hasRole) {
			sb.append(" and membership.role=:role");
		}
		if(StringHelper.containsNonWhitespace(organisationIdentifier)) {
			sb.append(" and org.identifier=:identifier");
		}
		TypedQuery<Long> query = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), Long.class)
				.setParameter("identityKey", identity.getKey());
		if(hasRole) {
			List<String> roleList = new ArrayList<>(role.length);
			for(String r:role) {
				if(StringHelper.containsNonWhitespace(r)) {
					roleList.add(r);
				}
			}
			query.setParameter("role", roleList);
		}
		if(StringHelper.containsNonWhitespace(organisationIdentifier)) {
			query.setParameter("identifier", organisationIdentifier);
		}		
	
		List<Long> memberships = query.setFirstResult(0)
			.setMaxResults(1)
			.getResultList();
		return memberships != null && !memberships.isEmpty() && memberships.get(0) != null && memberships.get(0).longValue() > 0;
	}
}
