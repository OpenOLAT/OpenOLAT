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

import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.zip.ZipOutputStream;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.id.Identity;
import org.olat.core.logging.OLog;
import org.olat.core.logging.Tracing;
import org.olat.core.util.FileUtils;
import org.olat.core.util.vfs.VFSContainer;
import org.olat.core.util.vfs.VFSItem;
import org.olat.core.util.vfs.VFSLeaf;
import org.olat.core.util.vfs.VFSManager;
import org.olat.ims.qti.QTI12EditorController;
import org.olat.ims.qti.QTI12PreviewController;
import org.olat.ims.qti.QTIConstants;
import org.olat.modules.qpool.QPoolSPI;
import org.olat.modules.qpool.QuestionItem;
import org.olat.modules.qpool.QuestionItemFull;
import org.olat.modules.qpool.QuestionItemShort;
import org.olat.modules.qpool.manager.FileStorage;
import org.olat.modules.qpool.manager.QEducationalContextDAO;
import org.olat.modules.qpool.manager.QItemTypeDAO;
import org.olat.modules.qpool.manager.QuestionItemDAO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLReaderFactory;

/**
 * 
 * Initial date: 21.02.2013<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
@Service("qtiPoolServiceProvider")
public class QTIQPoolServiceProvider implements QPoolSPI {
	
	private static final OLog log = Tracing.createLoggerFor(QTIQPoolServiceProvider.class);

	@Autowired
	private FileStorage qpoolFileStorage;
	@Autowired
	private QItemTypeDAO qItemTypeDao;
	@Autowired
	private QuestionItemDAO questionItemDao;
	@Autowired
	private QEducationalContextDAO qEduContextDao;
	
	public QTIQPoolServiceProvider() {
		//
	}

	@Override
	public int getPriority() {
		return 10;
	}

	@Override
	public String getFormat() {
		return QTIConstants.QTI_12_FORMAT;
	}

	@Override
	public boolean isCompatible(String filename, File file) {
		boolean ok = new ItemFileResourceValidator().validate(filename, file);
		return ok;
	}
	@Override
	public boolean isCompatible(String filename, VFSLeaf file) {
		boolean ok = new ItemFileResourceValidator().validate(filename, file);
		return ok;
	}

	@Override
	public String extractTextContent(QuestionItemFull item) {
		String content = null;
		if(item.getRootFilename() != null) {
			String dir = item.getDirectory();
			VFSContainer container = qpoolFileStorage.getContainer(dir);
			VFSItem file = container.resolve(item.getRootFilename());
			if(file instanceof VFSLeaf) {
				VFSLeaf leaf = (VFSLeaf)file;
				InputStream is = leaf.getInputStream();
				QTI12SAXHandler handler = new QTI12SAXHandler();
				try {
					XMLReader parser = XMLReaderFactory.createXMLReader();
					parser.setContentHandler(handler);
					parser.setFeature("http://xml.org/sax/features/validation", false);
					parser.parse(new InputSource(is));
				} catch (Exception e) {
					log.error("", e);
				} finally {
					FileUtils.closeSafely(is);
				}
				return handler.toString();
			}
		}
		return content;
	}

	@Override
	public List<QuestionItem> importItems(Identity owner, Locale defaultLocale, String filename, File file) {
		QTIImportProcessor processor = new QTIImportProcessor(owner, defaultLocale, filename, file,
				questionItemDao, qItemTypeDao, qEduContextDao, qpoolFileStorage);
		return processor.process();
	}

	@Override
	public void exportItem(QuestionItemFull item, ZipOutputStream zout) {
		QTIExportProcessor processor = new QTIExportProcessor(qpoolFileStorage);
		processor.process(item, zout);
	}
	
	public void assembleTest(List<QuestionItemShort> items) {
		List<Long> itemKeys = new ArrayList<Long>();
		for(QuestionItemShort item:items) {
			itemKeys.add(item.getKey());
		}

		List<QuestionItemFull> fullItems = questionItemDao.loadByIds(itemKeys);
		QTIExportProcessor processor = new QTIExportProcessor(qpoolFileStorage);
		processor.assembleTest(fullItems);	
	}

	@Override
	public void copyItem(QuestionItemFull original, QuestionItemFull copy) {
		VFSContainer originalDir = qpoolFileStorage.getContainer(original.getDirectory());
		VFSContainer copyDir = qpoolFileStorage.getContainer(copy.getDirectory());
		VFSManager.copyContent(originalDir, copyDir);
	}

	@Override
	public Controller getPreviewController(UserRequest ureq, WindowControl wControl, QuestionItem item) {
		QTI12PreviewController previewCtrl = new QTI12PreviewController(ureq, wControl, item);
		return previewCtrl;
	}

	@Override
	public boolean isTypeEditable() {
		return true;
	}

	@Override
	public Controller getEditableController(UserRequest ureq, WindowControl wControl, QuestionItem item) {
		QTI12EditorController previewCtrl = new QTI12EditorController(ureq, wControl, item);
		return previewCtrl;
	}
	

}