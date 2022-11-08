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
package org.olat.core.commons.services.vfs.manager;

import java.util.Date;
import java.util.List;
import java.util.UUID;

import jakarta.persistence.TemporalType;
import jakarta.persistence.TypedQuery;

import org.hibernate.jpa.SpecHints;
import org.olat.core.commons.persistence.DB;
import org.olat.core.commons.services.vfs.VFSMetadata;
import org.olat.core.commons.services.vfs.VFSMetadataRef;
import org.olat.core.commons.services.vfs.model.VFSMetadataImpl;
import org.olat.core.id.Identity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 
 * Initial date: 11 mars 2019<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
@Service
public class VFSMetadataDAO {
	
	@Autowired
	private DB dbInstance;
	
	public VFSMetadata createMetadata(String uuid, String relativePath, String filename,
			Date fileLastModified, long size, boolean directory, String uri, String uriProtocol,
			VFSMetadata parent) {
		VFSMetadataImpl metadata = new VFSMetadataImpl();
		metadata.setCreationDate(new Date());
		metadata.setLastModified(metadata.getCreationDate());
		metadata.setUuid(uuid);
		metadata.setRelativePath(relativePath);
		metadata.setFilename(filename);
		metadata.setDirectory(directory);
		metadata.setFileLastModified(fileLastModified);
		metadata.setFileSize(size);
		metadata.setUri(uri);
		metadata.setProtocol(uriProtocol);
		metadata.setParent(parent);
		dbInstance.getCurrentEntityManager().persist(metadata);
		metadata.setMaterializedPathKeys(getMaterializedPathKeys((VFSMetadataImpl)parent, metadata));
		metadata = dbInstance.getCurrentEntityManager().merge(metadata);
		return metadata;
	}
	
	public VFSMetadata createMetadata(VFSMetadataImpl metadata, String relativePath, String filename,
			Date fileLastModified, long size, boolean directory, String uri, String uriProtocol,
			VFSMetadata parent) {

		metadata.setCreationDate(new Date());
		metadata.setLastModified(metadata.getCreationDate());
		if(metadata.getUuid() == null) {
			metadata.setUuid(UUID.randomUUID().toString());
		}
		metadata.setRelativePath(relativePath);
		metadata.setFilename(filename);
		metadata.setDirectory(directory);
		metadata.setFileLastModified(fileLastModified);
		metadata.setFileSize(size);
		metadata.setUri(uri);
		metadata.setProtocol(uriProtocol);
		metadata.setParent(parent);
		dbInstance.getCurrentEntityManager().persist(metadata);
		metadata.setMaterializedPathKeys(getMaterializedPathKeys((VFSMetadataImpl)parent, metadata));
		metadata = dbInstance.getCurrentEntityManager().merge(metadata);
		return metadata;
	}
	
	/**
	 * Calculate the materialized path from the parent element.
	 * 
	 * @param parent The parent element (can be null if the element is a root one)
	 * @param element The curriculum element
	 * @return The materialized path of the specified element
	 */
	protected String getMaterializedPathKeys(VFSMetadataImpl parent, VFSMetadataImpl element) {
		if(parent != null) {
			String parentPathOfKeys = parent.getMaterializedPathKeys();
			if(parentPathOfKeys == null || "/".equals(parentPathOfKeys)) {
				parentPathOfKeys = "";
			}
			return parentPathOfKeys + element.getKey() + "/";
		}
		return "/" + element.getKey() + "/";
	}
	
	public VFSMetadata getMetadata(String uuid) {
		StringBuilder sb = new StringBuilder(256);
		sb.append("select metadata from filemetadata metadata")
		  .append(" left join fetch metadata.fileInitializedBy as fileInitializedBy")
		  .append(" left join fetch fileInitializedBy.user as fileInitializedByUser")
		  .append(" left join fetch metadata.licenseType as licenseType")
		  .append(" where metadata.uuid=:uuid");
  
		List<VFSMetadata> metadata = dbInstance.getCurrentEntityManager()
			.createQuery(sb.toString(), VFSMetadata.class)
			.setParameter("uuid", uuid)
			.setFirstResult(0)
			.setMaxResults(1)
			.getResultList();
		return metadata == null || metadata.isEmpty() ? null : metadata.get(0);
	}
	
	public VFSMetadata getMetadata(String relativePath, String filename, boolean directory) {
		StringBuilder sb = new StringBuilder(256);
		sb.append("select metadata from filemetadata metadata")
		  .append(" left join fetch metadata.fileInitializedBy as fileInitializedBy")
		  .append(" left join fetch fileInitializedBy.user as fileInitializedByUser")
		  .append(" left join fetch metadata.licenseType as licenseType")
		  .append(" where metadata.filename=:filename and metadata.relativePath=:relativePath and metadata.directory=:directory");

		List<VFSMetadata> metadata = dbInstance.getCurrentEntityManager()
			.createQuery(sb.toString(), VFSMetadata.class)
			.setParameter("filename", filename)
			.setParameter("relativePath", relativePath)
			.setParameter("directory", Boolean.valueOf(directory))
			.setFirstResult(0)
			.setMaxResults(1)
			.getResultList();
		return metadata == null || metadata.isEmpty() ? null : metadata.get(0);
	}
	
	public VFSMetadata loadMetadata(Long metadataKey) {
		StringBuilder sb = new StringBuilder(256);
		sb.append("select metadata from filemetadata metadata")
		  .append(" left join fetch metadata.fileInitializedBy as fileInitializedBy")
		  .append(" left join fetch fileInitializedBy.user as fileInitializedByUser")
		  .append(" left join fetch metadata.licenseType as licenseType")
		  .append(" where metadata.key=:metadataKey");

		List<VFSMetadata> metadata = dbInstance.getCurrentEntityManager()
			.createQuery(sb.toString(), VFSMetadata.class)
			.setParameter("metadataKey", metadataKey)
			.setFirstResult(0)
			.setMaxResults(1)
			.getResultList();
		return metadata == null || metadata.isEmpty() ? null : metadata.get(0);
	}
	
	/**
	 * The method is sorted by key, and doesn't fetch any
	 * associated objects.
	 * 
	 * @param startPosition Start position (mandatory)
	 * @param maxResults Max. number of rows to return
	 * @return A list of metadata without any fetched objects
	 */
	public List<VFSMetadata> getMetadatas(int startPosition, int batchSize) {
		String query = "select metadata from filemetadata metadata order by key";
		return dbInstance.getCurrentEntityManager()
				.createQuery(query, VFSMetadata.class)
				.setFirstResult(startPosition)
				.setMaxResults(batchSize)
				.getResultList();
	}
	
	/**
	 * This is an exact match to find the direct children of a specific
	 * directory.
	 * 
	 * @param relativePath The relative path
	 * @return A list of metadata
	 */
	public List<VFSMetadata> getMetadatas(String relativePath) {
		StringBuilder sb = new StringBuilder(256);
		sb.append("select metadata from filemetadata metadata")
		  .append(" left join fetch metadata.fileInitializedBy as fileInitializedBy")
		  .append(" left join fetch fileInitializedBy.user as fileInitializedByUser")
		  .append(" left join fetch metadata.licenseType as licenseType")
		  .append(" where metadata.relativePath=:relativePath");

		return dbInstance.getCurrentEntityManager()
			.createQuery(sb.toString(), VFSMetadata.class)
			.setParameter("relativePath", relativePath)
			.getResultList();
	}
	
	/**
	 * This is an exact match to find the direct children of the specific
	 * directory described by its metadata.
	 * 
	 * @param relativePath The relative path
	 * @return A list of metadata
	 */
	public List<VFSMetadata> getMetadatas(VFSMetadataRef parentMetadata) {
		StringBuilder sb = new StringBuilder(256);
		sb.append("select metadata from filemetadata metadata")
		  .append(" left join fetch metadata.fileInitializedBy as fileInitializedBy")
		  .append(" left join fetch fileInitializedBy.user as fileInitializedByUser")
		  .append(" left join fetch metadata.licenseType as licenseType")
		  .append(" where metadata.parent.key=:parentKey");

		return dbInstance.getCurrentEntityManager()
			.createQuery(sb.toString(), VFSMetadata.class)
			.setParameter("parentKey", parentMetadata.getKey())
			.getResultList();
	}
	
	public List<VFSMetadata> getMetadatasOnly(VFSMetadataRef parentMetadata) {
		return dbInstance.getCurrentEntityManager()
			.createNamedQuery("metadataOnlyByParent", VFSMetadata.class)
			.setParameter("parentKey", parentMetadata.getKey())
			.getResultList();
	}
	
	public List<VFSMetadata> getExpiredMetadatas(Date reference) {
		StringBuilder sb = new StringBuilder(256);
		sb.append("select metadata from filemetadata metadata")
		  .append(" left join fetch metadata.fileInitializedBy as fileInitializedBy")
		  .append(" left join fetch fileInitializedBy.user as fileInitializedByUser")
		  .append(" left join fetch metadata.licenseType as licenseType")
		  .append(" where metadata.expirationDate<=:referenceDate");

		return dbInstance.getCurrentEntityManager()
			.createQuery(sb.toString(), VFSMetadata.class)
			.setParameter("referenceDate", reference, TemporalType.TIMESTAMP)
			.getResultList();
	}
	
	/**
	 * This is an exact match to find the direct children of a specific
	 * directory.
	 * 
	 * @param relativePath The relative path
	 * @return A list of metadata
	 */
	public List<VFSMetadata> getMostDownloaded(String relativePath, int maxResult) {
		StringBuilder sb = new StringBuilder(256);
		sb.append("select metadata from filemetadata metadata")
		  .append(" left join fetch metadata.fileInitializedBy as fileInitializedBy")
		  .append(" left join fetch fileInitializedBy.user as fileInitializedByUser")
		  .append(" left join fetch metadata.licenseType as licenseType")
		  .append(" where metadata.relativePath like :relativePath")
		  .append(" and metadata.directory=false")
		  .append(" order by metadata.downloadCount desc nulls last");

		return dbInstance.getCurrentEntityManager()
			.createQuery(sb.toString(), VFSMetadata.class)
			.setParameter("relativePath", relativePath + "%")
			.setFirstResult(0)
			.setMaxResults(maxResult)
			.getResultList();
	}
	
	/**
	 * This is an exact match to find the direct children of a specific
	 * directory.
	 * 
	 * @param relativePath The relative path
	 * @return A list of metadata
	 */
	public List<VFSMetadata> getNewest(String relativePath, int maxResult) {
		StringBuilder sb = new StringBuilder(256);
		sb.append("select metadata from filemetadata metadata")
		  .append(" left join fetch metadata.fileInitializedBy as fileInitializedBy")
		  .append(" left join fetch fileInitializedBy.user as fileInitializedByUser")
		  .append(" left join fetch metadata.licenseType as licenseType")
		  .append(" where metadata.relativePath like :relativePath")
		  .append(" and metadata.directory=false")
		  .append(" order by metadata.fileLastModified desc nulls last");

		return dbInstance.getCurrentEntityManager()
			.createQuery(sb.toString(), VFSMetadata.class)
			.setParameter("relativePath", relativePath + "%")
			.setFirstResult(0)
			.setMaxResults(maxResult)
			.getResultList();
	}
	
	public List<VFSMetadata> getNewest(String relativePath, Date date) {
		StringBuilder sb = new StringBuilder(256);
		sb.append("select metadata from filemetadata metadata")
		  .append(" left join fetch metadata.fileInitializedBy as fileInitializedBy")
		  .append(" left join fetch fileInitializedBy.user as fileInitializedByUser")
		  .append(" left join fetch metadata.licenseType as licenseType")
		  .append(" where metadata.relativePath like :relativePath")
		  .append(" and metadata.fileLastModified>=:date")
		  .append(" and metadata.directory=false")
		  .append(" order by metadata.fileLastModified desc nulls last");

		return dbInstance.getCurrentEntityManager()
			.createQuery(sb.toString(), VFSMetadata.class)
			.setParameter("relativePath", relativePath + "%")
			.setParameter("date", date, TemporalType.TIMESTAMP)
			.getResultList();
	}
	
	public void increaseDownloadCount(String relativePath, String filename) {
		String updateQuery = "update vfsmetadatadownloadcount set downloadCount=downloadCount+1 where filename=:filename and relativePath=:relativePath";
		dbInstance.getCurrentEntityManager()
			.createQuery(updateQuery)
			.setParameter("filename", filename)
			.setParameter("relativePath", relativePath)
			.setHint(SpecHints.HINT_SPEC_QUERY_TIMEOUT, 1000)
			.executeUpdate();
	}
	
	public void setDownloadCount(VFSMetadata metadata, int count) {
		String updateQuery = "update vfsmetadatadownloadcount set downloadCount=:count where key=:key";
		dbInstance.getCurrentEntityManager()
			.createQuery(updateQuery)
			.setParameter("count", count)
			.setParameter("key", metadata.getKey())
			.executeUpdate();
	}
	
	/**
	 * Update existing files only.
	 * 
	 * @param fileSize The new file size (mandatory)
	 * @param lastModified The modification date (mandatory)
	 * @param initializedBy The identity who saved the file initially
	 * @param lastModifiedBy The identity who saved the file
	 * @param relativePath The path to the file
	 * @param filename The name of the file
	 */
	public void updateMetadata(long fileSize, Date lastModified, Identity initializedBy, Identity lastModifiedBy, String relativePath, String filename) {
		String updateQuery = "update vfsmetadatafilesaved set fileLastModified=:lastModified, fileInitializedBy=:initializedBy, fileLastModifiedBy=:lastModifiedBy, fileSize=:fileSize, deleted=false where filename=:filename and relativePath=:relativePath";
		dbInstance.getCurrentEntityManager()
			.createQuery(updateQuery)
			.setParameter("filename", filename)
			.setParameter("relativePath", relativePath)
			.setParameter("fileSize", fileSize)
			.setParameter("lastModified", lastModified)
			.setParameter("lastModifiedBy", lastModifiedBy)
			.setParameter("initializedBy", initializedBy)
			.executeUpdate();
	}
	
	public void updateMetadata(long fileSize, Date lastModified, String relativePath, String filename) {
		String updateQuery = "update vfsmetadatafilesaved set fileLastModified=:lastModified, fileSize=:fileSize, deleted=false where filename=:filename and relativePath=:relativePath";
		dbInstance.getCurrentEntityManager()
			.createQuery(updateQuery)
			.setParameter("filename", filename)
			.setParameter("relativePath", relativePath)
			.setParameter("fileSize", fileSize)
			.setParameter("lastModified", lastModified)
			.executeUpdate();
	}
	
	public VFSMetadata updateMetadata(VFSMetadata metadata) {
		((VFSMetadataImpl)metadata).setLastModified(new Date());
		return dbInstance.getCurrentEntityManager().merge(metadata);
	}
	
	public void removeMetadata(VFSMetadata metadata) {
		dbInstance.getCurrentEntityManager().remove(metadata);
	}
	
	/**
	 * Return the largest files on the system
	 * 
	 * @param maxResult
	 * @return
	 */
	public List<VFSMetadata> getLargest(int maxResult, 
			Date createdAtNewer, Date createdAtOlder, 
			Date editedAtNewer, Date editedAtOlder, 
			Date lockedAtNewer, Date lockedAtOlder,
			Boolean deleted, Boolean locked,
			Integer downloadCount, Long revisionCount, 
			Integer size) {
		StringBuilder sb = new StringBuilder(256);
		sb.append("select metadata from filemetadata metadata")
		.append(" left join fetch metadata.fileInitializedBy as fileInitializedBy")
		.append(" left join fetch fileInitializedBy.user as fileInitializedByUser")
		.append(" left join fetch metadata.licenseType as licenseType")
		.append(" where metadata.directory = false");
		if(createdAtNewer != null) {
			sb.append(" and metadata.creationDate>=:createdAtNewer");
		}
		if(createdAtOlder != null) {
			sb.append(" and metadata.creationDate<=:createdAtOlder");
		}
		if(editedAtNewer != null) {
			sb.append(" and metadata.lastModified>=:editedAtNewer");
		}
		if(editedAtOlder != null) {
			sb.append(" and metadata.lastModified<=:editedAtOlder");
		}
		if(lockedAtNewer != null) {
			sb.append(" and metadata.lockedDate>=:lockedAtNewer");
		}
		if(lockedAtOlder != null) {
			sb.append(" and metadata.lockedDate<=:lockedAtOlder");
		}
		if(deleted != null) {
			sb.append(" and metadata.deleted=:trashed");
		}
		if(locked != null) {
			sb.append(" and metadata.locked=:locked");
		}
		if(downloadCount > 0) {
			sb.append(" and metadata.downloadCount>=:downloadCount");
		}
		if(revisionCount > 0) {
			sb.append(" and metadata.revisionNr>=:revisionCount");
		}
		if(size > 0) {
			sb.append(" and metadata.fileSize>=:size");
		}
		sb.append(" order by metadata.fileSize desc nulls last");

		TypedQuery<VFSMetadata> query = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), VFSMetadata.class);

		if(createdAtNewer != null) {
			query.setParameter("createdAtNewer", createdAtNewer);
		}
		if(createdAtOlder != null) {
			query.setParameter("createdAtOlder", createdAtOlder);
		}
		if(editedAtNewer != null) {
			query.setParameter("editedAtNewer", editedAtNewer);
		}
		if(editedAtOlder != null) {
			query.setParameter("editedAtOlder", editedAtOlder);
		}
		if(lockedAtNewer != null) {
			query.setParameter("lockedAtNewer", lockedAtNewer);
		}
		if(lockedAtOlder != null) {
			query.setParameter("lockedAtOlder", lockedAtOlder);
		}
		if(deleted != null) {
			query.setParameter("trashed", deleted);
		}
		if(locked != null) {
			query.setParameter("locked", locked);
		}
		if(downloadCount > 0) {
			query.setParameter("downloadCount", downloadCount);
		}
		if(revisionCount > 0) {
			query.setParameter("revisionCount", revisionCount);
		}
		if(size > 0) {
			query.setParameter("size", Long.valueOf(size));
		}

		return query.setFirstResult(0)
				.setMaxResults(maxResult)		
				.getResultList();
	}

	public void setTranscodingStatus(Long vfsMetadataKey, int transcodingStatus) {
		String updateQuery = "update filemetadata set transcodingStatus=:transcodingStatus where key=:key";
		dbInstance.getCurrentEntityManager()
				.createQuery(updateQuery)
				.setParameter("transcodingStatus", transcodingStatus)
				.setParameter("key", vfsMetadataKey)
				.executeUpdate();
	}

	public List<VFSMetadata> getMetadatasInNeedForTranscoding() {
		String query = "select meta from filemetadata as meta" +
				" where deleted = false and transcodingStatus = " + VFSMetadata.TRANSCODING_STATUS_WAITING +
				" order by meta.creationDate asc, meta.id asc";
		return dbInstance.getCurrentEntityManager().createQuery(query, VFSMetadata.class).getResultList();
	}

	public List<VFSMetadata> getMetadatasWithUnresolvedTranscodingStatus() {
		String query = "select meta from filemetadata as meta" +
				" where transcodingStatus is not null and transcodingStatus <> " + VFSMetadata.TRANSCODING_STATUS_DONE +
				" and meta.deleted = false order by meta.creationDate asc, meta.id asc";
		return dbInstance.getCurrentEntityManager().createQuery(query, VFSMetadata.class).getResultList();
	}
}
