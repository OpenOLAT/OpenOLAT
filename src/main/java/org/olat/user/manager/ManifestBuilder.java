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
package org.olat.user.manager;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;
import java.util.UUID;

import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;
import jakarta.xml.bind.Marshaller;

import org.apache.logging.log4j.Logger;
import org.olat.core.logging.Tracing;
import org.olat.imscp.xml.manifest.FileType;
import org.olat.imscp.xml.manifest.ManifestMetadataType;
import org.olat.imscp.xml.manifest.ManifestType;
import org.olat.imscp.xml.manifest.MetadataType;
import org.olat.imscp.xml.manifest.ResourceType;
import org.olat.imscp.xml.manifest.ResourcesType;

/**
 * 
 * Initial date: 24 mai 2018<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class ManifestBuilder {
	
	private static final Logger log = Tracing.createLoggerFor(ManifestBuilder.class);
	private static final org.olat.imscp.xml.manifest.ObjectFactory objectFactory = new org.olat.imscp.xml.manifest.ObjectFactory();

	public static final String SCHEMA_LOCATIONS = "http://www.imsglobal.org/xsd/imscp_v1p1 http://www.imsglobal.org/xsd/imscp_v1p2.xsd http://www.imsglobal.org/xsd/imsmd_v1p2 http://www.imsglobal.org/xsd/imsmd_v1p2p4.xsd";

	private static JAXBContext context;
	static {
		try {
			context = JAXBContext.newInstance("org.olat.imscp.xml.manifest:org.olat.imsmd.xml.manifest");
		} catch (JAXBException e) {
			log.error("", e);
		}
	}

	private final ManifestType manifest;

	public ManifestBuilder() {
		manifest = objectFactory.createManifestType();
	}
	
	/**
	 * Create a manifest.
	 * 
	 * @return The builder
	 */
	public static ManifestBuilder createBuilder() {
		ManifestBuilder builder = new ManifestBuilder();
		ManifestMetadataType metadataType = objectFactory.createManifestMetadataType();
		metadataType.setSchema("IMS Content");
        metadataType.setSchemaversion("1.1");
        builder.manifest.setMetadata(metadataType);
        return builder;
	}
	
	public MetadataType getMetadata(ResourceType resourceType) {
		if(resourceType == null) return null;
		MetadataType metadata = resourceType.getMetadata();
		if(metadata == null) {
			metadata = objectFactory.createMetadataType();
			resourceType.setMetadata(metadata);
		}
		return metadata;
	}
	
	public List<ResourceType> getResourceList() {
		ResourcesType resources = manifest.getResources();
		if(resources == null) {
			resources = objectFactory.createResourcesType();
			manifest.setResources(resources);
		}
		return resources.getResource();
	}
	
	public void appendFile(String href) {
		ResourceType fileResourceType = objectFactory.createResourceType();
        fileResourceType.setIdentifier(UUID.randomUUID().toString());
        fileResourceType.setHref(href);
        getResourceList().add(fileResourceType);
		FileType itemFileType = objectFactory.createFileType();
        itemFileType.setHref(href);
        fileResourceType.getFile().add(itemFileType);
	}
	
	public MetadataType getMetadataType(ResourceType resource, boolean create) {
		if(resource == null) return null;
		
		MetadataType metadata = resource.getMetadata();
		if(metadata == null && create) {
			metadata = objectFactory.createMetadataType();
			resource.setMetadata(metadata);
		}
		return metadata;
	}

	public final void write(File file) {
        try(OutputStream out = new FileOutputStream(file)) {
			Marshaller marshaller = context.createMarshaller();
			marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
			marshaller.setProperty(Marshaller.JAXB_SCHEMA_LOCATION, SCHEMA_LOCATIONS);
			//TODO j2ee9 marshaller.marshal(objectFactory.createManifest(manifest), out);
		} catch (JAXBException | IOException e) {
			log.error("", e);
		}
	}
}
