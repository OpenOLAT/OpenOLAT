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
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;

import org.olat.basesecurity.SecurityGroupMembershipImpl;
import org.olat.core.commons.persistence.DB;
import org.olat.core.id.Identity;
import org.olat.group.BusinessGroup;
import org.olat.group.BusinessGroupImpl;
import org.olat.group.model.BGRepositoryEntryRelation;
import org.olat.group.model.BGResourceRelation;
import org.olat.repository.RepositoryEntry;
import org.olat.resource.OLATResource;
import org.olat.resource.OLATResourceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
@Service("businessGroupRelationDao")
public class BusinessGroupRelationDAO {

	@Autowired
	private DB dbInstance;
	
	public void addRelationToResource(BusinessGroup group, OLATResource resource) {
		BGResourceRelation relation = new BGResourceRelation();
		relation.setGroup(group);
		relation.setResource((OLATResourceImpl)resource);
		dbInstance.getCurrentEntityManager().persist(relation);
	}
	
	public void deleteRelation(BusinessGroup group, OLATResource resource) {
		StringBuilder sb = new StringBuilder();
		sb.append("select rel from ").append(BGResourceRelation.class.getName()).append(" as rel ")
			.append(" where rel.group.key=:groupKey and rel.resource.key=:resourceKey");

		EntityManager em = dbInstance.getCurrentEntityManager();
		List<BGResourceRelation> relations = em.createQuery(sb.toString(), BGResourceRelation.class)
				.setParameter("groupKey", group.getKey())
				.setParameter("resourceKey", resource.getKey())
				.getResultList();
		
		for(BGResourceRelation relation:relations) {
			em.remove(relation);
		}
	}
	
	public void deleteRelations(BusinessGroup group) {
		StringBuilder sb = new StringBuilder();
		sb.append("select rel from ").append(BGResourceRelation.class.getName()).append(" as rel ")
			.append(" where rel.group.key=:groupKey");

		EntityManager em = dbInstance.getCurrentEntityManager();
		List<BGResourceRelation> relations = em.createQuery(sb.toString(), BGResourceRelation.class)
				.setParameter("groupKey", group.getKey())
				.getResultList();
		
		for(BGResourceRelation relation:relations) {
			em.remove(relation);
		}
	}

	public boolean isIdentityInBusinessGroup(Identity identity, Long groupKey, OLATResource resource) {
		StringBuilder sb = new StringBuilder();
		sb.append("select count(bgi) from ").append(BusinessGroupImpl.class.getName()).append(" bgi");
		boolean and = false;
		if(groupKey != null) {
			and = and(sb, and);
			sb.append(" bgi.key=:groupKey");
		}
		and(sb, and);
		sb.append(" (")
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
			.append(" )");

		TypedQuery<Number> query = dbInstance.getCurrentEntityManager().createQuery(sb.toString(), Number.class)
				.setParameter("identityKey", identity.getKey())
				.setParameter("resourceKey", resource.getKey());
		if(groupKey != null) {
			query.setParameter("groupKey", groupKey);
		}
		query.setHint("org.hibernate.cacheable", Boolean.TRUE);
		Number count = query.getSingleResult();
		return count.intValue() > 0;
	}

	
	public int countMembersOf(OLATResource resource, boolean owner, boolean attendee) {
		if(!owner && !attendee) return 0;
		Number count = createMembersDBQuery(resource, owner, attendee, Number.class)
				.setHint("org.hibernate.cacheable", Boolean.TRUE)
				.getSingleResult();
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
			sb.append("select distinct identity from ").append(SecurityGroupMembershipImpl.class.getName()).append(" as sgmi ");
		} else {
			sb.append("select count(distinct identity) from ").append(SecurityGroupMembershipImpl.class.getName()).append(" as sgmi ");
		}
		sb.append(" inner join sgmi.identity as identity ")
		  .append(" inner join sgmi.securityGroup as secGroup ")
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
			sb.append("order by identity.name");
		}

		TypedQuery<T> db = dbInstance.getCurrentEntityManager().createQuery(sb.toString(), resultClass);
		db.setParameter("resourceKey", resource.getKey());
		return db;
	}
	
	public int countResources(BusinessGroup group) {
		if(group == null) return 0;
		StringBuilder sb = new StringBuilder();
		sb.append("select count(bgcr) from ").append(BGResourceRelation.class.getName()).append(" bgcr where bgcr.group.key=:groupKey");
		
		Number count = dbInstance.getCurrentEntityManager().createQuery(sb.toString(), Number.class)
				.setParameter("groupKey", group.getKey())
				.setHint("org.hibernate.cacheable", Boolean.TRUE)
				.getSingleResult();
		return count.intValue();
	}

	public List<OLATResource> findResources(Collection<BusinessGroup> groups, int firstResult, int maxResults) {
		if(groups == null || groups.isEmpty()) {
			return Collections.emptyList();
		}

		StringBuilder sb = new StringBuilder();
		sb.append("select distinct bgcr.resource from ").append(BGResourceRelation.class.getName()).append(" bgcr where bgcr.group.key in (:groupKeys)");
		
		TypedQuery<OLATResource> query = dbInstance.getCurrentEntityManager().createQuery(sb.toString(), OLATResource.class);
		query.setFirstResult(firstResult);
		if(maxResults > 0) {
			query.setMaxResults(maxResults);
		}
		
		List<Long> groupKeys = new ArrayList<Long>();
		for(BusinessGroup group:groups) {
			groupKeys.add(group.getKey());
		}
		query.setParameter("groupKeys", groupKeys);
		return query.getResultList();
	}
	
	public List<RepositoryEntry> findRepositoryEntries(Collection<BusinessGroup> groups, int firstResult, int maxResults) {
		if(groups == null || groups.isEmpty()) {
			return Collections.emptyList();
		}

		StringBuilder sb = new StringBuilder();
		sb.append("select v from ").append(RepositoryEntry.class.getName()).append(" as v ")
			.append(" inner join fetch v.olatResource as ores ")
			.append(" left join fetch v.ownerGroup as ownerGroup ")
			.append(" left join fetch v.tutorGroup as tutorGroup ")
			.append(" left join fetch v.participantGroup as participantGroup ")
			.append(" where ores in (")
			.append("  select bgcr.resource from ").append(BGResourceRelation.class.getName()).append(" as bgcr where bgcr.group.key in (:groupKeys)")
			.append(" )");

		TypedQuery<RepositoryEntry> query = dbInstance.getCurrentEntityManager().createQuery(sb.toString(), RepositoryEntry.class);
		query.setFirstResult(firstResult);
		if(maxResults > 0) {
			query.setMaxResults(maxResults);
		}
		
		List<Long> groupKeys = new ArrayList<Long>();
		for(BusinessGroup group:groups) {
			groupKeys.add(group.getKey());
		}
		query.setParameter("groupKeys", groupKeys);
		return query.getResultList();
	}
	
	public List<BGRepositoryEntryRelation> findRelationToRepositoryEntries(Collection<Long> groupKeys, int firstResult, int maxResults) {
		if(groupKeys == null || groupKeys.isEmpty()) {
			return Collections.emptyList();
		}

		StringBuilder sb = new StringBuilder();
		sb.append("select rel from ").append(BGRepositoryEntryRelation.class.getName()).append(" as rel ")
			.append(" where rel.groupKey in (:groupKeys)");

		TypedQuery<BGRepositoryEntryRelation> query = dbInstance.getCurrentEntityManager().createQuery(sb.toString(), BGRepositoryEntryRelation.class);
		query.setFirstResult(firstResult);
		if(maxResults > 0) {
			query.setMaxResults(maxResults);
		}
		
		query.setParameter("groupKeys", groupKeys);
		return query.getResultList();
	}
	
	private boolean and(StringBuilder sb, boolean and) {
		if(and) sb.append(" and ");
		else sb.append(" where ");
		return true;
	}
}
