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
 * Initial date: 21 mars 2019<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class DeletedFileListDataModel extends DefaultFlexiTableDataModel<DeletedFileRow>
implements SortableFlexiTableDataModel<DeletedFileRow> {
	
	private final Translator translator;
	
	public DeletedFileListDataModel(FlexiTableColumnModel columnModel, Translator translator) {
		super(columnModel);
		this.translator = translator;
	}

	@Override
	public void sort(SortKey orderBy) {
		if(orderBy != null) {
			List<DeletedFileRow> rows = new SortableFlexiTableModelDelegate<>(orderBy, this, translator.getLocale()).sort();
			super.setObjects(rows);
		}
	}

	@Override
	public Object getValueAt(int row, int col) {
		DeletedFileRow f = getObject(row);
		return getValueAt(f, col);
	}

	@Override
	public Object getValueAt(DeletedFileRow row, int col) {
		switch(DeletedCols.values()[col]) {
			case id: return row.getMetadata().getKey();
			case nr: return row.getMetadata().getRevisionNr();
			case css: return row.getFilename();
			case filename: return row.getFilename();
			case size: return row.getMetadata().getFileSize();
			case deletedBy: return row.getDeletedBy();
			case date: return row.getDate();
			case download: return row.getDownloadLink();
			case restore: return row.getLastRevision() != null;
			default: return "ERROR";
		}
	}
	
	public enum DeletedCols implements FlexiSortableColumnDef {
		
		id("table.header.id"),
		css("table.header.css"),
		nr("table.header.nr"),
		filename("table.header.file"),
		size("table.header.size"),
		deletedBy("version.deletedBy"),
		date("table.header.date"),
		download("download"),
		restore("version.restore");

		private final String i18nKey;
		
		private DeletedCols(String i18nKey) {
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
