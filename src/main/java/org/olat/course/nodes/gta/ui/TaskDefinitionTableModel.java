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
package org.olat.course.nodes.gta.ui;

import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiTableDataModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.util.Formatter;
import org.olat.course.nodes.gta.model.TaskDefinition;

/**
 * 
 * Initial date: 25.02.2015<br>
 * @author srosse, stephane.rosse@frentix.com, https://www.frentix.com
 *
 */
public class TaskDefinitionTableModel extends DefaultFlexiTableDataModel<TaskDefinitionRow> {

	public TaskDefinitionTableModel(FlexiTableColumnModel columnModel) {
		super(columnModel);
	}

	@Override
	public Object getValueAt(int row, int col) {
		TaskDefinitionRow taskDefRow = getObject(row);
		TaskDefinition taskDef = taskDefRow.taskDefinition();
		return switch (TDCols.values()[col]) {
			case title -> taskDef.getTitle();
			case desc -> Formatter.truncate(taskDef.getDescription(), 255);
			case file -> taskDefRow.documentLink();
			case author -> taskDefRow.author();
			case toolsLink -> taskDefRow.toolsLink();
			default -> "ERROR";
		};
	}

	public enum TDCols {
		title("task.title"),
		desc("table.header.desc"),
		file("task.file"),
		author("table.header.author"),
		edit("table.header.metadata"),
		toolsLink("table.header.action");
		
		private final String i18nKey;
	
		private TDCols(String i18nKey) {
			this.i18nKey = i18nKey;
		}
		
		public String i18nKey() {
			return i18nKey;
		}
	}
}
