package org.olat.ims.qti21.model.xml;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.olat.imscp.xml.manifest.ManifestType;

import uk.ac.ed.ph.jqtiplus.utils.contentpackaging.ContentPackageResource;
import uk.ac.ed.ph.jqtiplus.utils.contentpackaging.ImsManifestException;
import uk.ac.ed.ph.jqtiplus.utils.contentpackaging.QtiContentPackageExtractor;
import uk.ac.ed.ph.jqtiplus.utils.contentpackaging.QtiContentPackageSummary;
import uk.ac.ed.ph.jqtiplus.xmlutils.XmlResourceNotFoundException;

public class ManifestPackageTest {
	
	@Test
	public void makeManifest() throws XmlResourceNotFoundException, ImsManifestException, IOException {
		ManifestType manifestType = ManifestPackage.createEmptyManifest();
        String testFilename = ManifestPackage.appendAssessmentTest(manifestType);
        String itemFilename = ManifestPackage.appendAssessmentItem(manifestType);	
        Assert.assertNotNull(testFilename);
        Assert.assertNotNull(itemFilename);
        
        File tmpDir = new File("/HotCoffee/tmp/imsmanifest/");
        if(!tmpDir.exists()) {
        	tmpDir.mkdirs();
        }
        
        
        FileOutputStream out = new FileOutputStream(new File(tmpDir, "imsmanifest.xml"));
        ManifestPackage.write(manifestType, out);
        out.flush();
        out.close(); 
        
        QtiContentPackageExtractor extractor = new QtiContentPackageExtractor(tmpDir);
        QtiContentPackageSummary summary = extractor.parse();
        List<ContentPackageResource> items = summary.getItemResources();
        List<ContentPackageResource> tests = summary.getTestResources();
        Assert.assertEquals(1, items.size());
        Assert.assertEquals(1, tests.size());
	}
}
