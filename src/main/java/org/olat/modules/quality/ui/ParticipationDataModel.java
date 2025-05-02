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
package org.olat.modules.quality.ui;

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
 * Initial date: 13.06.2018<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class ParticipationDataModel extends DefaultFlexiTableDataModel<ParticipationRow>
		implements SortableFlexiTableDataModel<ParticipationRow> {
	
	private final static ParticipationCols[] COLS = ParticipationCols.values();
	
	private final Locale locale;

	public ParticipationDataModel(FlexiTableColumnModel columnsModel, Locale locale) {
		super(columnsModel);
		this.locale = locale;
	}
	
	@Override
	public void sort(SortKey orderBy) {
		List<ParticipationRow> rows = new SortableFlexiTableModelDelegate<>(orderBy, this, locale).sort();
		super.setObjects(rows);
	}
	
	@Override
	public Object getValueAt(int row, int col) {
		ParticipationRow participation = getObject(row);
		return getValueAt(participation, col);
	}
	
	@Override
	public Object getValueAt(ParticipationRow row, int col) {
		switch(COLS[col]) {
			case firstname: return row.getFirstname();
			case lastname: return row.getLastname();
			case email: return row.getEmail();
			case role: return row.getRole();
			case repositoryEntryName: return row.getAudienceRepositoryEntryName();
			case curriculumElementName: return row.getAudienceCurriculumElementName();
			case tools: return row.getToolsLink();
			default: return null;
		}
	}

	public enum ParticipationCols implements FlexiSortableColumnDef {
		firstname("participation.firstname"),
		lastname("participation.lastname"),
		email("participation.email"),
		role("participation.role"),
		repositoryEntryName("participation.repository.entry"),
		curriculumElementName("participation.curriculum.element"),
		tools("action.more");
		
		private final String i18nKey;
		
		private ParticipationCols(String i18nKey) {
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
