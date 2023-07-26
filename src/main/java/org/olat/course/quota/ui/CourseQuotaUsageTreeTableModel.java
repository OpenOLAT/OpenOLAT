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
package org.olat.course.quota.ui;

import java.util.List;
import java.util.Locale;

import org.olat.core.gui.components.form.flexible.elements.FlexiTableFilter;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiTreeTableDataModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.util.Formatter;
import org.olat.course.nodes.CourseNodeConfiguration;
import org.olat.course.nodes.CourseNodeFactory;

/**
 * Initial date: Jul 04, 2023
 *
 * @author Sumit Kapoor, sumit.kapoor@frentix.com, <a href="https://www.frentix.com">https://www.frentix.com</a>
 */
public class CourseQuotaUsageTreeTableModel extends DefaultFlexiTreeTableDataModel<CourseQuotaUsageRow> {

	private static final CourseQuotaUsageCols[] COLS = CourseQuotaUsageCols.values();
	
	Locale locale;

	protected CourseQuotaUsageTreeTableModel(FlexiTableColumnModel columnModel, Locale locale) {
		super(columnModel);
		this.locale = locale;
	}

	@Override
	public boolean hasChildren(int row) {
		CourseQuotaUsageRow level = getObject(row);
		return level != null && level.getNumOfChildren() != 0;
	}

	@Override
	public Object getValueAt(int row, int col) {
		CourseQuotaUsageRow level = getObject(row);
		return getValueAt(level, col);
	}

	private Object getValueAt(CourseQuotaUsageRow row, int col) {
		if (col >= 0 && col < CourseQuotaUsageCols.values().length) {
			switch (COLS[col]) {
				case resource -> {
					return row.getResource();
				}
				case type -> {
					CourseNodeConfiguration cnc = CourseNodeFactory.getInstance().getCourseNodeConfigurationEvenForDisabledBB(row.getType());
					return row.getTypeCssIcon() + " " + cnc.getLinkText(locale);
				}
				case external -> {
					return row.getExternal();
				}
				case numOfFiles -> {
					return row.getNumOfFiles();
				}
				case totalUsedSize -> {
					return row.getTotalUsedSize() != null
							? Formatter.formatKBytes(row.getTotalUsedSize())
							: null;
				}
				case quota -> {
					return row.getQuota();
				}
				case curUsed -> {
					return row.getCurUsed();
				}
				case editQuota -> {
					return row.getEditQuota();
				}
				case displayRss -> {
					return row.getDisplayRss();
				}
			}
		}
		return "ERROR";
	}

	@Override
	public void filter(String searchString, List<FlexiTableFilter> filters) {
		// No filters needed in this use case
	}
}
