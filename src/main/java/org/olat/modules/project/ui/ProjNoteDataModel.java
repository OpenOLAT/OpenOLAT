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
 * Initial date: 16 Dec 2022<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class ProjNoteDataModel extends DefaultFlexiTableDataModel<ProjNoteRow> implements SortableFlexiTableDataModel<ProjNoteRow> {
	
	private static final NoteCols[] COLS = NoteCols.values();

	private final Locale locale;
	
	public ProjNoteDataModel(FlexiTableColumnModel columnsModel, Locale locale) {
		super(columnsModel);
		this.locale = locale;
	}
	
	public ProjNoteRow getObjectByKey(Long key) {
		List<ProjNoteRow> rows = getObjects();
		for (ProjNoteRow row: rows) {
			if (row != null && row.getKey().equals(key)) {
				return row;
			}
		}
		return null;
	}

	@Override
	public void sort(SortKey orderBy) {
		List<ProjNoteRow> rows = new SortableFlexiTableModelDelegate<>(orderBy, this, locale).sort();
		super.setObjects(rows);
	}

	@Override
	public Object getValueAt(int row, int col) {
		ProjNoteRow note = getObject(row);
		return getValueAt(note, col);
	}

	@Override
	public Object getValueAt(ProjNoteRow row, int col) {
		switch(COLS[col]) {
		case id: return row.getKey();
		case displayName: return row.getDisplayName();
		case text: return row.getText();
		case creationDate: return row.getCreationDate();
		case lastModifiedDate: return row.getContentModifiedDate();
		case lastModifiedBy: return row.getContentModifiedBy();
		case tools: return row.getToolsLink();
		default: return null;
		}
	}
	
	public enum NoteCols implements FlexiSortableColumnDef {
		id("id"),
		displayName("title"),
		participants("note.participants"),
		text("note.text"),
		creationDate("created"),
		lastModifiedDate("last.modified.date"),
		lastModifiedBy("last.modified.by"),
		tools("tools");
		
		private final String i18nKey;
		
		private NoteCols(String i18nKey) {
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
