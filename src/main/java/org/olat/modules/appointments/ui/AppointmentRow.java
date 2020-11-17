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

import java.util.List;

import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.dropdown.DropdownItem;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.modules.appointments.Appointment;
import org.olat.modules.appointments.Participation;
import org.olat.modules.appointments.ui.AppointmentListController.FormItemList;
import org.olat.modules.appointments.ui.ParticipationsRenderer.ParticipantsWrapper;

/**
 * 
 * Initial date: 14 Apr 2020<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class AppointmentRow {
	
	private final Appointment appointment;
	private Participation participation;
	private List<String> participants;
	private Component showMoreLink;
	private String date;
	private String dateLong;
	private String dateShort1;
	private String dateShort2;
	private String time;
	private String location;
	private String details;
	private String translatedStatus;
	private String statusCSS;
	private Boolean showNumberOfParticipations;
	private Integer numberOfParticipations;
	private Integer freeParticipations;
	private FormItem dayEl;
	private String selectionCSS;
	private FormItemList recordingLinks;
	private FormLink selectLink;
	private FormLink confirmLink;
	private DropdownItem commandDropdown;
	
	public AppointmentRow(Appointment appointment) {
		this.appointment = appointment;
	}

	public Appointment getAppointment() {
		return appointment;
	}
	
	public Long getKey() {
		return appointment.getKey();
	}

	public Participation getParticipation() {
		return participation;
	}

	public void setParticipation(Participation participation) {
		this.participation = participation;
	}

	public List<String> getParticipants() {
		return participants;
	}

	public ParticipantsWrapper getParticipantsWrapper() {
		return participants != null? new ParticipantsWrapper(participants): null;
	}

	public void setParticipants(List<String> participants) {
		this.participants = participants;
	}

	public Component getShowMoreLink() {
		return showMoreLink;
	}

	public void setShowMoreLink(Component showMoreLink) {
		this.showMoreLink = showMoreLink;
	}
	
	public String getDate() {
		return date;
	}

	public void setDate(String date) {
		this.date = date;
	}

	public String getDateLong() {
		return dateLong;
	}

	public void setDateLong(String dateLong) {
		this.dateLong = dateLong;
	}

	public String getDateShort1() {
		return dateShort1;
	}

	public void setDateShort1(String dateShort1) {
		this.dateShort1 = dateShort1;
	}

	public String getDateShort2() {
		return dateShort2;
	}

	public void setDateShort2(String dateShort2) {
		this.dateShort2 = dateShort2;
	}

	public String getTime() {
		return time;
	}

	public void setTime(String time) {
		this.time = time;
	}

	public String getLocation() {
		return location;
	}

	public void setLocation(String location) {
		this.location = location;
	}

	public String getDetails() {
		return details;
	}

	public void setDetails(String details) {
		this.details = details;
	}

	public String getTranslatedStatus() {
		return translatedStatus;
	}

	public void setTranslatedStatus(String translatedStatus) {
		this.translatedStatus = translatedStatus;
	}

	public String getStatusCSS() {
		return statusCSS;
	}

	public void setStatusCSS(String statusCSS) {
		this.statusCSS = statusCSS;
	}

	public Boolean getShowNumberOfParticipations() {
		return showNumberOfParticipations;
	}

	public void setShowNumberOfParticipations(Boolean showNumberOfParticipations) {
		this.showNumberOfParticipations = showNumberOfParticipations;
	}

	public Integer getNumberOfParticipations() {
		return numberOfParticipations;
	}

	public void setNumberOfParticipations(Integer numberOfParticipations) {
		this.numberOfParticipations = numberOfParticipations;
	}

	public Integer getFreeParticipations() {
		return freeParticipations;
	}

	public void setFreeParticipations(Integer freeParticipations) {
		this.freeParticipations = freeParticipations;
	}

	public FormItem getDayEl() {
		return dayEl;
	}

	public String getDayElName() {
		return dayEl != null? dayEl.getName(): null;
	}

	public void setDayEl(FormItem dayEl) {
		this.dayEl = dayEl;
	}

	public String getSelectionCSS() {
		return selectionCSS != null? selectionCSS: "";
	}

	public void setSelectionCSS(String selectionCSS) {
		this.selectionCSS = selectionCSS;
	}
	
	public FormItemList getRecordingLinks() {
		return recordingLinks;
	}

	public void setRecordingLinks(FormItemList recordingLinks) {
		this.recordingLinks = recordingLinks;
	}

	public FormLink getSelectLink() {
		return selectLink;
	}

	public String getSelectLinkName() {
		return selectLink != null? selectLink.getName(): null;
	}

	public void setSelectLink(FormLink selectLink) {
		this.selectLink = selectLink;
	}
	
	public FormLink getConfirmLink() {
		return confirmLink;
	}

	public String getConfirmLinkName() {
		return confirmLink != null? confirmLink.getName(): null;
	}

	public void setConfirmLink(FormLink confirmLink) {
		this.confirmLink = confirmLink;
	}
	
	public DropdownItem getCommandDropdown() {
		return commandDropdown;
	}

	public void setCommandDropdown(DropdownItem commandDropdown) {
		this.commandDropdown = commandDropdown;
	}

}
