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
package org.olat.modules.qpool.ui;

import java.io.IOException;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import org.olat.core.CoreSpringFactory;
import org.olat.core.commons.services.license.License;
import org.olat.core.commons.services.license.LicenseModule;
import org.olat.core.commons.services.license.LicenseService;
import org.olat.core.gui.translator.Translator;
import org.apache.logging.log4j.Logger;
import org.olat.core.logging.Tracing;
import org.olat.core.util.Formatter;
import org.olat.core.util.StringHelper;
import org.olat.core.util.openxml.OpenXMLWorkbook;
import org.olat.core.util.openxml.OpenXMLWorkbookResource;
import org.olat.core.util.openxml.OpenXMLWorksheet;
import org.olat.core.util.openxml.OpenXMLWorksheet.Row;
import org.olat.modules.qpool.QPoolService;
import org.olat.modules.qpool.QuestionItem;
import org.olat.modules.qpool.QuestionItemAuditLog;
import org.olat.modules.qpool.QuestionItemShort;
import org.olat.modules.qpool.QuestionPoolModule;
import org.olat.modules.qpool.QuestionStatus;
import org.olat.modules.qpool.manager.QuestionPoolLicenseHandler;
import org.olat.modules.qpool.model.QEducationalContext;
import org.olat.modules.qpool.ui.metadata.MetaUIFactory;
import org.olat.modules.qpool.ui.metadata.MetaUIFactory.KeyValues;
import org.olat.user.UserManager;

/**
 * 
 * Initial date: 23.01.2018<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class QuestionItemAuditLogExport extends OpenXMLWorkbookResource {
	
	private static final Logger log = Tracing.createLoggerFor(QuestionItemAuditLogExport.class);
	
	private final List<QuestionItemAuditLog> auditLog;
	private final Translator translator;
	
	private final QPoolService qpoolService;
	private final QuestionPoolModule qpoolModule;
	private final UserManager userManager;
	private final LicenseService licenseService;
	private final LicenseModule licenseModule;
	private final QuestionPoolLicenseHandler licenseHandler;

	public QuestionItemAuditLogExport(QuestionItemShort item, List<QuestionItemAuditLog> auditLog,
			Translator translator) {
		super(label(item));
		this.auditLog = auditLog;
		this.translator = translator;
		qpoolService = CoreSpringFactory.getImpl(QPoolService.class);
		qpoolModule = CoreSpringFactory.getImpl(QuestionPoolModule.class);
		userManager = CoreSpringFactory.getImpl(UserManager.class);
		licenseService = CoreSpringFactory.getImpl(LicenseService.class);
		licenseModule = CoreSpringFactory.getImpl(LicenseModule.class);
		licenseHandler = CoreSpringFactory.getImpl(QuestionPoolLicenseHandler.class);
	}
	
	private static final String label(QuestionItemShort item) {
		return StringHelper.transformDisplayNameToFileSystemName(item.getTitle())
				+ "_" + Formatter.formatDatetimeFilesystemSave(new Date(System.currentTimeMillis()))
				+ ".xlsx";
	}

	@Override
	protected void generate(OutputStream out) {
		Collections.sort(auditLog, new QuestionItemAuditLogComparator());
		try(OpenXMLWorkbook workbook = new OpenXMLWorkbook(out, 1)) {
			OpenXMLWorksheet exportSheet = workbook.nextWorksheet();
			addSheetSettings(exportSheet);
			addHeader(exportSheet);
			addContent(exportSheet, workbook);
		} catch (IOException e) {
			log.error("", e);
		} catch (Exception e) {
			log.error("", e);
		}
	}
	
	private void addSheetSettings(OpenXMLWorksheet exportSheet) {
		exportSheet.setColumnWidth(1, 16);//width date time
		exportSheet.setColumnWidth(8, 16);//width date time
	}
	
	private void addHeader(OpenXMLWorksheet exportSheet) {
		exportSheet.setHeaderRows(2);
		
		Row headerRow = exportSheet.newRow();
		
		int pos = 0;
		headerRow.addCell(pos++, translator.translate("export.log.header.log.date"));
		headerRow.addCell(pos++, translator.translate("export.log.header.log.action"));
		headerRow.addCell(pos++, translator.translate("export.log.header.title"));
		headerRow.addCell(pos++, translator.translate("export.log.header.topic"));
		if (qpoolModule.isTaxonomyEnabled()) {
			headerRow.addCell(pos++, translator.translate("export.log.header.taxonomic.path"));
		}
		if (qpoolModule.isEducationalContextEnabled()) {
			headerRow.addCell(pos++, translator.translate("export.log.header.context"));
		}
		headerRow.addCell(pos++, translator.translate("export.log.header.keywords"));
		headerRow.addCell(pos++, translator.translate("export.log.header.additional.informations"));
		headerRow.addCell(pos++, translator.translate("export.log.header.coverage"));
		headerRow.addCell(pos++, translator.translate("export.log.header.language"));
		headerRow.addCell(pos++, translator.translate("export.log.header.assessment.type"));
		headerRow.addCell(pos++, translator.translate("export.log.header.item.type"));
		headerRow.addCell(pos++, translator.translate("export.log.header.learningTime"));
		headerRow.addCell(pos++, translator.translate("export.log.header.difficulty"));
		headerRow.addCell(pos++, translator.translate("export.log.header.stdevDifficulty"));
		headerRow.addCell(pos++, translator.translate("export.log.header.differentiation"));
		headerRow.addCell(pos++, translator.translate("export.log.header.numOfAnswerAlternatives"));
		headerRow.addCell(pos++, translator.translate("export.log.header.usage"));
		headerRow.addCell(pos++, translator.translate("export.log.header.version"));
		headerRow.addCell(pos++, translator.translate("export.log.header.status"));
		if (licenseModule.isEnabled(licenseHandler)) {
			headerRow.addCell(pos++, translator.translate("export.log.header.license"));
			headerRow.addCell(pos++, translator.translate("export.log.header.licensor"));
		}
		headerRow.addCell(pos++, translator.translate("export.log.header.log.author"));
	}

	@SuppressWarnings("deprecation")
	private void addContent(OpenXMLWorksheet exportSheet, OpenXMLWorkbook workbook) {
		for(QuestionItemAuditLog logEntry:auditLog) {
			int pos = 0;
			Row row = exportSheet.newRow();
			
			Date creationDate = logEntry.getCreationDate();
			row.addCell(pos++, creationDate, workbook.getStyles().getDateTimeStyle());
			row.addCell(pos++, logEntry.getAction());
			
			QuestionItem item = null;
			if (logEntry.getQuestionItemKey() != null) {
				item = qpoolService.toAuditQuestionItem(logEntry.getAfter());
			}
			if (item != null) {
				row.addCell(pos++, item.getTitle());
				row.addCell(pos++, item.getTopic());
				if (qpoolModule.isTaxonomyEnabled()) {
					row.addCell(pos++, item.getTaxonomicPath());
				}
				if (qpoolModule.isEducationalContextEnabled()) {
					row.addCell(pos++, getTranslatedContext(item.getEducationalContext()));
				}
				row.addCell(pos++, item.getKeywords());
				row.addCell(pos++, item.getAdditionalInformations());
				row.addCell(pos++, item.getCoverage());
				row.addCell(pos++, item.getLanguage());
				row.addCell(pos++, getTranslatedAssessmentType(item.getAssessmentType()));
				row.addCell(pos++, getTranslatedItemType(item.getItemType()));
				row.addCell(pos++, item.getEducationalLearningTime());
				row.addCell(pos++, format(item.getDifficulty()));
				row.addCell(pos++, format(item.getStdevDifficulty()));
				row.addCell(pos++, format(item.getDifferentiation()));
				row.addCell(pos++, String.valueOf(item.getNumOfAnswerAlternatives()));
				row.addCell(pos++, String.valueOf(item.getUsage()));
				row.addCell(pos++, item.getItemVersion());
				row.addCell(pos++, getTranslatedStatus(item.getQuestionStatus()));
			} else {
				pos += 16;
				if (qpoolModule.isTaxonomyEnabled()) {
					pos ++;
				}
				if (qpoolModule.isEducationalContextEnabled()) {
					pos++;
				}
			}
			
			if (licenseModule.isEnabled(licenseHandler)) {
				License license = licenseService.licenseFromXml(logEntry.getLicenseAfter());
				if (license != null) {
					row.addCell(pos++, license.getLicenseType() != null? license.getLicenseType().getName(): null);
					row.addCell(pos++, license.getLicensor());
				} else if (item != null) {
					// Backward compatibility:
					// Before the introduction of the LicenseService the license was stored in the item itself.
					row.addCell(pos++, item.getLicense() != null? item.getLicense().getLicenseKey(): null);
					row.addCell(pos++, item.getCreator());
				 } else {
					pos += 2;
				}
			}

			Long authorKey = logEntry.getAuthorKey();
			if(authorKey != null) {
				String fullname = userManager.getUserDisplayName(authorKey);
				row.addCell(pos++, fullname);
			}
		}
	}
	
	private String format(BigDecimal bigDecimal) {
		if (bigDecimal == null) return null;
		// remove 0 at the end of the string
		return String.valueOf(bigDecimal.doubleValue());
	}
	
	private String getTranslatedContext(QEducationalContext educationalContext) {
		if (educationalContext == null) return null;
		String translation = translator.translate("item.level." + educationalContext.getLevel().toLowerCase());
		if(translation.length() > 128) {
			translation = educationalContext.getLevel();
		}
		return translation;
	}

	private String getTranslatedAssessmentType(String assessmentType) {
		KeyValues assessmentTypes = MetaUIFactory.getAssessmentTypes(translator);
		for (int i = 0; i < assessmentTypes.getKeys().length; i++) {
			if (assessmentTypes.getKeys()[i].equals(assessmentType)) {
				return assessmentTypes.getValues()[i];
			}
		}
		return null;
	}

	private String getTranslatedItemType(String type) {
		if (!StringHelper.containsNonWhitespace(type)) return null;
		String translation = translator.translate("item.type." + type.toLowerCase());
		if(translation.length() > 128) {
			translation = type;
		}
		return translation;
	}

	private String getTranslatedStatus(QuestionStatus questionStatus) {
		if (questionStatus == null) return null;
		String translation = translator.translate("lifecycle.status." + questionStatus);
		if(translation.length() > 128) {
			translation = questionStatus.name();
		}
		return translation;
	}


	private class QuestionItemAuditLogComparator implements Comparator<QuestionItemAuditLog> {

		@Override
		public int compare(QuestionItemAuditLog o1, QuestionItemAuditLog o2) {
			if(o1 == null && o2 == null) return 0;
			if(o1 == null) return -1;
			if(o2 == null) return 1;
			
			Date d1 = o1.getCreationDate();
			Date d2 = o2.getCreationDate();
			if(d1 == null && d2 == null) return 0;
			if(d1 == null) return -1;
			if(d2 == null) return 1;
			return d1.compareTo(d2);
		}
	}

}
