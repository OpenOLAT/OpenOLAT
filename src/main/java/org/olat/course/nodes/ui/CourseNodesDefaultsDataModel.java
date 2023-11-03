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
package org.olat.course.nodes.ui;

import java.util.List;
import java.util.Locale;

import org.olat.core.commons.persistence.SortKey;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiTableDataModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiSortableColumnDef;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SortableFlexiTableDataModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SortableFlexiTableModelDelegate;

/**
 * Initial date: Nov 01, 2023
 *
 * @author skapoor, sumit.kapoor@frentix.com, <a href="https://www.frentix.com">https://www.frentix.com</a>
 */
public class CourseNodesDefaultsDataModel extends DefaultFlexiTableDataModel<CourseNodeDefaultConfigRow>
		implements SortableFlexiTableDataModel<CourseNodeDefaultConfigRow> {

	private static final CourseNodesDefaultsCols[] COLS = CourseNodesDefaultsCols.values();
	private final Locale locale;

	public CourseNodesDefaultsDataModel(FlexiTableColumnModel columnModel, Locale locale) {
		super(columnModel);
		this.locale = locale;
	}

	@Override
	public Object getValueAt(int row, int col) {
		return getValueAt(getObject(row), col);
	}

	@Override
	public void sort(SortKey sortKey) {
		List<CourseNodeDefaultConfigRow> rows = new SortableFlexiTableModelDelegate<>(sortKey, this, locale).sort();
		super.setObjects(rows);
	}

	@Override
	public Object getValueAt(CourseNodeDefaultConfigRow row, int col) {
		return switch (COLS[col]) {
			case courseElement -> row.getCourseElement();
			case enabledToggle -> row.getEnabledToggle();
			case courseNodeManual -> row.getExternalManualLinkItem() != null ? row.getExternalManualLinkItem() : "";
			default -> "ERROR";
		};
	}

	public enum CourseNodesDefaultsCols implements FlexiSortableColumnDef {
		courseElement("course.node.defaults.element"),
		enabledToggle("course.node.defaults.toggle"),
		editConfig("course.node.defaults.edit.config"),
		courseNodeManual("course.node.defaults.element.manual");

		private final String i18nKey;

		CourseNodesDefaultsCols(String i18nKey) {
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
