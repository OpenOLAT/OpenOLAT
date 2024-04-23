/**
 * <a href="https://www.openolat.org">
 * OpenOLAT - Online Learning and Training</a><br>
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); <br>
 * you may not use this file except in compliance with the License.<br>
 * You may obtain a copy of the License at the
 * <a href="https://www.apache.org/licenses/LICENSE-2.0">Apache homepage</a>
 * <p>
 * Unless required by applicable law or agreed to in writing,<br>
 * software distributed under the License is distributed on an "AS IS" BASIS, <br>
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
 * See the License for the specific language governing permissions and <br>
 * limitations under the License.
 * <p>
 * Initial code contributed and copyrighted by<br>
 * frentix GmbH, https://www.frentix.com
 * <p>
 */
package org.olat.core.commons.services.folder.ui;

import jakarta.servlet.http.HttpServletRequest;

import org.olat.core.commons.services.vfs.VFSMetadata;
import org.olat.core.commons.services.vfs.VFSRepositoryService;
import org.olat.core.dispatcher.mapper.Mapper;
import org.olat.core.gui.media.MediaResource;
import org.olat.core.gui.media.NotFoundMediaResource;
import org.olat.core.util.FileUtils;
import org.olat.core.util.StringHelper;
import org.olat.core.util.vfs.VFSItem;
import org.olat.core.util.vfs.VFSLeaf;
import org.olat.core.util.vfs.VFSMediaResource;
import org.olat.modules.audiovideorecording.AVModule;

/**
 * 
 * Initial date: 22 Apr 2024<br>
 * @author uhensler, urs.hensler@frentix.com, https://www.frentix.com
 *
 */
public class FolderThumbnailMapper implements Mapper {
	
	private final VFSMetadata vfsMetadata;
	private VFSItem vfsItem;
	
	private final VFSRepositoryService vfsRepositoryService;
	private final AVModule avModule;
	
	public FolderThumbnailMapper(VFSRepositoryService vfsRepositoryService, AVModule avModule, VFSMetadata vfsMetadata) {
		this(vfsRepositoryService, avModule, vfsMetadata, null);
	}
	
	public FolderThumbnailMapper(VFSRepositoryService vfsRepositoryService, AVModule avModule, VFSMetadata vfsMetadata, VFSItem vfsItem) {
		this.vfsRepositoryService = vfsRepositoryService;
		this.avModule = avModule;
		this.vfsMetadata = vfsMetadata;
		this.vfsItem = vfsItem;
	}

	@Override
	public MediaResource handle(String relPath, HttpServletRequest request) {
		if (relPath.startsWith("/")) {
			relPath = relPath.substring(1, relPath.length());
		}
		
		String[] parts = relPath.split("/");
		if (parts.length < 2) {
			return new NotFoundMediaResource();
		}
		
		int width;
		if (StringHelper.isLong(parts[0])) {
			width = Integer.valueOf(parts[0]);
		} else {
			return new NotFoundMediaResource();
		}
		
		int height;
		if (StringHelper.isLong(parts[1])) {
			height = Integer.valueOf(parts[1]);
		} else {
			return new NotFoundMediaResource();
		}
		
		// Goal: Lazy generation of thumbnails.
		
		// maper url = vfsid + lastmod time. Dann wird er vom Browser gecached und das vfsmetadata muss nicht immer neu geladen werden.
		
		if (vfsItem == null) {
			vfsItem = vfsRepositoryService.getItemFor(vfsMetadata);
		}
		if (vfsItem instanceof VFSLeaf vfsLeaf) {
			VFSLeaf thumbnail = null;
			if (isAudio(vfsMetadata, vfsLeaf)) {
				thumbnail = vfsRepositoryService.getLeafFor(avModule.getAudioWaveformUrl());
			} else {
				thumbnail = vfsRepositoryService.getThumbnail(vfsLeaf, vfsMetadata, width, height, false);
			}
			
			if (thumbnail != null) {
				return new VFSMediaResource(thumbnail);
			}
		}
		
		return new NotFoundMediaResource();
	}
	
	public static final boolean isAudio(VFSMetadata vfsMetadata, VFSLeaf vfsLeaf) {
		String filename = vfsMetadata != null
				? vfsMetadata.getFilename()
				: vfsLeaf.getName();
		if ("m4a".equalsIgnoreCase(FileUtils.getFileSuffix(filename))) {
			return true;
		}
		return false;
	}

}
