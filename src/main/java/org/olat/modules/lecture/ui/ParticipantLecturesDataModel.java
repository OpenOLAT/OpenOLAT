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

import java.util.List;
import java.util.Locale;

import org.olat.core.commons.persistence.SortKey;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiTableDataModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiSortableColumnDef;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableFooterModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SortableFlexiTableDataModel;
import org.olat.core.gui.translator.Translator;
import org.olat.modules.lecture.model.AggregatedLectureBlocksStatistics;
import org.olat.modules.lecture.model.LectureBlockStatistics;

/**
 * 
 * Initial date: 28 mars 2017<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class ParticipantLecturesDataModel extends DefaultFlexiTableDataModel<LectureBlockStatistics>
implements SortableFlexiTableDataModel<LectureBlockStatistics>, FlexiTableFooterModel {
	
	private final Locale locale;
	private final Translator translator;
	private AggregatedLectureBlocksStatistics totalStatistics;
	
	public ParticipantLecturesDataModel(FlexiTableColumnModel columnModel, Translator translator, Locale locale) {
		super(columnModel);
		this.locale = locale;
		this.translator = translator;
	}

	@Override
	public void sort(SortKey orderBy) {
		List<LectureBlockStatistics> rows = new ParticipantLecturesSortDelegate(orderBy, this, locale).sort();
		super.setObjects(rows);
	}

	@Override
	public Object getValueAt(int row, int col) {
		LectureBlockStatistics stats = getObject(row);
		return getValueAt(stats, col);
	}
	
	@Override
	public Object getValueAt(LectureBlockStatistics row, int col) {
		switch(LecturesCols.values()[col]) {
			case externalRef: return row.getExternalRef();
			case entry: return row.getDisplayName();
			case plannedLectures: return positive(row.getTotalPersonalPlannedLectures());
			case attendedLectures: return positive(row.getTotalAttendedLectures());
			case authorizedAbsentLectures: return positive(row.getTotalAuthorizedAbsentLectures());
			case dispensedLectures: return positive(row.getTotalDispensationLectures());
			case unauthorizedAbsentLectures:
			case absentLectures: return positive(row.getTotalAbsentLectures());
			case progress: return row;
			case rateWarning: {
				if(!row.isCalculateRate() || row.getTotalEffectiveLectures() <= 0) {
					return null;
				}
				return row;
			}
			case rate: {
				if(!row.isCalculateRate() || row.getTotalEffectiveLectures() <= 0) {
					return null;
				}
				return row.getAttendanceRate();
			}
			default: return null;
		}
	}

	@Override
	public String getFooterHeader() {
		return translator.translate("total");
	}

	@Override
	public Object getFooterValueAt(int col) {
		if(totalStatistics == null) return null;
		
		switch(LecturesCols.values()[col]) {
			case plannedLectures: return positive(totalStatistics.getPersonalPlannedLectures());
			case attendedLectures: return positive(totalStatistics.getAttendedLectures());
			case authorizedAbsentLectures: return positive(totalStatistics.getAuthorizedAbsentLectures());
			case dispensedLectures: return positive(totalStatistics.getDispensedLectures());
			case unauthorizedAbsentLectures:
			case absentLectures: return positive(totalStatistics.getAbsentLectures());
			case rate: return totalStatistics.getRate();
			default: return null;
		}
	}
	
	private long positive(long val) {
		return val < 0 ? 0 : val;
	}
	
	public void setObjects(List<LectureBlockStatistics> objects, AggregatedLectureBlocksStatistics totalStatistics) {
		super.setObjects(objects);
		this.totalStatistics = totalStatistics;
	}
	
	public enum LecturesCols implements FlexiSortableColumnDef {
		externalRef("table.header.external.ref"),
		entry("table.header.entry"),
		plannedLectures("table.header.planned.lectures"),
		attendedLectures("table.header.attended.lectures"),
		unauthorizedAbsentLectures("table.header.unauthorized.absence"),
		authorizedAbsentLectures("table.header.authorized.absence"),
		dispensedLectures("table.header.dispensation"),
		absentLectures("table.header.absent.lectures"),
		progress("table.header.progress"),
		rateWarning("table.header.rate.warning"),
		rate("table.header.rate");
		
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
