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

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import org.junit.Assert;
import org.junit.Test;
import org.olat.core.commons.modules.bc.FolderConfig;
import org.olat.core.commons.services.vfs.VFSRepositoryModule;
import org.olat.core.id.Identity;
import org.olat.core.util.vfs.VFSConstants;
import org.olat.test.JunitTestHelper;
import org.olat.test.OlatTestCase;


/**
 * 
 * Initial date: 19 janv. 2021<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class VFSRepositoryModuleTest extends OlatTestCase {
    
    @Test
    public void canMetaStandardFiles() {
    	Identity id = JunitTestHelper.createAndPersistIdentityAsRndUser("webdav-lock-1");
    	String userHome = FolderConfig.getUserHome(id);
    	File file = new File(FolderConfig.getCanonicalRoot(), userHome);
    	File publicFolder = new File(file, "public");
    	
    	// Standard files
    	Assert.assertEquals(VFSConstants.YES, VFSRepositoryModule.canMeta(new File(publicFolder, "Bonjour.docx")));
    	Assert.assertEquals(VFSConstants.YES, VFSRepositoryModule.canMeta(new File(publicFolder, "Hello image.jpeg")));
    	Assert.assertEquals(VFSConstants.YES, VFSRepositoryModule.canMeta(new File(publicFolder, "~Hello.tiff")));
    	
    	// System files
    	Assert.assertEquals(VFSConstants.NO, VFSRepositoryModule.canMeta(new File(publicFolder, ".DS_Store")));
    	Assert.assertEquals(VFSConstants.NO, VFSRepositoryModule.canMeta(new File(publicFolder, "__MACOSX")));

    	// Hidden files
    	Assert.assertEquals(VFSConstants.NO, VFSRepositoryModule.canMeta(new File(publicFolder, ".Hello.tiff")));
    	
    	// OpenOlat System files
    	Assert.assertEquals(VFSConstants.NO, VFSRepositoryModule.canMeta(new File(publicFolder, "._oo_meta_image.jpg")));
    	Assert.assertEquals(VFSConstants.NO, VFSRepositoryModule.canMeta(new File(publicFolder, "._oo_th_image.jpg")));
    	Assert.assertEquals(VFSConstants.NO, VFSRepositoryModule.canMeta(new File(publicFolder, "._oo_vr_image.jpg")));
    }
    
    @Test
    public void canMetaExternalFiles() throws IOException {
    	File file = File.createTempFile("external", "file");

    	Assert.assertEquals(VFSConstants.NO, VFSRepositoryModule.canMeta(file));
    	
    	Files.deleteIfExists(file.toPath());
    }
}
