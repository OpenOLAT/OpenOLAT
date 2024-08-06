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
package org.olat.modules.ceditor.ui;

import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiTableDataModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;

/**
 * Initial date: 2024-03-22<br>
 *
 * @author cpfranger, christoph.pfranger@frentix.com, <a href="https://www.frentix.com">https://www.frentix.com</a>
 */
public class QuestionModel extends DefaultFlexiTableDataModel<QuestionRow> {

	private static final QuestionColumns[] COLS = QuestionColumns.values();

	public QuestionModel(FlexiTableColumnModel columnModel) {
		super(columnModel);
	}

	@Override
	public Object getValueAt(int row, int col) {
		QuestionRow questionRow = getObject(row);

		return switch (COLS[col]) {
			case up -> row > 0;
			case down -> row < (getRowCount() - 1);
			case title -> questionRow.getTitle();
			case type -> questionRow.getType();
			case tools -> questionRow.getToolLink();
		};
	}

	public enum QuestionColumns {
		up("quiz.up"),
		down("quiz.down"),
		title("quiz.title"),
		type("quiz.type"),
		tools("quiz.tools");

		private final String i18nKey;

		QuestionColumns(String i18nKey) {
			this.i18nKey = i18nKey;
		}

		public String getI18nKey() {
			return i18nKey;
		}
	}
}
