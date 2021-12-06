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
package org.olat.modules.quality.generator.ui;

import java.util.List;
import java.util.Locale;

import org.olat.core.commons.persistence.SortKey;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiTableDataModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiSortableColumnDef;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SortableFlexiTableDataModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SortableFlexiTableModelDelegate;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.model.RepositoryEntryLifecycle;

/**
 * 
 * Initial date: 09.10.2018<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
class RepositoryEntryListDataModel extends DefaultFlexiTableDataModel<RepositoryEntry>
		implements SortableFlexiTableDataModel<RepositoryEntry> {
	
	private final Locale locale;
	
	RepositoryEntryListDataModel(FlexiTableColumnModel columnsModel, Locale locale) {
		super(columnsModel);
		this.locale = locale;
	}

	@Override
	public void sort(SortKey orderBy) {
		List<RepositoryEntry> rows = new SortableFlexiTableModelDelegate<>(orderBy, this, locale).sort();
		super.setObjects(rows);
	}

	@Override
	public Object getValueAt(int row, int col) {
		RepositoryEntry course = getObject(row);
		return getValueAt(course, col);
	}

	@Override
	public Object getValueAt(RepositoryEntry row, int col) {
		switch(Cols.values()[col]) {
			case id: return row.getKey();
			case displayName: return row.getDisplayname();
			case identifier: return row.getExternalRef();
			case begin: {
				RepositoryEntryLifecycle lifecycle = row.getLifecycle();
				return lifecycle != null? lifecycle.getValidFrom(): null;
			}
			case end:  {
				RepositoryEntryLifecycle lifecycle = row.getLifecycle();
				return lifecycle != null? lifecycle.getValidTo(): null;
			}
			default: return null;
		}
	}
	
	public enum Cols implements FlexiSortableColumnDef {
		id("repository.entry.id"),
		identifier("repository.entry.identifier"),
		displayName("repository.entry.display.name"),
		begin("repository.entry.begin"),
		end("repository.entry.end");
		
		private final String i18nHeaderKey;
		
		private Cols(String i18nHeaderKey) {
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
