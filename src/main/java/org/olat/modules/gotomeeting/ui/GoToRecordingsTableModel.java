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
package org.olat.modules.gotomeeting.ui;

import java.util.List;
import java.util.Locale;

import org.olat.core.commons.persistence.SortKey;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiTableDataModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SortableFlexiTableDataModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SortableFlexiTableModelDelegate;
import org.olat.modules.gotomeeting.model.GoToRecordingsG2T;

/**
 * 
 * Initial date: 22.03.2016<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class GoToRecordingsTableModel extends DefaultFlexiTableDataModel<GoToRecordingsG2T> implements SortableFlexiTableDataModel<GoToRecordingsG2T> {
	
	public GoToRecordingsTableModel(FlexiTableColumnModel columnsModel) {
		super(columnsModel);
	}

	@Override
	public void sort(SortKey orderBy) {
		if(orderBy != null) {
			List<GoToRecordingsG2T> views = new GoToRecordingTableModelSort(orderBy, this, null).sort();
			super.setObjects(views);
		}
	}

	@Override
	public Object getValueAt(int row, int col) {
		GoToRecordingsG2T recording = getObject(row);
		return getValueAt(recording, col);
	}

	@Override
	public Object getValueAt(GoToRecordingsG2T recording, int col) {
		switch(RecordingsCols.values()[col]) {
			case name: return recording.getName();
			case start: return recording.getStartDate();
			case end: return recording.getEndDate();
		}
		return null;
	}
	
	public enum RecordingsCols {
		
		name("meeting.name"),
		start("meeting.start"),
		end("meeting.end");
		
		private final String i18nHeaderKey;
		
		private RecordingsCols(String i18nHeaderKey) {
			this.i18nHeaderKey = i18nHeaderKey;
		}

		public String i18nHeaderKey() {
			return i18nHeaderKey;
		}
	}
	
	private static class GoToRecordingTableModelSort extends SortableFlexiTableModelDelegate<GoToRecordingsG2T> {
		
		public GoToRecordingTableModelSort(SortKey orderBy, SortableFlexiTableDataModel<GoToRecordingsG2T> tableModel, Locale locale) {
			super(orderBy, tableModel, locale);
		}

		@Override
		protected void sort(List<GoToRecordingsG2T> rows) {
			int columnIndex = getColumnIndex();
			RecordingsCols column = RecordingsCols.values()[columnIndex];
			switch(column) {
				default: {
					super.sort(rows);
				}
			}
		}
	}
}
