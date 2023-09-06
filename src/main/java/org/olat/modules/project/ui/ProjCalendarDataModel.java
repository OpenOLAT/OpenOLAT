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
package org.olat.modules.project.ui;

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
 * Initial date: 14 Jun 2023<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class ProjCalendarDataModel extends DefaultFlexiTableDataModel<ProjCalendarRow> implements SortableFlexiTableDataModel<ProjCalendarRow> {
	
	private static final CalendarCols[] COLS = CalendarCols.values();

	private final Locale locale;
	
	public ProjCalendarDataModel(FlexiTableColumnModel columnsModel, Locale locale) {
		super(columnsModel);
		this.locale = locale;
	}
	
	public ProjCalendarRow getObjectByKey(String type, Long key) {
		List<ProjCalendarRow> rows = getObjects();
		for (ProjCalendarRow row: rows) {
			if (row != null && row.getType().equalsIgnoreCase(type) && row.getKey().equals(key)) {
				return row;
			}
		}
		return null;
	}

	@Override
	public void sort(SortKey orderBy) {
		List<ProjCalendarRow> rows = new SortableFlexiTableModelDelegate<>(orderBy, this, locale).sort();
		super.setObjects(rows);
	}

	@Override
	public Object getValueAt(int row, int col) {
		ProjCalendarRow note = getObject(row);
		return getValueAt(note, col);
	}

	@Override
	public Object getValueAt(ProjCalendarRow row, int col) {
		switch(COLS[col]) {
		case id: return row.getKey();
		case type: return row.getTranslatedType();
		case displayName: return row.getDisplayName();
		case startDate: return row.getStartDate();
		case endDate: return row.getEndDate();
		case tags: return row.getFormattedTags();
		case involved: return row.getUserPortraits();
		case creationDate: return row.getCreationDate();
		case lastModifiedDate: return row.getContentModifiedDate();
		case lastModifiedBy: return row.getContentModifiedByName();
		case deletedDate: return row.getDeletedDate();
		case deletedBy: return row.getDeletedByName();
		case tools: return row.getToolsLink();
		default: return null;
		}
	}
	
	public enum CalendarCols implements FlexiSortableColumnDef {
		id("id"),
		type("calendar.type"),
		displayName("title"),
		startDate("appointment.edit.start"),
		endDate("appointment.edit.end"),
		tags("tags"),
		involved("involved"),
		creationDate("created"),
		lastModifiedDate("last.modified.date"),
		lastModifiedBy("last.modified.by"),
		deletedBy("deleted.by"),
		deletedDate("deleted.date"),
		tools("tools");
		
		private final String i18nKey;
		
		private CalendarCols(String i18nKey) {
			this.i18nKey = i18nKey;
		}
		
		@Override
		public String i18nHeaderKey() {
			return i18nKey;
		}
		
		@Override
		public boolean sortable() {
			return this != involved
					&& this != tools;
		}

		@Override
		public String sortKey() {
			 return name();
		}
	}
}
