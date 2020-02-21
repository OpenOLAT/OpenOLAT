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
package org.olat.course.run.userview;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.Test;
import org.olat.core.id.IdentityEnvironment;
import org.olat.core.id.Roles;
import org.olat.course.run.environment.CourseEnvironment;

/**
 * 
 * Initial date: 21.02.2020<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class UserCourseEnvironmentImplTest {

	@Test
	public void shouldBeParticipantIfIsMemebershipParticipant() {
		IdentityEnvironment identEnv = mock(IdentityEnvironment.class, RETURNS_DEEP_STUBS);
		CourseEnvironment courseEnv = mock(CourseEnvironment.class, RETURNS_DEEP_STUBS);
		UserCourseEnvironmentImpl sut =  new UserCourseEnvironmentImpl(identEnv, courseEnv);
		
		sut.setUserRoles(Boolean.FALSE, Boolean.FALSE, Boolean.TRUE);
		
		assertThat(sut.isParticipant()).isTrue();
	}
	
	@Test
	public void shouldNotBeParticipantIfIsMemberAdminOnly() {
		IdentityEnvironment identEnv = mock(IdentityEnvironment.class, RETURNS_DEEP_STUBS);
		CourseEnvironment courseEnv = mock(CourseEnvironment.class, RETURNS_DEEP_STUBS);
		UserCourseEnvironmentImpl sut =  new UserCourseEnvironmentImpl(identEnv, courseEnv);
		
		sut.setUserRoles(Boolean.TRUE, Boolean.FALSE, Boolean.FALSE);
		
		assertThat(sut.isParticipant()).isFalse();
	}
	
	@Test
	public void shouldNotBeParticipantIfIsMemberCoachOnly() {
		IdentityEnvironment identEnv = mock(IdentityEnvironment.class, RETURNS_DEEP_STUBS);
		CourseEnvironment courseEnv = mock(CourseEnvironment.class, RETURNS_DEEP_STUBS);
		UserCourseEnvironmentImpl sut =  new UserCourseEnvironmentImpl(identEnv, courseEnv);
		
		sut.setUserRoles(Boolean.FALSE, Boolean.TRUE, Boolean.FALSE);
		
		assertThat(sut.isParticipant()).isFalse();
	}

	@Test
	public void shouldNotBeParticipantIfIsGuestOnly() {
		IdentityEnvironment identEnv = mock(IdentityEnvironment.class);
		when(identEnv.getRoles()).thenReturn(Roles.guestRoles());
		CourseEnvironment courseEnv = mock(CourseEnvironment.class, RETURNS_DEEP_STUBS);
		UserCourseEnvironmentImpl sut =  new UserCourseEnvironmentImpl(identEnv, courseEnv);
		
		assertThat(sut.isParticipant()).isFalse();
	}
	
	@Test
	public void shouldBeParticipantIfIsWhetherMemberNorGuestOnly() {
		IdentityEnvironment identEnv = mock(IdentityEnvironment.class, RETURNS_DEEP_STUBS);
		CourseEnvironment courseEnv = mock(CourseEnvironment.class, RETURNS_DEEP_STUBS);
		UserCourseEnvironmentImpl sut =  new UserCourseEnvironmentImpl(identEnv, courseEnv);
		
		assertThat(sut.isParticipant()).isTrue();
	}

}
