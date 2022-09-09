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
package org.olat.modules.assessment.ui.event;

import org.olat.core.gui.control.Event;

/**
 * 
 * Initial date: 04.05.2016<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class UserFilterEvent extends Event {

	private static final long serialVersionUID = -8695229967742591048L;
	private static final String CMD = "filter-user";
	
	private final boolean withMembers;
	private final boolean withNonParticipantUsers;
	private final boolean withFakeParticipants;
	private final boolean withAnonymousUser;
	
	public UserFilterEvent(boolean withMembers, boolean withNonParticipantUsers, boolean withFakeParticipants, boolean withAnonymousUser) {
		super(CMD);
		this.withMembers = withMembers;
		this.withNonParticipantUsers = withNonParticipantUsers;
		this.withFakeParticipants = withFakeParticipants;
		this.withAnonymousUser = withAnonymousUser;
	}

	public boolean isWithMembers() {
		return withMembers;
	}

	public boolean isWithNonParticipantUsers() {
		return withNonParticipantUsers;
	}

	public boolean isWithFakeParticipants() {
		return withFakeParticipants;
	}

	public boolean isWithAnonymousUser() {
		return withAnonymousUser;
	}
}
