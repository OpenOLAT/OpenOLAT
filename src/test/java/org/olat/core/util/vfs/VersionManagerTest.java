package org.olat.core.util.vfs;

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
import org.junit.Before;
import org.junit.Test;
import org.olat.core.CoreSpringFactory;
import org.olat.core.commons.modules.bc.meta.MetaInfo;
import org.olat.core.commons.modules.bc.meta.tagged.MetaTagged;
import org.olat.core.commons.modules.bc.vfs.OlatRootFolderImpl;
import org.olat.core.id.Identity;
import org.olat.core.util.vfs.version.SimpleVersionConfig;
import org.olat.core.util.vfs.version.VFSRevision;
import org.olat.core.util.vfs.version.Versionable;
import org.olat.core.util.vfs.version.Versions;
import org.olat.core.util.vfs.version.VersionsFileManager;
import org.olat.test.JunitTestHelper;
import org.olat.test.OlatTestCase;

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
	
	@Before
	public void setUp() throws Exception {
		if(setuped) return;
		
		//create identities
		id1 = JunitTestHelper.createAndPersistIdentityAsUser("vm-one" + getRandomName());
		id2 = JunitTestHelper.createAndPersistIdentityAsUser("vm-two" + getRandomName());
		
		SimpleVersionConfig versionConfig = (SimpleVersionConfig)CoreSpringFactory.getBean("versioningConfigurator");
		versionConfig.setMaxNumberOfVersionsProperty(new Long(10));
		
		setuped = true;
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
		metaInfo.setAuthor(id1.getName());
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
		Versions versions = VersionsFileManager.getInstance().createVersionsFor((VFSLeaf)retrievedFile);	
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
	
	private String getRandomName() {
		return UUID.randomUUID().toString().replace("-", "");
	}
}