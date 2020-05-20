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

import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.course.nodes.appointments.Appointment;
import org.olat.course.nodes.appointments.ui.ParticipationsRenderer.ParticipantsWrapper;

/**
 * 
 * Initial date: 14 Apr 2020<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class AppointmentRow {
	
	private final Appointment appointment;
	private List<String> participants;
	private String date;
	private String dateLong;
	private String dateShort1;
	private String dateShort2;
	private String time;
	private String location;
	private String details;
	private String translatedStatus;
	private String statusCSS;
	private Integer freeParticipations;
	private Integer maxParticipations;
	private FormLink rebookLink;
	private FormLink confirmLink;
	private FormLink deleteLink;
	private FormLink editLink;
	
	public AppointmentRow(Appointment appointment) {
		this.appointment = appointment;
	}

	public Appointment getAppointment() {
		return appointment;
	}
	
	public Long getKey() {
		return appointment.getKey();
	}

	public List<String> getParticipants() {
		return participants;
	}

	public ParticipantsWrapper getParticipantsWrapper() {
		return new ParticipantsWrapper(participants);
	}

	public void setParticipants(List<String> participants) {
		this.participants = participants;
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

	public Integer getFreeParticipations() {
		return freeParticipations;
	}

	public void setFreeParticipations(Integer freeParticipations) {
		this.freeParticipations = freeParticipations;
	}

	public Integer getMaxParticipations() {
		return maxParticipations;
	}

	public void setMaxParticipations(Integer maxParticipations) {
		this.maxParticipations = maxParticipations;
	}

	public FormLink getRebookLink() {
		return rebookLink;
	}

	public String getRebookLinkName() {
		return rebookLink != null? rebookLink.getName(): null;
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

	public void setRebookLink(FormLink rebookLink) {
		this.rebookLink = rebookLink;
	}

	public FormLink getDeleteLink() {
		return deleteLink;
	}

	public String getDeleteLinkName() {
		return deleteLink != null? deleteLink.getComponent().getComponentName(): null;
	}

	public void setDeleteLink(FormLink deleteLink) {
		this.deleteLink = deleteLink;
	}

	public FormLink getEditLink() {
		return editLink;
	}

	public String getEditLinkName() {
		return editLink != null? editLink.getComponent().getComponentName(): null;
	}

	public void setEditLink(FormLink editLink) {
		this.editLink = editLink;
	}

}
