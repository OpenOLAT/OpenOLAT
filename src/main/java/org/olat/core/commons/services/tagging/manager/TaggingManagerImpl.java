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
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.persistence.Query;

import org.olat.core.commons.persistence.DB;
import org.olat.core.commons.persistence.DBQuery;
import org.olat.core.commons.services.tagging.model.Tag;
import org.olat.core.commons.services.tagging.model.TagImpl;
import org.olat.core.id.Identity;
import org.olat.core.id.OLATResourceable;
import org.olat.core.logging.AssertException;
import org.olat.core.manager.BasicManager;
import org.olat.core.util.StringHelper;

/**
 * 
 * Description:<br>
 * TODO: srosse Class Description for TaggingManagerImpl
 * 
 * <P>
 * Initial Date:  19 jul. 2010 <br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
public class TaggingManagerImpl extends BasicManager implements TaggingManager {

	private DB dbInstance;
	private TagProposalManager proposalManager;
	
	public TaggingManagerImpl() {
		//
	}
	
	/**
	 * [used by Spring]
	 * @param dbInstance
	 */
	public void setDbInstance(DB dbInstance) {
		this.dbInstance = dbInstance;
	}
	
	/**
	 * [used by Spring]
	 * @param proposalManager
	 */
	public void setProposalManager(TagProposalManager proposalManager) {
		this.proposalManager = proposalManager;
	}

	@Override
	public List<String> getTagsAsString(Identity identity, OLATResourceable ores, String subPath, String businessPath) {
		if (ores.getResourceableId() == null || ores.getResourceableTypeName() == null){
			// this ores seems not yet to be persisted, therefore has no key and as a result no tags!
			return null;
		}
		StringBuilder sb = new StringBuilder();
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
		
		DBQuery query = dbInstance.createQuery(sb.toString());
		query.setLong("resId", ores.getResourceableId());
		query.setString("resName", ores.getResourceableTypeName());
		if(subPath != null) {
			query.setString("subPath", subPath);
		}
		if (identity != null){
			query.setEntity("author", identity);
		}
		if(businessPath != null) {
			query.setString("businessPath", businessPath);
		}
		
		@SuppressWarnings("unchecked")
		List<String> tags = query.list();
		return tags;
	}

	@Override
	public List<String> getUserTagsAsString(Identity identity){
		if (identity == null) return Collections.emptyList();
		StringBuilder sb = new StringBuilder();
		sb.append("select tag.tag from ").append(TagImpl.class.getName())
			.append(" tag where tag.author=:author")
			.append(" group by tag.tag")
			.append(" order by count(tag.key) DESC");

		DBQuery query = dbInstance.createQuery(sb.toString());
		query.setEntity("author", identity);
		
		@SuppressWarnings("unchecked")
		List<String> tags = query.list();
		return tags;
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

		DBQuery query = dbInstance.createQuery(sb.toString());
		query.setEntity("author", identity);
		query.setString("resName", type);
		@SuppressWarnings("unchecked")
		List<String> tags = query.list();
		return tags;
	}

	@Override
	public List<Map<String, Integer>> getUserTagsWithFrequency(Identity identity){
		if (identity == null) return null;
		StringBuilder sb = new StringBuilder();
		sb.append("select new map ( tag.tag as tag, count(*) as nr ) from ").append(TagImpl.class.getName())
		.append(" tag where tag.author=:author and tag.tag=tag.tag ")
		.append("Group by tag.tag order by count(*) DESC, tag.tag ASC");
		
		DBQuery query = dbInstance.createQuery(sb.toString());
		query.setEntity("author", identity);
		
		@SuppressWarnings("unchecked")
		List<Map<String, Integer>> tags = query.list();
		return tags;				
	}
	
	@Override
	public HashSet<OLATResourceable> getResourcesByTags(List<Tag> tagList){
		if (tagList == null || tagList.size() == 0) return null;
		StringBuilder sb = new StringBuilder();
		sb.append("select tag from ").append(TagImpl.class.getName())
		.append(" tag where tag in ( :tagList )");
		
		DBQuery query = dbInstance.createQuery(sb.toString());
		query.setParameterList("tagList", tagList);
		
		@SuppressWarnings("unchecked")
		List<Tag> resList = query.list();
		HashSet<OLATResourceable> oresList = new HashSet<OLATResourceable>();
		for (Iterator<Tag> iterator = resList.iterator(); iterator.hasNext();) {
			Tag tag = iterator.next();
			OLATResourceable ores = tag.getOLATResourceable();
			oresList.add(ores);			
		}		
		return oresList;
	}
	
	
	@Override
	public List<Tag> loadTagsForResource(OLATResourceable ores, String subPath, String businessPath) {
		if (ores.getResourceableId() == null) return null;
		StringBuilder sb = new StringBuilder();
		sb.append("select tag from ").append(TagImpl.class.getName())
			.append(" tag where tag.resId=:resId and tag.resName=:resName");
		if(subPath != null) {
			sb.append(" and tag.subPath=:subPath");
		}
		if(businessPath != null) {
			sb.append(" and tag.businessPath=:businessPath");
		}
		
		DBQuery query = dbInstance.createQuery(sb.toString());
		query.setLong("resId", ores.getResourceableId());
		query.setString("resName", ores.getResourceableTypeName());
		if(subPath != null) {
			query.setString("subPath", subPath);
		}
		if(businessPath != null) {
			query.setString("businessPath", businessPath);
		}
		
		@SuppressWarnings("unchecked")
		List<Tag> tags = query.list();
		return tags;
	}

	@Override
	public Tag createAndPersistTag(Identity author, String tag, OLATResourceable ores, String subPath, String businessPath) {
		if (author == null || !StringHelper.containsNonWhitespace(tag) || ores.getResourceableId() == null || ores.getResourceableTypeName() == null){
			throw new AssertException("A tag cannot be created and persisted without an author, tag and a valid OlatResourcable");
		}
		if (tag.length() > 128){
			// truncate
			logWarn("tag was too long, truncated to 128 chars. Original: " + tag, null);
			tag = tag.substring(0, 125) + "...";
		}
		TagImpl t = new TagImpl();
		t.setAuthor(author);
		t.setTag(tag.trim());
		t.setResId(ores.getResourceableId());
		t.setResName(ores.getResourceableTypeName());
		t.setBusinessPath(businessPath);
		t.setResSubPath(subPath);
		dbInstance.saveObject(t);
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
		logAudit("Deleted " + tagsDeleted + " tags of resource: " + ores.getResourceableTypeName() + " :: " + ores.getResourceableId());
	}

	@Override
	public List<String> proposeTagsForInputText(String referenceText, boolean onlyExisting) {
		if(proposalManager != null) {
			List<String> proposedTags = proposalManager.proposeTagsForInputText(referenceText, onlyExisting);
			return proposedTags;
		}
		return Collections.emptyList();
	}

	@Override
	public float calculateTagRelevance(Tag tag, List<Tag> tagList) {
		return 0.0f;
	}
}
