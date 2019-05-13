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
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Iterator;
import java.util.List;

import org.dom4j.Attribute;
import org.dom4j.Document;
import org.dom4j.Element;
import org.apache.logging.log4j.Logger;
import org.olat.core.logging.Tracing;
import org.olat.core.util.PathUtils;
import org.olat.core.util.vfs.LocalFileImpl;
import org.olat.core.util.vfs.LocalFolderImpl;
import org.olat.core.util.vfs.VFSContainer;
import org.olat.core.util.vfs.VFSItem;
import org.olat.fileresource.types.FileResource;
import org.olat.fileresource.types.ResourceEvaluation;
import org.olat.ims.qti.process.AssessmentInstance;
import org.olat.ims.qti.process.QTIHelper;

/**
 * Initial Date:  Apr 6, 2004
 *
 * @author Mike Stock
 */
public class SurveyFileResource extends FileResource {
	
	private static final Logger log = Tracing.createLoggerFor(SurveyFileResource.class);
	private static final String QTI_FILE = "qti.xml";
	
	/**
	 * IMS QTI Survey file resource identifier.
	 */
	public static final String TYPE_NAME = "FileResource.SURVEY";

	public SurveyFileResource() {
		super(TYPE_NAME);
	}
	
	/**
	 * @param unzippedDir
	 * @return True if is of type.
	 */
	public static boolean validate(File unzippedDir) {
		VFSContainer vfsUnzippedRoot = new LocalFolderImpl(unzippedDir);
		VFSItem vfsQTI = vfsUnzippedRoot.resolve("qti.xml");
		//getDocument(..) ensures that InputStream is closed in every case.
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
			List assessment = doc.selectNodes("questestinterop/assessment");
			if(assessment.size() == 1) {
				Object assessmentObj = assessment.get(0);
				if(assessmentObj instanceof Element) {
					Element assessmentEl = (Element)assessmentObj;
					Attribute title = assessmentEl.attribute("title");
					if(title != null) {
						eval.setDisplayname(title.getValue());
					}
					
					List metas = assessmentEl.selectNodes("qtimetadata/qtimetadatafield");
					for (Iterator iter = metas.iterator(); iter.hasNext();) {
						Element el_metafield = (Element) iter.next();
						Element el_label = (Element) el_metafield.selectSingleNode("fieldlabel");
						String label = el_label.getText();
						if (label.equals(AssessmentInstance.QMD_LABEL_TYPE)) { // type meta
							Element el_entry = (Element) el_metafield.selectSingleNode("fieldentry");
							String entry = el_entry.getText();
							if(entry.equals(AssessmentInstance.QMD_ENTRY_TYPE_SURVEY)) {
								eval.setValid(true);
							}
						}
					}
				}
			}
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
