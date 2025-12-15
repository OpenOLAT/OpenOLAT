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
package org.olat.modules.curriculum.ui;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import jakarta.servlet.http.HttpServletRequest;

import org.olat.core.CoreSpringFactory;
import org.olat.core.commons.services.vfs.VFSMetadata;
import org.olat.core.commons.services.vfs.VFSRepositoryService;
import org.olat.core.commons.services.vfs.VFSThumbnailMetadata;
import org.olat.core.commons.services.vfs.manager.VFSMetadataDAO;
import org.olat.core.commons.services.vfs.manager.VFSThumbnailDAO;
import org.olat.core.commons.services.vfs.model.VFSThumbnailInfos;
import org.olat.core.dispatcher.mapper.Mapper;
import org.olat.core.gui.media.MediaResource;
import org.olat.core.gui.media.NotFoundMediaResource;
import org.olat.core.gui.media.ServletUtil;
import org.olat.core.id.OLATResourceable;
import org.olat.core.util.StringHelper;
import org.olat.core.util.vfs.VFSItem;
import org.olat.core.util.vfs.VFSLeaf;
import org.olat.core.util.vfs.VFSMediaResource;
import org.olat.core.util.vfs.VFSThumbnailResource;
import org.olat.modules.curriculum.CurriculumElementFileType;
import org.olat.modules.curriculum.CurriculumElementRef;
import org.olat.modules.curriculum.manager.CurriculumStorage;
import org.olat.modules.curriculum.model.CurriculumElementRefImpl;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 6 Dec 2024<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class CurriculumElementImageMapper implements Mapper {
	
	public static final String MAPPER_ID_210_140 = "curriculumelementImage210x140";
	public static final String MAPPER_ID_900_600 = "curriculumelementImage900x600";
	
	private final int maxWidth;
	private final int maxHeight;
	private final CurriculumElementFileType type;

	@Autowired
	private VFSMetadataDAO metadataDao;
	@Autowired
	private VFSThumbnailDAO thumbnailDao;
	@Autowired
	private VFSRepositoryService vfsRepositoryService;

	private CurriculumElementImageMapper(CurriculumElementFileType type, int maxWidth, int maxHeight) {
		this.type = type;
		this.maxWidth = maxWidth;
		this.maxHeight = maxHeight;
		CoreSpringFactory.autowireObject(this);
	}
	
	public static final CurriculumElementImageMapper mapper210x140() {
		return new CurriculumElementImageMapper(CurriculumElementFileType.teaserImage, 210, 140);
	}
	
	public static final CurriculumElementImageMapper mapper900x600() {
		return new CurriculumElementImageMapper(CurriculumElementFileType.teaserImage, 900, 600);
	}
	
	public Map<Long,VFSThumbnailInfos> getResourceableThumbnails(List<? extends OLATResourceable> elements) {
		List<String> paths = elements.stream()
				.map(e -> buildPath(e, type))
				.filter(Objects::nonNull)
				.toList();
		return getThumbnailsByPath(paths);
	}
	
	private final String buildPath(OLATResourceable resource, CurriculumElementFileType fileType) {
		if("CurriculumElement".equals(resource.getResourceableTypeName())) {
			return CurriculumStorage.buildPath(new CurriculumElementRefImpl(resource.getResourceableId()), fileType);
		}
		return null;
	}
	
	public String getThumbnailURL(String mapperUrl, CurriculumElementRef element) {
		String path = CurriculumStorage.buildPath(element, type);
		if(path == null) return null;
		
		 Map<Long,VFSThumbnailInfos> map = getThumbnailsByPath(List.of(path));
		return getThumbnailURL(mapperUrl, element.getKey(), map);
	}
	
	public String getThumbnailURL(String mapperUrl, Long curriculumElementKey, Map<Long,VFSThumbnailInfos> mimages) {
		VFSThumbnailInfos mimage = mimages.get(curriculumElementKey);
		return mimage == null
				? null
				: getImageURL(mapperUrl, mimage.metadata(), mimage.thumbnailMetadata());
	}
	
	public Map<Long,VFSThumbnailInfos> getThumbnails(List<? extends CurriculumElementRef> elements) {
		List<String> paths = elements.stream()
				.map(c -> CurriculumStorage.buildPath(c, type))
				.filter(Objects::nonNull)
				.toList();
		return getThumbnailsByPath(paths);
	}
	
	public Map<Long,VFSThumbnailInfos> getThumbnailsByPath(List<String> paths) {
		List<VFSThumbnailInfos> mimages = thumbnailDao.findThumbnails(paths, true, maxWidth, maxHeight);
		
		Map<Long, VFSThumbnailInfos> map = new HashMap<>();
		for(VFSThumbnailInfos mimage:mimages) {
			String filename = mimage.metadata().getFilename();
			if(filename.endsWith(".jpg") || filename.endsWith(".png") || filename.endsWith(".gif")) {
				Long elementKey = getCurriculumElementKey(mimage.metadata().getRelativePath());
				if(elementKey != null) {
					map.put(elementKey, mimage);
				}
			}
		}
		return map;
	}
	
	private Long getCurriculumElementKey(String path) {
		int index = path.indexOf("element/");
		int lastIndex = path.indexOf(type.name()) - 1;
		
		String key = path.substring(index + "elements/".length() - 1, lastIndex);
		if(StringHelper.isLong(key)) {
			return Long.valueOf(key);
		}
		return null;
	}

	@Override
	public MediaResource handle(String relPath, HttpServletRequest request) {
		
		String[] rels = relPath.split("/");
		if(rels == null || rels.length < 2) {
			return new NotFoundMediaResource();
		}
		
		MediaResource mediaResource = null;
		String key = rels[rels.length - 2];
		if("none".equals(key) && StringHelper.isLong(rels[rels.length - 3])) {
			Long metadataKey = Long.valueOf(rels[rels.length - 3]);
			VFSMetadata metadata = metadataDao.loadMetadata(metadataKey);
			VFSItem item = vfsRepositoryService.getItemFor(metadata);
			if(item instanceof VFSLeaf leaf) {
				VFSLeaf thumbnailLeaf = vfsRepositoryService.getThumbnail(leaf, metadata, maxWidth, maxHeight, true);
				if(thumbnailLeaf != null) {
					return new VFSMediaResource(thumbnailLeaf);
				}
			}
		} else if(StringHelper.isLong(rels[rels.length - 2])) {
			Long thumbnailKey = Long.valueOf(rels[rels.length - 2]);
			VFSThumbnailMetadata mthumbnail = thumbnailDao.loadByKey(Long.valueOf(thumbnailKey));
			return new VFSThumbnailResource(mthumbnail, ServletUtil.CACHE_ONE_YEAR);
		}
		if(mediaResource == null) {
			mediaResource = new NotFoundMediaResource();
		}
		return mediaResource;
	}
	
	public static String getImageURL(String mapperUrl, VFSMetadata image, VFSThumbnailMetadata thumbnail) {
		long lastModified = thumbnail == null 
				? image.getLastModified().getTime()
				: thumbnail.getLastModified().getTime();
		String cachePart = String.valueOf(lastModified);
		return mapperUrl + "/" + cachePart + "/cur/" + image.getKey() + "/" + (thumbnail == null ? "none" : thumbnail.getKey()) + "/" + image.getFilename();
	}

}
