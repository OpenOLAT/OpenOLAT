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
package org.olat.course.assessment.ui.tool;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.course.assessment.model.SearchAssessedIdentityParams.Particpant;
import org.olat.course.assessment.ui.tool.event.BusinessGroupEvent;
import org.olat.course.assessment.ui.tool.event.CurriculumElementEvent;
import org.olat.course.assessment.ui.tool.event.SelectionEvents;
import org.olat.modules.assessment.model.AssessmentEntryStatus;
import org.olat.modules.assessment.ui.AssessedIdentityListState;

/**
 * 
 * Initial date: 28 Jan 2022<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class AssessmentEventToState {
	
	private final Controller controller;
	
	public AssessmentEventToState(Controller controller) {
		this.controller = controller;
	}

	public boolean handlesEvent(Controller source, Event event) {
		if (this.controller == source) {
			if (event == SelectionEvents.USERS_EVENT) {
				return true;
			} else if (event == SelectionEvents.PASSED_EVENT) {
				return true;
			} else if (event == SelectionEvents.FAILED_EVENT) {
				return true;
			} else if (event == SelectionEvents.UNDEFINED_EVENT) {
				return true;
			} else if (event == SelectionEvents.DONE_EVENT) {
				return true;
			} else if (event == SelectionEvents.NOT_DONE_EVENT) {
				return true;
			} else if (event == SelectionEvents.MEMBERS_EVENT) {
				return true;
			} else if (event == SelectionEvents.NON_MEMBERS_EVENT) {
				return true;
			} else if (event == SelectionEvents.FAKE_PARTICIPANTS_EVENT) {
				return true;
			} else if (event instanceof BusinessGroupEvent) {
				return true;
			} else if (event instanceof CurriculumElementEvent) {
				return true;
			} 
		}
		return false;
	}
	
	public AssessedIdentityListState getState(Event event) {
		if (event == SelectionEvents.USERS_EVENT) {
			return null;
		} else if (event == SelectionEvents.PASSED_EVENT) {
			return new AssessedIdentityListState(null, Collections.singletonList("passed"), null, null, null, null, "Passed", false);
		} else if (event == SelectionEvents.FAILED_EVENT) {
			return new AssessedIdentityListState(null, Collections.singletonList("failed"), null, null, null, null, "Failed", false);
		} else if (event == SelectionEvents.UNDEFINED_EVENT) {
			return new AssessedIdentityListState(null, Collections.singletonList("notGraded"), null, null, null, null, null, false);
		} else if (event == SelectionEvents.DONE_EVENT) {
			return new AssessedIdentityListState(Collections.singletonList(AssessmentEntryStatus.done.name()),
					null, null, null, null, null, null, false);
		} else if (event == SelectionEvents.NOT_DONE_EVENT) {
			return new AssessedIdentityListState(
					List.of(AssessmentEntryStatus.notReady.name(), AssessmentEntryStatus.notStarted.name(),
							AssessmentEntryStatus.inProgress.name(), AssessmentEntryStatus.inReview.name()),
					null, null, null, null, null, null, false);
		} else if (event == SelectionEvents.MEMBERS_EVENT) {
			return new AssessedIdentityListState(null, null, null, Collections.singletonList(Particpant.member.name()), null, null, null, false);
		} else if (event == SelectionEvents.NON_MEMBERS_EVENT) {
			return new AssessedIdentityListState(null, null, null, Collections.singletonList(Particpant.nonMember.name()), null, null, null, false);
		} else if (event == SelectionEvents.FAKE_PARTICIPANTS_EVENT) {
			return new AssessedIdentityListState(null, null, null, Collections.singletonList(Particpant.fakeParticipant.name()), null, null, null, false);
		} else if (event instanceof BusinessGroupEvent) {
			BusinessGroupEvent bge = (BusinessGroupEvent)event;
			List<String> groupKeys = bge.getKeys().stream().map(key -> "businessgroup-" + key).collect(Collectors.toList());
			return new AssessedIdentityListState(null, null, null, null, null, groupKeys, null, false);
		} else if (event instanceof CurriculumElementEvent) {
			CurriculumElementEvent cee = (CurriculumElementEvent)event;
			List<String> ceKeys = cee.getKeys().stream().map(key -> "curriculumelement-" + key).collect(Collectors.toList());
			return new AssessedIdentityListState(null, null, null, null, null, ceKeys, null, false);
		} 
		return null;
	}

}
