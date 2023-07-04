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
package org.olat.modules.cemedia;

import java.io.File;
import java.util.Collection;
import java.util.List;

import org.olat.basesecurity.IdentityRef;
import org.olat.core.commons.services.tag.TagInfo;
import org.olat.core.id.Identity;
import org.olat.core.id.Organisation;
import org.olat.group.BusinessGroup;
import org.olat.modules.cemedia.model.MediaShare;
import org.olat.modules.cemedia.model.MediaUsage;
import org.olat.modules.cemedia.model.MediaUsageWithStatus;
import org.olat.modules.cemedia.model.MediaWithVersion;
import org.olat.modules.cemedia.model.SearchMediaParameters;
import org.olat.modules.taxonomy.TaxonomyLevel;
import org.olat.modules.taxonomy.TaxonomyLevelRef;

/**
 * 
 * 
 * Initial date: 25 mai 2023<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public interface MediaService {
	
	MediaHandler getMediaHandler(String type);
	
	List<MediaHandler> getMediaHandlers();
	
	Media getMediaByKey(Long key);
	
	List<MediaWithVersion> searchMedias(SearchMediaParameters params);
	
	List<Long> filterOwnedDeletableMedias(IdentityRef identity, List<Long> mediasKeys);
	
	Media updateMedia(Media media);
	
	boolean isUsed(Media media);
	
	int deleteMedia(Media media);
	
	Media addVersion(Media media, String content);
	
	Media addVersion(Media media, File file, String filename);
	
	List<MediaVersion> getVersions(Media media);
	
	/**
	 * The list of media and the binder, the course or simply the page where it is used
	 * with an indication if the share match the current state of the document.
	 * 
	 * @param media The media
	 * @return A list of location
	 */
	List<MediaUsageWithStatus> getMediaUsageWithStatus(Media media);
	
	boolean isMediaEditable(IdentityRef identity, MediaLight media);
	
	List<MediaUsage> getMediaUsage(MediaLight media);
	
	List<TagInfo> getTagInfos(Media media);
	
	List<TagInfo> getTagInfos(IdentityRef owner);
	
	List<MediaTag> getTags(IdentityRef owner);
	
	void updateTags(Identity identity, Media media, List<String> tags);
	
	List<TaxonomyLevel> getTaxonomyLevels(Media media);
	
	List<MediaToTaxonomyLevel> getTaxonomyLevels(IdentityRef author);
	
	void updateTaxonomyLevels(Media media, Collection<TaxonomyLevelRef> levels);
	
	List<MediaShare> getMediaShares(Media media);
	
	MediaToGroupRelation addRelation(Media media, boolean editable, Identity identity);
	
	void removeRelation(Media media, Identity identity);
	
	MediaToGroupRelation addRelation(Media media, boolean editable, Organisation organisation);
	
	void removeRelation(Media media, Organisation organisation);
	
	MediaToGroupRelation addRelation(Media media, boolean editable, BusinessGroup businessGroup);
	
	void removeRelation(Media media, BusinessGroup businessGroup);
	
	MediaToGroupRelation updateMediaToGroupRelation(MediaToGroupRelation relation);

}
