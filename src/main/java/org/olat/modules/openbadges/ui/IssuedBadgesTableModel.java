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
package org.olat.modules.openbadges.ui;

import java.util.List;
import java.util.Locale;

import org.olat.core.commons.persistence.SortKey;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiTableDataModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiSortableColumnDef;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SortableFlexiTableDataModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SortableFlexiTableModelDelegate;
import org.olat.core.gui.translator.Translator;

/**
 * Initial date: 2023-06-28<br>
 *
 * @author cpfranger, christoph.pfranger@frentix.com, <a href="https://www.frentix.com">https://www.frentix.com</a>
 */
public class IssuedBadgesTableModel extends DefaultFlexiTableDataModel<IssuedBadgeRow>
		implements SortableFlexiTableDataModel<IssuedBadgeRow> {

	private static final IssuedBadgeCols[] COLS = IssuedBadgeCols.values();

	private final Translator translator;
	private final Locale locale;

	public IssuedBadgesTableModel(FlexiTableColumnModel columnModel, Translator translator, Locale locale) {
		super(columnModel);
		this.translator = translator;
		this.locale = locale;
	}

	@Override
	public Object getValueAt(int row, int col) {
		IssuedBadgeRow issuedBadgeRow = getObject(row);
		return getValueAt(issuedBadgeRow, col);
	}

	@Override
	public Object getValueAt(IssuedBadgeRow row, int col) {
		return switch (COLS[col]) {
			case image -> row.getBadgeAssertion().getBakedImage();
			case title -> row.getName();
			case status -> translator.translate("assertion.status." + row.getBadgeAssertion().getStatus().name());
			case issuer -> row.getBadgeAssertion().getBadgeClass().getIssuerDisplayString();
			case issuedOn -> row.getBadgeAssertion().getIssuedOn();
			case tools -> row.getToolLink();
		};
	}

	@Override
	public void sort(SortKey sortKey) {
		if (sortKey != null) {
			List<IssuedBadgeRow> rows = new SortableFlexiTableModelDelegate<>(sortKey, this, locale).sort();
			super.setObjects(rows);
		}
	}

	public enum IssuedBadgeCols implements FlexiSortableColumnDef {
		image("form.image"),
		title("form.name"),
		status("form.status"),
		issuer("class.issuer"),
		issuedOn("form.issued.on"),
		tools("table.header.actions");

		private final String i18nKey;

		IssuedBadgeCols(String i18nKey) {
			this.i18nKey = i18nKey;
		}

		@Override
		public String i18nHeaderKey() {
			return i18nKey;
		}

		@Override
		public boolean sortable() {
			return this != image;
		}

		@Override
		public String sortKey() {
			return name();
		}
	}

	public enum IssuedBadgesFilter {
		STATUS("form.status"),
		ISSUER("class.issuer");

		private final String i18nKey;

		IssuedBadgesFilter(String i18nKey) {
			this.i18nKey = i18nKey;
		}

		public String getI18nKey() {
			return i18nKey;
		}
	}
}
