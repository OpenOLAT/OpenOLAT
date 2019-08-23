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
package org.olat.portfolio.manager;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.io.Writer;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import javax.xml.transform.Source;
import javax.xml.transform.Templates;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.apache.commons.io.IOUtils;
import org.apache.logging.log4j.Logger;
import org.olat.core.logging.Tracing;
import org.olat.core.util.xml.XStreamHelper;
import org.olat.portfolio.model.restriction.CollectRestriction;
import org.olat.portfolio.model.structel.EPAbstractMap;
import org.olat.portfolio.model.structel.EPDefaultMap;
import org.olat.portfolio.model.structel.EPPage;
import org.olat.portfolio.model.structel.EPStructureElement;
import org.olat.portfolio.model.structel.EPStructureToArtefactLink;
import org.olat.portfolio.model.structel.EPStructureToStructureLink;
import org.olat.portfolio.model.structel.EPStructuredMap;
import org.olat.portfolio.model.structel.EPStructuredMapTemplate;
import org.olat.portfolio.model.structel.PortfolioStructure;
import org.olat.resource.OLATResourceImpl;

import com.thoughtworks.xstream.XStream;
import org.olat.resource.OLATResourceImpl;

/**
 * 
 * 
 * 
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
public class EPXStreamHandler {
	
	private static final Logger log = Tracing.createLoggerFor(EPXStreamHandler.class);

	private static final XStream myStream = XStreamHelper.createXStreamInstanceForDBObjects();
	private static Templates filterArtefactsTemplates;
	
	static { // initialize supported types
		myStream.alias("defaultMap", EPDefaultMap.class);
		myStream.alias("structureMap", EPStructuredMap.class);
		myStream.alias("templateMap", EPStructuredMapTemplate.class);
		myStream.alias("structure", EPStructureElement.class);
		myStream.alias("page", EPPage.class);
		myStream.alias("structureToArtefact", EPStructureToArtefactLink.class);
		myStream.alias("structureToStructure", EPStructureToStructureLink.class);
		myStream.alias("collectionRestriction", CollectRestriction.class);
		myStream.alias("olatResource", OLATResourceImpl.class);
		myStream.alias("OLATResource", OLATResourceImpl.class);
		myStream.omitField(EPAbstractMap.class, "ownerGroup"); // see also OLAT-6344
		myStream.omitField(EPAbstractMap.class, "groups"); // see also OLAT-6344
		myStream.alias("olatResource", OLATResourceImpl.class);
		myStream.alias("OLATResource", OLATResourceImpl.class);
		try {
			InputStream xsltIn = EPXStreamHandler.class.getResourceAsStream("portfolio_without_artefacts.xsl");
			Source xsltSource = new StreamSource(xsltIn);
			filterArtefactsTemplates = TransformerFactory.newInstance().newTemplates(xsltSource);
		} catch (TransformerConfigurationException e) {
			log.error("", e);
		} catch (TransformerFactoryConfigurationError e) {
			log.error("", e);
		}
	}
	
	public static final PortfolioStructure copy(PortfolioStructure structure) {
		String stringuified = myStream.toXML(structure);
		PortfolioStructure newStructure = (PortfolioStructure)myStream.fromXML(stringuified);
		return newStructure;
	}
	
	public static final PortfolioStructure getAsObject(File fMapXml, boolean withArtefacts) {
		try {
			//extract from zip
			InputStream in = new FileInputStream(fMapXml);
			ZipInputStream zipIn = new ZipInputStream(in);
			//open the entry of the map
			zipIn.getNextEntry();

			Writer buffer = new StringWriter();
			if(!withArtefacts) {
				Transformer transformer = filterArtefactsTemplates.newTransformer();
				transformer.transform(new StreamSource(zipIn), new StreamResult(buffer));
			} else {
				IOUtils.copy(zipIn, buffer, "UTF-8");
			}

			PortfolioStructure struct = (PortfolioStructure) myStream.fromXML(buffer.toString());
			// OLAT-6344: reset ownerGroup from earlier exports. A new group is created by import in ePFMgr.importPortfolioMapTemplate() later on anyway.
			((EPAbstractMap) struct).setGroups(null); 
			return struct;
		} catch (Exception e) {
			log.error("Cannot export this map: " + fMapXml, e);
		}
		return null;
	}
	
	public static final InputStream toStream(PortfolioStructure structure)
	throws IOException {
		try {
			//prepare a zip
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			ZipOutputStream zipOut = new ZipOutputStream(out);
			zipOut.putNextEntry(new ZipEntry("map.xml"));
			myStream.toXML(structure, zipOut);
			zipOut.closeEntry();
			zipOut.close();
			
			//prepare media resource
			byte[] outArray = out.toByteArray();
			IOUtils.closeQuietly(out);
			return new ByteArrayInputStream(outArray);
		} catch (IOException e) {
			log.error("Cannot export this map: " + structure, e);
			return null;
		}
	}
}
