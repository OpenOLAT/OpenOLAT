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
package org.olat.admin.security;

import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiTableDataModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiSortableColumnDef;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;

/**
 * Initial date: 2024-10-21<br>
 *
 * @author cpfranger, christoph.pfranger@frentix.com, <a href="https://www.frentix.com">https://www.frentix.com</a>
 */
public class MediaServerModel extends DefaultFlexiTableDataModel<MediaServerRow> {

	public MediaServerModel(FlexiTableColumnModel columnsModel) {
		super(columnsModel);
	}

	@Override
	public Object getValueAt(int row, int col) {
		MediaServerRow serverRow = getObject(row);
		return getValueAt(serverRow, col);
	}

	private Object getValueAt(MediaServerRow serverRow, int col) {
		return switch (MediaServerCol.values()[col]) {
			case name -> serverRow.name();
			case domain -> serverRow.domain();
			case edit -> true;
			case delete -> true;
		};
	}

	public enum MediaServerCol implements FlexiSortableColumnDef {
		name("media.server.name"),
		domain("media.server.domain"),
		edit("edit"),
		delete("delete");

		private final String i18nKey;

		MediaServerCol(String i18nKey) {
			this.i18nKey = i18nKey;
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
			return i18nKey;
		}
	}
}
