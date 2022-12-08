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
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import javax.persistence.TypedQuery;

import org.olat.basesecurity.GroupMembershipInheritance;
import org.olat.basesecurity.IdentityImpl;
import org.olat.basesecurity.IdentityRef;
import org.olat.basesecurity.OrganisationRoles;
import org.olat.basesecurity.OrganisationService;
import org.olat.basesecurity.OrganisationStatus;
import org.olat.basesecurity.OrganisationType;
import org.olat.basesecurity.model.OrganisationImpl;
import org.olat.basesecurity.model.OrganisationMember;
import org.olat.basesecurity.model.OrganisationMembershipStats;
import org.olat.basesecurity.model.OrganisationNode;
import org.olat.basesecurity.model.SearchMemberParameters;
import org.olat.basesecurity.model.SearchOrganisationParameters;
import org.olat.core.commons.persistence.DB;
import org.olat.core.commons.persistence.PersistenceHelper;
import org.olat.core.commons.persistence.QueryBuilder;
import org.olat.core.id.Identity;
import org.olat.core.id.Organisation;
import org.olat.core.id.OrganisationRef;
import org.olat.core.util.StringHelper;
import org.olat.user.propertyhandlers.UserPropertyHandler;
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
		organisation.setStatus(OrganisationStatus.active.name());
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
	
	public String getMaterializedPathKeys(Organisation parent, Organisation level) {
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
	
	public void delete(Organisation organisation) {
		dbInstance.getCurrentEntityManager().remove(organisation);
	}
	
	/**
	 * The method fetch the group, the organisation type and the parent
	 * organisation but not the root.
	 * 
	 * @param organisationsRefs the primary keys of the organisations
	 * @return A list of the found organisations.
	 */
	public List<Organisation> loadByKeys(Collection<? extends OrganisationRef> organisationsRefs) {
		StringBuilder sb = new StringBuilder(256);
		sb.append("select org from organisation org")
		  .append(" inner join fetch org.group baseGroup")
		  .append(" left join fetch org.type orgType")
		  .append(" left join fetch org.parent parentOrg")
		  .append(" where org.key in :keys");
		
		return dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), Organisation.class)
				.setParameter("keys", organisationsRefs.stream().map(OrganisationRef::getKey).collect(Collectors.toList()))
				.getResultList();
	}
	
	/**
	 * Search in identifier, displayName or external ID
	 * 
	 * @param name 
	 * @return
	 */
	public List<Organisation> loadByLabel(String label) {
		StringBuilder sb = new StringBuilder(256);
		sb.append("select org from organisation org")
		  .append(" inner join fetch org.group baseGroup")
		  .append(" left join fetch org.type orgType")
		  .append(" left join fetch org.parent parentOrg")
		  .append(" where lower(org.identifier)=:label or lower(org.displayName)=:label or lower(org.externalId)=:label ");
		return dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), Organisation.class)
				.setParameter("label", label.toLowerCase())
				.getResultList();
	}
	
	public List<Organisation> loadByIdentifier(String identifier) {
		StringBuilder sb = new StringBuilder(256);
		sb.append("select org from organisation org")
		  .append(" inner join fetch org.group baseGroup")
		  .append(" left join fetch org.type orgType")
		  .append(" left join fetch org.parent parentOrg")
		  .append(" where lower(org.identifier)=:identifier");
		return dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), Organisation.class)
				.setParameter("identifier", identifier.toLowerCase())
				.getResultList();
	}
	
	public List<Organisation> loadDefaultOrganisation() {
		QueryBuilder sb = new QueryBuilder(256);
		sb.append("select org from organisation org")
		  .append(" inner join fetch org.group baseGroup")
		  .append(" left join fetch org.type orgType")
		  .append(" left join fetch org.parent parentOrg")
		  .append(" where org.identifier=:identifier ");
		return dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), Organisation.class)
				.setParameter("identifier", OrganisationService.DEFAULT_ORGANISATION_IDENTIFIER)
				.getResultList();
	}
	
	public List<Organisation> find(OrganisationStatus[] status) {
		QueryBuilder sb = new QueryBuilder(256);
		sb.append("select org from organisation org")
		  .append(" inner join fetch org.group baseGroup")
		  .append(" left join fetch org.type orgType")
		  .append(" left join fetch org.parent parentOrg")
		  .append(" where org.status ").in(status);
		return dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), Organisation.class)
				.getResultList();
	}
	
	public long count(OrganisationStatus[] status) {
		QueryBuilder sb = new QueryBuilder(256);
		sb.append("select count(org.key) from organisation org")
		  .append(" where org.status ").in(status);
		List<Long> count = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), Long.class)
				.getResultList();
		return count == null || count.isEmpty() || count.get(0) == null ? 0 : count.get(0).longValue();
	}
	
	public List<Organisation> findOrganisations(SearchOrganisationParameters searchParams) {
		QueryBuilder sb = new QueryBuilder(512);
		sb.append("select org from organisation org")
		  .append(" inner join fetch org.group baseGroup")
		  .append(" left join fetch org.type orgType")
		  .append(" left join fetch org.parent parentOrg");
		
		if(StringHelper.containsNonWhitespace(searchParams.getIdentifier())) {
			sb.and().append("lower(org.identifier)=:identifier");
		}
		if(StringHelper.containsNonWhitespace(searchParams.getExternalId())) {
			sb.and().append("org.externalId=:externalId");
		}

		TypedQuery<Organisation> query = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), Organisation.class);

		if(StringHelper.containsNonWhitespace(searchParams.getIdentifier())) {
			query.setParameter("identifier", searchParams.getIdentifier().toLowerCase());
		}
		if(StringHelper.containsNonWhitespace(searchParams.getExternalId())) {
			query.setParameter("externalId", searchParams.getExternalId());
		}
		
		return query.getResultList();
	}
	
	public List<OrganisationMember> getMembers(OrganisationRef organisation, SearchMemberParameters params) {
		QueryBuilder sb = new QueryBuilder(256);
		sb.append("select ident, membership.role, membership.inheritanceModeString from organisation org")
		  .append(" inner join org.group baseGroup")
		  .append(" inner join baseGroup.members membership")
		  .append(" inner join membership.identity ident")
		  .append(" inner join fetch ident.user user")
		  .append(" where org.key=:organisationKey");
		createUserPropertiesQueryPart(sb, params.getSearchString(), params.getUserProperties());
		
		TypedQuery<Object[]> query = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), Object[].class)
				.setParameter("organisationKey", organisation.getKey());
		createUserPropertiesQueryParameters(query, params.getSearchString());
		
		List<Object[]> objects = query.getResultList();
		List<OrganisationMember> members = new ArrayList<>(objects.size());
		for(Object[] object:objects) {
			Identity identity = (Identity)object[0];
			String role = (String)object[1];
			String inheritanceModeString = (String)object[2];
			GroupMembershipInheritance inheritanceMode = GroupMembershipInheritance.none;
			if(StringHelper.containsNonWhitespace(inheritanceModeString)) {
				inheritanceMode = GroupMembershipInheritance.valueOf(inheritanceModeString);
			}
			members.add(new OrganisationMember(identity, role, inheritanceMode));
		}
		return members;
	}
	
	private void createUserPropertiesQueryPart(QueryBuilder sb, String searchString, List<UserPropertyHandler> handlers) {
		if(!StringHelper.containsNonWhitespace(searchString)) return;
		
		// treat login and userProperties as one element in this query
		sb.append(" and ( ");			
		PersistenceHelper.appendFuzzyLike(sb, "ident.name", "searchString", dbInstance.getDbVendor());
		
		for(UserPropertyHandler handler:handlers) {
			if(handler.getDatabaseColumnName() != null) {
				sb.append(" or ");
				PersistenceHelper.appendFuzzyLike(sb, "user.".concat(handler.getName()), "searchString", dbInstance.getDbVendor());
			}
		}
		sb.append(")");
	}
	
	private void createUserPropertiesQueryParameters(TypedQuery<?> query, String searchString) {
		if(!StringHelper.containsNonWhitespace(searchString)) return;
		
		String fuzzySearch = PersistenceHelper.makeFuzzyQueryString(searchString);
		query.setParameter("searchString", fuzzySearch);
	}
	
	public List<Identity> getMembersIdentity(OrganisationRef organisation, String role) {
		StringBuilder sb = new StringBuilder(256);
		sb.append("select ident from organisation org")
		  .append(" inner join org.group baseGroup")
		  .append(" inner join baseGroup.members membership")
		  .append(" inner join membership.identity ident")
		  .append(" inner join fetch ident.user identUser")
		  .append(" where org.key=:organisationKey and membership.role=:role");
		return dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), Identity.class)
				.setParameter("organisationKey", organisation.getKey())
				.setParameter("role", role)
				.getResultList();
	}
	
	public List<Identity> getNonInheritedMembersIdentity(OrganisationRef organisation, String role) {
		QueryBuilder sb = new QueryBuilder(256);
		sb.append("select ident from organisation org")
		  .append(" inner join org.group baseGroup")
		  .append(" inner join baseGroup.members membership")
		  .append(" inner join membership.identity ident")
		  .append(" inner join fetch ident.user identUser")
		  .append(" where org.key=:organisationKey and membership.role=:role ")
		  .append(" and membership.inheritanceModeString").in(GroupMembershipInheritance.root, GroupMembershipInheritance.none);
		return dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), Identity.class)
				.setParameter("organisationKey", organisation.getKey())
				.setParameter("role", role)
				.getResultList();
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
	
	public Set<Long> getMemberKeySet(OrganisationRef organisation, OrganisationRoles... roles) {
		List<Long> memberKeys = getMemberKeys(organisation, roles);
		return new HashSet<>(memberKeys);
	}
	
	public List<Long> getMemberKeys(OrganisationRef organisation, OrganisationRoles... roles) {
		StringBuilder sb = new StringBuilder(256);
		sb.append("select distinct membership.identity.key from organisation org")
		  .append(" inner join org.group baseGroup")
		  .append(" inner join baseGroup.members membership")
		  .append(" where org.key=:organisationKey");
		boolean withRoles = roles != null && roles.length > 0 && roles[0] != null;
		if(withRoles) {
			sb.append(" and membership.role in (:roles)");
		}
		
		TypedQuery<Long> query = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), Long.class)
				.setParameter("organisationKey", organisation.getKey());
		if(withRoles) {
			query.setParameter("roles", OrganisationRoles.toList(roles));
		}
			
		return query.getResultList();
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
	
	/**
	 * The method search identities, which are not deleted,
	 * which a not part of an organization.
	 * 
	 * @return A list of identities
	 */
	public List<Identity> getIdentitiesWithoutOrganisations() {
		StringBuilder sb = new StringBuilder(256);
		sb.append("select ident from ").append(IdentityImpl.class.getCanonicalName()).append(" as ident")
		  .append(" where ident.status<199 and not exists (select membership.key from bgroupmember as membership")
		  .append("  inner join organisation as org on (org.group.key=membership.group.key)")
		  .append("  where membership.identity.key=ident.key")
		  .append(" )");
		return dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), Identity.class)
				.getResultList();
	}
	
	public List<Organisation> getOrganisations(IdentityRef identity, List<String> roleList, boolean withInheritence) {
		QueryBuilder sb = new QueryBuilder(256);
		sb.append("select org from organisation org")
		  .append(" inner join fetch org.group baseGroup")
		  .append(" inner join baseGroup.members membership")
		  .append(" left join fetch org.type orgType")
		  .append(" left join fetch org.parent parentOrg")
		  .append(" where membership.identity.key=:identityKey and membership.role in (:roles)")
		  .append(" and org.status ").in(OrganisationStatus.notDelete());
		if(!withInheritence) {
			sb.append(" and membership.inheritanceModeString ").in(GroupMembershipInheritance.root, GroupMembershipInheritance.none);
		}
		List<Organisation> organisations = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), Organisation.class)
				.setParameter("identityKey", identity.getKey())
				.setParameter("roles", roleList)
				.getResultList();
		// because of the CLOB on Oracle, we make the "distinct" in Java
		Set<Organisation> deduplicatedOrganisations = new HashSet<>(organisations);
		return new ArrayList<>(deduplicatedOrganisations);
	}
	
	public Map<Long,List<String>> getUsersOrganisationsName(List<IdentityRef> identities) {
		QueryBuilder sb = new QueryBuilder(256);
		sb.append("select membership.identity.key, org.displayName from organisation org")
		  .append(" inner join org.group baseGroup")
		  .append(" inner join baseGroup.members membership")
		  .append(" where membership.identity.key in (:identitiesKeys)")
		  .append(" and membership.role").in(OrganisationRoles.user);
		
		int count = 0;
		int batch = 5000;
		List<Long> identitiesKeys = identities.stream()
				.map(IdentityRef::getKey)
				.collect(Collectors.toList());
		Map<Long,List<String>> organisationsMap = new HashMap<>();
		TypedQuery<Object[]> organisationsQuery = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), Object[].class);
		do {
			int toIndex = Math.min(count + batch, identitiesKeys.size());
			List<Long> toLoad = identitiesKeys.subList(count, toIndex);
			List<Object[]> organisations = organisationsQuery
					.setParameter("identitiesKeys", toLoad)
					.getResultList();
			for(Object[] rawObject:organisations) {
				Long identityKey = (Long)rawObject[0];
				String name = (String)rawObject[1];
				List<String> names = organisationsMap
						.computeIfAbsent(identityKey, k -> new ArrayList<>(2));
				if(!names.contains(name)) {
					names.add(name);
				}
			}
			count += batch;
		} while(count < identitiesKeys.size());

		return organisationsMap;
	}
	
	public List<Organisation> getOrganisations(Collection<OrganisationRef> rootOrganisations) {
		if(rootOrganisations == null || rootOrganisations.isEmpty()) return new ArrayList<>();
		
		StringBuilder sb = new StringBuilder(128);
		sb.append("select org from organisation org")
		  .append(" where org.key in (:organisationKeys)");

		List<Long> organisationKeys = rootOrganisations.stream()
				.map(OrganisationRef::getKey).collect(Collectors.toList());
		return dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), Organisation.class)
				.setParameter("organisationKeys", organisationKeys)
				.getResultList();
	}
	
	public List<Organisation> getChildren(OrganisationRef organisation, OrganisationStatus[] status) {
		QueryBuilder sb = new QueryBuilder(256);
		sb.append("select org from organisation org")
		  .append(" inner join fetch org.group baseGroup")
		  .append(" left join fetch org.type orgType")
		  .append(" left join fetch org.parent parentOrg")
		  .append(" where parentOrg.key=:organisationKey and org.status ").in(status);
		return dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), Organisation.class)
				.setParameter("organisationKey", organisation.getKey())
				.getResultList();
	}
	
	public List<Organisation> getDescendants(Organisation organisation) {
		QueryBuilder sb = new QueryBuilder(256);
		sb.append("select org from organisation org")
		  .append(" inner join fetch org.group baseGroup")
		  .append(" left join fetch org.type orgType")
		  .append(" left join fetch org.parent parentOrg")
		  .append(" where org.materializedPathKeys like :materializedPathKeys and not(org.key=:organisationKey)")
		  .append(" and org.status ").in(OrganisationStatus.notDelete());
		return dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), Organisation.class)
				.setParameter("materializedPathKeys", organisation.getMaterializedPathKeys() + "%")
				.setParameter("organisationKey", organisation.getKey())
				.getResultList();
	}
	
	public OrganisationNode getDescendantTree(Organisation rootOrganisation) {
		OrganisationNode rootNode = new OrganisationNode(rootOrganisation);

		List<Organisation> descendants = getDescendants(rootOrganisation);
		Map<Long,OrganisationNode> keyToOrganisations = new HashMap<>();
		for(Organisation descendant:descendants) {
			keyToOrganisations.put(descendant.getKey(), new OrganisationNode(descendant));
		}

		for(Organisation descendant:descendants) {
			Long key = descendant.getKey();
			if(key.equals(rootOrganisation.getKey())) {
				continue;
			}
			
			OrganisationNode node = keyToOrganisations.get(key);
			Organisation parentOrganisation = descendant.getParent();
			Long parentKey = parentOrganisation.getKey();
			if(parentKey.equals(rootOrganisation.getKey())) {
				//this is a root, or the user has not access to parent
				rootNode.addChildrenNode(node);
			} else {
				OrganisationNode parentNode = keyToOrganisations.get(parentKey);
				parentNode.addChildrenNode(node);
			}
		}

		return rootNode;
	}
	
	public List<Organisation> getParentLine(Organisation organisation) {
		StringBuilder sb = new StringBuilder(256);
		sb.append("select org from organisation as org")
		  .append(" inner join org.group as baseGroup")
		  .append(" left join fetch org.parent as parent")
		  .append(" left join fetch org.type as type")
		  .append(" where locate(org.materializedPathKeys,:materializedPath) = 1");
		  
		List<Organisation> levels = dbInstance.getCurrentEntityManager()
			.createQuery(sb.toString(), Organisation.class)
			.setParameter("materializedPath", organisation.getMaterializedPathKeys() + "%")
			.getResultList();
		Collections.sort(levels, new PathMaterializedPathLengthComparator());
		return levels;
	}
	
	public List<OrganisationRef> getParentLineRefs(List<Organisation> organisations) {
		if(organisations.isEmpty()) return new ArrayList<>();
		
		StringBuilder sb = new StringBuilder(256);
		sb.append("select distinct new org.olat.basesecurity.model.OrganisationRefImpl(org.key) from organisation as org")
		  .append(" where ");
		for (int i = 0; i < organisations.size(); i++) {
			if (i > 0) {
				sb.append(" or ");
			}
			sb.append("locate(org.materializedPathKeys, :orgPath").append(i).append(") = 1");
		}
		  
		TypedQuery<OrganisationRef> query = dbInstance.getCurrentEntityManager()
			.createQuery(sb.toString(), OrganisationRef.class);
		for (int i = 0; i < organisations.size(); i++) {
			query.setParameter( "orgPath" + i, organisations.get(i).getMaterializedPathKeys());
		}
		return query.getResultList();
	}
	
	public boolean hasAnyRole(IdentityRef identity, String excludeRole) {
		StringBuilder sb = new StringBuilder(256);
		sb.append("select membership.key from organisation org")
		  .append(" inner join org.group baseGroup")
		  .append(" inner join baseGroup.members membership")
		  .append(" where membership.identity.key=:identityKey");
		
		if(StringHelper.containsNonWhitespace(excludeRole)) {
			sb.append(" and not(membership.role=:excludeRole)");
		}
		TypedQuery<Long> query = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), Long.class)
				.setParameter("identityKey", identity.getKey());
		if(StringHelper.containsNonWhitespace(excludeRole)) {
			query.setParameter("excludeRole", excludeRole);
		}

		List<Long> memberships = query.setFirstResult(0)
			.setMaxResults(1)
			.getResultList();
		return memberships != null && !memberships.isEmpty() && memberships.get(0) != null && memberships.get(0).longValue() > 0;	
	}
	
	public boolean hasRole(IdentityRef identity, String organisationIdentifier, OrganisationRef organisation, String... role) {
		StringBuilder sb = new StringBuilder(256);
		sb.append("select membership.key from organisation org")
		  .append(" inner join org.group baseGroup")
		  .append(" inner join baseGroup.members membership")
		  .append(" where membership.identity.key=:identityKey ");
		
		boolean hasRole = role != null && role.length > 0 && role[0] != null;
		if(hasRole) {
			sb.append(" and membership.role in (:roles)");
		}
		if(StringHelper.containsNonWhitespace(organisationIdentifier)) {
			sb.append(" and org.identifier=:identifier");
		}
		if(organisation != null) {
			sb.append(" and org.key=:organisationKey");
		}
		TypedQuery<Long> query = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), Long.class)
				.setParameter("identityKey", identity.getKey());
		if(hasRole) {
			List<String> roleList = PersistenceHelper.toList(role);
			query.setParameter("roles", roleList);
		}
		if(StringHelper.containsNonWhitespace(organisationIdentifier)) {
			query.setParameter("identifier", organisationIdentifier);
		}	
		if(organisation != null) {
			query.setParameter("organisationKey", organisation.getKey());
		}	
	
		List<Long> memberships = query.setFirstResult(0)
			.setMaxResults(1)
			.getResultList();
		return memberships != null && !memberships.isEmpty() && memberships.get(0) != null && memberships.get(0).longValue() > 0;
	}
	
	private static class PathMaterializedPathLengthComparator implements Comparator<Organisation> {
		@Override
		public int compare(Organisation l1, Organisation l2) {
			String s1 = l1.getMaterializedPathKeys();
			String s2 = l2.getMaterializedPathKeys();
			
			int len1 = s1 == null ? 0 : s1.length();
			int len2 = s2 == null ? 0 : s2.length();
			return len1 - len2;
		}
	}
	
	public List<OrganisationMembershipStats> getStatistics(OrganisationRef organisation, List<IdentityRef> identities) {
		StringBuilder sb = new StringBuilder(256);
		sb.append("select membership.role, count(distinct membership.key) from organisation org")
		  .append(" inner join org.group baseGroup")
		  .append(" inner join baseGroup.members membership")
		  .append(" where org.key=:organisationKey and membership.identity.key in (:identityKeys) and membership.role is not null")
		  .append(" group by membership.role");
		
		List<Long> identityKeys = identities.stream()
				.map(IdentityRef::getKey).collect(Collectors.toList());
		List<Object[]> rawObjects = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), Object[].class)
				.setParameter("organisationKey", organisation.getKey())
				.setParameter("identityKeys", identityKeys)
				.getResultList();
		return rawObjects.stream().map(rawObject -> {
			String roleStr = (String)rawObject[0];
			Long numOfMembers = PersistenceHelper.extractLong(rawObject, 1);
			OrganisationRoles role = (OrganisationRoles.isValue(roleStr) ? OrganisationRoles.valueOf(roleStr) : null);
			return new OrganisationMembershipStats(role, numOfMembers);
		}).collect(Collectors.toList());
	}


}
