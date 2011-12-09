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

import java.util.HashSet;
import java.util.List;
import java.util.Map;

import org.olat.core.commons.services.tagging.model.Tag;
import org.olat.core.id.Identity;
import org.olat.core.id.OLATResourceable;

/**
 * 
 * Description:<br>
 * TODO: srosse Class Description for TaggingManager
 * 
 * <P>
 * Initial Date:  19 jul. 2010 <br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
public interface TaggingManager {
	
	/**
	 * get only the tag-string of the found tags
	 * @param identity
	 * @param ores (mandatory)
	 * @param subPath
	 * @param businessPath
	 * @return The tags
	 */
	public List<String> getTagsAsString(Identity identity, OLATResourceable ores, String subPath, String businessPath);

	/**
	 * Get the DB objects for the specified resource
	 * @param ores The OLAT resourceable (mandatory)
	 * @param subPath The sub path (optional)
	 * @param businessPath The business path (optional)
	 * @return The tag DB objects
	 */
	public List<Tag> loadTagsForResource(OLATResourceable ores, String subPath, String businessPath);

	/**
	 * Create a tag and persist it on the DB.
	 * @param author The author (mandatory)
	 * @param tag The tag (mandatory)
	 * @param ores The OLAT resourceable (mandatory)
	 * @param subPath The sub path (optional)
	 * @param businessPath The business path (optional)
	 * @return
	 */
	public Tag createAndPersistTag(Identity author, String tag, OLATResourceable ores, String subPath, String businessPath);
	
	/**
	 * Update the specified tag
	 * @param updateTag The tag to update
	 */
	public void updateTag(Tag updateTag);
	
	/**
	 * Delete the specific tag
	 * @param tagToDelete The tag to delete
	 */
	public void deleteTag(Tag tagToDelete);
	
	/**
	 * Delete all tags from this resource
	 * @param ores
	 * @param subPath
	 * @param businessPath
	 */
	public void deleteTags(OLATResourceable ores, String subPath, String businessPath);

	/**
	 * Return a list of proposals for tagging
	 * @param referenceText
	 * @param onlyExisting if true, returns only such tags, that yet exist
	 * @return Some proposals for tags
	 */ 
	public List<String> proposeTagsForInputText(String referenceText, boolean onlyExisting);

	public float calculateTagRelevance(Tag tag, List<Tag> tagList);

	/** 
	 * get all tags as String a user owns without duplicate and ordered by frequency
	 * @param identity
	 * @return all tags (string only) of user (without duplicate)
	 */
	public List<String> getUserTagsAsString(Identity identity);
	
	/** 
	 * get all tags of given ORES-type as String without duplicate and ordered by frequency
	 * @param identity
	 * @return tags (string only) of given type of user (without duplicate)
	 */
	public List<String> getUserTagsOfTypeAsString(Identity identity, String type);

	/**
	 * get users tags sorted by occurrence and each only once
	 * @param identity
	 * @return a list containing maps with ("tag", "nr")
	 */
	public List<Map<String, Integer>> getUserTagsWithFrequency(Identity identity);

	/**
	 * get all Resources (each only once) tagged with given tagList
	 * @param tagList
	 * @return
	 */
	public HashSet<OLATResourceable> getResourcesByTags(List<Tag> tagList);

	
	
	
}
