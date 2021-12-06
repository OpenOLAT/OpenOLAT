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

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.olat.core.commons.persistence.SortKey;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiTableDataModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.ExportableFlexiTableDataModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiSortableColumnDef;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableComponent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SortableFlexiTableDataModel;
import org.olat.core.gui.media.MediaResource;
import org.olat.core.gui.translator.Translator;

/**
 * 
 * Initial date: 6 avr. 2017<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class ParticipantListDataModel extends DefaultFlexiTableDataModel<ParticipantRow>
implements SortableFlexiTableDataModel<ParticipantRow>, ExportableFlexiTableDataModel {
	
	private final Locale locale;
	private final Translator translator;
	
	public ParticipantListDataModel(FlexiTableColumnModel columnModel, Translator translator, Locale locale) {
		super(columnModel);
		this.locale = locale;
		this.translator = translator;
	}

	@Override
	public void sort(SortKey sortKey) {
		List<ParticipantRow> rows = new ParticipantListSortDelegate(sortKey, this, locale).sort();
		super.setObjects(rows);
	}

	@Override
	public MediaResource export(FlexiTableComponent ftC) {
		FlexiTableColumnModel columnModel = getTableColumnModel();
		int numOfColumns = columnModel.getColumnCount();
		List<FlexiColumnModel> columns = new ArrayList<>();
		for(int i=0; i<numOfColumns; i++) {
			FlexiColumnModel column = columnModel.getColumnModel(i);
			if(column.isExportable()) {
				columns.add(column);
			}
		}
		return new ParticipantListExport().export(ftC, columns, translator);
	}

	@Override
	public Object getValueAt(int row, int col) {
		ParticipantRow participant = getObject(row);
		return getValueAt(participant, col);
	}

	@Override
	public Object getValueAt(ParticipantRow row, int col) {
		if(col < ParticipantListRepositoryController.USER_PROPS_OFFSET) {
			switch(ParticipantsCols.values()[col]) {
				case progress: return row.getStatistics();
				case plannedLectures: return positive(row.getStatistics().getTotalPersonalPlannedLectures());
				case attendedLectures: return positive(row.getStatistics().getTotalAttendedLectures());
				case unauthorizedAbsenceLectures:
				case absentLectures: return positive(row.getStatistics().getTotalAbsentLectures());
				case authorizedAbsenceLectures: return positive(row.getStatistics().getTotalAuthorizedAbsentLectures());
				case dispensedLectures: return positive(row.getStatistics().getTotalDispensationLectures());
				case rateWarning: return row.getStatistics();
				case rate: return row.getStatistics().getAttendanceRate();
				case infos: return row;
				default: return null;
			}
		}
		int propPos = col - ParticipantListRepositoryController.USER_PROPS_OFFSET;
		return row.getIdentityProp(propPos);
	}
	
	private static final long positive(long pos) {
		return pos < 0 ? 0 : pos;
	}
	
	public enum ParticipantsCols implements FlexiSortableColumnDef {
		plannedLectures("table.header.planned.lectures"),
		attendedLectures("table.header.attended.lectures"),
		absentLectures("table.header.absent.lectures"),
		unauthorizedAbsenceLectures("table.header.unauthorized.absence"),
		authorizedAbsenceLectures("table.header.authorized.absence"),
		dispensedLectures("table.header.dispensation"),
		progress("table.header.progress"),
		rateWarning("table.header.rate.warning"),
		rate("table.header.rate"),
		infos("table.header.infos");
		
		private final String i18nKey;
		
		private ParticipantsCols(String i18nKey) {
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
