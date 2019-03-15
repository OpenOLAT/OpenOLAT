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
package org.olat.core.util.vfs.version;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.Callable;

import org.apache.commons.io.IOUtils;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.olat.core.CoreSpringFactory;
import org.olat.core.commons.services.vfs.VFSMetadata;
import org.olat.core.commons.services.vfs.VFSRepositoryService;
import org.olat.core.id.Identity;
import org.olat.core.util.vfs.VFSConstants;
import org.olat.core.util.vfs.VFSContainer;
import org.olat.core.util.vfs.VFSItem;
import org.olat.core.util.vfs.VFSLeaf;
import org.olat.core.util.vfs.VFSManager;
import org.olat.core.util.vfs.filters.SystemItemFilter;
import org.olat.test.JunitTestHelper;
import org.olat.test.OlatTestCase;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Description:<br>
 * 
 * <P>
 * Initial Date:  23 déc. 2011 <br>
 *
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
public class VersionManagerTest extends OlatTestCase {
	
	private static boolean setuped = false;
	
	private static Identity id1, id2;
	
	@Autowired
	private VersionsFileManager versionsManager;
	@Autowired
	private SimpleVersionConfig versioningConfigurator;
	@Autowired
	private VFSRepositoryService vfsRepositoryService;
	
	@Before
	public void setUp() throws Exception {
		if(setuped) return;
		
		//create identities
		id1 = JunitTestHelper.createAndPersistIdentityAsUser("vm-one" + getRandomName());
		id2 = JunitTestHelper.createAndPersistIdentityAsUser("vm-two" + getRandomName());
		
		SimpleVersionConfig versionConfig = (SimpleVersionConfig)CoreSpringFactory.getBean("versioningConfigurator");
		versionConfig.setMaxNumberOfVersionsProperty(10);
		waitForCondition(new SetMaxNumberOfVersions(versionConfig,  10l), 2000);
		setuped = true;
	}
	
	@After
	public void resetMaxVersions() {
		int maxNumberOfVersions = versioningConfigurator.getMaxNumberOfVersionsProperty();
		if(maxNumberOfVersions != 10) {
			versioningConfigurator.setMaxNumberOfVersionsProperty(10);
			waitForCondition(new SetMaxNumberOfVersions(versioningConfigurator,  10l), 2000);
		}
	}
	
	private static class SetMaxNumberOfVersions implements Callable<Boolean> {
		
		private final Long maxNumOfVersions;
		private final SimpleVersionConfig versioningConfig;
		
		public SetMaxNumberOfVersions(SimpleVersionConfig versioningConfig, Long maxNumOfVersions) {
			this.versioningConfig = versioningConfig;
			this.maxNumOfVersions = maxNumOfVersions;
		}

		@Override
		public Boolean call() throws Exception {
			int currentValue = versioningConfig.getMaxNumberOfVersionsProperty();
			return currentValue == maxNumOfVersions.longValue();
		}
	}
	
	@Test
	public void testVersionManager() throws IOException {
		//create a file
		VFSContainer rootTest = VFSManager.olatRootContainer("/test", null);
		String filename = getRandomName();
		VFSLeaf file = rootTest.createChildLeaf(filename);
		int byteCopied = copyTestTxt(file);
		assertFalse(byteCopied == 0);
		assertTrue(file instanceof Versionable);
		
		//save a first version
		Versionable versionedFile1 = (Versionable)file;
		InputStream in1 = new ByteArrayInputStream("Hello version 1".getBytes());
		versionedFile1.getVersions().addVersion(id2, "Version 1", in1);
		in1.close();
		
		//save a second version
		Versionable versionedFile2 = (Versionable)file;
		InputStream in2 = new ByteArrayInputStream("Hello version 2".getBytes());
		versionedFile2.getVersions().addVersion(id2, "Version 2", in2);
		in2.close();
		
		//save a third version
		Versionable versionedFile3 = (Versionable)file;
		InputStream in3 = new ByteArrayInputStream("Hello version 3".getBytes());
		versionedFile3.getVersions().addVersion(id2, "Version 3", in3);
		in3.close();

		//make the checks
		VFSItem retrievedFile = rootTest.resolve(filename);
		assertTrue(retrievedFile instanceof Versionable);
		Versions versions = versionsManager.createVersionsFor((VFSLeaf)retrievedFile);	
		List<VFSRevision> revisions = versions.getRevisions();
		assertNotNull(revisions);
		assertEquals(3, revisions.size());
		
		VFSRevision revision0 = revisions.get(0);
		//we don't set an author for the original file
		assertNull(null, revision0.getAuthor());
		VFSRevision revision1 = revisions.get(1);
		assertEquals(id2.getName(), revision1.getAuthor());
		VFSRevision revision2 = revisions.get(2);
		assertEquals(id2.getName(), revision2.getAuthor());

		//check the comments
		assertNull(revision0.getComment());	
		assertEquals("Version 1", revision1.getComment());
		assertEquals("Version 2", revision2.getComment());
		assertEquals("Version 3", versions.getComment());
	}
	
	@Test
	public void testOverflow_lowLevel() throws IOException {
		versioningConfigurator.setMaxNumberOfVersionsProperty(3);
		waitForCondition(new SetMaxNumberOfVersions(versioningConfigurator, 3l), 2000);
		
		//create a file
		VFSContainer rootTest = VFSManager.olatRootContainer("/test_" + UUID.randomUUID(), null);
		String filename = getRandomName();
		VFSLeaf file = rootTest.createChildLeaf(filename);
		int byteCopied = copyTestTxt(file);
		assertFalse(byteCopied == 0);
		assertTrue(file instanceof Versionable);
		
		//save a first version
		for(int i=0; i<5; i++) {
			Versionable versionedFile = (Versionable)file;
			InputStream inv = new ByteArrayInputStream(("Hello version " + i).getBytes());
			versionedFile.getVersions().addVersion(id2, "Version " + (1 +i), inv);
			inv.close();
		}

		VFSItem retrievedFile = rootTest.resolve(filename);
		Versions versions = versionsManager.createVersionsFor((VFSLeaf)retrievedFile);	
		List<VFSRevision> revisions = versions.getRevisions();
		assertNotNull(revisions);
		assertEquals(3, revisions.size());
		assertEquals("3", revisions.get(0).getRevisionNr());
		assertEquals("4", revisions.get(1).getRevisionNr());
		assertEquals("5", revisions.get(2).getRevisionNr());

		assertEquals("Version 5", versions.getComment());
		assertEquals(id2.getName(), revisions.get(2).getAuthor());
	}
	
	@Test
	public void testOverflow_lowLevel_deactivated() throws IOException {
		versioningConfigurator.setMaxNumberOfVersionsProperty(0);
		waitForCondition(new SetMaxNumberOfVersions(versioningConfigurator,  0l), 2000);
		
		//create a file
		VFSContainer rootTest = VFSManager.olatRootContainer("/test_" + UUID.randomUUID(), null);
		String filename = getRandomName();
		VFSLeaf file = rootTest.createChildLeaf(filename);
		int byteCopied = copyTestTxt(file);
		assertFalse(byteCopied == 0);
		assertTrue(file instanceof Versionable);
		
		//save a first version
		for(int i=0; i<5; i++) {
			Versionable versionedFile = (Versionable)file;
			InputStream inv = new ByteArrayInputStream(("Hello version " + i).getBytes());
			versionedFile.getVersions().addVersion(id2, "Version " + (1 +i), inv);
			inv.close();
		}

		VFSItem retrievedFile = rootTest.resolve(filename);
		Versions versions = versionsManager.createVersionsFor((VFSLeaf)retrievedFile);	
		List<VFSRevision> revisions = versions.getRevisions();
		assertNotNull(revisions);
		assertTrue(revisions.isEmpty());
	}
	
	@Test
	public void testVersionChecksum() throws IOException {
		VFSContainer rootTest = VFSManager.olatRootContainer("/ver-" + UUID.randomUUID(), null);
		String filename = getRandomName();
		VFSLeaf file = rootTest.createChildLeaf(filename);
		int byteCopied = copyTestTxt(file);
		assertFalse(byteCopied == 0);
		assertTrue(file instanceof Versionable);
		
		
		//save a first version
		Versionable versionedFile = (Versionable)file;
		InputStream in1 = VersionManagerTest.class.getResourceAsStream("test.txt");
		versionedFile.getVersions().addVersion(id2, "Version 1", in1);
		in1.close();
		
		//save a second version
		InputStream in2 = VersionManagerTest.class.getResourceAsStream("test.txt");
		versionedFile.getVersions().addVersion(id2, "Version 2", in2);
		in2.close();
		
		//save a third version
		InputStream in3 = VersionManagerTest.class.getResourceAsStream("test.txt");
		versionedFile.getVersions().addVersion(id2, "Version 3", in2);
		in3.close();
		
		//check if there is only one backup file
		VFSContainer versionContainer = versionsManager.getCanonicalVersionFolder(file.getParentContainer(), false);
		Assert.assertNotNull(versionContainer);
		List<VFSItem> items = versionContainer.getItems(new SystemItemFilter());
		Assert.assertNotNull(items);
		Assert.assertEquals(2, items.size());
		
		//check number of versions
		VFSItem reloadFile = rootTest.resolve(filename);
		assertTrue(reloadFile instanceof Versionable);
		Versionable reloadedVersionedFile = (Versionable)reloadFile;
		List<VFSRevision> revisions = reloadedVersionedFile.getVersions().getRevisions();
		Assert.assertNotNull(revisions);
		Assert.assertEquals(3, revisions.size());
	}
	
	/**
	 * The test create an original file and 3 revisions with exactly
	 * the same content. We delete the original and the first version.
	 * We check that version 2 and 3 survives and that the file exists.
	 * 
	 * @throws IOException
	 */
	@Test
	public void testDeleteRevisions_withSameFile() throws IOException {
		VFSContainer rootTest = VFSManager.olatRootContainer("/ver-" + UUID.randomUUID(), null);
		String filename = getRandomName();
		VFSLeaf file = rootTest.createChildLeaf(filename);
		int byteCopied = copyTestTxt(file);
		assertFalse(byteCopied == 0);
		assertTrue(file instanceof Versionable);
		
		//save a first version
		Versionable versionedFile = (Versionable)file;
		InputStream in1 = VersionManagerTest.class.getResourceAsStream("test.txt");
		versionedFile.getVersions().addVersion(id2, "Version 1", in1);
		in1.close();
		
		//save a second version
		InputStream in2 = VersionManagerTest.class.getResourceAsStream("test.txt");
		versionedFile.getVersions().addVersion(id2, "Version 2", in2);
		in2.close();
		
		//save a third version
		InputStream in3 = VersionManagerTest.class.getResourceAsStream("test.txt");
		versionedFile.getVersions().addVersion(id2, "Version 3", in2);
		in3.close();
		
		//delete revisions
		versionsManager.deleteRevisions(versionedFile, versionedFile.getVersions().getRevisions().subList(0, 2));
		
		//check number of versions
		VFSItem reloadFile = rootTest.resolve(filename);
		assertTrue(reloadFile instanceof Versionable);
		Versionable reloadedVersionedFile = (Versionable)reloadFile;
		List<VFSRevision> revisions = reloadedVersionedFile.getVersions().getRevisions();
		Assert.assertNotNull(revisions);
		Assert.assertEquals(1, revisions.size());
		//check surviving versions
		Assert.assertEquals("Version 2", revisions.get(0).getComment());
		Assert.assertEquals("Version 3", reloadedVersionedFile.getVersions().getComment());
		//check that the last backup file exists
		RevisionFileImpl revision2 = (RevisionFileImpl)revisions.get(0);
		VFSLeaf revision2File = revision2.getFile();
		Assert.assertNotNull(revision2File);
		Assert.assertTrue(revision2File.exists());
		
		//check if there is only one backup file
		VFSContainer versionContainer = versionsManager.getCanonicalVersionFolder(file.getParentContainer(), false);
		Assert.assertNotNull(versionContainer);
		List<VFSItem> items = versionContainer.getItems(new SystemItemFilter());
		Assert.assertNotNull(items);
		Assert.assertEquals(2, items.size());
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
	public void testDeleteRevisions_withMissingRevisionFile() throws IOException {
		VFSContainer rootTest = VFSManager.olatRootContainer("/ver-" + UUID.randomUUID(), null);
		String filename = getRandomName();
		VFSLeaf file = rootTest.createChildLeaf(filename);
		int byteCopied = copyTestTxt(file);
		assertFalse(byteCopied == 0);
		assertTrue(file instanceof Versionable);
		
		//save a first version
		Versionable versionedFile = (Versionable)file;
		InputStream in1 = new ByteArrayInputStream("Hello version 1".getBytes());
		versionedFile.getVersions().addVersion(id2, "Version 1", in1);
		in1.close();
		
		//save a second version
		InputStream in2 = new ByteArrayInputStream("Hello version 2".getBytes());
		versionedFile.getVersions().addVersion(id2, "Version 2", in2);
		in2.close();
		
		//save a third version
		InputStream in3 = new ByteArrayInputStream("Hello version 3".getBytes());
		versionedFile.getVersions().addVersion(id2, "Version 3", in3);
		in3.close();
		
		//save a fourth version
		InputStream in4 = new ByteArrayInputStream("Hello version 4".getBytes());
		versionedFile.getVersions().addVersion(id2, "Version 4", in4);
		in4.close();
		
		//save a fourth version
		InputStream in5 = new ByteArrayInputStream("Hello version 5".getBytes());
		versionedFile.getVersions().addVersion(id2, "Version 5", in5);
		in5.close();
		
		//delete a specific
		VFSRevision rev3 = versionedFile.getVersions().getRevisions().get(3);
		RevisionFileImpl toDeleteVersionImpl = (RevisionFileImpl)rev3;
		VFSContainer versionContainerAlt = versionsManager.getCanonicalVersionFolder(rootTest, false);
		VFSItem itemToDelete = versionContainerAlt.resolve(toDeleteVersionImpl.getFilename());
		itemToDelete.deleteSilently();
		
		//delete revisions
		List<VFSRevision> toDelete = new ArrayList<>(versionedFile.getVersions().getRevisions().subList(0, 3));
		versionsManager.deleteRevisions(versionedFile, toDelete);
		
		//check number of versions
		VFSItem reloadFile = rootTest.resolve(filename);
		assertTrue(reloadFile instanceof Versionable);
		Versionable reloadedVersionedFile = (Versionable)reloadFile;
		List<VFSRevision> revisions = reloadedVersionedFile.getVersions().getRevisions();
		Assert.assertNotNull(revisions);
		Assert.assertEquals(1, revisions.size());
		VFSRevision revision = revisions.get(0);
		Assert.assertEquals("Version 4", revision.getComment());
		Assert.assertEquals("Version 5", reloadedVersionedFile.getVersions().getComment());
		
		//check if there is only one backup file
		VFSContainer versionContainer = versionsManager.getCanonicalVersionFolder(file.getParentContainer(), false);
		Assert.assertNotNull(versionContainer);
		List<VFSItem> items = versionContainer.getItems(new SystemItemFilter());
		Assert.assertNotNull(items);
		Assert.assertEquals(2, items.size());
	}
	
	@Test
	public void testAuthorsAndCreators() throws IOException {
		//create a file
		VFSContainer rootTest = VFSManager.olatRootContainer("/test2", null);
		String filename = getRandomName();
		VFSLeaf file = rootTest.createChildLeaf(filename);
		int byteCopied = copyTestTxt(file);
		assertFalse(byteCopied == 0);
		assertTrue(file instanceof Versionable);
		assertEquals(VFSConstants.YES, file.canMeta());
		
		//set the author
		VFSMetadata metaInfo = file.getMetaInfo();
		metaInfo.setAuthor(id1);
		metaInfo.setCreator(id1.getName());
		metaInfo = vfsRepositoryService.updateMetadata(metaInfo);
		
		//save a first version -> id2
		Versionable versionedFile1 = (Versionable)file;
		InputStream in1 = new ByteArrayInputStream("Hello version 1".getBytes());
		versionedFile1.getVersions().addVersion(id2, "Version 1", in1);
		in1.close();
		
		//save a second version -> id1
		Versionable versionedFile2 = (Versionable)file;
		InputStream in2 = new ByteArrayInputStream("Hello version 2".getBytes());
		versionedFile2.getVersions().addVersion(id1, "Version 2", in2);
		in2.close();
		
		//save a third version -> id2
		Versionable versionedFile3 = (Versionable)file;
		InputStream in3 = new ByteArrayInputStream("Hello version 3".getBytes());
		versionedFile3.getVersions().addVersion(id2, "Version 3", in3);
		in3.close();

		//make the checks
		VFSItem retrievedFile = rootTest.resolve(filename);
		assertTrue(retrievedFile instanceof Versionable);
		assertTrue(retrievedFile instanceof VFSLeaf);
		Versions versions = versionsManager.createVersionsFor((VFSLeaf)retrievedFile);	
		List<VFSRevision> revisions = versions.getRevisions();
		assertNotNull(revisions);
		assertEquals(3, revisions.size());
		assertEquals(VFSConstants.YES, retrievedFile.canMeta());
		
		VFSRevision revision0 = revisions.get(0);
		//we don't set an author for the original file
		assertEquals(id1.getName(), revision0.getAuthor());
		VFSRevision revision1 = revisions.get(1);
		assertEquals(id2.getName(), revision1.getAuthor());
		VFSRevision revision2 = revisions.get(2);
		assertEquals(id1.getName(), revision2.getAuthor());
		//current
		assertEquals(id2.getName(), versions.getAuthor());
	}
	
	@Test
	public void testMove() throws IOException {
		//create a file
		VFSContainer rootTest = VFSManager.olatRootContainer("/test2", null);
		String filename = getRandomName();
		VFSLeaf file = rootTest.createChildLeaf(filename);
		int byteCopied = copyTestTxt(file);
		assertFalse(byteCopied == 0);
		assertTrue(file instanceof Versionable);
		assertEquals(VFSConstants.YES, file.canMeta());
		
		//set the author
		VFSMetadata metaInfo = file.getMetaInfo();
		metaInfo.setAuthor(id1);
		metaInfo.setCreator(id1.getName());
		metaInfo = vfsRepositoryService.updateMetadata(metaInfo);
		
		//save a first version -> id2
		Versionable versionedFile1 = (Versionable)file;
		InputStream in1 = new ByteArrayInputStream("Hello version 1".getBytes());
		versionedFile1.getVersions().addVersion(id2, "Version 1", in1);
		in1.close();
		
		//save a second version -> id1
		Versionable versionedFile2 = (Versionable)file;
		InputStream in2 = new ByteArrayInputStream("Hello version 2".getBytes());
		versionedFile2.getVersions().addVersion(id1, "Version 2", in2);
		in2.close();
		
		//move the file
		VFSLeaf retrievedLeaf = (VFSLeaf)rootTest.resolve(filename);
		String copyFilename = getRandomName();
		VFSLeaf copyFile = rootTest.createChildLeaf(copyFilename);
		OutputStream copyOutput = copyFile.getOutputStream(false);
		InputStream copyInput = retrievedLeaf.getInputStream();
		IOUtils.copy(copyInput, copyOutput);
		copyOutput.close();
		copyInput.close();
		//move the revisions
		versionsManager.move(retrievedLeaf, copyFile, id2);
		
		//check if the revisions are moved
		VFSLeaf retirevedCopyFile = (VFSLeaf)rootTest.resolve(copyFilename);
		assertTrue(retirevedCopyFile instanceof Versionable);
		Versions versions = versionsManager.createVersionsFor(retirevedCopyFile);	
		List<VFSRevision> revisions = versions.getRevisions();
		assertNotNull(revisions);
		assertEquals(2, revisions.size());
		
		VFSRevision revision0 = revisions.get(0);
		//we don't set an author for the original file
		assertEquals(id1.getName(), revision0.getAuthor());
		VFSRevision revision1 = revisions.get(1);
		assertEquals(id2.getName(), revision1.getAuthor());
		//current
		assertEquals(id1.getName(), versions.getCreator());
		assertEquals(id2.getName(), versions.getAuthor());
	}
	
	/**
	 * Create a file with 2 revision, move it to another name, move it to the primitive name:
	 * File A, change file A, change file A, move to file B, move to file A
	 * @throws IOException
	 */
	@Test
	public void testCircleMove() throws IOException {
		//create a file A
		VFSContainer rootTest = VFSManager.olatRootContainer("/test2", null);
		String filename = getRandomName();
		VFSLeaf file = rootTest.createChildLeaf(filename);
		int byteCopied = copyTestTxt(file);
		assertFalse(byteCopied == 0);
		assertTrue(file instanceof Versionable);
		assertEquals(VFSConstants.YES, file.canMeta());
		
		//set the author
		VFSMetadata metaInfo = file.getMetaInfo();
		metaInfo.setAuthor(id1);
		metaInfo.setCreator(id1.getName());
		metaInfo = vfsRepositoryService.updateMetadata(metaInfo);
		
		//save a first version of file A -> id2
		Versionable versionedFile1 = (Versionable)file;
		InputStream in1 = new ByteArrayInputStream("Hello version 1".getBytes());
		versionedFile1.getVersions().addVersion(id2, "Version 1", in1);
		in1.close();
		
		//save a second version of file A -> id1
		Versionable versionedFile2 = (Versionable)file;
		InputStream in2 = new ByteArrayInputStream("Hello version 2".getBytes());
		versionedFile2.getVersions().addVersion(id1, "Version 2", in2);
		in2.close();
		
		//move the file A -> file B
		VFSLeaf retrievedLeaf = (VFSLeaf)rootTest.resolve(filename);
		String copyFilename = getRandomName();
		VFSLeaf copyFile = rootTest.createChildLeaf(copyFilename);
		OutputStream copyOutput = copyFile.getOutputStream(false);
		InputStream copyInput = retrievedLeaf.getInputStream();
		IOUtils.copy(copyInput, copyOutput);
		copyOutput.close();
		copyInput.close();
		//move the revisions
		versionsManager.move(retrievedLeaf, copyFile, id2);
		
		//move the file B -> file A
		VFSLeaf retrievedCopyLeaf = (VFSLeaf)rootTest.resolve(copyFilename);
		VFSLeaf originalFile = (VFSLeaf)rootTest.resolve(filename);
		OutputStream originalOutput = originalFile.getOutputStream(false);
		InputStream retrievedCopyInput = retrievedCopyLeaf.getInputStream();
		IOUtils.copy(retrievedCopyInput, originalOutput);
		originalOutput.close();
		retrievedCopyInput.close();
		//move the revisions
		versionsManager.move(retrievedCopyLeaf, originalFile, id2);
		
		
		//check if the revisions are moved
		VFSLeaf retirevedOriginalFile = (VFSLeaf)rootTest.resolve(filename);
		assertTrue(retirevedOriginalFile instanceof Versionable);
		Versions versions = versionsManager.createVersionsFor(retirevedOriginalFile);	
		List<VFSRevision> revisions = versions.getRevisions();
		assertNotNull(revisions);
		assertEquals(2, revisions.size());
		
		VFSRevision revision0 = revisions.get(0);
		//we don't set an author for the original file
		assertEquals(id1.getName(), revision0.getAuthor());
		VFSRevision revision1 = revisions.get(1);
		assertEquals(id2.getName(), revision1.getAuthor());
		//current
		assertEquals(id1.getName(), versions.getCreator());
		assertEquals(id2.getName(), versions.getAuthor());
	}
	
	private int copyTestTxt(VFSLeaf file) {
		try(OutputStream out = file.getOutputStream(false);
				InputStream in = VersionManagerTest.class.getResourceAsStream("test.txt")) {
			return IOUtils.copy(in, out);
		} catch(IOException e) {
			return -1;
		}
	}
	
	private String getRandomName() {
		return UUID.randomUUID().toString().replace("-", "");
	}
}