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
import org.olat.modules.ceditor.PagePart;
import org.olat.modules.cemedia.Media;
import org.olat.modules.cemedia.MediaToPagePart;
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

	public MediaToPagePartImpl createRelation(Media media, PagePart pagePart) {
		MediaToPagePartImpl relation = new MediaToPagePartImpl();
		relation.setCreationDate(new Date());
		relation.setMedia(media);
		relation.setPagePart(pagePart);
		dbInstance.getCurrentEntityManager().persist(relation);
		return relation;
	}

	public List<PagePart> loadPageParts(Media media) {
		QueryBuilder queryBuilder = new QueryBuilder();
		queryBuilder
				.append("select pagePart from mediatopagepart m2pp")
				.append("  inner join m2pp.pagePart as pagePart")
				.where()
				.append("  m2pp.media.key=:mediaKey");

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
				.append("  relation.media.key=:mediaKey");

		return dbInstance.getCurrentEntityManager()
				.createQuery(queryBuilder.toString(), MediaToPagePart.class)
				.setParameter("mediaKey", media.getKey())
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
}
