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

import java.util.List;

import org.olat.core.commons.persistence.SortKey;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiTableDataModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiSortableColumnDef;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SortableFlexiTableDataModel;

/**
 * 
 * Initial date: 11 mai 2018<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class CurriculumElementTypesTableModel extends DefaultFlexiTableDataModel<CurriculumElementTypeRow>
implements SortableFlexiTableDataModel<CurriculumElementTypeRow> {
	
	public CurriculumElementTypesTableModel(FlexiTableColumnModel columnsModel) {
		super(columnsModel);
	}
	
	@Override
	public void sort(SortKey orderBy) {
		if(orderBy != null) {
			List<CurriculumElementTypeRow> views = new CurriculumElementTypesTableModelSortDelegate(orderBy, this, null).sort();
			super.setObjects(views);
		}
	}

	@Override
	public Object getValueAt(int row, int col) {
		CurriculumElementTypeRow type = getObject(row);
		return getValueAt(type, col);
	}
	
	@Override
	public Object getValueAt(CurriculumElementTypeRow row, int col) {
		switch(TypesCols.values()[col]) {
			case key: return row.getKey();
			case identifier: return row.getIdentifier();
			case displayName: return row.getDisplayName();
			case externalId: return row.getExternalId();
			case tools: return row.getToolsLink();
			default: return null;
		}
	}
	
	public enum TypesCols implements FlexiSortableColumnDef {
		key("table.header.key"),
		identifier("table.type.header.type.identifier"),
		displayName("table.type.header.type.displayName"),
		externalId("table.type.header.type.externalId"),
		tools("table.header.tools");
		
		private final String i18nHeaderKey;
		
		private TypesCols(String i18nHeaderKey) {
			this.i18nHeaderKey = i18nHeaderKey;
		}

		@Override
		public boolean sortable() {
			return this != tools;
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
