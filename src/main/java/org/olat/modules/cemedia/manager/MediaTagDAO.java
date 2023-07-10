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

import java.util.Date;
import java.util.List;

import jakarta.persistence.TypedQuery;

import org.olat.basesecurity.GroupRoles;
import org.olat.basesecurity.IdentityRef;
import org.olat.basesecurity.OrganisationRoles;
import org.olat.core.commons.persistence.DB;
import org.olat.core.commons.persistence.QueryBuilder;
import org.olat.core.commons.services.tag.Tag;
import org.olat.core.commons.services.tag.TagInfo;
import org.olat.modules.cemedia.Media;
import org.olat.modules.cemedia.MediaTag;
import org.olat.modules.cemedia.MediaToGroupRelation.MediaToGroupRelationType;
import org.olat.modules.cemedia.model.MediaTagImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 
 * Initial date: 30 mai 2023<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
@Service
public class MediaTagDAO {
	
	
	@Autowired
	private DB dbInstance;
	
	public MediaTag create(Media media, Tag tag) {
		MediaTagImpl mediaTag = new MediaTagImpl();
		mediaTag.setCreationDate(new Date());
		mediaTag.setMedia(media);
		mediaTag.setTag(tag);
		dbInstance.getCurrentEntityManager().persist(mediaTag);
		return mediaTag;
	}
	
	public List<TagInfo> loadMediaTagInfos(Media media, IdentityRef identity) {
		QueryBuilder sb = new QueryBuilder();
		sb.append("select new org.olat.core.commons.services.tag.model.TagInfoImpl(")
		  .append("    tag.key")
		  .append("  , min(tag.creationDate)")
		  .append("  , min(tag.displayName)")
		  .append("  , count(media.key)");
		if(media != null) {
			sb.append("   , sum(case when (mTag.media.key=:mediaKey) then 1 else 0 end) as selected");
		} else {
			sb.append("   , cast(0 as long) as selected");
		} 
		sb.append(")")
		  .append("from mediatag mTag")
		  .append(" inner join mTag.tag tag")
		  .append(" inner join mTag.media as media")
		  .append(" where media.author.key=:identityKey")
		  .append(" or exists (select shareRel.key from mediatogroup as shareRel")
		  .append("  inner join shareRel.group as uGroup")
		  .append("  inner join uGroup.members as uMember")
		  .append("  where shareRel.media.key=media.key and uMember.identity.key=:identityKey")
		  .append("  and (shareRel.type").in(MediaToGroupRelationType.USER)
		  .append("   or shareRel.type").in(MediaToGroupRelationType.BUSINESS_GROUP)
		  .append("   or (shareRel.type").in(MediaToGroupRelationType.ORGANISATION).append(" and uMember.role").in(OrganisationRoles.author, OrganisationRoles.learnresourcemanager, OrganisationRoles.administrator).append(")")
		  .append(" ))")
		  .append(" or exists (select shareReRel.key from mediatogroup as shareReRel")
		  .append("  inner join shareReRel.repositoryEntry as v")
		  .append("  inner join v.groups as relGroup")
		  .append("  inner join relGroup.group as vBaseGroup")
		  .append("  inner join vBaseGroup.members as vMembership")
		  .append("  where shareReRel.media.key=media.key and shareReRel.type").in(MediaToGroupRelationType.REPOSITORY_ENTRY)
		  .append("    and vMembership.identity.key=:identityKey and vMembership.role").in(GroupRoles.owner, OrganisationRoles.learnresourcemanager, OrganisationRoles.administrator)
		  .append(" )")
		  .append(" group by tag.key");
		
		TypedQuery<TagInfo> query = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), TagInfo.class)
				.setParameter("identityKey", identity.getKey());
		if(media != null) {
			query.setParameter("mediaKey", media.getKey());
		}
		return query.getResultList();
	}
	
	public List<TagInfo> loadSelectedMediaTagInfos(Media media) {
		String query = """
				select new org.olat.core.commons.services.tag.model.TagInfoImpl(
				       tag.key
				     , min(tag.creationDate)
				     , min(tag.displayName)
				     , count(media.key)
				     , cast(1 as long) as selected
				)
				from mediatag mTag
				inner join mTag.tag tag
				inner join mTag.media as media
				where media.key=:mediaKey
				group by tag.key
				""";
		
		return dbInstance.getCurrentEntityManager()
				.createQuery(query, TagInfo.class)
				.setParameter("mediaKey", media.getKey())
				.getResultList();
	}

	public List<MediaTag> loadMediaTags(Media media) {
		QueryBuilder sb = new QueryBuilder();
		sb.append("select mTag from mediatag mTag")
		  .append(" inner join fetch mTag.tag tag")
		  .append(" where mTag.media.key=:mediaKey");

		return dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), MediaTag.class)
				.setParameter("mediaKey", media.getKey())
				.getResultList();
	}
	
	public List<MediaTag> loadMediaTags(IdentityRef author, List<Long> mediaKeys) {
		StringBuilder sb = new StringBuilder();
		sb.append("select mTag from mediatag as mTag")
		  .append(" inner join fetch mTag.tag tag")
		  .append(" inner join fetch mTag.media as media")
		  .append(" where media.author.key=:authorKey");
		if(mediaKeys != null && !mediaKeys.isEmpty()) {
			sb.append(" or media.key in (:mediaKeys)");
		}
		
		TypedQuery<MediaTag> query = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), MediaTag.class)
				.setParameter("authorKey", author.getKey());
		if(mediaKeys != null && !mediaKeys.isEmpty()) {
			query.setParameter("mediaKeys", mediaKeys);
		}
		return query.getResultList();
	}
	
	public int deleteRelationToTags(Media media) {
		String query = "delete from mediatag rel where rel.media.key=:mediaKey";
		return dbInstance.getCurrentEntityManager()
			.createQuery(query)
			.setParameter("mediaKey", media.getKey())
			.executeUpdate();
	}
	
	public void delete(MediaTag mediaTag) {
		dbInstance.getCurrentEntityManager().remove(mediaTag);
	}

}
