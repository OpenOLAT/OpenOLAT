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
package org.olat.modules.lecture.ui;

import org.olat.core.commons.persistence.SortKey;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiTableDataModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiSortableColumnDef;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SortableFlexiTableDataModel;
import org.olat.modules.lecture.model.LectureStatistics;

/**
 * 
 * Initial date: 28 mars 2017<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class ParticipantLecturesDataModel extends DefaultFlexiTableDataModel<LectureStatistics>
implements SortableFlexiTableDataModel<LectureStatistics> {
	
	public ParticipantLecturesDataModel(FlexiTableColumnModel columnModel) {
		super(columnModel);
	}

	@Override
	public void sort(SortKey sortKey) {
		// 
	}

	@Override
	public Object getValueAt(int row, int col) {
		LectureStatistics stats = getObject(row);
		return getValueAt(stats, col);
	}
	
	@Override
	public Object getValueAt(LectureStatistics row, int col) {
		switch(LecturesCols.values()[col]) {
			case entry: return row.getDisplayName();
			case quota: return row.getTotalPlannedLectures();
			case progress: return row;
			default: return null;
		}
	}

	@Override
	public DefaultFlexiTableDataModel<LectureStatistics> createCopyWithEmptyList() {
		return new ParticipantLecturesDataModel(getTableColumnModel());
	}
	
	public enum LecturesCols implements FlexiSortableColumnDef {
		entry("table.header.entry"),
		quota("table.header.quota"),
		progress("table.header.progress");
		
		private final String i18nKey;
		
		private LecturesCols(String i18nKey) {
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
