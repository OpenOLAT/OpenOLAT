/**
 * <a href="https://www.openolat.org">
 * OpenOLAT - Online Learning and Training</a><br>
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); <br>
 * you may not use this file except in compliance with the License.<br>
 * You may obtain a copy of the License at the
 * <a href="https://www.apache.org/licenses/LICENSE-2.0">Apache homepage</a>
 * <p>
 * Unless required by applicable law or agreed to in writing,<br>
 * software distributed under the License is distributed on an "AS IS" BASIS, <br>
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
 * See the License for the specific language governing permissions and <br>
 * limitations under the License.
 * <p>
 * Initial code contributed and copyrighted by<br>
 * frentix GmbH, https://www.frentix.com
 * <p>
 */
package org.olat.modules.roommanagement.ui;

import java.util.List;
import java.util.Locale;

import org.olat.core.commons.persistence.SortKey;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiTableDataModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiSortableColumnDef;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SortableFlexiTableDataModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SortableFlexiTableModelDelegate;

/**
 * Initial date: 1 Jun 2026<br>
 * @author cpfranger, christoph.pfranger@frentix.com, <a href="https://www.frentix.com">https://www.frentix.com</a>
 */
public class BuildingListDataModel extends DefaultFlexiTableDataModel<BuildingRow>
		implements SortableFlexiTableDataModel<BuildingRow> {

	private static final BuildingCols[] COLS = BuildingCols.values();
	private final Locale locale;

	public BuildingListDataModel(FlexiTableColumnModel columnModel, Locale locale) {
		super(columnModel);
		this.locale = locale;
	}

	@Override
	public void sort(SortKey orderBy) {
		List<BuildingRow> rows = new SortableFlexiTableModelDelegate<>(orderBy, this, locale).sort();
		super.setObjects(rows);
	}

	@Override
	public Object getValueAt(int row, int col) {
		return getValueAt(getObject(row), col);
	}

	@Override
	public Object getValueAt(BuildingRow row, int col) {
		return switch (COLS[col]) {
			case color -> row.getBuilding().getColorCss();
			case reference -> row.getReferenceLink();
			case description -> row.getBuilding().getDescription();
			case status -> row.getBuilding().getStatus();
			case address -> row.getAddressLink();
			case infoUrl -> row.getInfoUrlLink();
			case orgRestriction -> row.getOrganisations();
			case additionalInfo -> row.getBuilding().getInfo();
			case rooms -> row.getRoomsLink() != null ? row.getRoomsLink() : String.valueOf(row.getRoomCount());
		};
	}

	public enum BuildingCols implements FlexiSortableColumnDef {
		color("building.col.color"),
		reference("building.col.reference"),
		description("building.col.description"),
		status("building.col.status"),
		address("building.col.address"),
		infoUrl("building.col.info.url"),
		orgRestriction("building.col.org.restriction"),
		additionalInfo("building.col.additional.info"),
		rooms("building.col.rooms");

		private final String i18nKey;

		BuildingCols(String i18nKey) {
			this.i18nKey = i18nKey;
		}

		@Override
		public String i18nHeaderKey() {
			return i18nKey;
		}

		@Override
		public boolean sortable() {
			return true;
		}

		@Override
		public String sortKey() {
			return name();
		}
	}
}
