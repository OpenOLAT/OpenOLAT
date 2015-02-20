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
package org.olat.ims.qti.qpool;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;
import org.olat.core.gui.media.MediaResource;
import org.olat.core.gui.translator.Translator;
import org.olat.core.logging.OLog;
import org.olat.core.logging.Tracing;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
import org.olat.core.util.openxml.OpenXMLDocument;
import org.olat.core.util.openxml.OpenXMLDocumentWriter;
import org.olat.core.util.vfs.VFSContainer;
import org.olat.core.util.vfs.VFSItem;
import org.olat.core.util.vfs.VFSLeaf;
import org.olat.ims.qti.editor.QTIEditHelper;
import org.olat.ims.qti.editor.beecom.objects.Item;
import org.olat.ims.qti.export.QTIWordExport;
import org.olat.modules.qpool.QuestionItemFull;
import org.olat.modules.qpool.QuestionItemShort;
import org.olat.modules.qpool.manager.QPoolFileStorage;
import org.olat.modules.qpool.manager.QuestionItemDAO;

/**
 * 
 * Initial date: 13.09.2013<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
class QTIPoolWordExport implements MediaResource {
	
	private final static OLog log = Tracing.createLoggerFor(QTIPoolWordExport.class);
	
	private final Locale locale;
	private final String encoding;
	private final List<QuestionItemShort> items;
	private final QuestionItemDAO questionItemDao;
	private final QPoolFileStorage qpoolFileStorage;
	
	public QTIPoolWordExport(List<QuestionItemShort> items, Locale locale, String encoding,
			QuestionItemDAO questionItemDao, QPoolFileStorage qpoolFileStorage) {
		this.encoding = encoding;
		this.locale = locale;
		this.items = items;
		this.questionItemDao = questionItemDao;
		this.qpoolFileStorage = qpoolFileStorage;
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

		ZipOutputStream zout = null;
		try {
			String label = "Items_Export";
			String secureLabel = StringHelper.transformDisplayNameToFileSystemName(label);
			
			List<Long> itemKeys = new ArrayList<Long>();
			for(QuestionItemShort item:items) {
				itemKeys.add(item.getKey());
			}

			List<QuestionItemFull> fullItems = questionItemDao.loadByIds(itemKeys);

			String file = secureLabel + ".zip";
			hres.setHeader("Content-Disposition","attachment; filename*=UTF-8''" + StringHelper.urlEncodeUTF8(file));			
			hres.setHeader("Content-Description", StringHelper.urlEncodeUTF8(label));
			
			zout = new ZipOutputStream(hres.getOutputStream());
			zout.setLevel(9);

			ZipEntry test = new ZipEntry(secureLabel + ".docx");
			zout.putNextEntry(test);
			exportTest(fullItems, zout, false);
			zout.closeEntry();
			
			ZipEntry responses = new ZipEntry(secureLabel + "_responses.docx");
			zout.putNextEntry(responses);
			exportTest(fullItems, zout, true);
			zout.closeEntry();
		} catch (Exception e) {
			log.error("", e);
		} finally {
			IOUtils.closeQuietly(zout);
		}
	}
	
	private void exportTest(List<QuestionItemFull> fullItems, OutputStream out, boolean withResponses) {
		ZipOutputStream zout = null;
		try {
			OpenXMLDocument document = new OpenXMLDocument();
			document.setDocumentHeader("");
			Translator translator = Util.createPackageTranslator(QTIWordExport.class, locale);

			for(Iterator<QuestionItemFull> itemIt=fullItems.iterator(); itemIt.hasNext(); ) {
				QuestionItemFull fullItem = itemIt.next();
				
				String dir = fullItem.getDirectory();
				VFSContainer container = qpoolFileStorage.getContainer(dir);
				document.setMediaContainer(container);
				
				VFSItem rootItem = container.resolve(fullItem.getRootFilename());
				Item item = QTIEditHelper.readItemXml((VFSLeaf)rootItem);
				if(item.isAlient()) {
					QTIWordExport.renderAlienItem(item, document, translator);
				} else {
					QTIWordExport.renderItem(item, document, withResponses, translator);
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
