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
package org.olat.resource.accesscontrol.ui;

import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiTableDataModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiColumnDef;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;

/**
 *
 * Initial date: 2 Sep 2026<br>
 * @author uhensler, urs.hensler@frentix.com, https://www.frentix.com
 *
 */
public class OrderFormListTableModel extends DefaultFlexiTableDataModel<OrderFormRow> {

	private static final OrderFormCols[] COLS = OrderFormCols.values();

	public OrderFormListTableModel(FlexiTableColumnModel columnsModel) {
		super(columnsModel);
	}

	@Override
	public Object getValueAt(int row, int col) {
		OrderFormRow formRow = getObject(row);
		return getValueAt(formRow, col);
	}

	public Object getValueAt(OrderFormRow row, int col) {
		switch (COLS[col]) {
		case title: return row.getTitle();
		case reference: return row.getReference();
		case stepName: return row.getStepName();
		case order: return row.getOrderNr();
		case status: return row.getStatus();
		case submissionDate: return row.getSubmissionDate();
		case view: return row.getViewLink();
		case tools: return row.getToolsLink();
		default: return null;
		}
	}

	public enum OrderFormCols implements FlexiColumnDef {
		title("offer.survey.title"),
		reference("offer.survey.reference"),
		stepName("offer.survey.step.name"),
		order("offer.survey.participation.order"),
		status("offer.survey.participation.status"),
		submissionDate("offer.survey.participation.submission.date"),
		view("offer.survey.participation.view.form"),
		tools("action.more");

		private final String i18nKey;

		private OrderFormCols(String i18nKey) {
			this.i18nKey = i18nKey;
		}

		@Override
		public String i18nHeaderKey() {
			return i18nKey;
		}
	}

}
