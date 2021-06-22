/**
* OLAT - Online Learning and Training<br>
* http://www.olat.org
* <p>
* Licensed under the Apache License, Version 2.0 (the "License"); <br>
* you may not use this file except in compliance with the License.<br>
* You may obtain a copy of the License at
* <p>
* http://www.apache.org/licenses/LICENSE-2.0
* <p>
* Unless required by applicable law or agreed to in writing,<br>
* software distributed under the License is distributed on an "AS IS" BASIS, <br>
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
* See the License for the specific language governing permissions and <br>
* limitations under the License.
* <p>
* Copyright (c) since 2004 at Multimedia- & E-Learning Services (MELS),<br>
* University of Zurich, Switzerland.
* <hr>
* <a href="http://www.openolat.org">
* OpenOLAT - Online Learning and Training</a><br>
* This file has been modified by the OpenOLAT community. Changes are licensed
* under the Apache 2.0 license as the original file.
*/

package org.olat.fileresource.types;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileVisitOption;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.dom4j.Attribute;
import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.Namespace;
import org.dom4j.Node;
import org.dom4j.XPath;
import org.apache.logging.log4j.Logger;
import org.olat.core.logging.Tracing;
import org.olat.core.util.PathUtils;
import org.olat.ims.resources.IMSLoader;

/**
 * Initial Date:  Apr 6, 2004
 *
 * @author Mike Stock
 */
public class ScormCPFileResource extends FileResource {
	
	private static final Logger log = Tracing.createLoggerFor(ScormCPFileResource.class);
	private static final String IMS_MANIFEST = "imsmanifest.xml";
	
	/**
	 * SCORM IMS CP file resource identifier.
	 */
	public static final String TYPE_NAME = "FileResource.SCORMCP";

	public ScormCPFileResource() {
		super(TYPE_NAME);
	}
	
	public static ResourceEvaluation evaluate(File file, String filename) {
		ResourceEvaluation eval = new ResourceEvaluation();
		try {
			ImsManifestFileFilter visitor = new ImsManifestFileFilter();
			Path fPath = PathUtils.visit(file, filename, visitor);
			if(visitor.isValid()) {
				Path realManifestPath = visitor.getManifestPath();
				Path manifestPath = fPath.resolve(realManifestPath);
				
				RootSearcher rootSearcher = new RootSearcher();
				Files.walkFileTree(fPath, EnumSet.noneOf(FileVisitOption.class), 16, rootSearcher);
				if(rootSearcher.foundRoot()) {
					manifestPath = rootSearcher.getRoot().resolve(IMS_MANIFEST);
				} else {
					manifestPath = fPath.resolve(IMS_MANIFEST);
				}
				
				Document doc = IMSLoader.loadIMSDocument(manifestPath);
				if(validateImsManifest(doc)) {
					eval.setValid(true);
				} else {
					eval.setValid(false);
				}
			} else {
				eval.setValid(false);
			}
			PathUtils.closeSubsequentFS(fPath);
		} catch (IOException | IllegalArgumentException e) {
			log.error("", e);
			eval.setValid(false);
		}
		return eval;
	}
	
	public static boolean validateImsManifest(Document doc) {
		
		try {
			//do not throw exception already here, as it might be only a generic zip file
			if (doc == null) return false;
			
			String adluri = null;
			String seqencingUri = null;
			String simpleSeqencingUri = null;
			// get all organization elements. need to set namespace
			Element rootElement = doc.getRootElement();
			String nsuri = rootElement.getNamespace().getURI();
			// look for the adl cp namespace that differs a scorm package from a normal cp package
			Namespace nsADL = rootElement.getNamespaceForPrefix("adlcp");
			if (nsADL != null ) adluri = nsADL.getURI();
			Namespace nsADLSeq = rootElement.getNamespaceForPrefix("adlseq");
			if (nsADLSeq != null ) seqencingUri = nsADLSeq.getURI();
			Namespace nsADLSS = rootElement.getNamespaceForPrefix("imsss");
			if (nsADLSS != null ) simpleSeqencingUri = nsADLSS.getURI();
			// we can only support scorm 1.2 so far.
			if (adluri != null && !((adluri.indexOf("adlcp_rootv1p2") != -1) || (adluri.indexOf("adlcp_rootv1p3") != -1))){
				//we dont have have scorm 1.2 or 1.3 namespace so it can't be a scorm package
				return false;
			}
			
			Map<String, String> nsuris = new HashMap<>(5);
			nsuris.put("ns", nsuri);
			//nsuris.put("adluri", adluri);
			//we might have a scorm 2004 which we do not yet support
			if (seqencingUri != null) nsuris.put("adlseq", seqencingUri);
			if (simpleSeqencingUri != null) nsuris.put("imsss", simpleSeqencingUri);

			// Check for organization element. Must provide at least one... title gets extracted from either
			// the (optional) <title> element or the mandatory identifier attribute.
			// This makes sure, at least a root node gets created in CPManifestTreeModel.	
			XPath meta = rootElement.createXPath("//ns:organization");
			meta.setNamespaceURIs(nsuris);
			Element orgaEl = (Element) meta.selectSingleNode(rootElement);
			if (orgaEl == null) {
				return false;
			}

			// Check for at least one <item> element referencing a <resource> of adlcp:scormtype="sco" or "asset",
			// which will serve as an entry point.
			XPath resourcesXPath = rootElement.createXPath("//ns:resources");
			resourcesXPath.setNamespaceURIs(nsuris);
			Element elResources = (Element)resourcesXPath.selectSingleNode(rootElement);
			if (elResources == null) {
				return false; 
			}
			XPath itemsXPath = rootElement.createXPath("//ns:item");
			itemsXPath.setNamespaceURIs(nsuris);
			List<Node> items = itemsXPath.selectNodes(rootElement);
			if (items.isEmpty()) {
				return false; // no <item> element.
			}
			
			// check for scorm 2004 simple sequencing stuff which we do not yet support
			if (seqencingUri != null) {
					XPath seqencingXPath = rootElement.createXPath("//ns:imsss");
					List<Node> sequences = seqencingXPath.selectNodes(rootElement);
					if (!sequences.isEmpty()) {
						return false; // seqencing elements found -> scorm 2004
					}
			}
			
			Set<String> set = new HashSet<>();
			for (Iterator<Node> iter = items.iterator(); iter.hasNext();) {
				Element item = (Element) iter.next();
				String identifier = item.attributeValue("identifier");
				//check if identifiers are unique, reject if not so
				if (!set.add(identifier)) {
					return false;
				}
			}
			
			
			for (Iterator<Node> iter = items.iterator(); iter.hasNext();) {
				Element item = (Element) iter.next();
				String identifierref = item.attributeValue("identifierref");
				if (identifierref == null) continue;
				XPath resourceXPath = rootElement.createXPath("//ns:resource[@identifier='" + identifierref + "']");
				resourceXPath.setNamespaceURIs(nsuris);
				Element elResource = (Element)resourceXPath.selectSingleNode(elResources);
				if (elResource == null) {
					return false;
				}
				//check for scorm attribute
				Attribute scormAttr = elResource.attribute("scormtype");
				//some packages have attribute written like "scormType"
				Attribute scormAttrUpper = elResource.attribute("scormType");
				if (scormAttr == null && scormAttrUpper == null) {
					return false;
				}
				String attr = "";
				if (scormAttr != null) attr = scormAttr.getStringValue();
				if (scormAttrUpper != null) attr = scormAttrUpper.getStringValue();
				if (attr == null) {
					return false;
				}
				if (elResource.attributeValue("href") != null && (attr.equalsIgnoreCase("sco") || attr.equalsIgnoreCase("asset"))) {
					return true; // success.
				}
			}
			return false;
		} catch (Exception e) {
			log.warn("Not a valid SCORM package", e);
			return false;
		}
	}
	
	private static class ImsManifestFileFilter extends SimpleFileVisitor<Path> {
		private boolean manifestFile;
		private Path manifestPath;

		@Override
		public FileVisitResult visitFile(Path file, BasicFileAttributes attrs)
		throws IOException {

			String filename = file.getFileName().toString();
			if(IMS_MANIFEST.equals(filename)) {
				manifestFile = true;
				manifestPath = file;
			}
			return manifestFile ? FileVisitResult.TERMINATE : FileVisitResult.CONTINUE;
		}
		
		public boolean isValid() {
			return manifestFile;
		}

		public Path getManifestPath() {
			return manifestPath;
		}
	}
}
