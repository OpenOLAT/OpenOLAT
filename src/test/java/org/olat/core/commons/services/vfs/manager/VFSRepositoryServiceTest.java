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

import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;

import org.apache.commons.io.IOUtils;
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
import org.apache.logging.log4j.Logger;
import org.olat.core.logging.Tracing;
import org.olat.core.util.vfs.VFSConstants;
import org.olat.core.util.vfs.VFSContainer;
import org.olat.core.util.vfs.VFSLeaf;
import org.olat.core.util.vfs.VFSManager;
import org.olat.test.OlatTestCase;
import org.springframework.beans.factory.annotation.Autowired;

/**
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
	public void getMetadataFor_file() {
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
	public void readWriteBinary() {
		String filename = UUID.randomUUID() + ".txt";
		VFSContainer testContainer = VFSManager.olatRootContainer(VFS_TEST_DIR, null);
		VFSLeaf leaf = testContainer.createChildLeaf(filename);
		Assert.assertEquals(VFSConstants.YES, leaf.canMeta());
		copyTestTxt(leaf);
		
		VFSMetadata metaInfo = leaf.getMetaInfo();
		metaInfo.setComment("A little comment");
		vfsRepositoryService.updateMetadata(metaInfo);
		
		byte[] binaryData = MetaInfoReader.toBinaries(metaInfo);
		Assert.assertNotNull(binaryData);
		Assert.assertTrue(binaryData.length > 0);
		
		
		String secondFilename = UUID.randomUUID() + ".txt";
		VFSLeaf secondLeaf = testContainer.createChildLeaf(secondFilename);
		copyTestTxt(secondLeaf);

		VFSMetadata secondMetaInfo = leaf.getMetaInfo();
		vfsRepositoryService.copyBinaries(secondMetaInfo, binaryData);
		String comment = secondMetaInfo.getComment();
		Assert.assertEquals("A little comment", comment);
	}
	
	private VFSLeaf createFile() {
		String filename = UUID.randomUUID() + ".txt";
		VFSContainer testContainer = VFSManager.olatRootContainer(VFS_TEST_DIR, null);
		VFSLeaf firstLeaf = testContainer.createChildLeaf(filename);
		copyTestTxt(firstLeaf);
		return firstLeaf;
	}
	
	private int copyTestTxt(VFSLeaf file) {
		try(OutputStream out = file.getOutputStream(false);
				InputStream in = VFSRepositoryServiceTest.class.getResourceAsStream("test.txt")) {
			return IOUtils.copy(in, out);
		} catch(Exception e) {
			log.error("", e);
			return -1;
		}
	}
}
