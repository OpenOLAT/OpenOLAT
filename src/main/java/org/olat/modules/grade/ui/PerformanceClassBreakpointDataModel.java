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
package org.olat.modules.grade.ui;

import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiTableDataModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiColumnDef;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;

/**
 * 
 * Initial date: 25 Feb 2022<br
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.comm
 *
 */
public class PerformanceClassBreakpointDataModel extends DefaultFlexiTableDataModel<PerformanceClassBreakpointRow> {
	
	private static final PerformanceClassBreakpointCols[] COLS = PerformanceClassBreakpointCols.values();
	
	
	public PerformanceClassBreakpointDataModel(FlexiTableColumnModel columnsModel) {
		super(columnsModel);
	}

	@Override
	public Object getValueAt(int row, int col) {
		PerformanceClassBreakpointRow performanceClassBreakpointRow = getObject(row);
		return getValueAt(performanceClassBreakpointRow, col);
	}

	private Object getValueAt(PerformanceClassBreakpointRow row, int col) {
		switch(COLS[col]) {
			case position: return row.getPerformanceClass();
			case name: return row.getTranslatedName();
			case lowerBound: return row.getLowerBoundEl();
			default: return null;
		}
	}
	
	public enum PerformanceClassBreakpointCols implements FlexiColumnDef {
		position("performance.class.breakpoint.postion"),
		name("performance.class.breakpoint.name"),
		lowerBound("performance.class.breakpoint.lower.bound");
		
		private final String i18nKey;
		
		private PerformanceClassBreakpointCols(String i18nKey) {
			this.i18nKey = i18nKey;
		}
		
		@Override
		public String i18nHeaderKey() {
			return i18nKey;
		}
	}
}
