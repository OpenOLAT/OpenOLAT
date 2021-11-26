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
package org.olat.ims.qti21.ui.editor.testsexport;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.util.concurrent.CompletionService;
import java.util.concurrent.Future;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;
import org.apache.logging.log4j.Logger;
import org.olat.core.CoreSpringFactory;
import org.olat.core.commons.services.pdf.PdfDocument;
import org.olat.core.commons.services.pdf.PdfService;
import org.olat.core.dispatcher.mapper.Mapper;
import org.olat.core.dispatcher.mapper.MapperService;
import org.olat.core.dispatcher.mapper.manager.MapperKey;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.creator.ControllerCreator;
import org.olat.core.gui.media.MediaResource;
import org.olat.core.gui.media.ServletUtil;
import org.olat.core.gui.util.SyntheticUserRequest;
import org.olat.core.id.Identity;
import org.olat.core.logging.Tracing;
import org.olat.core.util.StringHelper;
import org.olat.core.util.UserSession;
import org.olat.ims.qti21.QTI21Service;
import org.olat.ims.qti21.ui.ResourcesMapper;
import org.springframework.beans.factory.annotation.Autowired;

import uk.ac.ed.ph.jqtiplus.running.TestSessionController;

/**
 * 
 * Initial date: 9 nov. 2021<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class QTI21OfflineTestsPDFMediaResource implements MediaResource {
	
	private static final Logger log = Tracing.createLoggerFor(QTI21OfflineTestsPDFMediaResource.class);
	
	private final String label;
	private final Identity identity;
	private final WindowControl windowControl;
	private final TestsExportContext exportContext;
	
	@Autowired
	private PdfService pdfService;
	@Autowired
	private QTI21Service qtiService;
	@Autowired
	private MapperService mapperService;
	
	public QTI21OfflineTestsPDFMediaResource(Identity identity, WindowControl windowControl,
			TestsExportContext exportContext, String label) {
		this.label = label;
		this.identity = identity;
		this.windowControl = windowControl;
		this.exportContext = exportContext;
		CoreSpringFactory.autowireObject(this);
	}

	@Override
	public boolean acceptRanges() {
		return false;
	}

	@Override
	public String getContentType() {
		return "application/zip";
	}

	@Override
	public Long getSize() {
		return null;
	}

	@Override
	public InputStream getInputStream() {
		return null;
	}

	@Override
	public Long getLastModified() {
		return null;
	}

	@Override
	public long getCacheControlDuration() {
		return ServletUtil.CACHE_NO_CACHE;
	}

	@Override
	public void release() {
		//
	}

	@Override
	public void prepare(HttpServletResponse hres) {
		try {
			hres.setCharacterEncoding("UTF-8");
		} catch (Exception e) {
			log.error("", e);
		}

		String secureLabel = StringHelper.transformDisplayNameToFileSystemName(label);

		String file = secureLabel + ".zip";
		hres.setHeader("Content-Disposition", "attachment; filename*=UTF-8''" + StringHelper.urlEncodeUTF8(file));			
		hres.setHeader("Content-Description", StringHelper.urlEncodeUTF8(label));

		final File fUnzippedDirRoot = exportContext.getUnzippedDirRoot();
		final URI assessmentObjectUri = qtiService.createAssessmentTestUri(fUnzippedDirRoot);
		final Mapper mapper = new ResourcesMapper(assessmentObjectUri, fUnzippedDirRoot, (File)null);
		
		final int numOfTests = exportContext.getNumOfTests();
		CompletionService<PdfDocument> completion = pdfService.borrowCompletionService();
		for(int i=1; i<=numOfTests; i++) {
			try {
				TestSessionController testSessionController = exportContext.createTestSessionState();
				String serialNumber = exportContext.getSerialNumber(i);
				String testName = "tests/" + serialNumber + ".pdf";

				OfflineContentCreator test = new OfflineContentCreator(mapper, fUnzippedDirRoot, testSessionController, serialNumber, false);
				pdfService.asyncConvert(testName, identity, test, windowControl, completion);
				
				String solutionName = "solutions/" + serialNumber + "_solutions.pdf";
				OfflineContentCreator solution = new OfflineContentCreator(mapper, fUnzippedDirRoot, testSessionController, serialNumber, true);
				pdfService.asyncConvert(solutionName, identity, solution, windowControl, completion);
			} catch(Exception e) {
				log.error("Cannot show results", e);
			}
		}
		
		int count = (2 * numOfTests);
		try(OutputStream out = hres.getOutputStream();
				ZipOutputStream zout = new ZipOutputStream(hres.getOutputStream())) {
			while (count>0) {
				Future<PdfDocument> resultFuture = completion.take();
				count--;
				PdfDocument pdf = resultFuture.get();
				String filename = pdf.getName();
				ZipEntry entry = new ZipEntry(filename);
				zout.putNextEntry(entry);
				IOUtils.copy(new ByteArrayInputStream(pdf.getContent()), zout);
				zout.closeEntry();
			}
		} catch(Exception e) {
			log.error("", e);
		} finally {
			exhaustCompletion(count, completion);
		}
	}
	
	private void exhaustCompletion(int count, CompletionService<PdfDocument> completion) {
		for (; count-->0; ) {
			try {
				completion.take();
			} catch (Exception e) {
				log.error("", e);
			}
		}
	}
	
	private class OfflineContentCreator implements ControllerCreator {
		
		private final boolean solution;
		private final String serialNumber;
		
		private final Mapper mapper;
		private final File fUnzippedDirRoot;
		private final TestSessionController testSessionController;
		
		public OfflineContentCreator(Mapper mapper, File fUnzippedDirRoot,
				TestSessionController testSessionController, String serialNumber, boolean solution) {
			this.mapper = mapper;
			this.solution = solution;
			this.serialNumber = serialNumber;
			this.testSessionController = testSessionController;
			this.fUnzippedDirRoot = fUnzippedDirRoot;
		}

		@Override
		public Controller createController(UserRequest ureq, WindowControl wControl) {
			UserSession usess = ureq.getUserSession();
			MapperKey mapperBaseKey = mapperService.register(usess, "QTI21DetailsResources::", mapper, 3000);
			String mapperUriForPdf = mapperBaseKey.getUrl();
			ureq = new SyntheticUserRequest(ureq.getIdentity(), exportContext.getLocale(), ureq.getUserSession());
			return new QTI21OfflineTestsPDFController(ureq, wControl, fUnzippedDirRoot, mapperUriForPdf,
					exportContext, testSessionController, serialNumber, solution);
		}
	}
}
