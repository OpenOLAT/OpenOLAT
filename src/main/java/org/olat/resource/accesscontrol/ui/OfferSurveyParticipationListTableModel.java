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

import java.util.List;
import java.util.Locale;

import org.olat.core.commons.persistence.SortKey;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiTableDataModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiSortableColumnDef;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SortableFlexiTableDataModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SortableFlexiTableModelDelegate;

/**
 *
 * Initial date: 2 Sep 2026<br>
 * @author uhensler, urs.hensler@frentix.com, https://www.frentix.com
 *
 */
public class OfferSurveyParticipationListTableModel extends DefaultFlexiTableDataModel<OfferSurveyParticipationRow>
		implements SortableFlexiTableDataModel<OfferSurveyParticipationRow> {

	private static final OfferSurveyParticipationCols[] COLS = OfferSurveyParticipationCols.values();

	private final Locale locale;

	public OfferSurveyParticipationListTableModel(FlexiTableColumnModel columnsModel, Locale locale) {
		super(columnsModel);
		this.locale = locale;
	}

	@Override
	public void sort(SortKey orderBy) {
		List<OfferSurveyParticipationRow> rows = new SortableFlexiTableModelDelegate<>(orderBy, this, locale).sort();
		super.setObjects(rows);
	}

	@Override
	public Object getValueAt(int row, int col) {
		OfferSurveyParticipationRow participationRow = getObject(row);
		return getValueAt(participationRow, col);
	}

	@Override
	public Object getValueAt(OfferSurveyParticipationRow row, int col) {
		if (col >= 0 && col < COLS.length) {
			return switch (COLS[col]) {
			case offer -> row.getOfferLabel();
			case order -> row.getOrderNr();
			case status -> row.getStatus();
			case submissionDate -> row.getSubmissionDate();
			case view -> row.getViewLink();
			case tools -> row.getToolsLink();
			};
		}
		int propPos = col - OfferSurveyParticipationListController.USER_PROPS_OFFSET;
		return row.getIdentityProp(propPos);
	}

	public enum OfferSurveyParticipationCols implements FlexiSortableColumnDef {
		offer("offer.survey.offer.column"),
		order("offer.survey.participation.order"),
		status("offer.survey.participation.status"),
		submissionDate("offer.survey.participation.submission.date"),
		view("offer.survey.participation.view.form"),
		tools("action.more");

		private final String i18nKey;

		private OfferSurveyParticipationCols(String i18nKey) {
			this.i18nKey = i18nKey;
		}

		@Override
		public boolean sortable() {
			return this != view && this != tools;
		}

		@Override
		public String sortKey() {
			return name();
		}

		@Override
		public String i18nHeaderKey() {
			return i18nKey;
		}
	}

}
