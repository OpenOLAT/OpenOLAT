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
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.persistence.TemporalType;
import javax.persistence.TypedQuery;

import org.olat.basesecurity.GroupRoles;
import org.olat.basesecurity.IdentityRef;
import org.olat.basesecurity.model.GroupMembershipImpl;
import org.olat.core.commons.persistence.DB;
import org.olat.core.commons.persistence.PersistenceHelper;
import org.olat.core.commons.persistence.QueryBuilder;
import org.olat.core.commons.services.mark.impl.MarkImpl;
import org.olat.core.util.DateUtils;
import org.olat.core.util.StringHelper;
import org.olat.group.BusinessGroupModule;
import org.olat.group.BusinessGroupStatusEnum;
import org.olat.group.model.BusinessGroupMembershipImpl;
import org.olat.group.model.BusinessGroupQueryParams;
import org.olat.group.model.BusinessGroupQueryParams.LifecycleSyntheticStatus;
import org.olat.group.model.BusinessGroupRow;
import org.olat.group.model.BusinessGroupToSearch;
import org.olat.group.model.OpenBusinessGroupRow;
import org.olat.group.model.StatisticsBusinessGroupRow;
import org.olat.repository.RepositoryEntryRef;
import org.olat.repository.RepositoryEntryShort;
import org.olat.repository.RepositoryEntryStatusEnum;
import org.olat.resource.accesscontrol.Price;
import org.olat.resource.accesscontrol.model.AccessMethod;
import org.olat.resource.accesscontrol.model.PriceMethodBundle;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
@Service
public class BusinessGroupQueries {

	@Autowired
	private DB dbInstance;
	@Autowired
	private BusinessGroupModule businessGroupModule;
	
	
	public List<StatisticsBusinessGroupRow> searchBusinessGroupsWithMemberships(BusinessGroupQueryParams params, IdentityRef identity) {
	    QueryBuilder sm = new QueryBuilder();
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
		LifecycleParams lifecycleParams = new LifecycleParams();
		filterBusinessGroupToSearch(sm, params, lifecycleParams, true);

		TypedQuery<Object[]> objectsQuery = dbInstance.getCurrentEntityManager()
				.createQuery(sm.toString(), Object[].class);
		filterBusinessGroupToSearchParameters(objectsQuery, params, lifecycleParams, identity, true);
		
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
		QueryBuilder sb = new QueryBuilder();
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
		LifecycleParams lifecycleParams = new LifecycleParams();
		filterBusinessGroupToSearch(sb, params, lifecycleParams, false);

		TypedQuery<Object[]> objectsQuery = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), Object[].class);
		filterBusinessGroupToSearchParameters(objectsQuery, params, lifecycleParams, identity, true);
		
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
		QueryBuilder sb = new QueryBuilder();
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
		LifecycleParams lifecycleParams = new LifecycleParams();
		filterBusinessGroupToSearch(sb, params, lifecycleParams, false);
		sb.append(" order by bgi.name");

		TypedQuery<Object[]> objectsQuery = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), Object[].class);
		filterBusinessGroupToSearchParameters(objectsQuery, params, lifecycleParams, null, false);
		
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
		
		QueryBuilder sb = new QueryBuilder();
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
		LifecycleParams lifecycleParams = new LifecycleParams();
		filterBusinessGroupToSearch(sb, params, lifecycleParams, false);
		
		TypedQuery<Object[]> objectsQuery = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), Object[].class);
		filterBusinessGroupToSearchParameters(objectsQuery, params, lifecycleParams, identity, false);
		
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
		QueryBuilder sb = new QueryBuilder();
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
		LifecycleParams lifecycleParams = new LifecycleParams();
		filterBusinessGroupToSearch(sb, params, lifecycleParams, false);
		
		TypedQuery<Object[]> queryObjects = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), Object[].class);
		filterBusinessGroupToSearchParameters(queryObjects, params, lifecycleParams, identity, false);
		
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
	
	private void filterBusinessGroupToSearchParameters(TypedQuery<?> query, BusinessGroupQueryParams params,
			LifecycleParams lifecycleParams, IdentityRef identity, boolean needIdentity) {
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
		
		//technical type
		if(params.getTechnicalTypes() != null && !params.getTechnicalTypes().isEmpty()) {
			query.setParameter("technicalTypes", params.getTechnicalTypes());
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
		
		// last usage
		if(params.getLastUsageBefore() != null) {
			query.setParameter("lastUsageBefore", params.getLastUsageBefore());
		}
		
		if(params.getGroupStatus() != null && !params.getGroupStatus().isEmpty()) {
			List<String> statusList = params.getGroupStatus().stream()
					.map(BusinessGroupStatusEnum::name)
					.collect(Collectors.toList());
			query.setParameter("groupStatus", statusList);
		}
		
		if(lifecycleParams != null) {
			if(lifecycleParams.getLastUsagePeriodStart() != null) {
				query.setParameter("lastUsagePeriodStart", lifecycleParams.getLastUsagePeriodStart(), TemporalType.TIMESTAMP);
			}
			if(lifecycleParams.getInactivationPeriodStart() != null) {
				query.setParameter("inactivationPeriodStart", lifecycleParams.getInactivationPeriodStart(), TemporalType.TIMESTAMP);
			}
			if(lifecycleParams.getSoftDeletePeriodStart() != null) {
				query.setParameter("softDeletePeriodStart", lifecycleParams.getSoftDeletePeriodStart(), TemporalType.TIMESTAMP);
			}
		}
	}
	
	private void filterBusinessGroupToSearch(QueryBuilder sb, BusinessGroupQueryParams params, LifecycleParams lifecycleParams, boolean includeMemberships) {
		boolean memberOnly = params.isAttendee() || params.isOwner() || params.isWaiting();
		
		if(memberOnly) {
			sb.append("inner join bGroup.members as memberships on (memberships.identity.key=:identityKey and memberships.role in (:roles))");	
		} else if(includeMemberships) {
			sb.append("left join bGroup.members as memberships on (memberships.identity.key=:identityKey)");	
		}
		
		//coach / owner
		if(StringHelper.containsNonWhitespace(params.getOwnerName())) {
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
			sb.and().append(" bgi.key in (:businessGroupKeys)");
		}
		
		if(params.isMarked()) {
			sb.and().append(" exists (select mark.key from ").append(MarkImpl.class.getName()).append(" as mark ")
			  .append("  where mark.creator.key=:identityKey and mark.resId=bgi.key and mark.resName='BusinessGroup'")
			  .append(" )");
		}
		
		if(params.isAuthorConnection()) {
			sb.and().append(" bGroup.key in (select baseRelGroup.group.key from repositoryentry as v,")
			  .append("   repoentrytogroup as baseRelGroup, repoentrytogroup as relGroup, bgroupmember as remembership")
			  .append("     where baseRelGroup.entry.key=v.key and relGroup.entry.key=v.key and relGroup.group.key=remembership.group.key")
			  .append("     and remembership.identity.key=:identityKey and remembership.role='owner'")
			  .append(" )");
		}
		
		//id
		if(StringHelper.containsNonWhitespace(params.getIdRef())) {
			sb.and().append("(bgi.externalId=:idRefString");
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
			sb.and().append("(");
			searchLikeAttribute(sb, "bgi", "name", "search");
			sb.append(" or ");
			searchLikeAttribute(sb, "bgi", "description", "search");
			sb.append(")");
		} else {
			if(StringHelper.containsNonWhitespace(params.getName())) {
				sb.and();
				searchLikeAttribute(sb, "bgi", "name", "name");
			}
			if(StringHelper.containsNonWhitespace(params.getDescription())) {
				sb.and();
				searchLikeAttribute(sb, "bgi", "description", "description");
			}
		}
		
		//technical type
		if(params.getTechnicalTypes() != null && !params.getTechnicalTypes().isEmpty()) {
			sb.and().append("bgi.technicalType in (:technicalTypes)");
		}
	
		// course title
		if(StringHelper.containsNonWhitespace(params.getCourseTitle())) {
			sb.and()
			  .append(" bgi.baseGroup.key in (select baseRelGroup.group.key from repositoryentry as v")
			  .append("  inner join v.groups as baseRelGroup")
			  .append("  where baseRelGroup.entry.key=v.key and ");
			searchLikeAttribute(sb, "v", "displayname", "displayName");
			sb.append(" )");	
		}
		
		// open/public or not
		if(params.getPublicGroups() != null) {
			if(params.getPublicGroups().booleanValue()) {
				sb.and()
				  .append(" bgi.resource.key in (")
		          .append("   select offer.resource.key from acoffer offer ")
		          .append("     where offer.valid=true")
		          .append("     and (offer.validFrom is null or offer.validFrom<=:atDate)")
				  .append("     and (offer.validTo is null or offer.validTo>=:atDate)")
				  .append(" )");
				
			} else {
				sb.and()
				  .append(" bgi.resource.key not in (")
		          .append("   select offer.resource.key from acoffer offer ")
		          .append("     where offer.valid=true")
		          .append(" )");
			}
		}
		
		if(params.getManaged() != null) {
			if(params.getManaged().booleanValue()) {
				sb.and().append(" (bgi.managedFlagsString is not null or bgi.externalId is not null)");
			} else {
				sb.and().append(" (bgi.managedFlagsString is null and bgi.externalId is null)");
			}
		}
		
		if(params.getRepositoryEntry() != null) {
			sb.and().append(" bgi.baseGroup.key in (select entryRel.group.key from repoentrytogroup as entryRel where entryRel.entry.key=:repoEntryKey)");
		}
		
		// in course or not
		if(params.getResources() != null || params.isHeadless()) {
			if(params.getResources() != null && params.getResources().booleanValue()) {
				sb.and().append(" exists (select resourceRel.key from repoentrytogroup as resourceRel where bgi.baseGroup.key=resourceRel.group.key )");
			} else {
				sb.and().append(" not exists (select resourceRel.key from repoentrytogroup as resourceRel where resourceRel.group.key=bGroup.key)");
			}
		}
		
		// orphans
		if(params.isHeadless()) {
			sb.and()
			  .append(" not exists (select headMembership.key from bgroupmember as headMembership")
			  .append("   where bGroup.key=headMembership.group.key and headMembership.role in ('").append(GroupRoles.coach.name()).append("','").append(GroupRoles.participant.name()).append("')")
			  .append(" )");
		}
		
		// group status
		if(params.getGroupStatus() != null && !params.getGroupStatus().isEmpty()) {
			sb.and().append(" bgi.status in (:groupStatus)");
		}
		
		// lifecylce - last usage
		if(params.getLifecycleStatus() == LifecycleSyntheticStatus.ACTIVE
				|| params.getLifecycleStatus() == LifecycleSyntheticStatus.ACTIVE_LONG
				|| params.getLifecycleStatus() == LifecycleSyntheticStatus.ACTIVE_RESPONSE_DELAY
				|| params.getLifecycleStatus() == LifecycleSyntheticStatus.TO_START_INACTIVATE
				|| params.getLifecycleStatus() == LifecycleSyntheticStatus.TO_INACTIVATE) {
			filterActiveBusinessGroupLifecycleToSearch(sb, params.getLifecycleStatus(), params.getLifecycleStatusReference(), lifecycleParams);
		} else if(params.getLifecycleStatus() == LifecycleSyntheticStatus.INACTIVE
				|| params.getLifecycleStatus() == LifecycleSyntheticStatus.INACTIVE_LONG
				|| params.getLifecycleStatus() == LifecycleSyntheticStatus.INACTIVE_RESPONSE_DELAY
				|| params.getLifecycleStatus() == LifecycleSyntheticStatus.TO_START_SOFT_DELETE
				|| params.getLifecycleStatus() == LifecycleSyntheticStatus.TO_SOFT_DELETE) {
			filterInactiveBusinessGroupLifecycleToSearch(sb, params.getLifecycleStatus(), params.getLifecycleStatusReference(), lifecycleParams);
		} else if(params.getLifecycleStatus() == LifecycleSyntheticStatus.SOFT_DELETE
				|| params.getLifecycleStatus() == LifecycleSyntheticStatus.SOFT_DELETE_LONG
				|| params.getLifecycleStatus() == LifecycleSyntheticStatus.TO_DELETE) {
			filterSoftDeleteBusinessGroupLifecycleToSearch(sb, params.getLifecycleStatus(), params.getLifecycleStatusReference(), lifecycleParams);
		}
		
		if(params.getLastUsageBefore() != null) {
			sb.and().append(" bgi.lastUsage <= :lastUsageBefore");
		}
	}
	
	
	// active            status=active, lastusage<date-30, inactivationEmailDate is null
	// long not active   status=active, lastusage>date-30, inactivationEmailDate is null
	// response delay    status=active, inactivationEmailDate>date <date
	// to inactivate     status=active, inactivationEmailDate>date
	
	// inactive          status=inactive, inactivationDate<date-30, softDeleteEmailDate is null
	// long inactive     status=inactive, inactivationDate>date-30, softDeleteEmailDate is null
	// response delay    status=inactive, softDeleteEmailDate>date <date
	// to soft delete    status=inactive, softDeleteEmailDate>date
	
	// deleted           status=deleted, softDeleteDate>date <date
	// to delete         status=deleted, softDeleteDate>date <date
	
	private Date getStartActiveFocus(Date referenceDate) {
		int days = businessGroupModule.getNumberOfInactiveDayBeforeDeactivation();
		if(businessGroupModule.getNumberOfFocusDay() < days) {
			days -= businessGroupModule.getNumberOfFocusDay();
		}
		if(businessGroupModule.isMailBeforeDeactivation()) {
			days -= businessGroupModule.getNumberOfDayBeforeDeactivationMail();
		}
		return DateUtils.addDays(referenceDate, -days);
	}
	
	private void filterActiveBusinessGroupLifecycleToSearch(QueryBuilder sb, LifecycleSyntheticStatus status,
			Date referenceDate, LifecycleParams lifecycleParams) {
		
		sb.and().append("bgi.status").in(BusinessGroupStatusEnum.active);
		
		if(status == LifecycleSyntheticStatus.ACTIVE) {
			// show all active
		} else if(status == LifecycleSyntheticStatus.ACTIVE_LONG) {
			Date startPeriod = getStartActiveFocus(referenceDate);
			lifecycleParams.setLastUsagePeriodStart(startPeriod);
			sb.and().append(" bgi.lastUsage<=:lastUsagePeriodStart");
			
		} else if(status == LifecycleSyntheticStatus.ACTIVE_RESPONSE_DELAY) {
			sb.and().append(" bgi.inactivationEmailDate is not null");

		} else if(status == LifecycleSyntheticStatus.TO_START_INACTIVATE) {
			// manual with E-mail
			int inactivityDay = businessGroupModule.getNumberOfInactiveDayBeforeDeactivation();
			int mailDay = businessGroupModule.getNumberOfDayBeforeDeactivationMail();
			Date startPeriod = DateUtils.addDays(referenceDate, -(inactivityDay - mailDay));
			lifecycleParams.setLastUsagePeriodStart(startPeriod);
			
			sb.and().append(" bgi.lastUsage<:lastUsagePeriodStart")
			  .and().append(" bgi.inactivationEmailDate is null");
			
		} else if(status == LifecycleSyntheticStatus.TO_INACTIVATE) {
			// manual without E-mail (without reaction time)
			int inactivityDay = businessGroupModule.getNumberOfInactiveDayBeforeDeactivation();
			Date startPeriod = DateUtils.addDays(referenceDate, -inactivityDay);
			lifecycleParams.setLastUsagePeriodStart(startPeriod);
			
			sb.and().append(" bgi.lastUsage<:lastUsagePeriodStart");
		}
	}
	
	private Date getStartInactiveFocus(Date referenceDate) {
		int days = businessGroupModule.getNumberOfInactiveDayBeforeSoftDelete();
		if(businessGroupModule.getNumberOfFocusDay() < days) {
			days -= businessGroupModule.getNumberOfFocusDay();
		}
		if(businessGroupModule.isMailBeforeSoftDelete()) {
			days -= businessGroupModule.getNumberOfDayBeforeSoftDeleteMail();
		}
		return DateUtils.addDays(referenceDate, -days);
	}
	
	private void filterInactiveBusinessGroupLifecycleToSearch(QueryBuilder sb, LifecycleSyntheticStatus status,
			Date referenceDate, LifecycleParams lifecycleParams) {
		
		sb.and().append("bgi.status").in(BusinessGroupStatusEnum.inactive);
		
		if(status == LifecycleSyntheticStatus.INACTIVE) {
			// show all inactivae
		} else if(status == LifecycleSyntheticStatus.INACTIVE_LONG) {
			Date startPeriod = getStartInactiveFocus(referenceDate);
			lifecycleParams.setInactivationPeriodStart(startPeriod);
			sb.and().append(" bgi.inactivationDate<=:inactivationPeriodStart");
			
		} else if(status == LifecycleSyntheticStatus.INACTIVE_RESPONSE_DELAY) {

			sb.and().append(" bgi.softDeleteEmailDate is not null");

		} else if(status == LifecycleSyntheticStatus.TO_START_SOFT_DELETE) {
			// manual with or without E-m
			int inactivityDay = businessGroupModule.getNumberOfInactiveDayBeforeSoftDelete();
			int startDay = businessGroupModule.getNumberOfDayBeforeSoftDeleteMail();
			Date startPeriod = DateUtils.addDays(referenceDate, -(inactivityDay - startDay));
			lifecycleParams.setInactivationPeriodStart(startPeriod);

			sb.and().append(" bgi.inactivationDate<:inactivationPeriodStart")
			  .and().append(" bgi.softDeleteEmailDate is null");
			
		} else if(status == LifecycleSyntheticStatus.TO_SOFT_DELETE) {
			// with E-mail
			int startDay = businessGroupModule.getNumberOfDayBeforeSoftDeleteMail();
			Date startPeriod = DateUtils.addDays(referenceDate, -startDay);
			lifecycleParams.setInactivationPeriodStart(startPeriod);
			
			sb.and().append(" bgi.softDeleteEmailDate<:inactivationPeriodStart");
		}
	}
	
	private Date getStartSoftDeleteFocus(Date referenceDate) {
		int days = businessGroupModule.getNumberOfSoftDeleteDayBeforeDefinitivelyDelete();
		if(businessGroupModule.getNumberOfFocusDay() < days) {
			days -= businessGroupModule.getNumberOfFocusDay();
		}
		return DateUtils.addDays(referenceDate, -days);
	}
	
	private void filterSoftDeleteBusinessGroupLifecycleToSearch(QueryBuilder sb, LifecycleSyntheticStatus status,
			Date referenceDate, LifecycleParams lifecycleParams) {

		sb.and().append("bgi.status").in(BusinessGroupStatusEnum.trash);
		
		if(status == LifecycleSyntheticStatus.SOFT_DELETE) {
			// all in trash
		} else if(status == LifecycleSyntheticStatus.SOFT_DELETE_LONG || status == LifecycleSyntheticStatus.TO_DELETE) {
			Date startPeriod = getStartSoftDeleteFocus(referenceDate);
			lifecycleParams.setSoftDeletePeriodStart(startPeriod);
			
			sb.and().append(" bgi.softDeleteDate<=:softDeletePeriodStart");
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
		LifecycleParams lifecycleParams = new LifecycleParams();
		
		//resources
		QueryBuilder sr = new QueryBuilder();
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
			filterBusinessGroupToSearch(sr, params, lifecycleParams, false);
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
			filterBusinessGroupToSearchParameters(resourcesQuery, params, lifecycleParams, identity, false);
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
	
	private void appendMarkedSubQuery(QueryBuilder sb, BusinessGroupQueryParams params) {
		if(params.isMarked()) {
			sb.append(" ,1 as marks");
		} else {
			sb.append(" ,(select count(mark.key) from ").append(MarkImpl.class.getName()).append(" as mark ")
			  .append("   where mark.creator.key=:identityKey and mark.resId=bgi.key and mark.resName='BusinessGroup'")
			  .append(" ) as marks");
		}
	}
	
	private QueryBuilder searchLikeOwnerUserProperty(QueryBuilder sb, String key, String parameter) {
		if(dbInstance.getDbVendor().equals("mysql")) {
			sb.append(" ownerUser.").append(key).append(" like :").append(parameter);
		} else {
			sb.append(" lower(ownerUser.").append(key).append(") like :").append(parameter);
			if(dbInstance.getDbVendor().equals("oracle")) {
	 	 		sb.append(" escape '\\'");
	 	 	}
		}
		return sb;
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
	
	private static class LifecycleParams {
		
		private Date lastUsagePeriodStart;
		private Date inactivationPeriodStart;
		private Date softDeletePeriodStart;
		
		public Date getLastUsagePeriodStart() {
			return lastUsagePeriodStart;
		}
		
		public void setLastUsagePeriodStart(Date lastUsagePeriodStart) {
			this.lastUsagePeriodStart = lastUsagePeriodStart;
		}

		public Date getInactivationPeriodStart() {
			return inactivationPeriodStart;
		}

		public void setInactivationPeriodStart(Date inactivationPeriodStart) {
			this.inactivationPeriodStart = inactivationPeriodStart;
		}

		public Date getSoftDeletePeriodStart() {
			return softDeletePeriodStart;
		}

		public void setSoftDeletePeriodStart(Date softDeletePeriodStart) {
			this.softDeletePeriodStart = softDeletePeriodStart;
		}
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
