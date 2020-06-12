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
package org.olat.course.nodes.appointments.ui;

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
 * Initial date: 14 Apr 2020<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class AppointmentDataModel extends DefaultFlexiTableDataModel<AppointmentRow>
implements SortableFlexiTableDataModel<AppointmentRow> {
	
	private final Locale locale;
	
	public AppointmentDataModel(FlexiTableColumnModel columnsModel, Locale locale) {
		super(columnsModel);
		this.locale = locale;
	}

	@Override
	public void sort(SortKey orderBy) {
		List<AppointmentRow> rows = new SortableFlexiTableModelDelegate<>(orderBy, this, locale).sort();
		super.setObjects(rows);
	}

	@Override
	public Object getValueAt(int row, int col) {
		AppointmentRow appointment = getObject(row);
		return getValueAt(appointment, col);
	}

	@Override
	public Object getValueAt(AppointmentRow row, int col) {
		switch(AppointmentCols.values()[col]) {
			case id: return row.getAppointment().getKey();
			case status: return row.getAppointment().getStatus();
			case start: return row.getAppointment().getStart();
			case end: return row.getAppointment().getEnd();
			case location: return row.getAppointment().getLocation();
			case details: return row.getAppointment().getDetails();
			case maxParticipations: return row.getAppointment().getMaxParticipations();
			case freeParticipations: return row.getFreeParticipations();
			case numberOfParticipations: return row.getNumberOfParticipations();
			case participants: return row.getParticipantsWrapper();
			case select: return row.getSelectLink();
			case rebook: return row.getRebookLink();
			case confirm: return row.getConfirmLink();
			case delete: return row.getDeleteLink();
			case edit: return row.getEditLink();
			default: return null;
		}
	}

	@Override
	public DefaultFlexiTableDataModel<AppointmentRow> createCopyWithEmptyList() {
		return new AppointmentDataModel(getTableColumnModel(), locale);
	}
	
	public enum AppointmentCols implements FlexiSortableColumnDef {
		id("appointment.id"),
		status("appointment.status"),
		start("appointment.start"),
		end("appointment.end"),
		location("appointment.location"),
		details("appointment.details"),
		maxParticipations("appointment.max.participations"),
		numberOfParticipations("appointment.number.of.participations"),
		freeParticipations("appointment.free.participations"),
		participants("participants"),
		select("select"),
		rebook("rebook"),
		confirm("confirm"),
		edit("edit"),
		delete("delete");
		
		private final String i18nKey;
		
		private AppointmentCols(String i18nKey) {
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
