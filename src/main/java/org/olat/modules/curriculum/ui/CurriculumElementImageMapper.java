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

import java.util.UUID;

import jakarta.servlet.http.HttpServletRequest;

import org.olat.core.dispatcher.mapper.Mapper;
import org.olat.core.gui.media.MediaResource;
import org.olat.core.util.StringHelper;
import org.olat.core.util.vfs.VFSLeaf;
import org.olat.core.util.vfs.VFSMediaResource;
import org.olat.modules.curriculum.CurriculumElementFileType;
import org.olat.modules.curriculum.CurriculumElementRef;
import org.olat.modules.curriculum.CurriculumService;

/**
 * 
 * Initial date: 6 Dec 2024<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class CurriculumElementImageMapper implements Mapper {
	
	public static final String DEFAULT_ID = "ceimage";
	public static final int DEFAULT_EXPIRATION_TIME = 3600; // One hour
	
	private final CurriculumService curriculumService;

	public CurriculumElementImageMapper(CurriculumService curriculumService) {
		this.curriculumService = curriculumService;
	}

	@Override
	public MediaResource handle(String relPath, HttpServletRequest request) {
		MediaResource mediaResource = null;
		
		if (relPath.endsWith("/")) {
			relPath = relPath.substring(0, relPath.length() - 1);
		}
		
		int lastIndexOf = relPath.lastIndexOf("/");
		if (lastIndexOf > 0) {
			String imageType = relPath.substring(lastIndexOf + 1, relPath.length());
			if (CurriculumElementFileType.isValid(imageType)) {
				// Strip image type part
				relPath = relPath.substring(0, lastIndexOf);
				lastIndexOf = relPath.lastIndexOf("/");
				if (lastIndexOf > 0) {
					String elementKey = relPath.substring(lastIndexOf + 1, relPath.length());
					if (StringHelper.isLong(elementKey)) {
						VFSLeaf vfsLeaf = curriculumService.getCurriculumElemenFile(() -> Long.valueOf(elementKey), CurriculumElementFileType.valueOf(imageType));
						if (vfsLeaf != null) {
							mediaResource = new VFSMediaResource(vfsLeaf);
						}
					}
				}
				
			}
		}
		return mediaResource;
	}
	
	public String getImageUrl(String mapperUrl, CurriculumElementRef element, CurriculumElementFileType type) {
		VFSLeaf vfsLeaf = curriculumService.getCurriculumElemenFile(element, type);
		if (vfsLeaf == null) {
			return null;
		}
		
		long lastModified = vfsLeaf.getLastModified();
		String cachePart = lastModified > 0? String.valueOf(lastModified): UUID.randomUUID().toString().replace("-", "");
		return mapperUrl + "/" + cachePart + "/" + element.getKey() + "/" + type.name();
	}

}
