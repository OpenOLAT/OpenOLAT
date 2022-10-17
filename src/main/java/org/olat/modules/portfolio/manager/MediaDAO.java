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

import static org.olat.core.commons.persistence.PersistenceHelper.appendFuzzyLike;
import static org.olat.core.commons.persistence.PersistenceHelper.makeFuzzyQueryString;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import jakarta.persistence.TypedQuery;

import org.olat.basesecurity.IdentityRef;
import org.olat.core.commons.persistence.DB;
import org.olat.core.id.Identity;
import org.olat.core.util.StringHelper;
import org.olat.core.util.vfs.VFSContainer;
import org.olat.core.util.vfs.VFSItem;
import org.olat.core.util.vfs.VFSLeaf;
import org.olat.modules.portfolio.Media;
import org.olat.modules.portfolio.MediaLight;
import org.olat.modules.portfolio.model.BinderPageUsage;
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
	@Autowired
	private PortfolioFileStorage fileStorage;
	
	/**
	 * 
	 * @param title
	 * @param description
	 * @param content
	 * @param type
	 * @param businessPath
	 * @param referenceId Some external id
	 * @param signature
	 * @param author
	 * @return
	 */
	public Media createMedia(String title, String description, String content, String type, String businessPath,
			String referenceId, int signature, Identity author) {
		MediaImpl media = new MediaImpl();
		media.setCreationDate(new Date());
		media.setCollectionDate(media.getCreationDate());
		media.setType(type);
		media.setReferenceId(referenceId);
		
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
	
	public Media update(Media media) {
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
	
	public List<MediaLight> searchByAuthor(IdentityRef author, String searchString, List<String> tagNames) {
		StringBuilder sb = new StringBuilder();
		sb.append("select media from pfmedia as media")
		  .append(" inner join fetch media.author as author")
		  .append(" where author.key=:authorKey");
		if(StringHelper.containsNonWhitespace(searchString)) {
			searchString = makeFuzzyQueryString(searchString);
			sb.append(" and (");
			appendFuzzyLike(sb, "media.title", "searchString", dbInstance.getDbVendor());
			sb.append(" or ");
			appendFuzzyLike(sb, "media.description", "searchString", dbInstance.getDbVendor());
			sb.append(")");
		}
		if(tagNames != null && tagNames.size() > 0) {
			sb.append(" and exists(select rel.key from pfcategoryrelation as rel")
			  .append("  inner join rel.category as category")
			  .append("  where category.name in (:tagNames) and rel.resId=media.key and rel.resName='Media'")
			  .append(" )");
		}
		
		TypedQuery<MediaLight> query = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), MediaLight.class)
				.setParameter("authorKey", author.getKey());
		if(StringHelper.containsNonWhitespace(searchString)) {
			query.setParameter("searchString", searchString.toLowerCase());
		}
		if(tagNames != null && tagNames.size() > 0) {
			query.setParameter("tagNames", tagNames);
		}
		return query.getResultList();
	}
	
	public List<Media> load(IdentityRef author) {
		String query = "select media from pfmedia as media where media.author.key=:authorKey";
		return dbInstance.getCurrentEntityManager()
				.createQuery(query, Media.class)
				.setParameter("authorKey", author.getKey())
				.getResultList();
	}
	
	public List<BinderPageUsage> usedInBinders(MediaLight media) {
		StringBuilder sb = new StringBuilder();
		sb.append("select binder.key, binder.title, page.key, page.title, page.status")
		  .append(" from pfpage as page")
		  .append(" inner join page.body as pageBody")
		  .append(" inner join pageBody.parts as bodyPart")
		  .append(" left join page.section as section")
		  .append(" left join section.binder as binder")
		  .append(" where bodyPart.media.key=:mediaKey");
		
		List<Object[]> objects = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), Object[].class)
				.setParameter("mediaKey", media.getKey())
				.getResultList();
		List<BinderPageUsage> usage = new ArrayList<>(objects.size());
		for(Object[] object:objects) {
			Long binderKey = (Long)object[0];
			String binderTitle = (String)object[1];
			Long pageKey = (Long)object[2];
			String pageTitle = (String)object[3];
			String pageStatus = (String)object[4];
			usage.add(new BinderPageUsage(binderKey, binderTitle, pageKey, pageTitle, pageStatus));
		}
		return usage;
	}
	
	public boolean isUsed(MediaLight media) {
		StringBuilder sb = new StringBuilder();
		sb.append("select page.key")
		  .append(" from pfpage as page")
		  .append(" inner join page.body as pageBody")
		  .append(" inner join pageBody.parts as bodyPart")
		  .append(" where bodyPart.media.key=:mediaKey");
		
		List<Long> pageKey = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), Long.class)
				.setParameter("mediaKey", media.getKey())
				.setFirstResult(0)
				.setMaxResults(1)
				.getResultList();
		return pageKey != null && !pageKey.isEmpty() && pageKey.get(0) != null;
	}
	
	public void deleteMedia(Media media) {
		if(StringHelper.containsNonWhitespace(media.getRootFilename())) {
			VFSContainer container = fileStorage.getMediaContainer(media);
			VFSItem item = container.resolve(media.getRootFilename());
			if(item instanceof VFSLeaf) {
				((VFSLeaf)item).delete();
			}
		}
		Media reloadedMedia = dbInstance.getCurrentEntityManager().getReference(MediaImpl.class, media.getKey());
		dbInstance.getCurrentEntityManager().remove(reloadedMedia);
	}

}
