/**
 * <a href="https://www.openolat.org">
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
 * frentix GmbH, https://www.frentix.com
 * <p>
 */
package org.olat.core.commons.services.export.manager;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Date;
import java.util.List;
import java.util.UUID;

import org.junit.Assert;
import org.junit.Test;
import org.olat.core.commons.persistence.DB;
import org.olat.core.commons.services.export.ArchiveType;
import org.olat.core.commons.services.export.ExportMetadata;
import org.olat.core.commons.services.export.model.SearchExportMetadataParameters;
import org.olat.core.id.Identity;
import org.olat.core.util.DateUtils;
import org.olat.test.JunitTestHelper;
import org.olat.test.OlatTestCase;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 21 févr. 2024<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class ExportMetadataDAOTest extends OlatTestCase {
	
	@Autowired
	private DB dbInstance;
	@Autowired
	private ExportMetadataDAO exportMetadataDao;
	
	@Test
	public void createMetadata() {
		String title = "Some meta export";
		ExportMetadata metadata = exportMetadataDao.createMetadata(title, null, null, null, null, false, null, null, null, null);
		dbInstance.commit();
		
		Assert.assertNotNull(metadata);
		Assert.assertNotNull(metadata.getKey());
		Assert.assertNotNull(metadata.getCreationDate());
		Assert.assertNotNull(metadata.getLastModified());
		Assert.assertEquals(title, metadata.getTitle());
	}
	
	@Test
	public void getMetadataByKey() {
		String title = "Some meta export";
		Identity id = JunitTestHelper.createAndPersistIdentityAsRndUser("Metadata-owner");
		ExportMetadata metadata = exportMetadataDao.createMetadata(title, "My description", "log.txt", ArchiveType.PARTIAL, null, false, null, null, id, null);
		dbInstance.commitAndCloseSession();
		
		ExportMetadata reloadedMetadata = exportMetadataDao.getMetadataByKey(metadata.getKey());
		
		Assert.assertNotNull(metadata);
		Assert.assertNotNull(metadata.getKey());
		Assert.assertNotNull(metadata.getCreationDate());
		Assert.assertNotNull(metadata.getLastModified());
		Assert.assertEquals(title, metadata.getTitle());
		Assert.assertEquals("My description", metadata.getDescription());
		Assert.assertEquals("log.txt", metadata.getFilename());
		Assert.assertEquals(ArchiveType.PARTIAL, metadata.getArchiveType());
		Assert.assertEquals(id, metadata.getCreator());
		Assert.assertEquals(metadata, reloadedMetadata);
	}
	
	@Test
	public void expiredExports() {
		Date now = new Date();
		String title = "Some expired export";
		
		Date expirationDate = DateUtils.addDays(now, -2);
		ExportMetadata metadata = exportMetadataDao.createMetadata(title, null, null, null, expirationDate, false, null, null, null, null);
		dbInstance.commit();
		
		List<ExportMetadata> expiredMetadata = exportMetadataDao.expiredExports(now);
		assertThat(expiredMetadata)
			.contains(metadata);
	}
	
	@Test
	public void searchMetadatasByresSubPath() {
		Identity id = JunitTestHelper.createAndPersistIdentityAsRndUser("exporter-1");
		String title = "Some meta export";
		String resSubPath = UUID.randomUUID().toString();
		Date expirationDate = DateUtils.addDays(new Date(), 7);
		ExportMetadata metadata = exportMetadataDao.createMetadata(title, null, null, null, expirationDate, true, null, resSubPath, id, null);
		dbInstance.commit();
		
		SearchExportMetadataParameters params = new SearchExportMetadataParameters(null, resSubPath);
		List<ExportMetadata> metadataList = exportMetadataDao.searchMetadatas(params);
		assertThat(metadataList)
			.hasSize(1)
			.containsExactly(metadata);
	}

}
