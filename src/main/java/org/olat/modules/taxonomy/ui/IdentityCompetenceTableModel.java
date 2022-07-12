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
import org.olat.modules.taxonomy.TaxonomyLevelType;

/**
 * 
 * Initial date: 3 Oct 2017<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class IdentityCompetenceTableModel extends DefaultFlexiTableDataModel<IdentityCompetenceRow>
implements SortableFlexiTableDataModel<IdentityCompetenceRow> {

	public IdentityCompetenceTableModel(FlexiTableColumnModel columnsModel) {
		super(columnsModel);
	}
	
	@Override
	public void sort(SortKey orderBy) {
		if(orderBy != null) {
			List<IdentityCompetenceRow> views = new IdentityCompetenceTableModelSortDelegate(orderBy, this, null).sort();
			super.setObjects(views);
		}
	}

	@Override
	public Object getValueAt(int row, int col) {
		IdentityCompetenceRow competence = getObject(row);
		return getValueAt(competence, col);
	}
	
	@Override
	public Object getValueAt(IdentityCompetenceRow row, int col) {
		switch(IdCompetenceCols.values()[col]) {
			case key: return row.getCompetence().getKey();
			case taxonomyIdentifier: return row.getTaxonomy().getIdentifier();
			case taxonomyDisplayName: return row.getTaxonomy().getDisplayName();
			case taxonomyExternalId: return row.getTaxonomy().getExternalId();
			case taxonomyLevelIdentifier: return row.getTaxonomyLevel().getIdentifier();
			case taxonomyLevelDisplayName: return row.getDisplayName();
			case taxonomyLevelType: {
				TaxonomyLevelType type = row.getTaxonomyLevel().getType();
				return type == null ? null : type.getDisplayName();
			}
			case taxonomyLevelExternalId: return row.getTaxonomyLevel().getExternalId();
			case type: return row.getCompetenceType(); 
			case expiration: return row.getCompetence().getExpiration();
			case remove: return Boolean.valueOf(!row.isManaged());
			default: return null;
		}
	}
	
	public enum IdCompetenceCols implements FlexiSortableColumnDef {
		key("table.header.key"),
		taxonomyIdentifier("table.header.taxonomy.identifier"),
		taxonomyDisplayName("table.header.taxonomy.displayName"),
		taxonomyExternalId("table.header.taxonomy.externalId"),
		taxonomyLevelIdentifier("table.header.taxonomy.level.identifier"),
		taxonomyLevelDisplayName("table.header.taxonomy.level.displayName"),
		taxonomyLevelType("table.header.taxonomy.level.type"),
		taxonomyLevelExternalId("table.header.taxonomy.level.externalId"),
		type("table.header.competence.type"),
		expiration("table.header.competence.expiration"),
		remove("remove");
		
		private final String i18nHeaderKey;
		
		private IdCompetenceCols(String i18nHeaderKey) {
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
