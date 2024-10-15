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
package org.olat.core.commons.services.folder.ui;

import java.util.List;
import java.util.Locale;

import org.olat.core.commons.persistence.SortKey;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiTableDataModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiSortableColumnDef;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SortableFlexiTableDataModel;
import org.olat.core.util.vfs.VFSContainer;

/**
 * 
 * Initial date: 21 Feb 2024<br>
 * @author uhensler, urs.hensler@frentix.com, https://www.frentix.com
 *
 */
public class FolderDataModel extends DefaultFlexiTableDataModel<FolderRow> implements SortableFlexiTableDataModel<FolderRow> {
	
	static final FolderCols[] COLS = FolderCols.values();

	private final Locale locale;
	
	public FolderDataModel(FlexiTableColumnModel columnsModel, Locale locale) {
		super(columnsModel);
		this.locale = locale;
	}

	@Override
	public void sort(SortKey orderBy) {
		List<FolderRow> rows = new FolderModelSort(orderBy, this, locale).sort();
		super.setObjects(rows);
	}

	@Override
	public Object getValueAt(int row, int col) {
		FolderRow file = getObject(row);
		return getValueAt(file, col);
	}

	@Override
	public Object getValueAt(FolderRow row, int col) {
		return switch (COLS[col]) {
			case icon -> row;
			case title -> row.getTitleItem();
			case createdBy -> row.getCreatedBy();
			case lastModifiedDate -> row.getLastModifiedDate();
			case lastModifiedBy -> row.getLastModifiedBy();
			case deletedDate -> row.getDeletedDate();
			case deletedBy -> row.getDeletedBy();
			case type -> row.getTranslatedType();
			case status -> row;
			case size -> row.getTranslatedSize();
			case path -> row.getFilePathItem();
			case versions -> row.getVersions();
			case license -> row.getLicense();
			case tools -> row.getToolsLink();
			default -> null;
		};
	}

	@Override
	public boolean isSelectable(int row) {
		FolderRow file = getObject(row);
		return !(file.getVfsItem() instanceof VFSContainer);
	}
	
	public enum FolderCols implements FlexiSortableColumnDef {
		icon("table.icon"),
		title("table.title"),
		download("table.download"),
		createdBy("created.by"),
		lastModifiedDate("modified.date"),
		lastModifiedBy("modified.by"),
		deletedDate("deleted.date"),
		deletedBy("deleted.by"),
		type("table.type"),
		status("table.status"),
		size("table.size"),
		path("table.path"),
		versions("table.versions"),
		license("table.license"),
		tools("tools");
		
		private final String i18nKey;
		
		private FolderCols(String i18nKey) {
			this.i18nKey = i18nKey;
		}
		
		@Override
		public String i18nHeaderKey() {
			return i18nKey;
		}
		
		@Override
		public boolean sortable() {
			return this != icon
					&& this != download
					&& this != tools;
		}

		@Override
		public String sortKey() {
			return name();
		}
	}
}
