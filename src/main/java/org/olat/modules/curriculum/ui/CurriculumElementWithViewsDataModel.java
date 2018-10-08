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

import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiTableDataModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiSortableColumnDef;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;

/**
 * 
 * Initial date: 11 mai 2018<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class CurriculumElementWithViewsDataModel extends DefaultFlexiTableDataModel<CurriculumElementWithViewsRow> {
	
	public CurriculumElementWithViewsDataModel(FlexiTableColumnModel columnsModel) {
		super(columnsModel);
	}
	
	@Override
	public Object getValueAt(int row, int col) {
		CurriculumElementWithViewsRow curriculum = getObject(row);
		switch(ElementViewCols.values()[col]) {
			case key: return curriculum.getKey();
			case elementDisplayName: return curriculum.getCurriculumElementDisplayName();
			case elementIdentifier: return curriculum.getCurriculumElementIdentifier();
			case entryDisplayName: return curriculum.getRepositoryEntryDisplayName();
			case entryExternalRef: return curriculum.getRepositoryEntryExternalRef();
			case mark: return curriculum.getMarkLink();
			case select: return curriculum.getSelectLink();
			case details: return curriculum.getDetailsLink();
			case start: return curriculum.getStartLink();
			case calendars: return curriculum.getCalendarsLink();
			default: return "ERROR";
		}
	}

	@Override
	public CurriculumElementWithViewsDataModel createCopyWithEmptyList() {
		return new CurriculumElementWithViewsDataModel(getTableColumnModel());
	}
	
	public enum ElementViewCols implements FlexiSortableColumnDef {
		key("table.header.key"),
		elementDisplayName("table.header.curriculum.element.displayName"),
		elementIdentifier("table.header.curriculum.element.identifier"),
		entryDisplayName("table.header.repository.entry.displayName"),
		entryExternalRef("table.header.repository.entry.externalRef"),
		mark("table.header.mark"),
		select("table.header.details"),
		details("table.header.details"),
		start("table.header.start"),
		calendars("table.header.calendars");
		
		private final String i18nHeaderKey;
		
		private ElementViewCols(String i18nHeaderKey) {
			this.i18nHeaderKey = i18nHeaderKey;
		}

		@Override
		public boolean sortable() {
			return false;
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
