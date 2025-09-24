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
package org.olat.modules.certificationprogram.ui;

import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiTableDataModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiSortableColumnDef;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;

/**
 * 
 * Initial date: 11 sept. 2025<br>
 * @author srosse, stephane.rosse@frentix.com, https://www.frentix.com
 *
 */
public class CertificationProgramRecertificationTableModel extends DefaultFlexiTableDataModel<CertificationProgramRecertificationRow> {
	
	private static final RecertificationCols[] COLS = RecertificationCols.values();
	
	public CertificationProgramRecertificationTableModel(FlexiTableColumnModel columnsModel) {
		super(columnsModel);
	}

	@Override
	public Object getValueAt(int row, int col) {
		CertificationProgramRecertificationRow recertificationRow = getObject(row);
		return switch(COLS[col]) {
			case key -> recertificationRow.getCertificateKey();
			case recertificationCount -> recertificationRow.getRecertificationCount();
			case issuedOn -> recertificationRow.getCertificationDate();
			case status -> recertificationRow.getCertificationStatus();
			case certificate -> recertificationRow.getCertificate();
			case nextRecertificationDate -> recertificationRow.getNextRecertificationDate();
			case nextRecertificationDays -> recertificationRow.getNextRecertification();
			case recertificationDeadline -> recertificationRow.getRecertificationWindowDate();
			case tools -> Boolean.TRUE;
			default -> "ERROR";
		};
	}

	public enum RecertificationCols implements FlexiSortableColumnDef {
		key("table.header.id"),
		recertificationCount("table.header.recertification.count"),
		certificate("table.header.certificate"),
		issuedOn("table.header.issued.on"),
		status("table.header.status"),
		nextRecertificationDate("table.header.valid.until"),
		nextRecertificationDays("table.header.next.recertification.days"),
		recertificationDeadline("table.header.recertification.deadline"),
		tools("action.more");
		
		private final String i18nKey;
		
		private RecertificationCols(String i18nKey) {
			this.i18nKey = i18nKey;
		}
		
		@Override
		public String i18nHeaderKey() {
			return i18nKey;
		}

		@Override
		public boolean sortable() {
			return true;
		}

		@Override
		public String sortKey() {
			return name();
		}
	}
}
