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
package org.olat.course.editor.importnodes;

import java.util.List;
import java.util.Locale;

import org.olat.core.gui.components.form.flexible.elements.FlexiTableFilter;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiTreeTableDataModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiColumnDef;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;

/**
 * 
 * Initial date: 5 oct. 2021<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class ConfigurationCourseNodesTableModel extends DefaultFlexiTreeTableDataModel<ConfigurationCourseNodeRow> {
	
	private static final ConfigurationCols[] COLS = ConfigurationCols.values();
	
	private final Locale locale;
	
	public ConfigurationCourseNodesTableModel(FlexiTableColumnModel columnsModel, Locale locale) {
		super(columnsModel);
		this.locale = locale;
	}

	@Override
	public void filter(String searchString, List<FlexiTableFilter> filters) {
		// 
	}
	
	@Override
	public Object getValueAt(int row, int col) {
		ConfigurationCourseNodeRow node = getObject(row);
		switch(COLS[col]) {
			case node: return node;
			case resource: return node.getConfigurationItem();
			case reminder: return Integer.valueOf(node.getNumOfReminders());
			case messages: return node.getMessage();
			default: return "ERROR";
		}
	}
	
	@Override
	public boolean hasChildren(int row) {
		ConfigurationCourseNodeRow node = getObject(row);
		int numOfChildren = node.getEditorTreeNode().getChildCount();
		return numOfChildren > 0;
	}
	
	@Override
	public ConfigurationCourseNodesTableModel createCopyWithEmptyList() {
		return new ConfigurationCourseNodesTableModel(getTableColumnModel(), locale);
	}
	
	public enum ConfigurationCols implements FlexiColumnDef {
		node("table.header.node"),
		resource("table.header.resource"),
		reminder("table.header.reminder"),
		messages("table.header.messages");
		
		private final String i18nKey;
		
		private ConfigurationCols(String i18nKey) {
			this.i18nKey = i18nKey;
		}
		
		@Override
		public String i18nHeaderKey() {
			return i18nKey;
		}
	}

}
