/**
 * <a href="https://www.openolat.org">
 * OpenOLAT - Online Learning and Training</a><br>
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); <br>
 * you may not use this file except in compliance with the License.<br>
 * You may obtain a copy of the License at the
 * <a href="https://www.apache.org/licenses/LICENSE-2.0">Apache homepage</a>
 * <p>
 * Unless required by applicable law or agreed to in writing,<br>
 * software distributed under the License is distributed on an "AS IS" BASIS, <br>
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
 * See the License for the specific language governing permissions and <br>
 * limitations under the License.
 * <p>
 * Initial date: 01.07.2026<br>
 * @author uhensler, https://www.frentix.com
 */
package org.olat.course.config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.Test;
import org.olat.course.run.userview.UserCourseEnvironment;
import org.olat.repository.RepositoryEntrySecurity;

public class ExternalToolVisibilityTest {

	private static final int TOOL_INDEX = 1;

	private CourseConfig enabledOwnerOnly() {
		CourseConfig courseConfig = new CourseConfig();
		courseConfig.initDefaults();
		courseConfig.setExternalToolEnabled(TOOL_INDEX, true);
		courseConfig.setExternalToolVisible(TOOL_INDEX, ExternalToolVisibility.owner, true);
		courseConfig.setExternalToolVisible(TOOL_INDEX, ExternalToolVisibility.coach, false);
		courseConfig.setExternalToolVisible(TOOL_INDEX, ExternalToolVisibility.participant, false);
		return courseConfig;
	}

	private CourseConfig enabledCoachOnly() {
		CourseConfig courseConfig = new CourseConfig();
		courseConfig.initDefaults();
		courseConfig.setExternalToolEnabled(TOOL_INDEX, true);
		courseConfig.setExternalToolVisible(TOOL_INDEX, ExternalToolVisibility.owner, false);
		courseConfig.setExternalToolVisible(TOOL_INDEX, ExternalToolVisibility.coach, true);
		courseConfig.setExternalToolVisible(TOOL_INDEX, ExternalToolVisibility.participant, false);
		return courseConfig;
	}

	private CourseConfig enabledParticipantOnly() {
		CourseConfig courseConfig = new CourseConfig();
		courseConfig.initDefaults();
		courseConfig.setExternalToolEnabled(TOOL_INDEX, true);
		courseConfig.setExternalToolVisible(TOOL_INDEX, ExternalToolVisibility.owner, false);
		courseConfig.setExternalToolVisible(TOOL_INDEX, ExternalToolVisibility.coach, false);
		courseConfig.setExternalToolVisible(TOOL_INDEX, ExternalToolVisibility.participant, true);
		return courseConfig;
	}

	private CourseConfig disabled() {
		CourseConfig courseConfig = new CourseConfig();
		courseConfig.initDefaults();
		courseConfig.setExternalToolEnabled(TOOL_INDEX, false);
		courseConfig.setExternalToolVisible(TOOL_INDEX, ExternalToolVisibility.owner, true);
		courseConfig.setExternalToolVisible(TOOL_INDEX, ExternalToolVisibility.coach, true);
		courseConfig.setExternalToolVisible(TOOL_INDEX, ExternalToolVisibility.participant, true);
		return courseConfig;
	}

	private RepositoryEntrySecurity reSecurity(boolean entryAdmin, boolean owner, boolean principal,
			boolean lrm, boolean curriculumManager, boolean coach) {
		RepositoryEntrySecurity sec = mock(RepositoryEntrySecurity.class);
		when(sec.isEntryAdmin()).thenReturn(entryAdmin);
		when(sec.isOwner()).thenReturn(owner);
		when(sec.isPrincipal()).thenReturn(principal);
		when(sec.isLearnResourceManager()).thenReturn(lrm);
		when(sec.isCurriculumManager()).thenReturn(curriculumManager);
		when(sec.isCoach()).thenReturn(coach);
		return sec;
	}

	private UserCourseEnvironment mockUserCourseEnv(boolean participant) {
		UserCourseEnvironment userCourseEnv = mock(UserCourseEnvironment.class);
		when(userCourseEnv.isParticipant()).thenReturn(participant);
		return userCourseEnv;
	}

	@Test
	public void disabledTool_neverVisible() {
		CourseConfig courseConfig = disabled();
		RepositoryEntrySecurity sec = reSecurity(true, true, true, true, true, true);
		UserCourseEnvironment userCourseEnv = mockUserCourseEnv(true);

		assertThat(ExternalToolVisibility.isVisible(courseConfig, TOOL_INDEX, sec, userCourseEnv)).isFalse();
	}

	@Test
	public void ownerVisibility_entryAdmin() {
		CourseConfig courseConfig = enabledOwnerOnly();
		RepositoryEntrySecurity sec = reSecurity(true, false, false, false, false, false);

		assertThat(ExternalToolVisibility.isVisible(courseConfig, TOOL_INDEX, sec, null)).isTrue();
	}

	@Test
	public void ownerVisibility_owner() {
		CourseConfig courseConfig = enabledOwnerOnly();
		RepositoryEntrySecurity sec = reSecurity(false, true, false, false, false, false);

		assertThat(ExternalToolVisibility.isVisible(courseConfig, TOOL_INDEX, sec, null)).isTrue();
	}

	@Test
	public void ownerVisibility_principal() {
		CourseConfig courseConfig = enabledOwnerOnly();
		RepositoryEntrySecurity sec = reSecurity(false, false, true, false, false, false);

		assertThat(ExternalToolVisibility.isVisible(courseConfig, TOOL_INDEX, sec, null)).isTrue();
	}

	@Test
	public void ownerVisibility_learnResourceManager() {
		CourseConfig courseConfig = enabledOwnerOnly();
		RepositoryEntrySecurity sec = reSecurity(false, false, false, true, false, false);

		assertThat(ExternalToolVisibility.isVisible(courseConfig, TOOL_INDEX, sec, null)).isTrue();
	}

	@Test
	public void ownerVisibility_curriculumManager() {
		CourseConfig courseConfig = enabledOwnerOnly();
		RepositoryEntrySecurity sec = reSecurity(false, false, false, false, true, false);

		assertThat(ExternalToolVisibility.isVisible(courseConfig, TOOL_INDEX, sec, null)).isTrue();
	}

	@Test
	public void ownerVisibility_notConfigured_ownerNotVisible() {
		CourseConfig courseConfig = enabledOwnerOnly();
		courseConfig.setExternalToolVisible(TOOL_INDEX, ExternalToolVisibility.owner, false);
		RepositoryEntrySecurity sec = reSecurity(true, true, true, true, true, false);

		assertThat(ExternalToolVisibility.isVisible(courseConfig, TOOL_INDEX, sec, null)).isFalse();
	}

	@Test
	public void coachVisibility_coachSees() {
		CourseConfig courseConfig = enabledCoachOnly();
		RepositoryEntrySecurity sec = reSecurity(false, false, false, false, false, true);

		assertThat(ExternalToolVisibility.isVisible(courseConfig, TOOL_INDEX, sec, null)).isTrue();
	}

	@Test
	public void coachVisibility_participantDoesNotSee() {
		CourseConfig courseConfig = enabledCoachOnly();
		RepositoryEntrySecurity sec = reSecurity(false, false, false, false, false, false);
		UserCourseEnvironment userCourseEnv = mockUserCourseEnv(true);

		assertThat(ExternalToolVisibility.isVisible(courseConfig, TOOL_INDEX, sec, userCourseEnv)).isFalse();
	}

	@Test
	public void participantVisibility_participantSees() {
		CourseConfig courseConfig = enabledParticipantOnly();
		RepositoryEntrySecurity sec = reSecurity(false, false, false, false, false, false);
		UserCourseEnvironment userCourseEnv = mockUserCourseEnv(true);

		assertThat(ExternalToolVisibility.isVisible(courseConfig, TOOL_INDEX, sec, userCourseEnv)).isTrue();
	}

	@Test
	public void participantVisibility_nonParticipantDoesNotSee() {
		CourseConfig courseConfig = enabledParticipantOnly();
		RepositoryEntrySecurity sec = reSecurity(false, false, false, false, false, false);
		UserCourseEnvironment userCourseEnv = mockUserCourseEnv(false);

		assertThat(ExternalToolVisibility.isVisible(courseConfig, TOOL_INDEX, sec, userCourseEnv)).isFalse();
	}

	@Test
	public void participantVisibility_nullUceDoesNotSee() {
		CourseConfig courseConfig = enabledParticipantOnly();
		RepositoryEntrySecurity sec = reSecurity(false, false, false, false, false, false);

		assertThat(ExternalToolVisibility.isVisible(courseConfig, TOOL_INDEX, sec, null)).isFalse();
	}

	@Test
	public void allRolesEnabled_everyoneCanSee() {
		CourseConfig courseConfig = new CourseConfig();
		courseConfig.initDefaults();
		courseConfig.setExternalToolEnabled(TOOL_INDEX, true);
		courseConfig.setExternalToolVisible(TOOL_INDEX, ExternalToolVisibility.owner, true);
		courseConfig.setExternalToolVisible(TOOL_INDEX, ExternalToolVisibility.coach, true);
		courseConfig.setExternalToolVisible(TOOL_INDEX, ExternalToolVisibility.participant, true);

		RepositoryEntrySecurity ownerSec = reSecurity(false, true, false, false, false, false);
		RepositoryEntrySecurity coachSec = reSecurity(false, false, false, false, false, true);
		RepositoryEntrySecurity plainSec = reSecurity(false, false, false, false, false, false);
		UserCourseEnvironment participantUserCourseEnv = mockUserCourseEnv(true);

		assertThat(ExternalToolVisibility.isVisible(courseConfig, TOOL_INDEX, ownerSec, null)).isTrue();
		assertThat(ExternalToolVisibility.isVisible(courseConfig, TOOL_INDEX, coachSec, null)).isTrue();
		assertThat(ExternalToolVisibility.isVisible(courseConfig, TOOL_INDEX, plainSec, participantUserCourseEnv)).isTrue();
	}

	@Test
	public void noRolesEnabled_nobodyCanSee() {
		CourseConfig courseConfig = new CourseConfig();
		courseConfig.initDefaults();
		courseConfig.setExternalToolEnabled(TOOL_INDEX, true);
		courseConfig.setExternalToolVisible(TOOL_INDEX, ExternalToolVisibility.owner, false);
		courseConfig.setExternalToolVisible(TOOL_INDEX, ExternalToolVisibility.coach, false);
		courseConfig.setExternalToolVisible(TOOL_INDEX, ExternalToolVisibility.participant, false);

		RepositoryEntrySecurity ownerSec = reSecurity(true, true, true, true, true, true);
		UserCourseEnvironment participantUserCourseEnv = mockUserCourseEnv(true);

		assertThat(ExternalToolVisibility.isVisible(courseConfig, TOOL_INDEX, ownerSec, participantUserCourseEnv)).isFalse();
	}

}
