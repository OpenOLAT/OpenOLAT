/**
 * <a href="https://www.openolat.org">
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
 * frentix GmbH, https://www.frentix.com
 * <p>
 */
package org.olat.course.archiver.wizard;

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
 * Initial date: 21 févr. 2024<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class BulkCoursesArchivesRepositoryEntriesTableModel extends DefaultFlexiTableDataModel<ArchiveRepositoryEntryRow>
implements SortableFlexiTableDataModel<ArchiveRepositoryEntryRow>  {
	
	private static final ArchivesCols[] COLS = ArchivesCols.values();
	private final Locale locale;
	
	public BulkCoursesArchivesRepositoryEntriesTableModel(FlexiTableColumnModel columnModel, Locale locale) {
		super(columnModel);
		this.locale = locale;
	}
	
	@Override
	public void sort(SortKey orderBy) {
		if (orderBy != null) {
			List<ArchiveRepositoryEntryRow> views = new SortableFlexiTableModelDelegate<>(orderBy, this, locale).sort();
			super.setObjects(views);
		}
	}

	@Override
	public Object getValueAt(int row, int col) {
		ArchiveRepositoryEntryRow entryRow = getObject(row);
		return getValueAt(entryRow, col);
	}

	@Override
	public Object getValueAt(ArchiveRepositoryEntryRow row, int col) {
		switch(COLS[col]) {
			case title: return row.getDisplayName();
			case status: return row.getStatus();
			case externalRef: return row.getExternalRef();
			case numOfArchives: return row.getNumOfArchives();
			case statusArchives: return row;
			case typeArchive: return row.getArchiveTypeEl();
			default: return "ERROR";
		}
	}
	

	public enum ArchivesCols implements FlexiSortableColumnDef {
		title("table.header.entry.title"),
		status("table.header.entry.status"),
		externalRef("table.header.entry.external.ref"),
		numOfArchives("table.header.num.archives"),
		statusArchives("table.header.status.archives"),
		typeArchive("table.header.type.archive");
		
		private final String i18nKey;
		
		private ArchivesCols(String i18nKey) {
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
