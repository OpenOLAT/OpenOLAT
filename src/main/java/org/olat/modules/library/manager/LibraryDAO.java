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
package org.olat.modules.library.manager;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.persistence.TemporalType;

import org.olat.basesecurity.IdentityRef;
import org.olat.core.commons.persistence.DB;
import org.olat.core.commons.persistence.PersistenceHelper;
import org.olat.core.commons.persistence.QueryBuilder;
import org.olat.core.commons.services.thumbnail.ThumbnailService;
import org.olat.core.commons.services.vfs.VFSMetadata;
import org.olat.core.util.FileUtils;
import org.olat.modules.library.model.CatalogItem;
import org.olat.modules.library.model.ItemRating;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 
 * Initial date: 24 nov. 2022<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
@Service
public class LibraryDAO {
	
	@Autowired
	private DB dbInstance;
	@Autowired
	private ThumbnailService thumbnailService;

	public CatalogItem getCatalogItemByUUID(String uuid, IdentityRef identity) {
		QueryBuilder sb = selectWithCommentAndRatingsSubSelect()
		 .and().append(" metadata.uuid=:uuid");

		List<Object[]> rows = dbInstance.getCurrentEntityManager()
			.createQuery(sb.toString(), Object[].class)
			.setFirstResult(0)
			.setMaxResults(1)
			.setParameter("uuid", uuid)
			.setParameter("identityKey", identity.getKey())
			.getResultList();
		if(rows == null || rows.isEmpty()) {
			return null;
		}
		return extractResults(rows.get(0));
	}
	
	public CatalogItem getCatalogItemByPath(String relativePath, String filename, IdentityRef identity) {
		QueryBuilder sb = selectWithCommentAndRatingsSubSelect()
		  .and().append("metadata.relativePath=:relativePath and metadata.filename=:filename");

		List<Object[]> rows = dbInstance.getCurrentEntityManager()
			.createQuery(sb.toString(), Object[].class)
			.setFirstResult(0)
			.setMaxResults(1)
			.setParameter("relativePath", relativePath)
			.setParameter("filename", filename)
			.setParameter("identityKey", identity.getKey())
			.getResultList();
		if(rows == null || rows.isEmpty()) {
			return null;
		}
		return extractResults(rows.get(0));
	}
	
	public List<CatalogItem> getMostRatedCatalogItemByUUID(String relativePath, int maxResults, IdentityRef identity) {
		QueryBuilder sb = selectWithCommentAndRatingsSubSelect()
		  .and().append(" metadata.relativePath like :relativePath and metadata.directory=false")
		  .append(" order by avgRating desc nulls last");
		
		List<Object[]> rows = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), Object[].class)
				.setFirstResult(0)
				.setMaxResults(maxResults)
				.setParameter("relativePath", relativePath + "%")
				.setParameter("identityKey", identity.getKey())
				.getResultList();
		return extractResults(rows);
	}
	
	public List<CatalogItem> getNewestCatalogItems(String relativePath, int maxResults, IdentityRef identity) {
		QueryBuilder sb = selectWithCommentAndRatingsSubSelect()
		  .and().append("metadata.relativePath like :relativePath and metadata.directory=false")
		  .append(" order by metadata.fileLastModified desc nulls last");
		
		List<Object[]> rows = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), Object[].class)
				.setFirstResult(0)
				.setMaxResults(maxResults)
				.setParameter("relativePath", relativePath + "%")
				.setParameter("identityKey", identity.getKey())
				.getResultList();
		return extractResults(rows);
	}
	
	public List<CatalogItem> getNewCatalogItem(String relativePath, Date from, IdentityRef identity) {
		QueryBuilder sb = selectWithCommentAndRatingsSubSelect()
		  .and().append(" metadata.relativePath like :relativePath and metadata.directory=false")
		  .and().append(" metadata.fileLastModified>=:date")
		  .append(" order by metadata.fileLastModified desc nulls last");
		
		List<Object[]> rows = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), Object[].class)
				.setFirstResult(0)
				.setParameter("relativePath", relativePath + "%")
				.setParameter("identityKey", identity.getKey())
				.setParameter("date", from, TemporalType.TIMESTAMP)
				.getResultList();
		return extractResults(rows);
	}
	
	public List<CatalogItem> getMostDownloadedCatalogItems(String relativePath, int maxResults, IdentityRef identity) {
		QueryBuilder sb = selectWithCommentAndRatingsSubSelect()
		  .and().append(" metadata.relativePath like :relativePath and metadata.directory=false")
		  .and().append(" metadata.downloadCount>0")
		  .append(" order by metadata.downloadCount desc nulls last");
		
		List<Object[]> rows = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), Object[].class)
				.setFirstResult(0)
				.setMaxResults(maxResults)
				.setParameter("relativePath", relativePath + "%")
				.setParameter("identityKey", identity.getKey())
				.getResultList();
		return extractResults(rows);
	}
	
	public List<CatalogItem> getCatalogItems(VFSMetadata parentMetadata, IdentityRef identity) {	
		QueryBuilder sb = selectWithCommentAndRatingsSubSelect()
		  .and().append("metadata.parent.key=:parentKey");

		List<Object[]> rows = dbInstance.getCurrentEntityManager()
			.createQuery(sb.toString(), Object[].class)
			.setParameter("parentKey", parentMetadata.getKey())
			.setParameter("identityKey", identity.getKey())
			.getResultList();
		return extractResults(rows);
	}
	
	/**
	 * @return the base select with sub-select to count comments and retrieve the ratings
	 */
	private QueryBuilder selectWithCommentAndRatingsSubSelect() {
		QueryBuilder sb = new QueryBuilder(256);
		sb.append("select metadata,")
		  .append(" (select count(comment.key) from usercomment as comment where")
		  .append("   comment.resSubPath=metadata.uuid")
		  .append(" ) as numOfComments,")
		  .append(" (select count(curating.key) from userrating as curating where")
		  .append("   curating.resSubPath=metadata.uuid")
		  .append(" ) as numOfRatings,")
		  .append(" (select avg(aurating.rating) from userrating as aurating where")
		  .append("   aurating.resSubPath=metadata.uuid")
		  .append(" ) as avgRating,")
		  .append(" (select max(myurating.rating) from userrating as myurating where")
		  .append("   myurating.resSubPath=metadata.uuid and myurating.creator.key=:identityKey")
		  .append(" ) as myRating,")
		  .append(" (select count(thumb.key) from vfsthumbnail thumb where")
		  .append("   thumb.owner.key=metadata.key")
		  .append(" ) as numOfThumbnails")
		  .append(" from filemetadata metadata")
		  .where().append("metadata.filename<>'.noFolderIndexing' and metadata.filename<>'.DS_Store' and metadata.directory=false");
		return sb;
	}
	
	private List<CatalogItem> extractResults(List<Object[]> rows) {
		List<CatalogItem> items = new ArrayList<>(rows.size());
		for(Object[] row:rows) {
			items.add(extractResults(row));
		}
		return items;
	}

	private CatalogItem extractResults(Object[] row) {
		VFSMetadata metadata = (VFSMetadata)row[0];
		long numOfComments = PersistenceHelper.extractPrimitiveLong(row, 1);
		long numOfRatings = PersistenceHelper.extractPrimitiveLong(row, 2);
		float avgRating = PersistenceHelper.extractPrimitiveFloat(row, 3);
		int myRating = PersistenceHelper.extractPrimitiveInt(row, 4);
		long numOfThumbnails = PersistenceHelper.extractPrimitiveLong(row, 5);
		
		boolean hasThumbnails;
		if(numOfThumbnails > 0) {
			hasThumbnails = true;
		} else if(Boolean.TRUE.equals(metadata.getCannotGenerateThumbnails())) {
			hasThumbnails = false;
		} else {
			String extension = FileUtils.getFileSuffix(metadata.getFilename());
			hasThumbnails = thumbnailService.isThumbnailPossible(extension);
		}
		ItemRating ratings = new ItemRating(numOfRatings, avgRating, myRating);
		return new CatalogItem(metadata, numOfComments, ratings, hasThumbnails);
	}
}
