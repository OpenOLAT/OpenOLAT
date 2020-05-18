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
package org.olat.course.nodes.appointments;

import java.util.Date;

import org.olat.repository.RepositoryEntry;

/**
 * 
 * Initial date: 11 May 2020<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class AppointmentSearchParams {
	
	private Long appointmentKey;
	private Topic topic;
	private RepositoryEntry entry;
	private String subIdent;
	private Date startAfter;
	private Appointment.Status status;
	private boolean fetchTopic;

	public Long getAppointmentKey() {
		return appointmentKey;
	}

	public void setAppointmentKey(Long appointmentKey) {
		this.appointmentKey = appointmentKey;
	}
	
	public Topic getTopic() {
		return topic;
	}
	
	public void setTopic(Topic topic) {
		this.topic = topic;
	}
	
	public RepositoryEntry getEntry() {
		return entry;
	}
	
	public void setEntry(RepositoryEntry entry) {
		this.entry = entry;
	}
	
	public String getSubIdent() {
		return subIdent;
	}
	
	public void setSubIdent(String subIdent) {
		this.subIdent = subIdent;
	}
	
	public Date getStartAfter() {
		return startAfter;
	}

	public void setStartAfter(Date startAfter) {
		this.startAfter = startAfter;
	}

	public Appointment.Status getStatus() {
		return status;
	}
	
	public void setStatus(Appointment.Status status) {
		this.status = status;
	}

	public boolean isFetchTopic() {
		return fetchTopic;
	}

	public void setFetchTopic(boolean fetchTopic) {
		this.fetchTopic = fetchTopic;
	}

}
