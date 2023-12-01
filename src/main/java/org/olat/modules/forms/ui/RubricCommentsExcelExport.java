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
package org.olat.modules.forms.ui;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Date;

import org.apache.logging.log4j.Logger;
import org.olat.core.gui.media.MediaResource;
import org.olat.core.gui.translator.Translator;
import org.olat.core.logging.Tracing;
import org.olat.core.util.Formatter;
import org.olat.core.util.StringHelper;
import org.olat.core.util.openxml.OpenXMLWorkbook;
import org.olat.core.util.openxml.OpenXMLWorkbookResource;
import org.olat.core.util.openxml.OpenXMLWorksheet;
import org.olat.core.util.openxml.OpenXMLWorksheet.Row;
import org.olat.modules.forms.EvaluationFormResponse;
import org.olat.modules.forms.EvaluationFormSession;
import org.olat.modules.forms.handler.RubricHandler;
import org.olat.modules.forms.model.jpa.EvaluationFormResponses;
import org.olat.modules.forms.model.xml.Rubric;
import org.olat.modules.forms.model.xml.Slider;
import org.olat.modules.forms.ui.ReportHelper.Legend;

/**
 * 
 * Initial date: 23. Nov 2023<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class RubricCommentsExcelExport {
	
	private static final Logger log = Tracing.createLoggerFor(RubricCommentsExcelExport.class);

	private final Rubric rubric;
	private final ReportHelper reportHelper;
	private EvaluationFormResponses responses;
	private final Translator translator;
	private final String fileName;

	public RubricCommentsExcelExport(Rubric rubric, EvaluationFormResponses responses, ReportHelper reportHelper,
			Translator translator) {
		this.rubric = rubric;
		this.responses = responses;
		this.reportHelper = reportHelper;
		this.translator = translator;
		this.fileName = getFileName(rubric);
	}
	
	private String getFileName(Rubric rubric) {
		String name = rubric.getName();
		if (StringHelper.containsNonWhitespace(name)) {
			name += "_" + translator.translate("rubric.report.comments");
		} else {
			name = translator.translate("rubric.report.comments");
		}
		return new StringBuilder()
				.append(StringHelper.transformDisplayNameToFileSystemName(name))
				.append("_")
				.append(Formatter.formatDatetimeFilesystemSave(new Date(System.currentTimeMillis())))
				.append(".xlsx")
				.toString();
	}

	public MediaResource createMediaResource() {
		return new OpenXMLWorkbookResource(fileName) {
			@Override
			protected void generate(OutputStream out) {
				createWorkbook(out);
			}
		};
	}
	
	private void createWorkbook(OutputStream out) {
		try(OpenXMLWorkbook workbook = new OpenXMLWorkbook(out, 1)) {
			OpenXMLWorksheet exportSheet = workbook.nextWorksheet();
			addHeader(exportSheet);
			addContent(workbook, exportSheet);
		} catch (IOException e) {
			log.error("", e);
		} catch (Exception e) {
			log.error("", e);
		}
	}
	
	private void addHeader(OpenXMLWorksheet exportSheet) {
		exportSheet.setHeaderRows(1);
		
		Row headerRow = exportSheet.newRow();
		
		int col = 0;
		headerRow.addCell(col++, translator.translate("rubric.report.comments.slider"));
		headerRow.addCell(col++, translator.translate("report.excel.legend"));
		headerRow.addCell(col++, translator.translate("rubric.report.comments.comment"));
	}
	
	private void addContent(OpenXMLWorkbook workbook, OpenXMLWorksheet exportSheet) {
		ArrayList<EvaluationFormSession> sessions = new ArrayList<>(responses.getSessions());
		sessions.sort(reportHelper.getComparator());
		
		for (Slider slider : rubric.getSliders()) {
			String sliderLabel = EvaluationFormFormatter.formatSliderLabel(slider);
			String responseIdentifier = RubricHandler.getSliderCommentId(slider);
			for (EvaluationFormSession session : sessions) {
				EvaluationFormResponse response = responses.getResponse(session, responseIdentifier);
				if (response != null) {
					Row row = exportSheet.newRow();
					int col = 0;
					
					row.addCell(col++, sliderLabel, workbook.getStyles().getTopAlignStyle());
					
					Legend legend = reportHelper.getLegend(session);
					row.addCell(col++, legend.getName(), workbook.getStyles().getTopAlignStyle());
					
					String text = response.getStringuifiedResponse();
					if (StringHelper.containsNonWhitespace(text)) {
						text = text.replaceAll("\n", "");
						row.addCell(col++, text, workbook.getStyles().getTopAlignStyle());
					}
				}
			}
		}
	}

}
