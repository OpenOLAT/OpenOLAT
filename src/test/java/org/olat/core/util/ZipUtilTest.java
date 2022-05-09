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
package org.olat.core.util;

import java.io.File;
import java.net.URL;

import org.junit.Assert;
import org.junit.Test;
import org.olat.core.commons.modules.bc.FolderConfig;
import org.olat.core.id.Identity;
import org.olat.core.util.vfs.VFSContainer;
import org.olat.core.util.vfs.VFSItem;
import org.olat.core.util.vfs.VFSLeaf;
import org.olat.core.util.vfs.VFSManager;
import org.olat.core.util.vfs.callbacks.FullAccessCallback;
import org.olat.test.OlatTestCase;
import org.olat.test.VFSJavaIOFile;

/**
 * 
 * Initial date: 21 juin 2021<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class ZipUtilTest extends OlatTestCase {
	
	@Test
	public void simpleUnzipFileToDir() throws Exception {
		File tmpFolder = new File(WebappHelper.getTmpDir(), "zip" + CodeHelper.getRAMUniqueID());
		URL url = ZipUtilTest.class.getResource("Images.zip");
		File zipFile = new File(url.toURI());

		File unzipDir = new File(tmpFolder, "unzipSimple1");
		unzipDir.mkdirs();
		
		boolean success = ZipUtil.unzip(zipFile, unzipDir);
		Assert.assertTrue(success);
		FileUtils.deleteDirsAndFiles(tmpFolder, true, true);
	}
	
	@Test
	public void simpleUnzipLeafToContainer() throws Exception {
		URL url = ZipUtilTest.class.getResource("Images.zip");
		VFSLeaf zipLeaf = new VFSJavaIOFile(url.toURI());
		
		VFSContainer tmpDir = VFSManager.olatRootContainer(FolderConfig.getRelativeTmpDir());
		tmpDir.setLocalSecurityCallback(new FullAccessCallback());
		VFSContainer targetDir = tmpDir.createChildContainer("zip" + CodeHelper.getForeverUniqueID());
		boolean success = ZipUtil.unzip(zipLeaf, targetDir, (Identity)null, false);

		Assert.assertTrue(success);
		targetDir.deleteSilently();
	}
	
	@Test
	public void simpleUnzipNonStrictLeafToContainer() throws Exception {
		URL url = ZipUtilTest.class.getResource("Images.zip");
		VFSLeaf zipLeaf = new VFSJavaIOFile(url.toURI());
		
		VFSContainer tmpDir = VFSManager.olatRootContainer(FolderConfig.getRelativeTmpDir());
		tmpDir.setLocalSecurityCallback(new FullAccessCallback());
		VFSContainer targetDir = tmpDir.createChildContainer("zip" + CodeHelper.getForeverUniqueID());
		boolean success = ZipUtil.unzipNonStrict(zipLeaf, targetDir, (Identity)null, false);

		Assert.assertTrue(success);
		targetDir.deleteSilently();
	}

	@Test
	public void unzipFileToDir() throws Exception {
		File tmpFolder = new File(WebappHelper.getTmpDir(), "zip" + CodeHelper.getRAMUniqueID());
		URL url = ZipUtilTest.class.getResource("Slide.zip");
		File zipFile = new File(url.toURI());
		
		File unzipDir = new File(tmpFolder, "unzip1");
		unzipDir.mkdirs();
		
		boolean success = ZipUtil.unzip(zipFile, unzipDir);
		Assert.assertFalse(success);
		File img = new File(tmpFolder, "IMG_1489.png");
		Assert.assertFalse(img.exists());
		FileUtils.deleteDirsAndFiles(tmpFolder, true, true);
	}
	
	@Test
	public void unzipLeafToContainer() throws Exception {
		URL url = ZipUtilTest.class.getResource("Slide.zip");
		VFSLeaf zipLeaf = new VFSJavaIOFile(url.toURI());
		VFSContainer tmpDir = VFSManager.olatRootContainer(FolderConfig.getRelativeTmpDir());
		tmpDir.setLocalSecurityCallback(new FullAccessCallback());
		VFSContainer targetDir = tmpDir.createChildContainer("zip" + CodeHelper.getForeverUniqueID());
		boolean success = ZipUtil.unzip(zipLeaf, targetDir, (Identity)null, false);
		
		VFSItem img = tmpDir.resolve("IMG_1489.png");
		Assert.assertNull(img);
		Assert.assertFalse(success);
		
		targetDir.deleteSilently();
	}
	
	@Test
	public void unzipFileToDirInflationRatio() throws Exception {
		File tmpFolder = new File(WebappHelper.getTmpDir(), "zip" + CodeHelper.getRAMUniqueID());
		URL url = ZipUtilTest.class.getResource("Zero.zip");
		File zipFile = new File(url.toURI());
		
		File unzipDir = new File(tmpFolder, "unzip1");
		unzipDir.mkdirs();
		
		boolean success = ZipUtil.unzip(zipFile, unzipDir);
		Assert.assertFalse(success);
		FileUtils.deleteDirsAndFiles(tmpFolder, true, true);
	}
	
	@Test
	public void unzipLeafToContainerInflationRatio() throws Exception {
		URL url = ZipUtilTest.class.getResource("Zero.zip");
		VFSLeaf zipLeaf = new VFSJavaIOFile(url.toURI());
		VFSContainer tmpDir = VFSManager.olatRootContainer(FolderConfig.getRelativeTmpDir());
		tmpDir.setLocalSecurityCallback(new FullAccessCallback());
		VFSContainer targetDir = tmpDir.createChildContainer("zip" + CodeHelper.getForeverUniqueID());
		boolean success = ZipUtil.unzip(zipLeaf, targetDir, (Identity)null, false);
		Assert.assertFalse(success);
		
		targetDir.deleteSilently();
	}
	
	@Test
	public void unzipNonStrictLeafToContainerInflationRatio() throws Exception {
		URL url = ZipUtilTest.class.getResource("Zero.zip");
		VFSLeaf zipLeaf = new VFSJavaIOFile(url.toURI());
		
		VFSContainer tmpDir = VFSManager.olatRootContainer(FolderConfig.getRelativeTmpDir());
		tmpDir.setLocalSecurityCallback(new FullAccessCallback());
		VFSContainer targetDir = tmpDir.createChildContainer("zip" + CodeHelper.getForeverUniqueID());
		boolean success = ZipUtil.unzipNonStrict(zipLeaf, targetDir, (Identity)null, false);

		Assert.assertFalse(success);
		targetDir.deleteSilently();
	}
	
	
	/**
	 * Empty files have break the unzip loop (OO-6214)
	 * @throws Exception
	 */
	@Test
	public void unzipWithEmptyFiles() throws Exception {
		URL url = ZipUtilTest.class.getResource("Empty_file.zip");
		VFSLeaf zipLeaf = new VFSJavaIOFile(url.toURI());
		
		VFSContainer tmpDir = VFSManager.olatRootContainer(FolderConfig.getRelativeTmpDir());
		tmpDir.setLocalSecurityCallback(new FullAccessCallback());
		VFSContainer targetDir = tmpDir.createChildContainer("zip" + CodeHelper.getForeverUniqueID());
		boolean success = ZipUtil.unzip(zipLeaf, targetDir, (Identity)null, false);
		Assert.assertTrue(success);
		
		// check the empty file
		VFSItem emptyLeaf = targetDir.resolve("vorgehen2.html");
		Assert.assertNotNull(emptyLeaf);
		Assert.assertTrue(emptyLeaf instanceof VFSLeaf);
		Assert.assertEquals(0l, ((VFSLeaf)emptyLeaf).getSize());
		
		// check the other file
		VFSItem leaf = targetDir.resolve("wissens.html");
		Assert.assertNotNull(leaf);
		Assert.assertTrue(leaf instanceof VFSLeaf);
		Assert.assertTrue(((VFSLeaf)leaf).getSize() > 300);

		targetDir.deleteSilently();
	}
}
