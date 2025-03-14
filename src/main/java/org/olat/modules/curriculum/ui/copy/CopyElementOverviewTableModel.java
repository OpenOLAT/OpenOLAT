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

import org.olat.core.gui.components.form.flexible.elements.FlexiTableFilter;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiTreeTableDataModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiSortableColumnDef;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;

/**
 * 
 * Initial date: 18 févr. 2025<br>
 * @author srosse, stephane.rosse@frentix.com, https://www.frentix.com
 *
 */
public class CopyElementOverviewTableModel extends DefaultFlexiTreeTableDataModel<CopyElementRow> {
	
	private static final CopyElementCols[] COLS = CopyElementCols.values();
	
	public CopyElementOverviewTableModel(FlexiTableColumnModel columnsModel) {
		super(columnsModel);
	}

	@Override
	public void filter(String searchString, List<FlexiTableFilter> filters) {
		//
	}
	
	@Override
	public boolean hasChildren(int row) {
		CopyElementRow elementRow = getObject(row);
		return elementRow != null && elementRow.isHasChildren();
	}

	@Override
	public Object getValueAt(int row, int col) {
		CopyElementRow elementRow = getObject(row);
		return switch(COLS[col]) {
			case key -> elementRow.getKey();
			case displayName -> elementRow.getDisplayName();
			case identifier -> elementRow.getIdentifier();
			case beginDate -> elementRow.getBeginDateEl();
			case endDate -> elementRow.getEndDateEl();
			case type -> elementRow.getTypeDisplayName();
			case numOfResources -> elementRow.getNumOfResources();
			case numOfTemplates -> elementRow.getNumOfTemplates();
			case numOfLectureBlocks -> elementRow.getNumOfLectureBlocks();
			default -> "ERROR";
		};
	}
	
	public enum CopyElementCols implements FlexiSortableColumnDef {
		key("table.header.key"),
		displayName("table.header.title"),
		identifier("table.header.external.ref"),
		beginDate("table.header.begin.date"),
		endDate("table.header.end.date"),
		type("table.header.type"),
		numOfResources("table.header.num.of.courses"),
		numOfTemplates("table.header.num.of.templates"),
		numOfLectureBlocks("table.header.num.of.lecture.blocks");
		
		private final String i18nKey;
		
		private CopyElementCols(String i18nKey) {
			this.i18nKey = i18nKey;
		}
		
		@Override
		public String i18nHeaderKey() {
			return i18nKey;
		}

		@Override
		public boolean sortable() {
			return false;
		}

		@Override
		public String sortKey() {
			return name();
		}
	}
}
