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
import java.net.URISyntaxException;
import java.net.URL;
import java.util.List;
import java.util.UUID;

import org.junit.Assert;
import org.junit.Test;
import org.olat.core.util.FileUtils;
import org.olat.core.util.WebappHelper;
import org.olat.imsqti.xml.manifest.QTIMetadataType;
import org.olat.oo.xml.manifest.OpenOLATMetadataType;

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
        
        File tmpDir = new File(WebappHelper.getTmpDir(), "itembuilder" + UUID.randomUUID());
		tmpDir.mkdirs();
        

        
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
	
	/**
	 * 
	 * @throws URISyntaxException
	 */
	@Test
	public void readManifest() throws URISyntaxException {
		URL xmlUrl = ManifestPackageTest.class.getResource("resources/manifest/oo_assessmentitem_imsmanifest_12_2.xml");
		File xmlFile = new File(xmlUrl.toURI());
		ManifestBuilder manifest = ManifestBuilder.read(xmlFile);
		
		
		ManifestMetadataBuilder questionMetadata = manifest.getResourceBuilderByHref("sca9b540c9684ba58f489f02e8b5c590.xml");
		Assert.assertNotNull(questionMetadata);
		
		//LOM
		String title = questionMetadata.getTitle();
		Assert.assertEquals("Metadata", title);
		String identifier = questionMetadata.getIdentifier();
		Assert.assertEquals("id9f1ae47b-dc7f-482e-a688-111287f99fa6", identifier);
		

		String keywords = questionMetadata.getGeneralKeywords();
		Assert.assertTrue(keywords.contains("Meta"));
		Assert.assertTrue(keywords.contains("data"));
		Assert.assertTrue(keywords.contains("keywords"));
		
		String context = "de";
		Assert.assertEquals("de", context);
		
		//educational
		String educationContext = questionMetadata.getEducationContext();
		Assert.assertEquals("Primarschule", educationContext);
		String typicalLearningTime = "P1DT2H3M4S";
		Assert.assertEquals("P1DT2H3M4S", typicalLearningTime);
		
		//lifecycle
		String version = "1.0";
		Assert.assertEquals("1.0", version);
		
		// classification
		String taxonomyPath = questionMetadata.getClassificationTaxonomy();
		Assert.assertEquals("/Mathematik/Topologie", taxonomyPath);

		//QTI 2.1
		QTIMetadataType qtiMetadata = questionMetadata.getQtiMetadata(false);
		Assert.assertTrue(qtiMetadata.getInteractionType().contains("choiceInteraction"));
		Assert.assertEquals("OpenOLAT", qtiMetadata.getToolName());
		Assert.assertEquals("12.3a", qtiMetadata.getToolVersion());
		
		//OpenOLAT specific
		OpenOLATMetadataType openolatMetadata = questionMetadata.getOpenOLATMetadata(false);
		Assert.assertEquals(Double.valueOf(0.5d), openolatMetadata.getDiscriminationIndex());
		Assert.assertEquals(Double.valueOf(0.3d), openolatMetadata.getDifficulty());
		Assert.assertEquals(Double.valueOf(0.4d), openolatMetadata.getStandardDeviation());
		Assert.assertEquals(Integer.valueOf(1), openolatMetadata.getDistractors());
		Assert.assertEquals("sc", openolatMetadata.getQuestionType());
		Assert.assertEquals(Integer.valueOf(12), openolatMetadata.getUsage());
		Assert.assertEquals("formative", openolatMetadata.getAssessmentType());

	}
}
