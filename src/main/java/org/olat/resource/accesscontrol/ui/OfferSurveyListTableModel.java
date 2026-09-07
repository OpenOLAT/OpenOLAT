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
import org.olat.resource.accesscontrol.Offer;

/**
 *
 * Initial date: 1 Sep 2026<br>
 * @author uhensler, urs.hensler@frentix.com, https://www.frentix.com
 *
 */
public class OfferSurveyListTableModel extends DefaultFlexiTableDataModel<OfferSurveyRow>
		implements SortableFlexiTableDataModel<OfferSurveyRow> {

	private static final OfferSurveyCols[] COLS = OfferSurveyCols.values();

	private final Locale locale;
	private final List<Offer> offers;

	public OfferSurveyListTableModel(FlexiTableColumnModel columnsModel, Locale locale, List<Offer> offers) {
		super(columnsModel);
		this.locale = locale;
		this.offers = offers;
	}

	@Override
	public void sort(SortKey orderBy) {
		List<OfferSurveyRow> rows = new SortableFlexiTableModelDelegate<>(orderBy, this, locale).sort();
		super.setObjects(rows);
	}

	@Override
	public Object getValueAt(int row, int col) {
		OfferSurveyRow surveyRow = getObject(row);
		return getValueAt(surveyRow, col);
	}

	@Override
	public Object getValueAt(OfferSurveyRow row, int col) {
		if (col >= COLS.length) {
			Offer offer = offers.get(col - COLS.length);
			Integer pos = row.getPosition(offer.getKey());
			return pos != null ? pos + 1 : null;
		}
		switch (COLS[col]) {
		case title: return row.getTitle();
		case reference: return row.getReference();
		case stepName: return row.getStepNameLink() != null ? row.getStepNameLink() : row.getStepName();
		case open: return row.getOpenCount();
		case completed: return row.getCompletedCount();
		case canceled: return row.getCanceledCount();
		case tools: return row.getToolsLink();
		default: return null;
		}
	}

	public enum OfferSurveyCols implements FlexiSortableColumnDef {
		title("offer.survey.title"),
		reference("offer.survey.reference"),
		stepName("offer.survey.step.name"),
		open("offer.survey.open.count"),
		completed("offer.survey.completed"),
		canceled("offer.survey.canceled"),
		tools("action.more");

		private final String i18nKey;

		private OfferSurveyCols(String i18nKey) {
			this.i18nKey = i18nKey;
		}

		@Override
		public boolean sortable() {
			return this != stepName && this != tools;
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
