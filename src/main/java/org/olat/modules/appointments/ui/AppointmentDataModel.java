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
package org.olat.modules.appointments.ui;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import org.olat.core.commons.persistence.SortKey;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableFilter;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiTableDataModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FilterableFlexiTableModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiSortableColumnDef;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SortableFlexiTableDataModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SortableFlexiTableModelDelegate;
import org.olat.core.gui.translator.Translator;
import org.olat.modules.appointments.Appointment;

/**
 * 
 * Initial date: 14 Apr 2020<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class AppointmentDataModel extends DefaultFlexiTableDataModel<AppointmentRow>
implements SortableFlexiTableDataModel<AppointmentRow>, FilterableFlexiTableModel {
	
	public static final String FILTER_ALL = "all";
	public static final String FILTER_PARTICIPATED = "participated";
	public static final String FILTER_FUTURE = "future";
	
	private final Translator translator;
	private List<AppointmentRow> backups;
	
	public AppointmentDataModel(FlexiTableColumnModel columnsModel, Translator translator) {
		super(columnsModel);
		this.translator = translator;
	}

	@Override
	public void sort(SortKey orderBy) {
		List<AppointmentRow> rows = new SortableFlexiTableModelDelegate<>(orderBy, this, translator.getLocale()).sort();
		super.setObjects(rows);
	}
	
	@Override
	public void setObjects(List<AppointmentRow> objects) {
		super.setObjects(objects);
		backups = objects;
	}
	
	@Override
	public void filter(String searchString, List<FlexiTableFilter> filters) {
		if (filters == null || filters.isEmpty() || FILTER_ALL.equals(filters.get(0).getFilter())) {
			super.setObjects(backups);
		} else {
			List<String> filterKeys = filters.stream().map(FlexiTableFilter::getFilter).collect(Collectors.toList());
			Date now = new Date();
			List<AppointmentRow> filteredRows = backups.stream()
					.filter(r -> apply(filterKeys, r, now))
					.collect(Collectors.toList());
			super.setObjects(filteredRows);
		}
	}
	
	private boolean apply(List<String> filterKeys, AppointmentRow row, Date date) {
		if (filterKeys.contains(FILTER_PARTICIPATED) && row.getParticipation() != null) {
			return true;
		}
		if (filterKeys.contains(FILTER_FUTURE)) {
			Appointment appointment = row.getAppointment();
			return AppointmentsUIFactory.isEndInFuture(appointment, date);
		}
		return false;
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
			case location: return AppointmentsUIFactory.getDisplayLocation(translator, row.getAppointment());
			case details: return row.getAppointment().getDetails();
			case maxParticipations: return row.getAppointment().getMaxParticipations();
			case freeParticipations: return row.getFreeParticipations();
			case numberOfParticipations: return row.getNumberOfParticipations();
			case participants: return row.getParticipantsWrapper();
			case recordings: return row.getRecordingLinks();
			case select: return row.getSelectLink();
			case confirm: return row.getConfirmLink();
			case commands: return row.getCommandDropdown();
			default: return null;
		}
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
		recordings("recordings"),
		select("select"),
		confirm("confirm"),
		commands("table.header.commands");
		
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
