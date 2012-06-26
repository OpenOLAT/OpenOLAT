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

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import javax.persistence.EntityManager;
import javax.persistence.LockModeType;
import javax.persistence.TypedQuery;

import org.olat.basesecurity.BaseSecurity;
import org.olat.basesecurity.Constants;
import org.olat.basesecurity.IdentityImpl;
import org.olat.basesecurity.SecurityGroup;
import org.olat.basesecurity.SecurityGroupMembershipImpl;
import org.olat.collaboration.CollaborationTools;
import org.olat.core.commons.persistence.DB;
import org.olat.core.id.Identity;
import org.olat.core.logging.OLog;
import org.olat.core.logging.Tracing;
import org.olat.core.util.StringHelper;
import org.olat.group.BusinessGroup;
import org.olat.group.BusinessGroupImpl;
import org.olat.group.model.BGResourceRelation;
import org.olat.group.model.SearchBusinessGroupParams;
import org.olat.group.properties.BusinessGroupPropertyManager;
import org.olat.properties.Property;
import org.olat.resource.OLATResource;
import org.olat.resource.OLATResourceImpl;
import org.olat.resource.OLATResourceManager;
import org.olat.user.UserImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
@Service("businessGroupDao")
public class BusinessGroupDAO {
	
	private OLog log = Tracing.createLoggerFor(BusinessGroupDAO.class);
	
	@Autowired
	private DB dbInstance;
	@Autowired
	private BaseSecurity securityManager;
	@Autowired
	private OLATResourceManager olatResourceManager;
	
	public BusinessGroup createAndPersist(Identity creator, String name, String description, String type,
			int minParticipants, int maxParticipants, boolean waitingListEnabled, boolean autoCloseRanksEnabled) {

		BusinessGroupImpl businessgroup = null;
		//security groups
		SecurityGroup ownerGroup = securityManager.createAndPersistSecurityGroup();
		SecurityGroup participantGroup = securityManager.createAndPersistSecurityGroup();
		SecurityGroup waitingGroup = securityManager.createAndPersistSecurityGroup();
		
		String realType = type == null ? BusinessGroup.TYPE_LEARNINGROUP : type;
		businessgroup = new BusinessGroupImpl(realType, name, description, ownerGroup, participantGroup, waitingGroup);
		businessgroup.setMinParticipants(minParticipants);
		businessgroup.setMaxParticipants(maxParticipants);
		businessgroup.setWaitingListEnabled(waitingListEnabled);
		businessgroup.setAutoCloseRanksEnabled(autoCloseRanksEnabled);
		
		EntityManager em = dbInstance.getCurrentEntityManager();
		em.persist(businessgroup);
		
		if(log.isDebug()){
			log.debug("created Buddy Group named " + name + " for Identity " + creator);
		}
		/*
		 * policies: - ownerGroup can do everything on this businessgroup -> is an
		 * admin, can invite people to owner.- & partipiciantgroup -
		 * partipiciantGroup can read this businessgroup
		 */
		OLATResource businessgroupOlatResource =  olatResourceManager.createOLATResourceInstance(businessgroup);
		olatResourceManager.saveOLATResource(businessgroupOlatResource);

		//		securityManager.createAndPersistPolicy(ownerGroup, Constants.PERMISSION_ACCESS, businessgroup);
		securityManager.createAndPersistPolicyWithResource(ownerGroup, Constants.PERMISSION_ACCESS, businessgroupOlatResource);
		securityManager.createAndPersistPolicyWithResource(participantGroup, Constants.PERMISSION_READ, businessgroupOlatResource);
		// membership: add identity
		if (creator != null) {
			securityManager.addIdentityToSecurityGroup(creator, ownerGroup);
		}

		// per default all collaboration-tools are disabled

		// group members visibility
		BusinessGroupPropertyManager bgpm = new BusinessGroupPropertyManager(businessgroup);
		if(BusinessGroup.TYPE_RIGHTGROUP.equals(type)) {
			bgpm.createAndPersistDisplayMembers(false, true, false);
		} else {
			bgpm.createAndPersistDisplayMembers(true, false, false);
		}
		
		return businessgroup;
	}
	
	public void addRelationToResource(BusinessGroup group, OLATResource resource) {
		BGResourceRelation relation = new BGResourceRelation();
		relation.setGroup(group);
		relation.setResource((OLATResourceImpl)resource);
		dbInstance.getCurrentEntityManager().persist(relation);
	}
	
	public BusinessGroup load(Long id) {
		EntityManager em = dbInstance.getCurrentEntityManager();
		BusinessGroup group = em.find(BusinessGroupImpl.class, id, LockModeType.NONE);
		return group;
	}
	
	public List<BusinessGroup> load(Collection<Long> ids) {
		if(ids == null || ids.isEmpty()) {
			return Collections.emptyList();
		}
		
		EntityManager em = dbInstance.getCurrentEntityManager();
		List<BusinessGroup> groups = em.createQuery("select grp from " + BusinessGroupImpl.class.getName() + " grp where grp.id in (:ids)", BusinessGroup.class)
				.getResultList();
		return groups;
	}
	
	public List<BusinessGroup> loadAll() {
		EntityManager em = dbInstance.getCurrentEntityManager();
		List<BusinessGroup> groups = em.createQuery("select grp from " + BusinessGroupImpl.class.getName() + " grp", BusinessGroup.class)
				.getResultList();
		return groups;
	}
	
	public BusinessGroup loadForUpdate(Long id) {
		EntityManager em = dbInstance.getCurrentEntityManager();
		BusinessGroup group = em.find(BusinessGroupImpl.class, id, LockModeType.PESSIMISTIC_WRITE);
		return group;
	}
	
	public BusinessGroup merge(BusinessGroup group) {
		EntityManager em = dbInstance.getCurrentEntityManager();
		BusinessGroup mergedGroup = em.merge(group);
		return mergedGroup;
	}
	
	public void delete(BusinessGroup group) {
		EntityManager em = dbInstance.getCurrentEntityManager();
		em.remove(group);
	}
	
	/**
	 * Work with the hibernate session
	 * @param group
	 * @return
	 */
	public BusinessGroup update(BusinessGroup group) {
		dbInstance.updateObject(group);
		return group;
	}
	
	public boolean isIdentityInBusinessGroup(Identity identity, String name, String groupType, OLATResource resource) {
		StringBuilder sb = new StringBuilder();
		sb.append("select count(bgi) from ").append(BusinessGroupImpl.class.getName()).append(" bgi where")
		  .append(" bgi.name = :name")
		  .append(" and (")
		  .append("   bgi.partipiciantGroup in (")
		  .append("     select participantMemberShip.securityGroup from ").append(SecurityGroupMembershipImpl.class.getName()).append(" participantMemberShip ")
		  .append("       where participantMemberShip.identity.key=:identityKey")
		  .append("   )")
		  .append("   or")
		  .append("   bgi.ownerGroup in (")
		  .append("     select ownerMemberShip.securityGroup from ").append(SecurityGroupMembershipImpl.class.getName()).append(" ownerMemberShip ")
		  .append("       where ownerMemberShip.identity.key=:identityKey")
		  .append("   )")
		  .append(" )")
			.append(" and bgi in (")
			.append("   select relation.group from ").append(BGResourceRelation.class.getName()).append(" relation where relation.resource.key=:resourceKey")
			.append(" )")
			.append(" and bgi.type=:type");

		System.out.println(sb.toString());
		Number count = dbInstance.getCurrentEntityManager().createQuery(sb.toString(), Number.class)
				.setParameter("identityKey", identity.getKey())
				.setParameter("name", name)
				.setParameter("resourceKey", resource.getKey())
				.setParameter("type", groupType)
				.getSingleResult();
		return count.intValue() > 0;
	}
	
	public boolean checkIfOneOrMoreNameExistsInContext(Set<String> names, OLATResource resource) {
		StringBuilder sb = new StringBuilder();
		sb.append("select count(bgs) from ").append(BGResourceRelation.class.getName()).append(" as rel ")
			.append(" inner join rel.group bgs ")
		  .append(" where rel.resource.key=:resourceKey and bgs.name in (:names)");
		
		Number count = dbInstance.getCurrentEntityManager().createQuery(sb.toString(), Number.class)
				.setParameter("resourceKey", resource.getKey())
				.setParameter("names", names)
				.getSingleResult();
		return count.intValue() > 0;
	}
	
	public BusinessGroup findBusinessGroup(SecurityGroup secGroup) {
		StringBuilder sb = new StringBuilder(); 
		sb.append("select bgi from ").append(BusinessGroupImpl.class.getName()).append(" as bgi where ")
			.append("(bgi.partipiciantGroup=:secGroup or bgi.ownerGroup=:secGroup or bgi.waitingGroup=:secGroup)");

		List<BusinessGroup> res = dbInstance.getCurrentEntityManager().createQuery(sb.toString(), BusinessGroup.class)
				.setParameter("secGroup", secGroup)
				.getResultList();

		if(res.isEmpty()) return null;
		return res.get(0);
	}
	
	public List<BusinessGroup> findBusinessGroupsWithWaitingListAttendedBy(Identity identity,  OLATResource resource) {
		StringBuilder sb = new StringBuilder();
		if(resource == null) {
			sb.append("select bgs from ").append(BusinessGroupImpl.class.getName()).append(" as bgs ")
		    .append(" inner join bgs.waitingGroup waitingList ")
		    .append(" where ");
		} else {
			sb.append("select bgs from ").append(BGResourceRelation.class.getName()).append(" as rel ")
			  .append(" inner join rel.group bgs ")
			  .append(" inner join bgs.waitingGroup waitingList ")
		    .append(" where rel.resource.key=:resourceKey and ");
		}
		sb.append(" waitingList in (select memberShip.securityGroup from ").append(SecurityGroupMembershipImpl.class.getName()).append(" memberShip ")
      .append("   where memberShip.identity.key=:identityKey ")
      .append(" )");
		
		System.out.println(sb.toString());
		
		TypedQuery<BusinessGroup> query = dbInstance.getCurrentEntityManager().createQuery(sb.toString(), BusinessGroup.class)
				.setParameter("identityKey", identity.getKey());
		if(resource != null) {
			query.setParameter("resourceKey", resource.getKey());
		}
		List<BusinessGroup> groups = query.getResultList();
		return groups;
	}
	
	public int countMembersOf(OLATResource resource, boolean owner, boolean attendee) {
		if(!owner && !attendee) return 0;
		TypedQuery<Number> query = createMembersDBQuery(resource, owner, attendee, Number.class);
		Number count = query.getSingleResult();
		return count.intValue();
	}

	public List<Identity> getMembersOf(OLATResource resource, boolean owner, boolean attendee) {
		if(!owner && !attendee) return Collections.emptyList();
		TypedQuery<Identity> query = createMembersDBQuery(resource, owner, attendee, Identity.class);
		List<Identity> members = query.getResultList();
		return members;
	}
	
	private <T> TypedQuery<T> createMembersDBQuery(OLATResource resource, boolean owner, boolean attendee, Class<T> resultClass) {
		StringBuilder sb = new StringBuilder();
		if(Identity.class.equals(resultClass)) {
			sb.append("select distinct sgmi.identity from ").append(SecurityGroupMembershipImpl.class.getName()).append(" as sgmi ");
		} else {
			sb.append("select count(distinct sgmi.identity) from ").append(SecurityGroupMembershipImpl.class.getName()).append(" as sgmi ");
		}
		sb.append(" inner join sgmi.securityGroup as secGroup ")
		  .append(" where ");
		
		if(owner) {
			sb.append("  secGroup in (")
		    .append("    select rel1.group.ownerGroup from ").append(BGResourceRelation.class.getName()).append(" as rel1")
		    .append("      where rel1.resource.key=:resourceKey")
		    .append("  )");
		}
		if(attendee) {
			if(owner) sb.append(" or ");
			sb.append("  secGroup in (")
	      .append("    select rel2.group.partipiciantGroup from ").append(BGResourceRelation.class.getName()).append(" as rel2")
	      .append("      where rel2.resource.key=:resourceKey")
	      .append("  )");
		}  
		if(Identity.class.equals(resultClass)) {
			sb.append("order by sgmi.identity.name");
		}

		TypedQuery<T> db = dbInstance.getCurrentEntityManager().createQuery(sb.toString(), resultClass);
		db.setParameter("resourceKey", resource.getKey());
		return db;
	}
	
	public int countBusinessGroups(SearchBusinessGroupParams params, Identity identity,
			boolean ownedById, boolean attendedById, OLATResource resource) {
		TypedQuery<Number> query = createFindDBQuery(params, identity, ownedById, attendedById, resource, Number.class);

		Number count = query.getSingleResult();
		return count.intValue();
	}
	
	public List<BusinessGroup> findBusinessGroups(SearchBusinessGroupParams params, Identity identity,
			boolean ownedById, boolean attendedById, OLATResource resource, int firstResult, int maxResults) {
		TypedQuery<BusinessGroup> query = createFindDBQuery(params, identity, ownedById, attendedById, resource, BusinessGroup.class);
		query.setFirstResult(firstResult);
		if(maxResults > 0) {
			query.setMaxResults(maxResults);
		}
		List<BusinessGroup> groups = query.getResultList();
		return groups;
	}
	
	private <T> TypedQuery<T> createFindDBQuery(SearchBusinessGroupParams params, Identity identity,
			boolean ownedById, boolean attendedById, OLATResource resource, Class<T> resultClass) {
		StringBuilder query = new StringBuilder();
		if(BusinessGroup.class.equals(resultClass)) {
			query.append("select distinct(bgi) from ");
		} else {
			query.append("select count(bgi.key) from ");
		}
		query.append(org.olat.group.BusinessGroupImpl.class.getName()).append(" as bgi ");

		if(StringHelper.containsNonWhitespace(params.getOwner())) {
			//implicit joins
			query.append(", ").append(SecurityGroupMembershipImpl.class.getName()).append(" as sgmi ")
				   .append(", ").append(IdentityImpl.class.getName()).append(" identity")
				   .append(", ").append(UserImpl.class.getName()).append(" user ");
		}
		//inner joins
		if(BusinessGroup.class.equals(resultClass)) {
			query.append("left join fetch bgi.ownerGroup ownerGroup ");
			query.append("left join fetch bgi.partipiciantGroup participantGroup ");
			query.append("left join fetch bgi.waitingGroup waitingGroup ");
		}

		boolean where = false;
		if(StringHelper.containsNonWhitespace(params.getOwner())) {
			where = true;
			query.append(" where ownerGroup = sgmi.securityGroup")
				   .append(" and sgmi.identity = identity ")
				   .append(" and identity.user = user and ")
			//query the name in login, firstName and lastName
				   .append("(");
			searchLikeUserProperty(query, "firstName", "owner");
			query.append(" or ");
			searchLikeUserProperty(query, "lastName", "owner");
			query.append(" or ");
			searchLikeAttribute(query, "identity", "name", "owner");
			query.append(")");
		}
		
		if(params.getKey() != null) {
			where = where(query, where);
			query.append("bgi.key=:id");
		}
		
		if(resource != null) {
			where = where(query, where);
			query.append("bgi in (")
			     .append("  select relation.group from ").append(BGResourceRelation.class.getName()).append(" relation where relation.resource.key=:resourceKey")
			     .append(")");
		}
		
		if(params.getTypes() != null && !params.getTypes().isEmpty()) {
			where = where(query, where);
			query.append("bgi.type in (:types)");
		}
		
		if(ownedById || attendedById) {
			where = where(query, where);
			query.append('(');
			if(ownedById) {
				query.append("ownerGroup.key in (select ownerMemberShip.securityGroup.key from ").append(SecurityGroupMembershipImpl.class.getName()).append(" ownerMemberShip ")
				     .append(" where ownerMemberShip.identity.key=:identId ")
				     .append(")");
			}
			if(attendedById) {
				if(ownedById) query.append(" or ");
				query.append(" participantGroup.key in (select partMembership.securityGroup.key from ").append(SecurityGroupMembershipImpl.class.getName()).append(" as partMembership ")
				     .append("  where partMembership.identity.key=:identId")
				     .append(" )");
			}
			query.append(')');
		}
		
		if(StringHelper.containsNonWhitespace(params.getNameOrDesc())) {
			where = where(query, where);
			query.append("(");
			searchLikeAttribute(query, "bgi", "name", "search");
			query.append(" or ");
			searchLikeAttribute(query, "bgi", "description", "search");
			query.append(")");
		} else {
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
			query.append(" order by bgi.name,bgi.key");
		}
		
		TypedQuery<T> dbq = dbInstance.getCurrentEntityManager().createQuery(query.toString(), resultClass);
		//add parameters
		if(ownedById || attendedById) {
			dbq.setParameter("identId", identity.getKey().longValue());
		}
		if(params.getKey() != null) {
			dbq.setParameter("id", params.getKey());
		}
		
		if (resource != null) {
			dbq.setParameter("resourceKey", resource.getKey());
		}
		if (params.getTypes() != null && !params.getTypes().isEmpty()) {
			dbq.setParameter("types", params.getTypes());
		}
		if(params.getTools() != null && !params.getTools().isEmpty()) {
			dbq.setParameter("tools", params.getTools());
		}
		if(StringHelper.containsNonWhitespace(params.getOwner())) {
			dbq.setParameter("owner", params.getOwner());
		}
		if(StringHelper.containsNonWhitespace(params.getNameOrDesc())) {
			dbq.setParameter("search", params.getNameOrDesc());
		} else {
			if(StringHelper.containsNonWhitespace(params.getName())) {
				dbq.setParameter("name", params.getName());
			}
			if(StringHelper.containsNonWhitespace(params.getDescription())) {
				dbq.setParameter("description", params.getDescription());
			}
		}
		return dbq;
	}
	
	public int countContacts(Identity identity) {
		Number result = createContactsQuery(identity, Number.class).getSingleResult();
		int numOfContacts = result.intValue();
		if(numOfContacts > 0) {
			numOfContacts--;//always a contact of myself with this query
		}
		return numOfContacts;
	}

	public List<Identity> findContacts(Identity identity, int firstResult, int maxResults) {
		TypedQuery<Identity> query = createContactsQuery(identity, Identity.class);
		query.setFirstResult(firstResult);
		if(maxResults > 0) {
			query.setMaxResults(maxResults + 1);
		}
		List<Identity> contacts = query.getResultList();
		if(!contacts.remove(identity) && maxResults > 0 && contacts.size() > maxResults) {
			contacts.remove(contacts.size() - 1);
		}
		return contacts;
	}
	
	private <T> TypedQuery<T> createContactsQuery(Identity identity, Class<T> resultClass) {
		StringBuilder query = new StringBuilder();
		if(Identity.class.equals(resultClass)) {
			query.append("select distinct sgmi.identity from ").append(SecurityGroupMembershipImpl.class.getName()).append(" as sgmi ");
		} else {
			query.append("select count(distinct sgmi.identity) from ").append(SecurityGroupMembershipImpl.class.getName()).append(" as sgmi ");
		}
		query.append(" inner join sgmi.securityGroup as secGroup ")
		     .append(" where ")
		     .append("  secGroup in (")
		     .append("    select bg1.ownerGroup from ").append(BusinessGroupImpl.class.getName()).append(" as bg1,").append(Property.class.getName()).append(" as prop where prop.grp=bg1 and prop.name='displayMembers' and prop.longValue in (1,3,5,7)")
		     .append("      and bg1.ownerGroup in (select ownerSgmi.securityGroup from ").append(SecurityGroupMembershipImpl.class.getName()).append(" as ownerSgmi where ownerSgmi.identity.key=:identKey)")
		     .append("  ) or")
		     .append("  secGroup in (")
		     .append("    select bg3.ownerGroup from ").append(BusinessGroupImpl.class.getName()).append(" as bg3,").append(Property.class.getName()).append(" as prop where prop.grp=bg3 and prop.name='displayMembers' and prop.longValue in (1,3,5,7)")
		     .append("      and bg3.partipiciantGroup in (select partSgmi.securityGroup from ").append(SecurityGroupMembershipImpl.class.getName()).append(" as partSgmi where partSgmi.identity.key=:identKey)")
		     .append("  ) or")
		     .append("  secGroup in (")
		     .append("    select bg2.partipiciantGroup from ").append(BusinessGroupImpl.class.getName()).append(" as bg2,").append(Property.class.getName()).append(" as prop where prop.grp=bg2 and prop.name='displayMembers' and prop.longValue in (2,3,6,7)")
		     .append("      and bg2.partipiciantGroup in (select partSgmi.securityGroup from ").append(SecurityGroupMembershipImpl.class.getName()).append(" as partSgmi where partSgmi.identity.key=:identKey)")
		     .append("  ) or")
		     .append("  secGroup in (")
		     .append("    select bg4.partipiciantGroup from ").append(BusinessGroupImpl.class.getName()).append(" as bg4,").append(Property.class.getName()).append(" as prop where prop.grp=bg4 and prop.name='displayMembers' and prop.longValue in (2,3,6,7)")
		     .append("      and bg4.ownerGroup in (select ownerSgmi.securityGroup from ").append(SecurityGroupMembershipImpl.class.getName()).append(" as ownerSgmi where ownerSgmi.identity.key=:identKey)")
		     .append("  )");
		if(Identity.class.equals(resultClass)) {
			query.append("order by sgmi.identity.name");
		}

		TypedQuery<T> db = dbInstance.getCurrentEntityManager().createQuery(query.toString(), resultClass);
		db.setParameter("identKey", identity.getKey());
		return db;
	}
	
	private StringBuilder searchLikeUserProperty(StringBuilder sb, String key, String var) {
		if(dbInstance.getDbVendor().equals("mysql")) {
			sb.append(" user.properties['").append(key).append("'] like :").append(var);
		} else {
			sb.append(" lower(user.properties['").append(key).append("']) like :").append(var);
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
	
	private boolean where(StringBuilder sb, boolean where) {
		if(where) {
			sb.append(" and ");
		} else {
			sb.append(" where ");
		}
		return true;
	}

}
