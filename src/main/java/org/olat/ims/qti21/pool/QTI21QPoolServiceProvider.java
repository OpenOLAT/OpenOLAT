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
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.zip.ZipOutputStream;

import org.apache.logging.log4j.Logger;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.media.MediaResource;
import org.olat.core.helpers.Settings;
import org.olat.core.id.Identity;
import org.olat.core.logging.Tracing;
import org.olat.core.util.FileUtils;
import org.olat.core.util.StringHelper;
import org.olat.core.util.i18n.I18nModule;
import org.olat.core.util.vfs.LocalImpl;
import org.olat.core.util.vfs.VFSContainer;
import org.olat.core.util.vfs.VFSItem;
import org.olat.core.util.vfs.VFSLeaf;
import org.olat.core.util.vfs.VFSManager;
import org.olat.core.util.xml.XMLFactories;
import org.olat.fileresource.FileResourceManager;
import org.olat.fileresource.types.ImsQTI21Resource;
import org.olat.ims.qti21.QTI21Constants;
import org.olat.ims.qti21.QTI21Service;
import org.olat.ims.qti21.model.QTI21QuestionType;
import org.olat.ims.qti21.model.xml.AssessmentItemBuilder;
import org.olat.ims.qti21.model.xml.AssessmentItemBuilderFactory;
import org.olat.ims.qti21.model.xml.AssessmentItemMetadata;
import org.olat.ims.qti21.model.xml.ManifestBuilder;
import org.olat.ims.qti21.model.xml.ManifestMetadataBuilder;
import org.olat.ims.qti21.questionimport.AssessmentItemAndMetadata;
import org.olat.ims.resources.IMSEntityResolver;
import org.olat.imscp.xml.manifest.ResourceType;
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

import uk.ac.ed.ph.jqtiplus.node.item.AssessmentItem;
import uk.ac.ed.ph.jqtiplus.node.test.AssessmentItemRef;
import uk.ac.ed.ph.jqtiplus.resolution.ResolvedAssessmentItem;
import uk.ac.ed.ph.jqtiplus.resolution.ResolvedAssessmentTest;
import uk.ac.ed.ph.jqtiplus.resolution.RootNodeLookup;

/**
 * 
 * Initial date: 05.02.2016<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
@Service("qti21PoolServiceProvider")
public class QTI21QPoolServiceProvider implements QPoolSPI {
	
	private static final Logger log = Tracing.createLoggerFor(QTI21QPoolServiceProvider.class);
	
	public static final String QTI_12_OO_TEST = "OpenOLAT Test";

	@Autowired
	private QTI21Service qtiService;

	@Autowired
	private QPoolService qpoolService;
	@Autowired
	private QPoolFileStorage qpoolFileStorage;
	@Autowired
	private QuestionItemDAO questionItemDao;
	
	private static final List<ExportFormatOptions> formats = new ArrayList<>(4);
	static {
		formats.add(DefaultExportFormat.ZIP_EXPORT_FORMAT);
		formats.add(DefaultExportFormat.DOCX_EXPORT_FORMAT);
		formats.add(new DefaultExportFormat(QTI21Constants.QTI_21_FORMAT, Outcome.download, null));
		formats.add(new DefaultExportFormat(QTI21Constants.QTI_21_FORMAT, Outcome.repository, ImsQTI21Resource.TYPE_NAME));
	}
	
	
	public QTI21QPoolServiceProvider() {
		//
	}

	@Override
	public int getPriority() {
		return 20;
	}

	@Override
	public String getFormat() {
		return QTI21Constants.QTI_21_FORMAT;
	}

	@Override
	public List<ExportFormatOptions> getTestExportFormats() {
		return Collections.unmodifiableList(formats);
	}

	@Override
	public boolean isCompatible(String filename, File file) {
		return new AssessmentItemFileResourceValidator().validate(filename, file);
	}
	
	@Override
	public boolean isConversionPossible(QuestionItemShort item) {
		return false;
	}

	@Override
	public List<QItemFactory> getItemfactories() {
		List<QItemFactory> factories = new ArrayList<>();
		for(QTI21QuestionType type:QTI21QuestionType.values()) {
			if(type.hasEditor()) {
				factories.add(new QTI21AssessmentItemFactory(type));
			}
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
				if(leaf.getSize() <= 0l) {
					return "";
				}
				
				QTI21SAXHandler handler = new QTI21SAXHandler();
				try(InputStream is = leaf.getInputStream()) {
					XMLReader parser = XMLFactories.newSAXParser().getXMLReader();
					parser.setContentHandler(handler);
					parser.setEntityResolver(new IMSEntityResolver());
					parser.setFeature("http://xml.org/sax/features/validation", false);
					parser.parse(new InputSource(is));
				} catch (Exception e) {
					log.error("Cannot read the XML file of the question item: {}", leaf, e);
				}
				return handler.toString();
			}
		}
		return content;
	}

	@Override
	public List<QuestionItem> importItems(Identity owner, Locale defaultLocale, String filename, File file) {
		QTI21ImportProcessor processor = new QTI21ImportProcessor(owner, defaultLocale);
		return processor.process(file);
	}
	
	public List<QuestionItem> importRepositoryEntry(Identity owner, RepositoryEntry repositoryEntry, Locale defaultLocale) {
		
		FileResourceManager frm = FileResourceManager.getInstance();
		File unzippedDirRoot = frm.unzipFileResource(repositoryEntry.getOlatResource());
		ResolvedAssessmentTest resolvedAssessmentTest = qtiService.loadAndResolveAssessmentTest(unzippedDirRoot, false, true);
		ManifestBuilder clonedManifestBuilder = ManifestBuilder.read(new File(unzippedDirRoot, "imsmanifest.xml"));
		
		List<AssessmentItemRef> itemRefs = resolvedAssessmentTest.getAssessmentItemRefs();
		List<QuestionItem> importedItems = new ArrayList<>(itemRefs.size());
		
		for(AssessmentItemRef itemRef:itemRefs) {
			ResolvedAssessmentItem resolvedAssessmentItem = resolvedAssessmentTest.getResolvedAssessmentItem(itemRef);
			RootNodeLookup<AssessmentItem> rootNode = resolvedAssessmentItem.getItemLookup();
			
			URI itemUri = rootNode.getSystemId();
			File itemFile = new File(itemUri);
			String relativePathToManifest = unzippedDirRoot.toPath().relativize(itemFile.toPath()).toString();
			
			AssessmentItem assessmentItem = resolvedAssessmentItem.getRootNodeLookup().extractIfSuccessful();

			ResourceType resource = clonedManifestBuilder.getResourceTypeByHref(relativePathToManifest);
			ManifestMetadataBuilder metadata = clonedManifestBuilder.getMetadataBuilder(resource, true);
			
			QuestionItem qitem = importAssessmentItemRef(owner, assessmentItem, itemFile, metadata, defaultLocale);
			importedItems.add(qitem);
		}
		
		return importedItems;
	}

	@Override
	public MediaResource exportTest(List<QuestionItemShort> items, ExportFormatOptions format, Locale locale) {
		if(QTI21Constants.QTI_21_FORMAT.equals(format.getFormat())) {
			return new QTI21ExportTestResource("UTF-8", locale, items, this);
		} else if(DefaultExportFormat.DOCX_EXPORT_FORMAT.getFormat().equals(format.getFormat())) {
			return new QTI21PoolWordExport(items, I18nModule.getDefaultLocale(), "UTF-8", questionItemDao);
		}
		return null;
	}

	@Override
	public void exportItem(QuestionItemFull item, ZipOutputStream zout, Locale locale, Set<String> names) {
		QTI21ExportProcessor processor = new QTI21ExportProcessor(qtiService, qpoolFileStorage, locale);
		processor.process(item, zout);
	}

	@Override
	public void copyItem(QuestionItemFull original, QuestionItemFull copy) {
		VFSContainer originalDir = qpoolFileStorage.getContainer(original.getDirectory());
		VFSContainer copyDir = qpoolFileStorage.getContainer(copy.getDirectory());
		VFSManager.copyContent(originalDir, copyDir);
		
		File file = qpoolService.getRootFile(copy);
		File resourceDirectory = qpoolService.getRootDirectory(copy);
		URI assessmentItemUri = file.toURI();
		File itemFile = qpoolService.getRootFile(copy);
		
		ResolvedAssessmentItem resolvedAssessmentItem = qtiService
				.loadAndResolveAssessmentItem(assessmentItemUri, resourceDirectory);
		AssessmentItem assessmentItem = resolvedAssessmentItem.getRootNodeLookup().extractIfSuccessful();
		if(assessmentItem == null) {
			log.error("Question not readable: {} (key: {}, path: {})", original.getTitle(), original.getKey(), original.getDirectory());
		} else {
			assessmentItem.setTitle(copy.getTitle());
			qtiService.persistAssessmentObject(itemFile, assessmentItem);
		}
	}

	@Override
	public QuestionItem convert(Identity owner, QuestionItemShort itemToConvert, Locale locale) {
		return null;
	}

	@Override
	public Controller getPreviewController(UserRequest ureq, WindowControl wControl, QuestionItem item, boolean summary) {
		return new QTI21PreviewController(ureq, wControl, item);
	}

	@Override
	public boolean isTypeEditable() {
		return true;
	}

	@Override
	public QPoolItemEditorController getEditableController(UserRequest ureq, WindowControl wControl, QuestionItem qitem) {
		return new QTI21EditorController(ureq, wControl, qitem, false);
	}

	@Override
	public Controller getReadOnlyController(UserRequest ureq, WindowControl wControl, QuestionItem item) {
		return new QTI21EditorController(ureq, wControl, item, true);
	}

	public QuestionItem createItem(Identity identity, QTI21QuestionType type, String title, Locale locale) {
		AssessmentItemBuilder itemBuilder = AssessmentItemBuilderFactory.get(type, locale);

		AssessmentItem assessmentItem = itemBuilder.getAssessmentItem();
		assessmentItem.setLabel(title);
		assessmentItem.setTitle(title);
		
		AssessmentItemMetadata itemMetadata = new AssessmentItemMetadata();
		itemMetadata.setQuestionType(type);
		
		QTI21ImportProcessor processor = new QTI21ImportProcessor(identity, locale);
		QuestionItemImpl qitem = processor.processItem(assessmentItem, "", null, "OpenOLAT", Settings.getVersion(), itemMetadata);

		VFSContainer baseDir = qpoolFileStorage.getContainer(qitem.getDirectory());
		VFSLeaf leaf = baseDir.createChildLeaf(qitem.getRootFilename());
		File itemFile = ((LocalImpl)leaf).getBasefile();
		qtiService.persistAssessmentObject(itemFile, assessmentItem);
		
		//create imsmanifest
		ManifestBuilder manifest = ManifestBuilder.createAssessmentItemBuilder();
		manifest.appendAssessmentItem(itemFile.getName());	
		manifest.write(new File(itemFile.getParentFile(), "imsmanifest.xml"));
		return qitem;
	}
	
	public QuestionItemImpl importExcelItem(Identity owner, AssessmentItemAndMetadata itemAndMetadata, Locale defaultLocale) {
		QTI21ImportProcessor processor =  new QTI21ImportProcessor(owner, defaultLocale);
		
		String editor = itemAndMetadata.getEditor();
		String editorVersion = itemAndMetadata.getEditorVersion();
		AssessmentItemBuilder itemBuilder = itemAndMetadata.getItemBuilder();
		itemBuilder.build();
		AssessmentItem assessmentItem = itemBuilder.getAssessmentItem();
		QuestionItemImpl qitem = processor.processItem(assessmentItem, null, null,
				editor, editorVersion, itemAndMetadata);

		String originalItemFilename = qitem.getRootFilename();
		File itemStorage = qpoolFileStorage.getDirectory(qitem.getDirectory());
		File itemFile = new File(itemStorage, originalItemFilename);
		qtiService.persistAssessmentObject(itemFile, assessmentItem);

		//create manifest
		ManifestBuilder manifest = ManifestBuilder.createAssessmentItemBuilder();
		ResourceType resource = manifest.appendAssessmentItem(UUID.randomUUID().toString(), originalItemFilename);
		ManifestMetadataBuilder metadataBuilder = manifest.getMetadataBuilder(resource, true);
		itemAndMetadata.toBuilder(metadataBuilder, defaultLocale);
		manifest.write(new File(itemStorage, "imsmanifest.xml"));
		
		return qitem;
	}
	
	/**
	 * Very important, the ManifestMetadataBuilder will be changed, it need to be a clone
	 * 
	 * @param owner The future owner of the question
	 * @param assessmentItem The assessment item to convert
	 * @param itemFile The file where the assessment item is saved
	 * @param clonedMetadataBuilder The metadata builder need to be a clone!
	 * @param fUnzippedDirRoot The directory of the assessment item or the assessment test.
	 * @param defaultLocale The locale used by some translation
	 * @return
	 */
	public QuestionItem importAssessmentItemRef(Identity owner, AssessmentItem assessmentItem,
			File itemFile, ManifestMetadataBuilder clonedMetadataBuilder, Locale defaultLocale) {
		QTI21ImportProcessor processor =  new QTI21ImportProcessor(owner, defaultLocale);
		
		AssessmentItemMetadata metadata = new AssessmentItemMetadata(clonedMetadataBuilder);

		String editor = null;
		String editorVersion = null;
		if(StringHelper.containsNonWhitespace(assessmentItem.getToolName())) {
			editor = assessmentItem.getToolName();
		}
		if(StringHelper.containsNonWhitespace(assessmentItem.getToolVersion())) {
			editorVersion = assessmentItem.getToolVersion();
		}

		String originalItemFilename = itemFile.getName();
		QuestionItemImpl qitem = processor.processItem(assessmentItem, null, originalItemFilename,
				editor, editorVersion, metadata);
		
		//storage
		File itemStorage = qpoolFileStorage.getDirectory(qitem.getDirectory());
		FileUtils.copyDirContentsToDir(itemFile, itemStorage, false, "QTI21 import item xml in pool");
		
		//create manifest
		ManifestBuilder manifest = ManifestBuilder.createAssessmentItemBuilder();
		ResourceType resource = manifest.appendAssessmentItem(UUID.randomUUID().toString(), originalItemFilename);
		ManifestMetadataBuilder exportedMetadataBuilder = manifest.getMetadataBuilder(resource, true);
		exportedMetadataBuilder.setMetadata(clonedMetadataBuilder.getMetadata());
		manifest.write(new File(itemStorage, "imsmanifest.xml"));
		
		//process material
		File materialDirRoot = itemFile.getParentFile();
		List<String> materials = ImportExportHelper.getMaterials(assessmentItem);
		for(String material:materials) {
			if(material.indexOf("://") < 0) {// material can be an external URL
				try {
					File materialFile = new File(materialDirRoot, material);
					if(materialFile.isFile() && materialFile.exists()) {
						File itemMaterialFile = new File(itemStorage, material);
						if(!itemMaterialFile.getParentFile().exists()) {
							itemMaterialFile.getParentFile().mkdirs();
						}
						
						Files.copy(materialFile.toPath(), itemMaterialFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
					}
				} catch (IOException e) {
					log.error("", e);
				}
			}
		}
		
		return qitem;
	}
	
	public QuestionItemFull getFullQuestionItem(QuestionItemShort qitem) {
		return questionItemDao.loadById(qitem.getKey());
	}
	
	/**
	 * Export to QTI editor an item from the pool. The ident of the item
	 * is always regenerated as an UUID.
	 * @param qitem
	 * @param editorContainer
	 * @return
	 */
	public AssessmentItem exportToQTIEditor(QuestionItemFull qitem, Locale locale, File editorContainer) throws IOException {
		QTI21ExportProcessor processor = new QTI21ExportProcessor(qtiService, qpoolFileStorage, locale);
		ResolvedAssessmentItem resolvedAssessmentItem = processor.exportToQTIEditor(qitem, editorContainer);
		if(resolvedAssessmentItem != null) {
			AssessmentItem assessmentItem = resolvedAssessmentItem.getItemLookup().extractAssumingSuccessful();
			assessmentItem.setIdentifier(QTI21QuestionType.generateNewIdentifier(assessmentItem.getIdentifier()));
			return assessmentItem;
		}
		return null;
	}
	
	public void assembleTest(List<QuestionItemShort> items, Locale locale, ZipOutputStream zout) {
		List<QuestionItemFull> fullItems = loadQuestionFullItems(items);
		QTI21ExportProcessor processor = new QTI21ExportProcessor(qtiService, qpoolFileStorage, locale);
		processor.assembleTest(fullItems, zout);	
	}
	
	/**
	 * 
	 * @param testTitle The title of the test
	 * @param exportDir The directory to export to
	 * @param items The list of questions to export
	 * @param locale The language
	 */
	public void exportToEditorPackage(String testTitle, File exportDir, List<QuestionItemShort> items, boolean groupByTaxonomyLevel, Locale locale) {
		List<QuestionItemFull> fullItems = loadQuestionFullItems(items);
		QTI21ExportProcessor processor = new QTI21ExportProcessor(qtiService, qpoolFileStorage, locale);
		processor.assembleTest(testTitle, fullItems, groupByTaxonomyLevel, exportDir);
	}
	
	private List<QuestionItemFull> loadQuestionFullItems(List<QuestionItemShort> items) {
		List<Long> itemKeys = toKeys(items);
		List<QuestionItemFull> fullItems = questionItemDao.loadByIds(itemKeys);
		Map<Long, QuestionItemFull> fullItemMap = new HashMap<>();
		for(QuestionItemFull fullItem:fullItems) {
			fullItemMap.put(fullItem.getKey(), fullItem);
		}
		
		//reorder the full items
		List<QuestionItemFull> reorderedFullItems = new ArrayList<>(fullItems.size());
		for(QuestionItemShort item:items) {
			QuestionItemFull itemFull = fullItemMap.get(item.getKey());
			if(itemFull != null) {
				reorderedFullItems.add(itemFull);
			}
		}
		return reorderedFullItems;
	}
	
	private List<Long> toKeys(List<? extends QuestionItemShort> items) {
		List<Long> keys = new ArrayList<>(items.size());
		for(QuestionItemShort item:items) {
			keys.add(item.getKey());
		}
		return keys;
	}

}