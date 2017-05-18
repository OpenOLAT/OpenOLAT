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

import java.util.Locale;

import org.olat.core.commons.persistence.SortKey;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiTableDataModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiSortableColumnDef;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SortableFlexiTableDataModel;

/**
 * 
 * Initial date: 6 avr. 2017<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class ParticipantListDataModel extends DefaultFlexiTableDataModel<ParticipantRow>
implements SortableFlexiTableDataModel<ParticipantRow> {
	
	private final Locale locale;
	
	public ParticipantListDataModel(FlexiTableColumnModel columnModel, Locale locale) {
		super(columnModel);
		this.locale = locale;
	}

	@Override
	public void sort(SortKey sortKey) {
		//
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
				case username: return row.getIdentityName();
				case progress: return row.getStatistics();
				case plannedLectures: return positive(row.getStatistics().getTotalPersonalPlannedLectures());
				case attendedLectures: return positive(row.getStatistics().getTotalAttendedLectures());
				case absentLectures: return positive(row.getStatistics().getTotalAbsentLectures());
				case authorizedAbsenceLectures: return positive(row.getStatistics().getTotalAuthorizedAbsentLectures());
				case rateWarning: return row.getStatistics();
				case rate: return row.getStatistics().getAttendanceRate();
				default: return null;
			}
		}
		int propPos = col - ParticipantListRepositoryController.USER_PROPS_OFFSET;
		return row.getIdentityProp(propPos);
	}
	
	private static final long positive(long pos) {
		return pos < 0 ? 0 : pos;
	}

	@Override
	public DefaultFlexiTableDataModel<ParticipantRow> createCopyWithEmptyList() {
		return new ParticipantListDataModel(getTableColumnModel(), locale);
	}
	
	public enum ParticipantsCols implements FlexiSortableColumnDef {
		username("table.header.username"),
		plannedLectures("table.header.planned.lectures"),
		attendedLectures("table.header.attended.lectures"),
		absentLectures("table.header.absent.lectures"),
		authorizedAbsenceLectures("table.header.authorized.absence"),
		progress("table.header.progress"),
		rateWarning("table.header.rate.warning"),
		rate("table.header.rate");
		
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
