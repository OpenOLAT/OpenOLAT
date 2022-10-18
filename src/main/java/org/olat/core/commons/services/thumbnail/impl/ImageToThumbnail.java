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

package org.olat.core.commons.services.thumbnail.impl;

import java.util.ArrayList;
import java.util.List;

import jakarta.annotation.PostConstruct;
import javax.imageio.ImageIO;

import org.olat.core.commons.services.image.ImageService;
import org.olat.core.commons.services.image.Size;
import org.olat.core.commons.services.thumbnail.FinalSize;
import org.olat.core.commons.services.thumbnail.ThumbnailSPI;
import org.olat.core.util.vfs.VFSLeaf;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 
 * Description:<br>
 * Generate a thumbnail from an image based on ImageIO
 * 
 * <P>
 * Initial Date:  30 mar. 2010 <br>
 * @author srosse, stephane.rosse@frentix.com
 */
@Service
public class ImageToThumbnail implements ThumbnailSPI {
	
	private final List<String> extensions = new ArrayList<>();
	
	@Autowired
	private ImageService imageHelper;

	@PostConstruct
	private void initExtensions() {
		for(String imageIOSuffix : ImageIO.getWriterFileSuffixes()) {
			extensions.add(imageIOSuffix);
		}
	}
	
	@Override
	public List<String> getExtensions() {
		return extensions;
	}

	@Override
	public FinalSize generateThumbnail(VFSLeaf file, VFSLeaf thumbnailFile, int maxWidth, int maxHeight, boolean fill) {
		Size finalSize = imageHelper.scaleImage(file, thumbnailFile, maxWidth, maxHeight, fill);
		if(finalSize != null) {
			return new FinalSize(finalSize.getWidth(), finalSize.getHeight());
		}
		//a problem happens
		return null;
	}
}
