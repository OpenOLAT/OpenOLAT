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
import org.olat.modules.forms.model.xml.Choice;

/**
 * 
 * Initial date: 11.04.2018<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class ChoiceDataModel extends DefaultFlexiTableDataModel<Choice> {

	public ChoiceDataModel(FlexiTableColumnModel columnModel) {
		super(columnModel);
	}

	@Override
	public DefaultFlexiTableDataModel<Choice> createCopyWithEmptyList() {
		return new ChoiceDataModel(getTableColumnModel());
	}

	@Override
	public Object getValueAt(int row, int col) {
		Choice choice = getObject(row);
		switch (ChoiceCols.values()[col]) {
			case up:
				return row == 0 ? Boolean.FALSE : Boolean.TRUE;
			case down:
				return row >= (getRowCount() - 1) ? Boolean.FALSE : Boolean.TRUE;
			case value:
				return choice.getValue();
			default:
				return "";
		}
	}

	enum ChoiceCols implements FlexiColumnDef {
		up("choice.up"),
		down("choice.down"),
		value("choice.value"),
		edit("choice.edit"),
		delete("choice.delete");
		
		private final String i18nKey;

		private ChoiceCols(String i18nKey) {
			this.i18nKey = i18nKey;
		}
		
		@Override
		public String i18nHeaderKey() {
			return i18nKey;
		}
	}
}
