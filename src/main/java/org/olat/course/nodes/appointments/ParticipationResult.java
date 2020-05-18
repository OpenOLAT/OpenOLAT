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

import org.olat.course.nodes.appointments.model.ParticipationResultImpl;

/**
 * 
 * Initial date: 12 May 2020<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public interface ParticipationResult {
	
	static final ParticipationResult APPOINTMENT_DELETED = new ParticipationResultImpl(Status.appointmentDeleted);
	static final ParticipationResult APPOINTMENT_FULL = new ParticipationResultImpl(Status.appointmentFull);
	static final ParticipationResult APPOINTMENT_CONFIRMED = new ParticipationResultImpl(Status.appointmentConfirmed);
	
	static ParticipationResult of(Participation participation) {
		return new ParticipationResultImpl(Status.ok, participation);
	}
	
	enum Status {
		ok,
		appointmentDeleted,
		appointmentFull,
		appointmentConfirmed
	}
	
	public Status getStatus();
	
	/**
	 *
	 * @return the participation if status == ok.
	 */
	public Participation getParticipation();

}
