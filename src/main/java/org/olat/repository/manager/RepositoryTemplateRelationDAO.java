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
package org.olat.repository.manager;

import java.util.Date;
import java.util.List;

import jakarta.persistence.EntityManager;

import org.olat.basesecurity.Group;
import org.olat.core.commons.persistence.DB;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryEntryRef;
import org.olat.repository.model.RepositoryTemplateToGroupRelation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 
 * Initial date: 13 févr. 2025<br>
 * @author srosse, stephane.rosse@frentix.com, https://www.frentix.com
 *
 */
@Service
public class RepositoryTemplateRelationDAO {
	
	@Autowired
	private DB dbInstance;
	
	public RepositoryTemplateToGroupRelation createRelation(Group group, RepositoryEntry re) {
		RepositoryTemplateToGroupRelation rel = new RepositoryTemplateToGroupRelation();
		rel.setCreationDate(new Date());
		rel.setGroup(group);
		rel.setEntry(re);
		dbInstance.getCurrentEntityManager().persist(rel);
		return rel;
	}
	
	public boolean hasRelation(Group group, RepositoryEntryRef template) {
		if(template == null || group == null) return false;
		
		String query = "select rel.key from repotemplatetogroup as rel where rel.entry.key=:templateKey and rel.group.key=:groupKey";

		List<Long> relations = dbInstance.getCurrentEntityManager()
			.createQuery(query, Long.class)
			.setParameter("templateKey", template.getKey())
			.setParameter("groupKey", group.getKey())
			.setFirstResult(0)
			.setMaxResults(1)
			.getResultList();
		return relations != null && !relations.isEmpty() && relations.get(0) != null;
	}
	
	public boolean hasRelations(RepositoryEntryRef template) {
		if(template == null || template.getKey() == null) return false;
		
		String query = "select rel.key from repotemplatetogroup as rel where rel.entry.key=:templateKey";

		List<Long> relations = dbInstance.getCurrentEntityManager()
			.createQuery(query, Long.class)
			.setParameter("templateKey", template.getKey())
			.setFirstResult(0)
			.setMaxResults(1)
			.getResultList();
		return relations != null && !relations.isEmpty() && relations.get(0) != null;
	}
	
	public int removeRelation(Group group, RepositoryEntryRef re) {
		EntityManager em = dbInstance.getCurrentEntityManager();
		List<RepositoryTemplateToGroupRelation> rels = em.createNamedQuery("relationByRepositoryTemplateAndGroup", RepositoryTemplateToGroupRelation.class)
			.setParameter("repoKey", re.getKey())
			.setParameter("groupKey", group.getKey())
			.getResultList();

		for(RepositoryTemplateToGroupRelation rel:rels) {
			em.remove(rel);
		}
		return rels.size();
	}
}
