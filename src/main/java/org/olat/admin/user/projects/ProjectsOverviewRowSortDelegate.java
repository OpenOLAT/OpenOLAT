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

import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

import org.olat.core.commons.persistence.SortKey;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SortableFlexiTableDataModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SortableFlexiTableModelDelegate;
import org.olat.modules.project.ui.ProjProjectDataModel;

/**
 * Initial date: Aug 23, 2024
 *
 * @author skapoor, sumit.kapoor@frentix.com, <a href="https://www.frentix.com">https://www.frentix.com</a>
 */
public class ProjectsOverviewRowSortDelegate extends SortableFlexiTableModelDelegate<ProjectsOverviewRow> {

	public ProjectsOverviewRowSortDelegate(SortKey orderBy, SortableFlexiTableDataModel<ProjectsOverviewRow> tableModel, Locale locale) {
		super(orderBy, tableModel, locale);
	}

	@Override
	protected void sort(List<ProjectsOverviewRow> rows) {
		int columnIndex = getColumnIndex();
		ProjProjectDataModel.ProjectCols column = ProjProjectDataModel.ProjectCols.values()[columnIndex];
		if (Objects.requireNonNull(column) == ProjProjectDataModel.ProjectCols.status) {
			rows.sort(new StatusComparator());
		} else {
			super.sort(rows);
		}
	}

	private class StatusComparator implements Comparator<ProjectsOverviewRow> {
		@Override
		public int compare(ProjectsOverviewRow t1, ProjectsOverviewRow t2) {
			return compareString(t1.getTranslatedStatus(), t2.getTranslatedStatus());
		}
	}
}
