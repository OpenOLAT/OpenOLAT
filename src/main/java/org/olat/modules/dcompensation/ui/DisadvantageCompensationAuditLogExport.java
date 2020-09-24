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
package org.olat.modules.dcompensation.ui;

import java.io.OutputStream;
import java.util.List;

import org.apache.logging.log4j.Logger;
import org.olat.core.CoreSpringFactory;
import org.olat.core.gui.translator.Translator;
import org.olat.core.logging.Tracing;
import org.olat.core.util.openxml.OpenXMLWorkbook;
import org.olat.core.util.openxml.OpenXMLWorkbookResource;
import org.olat.core.util.openxml.OpenXMLWorksheet;
import org.olat.core.util.openxml.OpenXMLWorksheet.Row;
import org.olat.modules.dcompensation.DisadvantageCompensationAuditLog;
import org.olat.user.UserManager;

/**
 * 
 * Initial date: 24 sept. 2020<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class DisadvantageCompensationAuditLogExport extends OpenXMLWorkbookResource {
	
	private static final Logger log = Tracing.createLoggerFor(DisadvantageCompensationAuditLogExport.class);
	
	private final Translator translator;
	private final List<DisadvantageCompensationAuditLog> auditLog;
	
	private final UserManager userManager;
	
	public DisadvantageCompensationAuditLogExport(String name, List<DisadvantageCompensationAuditLog> auditLog,
			Translator translator) {
		super(name);
		this.auditLog = auditLog;
		this.translator = translator;
		userManager = CoreSpringFactory.getImpl(UserManager.class);
	}

	@Override
	protected void generate(OutputStream out) {
		try(OpenXMLWorkbook workbook = new OpenXMLWorkbook(out, 1)) {
			OpenXMLWorksheet exportSheet = workbook.nextWorksheet();
			addSheetSettings(exportSheet);
			addHeader(exportSheet);
			addContent(exportSheet, workbook);
		} catch (Exception e) {
			log.error("", e);
		}
	}
	
	protected void addSheetSettings(OpenXMLWorksheet exportSheet) {
		exportSheet.setColumnWidth(1, 16);//width date time
	}
	
	private void addHeader(OpenXMLWorksheet exportSheet) {
		Row headerRow = exportSheet.newRow();
		
		int pos = 0;
		headerRow.addCell(pos++, translator.translate("table.header.creationdate"));//creationDate
		headerRow.addCell(pos++, translator.translate("table.header.log.action"));//action
		headerRow.addCell(pos++, translator.translate("table.header.before"));
		headerRow.addCell(pos++, translator.translate("table.header.after"));
		headerRow.addCell(pos, translator.translate("table.header.doer"));
	}
	
	private void addContent(OpenXMLWorksheet exportSheet, OpenXMLWorkbook workbook) {
		for(DisadvantageCompensationAuditLog logEntry:auditLog) {
			int pos = 0;
			Row row = exportSheet.newRow();
			row.addCell(pos++, logEntry.getCreationDate(), workbook.getStyles().getDateTimeStyle());
			row.addCell(pos++, logEntry.getAction());
			row.addCell(pos++, logEntry.getBefore());
			row.addCell(pos++, logEntry.getAfter());

			Long authorKey = logEntry.getAuthorKey();
			if(authorKey != null) {
				String fullname = userManager.getUserDisplayName(authorKey);
				row.addCell(pos, fullname);
			}
		}
	}
}
