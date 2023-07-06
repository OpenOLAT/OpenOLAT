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

import org.olat.basesecurity.IdentityRef;
import org.olat.core.commons.persistence.DB;
import org.olat.core.commons.persistence.QueryBuilder;
import org.olat.modules.cemedia.Media;
import org.olat.modules.cemedia.MediaToTaxonomyLevel;
import org.olat.modules.cemedia.model.MediaToTaxonomyLevelImpl;
import org.olat.modules.taxonomy.TaxonomyLevel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 
 * Initial date: 31 mai 2023<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
@Service
public class MediaToTaxonomyLevelDAO {
	
	@Autowired
	private DB dbInstance;
	
	public MediaToTaxonomyLevel createRelation(Media media, TaxonomyLevel taxonomyLevel) {
		MediaToTaxonomyLevelImpl rel = new MediaToTaxonomyLevelImpl();
		rel.setCreationDate(new Date());
		rel.setMedia(media);
		rel.setTaxonomyLevel(taxonomyLevel);
		dbInstance.getCurrentEntityManager().persist(rel);
		return rel;
	}
	
	public List<TaxonomyLevel> loadTaxonomyLevels(Media media) {
		QueryBuilder sb = new QueryBuilder();
		sb.append("select level from mediatotaxonomylevel rel")
		  .append(" inner join rel.taxonomyLevel as level")
		  .and().append(" rel.media.key=:mediaKey");
		
		return dbInstance.getCurrentEntityManager().createQuery(sb.toString(), TaxonomyLevel.class)
				.setParameter("mediaKey", media.getKey())
				.getResultList();
	}
	
	public List<MediaToTaxonomyLevel> loadRelations(Media media) {
		QueryBuilder sb = new QueryBuilder();
		sb.append("select rel from mediatotaxonomylevel rel")
		  .append(" inner join fetch rel.taxonomyLevel as level")
		  .and().append(" rel.media.key=:mediaKey");
		
		return dbInstance.getCurrentEntityManager().createQuery(sb.toString(), MediaToTaxonomyLevel.class)
				.setParameter("mediaKey", media.getKey())
				.getResultList();
	}
	
	public List<MediaToTaxonomyLevel> loadRelations(IdentityRef author) {
		StringBuilder sb = new StringBuilder();
		sb.append("select rel from mediatotaxonomylevel rel")
		  .append(" inner join fetch rel.taxonomyLevel level")
		  .append(" left join fetch level.parent as parent")
		  .append(" left join fetch level.type as type")
		  .append(" inner join fetch level.taxonomy as taxonomy")
		  .append(" inner join fetch rel.media as media")
		  .append(" where media.author.key=:authorKey");
		
		return dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), MediaToTaxonomyLevel.class)
				.setParameter("authorKey", author.getKey())
				.getResultList();
	}
	
	
	public int deleteRelation(Media media) {
		String query = "delete from mediatotaxonomylevel rel where rel.media.key=:mediaKey";
		return dbInstance.getCurrentEntityManager()
			.createQuery(query)
			.setParameter("mediaKey", media.getKey())
			.executeUpdate();
	}
	
	public void deleteRelation(MediaToTaxonomyLevel relation) {
		dbInstance.getCurrentEntityManager().remove(relation);
	}
}
