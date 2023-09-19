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
package org.olat.core.commons.services.doceditor.drawio.manager;

import java.util.List;

import org.olat.core.commons.services.doceditor.drawio.DrawioConversionService;
import org.olat.core.commons.services.doceditor.drawio.DrawioModule;
import org.olat.core.commons.services.thumbnail.CannotGenerateThumbnailException;
import org.olat.core.commons.services.thumbnail.FinalSize;
import org.olat.core.commons.services.thumbnail.ThumbnailSPI;
import org.olat.core.util.vfs.VFSLeaf;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 
 * Initial date: 10 Aug 2021<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
@Service
public class DrawioThumbnailSPI implements ThumbnailSPI {

	private static final List<String> PNG_OUTPUT_EXTENSION = List.of("drawio", "dwb");
	
	@Autowired
	private DrawioModule drawioModule;
	@Autowired
	private DrawioConversionService drawioConversionService;
	
	@Override
	public List<String> getExtensions() {
		if (drawioModule.isEnabled() && drawioModule.isThumbnailEnabled()) {
			return PNG_OUTPUT_EXTENSION;
		}
		return List.of();
	}

	@Override
	public FinalSize generateThumbnail(VFSLeaf file, VFSLeaf thumbnailFile, int maxWidth, int maxHeight, boolean fill)
			throws CannotGenerateThumbnailException {
		return drawioConversionService.createThumbnail(file, thumbnailFile, maxWidth, maxHeight);
	}

}
