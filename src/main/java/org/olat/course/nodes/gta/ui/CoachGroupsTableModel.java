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
package org.olat.course.nodes.gta.ui;

import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiTableDataModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.group.BusinessGroup;

/**
 * 
 * Initial date: 11.03.2015<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class CoachGroupsTableModel extends DefaultFlexiTableDataModel<BusinessGroup> {
	
	public CoachGroupsTableModel(FlexiTableColumnModel columnModel) {
		super(columnModel);
	}

	@Override
	public DefaultFlexiTableDataModel<BusinessGroup> createCopyWithEmptyList() {
		return new CoachGroupsTableModel(getTableColumnModel());
	}

	@Override
	public Object getValueAt(int row, int col) {
		BusinessGroup group = getObject(row);
		switch(CGCols.values()[col]) {
			case name: return group.getName();
			default: return "ERROR";
		}
	}
	
	public enum CGCols {
		name("table.header.group.name");
		
		private final String i18nKey;
		
		private CGCols(String i18nKey) {
			this.i18nKey = i18nKey;
		}
		
		public String i18nKey() {
			return i18nKey;
		}
	}
}
