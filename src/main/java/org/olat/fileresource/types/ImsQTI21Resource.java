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
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.file.FileVisitOption;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.EnumSet;

import org.apache.logging.log4j.Logger;
import org.olat.core.logging.Tracing;
import org.olat.core.util.PathUtils;
import org.olat.ims.qti21.QTI21ContentPackage;
import org.olat.ims.qti21.model.xml.BadRessourceHelper;

import uk.ac.ed.ph.jqtiplus.reading.QtiXmlReader;
import uk.ac.ed.ph.jqtiplus.xmlutils.XmlReadResult;
import uk.ac.ed.ph.jqtiplus.xmlutils.locators.ChainedResourceLocator;
import uk.ac.ed.ph.jqtiplus.xmlutils.locators.NetworkHttpResourceLocator;
import uk.ac.ed.ph.jqtiplus.xmlutils.locators.ResourceLocator;

/**
 * Description:<br>
 * Try to validate the resource against QTI 2.1
 * 
 * <P>
 * Initial Date:  Jun 24, 2009 <br>
 * @author matthai
 */
public class ImsQTI21Resource extends FileResource {
	
	private static final Logger log = Tracing.createLoggerFor(ImsQTI21Resource.class);
	private static final String IMS_MANIFEST = "imsmanifest.xml";
	
	/**
	 * IMS QTI 21 file resource identifier.
	 */
	public static final String TYPE_NAME = "FileResource.IMSQTI21";

	public ImsQTI21Resource() {
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
				
				QTI21ContentPackage	cp = new QTI21ContentPackage(manifestPath);
				if(validateImsManifest(cp, new PathResourceLocator(manifestPath.getParent()))) {
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
	

	public static boolean validateImsManifest(QTI21ContentPackage cp, ResourceLocator resourceLocator) {
		try {
			if(cp.hasTest()) {
				URI test = cp.getTest().toUri();
				ResourceLocator chainedResourceLocator = createResolvingResourceLocator(resourceLocator);
				XmlReadResult result = new QtiXmlReader().read(chainedResourceLocator, test, true, true);
				if(result != null && !result.isSchemaValid()) {
					StringBuilder out = new StringBuilder();
					BadRessourceHelper.extractMessage(result.getXmlParseResult(), out);
					log.warn(out.toString());
				}
				return result != null && result.isSchemaValid() || true;
			}
			return false;
		} catch (Exception e) {
			log.error("", e);
			return false;
		}
	}
	
	public static boolean validate(File resource) {
		try {
			PathResourceLocator resourceLocator = new PathResourceLocator(resource.getParentFile().toPath());
			ResourceLocator chainedResourceLocator = createResolvingResourceLocator(resourceLocator);
			XmlReadResult result = new QtiXmlReader().read(chainedResourceLocator, resource.toURI(), true, true);
			return result != null && result.isSchemaValid();
		} catch (Exception e) {
			log.error("", e);
			return false;
		}
	}
	
	public static ResourceLocator createResolvingResourceLocator(ResourceLocator resourceLocator) {
        final ResourceLocator result = new ChainedResourceLocator(
        		resourceLocator,
        		QtiXmlReader.JQTIPLUS_PARSER_RESOURCE_LOCATOR, /* (to resolve internal HTTP resources, e.g. RP templates) */
        		new NetworkHttpResourceLocator() /* (to resolve external HTTP resources, e.g. RP templates, external items) */
        	);
        return result;
    }
	
	public static class PathResourceLocator implements ResourceLocator {
		
		private Path root;
		
		public PathResourceLocator(Path root) {
			this.root = root;
		}

		@Override
		public InputStream findResource(URI systemId) {
			 if ("file".equals(systemId.getScheme())) {
	            try {
	                return new FileInputStream(new File(systemId));
	            } catch (final Exception e) {
	                log.info("File {} does not exist:" + systemId);
	                return null;
	            }
	        } else if("jar".equals(systemId.getScheme())) {
	        	try {
					String toPath = systemId.toString();
					if(toPath.contains("!")) {
						int index = toPath.indexOf('!');
						String relative = toPath.substring(index + 1);
						Path newPath = root.resolve(relative);
						return Files.newInputStream(newPath);
					}
					
				} catch (Exception e) {
	                log.error("File {} does not exist:" + systemId, e);
	                return null;
				}
	        } else if("zip".equals(systemId.getScheme())) {
	        	try {
					String toPath = systemId.toString();
					if(toPath.contains(":")) {
						int index = toPath.indexOf(':');
						String relative = toPath.substring(index + 1);
						Path newPath = root.resolve(relative);
						return Files.newInputStream(newPath);
					}
					
				} catch (Exception e) {
	                log.error("File {} does not exist:" + systemId, e);
	                return null;
				}
	        }
			return null;
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
