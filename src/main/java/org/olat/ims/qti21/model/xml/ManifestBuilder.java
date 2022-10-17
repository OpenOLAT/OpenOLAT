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
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.UUID;

import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBElement;
import jakarta.xml.bind.JAXBException;
import jakarta.xml.bind.Marshaller;

import org.apache.logging.log4j.Logger;
import org.olat.core.logging.Tracing;
import org.olat.ims.qti21.model.IdentifierGenerator;
import org.olat.imscp.xml.manifest.FileType;
import org.olat.imscp.xml.manifest.ManifestMetadataType;
import org.olat.imscp.xml.manifest.ManifestType;
import org.olat.imscp.xml.manifest.MetadataType;
import org.olat.imscp.xml.manifest.ResourceType;
import org.olat.imscp.xml.manifest.ResourcesType;
import org.olat.imsmd.xml.manifest.LomType;
import org.olat.imsmd.xml.manifest.TechnicalType;

/**
 * manifest
 * -> metadata
 * -> organizations
 * -> resources
 * -> -> resource
 * -> -> -> metadata
 * -> -> -> file
 * -> -> -> dependency
 * Initial date: 22.02.2016<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class ManifestBuilder {
	
	private static final Logger log = Tracing.createLoggerFor(ManifestBuilder.class);
	
	protected static final org.olat.oo.xml.manifest.ObjectFactory ooObjectFactory = new org.olat.oo.xml.manifest.ObjectFactory();
	protected static final org.olat.imscp.xml.manifest.ObjectFactory cpObjectFactory = new org.olat.imscp.xml.manifest.ObjectFactory();
	protected static final org.olat.imsmd.xml.manifest.ObjectFactory mdObjectFactory = new org.olat.imsmd.xml.manifest.ObjectFactory();
	protected static final org.olat.imsqti.xml.manifest.ObjectFactory qtiObjectFactory = new org.olat.imsqti.xml.manifest.ObjectFactory();
	
	public static final String ASSESSMENTTEST_MIMETYPE = "text/x-imsqti-test-xml";
	public static final String ASSESSMENTITEM_MIMETYPE = "text/x-imsqti-item-xml";
	
	public static final String SCHEMA_LOCATIONS = "http://www.imsglobal.org/xsd/imscp_v1p1 http://www.imsglobal.org/xsd/imscp_v1p2.xsd http://www.imsglobal.org/xsd/imsmd_v1p2 http://www.imsglobal.org/xsd/imsmd_v1p2p4.xsd http://www.imsglobal.org/xsd/imsqti_metadata_v2p1 http://www.imsglobal.org/xsd/qti/qtiv2p1/imsqti_metadata_v2p1.xsd";

	private static JAXBContext context;
	static {
		try {
			context = JAXBContext.newInstance("org.olat.imscp.xml.manifest:org.olat.imsqti.xml.manifest:org.olat.imsmd.xml.manifest:org.olat.oo.xml.manifest");
		} catch (JAXBException e) {
			log.error("", e);
		}
	}

	private final ManifestType manifest;

	public ManifestBuilder() {
		manifest = cpObjectFactory.createManifestType();
	}
	
	public ManifestBuilder(ManifestType manifest) {
		this.manifest = manifest;
	}
	
	/**
	 * Create a manifest with some metadata specific to the
	 * assessment test.
	 * 
	 * @return
	 */
	public static ManifestBuilder createAssessmentTestBuilder() {
		ManifestBuilder builder = new ManifestBuilder();
		//schema
		ManifestMetadataType metadataType = createManifestMetadataType();
        metadataType.setSchema("QTIv2.1 Package");
        metadataType.setSchemaversion("1.0.0");
        builder.manifest.setMetadata(metadataType);
        //lom technical
        LomType lom = createLom(true, true);
        metadataType.getAny().add(mdObjectFactory.createLom(lom));
        return builder;
	}
	
	/**
	 * Create a manifest with some metadata specific to the
	 * assessment item.
	 * 
	 * @return
	 */
	public static ManifestBuilder createAssessmentItemBuilder() {
		ManifestBuilder builder = new ManifestBuilder();
		//schema
		ManifestMetadataType metadataType = createManifestMetadataType();
        builder.manifest.setMetadata(metadataType);
        //lom technical
        LomType lom = createLom(false, true);
        metadataType.getAny().add(mdObjectFactory.createLom(lom));
        return builder;
	}
	
	private static ManifestMetadataType createManifestMetadataType() {
		ManifestMetadataType metadataType = cpObjectFactory.createManifestMetadataType();
        metadataType.setSchema("QTIv2.1 Package");
        metadataType.setSchemaversion("1.0.0");
        return metadataType;
	}
	
	private static LomType createLom(boolean assessmentTest, boolean assessmentItem) {
		LomType lom = mdObjectFactory.createLomType();
		TechnicalType technical = mdObjectFactory.createTechnicalType();
		if(assessmentTest) {
			technical.getContent().add(createTechnicalFormat(ASSESSMENTTEST_MIMETYPE));
		}
		if(assessmentItem) {
			technical.getContent().add(createTechnicalFormat(ASSESSMENTITEM_MIMETYPE));
		}
        return lom;
	}
	
	private static JAXBElement<String> createTechnicalFormat(String format) {
		return mdObjectFactory.createFormat(format);
	}
	
	public String appendAssessmentTest() {
		String testId = "test" + UUID.randomUUID().toString();
        String testFilename = testId + ".xml";
		appendAssessmentTest(testId, testFilename);
        return testFilename;
	}
	
	public String appendAssessmentTest(String testFilename) {
		String testId = "test" + UUID.randomUUID().toString();
		appendAssessmentTest(testId, testFilename);
        return testFilename;
	}
	
	private final void appendAssessmentTest(String testId, String testFilename) {
		ResourceType testResourceType = cpObjectFactory.createResourceType();
        testResourceType.setIdentifier(testId);
        testResourceType.setType("imsqti_test_xmlv2p1");
        testResourceType.setHref(testFilename);
        getResourceList().add(testResourceType);
        appendFile(testResourceType, testFilename);
	}
	
	public String appendAssessmentItem() {
		String itemId = IdentifierGenerator.newAsString("item");
        String itemFileName = itemId + ".xml";
		appendAssessmentItem(itemId, itemFileName);
		return itemFileName;
	}
	
	public ResourceType appendAssessmentItem(String itemFileName) {
		String itemId = IdentifierGenerator.newAsString("item");
		return appendAssessmentItem(itemId, itemFileName);
	}
	
	public ManifestMetadataBuilder getResourceBuilderByIdentifier(String resourceId) {
		ResourceType resourceType = getResourceTypeByIdentifier(resourceId);
		MetadataType metadata = getMetadata(resourceType);
		return metadata == null ? null : new ManifestMetadataBuilder(metadata);
	}
	
	public ResourceType getResourceTypeByIdentifier(String resourceId) {
		List<ResourceType> resources = getResourceList();
		for(ResourceType resource:resources) {
			if(resourceId.equals(resource.getIdentifier())) {
				return resource;
			}
		}
		return null;
	}
	
	public ManifestMetadataBuilder getResourceBuilderByHref(String href) {
		ResourceType resourceType = getResourceTypeByHref(href);
		MetadataType metadata = getMetadata(resourceType);
		return metadata == null ? null : new ManifestMetadataBuilder(metadata);
	}
	
	public MetadataType getMetadata(ResourceType resourceType) {
		if(resourceType == null) return null;
		MetadataType metadata = resourceType.getMetadata();
		if(metadata == null) {
			metadata = cpObjectFactory.createMetadataType();
			resourceType.setMetadata(metadata);
		}
		return metadata;
	}
	
	public ResourceType getResourceTypeByHref(String href) {
		List<ResourceType> resources = getResourceList();
		for(ResourceType resource:resources) {
			if(href.equals(resource.getHref())) {
				return resource;
			}
		}
		return null;
	}
	
	public ResourceType appendAssessmentItem(String itemId, String itemFileName) {
        ResourceType itemResourceType = cpObjectFactory.createResourceType();
        itemResourceType.setIdentifier(itemId);
        itemResourceType.setType("imsqti_item_xmlv2p1");
        itemResourceType.setHref(itemFileName);
        getResourceList().add(itemResourceType);
        appendFile(itemResourceType, itemFileName);
        return itemResourceType;
	}
	
	public List<ResourceType> getResourceList() {
		ResourcesType resources = manifest.getResources();
		if(resources == null) {
			resources = cpObjectFactory.createResourcesType();
			manifest.setResources(resources);
		}
		return resources.getResource();
	}
	
	public void appendFile(ResourceType resource, String href) {
		FileType itemFileType = cpObjectFactory.createFileType();
        itemFileType.setHref(href);
        resource.getFile().add(itemFileType);
	}
	
	public ManifestMetadataBuilder getMetadataBuilder(ResourceType resource, boolean create) {
		MetadataType metadata = getMetadataType(resource, create);
		return metadata == null ? null : new ManifestMetadataBuilder(metadata);
		
	}
	
	public MetadataType getMetadataType(ResourceType resource, boolean create) {
		if(resource == null) return null;
		
		MetadataType metadata = resource.getMetadata();
		if(metadata == null && create) {
			metadata = cpObjectFactory.createMetadataType();
			resource.setMetadata(metadata);
		}
		return metadata;
	}
	
	public void remove(ResourceType resource) {
		manifest.getResources().getResource().remove(resource);
	}
	
	public void build() {
		//
	}

	public final void write(File file) {
		build();
		
        try(OutputStream out = new FileOutputStream(file)) {
			Marshaller marshaller = context.createMarshaller();
			marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
			marshaller.setProperty(Marshaller.JAXB_SCHEMA_LOCATION, SCHEMA_LOCATIONS);
			marshaller.marshal(cpObjectFactory.createManifest(manifest), out);
		} catch (JAXBException | IOException e) {
			log.error("", e);
		}
	}
	
	public final void write(OutputStream out) {
		build();
		
        try {
			Marshaller marshaller = context.createMarshaller();
			marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
			marshaller.setProperty(Marshaller.JAXB_SCHEMA_LOCATION, SCHEMA_LOCATIONS);
			marshaller.marshal(cpObjectFactory.createManifest(manifest), out);
		} catch (JAXBException e) {
			log.error("", e);
		}
	}
	
	public static final ManifestBuilder read(File file) {
		return read(file.toPath());
	}
	
	public static final ManifestBuilder read(Path file) {
		try(InputStream in = Files.newInputStream(file)) {
			ManifestType manifest = (ManifestType)((JAXBElement<?>)context
					.createUnmarshaller().unmarshal(in)).getValue();
			return new ManifestBuilder(manifest);
		} catch (JAXBException | IOException e) {
			log.error("", e);
			return null;
		}
	}
}
