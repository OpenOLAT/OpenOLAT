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
package org.olat.modules.cemedia;

import java.util.List;

import org.olat.basesecurity.IdentityRef;
import org.olat.modules.ceditor.Category;
import org.olat.modules.ceditor.model.jpa.CategoryLight;

/**
 * 
 * 
 * Initial date: 25 mai 2023<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public interface MediaService {
	
	/**
	 * The list of categories of the specified media.
	 * @param media
	 * @return A list of categories
	 */
	List<Category> getCategories(Media media);
	
	List<CategoryLight> getMediaCategories(IdentityRef owner);
	
	MediaHandler getMediaHandler(String type);
	
	List<MediaHandler> getMediaHandlers();
	
	Media getMediaByKey(Long key);
	
	List<MediaLight> searchOwnedMedias(IdentityRef author, String searchString, List<String> tagNames);
	
	Media updateMedia(Media media);
	
	void updateCategories(Media media, List<String> categories);
	
	void deleteMedia(Media media);

}
