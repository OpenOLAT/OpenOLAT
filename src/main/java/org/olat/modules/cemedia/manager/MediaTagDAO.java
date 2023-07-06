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

import org.olat.basesecurity.IdentityRef;
import org.olat.core.commons.persistence.DB;
import org.olat.core.commons.persistence.QueryBuilder;
import org.olat.core.commons.services.tag.Tag;
import org.olat.core.commons.services.tag.TagInfo;
import org.olat.modules.cemedia.Media;
import org.olat.modules.cemedia.MediaTag;
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
	
	public List<TagInfo> loadMediaTagInfos(Media media, IdentityRef author) {
		QueryBuilder sb = new QueryBuilder();
		sb.append("select new org.olat.core.commons.services.tag.model.TagInfoImpl(");
		sb.append("       tag.key");
		sb.append("     , min(tag.creationDate)");
		sb.append("     , min(tag.displayName)");
		sb.append("     , count(media.key)");
		sb.append("     , cast(0 as long) as selected");
		sb.append(")");
		sb.append(" from mediatag mTag");
		sb.append(" inner join mTag.tag tag")
		  .append(" inner join mTag.media as media");
		
		if(media != null) {
			sb.and().append("media.key=:mediaKey");
		}
		if(author != null) {
			sb.and().append("media.author.key=:authorKey");
		}
		sb.groupBy().append("tag.key");
		
		TypedQuery<TagInfo> query = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), TagInfo.class);
		if(media != null) {
			query.setParameter("mediaKey", media.getKey());
		}
		if(author != null) {
			query.setParameter("authorKey", author.getKey());
		}
		return query.getResultList();
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
	
	public List<MediaTag> loadMediaTags(IdentityRef author) {
		StringBuilder sb = new StringBuilder();
		sb.append("select mTag from mediatag as mTag")
		  .append(" inner join fetch mTag.tag tag")
		  .append(" inner join fetch mTag.media as media")
		  .append(" where media.author.key=:authorKey");
		
		return dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), MediaTag.class)
				.setParameter("authorKey", author.getKey())
				.getResultList();
	}
	
	public int deleteRelation(Media media) {
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
