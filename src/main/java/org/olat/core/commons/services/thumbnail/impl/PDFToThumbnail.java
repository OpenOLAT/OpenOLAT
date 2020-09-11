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

import java.util.Collections;
import java.util.List;

import org.olat.core.commons.services.image.ImageService;
import org.olat.core.commons.services.image.Size;
import org.olat.core.commons.services.thumbnail.CannotGenerateThumbnailException;
import org.olat.core.commons.services.thumbnail.FinalSize;
import org.olat.core.commons.services.thumbnail.ThumbnailSPI;
import org.olat.core.util.vfs.VFSLeaf;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 
 * Description:<br>
 * Generate a thumbnail from the first page of a PDF
 * 
 * <P>
 * Initial Date:  30 mar. 2010 <br>
 * @author srosse, stephane.rosse@frentix.com
 */ @Service
public class PDFToThumbnail implements ThumbnailSPI {

	private List<String> extensions = Collections.singletonList("pdf");
	
	@Autowired
	private ImageService imageHelper;
	
	public void setImageHelper(ImageService imageHelper) {
		this.imageHelper = imageHelper;
	}

	@Override
	public List<String> getExtensions() {
		return extensions;
	}

	@Override
	public FinalSize generateThumbnail(VFSLeaf pdfFile, VFSLeaf thumbnailFile, int maxWidth, int maxHeight, boolean fill)
			throws CannotGenerateThumbnailException {
		Size size = imageHelper.thumbnailPDF(pdfFile, thumbnailFile, maxWidth, maxHeight, fill);
		if(size != null) {
			return new FinalSize(size.getWidth(), size.getHeight());
		}
		return null;
	}
}