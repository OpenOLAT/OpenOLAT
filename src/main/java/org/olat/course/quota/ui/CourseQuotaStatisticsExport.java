/**
 * <a href="https://www.openolat.org">
 * OpenOLAT - Online Learning and Training</a><br>
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); <br>
 * you may not use this file except in compliance with the License.<br>
 * You may obtain a copy of the License at the
 * <a href="https://www.apache.org/licenses/LICENSE-2.0">Apache homepage</a>
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
package org.olat.course.quota.ui;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Date;
import java.util.List;

import org.apache.logging.log4j.Logger;
import org.olat.core.gui.translator.Translator;
import org.olat.core.logging.Tracing;
import org.olat.core.util.Formatter;
import org.olat.core.util.openxml.OpenXMLWorkbook;
import org.olat.core.util.openxml.OpenXMLWorkbookResource;
import org.olat.core.util.openxml.OpenXMLWorksheet;
import org.olat.core.util.openxml.OpenXMLWorksheet.Row;
import org.olat.course.nodes.CourseNodeFactory;

/**
 * Initial date: Aug 03, 2023
 *
 * @author skapoor, sumit.kapoor@frentix.com, <a href="https://www.frentix.com">https://www.frentix.com</a>
 */
public class CourseQuotaStatisticsExport extends OpenXMLWorkbookResource {

	private static final Logger log = Tracing.createLoggerFor(CourseQuotaStatisticsExport.class);
	private final Translator translator;
	private final List<CourseQuotaUsageRow> courseQuotaUsageRowList;


	public CourseQuotaStatisticsExport(List<CourseQuotaUsageRow> courseQuotaUsageRowList, Translator translator) {
		super(label());
		this.courseQuotaUsageRowList = courseQuotaUsageRowList;
		this.translator = translator;
	}

	private static String label() {
		return "CourseQuotaExport_" + Formatter.formatDatetimeFilesystemSave(new Date(System.currentTimeMillis()))
				+ ".xlsx";
	}

	@Override
	protected void generate(OutputStream out) {
		try (OpenXMLWorkbook workbook = new OpenXMLWorkbook(out, 1)) {
			OpenXMLWorksheet sheet = workbook.nextWorksheet();
			createHeader(sheet);
			createData(sheet);
		} catch (IOException e) {
			log.error("Unable to export xlsx.", e);
		}
	}

	private void createHeader(OpenXMLWorksheet worksheet) {
		Row headerRow = worksheet.newRow();
		int col = 0;

		headerRow.addCell(col++, translator.translate("table.header.course.quota.resource"));
		headerRow.addCell(col++, translator.translate("table.header.course.quota.type"));
		// headerRow.addCell(col++, translator.translate("table.header.course.quota.external"));
		headerRow.addCell(col++, translator.translate("table.header.course.quota.num.files"));
		headerRow.addCell(col++, translator.translate("table.header.course.quota.size"));
		headerRow.addCell(col++, translator.translate("table.header.course.quota.quota"));
		headerRow.addCell(col, translator.translate("table.header.course.quota.used"));
	}

	private void createData(OpenXMLWorksheet worksheet) {
		worksheet.setHeaderRows(1);

		for (CourseQuotaUsageRow row : courseQuotaUsageRowList) {
			Row dataRow = worksheet.newRow();
			int c = 0;

			String resource = row.getResource();
			String type = CourseNodeFactory.getInstance().getCourseNodeConfigurationEvenForDisabledBB(row.getType()).getLinkText(translator.getLocale());
			// boolean external = row.getExternal() != null;
			String numOfFiles = row.getNumOfFiles() != null ? row.getNumOfFiles().toString() : "";
			String size = row.getTotalUsedSize() != null ? Formatter.formatKBytes(row.getTotalUsedSize()) : "";
			String quota = row.getQuota();
			String usedQuotaProgress = row.getCurUsed() != null ? Math.round(row.getCurUsed().getActual()) + "%" : "";

			dataRow.addCell(c++, resource);
			dataRow.addCell(c++, type);
			// dataRow.addCell(c++, String.valueOf(external));
			dataRow.addCell(c++, numOfFiles);
			dataRow.addCell(c++, size);
			dataRow.addCell(c++, quota);
			dataRow.addCell(c, usedQuotaProgress);
		}
	}
}
