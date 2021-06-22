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
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.XPath;
import org.apache.logging.log4j.Logger;
import org.olat.core.logging.Tracing;
import org.olat.core.util.PathUtils;
import org.olat.ims.resources.IMSLoader;
import org.olat.repository.handlers.CourseHandler;

/**
 * Initial Date:  Apr 6, 2004
 *
 * @author Mike Stock
 */
public class ImsCPFileResource extends FileResource {
	
	private static final Logger log = Tracing.createLoggerFor(ImsCPFileResource.class);
	private static final String IMS_MANIFEST = "imsmanifest.xml";

	/**
	 * IMS CP file resource identifier.
	 */
	public static final String TYPE_NAME = "FileResource.IMSCP";

	public ImsCPFileResource() {
		super(TYPE_NAME);
	}

	public static ResourceEvaluation evaluate(File file, String filename) {
		ResourceEvaluation eval = new ResourceEvaluation();
		try {
			ImsManifestFileFilter visitor = new ImsManifestFileFilter();
			Path fPath = PathUtils.visit(file, filename, visitor);
			if(visitor.hasManifest()) {
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
					if(visitor.hasEditorTreeModel()) {
						XMLScanner scanner = new XMLScanner();
						scanner.scan(visitor.getEditorTreeModelPath());
						eval.setValid(!scanner.hasEditorTreeModelMarkup());	
					} else {
						eval.setValid(true);
					}
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
	
	private static boolean validateImsManifest(Document doc) {
		try {
			//do not throw exception already here, as it might be only a generic zip file
			if (doc == null) return false;

			// get all organization elements. need to set namespace
			Element rootElement = doc.getRootElement();
			String nsuri = rootElement.getNamespace().getURI();
			Map<String,String> nsuris = new HashMap<>(1);
			nsuris.put("ns", nsuri);

			// Check for organiztaion element. Must provide at least one... title gets ectracted from either
			// the (optional) <title> element or the mandatory identifier attribute.
			// This makes sure, at least a root node gets created in CPManifestTreeModel.
			XPath meta = rootElement.createXPath("//ns:organization");
			meta.setNamespaceURIs(nsuris);
			Element orgaEl = (Element) meta.selectSingleNode(rootElement);
			if (orgaEl == null) {
				return false;
			}

			// Check for at least one <item> element referencing a <resource>, which will serve as an entry point.
			// This is mandatory, as we need an entry point as the user has the option of setting
			// CPDisplayController to not display a menu at all, in which case the first <item>/<resource>
			// element pair gets displayed.
			XPath resourcesXPath = rootElement.createXPath("//ns:resources");
			resourcesXPath.setNamespaceURIs(nsuris);
			Element elResources = (Element)resourcesXPath.selectSingleNode(rootElement);
			if (elResources == null) {
				return false; // no <resources> element.
			}
			XPath itemsXPath = rootElement.createXPath("//ns:item");
			itemsXPath.setNamespaceURIs(nsuris);
			List items = itemsXPath.selectNodes(rootElement);
			if (items.size() == 0) {
				return false; // no <item> element.
			}
			for (Iterator iter = items.iterator(); iter.hasNext();) {
				Element item = (Element) iter.next();
				String identifierref = item.attributeValue("identifierref");
				if (identifierref == null) continue;
				XPath resourceXPath = rootElement.createXPath("//ns:resource[@identifier='" + identifierref + "']");
				resourceXPath.setNamespaceURIs(nsuris);
				Element elResource = (Element)resourceXPath.selectSingleNode(elResources);
				if (elResource == null) {
					return false;
				}
				if (elResource.attribute("scormtype") != null) {
					return false;
				}
				if (elResource.attribute("scormType") != null) {
					return false;
				}
				if (elResource.attribute("SCORMTYPE") != null) {
					return false;
				}
				if (elResource.attributeValue("href") != null) {
					return true; // success.
				}
			}
		} catch (Exception e) {
			log.warn("", e);
		}
		return false;
	}
	
	private static class ImsManifestFileFilter extends SimpleFileVisitor<Path> {
		private boolean course;
		private boolean manifestFile;
		
		private Path manifestPath;
		private Path editorTreeModelPath;

		@Override
		public FileVisitResult visitFile(Path file, BasicFileAttributes attrs)
		throws IOException {

			String filename = file.getFileName().toString();
			if(IMS_MANIFEST.equals(filename)) {
				manifestFile = true;
				manifestPath = file;
			}
			
			if(CourseHandler.EDITOR_XML.equals(filename)) {
				course = true;
				editorTreeModelPath = file;
			}
			return manifestFile ? FileVisitResult.TERMINATE : FileVisitResult.CONTINUE;
		}
		
		public boolean hasManifest() {
			return manifestFile;
		}
		
		public boolean hasEditorTreeModel() {
			return course;
		}
		
		public Path getManifestPath() {
			return manifestPath;
		}

		public Path getEditorTreeModelPath() {
			return editorTreeModelPath;
		}
	}
}