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
package org.olat.modules.lecture.ui.export;

import java.util.Date;
import java.util.List;

import org.olat.core.gui.translator.Translator;
import org.olat.core.util.Formatter;
import org.olat.core.util.StringHelper;
import org.olat.core.util.openxml.OpenXMLWorksheet;
import org.olat.core.util.openxml.OpenXMLWorksheet.Row;
import org.olat.modules.lecture.LectureBlockAuditLog;
import org.olat.repository.RepositoryEntry;

/**
 * 
 * Initial date: 12 juil. 2017<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class RepositoryEntryAuditLogExport extends AbstractLectureBlockAuditLogExport {

	private RepositoryEntry entry;

	public RepositoryEntryAuditLogExport(RepositoryEntry entry, List<LectureBlockAuditLog> auditLog,
			boolean authorizedAbsenceEnabled, Translator translator) {
		super(label(entry), auditLog, authorizedAbsenceEnabled, translator);
		this.entry = entry;
		cacheRepositoryEntry(entry);
	}
	
	private static final String label(RepositoryEntry entry) {
		return StringHelper.transformDisplayNameToFileSystemName(entry.getDisplayname())
				+ "_" + Formatter.formatDatetimeFilesystemSave(new Date(System.currentTimeMillis()))
				+ ".xlsx";
	}

	@Override
	protected void addSheetSettings(OpenXMLWorksheet exportSheet) {
		exportSheet.setHeaderRows(2);
		super.addSheetSettings(exportSheet);
	}

	@Override
	protected void addHeaders(OpenXMLWorksheet exportSheet) {
		Row headerRow = exportSheet.newRow();
		
		int pos = 0;
		headerRow.addCell(pos++, translator.translate("export.header.entry", new String[] { entry.getDisplayname() }));
	}
}