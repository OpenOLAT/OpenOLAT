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
package org.olat.modules.cemedia.ui;

import java.util.List;
import java.util.Locale;

import org.olat.core.commons.persistence.SortKey;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiTableDataModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiSortableColumnDef;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SortableFlexiTableDataModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SortableFlexiTableModelDelegate;
import org.olat.core.gui.translator.Translator;

/**
 * 
 * Initial date: 5 juin 2023<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class MediaUsageTableModel extends DefaultFlexiTableDataModel<MediaUsageRow>
implements SortableFlexiTableDataModel<MediaUsageRow> {
	
	private static final MediaUsageCols[] COLS = MediaUsageCols.values();
	
	private final Locale locale;
	private final Translator translator;
	
	public MediaUsageTableModel(FlexiTableColumnModel columnsModel, Translator translator, Locale locale) {
		super(columnsModel);
		this.locale = locale;
		this.translator = translator;
	}
	
	@Override
	public void sort(SortKey orderBy) {
		if(orderBy != null) {
			List<MediaUsageRow> views = new SortableFlexiTableModelDelegate<>(orderBy, this, locale).sort();
			super.setObjects(views);
		}
	}

	@Override
	public Object getValueAt(int row, int col) {
		MediaUsageRow usageRow = getObject(row);
		return getValueAt(usageRow, col);
	}

	@Override
	public Object getValueAt(MediaUsageRow row, int col) {
		switch(COLS[col]) {
			case use: return row;
			case resource: return row;
			case version: return getVersion(row);
			case usedBy: return row.getUserFullName();
			case status: return Boolean.valueOf(row.isRevoked());
			default: return "ERROR";
		}
	}
	
	public String getVersion(MediaUsageRow row) {
		String version = row.getVersionName();
		if(version == null || "0".equals(version)) {
			return translator.translate("last.version.short");
		}
		return version;
	}
	
	public enum MediaUsageCols implements FlexiSortableColumnDef {
		use("table.header.use"),
		resource("table.header.resource"),
		version("table.header.version"),
		usedBy("table.header.used.by"),
		status("table.header.status");

		private final String i18nKey;
		
		private MediaUsageCols(String i18nKey) {
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
