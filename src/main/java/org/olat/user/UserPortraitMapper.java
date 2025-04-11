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
package org.olat.user;

import jakarta.servlet.http.HttpServletRequest;

import org.olat.core.CoreSpringFactory;
import org.olat.core.dispatcher.mapper.Mapper;
import org.olat.core.gui.media.MediaResource;
import org.olat.core.util.StringHelper;
import org.olat.core.util.vfs.VFSLeaf;
import org.olat.core.util.vfs.VFSMediaResource;

/**
 * 
 * Initial date: Apr 11, 2025<br>
 * @author uhensler, urs.hensler@frentix.com, https://www.frentix.com
 *
 */
public class UserPortraitMapper implements Mapper {
	
	private UserPortraitService userPortraitService;
	
	private UserPortraitService getUserPortraitService() {
		if (userPortraitService == null) {
			userPortraitService = CoreSpringFactory.getImpl(UserPortraitService.class);
		}
		return userPortraitService;
	}
	
	@Override
	public MediaResource handle(String relPath, HttpServletRequest request) {
		if (!StringHelper.containsNonWhitespace(relPath)) {
			return null;
		}
		
		if (relPath.startsWith("/")) {
			relPath = relPath.substring(1, relPath.length());
		}
		
		int sizePathIndex = relPath.lastIndexOf("/");
		if (sizePathIndex < 0) {
			return null;
		}
		
		String sizePath = relPath.substring(sizePathIndex + 1);
		int sizeSuffixIndex = sizePath.indexOf(".");
		if (sizeSuffixIndex < 0) {
			return null;
		}
		sizePath = sizePath.substring(0, sizeSuffixIndex);
		PortraitSize portraitSize = null;
		if (PortraitSize.isValid(sizePath)) {
			portraitSize = PortraitSize.valueOf(sizePath);
		} else {
			return null;
		}
		
		String imagePath = relPath.substring(0, sizePathIndex);
		
		VFSLeaf imageLeaf = getUserPortraitService().getImage(imagePath, portraitSize);
		if (imageLeaf == null) {
			return null;
		}
		
		return new VFSMediaResource(imageLeaf);
	}
	
	public static String createPathFor(String mapperPath, String imagePath, PortraitSize portraitSize) {
		return mapperPath + "/" + imagePath + "/" + portraitSize.name() + ".jpg"; 
	}
}
