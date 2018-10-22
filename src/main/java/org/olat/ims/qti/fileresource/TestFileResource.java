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

package org.olat.ims.qti.fileresource;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.FileVisitResult;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Iterator;
import java.util.List;

import org.dom4j.Attribute;
import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.Node;
import org.olat.core.logging.OLog;
import org.olat.core.logging.Tracing;
import org.olat.core.util.PathUtils;
import org.olat.core.util.vfs.LocalFileImpl;
import org.olat.core.util.vfs.LocalFolderImpl;
import org.olat.core.util.vfs.VFSContainer;
import org.olat.core.util.vfs.VFSItem;
import org.olat.core.util.xml.XMLParser;
import org.olat.fileresource.FileResourceManager;
import org.olat.fileresource.types.FileResource;
import org.olat.fileresource.types.ResourceEvaluation;
import org.olat.ims.qti.editor.beecom.objects.QTIDocument;
import org.olat.ims.qti.editor.beecom.parser.ParserManager;
import org.olat.ims.qti.process.AssessmentInstance;
import org.olat.ims.qti.process.ImsRepositoryResolver;
import org.olat.ims.qti.process.QTIHelper;
import org.olat.ims.resources.IMSEntityResolver;
import org.olat.resource.OLATResource;

/**
 * Initial Date: Apr 6, 2004
 * 
 * @author Mike Stock
 */
public class TestFileResource extends FileResource {
	private static final OLog log = Tracing.createLoggerFor(TestFileResource.class);
	private static final String QTI_FILE = "qti.xml";

	/**
	 * IMS QTI Test file resource identifier.
	 */
	public static final String TYPE_NAME = "FileResource.TEST";

	public TestFileResource() {
		super(TYPE_NAME);
	}
	
	public static QTIDocument getQTIDocument(OLATResource resource) {
		File packageDir = FileResourceManager.getInstance().unzipFileResource(resource);
		File qtiFile = new File(packageDir, ImsRepositoryResolver.QTI_FILE);
		try(InputStream in = new FileInputStream(qtiFile)) {
			XMLParser xmlParser = new XMLParser(new IMSEntityResolver());
			Document doc = xmlParser.parse(in, true);
			ParserManager parser = new ParserManager();
			return (QTIDocument)parser.parse(doc);
		} catch (Exception e) {			
			log.error("Exception when parsing input QTI input stream for ", e);
			return null;
		}
	}
	
	public static QTIReaderPackage getQTIEditorPackageReader(OLATResource resource) {
		VFSContainer baseDir = FileResourceManager.getInstance().unzipContainerResource(resource);
		QTIDocument document = getQTIDocument(resource);
		return new QTIReaderPackage(baseDir, document);	
	}
	
	/**
	 * @param unzippedDir
	 * @return True if is of type.
	 */
	public static boolean validate(File unzippedDir) {
		// no longer needed.
		VFSContainer vfsUnzippedRoot = new LocalFolderImpl(unzippedDir);
		VFSItem vfsQTI = vfsUnzippedRoot.resolve("qti.xml");
		// getDocument(..) ensures that InputStream is closed in every case.
		Document doc = QTIHelper.getDocument((LocalFileImpl) vfsQTI);
		return validateQti(doc, new ResourceEvaluation(false)).isValid();
	}
	
	public static ResourceEvaluation evaluate(File file, String filename) {
		ResourceEvaluation eval = new ResourceEvaluation();
		try {
			QTIFileFilter visitor = new QTIFileFilter();
			Path fPath = PathUtils.visit(file, filename, visitor);
			if(visitor.isValid()) {
				Path qtiPath = fPath.resolve(QTI_FILE);
				Document doc = QTIHelper.getDocument(qtiPath);
				validateQti(doc, eval);
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

	private static ResourceEvaluation validateQti(Document doc, ResourceEvaluation eval) {
		if (doc == null) {
			eval.setValid(false);
		} else {
			boolean validType = false;
			boolean validScore = true;
			
			List<Node> assessment = doc.selectNodes("questestinterop/assessment");
			if(assessment.size() == 1) {
				Object assessmentObj = assessment.get(0);
				if(assessmentObj instanceof Element) {
					Element assessmentEl = (Element)assessmentObj;
					Attribute title = assessmentEl.attribute("title");
					if(title != null) {
						eval.setDisplayname(title.getValue());
					}

					// check if this is marked as test
					List<Node> metas = assessmentEl.selectNodes("qtimetadata/qtimetadatafield");
					for (Iterator<Node> iter = metas.iterator(); iter.hasNext();) {
						Element el_metafield = (Element) iter.next();
						Element el_label = (Element) el_metafield.selectSingleNode("fieldlabel");
						String label = el_label.getText();
						if (label.equals(AssessmentInstance.QMD_LABEL_TYPE)) { // type meta
							Element el_entry = (Element) el_metafield.selectSingleNode("fieldentry");
							String entry = el_entry.getText();
							if(entry.equals(AssessmentInstance.QMD_ENTRY_TYPE_SELF)
									|| entry.equals(AssessmentInstance.QMD_ENTRY_TYPE_ASSESS)) {
								validType = true;
							}
						}
					}
			
					// check if at least one section with one item
					List<Node> sectionItems = assessmentEl.selectNodes("section/item");
					if (!sectionItems.isEmpty()) {
						for (Node it : sectionItems) {
							List<Node> sv = it.selectNodes("resprocessing/outcomes/decvar[@varname='SCORE']");
							// the QTIv1.2 system relies on the SCORE variable of items
							if (sv.size() != 1) {
								validScore &= false;
							}
						}
					}
				}
			}
			
			eval.setValid(validType && validScore);
		}
				
		return eval;
	}
	
	private static class QTIFileFilter extends SimpleFileVisitor<Path> {
		private boolean qtiFile;

		@Override
		public FileVisitResult visitFile(Path file, BasicFileAttributes attrs)
		throws IOException {

			String filename = file.getFileName().toString();
			if(QTI_FILE.equals(filename)) {
				qtiFile = true;
			}
			return qtiFile ? FileVisitResult.TERMINATE : FileVisitResult.CONTINUE;
		}
		
		public boolean isValid() {
			return qtiFile;
		}
	}
}
