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
import org.olat.core.util.StringHelper;

/**
 * 
 * Initial date: 19 mars 2019<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class RevisionListDataModel extends DefaultFlexiTableDataModel<RevisionRow>
implements SortableFlexiTableDataModel<RevisionRow> {
	
	private final Translator translator;
	
	public RevisionListDataModel(FlexiTableColumnModel columnModel, Translator translator) {
		super(columnModel);
		this.translator = translator;
	}

	@Override
	public void sort(SortKey orderBy) {
		if(orderBy != null) {
			List<RevisionRow> rows = new SortableFlexiTableModelDelegate<>(orderBy, this, translator.getLocale()).sort();
			super.setObjects(rows);
		}
	}

	@Override
	public Object getValueAt(int row, int col) {
		RevisionRow rev = getObject(row);
		return getValueAt(rev, col);
	}

	@Override
	public Object getValueAt(RevisionRow row, int col) {
		switch(RevisionCols.values()[col]) {
			case id: return row.getKey();
			case nr: return row.getFormatedRevisionNr();
			case size: return row.getSize();
			case author: return row.getAuthor();
			case revisionComment: {
				String comment = row.getRevisionComment();
				if(!StringHelper.containsNonWhitespace(comment) && row.getRevisionNr() <= 1) {
					comment = translator.translate("version.initialRevision");
				}
				return comment;
			}
			case date: return row.getRevisionDate();
			case download: return row.getDownloadLink();
			case restore:
			case delete: return Boolean.valueOf(!row.isCurrent());
			default: return "ERROR";
		}
	}
	
	public enum RevisionCols implements FlexiSortableColumnDef {
		
		id("table.header.id"),
		nr("table.header.nr"),
		size("table.header.size"),
		author("table.header.author"),
		revisionComment("table.header.comment"),
		date("table.header.date"),
		download("download"),
		restore("version.restore"),
		delete("delete");

		private final String i18nKey;
		
		private RevisionCols(String i18nKey) {
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
