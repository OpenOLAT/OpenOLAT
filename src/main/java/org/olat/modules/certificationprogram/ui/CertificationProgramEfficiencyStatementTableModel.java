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

import java.util.Locale;

import org.olat.core.commons.persistence.SortKey;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiTableDataModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiSortableColumnDef;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SortableFlexiTableDataModel;

/**
 * 
 * Initial date: 12 sept. 2025<br>
 * @author srosse, stephane.rosse@frentix.com, https://www.frentix.com
 *
 */
public class CertificationProgramEfficiencyStatementTableModel extends DefaultFlexiTableDataModel<CertificationProgramEfficiencyStatementRow>
implements SortableFlexiTableDataModel<CertificationProgramEfficiencyStatementRow>{
	
	private static final StatmentCols[] COLS = StatmentCols.values();
	
	private final Locale locale;
	
	public CertificationProgramEfficiencyStatementTableModel(FlexiTableColumnModel columnsModel, Locale locale) {
		super(columnsModel);
		this.locale = locale;
	}

	@Override
	public void sort(SortKey orderBy) {
		if(orderBy != null) {
			CertificationProgramEfficiencyStatementTableSortDelegate sort = new CertificationProgramEfficiencyStatementTableSortDelegate(orderBy, this, locale);
			super.setObjects(sort.sort());
		}
	}

	@Override
	public Object getValueAt(int row, int col) {
		CertificationProgramEfficiencyStatementRow statementRow = getObject(row);
		return getValueAt(statementRow, col);
	}

	@Override
	public Object getValueAt(CertificationProgramEfficiencyStatementRow statementRow, int col) {
		return switch(COLS[col]) {
			case key -> statementRow.getRepositoryEntryKey();
			case repositoryEntryDisplayName -> statementRow.getRepositoryEntryDisplayname();
			case repositoryEntryExternalRef -> statementRow.getRepositoryEntryExternalRef();
			case completion -> statementRow.getCurrentRunCompletion();
			case score -> statementRow.getScoreInfos();
			case passed -> statementRow.getPassed();
			case tools -> Boolean.TRUE;
			default -> "ERROR";
		};
	}
	
	public enum StatmentCols implements FlexiSortableColumnDef {
		key("table.header.id"),
		repositoryEntryDisplayName("table.header.displayname"),
		repositoryEntryExternalRef("table.header.external.ref"),
		completion("table.header.completion"),
		score("table.header.score"),
		passed("table.header.passed"),
		tools("action.more");
		
		private final String i18nKey;
		
		private StatmentCols(String i18nKey) {
			this.i18nKey = i18nKey;
		}
		
		@Override
		public String i18nHeaderKey() {
			return i18nKey;
		}

		@Override
		public boolean sortable() {
			return this != tools;
		}

		@Override
		public String sortKey() {
			return name();
		}
	}
}
