/**
 * <a href="https://www.openolat.org">
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
 * frentix GmbH, https://www.frentix.com
 * <p>
 */
package org.olat.ims.qti21.ui.logviewer;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Date;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.apache.logging.log4j.Logger;
import org.olat.core.CoreSpringFactory;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableDataModelFactory;
import org.olat.core.gui.components.form.flexible.impl.elements.table.XlsFlexiTableExporter;
import org.olat.core.gui.translator.Translator;
import org.olat.core.logging.Tracing;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
import org.olat.core.util.io.ShieldOutputStream;
import org.olat.core.util.openxml.OpenXMLWorkbook;
import org.olat.core.util.openxml.OpenXMLWorksheet;
import org.olat.core.util.openxml.OpenXMLWorksheet.Row;
import org.olat.core.util.openxml.workbookstyle.CellStyle;
import org.olat.fileresource.FileResourceManager;
import org.olat.ims.qti21.AssessmentTestSession;
import org.olat.ims.qti21.QTI21Service;
import org.olat.ims.qti21.manager.LogViewerDeserializer;
import org.olat.ims.qti21.model.LogViewerEntry;
import org.olat.ims.qti21.model.LogViewerEntry.Answer;
import org.olat.ims.qti21.model.LogViewerEntry.Answers;
import org.olat.ims.qti21.ui.editor.AssessmentTestComposerController;
import org.olat.ims.qti21.ui.logviewer.LogViewerTableDataModel.LogEntryCols;
import org.springframework.beans.factory.annotation.Autowired;

import uk.ac.ed.ph.jqtiplus.resolution.ResolvedAssessmentTest;

/**
 * 
 * Initial date: 26 févr. 2024<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class LogExcelExport {
	
	private static final Logger log = Tracing.createLoggerFor(LogExcelExport.class);
	
	private final Translator translator;
	private final AssessmentTestSession testSession;
	
	public LogExcelExport(AssessmentTestSession testSession, Translator translator) {
		CoreSpringFactory.autowireObject(this);
		this.translator = Util.createPackageTranslator(LogViewerController.class, translator.getLocale(),
				Util.createPackageTranslator(AssessmentTestComposerController.class, translator.getLocale()));
		this.testSession = testSession;
	}
	
	@Autowired
	private QTI21Service qtiService;
	
	public void export(String filename,  ZipOutputStream exportStream) {
		try(OutputStream out = new ShieldOutputStream(exportStream)) {
			exportStream.putNextEntry(new ZipEntry(filename));
			exportWorkbook(testSession, out);
			exportStream.closeEntry();
		} catch (IOException e) {
			log.error("", e);
		}
	}
	
	public void exportWorkbook(AssessmentTestSession session, OutputStream out) {
		FileResourceManager frm = FileResourceManager.getInstance();
		final File fUnzippedDirRoot = frm.unzipFileResource(session.getTestEntry().getOlatResource());
		ResolvedAssessmentTest resolvedAssessmentTest = qtiService.loadAndResolveAssessmentTest(fUnzippedDirRoot, false, false);
		File logFile = qtiService.getAssessmentSessionAuditLogFile(session);
		LogViewerDeserializer deserializer = new LogViewerDeserializer(logFile, resolvedAssessmentTest, translator);
		List<LogViewerEntry> entries = deserializer.readEntries();

		try(OpenXMLWorkbook workbook = new OpenXMLWorkbook(out, 1)) {	
			//headers
			OpenXMLWorksheet exportSheet = workbook.nextWorksheet();
			exportSheet.setHeaderRows(2);
			writeHeaders(exportSheet, workbook);
			writeData(entries, exportSheet, workbook);
		} catch (Exception e) {
			log.error("", e);
		}
	}
	
	private void writeHeaders(OpenXMLWorksheet exportSheet, OpenXMLWorkbook workbook) {
		CellStyle headerStyle = workbook.getStyles().getHeaderStyle();
		//first header
		int col = 0;
		Row headerRow = exportSheet.newRow();
		headerRow.addCell(col++, translator.translate("table.header.date"), headerStyle);
		headerRow.addCell(col++, translator.translate("table.header.event"), headerStyle);
		headerRow.addCell(col++, translator.translate("table.header.item.title"), headerStyle);
		headerRow.addCell(col++, translator.translate("table.header.item.id"), headerStyle);
		headerRow.addCell(col++, translator.translate("table.header.interactions.types"), headerStyle);
		headerRow.addCell(col++, translator.translate("table.header.response"), headerStyle);
		headerRow.addCell(col++, translator.translate("table.header.response.ids"), headerStyle);
		headerRow.addCell(col++, translator.translate("table.header.min.max.score"), headerStyle);
		headerRow.addCell(col, translator.translate("table.header.score"), headerStyle);
	}

	private void writeData(List<LogViewerEntry> entries, OpenXMLWorksheet exportSheet, OpenXMLWorkbook workbook) {
		FlexiTableColumnModel columnsModel = FlexiTableDataModelFactory.createFlexiTableColumnModel();
		LogViewerTableDataModel model = new LogViewerTableDataModel(columnsModel, "", translator);
		model.setObjects(entries);

		for(LogViewerEntry entry:entries) {
			writeData(entry, model, exportSheet, workbook);
		}	
	}

	private void writeData(LogViewerEntry entry, LogViewerTableDataModel model, OpenXMLWorksheet exportSheet, OpenXMLWorkbook workbook) {
		int col = 0;
		Row row = exportSheet.newRow();
		
		Date date = entry.getDate();
		row.addCell(col++, date, workbook.getStyles().getDateTimeStyle());
		
		Object event = model.getValueAt(entry, LogEntryCols.event.ordinal());
		row.addCell(col++, (String)event);
		
		Object title = model.getValueAt(entry, LogEntryCols.itemTitle.ordinal());
		row.addCell(col++, (String)title);
		
		Object id = model.getValueAt(entry, LogEntryCols.itemId.ordinal());
		row.addCell(col++, (String)id);
		
		Object types = model.getValueAt(entry, LogEntryCols.interactionsTypes.ordinal());
		row.addCell(col++, (String)types);
		
		String response = getAnswers(entry.getAnswers(), false);
		row.addCell(col++, response);
		
		String responseIds = getAnswers(entry.getAnswers(), true);
		row.addCell(col++, responseIds);
		
		Object minMaxScore = model.getValueAt(entry, LogEntryCols.minMaxScore.ordinal());
		row.addCell(col++, (String)minMaxScore);
		
		Double score = entry.getScore();
		row.addCell(col, score, workbook.getStyles().getDoubleStyle());
	}
	
	private String getAnswers(Answers answers, boolean showIds) {
		if(answers == null || answers.answers() == null || answers.answers().isEmpty()) return null;
		
		StringBuilder sb = new StringBuilder(128);
		List<Answer> answerList = answers.answers();

		boolean append = false;		
		for(Answer answer:answerList) {
			List<String> list = showIds ? answer.ids() : answer.values();
			if(list != null) {
				for(int i=0; i<list.size(); i++) {
					String text = list.get(i);
					if(StringHelper.containsNonWhitespace(text)) {
						if(append) {
							sb.append(XlsFlexiTableExporter.LINE_BREAK_MARKER);
						} else {
							append = true;
						}
						text = text.replace("\\r", "").replace("\\n", XlsFlexiTableExporter.LINE_BREAK_MARKER);
						sb.append(text);
					}
				}
			}
		}
		return sb.toString();
	}
}
