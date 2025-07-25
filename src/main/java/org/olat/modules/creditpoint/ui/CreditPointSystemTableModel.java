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
package org.olat.modules.creditpoint.ui;

import java.util.List;
import java.util.Locale;

import org.olat.core.commons.persistence.SortKey;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiTableDataModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiSortableColumnDef;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SortableFlexiTableDataModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SortableFlexiTableModelDelegate;

/**
 * 
 * Initial date: 3 juil. 2025<br>
 * @author srosse, stephane.rosse@frentix.com, https://www.frentix.com
 *
 */
public class CreditPointSystemTableModel extends DefaultFlexiTableDataModel<CreditPointSystemRow>
implements SortableFlexiTableDataModel<CreditPointSystemRow> {
	
	private static final SystemCols[] COLS = SystemCols.values();
	
	private final Locale locale;
	
	public CreditPointSystemTableModel(FlexiTableColumnModel columnModel, Locale locale) {
		super(columnModel);
		this.locale = locale;
	}

	@Override
	public void sort(SortKey orderBy) {
		if(orderBy != null) {
			List<CreditPointSystemRow> rows = new SortableFlexiTableModelDelegate<>(orderBy, this, locale).sort();
			super.setObjects(rows);
		}
	}
	
	@Override
	public Object getValueAt(int row, int col) {
		CreditPointSystemRow system = getObject(row);
		return getValueAt(system, col);
	}

	@Override
	public Object getValueAt(CreditPointSystemRow row, int col) {
		return switch(COLS[col]) {
			case id -> row.getKey();
			case name -> row.getName();
			case label -> row.getLabel();
			case expiration -> row.getExpiration();
			case usage -> Long.valueOf(row.getUsage());
			case status -> row.getStatus();
			case creationDate -> row.getCreationDate();
			case tools -> Boolean.TRUE;
			default -> "ERROR";
		};
	}
	
	public enum SystemCols implements FlexiSortableColumnDef {
		id("table.header.id"),
		name("table.header.name"),
		label("table.header.label"),
		expiration("table.header.validity"),
		usage("table.header.usage"),
		status("table.header.status"),
		creationDate("table.header.creation.date"),
		tools("action.more");
		
		private final String i18nKey;
		
		private SystemCols(String i18nKey) {
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
