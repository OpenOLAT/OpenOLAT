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
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.LockModeType;
import javax.persistence.TemporalType;
import javax.persistence.TypedQuery;

import org.olat.basesecurity.BaseSecurity;
import org.olat.basesecurity.Group;
import org.olat.basesecurity.GroupRoles;
import org.olat.basesecurity.manager.GroupDAO;
import org.olat.collaboration.CollaborationTools;
import org.olat.core.commons.persistence.DB;
import org.olat.core.commons.persistence.PersistenceHelper;
import org.olat.core.commons.services.mark.impl.MarkImpl;
import org.olat.core.id.Identity;
import org.olat.core.id.Roles;
import org.olat.core.util.StringHelper;
import org.olat.group.BusinessGroup;
import org.olat.group.BusinessGroupImpl;
import org.olat.group.BusinessGroupMembership;
import org.olat.group.BusinessGroupModule;
import org.olat.group.BusinessGroupOrder;
import org.olat.group.BusinessGroupShort;
import org.olat.group.BusinessGroupView;
import org.olat.group.model.BGRepositoryEntryRelation;
import org.olat.group.model.BusinessGroupMembershipImpl;
import org.olat.group.model.BusinessGroupMembershipViewImpl;
import org.olat.group.model.BusinessGroupShortImpl;
import org.olat.group.model.BusinessGroupViewImpl;
import org.olat.group.model.IdentityGroupKey;
import org.olat.group.model.SearchBusinessGroupParams;
import org.olat.properties.Property;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryEntryRef;
import org.olat.resource.OLATResource;
import org.olat.resource.OLATResourceManager;
import org.olat.resource.accesscontrol.model.OfferImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
@Service("businessGroupDao")
public class BusinessGroupDAO {

	@Autowired
	private DB dbInstance;
	@Autowired
	private GroupDAO groupDao;
	@Autowired
	private BaseSecurity securityManager;
	@Autowired
	private OLATResourceManager olatResourceManager;
	@Autowired
	private BusinessGroupModule businessGroupModule;
	
	public BusinessGroup createAndPersist(Identity creator, String name, String description,
			Integer minParticipants, Integer maxParticipants, boolean waitingListEnabled, boolean autoCloseRanksEnabled,
			boolean showOwners, boolean showParticipants, boolean showWaitingList) {
		return createAndPersist(creator, name, description, null, null,
				minParticipants, maxParticipants, waitingListEnabled, autoCloseRanksEnabled,
				showOwners, showParticipants, showWaitingList);
	}
		
	public BusinessGroup createAndPersist(Identity creator, String name, String description,
				String externalId, String managedFlags,
				Integer minParticipants, Integer maxParticipants, boolean waitingListEnabled, boolean autoCloseRanksEnabled,
				boolean showOwners, boolean showParticipants, boolean showWaitingList) {

		BusinessGroupImpl businessgroup = new BusinessGroupImpl(name, description);
		if(minParticipants != null && minParticipants.intValue() >= 0) {
			businessgroup.setMinParticipants(minParticipants);
		}
		if(maxParticipants != null && maxParticipants.intValue() >= 0) {
			businessgroup.setMaxParticipants(maxParticipants);
		}
		
		if(StringHelper.containsNonWhitespace(externalId)) {
			businessgroup.setExternalId(externalId);
		}
		if(StringHelper.containsNonWhitespace(managedFlags)) {
			businessgroup.setManagedFlagsString(managedFlags);
		}
		businessgroup.setOwnersVisibleIntern(showOwners);
		businessgroup.setParticipantsVisibleIntern(showParticipants);
		businessgroup.setWaitingListVisibleIntern(showWaitingList);
		// group members visibility
		businessgroup.setOwnersVisiblePublic(false);
		businessgroup.setParticipantsVisiblePublic(false);
		businessgroup.setWaitingListVisiblePublic(false);
		businessgroup.setDownloadMembersLists(false);
		
		if(creator == null) {
			businessgroup.setAllowToLeave(businessGroupModule.isAllowLeavingGroupCreatedByAuthors());
		} else {
			Roles roles = securityManager.getRoles(creator);
			if(roles.isAuthor()) {
				businessgroup.setAllowToLeave(businessGroupModule.isAllowLeavingGroupCreatedByAuthors());
			} else {
				businessgroup.setAllowToLeave(businessGroupModule.isAllowLeavingGroupCreatedByLearners());
			}
		}
		
		businessgroup.setWaitingListEnabled(waitingListEnabled);
		businessgroup.setAutoCloseRanksEnabled(autoCloseRanksEnabled);

		Group group = groupDao.createGroup();
		businessgroup.setBaseGroup(group);
		if (creator != null) {
			groupDao.addMembership(group, creator, GroupRoles.coach.name());
		}

		EntityManager em = dbInstance.getCurrentEntityManager();
		em.persist(businessgroup);

		OLATResource businessgroupOlatResource =  olatResourceManager.createOLATResourceInstance(businessgroup);
		olatResourceManager.saveOLATResource(businessgroupOlatResource);
		businessgroup.setResource(businessgroupOlatResource);
		businessgroup = em.merge(businessgroup);

		// per default all collaboration-tools are disabled
		return businessgroup;
	}
	
	public BusinessGroup load(Long key) {
		StringBuilder sb = new StringBuilder();
		sb.append("select bgi from ").append(BusinessGroupImpl.class.getName()).append(" bgi ")
		  .append(" left join fetch bgi.baseGroup baseGroup")
		  .append(" left join fetch bgi.resource resource")
		  .append(" where bgi.key=:key");
		List<BusinessGroup> groups = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), BusinessGroup.class)
				.setParameter("key", key)
				.getResultList();
		return groups == null || groups.isEmpty() ? null : groups.get(0);
	}
	
	public List<BusinessGroupShort> loadShort(Collection<Long> ids) {
		if(ids == null || ids.isEmpty()) {
			return Collections.emptyList();
		}
		StringBuilder sb = new StringBuilder();
		sb.append("select bgi from ").append(BusinessGroupShortImpl.class.getName()).append(" bgi ")
		  .append(" where bgi.key in (:ids)");

		List<BusinessGroupShort> groups = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), BusinessGroupShort.class)
				.setParameter("ids", ids)
				.setHint("org.hibernate.cacheable", Boolean.TRUE)
				.getResultList();
		return groups;
	}
	
	public List<BusinessGroup> load(Collection<Long> ids) {
		if(ids == null || ids.isEmpty()) {
			return Collections.emptyList();
		}
		StringBuilder sb = new StringBuilder();
		sb.append("select bgi from ").append(BusinessGroupImpl.class.getName()).append(" bgi ")
		  .append(" inner join fetch bgi.resource resource")
		  .append(" where bgi.key in (:ids)");

		List<BusinessGroup> groups = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), BusinessGroup.class)
				.setParameter("ids", ids)
				.getResultList();
		return groups;
	}
	
	public List<BusinessGroup> loadAll() {
		StringBuilder sb = new StringBuilder();
		sb.append("select bgi from ").append(BusinessGroupImpl.class.getName()).append(" bgi ")
		  .append(" inner join fetch bgi.resource resource");

		List<BusinessGroup> groups = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), BusinessGroup.class)
				.getResultList();
		return groups;
	}
	
	public BusinessGroup loadForUpdate(Long id) {
		StringBuilder sb = new StringBuilder();
		sb.append("select bgi from ").append(BusinessGroupImpl.class.getName()).append(" bgi ")
			.append(" inner join fetch bgi.resource resource")
			.append(" where bgi.key=:key");
		
		List<BusinessGroup> groups = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), BusinessGroup.class)
				.setParameter("key", id)
				.setLockMode(LockModeType.PESSIMISTIC_WRITE)
				.getResultList();
		if(groups.isEmpty()) return null;
		return groups.get(0);
	}
	
	public BusinessGroup merge(BusinessGroup group) {
		EntityManager em = dbInstance.getCurrentEntityManager();
		BusinessGroup mergedGroup = em.merge(group);
		return mergedGroup;
	}
	
	/**
	 * The method don't reload/reattach the object, make sure that you have
	 * reloaded the business group before trying to delete it.
	 * 
	 * @param group
	 */
	public void delete(BusinessGroup group) {
		groupDao.removeMemberships(group.getBaseGroup());
		dbInstance.getCurrentEntityManager().remove(group);
		dbInstance.getCurrentEntityManager().remove(group.getBaseGroup());
	}
	
	/**
	 * Work with the hibernate session
	 * @param group
	 * @return
	 */
	public BusinessGroup update(BusinessGroup group) {
		return dbInstance.getCurrentEntityManager().merge(group);
	}
	
	public List<BusinessGroupMembership> getBusinessGroupsMembership(Collection<BusinessGroup> groups) {
		List<Long> groupKeys = new ArrayList<>();
		for(BusinessGroup group:groups) {
			groupKeys.add(group.getKey());
		}

		Map<IdentityGroupKey, BusinessGroupMembershipImpl> memberships = new HashMap<IdentityGroupKey, BusinessGroupMembershipImpl>();
		loadBusinessGroupsMembership(groupKeys, memberships);
		return new ArrayList<BusinessGroupMembership>(memberships.values());
	}
	
	private void loadBusinessGroupsMembership(Collection<Long> groupKeys,
			Map<IdentityGroupKey, BusinessGroupMembershipImpl> memberships) {
		
		if(groupKeys == null || groupKeys.isEmpty()) {
			return;
		}
		
		StringBuilder sb = new StringBuilder(); 
		sb.append("select membership.identity.key, membership.creationDate, membership.lastModified, membership.role, grp.key ")
		  .append(" from ").append(BusinessGroupImpl.class.getName()).append(" as grp ")
		  .append(" inner join grp.baseGroup as baseGroup ")
		  .append(" inner join baseGroup.members as membership ")
		  .append(" where grp.key in (:groupKeys)");
		
		List<Object[]> members = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), Object[].class)
				.setParameter("groupKeys", groupKeys)
				.getResultList();
		
		for(Object[] membership:members) {
			Long identityKey = (Long)membership[0];
			Date creationDate = (Date)membership[1];
			Date lastModified = (Date)membership[2];
			String role = (String)membership[3];
			Long groupKey = (Long)membership[4];

			IdentityGroupKey key = new IdentityGroupKey(identityKey, groupKey);
			if(!memberships.containsKey(key)) {
				memberships.put(key, new BusinessGroupMembershipImpl(identityKey, groupKey));
			}
			BusinessGroupMembershipImpl mb = memberships.get(key);
			mb.setCreationDate(creationDate);
			mb.setLastModified(lastModified);
			if(GroupRoles.coach.name().equals(role)) {
				mb.setOwner(true);
			} else if(GroupRoles.participant.name().equals(role)) {
				mb.setParticipant(true);
			} else if(GroupRoles.waiting.name().equals(role)) {
				mb.setWaiting(true);
			}
		}
	}
	
	public int countMembershipInfoInBusinessGroups(Identity identity, List<Long> groupKeys) {
		StringBuilder sb = new StringBuilder(); 
		sb.append("select count(membership) from bgmembershipview as membership ")
		  .append(" where membership.identityKey=:identId ");
		if(groupKeys != null && !groupKeys.isEmpty()) {
		  sb.append(" and membership.groupKey in (:groupKeys)");
		}
		
		TypedQuery<Number> query = dbInstance.getCurrentEntityManager().createQuery(sb.toString(), Number.class)
				.setParameter("identId", identity.getKey());
		if(groupKeys != null && !groupKeys.isEmpty()) {
			query.setParameter("groupKeys", groupKeys);
		}
		
		Number res = query.getSingleResult();
		return res.intValue();
	}

	public List<BusinessGroupMembershipViewImpl> getMembershipInfoInBusinessGroups(Collection<BusinessGroup> groups, List<Identity> identities) {
		List<Long> groupKeys = new ArrayList<Long>();
		for(BusinessGroup group:groups) {
			groupKeys.add(group.getKey());
		}
		Identity[] identityArr = identities.toArray(new Identity[identities.size()]);
		return getMembershipInfoInBusinessGroups(groupKeys, identityArr);
	}
	
	public List<BusinessGroupMembershipViewImpl> getMembershipInfoInBusinessGroups(Collection<Long> groupKeys, Identity... identity) {	
		StringBuilder sb = new StringBuilder(); 
		sb.append("select membership from bgmembershipview as membership ");
		boolean and = false;
		if(identity != null && identity.length > 0) {
			and = and(sb, and);
			sb.append("membership.identityKey in (:identIds) ");
		}
		if(groupKeys != null && !groupKeys.isEmpty()) {
			and = and(sb, and);
			sb.append("membership.groupKey in (:groupKeys)");
		}
		
		TypedQuery<BusinessGroupMembershipViewImpl> query = dbInstance.getCurrentEntityManager().createQuery(sb.toString(), BusinessGroupMembershipViewImpl.class);
		if(identity != null && identity.length > 0) {
			List<Long> ids = new ArrayList<Long>(identity.length);
			for(Identity id:identity) {
				ids.add(id.getKey());
			}
			query.setParameter("identIds", ids);
		}	
		if(groupKeys != null && !groupKeys.isEmpty()) {
			query.setParameter("groupKeys", groupKeys);
		}
		
		List<BusinessGroupMembershipViewImpl> res = query.getResultList();
		return res;
	}

	public List<Long> isIdentityInBusinessGroups(Identity identity, boolean owner, boolean attendee, boolean waiting, List<BusinessGroup> groups) {
		if(groups == null || groups.isEmpty() || (!owner && !attendee && !waiting)) {
			return Collections.emptyList();
		}
		
		StringBuilder sb = new StringBuilder(); 
		sb.append("select bgi.key from ").append(BusinessGroupImpl.class.getName()).append(" as bgi ")
		  .append(" inner join bgi.baseGroup as baseGroup")
		  .append(" inner join baseGroup.members as bmember")
		  .append(" where bgi.key in (:groupKeys) and bmember.identity.key=:identityKey and bmember.role in (:roles)");

		List<String> roles = new ArrayList<>(3);
		if(owner) {
			roles.add(GroupRoles.coach.name());
		}
		if(attendee) {
			roles.add(GroupRoles.participant.name());
		}
		if(waiting) {
			roles.add(GroupRoles.waiting.name());
		}

		List<Long> groupKeys = PersistenceHelper.toKeys(groups);
		List<Long> res = dbInstance.getCurrentEntityManager().createQuery(sb.toString(), Long.class)
				.setParameter("groupKeys", groupKeys)
				.setParameter("identityKey", identity.getKey())
				.setParameter("roles", roles)
				.setHint("org.hibernate.cacheable", Boolean.TRUE)
				.getResultList();
		return res;
	}
	
	public List<BusinessGroup> findBusinessGroup(Identity identity, int maxResults, BusinessGroupOrder... ordering) {
		StringBuilder sb = new StringBuilder();
		sb.append("select bgi from ").append(BusinessGroupImpl.class.getName()).append(" as bgi ")
		  .append(" inner join bgi.baseGroup as baseGroup")
		  .append(" where exists (select bmember from bgroupmember as bmember")
		  .append("   where bmember.identity.key=:identKey and bmember.group=baseGroup and bmember.role in ('").append(GroupRoles.coach.name()).append("','").append(GroupRoles.participant.name()).append("')")
		  .append(" )");
		
		if(ordering != null && ordering.length > 0 && ordering[0] != null) {
			sb.append(" order by ");
			for(BusinessGroupOrder o:ordering) {
				switch(o) {
					case nameAsc: sb.append("bgi.name");break;
					case nameDesc: sb.append("bgi.name desc");break;
					case creationDateAsc: sb.append("bgi.creationDate");break;
					case creationDateDesc: sb.append("bgi.creationDate desc");break;
				}
			}
			//sb.append(" gp.key ");
		}

		TypedQuery<BusinessGroup> query = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), BusinessGroup.class)
				.setParameter("identKey", identity.getKey());
		if(maxResults > 0) {
			query.setMaxResults(maxResults);
		}
		return query.getResultList();
	}
	
	public BusinessGroup findBusinessGroup(Group baseGroup) {
		StringBuilder sb = new StringBuilder();
		sb.append("select bgs from ").append(BusinessGroupImpl.class.getName()).append(" as bgs ")
		  .append(" inner join fetch bgs.resource as bgResource")
		  .append(" inner join fetch bgs.baseGroup as baseGroup")
		  .append(" where baseGroup=:group");

		List<BusinessGroup> groups = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), BusinessGroup.class)
				.setParameter("group", baseGroup)
				.getResultList();
		return groups.isEmpty() ? null : groups.get(0);
	}
	
	public List<BusinessGroup> findBusinessGroupsWithWaitingListAttendedBy(Identity identity, RepositoryEntryRef repoEntry) {
		StringBuilder sb = new StringBuilder();
		sb.append("select bgs from ").append(BusinessGroupImpl.class.getName()).append(" as bgs ")
		  .append(" inner join fetch bgs.resource as bgResource")
		  .append(" inner join fetch bgs.baseGroup as baseGroup")
		  .append(" inner join baseGroup.members as membership")
		  .append(" where ");
		if(repoEntry != null) {
			sb.append(" exists (select relation from repoentrytogroup as relation where relation.group=baseGroup and relation.entry.key=:repoEntryKey)")
			  .append(" and ");
		}
		sb.append(" membership.identity.key=:identityKey and membership.role='").append(GroupRoles.waiting.name()).append("'");
		
		TypedQuery<BusinessGroup> query = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), BusinessGroup.class)
				.setParameter("identityKey", identity.getKey());
		if(repoEntry != null) {
			query.setParameter("repoEntryKey", repoEntry.getKey());
		}
		return query.getResultList();
	}
	
	public int countBusinessGroups(SearchBusinessGroupParams params, RepositoryEntryRef resource) {
		TypedQuery<Number> query = createFindDBQuery(params, resource, Number.class)
				.setHint("org.hibernate.cacheable", Boolean.TRUE);

		Number count = query.getSingleResult();
		return count.intValue();
	}
	
	public List<BusinessGroup> findBusinessGroups(SearchBusinessGroupParams params, RepositoryEntryRef resource,
			int firstResult, int maxResults, BusinessGroupOrder... ordering) {
		TypedQuery<BusinessGroup> query = createFindDBQuery(params, resource, BusinessGroup.class, ordering);
		query.setFirstResult(firstResult);
		if(maxResults > 0) {
			query.setMaxResults(maxResults);
		}
		List<BusinessGroup> groups = query.getResultList();
		return groups;
	}
	
	private <T> TypedQuery<T> createFindDBQuery(SearchBusinessGroupParams params, RepositoryEntryRef resource, Class<T> resultClass, BusinessGroupOrder... ordering) {
		StringBuilder query = new StringBuilder();
		if(BusinessGroup.class.equals(resultClass)) {
			query.append("select distinct(bgi) from ");
		} else {
			query.append("select count(bgi.key) from ");
		}
		query.append(org.olat.group.BusinessGroupImpl.class.getName()).append(" as bgi ");

		//inner joins
		if(BusinessGroup.class.equals(resultClass)) {
			query.append("inner join fetch bgi.resource bgResource ");
		} else {
			query.append("inner join bgi.resource bgResource ");
		}

		if(StringHelper.containsNonWhitespace(params.getOwnerName()) || params.getResources() != null ||
				resource != null || params.isOwner() || params.isAttendee() || params.isWaiting()) {
			query.append(" inner join bgi.baseGroup as baseGroup");
		}

		boolean where = false;
		if(StringHelper.containsNonWhitespace(params.getOwnerName())) {
			where = true;
			query.append(" inner join baseGroup.members as ownerMember on ownerMember.role='coach'")
			     .append(" inner join ownerMember.identity as identity")
			     .append(" inner join identity.user as user")
			     .append(" where ");
			//query the name in login, firstName and lastName

			searchLikeUserProperty(query, "firstName", "owner");
			query.append(" or ");
			searchLikeUserProperty(query, "lastName", "owner");
			query.append(" or ");
			searchLikeAttribute(query, "identity", "name", "owner");
		}

		if(StringHelper.containsNonWhitespace(params.getExternalId())) {
			where = where(query, where);
			query.append("bgi.externalId=:externalId");
		}
		
		Long id = null;
		if(StringHelper.containsNonWhitespace(params.getIdRef())) {
			if(StringHelper.isLong(params.getIdRef())) {
				try {
					id = new Long(params.getIdRef());
				} catch (NumberFormatException e) {
					//not a real number, can be a very long numerical external id
				}
			}
			where = where(query, where);
			query.append("(bgi.externalId=:idRefString");
			if(id != null) {
				query.append(" or bgi.key=:idRefLong");
			}
			query.append(")");
		}
		
		if(params.getManaged() != null) {
			where = where(query, where);
			if(params.getManaged().booleanValue()) {
				query.append("bgi.managedFlagsString is not null");
			} else {
				query.append("bgi.managedFlagsString is null");
			}
		}
		
		if(params.getGroupKeys() != null && !params.getGroupKeys().isEmpty()) {
			where = where(query, where);
			query.append("bgi.key in (:groupKeys)");
		}
		
		if(resource != null) {
			where = where(query, where);
			query.append(" exists (")
			     .append("   select relation from repoentrytogroup as relation where relation.group=baseGroup and relation.entry.key=:resourceKey")
			     .append(" )");
		} else if(params.getResources() != null) {
			where = where(query, where);
			query.append(" ").append(params.getResources().booleanValue() ? "" : "not").append(" exists (")
			     .append("   select relation from repoentrytogroup as relation where relation.group=baseGroup")
			     .append(" )");
		}
		
		if(StringHelper.containsNonWhitespace(params.getCourseTitle())) {
			where = where(query, where);
			query.append(" bgi.key in (")
			     .append("   select bgRel.relationId.groupKey from repoentryrelationview bgRel ")
			     .append("     where ");
			searchLikeAttribute(query, "bgRel", "repositoryEntryDisplayName", "displayName");
			query.append(" )");
		}
		
		List<String> roles = null;
		if(params.isOwner() || params.isAttendee() || params.isWaiting()) {
			where = where(query, where);
			roles = new ArrayList<>();
			query.append(" exists (select bmember from bgroupmember as bmember")
			     .append("   where bmember.identity.key=:identId and bmember.group=baseGroup and bmember.role in (:roles)")
			     .append(" )");
			
			if(params.isOwner()) {
				roles.add(GroupRoles.coach.name());
			}
			if(params.isAttendee()) {
				roles.add(GroupRoles.participant.name());
			}
			if(params.isWaiting()) {
				roles.add(GroupRoles.waiting.name());
			}
		}
		
		if(params.getPublicGroups() != null) {
			where = where(query, where);
			if(params.getPublicGroups().booleanValue()) {
		    query.append(" bgResource.key in (")
		         .append("   select offer.resource.key from ").append(OfferImpl.class.getName()).append(" offer ")
		         .append("     where offer.valid=true")
		         .append("     and (offer.validFrom is null or offer.validFrom<=:atDate)")
						 .append("     and (offer.validTo is null or offer.validTo>=:atDate)")
						 .append(" )");
			} else {
		    query.append(" bgResource.key not in (")
		         .append("   select offer.resource.key from ").append(OfferImpl.class.getName()).append(" offer ")
		         .append("     where offer.valid=true")
		         .append(" )");
			}
		}
		
		if(params.getMarked() != null) {
			where = where(query, where);
			query.append(" bgi.key ").append(params.getMarked().booleanValue() ? "" : "not").append(" in (")
			     .append("   select mark.resId from ").append(MarkImpl.class.getName()).append(" mark ")
			     .append("     where mark.resName='BusinessGroup' and mark.creator.key=:identId")
			     .append(" )");
		}
		
		if(StringHelper.containsNonWhitespace(params.getNameOrDesc())) {
			where = where(query, where);
			query.append("(");
			searchLikeAttribute(query, "bgi", "name", "search");
			query.append(" or ");
			searchLikeAttribute(query, "bgi", "description", "search");
			query.append(")");
		} else {
			if(StringHelper.containsNonWhitespace(params.getExactName())) {
				where = where(query, where);
				query.append("bgi.name=:exactName");
			}
			if(StringHelper.containsNonWhitespace(params.getName())) {
				where = where(query, where);
				searchLikeAttribute(query, "bgi", "name", "name");
			}
			if(StringHelper.containsNonWhitespace(params.getDescription())) {
				where = where(query, where);
				searchLikeAttribute(query, "bgi", "description", "description");
			}
		}
		
		if(params.getTools() != null && !params.getTools().isEmpty()) {
			where = where(query, where);
			query.append("bgi.key in (select prop.resourceTypeId from ").append(Property.class.getName()).append(" prop")
				.append(" where prop.category='").append(CollaborationTools.PROP_CAT_BG_COLLABTOOLS).append("'")
				.append(" and prop.name in (:tools) and prop.stringValue='true' and prop.resourceTypeName='BusinessGroup')");
		}
		//order by (not for count)
		if(BusinessGroup.class.equals(resultClass)) {
			query.append(" order by ");
			if(ordering != null && ordering.length > 0) {
				for(BusinessGroupOrder o:ordering) {
					switch(o) {
						case nameAsc: query.append("bgi.name,");break;
						case nameDesc: query.append("bgi.name desc,");break;
						case creationDateAsc: query.append("bgi.creationDate,");break;
						case creationDateDesc: query.append("bgi.creationDate desc,");break;
					}
				}
			} else {
				query.append("bgi.name,");
			}
			query.append("bgi.key");
		}

		TypedQuery<T> dbq = dbInstance.getCurrentEntityManager().createQuery(query.toString(), resultClass);
		//add parameters
		if(params.isOwner() || params.isAttendee() || params.isWaiting() || params.getMarked() != null) {
			dbq.setParameter("identId", params.getIdentity().getKey());
		}
		if(params.getPublicGroups() != null && params.getPublicGroups().booleanValue()) {
			dbq.setParameter("atDate", new Date(), TemporalType.TIMESTAMP);
		}
		if(params.getGroupKeys() != null && !params.getGroupKeys().isEmpty()) {
			dbq.setParameter("groupKeys", params.getGroupKeys());
		}
		if(StringHelper.containsNonWhitespace(params.getExternalId())) {
			dbq.setParameter("externalId", params.getExternalId());
		}
		
		if(StringHelper.containsNonWhitespace(params.getIdRef())) {
			dbq.setParameter("idRefString", params.getIdRef());
			if(id != null) {
				dbq.setParameter("idRefLong", id);
			}
		}
		
		if (resource != null) {
			dbq.setParameter("resourceKey", resource.getKey());
		}
		if(params.getTools() != null && !params.getTools().isEmpty()) {
			dbq.setParameter("tools", params.getTools());
		}
		if(StringHelper.containsNonWhitespace(params.getOwnerName())) {
			dbq.setParameter("owner", makeFuzzyQueryString(params.getOwnerName()));
		}
		if(roles != null) {
			dbq.setParameter("roles", roles);
		}
		if(StringHelper.containsNonWhitespace(params.getNameOrDesc())) {
			dbq.setParameter("search", makeFuzzyQueryString(params.getNameOrDesc()));
		} else {
			if(StringHelper.containsNonWhitespace(params.getExactName())) {
				dbq.setParameter("exactName", params.getExactName());
			}
			if(StringHelper.containsNonWhitespace(params.getName())) {
				dbq.setParameter("name", makeFuzzyQueryString(params.getName()));
			}
			if(StringHelper.containsNonWhitespace(params.getDescription())) {
				dbq.setParameter("description", makeFuzzyQueryString(params.getDescription()));
			}
		}
		if(StringHelper.containsNonWhitespace(params.getCourseTitle())) {
			dbq.setParameter("displayName", makeFuzzyQueryString(params.getCourseTitle()));
		}
		return dbq;
	}
	
	public List<BusinessGroupView> findBusinessGroupWithAuthorConnection(Identity author) {
		StringBuilder sb = new StringBuilder();
		sb.append("select bgi from ").append(BusinessGroupViewImpl.class.getName()).append(" as bgi ")
		  .append(" inner join fetch bgi.resource bgResource ")
		  .append(" inner join fetch bgi.baseGroup as baseGroup")
		  .append(" where baseGroup in (select baseRelGroup.group from ").append(RepositoryEntry.class.getName()).append(" as v, ")
		  .append("   repoentrytogroup as baseRelGroup, repoentrytogroup as relGroup, bgroupmember as remembership ")
		  .append("     where baseRelGroup.entry=v and relGroup.entry=v and relGroup.group=remembership.group ")
		  .append("     and remembership.identity.key=:authorKey")
		  .append("    ")
		  .append(" )");
		
		
		// membership.identity.key=:authorKey

		List<BusinessGroupView> groups = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), BusinessGroupView.class)
				.setParameter("authorKey", author.getKey())
				.getResultList();
		return groups;
	}

	public int countBusinessGroupViews(SearchBusinessGroupParams params, RepositoryEntryRef resource) {
		TypedQuery<Number> query = createFindViewDBQuery(params, resource, Number.class)
				.setHint("org.hibernate.cacheable", Boolean.TRUE);

		Number count = query.getSingleResult();
		return count.intValue();
	}
	
	public List<BusinessGroupView> findBusinessGroupViews(SearchBusinessGroupParams params, RepositoryEntryRef resource,
			int firstResult, int maxResults, BusinessGroupOrder... ordering) {
		TypedQuery<BusinessGroupView> query = createFindViewDBQuery(params, resource, BusinessGroupView.class, ordering);
		query.setFirstResult(firstResult);
		if(maxResults > 0) {
			query.setMaxResults(maxResults);
		}
		List<BusinessGroupView> groups = query.getResultList();
		return groups;
	}
	
	private <T> TypedQuery<T> createFindViewDBQuery(SearchBusinessGroupParams params, RepositoryEntryRef repoEntry, Class<T> resultClass, BusinessGroupOrder... ordering) {
		StringBuilder query = new StringBuilder();
		if(BusinessGroupView.class.equals(resultClass)) {
			query.append("select distinct(bgi) from ");
		} else {
			query.append("select count(bgi.key) from ");
		}
		query.append(BusinessGroupViewImpl.class.getName()).append(" as bgi ");

		//inner joins
		if(BusinessGroupView.class.equals(resultClass)) {
			query.append("inner join fetch bgi.resource as bgResource ");
		} else {
			query.append("inner join bgi.resource as bgResource ");
		}
		
		if(StringHelper.containsNonWhitespace(params.getOwnerName())
				|| repoEntry != null || params.isOwner() || params.isAttendee() || params.isWaiting()) {
			query.append(" inner join bgi.baseGroup as baseGroup");
		}

		boolean where = false;
		if(StringHelper.containsNonWhitespace(params.getOwnerName())) {
			where = true;
			query.append(" inner join baseGroup.members as ownerMember on ownerMember.role='coach'")
			     .append(" inner join ownerMember.identity as identity")
			     .append(" inner join identity.user as user")
			//query the name in login, firstName and lastName
			     .append(" where (");
			searchLikeUserProperty(query, "firstName", "owner");
			query.append(" or ");
			searchLikeUserProperty(query, "lastName", "owner");
			query.append(" or ");
			searchLikeAttribute(query, "identity", "name", "owner");
			query.append(")");
		}
		
		if(params.getGroupKeys() != null && !params.getGroupKeys().isEmpty()) {
			where = where(query, where);
			query.append("bgi.key in (:groupKeys)");
		}
		
		if(repoEntry != null) {
			where = where(query, where);
			query.append(" exists (")
			     .append("  select relation from repoentrytogroup relation where relation.group=baseGroup and relation.entry.key=:resourceKey")
			     .append(")");
		}
		
		if(params.getResources() != null) {
			where = where(query, where);
			query.append(" bgi.numOfRelations").append(params.getResources().booleanValue() ? ">0" : "<=0");
		}
		
		Long id = null;
		if(StringHelper.containsNonWhitespace(params.getIdRef())) {
			if(StringHelper.isLong(params.getIdRef())) {
				try {
					id = new Long(params.getIdRef());
				} catch (NumberFormatException e) {
					//not a real number, can be a very long numerical external id
				}
			}
			where = where(query, where);
			query.append("(bgi.externalId=:idRefString");
			if(id != null) {
				query.append(" or bgi.key=:idRefLong");
			}
			query.append(")");
		}
		
		if(StringHelper.containsNonWhitespace(params.getCourseTitle())) {
			where = where(query, where);
			query.append(" bgi.key in (")
			     .append("   select bgRel.relationId.groupKey from ").append(BGRepositoryEntryRelation.class.getName()).append(" bgRel ")
			     .append("     where ");
			searchLikeAttribute(query, "bgRel", "repositoryEntryDisplayName", "displayName");
			query.append(" )");
		}
		
		List<String> roles = null;
		if(params.isOwner() || params.isAttendee() || params.isWaiting()) {
			where = where(query, where);
			query.append(" exists (select bmember from bgroupmember as bmember")
		         .append("   where bmember.identity.key=:identId and bmember.group=baseGroup and bmember.role in (:roles)")
		         .append(" )");
			
			roles = new ArrayList<>(3);
			if(params.isOwner()) {
				roles.add(GroupRoles.coach.name());
			}
			if(params.isAttendee()) {
				roles.add(GroupRoles.participant.name());
			}
			if(params.isWaiting()) {
				roles.add(GroupRoles.waiting.name());
			}
		}
		
		if(params.getPublicGroups() != null) {
			where = where(query, where);
			if(params.getPublicGroups().booleanValue()) {
		    query.append(" bgi.numOfValidOffers>0");
			} else {
		    query.append(" bgi.numOfOffers<=0");
			}
		}
		
		if(params.getMarked() != null) {
			where = where(query, where);
			query.append(" bgi.key ").append(params.getMarked().booleanValue() ? "" : "not").append(" in (")
			     .append("   select mark.resId from ").append(MarkImpl.class.getName()).append(" mark ")
			     .append("     where mark.resName='BusinessGroup' and mark.creator.key=:identId")
			     .append(" )");
		}
		
		if(params.isHeadless()) {
			where = where(query, where);
			query.append(" bgi.numOfRelations=0 and bgi.numOfOwners=0 and bgi.numOfParticipants=0");
		}
		
		if(params.getNumOfMembers() > -1) {
			where = where(query, where);
			if(params.isNumOfMembersBigger()) {
				query.append(" (bgi.numOfOwners + bgi.numOfParticipants)>=").append(params.getNumOfMembers());
			} else {
				query.append(" (bgi.numOfOwners + bgi.numOfParticipants)<=").append(params.getNumOfMembers());
			}	
		}
		
		if(StringHelper.containsNonWhitespace(params.getNameOrDesc())) {
			where = where(query, where);
			query.append("(");
			searchLikeAttribute(query, "bgi", "name", "search");
			query.append(" or ");
			searchLikeAttribute(query, "bgi", "description", "search");
			query.append(")");
		} else {
			if(StringHelper.containsNonWhitespace(params.getExactName())) {
				where = where(query, where);
				query.append("bgi.name=:exactName");
			}
			if(StringHelper.containsNonWhitespace(params.getName())) {
				where = where(query, where);
				searchLikeAttribute(query, "bgi", "name", "name");
			}
			if(StringHelper.containsNonWhitespace(params.getDescription())) {
				where = where(query, where);
				searchLikeAttribute(query, "bgi", "description", "description");
			}
		}
		
		if(params.getTools() != null && !params.getTools().isEmpty()) {
			where = where(query, where);
			query.append("bgi.key in (select prop.resourceTypeId from ").append(Property.class.getName()).append(" prop")
				.append(" where prop.category='").append(CollaborationTools.PROP_CAT_BG_COLLABTOOLS).append("'")
				.append(" and prop.name in (:tools) and prop.stringValue='true' and prop.resourceTypeName='BusinessGroup')");
		}
		//order by (not for count)
		if(BusinessGroupView.class.equals(resultClass)) {
			query.append(" order by ");
			if(ordering != null && ordering.length > 0) {
				for(BusinessGroupOrder o:ordering) {
					switch(o) {
						case nameAsc: query.append("bgi.name,");break;
						case nameDesc: query.append("bgi.name desc,");break;
						case creationDateAsc: query.append("bgi.creationDate,");break;
						case creationDateDesc: query.append("bgi.creationDate desc,");break;
					}
				}
			} else {
				query.append("bgi.name,");
			}
			query.append("bgi.key");
		}

		TypedQuery<T> dbq = dbInstance.getCurrentEntityManager().createQuery(query.toString(), resultClass);
		//add parameters
		if(params.isOwner() || params.isAttendee() || params.isWaiting() || params.getMarked() != null) {
			dbq.setParameter("identId", params.getIdentity().getKey());
		}
		if(params.getGroupKeys() != null && !params.getGroupKeys().isEmpty()) {
			dbq.setParameter("groupKeys", params.getGroupKeys());
		}
		if(StringHelper.containsNonWhitespace(params.getIdRef())) {
			dbq.setParameter("idRefString", params.getIdRef());
			if(id != null) {
				dbq.setParameter("idRefLong", id);
			}
		}
		if (repoEntry != null) {
			dbq.setParameter("resourceKey", repoEntry.getKey());
		}
		if(params.getTools() != null && !params.getTools().isEmpty()) {
			dbq.setParameter("tools", params.getTools());
		}
		if(StringHelper.containsNonWhitespace(params.getOwnerName())) {
			dbq.setParameter("owner", makeFuzzyQueryString(params.getOwnerName()));
		}
		if(roles != null) {
			dbq.setParameter("roles", roles);
		}
		if(StringHelper.containsNonWhitespace(params.getNameOrDesc())) {
			dbq.setParameter("search", makeFuzzyQueryString(params.getNameOrDesc()));
		} else {
			if(StringHelper.containsNonWhitespace(params.getExactName())) {
				dbq.setParameter("exactName", params.getExactName());
			}
			if(StringHelper.containsNonWhitespace(params.getName())) {
				dbq.setParameter("name", makeFuzzyQueryString(params.getName()));
			}
			if(StringHelper.containsNonWhitespace(params.getDescription())) {
				dbq.setParameter("description", makeFuzzyQueryString(params.getDescription()));
			}
		}
		if(StringHelper.containsNonWhitespace(params.getCourseTitle())) {
			dbq.setParameter("displayName", makeFuzzyQueryString(params.getCourseTitle()));
		}
		return dbq;
	}
	
	private StringBuilder searchLikeUserProperty(StringBuilder sb, String key, String var) {
		if(dbInstance.getDbVendor().equals("mysql")) {
			sb.append(" user.userProperties['").append(key).append("'] like :").append(var);
		} else {
			sb.append(" lower(user.userProperties['").append(key).append("']) like :").append(var);
			if(dbInstance.getDbVendor().equals("oracle")) {
	 	 		sb.append(" escape '\\'");
	 	 	}
		}
		return sb;
	}
	
	private StringBuilder searchLikeAttribute(StringBuilder sb, String objName, String attribute, String var) {
		if(dbInstance.getDbVendor().equals("mysql")) {
			sb.append(" ").append(objName).append(".").append(attribute).append(" like :").append(var);
		} else {
			sb.append(" lower(").append(objName).append(".").append(attribute).append(") like :").append(var);
			if(dbInstance.getDbVendor().equals("oracle")) {
	 	 		sb.append(" escape '\\'");
	 	 	}
		}
		return sb;
	}
	
	private String makeFuzzyQueryString(String string) {
		// By default only fuzzyfy at the end. Usually it makes no sense to do a
		// fuzzy search with % at the beginning, but it makes the query very very
		// slow since it can not use any index and must perform a fulltext search.
		// User can always use * to make it a really fuzzy search query
		string = string.replace('*', '%');
		string = string + "%";
		// with 'LIKE' the character '_' is a wildcard which matches exactly one character.
		// To test for literal instances of '_', we have to escape it.
		string = string.replace("_", "\\_");
		return string.toLowerCase();
	}
	
	private final boolean where(StringBuilder sb, boolean where) {
		if(where) {
			sb.append(" and ");
		} else {
			sb.append(" where ");
		}
		return true;
	}
	
	private final boolean and(StringBuilder sb, boolean and) {
		if(and) sb.append(" and ");
		else sb.append(" where ");
		return true;
	}
}
