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
package org.olat.modules.qpool.impl;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.apache.logging.log4j.Logger;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.XMLWriter;
import org.olat.core.CoreSpringFactory;
import org.olat.core.commons.services.license.LicenseService;
import org.olat.core.commons.services.license.LicenseType;
import org.olat.core.commons.services.license.ResourceLicense;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.messages.MessageUIFactory;
import org.olat.core.gui.media.MediaResource;
import org.olat.core.gui.translator.Translator;
import org.olat.core.id.Identity;
import org.olat.core.logging.Tracing;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
import org.olat.core.util.ZipUtil;
import org.olat.core.util.vfs.VFSContainer;
import org.olat.core.util.vfs.VFSItem;
import org.olat.core.util.vfs.VFSManager;
import org.olat.core.util.vfs.filters.VFSSystemItemFilter;
import org.olat.course.nodes.iq.IQEditController;
import org.olat.modules.qpool.ExportFormatOptions;
import org.olat.modules.qpool.QItemFactory;
import org.olat.modules.qpool.QPoolItemEditorController;
import org.olat.modules.qpool.QPoolSPI;
import org.olat.modules.qpool.QuestionItem;
import org.olat.modules.qpool.QuestionItemFull;
import org.olat.modules.qpool.QuestionItemShort;
import org.olat.modules.qpool.QuestionStatus;
import org.olat.modules.qpool.manager.QPoolFileStorage;
import org.olat.modules.qpool.model.DefaultExportFormat;
import org.olat.modules.qpool.model.QEducationalContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Only allow to export the questions as ZIP.
 * 
 * Initial date: 21.02.2013<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
@Service("qtiPoolServiceProvider")
public class QTI12QPoolServiceProvider implements QPoolSPI {
	
	private static final Logger log = Tracing.createLoggerFor(QTI12QPoolServiceProvider.class);

	public static final String QTI_12_FORMAT = "IMS QTI 1.2";
	
	private static final List<ExportFormatOptions> formats = new ArrayList<>(2);
	static {
		formats.add(DefaultExportFormat.ZIP_EXPORT_FORMAT);
	}
	
	@Autowired
	private QPoolFileStorage qpoolFileStorage;
	
	public QTI12QPoolServiceProvider() {
		//
	}

	@Override
	public int getPriority() {
		return 1;
	}

	@Override
	public String getFormat() {
		return QTI_12_FORMAT;
	}

	@Override
	public List<ExportFormatOptions> getTestExportFormats() {
		return Collections.unmodifiableList(formats);
	}

	@Override
	public boolean isCompatible(String filename, File file) {
		return false;
	}
	
	@Override
	public boolean isConversionPossible(QuestionItemShort question) {
		return false;
	}

	@Override
	public List<QItemFactory> getItemfactories() {
		return Collections.emptyList();
	}

	@Override
	public String extractTextContent(QuestionItemFull item) {
		return null;
	}

	@Override
	public List<QuestionItem> importItems(Identity owner, Locale defaultLocale, String filename, File file) {
		return Collections.emptyList();
	}
	


	@Override
	public MediaResource exportTest(List<QuestionItemShort> items, ExportFormatOptions format, Locale locale) {
		return null;
	}

	@Override
	public void exportItem(QuestionItemFull fullItem, ZipOutputStream zout, Locale locale, Set<String> names) {
		String dir = fullItem.getDirectory();
		VFSContainer container = qpoolFileStorage.getContainer(dir);

		String rootDir = "qitem_" + fullItem.getKey();
		List<VFSItem> items = container.getItems();
		addMetadata(fullItem, rootDir, zout);
		for(VFSItem item:items) {
			ZipUtil.addToZip(item, rootDir, zout, new VFSSystemItemFilter(), false);
		}
	}
	
	private void addMetadata(QuestionItemFull fullItem, String dir, ZipOutputStream zout) {
		try {
			Document document = DocumentHelper.createDocument();
			Element qtimetadata = document.addElement("qtimetadata");
			QTIMetadataConverter enricher = new QTIMetadataConverter(qtimetadata);
			enricher.toXml(fullItem);
			zout.putNextEntry(new ZipEntry(dir + "/" + "qitem_" + fullItem.getKey() + "_metadata.xml"));
			OutputFormat format = OutputFormat.createPrettyPrint();
			XMLWriter writer = new XMLWriter(zout, format);
			writer.write(document);
		} catch (IOException e) {
			log.error("",  e);
		}
	}

	@Override
	public void copyItem(QuestionItemFull original, QuestionItemFull copy) {
		VFSContainer originalDir = qpoolFileStorage.getContainer(original.getDirectory());
		VFSContainer copyDir = qpoolFileStorage.getContainer(copy.getDirectory());
		VFSManager.copyContent(originalDir, copyDir);
	}

	@Override
	public QuestionItem convert(Identity identity, QuestionItemShort question, Locale locale) {
		return null;
	}

	@Override
	public Controller getPreviewController(UserRequest ureq, WindowControl wControl, QuestionItem item, boolean summary) {
		Translator trans = Util.createPackageTranslator(IQEditController.class, ureq.getLocale());
		return MessageUIFactory.createInfoMessage(ureq, wControl, "", trans.translate("error.qti12"));
	}

	@Override
	public boolean isTypeEditable() {
		return false;
	}

	@Override
	public QPoolItemEditorController getEditableController(UserRequest ureq, WindowControl wControl, QuestionItem item) {
		return null;
	}

	@Override
	public Controller getReadOnlyController(UserRequest ureq, WindowControl wControl, QuestionItem item) {
		return getPreviewController(ureq, wControl, item, false);
	}
	

	private class QTIMetadataConverter {
		
		private Element qtimetadata;
		
		QTIMetadataConverter(Element qtimetadata) {
			this.qtimetadata = qtimetadata;
		}
		
		protected void toXml(QuestionItemFull fullItem) {
			addMetadataField("additional_informations", fullItem.getAdditionalInformations(), qtimetadata);
			addMetadataField("oo_assessment_type", fullItem.getAssessmentType(), qtimetadata);
			addMetadataField("coverage", fullItem.getCoverage(), qtimetadata);
			addMetadataField("description", fullItem.getDescription(), qtimetadata);
			addMetadataField("oo_differentiation", fullItem.getDifferentiation(), qtimetadata);
			addMetadataField("qmd_levelofdifficulty", fullItem.getDifficulty(), qtimetadata);
			addMetadataField("qmd_toolvendor", fullItem.getEditor(), qtimetadata);
			addMetadataField("oo_toolvendor_version", fullItem.getEditorVersion(), qtimetadata);
			addMetadataField("oo_educational_context", fullItem.getEducationalContext(), qtimetadata);
			addMetadataField("oo_education_learning_time", fullItem.getEducationalLearningTime(), qtimetadata);
			addMetadataField("format", fullItem.getFormat(), qtimetadata);
			addMetadataField("oo_identifier", fullItem.getIdentifier(), qtimetadata);
			addMetadataField("type", fullItem.getItemType(), qtimetadata);
			addMetadataField("version", fullItem.getItemVersion(), qtimetadata);
			addMetadataField("keywords", fullItem.getKeywords(), qtimetadata);
			addMetadataField("language", fullItem.getLanguage(), qtimetadata);
			addLicenseMetadataField("license", fullItem, qtimetadata);
			addMetadataField("oo_master", fullItem.getMasterIdentifier(), qtimetadata);
			addMetadataField("oo_num_of_answer_alternatives", fullItem.getNumOfAnswerAlternatives(), qtimetadata);
			addMetadataField("status", fullItem.getQuestionStatus(), qtimetadata);
			addMetadataField("oo_std_dev_difficulty", fullItem.getStdevDifficulty(), qtimetadata);
			addMetadataField("oo_taxonomy", fullItem.getTaxonomicPath(), qtimetadata);
			addMetadataField("title", fullItem.getTitle(), qtimetadata);
			addMetadataField("oo_topic", fullItem.getTopic(), qtimetadata);
			addMetadataField("oo_usage", fullItem.getUsage(), qtimetadata);
			if(fullItem.getCorrectionTime() != null) {
				addMetadataField("oo_correction_time", fullItem.getCorrectionTime(), qtimetadata);
			}
		}
		
		private void addMetadataField(String label, int entry, Element metadata) {
			if(entry >=  0) {
				addMetadataField(label, Integer.toString(entry), metadata);
			}
		}
		
		private void addLicenseMetadataField(String label, QuestionItemFull fullItem, Element metadata) {
			LicenseService lService = CoreSpringFactory.getImpl(LicenseService.class);
			ResourceLicense license = lService.loadLicense(fullItem);
			if(license != null) {
				String licenseText = null;
				LicenseType licenseType = license.getLicenseType();
				if (lService.isFreetext(licenseType)) {
					licenseText = license.getFreetext();
				} else if (!lService.isNoLicense(licenseType)) {
					licenseText = license.getLicenseType().getName();
				}
				if (StringHelper.containsNonWhitespace(licenseText)) {
					addMetadataField(label, licenseText, metadata);
				}
			}
		}
		
		private void addMetadataField(String label, QEducationalContext entry, Element metadata) {
			if(entry != null) {
				addMetadataField(label, entry.getLevel(), metadata);
			}
		}
		
		private void addMetadataField(String label, QuestionStatus entry, Element metadata) {
			if(entry != null) {
				addMetadataField(label, entry.name(), metadata);
			}
		}
		
		private void addMetadataField(String label, BigDecimal entry, Element metadata) {
			if(entry != null) {
				addMetadataField(label, entry.toPlainString(), metadata);
			}
		}
		
		private void addMetadataField(String label, String entry, Element metadata) {
			if(entry != null) {
				Element qtimetadatafield = metadata.addElement("qtimetadatafield");
				qtimetadatafield.addElement("fieldlabel").setText(label);
				qtimetadatafield.addElement("fieldentry").setText(entry);
			}
		}
	}
}