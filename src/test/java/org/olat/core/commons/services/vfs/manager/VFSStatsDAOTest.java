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

import org.junit.Assert;
import org.junit.Test;
import org.olat.core.commons.persistence.DB;
import org.olat.core.commons.services.vfs.VFSStatistics;
import org.olat.core.commons.services.vfs.model.VFSFileStatistics;
import org.olat.core.commons.services.vfs.model.VFSRevisionStatistics;
import org.olat.core.commons.services.vfs.model.VFSThumbnailStatistics;
import org.olat.test.OlatTestCase;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 18 mars 2019<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class VFSStatsDAOTest extends OlatTestCase {
	
	@Autowired
	private DB dbInstance;
	@Autowired
	private VFSStatsDAO vfsStatsDAO;
	
	@Test
	public void getFileStats() {
		VFSFileStatistics fileStatistics = vfsStatsDAO.getFileStats();
		
		Assert.assertTrue(fileStatistics.getFilesAmount() >= 0);
		Assert.assertTrue(fileStatistics.getFilesSize() >= 0);
		Assert.assertTrue(fileStatistics.getTrashAmount() >= 0);
		Assert.assertTrue(fileStatistics.getTrashSize() >= 0);
	}
	
	@Test
	public void getRevisionStats() {
		VFSRevisionStatistics revisionStatistics = vfsStatsDAO.getRevisionStats();
		
		Assert.assertTrue(revisionStatistics.getRevisionsAmount() >= 0);
		Assert.assertTrue(revisionStatistics.getRevisionsSize() >= 0);
	}
	
	@Test
	public void getThumbnailStats() {
		VFSThumbnailStatistics thumbnailStatistics = vfsStatsDAO.getThumbnailStats();
		
		Assert.assertTrue(thumbnailStatistics.getThumbnailsAmount() >= 0);
		Assert.assertTrue(thumbnailStatistics.getThumbnailsSize() >= 0);
	}
	
	@Test
	public void createStatistics() {
		VFSStatistics statistics = vfsStatsDAO.createStatistics();
		dbInstance.commit();
		
		Assert.assertTrue(statistics.getFilesAmount() >= 0);
		Assert.assertTrue(statistics.getFilesSize() >= 0);
		Assert.assertTrue(statistics.getTrashAmount() >= 0);
		Assert.assertTrue(statistics.getTrashSize() >= 0);
		Assert.assertTrue(statistics.getThumbnailsAmount() >= 0);
		Assert.assertTrue(statistics.getThumbnailsSize() >= 0);
		Assert.assertTrue(statistics.getRevisionsAmount() >= 0);
		Assert.assertTrue(statistics.getRevisionsSize() >= 0);
		
		VFSStatistics lastStatistics = vfsStatsDAO.getLastStatistics();
		Assert.assertNotNull(lastStatistics);
	}
}
