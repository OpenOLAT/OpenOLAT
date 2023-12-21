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
import org.olat.core.gui.components.util.SelectionValues;
import org.olat.core.gui.translator.Translator;
import org.olat.core.id.Identity;
import org.olat.core.id.Organisation;
import org.olat.core.id.Roles;
import org.olat.core.util.FileUtils.Usage;
import org.olat.core.util.vfs.Quota;
import org.olat.group.BusinessGroup;
import org.olat.modules.cemedia.MediaLog.Action;
import org.olat.modules.cemedia.model.MediaShare;
import org.olat.modules.cemedia.model.MediaUsage;
import org.olat.modules.cemedia.model.MediaUsageWithStatus;
import org.olat.modules.cemedia.model.MediaWithVersion;
import org.olat.modules.cemedia.model.SearchMediaLogParameters;
import org.olat.modules.cemedia.model.SearchMediaParameters;
import org.olat.modules.taxonomy.TaxonomyLevel;
import org.olat.modules.taxonomy.TaxonomyLevelRef;
import org.olat.repository.RepositoryEntry;

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
	
	/**
	 * Copy the current content to a new version.
	 *
	 * @param media The media
	 * @param mediaVersion The media version
	 * @return The updated media
	 */
	Media setVersion(Media media, MediaVersion mediaVersion, Identity doer);
	
	Media restoreVersion(Media media, MediaVersion version);

	Media addVersion(Media media, Identity doer, MediaVersionMetadata versionMetadata, Action action);

	Media addVersion(Media media, String content, Identity identity, MediaLog.Action action);
	
	Media addVersion(Media media, File file, String filename, Identity identity, MediaLog.Action action);
	
	MediaVersion getMediaVersionByKey(Long key);
	
	MediaVersion updateMediaVersion(MediaVersion mediaVersion);
	
	List<MediaVersion> getVersions(Media media);
	
	/**
	 * The list of media and the binder, the course or simply the page where it is used
	 * with an indication if the share match the current state of the document.
	 * 
	 * @param media The media
	 * @return A list of location
	 */
	List<MediaUsageWithStatus> getMediaUsageWithStatus(IdentityRef identity, Media media);
	
	boolean isMediaEditable(IdentityRef identity, MediaLight media);
	
	/**
	 * Is the specified identity indirectly administrator of the
	 * course which shared this media.
	 * 
	 * @param identity The identity
	 * @param media The media
	 * @return true/false
	 */
	boolean isAdminOf(IdentityRef identity, MediaLight media);
	
	/**
	 * 
	 * @param identity The identity (mandatory)
	 * @param media The media (mandatory)
	 * @param editable If sharing is editable or not (optional)
	 * @return true if the media is shared with the specified options
	 */
	boolean isMediaShared(IdentityRef identity, MediaLight media, Boolean editable);
	
	boolean isInMediaCenter(IdentityRef identity, File file);
	
	List<MediaUsage> getMediaUsage(MediaLight media);
	
	long countMediaUsage(List<? extends MediaLight> medias);
	
	Usage getFileUsage(IdentityRef author);
	
	Quota getQuota(IdentityRef identity, Roles roles);
	
	/**
	 * Log trail of the specified media.
	 * @param media The media
	 * @return The log trail order by date descending
	 */
	List<MediaLog> getMediaLogs(MediaLight media, SearchMediaLogParameters params);
	
	/**
	 * List all users which have done something to the media.
	 * 
	 * @param media The media
	 * @return A list of users
	 */
	List<Identity> getMediaDoers(MediaLight media);
	
	MediaLog addMediaLog(Action action, Media media, Identity doer);
	
	List<TagInfo> getTagInfos(Media media, IdentityRef identity, boolean selectedOnly);
	
	List<MediaTag> getTags(IdentityRef owner, List<Long> mediaKays);
	
	void updateTags(Identity identity, Media media, List<String> tags);
	
	List<TaxonomyLevel> getTaxonomyLevels(Media media);
	
	List<MediaToTaxonomyLevel> getTaxonomyLevels(IdentityRef author);
	
	void updateTaxonomyLevels(Media media, Collection<TaxonomyLevelRef> levels);
	
	List<MediaShare> getMediaShares(Media media);
	
	List<MediaShare> getMediaShares(Media media, RepositoryEntry entry);
	
	MediaToGroupRelation addRelation(Media media, boolean editable, Identity identity);
	
	void removeRelation(Media media, Identity identity);
	
	MediaToGroupRelation addRelation(Media media, boolean editable, Organisation organisation);
	
	void removeRelation(Media media, Organisation organisation);
	
	MediaToGroupRelation addRelation(Media media, boolean editable, BusinessGroup businessGroup);
	
	void removeRelation(Media media, BusinessGroup businessGroup);
	
	MediaToGroupRelation addRelation(Media media, boolean editable, RepositoryEntry entry);
	
	void removeRelation(Media media, RepositoryEntry entry);
	
	
	MediaToGroupRelation updateMediaToGroupRelation(MediaToGroupRelation relation);

	void updateMediaVersionMetadata(Long mediaVersionKey, int width, int height);

	void updateMediaVersionMetadata(Long mediaVersionKey, String formattedTime);

	SelectionValues getSources(IdentityRef identityRef, Translator translator);
}
