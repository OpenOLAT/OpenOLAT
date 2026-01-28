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
package org.olat.repository.ui;


import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import jakarta.servlet.http.HttpServletRequest;

import org.olat.core.CoreSpringFactory;
import org.olat.core.commons.services.vfs.VFSMetadata;
import org.olat.core.commons.services.vfs.VFSRepositoryService;
import org.olat.core.commons.services.vfs.VFSThumbnailMetadata;
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
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryEntryRef;
import org.olat.repository.RepositoryManager;
import org.olat.repository.RepositoryService;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 20.11.2012<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class RepositoryEntryImageMapper implements Mapper {
	
	public static final String MAPPER_ID_210_140 = "repositoryentryImage210x140";
	public static final String MAPPER_ID_900_600 = "repositoryentryImage900x600";

	private final int maxWidth;
	private final int maxHeight;
	
	@Autowired
	private VFSThumbnailDAO thumbnailDao;
	@Autowired
	private RepositoryService repositoryService;
	@Autowired
	private VFSRepositoryService vfsRepositoryService;
	
	private RepositoryEntryImageMapper(int maxWidth, int maxHeight) {
		this.maxWidth = maxWidth;
		this.maxHeight = maxHeight;
		CoreSpringFactory.autowireObject(this);
	}
	
	public static final RepositoryEntryImageMapper mapper210x140() {
		return new RepositoryEntryImageMapper(210, 140);
	}
	
	public static final RepositoryEntryImageMapper mapper900x600() {
		return new RepositoryEntryImageMapper(900, 600);
	}
	
	/**
	 * 
	 * @param entry The repository entry
	 * @return true if the resource has a teaser image or video
	 */
	public boolean hasTeaser(RepositoryEntry entry) {
		String path = RepositoryManager.buildPath(entry.getOlatResource());
		if(path == null) return false;
		
		List<VFSThumbnailInfos> mimages = thumbnailDao.findThumbnails(List.of(path), true, maxWidth, maxHeight);
		for(VFSThumbnailInfos mimage:mimages) {
			String filename = mimage.metadata().getFilename();
			if(filename.endsWith(".jpg") || filename.endsWith(".jpeg") || filename.endsWith(".png") || filename.endsWith(".gif") || filename.endsWith(".mp4")) {
				return true;
			}
		}
		return false;
	}
	
	public VFSThumbnailInfos getRepositoryThumbnail(RepositoryEntry entry) {
		String path = RepositoryManager.buildPath(entry.getOlatResource());
		if(path == null) return null;
		
		 Map<Long,VFSThumbnailInfos> map = getThumbnailsByPath(List.of(path));
		 return map.get(entry.getKey());
	}
	
	public Map<Long, VFSThumbnailInfos> getRepositoryThumbnails(List<RepositoryEntry> entries) {
		List<String> pathList = entries.stream()
				.map(e -> RepositoryManager.buildPath(e.getOlatResource()))
				.filter(Objects::nonNull)
				.toList();
		return  getThumbnailsByPath(pathList);
	}
	
	public Map<Long, VFSThumbnailInfos> getResourceableThumbnails(List<? extends OLATResourceable> entries) {
		List<String> pathList = entries.stream()
				.map(RepositoryManager::buildPath)
				.filter(Objects::nonNull)
				.toList();
		return  getThumbnailsByPath(pathList);
	}
	

	
	private Map<Long, VFSThumbnailInfos> getThumbnailsByPath(List<String> pathList) {
		List<VFSThumbnailInfos> mimages = thumbnailDao.findThumbnails(pathList, true, maxWidth, maxHeight);
		Map<Long, VFSThumbnailInfos> map = new HashMap<>();
		
		for(VFSThumbnailInfos mimage:mimages) {
			String filename = mimage.metadata().getFilename();
			if(filename.endsWith(".jpg") || filename.endsWith(".jpeg") || filename.endsWith(".png") || filename.endsWith(".gif")) {
				Long repositoryEntryKey = getRepositoryEntryKey(filename);
				if(repositoryEntryKey != null) {
					map.put(repositoryEntryKey, mimage);
				}
			}
		}
		return map;
	}
	
	private static final Long getRepositoryEntryKey(String string) {
		int lastIndex = string.lastIndexOf('.');
		if(lastIndex >= 0) {
			string = string.substring(0, lastIndex);
		}
		if(StringHelper.isLong(string)) {
			return Long.valueOf(string);
		}
		return null;
	}
	
	public String getThumbnailURL(String mapperUrl, RepositoryEntryRef repoEntry, Map<Long,VFSThumbnailInfos> mimages) {
		return getThumbnailURL(mapperUrl, repoEntry.getKey(), mimages);
	}
	
	public String getThumbnailURL(String mapperUrl, Long repoEntryKey, Map<Long,VFSThumbnailInfos> mimages) {
		VFSThumbnailInfos mimage = mimages.get(repoEntryKey);
		return mimage == null
				? null
				: getImageURL(mapperUrl, mimage.metadata(), mimage.thumbnailMetadata());
	}
	
	public static String getImageURL(String mapperUrl, VFSMetadata image, VFSThumbnailMetadata thumbnail) {
		long lastModified = thumbnail == null 
				? image.getLastModified().getTime()
				: thumbnail.getLastModified().getTime();
		String cachePart = String.valueOf(lastModified);
		return mapperUrl + "/" + cachePart + "/" + (thumbnail == null ? "none" : thumbnail.getKey()) + "/" + image.getFilename();
	}
	
	@Override
	public MediaResource handle(String relPath, HttpServletRequest request) {
		String[] rels = relPath.split("/");
		if(rels == null || rels.length < 2) {
			return new NotFoundMediaResource();
		}
		
		String repositoryEntryKey = rels[rels.length - 1];
		int lastIndex = repositoryEntryKey.lastIndexOf('.');
		if(lastIndex >= 0) {
			repositoryEntryKey = repositoryEntryKey.substring(0, lastIndex);
		}

		String thumbnailKey = rels[rels.length - 2];
		
		MediaResource resource = null;
		if(StringHelper.isLong(repositoryEntryKey)) {
			if(StringHelper.isLong(thumbnailKey)) {
				VFSThumbnailMetadata mthumbnail = thumbnailDao.loadByKey(Long.valueOf(thumbnailKey));
				resource = new VFSThumbnailResource(mthumbnail, ServletUtil.CACHE_ONE_YEAR);
			} else {
				RepositoryEntry re = repositoryService.loadByKey(Long.valueOf(repositoryEntryKey));
				VFSItem image = repositoryService.getIntroductionImage(re);
				if(image instanceof VFSLeaf leaf) {
					VFSLeaf thumbnail = vfsRepositoryService.getThumbnail(leaf, maxWidth, maxHeight, true);
					if(thumbnail != null) {
						resource = new VFSMediaResource(thumbnail);
					}
					
					if(resource == null) {
						resource = new VFSMediaResource(leaf);
					}
				} else {
					resource = new NotFoundMediaResource();
				}
			}
		} else {
			resource = new NotFoundMediaResource();
		}
		return resource;
	}
}
