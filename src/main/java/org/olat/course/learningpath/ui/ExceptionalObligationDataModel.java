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
package org.olat.course.learningpath.ui;

import java.util.Locale;

import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiTableDataModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiColumnDef;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;

/**
 * 
 * Initial date: 1 Sep 2021<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class ExceptionalObligationDataModel extends DefaultFlexiTableDataModel<ExceptionalObligationRow> {
	
	private static final ExceptionalObligationCols[] COLS = ExceptionalObligationCols.values();
	private final Locale locale;
	
	public ExceptionalObligationDataModel(FlexiTableColumnModel columnsModel, Locale locale) {
		super(columnsModel);
		this.locale = locale;
	}

	@Override
	public Object getValueAt(int row, int col) {
		ExceptionalObligationRow reason = getObject(row);
		return getValueAt(reason, col);
	}

	public Object getValueAt(ExceptionalObligationRow row, int col) {
		switch(COLS[col]) {
			case name: return row.getName();
			case type: return row.getType();
			case mandatory: return row.getMandatoryEl();
			case optional: return row.getOptionalEl();
			case excluded: return row.getExcludedEl();
			case delete: return row.getDeleteLink();
			default: return null;
		}
	}

	@Override
	public DefaultFlexiTableDataModel<ExceptionalObligationRow> createCopyWithEmptyList() {
		return new ExceptionalObligationDataModel(getTableColumnModel(), locale);
	}
	
	public enum ExceptionalObligationCols implements FlexiColumnDef {
		name("exceptional.obligation.name"),
		type("exceptional.obligation.type"),
		mandatory("exceptional.obligation.mandatory"),
		optional("exceptional.obligation.optional"),
		excluded("exceptional.obligation.excluded"),
		delete("exceptional.obligation.delete");
		
		private final String i18nKey;
		
		private ExceptionalObligationCols(String i18nKey) {
			this.i18nKey = i18nKey;
		}
		
		@Override
		public String i18nHeaderKey() {
			return i18nKey;
		}
	}
}
