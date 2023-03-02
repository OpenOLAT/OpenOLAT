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
package org.olat.modules.project;

import java.util.Collection;
import java.util.stream.Collectors;

/**
 * 
 * Initial date: 13. Feb 2023<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class ProjAppointmentSearchParams {
	
	private ProjProjectRef project;
	private Collection<Long> appointmentKeys;
	private Collection<String> identifiers;
	private Collection<String> eventIds;
	private Collection<Long> artefactKeys;
	private Collection<ProjectStatus> status;
	private Boolean recurrenceIdAvailable;

	public ProjProjectRef getProject() {
		return project;
	}

	public void setProject(ProjProjectRef project) {
		this.project = project;
	}

	public Collection<Long> getAppointmentKeys() {
		return appointmentKeys;
	}
	
	public void setAppointments(Collection<? extends ProjAppointmentRef> appointments) {
		this.appointmentKeys = appointments.stream().map(ProjAppointmentRef::getKey).collect(Collectors.toList());
	}

	public Collection<String> getIdentifiers() {
		return identifiers;
	}

	public void setIdentifiers(Collection<String> identifiers) {
		this.identifiers = identifiers;
	}

	public Collection<String> getEventIds() {
		return eventIds;
	}

	public void setEventIds(Collection<String> eventIds) {
		this.eventIds = eventIds;
	}

	public Collection<Long> getArtefactKeys() {
		return artefactKeys;
	}
	
	public void setArtefacts(Collection<ProjArtefact> artefacts) {
		this.artefactKeys = artefacts.stream().map(ProjArtefact::getKey).collect(Collectors.toSet());
	}

	public Collection<ProjectStatus> getStatus() {
		return status;
	}

	public void setStatus(Collection<ProjectStatus> status) {
		this.status = status;
	}

	public Boolean getRecurrenceIdAvailable() {
		return recurrenceIdAvailable;
	}

	public void setRecurrenceIdAvailable(Boolean recurrenceIdAvailable) {
		this.recurrenceIdAvailable = recurrenceIdAvailable;
	}
	
}
