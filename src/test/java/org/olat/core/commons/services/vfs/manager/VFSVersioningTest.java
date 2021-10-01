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

import static org.junit.Assert.assertEquals;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.Callable;

import org.apache.commons.io.IOUtils;
import org.apache.logging.log4j.Logger;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.olat.core.commons.persistence.DB;
import org.olat.core.commons.services.vfs.VFSMetadata;
import org.olat.core.commons.services.vfs.VFSRepositoryService;
import org.olat.core.commons.services.vfs.VFSRevision;
import org.olat.core.commons.services.vfs.VFSVersionModule;
import org.olat.core.commons.services.vfs.model.VFSRevisionImpl;
import org.olat.core.id.Identity;
import org.olat.core.logging.Tracing;
import org.olat.core.util.vfs.VFSConstants;
import org.olat.core.util.vfs.VFSContainer;
import org.olat.core.util.vfs.VFSItem;
import org.olat.core.util.vfs.VFSLeaf;
import org.olat.core.util.vfs.VFSManager;
import org.olat.core.util.vfs.filters.VFSItemFilter;
import org.olat.test.JunitTestHelper;
import org.olat.test.OlatTestCase;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 20 mars 2019<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class VFSVersioningTest extends OlatTestCase {
	
	private static final Logger log = Tracing.createLoggerFor(VFSVersioningTest.class);
	
	@Autowired
	private DB dbInstance;
	@Autowired
	private VFSVersionModule versionsModule;
	@Autowired
	private VFSRepositoryService vfsRepositoryService;
	
	@Before
	public void setUp() throws Exception {
		waitForCondition(new SetMaxNumberOfVersions(versionsModule, 10l), 2000);
	}
	
	@After
	public void resetMaxVersions() {
		int maxNumberOfVersions = versionsModule.getMaxNumberOfVersions();
		if(maxNumberOfVersions != 10) {
			versionsModule.setMaxNumberOfVersions(10);
			waitForCondition(new SetMaxNumberOfVersions(versionsModule, 10l), 2000);
		}
	}
	
	@Test
	public void addVersions() throws IOException {
		Identity id = JunitTestHelper.createAndPersistIdentityAsRndUser("vers-1");
		
		//create a file
		VFSContainer rootTest = VFSManager.olatRootContainer("/test", null);
		String filename = UUID.randomUUID().toString() + ".txt";
		VFSLeaf file = rootTest.createChildLeaf(filename);
		int byteCopied = copyTestTxt(file);
		Assert.assertNotEquals(0, byteCopied);
		vfsRepositoryService.itemSaved(file, id);
		VFSMetadata data = vfsRepositoryService.getMetadataFor(file);
		vfsRepositoryService.updateMetadata(data);
		dbInstance.commitAndCloseSession();
		
		//save a first version
		InputStream in1 = new ByteArrayInputStream("Hello version 1".getBytes());
		vfsRepositoryService.addVersion(file, id, false, "Version 1", in1);
		in1.close();
		
		//save a second version
		InputStream in2 = new ByteArrayInputStream("Hello version 2".getBytes());
		vfsRepositoryService.addVersion(file, id, false, "Version 2", in2);
		in2.close();
		
		//save a third version
		InputStream in3 = new ByteArrayInputStream("Hello version 3".getBytes());
		vfsRepositoryService.addVersion(file, id, false, "Version 3", in3);
		in3.close();

		//make the checks
		VFSItem retrievedFile = rootTest.resolve(filename);
		VFSMetadata metadata = vfsRepositoryService.getMetadataFor(retrievedFile);
		List<VFSRevision> revisions = vfsRepositoryService.getRevisions(metadata);	
		Assert.assertNotNull(revisions);
		Assert.assertEquals(3, revisions.size());
		
		VFSRevision revision0 = revisions.get(0);
		//we don't set an author for the original file
		Assert.assertEquals(id, revision0.getFileInitializedBy());
		VFSRevision revision1 = revisions.get(1);
		Assert.assertEquals(id, revision1.getFileInitializedBy());
		VFSRevision revision2 = revisions.get(2);
		Assert.assertEquals(id, revision2.getFileInitializedBy());

		//check the comments
		Assert.assertNull(revision0.getRevisionComment());	
		Assert.assertEquals("Version 1", revision1.getRevisionComment());
		Assert.assertEquals("Version 2", revision2.getRevisionComment());
		Assert.assertEquals("Version 3", metadata.getRevisionComment());
	}
	
	@Test
	public void addVersions_overflow_lowLevel() throws IOException {
		versionsModule.setMaxNumberOfVersions(3);
		waitForCondition(new SetMaxNumberOfVersions(versionsModule, 3l), 2000);
		
		Identity id2 = JunitTestHelper.createAndPersistIdentityAsRndUser("vers-2");
		
		//create a file
		VFSContainer rootTest = VFSManager.olatRootContainer("/test_" + UUID.randomUUID(), null);
		String filename = UUID.randomUUID().toString() + ".txt";
		VFSLeaf file = rootTest.createChildLeaf(filename);
		int byteCopied = copyTestTxt(file);
		Assert.assertNotEquals(0, byteCopied);
		
		VFSItem retrievedFile = rootTest.resolve(filename);
		VFSMetadata metadata = vfsRepositoryService.getMetadataFor(retrievedFile);
		
		//save five versions
		for(int i=0; i<5; i++) {
			InputStream inv = new ByteArrayInputStream(("Hello version " + i).getBytes());
			vfsRepositoryService.addVersion(file, id2, false, "Version " + (1 +i), inv);
			inv.close();
		}
		
		metadata = vfsRepositoryService.getMetadataFor(retrievedFile);
		List<VFSRevision> revisions = vfsRepositoryService.getRevisions(metadata);
		Assert.assertNotNull(revisions);
		Assert.assertEquals(3, revisions.size());
		Assert.assertEquals(3, revisions.get(0).getRevisionNr());
		Assert.assertEquals(4, revisions.get(1).getRevisionNr());
		Assert.assertEquals(5, revisions.get(2).getRevisionNr());

		Assert.assertEquals("Version 5", metadata.getRevisionComment());
		Assert.assertEquals(id2, revisions.get(2).getFileInitializedBy());
	}
	
	@Test
	public void addVersionsDeleteRename() throws IOException {
		versionsModule.setMaxNumberOfVersions(3);
		waitForCondition(new SetMaxNumberOfVersions(versionsModule, 3l), 2000);
		
		Identity id2 = JunitTestHelper.createAndPersistIdentityAsRndUser("vers-2");
		
		//create a file
		VFSContainer rootTest = VFSManager.olatRootContainer("/test_" + UUID.randomUUID(), null);
		String filename = "orig_" + UUID.randomUUID().toString() + ".txt";
		VFSLeaf file = rootTest.createChildLeaf(filename);
		int byteCopied = copyTestTxt(file);
		Assert.assertNotEquals(0, byteCopied);
		
		//save a first version
		for(int i=0; i<2; i++) {
			InputStream inv = new ByteArrayInputStream(("Hello version " + i).getBytes());
			vfsRepositoryService.addVersion(file, id2, false, "Version " + (1 +i), inv);
			inv.close();
		}

		VFSItem retrievedFile = rootTest.resolve(filename);
		VFSMetadata metadata = vfsRepositoryService.getMetadataFor(retrievedFile);
		List<VFSRevision> revisions = vfsRepositoryService.getRevisions(metadata);
		Assert.assertNotNull(revisions);
		Assert.assertEquals(2, revisions.size());
		
		// delete the file
		retrievedFile.delete();
		
		// new file
		String newFilename = "new_" + UUID.randomUUID().toString() + ".txt";
		VFSLeaf newFile = rootTest.createChildLeaf(newFilename);
		int newByteCopied = copyTestTxt(newFile);
		Assert.assertNotEquals(0, newByteCopied);
		
		// rename new file to old name
		newFile.rename(filename);
		dbInstance.commit();
	}
	
	@Test
	public void addVersions_overflow_lowLevel_deactivated() throws IOException {
		versionsModule.setMaxNumberOfVersions(0);
		waitForCondition(new SetMaxNumberOfVersions(versionsModule,  0l), 2000);
		
		Identity id = JunitTestHelper.createAndPersistIdentityAsRndUser("vers-");
		
		//create a file
		VFSContainer rootTest = VFSManager.olatRootContainer("/test_" + UUID.randomUUID(), null);
		String filename = UUID.randomUUID().toString() + ".txt";
		VFSLeaf file = rootTest.createChildLeaf(filename);
		int byteCopied = copyTestTxt(file);
		Assert.assertNotEquals(0, byteCopied);
		
		//save a first version
		for(int i=0; i<5; i++) {
			InputStream inv = new ByteArrayInputStream(("Hello version " + i).getBytes());
			vfsRepositoryService.addVersion(file, id, false, "Version " + (1 +i), inv);
			inv.close();
		}

		VFSItem retrievedFile = rootTest.resolve(filename);
		VFSMetadata metadata = vfsRepositoryService.getMetadataFor(retrievedFile);	
		List<VFSRevision> revisions = vfsRepositoryService.getRevisions(metadata);
		Assert.assertNotNull(revisions);
		Assert.assertTrue(revisions.isEmpty());
	}
	
	@Test
	public void addVersions_temp() throws IOException {
		Identity id = JunitTestHelper.createAndPersistIdentityAsRndUser("vers-t-1");
		
		//create a file
		VFSContainer rootTest = VFSManager.olatRootContainer("/test", null);
		String filename = UUID.randomUUID().toString() + ".txt";
		VFSLeaf file = rootTest.createChildLeaf(filename);
		int byteCopied = copyTestTxt(file);
		Assert.assertNotEquals(0, byteCopied);
		vfsRepositoryService.itemSaved(file, id);
		VFSMetadata data = vfsRepositoryService.getMetadataFor(file);
		vfsRepositoryService.updateMetadata(data);
		dbInstance.commitAndCloseSession();
		
		//save a first version
		InputStream in1 = new ByteArrayInputStream("Hello version 1".getBytes());
		vfsRepositoryService.addVersion(file, id, false, "Version 1", in1);
		in1.close();
		
		//save a second version
		InputStream in2 = new ByteArrayInputStream("Hello version 2".getBytes());
		vfsRepositoryService.addVersion(file, id, false, "Version 2", in2);
		in2.close();
		
		//save a temp version
		InputStream in2t1 = new ByteArrayInputStream("Hello version 2.1".getBytes());
		vfsRepositoryService.addVersion(file, id, true, "Version 2.1", in2t1);
		in2t1.close();
		
		//save a temp version
		InputStream in2t2 = new ByteArrayInputStream("Hello version 2.2".getBytes());
		vfsRepositoryService.addVersion(file, id, true, "Version 2.2", in2t2);
		in2t2.close();

		//make the checks
		VFSItem retrievedFile = rootTest.resolve(filename);
		VFSMetadata metadata = vfsRepositoryService.getMetadataFor(retrievedFile);
		List<VFSRevision> revisions = vfsRepositoryService.getRevisions(metadata);
		Assert.assertNotNull(revisions);
		Assert.assertEquals(4, revisions.size());
		
		//check the comments
		Assert.assertNull(revisions.get(0).getRevisionComment());
		Assert.assertEquals("Version 1", revisions.get(1).getRevisionComment());
		Assert.assertEquals("Version 2", revisions.get(2).getRevisionComment());
		Assert.assertEquals("Version 2.1", revisions.get(3).getRevisionComment());
		
		//save more versions
		InputStream in3 = new ByteArrayInputStream("Hello version 3".getBytes());
		vfsRepositoryService.addVersion(file, id, false, "Version 3", in3);
		in3.close();
		
		InputStream in4 = new ByteArrayInputStream("Hello version 4".getBytes());
		vfsRepositoryService.addVersion(file, id, false, "Version 4", in4);
		in4.close();
		
		//make the checks
		metadata = vfsRepositoryService.getMetadataFor(retrievedFile);
		revisions = vfsRepositoryService.getRevisions(metadata);
		Assert.assertNotNull(revisions);
		Assert.assertEquals(4, revisions.size());
		//check the comments
		Assert.assertNull(revisions.get(0).getRevisionComment());
		Assert.assertEquals("Version 1", revisions.get(1).getRevisionComment());
		Assert.assertEquals("Version 2", revisions.get(2).getRevisionComment());
		Assert.assertEquals("Version 3", revisions.get(3).getRevisionComment());
	}
	
	@Test
	public void addVersions_temp_level_ignored() throws IOException {
		versionsModule.setMaxNumberOfVersions(3);
		waitForCondition(new SetMaxNumberOfVersions(versionsModule, 3l), 2000);
		
		Identity id = JunitTestHelper.createAndPersistIdentityAsRndUser("vers-t-2");
		
		//create a file
		VFSContainer rootTest = VFSManager.olatRootContainer("/test_" + UUID.randomUUID(), null);
		String filename = UUID.randomUUID().toString() + ".txt";
		VFSLeaf file = rootTest.createChildLeaf(filename);
		int byteCopied = copyTestTxt(file);
		Assert.assertNotEquals(0, byteCopied);
		
		// set metadata
		VFSMetadata metadata = vfsRepositoryService.getMetadataFor(file);
		metadata.setComment("Initital version 0");
		vfsRepositoryService.updateMetadata(metadata);
		dbInstance.commitAndCloseSession();
		
		//save a first version
		InputStream in1 = new ByteArrayInputStream("Hello version 1".getBytes());
		vfsRepositoryService.addVersion(file, id, false, "Version 1", in1);
		in1.close();
		
		// create 10 temporary versions
		for(int i=0; i<10; i++) {
			InputStream inv = new ByteArrayInputStream(("Hello version " + i).getBytes());
			vfsRepositoryService.addVersion(file, id, true, "Version 1." + (1 +i), inv);
			inv.close();
		}
		
		// Check if all temporary versions are persisted
		VFSItem retrievedFile = rootTest.resolve(filename);
		metadata = vfsRepositoryService.getMetadataFor(retrievedFile);
		List<VFSRevision> revisions = vfsRepositoryService.getRevisions(metadata);
		Assert.assertNotNull(revisions);
		Assert.assertEquals(11, revisions.size());
		// The stable versions should still exist.
		Assert.assertEquals("Version 1", revisions.get(1).getRevisionComment());
		Assert.assertEquals("Version 1.9", revisions.get(10).getRevisionComment());
		
		//save a second stable version
		InputStream in2 = new ByteArrayInputStream("Hello version 2".getBytes());
		vfsRepositoryService.addVersion(file, id, false, "Version 2", in2);
		in2.close();
		
		// Stable version 2
		metadata = vfsRepositoryService.getMetadataFor(retrievedFile);
		Assert.assertEquals("Version 2", metadata.getRevisionComment());
		revisions = vfsRepositoryService.getRevisions(metadata);
		Assert.assertNotNull(revisions);
		Assert.assertEquals(2, revisions.size());
		Assert.assertNull(revisions.get(0).getRevisionComment());
		Assert.assertEquals("Version 1", revisions.get(1).getRevisionComment());
	}
	
	@Test
	public void versionChecksum() throws IOException {
		VFSContainer rootTest = VFSManager.olatRootContainer("/ver-" + UUID.randomUUID(), null);
		String filename = UUID.randomUUID().toString() + ".txt";
		VFSLeaf file = rootTest.createChildLeaf(filename);
		int byteCopied = copyTestTxt(file);
		Assert.assertNotEquals(0, byteCopied);
		
		Identity id = JunitTestHelper.createAndPersistIdentityAsRndUser("vers-4");

		//save a first version
		InputStream in1 = VFSVersioningTest.class.getResourceAsStream("test.txt");
		vfsRepositoryService.addVersion(file, id, false, "Version 1", in1);
		in1.close();
		
		//save a second version
		InputStream in2 = VFSVersioningTest.class.getResourceAsStream("test.txt");
		vfsRepositoryService.addVersion(file, id, false, "Version 2", in2);
		in2.close();
		
		//save a third version
		InputStream in3 = VFSVersioningTest.class.getResourceAsStream("test.txt");
		vfsRepositoryService.addVersion(file, id, false, "Version 3", in3);
		in3.close();
		
		//check if there is only one backup file
		VFSContainer versionContainer = file.getParentContainer();
		List<VFSItem> items = versionContainer.getItems(new VersionsFilter(filename));
		Assert.assertEquals(1, items.size());
		
		//check number of versions
		VFSItem reloadFile = rootTest.resolve(filename);
		VFSMetadata metadata = vfsRepositoryService.getMetadataFor(reloadFile);
		List<VFSRevision> revisions = vfsRepositoryService.getRevisions(metadata);
		Assert.assertNotNull(revisions);
		Assert.assertEquals(3, revisions.size());
	}
	
	@Test
	public void container_copyFrom() throws IOException {
		Identity id = JunitTestHelper.createAndPersistIdentityAsRndUser("vers-11");
		
		//create a file
		VFSContainer rootTest = VFSManager.olatRootContainer("/test_" + UUID.randomUUID(), null);
		VFSContainer targetRootTest = rootTest.createChildContainer("Copy");
		String filename = UUID.randomUUID().toString() + ".txt";
		VFSLeaf file = rootTest.createChildLeaf(filename);
		int byteCopied = copyTestTxt(file);
		Assert.assertNotEquals(0, byteCopied);
		
		//save a first version
		for(int i=0; i<2; i++) {
			InputStream inv = new ByteArrayInputStream(("Hello version " + i).getBytes());
			vfsRepositoryService.addVersion(file, id, false, "Version " + (1 +i), inv);
			inv.close();
		}

		targetRootTest.copyFrom(file, null);
		dbInstance.commitAndCloseSession();
		
		VFSItem targetFile = targetRootTest.resolve(filename);
		VFSMetadata targetMetadata = vfsRepositoryService.getMetadataFor(targetFile);
		List<VFSRevision> revisions = vfsRepositoryService.getRevisions(targetMetadata);
		Assert.assertNotNull(revisions);
		Assert.assertEquals(2, revisions.size());
		for(VFSRevision revision:revisions) {
			VFSItem revFile = targetRootTest.resolve(((VFSRevisionImpl)revision).getFilename());
			Assert.assertNotNull(revFile);
			Assert.assertTrue(revFile.exists());
			Assert.assertTrue(revFile instanceof VFSLeaf);
			Assert.assertTrue(((VFSLeaf)revFile).getSize() > 4);
		}
	}
	
	/**
	 * The test create an original file and 3 revisions with exactly
	 * the same content. We delete the original and the first version.
	 * We check that version 2 and 3 survives and that the file exists.
	 * 
	 * @throws IOException
	 */
	@Test
	public void deleteVersions_withSameFile() throws IOException, URISyntaxException {
		VFSContainer rootTest = VFSManager.olatRootContainer("/ver-" + UUID.randomUUID(), null);
		String filename = UUID.randomUUID().toString() + ".txt";
		VFSLeaf file = rootTest.createChildLeaf(filename);
		int byteCopied = copyTestTxt(file);
		Assert.assertNotEquals(0, byteCopied);
		
		Identity id = JunitTestHelper.createAndPersistIdentityAsRndUser("vers-5");
		
		//save a first version
		InputStream in1 = VFSVersioningTest.class.getResourceAsStream("test.txt");
		vfsRepositoryService.addVersion(file, id, false, "Version 1", in1);
		in1.close();
		
		//save a second version
		InputStream in2 = VFSVersioningTest.class.getResourceAsStream("test.txt");
		vfsRepositoryService.addVersion(file, id, false, "Version 2", in2);
		in2.close();
		
		//save a third version
		InputStream in3 = VFSVersioningTest.class.getResourceAsStream("test.txt");
		vfsRepositoryService.addVersion(file, id, false, "Version 3", in3);
		in3.close();
		
		//delete revisions
		VFSItem reloadFile = rootTest.resolve(filename);
		VFSMetadata metadata = vfsRepositoryService.getMetadataFor(reloadFile);
		List<VFSRevision> revisions = vfsRepositoryService.getRevisions(metadata);
		vfsRepositoryService.deleteRevisions(id, revisions.subList(0, 2));
		dbInstance.commitAndCloseSession();
		
		//check number of versions
		List<VFSRevision> trimmedRevisions = vfsRepositoryService.getRevisions(metadata);

		Assert.assertNotNull(trimmedRevisions);
		Assert.assertEquals(1, trimmedRevisions.size());
		//check surviving versions
		Assert.assertEquals("Version 2", trimmedRevisions.get(0).getRevisionComment());
		Assert.assertEquals("Version 3", metadata.getRevisionComment());
		//check that the last backup file exists
		VFSRevisionImpl revision2 = (VFSRevisionImpl)trimmedRevisions.get(0);
		VFSItem revision2File =  reloadFile.getParentContainer().resolve(revision2.getFilename());
		Assert.assertNotNull(revision2File);
		Assert.assertTrue(revision2File.exists());
		Assert.assertTrue(revision2File instanceof VFSLeaf);
		
		//check if there is only one backup file
		VFSContainer versionContainer = reloadFile.getParentContainer();
		List<VFSItem> items = versionContainer.getItems(new VersionsFilter(filename));
		Assert.assertEquals(1, items.size());
	}
	
	/**
	 * The test create an original file and 5 versions. It manually
	 * delete the physical back up file. We delete the versions
	 * of the orginal, 1 and 2. At the end, there is only version 4
	 * and 5.
	 * 
	 * 
	 * @throws IOException
	 */
	@Test
	public void deleteVersions_withMissingRevisionFile() throws IOException {
		VFSContainer rootTest = VFSManager.olatRootContainer("/ver-" + UUID.randomUUID(), null);
		String filename = UUID.randomUUID().toString() + ".txt";
		VFSLeaf file = rootTest.createChildLeaf(filename);
		int byteCopied = copyTestTxt(file);
		Assert.assertNotEquals(0, byteCopied);
		
		Identity id2 = JunitTestHelper.createAndPersistIdentityAsRndUser("vers-6");
		
		//save a first version
		InputStream in1 = new ByteArrayInputStream("Hello version 1".getBytes());
		vfsRepositoryService.addVersion(file, id2, false, "Version 1", in1);
		in1.close();
		
		//save a second version
		InputStream in2 = new ByteArrayInputStream("Hello version 2".getBytes());
		vfsRepositoryService.addVersion(file, id2, false, "Version 2", in2);
		in2.close();
		
		//save a third version
		InputStream in3 = new ByteArrayInputStream("Hello version 3".getBytes());
		vfsRepositoryService.addVersion(file, id2, false, "Version 3", in3);
		in3.close();
		
		//save a fourth version
		InputStream in4 = new ByteArrayInputStream("Hello version 4".getBytes());
		vfsRepositoryService.addVersion(file, id2, false, "Version 4", in4);
		in4.close();
		
		//save a fourth version
		InputStream in5 = new ByteArrayInputStream("Hello version 5".getBytes());
		vfsRepositoryService.addVersion(file, id2, false, "Version 5", in5);
		in5.close();
		
		dbInstance.commitAndCloseSession();
		
		//delete a specific
		VFSMetadata metadata = vfsRepositoryService.getMetadataFor(file);
		List<VFSRevision> revisions = vfsRepositoryService.getRevisions(metadata);
		VFSRevision rev4 = revisions.get(3);
		VFSRevisionImpl toDeleteVersionImpl = (VFSRevisionImpl)rev4;
		VFSItem itemToDelete = file.getParentContainer().resolve(toDeleteVersionImpl.getFilename());
		itemToDelete.deleteSilently();
		
		//delete revisions
		List<VFSRevision> toDelete = new ArrayList<>(revisions.subList(0, 3));
		vfsRepositoryService.deleteRevisions(id2, toDelete);
		
		dbInstance.commitAndCloseSession();
		
		//check number of versions
		VFSItem reloadFile = rootTest.resolve(filename);
		VFSMetadata reloadedMetadata = vfsRepositoryService.getMetadataFor(reloadFile);
		List<VFSRevision> reloadedRevisions = vfsRepositoryService.getRevisions(reloadedMetadata);

		Assert.assertNotNull(reloadedRevisions);
		Assert.assertEquals(1, reloadedRevisions.size());
		VFSRevision revision = reloadedRevisions.get(0);
		Assert.assertEquals("Version 4", revision.getRevisionComment());
		Assert.assertEquals("Version 5", reloadedMetadata.getRevisionComment());
		
		//check if there is only one backup file
		VFSContainer versionContainer = file.getParentContainer();
		List<VFSItem> items = versionContainer.getItems(new VersionsFilter(filename));
		Assert.assertEquals(1, items.size());
	}
	
	@Test
	public void verionsWithAuthorsAndCreators() throws IOException {
		//create a file
		VFSContainer rootTest = VFSManager.olatRootContainer("/ver" + UUID.randomUUID(), null);
		String filename = UUID.randomUUID().toString() + ".txt";
		VFSLeaf file = rootTest.createChildLeaf(filename);
		int byteCopied = copyTestTxt(file);
		Assert.assertNotEquals(0, byteCopied);
		Assert.assertEquals(VFSConstants.YES, file.canMeta());
		
		Identity id1 = JunitTestHelper.createAndPersistIdentityAsRndUser("vers-7");
		Identity id2 = JunitTestHelper.createAndPersistIdentityAsRndUser("vers-8");
		
		//set the author
		vfsRepositoryService.itemSaved(file, id1);
		VFSMetadata metadata = vfsRepositoryService.getMetadataFor(file);
		metadata.setCreator(id1.getName());
		metadata = vfsRepositoryService.updateMetadata(metadata);
		dbInstance.commitAndCloseSession();
		
		//save a first version -> id2
		InputStream in1 = new ByteArrayInputStream("Hello version 1".getBytes());
		vfsRepositoryService.addVersion(file, id2, false, "Version 1", in1);
		in1.close();
		
		//save a second version -> id1
		InputStream in2 = new ByteArrayInputStream("Hello version 2".getBytes());
		vfsRepositoryService.addVersion(file, id1, false, "Version 2", in2);
		in2.close();
		
		//save a third version -> id2
		InputStream in3 = new ByteArrayInputStream("Hello version 3".getBytes());
		vfsRepositoryService.addVersion(file, id2, false, "Version 3", in3);
		in3.close();

		//make the checks
		VFSItem retrievedFile = rootTest.resolve(filename);
		metadata = vfsRepositoryService.getMetadataFor(file);	
		List<VFSRevision> revisions = vfsRepositoryService.getRevisions(metadata);
		Assert.assertNotNull(revisions);
		Assert.assertEquals(3, revisions.size());
		Assert.assertEquals(VFSConstants.YES, retrievedFile.canMeta());
		
		VFSRevision revision0 = revisions.get(0);
		//we don't set an author for the original file
		Assert.assertEquals(id1, revision0.getFileInitializedBy());
		VFSRevision revision1 = revisions.get(1);
		Assert.assertEquals(id2, revision1.getFileInitializedBy());
		VFSRevision revision2 = revisions.get(2);
		Assert.assertEquals(id1, revision2.getFileInitializedBy());
		//current
		Assert.assertEquals(id2, metadata.getFileInitializedBy());
	}
	
	@Test
	public void deleteLeaf() throws IOException {
		//create a file
		VFSContainer rootTest = VFSManager.olatRootContainer("/ver" + UUID.randomUUID(), null);
		String filename = UUID.randomUUID().toString() + ".txt";
		VFSLeaf file = rootTest.createChildLeaf(filename);
		int byteCopied = copyTestTxt(file);
		Assert.assertNotEquals(0, byteCopied);
		Assert.assertEquals(VFSConstants.YES, file.canMeta());
		
		Identity id = JunitTestHelper.createAndPersistIdentityAsRndUser("vers-12");
		
		//save a first version -> id
		InputStream in1 = new ByteArrayInputStream("Hello version 1".getBytes());
		vfsRepositoryService.addVersion(file, id, false, "Version 1", in1);
		in1.close();
		
		dbInstance.commitAndCloseSession();
		VFSMetadata metadata = vfsRepositoryService.getMetadataFor(file);
		
		
		// make sure the version is there
		List<VFSItem> revFiles = file.getParentContainer().getItems(new VersionsFilter(filename));
		Assert.assertEquals(1, revFiles.size());
		
		// delete all metadata and versions
		file.deleteSilently();
		dbInstance.commitAndCloseSession();
		
		List<VFSRevision> revisions = vfsRepositoryService.getRevisions(metadata);
		Assert.assertTrue(revisions.isEmpty());
		List<VFSItem> deletedRevFiles = file.getParentContainer().getItems(new VersionsFilter(filename));
		Assert.assertTrue(deletedRevFiles.isEmpty());
	}
	
	@Test
	public void rename_leaf() throws IOException {
		//create a file
		VFSContainer rootTest = VFSManager.olatRootContainer("/ver" + UUID.randomUUID(), null);
		String filename = UUID.randomUUID().toString() + ".txt";
		VFSLeaf file = rootTest.createChildLeaf(filename);
		int byteCopied = copyTestTxt(file);
		Assert.assertNotEquals(0, byteCopied);
		Assert.assertEquals(VFSConstants.YES, file.canMeta());
		
		Identity id = JunitTestHelper.createAndPersistIdentityAsRndUser("vers-12");
		
		//save a first version -> id
		InputStream in1 = new ByteArrayInputStream("Hello, move me".getBytes());
		vfsRepositoryService.addVersion(file, id, false, "Version 1", in1);
		in1.close();
		dbInstance.commitAndCloseSession();
		
		VFSMetadata metadata = vfsRepositoryService.getMetadataFor(file);

		// make sure the version is there
		List<VFSItem> revFiles = file.getParentContainer().getItems(new VersionsFilter(filename));
		Assert.assertEquals(1, revFiles.size());
		
		// delete all metadata and versions
		String newName = "IMoved.txt";
		file.rename(newName);
		dbInstance.commitAndCloseSession();
		
		List<VFSRevision> revisions = vfsRepositoryService.getRevisions(metadata);
		Assert.assertEquals(1, revisions.size());
		List<VFSItem> movedRevFiles = file.getParentContainer().getItems(new VersionsFilter(newName));
		Assert.assertEquals(1, movedRevFiles.size());
		Assert.assertEquals(((VFSRevisionImpl)revisions.get(0)).getFilename(), movedRevFiles.get(0).getName());
	}
	
	@Test
	public void testMove() throws IOException {
		//create a file
		VFSContainer rootTest = VFSManager.olatRootContainer("/ver" + UUID.randomUUID(), null);
		String filename = UUID.randomUUID().toString() + ".txt";
		VFSLeaf file = rootTest.createChildLeaf(filename);
		int byteCopied = copyTestTxt(file);
		Assert.assertNotEquals(0, byteCopied);
		Assert.assertEquals(VFSConstants.YES, file.canMeta());
		
		Identity id1 = JunitTestHelper.createAndPersistIdentityAsRndUser("vers-9");
		Identity id2 = JunitTestHelper.createAndPersistIdentityAsRndUser("vers-10");
		
		
		//set the author
		vfsRepositoryService.itemSaved(file, id1);
		VFSMetadata metadata = vfsRepositoryService.getMetadataFor(file);
		metadata.setCreator(id1.getName());
		metadata = vfsRepositoryService.updateMetadata(metadata);
		
		//save a first version -> id2
		InputStream in1 = new ByteArrayInputStream("Hello version 1".getBytes());
		vfsRepositoryService.addVersion(file, id2, false, "Version 1", in1);
		in1.close();
		
		//save a second version -> id1
		InputStream in2 = new ByteArrayInputStream("Hello version 2".getBytes());
		vfsRepositoryService.addVersion(file, id1, false, "Version 2", in2);
		in2.close();
		
		//move the file
		VFSLeaf retrievedLeaf = (VFSLeaf)rootTest.resolve(filename);
		String copyFilename = UUID.randomUUID().toString() + ".txt";
		VFSLeaf copyFile = rootTest.createChildLeaf(copyFilename);
		OutputStream copyOutput = copyFile.getOutputStream(false);
		InputStream copyInput = retrievedLeaf.getInputStream();
		IOUtils.copy(copyInput, copyOutput);
		copyOutput.close();
		copyInput.close();
		//move the revisions
		vfsRepositoryService.move(retrievedLeaf, copyFile, id2);
		
		//check if the revisions are moved
		VFSLeaf retirevedCopyFile = (VFSLeaf)rootTest.resolve(copyFilename);
		VFSMetadata metadataCopy = vfsRepositoryService.getMetadataFor(retirevedCopyFile);	
		List<VFSRevision> revisions = vfsRepositoryService.getRevisions(metadataCopy);
		Assert.assertNotNull(revisions);
		Assert.assertEquals(2, revisions.size());
		
		VFSRevision revision0 = revisions.get(0);
		//we don't set an author for the original file
		assertEquals(id1, revision0.getFileInitializedBy());
		VFSRevision revision1 = revisions.get(1);
		assertEquals(id2, revision1.getFileInitializedBy());
		//current
		assertEquals(id1.getName(), metadataCopy.getCreator());
		assertEquals(id2, metadataCopy.getFileInitializedBy());
	}
	
	@Test
	public void restore() throws IOException {
		//create a file
		VFSContainer rootTest = VFSManager.olatRootContainer("/ver" + UUID.randomUUID(), null);
		String filename = UUID.randomUUID().toString() + ".txt";
		VFSLeaf file = rootTest.createChildLeaf(filename);
		int byteCopied = copyTestTxt(file);
		Assert.assertNotEquals(0, byteCopied);
		Assert.assertEquals(VFSConstants.YES, file.canMeta());
		
		Identity id = JunitTestHelper.createAndPersistIdentityAsRndUser("vers-12");
		
		//save a first version -> id
		InputStream in1 = new ByteArrayInputStream("Hello 1".getBytes());
		vfsRepositoryService.addVersion(file, id, false, "Version 1", in1);
		in1.close();
		dbInstance.commitAndCloseSession();
		// save version 2
		InputStream in2 = new ByteArrayInputStream("Hello 2".getBytes());
		vfsRepositoryService.addVersion(file, id, false, "Version 2", in2);
		in2.close();
		dbInstance.commitAndCloseSession();
		// save version 3
		InputStream in3 = new ByteArrayInputStream("Hello 3".getBytes());
		vfsRepositoryService.addVersion(file, id, false, "Version 3", in3);
		in3.close();
		dbInstance.commitAndCloseSession();
		
		// get the revisions
		VFSMetadata metadata = vfsRepositoryService.getMetadataFor(file);
		List<VFSRevision> revisions = vfsRepositoryService.getRevisions(metadata);
		
		// restore the original file
		VFSRevision toRestore = revisions.get(0);
		boolean restored = vfsRepositoryService.restoreRevision(id, toRestore, "Restore");
		Assert.assertTrue(restored);
		
		String restoredData = null;
		try(InputStream in = file.getInputStream()) {
			restoredData = IOUtils.toString(in, "UTF-8");
		} catch(IOException e) {
			log.error("", e);
		}
		Assert.assertNotNull(restoredData);
		Assert.assertEquals("Hello", restoredData);
	}
	
	@Test
	public void restore_metadata() throws IOException {
		//create a file
		VFSContainer rootTest = VFSManager.olatRootContainer("/ver" + UUID.randomUUID(), null);
		String filename = UUID.randomUUID().toString() + ".txt";
		VFSLeaf file = rootTest.createChildLeaf(filename);
		int byteCopied = copyTestTxt(file);
		Assert.assertNotEquals(0, byteCopied);
		Assert.assertEquals(VFSConstants.YES, file.canMeta());
		
		Identity id = JunitTestHelper.createAndPersistIdentityAsRndUser("vers-12");
		
		// set metadata
		VFSMetadata metadata = vfsRepositoryService.getMetadataFor(file);
		metadata.setComment("Initital version 0");
		metadata.setPublisher("frentix GmbH");
		vfsRepositoryService.updateMetadata(metadata);
		dbInstance.commitAndCloseSession();
		
		//save a first version -> id
		InputStream in1 = new ByteArrayInputStream("Hello 1".getBytes());
		vfsRepositoryService.addVersion(file, id, false, "Version 1", in1);
		in1.close();
		dbInstance.commitAndCloseSession();
		
		// set metadata for version 1
		metadata = vfsRepositoryService.getMetadataFor(file);
		metadata.setComment("Initital version 1");
		metadata.setPublisher("OpenOLAT org.");
		vfsRepositoryService.updateMetadata(metadata);
		dbInstance.commitAndCloseSession();
		
		// save version 2
		InputStream in2 = new ByteArrayInputStream("Hello 2".getBytes());
		vfsRepositoryService.addVersion(file, id, false, "Version 2", in2);
		in2.close();
		dbInstance.commitAndCloseSession();
		// save version 3
		InputStream in3 = new ByteArrayInputStream("Hello 3".getBytes());
		vfsRepositoryService.addVersion(file, id, false, "Version 3", in3);
		in3.close();
		dbInstance.commitAndCloseSession();
		
		// get the revisions
		metadata = vfsRepositoryService.getMetadataFor(file);
		List<VFSRevision> revisions = vfsRepositoryService.getRevisions(metadata);
		
		// restore the original file
		VFSRevision toRestore = revisions.get(0);
		boolean restored = vfsRepositoryService.restoreRevision(id, toRestore, "Restore");
		Assert.assertTrue(restored);
		dbInstance.commitAndCloseSession();
		
		// check the restored metadata
		VFSMetadata restoredMetadata = vfsRepositoryService.getMetadataFor(file);
		Assert.assertNotNull(restoredMetadata);
		Assert.assertEquals("Initital version 0", restoredMetadata.getComment());
	}
	
	private int copyTestTxt(VFSLeaf file) {
		try(OutputStream out = file.getOutputStream(false);
				InputStream in = VFSVersioningTest.class.getResourceAsStream("test.txt")) {
			return IOUtils.copy(in, out);
		} catch(IOException e) {
			return -1;
		}
	}
	

	private static class SetMaxNumberOfVersions implements Callable<Boolean> {
		
		private final Long maxNumOfVersions;
		private final VFSVersionModule versionConfig;
		
		public SetMaxNumberOfVersions(VFSVersionModule versionConfig, Long maxNumOfVersions) {
			this.versionConfig = versionConfig;
			this.maxNumOfVersions = maxNumOfVersions;
		}

		@Override
		public Boolean call() throws Exception {
			int currentValue = versionConfig.getMaxNumberOfVersions();
			return currentValue == maxNumOfVersions.longValue();
		}
	}
	
	private static class VersionsFilter implements VFSItemFilter {
		
		private final String filename;
		
		public VersionsFilter(String filename) {
			this.filename = filename;
		}

		@Override
		public boolean accept(VFSItem item) {
			if(item instanceof VFSLeaf && item.isHidden() && item.getName().endsWith(filename)) {
				return true;
			}
			return false;
		}
		
	}
}
