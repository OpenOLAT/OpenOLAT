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
package org.olat.modules.forms.ui;

import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiTableDataModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiColumnDef;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;

/**
 * 
 * Initial date: 23.05.2018<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class RubricsTotalDataModel extends DefaultFlexiTableDataModel<RubricsTotalRow> {

	public RubricsTotalDataModel(FlexiTableColumnModel columnModel) {
		super(columnModel);
	}

	@Override
	public Object getValueAt(int row, int col) {
		RubricsTotalRow rubricsTotalRow = getObject(row);
		switch (RubricsTotalCols.values()[col]) {
			case name: return rubricsTotalRow.getName();
			case avg: return EvaluationFormFormatter.formatDouble(rubricsTotalRow.getAverage());
			case scale: return rubricsTotalRow.getScale();
			default: return null;
		}
	}

	@Override
	public DefaultFlexiTableDataModel<RubricsTotalRow> createCopyWithEmptyList() {
		return new RubricsTotalDataModel(getTableColumnModel());
	}
	
	public enum RubricsTotalCols implements FlexiColumnDef {
		name("rubric.report.name.title"),
		avg("rubric.report.avg.title"),
		scale("rubric.report.scale.title");
		
		private final String i18nKey;
		
		private RubricsTotalCols(String i18nKey) {
			this.i18nKey = i18nKey;
		}
		
		@Override
		public String i18nHeaderKey() {
			return i18nKey;
		}
	}

}
