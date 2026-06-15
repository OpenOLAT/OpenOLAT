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
import org.olat.core.util.StringHelper;
import org.olat.modules.roommanagement.Room;

/**
 * Initial date: 12 Jun 2026<br>
 * @author cpfranger, christoph.pfranger@frentix.com, <a href="https://www.frentix.com">https://www.frentix.com</a>
 */
public class RoomSchedulingDataModel extends DefaultFlexiTableDataModel<RoomSchedulingRow>
		implements SortableFlexiTableDataModel<RoomSchedulingRow> {

	private final Locale locale;

	public RoomSchedulingDataModel(FlexiTableColumnModel columnsModel, Locale locale) {
		super(columnsModel);
		this.locale = locale;
	}

	@Override
	public void sort(SortKey orderBy) {
		if (orderBy != null) {
			List<RoomSchedulingRow> sorted = new SortableFlexiTableModelDelegate<>(orderBy, this, locale).sort();
			super.setObjects(sorted);
		}
	}

	@Override
	public Object getValueAt(int row, int col) {
		return getValueAt(getObject(row), col);
	}

	@Override
	public Object getValueAt(RoomSchedulingRow row, int col) {
		return switch (SchedulingCols.values()[col]) {
			case warnings -> row;
			case date -> row.getDateLink();
			case from -> row.getBooking().getStartDate();
			case to -> row.getBooking().getEndDate();
			case reference -> row.getRoomLink();
			case description -> {
				Room room = row.getBooking().getRoom();
				if (room == null) yield null;
				String desc = room.getDescription();
				String ref = room.getExternalRef();
				yield StringHelper.containsNonWhitespace(desc) && !desc.equals(ref) ? desc : null;
			}
			case status -> row.getBooking().getRoom() != null ? row.getBooking().getRoom().getStatus() : null;
			case building -> row;
			case event -> row.getEventLink();
			case statusEvent -> row.getBooking().getLectureBlock();
			case element -> null;
			case course -> null;
			case numParticipants -> row.getNumParticipants();
			case numSeats -> {
				Room room = row.getBooking().getRoom();
				yield room != null ? room.getSeats() : null;
			}
		};
	}

	public enum SchedulingCols implements FlexiSortableColumnDef {
		warnings("room.scheduling.col.warnings"),
		date("room.scheduling.col.date"),
		from("room.scheduling.col.from"),
		to("room.scheduling.col.to"),
		reference("room.scheduling.col.reference"),
		description("room.scheduling.col.description"),
		status("room.scheduling.col.status"),
		building("room.scheduling.col.building"),
		event("room.scheduling.col.event"),
		statusEvent("room.scheduling.col.status.event"),
		element("room.scheduling.col.element"),
		course("room.scheduling.col.course"),
		numParticipants("room.scheduling.col.num.participants"),
		numSeats("room.scheduling.col.num.seats");

		private final String i18nHeaderKey;

		SchedulingCols(String i18nHeaderKey) {
			this.i18nHeaderKey = i18nHeaderKey;
		}

		@Override
		public boolean sortable() {
			return this != warnings && this != building;
		}

		@Override
		public String sortKey() {
			return name();
		}

		@Override
		public String i18nHeaderKey() {
			return i18nHeaderKey;
		}
	}
}
