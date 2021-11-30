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
package org.olat.core.commons.services.pdf;

import java.io.File;
import java.io.OutputStream;

import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.creator.ControllerCreator;
import org.olat.core.gui.media.MediaResource;
import org.olat.core.id.Identity;

/**
 * 
 * Initial date: 6 févr. 2019<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public interface PdfService {
	
	/**
	 * Convert to PDF a file. The file need to have links to the
	 * CSS and Javascript files.
	 * 
	 * @param path The root directory
	 * @param rootFilename The root filename
	 * @param out The output stream where the PDF file is written
	 */
	public void convert(File path, String rootFilename, OutputStream out);
	
	/**
	 * Convert a controller to the a downloadable PDF.
	 * 
	 * @param filename The name of the PDF file (without .pdf extension)
	 * @param identity The user
	 * @param creator A factory to create the controller like in popup print window
	 * @param windowControl The window control of the user
	 * @return A downloadable PDF
	 */
	public MediaResource convert(String filename, Identity identity, ControllerCreator creator, WindowControl windowControl);
	
	/**
	 * 
	 * @param identity The identity
	 * @param creator A factory to create the controller like in popup print window
	 * @param windowControl The window control of the user
	 * @param out The output stream where the PDF file is written
	 */
	public void convert(Identity identity, ControllerCreator creator, WindowControl windowControl, OutputStream out);

}
