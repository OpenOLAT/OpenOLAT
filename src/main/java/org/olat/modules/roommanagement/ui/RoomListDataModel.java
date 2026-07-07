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
 * Initial date: 5 Jun 2026<br>
 * @author cpfranger, christoph.pfranger@frentix.com, <a href="https://www.frentix.com">https://www.frentix.com</a>
 */
public class RoomListDataModel extends DefaultFlexiTableDataModel<RoomRow>
		implements SortableFlexiTableDataModel<RoomRow> {

	private static final RoomCols[] COLS = RoomCols.values();
	private final Locale locale;

	public RoomListDataModel(FlexiTableColumnModel columnModel, Locale locale) {
		super(columnModel);
		this.locale = locale;
	}

	@Override
	public void sort(SortKey orderBy) {
		List<RoomRow> rows = new SortableFlexiTableModelDelegate<>(orderBy, this, locale).sort();
		super.setObjects(rows);
	}

	@Override
	public Object getValueAt(int row, int col) {
		return getValueAt(getObject(row), col);
	}

	@Override
	public Object getValueAt(RoomRow row, int col) {
		return switch (COLS[col]) {
			case reference -> row.getReferenceLink();
			case description -> row.getRoom().getDescription();
			case status -> row.getRoom().getStatus();
			case seats -> row.getRoom().getSeats();
			case additionalInfo -> RoomUIHelper.truncateColumnInfoText(row.getRoom().getRoomInfo());
			case adminInfo -> RoomUIHelper.truncateColumnInfoText(row.getRoom().getAdminInfo());
			case building -> row;
			case occupancyRate -> {
				int pct = row.getOccupancyRatePercent();
				yield pct < 0 ? null : pct + "%";
			}
			case nextEvent -> row.getNextEvent();
			case calendarIcon -> row.getCalendarIconLink();
			case tools -> row.getToolsLink();
		};
	}

	public enum RoomCols implements FlexiSortableColumnDef {
		reference("room.col.reference"),
		description("room.col.description"),
		status("room.col.status"),
		seats("room.col.seats"),
		additionalInfo("room.col.additional.info"),
		adminInfo("room.col.admin.info"),
		building("room.col.building"),
		occupancyRate("room.col.occupancy.rate"),
		nextEvent("room.col.next.event") {
			@Override
			public boolean sortable() {
				return false;
			}
		},
		calendarIcon("room.col.calendar") {
			@Override
			public String iconHeader() {
				return "o_icon o_icon_calendar";
			}

			@Override
			public boolean sortable() {
				return false;
			}
		},
		tools("action.more");

		private final String i18nKey;

		RoomCols(String i18nKey) {
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
