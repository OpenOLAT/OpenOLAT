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
package org.olat.modules.cemedia.manager;

import java.util.ArrayList;
import java.util.List;

import org.olat.basesecurity.IdentityRef;
import org.olat.core.id.OLATResourceable;
import org.olat.core.util.resource.OresHelper;
import org.olat.modules.ceditor.Category;
import org.olat.modules.ceditor.manager.CategoryDAO;
import org.olat.modules.ceditor.model.jpa.CategoryLight;
import org.olat.modules.cemedia.Media;
import org.olat.modules.cemedia.MediaHandler;
import org.olat.modules.cemedia.MediaLight;
import org.olat.modules.cemedia.MediaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 
 * Initial date: 25 mai 2023<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
@Service
public class MediaServiceImpl implements MediaService {

	@Autowired
	private MediaDAO mediaDao;
	@Autowired
	private CategoryDAO categoryDao;

	@Autowired
	private List<MediaHandler> mediaHandlers;
	
	@Override
	public List<Category> getCategories(Media media) {
		OLATResourceable ores = OresHelper.createOLATResourceableInstance(Media.class, media.getKey());
		return categoryDao.getCategories(ores);
	}

	@Override
	public List<CategoryLight> getMediaCategories(IdentityRef owner) {
		return categoryDao.getMediaCategories(owner);
	}
	
	@Override
	public MediaHandler getMediaHandler(String type) {
		if(mediaHandlers != null) {
			for(MediaHandler handler:mediaHandlers) {
				if(type.equals(handler.getType())) {
					return handler;
				}
			}
		}
		return null;
	}

	@Override
	public List<MediaHandler> getMediaHandlers() {
		return new ArrayList<>(mediaHandlers);
	}

	@Override
	public Media updateMedia(Media media) {
		return mediaDao.update(media);
	}

	@Override
	public void deleteMedia(Media media) {
		mediaDao.deleteMedia(media);
	}

	@Override
	public void updateCategories(Media media, List<String> categories) {
		OLATResourceable ores = OresHelper.createOLATResourceableInstance(Media.class, media.getKey());
		categoryDao.updateCategories(ores, categories);
	}

	@Override
	public List<MediaLight> searchOwnedMedias(IdentityRef author, String searchString, List<String> tagNames) {
		return mediaDao.searchByAuthor(author, searchString, tagNames);
	}

	@Override
	public Media getMediaByKey(Long key) {
		return mediaDao.loadByKey(key);
	}

}
