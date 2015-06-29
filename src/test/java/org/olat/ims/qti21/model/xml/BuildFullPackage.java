package org.olat.ims.qti21.model.xml;

import java.io.File;
import java.io.FileOutputStream;
import java.net.URISyntaxException;

import org.junit.Test;
import org.olat.core.logging.OLog;
import org.olat.core.logging.Tracing;
import org.olat.imscp.xml.manifest.ManifestType;

import uk.ac.ed.ph.jqtiplus.JqtiExtensionManager;
import uk.ac.ed.ph.jqtiplus.node.item.AssessmentItem;
import uk.ac.ed.ph.jqtiplus.node.test.AssessmentTest;
import uk.ac.ed.ph.jqtiplus.serialization.QtiSerializer;

public class BuildFullPackage {
	
	private static final OLog log = Tracing.createLoggerFor(BuildFullPackage.class);
	private static final File fullPackage = new File("/Users/srosse/Desktop/QTI/Full/");
	
	@Test
	public void fullPackage() throws URISyntaxException {
		if(!fullPackage.exists()) {
			fullPackage.mkdirs();
		}
		
		ManifestType manifestType = ManifestPackage.createEmptyManifest();
		
		//item
		AssessmentItem assessmentItem = new AssessmentItemPackageTest().createAssessmentItem();
		QtiSerializer qtiSerializer = new QtiSerializer(new JqtiExtensionManager());
		File itemFile = new File(fullPackage, "generated_item.xml");
		try(FileOutputStream out = new FileOutputStream(itemFile)) {
			qtiSerializer.serializeJqtiObject(assessmentItem, out);	
		} catch(Exception e) {
			log.error("", e);
		}
		ManifestPackage.appendAssessmentItem(itemFile.getName(), manifestType);	
		
		//test
		AssessmentTest assessmentTest = new AssessmentTestPackageTest().createAssessmentTest(itemFile);
        File testFile = new File(fullPackage, "generated_test.xml");
		try(FileOutputStream out = new FileOutputStream(testFile)) {
			qtiSerializer.serializeJqtiObject(assessmentTest, out);	
		} catch(Exception e) {
			log.error("", e);
		}
        ManifestPackage.appendAssessmentTest(testFile.getName(), manifestType);

        try(FileOutputStream out = new FileOutputStream(new File(fullPackage, "imsmanifest.xml"))) {
        	ManifestPackage.write(manifestType, out);
        } catch(Exception e) {
        	log.error("", e);
        }

	}
	
	

}
