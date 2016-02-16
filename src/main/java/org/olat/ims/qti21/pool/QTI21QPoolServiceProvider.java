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
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.zip.ZipOutputStream;

import org.olat.core.commons.persistence.DB;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.media.MediaResource;
import org.olat.core.helpers.Settings;
import org.olat.core.id.Identity;
import org.olat.core.logging.OLog;
import org.olat.core.logging.Tracing;
import org.olat.core.util.vfs.LocalImpl;
import org.olat.core.util.vfs.VFSContainer;
import org.olat.core.util.vfs.VFSItem;
import org.olat.core.util.vfs.VFSLeaf;
import org.olat.core.util.vfs.VFSManager;
import org.olat.ims.qti.fileresource.TestFileResource;
import org.olat.ims.qti21.QTI21Constants;
import org.olat.ims.qti21.QTI21Service;
import org.olat.ims.qti21.model.QTI21QuestionType;
import org.olat.ims.qti21.model.QTI21QuestionTypeDetector;
import org.olat.ims.qti21.model.xml.AssessmentItemBuilder;
import org.olat.ims.qti21.model.xml.AssessmentItemMetadata;
import org.olat.ims.qti21.model.xml.ManifestPackage;
import org.olat.ims.qti21.model.xml.interactions.EssayAssessmentItemBuilder;
import org.olat.ims.qti21.model.xml.interactions.KPrimAssessmentItemBuilder;
import org.olat.ims.qti21.model.xml.interactions.MultipleChoiceAssessmentItemBuilder;
import org.olat.ims.qti21.model.xml.interactions.SingleChoiceAssessmentItemBuilder;
import org.olat.ims.resources.IMSEntityResolver;
import org.olat.imscp.xml.manifest.ManifestType;
import org.olat.modules.qpool.ExportFormatOptions;
import org.olat.modules.qpool.ExportFormatOptions.Outcome;
import org.olat.modules.qpool.QItemFactory;
import org.olat.modules.qpool.QPoolSPI;
import org.olat.modules.qpool.QuestionItem;
import org.olat.modules.qpool.QuestionItemFull;
import org.olat.modules.qpool.QuestionItemShort;
import org.olat.modules.qpool.manager.QEducationalContextDAO;
import org.olat.modules.qpool.manager.QItemTypeDAO;
import org.olat.modules.qpool.manager.QLicenseDAO;
import org.olat.modules.qpool.manager.QPoolFileStorage;
import org.olat.modules.qpool.manager.QuestionItemDAO;
import org.olat.modules.qpool.manager.TaxonomyLevelDAO;
import org.olat.modules.qpool.model.DefaultExportFormat;
import org.olat.modules.qpool.model.QuestionItemImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLReaderFactory;

import uk.ac.ed.ph.jqtiplus.node.item.AssessmentItem;
import uk.ac.ed.ph.jqtiplus.resolution.ResolvedAssessmentItem;

/**
 * 
 * Initial date: 05.02.2016<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
@Service("qti21PoolServiceProvider")
public class QTI21QPoolServiceProvider implements QPoolSPI {
	
	private static final OLog log = Tracing.createLoggerFor(QTI21QPoolServiceProvider.class);
	
	public static final String QTI_12_OO_TEST = "OpenOLAT Test";

	@Autowired
	private DB dbInstance;
	@Autowired
	private QTI21Service qtiService;
	
	@Autowired
	private QPoolFileStorage qpoolFileStorage;
	@Autowired
	private QLicenseDAO qLicenseDao;
	@Autowired
	private QItemTypeDAO qItemTypeDao;
	@Autowired
	private QuestionItemDAO questionItemDao;
	@Autowired
	private QEducationalContextDAO qEduContextDao;
	@Autowired
	private TaxonomyLevelDAO taxonomyLevelDao;
	
	private static final List<ExportFormatOptions> formats = new ArrayList<ExportFormatOptions>(4);
	static {
		formats.add(DefaultExportFormat.ZIP_EXPORT_FORMAT);
		formats.add(new DefaultExportFormat(QTI21Constants.QTI_21_FORMAT, Outcome.download, null));
		formats.add(new DefaultExportFormat(QTI21Constants.QTI_21_FORMAT, Outcome.repository, TestFileResource.TYPE_NAME));
	}
	
	
	public QTI21QPoolServiceProvider() {
		//
	}

	@Override
	public int getPriority() {
		return 10;
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
		boolean ok = new AssessmentItemFileResourceValidator().validate(filename, file);
		return ok;
	}
	@Override
	public boolean isCompatible(String filename, VFSLeaf file) {
		boolean ok = new AssessmentItemFileResourceValidator().validate(filename, file);
		return ok;
	}
	
	@Override
	public List<QItemFactory> getItemfactories() {
		List<QItemFactory> factories = new ArrayList<QItemFactory>();
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
				QTI21SAXHandler handler = new QTI21SAXHandler();
				try(InputStream is = leaf.getInputStream()) {
					XMLReader parser = XMLReaderFactory.createXMLReader();
					parser.setContentHandler(handler);
					parser.setEntityResolver(new IMSEntityResolver());
					parser.setFeature("http://xml.org/sax/features/validation", false);
					parser.parse(new InputSource(is));
				} catch (Exception e) {
					log.error("", e);
				}
				return handler.toString();
			}
		}
		return content;
	}

	@Override
	public List<QuestionItem> importItems(Identity owner, Locale defaultLocale, String filename, File file) {
		QTI21ImportProcessor processor = new QTI21ImportProcessor(owner, defaultLocale, filename, file,
				questionItemDao, qItemTypeDao, qEduContextDao, taxonomyLevelDao, qLicenseDao, qpoolFileStorage, dbInstance);
		return processor.process();
	}
	


	@Override
	public MediaResource exportTest(List<QuestionItemShort> items, ExportFormatOptions format) {
		if(QTI21Constants.QTI_21_FORMAT.equals(format.getFormat())) {
			return new QTI21ExportTestResource("UTF-8", items, this);
		}
		
		return null;
	}

	@Override
	public void exportItem(QuestionItemFull item, ZipOutputStream zout, Set<String> names) {
		QTI21ExportProcessor processor = new QTI21ExportProcessor(qtiService, qpoolFileStorage);
		processor.process(item, zout, names);
	}

	@Override
	public void copyItem(QuestionItemFull original, QuestionItemFull copy) {
		VFSContainer originalDir = qpoolFileStorage.getContainer(original.getDirectory());
		VFSContainer copyDir = qpoolFileStorage.getContainer(copy.getDirectory());
		VFSManager.copyContent(originalDir, copyDir);
	}

	@Override
	public Controller getPreviewController(UserRequest ureq, WindowControl wControl, QuestionItem item, boolean summary) {
		return new QTI21PreviewController(ureq, wControl, item, summary);
	}

	@Override
	public boolean isTypeEditable() {
		return true;
	}

	@Override
	public Controller getEditableController(UserRequest ureq, WindowControl wControl, QuestionItem qitem) {
		Controller editorCtrl = new QTI21EditorController(ureq, wControl, qitem);
		return editorCtrl;
	}

	public QuestionItem createItem(Identity identity, QTI21QuestionType type, String title, Locale locale) {
		AssessmentItemBuilder itemBuilder = null;
		switch(type) {
			case sc: itemBuilder = new SingleChoiceAssessmentItemBuilder(qtiService.qtiSerializer()); break;
			case mc: itemBuilder = new MultipleChoiceAssessmentItemBuilder(qtiService.qtiSerializer()); break;
			case kprim: itemBuilder = new KPrimAssessmentItemBuilder(qtiService.qtiSerializer()); break;
			//case fib: item = QTIEditHelper.createFIBItem(trans); break;
			case essay: itemBuilder = new EssayAssessmentItemBuilder(qtiService.qtiSerializer()); break;
			default: return null;
		}

		AssessmentItem assessmentItem = itemBuilder.getAssessmentItem();
		assessmentItem.setLabel(title);
		assessmentItem.setTitle(title);
		
		AssessmentItemMetadata itemMetadata = new AssessmentItemMetadata();
		itemMetadata.setQuestionType(type);
		
		QTI21ImportProcessor processor = new QTI21ImportProcessor(identity, locale, null, null,
				questionItemDao, qItemTypeDao, qEduContextDao, taxonomyLevelDao, qLicenseDao, qpoolFileStorage, dbInstance);
		QuestionItemImpl qitem = processor.processItem(assessmentItem, "", null, "OpenOLAT", Settings.getVersion(), itemMetadata);

		VFSContainer baseDir = qpoolFileStorage.getContainer(qitem.getDirectory());
		VFSLeaf leaf = baseDir.createChildLeaf(qitem.getRootFilename());
		File itemFile = ((LocalImpl)leaf).getBasefile();
		qtiService.persistAssessmentObject(itemFile, assessmentItem);
		
		//create imsmanifest
		ManifestType manifestType = ManifestPackage.createEmptyManifest();
        ManifestPackage.appendAssessmentItem(itemFile.getName(), manifestType);	
        ManifestPackage.write(manifestType, new File(itemFile.getParentFile(), "imsmanifest.xml"));
		
		return qitem;
	}
	
	/**
	 * Export to QTI editor an item from the pool. The ident of the item
	 * is always regenerated as an UUID.
	 * @param qitem
	 * @param editorContainer
	 * @return
	 */
	public AssessmentItem exportToQTIEditor(QuestionItemShort qitem, File editorContainer) throws IOException {
		QTI21ExportProcessor processor = new QTI21ExportProcessor(qtiService, qpoolFileStorage);
		QuestionItemFull fullItem = questionItemDao.loadById(qitem.getKey());
		ResolvedAssessmentItem resolvedAssessmentItem = processor.exportToQTIEditor(fullItem, editorContainer);
		AssessmentItem assessmentItem = resolvedAssessmentItem.getItemLookup().extractAssumingSuccessful();
		assessmentItem.setIdentifier(QTI21QuestionTypeDetector.generateNewIdentifier(assessmentItem.getIdentifier()));
		return assessmentItem;
	}
	

}