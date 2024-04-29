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
package org.olat.modules.ceditor.ui;

import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiTableDataModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;

/**
 * Initial date: 2024-04-29<br>
 *
 * @author cpfranger, christoph.pfranger@frentix.com, <a href="https://www.frentix.com">https://www.frentix.com</a>
 */
public class GalleryModel extends DefaultFlexiTableDataModel<GalleryRow> {

	private static final GalleryColumn[] COLS = GalleryColumn.values();

	public GalleryModel(FlexiTableColumnModel columnModel) {
		super(columnModel);
	}

	@Override
	public Object getValueAt(int row, int col) {
		GalleryRow galleryRow = getObject(row);

		return switch (COLS[col]) {
			case up -> row > 0;
			case down -> row < (getRowCount() - 1);
			case title -> galleryRow.getTitle();
			case description -> galleryRow.getDescription();
			case version -> galleryRow.getVersion();
			case tools -> galleryRow.getToolLink();
		};
	}

	public enum GalleryColumn {
		up("gallery.up"),
		down("gallery.down"),
		title("gallery.title"),
		description("gallery.description"),
		version("gallery.version"),
		tools("gallery.tools");

		private final String i18nKey;

		GalleryColumn(String i18nKey) {
			this.i18nKey = i18nKey;
		}

		public String getI18nKey() {
			return i18nKey;
		}
	}
}
