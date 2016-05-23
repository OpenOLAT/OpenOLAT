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
package org.olat.ims.qti21.model.xml;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.olat.core.util.FileUtils;

import uk.ac.ed.ph.jqtiplus.utils.contentpackaging.ContentPackageResource;
import uk.ac.ed.ph.jqtiplus.utils.contentpackaging.ImsManifestException;
import uk.ac.ed.ph.jqtiplus.utils.contentpackaging.QtiContentPackageExtractor;
import uk.ac.ed.ph.jqtiplus.utils.contentpackaging.QtiContentPackageSummary;
import uk.ac.ed.ph.jqtiplus.xmlutils.XmlResourceNotFoundException;

/**
 * 
 * Initial date: 04.06.2015<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class ManifestPackageTest {
	
	@Test
	public void makeManifest() throws XmlResourceNotFoundException, ImsManifestException, IOException {
		ManifestBuilder manifest = ManifestBuilder.createAssessmentTestBuilder();
        String testFilename = manifest.appendAssessmentTest();
        String itemFilename = manifest.appendAssessmentItem();	
        Assert.assertNotNull(testFilename);
        Assert.assertNotNull(itemFilename);
        
        File tmpDir = Files.createTempDirectory("manifests").toFile();
        if(!tmpDir.exists()) {
        	tmpDir.mkdirs();
        }
        
        File manifestFile = new File(tmpDir, "imsmanifest.xml");
        FileOutputStream out = new FileOutputStream(manifestFile);
        manifest.write(out);
        out.flush();
        out.close(); 
        
        QtiContentPackageExtractor extractor = new QtiContentPackageExtractor(tmpDir);
        QtiContentPackageSummary summary = extractor.parse();
        List<ContentPackageResource> items = summary.getItemResources();
        List<ContentPackageResource> tests = summary.getTestResources();
        Assert.assertEquals(1, items.size());
        Assert.assertEquals(1, tests.size());
        
        ManifestBuilder reloadManifest = ManifestBuilder.read(manifestFile);
        Assert.assertNotNull(reloadManifest);
        
        FileUtils.deleteDirsAndFiles(tmpDir.toPath());
	}
}
