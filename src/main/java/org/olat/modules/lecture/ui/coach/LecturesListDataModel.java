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
package org.olat.modules.lecture.ui.coach;

import java.util.List;

import org.olat.core.commons.persistence.SortKey;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiTableDataModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiSortableColumnDef;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableFooterModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SortableFlexiTableDataModel;
import org.olat.core.gui.translator.Translator;
import org.olat.modules.lecture.model.AggregatedLectureBlocksStatistics;
import org.olat.modules.lecture.model.LectureBlockIdentityStatistics;

/**
 * 
 * Initial date: 16 juin 2017<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class LecturesListDataModel extends DefaultFlexiTableDataModel<LectureBlockIdentityStatisticsRow>
implements SortableFlexiTableDataModel<LectureBlockIdentityStatisticsRow>, FlexiTableFooterModel {
	
	private static final StatsCols[] COLS = StatsCols.values();
	
	private final Translator translator;
	private AggregatedLectureBlocksStatistics totalStatistics;
	
	public LecturesListDataModel(FlexiTableColumnModel columnModel, Translator translator) {
		super(columnModel);
		this.translator = translator;
	}

	@Override
	public void sort(SortKey orderBy) {
		if(orderBy != null) {
			List<LectureBlockIdentityStatisticsRow> views = new LecturesListDataSortDelegate(orderBy, this, null).sort();
			super.setObjects(views);
		}
	}

	@Override
	public Object getValueAt(int row, int col) {
		LectureBlockIdentityStatisticsRow stats = getObject(row);
		return getValueAt(stats, col);
	}

	@Override
	public Object getValueAt(LectureBlockIdentityStatisticsRow row, int col) {
		LectureBlockIdentityStatistics stats = row.getStatistics();
		if(col >= 0 && col < StatsCols.values().length) {
			switch(COLS[col]) {
				case id: return stats.getIdentityKey();
				case externalRef: return stats.getExternalRef();
				case entry: return stats.getDisplayName();
				case plannedLectures: return positive(stats.getTotalPersonalPlannedLectures());
				case attendedLectures: return positive(stats.getTotalAttendedLectures());
				case unauthorizedAbsenceLectures:
				case absentLectures:
					return row.getUnauthorizedLink() == null ? positive(stats.getTotalAbsentLectures()) : row.getUnauthorizedLink();
				case authorizedAbsenceLectures: return positive(stats.getTotalAuthorizedAbsentLectures());
				case currentRate: return stats.getAttendanceRate();
			}
		}
		
		int propPos = col - LecturesListController.USER_PROPS_OFFSET;
		return stats.getIdentityProp(propPos);
	}

	@Override
	public String getFooterHeader() {
		return translator.translate("total");
	}

	@Override
	public Object getFooterValueAt(int col) {
		if(totalStatistics == null) return null;
		
		if(col >= 0 && col < StatsCols.values().length) {
			switch(StatsCols.values()[col]) {
				case plannedLectures: return positive(totalStatistics.getPersonalPlannedLectures());
				case attendedLectures: return positive(totalStatistics.getAttendedLectures());
				case unauthorizedAbsenceLectures:
				case absentLectures: return positive(totalStatistics.getAbsentLectures());
				case authorizedAbsenceLectures: return positive(totalStatistics.getAuthorizedAbsentLectures());
				default: return null;
			}
		}
		return null;
	}

	private static final long positive(long pos) {
		return pos < 0 ? 0 : pos;
	}
	
	public void setObjects(List<LectureBlockIdentityStatisticsRow> objects, AggregatedLectureBlocksStatistics totalStatistics) {
		super.setObjects(objects);
		this.totalStatistics = totalStatistics;
	}
	
	@Override
	public LecturesListDataModel createCopyWithEmptyList() {
		return new LecturesListDataModel(getTableColumnModel(), translator);
	}
	
	public enum StatsCols implements FlexiSortableColumnDef {
		id("table.header.id"),
		externalRef("table.header.entry"),
		entry("table.header.entry"),
		plannedLectures("table.header.planned.lectures"),
		attendedLectures("table.header.attended.lectures"),
		absentLectures("table.header.absent.lectures"),
		unauthorizedAbsenceLectures("table.header.unauthorized.absence"),
		authorizedAbsenceLectures("table.header.authorized.absence"),
		currentRate("table.header.attended.current.rate");
		
		private final String i18nKey;
		
		private StatsCols(String i18nKey) {
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
