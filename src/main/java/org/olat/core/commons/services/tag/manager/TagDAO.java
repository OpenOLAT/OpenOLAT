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
package org.olat.core.commons.services.tag.manager;

import java.util.Collection;
import java.util.Date;
import java.util.List;

import org.olat.core.commons.persistence.DB;
import org.olat.core.commons.persistence.QueryBuilder;
import org.olat.core.commons.services.tag.Tag;
import org.olat.core.commons.services.tag.TagRef;
import org.olat.core.commons.services.tag.model.TagImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 
 * Initial date: 8 Mar 2023<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
@Service
public class TagDAO {
	
	@Autowired
	private DB dbInstance;

	public Tag createTag(String displayName) {
		TagImpl tag = new TagImpl();
		tag.setCreationDate(new Date());
		tag.setDisplayName(displayName);
		
		dbInstance.getCurrentEntityManager().persist(tag);
		return tag;
	}

	public void delete(TagRef tag) {
		QueryBuilder sb = new QueryBuilder();
		sb.append("delete from tag tag");
		sb.and().append("tag.key = :tagKey");
		
		dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString())
				.setParameter("tagKey", tag.getKey())
				.executeUpdate();
	}
	
	public Tag loadTag(TagRef tag) {
		QueryBuilder sb = new QueryBuilder();
		sb.append("select tag");
		sb.append("  from tag tag");
		sb.and().append("tag.key = :tagKey");
		
		List<Tag> results = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), Tag.class)
				.setParameter("tagKey", tag.getKey())
				.getResultList();
		
		return !results.isEmpty()? results.get(0): null;
	}
	
	public Tag loadTag(String displayName) {
		QueryBuilder sb = new QueryBuilder();
		sb.append("select tag");
		sb.append("  from tag tag");
		sb.and().append("tag.displayName = :displayName");
		
		List<Tag> results = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), Tag.class)
				.setParameter("displayName", displayName)
				.getResultList();
		
		return !results.isEmpty()? results.get(0): null;
	}
	
	public List<Tag> loadTags(Collection<String> displayNames) {
		QueryBuilder sb = new QueryBuilder();
		sb.append("select tag");
		sb.append("  from tag tag");
		sb.and().append("tag.displayName in :displayNames");
		
		return dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), Tag.class)
				.setParameter("displayNames", displayNames)
				.getResultList();
	}

}
