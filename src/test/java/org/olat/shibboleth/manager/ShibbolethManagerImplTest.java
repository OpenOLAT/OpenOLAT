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
package org.olat.shibboleth.manager;

import static org.mockito.AdditionalAnswers.returnsFirstArg;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.olat.basesecurity.BaseSecurity;
import org.olat.basesecurity.Constants;
import org.olat.basesecurity.SecurityGroup;
import org.olat.core.id.Identity;
import org.olat.core.id.Preferences;
import org.olat.core.id.User;
import org.olat.shibboleth.ShibbolethDispatcher;
import org.olat.user.UserManager;
import org.springframework.test.util.ReflectionTestUtils;

/**
 *
 * Initial date: 19.07.2017<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class ShibbolethManagerImplTest {

	@Mock
	private BaseSecurity securityManagerMock;
	@Mock
	private SecurityGroup securityGroupOlatusersMock;
	@Mock
	private UserManager userManagerMock;
	@Mock
	private Identity identityMock;
	@Mock
	private User userMock;
	@Mock
	private Preferences preferencesMock;
	@Mock
	private ShibbolethAttributes attributesMock;

	@InjectMocks
	private ShibbolethManagerImpl sut;

	@Before
	public void setUp() {
		MockitoAnnotations.initMocks(this);
		ReflectionTestUtils.setField(sut, "securityManager", securityManagerMock);

		when(securityManagerMock.findSecurityGroupByName(Constants.GROUP_OLATUSERS))
				.thenReturn(securityGroupOlatusersMock);
		when(securityManagerMock.createAndPersistIdentityAndUser(anyString(), isNull(), any(User.class), anyString(), anyString()))
				.thenReturn(identityMock);
		when(userManagerMock.createUser(null, null, null)).thenReturn(userMock);
		when(identityMock.getUser()).thenReturn(userMock);
		when(userMock.getPreferences()).thenReturn(preferencesMock);
		when(attributesMock.syncUser(any(User.class))).then(returnsFirstArg());
	}

	@Test
	public void shouldCreateAndPersistNewUser() {
		sut.createAndPersistUser(anyString(), anyString(), anyString(), attributesMock);

		verify(securityManagerMock).createAndPersistIdentityAndUser(
				anyString(), eq(null), eq(userMock), eq(ShibbolethDispatcher.PROVIDER_SHIB), anyString());
	}

	@Test
	public void shouldAddNewUserToUsersGroup() {
		sut.createAndPersistUser(anyString(), anyString(), anyString(), attributesMock);

		verify(securityManagerMock).addIdentityToSecurityGroup(identityMock, securityGroupOlatusersMock);
	}

	@Test
	public void shouldSyncUserWhenAttributesChanged() {
		when(attributesMock.hasDifference(userMock)).thenReturn(true);

		sut.syncUser(identityMock, attributesMock);

		verify(attributesMock).syncUser(userMock);
	}

	@Test
	public void shouldUpdateWhenUserAttributesChanged() {
		when(attributesMock.hasDifference(userMock)).thenReturn(true);

		sut.syncUser(identityMock, attributesMock);

		verify(userManagerMock).updateUser(userMock);
	}

	@Test
	public void shouldNotUpdateUserWhenAttributesNotChanged() {
		when(attributesMock.hasDifference(userMock)).thenReturn(false);

		sut.syncUser(identityMock, attributesMock);

		verify(userManagerMock, never()).updateUser(userMock);

	}
}
