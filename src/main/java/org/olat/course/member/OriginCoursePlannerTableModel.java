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
package org.olat.course.member;

import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiTableDataModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiColumnDef;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.course.member.model.OriginCoursePlannerRow;

/**
 * Initial date: 2025-12-22<br>
 *
 * @author cpfranger, christoph.pfranger@frentix.com, <a href="https://www.frentix.com">https://www.frentix.com</a>
 */
public class OriginCoursePlannerTableModel extends DefaultFlexiTableDataModel<OriginCoursePlannerRow> {
	
	private static final Cols[] COLS = Cols.values();
	
	public OriginCoursePlannerTableModel(FlexiTableColumnModel columnModel) {
		super(columnModel);
	}

	@Override
	public Object getValueAt(int row, int col) {
		OriginCoursePlannerRow rowObj = getObject(row);
		return getValueAt(rowObj, col);
	}
	
	private Object getValueAt(OriginCoursePlannerRow row, int col) {
		return switch (COLS[col]) {
			case role -> row.role();
			case element -> row.elementName();
			case extRef -> row.elementIdentifier();
			case product -> row.curriculumName();
			case created -> row.created();
		};
	}
	
	public enum Cols implements FlexiColumnDef {
		role("table.header.role"),
		element("table.header.cpl.element"),
		extRef("table.header.external.ref"),
		product("table.header.product"),
		created("table.header.created");

		private final String i18nHeaderKey;

		Cols(String i18nHeaderKey) {
			this.i18nHeaderKey = i18nHeaderKey;
		}

		@Override
		public String i18nHeaderKey() {
			return i18nHeaderKey;
		}
	}
}
