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
package org.olat.user;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.olat.core.gui.translator.Translator;
import org.olat.core.id.User;
import org.olat.login.LoginModule;
import org.olat.login.auth.AuthenticationProvider;
import org.olat.registration.RegistrationManager;


/**
 * 
 * Initial date: 27.10.2017<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class UserManagerImplTest {

	private static final String UNUSED_EMAIL = "email@mytrashmail.com";
	private static final String DUPLICATE_EMAIL = "duplicate@mytrashmail.com";

	@Mock
	private UserDAO userDaoMock;
	@Mock
	private UserModule userModuleMock;
	@Mock
	private RegistrationManager registrationManagerMock;
	@Mock
	private LoginModule loginModuleMock;
	
	@InjectMocks
	private UserManagerImpl sut = new UserManagerImpl();
	
	@Before
	public void setUp() {
		MockitoAnnotations.initMocks(this);
		
		when(userDaoMock.isEmailInUse(UNUSED_EMAIL)).thenReturn(false);
		when(userDaoMock.isEmailInUse(DUPLICATE_EMAIL)).thenReturn(true);
		when(userDaoMock.isEmailInUse(null)).thenReturn(true);
	}
	
	@Test
	public void shouldAllowUnusedEmailIf_UniqueMandatory_EmailMandatory() {
		when(userModuleMock.isEmailUnique()).thenReturn(true);
		when(userModuleMock.isEmailMandatory()).thenReturn(true);
		
		boolean isEmailAllowed = sut.isEmailAllowed(UNUSED_EMAIL);

		assertThat(isEmailAllowed).isTrue();
	}

	@Test
	public void shouldNotAllowDuplicateEmailIf_UniqueMandatory_EmailMandatory() {
		when(userModuleMock.isEmailUnique()).thenReturn(true);
		when(userModuleMock.isEmailMandatory()).thenReturn(true);
		
		boolean isEmailAllowed = sut.isEmailAllowed(DUPLICATE_EMAIL);

		assertThat(isEmailAllowed).isFalse();
	}

	@Test
	public void shouldNotAllowNullEmailIf_UniqueMandatory_EmailMandatory() {
		when(userModuleMock.isEmailUnique()).thenReturn(true);
		when(userModuleMock.isEmailMandatory()).thenReturn(true);
		
		boolean isEmailAllowed = sut.isEmailAllowed(null);

		assertThat(isEmailAllowed).isFalse();
	}

	@Test
	public void shouldAllowUnusedEmailIf_UniqueOptional_EmailOptional() {
		when(userModuleMock.isEmailUnique()).thenReturn(false);
		when(userModuleMock.isEmailMandatory()).thenReturn(false);
		
		boolean isEmailAllowed = sut.isEmailAllowed(UNUSED_EMAIL);

		assertThat(isEmailAllowed).isTrue();
	}

	@Test
	public void shouldAllowDuplicateEmailIf_UniqueOptional_EmailOptional() {
		when(userModuleMock.isEmailUnique()).thenReturn(false);
		when(userModuleMock.isEmailMandatory()).thenReturn(false);
		
		boolean isEmailAllowed = sut.isEmailAllowed(DUPLICATE_EMAIL);

		assertThat(isEmailAllowed).isTrue();
	}

	@Test
	public void shouldAllowNullEmailIf_UniqueOptional_EmailOptional() {
		when(userModuleMock.isEmailUnique()).thenReturn(false);
		when(userModuleMock.isEmailMandatory()).thenReturn(false);
		
		boolean isEmailAllowed = sut.isEmailAllowed(null);

		assertThat(isEmailAllowed).isTrue();
	}

	@Test
	public void shouldAllowUnusedEmailIf_UniqueOptional_EmailMandatory() {
		when(userModuleMock.isEmailUnique()).thenReturn(false);
		when(userModuleMock.isEmailMandatory()).thenReturn(true);
		
		boolean isEmailAllowed = sut.isEmailAllowed(UNUSED_EMAIL);

		assertThat(isEmailAllowed).isTrue();
	}

	@Test
	public void shouldAllowDuplicateEmailIf_UniqueOptional_EmailMandatory() {
		when(userModuleMock.isEmailUnique()).thenReturn(false);
		when(userModuleMock.isEmailMandatory()).thenReturn(true);
		
		boolean isEmailAllowed = sut.isEmailAllowed(DUPLICATE_EMAIL);

		assertThat(isEmailAllowed).isTrue();
	}

	@Test
	public void shouldNotAllowNullEmailIf_UniqueOptional_EmailMandatory() {
		when(userModuleMock.isEmailUnique()).thenReturn(false);
		when(userModuleMock.isEmailMandatory()).thenReturn(true);
		
		boolean isEmailAllowed = sut.isEmailAllowed(null);

		assertThat(isEmailAllowed).isFalse();
	}

	@Test
	public void shouldAllowUnusedEmailIf_UniqueMandatory_EmailOptional() {
		when(userModuleMock.isEmailUnique()).thenReturn(true);
		when(userModuleMock.isEmailMandatory()).thenReturn(false);
		
		boolean isEmailAllowed = sut.isEmailAllowed(UNUSED_EMAIL);

		assertThat(isEmailAllowed).isTrue();
	}

	@Test
	public void shouldNotAllowDuplicateEmailIf_UniqueMandatory_EmailOptional() {
		when(userModuleMock.isEmailUnique()).thenReturn(true);
		when(userModuleMock.isEmailMandatory()).thenReturn(false);
		
		boolean isEmailAllowed = sut.isEmailAllowed(DUPLICATE_EMAIL);

		assertThat(isEmailAllowed).isFalse();
	}

	@Test
	public void shouldAllowNullEmailIf_UniqueMandatory_EmailOptional() {
		when(userModuleMock.isEmailUnique()).thenReturn(true);
		when(userModuleMock.isEmailMandatory()).thenReturn(false);
		
		boolean isEmailAllowed = sut.isEmailAllowed(null);

		assertThat(isEmailAllowed).isTrue();
	}
	
	@Test
	public void shouldNotAllowUnusedEmailIfReserved_UniqueMandatory_EmailMandatory() {
		when(userModuleMock.isEmailUnique()).thenReturn(true);
		when(userModuleMock.isEmailMandatory()).thenReturn(true);
		when(registrationManagerMock.isEmailReserved(UNUSED_EMAIL)).thenReturn(true);
		
		boolean isEmailAllowed = sut.isEmailAllowed(UNUSED_EMAIL);

		assertThat(isEmailAllowed).isFalse();
	}
	
	@Test
	public void shouldAllowEmailIfItsOwnEmail() {
		when(userModuleMock.isEmailUnique()).thenReturn(true);
		when(userModuleMock.isEmailMandatory()).thenReturn(true);
		User myselfMock = mock(User.class);
		when(myselfMock.getEmail()).thenReturn(DUPLICATE_EMAIL);
		
		boolean isEmailAllowed = sut.isEmailAllowed(DUPLICATE_EMAIL, myselfMock);

		assertThat(isEmailAllowed).isTrue();
	}
	
	@Test
	public void shouldAllowEmailIfItsOwnEmailCaseInsensitive() {
		when(userModuleMock.isEmailUnique()).thenReturn(true);
		when(userModuleMock.isEmailMandatory()).thenReturn(true);
		User myselfMock = mock(User.class);
		when(myselfMock.getEmail()).thenReturn(DUPLICATE_EMAIL.toUpperCase());
		
		boolean isEmailAllowed = sut.isEmailAllowed(DUPLICATE_EMAIL.toLowerCase(), myselfMock);

		assertThat(isEmailAllowed).isTrue();
	}
	
	@Test
	public void shouldDisplayEmail() {
		Translator translatorMock = mock(Translator.class);
		
		String email = sut.getUserDisplayEmail(UNUSED_EMAIL, translatorMock);

		assertThat(email).isEqualTo(UNUSED_EMAIL);
	}
	
	@Test
	public void shouldDisplayDefaultIfHasNoEmail() {
		Translator translatorMock = mock(Translator.class);
		when(translatorMock.translate(any())).thenReturn(UNUSED_EMAIL);
		
		String email = sut.getUserDisplayEmail(null, translatorMock);

		assertThat(email).isEqualTo(UNUSED_EMAIL);
	}
	
	@Test
	public void shouldEnsureEmail() {
		User userWithEmailMock = mock(User.class);
		when(userWithEmailMock.getEmail()).thenReturn(UNUSED_EMAIL);
		
		String ensuredEmail = sut.getEnsuredEmail(userWithEmailMock);
		
		assertThat(ensuredEmail).isEqualTo(UNUSED_EMAIL);
	}
	
	@Test
	public void shouldEnsureEmailIfHasNoEmail() {
		Long userKey = 123l;
		String issuer = "issuer";
		AuthenticationProvider authenticationProviderMock = mock(AuthenticationProvider.class);
		when(authenticationProviderMock.getIssuerIdentifier(any())).thenReturn(issuer);
		when(loginModuleMock.getAuthenticationProvider("OLAT")).thenReturn(authenticationProviderMock);
		User userWithoutEmailMock = mock(User.class);
		when(userWithoutEmailMock.getKey()).thenReturn(userKey);
		
		String ensuredEmail = sut.getEnsuredEmail(userWithoutEmailMock);
		
		String expectedValue = userKey + "@" + issuer;
		assertThat(ensuredEmail).isEqualTo(expectedValue);
	}
	
}
