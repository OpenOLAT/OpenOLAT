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
package org.olat.modules.portfolio.ui;

import java.util.List;

import jakarta.servlet.http.HttpServletRequest;

import org.olat.core.CoreSpringFactory;
import org.olat.core.commons.services.image.Size;
import org.olat.core.commons.services.vfs.VFSRepositoryService;
import org.olat.core.dispatcher.mapper.Mapper;
import org.olat.core.gui.media.MediaResource;
import org.olat.core.util.vfs.VFSLeaf;
import org.olat.core.util.vfs.VFSMediaResource;
import org.olat.modules.portfolio.ui.model.BinderRow;

/**
 * 
 * Initial date: 11 mars 2019<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class ImageMapper implements Mapper {

	private static final Size BACKGROUND_SIZE = new Size(400, 230, false);
	
	private final BindersDataModel binderModel;
	private final VFSRepositoryService vfsRepositoryService;
	
	public ImageMapper(BindersDataModel model) {
		this.binderModel = model;
		vfsRepositoryService = CoreSpringFactory.getImpl(VFSRepositoryService.class);
	}

	@Override
	public MediaResource handle(String relPath, HttpServletRequest request) {
		String row = relPath;
		if(row.startsWith("/")) {
			row = row.substring(1, row.length());
		}
		int index = row.indexOf('/');
		if(index > 0) {
			row = row.substring(0, index);
			Long key = Long.valueOf(row); 
			List<BinderRow> rows = binderModel.getObjects();
			for(BinderRow prow:rows) {
				if(key.equals(prow.getKey())) {
					VFSLeaf image = prow.getBackgroundImage();
					VFSLeaf thumbnail = vfsRepositoryService.getThumbnail(image, BACKGROUND_SIZE.getWidth(), BACKGROUND_SIZE.getHeight(), true);
					if(thumbnail != null) {
						image = thumbnail;
					}
					return new VFSMediaResource(image);
				}
			}
		}
		
		return null;
	}
}