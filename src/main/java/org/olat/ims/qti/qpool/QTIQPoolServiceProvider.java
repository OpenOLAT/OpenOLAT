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
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.zip.ZipOutputStream;

import org.apache.logging.log4j.Logger;
import org.dom4j.Document;
import org.dom4j.Element;
import org.olat.core.commons.persistence.DB;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.media.MediaResource;
import org.olat.core.gui.translator.Translator;
import org.olat.core.helpers.Settings;
import org.olat.core.id.Identity;
import org.olat.core.id.OLATResourceable;
import org.olat.core.logging.Tracing;
import org.olat.core.util.FileUtils;
import org.olat.core.util.Util;
import org.olat.core.util.i18n.I18nModule;
import org.olat.core.util.vfs.VFSContainer;
import org.olat.core.util.vfs.VFSItem;
import org.olat.core.util.vfs.VFSLeaf;
import org.olat.core.util.vfs.VFSManager;
import org.olat.core.util.xml.XMLFactories;
import org.olat.fileresource.FileResourceManager;
import org.olat.ims.qti.QTI12EditorController;
import org.olat.ims.qti.QTI12PreviewController;
import org.olat.ims.qti.QTIConstants;
import org.olat.ims.qti.QTIModule;
import org.olat.ims.qti.editor.QTIEditHelper;
import org.olat.ims.qti.editor.QTIEditorMainController;
import org.olat.ims.qti.editor.QTIEditorPackageImpl;
import org.olat.ims.qti.editor.beecom.objects.Item;
import org.olat.ims.qti.editor.beecom.objects.Section;
import org.olat.ims.qti.editor.beecom.parser.ParserManager;
import org.olat.ims.qti.fileresource.TestFileResource;
import org.olat.ims.qti.qpool.QTI12ItemFactory.Type;
import org.olat.ims.qti.questionimport.ItemAndMetadata;
import org.olat.ims.qti21.pool.QTI12And21PoolWordExport;
import org.olat.ims.resources.IMSEntityResolver;
import org.olat.modules.qpool.ExportFormatOptions;
import org.olat.modules.qpool.ExportFormatOptions.Outcome;
import org.olat.modules.qpool.QItemFactory;
import org.olat.modules.qpool.QPoolItemEditorController;
import org.olat.modules.qpool.QPoolSPI;
import org.olat.modules.qpool.QPoolService;
import org.olat.modules.qpool.QuestionItem;
import org.olat.modules.qpool.QuestionItemFull;
import org.olat.modules.qpool.QuestionItemShort;
import org.olat.modules.qpool.manager.QPoolFileStorage;
import org.olat.modules.qpool.manager.QuestionItemDAO;
import org.olat.modules.qpool.model.DefaultExportFormat;
import org.olat.modules.qpool.model.QuestionItemImpl;
import org.olat.repository.RepositoryEntry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;

/**
 * 
 * Initial date: 21.02.2013<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
@Service("qtiPoolServiceProvider")
public class QTIQPoolServiceProvider implements QPoolSPI {
	
	private static final Logger log = Tracing.createLoggerFor(QTIQPoolServiceProvider.class);
	
	public static final String QTI_12_OO_TEST = "OpenOLAT Test";

	@Autowired
	private DB dbInstance;
	@Autowired
	private QPoolService qpoolService;
	@Autowired
	private QTIModule qtiModule;
	@Autowired
	private QPoolFileStorage qpoolFileStorage;
	@Autowired
	private QuestionItemDAO questionItemDao;
	
	private static final List<ExportFormatOptions> formats = new ArrayList<>(2);
	static {
		formats.add(DefaultExportFormat.ZIP_EXPORT_FORMAT);
		formats.add(DefaultExportFormat.DOCX_EXPORT_FORMAT);
		formats.add(new DefaultExportFormat(QTIConstants.QTI_12_FORMAT, Outcome.download, null));
		formats.add(new DefaultExportFormat(QTIConstants.QTI_12_FORMAT, Outcome.repository, TestFileResource.TYPE_NAME));
	}
	
	
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
	public List<ExportFormatOptions> getTestExportFormats() {
		return Collections.unmodifiableList(formats);
	}

	@Override
	public boolean isCompatible(String filename, File file) {
		return qtiModule.isCreateResourcesEnabled() && new ItemFileResourceValidator().validate(filename, file);
	}
	
	@Override
	public boolean isConversionPossible(QuestionItemShort question) {
		return false;
	}

	@Override
	public List<QItemFactory> getItemfactories() {
		List<QItemFactory> factories = new ArrayList<>();
		if(qtiModule.isCreateResourcesEnabled()) {
			factories.add(new QTI12ItemFactory(Type.sc));
			factories.add(new QTI12ItemFactory(Type.mc));
			factories.add(new QTI12ItemFactory(Type.kprim));
			factories.add(new QTI12ItemFactory(Type.fib));
			factories.add(new QTI12ItemFactory(Type.essay));
		}
		return factories;
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
					XMLReader parser = XMLFactories.newSAXParser().getXMLReader();
					parser.setContentHandler(handler);
					parser.setEntityResolver(new IMSEntityResolver());
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
		QTIImportProcessor processor = new QTIImportProcessor(owner, defaultLocale, filename, file);
		return processor.process();
	}
	
	public List<QuestionItem> importRepositoryEntry(Identity owner, RepositoryEntry repositoryEntry, Locale defaultLocale) {
		OLATResourceable ores = repositoryEntry.getOlatResource();
		FileResourceManager frm = FileResourceManager.getInstance();
		File testFile = frm.getFileResource(ores);
		List<QuestionItem> importedItem = importItems(owner, defaultLocale, testFile.getName(), testFile);
		if(importedItem != null && !importedItem.isEmpty()) {
			dbInstance.getCurrentEntityManager().flush();
		}
		return importedItem;
	}
	
	public QuestionItem createItem(Identity owner, QTI12ItemFactory.Type type, String title, Locale defaultLocale) {
		Translator trans = Util.createPackageTranslator(QTIEditorMainController.class, defaultLocale);
		Item item;
		switch(type) {
			case sc: item = QTIEditHelper.createSCItem(trans); break;
			case mc: item = QTIEditHelper.createMCItem(trans); break;
			case kprim: item = QTIEditHelper.createKPRIMItem(trans); break;
			case fib: item = QTIEditHelper.createFIBItem(trans); break;
			case essay: item = QTIEditHelper.createEssayItem(trans); break;
			default: return null;
		}
		item.setLabel(title);
		item.setTitle(title);
		
		QTIImportProcessor processor = new QTIImportProcessor(owner, defaultLocale);
		
		Document doc = QTIEditHelper.itemToXml(item);
		Element itemEl = (Element)doc.selectSingleNode("questestinterop/item");
		QuestionItemImpl qitem = processor.processItem(itemEl, "", null, "OpenOLAT", Settings.getVersion(), null, null);
		//save to file System
		VFSContainer baseDir = qpoolFileStorage.getContainer(qitem.getDirectory());
		VFSLeaf leaf = baseDir.createChildLeaf(qitem.getRootFilename());
		QTIEditHelper.serialiazeDoc(doc, leaf);
		return qitem;
	}
	
	public QuestionItemImpl importBeecomItem(Identity owner, ItemAndMetadata itemAndMetadata, VFSContainer sourceDir, Locale defaultLocale) {
		QTIImportProcessor processor = new QTIImportProcessor(owner, defaultLocale);
		
		String editor = null;
		String editorVersion = null;
		Item item = itemAndMetadata.getItem();
		if(!item.isAlient()) {
			editor = "OpenOLAT";
			editorVersion = Settings.getVersion();
		}
		
		Document doc = QTIEditHelper.itemToXml(item);
		Element itemEl = (Element)doc.selectSingleNode("questestinterop/item");
		QuestionItemImpl qitem = processor.processItem(itemEl, "", null, editor, editorVersion, null, itemAndMetadata);
		//save to file System
		VFSContainer baseDir = qpoolFileStorage.getContainer(qitem.getDirectory());
		VFSLeaf leaf = baseDir.createChildLeaf(qitem.getRootFilename());
		QTIEditHelper.serialiazeDoc(doc, leaf);
		//process materials
		
		if(sourceDir != null) {
			List<String> materials = processor.getMaterials(itemEl);
			//copy materials
			for(String material:materials) {
				VFSItem sourceItem = sourceDir.resolve(material);
				if(sourceItem instanceof VFSLeaf) {
					VFSLeaf targetItem = baseDir.createChildLeaf(material);
					VFSManager.copyContent((VFSLeaf)sourceItem, targetItem, false, null);
				}
			}
		}
		return qitem;
	}
	
	public List<QuestionItem> importBeecomItem(Identity owner, List<ItemAndMetadata> items, Locale defaultLocale) {
		int count = 0;
		List<QuestionItem> qItems = new ArrayList<>(items.size());
		for(ItemAndMetadata item:items) {
			QuestionItem qItem = importBeecomItem(owner, item, null, defaultLocale);
			qItems.add(qItem);
			if(++count % 10 == 0) {
				dbInstance.commitAndCloseSession();
			}
		}
		return qItems;
	}
	
	public void exportToEditorPackage(QTIEditorPackageImpl editorPackage, List<QuestionItemShort> items, boolean newTest) {
		VFSContainer editorContainer = editorPackage.getBaseDir();
		List<Long> itemKeys = toKeys(items);
		List<QuestionItemFull> fullItems = questionItemDao.loadByIds(itemKeys);
		
		
		Section section = editorPackage.getQTIDocument().getAssessment().getSections().get(0);
		if(newTest) {
			//remove autogenerated question
			section.getItems().clear();
		}

		QTIExportProcessor processor = new QTIExportProcessor(qpoolFileStorage);
		for(QuestionItemFull fullItem:fullItems) {
			Element itemEl = processor.exportToQTIEditor(fullItem, editorContainer);
			Item item = (Item)new ParserManager().parse(itemEl);
			item.setIdent(QTIEditHelper.generateNewIdent(item.getIdent()));
			section.getItems().add(item);
		}
	}
	
	private List<Long> toKeys(List<? extends QuestionItemShort> items) {
		List<Long> keys = new ArrayList<>(items.size());
		for(QuestionItemShort item:items) {
			keys.add(item.getKey());
		}
		return keys;
	}

	@Override
	public MediaResource exportTest(List<QuestionItemShort> items, ExportFormatOptions format, Locale locale) {
		if(QTIConstants.QTI_12_FORMAT.equals(format.getFormat())) {
			return new QTIExportTestResource("UTF-8", locale, items, this);
		} else if(DefaultExportFormat.DOCX_EXPORT_FORMAT.getFormat().equals(format.getFormat())) {
			return new QTI12And21PoolWordExport(items, I18nModule.getDefaultLocale(), "UTF-8", questionItemDao, qpoolFileStorage);
		}
		
		return null;
	}

	@Override
	public void exportItem(QuestionItemFull item, ZipOutputStream zout, Locale locale, Set<String> names) {
		QTIExportProcessor processor = new QTIExportProcessor(qpoolFileStorage);
		processor.process(item, zout, names);
	}
	
	public void assembleTest(List<QuestionItemShort> items, ZipOutputStream zout) {
		List<Long> itemKeys = new ArrayList<>();
		for(QuestionItemShort item:items) {
			itemKeys.add(item.getKey());
		}

		List<QuestionItemFull> fullItems = questionItemDao.loadByIds(itemKeys);
		QTIExportProcessor processor = new QTIExportProcessor(qpoolFileStorage);
		processor.assembleTest(fullItems, zout);	
	}
	
	/**
	 * Export to QTI editor an item from the pool. The ident of the item
	 * is always regenerated as an UUID.
	 * @param qitem
	 * @param editorContainer
	 * @return
	 */
	public Item exportToQTIEditor(QuestionItemShort qitem, VFSContainer editorContainer) {
		QTIExportProcessor processor = new QTIExportProcessor(qpoolFileStorage);
		QuestionItemFull fullItem = questionItemDao.loadById(qitem.getKey());
		Element itemEl = processor.exportToQTIEditor(fullItem, editorContainer);
		Item exportedItem = (Item)new ParserManager().parse(itemEl);
		exportedItem.setIdent(QTIEditHelper.generateNewIdent(exportedItem.getIdent()));
		return exportedItem;
	}

	@Override
	public void copyItem(QuestionItemFull original, QuestionItemFull copy) {
		VFSContainer originalDir = qpoolFileStorage.getContainer(original.getDirectory());
		VFSContainer copyDir = qpoolFileStorage.getContainer(copy.getDirectory());
		VFSManager.copyContent(originalDir, copyDir);
		
		VFSLeaf itemLeaf = qpoolService.getRootLeaf(copy);
		Item item = QTIEditHelper.readItemXml(itemLeaf);
		item.setTitle(copy.getTitle());
		QTIEditHelper.serialiazeItem(item, itemLeaf);
	}

	@Override
	public QuestionItem convert(Identity identity, QuestionItemShort question, Locale locale) {
		return null;
	}

	@Override
	public Controller getPreviewController(UserRequest ureq, WindowControl wControl, QuestionItem item, boolean summary) {
		return new QTI12PreviewController(ureq, wControl, item, summary);
	}

	@Override
	public boolean isTypeEditable() {
		return true;
	}

	@Override
	public QPoolItemEditorController getEditableController(UserRequest ureq, WindowControl wControl, QuestionItem item) {
		return new QTI12EditorController(ureq, wControl, item);
	}

	@Override
	public Controller getReadOnlyController(UserRequest ureq, WindowControl wControl, QuestionItem item) {
		return getPreviewController(ureq, wControl, item, false);
	}
	

}