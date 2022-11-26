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
package org.olat.modules.library;


import static org.assertj.core.api.Assertions.assertThat;

import java.net.URL;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.olat.core.commons.persistence.DB;
import org.olat.core.commons.services.commentAndRating.manager.UserRatingsDAO;
import org.olat.core.commons.services.vfs.VFSMetadata;
import org.olat.core.commons.services.vfs.manager.VFSMetadataDAO;
import org.olat.core.id.Identity;
import org.olat.core.id.OLATResourceable;
import org.olat.core.util.DateUtils;
import org.olat.core.util.ZipUtil;
import org.olat.core.util.vfs.VFSContainer;
import org.olat.core.util.vfs.VFSLeaf;
import org.olat.modules.library.model.CatalogItem;
import org.olat.modules.sharedfolder.SharedFolderManager;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.handlers.SharedFolderHandler;
import org.olat.test.JunitTestHelper;
import org.olat.test.OlatTestCase;
import org.olat.test.VFSJavaIOFile;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 25 nov. 2022<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class LibraryManagerTest extends OlatTestCase {
	
	private static RepositoryEntry library;
	
	@Autowired
	private DB dbInstance;
	@Autowired
	private VFSMetadataDAO metadataDao;
	@Autowired
	private LibraryModule libraryModule;
	@Autowired
	private LibraryManager libraryManager;
	@Autowired
	private UserRatingsDAO userRatingsDao;
	
	@Before
	public void createLibrary() throws Exception {
		if(library != null) return;
		
		Identity owner = JunitTestHelper.createAndPersistIdentityAsRndUser("librarian-");
		library = new SharedFolderHandler().createResource(owner, "My Library", "A shared library", null, null, Locale.ENGLISH);
		VFSContainer container = SharedFolderManager.getInstance().getNamedSharedFolder(library, true);
		container.getMetaInfo();
		
		URL url = LibraryManagerTest.class.getResource("Library.zip");
		VFSLeaf zipLeaf = new VFSJavaIOFile(url.toURI());
		ZipUtil.unzip(zipLeaf, container, owner, false);
		
		libraryModule.setLibraryEntryKey(library.getKey().toString());
	}
	
	@Test
	public void getSharedFolder() {
		VFSContainer sharedFolder = libraryManager.getSharedFolder();
		Assert.assertNotNull(sharedFolder);
	}
	
	@Test
	public void getCatalogRepoEntry() {
		RepositoryEntry entry = libraryManager.getCatalogRepoEntry();
		Assert.assertNotNull(entry);
		Assert.assertEquals(library, entry);
	}
	
	@Test
	public void getLibraryResourceable() {
		OLATResourceable ores = libraryManager.getLibraryResourceable();
		Assert.assertNotNull(ores);
		Assert.assertEquals("LibrarySite", ores.getResourceableTypeName());
		Assert.assertEquals(library.getOlatResource().getResourceableId(), ores.getResourceableId());
	}
	
	@Test
	public void getCatalogItems() {
		Identity id = JunitTestHelper.createAndPersistIdentityAsRndUser("visitor-1");
		VFSContainer sharedFolder = libraryManager.getSharedFolder();
		VFSMetadata parentMetadata = sharedFolder.resolve("Positions").getMetaInfo();
		
		List<CatalogItem> items = libraryManager.getCatalogItems(parentMetadata, id);
		assertThat(items)
			.hasSize(2)
			.map(CatalogItem::getFilename)
			.containsExactlyInAnyOrder("DocPosition_1.pdf", "DocPosition_2.pdf");
	}

	@Test
	public void getCatalogItemsByUrl() {
		Identity id = JunitTestHelper.createAndPersistIdentityAsRndUser("visitor-2");
		String businessPath = "[LibrarySite:0][path=/Positions/DocPosition_1.pdf]";
		
		CatalogItem item = libraryManager.getCatalogItemsByUrl(businessPath, id);
		Assert.assertNotNull(item);
		Assert.assertEquals("DocPosition_1.pdf", item.getFilename());
	}
	
	@Test
	public void getNewCatalogItems() {
		Identity id = JunitTestHelper.createAndPersistIdentityAsRndUser("visitor-3");

		List<CatalogItem> items = libraryManager.getNewCatalogItems(DateUtils.addDays(new Date(), -23000), id);
		Assert.assertEquals(5, items.size());// All files
	}
	
	@Test
	public void getNewestCatalogItems() {
		Identity id = JunitTestHelper.createAndPersistIdentityAsRndUser("visitor-4");

		List<CatalogItem> items = libraryManager.getNewestCatalogItems(3, id);
		Assert.assertEquals(3, items.size());
	}
	
	@Test
	public void getMostViewedCatalogItems() {
		Identity id = JunitTestHelper.createAndPersistIdentityAsRndUser("visitor-5");
		VFSContainer sharedFolder = libraryManager.getSharedFolder();
		VFSMetadata positionMetadata = sharedFolder.resolve("Positions/DocPosition_1.pdf").getMetaInfo();
		metadataDao.increaseDownloadCount(positionMetadata.getRelativePath(), positionMetadata.getFilename());

		List<CatalogItem> items = libraryManager.getMostViewedCatalogItems(10, id);
		assertThat(items)
			.isNotEmpty()
			.map(CatalogItem::getFilename)
			.contains("DocPosition_1.pdf");
	}
	
	@Test
	public void getMostRatedCatalogItems() {
		Identity id = JunitTestHelper.createAndPersistIdentityAsRndUser("visitor-6");
		VFSContainer sharedFolder = libraryManager.getSharedFolder();
		VFSMetadata metadata = sharedFolder.resolve("Office/SimpleExcel.xlsx").getMetaInfo();
		OLATResourceable libraryOres = libraryManager.getLibraryResourceable();
		userRatingsDao.updateRating(id, libraryOres, metadata.getUuid(), Math.round(4.0f));
		dbInstance.commitAndCloseSession();

		List<CatalogItem> items = libraryManager.getMostRatedCatalogItems(1, id);
		assertThat(items)
			.isNotEmpty()
			.map(CatalogItem::getFilename)
			.contains("SimpleExcel.xlsx");
	}
	
	@Test
	public void getCatalogItemByUUID() {
		Identity id = JunitTestHelper.createAndPersistIdentityAsRndUser("visitor-7");
		VFSContainer sharedFolder = libraryManager.getSharedFolder();
		VFSMetadata possitionMetadata = sharedFolder.resolve("Positions/DocPosition_2.pdf").getMetaInfo();

		CatalogItem item = libraryManager.getCatalogItemByUUID(possitionMetadata.getUuid(), id);
		Assert.assertNotNull(item);
		Assert.assertEquals("DocPosition_2.pdf", item.getFilename());
	}
	
	@Test
	public void getFileByUUID() {
		VFSContainer sharedFolder = libraryManager.getSharedFolder();
		VFSMetadata possitionMetadata = sharedFolder.resolve("Office/SimpleExcel.xlsx").getMetaInfo();

		VFSLeaf file = libraryManager.getFileByUUID(possitionMetadata.getUuid());
		Assert.assertNotNull(file);
		Assert.assertEquals("SimpleExcel.xlsx", file.getName());
	}
	

}
