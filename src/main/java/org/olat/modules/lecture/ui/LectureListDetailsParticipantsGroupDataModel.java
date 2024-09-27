/**
 * <a href="https://www.openolat.org">
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
 * frentix GmbH, https://www.frentix.com
 * <p>
 */
package org.olat.modules.lecture.ui;

import java.util.List;
import java.util.Locale;

import org.olat.core.commons.persistence.SortKey;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiTableDataModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiSortableColumnDef;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SortableFlexiTableDataModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SortableFlexiTableModelDelegate;

/**
 * 
 * Initial date: 27 sept. 2024<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class LectureListDetailsParticipantsGroupDataModel extends DefaultFlexiTableDataModel<LectureBlockParticipantGroupRow>
implements SortableFlexiTableDataModel<LectureBlockParticipantGroupRow> {
	
	private static final GroupCols[] COLS = GroupCols.values();
	
	private final Locale locale;
	
	public LectureListDetailsParticipantsGroupDataModel(FlexiTableColumnModel columnModel, Locale locale) {
		super(columnModel);
		this.locale = locale;
	}
	
	@Override
	public void sort(SortKey orderBy) {
		if(orderBy != null) {
			List<LectureBlockParticipantGroupRow> rows = new SortableFlexiTableModelDelegate<>(orderBy, this, locale).sort();
			super.setObjects(rows);
		}
	}

	@Override
	public Object getValueAt(int row, int col) {
		LectureBlockParticipantGroupRow groupRow = getObject(row);
		return getValueAt(groupRow, col);
	}

	@Override
	public Object getValueAt(LectureBlockParticipantGroupRow row, int col) {
		return switch(COLS[col]) {
			case title -> row.getTitleLink();
			case numParticipants -> row.getNumOfParticipants();
			case status -> Boolean.valueOf(row.isExcluded());
			case tools -> row.getToolsLink();
			default -> "ERROR";
		};
	}
	
	public enum GroupCols implements FlexiSortableColumnDef {
		title("table.header.for"),
		status("table.header.status"),
		numParticipants("table.header.participants"),
		tools("table.header.actions");
		
		private final String i18nKey;
		
		private GroupCols(String i18nKey) {
			this.i18nKey = i18nKey;
		}
		
		@Override
		public String i18nHeaderKey() {
			return i18nKey;
		}

		@Override
		public boolean sortable() {
			return this != tools;
		}

		@Override
		public String sortKey() {
			return name();
		}
	}
}
