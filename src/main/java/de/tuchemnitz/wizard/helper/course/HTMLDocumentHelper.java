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
* <p>
* Initial code contributed and copyrighted by<br>
* Technische Universitaet Chemnitz Lehrstuhl Technische Informatik<br>
* <br>
* Author Marcel Karras (toka@freebits.de)<br>
* Author Norbert Englisch (norbert.englisch@informatik.tu-chemnitz.de)<br>
* Author Sebastian Fritzsche (seb.fritzsche@googlemail.com)
*/

package de.tuchemnitz.wizard.helper.course;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import org.apache.logging.log4j.Logger;
import org.olat.core.logging.Tracing;
import org.olat.core.util.FileUtils;
import org.olat.core.util.StringHelper;
import org.olat.core.util.vfs.VFSContainer;
import org.olat.core.util.vfs.VFSLeaf;
import org.olat.course.ICourse;

public final class HTMLDocumentHelper {
	private static final Logger log = Tracing.createLoggerFor(HTMLDocumentHelper.class);
	public static final String ENCODING = "utf-8";

	/**
	 * Create a HTML file and put it into the course folder container.
	 * 
	 * @param course the corresponding course object
	 * @param relFilePath HTML file name
	 * @param htmlText the full html site content with head and body
	 * @return the created folder leaf
	 */
	public static final VFSLeaf createHtmlDocument(final ICourse course, final String relFilePath, final String htmlText) {
		// Create the HTML file inside the course base folder container
		
		VFSContainer parent = course.getCourseFolderContainer();
		VFSLeaf file = (VFSLeaf) parent.resolve(relFilePath);
		if (file == null) {
			// Expected: file does not exist, create it now. 
			String[] pathSegments = relFilePath.split("/");
			for (int i = 0; i < pathSegments.length; i++) {
				String segment = pathSegments[i];
				if (StringHelper.containsNonWhitespace(segment)) {
					if (i == pathSegments.length -1) {
						// last one is leaf
						file = parent.createChildLeaf(segment);											
					} else {
						parent = parent.createChildContainer(segment);
					}						
				}
			}
			try(OutputStream out = file.getOutputStream(false);
					BufferedOutputStream bos = new BufferedOutputStream(out)) {
				FileUtils.save(bos, htmlText, ENCODING);
			} catch (IOException e) {
				log.error("Error writing the HTML file::" + relFilePath, e);
			}
		} else {
			log.error("Can not create file::" + relFilePath + ", does already exist");
		}
		return file;
	}
}
