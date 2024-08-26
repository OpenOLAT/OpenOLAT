/**
 * <a href="https://www.openolat.org">
 * OpenOLAT - Online Learning and Training</a><br>
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); <br>
 * you may not use this file except in compliance with the License.<br>
 * You may obtain a copy of the License at the
 * <a href="https://www.apache.org/licenses/LICENSE-2.0">Apache homepage</a>
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
package org.olat.admin.user.projects;

import java.util.List;
import java.util.Locale;

import org.olat.core.commons.persistence.SortKey;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiTableDataModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiSortableColumnDef;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SortableFlexiTableDataModel;

/**
 * Initial date: Aug 23, 2024
 *
 * @author skapoor, sumit.kapoor@frentix.com, <a href="https://www.frentix.com">https://www.frentix.com</a>
 */
public class ProjectsOverviewDataModel extends DefaultFlexiTableDataModel<ProjectsOverviewRow>
		implements SortableFlexiTableDataModel<ProjectsOverviewRow> {

	private static final ProjectOverviewCols[] COLS = ProjectOverviewCols.values();

	private final Locale locale;

	public ProjectsOverviewDataModel(FlexiTableColumnModel columnModel, Locale locale) {
		super(columnModel);
		this.locale = locale;
	}

	@Override
	public void sort(SortKey sortKey) {
		List<ProjectsOverviewRow> rows = new ProjectsOverviewRowSortDelegate(sortKey, this, locale).sort();
		super.setObjects(rows);
	}

	@Override
	public Object getValueAt(ProjectsOverviewRow row, int col) {
		return switch (COLS[col]) {
			case title -> row.getTitle();
			case externalRef -> row.getExternalRef();
			case status -> row;
			case roles -> row.getRoles();
			case regDate -> row.getRegistrationDate();
			case lastVisitDate -> row.getLastActivityDate();
			case removes -> true;
		};
	}

	@Override
	public Object getValueAt(int row, int col) {
		ProjectsOverviewRow projectRow = getObject(row);
		return getValueAt(projectRow, col);
	}

	public enum ProjectOverviewCols implements FlexiSortableColumnDef {
		title("table.header.proj.overview.title"),
		externalRef("table.header.proj.overview.externalRef"),
		status("table.header.proj.overview.status"),
		roles("table.header.proj.overview.roles"),
		regDate("table.header.proj.overview.reg.date"),
		lastVisitDate("table.header.proj.overview.last.visit.date"),
		removes("table.header.proj.overview.removes");


		private final String i18nKey;

		ProjectOverviewCols(String i18nKey) {
			this.i18nKey = i18nKey;
		}

		@Override
		public boolean sortable() {
			return this != removes;
		}

		@Override
		public String sortKey() {
			return name();
		}

		@Override
		public String i18nHeaderKey() {
			return i18nKey;
		}
	}
}
