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
package org.olat.modules.curriculum.ui.copy;

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
 * Initial date: 19 févr. 2025<br>
 * @author srosse, stephane.rosse@frentix.com, https://www.frentix.com
 *
 */
public class CopyElementDetailsLectureBlocksTableModel extends DefaultFlexiTableDataModel<CopyElementDetailsLectureBlocksRow>
implements SortableFlexiTableDataModel<CopyElementDetailsLectureBlocksRow> {
	
	private static final CopyLectureBlockCols[] COLS = CopyLectureBlockCols.values();
	
	private final Locale locale;
	
	public CopyElementDetailsLectureBlocksTableModel(FlexiTableColumnModel columnModel, Locale locale) {
		super(columnModel);
		this.locale = locale;
	}

	@Override
	public void sort(SortKey orderBy) {
		if (orderBy != null) {
			List<CopyElementDetailsLectureBlocksRow> sorted = new SortableFlexiTableModelDelegate<>(orderBy, this, locale).sort();
			super.setObjects(sorted);
		}
	}

	@Override
	public Object getValueAt(int row, int col) {
		CopyElementDetailsLectureBlocksRow lecturesBlockRow = getObject(row);
		return getValueAt(lecturesBlockRow, col);
	}

	@Override
	public Object getValueAt(CopyElementDetailsLectureBlocksRow row, int col) {
		return switch(COLS[col]) {
			case activity -> row.getCopySetting();
			case key -> row.getKey();
			case title -> row.getTitle();
			case externalId -> row.getExternalId();
			case externalRef -> row.getExternalRef();
			case resource -> row.getRepositoryEntryDisplayName();
			case beginDate -> row.getBeginDate();
			default -> "ERROR";
		};
	}
	
	public enum CopyLectureBlockCols implements FlexiSortableColumnDef {
		activity("table.header.activity"),
		key("table.header.key"),
		title("table.header.title"),
		externalId("table.header.external.id"),
		externalRef("table.header.external.ref"),
		resource("table.header.resource"),
		beginDate("table.header.date");

		private final String i18nKey;

		private CopyLectureBlockCols(String i18nKey) {
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
