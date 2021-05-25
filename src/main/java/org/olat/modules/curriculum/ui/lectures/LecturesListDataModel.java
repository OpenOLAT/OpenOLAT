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
package org.olat.modules.curriculum.ui.lectures;

import java.util.List;
import java.util.stream.Collectors;

import org.olat.core.commons.persistence.SortKey;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableFilter;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiTableDataModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.ExportableFlexiTableDataModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FilterableFlexiTableModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiSortableColumnDef;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableComponent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableFooterModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SortableFlexiTableDataModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SortableFlexiTableModelDelegate;
import org.olat.core.gui.media.MediaResource;
import org.olat.core.gui.translator.Translator;
import org.olat.modules.lecture.LectureRateWarning;
import org.olat.modules.lecture.model.AggregatedLectureBlocksStatistics;
import org.olat.modules.lecture.model.LectureBlockIdentityStatistics;

/**
 * 
 * Initial date: 16 juin 2017<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class LecturesListDataModel extends DefaultFlexiTableDataModel<LectureBlockIdentityStatistics>
implements SortableFlexiTableDataModel<LectureBlockIdentityStatistics>, FlexiTableFooterModel,
		FilterableFlexiTableModel, ExportableFlexiTableDataModel {
	
	private final Translator translator;
	private AggregatedLectureBlocksStatistics totalStatistics;
	private final ExportableFlexiTableDataModel exportDelegate;

	private List<LectureBlockIdentityStatistics> backups;
	
	public LecturesListDataModel(ExportableFlexiTableDataModel exportDelegate, FlexiTableColumnModel columnModel, Translator translator) {
		super(columnModel);
		this.translator = translator;
		this.exportDelegate = exportDelegate;
	}

	@Override
	public void sort(SortKey orderBy) {
		SortableFlexiTableModelDelegate<LectureBlockIdentityStatistics> sorter
			= new SortableFlexiTableModelDelegate<>(orderBy, this, null);
		List<LectureBlockIdentityStatistics> views = sorter.sort();
		super.setObjects(views);
	}

	@Override
	public void filter(String searchString, List<FlexiTableFilter> filters) {
		if(filters != null && !filters.isEmpty() && filters.get(0) != null && !filters.get(0).isShowAll()) {
			List<LectureBlockIdentityStatistics> filteredRows = backups.stream()
						.filter(row -> row.getExplicitWarning() == LectureRateWarning.error)
						.collect(Collectors.toList());
			super.setObjects(filteredRows);
		} else {
			super.setObjects(backups);
		}
	}

	@Override
	public MediaResource export(FlexiTableComponent ftC) {
		return exportDelegate.export(ftC);
	}

	@Override
	public Object getValueAt(int row, int col) {
		LectureBlockIdentityStatistics stats = getObject(row);
		return getValueAt(stats, col);
	}

	@Override
	public Object getValueAt(LectureBlockIdentityStatistics row, int col) {
		if(col >= 0 && col < StatsCols.values().length) {
			switch(StatsCols.values()[col]) {
				case id: return row.getIdentityKey();
				case plannedLectures: return positive(row.getTotalPersonalPlannedLectures());
				case attendedLectures: return positive(row.getTotalAttendedLectures());
				case unauthorizedAbsenceLectures:
				case absentLectures: return positive(row.getTotalAbsentLectures());
				case authorizedAbsenceLectures: return positive(row.getTotalAuthorizedAbsentLectures());
				case dispensedLectures: return positive(row.getTotalDispensationLectures());
				case currentRate: return row.getAttendanceRate();
				case progress: return row;
				case rateWarning: return row;
			}
		}
		
		int propPos = col - LecturesListController.USER_PROPS_OFFSET;
		return row.getIdentityProp(propPos);
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
				case dispensedLectures: return positive(totalStatistics.getDispensedLectures());
				default: return null;
			}
		}
		return null;
	}

	private static final long positive(long pos) {
		return pos < 0 ? 0 : pos;
	}
	
	public void setObjects(List<LectureBlockIdentityStatistics> objects, AggregatedLectureBlocksStatistics totalStatistics) {
		super.setObjects(objects);
		this.totalStatistics = totalStatistics;
		backups = objects;
	}
	
	@Override
	public DefaultFlexiTableDataModel<LectureBlockIdentityStatistics> createCopyWithEmptyList() {
		return new LecturesListDataModel(exportDelegate, getTableColumnModel(), translator);
	}
	
	public enum StatsCols implements FlexiSortableColumnDef {
		id("table.header.id"),
		plannedLectures("table.header.planned.lectures"),
		attendedLectures("table.header.attended.lectures"),
		absentLectures("table.header.absent.lectures"),
		unauthorizedAbsenceLectures("table.header.unauthorized.absence"),
		authorizedAbsenceLectures("table.header.authorized.absence"),
		dispensedLectures("table.header.dispensation"),
		currentRate("table.header.attended.current.rate"),
		progress("table.header.progress"),
		rateWarning("table.header.rate.warning");
		
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
