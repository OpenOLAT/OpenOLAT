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
import javax.persistence.TypedQuery;

import org.olat.basesecurity.BaseSecurity;
import org.olat.basesecurity.Group;
import org.olat.basesecurity.GroupRoles;
import org.olat.basesecurity.IdentityRef;
import org.olat.basesecurity.manager.GroupDAO;
import org.olat.basesecurity.model.GroupMembershipImpl;
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
import org.olat.group.model.BusinessGroupMembershipImpl;
import org.olat.group.model.BusinessGroupMembershipInfos;
import org.olat.group.model.BusinessGroupMembershipViewImpl;
import org.olat.group.model.BusinessGroupQueryParams;
import org.olat.group.model.BusinessGroupRow;
import org.olat.group.model.BusinessGroupToSearch;
import org.olat.group.model.IdentityGroupKey;
import org.olat.group.model.OpenBusinessGroupRow;
import org.olat.group.model.SearchBusinessGroupParams;
import org.olat.group.model.StatisticsBusinessGroupRow;
import org.olat.properties.Property;
import org.olat.repository.RepositoryEntryRef;
import org.olat.repository.RepositoryEntryShort;
import org.olat.repository.RepositoryEntryStatusEnum;
import org.olat.resource.OLATResource;
import org.olat.resource.OLATResourceManager;
import org.olat.resource.accesscontrol.Price;
import org.olat.resource.accesscontrol.model.AccessMethod;
import org.olat.resource.accesscontrol.model.PriceMethodBundle;
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
			groupDao.addMembershipTwoWay(group, creator, GroupRoles.coach.name());
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
		sb.append("select bgi from businessgroup bgi ")
		  .append(" left join fetch bgi.baseGroup baseGroup")
		  .append(" left join fetch bgi.resource resource")
		  .append(" where bgi.key=:key");
		List<BusinessGroup> groups = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), BusinessGroup.class)
				.setParameter("key", key)
				.getResultList();
		return groups == null || groups.isEmpty() ? null : groups.get(0);
	}
	
	public String loadDescription(Long key) {
		StringBuilder sb = new StringBuilder();
		sb.append("select bgi.description from businessgroup bgi where bgi.key=:key");
		List<String> descriptions = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), String.class)
				.setParameter("key", key)
				.setHint("org.hibernate.cacheable", Boolean.TRUE)
				.getResultList();
		return descriptions == null || descriptions.isEmpty() ? null : descriptions.get(0);
	}
	
	public List<BusinessGroupShort> loadShort(Collection<Long> ids) {
		if(ids == null || ids.isEmpty()) {
			return Collections.emptyList();
		}

		return dbInstance.getCurrentEntityManager()
				.createNamedQuery("loadBusinessGroupShortByIds", BusinessGroupShort.class)
				.setParameter("ids", ids)
				.setHint("org.hibernate.cacheable", Boolean.TRUE)
				.getResultList();
	}
	
	public List<BusinessGroup> load(Collection<Long> ids) {
		if(ids == null || ids.isEmpty()) {
			return Collections.emptyList();
		}
		StringBuilder sb = new StringBuilder();
		sb.append("select bgi from businessgroup bgi ")
		  .append(" inner join fetch bgi.resource resource")
		  .append(" where bgi.key in (:ids)");

		return dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), BusinessGroup.class)
				.setParameter("ids", ids)
				.getResultList();
	}
	
	public List<BusinessGroup> loadAll() {
		StringBuilder sb = new StringBuilder();
		sb.append("select bgi from businessgroup bgi ")
		  .append(" inner join fetch bgi.resource resource");

		return dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), BusinessGroup.class)
				.getResultList();
	}
	
	public BusinessGroup loadByResourceId(Long resourceId) {
		StringBuilder sb = new StringBuilder();
		sb.append("select bgi from businessgroup bgi ")
		  .append(" inner join fetch bgi.resource resource")
		  .append(" where resource.resName='BusinessGroup' and resource.resId=:resId");

		List<BusinessGroup> groups = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), BusinessGroup.class)
				.setParameter("resId", resourceId)
				.getResultList();
		return groups == null || groups.isEmpty() ? null : groups.get(0);
	}
	
	public BusinessGroup loadForUpdate(Long id) {
		StringBuilder sb = new StringBuilder();
		sb.append("select bgi from businessgroup bgi ")
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
	
	public BusinessGroup loadForUpdate(BusinessGroup group) {
		long groupKey = group.getKey();
		StringBuilder sb = new StringBuilder();
		sb.append("select bgi from businessgroup bgi ")
			.append(" inner join fetch bgi.resource resource")
			.append(" where bgi.key=:key");
		if(dbInstance.getCurrentEntityManager().contains(group)) {
			dbInstance.getCurrentEntityManager().detach(group);
		}
		List<BusinessGroup> groups = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), BusinessGroup.class)
				.setParameter("key", groupKey)
				.setLockMode(LockModeType.PESSIMISTIC_WRITE)
				.getResultList();
		if(groups.isEmpty()) return null;
		return groups.get(0);
	}
	
	/**
	 * Work with the hibernate session
	 * @param group
	 * @return
	 */
	public BusinessGroup merge(BusinessGroup group) {
		return dbInstance.getCurrentEntityManager().merge(group);
	}
	
	/**
	 * The method don't reload/reattach the object, make sure that you have
	 * reloaded the business group before trying to delete it.
	 * 
	 * @param group
	 */
	public void delete(BusinessGroup group) {
		group = load(group.getKey());
		groupDao.removeMemberships(group.getBaseGroup());
		dbInstance.getCurrentEntityManager().remove(group);
		dbInstance.getCurrentEntityManager().remove(group.getBaseGroup());
	}
	
	public List<BusinessGroupMembership> getBusinessGroupsMembership(Collection<BusinessGroup> groups) {
		List<Long> groupKeys = new ArrayList<>();
		for(BusinessGroup group:groups) {
			groupKeys.add(group.getKey());
		}

		Map<IdentityGroupKey, BusinessGroupMembershipImpl> memberships = new HashMap<>();
		loadBusinessGroupsMembership(groupKeys, memberships);
		return new ArrayList<>(memberships.values());
	}
	
	private void loadBusinessGroupsMembership(Collection<Long> groupKeys,
			Map<IdentityGroupKey, BusinessGroupMembershipImpl> memberships) {
		
		if(groupKeys == null || groupKeys.isEmpty()) {
			return;
		}
		
		StringBuilder sb = new StringBuilder(); 
		sb.append("select membership.identity.key, membership.creationDate, membership.lastModified, membership.role, grp.key ")
		  .append(" from businessgroup as grp ")
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
	
	public List<BusinessGroupMembershipInfos> getMemberships(IdentityRef identity) {
		StringBuilder sb = new StringBuilder(256);
		sb.append("select bgi.key, bgi.name, bmember.role, bmember.creationDate, bmember.lastModified")
		  .append(" from businessgroup as bgi")
		  .append(" inner join bgi.baseGroup as baseGroup")
		  .append(" inner join baseGroup.members as bmember")
		  .append(" where bmember.identity.key=:identityKey");
		
		List<Object[]> rawObjects = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), Object[].class)
				.setParameter("identityKey", identity.getKey())
				.getResultList();
		List<BusinessGroupMembershipInfos> memberships = new ArrayList<>(rawObjects.size());
		for(Object[] rawObject:rawObjects) {
			Long businessGroupKey = (Long)rawObject[0];
			String businessGroupName = (String)rawObject[1];
			String role = (String)rawObject[2];
			Date lastModified = (Date)rawObject[3];
			Date creationDate = (Date)rawObject[4];
			
			memberships.add(new BusinessGroupMembershipInfos(identity.getKey(), businessGroupKey, businessGroupName,
					role, creationDate, lastModified));
		}
		return memberships;
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
		List<Long> groupKeys = new ArrayList<>();
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
			List<Long> ids = new ArrayList<>(identity.length);
			for(Identity id:identity) {
				ids.add(id.getKey());
			}
			query.setParameter("identIds", ids);
		}	
		if(groupKeys != null && !groupKeys.isEmpty()) {
			query.setParameter("groupKeys", groupKeys);
		}
		
		return query.getResultList();
	}

	public List<Long> isIdentityInBusinessGroups(Identity identity, boolean owner, boolean attendee, boolean waiting, List<BusinessGroup> groups) {
		if(groups == null || groups.isEmpty() || (!owner && !attendee && !waiting)) {
			return Collections.emptyList();
		}
		
		StringBuilder sb = new StringBuilder(); 
		sb.append("select bgi.key from businessgroup as bgi ")
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
		sb.append("select bgi from businessgroup as bgi ")
		  .append(" inner join fetch bgi.resource as bgResource")
		  .append(" inner join fetch bgi.baseGroup as baseGroup")
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
		sb.append("select bgs from businessgroup as bgs ")
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
		sb.append("select bgs from businessgroup as bgs ")
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
		query.append("businessgroup as bgi ");

		//inner joins
		if(BusinessGroup.class.equals(resultClass)) {
			query.append("inner join fetch bgi.resource bgResource ");
			query.append("inner join fetch bgi.baseGroup as baseGroup ");
		} else {
			query.append("inner join bgi.resource bgResource ");
			query.append("inner join bgi.baseGroup as baseGroup ");
		}

		boolean where = false;
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
			query.append(" bgi.baseGroup.key in (")
			     .append("   select relation.group.key from repoentrytogroup as relation where relation.entry.key=:resourceKey")
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
			query.append(" bgi.baseGroup.key in (select bmember.group.key from bgroupmember as bmember")
			     .append("   where bmember.identity.key=:identId and bmember.role in (:roles)")
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
		if(params.isOwner() || params.isAttendee() || params.isWaiting()) {
			dbq.setParameter("identId", params.getIdentity().getKey());
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
		if(roles != null) {
			dbq.setParameter("roles", roles);
		}
		if(StringHelper.containsNonWhitespace(params.getNameOrDesc())) {
			dbq.setParameter("search", PersistenceHelper.makeFuzzyQueryString(params.getNameOrDesc()));
		} else {
			if(StringHelper.containsNonWhitespace(params.getExactName())) {
				dbq.setParameter("exactName", params.getExactName());
			}
			if(StringHelper.containsNonWhitespace(params.getName())) {
				dbq.setParameter("name", PersistenceHelper.makeFuzzyQueryString(params.getName()));
			}
			if(StringHelper.containsNonWhitespace(params.getDescription())) {
				dbq.setParameter("description", PersistenceHelper.makeFuzzyQueryString(params.getDescription()));
			}
		}
		if(StringHelper.containsNonWhitespace(params.getCourseTitle())) {
			dbq.setParameter("displayName", PersistenceHelper.makeFuzzyQueryString(params.getCourseTitle()));
		}
		return dbq;
	}
	
	public List<StatisticsBusinessGroupRow> searchBusinessGroupsWithMemberships(BusinessGroupQueryParams params, IdentityRef identity) {
	    StringBuilder sm = new StringBuilder();
		sm.append("select memberships, bgi,")
		  .append(" (select count(nCoaches.key) from bgroupmember as nCoaches ")
		  .append("  where nCoaches.group.key=bgi.baseGroup.key and nCoaches.role='").append(GroupRoles.coach.name()).append("'")
		  .append(" ) as numOfCoaches,")
		  .append(" (select count(nParticipants.key) from bgroupmember as nParticipants ")
		  .append("  where nParticipants.group.key=bgi.baseGroup.key and nParticipants.role='").append(GroupRoles.participant.name()).append("'")
		  .append(" ) as numOfParticipants,")
		  .append(" (select count(nWaiting.key) from bgroupmember as nWaiting ")
		  .append("  where bgi.waitingListEnabled=true and nWaiting.group.key=bgi.baseGroup.key and nWaiting.role='").append(GroupRoles.waiting.name()).append("'")
		  .append(" ) as numWaiting,")
		  .append(" (select count(reservation.key) from resourcereservation as reservation ")
		  .append("  where reservation.resource.key=bgi.resource.key")
		  .append(" ) as numOfReservations");
		appendMarkedSubQuery(sm, params);
		sm.append(" from businessgrouptosearch as bgi ")
		  .append(" inner join fetch bgi.resource as bgResource ")
		  .append(" inner join bgi.baseGroup as bGroup ");
		filterBusinessGroupToSearch(sm, params, true);

		TypedQuery<Object[]> objectsQuery = dbInstance.getCurrentEntityManager()
				.createQuery(sm.toString(), Object[].class);
		filterBusinessGroupToSearchParameters(objectsQuery, params, identity, true);
		
		List<Object[]> objects = objectsQuery.getResultList();
		List<StatisticsBusinessGroupRow> groups = new ArrayList<>(objects.size());
		Map<Long,StatisticsBusinessGroupRow> keyToGroup = new HashMap<>();
		Map<Long,StatisticsBusinessGroupRow> resourceKeyToGroup = new HashMap<>();
		for(Object[] object:objects) {
			BusinessGroupToSearch businessGroup = (BusinessGroupToSearch)object[1];
			Number numOfCoaches = (Number)object[2];
			Number numOfParticipants = (Number)object[3];
			Number numWaiting = (Number)object[4];
			Number numPending = (Number)object[5];
			Number numOfMarks = (Number)object[6];
			
			StatisticsBusinessGroupRow row;
			if(keyToGroup.containsKey(businessGroup.getKey())) {
				row = keyToGroup.get(businessGroup.getKey());
			} else {
				row = new StatisticsBusinessGroupRow(businessGroup, numOfCoaches, numOfParticipants, numWaiting, numPending);
				groups.add(row);
				keyToGroup.put(businessGroup.getKey(), row);
				resourceKeyToGroup.put(businessGroup.getResource().getKey(), row);
			}
			row.setMarked(numOfMarks == null ? false : numOfMarks.intValue() > 0);
			addMembershipToRow(row, (GroupMembershipImpl)object[0]);
		}
		
		loadRelations(keyToGroup, params, identity);
		loadOfferAccess(resourceKeyToGroup);	
		return groups;
	}
	
	public List<StatisticsBusinessGroupRow> searchBusinessGroupsForSelection(BusinessGroupQueryParams params, IdentityRef identity) {
		StringBuilder sb = new StringBuilder();
		sb.append("select bgi, ")
		  .append(" (select count(nCoaches.key) from bgroupmember as nCoaches ")
		  .append("  where nCoaches.group.key=bgi.baseGroup.key and nCoaches.role='").append(GroupRoles.coach.name()).append("'")
		  .append(" ) as numOfCoaches,")
		  .append(" (select count(nParticipants.key) from bgroupmember as nParticipants ")
		  .append("  where nParticipants.group.key=bgi.baseGroup.key and nParticipants.role='").append(GroupRoles.participant.name()).append("'")
		  .append(" ) as numOfParticipants,")
		  .append(" (select count(nWaiting.key) from bgroupmember as nWaiting ")
		  .append("  where bgi.waitingListEnabled=true and nWaiting.group.key=bgi.baseGroup.key and nWaiting.role='").append(GroupRoles.waiting.name()).append("'")
		  .append(" ) as numWaiting,")
		  .append(" (select count(reservation.key) from resourcereservation as reservation ")
		  .append("  where reservation.resource.key=bgi.resource.key")
		  .append(" ) as numOfReservations,")
		  .append(" (select count(mark.key) from ").append(MarkImpl.class.getName()).append(" as mark ")
		  .append("   where mark.creator.key=:identityKey and mark.resId=bgi.key and mark.resName='BusinessGroup'")
		  .append(" ) as marks")
		  .append(" from businessgrouptosearch as bgi")
		  .append(" inner join fetch bgi.resource as bgResource ")
		  .append(" inner join bgi.baseGroup as bGroup ");
		filterBusinessGroupToSearch(sb, params, false);

		TypedQuery<Object[]> objectsQuery = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), Object[].class);
		filterBusinessGroupToSearchParameters(objectsQuery, params, identity, true);
		
		List<Object[]> objects = objectsQuery.getResultList();
		List<StatisticsBusinessGroupRow> groups = new ArrayList<>(objects.size());
		Map<Long,BusinessGroupRow> keyToGroup = new HashMap<>();
		for(Object[] object:objects) {
			BusinessGroupToSearch businessGroup = (BusinessGroupToSearch)object[0];
			Number numOfCoaches = (Number)object[1];
			Number numOfParticipants = (Number)object[2];
			Number numWaiting = (Number)object[3];
			Number numPending = (Number)object[4];
			Number marked = (Number)object[5];
			
			StatisticsBusinessGroupRow row
				= new StatisticsBusinessGroupRow(businessGroup, numOfCoaches, numOfParticipants, numWaiting, numPending);
			groups.add(row);
			row.setMarked(marked == null ? false : marked.longValue() > 0);
			keyToGroup.put(businessGroup.getKey(), row);
		}
		
		loadRelations(keyToGroup, params, identity);
		return groups;
	}
	
	public List<StatisticsBusinessGroupRow> searchBusinessGroupsStatistics(BusinessGroupQueryParams params) {
		StringBuilder sb = new StringBuilder();
		sb.append("select bgi, ")
		  .append(" (select count(nCoaches.key) from bgroupmember as nCoaches ")
		  .append("  where nCoaches.group.key=bgi.baseGroup.key and nCoaches.role='").append(GroupRoles.coach.name()).append("'")
		  .append(" ) as numOfCoaches,")
		  .append(" (select count(nParticipants.key) from bgroupmember as nParticipants ")
		  .append("  where nParticipants.group.key=bgi.baseGroup.key and nParticipants.role='").append(GroupRoles.participant.name()).append("'")
		  .append(" ) as numOfParticipants,")
		  .append(" (select count(nWaiting.key) from bgroupmember as nWaiting ")
		  .append("  where bgi.waitingListEnabled=true and nWaiting.group.key=bgi.baseGroup.key and nWaiting.role='").append(GroupRoles.waiting.name()).append("'")
		  .append(" ) as numWaiting,")
		  .append(" (select count(reservation.key) from resourcereservation as reservation ")
		  .append("  where reservation.resource.key=bgi.resource.key")
		  .append(" ) as numOfReservations")
		  .append(" from businessgrouptosearch as bgi")
		  .append(" inner join fetch bgi.resource as bgResource ")
		  .append(" inner join bgi.baseGroup as bGroup ");
		filterBusinessGroupToSearch(sb, params, false);
		sb.append(" order by bgi.name");

		TypedQuery<Object[]> objectsQuery = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), Object[].class);
		filterBusinessGroupToSearchParameters(objectsQuery, params, null, false);
		
		List<Object[]> objects = objectsQuery.getResultList();
		List<StatisticsBusinessGroupRow> groups = new ArrayList<>(objects.size());

		for(Object[] object:objects) {
			BusinessGroupToSearch businessGroup = (BusinessGroupToSearch)object[0];
			Number numOfCoaches = (Number)object[1];
			Number numOfParticipants = (Number)object[2];
			Number numWaiting = (Number)object[3];
			Number numPending = (Number)object[4];

			StatisticsBusinessGroupRow row
				= new StatisticsBusinessGroupRow(businessGroup, numOfCoaches, numOfParticipants, numWaiting, numPending);
			groups.add(row);
		}
		return groups;
	}
	
	/**
	 * 
	 * @param entry
	 * @return
	 */
	public List<StatisticsBusinessGroupRow> searchBusinessGroupsForRepositoryEntry(BusinessGroupQueryParams params, IdentityRef identity, RepositoryEntryRef entry) {
		//name, externalId, description, resources, tutors, participants, free places, waiting, access
		
		StringBuilder sb = new StringBuilder();
		sb.append("select bgi, ")
		  .append(" (select count(nCoaches.key) from bgroupmember as nCoaches ")
		  .append("  where nCoaches.group.key=bgi.baseGroup.key and nCoaches.role='").append(GroupRoles.coach.name()).append("'")
		  .append(" ) as numOfCoaches,")
		  .append(" (select count(nParticipants.key) from bgroupmember as nParticipants ")
		  .append("  where nParticipants.group.key=bgi.baseGroup.key and nParticipants.role='").append(GroupRoles.participant.name()).append("'")
		  .append(" ) as numOfParticipants,")
		  .append(" (select count(nWaiting.key) from bgroupmember as nWaiting ")
		  .append("  where nWaiting.group.key=bgi.baseGroup.key and nWaiting.role='").append(GroupRoles.waiting.name()).append("'")
		  .append(" ) as numWaiting,")
		  .append(" (select count(reservation.key) from resourcereservation as reservation ")
		  .append("  where reservation.resource.key=bgi.resource.key")
		  .append(" ) as numOfReservations")
		  .append(" from businessgrouptosearch as bgi")
		  .append(" inner join fetch bgi.resource as bgResource ")
		  .append(" inner join bgi.baseGroup as bGroup ");
		if(params.getRepositoryEntry() == null) {
			params.setRepositoryEntry(entry);//make sur the restricition is applied
		}
		filterBusinessGroupToSearch(sb, params, false);
		
		TypedQuery<Object[]> objectsQuery = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), Object[].class);
		filterBusinessGroupToSearchParameters(objectsQuery, params, identity, false);
		
		List<Object[]> objects = objectsQuery.getResultList();
		List<StatisticsBusinessGroupRow> groups = new ArrayList<>(objects.size());
		Map<Long,BusinessGroupRow> keyToGroup = new HashMap<>();
		Map<Long,BusinessGroupRow> resourceKeyToGroup = new HashMap<>();
		for(Object[] object:objects) {
			BusinessGroupToSearch businessGroup = (BusinessGroupToSearch)object[0];
			Number numOfCoaches = (Number)object[1];
			Number numOfParticipants = (Number)object[2];
			Number numWaiting = (Number)object[3];
			Number numPending = (Number)object[4];

			StatisticsBusinessGroupRow row
				= new StatisticsBusinessGroupRow(businessGroup, numOfCoaches, numOfParticipants, numWaiting, numPending);
			groups.add(row);
			keyToGroup.put(businessGroup.getKey(), row);
			resourceKeyToGroup.put(businessGroup.getResource().getKey(), row);
		}
		
		loadOfferAccess(resourceKeyToGroup);
		loadRelations(keyToGroup, params, identity);
		return groups;
	}
	
	/**
	 * 
	 * @return
	 */
	public List<OpenBusinessGroupRow> searchPublishedBusinessGroups(BusinessGroupQueryParams params, IdentityRef identity) {
		//need resources, access type, membership, num of pending, num of participants
		StringBuilder sb = new StringBuilder();
		sb.append("select bgi, ")
		  .append(" (select count(nParticipants.key) from bgroupmember as nParticipants ")
		  .append("  where nParticipants.group.key=bgi.baseGroup and nParticipants.role='").append(GroupRoles.participant.name()).append("'")
		  .append(" ) as numOfParticipants,")
		  .append(" (select count(reservation.key) from resourcereservation as reservation ")
		  .append("  where reservation.resource.key=bgi.resource.key")
		  .append(" ) as numOfReservations")
		  .append(" from businessgrouptosearch as bgi ")
		  .append(" inner join fetch bgi.resource as bgResource ")
		  .append(" inner join fetch bgi.baseGroup as bGroup ");
		filterBusinessGroupToSearch(sb, params, false);
		
		TypedQuery<Object[]> queryObjects = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), Object[].class);
		filterBusinessGroupToSearchParameters(queryObjects, params, identity, false);
		
		List<Object[]> objects = queryObjects.getResultList();
		List<OpenBusinessGroupRow> groups = new ArrayList<>(objects.size());
		Map<Long, OpenBusinessGroupRow> keyToGroup = new HashMap<>();
		Map<Long, OpenBusinessGroupRow> resourceKeyToGroup = new HashMap<>();
		for(Object[] object:objects) {
			BusinessGroupToSearch businessGroup = (BusinessGroupToSearch)object[0];
			if(!keyToGroup.containsKey(businessGroup.getKey())) {
				Long numOfParticipants = (Long)object[1];
				Long numOfReservations = (Long)object[2];
				
				OpenBusinessGroupRow row = new OpenBusinessGroupRow(businessGroup, numOfParticipants, numOfReservations);
				groups.add(row);
				keyToGroup.put(businessGroup.getKey(), row);
				resourceKeyToGroup.put(businessGroup.getResource().getKey(), row);
			}
		}
		
		loadRelations(keyToGroup, params, identity);
		loadOfferAccess(resourceKeyToGroup);
		loadMemberships(identity, keyToGroup);
		return groups;
	}
	
	private void filterBusinessGroupToSearchParameters(TypedQuery<?> query, BusinessGroupQueryParams params, IdentityRef identity, boolean needIdentity) {
		boolean memberOnly = params.isAttendee() || params.isOwner() || params.isWaiting();

		if(memberOnly) {
			List<String> roles = new ArrayList<>(3);
			if(params.isOwner()) {
				roles.add(GroupRoles.coach.name());
			}
			if(params.isAttendee()) {
				roles.add(GroupRoles.participant.name());
			}
			if(params.isWaiting()) {
				roles.add(GroupRoles.waiting.name());
			}
			query.setParameter("roles", roles);
		}
	
		if(memberOnly || needIdentity || params.isMarked() || params.isAuthorConnection()) {
			query.setParameter("identityKey", identity.getKey());
		}
		
		if(params.getBusinessGroupKeys() != null && !params.getBusinessGroupKeys().isEmpty()) {
			query.setParameter("businessGroupKeys", params.getBusinessGroupKeys());
		}
		
		if(params.getRepositoryEntry() != null) {
			query.setParameter("repoEntryKey", params.getRepositoryEntry().getKey());
		}
		
		//owner
		if(StringHelper.containsNonWhitespace(params.getOwnerName())) {
			query.setParameter("owner", PersistenceHelper.makeFuzzyQueryString(params.getOwnerName()));
		}
		
		//id
		if(StringHelper.containsNonWhitespace(params.getIdRef())) {
			if(StringHelper.isLong(params.getIdRef())) {
				try {
					Long id = Long.valueOf(params.getIdRef());
					query.setParameter("idRefLong", id);
				} catch (NumberFormatException e) {
					//not a real number, can be a very long numerical external id
				}
			}
			query.setParameter("idRefString", params.getIdRef());
		}
		
		//name
		if(StringHelper.containsNonWhitespace(params.getNameOrDesc())) {
			query.setParameter("search", PersistenceHelper.makeFuzzyQueryString(params.getNameOrDesc()));
		} else {
			if(StringHelper.containsNonWhitespace(params.getName())) {
				query.setParameter("name", PersistenceHelper.makeFuzzyQueryString(params.getName()));
			}
			if(StringHelper.containsNonWhitespace(params.getDescription())) {
				query.setParameter("description", PersistenceHelper.makeFuzzyQueryString(params.getDescription()));
			}
		}
		
		//course title
		if(StringHelper.containsNonWhitespace(params.getCourseTitle())) {
			query.setParameter("displayName", PersistenceHelper.makeFuzzyQueryString(params.getCourseTitle()));
		}
		
		//public group
		if(params.getPublicGroups() != null) {
			if(params.getPublicGroups().booleanValue()) {
				query.setParameter("atDate", new Date());
			}
		}
	}
	
	private void filterBusinessGroupToSearch(StringBuilder sb, BusinessGroupQueryParams params, boolean includeMemberships) {
		boolean where = false;
		boolean memberOnly = params.isAttendee() || params.isOwner() || params.isWaiting();
		
		if(memberOnly) {
			sb.append("inner join bGroup.members as memberships on (memberships.identity.key=:identityKey and memberships.role in (:roles))");	
		} else if(includeMemberships) {
			sb.append("left join bGroup.members as memberships on (memberships.identity.key=:identityKey)");	
		}
		
		//coach / owner
		if(StringHelper.containsNonWhitespace(params.getOwnerName())) {
			where = true;
			sb.append(" inner join bGroup.members as ownerMember on ownerMember.role='coach'")
			  .append(" inner join ownerMember.identity as ownerIdentity")
			  .append(" inner join ownerIdentity.user as ownerUser")
			//query the name in login, firstName and lastName
			  .append(" where (");
			searchLikeOwnerUserProperty(sb, "firstName", "owner");
			sb.append(" or ");
			searchLikeOwnerUserProperty(sb, "lastName", "owner");
			sb.append(" or ");
			searchLikeAttribute(sb, "ownerIdentity", "name", "owner");
			sb.append(")");
		}
		
		if(params.getBusinessGroupKeys() != null && !params.getBusinessGroupKeys().isEmpty()) {
			where = PersistenceHelper.appendAnd(sb, where);
			sb.append(" bgi.key in (:businessGroupKeys)");
		}
		
		if(params.isMarked()) {
			where = PersistenceHelper.appendAnd(sb, where);
			sb.append(" exists (select mark.key from ").append(MarkImpl.class.getName()).append(" as mark ")
			  .append("  where mark.creator.key=:identityKey and mark.resId=bgi.key and mark.resName='BusinessGroup'")
			  .append(" )");
		}
		
		if(params.isAuthorConnection()) {
			where = PersistenceHelper.appendAnd(sb, where);
			sb.append(" bGroup.key in (select baseRelGroup.group.key from repositoryentry as v,")
			  .append("   repoentrytogroup as baseRelGroup, repoentrytogroup as relGroup, bgroupmember as remembership")
			  .append("     where baseRelGroup.entry.key=v.key and relGroup.entry.key=v.key and relGroup.group.key=remembership.group.key")
			  .append("     and remembership.identity.key=:identityKey and remembership.role='owner'")
			  .append(" )");
		}
		
		//id
		if(StringHelper.containsNonWhitespace(params.getIdRef())) {
			where = PersistenceHelper.appendAnd(sb, where);
			sb.append("(bgi.externalId=:idRefString");
			if(StringHelper.isLong(params.getIdRef())) {
				try {
					Long.parseLong(params.getIdRef());
					sb.append(" or bgi.key=:idRefLong");
				} catch (NumberFormatException e) {
					//not a real number, can be a very long numerical external id
				}
			}
			sb.append(")");
		}
		
		//name
		if(StringHelper.containsNonWhitespace(params.getNameOrDesc())) {
			where = PersistenceHelper.appendAnd(sb, where);
			sb.append("(");
			searchLikeAttribute(sb, "bgi", "name", "search");
			sb.append(" or ");
			searchLikeAttribute(sb, "bgi", "description", "search");
			sb.append(")");
		} else {
			if(StringHelper.containsNonWhitespace(params.getName())) {
				where = PersistenceHelper.appendAnd(sb, where);
				searchLikeAttribute(sb, "bgi", "name", "name");
			}
			if(StringHelper.containsNonWhitespace(params.getDescription())) {
				where = PersistenceHelper.appendAnd(sb, where);
				searchLikeAttribute(sb, "bgi", "description", "description");
			}
		}
	
		// course title
		if(StringHelper.containsNonWhitespace(params.getCourseTitle())) {
			where = PersistenceHelper.appendAnd(sb, where);
			sb.append(" bgi.baseGroup.key in (select baseRelGroup.group.key from repositoryentry as v")
			  .append("  inner join v.groups as baseRelGroup")
			  .append("  where baseRelGroup.entry.key=v.key and ");
			searchLikeAttribute(sb, "v", "displayname", "displayName");
			sb.append(" )");	
		}
		
		// open/public or not
		if(params.getPublicGroups() != null) {
			where = PersistenceHelper.appendAnd(sb, where);
			if(params.getPublicGroups().booleanValue()) {
				sb.append(" bgi.resource.key in (")
		         .append("   select offer.resource.key from acoffer offer ")
		         .append("     where offer.valid=true")
		         .append("     and (offer.validFrom is null or offer.validFrom<=:atDate)")
				 .append("     and (offer.validTo is null or offer.validTo>=:atDate)")
				 .append(" )");
				
			} else {
				sb.append(" bgi.resource.key not in (")
		          .append("   select offer.resource.key from acoffer offer ")
		          .append("     where offer.valid=true")
		          .append(" )");
			}
		}
		
		if(params.getRepositoryEntry() != null) {
			where = PersistenceHelper.appendAnd(sb, where);
			sb.append(" bgi.baseGroup.key in (select entryRel.group.key from repoentrytogroup as entryRel where entryRel.entry.key=:repoEntryKey)");
		}
		
		// in course or not
		if(params.getResources() != null || params.isHeadless()) {
			where = PersistenceHelper.appendAnd(sb, where);
			if(params.getResources() != null && params.getResources().booleanValue()) {
				sb.append(" exists (select resourceRel.key from repoentrytogroup as resourceRel where bgi.baseGroup.key=resourceRel.group.key )");
			} else {
				sb.append(" not exists (select resourceRel.key from repoentrytogroup as resourceRel where resourceRel.group.key=bGroup.key)");
			}
		}
		
		// orphans
		if(params.isHeadless()) {
			where = PersistenceHelper.appendAnd(sb, where);
			sb.append(" not exists (select headMembership.key from bgroupmember as headMembership")
			  .append("   where bGroup.key=headMembership.group.key and headMembership.role in ('").append(GroupRoles.coach.name()).append("','").append(GroupRoles.participant.name()).append("')")
			  .append(" )");
		}
	}
	
	private void loadMemberships(IdentityRef identity, Map<Long, ? extends BusinessGroupRow> keyToGroup) {
		//memberships
	    StringBuilder sm = new StringBuilder();
		sm.append("select membership, bgi.key")
		  .append(" from businessgroup as bgi ")
		  .append(" inner join bgi.baseGroup as bGroup ")
		  .append(" inner join bGroup.members as membership")
		  .append(" where membership.identity.key=:identityKey");
		
		List<Object[]> memberships = dbInstance.getCurrentEntityManager()
				.createQuery(sm.toString(), Object[].class)
				.setParameter("identityKey", identity.getKey())
				.getResultList();
		for(Object[] membership:memberships) {
			Long groupkey = (Long)membership[1];
			BusinessGroupRow row = keyToGroup.get(groupkey);
			addMembershipToRow(row, (GroupMembershipImpl)membership[0]);
		}
	}

	private void loadRelations(Map<Long, ? extends BusinessGroupRow> keyToGroup, BusinessGroupQueryParams params, IdentityRef identity) {
		if(keyToGroup.isEmpty()) return;
		if(params.getResources() != null && !params.getResources().booleanValue()) return;//no resources, no relations
		if(params.isHeadless()) return; //headless don't have relations
		
		final int RELATIONS_IN_LIMIT = 64;
		final boolean restrictToMembership = params != null && identity != null
				&& (params.isAttendee() || params.isOwner() || params.isWaiting() || params.isMarked());
		
		//resources
		StringBuilder sr = new StringBuilder();
		sr.append("select entry.key, entry.displayname, bgi.key from repoentrytobusinessgroup as v")
		  .append(" inner join v.entry entry")
		  .append(" inner join v.businessGroup relationToGroup")
		  .append(" inner join relationToGroup.businessGroups bgi");
		if(restrictToMembership) {
			sr.append(" inner join bgi.baseGroup as bGroup ")
			  .append(" inner join bGroup.members as membership on membership.identity.key=:identityKey");
		} else if(keyToGroup.size() < RELATIONS_IN_LIMIT) {
			sr.append(" where bgi.key in (:businessGroupKeys)");
		} else if(params.getRepositoryEntry() != null) {
			sr.append(" inner join repoentrytobusinessgroup as refBgiToGroup")
			  .append("   on (refBgiToGroup.entry.key=:repositoryEntryKey and bgi.baseGroup.key=refBgiToGroup.businessGroup.key)");
		} else {
			sr.append(" inner join bgi.resource as bgResource ")
			  .append(" inner join bgi.baseGroup as bGroup ");
			filterBusinessGroupToSearch(sr, params, false);
		}
		
		TypedQuery<Object[]> resourcesQuery = dbInstance.getCurrentEntityManager()
				.createQuery(sr.toString(), Object[].class);
		if(restrictToMembership) {
			resourcesQuery.setParameter("identityKey", identity.getKey());
		} else if(keyToGroup.size() < RELATIONS_IN_LIMIT) {
			List<Long> businessGroupKeys = new ArrayList<>(keyToGroup.size());
			for(Long businessGroupKey:keyToGroup.keySet()) {
				businessGroupKeys.add(businessGroupKey);
			}
			resourcesQuery.setParameter("businessGroupKeys", businessGroupKeys);
		} else if(params.getRepositoryEntry() != null) {
			resourcesQuery.setParameter("repositoryEntryKey", params.getRepositoryEntry().getKey());
		} else {
			filterBusinessGroupToSearchParameters(resourcesQuery, params, identity, false);
		}
		
		List<Object[]> resources = resourcesQuery.getResultList();
		for(Object[] resource:resources) {
			Long groupKey = (Long)resource[2];
			BusinessGroupRow row = keyToGroup.get(groupKey);
			if(row != null) {
				Long entryKey = (Long)resource[0];
				String displayName = (String)resource[1];
				REShort entry = new REShort(entryKey, displayName);
				if(row.getResources() == null) {
					row.setResources(new ArrayList<>(4));
				}
				row.getResources().add(entry);
			}
		}
	}
	
	public void loadOfferAccess(Map<Long, ? extends BusinessGroupRow> resourceKeyToGroup) {
		if(resourceKeyToGroup.isEmpty()) return;

		final int OFFERS_IN_LIMIT = 255;
		
		//offers
		StringBuilder so = new StringBuilder();
		so.append("select access.method, resource.key, offer.price from acofferaccess access ")
			.append(" inner join access.offer offer")
			.append(" inner join offer.resource resource")
			.append(" where offer.valid=true");
		if(resourceKeyToGroup.size() < OFFERS_IN_LIMIT) {
			so.append(" and resource.key in (:resourceKeys)");
		} else {
			so.append(" and exists (select bgi.key from businessgroup bgi where bgi.resource=resource)");
		}
			
		TypedQuery<Object[]> offersQuery = dbInstance.getCurrentEntityManager()
				.createQuery(so.toString(), Object[].class);
				
		if(resourceKeyToGroup.size() < OFFERS_IN_LIMIT) {
			List<Long> resourceKeys = new ArrayList<>(resourceKeyToGroup.size());
			for(Long resourceKey:resourceKeyToGroup.keySet()) {
				resourceKeys.add(resourceKey);
			}
			offersQuery.setParameter("resourceKeys", resourceKeys);
		}
		
		List<Object[]> offers = offersQuery.getResultList();
		for(Object[] offer:offers) {
			Long resourceKey = (Long)offer[1];
			
			BusinessGroupRow row = resourceKeyToGroup.get(resourceKey);
			if(row != null) {
				AccessMethod method = (AccessMethod)offer[0];
				Price price = (Price)offer[2];
				if(row.getBundles() == null) {
					row.setBundles(new ArrayList<>(3));
				}
				row.getBundles().add(new PriceMethodBundle(price, method));	
			}
		}
	}
	
	private void addMembershipToRow(BusinessGroupRow row, GroupMembershipImpl member) {
		if(row != null && member != null) {
			if(row.getMember() == null) {
				row.setMember(new BusinessGroupMembershipImpl());
			}
			
			String role = member.getRole();
			BusinessGroupMembershipImpl mb = row.getMember();
			mb.setCreationDate(member.getCreationDate());
			mb.setLastModified(member.getLastModified());
			if(GroupRoles.coach.name().equals(role)) {
				mb.setOwner(true);
			} else if(GroupRoles.participant.name().equals(role)) {
				mb.setParticipant(true);
			} else if(GroupRoles.waiting.name().equals(role)) {
				mb.setWaiting(true);
			}
		}
	}
	
	private void appendMarkedSubQuery(StringBuilder sb, BusinessGroupQueryParams params) {
		if(params.isMarked()) {
			sb.append(" ,1 as marks");
		} else {
			sb.append(" ,(select count(mark.key) from ").append(MarkImpl.class.getName()).append(" as mark ")
			  .append("   where mark.creator.key=:identityKey and mark.resId=bgi.key and mark.resName='BusinessGroup'")
			  .append(" ) as marks");
		}
	}
	
	private StringBuilder searchLikeOwnerUserProperty(StringBuilder sb, String key, String var) {
		if(dbInstance.getDbVendor().equals("mysql")) {
			sb.append(" ownerUser.").append(key).append(" like :").append(var);
		} else {
			sb.append(" lower(ownerUser.").append(key).append(") like :").append(var);
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
	

	private static class REShort implements RepositoryEntryShort {
		private final Long key;
		private final String displayname;
		public REShort(Long entryKey, String displayname) {
			this.key = entryKey;
			this.displayname = displayname;
		}

		@Override
		public Long getKey() {
			return key;
		}

		@Override
		public String getDisplayname() {
			return displayname;
		}

		@Override
		public String getResourceType() {
			return "CourseModule";
		}

		@Override
		public RepositoryEntryStatusEnum getEntryStatus() {
			return null;
		}

		@Override
		public int hashCode() {
			return key.hashCode();
		}

		@Override
		public boolean equals(Object obj) {
			if(this == obj) {
				return true;
			}
			if(obj instanceof REShort) {
				REShort re = (REShort)obj;
				return key != null && key.equals(re.key);
			}
			return false;
		}
	}
}
