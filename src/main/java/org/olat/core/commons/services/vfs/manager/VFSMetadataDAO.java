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

import javax.persistence.TemporalType;

import org.olat.core.commons.persistence.DB;
import org.olat.core.commons.services.vfs.VFSMetadata;
import org.olat.core.commons.services.vfs.VFSMetadataRef;
import org.olat.core.commons.services.vfs.model.VFSMetadataImpl;
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
	private String getMaterializedPathKeys(VFSMetadataImpl parent, VFSMetadataImpl element) {
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
		  .append(" left join fetch metadata.author as author")
		  .append(" left join fetch author.user as authorUser")
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
		  .append(" left join fetch metadata.author as author")
		  .append(" left join fetch author.user as authorUser")
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
		  .append(" left join fetch metadata.author as author")
		  .append(" left join fetch author.user as authorUser")
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
	 * This is an exact match to find the direct children of a specific
	 * directory.
	 * 
	 * @param relativePath The relative path
	 * @return A list of metadata
	 */
	public List<VFSMetadata> getMetadatas(String relativePath) {
		StringBuilder sb = new StringBuilder(256);
		sb.append("select metadata from filemetadata metadata")
		  .append(" left join fetch metadata.author as author")
		  .append(" left join fetch author.user as authorUser")
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
		  .append(" left join fetch metadata.author as author")
		  .append(" left join fetch author.user as authorUser")
		  .append(" left join fetch metadata.licenseType as licenseType")
		  .append(" where metadata.parent.key=:parentKey");

		return dbInstance.getCurrentEntityManager()
			.createQuery(sb.toString(), VFSMetadata.class)
			.setParameter("parentKey", parentMetadata.getKey())
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
		  .append(" left join fetch metadata.author as author")
		  .append(" left join fetch author.user as authorUser")
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
		  .append(" left join fetch metadata.author as author")
		  .append(" left join fetch author.user as authorUser")
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
		  .append(" left join fetch metadata.author as author")
		  .append(" left join fetch author.user as authorUser")
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
			.setHint("javax.persistence.query.timeout", 1000)
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
	
	public void updateMetadata(long fileSize, Date lastModified, String relativePath, String filename) {
		String updateQuery = "update vfsmetadatafilesaved set fileLastModified=:lastModified, fileSize=:fileSize where filename=:filename and relativePath=:relativePath";
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
	


}
