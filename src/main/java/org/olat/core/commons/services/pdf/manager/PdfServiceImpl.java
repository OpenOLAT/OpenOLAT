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
package org.olat.core.commons.services.pdf.manager;

import java.io.File;
import java.io.OutputStream;

import org.olat.core.commons.fullWebApp.popup.BaseFullWebappPopupLayoutFactory;
import org.olat.core.commons.services.pdf.PdfControllerResource;
import org.olat.core.commons.services.pdf.PdfModule;
import org.olat.core.commons.services.pdf.PdfService;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.creator.ControllerCreator;
import org.olat.core.gui.media.MediaResource;
import org.olat.core.id.Identity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 
 * Initial date: 6 févr. 2019<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
@Service
public class PdfServiceImpl implements PdfService {

	@Autowired
	private PdfModule pdfModule;

	@Override
	public void convert(File path, String rootFilename, OutputStream out) {
		pdfModule.getPdfServiceProvider().convert(path, rootFilename, out);
	}
	
	@Override
	public MediaResource convert(String filename, Identity identity, ControllerCreator creator, WindowControl wControl) {
		return new PdfControllerResource(filename, identity, creator, wControl);
	}

	@Override
	public void convert(Identity identity, ControllerCreator creator, WindowControl windowControl, OutputStream out) {
		ControllerCreator printCreator = BaseFullWebappPopupLayoutFactory.createPrintPopupLayout(creator);
		pdfModule.getPdfServiceProvider().convert(identity, printCreator, windowControl, out);
	}
}
