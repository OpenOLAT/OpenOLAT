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
package org.olat.modules.taxonomy.ui;

import java.util.List;

import org.olat.core.commons.persistence.SortKey;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiTableDataModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiSortableColumnDef;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SortableFlexiTableDataModel;

/**
 * 
 * Initial date: 2 oct. 2017<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class TaxonomyLevelTypesTableModel extends DefaultFlexiTableDataModel<TaxonomyLevelTypeRow>
implements SortableFlexiTableDataModel<TaxonomyLevelTypeRow> {
	
	public TaxonomyLevelTypesTableModel(FlexiTableColumnModel columnsModel) {
		super(columnsModel);
	}
	
	@Override
	public void sort(SortKey orderBy) {
		if(orderBy != null) {
			List<TaxonomyLevelTypeRow> views = new TaxonomyLevelTypesTableModelSortDelegate(orderBy, this, null).sort();
			super.setObjects(views);
		}
	}

	@Override
	public Object getValueAt(int row, int col) {
		TaxonomyLevelTypeRow type = getObject(row);
		return getValueAt(type, col);
	}
	
	@Override
	public Object getValueAt(TaxonomyLevelTypeRow row, int col) {
		switch(TypesCols.values()[col]) {
			case identifier: return row.getIdentifier();
			case displayName: return row.getDisplayName();
			case allowedAsCompetence: return row.getType().isAllowedAsCompetence();
			case tools: return row.getToolsLink();
			default: return null;
		}
	}
	
	@Override
	public DefaultFlexiTableDataModel<TaxonomyLevelTypeRow> createCopyWithEmptyList() {
		return new TaxonomyLevelTypesTableModel(getTableColumnModel());
	}
	
	public enum TypesCols implements FlexiSortableColumnDef {
		identifier("table.header.type.identifier"),
		displayName("table.header.type.displayName"),
		allowedAsCompetence("table.header.type.competence.allowed"),
		tools("table.header.actions");
		
		private final String i18nHeaderKey;
		
		private TypesCols(String i18nHeaderKey) {
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
