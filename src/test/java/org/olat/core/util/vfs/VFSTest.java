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
package org.olat.core.util.vfs;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;
import java.util.UUID;

import org.apache.logging.log4j.Logger;
import org.junit.Assert;
import org.junit.Test;
import org.olat.core.commons.services.vfs.VFSMetadata;
import org.olat.core.commons.services.vfs.VFSRepositoryService;
import org.olat.core.commons.services.vfs.manager.VFSMetadataDAO;
import org.olat.core.logging.Tracing;
import org.olat.core.util.FileUtils;
import org.olat.test.OlatTestCase;
import org.springframework.beans.factory.annotation.Autowired;


/**
 * 
 * Initial date: 8 janv. 2019<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class VFSTest extends OlatTestCase {
	
	private static final Logger log = Tracing.createLoggerFor(VFSTest.class);
	
	private static final String VFS_TEST_DIR = "/vfstest";
	
	@Autowired
	private VFSMetadataDAO vfsMetadaDao;
	@Autowired
	private VFSRepositoryService vfsRepositoryService;
	
	/**
	 * Test the copyFrom method (inclusive copy of metadata)
	 */
	@Test
	public void copyFrom() {
		String filename = UUID.randomUUID() + ".txt";
		VFSContainer testContainer = VFSManager.olatRootContainer(VFS_TEST_DIR, null);
		VFSLeaf firstLeaf = testContainer.createChildLeaf(filename);
		Assert.assertEquals(VFSConstants.YES, firstLeaf.canMeta());
		prepareFile(firstLeaf);
		
		VFSMetadata metaInfo = firstLeaf.getMetaInfo();
		metaInfo.setComment("A comment");
		metaInfo.setCreator("Me");
		Assert.assertNotNull(vfsRepositoryService.updateMetadata(metaInfo));
		
		VFSContainer targetContainer = VFSManager.olatRootContainer(VFS_TEST_DIR + "/vfstarger" + UUID.randomUUID(), null);
		Assert.assertEquals(VFSConstants.YES, targetContainer.canMeta());
		targetContainer.copyFrom(firstLeaf, null);
		
		VFSItem copiedItem = targetContainer.resolve(filename);
		Assert.assertTrue(copiedItem instanceof VFSLeaf);
		
		VFSLeaf copiedLeaf = (VFSLeaf)copiedItem;
		Assert.assertEquals(VFSConstants.YES, copiedLeaf.canMeta());
		
		VFSMetadata copiedMetaInfo = copiedLeaf.getMetaInfo();
		Assert.assertEquals("A comment", copiedMetaInfo.getComment());
		Assert.assertEquals("Me", copiedMetaInfo.getCreator());
	}
	
	@Test
	public void rename() {
		String filename = UUID.randomUUID() + ".txt";
		VFSContainer testContainer = VFSManager.olatRootContainer(VFS_TEST_DIR, null);
		VFSLeaf firstLeaf = testContainer.createChildLeaf(filename);
		Assert.assertEquals(VFSConstants.YES, firstLeaf.canMeta());
		prepareFile(firstLeaf);
		
		VFSMetadata metaInfo = firstLeaf.getMetaInfo();
		metaInfo.setComment("my old comment");
		metaInfo.setCreator("Always Me");
		Assert.assertNotNull(vfsRepositoryService.updateMetadata(metaInfo));
		
		String newName = UUID.randomUUID() + ".txt";
		VFSStatus renamedStatus = firstLeaf.rename(newName);
		Assert.assertEquals(VFSConstants.YES, renamedStatus);
		
		VFSItem renamedItem = testContainer.resolve(newName);
		Assert.assertTrue(renamedItem instanceof VFSLeaf);
		VFSLeaf renamedLeaf = (VFSLeaf)renamedItem;
		
		VFSMetadata renamedMetaInfo = renamedLeaf.getMetaInfo();
		Assert.assertEquals("my old comment", renamedMetaInfo.getComment());
		Assert.assertEquals("Always Me", renamedMetaInfo.getCreator());
	}
	
	@Test
	public void renameSameName() {
		// The first file
		String filename = "samename.txt";
		VFSContainer testContainer = VFSManager.olatRootContainer(VFS_TEST_DIR, null);
		testContainer = VFSManager.getOrCreateContainer(testContainer, UUID.randomUUID().toString());
		
		VFSLeaf firstLeaf = testContainer.createChildLeaf(filename);
		Assert.assertEquals(VFSConstants.YES, firstLeaf.canMeta());
		prepareFile(firstLeaf);
		
		VFSMetadata metaInfo = firstLeaf.getMetaInfo();
		metaInfo.setComment("my old comment");
		metaInfo.setCreator("Always Me");
		Assert.assertNotNull(vfsRepositoryService.updateMetadata(metaInfo));
		
		// A second file
		String newName = UUID.randomUUID() + ".txt";
		VFSLeaf secondLeaf = testContainer.createChildLeaf(newName);
		Assert.assertEquals(VFSConstants.YES, secondLeaf.canMeta());
		prepareFile(secondLeaf);
		
		// Rename the second file to the first one
		VFSStatus renamedStatus = firstLeaf.rename(filename);
		Assert.assertEquals(VFSConstants.YES, renamedStatus);
		
		VFSItem renamedItem = testContainer.resolve(filename);
		Assert.assertTrue(renamedItem instanceof VFSLeaf);
		VFSLeaf renamedLeaf = (VFSLeaf)renamedItem;
		
		VFSMetadata renamedMetaInfo = renamedLeaf.getMetaInfo();
		Assert.assertEquals("my old comment", renamedMetaInfo.getComment());
		Assert.assertEquals("Always Me", renamedMetaInfo.getCreator());
		
		
		List<VFSMetadata> metadata = vfsMetadaDao.getMetadatas(metaInfo.getRelativePath());
		Assert.assertNotNull(metadata);
		Assert.assertEquals(1, metadata.size());
	}
	
	private void prepareFile(VFSLeaf file) {
		try(OutputStream out = file.getOutputStream(false);
				InputStream in = VFSTest.class.getResourceAsStream("test.txt")) {
			FileUtils.cpio(in, out, "");
		} catch(IOException e) {
			log.error("", e);
		}
	}
}
