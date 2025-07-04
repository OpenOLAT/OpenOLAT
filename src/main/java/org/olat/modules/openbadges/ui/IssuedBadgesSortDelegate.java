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

import java.util.Comparator;
import java.util.List;
import java.util.Locale;

import org.olat.core.commons.persistence.SortKey;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SortableFlexiTableDataModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SortableFlexiTableModelDelegate;
import org.olat.core.gui.translator.Translator;
import org.olat.modules.openbadges.OpenBadgesManager;

/**
 * Initial date: 2025-07-04<br>
 *
 * @author cpfranger, christoph.pfranger@frentix.com, <a href="https://www.frentix.com">https://www.frentix.com</a>
 */
public class IssuedBadgesSortDelegate extends SortableFlexiTableModelDelegate<IssuedBadgeRow> {

	private final OpenBadgesManager openBadgesManager;
	private final Translator translator;

	public IssuedBadgesSortDelegate(SortKey orderBy, SortableFlexiTableDataModel<IssuedBadgeRow> tableModel, Locale locale,
									OpenBadgesManager openBadgesManager, Translator translator) {
		super(orderBy, tableModel, locale);
		this.openBadgesManager = openBadgesManager;
		this.translator = translator;
	}

	@Override
	protected void sort(List<IssuedBadgeRow> rows) {
		if (IssuedBadgesTableModel.COLS[getColumnIndex()] == IssuedBadgesTableModel.IssuedBadgeCols.status) {
			rows.sort(Comparator.comparing(this::statusColumnString));
			return;
		}
		super.sort(rows);
	}
	
	private String statusColumnString(IssuedBadgeRow row) {
		if (openBadgesManager.isBadgeAssertionExpired(row.getBadgeAssertion())) {
			return translator.translate("expired");
		} else {
			return translator.translate("assertion.status." + row.getBadgeAssertion().getStatus().name());
		}
	}
}
