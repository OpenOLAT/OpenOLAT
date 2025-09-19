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

import static org.olat.test.JunitTestHelper.random;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;
import java.util.UUID;

import org.apache.logging.log4j.Logger;
import org.junit.Assert;
import org.junit.Test;
import org.olat.admin.quota.QuotaImpl;
import org.olat.core.commons.services.vfs.VFSMetadata;
import org.olat.core.commons.services.vfs.VFSRepositoryService;
import org.olat.core.commons.services.vfs.manager.VFSMetadataDAO;
import org.olat.core.logging.Tracing;
import org.olat.core.util.FileUtils;
import org.olat.core.util.vfs.callbacks.DefaultVFSSecurityCallback;
import org.olat.core.util.vfs.callbacks.FullAccessWithQuotaCallback;
import org.olat.core.util.vfs.callbacks.VFSSecurityCallback;
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
		Assert.assertEquals(VFSStatus.YES, firstLeaf.canMeta());
		prepareFile(firstLeaf);
		
		VFSMetadata metaInfo = firstLeaf.getMetaInfo();
		metaInfo.setComment("A comment");
		metaInfo.setCreator("Me");
		Assert.assertNotNull(vfsRepositoryService.updateMetadata(metaInfo));
		
		VFSContainer targetContainer = VFSManager.olatRootContainer(VFS_TEST_DIR + "/vfstarger" + UUID.randomUUID(), null);
		Assert.assertEquals(VFSStatus.YES, targetContainer.canMeta());
		targetContainer.copyFrom(firstLeaf, null);
		
		VFSItem copiedItem = targetContainer.resolve(filename);
		Assert.assertTrue(copiedItem instanceof VFSLeaf);
		
		VFSLeaf copiedLeaf = (VFSLeaf)copiedItem;
		Assert.assertEquals(VFSStatus.YES, copiedLeaf.canMeta());
		
		VFSMetadata copiedMetaInfo = copiedLeaf.getMetaInfo();
		Assert.assertEquals("A comment", copiedMetaInfo.getComment());
		Assert.assertEquals("Me", copiedMetaInfo.getCreator());
	}
	
	@Test
	public void copyFrom_sourceDoesNotAllow() {
		VFSContainer testContainer = VFSManager.olatRootContainer(VFS_TEST_DIR, null);
		VFSContainer sourceContainer = VFSManager.olatRootContainer(VFS_TEST_DIR + "/" + random(), null);
		sourceContainer.createChildLeaf(random() + ".txt");
		sourceContainer.setLocalSecurityCallback(new DefaultVFSSecurityCallback());
		
		VFSSuccess success = testContainer.copyFrom(sourceContainer, null);
		
		Assert.assertEquals(VFSSuccess.ERROR_SECURITY_DENIED, success);
	}
	
	@Test
	public void copyFrom_quotaCheckLeaf() {
		VFSContainer targetContainer = VFSManager.olatRootContainer(VFS_TEST_DIR + "/" + random(), null);
		VFSContainer sourceContainer = VFSManager.olatRootContainer(VFS_TEST_DIR + "/" + random(), null);
		VFSLeaf sourceFile = sourceContainer.createChildLeaf(random() + ".txt");
		fillFile(sourceFile, 9);
		
		// No quota
		VFSSuccess success = targetContainer.copyContentOf(sourceContainer, null);
		Assert.assertEquals(VFSSuccess.SUCCESS, success);
		
		// Enough quota
		sourceFile.rename(random() + ".txt");
		VFSSecurityCallback quotaSecCallback = new FullAccessWithQuotaCallback(new QuotaImpl(null, 20l, 10l));
		targetContainer.setLocalSecurityCallback(quotaSecCallback);
		success = targetContainer.copyContentOf(sourceContainer, null);
		Assert.assertEquals(VFSSuccess.SUCCESS, success);
		
		// Not enough quota anymore
		sourceFile.rename(random() + ".txt");
		success = targetContainer.copyContentOf(sourceContainer, null);
		Assert.assertEquals(VFSSuccess.ERROR_QUOTA_EXCEEDED, success);
	}
	
	@Test
	public void copyFrom_quotaCheckSub() {
		VFSContainer targetContainer = VFSManager.olatRootContainer(VFS_TEST_DIR + "/" + random(), null);
		VFSContainer sourceContainer = VFSManager.olatRootContainer(VFS_TEST_DIR + "/" + random(), null);
		VFSContainer sourceSubContainer = sourceContainer.createChildContainer(random());
		VFSLeaf sourceFile = sourceSubContainer.createChildLeaf(random() + ".txt");
		fillFile(sourceFile, 9);
		
		// No quota
		VFSSuccess success = targetContainer.copyContentOf(sourceContainer, null);
		Assert.assertEquals(VFSSuccess.SUCCESS, success);
		
		// Enough quota
		sourceSubContainer.rename(random());
		VFSSecurityCallback quotaSecCallback = new FullAccessWithQuotaCallback(new QuotaImpl(null, 20l, 10l));
		targetContainer.setLocalSecurityCallback(quotaSecCallback);
		success = targetContainer.copyContentOf(sourceContainer, null);
		Assert.assertEquals(VFSSuccess.SUCCESS, success);
		
		// Not enough quota anymore
		sourceSubContainer.rename(random());
		success = targetContainer.copyContentOf(sourceContainer, null);
		Assert.assertEquals(VFSSuccess.ERROR_QUOTA_EXCEEDED, success);
	}
	
	@Test
	public void rename() {
		String filename = UUID.randomUUID() + ".txt";
		VFSContainer testContainer = VFSManager.olatRootContainer(VFS_TEST_DIR, null);
		VFSLeaf firstLeaf = testContainer.createChildLeaf(filename);
		Assert.assertEquals(VFSStatus.YES, firstLeaf.canMeta());
		prepareFile(firstLeaf);
		
		VFSMetadata metaInfo = firstLeaf.getMetaInfo();
		metaInfo.setComment("my old comment");
		metaInfo.setCreator("Always Me");
		Assert.assertNotNull(vfsRepositoryService.updateMetadata(metaInfo));
		
		String newName = UUID.randomUUID() + ".txt";
		VFSSuccess renamedStatus = firstLeaf.rename(newName);
		Assert.assertEquals(VFSSuccess.SUCCESS, renamedStatus);
		
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
		Assert.assertEquals(VFSStatus.YES, firstLeaf.canMeta());
		prepareFile(firstLeaf);
		
		VFSMetadata metaInfo = firstLeaf.getMetaInfo();
		metaInfo.setComment("my old comment");
		metaInfo.setCreator("Always Me");
		Assert.assertNotNull(vfsRepositoryService.updateMetadata(metaInfo));
		
		// A second file
		String newName = UUID.randomUUID() + ".txt";
		VFSLeaf secondLeaf = testContainer.createChildLeaf(newName);
		Assert.assertEquals(VFSStatus.YES, secondLeaf.canMeta());
		prepareFile(secondLeaf);
		
		// Rename the second file to the first one
		VFSSuccess renamedStatus = firstLeaf.rename(filename);
		Assert.assertEquals(VFSSuccess.SUCCESS, renamedStatus);
		
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
	
	private void fillFile(VFSLeaf leaf, int sizeInKB) {
		try (OutputStream out =  leaf.getOutputStream(true)) {
			int sizeInBytes = sizeInKB * 1024;
			for (int i = 0; i < sizeInBytes; i++) {
				out.write('a');
			}
		} catch (IOException e) {
			log.error("", e);
		}
	}
	
}
