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

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.olat.basesecurity.Group;
import org.olat.basesecurity.GroupRoles;
import org.olat.basesecurity.IdentityRef;
import org.olat.basesecurity.manager.GroupDAO;
import org.olat.core.commons.services.tag.Tag;
import org.olat.core.commons.services.tag.TagInfo;
import org.olat.core.commons.services.tag.TagService;
import org.olat.core.id.Identity;
import org.olat.core.id.Organisation;
import org.olat.core.util.FileUtils;
import org.olat.group.BusinessGroup;
import org.olat.modules.ceditor.manager.ContentEditorFileStorage;
import org.olat.modules.cemedia.Media;
import org.olat.modules.cemedia.MediaHandler;
import org.olat.modules.cemedia.MediaLight;
import org.olat.modules.cemedia.MediaService;
import org.olat.modules.cemedia.MediaTag;
import org.olat.modules.cemedia.MediaToGroupRelation;
import org.olat.modules.cemedia.MediaToGroupRelation.MediaToGroupRelationType;
import org.olat.modules.cemedia.MediaToTaxonomyLevel;
import org.olat.modules.cemedia.MediaVersion;
import org.olat.modules.cemedia.model.MediaShare;
import org.olat.modules.cemedia.model.MediaUsage;
import org.olat.modules.cemedia.model.MediaUsageWithStatus;
import org.olat.modules.cemedia.model.MediaWithVersion;
import org.olat.modules.cemedia.model.SearchMediaParameters;
import org.olat.modules.taxonomy.TaxonomyLevel;
import org.olat.modules.taxonomy.TaxonomyLevelRef;
import org.olat.modules.taxonomy.manager.TaxonomyLevelDAO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 
 * Initial date: 25 mai 2023<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
@Service
public class MediaServiceImpl implements MediaService {

	@Autowired
	private GroupDAO groupDao;
	@Autowired
	private MediaDAO mediaDao;
	@Autowired
	private TagService tagService;
	@Autowired
	private MediaTagDAO mediaTagDao;
	@Autowired
	private MediaRelationDAO mediaRelationDao;
	@Autowired
	private TaxonomyLevelDAO taxonomyLevelDao;
	@Autowired
	private ContentEditorFileStorage fileStorage;
	@Autowired
	private MediaToTaxonomyLevelDAO mediaToTaxonomyLevelDao;

	@Autowired
	private List<MediaHandler> mediaHandlers;
	
	@Override
	public MediaHandler getMediaHandler(String type) {
		if(mediaHandlers != null) {
			for(MediaHandler handler:mediaHandlers) {
				if(type.equals(handler.getType())) {
					return handler;
				}
			}
		}
		return null;
	}

	@Override
	public List<MediaHandler> getMediaHandlers() {
		return new ArrayList<>(mediaHandlers);
	}

	@Override
	public Media updateMedia(Media media) {
		return mediaDao.update(media);
	}

	@Override
	public void deleteMedia(Media media) {
		mediaDao.deleteMedia(media);
	}

	@Override
	public Media addVersion(Media media, String content) {
		return mediaDao.addVersion(media, new Date(), content, null, null);
	}

	@Override
	public Media addVersion(Media media, File file, String filename) {
		File mediaDir = fileStorage.generateMediaSubDirectory(media);
		File mediaFile = new File(mediaDir, filename);
		FileUtils.copyFileToFile(file, mediaFile, false);
		String storagePath = fileStorage.getRelativePath(mediaDir);
		return mediaDao.addVersion(media, new Date(), filename, storagePath, filename);
	}
	
	@Override
	public List<MediaVersion> getVersions(Media media) {
		return mediaDao.getVersions(media);
	}

	@Override
	public List<MediaUsageWithStatus> getMediaUsageWithStatus(IdentityRef identity, MediaLight media) {
		List<MediaUsageWithStatus> usages = mediaDao.getPageUsages(identity, media);
		List<MediaUsageWithStatus> portfolioUsages = mediaDao.getPortfolioUsages(identity, media);
		usages.addAll(portfolioUsages);
		return usages;
	}

	@Override
	public boolean isMediaEditable(IdentityRef identity, MediaLight media) {
		return mediaDao.isEditable(identity, media);
	}
	
	@Override
	public List<MediaUsage> getMediaUsage(MediaLight media) {
		return mediaDao.getUsages(media);
	}

	@Override
	public List<TagInfo> getTagInfos(Media media) {
		if(media == null || media.getKey() == null) {
			return new ArrayList<>();
		}
		return mediaTagDao.loadMediaTagInfos(media);
	}
	
	@Override
	public List<MediaTag> getTags(IdentityRef owner) {
		return mediaTagDao.loadMediaTags(owner);
	}

	@Override
	public void updateTags(Identity identity, Media media, List<String> displayNames) {
		List<MediaTag> mediaTags = mediaTagDao.loadMediaTags(media);
		List<Tag> currentTags = mediaTags.stream().map(MediaTag::getTag).toList();
		List<Tag> tags = tagService.getOrCreateTags(displayNames);

		for (Tag tag : tags) {
			if (!currentTags.contains(tag)) {
				mediaTagDao.create(media, tag);
			}
		}
		
		for (MediaTag mediaTag : mediaTags) {
			if (!tags.contains(mediaTag.getTag())) {
				mediaTagDao.delete(mediaTag);
			}
		}
	}
	
	@Override
	public List<TaxonomyLevel> getTaxonomyLevels(Media media) {
		if(media == null || media.getKey() == null) {
			return new ArrayList<>();
		}
		return mediaToTaxonomyLevelDao.loadTaxonomyLevels(media);
	}

	@Override
	public List<MediaToTaxonomyLevel> getTaxonomyLevels(IdentityRef author) {
		return mediaToTaxonomyLevelDao.loadRelations(author);
	}

	@Override
	public void updateTaxonomyLevels(Media media, Collection<TaxonomyLevelRef> levels) {
		List<MediaToTaxonomyLevel> currentLevels = mediaToTaxonomyLevelDao.loadRelations(media);
		Set<Long> selectedKeys = levels.stream()
				.map(TaxonomyLevelRef::getKey)
				.collect(Collectors.toSet());
		
		Set<Long> currentKeys = currentLevels.stream()
				.map(MediaToTaxonomyLevel::getTaxonomyLevel)
				.map(TaxonomyLevel::getKey)
				.collect(Collectors.toSet());
		
		for(Long selectedKey:selectedKeys) {
			if(!currentKeys.contains(selectedKey)) {
				TaxonomyLevel level = taxonomyLevelDao.loadByKey(selectedKey);
				mediaToTaxonomyLevelDao.createRelation(media, level);
			}
		}
		
		for(MediaToTaxonomyLevel currentRelation:currentLevels) {
			if(!selectedKeys.contains(currentRelation.getTaxonomyLevel().getKey())) {
				mediaToTaxonomyLevelDao.deleteRelation(currentRelation);
			}
		}
	}

	@Override
	public List<MediaWithVersion> searchMedias(SearchMediaParameters parameters) {
		return mediaDao.searchBy(parameters);
	}

	@Override
	public List<Long> filterOwnedDeletableMedias(IdentityRef identity, List<Long> mediasKeys) {
		return mediaDao.filterOwnedDeletableMedias(identity, mediasKeys);
	}

	@Override
	public Media getMediaByKey(Long key) {
		return mediaDao.loadByKey(key);
	}

	@Override
	public List<MediaShare> getMediaShares(Media media) {
		List<MediaShare> shares = mediaRelationDao.getUserRelations(media);
		List<MediaShare> businessGroupShares = mediaRelationDao.getBusinesGroupRelations(media);
		shares.addAll(businessGroupShares);
		List<MediaShare> organisationsShares = mediaRelationDao.getOrganisationRelations(media);
		shares.addAll(organisationsShares);
		return shares;
	}

	@Override
	public MediaToGroupRelation updateMediaToGroupRelation(MediaToGroupRelation relation) {
		return mediaRelationDao.updateRelation(relation);
	}

	@Override
	public MediaToGroupRelation addRelation(Media media, boolean editable, Identity identity) {
		MediaToGroupRelation relation = mediaRelationDao.getRelation(media, MediaToGroupRelationType.USER, editable);
		MediaToGroupRelation reverseRelation = mediaRelationDao.getRelation(media, MediaToGroupRelationType.USER, !editable);
		if(reverseRelation != null && groupDao.hasRole(reverseRelation.getGroup(), identity, GroupRoles.participant.name())) {
			groupDao.removeMembership(reverseRelation.getGroup(), identity, GroupRoles.participant.name());
		}
		if(relation == null) {
			Group userGroup = groupDao.createGroup();
			relation = mediaRelationDao.createRelation(MediaToGroupRelationType.USER, editable, media, userGroup);
			groupDao.addMembershipTwoWay(relation.getGroup(), identity, GroupRoles.participant.name());
		} else if(!groupDao.hasRole(relation.getGroup(), identity, GroupRoles.participant.name())) {
			groupDao.addMembershipTwoWay(relation.getGroup(), identity, GroupRoles.participant.name());
		}
		return relation;
	}

	@Override
	public void removeRelation(Media media, Identity identity) {
		MediaToGroupRelation relation = mediaRelationDao.getRelation(media, MediaToGroupRelationType.USER, true);
		if(relation != null && groupDao.hasRole(relation.getGroup(), identity, GroupRoles.participant.name())) {
			groupDao.removeMembership(relation.getGroup(), identity, GroupRoles.participant.name());
		}
		MediaToGroupRelation reverseRelation = mediaRelationDao.getRelation(media, MediaToGroupRelationType.USER, false);
		if(reverseRelation != null && groupDao.hasRole(reverseRelation.getGroup(), identity, GroupRoles.participant.name())) {
			groupDao.removeMembership(reverseRelation.getGroup(), identity, GroupRoles.participant.name());
		}
	}

	@Override
	public MediaToGroupRelation addRelation(Media media, boolean editable, Organisation organisation) {
		MediaToGroupRelation relation = mediaRelationDao.getRelation(media, MediaToGroupRelationType.ORGANISATION,
				editable, organisation.getGroup());
		if(relation == null) {
			relation = mediaRelationDao.createRelation(MediaToGroupRelationType.ORGANISATION, editable, media, organisation.getGroup());
		}
		return relation;
	}
	
	@Override
	public void removeRelation(Media media, Organisation organisation) {
		List<MediaToGroupRelation> relations = mediaRelationDao.getRelations(media, MediaToGroupRelationType.ORGANISATION, organisation.getGroup());
		for(MediaToGroupRelation relation:relations) {
			mediaRelationDao.deleteRelation(relation);
		}
	}

	@Override
	public MediaToGroupRelation addRelation(Media media, boolean editable, BusinessGroup businessGroup) {
		MediaToGroupRelation relation = mediaRelationDao.getRelation(media, MediaToGroupRelationType.BUSINESS_GROUP,
				editable, businessGroup.getBaseGroup());
		if(relation == null) {
			relation = mediaRelationDao.createRelation(MediaToGroupRelationType.BUSINESS_GROUP, editable, media, businessGroup.getBaseGroup());
		}
		return relation;
	}
	
	@Override
	public void removeRelation(Media media, BusinessGroup businessGroup) {
		List<MediaToGroupRelation> relations = mediaRelationDao.getRelations(media, MediaToGroupRelationType.BUSINESS_GROUP, businessGroup.getBaseGroup());
		for(MediaToGroupRelation relation:relations) {
			mediaRelationDao.deleteRelation(relation);
		}
	}

}
