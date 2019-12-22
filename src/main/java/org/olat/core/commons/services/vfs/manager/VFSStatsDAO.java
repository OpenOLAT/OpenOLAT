package org.olat.core.commons.services.vfs.manager;

import java.util.List;

import org.olat.core.commons.persistence.DB;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class VFSStatsDAO {

	@Autowired
	private DB dbInstance;
	
	public List<Object[]> getFileStats() {
		StringBuilder sb = new StringBuilder(256);
		sb.append("select sum(case when metadata.deleted = false then 1 else 0 end) as filesAmount,");
		sb.append(" sum(metadata.fileSize) as filesSize,");
		sb.append(" sum(case when metadata.deleted = true then 1 else 0 end) as trashAmount,");
		sb.append(" sum(case when metadata.deleted = true then metadata.fileSize else 0 end) as trashSize");
		sb.append(" from filemetadata as metadata");
		sb.append(" where metadata.directory = false");
		
		List<Object[]> filesAmount = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), Object[].class)
				.getResultList();
		
		return !filesAmount.isEmpty() ? filesAmount : null;
	}
	
	public List<Object[]> getRevisionStats() {
		StringBuilder sb = new StringBuilder(256);
		sb.append("select count(revision.key) as revisionAmount,");
		sb.append(" sum(revision.size) as revisionSize");
		sb.append(" from vfsrevision as revision");
		
		List<Object[]> filesAmount = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), Object[].class)
				.getResultList();
		
		return !filesAmount.isEmpty() ? filesAmount : null;
	}
	
	public List<Object[]> getThumbnailStats() {
		StringBuilder sb = new StringBuilder(256);
		sb.append("select count(thumbnail.key) as thumbnailAmount,");
		sb.append(" sum(thumbnail.fileSize) as thumbnailSize");
		sb.append(" from vfsthumbnail as thumbnail");
		
		List<Object[]> filesAmount = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), Object[].class)
				.getResultList();
		
		return !filesAmount.isEmpty() ? filesAmount : null;
	}
}
