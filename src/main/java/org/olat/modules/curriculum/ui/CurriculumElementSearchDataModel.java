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
package org.olat.modules.curriculum.ui;

import org.olat.core.commons.persistence.SortKey;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiTableDataModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiSortableColumnDef;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SortableFlexiTableDataModel;

/**
 * 
 * Initial date: 10 Oct 2018<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class CurriculumElementSearchDataModel extends DefaultFlexiTableDataModel<CurriculumElementSearchRow>
implements SortableFlexiTableDataModel<CurriculumElementSearchRow> {
	
	public CurriculumElementSearchDataModel(FlexiTableColumnModel columnsModel) {
		super(columnsModel);
	}

	@Override
	public void sort(SortKey sortKey) {
		
	}

	@Override
	public Object getValueAt(int row, int col) {
		CurriculumElementSearchRow element = getObject(row);
		return getValueAt(element, col);
	}

	@Override
	public Object getValueAt(CurriculumElementSearchRow row, int col) {
		switch(SearchCols.values()[col]) {
			case key: return row.getKey();
			case curriculum: return row.getCurriculumDisplayName();
			case displayName: return row.getDisplayName();
			case identifier: return row.getIdentifier();
			case externalId: return row.getExternalID();
			case beginDate: return row.getBeginDate();
			case endDate: return row.getEndDate();
			case typeDisplayName: return row.getCurriculumElementTypeDisplayName();
			case resources: return row.getResourcesLink();
			case tools: return row.getToolsLink();
			default: return "ERROR";
		}
	}
	
	public enum SearchCols implements FlexiSortableColumnDef {
		key("table.header.key"),
		curriculum("table.header.curriculum"),
		displayName("table.header.curriculum.element.displayName"),
		identifier("table.header.curriculum.element.identifier"),
		externalId("table.header.external.id"),
		beginDate("table.header.begin.date"),
		endDate("table.header.end.date"),
		typeDisplayName("table.header.type"),
		resources("table.header.resources"),
		tools("table.header.tools");
		
		private final String i18nHeaderKey;
		
		private SearchCols(String i18nHeaderKey) {
			this.i18nHeaderKey = i18nHeaderKey;
		}

		@Override
		public boolean sortable() {
			return true;
		}

		@Override
		public String sortKey() {
			return name();
		}

		@Override
		public String i18nHeaderKey() {
			return i18nHeaderKey;
		}
	}
}
