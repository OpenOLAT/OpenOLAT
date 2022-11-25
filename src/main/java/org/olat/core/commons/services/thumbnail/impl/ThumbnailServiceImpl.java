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

import java.util.List;

import org.olat.core.commons.services.thumbnail.CannotGenerateThumbnailException;
import org.olat.core.commons.services.thumbnail.FinalSize;
import org.olat.core.commons.services.thumbnail.ThumbnailSPI;
import org.olat.core.commons.services.thumbnail.ThumbnailService;
import org.olat.core.util.FileUtils;
import org.olat.core.util.StringHelper;
import org.olat.core.util.vfs.VFSLeaf;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 
 * Description:<br>
 * The implementation delegate all the job to the different SPIs
 * 
 * <P>
 * Initial Date:  30 mar. 2010 <br>
 * @author srosse, stephane.rosse@frentix.com
 */
@Service
public class ThumbnailServiceImpl implements ThumbnailService {

	@Autowired
	private List<ThumbnailSPI> thumbnailSPIes;
	
	public ThumbnailServiceImpl() {
		//
	}
	
	public List<ThumbnailSPI> getThumbnailSPIes() {
		return thumbnailSPIes;
	}

	public void setThumbnailSPIes(List<ThumbnailSPI> thumbnailSPIes) {
		this.thumbnailSPIes.addAll(thumbnailSPIes);
	}

	public void addThumbnailSPI(ThumbnailSPI thumbnailSPI) {
		this.thumbnailSPIes.add(thumbnailSPI);
	}

	@Override
	public boolean isThumbnailPossible(VFSLeaf file) {
		String extension = FileUtils.getFileSuffix(file.getName());
		return isThumbnailPossible(extension);
	}
	
	public boolean isThumbnailPossible(String extension) {
		if(StringHelper.containsNonWhitespace(extension)) {
			for(ThumbnailSPI thumbnailSPI : thumbnailSPIes) {
				if(thumbnailSPI.getExtensions().contains(extension)) {
					return true;
				}
			}
		}
		return false;
	}

	@Override
	public FinalSize generateThumbnail(VFSLeaf file, VFSLeaf thumbnailFile, int maxWidth, int maxHeight, boolean fill)
	throws CannotGenerateThumbnailException {
		String extension = FileUtils.getFileSuffix(file.getName()).toLowerCase();
		for(ThumbnailSPI thumbnailSPI : thumbnailSPIes) {
			if(thumbnailSPI.getExtensions().contains(extension)) {
				FinalSize finalSize = thumbnailSPI.generateThumbnail(file, thumbnailFile, maxWidth, maxHeight, fill);
				if(finalSize != null) {
					return finalSize;
				}//else, try to find an other SPI which can thumbnailed this file
			}
		}
		return null;
	}	
}
