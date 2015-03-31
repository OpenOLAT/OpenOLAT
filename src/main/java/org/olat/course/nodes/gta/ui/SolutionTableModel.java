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
import org.olat.course.nodes.gta.model.Solution;

/**
 * 
 * Initial date: 25.02.2015<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class SolutionTableModel extends DefaultFlexiTableDataModel<Solution> {
	
	public SolutionTableModel(FlexiTableColumnModel columnModel) {
		super(columnModel);
	}

	@Override
	public DefaultFlexiTableDataModel<Solution> createCopyWithEmptyList() {
		return new SolutionTableModel(getTableColumnModel());
	}

	@Override
	public Object getValueAt(int row, int col) {
		Solution taskDef = getObject(row);
		switch(SolCols.values()[col]) {
			case title: return taskDef.getTitle();
			case file: return taskDef.getFilename();
			default: return "ERROR";
		}
	}

	public enum SolCols {
		title("task.title"),
		file("task.file");
		
		private final String i18nKey;
	
		private SolCols(String i18nKey) {
			this.i18nKey = i18nKey;
		}
		
		public String i18nKey() {
			return i18nKey;
		}
	}
}
