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
package org.olat.modules.video.ui;

import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiTableDataModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiSortableColumnDef;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;

/**
 *
 * Initial date: 21.10.2016<br>
 * @author Fabian Kiefer, fabian.kiefer@frentix.com, http://www.frentix.com
 *
 */
public class VideoChapterTableModel extends DefaultFlexiTableDataModel<VideoChapterTableRow>{

	public VideoChapterTableModel(FlexiTableColumnModel columnModel) {
		super(columnModel);
	}

	@Override
	public Object getValueAt(int row, int col) {
		VideoChapterTableRow chapter = getObject(row);
		switch(ChapterTableCols.values()[col]) {
			case chapterName: return chapter.getChapterName();
			case intervals: return chapter.getIntervals();
			default: return "";
		}
	}

	public enum ChapterTableCols implements FlexiSortableColumnDef {
		chapterName("video.chapter.chapterName"),
		intervals("video.chapter.intervals");

		private final String i18nKey;

		private ChapterTableCols(String i18nKey) {
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
		
		public String i18nKey() {
			return i18nKey;
		}
	}

}