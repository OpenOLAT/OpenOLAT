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
import java.util.List;
import java.util.UUID;

import org.apache.commons.io.IOUtils;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.olat.core.CoreSpringFactory;
import org.olat.core.commons.modules.bc.meta.MetaInfo;
import org.olat.core.commons.modules.bc.meta.tagged.MetaTagged;
import org.olat.core.commons.modules.bc.vfs.OlatRootFolderImpl;
import org.olat.core.id.Identity;
import org.olat.core.util.vfs.VFSContainer;
import org.olat.core.util.vfs.VFSItem;
import org.olat.core.util.vfs.VFSLeaf;
import org.olat.core.util.vfs.restapi.SystemItemFilter;
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
	private VersionsFileManager versionManager;
	@Autowired
	private SimpleVersionConfig versioningConfigurator;
	
	@Before
	public void setUp() throws Exception {
		if(setuped) return;
		
		//create identities
		id1 = JunitTestHelper.createAndPersistIdentityAsUser("vm-one" + getRandomName());
		id2 = JunitTestHelper.createAndPersistIdentityAsUser("vm-two" + getRandomName());
		
		SimpleVersionConfig versionConfig = (SimpleVersionConfig)CoreSpringFactory.getBean("versioningConfigurator");
		versionConfig.setMaxNumberOfVersionsProperty(new Long(10));
		sleep(2000);
		
		setuped = true;
	}
	
	@After
	public void resetMaxVersions() {
		versioningConfigurator.setMaxNumberOfVersionsProperty(new Long(10));
		sleep(2000);
		versioningConfigurator.getMaxNumberOfVersionsProperty();
	}
	
	@Test
	public void testVersionManager() throws IOException {
		//create a file
		OlatRootFolderImpl rootTest = new OlatRootFolderImpl("/test", null);
		String filename = getRandomName();
		VFSLeaf file = rootTest.createChildLeaf(filename);
		OutputStream out = file.getOutputStream(false);
		InputStream in = VersionManagerTest.class.getResourceAsStream("test.txt");
		int byteCopied = IOUtils.copy(in, out);
		IOUtils.closeQuietly(in);
		assertFalse(byteCopied == 0);
		assertTrue(file instanceof Versionable);
		
		//save a first version
		Versionable versionedFile1 = (Versionable)file;
		InputStream in1 = new ByteArrayInputStream("Hello version 1".getBytes());
		versionedFile1.getVersions().addVersion(id2, "Version 1", in1);
		IOUtils.closeQuietly(in1);
		
		//save a second version
		Versionable versionedFile2 = (Versionable)file;
		InputStream in2 = new ByteArrayInputStream("Hello version 2".getBytes());
		versionedFile2.getVersions().addVersion(id2, "Version 2", in2);
		IOUtils.closeQuietly(in2);
		
		//save a third version
		Versionable versionedFile3 = (Versionable)file;
		InputStream in3 = new ByteArrayInputStream("Hello version 3".getBytes());
		versionedFile3.getVersions().addVersion(id2, "Version 3", in3);
		IOUtils.closeQuietly(in3);

		//make the checks
		VFSItem retrievedFile = rootTest.resolve(filename);
		assertTrue(retrievedFile instanceof Versionable);
		Versions versions = VersionsFileManager.getInstance().createVersionsFor((VFSLeaf)retrievedFile);	
		List<VFSRevision> revisions = versions.getRevisions();
		assertNotNull(revisions);
		assertEquals(3, revisions.size());
		
		VFSRevision revision0 = revisions.get(0);
		//we don't set an author for the original file
		assertEquals("-", revision0.getAuthor());
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
		versioningConfigurator.setMaxNumberOfVersionsProperty(new Long(3));
		sleep(1000);
		
		//create a file
		OlatRootFolderImpl rootTest = new OlatRootFolderImpl("/test_" + UUID.randomUUID(), null);
		String filename = getRandomName();
		VFSLeaf file = rootTest.createChildLeaf(filename);
		OutputStream out = file.getOutputStream(false);
		InputStream in = VersionManagerTest.class.getResourceAsStream("test.txt");
		int byteCopied = IOUtils.copy(in, out);
		IOUtils.closeQuietly(in);
		assertFalse(byteCopied == 0);
		assertTrue(file instanceof Versionable);
		
		//save a first version
		for(int i=0; i<5; i++) {
			Versionable versionedFile = (Versionable)file;
			InputStream inv = new ByteArrayInputStream(("Hello version " + i).getBytes());
			versionedFile.getVersions().addVersion(id2, "Version " + (1 +i), inv);
			IOUtils.closeQuietly(inv);
		}

		VFSItem retrievedFile = rootTest.resolve(filename);
		Versions versions = VersionsFileManager.getInstance().createVersionsFor((VFSLeaf)retrievedFile);	
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
		versioningConfigurator.setMaxNumberOfVersionsProperty(new Long(0));
		sleep(1000);
		
		//create a file
		OlatRootFolderImpl rootTest = new OlatRootFolderImpl("/test_" + UUID.randomUUID(), null);
		String filename = getRandomName();
		VFSLeaf file = rootTest.createChildLeaf(filename);
		OutputStream out = file.getOutputStream(false);
		InputStream in = VersionManagerTest.class.getResourceAsStream("test.txt");
		int byteCopied = IOUtils.copy(in, out);
		IOUtils.closeQuietly(in);
		assertFalse(byteCopied == 0);
		assertTrue(file instanceof Versionable);
		
		//save a first version
		for(int i=0; i<5; i++) {
			Versionable versionedFile = (Versionable)file;
			InputStream inv = new ByteArrayInputStream(("Hello version " + i).getBytes());
			versionedFile.getVersions().addVersion(id2, "Version " + (1 +i), inv);
			IOUtils.closeQuietly(inv);
		}

		VFSItem retrievedFile = rootTest.resolve(filename);
		Versions versions = VersionsFileManager.getInstance().createVersionsFor((VFSLeaf)retrievedFile);	
		List<VFSRevision> revisions = versions.getRevisions();
		assertNotNull(revisions);
		assertTrue(revisions.isEmpty());
	}
	
	@Test
	public void testVersionChecksum() throws IOException {
		OlatRootFolderImpl rootTest = new OlatRootFolderImpl("/ver-" + UUID.randomUUID(), null);
		String filename = getRandomName();
		VFSLeaf file = rootTest.createChildLeaf(filename);
		OutputStream out = file.getOutputStream(false);
		InputStream in = VersionManagerTest.class.getResourceAsStream("test.txt");
		int byteCopied = IOUtils.copy(in, out);
		IOUtils.closeQuietly(in);
		assertFalse(byteCopied == 0);
		assertTrue(file instanceof Versionable);
		
		
		//save a first version
		Versionable versionedFile = (Versionable)file;
		InputStream in1 = VersionManagerTest.class.getResourceAsStream("test.txt");
		versionedFile.getVersions().addVersion(id2, "Version 1", in1);
		IOUtils.closeQuietly(in1);
		
		//save a second version
		InputStream in2 = VersionManagerTest.class.getResourceAsStream("test.txt");
		versionedFile.getVersions().addVersion(id2, "Version 2", in2);
		IOUtils.closeQuietly(in2);
		
		//save a third version
		InputStream in3 = VersionManagerTest.class.getResourceAsStream("test.txt");
		versionedFile.getVersions().addVersion(id2, "Version 3", in2);
		IOUtils.closeQuietly(in3);
		
		//check if there is only one backup file
		VFSContainer versionContainer = versionManager.getCanonicalVersionFolder(file.getParentContainer(), false);
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
	
	@Test
	public void testDeleteRevisions_withSameFile() throws IOException {
		OlatRootFolderImpl rootTest = new OlatRootFolderImpl("/ver-" + UUID.randomUUID(), null);
		String filename = getRandomName();
		VFSLeaf file = rootTest.createChildLeaf(filename);
		OutputStream out = file.getOutputStream(false);
		InputStream in = VersionManagerTest.class.getResourceAsStream("test.txt");
		int byteCopied = IOUtils.copy(in, out);
		IOUtils.closeQuietly(in);
		assertFalse(byteCopied == 0);
		assertTrue(file instanceof Versionable);
		
		
		//save a first version
		Versionable versionedFile = (Versionable)file;
		InputStream in1 = VersionManagerTest.class.getResourceAsStream("test.txt");
		versionedFile.getVersions().addVersion(id2, "Version 1", in1);
		IOUtils.closeQuietly(in1);
		
		//save a second version
		InputStream in2 = VersionManagerTest.class.getResourceAsStream("test.txt");
		versionedFile.getVersions().addVersion(id2, "Version 2", in2);
		IOUtils.closeQuietly(in2);
		
		//save a third version
		InputStream in3 = VersionManagerTest.class.getResourceAsStream("test.txt");
		versionedFile.getVersions().addVersion(id2, "Version 3", in2);
		IOUtils.closeQuietly(in3);
		
		//delete revisions
		versionManager.deleteRevisions(versionedFile, versionedFile.getVersions().getRevisions().subList(0, 2));
		
		//check number of versions
		VFSItem reloadFile = rootTest.resolve(filename);
		assertTrue(reloadFile instanceof Versionable);
		Versionable reloadedVersionedFile = (Versionable)reloadFile;
		List<VFSRevision> revisions = reloadedVersionedFile.getVersions().getRevisions();
		Assert.assertNotNull(revisions);
		Assert.assertEquals(1, revisions.size());
		
		//check if there is only one backup file
		VFSContainer versionContainer = versionManager.getCanonicalVersionFolder(file.getParentContainer(), false);
		Assert.assertNotNull(versionContainer);
		List<VFSItem> items = versionContainer.getItems(new SystemItemFilter());
		Assert.assertNotNull(items);
		Assert.assertEquals(2, items.size());
	}
	
	@Test
	public void testAuthorsAndCreators() throws IOException {
		//create a file
		OlatRootFolderImpl rootTest = new OlatRootFolderImpl("/test2", null);
		String filename = getRandomName();
		VFSLeaf file = rootTest.createChildLeaf(filename);
		OutputStream out = file.getOutputStream(false);
		InputStream in = VersionManagerTest.class.getResourceAsStream("test.txt");
		int byteCopied = IOUtils.copy(in, out);
		IOUtils.closeQuietly(in);
		assertFalse(byteCopied == 0);
		assertTrue(file instanceof Versionable);
		assertTrue(file instanceof MetaTagged);
		
		//set the author
		MetaTagged metaTagged = (MetaTagged)file;
		MetaInfo metaInfo = metaTagged.getMetaInfo();
		metaInfo.setAuthor(id1);
		metaInfo.setCreator(id1.getName());
		metaInfo.write();
		
		//save a first version -> id2
		Versionable versionedFile1 = (Versionable)file;
		InputStream in1 = new ByteArrayInputStream("Hello version 1".getBytes());
		versionedFile1.getVersions().addVersion(id2, "Version 1", in1);
		IOUtils.closeQuietly(in1);
		
		//save a second version -> id1
		Versionable versionedFile2 = (Versionable)file;
		InputStream in2 = new ByteArrayInputStream("Hello version 2".getBytes());
		versionedFile2.getVersions().addVersion(id1, "Version 2", in2);
		IOUtils.closeQuietly(in2);
		
		//save a third version -> id2
		Versionable versionedFile3 = (Versionable)file;
		InputStream in3 = new ByteArrayInputStream("Hello version 3".getBytes());
		versionedFile3.getVersions().addVersion(id2, "Version 3", in3);
		IOUtils.closeQuietly(in3);
		
		
		//make the checks
		VFSItem retrievedFile = rootTest.resolve(filename);
		assertTrue(retrievedFile instanceof Versionable);
		Versions versions = versionManager.createVersionsFor((VFSLeaf)retrievedFile);	
		List<VFSRevision> revisions = versions.getRevisions();
		assertNotNull(revisions);
		assertEquals(3, revisions.size());
		assertTrue(retrievedFile instanceof MetaTagged);
		
		
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
		OlatRootFolderImpl rootTest = new OlatRootFolderImpl("/test2", null);
		String filename = getRandomName();
		VFSLeaf file = rootTest.createChildLeaf(filename);
		OutputStream out = file.getOutputStream(false);
		InputStream in = VersionManagerTest.class.getResourceAsStream("test.txt");
		int byteCopied = IOUtils.copy(in, out);
		IOUtils.closeQuietly(in);
		assertFalse(byteCopied == 0);
		assertTrue(file instanceof Versionable);
		assertTrue(file instanceof MetaTagged);
		
		//set the author
		MetaTagged metaTagged = (MetaTagged)file;
		MetaInfo metaInfo = metaTagged.getMetaInfo();
		metaInfo.setAuthor(id1);
		metaInfo.setCreator(id1.getName());
		metaInfo.write();
		
		//save a first version -> id2
		Versionable versionedFile1 = (Versionable)file;
		InputStream in1 = new ByteArrayInputStream("Hello version 1".getBytes());
		versionedFile1.getVersions().addVersion(id2, "Version 1", in1);
		IOUtils.closeQuietly(in1);
		
		//save a second version -> id1
		Versionable versionedFile2 = (Versionable)file;
		InputStream in2 = new ByteArrayInputStream("Hello version 2".getBytes());
		versionedFile2.getVersions().addVersion(id1, "Version 2", in2);
		IOUtils.closeQuietly(in2);
		
		//move the file
		VFSLeaf retrievedLeaf = (VFSLeaf)rootTest.resolve(filename);
		String copyFilename = getRandomName();
		VFSLeaf copyFile = rootTest.createChildLeaf(copyFilename);
		OutputStream copyOutput = copyFile.getOutputStream(false);
		InputStream copyInput = retrievedLeaf.getInputStream();
		IOUtils.copy(copyInput, copyOutput);
		IOUtils.closeQuietly(copyOutput);
		IOUtils.closeQuietly(copyInput);
		//move the revisions
		VersionsManager.getInstance().move(retrievedLeaf, copyFile, id2);
		
		//check if the revisions are moved
		VFSLeaf retirevedCopyFile = (VFSLeaf)rootTest.resolve(copyFilename);
		assertTrue(retirevedCopyFile instanceof Versionable);
		Versions versions = VersionsFileManager.getInstance().createVersionsFor(retirevedCopyFile);	
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
		OlatRootFolderImpl rootTest = new OlatRootFolderImpl("/test2", null);
		String filename = getRandomName();
		VFSLeaf file = rootTest.createChildLeaf(filename);
		OutputStream out = file.getOutputStream(false);
		InputStream in = VersionManagerTest.class.getResourceAsStream("test.txt");
		int byteCopied = IOUtils.copy(in, out);
		IOUtils.closeQuietly(in);
		assertFalse(byteCopied == 0);
		assertTrue(file instanceof Versionable);
		assertTrue(file instanceof MetaTagged);
		
		//set the author
		MetaTagged metaTagged = (MetaTagged)file;
		MetaInfo metaInfo = metaTagged.getMetaInfo();
		metaInfo.setAuthor(id1);
		metaInfo.setCreator(id1.getName());
		metaInfo.write();
		
		//save a first version of file A -> id2
		Versionable versionedFile1 = (Versionable)file;
		InputStream in1 = new ByteArrayInputStream("Hello version 1".getBytes());
		versionedFile1.getVersions().addVersion(id2, "Version 1", in1);
		IOUtils.closeQuietly(in1);
		
		//save a second version of file A -> id1
		Versionable versionedFile2 = (Versionable)file;
		InputStream in2 = new ByteArrayInputStream("Hello version 2".getBytes());
		versionedFile2.getVersions().addVersion(id1, "Version 2", in2);
		IOUtils.closeQuietly(in2);
		
		//move the file A -> file B
		VFSLeaf retrievedLeaf = (VFSLeaf)rootTest.resolve(filename);
		String copyFilename = getRandomName();
		VFSLeaf copyFile = rootTest.createChildLeaf(copyFilename);
		OutputStream copyOutput = copyFile.getOutputStream(false);
		InputStream copyInput = retrievedLeaf.getInputStream();
		IOUtils.copy(copyInput, copyOutput);
		IOUtils.closeQuietly(copyOutput);
		IOUtils.closeQuietly(copyInput);
		//move the revisions
		VersionsManager.getInstance().move(retrievedLeaf, copyFile, id2);
		
		//move the file B -> file A
		VFSLeaf retrievedCopyLeaf = (VFSLeaf)rootTest.resolve(copyFilename);
		VFSLeaf originalFile = (VFSLeaf)rootTest.resolve(filename);
		OutputStream originalOutput = originalFile.getOutputStream(false);
		InputStream retrievedCopyInput = retrievedCopyLeaf.getInputStream();
		IOUtils.copy(retrievedCopyInput, originalOutput);
		IOUtils.closeQuietly(originalOutput);
		IOUtils.closeQuietly(retrievedCopyInput);
		//move the revisions
		VersionsManager.getInstance().move(retrievedCopyLeaf, originalFile, id2);
		
		
		//check if the revisions are moved
		VFSLeaf retirevedOriginalFile = (VFSLeaf)rootTest.resolve(filename);
		assertTrue(retirevedOriginalFile instanceof Versionable);
		Versions versions = VersionsFileManager.getInstance().createVersionsFor(retirevedOriginalFile);	
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
	
	private String getRandomName() {
		return UUID.randomUUID().toString().replace("-", "");
	}
}