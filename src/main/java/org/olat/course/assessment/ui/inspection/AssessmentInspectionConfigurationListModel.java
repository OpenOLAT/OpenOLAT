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
package org.olat.course.assessment.ui.inspection;

import java.util.List;
import java.util.Locale;

import org.olat.core.commons.persistence.SortKey;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiTableDataModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiSortableColumnDef;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SortableFlexiTableDataModel;
import org.olat.core.util.StringHelper;
import org.olat.course.assessment.AssessmentInspectionConfiguration;

/**
 * 
 * Initial date: 12 déc. 2023<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class AssessmentInspectionConfigurationListModel extends DefaultFlexiTableDataModel<AssessmentInspectionConfigurationRow>
implements SortableFlexiTableDataModel<AssessmentInspectionConfigurationRow> {
	
	private static final InspectionCols[] COLS = InspectionCols.values();
	
	private final Locale locale;

	public AssessmentInspectionConfigurationListModel(FlexiTableColumnModel columnsModel, Locale locale) {
		super(columnsModel);
		this.locale = locale;
	}

	@Override
	public void sort(SortKey orderBy) {
		if(orderBy != null) {
			List<AssessmentInspectionConfigurationRow> rows = new AssessmentInspectionConfigurationListModelSortDelegate(orderBy, this, locale).sort();
			super.setObjects(rows);
		}
	}
	
	@Override
	public Object getValueAt(int row, int col) {
		AssessmentInspectionConfigurationRow config = getObject(row);
		return getValueAt(config, col);
	}

	@Override
	public Object getValueAt(AssessmentInspectionConfigurationRow row, int col) {
		AssessmentInspectionConfiguration configuration = row.getConfiguration();
		switch(COLS[col]) {
			case name: return configuration.getName();
			case duration: return getDurationInMinutes(configuration);
			case resultsDisplay: return row;
			case ips: return getIps(configuration);
			case seb: return Boolean.valueOf(configuration.isSafeExamBrowser());
			case usages: return row.getUsage();
			case tools: return row.getToolsButton();
			default: return "ERROR";
		}
	}
	
	private Integer getDurationInMinutes(AssessmentInspectionConfiguration configuration) {
		int durationInSeconds = configuration.getDuration();
		return durationInSeconds / 60;
	}
	
	private String getIps(AssessmentInspectionConfiguration configuration) {
		if(configuration.isRestrictAccessIps() && StringHelper.containsNonWhitespace(configuration.getIpList())) {
			return configuration.getIpList();
		}
		return null;
	}

	public enum InspectionCols implements FlexiSortableColumnDef {
		name("table.header.name"),
		duration("table.header.duration"),
		resultsDisplay("table.header.results.display"),
		ips("table.header.ips"),
		seb("table.header.seb"),
		usages("table.header.usages"),
		tools("table.header.tools");
		
		private final String i18nKey;
		
		private InspectionCols(String i18nKey) {
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
