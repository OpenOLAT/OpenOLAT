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
package org.olat.core.commons.services.vfs.ui.version;

import java.util.List;

import org.olat.core.commons.persistence.SortKey;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiTableDataModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiSortableColumnDef;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SortableFlexiTableDataModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SortableFlexiTableModelDelegate;
import org.olat.core.gui.translator.Translator;

/**
 * 
 * Initial date: 3 avr. 2019<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class VersionsDeletedFileDataModel extends DefaultFlexiTableDataModel<VersionsDeletedFileRow>
implements SortableFlexiTableDataModel<VersionsDeletedFileRow> {
	
	private final Translator translator;
	
	public VersionsDeletedFileDataModel(FlexiTableColumnModel columnModel, Translator translator) {
		super(columnModel);
		this.translator = translator;
	}

	@Override
	public void sort(SortKey orderBy) {
		if(orderBy != null) {
			List<VersionsDeletedFileRow> rows = new SortableFlexiTableModelDelegate<>(orderBy, this, translator.getLocale()).sort();
			super.setObjects(rows);
		}
	}

	@Override
	public Object getValueAt(int row, int col) {
		VersionsDeletedFileRow f = getObject(row);
		return getValueAt(f, col);
	}

	@Override
	public Object getValueAt(VersionsDeletedFileRow row, int col) {
		switch(VersionsDeletedCols.values()[col]) {
			case id: return row.getRevisionKey();
			case nr: return row.getRevisionNr();
			case relativePath: return row.getRelativePath();
			case filename: return row.getFilename();
			case size: return row.getSize();
			
			default: return "ERROR";
		}
	}
	
	public enum VersionsDeletedCols implements FlexiSortableColumnDef {
		
		id("table.header.id"),
		nr("table.header.nr"),
		relativePath("table.header.path"),
		filename("table.header.file"),
		size("table.header.size");

		private final String i18nKey;
		
		private VersionsDeletedCols(String i18nKey) {
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
