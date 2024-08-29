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

import static org.assertj.core.api.Assertions.assertThat;
import static org.olat.test.JunitTestHelper.random;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import org.apache.commons.io.IOUtils;
import org.apache.logging.log4j.Logger;
import org.assertj.core.api.SoftAssertions;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.olat.core.commons.persistence.DB;
import org.olat.core.commons.services.license.License;
import org.olat.core.commons.services.license.LicenseService;
import org.olat.core.commons.services.license.LicenseType;
import org.olat.core.commons.services.license.manager.LicenseCleaner;
import org.olat.core.commons.services.vfs.VFSMetadata;
import org.olat.core.commons.services.vfs.VFSRepositoryService;
import org.olat.core.id.Identity;
import org.olat.core.logging.Tracing;
import org.olat.core.util.CodeHelper;
import org.olat.core.util.DateUtils;
import org.olat.core.util.FileUtils;
import org.olat.core.util.vfs.LocalFileImpl;
import org.olat.core.util.vfs.LocalFolderImpl;
import org.olat.core.util.vfs.VFSContainer;
import org.olat.core.util.vfs.VFSItem;
import org.olat.core.util.vfs.VFSLeaf;
import org.olat.core.util.vfs.VFSManager;
import org.olat.core.util.vfs.VFSStatus;
import org.olat.test.JunitTestHelper;
import org.olat.test.OlatTestCase;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Versioning is test with @see org.olat.core.commons.services.vfs.manager.VFSVersioningTest
 * 
 * Initial date: 12 mars 2019<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class VFSRepositoryServiceTest extends OlatTestCase {
	
	private static final Logger log = Tracing.createLoggerFor(VFSRepositoryServiceTest.class);
	private static final String VFS_TEST_DIR = "/vfsrepotest";
	
	@Autowired
	private DB dbInstance;
	@Autowired
	private LicenseService licenseService;
	@Autowired
	private LicenseCleaner licenseCleaner;
	@Autowired
	private VFSRepositoryService vfsRepositoryService;
	
	@Before
	public void cleanUp() {
		licenseCleaner.deleteAll();
	}
	
	@Test
	public void getMetadataForVFSLeaf() {
		VFSLeaf leaf = createFile();
		
		// create metadata
		VFSMetadata metadata = vfsRepositoryService.getMetadataFor(leaf);
		dbInstance.commitAndCloseSession();
		Assert.assertNotNull(metadata);
		Assert.assertNotNull(metadata.getKey());
		Assert.assertNotNull(metadata.getCreationDate());
		Assert.assertNotNull(metadata.getLastModified());
		Assert.assertNotNull(metadata.getFileLastModified());
		Assert.assertEquals(leaf.getName(), metadata.getFilename());
		Assert.assertFalse(metadata.isDirectory());	
	}
	
	@Test
	public void getMetadataForFile() {
		VFSLeaf leaf = createFile();
		File file = ((LocalFileImpl)leaf).getBasefile();
		
		// create metadata
		VFSMetadata metadata = vfsRepositoryService.getMetadataFor(file);
		dbInstance.commitAndCloseSession();
		Assert.assertNotNull(metadata);
		Assert.assertNotNull(metadata.getKey());
		Assert.assertNotNull(metadata.getCreationDate());
		Assert.assertNotNull(metadata.getLastModified());
		Assert.assertNotNull(metadata.getFileLastModified());
		Assert.assertEquals(leaf.getName(), metadata.getFilename());
		Assert.assertFalse(metadata.isDirectory());	
	}
	
	@Test
	public void deleteMetadata() {
		VFSLeaf leaf = createImage();
		VFSMetadata metadata = vfsRepositoryService.getMetadataFor(leaf);
		Assert.assertNotNull(metadata);
		
		VFSLeaf thumbnail1 = vfsRepositoryService.getThumbnail(leaf, 200, 200, true);
		Assert.assertNotNull(thumbnail1);
		Assert.assertTrue(thumbnail1.getSize() > 32);
		VFSLeaf thumbnail2 = vfsRepositoryService.getThumbnail(leaf, 180, 180, false);
		Assert.assertNotNull(thumbnail2);
		Assert.assertTrue(thumbnail2.getSize() > 32);
		
		vfsRepositoryService.deleteMetadata(metadata);
		dbInstance.commitAndCloseSession();
	}
	
	@Test
	public void deleteMetadataFolder() {
		VFSContainer testContainer = VFSManager.olatRootContainer(VFS_TEST_DIR, null);
		VFSContainer container = createContainerRecursive(testContainer, 0, 2, 5, 5);
	
		VFSMetadata metadata = vfsRepositoryService.getMetadataFor(container);
		dbInstance.commitAndCloseSession();
		
		String containerPath = metadata.getRelativePath() + "/" + metadata.getFilename();
		List<VFSMetadata> children = vfsRepositoryService.getChildren(containerPath);
		Assert.assertNotNull(children);
		Assert.assertEquals(10, children.size());
		
		container.deleteSilently();
		dbInstance.commitAndCloseSession();
		
		List<VFSMetadata> afterChildren = vfsRepositoryService.getChildren(containerPath);
		Assert.assertNotNull(afterChildren);
		Assert.assertEquals(0, afterChildren.size());
	}
	
	@Test
	public void deleteDirsAndFiles() {
		VFSContainer testContainer = VFSManager.olatRootContainer(VFS_TEST_DIR, null);
		VFSContainer container = createContainerRecursive(testContainer, 0, 2, 3, 3);
	
		VFSMetadata metadata = vfsRepositoryService.getMetadataFor(container);
		dbInstance.commitAndCloseSession();
		
		String containerPath = metadata.getRelativePath() + "/" + metadata.getFilename();
		List<VFSMetadata> children = vfsRepositoryService.getChildren(containerPath);
		Assert.assertNotNull(children);
		Assert.assertEquals(6, children.size());
		
		File dir = ((LocalFolderImpl)container).getBasefile();
		FileUtils.deleteDirsAndFiles(dir, true, true);
		dbInstance.commitAndCloseSession();
		
		List<VFSMetadata> afterChildren = vfsRepositoryService.getChildren(containerPath);
		Assert.assertNotNull(afterChildren);
		Assert.assertEquals(0, afterChildren.size());
	}
	
	@Test
	public void itemSaved() {
		Identity savedBy = JunitTestHelper.createAndPersistIdentityAsRndUser(random());
		VFSLeaf leaf = createFile();
		dbInstance.commitAndCloseSession();
		
		vfsRepositoryService.itemSaved(leaf, savedBy);
		dbInstance.commitAndCloseSession();
		
		VFSMetadata vfsMetadata = vfsRepositoryService.getMetadataFor(leaf);
		SoftAssertions softly = new SoftAssertions();
		softly.assertThat(vfsMetadata.getFileLastModified()).isCloseTo(new Date(), 5000);
		softly.assertThat(vfsMetadata.getFileLastModifiedBy()).isEqualTo(savedBy);
		softly.assertThat(vfsMetadata.getFileSize()).isEqualTo(leaf.getSize());
		softly.assertAll();
	}
	
	@Test
	public void shouldLoadExistingLicenseType() {
		String typeName = "name";
		LicenseType licenseType = licenseService.createLicenseType(typeName);
		licenseType = licenseService.saveLicenseType(licenseType);
		dbInstance.commitAndCloseSession();
		String licensor = "licensor";
		String name = licenseType.getName();
		
		VFSLeaf file = createFile();
		VFSMetadata meta = vfsRepositoryService.getMetadataFor(file);
		meta.setLicenseTypeName(name);
		meta.setLicensor(licensor);
		License license = vfsRepositoryService.getLicense(meta);

		assertThat(license.getLicensor()).isEqualTo(licensor);
		LicenseType loadedLicenseType = license.getLicenseType();
		assertThat(loadedLicenseType).isEqualTo(licenseType);
	}
	
	@Test
	public void shouldCreateNonExistingLicenseType() {
		String typeName = "name";
		LicenseType licenseType = licenseService.createLicenseType(typeName);
		licenseType = licenseService.saveLicenseType(licenseType);
		dbInstance.commitAndCloseSession();
		
		String licensor = "licensor";
		String name = "new";
		String text = "text";
		VFSLeaf file = createFile();
		VFSMetadata meta = vfsRepositoryService.getMetadataFor(file);

		meta.setLicenseTypeName(name);
		meta.setLicensor(licensor);
		meta.setLicenseText(text);
		License license = vfsRepositoryService.getLicense(meta);

		assertThat(license.getLicensor()).isEqualTo(licensor);
		LicenseType loadedLicenseType = license.getLicenseType();
		assertThat(loadedLicenseType.getName()).isEqualTo(name);
		assertThat(loadedLicenseType.getText()).isEqualTo(text);
		
		LicenseType createdLicenseType = licenseService.loadLicenseTypeByName(name);
		assertThat(createdLicenseType).isNotNull();
	}
	
	@Test
	public void shouldNotCreateLicenseForDirectories() {
		VFSLeaf file = createFile();
		VFSMetadata meta = vfsRepositoryService.getMetadataFor(file);
		License license = vfsRepositoryService.getLicense(meta);
		meta.setLicenseTypeName(random());
		meta.setLicensor(random());
		
		VFSContainer directory = VFSManager.olatRootContainer(VFS_TEST_DIR, null).createChildContainer(random());
		meta = vfsRepositoryService.getMetadataFor(directory);
		meta.setLicenseTypeName(random());
		meta.setLicensor(random());
		license = vfsRepositoryService.getLicense(meta);
		assertThat(license).isNull();
	}
	
	@Test
	public void readWriteBinary() {
		String filename = UUID.randomUUID() + ".txt";
		VFSContainer testContainer = VFSManager.olatRootContainer(VFS_TEST_DIR, null);
		VFSLeaf leaf = testContainer.createChildLeaf(filename);
		Assert.assertEquals(VFSStatus.YES, leaf.canMeta());
		copyTestTxt(leaf, "test.txt");
		
		VFSMetadata metaInfo = leaf.getMetaInfo();
		metaInfo.setComment("A little comment");
		vfsRepositoryService.updateMetadata(metaInfo);
		
		byte[] binaryData = MetaInfoReader.toBinaries(metaInfo);
		Assert.assertNotNull(binaryData);
		Assert.assertTrue(binaryData.length > 0);
		
		
		String secondFilename = UUID.randomUUID() + ".txt";
		VFSLeaf secondLeaf = testContainer.createChildLeaf(secondFilename);
		copyTestTxt(secondLeaf, "test.txt");

		VFSMetadata secondMetaInfo = leaf.getMetaInfo();
		String comment = null;
		try(InputStream in = new ByteArrayInputStream(binaryData)) {
			vfsRepositoryService.copyBinaries(secondMetaInfo, in);
			comment = secondMetaInfo.getComment();
		} catch(Exception e) {
			log.error("", e);
		}
		Assert.assertEquals("A little comment", comment);
	}
	
	@Test
	public void deleteExpiredfiles() {
		Date now = new Date();
		String uuid = UUID.randomUUID().toString();
		VFSContainer testContainer = VFSManager.olatRootContainer(VFS_TEST_DIR, null);
		
		// An expired file
		String expiredFilename = "expired_" + uuid + ".txt";
		VFSLeaf expiredLeaf = testContainer.createChildLeaf(expiredFilename);
		copyTestTxt(expiredLeaf, "test.txt");
		VFSMetadata expiredMetaInfo = expiredLeaf.getMetaInfo();
		expiredMetaInfo.setExpirationDate(DateUtils.addDays(now, -2));
		vfsRepositoryService.updateMetadata(expiredMetaInfo);
		
		// Expire in future
		String notYetExpiredFilename = "notexpired_" + uuid + ".txt";
		VFSLeaf notYetExpiredLeaf = testContainer.createChildLeaf(notYetExpiredFilename);
		copyTestTxt(notYetExpiredLeaf, "test.txt");
		VFSMetadata notYetExpiredMetaInfo = notYetExpiredLeaf.getMetaInfo();
		notYetExpiredMetaInfo.setExpirationDate(DateUtils.addDays(now, 2));
		vfsRepositoryService.updateMetadata(notYetExpiredMetaInfo);
		
		// Not expiration  in future
		String noExpirationFilename = "noexpiration_" + uuid + ".txt";
		VFSLeaf notExpirationLeaf = testContainer.createChildLeaf(noExpirationFilename);
		copyTestTxt(notExpirationLeaf, "test.txt");
		VFSMetadata notExpirationMetaInfo = notExpirationLeaf.getMetaInfo();
		vfsRepositoryService.updateMetadata(notExpirationMetaInfo);

		dbInstance.commitAndCloseSession();
		
		((VFSRepositoryServiceImpl)vfsRepositoryService).deleteExpiredFiles();
		
		// Check the files
		VFSItem expired = testContainer.resolve(expiredFilename);
		Assert.assertNull(expired);
		VFSItem notYetExpired = testContainer.resolve(notYetExpiredFilename);
		Assert.assertNotNull(notYetExpired);
		Assert.assertTrue(notYetExpired.exists());
		VFSItem noExpiration = testContainer.resolve(noExpirationFilename);
		Assert.assertNotNull(noExpiration);
		Assert.assertTrue(noExpiration.exists());
	}
	
	@Test
	public void renameFolder() throws IOException {
		VFSContainer testContainer = VFSManager.olatRootContainer(VFS_TEST_DIR, null);
		
		// Create the container
		String containerName = UUID.randomUUID().toString();
		VFSContainer container = testContainer.createChildContainer(containerName);
		VFSLeaf image = container.createChildLeaf("Image.jpg");
		copyTestTxt(image, "IMG_1491.jpg");
		VFSLeaf thumbnail = vfsRepositoryService.getThumbnail(image, 20, 20, true);
		Assert.assertNotNull(thumbnail);
		
		// Delete it
		container.delete();
		
		//create a file
		VFSContainer newContainer = testContainer.createChildContainer("MyName_" + CodeHelper.getForeverUniqueID());
		newContainer.rename(containerName);
		dbInstance.commitAndCloseSession();
	}
	
	@Test
	public void renameFolder_updateChildren() throws IOException {
		VFSContainer testContainer = VFSManager.olatRootContainer(VFS_TEST_DIR, null);
		
		// Create the container
		String containerName = UUID.randomUUID().toString();
		VFSContainer container = testContainer.createChildContainer(containerName);
		VFSContainer container1 = container.createChildContainer("sub1");
		VFSContainer container2 = container1.createChildContainer("sub2");
		VFSLeaf image = container2.createChildLeaf("Image.jpg");
		copyTestTxt(image, "IMG_1491.jpg");
		dbInstance.commitAndCloseSession();

		String newName = UUID.randomUUID().toString();
		container.rename(newName);
		dbInstance.commitAndCloseSession();
		
		testContainer = VFSManager.olatRootContainer(VFS_TEST_DIR, null);
		container = (VFSContainer) testContainer.resolve(newName);
		container1 = (VFSContainer) container.resolve("sub1");
		container2 = (VFSContainer) container1.resolve("sub2");
		VFSItem vfsItem = container2.resolve("Image.jpg");
		VFSMetadata imageMetadata = vfsRepositoryService.getMetadataFor(vfsItem);
		Assert.assertTrue(imageMetadata.getRelativePath().indexOf(newName + "/sub1/sub2") > -1);
		Assert.assertTrue(imageMetadata.getUri().indexOf(newName + "/sub1/sub2") > -1);
	}
	
	@Test
	public void synchMetadatas() throws IOException {
		VFSContainer testContainer = VFSManager.olatRootContainer(VFS_TEST_DIR, null);
		VFSContainer container = testContainer.createChildContainer(UUID.randomUUID().toString());
		VFSContainer container1 = container.createChildContainer("sub1");
		container1.getMetaInfo();
		VFSLeaf file11 = createFile(container);
		VFSLeaf file12 = createFile(container);
		
		List<VFSMetadata> descendants = vfsRepositoryService.getDescendants(container.getMetaInfo(), Boolean.FALSE);
		assertThat(descendants).containsExactlyInAnyOrder(
				container1.getMetaInfo(),
				file11.getMetaInfo(),
				file12.getMetaInfo()
			);
		
		// Create container and file without metadata
		new File(((LocalFolderImpl)container).getBasefile(), "sub2").mkdir();
		VFSContainer container2 = (VFSContainer)container.resolve("sub2");
		VFSLeaf file21 = container2.createChildLeaf("image21.jpg");
		copyTestTxt(file21, "IMG_1491.jpg");
		descendants = vfsRepositoryService.getDescendants(container.getMetaInfo(), Boolean.FALSE);
		assertThat(descendants).containsExactlyInAnyOrder(
				container1.getMetaInfo(),
				file11.getMetaInfo(),
				file12.getMetaInfo()
			);
		
		// Synch and check whether the matadata are generated
		vfsRepositoryService.synchMetadatas(container);
		descendants = vfsRepositoryService.getDescendants(container.getMetaInfo(), Boolean.FALSE);
		assertThat(descendants).containsExactlyInAnyOrder(
				container1.getMetaInfo(),
				file11.getMetaInfo(),
				file12.getMetaInfo(),
				container2.getMetaInfo(),
				file21.getMetaInfo()
			);
		
		// Delete a container and a file but not the metadata
		VFSManager.olatRootFile(file21.getRelPath()).delete();
		VFSManager.olatRootFile(container2.getRelPath()).delete();
		VFSManager.olatRootFile(file12.getRelPath()).delete();
		descendants = vfsRepositoryService.getDescendants(container.getMetaInfo(), Boolean.FALSE);
		assertThat(descendants).containsExactlyInAnyOrder(
				container1.getMetaInfo(),
				file11.getMetaInfo(),
				file12.getMetaInfo(),
				container2.getMetaInfo(),
				file21.getMetaInfo()
			);
		
		// Synch and check whether the matadata are deleted
		vfsRepositoryService.synchMetadatas(container);
		descendants = vfsRepositoryService.getDescendants(container.getMetaInfo(), Boolean.FALSE);
		assertThat(descendants).containsExactlyInAnyOrder(
				container1.getMetaInfo(),
				file11.getMetaInfo()
			);
	}
	
	@Test
	public void markAsDeleted_leaf() {
		// Create a folder with a file
		VFSContainer testContainer = VFSManager.olatRootContainer(VFS_TEST_DIR, null);
		String containerName = UUID.randomUUID().toString();
		VFSContainer container = testContainer.createChildContainer(containerName);
		String imageName = "imageToDelete.jpg";
		VFSLeaf imageLeaf = container.createChildLeaf(imageName);
		copyTestTxt(imageLeaf, "IMG_1491.jpg");
		dbInstance.commitAndCloseSession();
		
		// Mark the file as deleted
		imageLeaf.delete();
		dbInstance.commitAndCloseSession();
		
		// The file should be in the trash
		List<VFSMetadata> deletedDescendants = vfsRepositoryService.getDescendants(container.getMetaInfo(), Boolean.TRUE);
		Assert.assertTrue(1 == deletedDescendants.size());
		VFSMetadata imageMetadata = deletedDescendants.get(0);
		Assert.assertTrue(imageMetadata.isDeleted());
		Assert.assertTrue(imageMetadata.getUri().indexOf(VFSRepositoryService.TRASH_NAME) > -1);
		Assert.assertTrue(imageMetadata.getRelativePath().indexOf(VFSRepositoryService.TRASH_NAME) > -1);
		List<VFSMetadata> undeletedDescendants = vfsRepositoryService.getDescendants(container.getMetaInfo(), Boolean.FALSE);
		Assert.assertTrue(1 == undeletedDescendants.size());
		Assert.assertTrue(undeletedDescendants.get(0).getFilename().equals(VFSRepositoryService.TRASH_NAME));
	}
	
	@Test
	public void markAsDeleted_container() {
		// Create a folder with a file
		VFSContainer testContainer = VFSManager.olatRootContainer(VFS_TEST_DIR, null);
		VFSContainer container = testContainer.createChildContainer(UUID.randomUUID().toString());
		VFSContainer containerToTrash = container.createChildContainer(UUID.randomUUID().toString());
		VFSContainer containerToTrashSub1 = containerToTrash.createChildContainer(UUID.randomUUID().toString());
		VFSLeaf imageLeaf = containerToTrashSub1.createChildLeaf("imageToDelete.jpg");
		copyTestTxt(imageLeaf, "IMG_1491.jpg");
		dbInstance.commitAndCloseSession();
		
		// Mark as deleted
		containerToTrash.delete();
		dbInstance.commitAndCloseSession();
		
		// Assert the trash
		List<VFSMetadata> deletedDescendants = vfsRepositoryService.getDescendants(container.getMetaInfo(), null);
		Assert.assertTrue(4 == deletedDescendants.size());
		deletedDescendants.sort((m1, m2) -> Integer.compare(m1.getRelativePath().length(), m2.getRelativePath().length()));
		
		Assert.assertFalse(deletedDescendants.get(0).isDeleted());
		Assert.assertTrue(deletedDescendants.get(0).getFilename().equals(VFSRepositoryService.TRASH_NAME));
		
		Assert.assertTrue(deletedDescendants.get(1).isDeleted());
		Assert.assertTrue(deletedDescendants.get(1).getFilename().equals(containerToTrash.getName()));
		Assert.assertTrue(deletedDescendants.get(1).getRelativePath().endsWith(VFSRepositoryService.TRASH_NAME));
		
		Assert.assertTrue(deletedDescendants.get(2).isDeleted());
		Assert.assertTrue(deletedDescendants.get(2).getFilename().equals(containerToTrashSub1.getName()));
		Assert.assertTrue(deletedDescendants.get(2).getRelativePath().endsWith(VFSRepositoryService.TRASH_NAME + "/" + containerToTrash.getName()));
		
		Assert.assertTrue(deletedDescendants.get(3).isDeleted());
		Assert.assertTrue(deletedDescendants.get(3).getFilename().equals(imageLeaf.getName()));
		Assert.assertTrue(deletedDescendants.get(3).getRelativePath().endsWith(VFSRepositoryService.TRASH_NAME + "/" + containerToTrash.getName() + "/" +  containerToTrashSub1.getName()));
	}
	
	@Test
	public void markAsDeleted_mergeSubTrashes() {
		// Create a folder with a file
		VFSContainer testContainer = VFSManager.olatRootContainer(VFS_TEST_DIR, null);
		VFSContainer container = testContainer.createChildContainer(UUID.randomUUID().toString());
		VFSContainer container1 = container.createChildContainer("container1");
		VFSContainer container1Sub1 = container1.createChildContainer("container1Sub1");
		VFSContainer container1Sub1Sub1 = container1Sub1.createChildContainer("container1Sub1Sub1");
		VFSContainer container1Sub2 = container1.createChildContainer("container1Sub2");
		VFSLeaf imageLeaf1 = container1Sub1Sub1.createChildLeaf("imageToDelete1.jpg");
		copyTestTxt(imageLeaf1, "IMG_1491.jpg");
		VFSLeaf imageLeaf2 = container1Sub2.createChildLeaf("imageToDelete2.jpg");
		copyTestTxt(imageLeaf2, "IMG_1491.jpg");
		dbInstance.commitAndCloseSession();
		
		// Delete the first sub container
		container1Sub1.delete();
		dbInstance.commitAndCloseSession();
		// Delete the first sub container
		container1Sub2.delete();
		dbInstance.commitAndCloseSession();
		// Delete the first parent container
		container1.delete();
		dbInstance.commitAndCloseSession();
		
		// The file should be in the trash
		List<VFSMetadata> deletedDescendants = vfsRepositoryService.getDescendants(container.getMetaInfo(), Boolean.TRUE);
		Assert.assertTrue(6 == deletedDescendants.size());
		
		VFSMetadata deletedMetadata = deletedDescendants.stream().filter(metadata -> container1.getName().equals(metadata.getFilename())).findFirst().get();
		Assert.assertTrue(deletedMetadata.isDeleted());
		Assert.assertTrue(deletedMetadata.getRelativePath().endsWith(VFSRepositoryService.TRASH_NAME));
		
		deletedMetadata = deletedDescendants.stream().filter(metadata -> container1Sub1.getName().equals(metadata.getFilename())).findFirst().get();
		Assert.assertTrue(deletedMetadata.isDeleted());
		Assert.assertTrue(deletedMetadata.getRelativePath().endsWith(VFSRepositoryService.TRASH_NAME + "/" + container1.getName()));
		
		deletedMetadata = deletedDescendants.stream().filter(metadata -> container1Sub1Sub1.getName().equals(metadata.getFilename())).findFirst().get();
		Assert.assertTrue(deletedMetadata.isDeleted());
		Assert.assertTrue(deletedMetadata.getRelativePath().endsWith(VFSRepositoryService.TRASH_NAME + "/" + container1.getName() + "/" + container1Sub1.getName()));
		
		deletedMetadata = deletedDescendants.stream().filter(metadata -> imageLeaf1.getName().equals(metadata.getFilename())).findFirst().get();
		Assert.assertTrue(deletedMetadata.isDeleted());
		Assert.assertTrue(deletedMetadata.getRelativePath().endsWith(VFSRepositoryService.TRASH_NAME + "/" + container1.getName() + "/" + container1Sub1.getName() + "/" + container1Sub1Sub1.getName()));
		
		deletedMetadata = deletedDescendants.stream().filter(metadata -> container1Sub2.getName().equals(metadata.getFilename())).findFirst().get();
		Assert.assertTrue(deletedMetadata.isDeleted());
		Assert.assertTrue(deletedMetadata.getRelativePath().endsWith(VFSRepositoryService.TRASH_NAME + "/" + container1.getName()));
		
		deletedMetadata = deletedDescendants.stream().filter(metadata -> imageLeaf2.getName().equals(metadata.getFilename())).findFirst().get();
		Assert.assertTrue(deletedMetadata.isDeleted());
		Assert.assertTrue(deletedMetadata.getRelativePath().endsWith(VFSRepositoryService.TRASH_NAME + "/" + container1.getName() + "/" + container1Sub2.getName()));
	}
	
	private VFSLeaf createFile() {
		VFSContainer testContainer = VFSManager.olatRootContainer(VFS_TEST_DIR, null);
		return createFile(testContainer);
	}
	
	private VFSLeaf createFile(VFSContainer container) {
		String filename = UUID.randomUUID() + ".txt";
		VFSLeaf firstLeaf = container.createChildLeaf(filename);
		copyTestTxt(firstLeaf, "test.txt");
		firstLeaf.getMetaInfo();
		dbInstance.commitAndCloseSession();
		return firstLeaf;
	}
	
	private VFSLeaf createImage() {
		String filename = UUID.randomUUID() + ".jpg";
		VFSContainer testContainer = VFSManager.olatRootContainer(VFS_TEST_DIR, null);
		VFSLeaf firstLeaf = testContainer.createChildLeaf(filename);
		copyTestTxt(firstLeaf, "IMG_1491.jpg");
		return firstLeaf;
	}
	
	private VFSContainer createContainerRecursive(VFSContainer parent, int depth, int maxDepth, int numOfFiles, int numOfContainers) {
		if(depth > maxDepth) {
			return null;
		}
		
		String filename = UUID.randomUUID().toString();
		VFSContainer container = parent.createChildContainer(filename);
		
		for(int i=0; i<numOfFiles; i++) {
			String imageName = "IMG_" + depth + "_" + i + ".jpg";
			VFSLeaf image = container.createChildLeaf(imageName);
			copyTestTxt(image, "IMG_1491.jpg");
			
			for(int j=1; j<5; j++) {
				VFSLeaf thumbnail = vfsRepositoryService.getThumbnail(image, j * 20, j * 20, true);
				Assert.assertNotNull(thumbnail);
				Assert.assertTrue(thumbnail.getSize() > 32);
			}
			dbInstance.commitAndCloseSession();
		}
		
		for(int i=0; i<numOfContainers; i++) {
			createContainerRecursive(container, depth + 1, maxDepth, numOfFiles, numOfContainers);
		}
		dbInstance.commitAndCloseSession();
		return container;
	}
	
	private int copyTestTxt(VFSLeaf file, String sourceFilename) {
		try(OutputStream out = file.getOutputStream(false);
				InputStream in = VFSRepositoryServiceTest.class.getResourceAsStream(sourceFilename)) {
			return IOUtils.copy(in, out);
		} catch(Exception e) {
			log.error("", e);
			return -1;
		}
	}
}
