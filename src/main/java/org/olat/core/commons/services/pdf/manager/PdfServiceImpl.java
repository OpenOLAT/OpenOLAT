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

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.annotation.PreDestroy;

import org.apache.logging.log4j.Logger;
import org.olat.core.commons.fullWebApp.popup.BaseFullWebappPopupLayoutFactory;
import org.olat.core.commons.services.pdf.PdfControllerResource;
import org.olat.core.commons.services.pdf.PdfDocument;
import org.olat.core.commons.services.pdf.PdfModule;
import org.olat.core.commons.services.pdf.PdfService;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.creator.ControllerCreator;
import org.olat.core.gui.media.MediaResource;
import org.olat.core.id.Identity;
import org.olat.core.logging.Tracing;
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
	
	private static final Logger log = Tracing.createLoggerFor(PdfServiceImpl.class);
	
	@Autowired
	private PdfModule pdfModule;
	
	private static final ExecutorService executor = Executors.newFixedThreadPool(5);
	
	@PreDestroy
	public void shutdown() {
		try {
			executor.shutdownNow();
		} catch (Exception e) {
			log.error("", e);
		}
	}

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

	@Override
	public CompletionService<PdfDocument> borrowCompletionService() {
		return new ExecutorCompletionService<>(executor);
	}

	@Override
	public void asyncConvert(final String name, Identity identity, ControllerCreator creator, WindowControl windowControl,
			CompletionService<PdfDocument> service) {
		service.submit(() -> {
			try(ByteArrayOutputStream out = new ByteArrayOutputStream()) {
				convert(identity, creator, windowControl, out);
				return new PdfDocument(name, out.toByteArray());
			} catch(IOException e) {
				log.error("", e);
				return null;
			}
		});
	}
}
