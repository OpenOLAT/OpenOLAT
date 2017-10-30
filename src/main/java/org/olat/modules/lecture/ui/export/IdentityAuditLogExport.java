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
import org.olat.core.id.Identity;
import org.olat.core.util.Formatter;
import org.olat.core.util.StringHelper;
import org.olat.core.util.openxml.OpenXMLWorksheet;
import org.olat.core.util.openxml.OpenXMLWorksheet.Row;
import org.olat.modules.lecture.LectureBlockAuditLog;

/**
 * 
 * 
 * Initial date: 12 juil. 2017<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class IdentityAuditLogExport extends AbstractLectureBlockAuditLogExport {
	
	private Identity identity;

	public IdentityAuditLogExport(Identity identity, List<LectureBlockAuditLog> auditLog,
			boolean authorizedAbsenceEnabled, Translator translator) {
		super(label(identity), auditLog, authorizedAbsenceEnabled, translator);
		this.identity = identity;
	}

	private static final String label(Identity identity) {
		return StringHelper.transformDisplayNameToFileSystemName(identity.getUser().getLastName())
				+ "_" + StringHelper.transformDisplayNameToFileSystemName(identity.getUser().getFirstName())
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
		headerRow.addCell(pos++, userManager.getUserDisplayName(identity));
	}
}
