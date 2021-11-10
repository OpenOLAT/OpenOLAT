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
import org.olat.core.commons.services.vfs.VFSStatistics;
import org.olat.core.commons.services.vfs.model.VFSFileStatistics;
import org.olat.core.commons.services.vfs.model.VFSRevisionStatistics;
import org.olat.core.commons.services.vfs.model.VFSStatisticsImpl;
import org.olat.core.commons.services.vfs.model.VFSThumbnailStatistics;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 
 * Initial date: 23 Dec 2019<br>
 * @author aboeckle, alexander.boeckle@frentix.com, http://www.frentix.com
 *
 */
@Service
public class VFSStatsDAO {

	@Autowired
	private DB dbInstance;
	
	public VFSStatistics createStatistics() {
		VFSFileStatistics fileStats = getFileStats();
		VFSRevisionStatistics revStats = getRevisionStats();
		VFSThumbnailStatistics thumbnailStats = getThumbnailStats();
		
		VFSStatisticsImpl stats = new VFSStatisticsImpl();
		stats.setCreationDate(new Date());
		stats.setFilesAmount(fileStats.getFilesAmount());
		stats.setFilesSize(fileStats.getFilesSize());
		stats.setTrashAmount(fileStats.getTrashAmount());
		stats.setTrashSize(fileStats.getTrashSize());
		stats.setRevisionsAmount(revStats.getRevisionsAmount());
		stats.setRevisionsSize(revStats.getRevisionsSize());
		stats.setThumbnailsAmount(thumbnailStats.getThumbnailsAmount());
		stats.setThumbnailsSize(thumbnailStats.getThumbnailsSize());
		
		dbInstance.getCurrentEntityManager().persist(stats);
		return stats;
	}

	public VFSStatistics getLastStatistics() {
		String q = "select stats from vfsstatistics stats order by stats.creationDate desc";
		
		List<VFSStatistics> stats = dbInstance.getCurrentEntityManager()
				.createQuery(q, VFSStatistics.class)
				.setMaxResults(1)
				.setFirstResult(0)
				.getResultList();
		return stats == null || stats.isEmpty() ? null : stats.get(0);
	}
	
	
	public VFSFileStatistics getFileStats() {
		StringBuilder sb = new StringBuilder(256);
		sb.append("select new org.olat.core.commons.services.vfs.model.VFSFileStatistics(");
		sb.append(" sum(case when metadata.deleted = false then 1 else 0 end) as filesAmount,");
		sb.append(" sum(case when metadata.deleted = false then metadata.fileSize else 0 end) as filesSize,");
		sb.append(" sum(case when metadata.deleted = true then 1 else 0 end) as trashAmount,");
		sb.append(" sum(case when metadata.deleted = true then metadata.fileSize else 0 end) as trashSize");
		sb.append(")");
		sb.append(" from filemetadata as metadata");
		sb.append(" where metadata.directory = false");
		
		List<VFSFileStatistics> queryResult = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), VFSFileStatistics.class)
				.getResultList();
		
		return queryResult.isEmpty() ? new VFSFileStatistics() : queryResult.get(0);
	}
	
	public VFSRevisionStatistics getRevisionStats() {
		StringBuilder sb = new StringBuilder(256);
		sb.append("select new org.olat.core.commons.services.vfs.model.VFSRevisionStatistics(");
		sb.append(" count(revision.key) as revisionAmount,");
		sb.append(" sum(revision.size) as revisionSize");
		sb.append(")");
		sb.append(" from vfsrevision as revision");
		
		List<VFSRevisionStatistics> queryResult = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), VFSRevisionStatistics.class)
				.getResultList();
		
		return queryResult.isEmpty() ? new VFSRevisionStatistics() : queryResult.get(0);
	}
	
	public VFSThumbnailStatistics getThumbnailStats() {
		StringBuilder sb = new StringBuilder(256);
		sb.append("select new org.olat.core.commons.services.vfs.model.VFSThumbnailStatistics(");
		sb.append(" count(thumbnail.key) as thumbnailAmount,");
		sb.append(" sum(thumbnail.fileSize) as thumbnailSize");
		sb.append(")");
		sb.append(" from vfsthumbnail as thumbnail");
		
		List<VFSThumbnailStatistics> queryResult = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), VFSThumbnailStatistics.class)
				.getResultList();
		
		return queryResult.isEmpty() ? new VFSThumbnailStatistics() : queryResult.get(0);
	}
}
