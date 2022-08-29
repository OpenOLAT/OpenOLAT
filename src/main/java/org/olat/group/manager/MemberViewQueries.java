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
package org.olat.group.manager;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import javax.persistence.TypedQuery;

import org.olat.basesecurity.Group;
import org.olat.basesecurity.GroupMembership;
import org.olat.basesecurity.GroupRoles;
import org.olat.basesecurity.OrganisationRoles;
import org.olat.core.commons.persistence.DB;
import org.olat.core.commons.persistence.PersistenceHelper;
import org.olat.core.commons.persistence.QueryBuilder;
import org.olat.core.id.Identity;
import org.olat.core.util.StringHelper;
import org.olat.group.BusinessGroup;
import org.olat.group.model.MemberView;
import org.olat.group.model.SearchBusinessGroupParams;
import org.olat.group.ui.main.SearchMembersParams;
import org.olat.group.ui.main.SearchMembersParams.Origin;
import org.olat.group.ui.main.SearchMembersParams.UserType;
import org.olat.modules.curriculum.CurriculumElement;
import org.olat.modules.curriculum.manager.CurriculumElementDAO;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryEntryStatusEnum;
import org.olat.resource.OLATResource;
import org.olat.user.propertyhandlers.UserPropertyHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 
 * Initial date: 6 juin 2018<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
@Service
public class MemberViewQueries {
	
	@Autowired
	private DB dbInstance;
	@Autowired
	private BusinessGroupDAO businessGroupDao;
	@Autowired
	private CurriculumElementDAO curriculumElementDao;
	
	public List<MemberView> getBusinessGroupMembers(BusinessGroup businessGroup, SearchMembersParams params,
			List<UserPropertyHandler> userPropertyHandlers, Locale locale) {
		if(businessGroup == null) return Collections.emptyList();
		
		StringBuilder sb = new StringBuilder();
		sb.append("select membership.key, membership.role, membership.creationDate, membership.lastModified, ident")
		  .append(" from businessgroup as grp")
		  .append(" inner join grp.baseGroup as baseGroup")
		  .append(" inner join baseGroup.members as membership")
		  .append(" inner join membership.identity as ident")
		  .append(" inner join fetch ident.user as identUser")
		  .append(" where grp.key=:businessGroupKey");
		searchByIdentity(sb, params);
		
		TypedQuery<Object[]> query = dbInstance.getCurrentEntityManager()
			.createQuery(sb.toString(), Object[].class)
			.setParameter("businessGroupKey", businessGroup.getKey());
		searchByIdentity(query, params);
		
		List<Object[]> rawObjects = query.getResultList();
		Map<Identity,MemberView> views = new HashMap<>();
		for(Object[] objects:rawObjects) {
			int pos = 1;// 0 is the membershipKey
			String role = (String)objects[pos++];
			Date creationDate = (Date)objects[pos++];
			Date lastModified = (Date)objects[pos++];
			
			Identity identity = (Identity)objects[pos++];
			MemberView view = views.computeIfAbsent(identity, id -> new MemberView(id, userPropertyHandlers, locale, creationDate, lastModified));
			view.addGroup(businessGroup);
			view.getMemberShip().setBusinessGroupRole(role);
		}

		getPending(views, businessGroup, params, userPropertyHandlers, locale);
		// Pending create the membership object
		getExternalUsers(views, businessGroup);
		
		List<MemberView> members = new ArrayList<>(views.values());
		filterByRoles(members, params);
		filterByOrigin(members, params);
		filterByUserTypes(members, params);
		return members;
	}
	
	public List<MemberView> getIdentityMemberships(Identity identity) {
		QueryBuilder sb = new QueryBuilder(1024);
		sb.append("select membership, v, relGroup.defaultGroup, curEl, bgp")
		  .append(" from repositoryentry as v ")
		  .append(" inner join fetch v.olatResource as res")
		  .append(" inner join v.groups as relGroup")
		  .append(" inner join relGroup.group as baseGroup")
		  .append(" inner join baseGroup.members as membership")
		  .append(" inner join fetch membership.group as mGroup")
		  .append(" left join fetch curriculumelement as curEl on (relGroup.defaultGroup=false and curEl.group.key=mGroup.key)")
		  .append(" left join fetch businessgroup as bgp on (relGroup.defaultGroup=false and bgp.baseGroup.key=mGroup.key)")
		  .append(" where membership.identity.key=:identityKey and membership.role ").in(GroupRoles.participant, GroupRoles.coach,GroupRoles.owner, GroupRoles.waiting)
		  .append(" and v.status ").in(RepositoryEntryStatusEnum.preparationToClosed());
		
		List<Object[]> rawObjects = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), Object[].class)
				.setParameter("identityKey", identity.getKey())
				.getResultList();
		
		Map<RepositoryEntry,MemberView> views = new HashMap<>();
		for(Object[] rawObject:rawObjects) {
			GroupMembership membership = (GroupMembership)rawObject[0];
			RepositoryEntry entry = (RepositoryEntry)rawObject[1];
			Boolean defaultGroup = (Boolean)rawObject[2];
			CurriculumElement curriculumElement = (CurriculumElement)rawObject[3];
			BusinessGroup businessGroup = (BusinessGroup)rawObject[4];
			
			MemberView view = views.computeIfAbsent(entry, re -> {
				MemberView v = new MemberView(identity, Collections.emptyList(), null);
				v.setRepositoryEntry(entry);
				return v;
			});

			if(defaultGroup != null && defaultGroup.booleanValue()) {
				view.getMemberShip().setRepositoryEntryRole(membership.getRole());
			} else if(curriculumElement != null) {
				view.getMemberShip().setCurriculumElementRole(membership.getRole());
			} else if(businessGroup != null) {
				view.getMemberShip().setBusinessGroupRole(membership.getRole());
			}
		}
		
		return new ArrayList<>(views.values());
	}
	
	public List<MemberView> getRepositoryEntryMembers(RepositoryEntry entry, SearchMembersParams params,
			List<UserPropertyHandler> userPropertyHandlers, Locale locale) {
		if(entry == null) return Collections.emptyList();

		Map<Identity,MemberView> views = getMembersView(entry, params, userPropertyHandlers, locale);
		getPending(views, entry, params, userPropertyHandlers, locale);
		getExternalUsers(views, entry);
		
		List<MemberView> members = new ArrayList<>(views.values());
		filterByRoles(members, params);
		filterByOrigin(members, params);
		filterByUserTypes(members, params);
		return members;
	}
	
	private Map<Identity,MemberView> getMembersView(RepositoryEntry entry, SearchMembersParams params,
			List<UserPropertyHandler> userPropertyHandlers, Locale locale) {
		StringBuilder sb = new StringBuilder();
		sb.append("select membership, relGroup.defaultGroup")
		  .append(" from repositoryentry as v ")
		  .append(" inner join v.groups as relGroup")
		  .append(" inner join relGroup.group as baseGroup")
		  .append(" inner join baseGroup.members as membership")
		  .append(" inner join fetch membership.identity as ident")
		  .append(" inner join fetch ident.user as identUser")
		  .append(" inner join fetch membership.group as mGroup")
		  .append(" where v.key=:repoEntryKey and membership.role in ('")
		  	.append(GroupRoles.participant).append("','").append(GroupRoles.coach).append("','")
		  	.append(GroupRoles.owner).append("','").append(GroupRoles.waiting).append("')");
		searchByIdentity(sb, params);
		
		TypedQuery<Object[]> query = dbInstance.getCurrentEntityManager()
			.createQuery(sb.toString(), Object[].class)
			.setParameter("repoEntryKey", entry.getKey());
		searchByIdentity(query, params);
		
		// lazy load them
		Map<Group,CurriculumElement> groupToCurriculumElement = null;
		Map<Group,BusinessGroup> groupToBusinessGroup = null;
		
		List<Object[]> rawObjects = query.getResultList();
		Map<Identity,MemberView> views = new HashMap<>();
		for(Object[] rawObject:rawObjects) {
			GroupMembership membership = (GroupMembership)rawObject[0];
			Boolean defaultGroup = (Boolean)rawObject[1];
			Identity member = membership.getIdentity();
			
			MemberView view = views.computeIfAbsent(member, id
					-> new MemberView(id, userPropertyHandlers, locale, membership.getCreationDate(), membership.getLastModified()));
			
			if(defaultGroup != null && defaultGroup.booleanValue()) {
				view.setRepositoryEntry(entry);
				view.getMemberShip().setRepositoryEntryRole(membership.getRole());
			} else {
				Group group = membership.getGroup();
				if(groupToBusinessGroup == null) {
					groupToBusinessGroup = getBusinessGroups(entry);
				}

				BusinessGroup businessGroup = groupToBusinessGroup.get(group);
				if(businessGroup != null) {
					view.addGroup(new MemberView
							.BusinessGroupShortImpl(businessGroup.getKey(), businessGroup.getName(), businessGroup.getManagedFlagsString()));
					view.getMemberShip().setBusinessGroupRole(membership.getRole());
				} else {
					if(groupToCurriculumElement == null) {
						groupToCurriculumElement = getCurriculumElements(entry);
					}
					CurriculumElement element = groupToCurriculumElement.get(group);
					if(element != null) {
						view.addCurriculumElement(new MemberView
								.CurriculumElementShortImpl(element.getKey(), element.getDisplayName(), element.getManagedFlags()));
						view.getMemberShip().setCurriculumElementRole(membership.getRole());
					}
				}
			}
		}
		return views;
	}
	
	private Map<Group,BusinessGroup> getBusinessGroups(RepositoryEntry entry) {
		SearchBusinessGroupParams params = new SearchBusinessGroupParams();
		List<BusinessGroup> groups = businessGroupDao.findBusinessGroups(params, entry, 0, -1);
		return groups.stream().collect(Collectors.toMap(BusinessGroup::getBaseGroup, grp -> grp, (grp1, grp2) -> grp1));
	}
	
	private Map<Group,CurriculumElement> getCurriculumElements(RepositoryEntry entry) {
		List<CurriculumElement> elements = curriculumElementDao.loadElements(entry);
		return elements.stream().collect(Collectors.toMap(CurriculumElement::getGroup, el -> el, (el1, el2) -> el1));
	}
	
	private void getExternalUsers(Map<Identity,MemberView> views, RepositoryEntry repositoryEntry) {
		QueryBuilder sb = new QueryBuilder();
		sb.append("select inviteeMembership.identity.key")
		  .append(" from repoentrytogroup as relGroup")
		  .append(" inner join relGroup.group as baseGroup")
		  .append(" inner join baseGroup.members as membership")
		  .append(" inner join bgroupmember as inviteeMembership on (membership.identity.key=inviteeMembership.identity.key)")
		  .append(" where relGroup.entry.key=:repositoryEntryKey and inviteeMembership.role ").in(OrganisationRoles.invitee);
		
		List<Long> externalIdentityKeys = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), Long.class)
				.setParameter("repositoryEntryKey", repositoryEntry.getKey())
				.getResultList();
		Set<Long> externalIdentityKeySet = new HashSet<>(externalIdentityKeys);
		for(Map.Entry<Identity,MemberView> view:views.entrySet()) {
			if(externalIdentityKeySet.contains(view.getKey().getKey())) {
				view.getValue().getMemberShip().setExternalUser(true);
			}
		}
	}
	
	private void getPending(Map<Identity,MemberView> views, RepositoryEntry entry, SearchMembersParams params,
			List<UserPropertyHandler> userPropertyHandlers, Locale locale) {
		StringBuilder sb = new StringBuilder();
		sb.append("select ident, reservation.resource from resourcereservation as reservation")
		  .append(" inner join reservation.identity as ident")
		  .append(" inner join fetch ident.user as identUser")
		  .append(" where (reservation.resource.key in (select v.olatResource.key from repositoryentry as v where v.key=:repoEntryKey)")
		  .append(" or reservation.resource.key in (select grp.resource.key from businessgroup as grp")
		  .append("   inner join repoentrytogroup as rel on (grp.baseGroup.key=rel.group.key)")
		  .append("   where rel.entry.key=:repoEntryKey")
		  .append(" ))");
		searchByIdentity(sb, params);

		TypedQuery<Object[]> query = dbInstance.getCurrentEntityManager()
			.createQuery(sb.toString(), Object[].class)
			.setParameter("repoEntryKey", entry.getKey());
		searchByIdentity(query, params);
		
		Map<OLATResource,BusinessGroup> groupToBusinessGroup = null;
		List<Object[]> rawObjects = query.getResultList();
		for(Object[] rawObject:rawObjects) {
			Identity identity = (Identity)rawObject[0];
			OLATResource resource = (OLATResource)rawObject[1];
			
			MemberView m = views.computeIfAbsent(identity, id -> new MemberView(id, userPropertyHandlers, locale));
			m.getMemberShip().setPending(true);
			if(resource != null ) {
				if(resource.equals(entry.getOlatResource())) {
					m.setRepositoryEntry(entry);
				} else {
					if(groupToBusinessGroup == null) {
						groupToBusinessGroup = mapToResource(getBusinessGroups(entry));
					}
					m.addGroup(groupToBusinessGroup.get(resource));
				}
			}
		}
	}
	
	private Map<OLATResource,BusinessGroup> mapToResource(Map<Group,BusinessGroup> businessGroups) {
		return businessGroups.values().stream()
				.collect(Collectors.toMap(BusinessGroup::getResource, b -> b, (u, v) -> u));
		
	}
	
	private void getExternalUsers(Map<Identity,MemberView> views, BusinessGroup businessGroup) {
		QueryBuilder sb = new QueryBuilder();
		sb.append("select inviteeMembership.identity.key")
		  .append(" from businessgroup as grp")
		  .append(" inner join grp.baseGroup as baseGroup")
		  .append(" inner join baseGroup.members as membership")
		  .append(" inner join bgroupmember as inviteeMembership on (membership.identity.key=inviteeMembership.identity.key)")
		  .append(" where grp.key=:groupKey and inviteeMembership.role ").in(OrganisationRoles.invitee);
		
		List<Long> externalIdentityKeys = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), Long.class)
				.setParameter("groupKey", businessGroup.getKey())
				.getResultList();
		Set<Long> externalIdentityKeySet = new HashSet<>(externalIdentityKeys);
		for(Map.Entry<Identity,MemberView> view:views.entrySet()) {
			if(externalIdentityKeySet.contains(view.getKey().getKey())) {
				view.getValue().getMemberShip().setExternalUser(true);
			}
		}
	}
	
	private void getPending(Map<Identity,MemberView> views, BusinessGroup entry, SearchMembersParams params,
			List<UserPropertyHandler> userPropertyHandlers, Locale locale) {
		StringBuilder sb = new StringBuilder();
		sb.append("select ident from resourcereservation as reservation")
		  .append(" inner join reservation.identity as ident")
		  .append(" inner join fetch ident.user as identUser")
		  .append(" inner join businessgroup as grp on (reservation.resource.key = grp.resource.key)")
		  .append(" where grp.key=:groupKey");
		searchByIdentity(sb, params);
		
		TypedQuery<Identity> query = dbInstance.getCurrentEntityManager()
			.createQuery(sb.toString(), Identity.class)
			.setParameter("groupKey", entry.getKey());
		searchByIdentity(query, params);
		
		List<Identity> identities = query.getResultList();
		for(Identity identity:identities) {
			MemberView m = views.computeIfAbsent(identity, id -> new MemberView(id, userPropertyHandlers, locale));
			m.addGroup(entry);
			m.getMemberShip().setPending(true);
		}
	}
	
	private void searchByIdentity(StringBuilder sb, SearchMembersParams params) {
		if (params.getLogin() == null && (params.getUserPropertiesSearch() == null || params.getUserPropertiesSearch().isEmpty())) return;

		sb.append(" and (");			

		// append query for login
		boolean appendOr = false;
		if (params.getLogin() != null) {// backwards compatibility
			appendOr = true;
			PersistenceHelper.appendFuzzyLike(sb, "ident.name", "login", dbInstance.getDbVendor());
			sb.append(" or ");
			PersistenceHelper.appendFuzzyLike(sb, "identUser.nickName", "login", dbInstance.getDbVendor());
		}

		// append queries for user fields
		if (params.getUserPropertiesSearch() != null && !params.getUserPropertiesSearch().isEmpty()) {
			// add other fields
			for (String key : params.getUserPropertiesSearch().keySet()) {
				if(appendOr) {
					sb.append(" or ");
				} else {
					appendOr = true;
				}
				PersistenceHelper.appendFuzzyLike(sb, "identUser.".concat(key), key.concat("_value"), dbInstance.getDbVendor());
			}
		}

		sb.append(" )");
	}
	
	private void searchByIdentity(TypedQuery<?> query, SearchMembersParams params) {
		if (params.getLogin() != null) {
			String login = PersistenceHelper.makeFuzzyQueryString(params.getLogin());
			query.setParameter("login", login.toLowerCase());
		}

		//	 add user properties attributes
		if (params.getUserPropertiesSearch() != null && !params.getUserPropertiesSearch().isEmpty()) {
			for (Map.Entry<String, String> entry : params.getUserPropertiesSearch().entrySet()) {
				String value = entry.getValue();
				value = PersistenceHelper.makeFuzzyQueryString(value);
				query.setParameter(entry.getKey() + "_value", value.toLowerCase());
			}
		}
	}
	
	private void filterByUserTypes(List<MemberView> memberList, SearchMembersParams params) {
		for(Iterator<MemberView> it=memberList.iterator(); it.hasNext(); ) {
			MemberView m = it.next();
			if((m.getMemberShip().isExternalUser() && params.hasUserType(UserType.invitee))
					|| (!m.getMemberShip().isExternalUser() && params.hasUserType(UserType.user))) {
				continue;
			}

			it.remove();
		}
	}
	
	private void filterByOrigin(List<MemberView> memberList, SearchMembersParams params) {
		for(Iterator<MemberView> it=memberList.iterator(); it.hasNext(); ) {
			MemberView m = it.next();
			if((m.getGroups() != null && !m.getGroups().isEmpty() && params.hasOrigin(Origin.businessGroup))
					|| (m.getCurriculumElements() != null && !m.getCurriculumElements().isEmpty() && params.hasOrigin(Origin.curriculum))
					|| (StringHelper.containsNonWhitespace(m.getRepositoryEntryDisplayName()) && params.hasOrigin(Origin.repositoryEntry))) {
				continue;
			}

			it.remove();
		}
	}
	
	/**
	 * This filter method preserve the multiple roles of a member. If we want only the waiting list but
	 * a member is in the waiting list and owner of the course, we want it to know.
	 * @param memberList
	 * @param params
	 * @return
	 */
	private void filterByRoles(Collection<MemberView> memberList, SearchMembersParams params) {
		List<MemberView> members = new ArrayList<>(memberList);

		if(params.isRole(GroupRoles.owner)) {
			for(Iterator<MemberView> it=members.iterator(); it.hasNext(); ) {
				if(it.next().getMemberShip().isOwner()) {
					it.remove();
				}
			}
		}
	
		if(params.isRole(GroupRoles.participant)) {
			for(Iterator<MemberView> it=members.iterator(); it.hasNext(); ) {
				if(it.next().getMemberShip().isParticipant()) {
					it.remove();
				}
			}
		}
		
		if(params.isRole(GroupRoles.coach)) {
			for(Iterator<MemberView> it=members.iterator(); it.hasNext(); ) {
				if(it.next().getMemberShip().isCoach()) {
					it.remove();
				}
			}
		}
		
		if(params.isRole(GroupRoles.waiting)) {
			for(Iterator<MemberView> it=members.iterator(); it.hasNext(); ) {
				if(it.next().getMemberShip().isBusinessGroupWaiting()) {
					it.remove();
				}
			}
		}

		if(params.isPending()) {
			for(Iterator<MemberView> it=members.iterator(); it.hasNext(); ) {
				if(it.next().getMemberShip().isPending()) {
					it.remove();
				}
			}
		}
		
		memberList.removeAll(members);
	}
}
