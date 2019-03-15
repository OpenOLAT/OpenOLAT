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

import org.olat.core.commons.persistence.DB;
import org.olat.core.commons.services.vfs.VFSMetadata;
import org.olat.core.commons.services.vfs.VFSMetadataRef;
import org.olat.core.commons.services.vfs.VFSThumbnailMetadata;
import org.olat.core.commons.services.vfs.model.VFSThumbnailMetadataImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 
 * Initial date: 14 mars 2019<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
@Service
public class VFSThumbnailDAO {
	
	@Autowired
	private DB dbInstance;
	
	public VFSThumbnailMetadata createThumbnailMetadata(VFSMetadata metadata, String filename,
			long size, boolean fill, int maxWidth, int maxHeight, int finalWidth, int finalHeight) {
		VFSThumbnailMetadataImpl thumbnail = new VFSThumbnailMetadataImpl();
		thumbnail.setCreationDate(new Date());
		thumbnail.setLastModified(thumbnail.getCreationDate());
		thumbnail.setFilename(filename);
		thumbnail.setFileSize(size);
		thumbnail.setFill(fill);
		thumbnail.setMaxWidth(maxWidth);
		thumbnail.setMaxHeight(maxHeight);
		thumbnail.setFinalWidth(finalWidth);
		thumbnail.setFinalHeight(finalHeight);
		thumbnail.setOwner(metadata);
		dbInstance.getCurrentEntityManager().persist(thumbnail);
		return thumbnail;
	}
	
	public VFSThumbnailMetadata loadByKey(Long key) {
		List<VFSThumbnailMetadata> metas = dbInstance.getCurrentEntityManager()
				.createNamedQuery("loadThumbnailByKey", VFSThumbnailMetadata.class)
				.setParameter("thumbnailKey", key)
				.setFirstResult(0)
				.setMaxResults(1)
				.getResultList();
		return metas == null || metas.isEmpty() ? null : metas.get(0);
	}
	
	public List<VFSThumbnailMetadata> loadByMetadata(VFSMetadataRef metadata) {
		String q = "select thumb from vfsthumbnail thumb where thumb.owner.key=:metadataKey";
		return dbInstance.getCurrentEntityManager()
				.createQuery(q, VFSThumbnailMetadata.class)
				.setParameter("metadataKey", metadata.getKey())
				.getResultList();
	}

	public VFSThumbnailMetadata findThumbnail(String relativePath, String filename, boolean fill, int maxWidth, int maxHeight) {
		StringBuilder sb = new StringBuilder();
		sb.append("select thumb from vfsthumbnail thumb")
		  .append(" inner join thumb.owner as meta")
		  .append(" where meta.filename=:filename and meta.relativePath=:relativePath")
		  .append(" and thumb.maxWidth=:maxWidth and thumb.maxHeight=:maxHeight and thumb.fill=:fill");

		List<VFSThumbnailMetadata> metas = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), VFSThumbnailMetadata.class)
				.setParameter("filename", filename)
				.setParameter("relativePath", relativePath)
				.setParameter("maxWidth", Integer.valueOf(maxWidth))
				.setParameter("maxHeight", Integer.valueOf(maxHeight))
				.setParameter("fill", Boolean.valueOf(fill))
				.setFirstResult(0)
				.setMaxResults(1)
				.getResultList();
		return metas == null || metas.isEmpty() ? null : metas.get(0);
	}
	
	public List<VFSThumbnailMetadata> findThumbnails(String relativePath, String filename) {
		StringBuilder sb = new StringBuilder();
		sb.append("select thumb from vfsthumbnail thumb")
		  .append(" inner join thumb.owner as meta")
		  .append(" where meta.filename=:filename and meta.relativePath=:relativePath");

		return dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), VFSThumbnailMetadata.class)
				.setParameter("filename", filename)
				.setParameter("relativePath", relativePath)
				.getResultList();
	}
	
	public VFSThumbnailMetadata findThumbnail(VFSMetadata owner, boolean fill, int maxWidth, int maxHeight) {
		StringBuilder sb = new StringBuilder();
		sb.append("select thumb from vfsthumbnail thumb")
		  .append(" inner join thumb.owner as meta")
		  .append(" where meta.key=:ownerKey and thumb.maxWidth=:maxWidth and thumb.maxHeight=:maxHeight and thumb.fill=:fill");

		List<VFSThumbnailMetadata> metas = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), VFSThumbnailMetadata.class)
				.setParameter("ownerKey", owner.getKey())
				.setParameter("maxWidth", Integer.valueOf(maxWidth))
				.setParameter("maxHeight", Integer.valueOf(maxHeight))
				.setParameter("fill", Boolean.valueOf(fill))
				.setFirstResult(0)
				.setMaxResults(1)
				.getResultList();
		return metas == null || metas.isEmpty() ? null : metas.get(0);
	}
	
	public void removeThumbnail(VFSThumbnailMetadata thumbnail) {
		dbInstance.getCurrentEntityManager().remove(thumbnail);
	}
}
