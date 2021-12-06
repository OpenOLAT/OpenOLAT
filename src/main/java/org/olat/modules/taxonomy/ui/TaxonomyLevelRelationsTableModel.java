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
import org.olat.core.gui.components.form.flexible.impl.elements.table.SortableFlexiTableModelDelegate;

/**
 * 
 * Initial date: 16 nov. 2017<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class TaxonomyLevelRelationsTableModel extends DefaultFlexiTableDataModel<TaxonomyLevelRelationRow>
implements SortableFlexiTableDataModel<TaxonomyLevelRelationRow> {

	public TaxonomyLevelRelationsTableModel(FlexiTableColumnModel columnsModel) {
		super(columnsModel);
	}
	
	@Override
	public void sort(SortKey orderBy) {
		if(orderBy != null) {
			SortableFlexiTableModelDelegate<TaxonomyLevelRelationRow> sorter
				= new SortableFlexiTableModelDelegate<>(orderBy, this, null);
			List<TaxonomyLevelRelationRow> views = sorter.sort();
			super.setObjects(views);
		}
	}
	
	@Override
	public Object getValueAt(int row, int col) {
		TaxonomyLevelRelationRow relation = getObject(row);
		return getValueAt(relation, col);
	}

	@Override
	public Object getValueAt(TaxonomyLevelRelationRow row, int col) {
		switch(RelationsCols.values()[col]) {
			case key: return row.getKey();
			case type: return row.getRelation().getClass().getSimpleName();
			case displayName: return row.getDisplayName();
			case externalId: return row.getExternalId();
			default: return "ERROR";
		}
	}

	public enum RelationsCols implements FlexiSortableColumnDef {
		key("table.header.key"),
		type("table.header.competence.type"),
		displayName("table.header.displayName"),
		externalId("table.header.relation.externalId");
		
		private final String i18nHeaderKey;
		
		private RelationsCols(String i18nHeaderKey) {
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
