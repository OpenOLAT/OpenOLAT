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
package org.olat.modules.selectus.ui.report;

import java.io.OutputStream;
import java.util.Date;
import java.util.List;

import org.apache.logging.log4j.Logger;
import org.olat.core.gui.translator.Translator;
import org.olat.core.id.Identity;
import org.olat.core.logging.Tracing;
import org.olat.core.util.Formatter;
import org.olat.core.util.openxml.OpenXMLWorkbook;
import org.olat.core.util.openxml.OpenXMLWorksheet;
import org.olat.core.util.openxml.OpenXMLWorksheet.Row;
import org.springframework.stereotype.Service;

import org.olat.modules.selectus.model.Position;

/**
 * 
 * Initial date: 8 nov. 2023<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
@Service("reportGeneratorBasic")
public class BasicReport implements ReportGenerator {

	private static final Logger log = Tracing.createLoggerFor(BasicReport.class);
	
	@Override
	public String getFilename(List<Position> positions, Identity identity, Translator translator) {
		String date = Formatter.formatShortDateFilesystem(new Date());
		return "Basic_report_" + date;
	}

	@Override
	public void generateReport(List<Position> positions, Identity identity, Translator translator, OutputStream out) {
		try (OpenXMLWorkbook workbook = new OpenXMLWorkbook(out, 1)) {
			OpenXMLWorksheet sheet = workbook.nextWorksheet();
			generateHeaders(sheet);

		} catch (Exception e) {
			log.error("Unable to generate report", e);
		}
	}
	
	protected void generateHeaders(OpenXMLWorksheet sheet) {
		Row headerRow = sheet.newRow();
		int col = 0;
		
		// course
		headerRow.addCell(col++, "Dummy header");
	}

}
