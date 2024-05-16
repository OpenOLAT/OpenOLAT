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

import java.util.Date;
import java.util.List;

import org.olat.core.commons.persistence.DB;
import org.olat.core.commons.persistence.QueryBuilder;
import org.olat.core.id.Identity;
import org.olat.modules.ceditor.PagePart;
import org.olat.modules.ceditor.model.jpa.GalleryPart;
import org.olat.modules.ceditor.model.jpa.ImageComparisonPart;
import org.olat.modules.cemedia.Media;
import org.olat.modules.cemedia.MediaToPagePart;
import org.olat.modules.cemedia.MediaVersion;
import org.olat.modules.cemedia.model.MediaToPagePartImpl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Initial date: 2024-04-19<br>
 *
 * @author cpfranger, christoph.pfranger@frentix.com, <a href="https://www.frentix.com">https://www.frentix.com</a>
 */
@Service
public class MediaToPagePartDAO {

	@Autowired
	private DB dbInstance;

	public GalleryPart persistRelation(GalleryPart galleryPart, Media media) {
		return persistRelation(galleryPart, media, null, null);
	}

	public GalleryPart persistRelation(GalleryPart galleryPart, Media media, MediaVersion mediaVersion, Identity identity) {
		MediaToPagePartImpl relation = new MediaToPagePartImpl();
		relation.setCreationDate(new Date());
		relation.setLastModified(relation.getCreationDate());
		relation.setMedia(media);
		relation.setMediaVersion(mediaVersion);
		relation.setIdentity(identity);
		relation.setPagePart(galleryPart);
		galleryPart.getRelations().size();
		galleryPart.getRelations().add(relation);
		dbInstance.getCurrentEntityManager().persist(relation);
		return dbInstance.getCurrentEntityManager().merge(galleryPart);
	}

	public ImageComparisonPart persistRelation(ImageComparisonPart imageComparisonPart, Media media, MediaVersion mediaVersion, Identity identity) {
		PagePart pagePart = persistRelation(imageComparisonPart, imageComparisonPart.getRelations(), media, mediaVersion, identity);
		return (ImageComparisonPart) pagePart;
	}

	private PagePart persistRelation(PagePart pagePart, List<MediaToPagePart> relations, Media media, MediaVersion mediaVersion, Identity identity) {
		MediaToPagePartImpl relation = new MediaToPagePartImpl();
		relation.setCreationDate(new Date());
		relation.setLastModified(relation.getCreationDate());
		relation.setMedia(media);
		relation.setMediaVersion(mediaVersion);
		relation.setIdentity(identity);
		relation.setPagePart(pagePart);
		relations.size();
		relations.add(relation);
		dbInstance.getCurrentEntityManager().persist(relation);
		return dbInstance.getCurrentEntityManager().merge(pagePart);
	}


	public void move(GalleryPart galleryPart, MediaToPagePart relation, boolean up) {
		galleryPart.getRelations().size();

		int index = galleryPart.getRelations().indexOf(relation);
		if (index < 0) {
			galleryPart.getRelations().add(0, relation);
		} else if (up && index > 0) {
			galleryPart.getRelations().remove(index);
			galleryPart.getRelations().add(index - 1, relation);
		} else if (!up && (index < (galleryPart.getRelations().size() - 1))) {
			galleryPart.getRelations().remove(index);
			galleryPart.getRelations().add(index + 1, relation);
		}
		galleryPart.setLastModified(new Date());
		dbInstance.getCurrentEntityManager().merge(galleryPart);
	}

	public List<PagePart> loadPageParts(Media media) {
		QueryBuilder queryBuilder = new QueryBuilder();
		queryBuilder
				.append("select pagePart from mediatopagepart relation")
				.append("  inner join relation.pagePart as pagePart")
				.where()
				.append("  relation.media.key=:mediaKey")
				.append("  order by relation.pos");

		return dbInstance.getCurrentEntityManager()
				.createQuery(queryBuilder.toString(), PagePart.class)
				.setParameter("mediaKey", media.getKey()).getResultList();
	}

	public List<MediaToPagePart> loadRelations(Media media) {
		QueryBuilder queryBuilder = new QueryBuilder();
		queryBuilder
				.append("select relation from mediatopagepart relation")
				.append("  inner join fetch relation.pagePart as pagePart")
				.where()
				.append("  relation.media.key=:mediaKey")
				.append("  order by relation.pos");

		return dbInstance.getCurrentEntityManager()
				.createQuery(queryBuilder.toString(), MediaToPagePart.class)
				.setParameter("mediaKey", media.getKey())
				.getResultList();
	}

	public List<MediaToPagePart> loadRelations(PagePart pagePart) {
		QueryBuilder queryBuilder = new QueryBuilder();
		queryBuilder
				.append("select relation from mediatopagepart relation")
				.append("  inner join fetch relation.media as media")
				.where()
				.append("  relation.pagePart.key=:pagePartKey")
				.append("  order by relation.pos");

		return dbInstance.getCurrentEntityManager()
				.createQuery(queryBuilder.toString(), MediaToPagePart.class)
				.setParameter("pagePartKey", pagePart.getKey())
				.getResultList();
	}

	public List<Media> loadMediaItems(PagePart pagePart) {
		QueryBuilder queryBuilder = new QueryBuilder();
		queryBuilder
				.append("select media from mediatopagepart relation")
				.append("  inner join relation.media as media")
				.where()
				.append("  relation.pagePart.key=:pagePartKey")
				.append("  order by relation.pos");

		return dbInstance.getCurrentEntityManager()
				.createQuery(queryBuilder.toString(), Media.class)
				.setParameter("pagePartKey", pagePart.getKey())
				.getResultList();
	}

	public int deleteRelations(Media media) {
		QueryBuilder queryBuilder = new QueryBuilder();
		queryBuilder
				.append("delete from mediatopagepart relation")
				.where()
				.append("  relation.media.key=:mediaKey");
		return dbInstance.getCurrentEntityManager()
				.createQuery(queryBuilder.toString())
				.setParameter("mediaKey", media.getKey())
				.executeUpdate();
	}

	public void deleteRelation(MediaToPagePart relation) {
		dbInstance.getCurrentEntityManager().remove(relation);
	}

	public MediaToPagePart loadRelation(Long key) {
		return dbInstance.getCurrentEntityManager().find(MediaToPagePartImpl.class, key);
	}

	public MediaToPagePart updateMediaVersion(MediaToPagePart relation, MediaVersion mediaVersion, Identity identity) {
		MediaToPagePartImpl mediaToPagePart = (MediaToPagePartImpl) relation;
		mediaToPagePart.setLastModified(new Date());
		mediaToPagePart.setMediaVersion(mediaVersion);
		mediaToPagePart.setIdentity(identity);
		return dbInstance.getCurrentEntityManager().merge(mediaToPagePart);
	}

	public MediaToPagePart updateMedia(MediaToPagePart relation, Media media) {
		MediaToPagePartImpl mediaToPagePart = (MediaToPagePartImpl) relation;
		mediaToPagePart.setLastModified(new Date());
		mediaToPagePart.setMedia(media);
		return dbInstance.getCurrentEntityManager().merge(mediaToPagePart);
	}
}
