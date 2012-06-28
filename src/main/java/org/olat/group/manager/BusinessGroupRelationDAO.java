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

import org.olat.core.commons.persistence.DB;
import org.olat.group.BusinessGroup;
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

	public List<OLATResource> findResources(Collection<BusinessGroup> groups, int firstResult, int maxResults) {
		if(groups == null || groups.isEmpty()) {
			return Collections.emptyList();
		}

		StringBuilder sb = new StringBuilder();
		sb.append("select bgcr.resource from ").append(BGResourceRelation.class.getName()).append(" bgcr where bgcr.group.key in (:groupKeys)");
		
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
}
