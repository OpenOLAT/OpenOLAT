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

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import jakarta.persistence.EntityNotFoundException;
import jakarta.persistence.TemporalType;
import jakarta.persistence.TypedQuery;

import org.apache.logging.log4j.Logger;
import org.olat.core.commons.persistence.DB;
import org.olat.core.commons.persistence.QueryBuilder;
import org.olat.core.commons.services.vfs.VFSMetadata;
import org.olat.core.commons.services.vfs.VFSMetadataRef;
import org.olat.core.commons.services.vfs.VFSRevision;
import org.olat.core.commons.services.vfs.model.VFSRevisionImpl;
import org.olat.core.id.Identity;
import org.olat.core.logging.Tracing;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 
 * Initial date: 18 mars 2019<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
@Service
public class VFSRevisionDAO {
	
	private static final Logger log = Tracing.createLoggerFor(VFSRevisionDAO.class);

	@Autowired
	private DB dbInstance;

	public VFSRevision createRevision(Identity fileInitializedBy, Identity fileLastModifiedBy, String filename,
			int revisionNr, Integer revisionTempNr, long size, Date fileLastModified, String revisionComment,
			VFSMetadata metadata) {
		VFSRevisionImpl rev = new VFSRevisionImpl();
		rev.setCreationDate(new Date());
		rev.setLastModified(rev.getCreationDate());
		rev.setRevisionNr(revisionNr);
		rev.setRevisionTempNr(revisionTempNr);
		rev.setFilename(filename);
		if(fileLastModified == null) {
			rev.setFileLastModified(rev.getCreationDate());
		} else {
			rev.setFileLastModified(fileLastModified);
		}
		rev.setSize(size);
		rev.setRevisionComment(revisionComment);
		rev.copyValues(metadata);
		rev.setFileInitializedBy(fileInitializedBy);
		rev.setFileLastModifiedBy(fileLastModifiedBy);
		rev.setMetadata(metadata);
		dbInstance.getCurrentEntityManager().persist(rev);
		return rev;
	}

	public VFSRevision createRevisionCopy(Identity fileInitialitedBy, Identity fileLastModifiedBy, String revisionComment, VFSMetadata metadata, VFSRevision revisionToCopy) {
		VFSRevisionImpl rev = new VFSRevisionImpl();
		VFSRevisionImpl revToCopy = (VFSRevisionImpl)revisionToCopy;
		rev.setCreationDate(new Date());
		rev.setLastModified(rev.getCreationDate());
		rev.setRevisionNr(revToCopy.getRevisionNr());
		rev.setFilename(revToCopy.getFilename());
		rev.setFileLastModified(revToCopy.getFileLastModified());
		rev.setSize(revToCopy.getSize());
		rev.setRevisionComment(revisionComment);
		rev.setFileInitializedBy(fileInitialitedBy);
		rev.setFileLastModifiedBy(fileLastModifiedBy);
		rev.copyValues(revToCopy);
		rev.setMetadata(metadata);
		dbInstance.getCurrentEntityManager().persist(rev);
		return rev;
	}

	/**
	 * @param revisionKey The primary key
	 * @return A revision with initialized by and metadata loaded
	 */
	public VFSRevision loadRevision(Long revisionKey) {
		StringBuilder sb = new StringBuilder(256);
		sb.append("select rev from vfsrevision rev")
		.append(" left join fetch rev.fileInitializedBy as fileInitializedBy")
		.append(" left join fetch fileInitializedBy.user as fileInitializedByUser")
		.append(" inner join fetch rev.metadata meta")
		.append(" where rev.key=:revisionKey");
		List<VFSRevision> revisions = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), VFSRevision.class)
				.setParameter("revisionKey", revisionKey)
				.getResultList();
		return revisions == null || revisions.isEmpty() ? null : revisions.get(0);
	}
	
	/**
	 * @param revisionKey The primary key
	 * @return A revision without any fetch loading
	 */
	public VFSRevision loadRevisionReference(Long revisionKey) {
		if(revisionKey == null) return null;
		
		List<VFSRevision> revisions = dbInstance.getCurrentEntityManager()
				.createNamedQuery("loadRevisionReferenceByKey", VFSRevision.class)
				.setParameter("revisionKey", revisionKey)
				.getResultList();
		return revisions == null || revisions.isEmpty() ? null : revisions.get(0);
	}

	public List<VFSRevision> getRevisions(VFSMetadataRef metadata) {
		if(metadata == null) return new ArrayList<>();

		QueryBuilder sb = new QueryBuilder(256);
		sb.append("select rev from vfsrevision rev")
		.append(" left join fetch rev.fileInitializedBy as fileInitializedBy")
		.append(" left join fetch fileInitializedBy.user as fileInitializedByUser")
		.append(" where rev.metadata.key=:metadataKey")
		.append(" order by rev.revisionNr, rev.revisionTempNr").append(" nulls first", !dbInstance.isMySQL());
		return dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), VFSRevision.class)
				.setParameter("metadataKey", metadata.getKey())
				.getResultList();
	}
	
	/**
	 * @param metadata The metadata
	 * @return A list of revisions, without any fetch data, not ordered
	 */
	public List<VFSRevision> getRevisionsOnly(VFSMetadataRef metadata) {
		if(metadata == null) return new ArrayList<>();

		String sb = "select rev from vfsrevision rev where rev.metadata.key=:metadataKey";
		return dbInstance.getCurrentEntityManager()
				.createQuery(sb, VFSRevision.class)
				.setParameter("metadataKey", metadata.getKey())
				.getResultList();
	}

	public List<VFSRevision> getRevisions(List<VFSMetadataRef> metadatas) {
		if(metadatas == null || metadatas.isEmpty()) return new ArrayList<>();

		List<Long> metadataKeys = metadatas.stream().map(VFSMetadataRef::getKey).collect(Collectors.toList());

		StringBuilder sb = new StringBuilder(256);
		sb.append("select rev from vfsrevision rev")
		.append(" left join fetch rev.fileInitializedBy as fileInitializedBy")
		.append(" left join fetch fileInitializedBy.user as fileInitializedByUser")
		.append(" where rev.metadata.key in (:metadataKeys)");
		return dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), VFSRevision.class)
				.setParameter("metadataKeys", metadataKeys)
				.getResultList();
	}

	public List<VFSMetadataRef> getMetadataWithMoreThan(long numOfRevisions) {
		StringBuilder sb = new StringBuilder(256);
		sb.append("select new org.olat.core.commons.services.vfs.model.VFSMetadataRefImpl(meta.key)")
		.append(" from filemetadata meta")
		.append(" where :numOfRevisions < (select count(rev.key) from vfsrevision rev where rev.metadata.key=meta.key)");
		return dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), VFSMetadataRef.class)
				.setParameter("numOfRevisions", Long.valueOf(numOfRevisions))
				.getResultList();
	}

	public List<Long> getMetadataKeysOfDeletedFiles() {
		StringBuilder sb = new StringBuilder(256);
		sb.append("select meta.key")
		.append(" from filemetadata meta")
		.append(" inner join vfsrevision rev on (rev.metadata.key=meta.key)")
		.append(" where meta.deleted=:deleted");
		return dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), Long.class)
				.setParameter("deleted", Boolean.TRUE)
				.getResultList();
	}

	/**
	 * @return A list of metadata of deleted files with revisions.
	 */
	public List<VFSMetadataRef> getMetadataOfDeletedFiles() {
		StringBuilder sb = new StringBuilder(256);
		sb.append("select new org.olat.core.commons.services.vfs.model.VFSMetadataRefImpl(meta.key)")
		.append(" from vfsrevision rev")
		.append(" inner join rev.metadata meta")
		.append(" where meta.deleted=:deleted");
		return dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), VFSMetadataRef.class)
				.setParameter("deleted", Boolean.TRUE)
				.getResultList();
	}

	/**
	 * @return A list of revisions of deleted files.
	 */
	public List<VFSRevision> getRevisionsOfDeletedFiles() {
		StringBuilder sb = new StringBuilder(256);
		sb.append("select rev from vfsrevision rev")
		.append(" inner join fetch rev.metadata meta")
		.append(" where meta.deleted=:deleted");
		return dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), VFSRevision.class)
				.setParameter("deleted", Boolean.TRUE)
				.getResultList();
	}

	public long getRevisionsSizeOfDeletedFiles() {
		StringBuilder sb = new StringBuilder(256);
		sb.append("select sum(rev.size) from vfsrevision rev")
		.append(" inner join rev.metadata meta")
		.append(" where meta.deleted=:deleted");
		List<Long> size = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), Long.class)
				.setParameter("deleted", Boolean.TRUE)
				.getResultList();
		return size == null || size.isEmpty() || size.get(0) == null ? 0 : size.get(0).longValue();
	}



	public long calculateRevisionsSize() {
		StringBuilder sb = new StringBuilder(256);
		sb.append("select sum(rev.size) from vfsrevision rev");
		List<Long> size = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), Long.class)
				.getResultList();
		return size == null || size.isEmpty() || size.get(0) == null ? -1l : size.get(0).longValue();
	}

	public VFSRevision updateRevision(VFSRevision revision) {
		return dbInstance.getCurrentEntityManager().merge(revision);
	}

	public void deleteRevision(VFSRevision revision) {
		if(revision instanceof VFSRevisionImpl) {
			try {
				VFSRevision reloadedRev = loadRevisionReference(((VFSRevisionImpl)revision).getKey());
				if(reloadedRev != null) {
					dbInstance.getCurrentEntityManager().remove(reloadedRev);
				}
			} catch (EntityNotFoundException e) {
				log.error("Entity not found", e);
			}
		}
	}

	public List<VFSRevision> getLargest(int maxResult, 
			Date createdAtNewer, Date createdAtOlder, 
			Date editedAtNewer, Date editedAtOlder, 
			Date lockedAtNewer, Date lockedAtOlder,
			Boolean trashed, Boolean locked,
			Integer downloadCount, Long revisionCount,
			Integer size) {

		QueryBuilder qb = new QueryBuilder(256);
		qb.append("select rev from vfsrevision rev")
		.append(" inner join fetch rev.metadata metadata")
		.append(" left join fetch rev.fileInitializedBy as fileInitializedBy")
		.append(" left join fetch fileInitializedBy.user as fileInitializedByUser");

		if(createdAtNewer != null) {
			qb.where().append("rev.creationDate>=:createdAtNewer");
		}
		if(createdAtOlder != null) {
			qb.where().append("rev.creationDate<=:createdAtOlder");
		}
		if(editedAtNewer != null) {
			qb.where().append("rev.lastModified>=:editedAtNewer");
		}
		if(editedAtOlder != null) {
			qb.where().append("rev.lastModified<=:editedAtOlder");
		}
		if(lockedAtNewer != null) {
			qb.where().append("metadata.lockedDate>=:lockedAtNewer");
		}
		if(lockedAtOlder != null) {
			qb.where().append("metadata.lockedDate<=:lockedAtOlder");
		}
		if(trashed != null) {
			qb.where().append("metadata.deleted=:trashed");
		}
		if(locked != null) {
			qb.where().append("metadata.locked=:locked");
		}
		if(downloadCount != null && downloadCount.longValue() > 0) {
			qb.where().append("metadata.downloadCount>=:downloadCount");
		}
		if(revisionCount != null && revisionCount.longValue() > 0) {
			qb.where().append("metadata.revisionNr>=:revisionCount");
		}
		if(size != null && size.intValue() > 0) {
			qb.where().append("rev.size>=:size");
		}


		qb.append(" order by rev.size desc nulls last");

		TypedQuery<VFSRevision> query = dbInstance.getCurrentEntityManager()
				.createQuery(qb.toString(), VFSRevision.class);

		if(createdAtNewer != null) {
			query.setParameter("createdAtNewer", createdAtNewer, TemporalType.TIMESTAMP);
		}
		if(createdAtOlder != null) {
			query.setParameter("createdAtOlder", createdAtOlder, TemporalType.TIMESTAMP);
		}
		if(editedAtNewer != null) {
			query.setParameter("editedAtNewer", editedAtNewer, TemporalType.TIMESTAMP);
		}
		if(editedAtOlder != null) {
			query.setParameter("editedAtOlder", editedAtOlder, TemporalType.TIMESTAMP);
		}
		if(lockedAtNewer != null) {
			query.setParameter("lockedAtNewer", lockedAtNewer, TemporalType.TIMESTAMP);
		}
		if(lockedAtOlder != null) {
			query.setParameter("lockedAtOlder", lockedAtOlder, TemporalType.TIMESTAMP);
		}
		if(trashed != null) {
			query.setParameter("trashed", trashed);
		}
		if(locked != null) {
			query.setParameter("locked", locked);
		}
		if(downloadCount != null && downloadCount.longValue() > 0) {
			query.setParameter("downloadCount", downloadCount);
		}
		if(revisionCount != null && revisionCount.longValue() > 0) {
			query.setParameter("revisionCount", revisionCount);
		}
		if(size != null && size.intValue() > 0) {
			query.setParameter("size", Long.valueOf(size));
		}
		
		return query.setFirstResult(0)
				.setMaxResults(maxResult > 0 && maxResult <= 100 ? maxResult : 100)
				.getResultList();

	}
}
