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
package org.olat.ims.qti21.pool;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.servlet.http.HttpServletResponse;

import org.apache.logging.log4j.Logger;
import org.olat.core.CoreSpringFactory;
import org.olat.core.gui.media.MediaResource;
import org.olat.core.gui.translator.Translator;
import org.olat.core.logging.Tracing;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
import org.olat.core.util.openxml.OpenXMLDocument;
import org.olat.core.util.openxml.OpenXMLDocumentWriter;
import org.olat.core.util.vfs.VFSContainer;
import org.olat.ims.qti21.QTI21Constants;
import org.olat.ims.qti21.QTI21Service;
import org.olat.ims.qti21.manager.openxml.QTI21WordExport;
import org.olat.ims.qti21.model.xml.AssessmentHtmlBuilder;
import org.olat.ims.qti21.ui.AssessmentTestDisplayController;
import org.olat.ims.qti21.ui.editor.AssessmentTestComposerController;
import org.olat.modules.qpool.QPoolService;
import org.olat.modules.qpool.QuestionItemFull;
import org.olat.modules.qpool.QuestionItemShort;
import org.olat.modules.qpool.manager.QuestionItemDAO;

import uk.ac.ed.ph.jqtiplus.node.item.AssessmentItem;
import uk.ac.ed.ph.jqtiplus.resolution.ResolvedAssessmentItem;

/**
 * 
 * Initial date: 30 sept. 2016<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class QTI21PoolWordExport implements MediaResource {
	
	private static final Logger log = Tracing.createLoggerFor(QTI21PoolWordExport.class);
	
	private final Locale locale;
	private final String encoding;
	private final List<QuestionItemShort> items;
	private final QuestionItemDAO questionItemDao;
	private final QPoolService qpoolService;
	private final QTI21Service qtiService;
	
	public QTI21PoolWordExport(List<QuestionItemShort> items, Locale locale, String encoding, QuestionItemDAO questionItemDao) {
		this.encoding = encoding;
		this.locale = locale;
		this.items = items;
		this.questionItemDao = questionItemDao;
		qtiService = CoreSpringFactory.getImpl(QTI21Service.class);
		qpoolService = CoreSpringFactory.getImpl(QPoolService.class);
	}
	
	@Override
	public long getCacheControlDuration() {
		return 0;
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
	public void release() {
		//
	}

	@Override
	public void prepare(HttpServletResponse hres) {
		try {
			hres.setCharacterEncoding(encoding);
		} catch (Exception e) {
			log.error("", e);
		}

		try(ZipOutputStream zout = new ZipOutputStream(hres.getOutputStream());) {
			String label = "Items_Export";
			String secureLabel = StringHelper.transformDisplayNameToFileSystemName(label);
			
			List<Long> itemKeys = new ArrayList<>();
			for(QuestionItemShort item:items) {
				itemKeys.add(item.getKey());
			}

			List<QuestionItemFull> fullItems = questionItemDao.loadByIds(itemKeys);

			String file = secureLabel + ".zip";
			hres.setHeader("Content-Disposition","attachment; filename*=UTF-8''" + StringHelper.urlEncodeUTF8(file));			
			hres.setHeader("Content-Description", StringHelper.urlEncodeUTF8(label));
			
			zout.setLevel(9);

			ZipEntry test = new ZipEntry(secureLabel + ".docx");
			zout.putNextEntry(test);
			exportItems(fullItems, zout, false);
			zout.closeEntry();
			
			ZipEntry responses = new ZipEntry(secureLabel + "_responses.docx");
			zout.putNextEntry(responses);
			exportItems(fullItems, zout, true);
			zout.closeEntry();
		} catch (Exception e) {
			log.error("", e);
		}
	}
	
	private void exportItems(List<QuestionItemFull> fullItems, OutputStream out, boolean withResponses) {
		ZipOutputStream zout = null;
		try {
			OpenXMLDocument document = new OpenXMLDocument();
			document.setDocumentHeader("");
			
			Translator translator = Util.createPackageTranslator(AssessmentTestDisplayController.class, locale,
					Util.createPackageTranslator(AssessmentTestComposerController.class, locale));
			
			AssessmentHtmlBuilder htmlBuilder = new AssessmentHtmlBuilder();

			for(Iterator<QuestionItemFull> itemIt=fullItems.iterator(); itemIt.hasNext(); ) {
				QuestionItemFull fullItem = itemIt.next();
				if(QTI21Constants.QTI_21_FORMAT.equals(fullItem.getFormat())) {
					File resourceDirectory = qpoolService.getRootDirectory(fullItem);
					VFSContainer resourceContainer = qpoolService.getRootContainer(fullItem);
					document.setMediaContainer(resourceContainer);
					
					File resourceFile = qpoolService.getRootFile(fullItem);
					URI assessmentItemUri = resourceFile.toURI();
					
					ResolvedAssessmentItem resolvedAssessmentItem = qtiService
							.loadAndResolveAssessmentItem(assessmentItemUri, resourceDirectory);
					AssessmentItem item = resolvedAssessmentItem.getItemLookup().extractIfSuccessful();
					QTI21WordExport.renderAssessmentItem(item, resourceFile, resourceDirectory, document, withResponses, translator, htmlBuilder);
				}
				if(itemIt.hasNext()) {
					document.appendPageBreak();
				}
			}
			
			zout = new ZipOutputStream(out);
			zout.setLevel(9);
			
			OpenXMLDocumentWriter writer = new OpenXMLDocumentWriter();
			writer.createDocument(zout, document);
		} catch (Exception e) {
			log.error("", e);
		} finally {
			if(zout != null) {
				try {
					zout.finish();
				} catch (IOException e) {
					log.error("", e);
				}
			}
		}
	}
}
