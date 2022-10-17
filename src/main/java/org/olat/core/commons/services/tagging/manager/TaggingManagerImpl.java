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
package org.olat.core.commons.services.tagging.manager;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import jakarta.persistence.Query;
import jakarta.persistence.TypedQuery;

import org.olat.core.commons.persistence.DB;
import org.olat.core.commons.services.tagging.model.Tag;
import org.olat.core.commons.services.tagging.model.TagImpl;
import org.olat.core.id.Identity;
import org.olat.core.id.OLATResourceable;
import org.olat.core.logging.AssertException;
import org.apache.logging.log4j.Logger;
import org.olat.core.logging.Tracing;
import org.olat.core.util.StringHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 
 * <P>
 * Initial Date:  19 jul. 2010 <br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
@Service("taggingManager")
public class TaggingManagerImpl implements TaggingManager {
	
	private static final Logger log = Tracing.createLoggerFor(TaggingManagerImpl.class);

	@Autowired
	private DB dbInstance;
	@Autowired
	private TagProposalManager proposalManager;

	@Override
	public List<String> getTagsAsString(Identity identity, OLATResourceable ores, String subPath, String businessPath) {
		if (ores.getResourceableId() == null || ores.getResourceableTypeName() == null){
			// this ores seems not yet to be persisted, therefore has no key and as a result no tags!
			return Collections.emptyList();
		}
		StringBuilder sb = new StringBuilder(128);
		sb.append("select tag.tag from ").append(TagImpl.class.getName())
			.append(" tag where tag.resId=:resId and tag.resName=:resName");
		if(subPath != null) {
			sb.append(" and tag.subPath=:subPath");
		}
		if (identity != null) {
			sb.append(" and tag.author=:author");
		}
		if(businessPath != null) {
			sb.append(" and tag.businessPath=:businessPath");
		}
		sb.append(" group by tag.tag");
		
		TypedQuery<String> query = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), String.class)
				.setParameter("resId", ores.getResourceableId())
				.setParameter("resName", ores.getResourceableTypeName());
		if(subPath != null) {
			query.setParameter("subPath", subPath);
		}
		if (identity != null){
			query.setParameter("author", identity);
		}
		if(businessPath != null) {
			query.setParameter("businessPath", businessPath);
		}
		return query.getResultList();
	}

	@Override
	public List<String> getUserTagsAsString(Identity identity){
		if (identity == null) return Collections.emptyList();
		
		StringBuilder sb = new StringBuilder(128);
		sb.append("select tag.tag from ").append(TagImpl.class.getName())
			.append(" tag where tag.author=:author")
			.append(" group by tag.tag")
			.append(" order by count(tag.key) DESC");
		return dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), String.class)
				.setParameter("author", identity)
				.getResultList();
	}
	
	/**
	 * @see org.olat.core.commons.services.tagging.manager.TaggingManager#getUserTagsOfTypeAsString(org.olat.core.id.Identity, java.lang.String)
	 */
	@Override
	public List<String> getUserTagsOfTypeAsString(Identity identity, String type) {
		if (identity == null || !StringHelper.containsNonWhitespace(type)) return Collections.emptyList();
		
		StringBuilder sb = new StringBuilder();
		sb.append("select tag.tag from ").append(TagImpl.class.getName())
			.append(" tag where tag.author=:author and tag.resName=:resName")
			.append(" group by tag.tag")
			.append(" order by count(tag.key) DESC");

		return dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), String.class)
				.setParameter("author", identity)
				.setParameter("resName", type)
				.getResultList();
	}

	@Override
	public List<Map<String, Integer>> getUserTagsWithFrequency(Identity identity){
		if (identity == null) return Collections.emptyList();
		
		StringBuilder sb = new StringBuilder();
		sb.append("select new map ( tag.tag as tag, count(*) as nr ) from ").append(TagImpl.class.getName())
		.append(" tag where tag.author=:author and tag.tag=tag.tag ")
		.append("Group by tag.tag order by count(*) DESC, tag.tag ASC");
		
		return dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString())
				.setParameter("author", identity).getResultList();		
	}
	
	@Override
	public Set<OLATResourceable> getResourcesByTags(List<Tag> tagList){
		if (tagList == null || tagList.isEmpty()) return Collections.emptySet();
		
		StringBuilder sb = new StringBuilder();
		sb.append("select tag from ").append(TagImpl.class.getName())
		  .append(" tag where tag in ( :tagList )");
		
		List<Tag> resList = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), Tag.class)
				.setParameter("tagList", tagList)
				.getResultList();
		Set<OLATResourceable> oresList = new HashSet<>();
		for (Tag tag : resList) {
			oresList.add(tag.getOLATResourceable());			
		}		
		return oresList;
	}
	
	@Override
	public List<Tag> loadTagsForResource(OLATResourceable ores, String subPath, String businessPath) {
		if (ores.getResourceableId() == null) return Collections.emptyList();
		
		StringBuilder sb = new StringBuilder();
		sb.append("select tag from ").append(TagImpl.class.getName())
			.append(" tag where tag.resId=:resId and tag.resName=:resName");
		if(subPath != null) {
			sb.append(" and tag.subPath=:subPath");
		}
		if(businessPath != null) {
			sb.append(" and tag.businessPath=:businessPath");
		}
		
		TypedQuery<Tag> query = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), Tag.class)
				.setParameter("resId", ores.getResourceableId())
				.setParameter("resName", ores.getResourceableTypeName());
		if(subPath != null) {
			query.setParameter("subPath", subPath);
		}
		if(businessPath != null) {
			query.setParameter("businessPath", businessPath);
		}
		return query.getResultList();
	}

	@Override
	public Tag createAndPersistTag(Identity author, String tag, OLATResourceable ores, String subPath, String businessPath) {
		if (author == null || !StringHelper.containsNonWhitespace(tag) || ores.getResourceableId() == null || ores.getResourceableTypeName() == null){
			throw new AssertException("A tag cannot be created and persisted without an author, tag and a valid OlatResourcable");
		}
		if (tag.length() > 128){
			// truncate
			log.warn("tag was too long, truncated to 128 chars. Original: " + tag);
			tag = tag.substring(0, 125) + "...";
		}
		TagImpl t = new TagImpl();
		t.setAuthor(author);
		t.setTag(tag.trim());
		t.setResId(ores.getResourceableId());
		t.setResName(ores.getResourceableTypeName());
		t.setBusinessPath(businessPath);
		t.setResSubPath(subPath);
		dbInstance.getCurrentEntityManager().persist(t);
		return t;
	}

	@Override
	public void updateTag(Tag updateTag) {
		dbInstance.updateObject(updateTag);
	}

	@Override
	public void deleteTag(Tag tagToDelete) {
		dbInstance.deleteObject(tagToDelete);
	}

	@Override
	public void deleteTags(OLATResourceable ores, String subPath, String businessPath) {
		StringBuilder sb = new StringBuilder();
		sb.append("delete from ").append(TagImpl.class.getName()).append(" where resId=:resId and resName=:resName");
		if(subPath != null) {
			sb.append(" and resSubPath=:subPath");
		}
		if(businessPath != null) {
			sb.append(" and businessPath=:businessPath");
		}
		
		Query query = dbInstance.getCurrentEntityManager()
			.createQuery(sb.toString())
			.setParameter("resId", ores.getResourceableId())
			.setParameter("resName", ores.getResourceableTypeName());
		if(subPath != null) {
			query.setParameter("subPath", subPath);
		}
		if(businessPath != null) {
			query.setParameter("businessPath", businessPath);
		}
		
		int tagsDeleted = query.executeUpdate();
		log.info(Tracing.M_AUDIT, "Deleted " + tagsDeleted + " tags of resource: " + ores.getResourceableTypeName() + " :: " + ores.getResourceableId());
	}

	@Override
	public List<String> proposeTagsForInputText(String referenceText, boolean onlyExisting) {
		if(proposalManager != null) {
			return proposalManager.proposeTagsForInputText(referenceText, onlyExisting);
		}
		return Collections.emptyList();
	}

	@Override
	public float calculateTagRelevance(Tag tag, List<Tag> tagList) {
		return 0.0f;
	}
}
