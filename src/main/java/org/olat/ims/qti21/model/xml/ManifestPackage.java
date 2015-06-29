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

import java.io.OutputStream;
import java.util.UUID;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;

import org.olat.core.logging.OLog;
import org.olat.core.logging.Tracing;
import org.olat.imscp.xml.manifest.FileType;
import org.olat.imscp.xml.manifest.ManifestMetadataType;
import org.olat.imscp.xml.manifest.ManifestType;
import org.olat.imscp.xml.manifest.ObjectFactory;
import org.olat.imscp.xml.manifest.OrganizationsType;
import org.olat.imscp.xml.manifest.ResourceType;
import org.olat.imscp.xml.manifest.ResourcesType;

/**
 * 
 * Initial date: 04.06.2015<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class ManifestPackage {
	
	private static final OLog log = Tracing.createLoggerFor(ManifestPackage.class);
	private static final ObjectFactory objectFactory = new ObjectFactory();
	
	public static ManifestType createEmptyManifest() {
		ManifestType manifestType = objectFactory.createManifestType();
        ManifestMetadataType metadataType = objectFactory.createManifestMetadataType();
        metadataType.setSchema("QTIv2.1 Package");
        metadataType.setSchemaversion("1.0.0");
        manifestType.setMetadata(metadataType);
        
        OrganizationsType organisationsType = objectFactory.createOrganizationsType();
        manifestType.setOrganizations(organisationsType);

        ResourcesType resourcesType = objectFactory.createResourcesType();
        manifestType.setResources(resourcesType);
        return manifestType;
	}
	
	public static String appendAssessmentTest(ManifestType manifest) {
		String testId = "id" + UUID.randomUUID().toString();
        String testFileName = testId + ".xml";
        ResourceType testResourceType = objectFactory.createResourceType();
        testResourceType.setIdentifier(testId);
        testResourceType.setType("imsqti_test_xmlv2p1");
        testResourceType.setHref(testFileName);
        manifest.getResources().getResource().add(testResourceType);

        appendFile(testResourceType, testFileName);
        return testFileName;
	}
	
	public static void appendAssessmentTest(String testFileName, ManifestType manifest) {  
		String testId = "id" + UUID.randomUUID().toString();
        ResourceType testResourceType = objectFactory.createResourceType();
        testResourceType.setIdentifier(testId);
        testResourceType.setType("imsqti_test_xmlv2p1");
        testResourceType.setHref(testFileName);
        manifest.getResources().getResource().add(testResourceType);

        appendFile(testResourceType, testFileName);
	}
	
	public static String appendAssessmentItem(ManifestType manifest) {
		String itemId = "id" + UUID.randomUUID().toString();
        String itemFileName = itemId + ".xml";
        
        ResourceType itemResourceType = objectFactory.createResourceType();
        itemResourceType.setIdentifier(itemId);
        itemResourceType.setType("imsqti_item_xmlv2p1");
        itemResourceType.setHref(itemFileName);
        manifest.getResources().getResource().add(itemResourceType);
        
        appendFile(itemResourceType, itemFileName);
		return itemFileName;
	}
	
	public static void appendAssessmentItem(String itemFileName, ManifestType manifest) {
		String itemId = "id" + UUID.randomUUID().toString();

        ResourceType itemResourceType = objectFactory.createResourceType();
        itemResourceType.setIdentifier(itemId);
        itemResourceType.setType("imsqti_item_xmlv2p1");
        itemResourceType.setHref(itemFileName);
        manifest.getResources().getResource().add(itemResourceType);
        
        appendFile(itemResourceType, itemFileName);
	}

	public static void appendFile(ResourceType resource, String href) {
		FileType itemFileType = objectFactory.createFileType();
        itemFileType.setHref(href);
        resource.getFile().add(itemFileType);
	}
	
	public static void write(ManifestType manifest, OutputStream out) {
        try {
			JAXBContext context = JAXBContext.newInstance("org.olat.imscp.xml.manifest");
			Marshaller marshaller = context.createMarshaller();
			marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
			marshaller.setProperty(Marshaller.JAXB_SCHEMA_LOCATION, "http://www.imsglobal.org/xsd/imscp_v1p1 http://www.imsglobal.org/xsd/qti/qtiv2p1/qtiv2p1_imscpv1p2_v1p0.xsd");

			marshaller.marshal(objectFactory.createManifest(manifest), out);
		} catch (JAXBException e) {
			log.error("", e);
		}
	}
}
