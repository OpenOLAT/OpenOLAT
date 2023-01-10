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
package org.olat.modules.video.ui.editor;

import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiTableDataModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiSortableColumnDef;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.modules.video.ui.VideoChapterTableRow;

/**
 * Initial date: 2022-12-12<br>
 *
 * @author cpfranger, christoph.pfranger@frentix.com, <a href="https://www.frentix.com">https://www.frentix.com</a>
 */
public class ChapterTableModel extends DefaultFlexiTableDataModel<VideoChapterTableRow> {
	public ChapterTableModel(FlexiTableColumnModel columnModel) {
		super(columnModel);
	}


	@Override
	public Object getValueAt(int row, int col) {
		VideoChapterTableRow chapterTableRow = getObject(row);
		switch (ChapterTableCols.values()[col]) {
			case start:
				return chapterTableRow.getIntervals();
			case text:
				return chapterTableRow.getChapterName();
			default:
				return "";
		}
	}

	public enum ChapterTableCols implements FlexiSortableColumnDef {
		start("table.header.chapter.start"),
		text("table.header.chapter.text"),
		edit("table.header.chapter.edit");

		private final String i18nKey;

		ChapterTableCols(String i18nKey) {
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
