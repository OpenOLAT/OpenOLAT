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
package org.olat.course.nodes.ms;

import java.io.IOException;
import java.io.OutputStream;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.apache.logging.log4j.Logger;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableComponent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.XlsFlexiTableExporter;
import org.olat.core.gui.translator.Translator;
import org.olat.core.logging.Tracing;
import org.olat.core.util.StringHelper;
import org.olat.core.util.ZipUtil;
import org.olat.core.util.io.ShieldOutputStream;
import org.olat.core.util.openxml.OpenXMLWorkbook;
import org.olat.core.util.openxml.OpenXMLWorksheet.Row;

/**
 * 
 * Initial date: Nov 3, 2025<br>
 * @author uhensler, urs.hensler@frentix.com, https://www.frentix.com
 *
 */
public class MSStatisticsExport extends XlsFlexiTableExporter {
	
	private static final Logger log = Tracing.createLoggerFor(MSStatisticsExport.class);

	@Override
	protected void createHeaderCell(FlexiColumnModel cd, Row headerRow, int col, Translator translator,
			OpenXMLWorkbook workbook) {
		String headerTooltip = cd.getHeaderTooltip();
		if (StringHelper.containsNonWhitespace(headerTooltip)) {
			headerRow.addCell(col, headerTooltip, workbook.getStyles().getHeaderStyle());
		} else {
			super.createHeaderCell(cd, headerRow, col, translator, workbook);
		}
	}
	
	public void export(ZipOutputStream out, String currentPath, FlexiTableComponent ftC, List<FlexiColumnModel> columns, Translator translator) throws IOException {
		String fileName = createFileName();
		String name = ZipUtil.concat(currentPath, fileName);
		try (OutputStream shieldOut = new ShieldOutputStream(out)) {
			out.putNextEntry(new ZipEntry(name));
			super.createWorkbook(shieldOut, ftC, columns, translator);
			out.closeEntry();
		} catch (IOException e) {
			log.error("", e);
		}
	}
	
}
