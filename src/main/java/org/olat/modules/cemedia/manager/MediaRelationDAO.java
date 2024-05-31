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
package org.olat.modules.cemedia.manager;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.olat.basesecurity.Group;
import org.olat.core.commons.persistence.DB;
import org.olat.core.commons.persistence.QueryBuilder;
import org.olat.core.id.Identity;
import org.olat.core.id.Organisation;
import org.olat.group.BusinessGroup;
import org.olat.modules.cemedia.Media;
import org.olat.modules.cemedia.MediaToGroupRelation;
import org.olat.modules.cemedia.MediaToGroupRelation.MediaToGroupRelationType;
import org.olat.modules.cemedia.model.MediaShare;
import org.olat.modules.cemedia.model.MediaToGroupRelationImpl;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryEntryRef;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 
 * Initial date: 6 juin 2023<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
@Service
public class MediaRelationDAO {
	
	@Autowired
	private DB dbInstance;
	
	public MediaToGroupRelation createRelation(MediaToGroupRelationType type, boolean editable, Media media, Group group, RepositoryEntry entry) {
		MediaToGroupRelationImpl rel = new MediaToGroupRelationImpl();
		rel.setCreationDate(new Date());
		rel.setLastModified(rel.getCreationDate());
		rel.setEditable(editable);
		rel.setType(type);
		rel.setMedia(media);
		rel.setGroup(group);
		rel.setRepositoryEntry(entry);
		dbInstance.getCurrentEntityManager().persist(rel);
		return rel;
	}
	
	public MediaToGroupRelation updateRelation(MediaToGroupRelation relation) {
		((MediaToGroupRelationImpl)relation).setLastModified(new Date());
		return dbInstance.getCurrentEntityManager().merge(relation);
	}
	
	public MediaToGroupRelation getRelation(Media media, MediaToGroupRelationType type, boolean editable) {
		QueryBuilder sb = new QueryBuilder();
		sb.append("select rel from mediatogroup rel")
		  .append(" where rel.type=:type and rel.editable=:editable and rel.media.key=:mediaKey");
		
		List<MediaToGroupRelation> relations = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), MediaToGroupRelation.class)
				.setParameter("type", type)
				.setParameter("editable", Boolean.valueOf(editable))
				.setParameter("mediaKey", media.getKey())
				.getResultList();
		return relations == null || relations.isEmpty() ? null : relations.get(0);
	}
	
	public MediaToGroupRelation getRelation(Media media, MediaToGroupRelationType type, boolean editable, Group group) {
		QueryBuilder sb = new QueryBuilder();
		sb.append("select rel from mediatogroup rel")
		  .append(" where rel.media.key=:mediaKey and rel.group.key=:groupKey")
		  .append("  and rel.type=:type and rel.editable=:editable");
		
		List<MediaToGroupRelation> relations = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), MediaToGroupRelation.class)
				.setParameter("type", type)
				.setParameter("editable", Boolean.valueOf(editable))
				.setParameter("mediaKey", media.getKey())
				.setParameter("groupKey", group.getKey())
				.getResultList();
		return relations == null || relations.isEmpty() ? null : relations.get(0);
	}
	
	public List<MediaToGroupRelation> getRelations(Media media, MediaToGroupRelationType type, Group group) {
		QueryBuilder sb = new QueryBuilder();
		sb.append("select rel from mediatogroup rel")
		  .append(" where rel.media.key=:mediaKey and rel.group.key=:groupKey and rel.type=:type");
		
		return dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), MediaToGroupRelation.class)
				.setParameter("type", type)
				.setParameter("mediaKey", media.getKey())
				.setParameter("groupKey", group.getKey())
				.getResultList();
	}
	
	public List<MediaToGroupRelation> getRelations(Media media, MediaToGroupRelationType type, RepositoryEntryRef entry) {
		QueryBuilder sb = new QueryBuilder();
		sb.append("select rel from mediatogroup rel")
		  .append(" where rel.media.key=:mediaKey and rel.repositoryEntry.key=:entryKey and rel.type=:type");
		
		return dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), MediaToGroupRelation.class)
				.setParameter("type", type)
				.setParameter("mediaKey", media.getKey())
				.setParameter("entryKey", entry.getKey())
				.getResultList();
	}
	
	public List<MediaShare> getUserRelations(Media media) {
		QueryBuilder sb = new QueryBuilder();
		sb.append("select rel, ident from mediatogroup rel")
		  .append(" inner join rel.group as bGroup")
		  .append(" inner join bGroup.members as members")
		  .append(" inner join members.identity as ident")
		  .append(" inner join fetch ident.user as iUser")
		  .append(" where rel.media.key=:mediaKey and rel.type=:type");
		
		List<Object[]> relations = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), Object[].class)
				.setParameter("type", MediaToGroupRelationType.USER)
				.setParameter("mediaKey", media.getKey())
				.getResultList();
		List<MediaShare> shares = new ArrayList<>(relations.size());
		for(Object[] rawRelation:relations) {
			MediaToGroupRelation relation = (MediaToGroupRelation)rawRelation[0];
			Identity user = (Identity)rawRelation[1];
			shares.add(new MediaShare(relation, user));
		}
		return shares;
	}
	
	public List<MediaShare> getBusinesGroupRelations(Media media) {
		QueryBuilder sb = new QueryBuilder();
		sb.append("select rel, bgr from mediatogroup rel")
		  .append(" inner join rel.group as bGroup")
		  .append(" inner join businessgroup as bgr on(bGroup.key=bgr.baseGroup.key)")
		  .append(" where rel.media.key=:mediaKey and rel.type=:type");
		
		List<Object[]> relations = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), Object[].class)
				.setParameter("type", MediaToGroupRelationType.BUSINESS_GROUP)
				.setParameter("mediaKey", media.getKey())
				.getResultList();
		List<MediaShare> shares = new ArrayList<>(relations.size());
		for(Object[] rawRelation:relations) {
			MediaToGroupRelation relation = (MediaToGroupRelation)rawRelation[0];
			BusinessGroup businessGroup = (BusinessGroup)rawRelation[1];
			shares.add(new MediaShare(relation, businessGroup));
		}
		return shares;
	}
	
	public List<MediaShare> getOrganisationRelations(Media media) {
		QueryBuilder sb = new QueryBuilder();
		sb.append("select rel, org from mediatogroup rel")
		  .append(" inner join rel.group as bGroup")
		  .append(" inner join organisation as org on(bGroup.key=org.group.key)")
		  .append(" where rel.media.key=:mediaKey and rel.type=:type");
		
		List<Object[]> relations = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), Object[].class)
				.setParameter("type", MediaToGroupRelationType.ORGANISATION)
				.setParameter("mediaKey", media.getKey())
				.getResultList();
		List<MediaShare> shares = new ArrayList<>(relations.size());
		for(Object[] rawRelation:relations) {
			MediaToGroupRelation relation = (MediaToGroupRelation)rawRelation[0];
			Organisation organisation = (Organisation)rawRelation[1];
			shares.add(new MediaShare(relation, organisation));
		}
		return shares;
	}
	
	public List<MediaShare> getRepositoryEntryRelations(Media media) {
		QueryBuilder sb = new QueryBuilder();
		sb.append("select rel, v from mediatogroup rel")
		  .append(" inner join rel.repositoryEntry as v")
		  .append(" where rel.media.key=:mediaKey and rel.type=:type");
		
		List<Object[]> relations = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), Object[].class)
				.setParameter("type", MediaToGroupRelationType.REPOSITORY_ENTRY)
				.setParameter("mediaKey", media.getKey())
				.getResultList();
		List<MediaShare> shares = new ArrayList<>(relations.size());
		for(Object[] rawRelation:relations) {
			MediaToGroupRelation relation = (MediaToGroupRelation)rawRelation[0];
			RepositoryEntry repositoryEntry = (RepositoryEntry)rawRelation[1];
			shares.add(new MediaShare(relation, repositoryEntry));
		}
		return shares;
	}
	
	public int deleteRelation(Media media) {
		String query = "delete from mediatogroup rel where rel.media.key=:mediaKey";
		return dbInstance.getCurrentEntityManager()
			.createQuery(query)
			.setParameter("mediaKey", media.getKey())
			.executeUpdate();
	}
	
	public void deleteRelation(MediaToGroupRelation relation) {
		dbInstance.getCurrentEntityManager().remove(relation);
	}

	public int deleteRelations(RepositoryEntry repositoryEntry) {
		String query = "delete from mediatogroup relation where relation.repositoryEntry.key = :repositoryEntryKey";
		return dbInstance.getCurrentEntityManager()
				.createQuery(query)
				.setParameter("repositoryEntryKey", repositoryEntry.getKey())
				.executeUpdate();
	}
}
