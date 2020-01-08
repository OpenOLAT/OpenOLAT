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

import java.util.List;

import org.olat.core.commons.persistence.DB;
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
	
	// TODO list array to object
	// TODO Unit test 
	public Object[] getFileStats() {
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
		
		return !filesAmount.isEmpty() ? filesAmount.get(0): null;
	}
	
	public Object[] getRevisionStats() {
		StringBuilder sb = new StringBuilder(256);
		sb.append("select count(revision.key) as revisionAmount,");
		sb.append(" sum(revision.size) as revisionSize");
		sb.append(" from vfsrevision as revision");
		
		List<Object[]> filesAmount = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), Object[].class)
				.getResultList();
		
		return !filesAmount.isEmpty() ? filesAmount.get(0) : null;
	}
	
	public Object[] getThumbnailStats() {
		StringBuilder sb = new StringBuilder(256);
		sb.append("select count(thumbnail.key) as thumbnailAmount,");
		sb.append(" sum(thumbnail.fileSize) as thumbnailSize");
		sb.append(" from vfsthumbnail as thumbnail");
		
		List<Object[]> filesAmount = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), Object[].class)
				.getResultList();
		
		return !filesAmount.isEmpty() ? filesAmount.get(0) : null;
	}
}
