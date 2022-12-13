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

import jakarta.persistence.EntityManager;
import jakarta.persistence.LockModeType;
import jakarta.persistence.TypedQuery;

import org.olat.basesecurity.BaseSecurity;
import org.olat.basesecurity.Group;
import org.olat.basesecurity.GroupMembership;
import org.olat.basesecurity.GroupRoles;
import org.olat.basesecurity.IdentityRef;
import org.olat.basesecurity.manager.GroupDAO;
import org.olat.collaboration.CollaborationTools;
import org.olat.core.commons.persistence.DB;
import org.olat.core.commons.persistence.PersistenceHelper;
import org.olat.core.commons.persistence.QueryBuilder;
import org.olat.core.id.Identity;
import org.olat.core.id.Roles;
import org.olat.core.util.StringHelper;
import org.olat.group.BusinessGroup;
import org.olat.group.BusinessGroupImpl;
import org.olat.group.BusinessGroupMembership;
import org.olat.group.BusinessGroupModule;
import org.olat.group.BusinessGroupOrder;
import org.olat.group.BusinessGroupShort;
import org.olat.group.BusinessGroupStatusEnum;
import org.olat.group.model.BusinessGroupMembershipImpl;
import org.olat.group.model.BusinessGroupMembershipInfos;
import org.olat.group.model.BusinessGroupMembershipViewImpl;
import org.olat.group.model.BusinessGroupRow;
import org.olat.group.model.IdentityGroupKey;
import org.olat.group.model.SearchBusinessGroupParams;
import org.olat.properties.Property;
import org.olat.repository.RepositoryEntryRef;
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
	
	public BusinessGroup createAndPersist(Identity creator, String name, String description, String technicalType,
			Integer minParticipants, Integer maxParticipants, boolean waitingListEnabled, boolean autoCloseRanksEnabled,
			boolean showOwners, boolean showParticipants, boolean showWaitingList) {
		return createAndPersist(creator, name, description, technicalType, null, null,
				minParticipants, maxParticipants, waitingListEnabled, autoCloseRanksEnabled,
				showOwners, showParticipants, showWaitingList, null);
	}
		
	public BusinessGroup createAndPersist(Identity creator, String name, String description,
				String technicalType, String externalId, String managedFlags,
				Integer minParticipants, Integer maxParticipants, boolean waitingListEnabled, boolean autoCloseRanksEnabled,
				boolean showOwners, boolean showParticipants, boolean showWaitingList, Boolean allowToLeave) {

		BusinessGroupImpl businessgroup = new BusinessGroupImpl();
		businessgroup.setCreationDate(new Date());
		businessgroup.setLastModified(businessgroup.getCreationDate());
		businessgroup.setLastUsage(businessgroup.getCreationDate());
		businessgroup.setName(name);
		businessgroup.setDescription(description);
		
		businessgroup.setGroupStatus(BusinessGroupStatusEnum.active);
		businessgroup.setExcludeFromAutoLifecycle(false);
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
		businessgroup.setTechnicalType(technicalType);
		businessgroup.setOwnersVisibleIntern(showOwners);
		businessgroup.setParticipantsVisibleIntern(showParticipants);
		businessgroup.setWaitingListVisibleIntern(showWaitingList);
		// group members visibility
		businessgroup.setOwnersVisiblePublic(false);
		businessgroup.setParticipantsVisiblePublic(false);
		businessgroup.setWaitingListVisiblePublic(false);
		businessgroup.setDownloadMembersLists(false);
		
		businessgroup.setLTI13DeploymentByCoachWithAuthorRightsEnabled(false);
		businessgroup.setInvitationByCoachWithAuthorRightsEnabled(false);
		
		if(creator == null) {
			allowToLeave(businessgroup, allowToLeave, businessGroupModule.isAllowLeavingGroupCreatedByAuthors());
		} else {
			Roles roles = securityManager.getRoles(creator);
			if(roles.isAuthor()) {
				allowToLeave(businessgroup, allowToLeave, businessGroupModule.isAllowLeavingGroupCreatedByAuthors());
			} else {
				allowToLeave(businessgroup, allowToLeave, businessGroupModule.isAllowLeavingGroupCreatedByLearners());
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
	
	private void allowToLeave(BusinessGroupImpl businessgroup, Boolean wanted, boolean configuration) {
		if(configuration) {
			businessgroup.setAllowToLeave(wanted == null ? configuration : wanted.booleanValue());
		} else {
			businessgroup.setAllowToLeave(configuration);
		}
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

		List<BusinessGroup> businessGroups = new ArrayList<>(ids.size());
		for (List<Long> chunkOfIds : PersistenceHelper.collectionOfChunks(new ArrayList<>(ids))) {
			List<BusinessGroup> chunkOfBusinessGroups = dbInstance.getCurrentEntityManager()
		 				.createQuery(sb.toString(), BusinessGroup.class)
		 				.setParameter("ids", chunkOfIds)
		 				.getResultList();
			businessGroups.addAll(chunkOfBusinessGroups);
		}
		return businessGroups;
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
	
	public void removeMemberships(BusinessGroup group) {
		groupDao.removeMemberships(group.getBaseGroup());
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

	public BusinessGroupMembershipInfos getMembershipInfos(BusinessGroup businessGroup, IdentityRef identity) {
		List<GroupMembership> memberships = groupDao.getMemberships(businessGroup.getBaseGroup(), identity);
		if(!memberships.isEmpty()) {
			Date creationDate = null;
			Date lastModified = null;
			String role = null;
			if(memberships.size() == 1) {
				role = memberships.get(0).getRole();
				creationDate = memberships.get(0).getCreationDate();
				lastModified = memberships.get(0).getLastModified();
			} else {
				// Last modified is set at creation of the membership, prefer a last modified which is not the same as the creation date
				// but fallback to it because we do it everywhere
				Date notEqualsLastModified = null;
				for(GroupMembership membership:memberships) {
					if(membership.getCreationDate() != null
							&& (creationDate == null || creationDate.after(membership.getCreationDate()))) {
						creationDate = membership.getCreationDate();
					}
					if(membership.getLastModified() != null
							&& (lastModified == null || lastModified.before(membership.getLastModified()))) {
						lastModified = membership.getLastModified();
					}
					if(membership.getLastModified() != null
							&& !membership.getLastModified().equals(membership.getCreationDate()) 
							&& (notEqualsLastModified == null || notEqualsLastModified.before(membership.getLastModified()))) {
						notEqualsLastModified = membership.getLastModified();
					}
				}
				
				if(notEqualsLastModified != null) {
					lastModified = notEqualsLastModified;
				}
			}
			return new BusinessGroupMembershipInfos(identity.getKey(), businessGroup.getKey(), businessGroup.getName(),
					role, creationDate, lastModified);
		}
		return null;
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
		QueryBuilder sb = new QueryBuilder(); 
		sb.append("select membership from bgmembershipview as membership ");
		if(identity != null && identity.length > 0) {
			sb.and().append("membership.identityKey in (:identIds) ");
		}
		if(groupKeys != null && !groupKeys.isEmpty()) {
			sb.and().append("membership.groupKey in (:groupKeys)");
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
		return dbInstance.getCurrentEntityManager().createQuery(sb.toString(), Long.class)
				.setParameter("groupKeys", groupKeys)
				.setParameter("identityKey", identity.getKey())
				.setParameter("roles", roles)
				.setHint("org.hibernate.cacheable", Boolean.TRUE)
				.getResultList();
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
		return query.getResultList();
	}
	
	private <T> TypedQuery<T> createFindDBQuery(SearchBusinessGroupParams params, RepositoryEntryRef resource, Class<T> resultClass, BusinessGroupOrder... ordering) {
		QueryBuilder query = new QueryBuilder();
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

		if(StringHelper.containsNonWhitespace(params.getExternalId())) {
			query.and().append("bgi.externalId=:externalId");
		}
		
		Long id = null;
		if(StringHelper.containsNonWhitespace(params.getIdRef())) {
			if(StringHelper.isLong(params.getIdRef())) {
				try {
					id = Long.valueOf(params.getIdRef());
				} catch (NumberFormatException e) {
					//not a real number, can be a very long numerical external id
				}
			}
			query.and().append("(bgi.externalId=:idRefString");
			if(id != null) {
				query.append(" or bgi.key=:idRefLong");
			}
			query.append(")");
		}
		
		if(params.getManaged() != null) {
			if(params.getManaged().booleanValue()) {
				query.and().append("bgi.managedFlagsString is not null");
			} else {
				query.and().append("bgi.managedFlagsString is null");
			}
		}
		
		if(params.getGroupKeys() != null && !params.getGroupKeys().isEmpty()) {
			query.and().append("bgi.key in (:groupKeys)");
		}
		
		if(resource != null) {
			query.and()
				 .append(" bgi.baseGroup.key in (")
			     .append("   select relation.group.key from repoentrytogroup as relation where relation.entry.key=:resourceKey")
			     .append(" )");
		}
		
		if(StringHelper.containsNonWhitespace(params.getCourseTitle())) {
			query.and()
				 .append(" bgi.key in (")
			     .append("   select bgRel.relationId.groupKey from repoentryrelationview bgRel ")
			     .append("     where ");
			searchLikeAttribute(query, "bgRel", "repositoryEntryDisplayName", "displayName");
			query.append(" )");
		}
		
		List<String> roles = null;
		if(params.isOwner() || params.isAttendee() || params.isWaiting()) {
			roles = new ArrayList<>();
			query.and()
				 .append(" bgi.baseGroup.key in (select bmember.group.key from bgroupmember as bmember")
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
			query.and().append("(");
			searchLikeAttribute(query, "bgi", "name", "search");
			query.append(" or ");
			searchLikeAttribute(query, "bgi", "description", "search");
			query.append(")");
		} else {
			if(StringHelper.containsNonWhitespace(params.getExactName())) {
				query.and().append("bgi.name=:exactName");
			}
			if(StringHelper.containsNonWhitespace(params.getName())) {
				query.and();
				searchLikeAttribute(query, "bgi", "name", "name");
			}
			if(StringHelper.containsNonWhitespace(params.getDescription())) {
				query.and();
				searchLikeAttribute(query, "bgi", "description", "description");
			}
		}
		
		if(params.getTools() != null && !params.getTools().isEmpty()) {
			query.and()
				 .append("bgi.key in (select prop.resourceTypeId from ").append(Property.class.getName()).append(" prop")
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
	
	private QueryBuilder searchLikeAttribute(QueryBuilder sb, String objName, String attribute, String parameter) {
		if(dbInstance.getDbVendor().equals("mysql")) {
			sb.append(" ").append(objName).append(".").append(attribute).append(" like :").append(parameter);
		} else {
			sb.append(" lower(").append(objName).append(".").append(attribute).append(") like :").append(parameter);
			if(dbInstance.getDbVendor().equals("oracle")) {
	 	 		sb.append(" escape '\\'");
	 	 	}
		}
		return sb;
	}
}
