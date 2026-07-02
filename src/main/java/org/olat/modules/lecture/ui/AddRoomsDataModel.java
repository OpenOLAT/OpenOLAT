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
package org.olat.modules.lecture.ui;

import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiTableDataModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiSortableColumnDef;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;

/**
 * Initial date: 30 Jun 2026<br>
 * @author cpfranger, christoph.pfranger@frentix.com, <a href="https://www.frentix.com">https://www.frentix.com</a>
 */
public class AddRoomsDataModel extends DefaultFlexiTableDataModel<AddRoomsRow> {

	public enum AddRoomsCols implements FlexiSortableColumnDef {
		reference("add.rooms.col.reference"),
		description("add.rooms.col.description"),
		seats("add.rooms.col.seats"),
		availability("add.rooms.col.availability"),
		occupiedBy("add.rooms.col.occupied.by"),
		element("add.rooms.col.element"),
		course("add.rooms.col.course"),
		earlierSlot("add.rooms.col.earlier.slot"),
		laterSlot("add.rooms.col.later.slot");

		private final String i18nKey;

		AddRoomsCols(String i18nKey) {
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

	public AddRoomsDataModel(FlexiTableColumnModel columnsModel) {
		super(columnsModel);
	}

	@Override
	public Object getValueAt(int row, int col) {
		AddRoomsRow roomRow = getObject(row);
		return switch (AddRoomsCols.values()[col]) {
			case reference -> roomRow.getReference();
			case description -> roomRow.getDescription();
			case seats -> roomRow;
			case availability -> roomRow.getAvailability();
			case occupiedBy -> roomRow.getOccupiedBy();
			case element -> roomRow.getElementReference();
			case course -> roomRow.getCourseReference();
			case earlierSlot -> roomRow;
			case laterSlot -> roomRow;
		};
	}
}
