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
 * Initial date: 3 Oct 2017<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class TaxonomyLevelCompetenceTableModel extends DefaultFlexiTableDataModel<TaxonomyLevelCompetenceRow>
implements SortableFlexiTableDataModel<TaxonomyLevelCompetenceRow> {

	public TaxonomyLevelCompetenceTableModel(FlexiTableColumnModel columnsModel) {
		super(columnsModel);
	}
	
	@Override
	public void sort(SortKey orderBy) {
		if(orderBy != null) {
			List<TaxonomyLevelCompetenceRow> views = new TaxonomyLevelCompetenceTableModelSortDelegate(orderBy, this, null).sort();
			super.setObjects(views);
		}
	}

	@Override
	public Object getValueAt(int row, int col) {
		TaxonomyLevelCompetenceRow competence = getObject(row);
		return getValueAt(competence, col);
	}
	
	@Override
	public Object getValueAt(TaxonomyLevelCompetenceRow row, int col) {
		if(col < TaxonomyLevelCompetenceController.USER_PROPS_OFFSET) {
			switch(CompetenceCols.values()[col]) {
				case key: return row.getKey();
				case type: return row.getCompetenceType(); 
				case achievement: return row.getAchievement();
				case reliability: return row.getReliability();
				case expiration: return row.getExpiration();
			}
		} else {
			int propPos = col - TaxonomyLevelCompetenceController.USER_PROPS_OFFSET;
			return row.getIdentityProp(propPos);
		}
		return null;
	}
	
	public enum CompetenceCols implements FlexiSortableColumnDef {
		key("table.header.key"),
		type("table.header.competence.type"),
		achievement("table.header.competence.achievement"),
		reliability("table.header.competence.reliability"),
		expiration("table.header.competence.expiration");
		
		private final String i18nHeaderKey;
		
		private CompetenceCols(String i18nHeaderKey) {
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
