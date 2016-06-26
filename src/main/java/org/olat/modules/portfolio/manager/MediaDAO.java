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
package org.olat.modules.portfolio.manager;

import java.util.Date;
import java.util.List;

import org.olat.basesecurity.IdentityRef;
import org.olat.core.commons.persistence.DB;
import org.olat.core.id.Identity;
import org.olat.modules.portfolio.Media;
import org.olat.modules.portfolio.MediaLight;
import org.olat.modules.portfolio.model.MediaImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 
 * Initial date: 17.06.2016<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
@Service
public class MediaDAO {
	
	@Autowired
	private DB dbInstance;
	
	public Media createMedia(String title, String description, String content, String type, String businessPath, int signature, Identity author) {
		MediaImpl media = new MediaImpl();
		media.setCreationDate(new Date());
		media.setCollectionDate(media.getCreationDate());
		media.setType(type);
		
		media.setTitle(title);
		media.setDescription(description);
		media.setContent(content);
		media.setSignature(signature);
		media.setBusinessPath(businessPath);
		
		media.setAuthor(author);
		dbInstance.getCurrentEntityManager().persist(media);
		return media;
	}
	
	public Media updateStoragePath(Media media, String storagePath, String rootFilename) {
		((MediaImpl)media).setStoragePath(storagePath);
		((MediaImpl)media).setRootFilename(rootFilename);
		return dbInstance.getCurrentEntityManager().merge(media);
	}
	
	public Media loadByKey(Long key) {
		StringBuilder sb = new StringBuilder();
		sb.append("select media from pfmedia as media")
		  .append(" inner join fetch media.author as author")
		  .append(" where media.key=:mediaKey");
		
		List<Media> medias = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), Media.class)
				.setParameter("mediaKey", key)
				.getResultList();
		return medias == null || medias.isEmpty() ? null : medias.get(0);
	}
	
	public List<MediaLight> loadByAuthor(IdentityRef author) {
		StringBuilder sb = new StringBuilder();
		sb.append("select media from pfmedia as media")
		  .append(" inner join fetch media.author as author")
		  .append(" where author.key=:authorKey");
		
		List<MediaLight> medias = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), MediaLight.class)
				.setParameter("authorKey", author.getKey())
				.getResultList();
		return medias;
	}

}
