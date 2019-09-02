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
package org.olat.course.learningpath.model;

import org.olat.course.learningpath.LearningPathRoles;

/**
 * 
 * Initial date: 2 Sep 2019<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class LearningPathRolesImpl implements LearningPathRoles {

	private final boolean participant;
	private final boolean coach;
	private final boolean admin;
	
	public LearningPathRolesImpl(boolean participant, boolean coach, boolean admin) {
		this.participant = participant;
		this.coach = coach;
		this.admin = admin;
	}

	@Override
	public boolean isParticipant() {
		return participant;
	}

	@Override
	public boolean isCoach() {
		return coach;
	}

	@Override
	public boolean isAdmin() {
		return admin;
	}

}
