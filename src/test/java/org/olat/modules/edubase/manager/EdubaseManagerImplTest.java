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
package org.olat.modules.edubase.manager;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Answers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.olat.basesecurity.AuthHelper;
import org.olat.basesecurity.IdentityImpl;
import org.olat.core.id.IdentityEnvironment;
import org.olat.login.LoginModule;
import org.olat.login.auth.AuthenticationProvider;
import org.olat.modules.edubase.BookSection;
import org.olat.modules.edubase.EdubaseModule;
import org.olat.modules.edubase.model.BookSectionImpl;

/**
 *
 * Initial date: 26.06.2017<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
@RunWith(MockitoJUnitRunner.class)
public class EdubaseManagerImplTest {

	private static String BOOK_ID = "12345";

	@Mock
	private EdubaseModule edubaseModuleMock;
	@Mock(answer = Answers.RETURNS_DEEP_STUBS)
	private IdentityEnvironment identityEnvironmentMock;
	@Mock
	private LoginModule loginModulMock;
	@Mock
	private AuthenticationProvider authenticationProviderMock;

	@InjectMocks
	private EdubaseManagerImpl sut;


	@Test
	public void shouldReturnValidIfTheBookIdUrlIsABookId() {
		boolean isValid = sut.validateBookId(BOOK_ID);

		assertThat(isValid).isTrue();
	}

	@Test
	public void shouldReturnNotValidIfTheBookIdUrlIsNoRegularUrl() {
		boolean isValid = sut.validateBookId("noInteger");

		assertThat(isValid).isFalse();
	}

	@Test
	public void shouldReturnNotValidIfTheBookIdUrlIsNull() {
		boolean isValid = sut.validateBookId(null);

		assertThat(isValid).isFalse();
	}

	@Test
	public void getBookIdShouldReturnBookIdIfDetailsUrl() {
		String url = new StringBuilder("https://app.edubase.ch/#details/").append(BOOK_ID).toString();
		getBookIdShouldReturnBookId(url);
	}

	@Test
	public void getBookIdShouldReturnBookIdIfRegularUrl() {
		String url = new StringBuilder("https://app.edubase.ch/#doc/").append(BOOK_ID).append("/1").toString();
		getBookIdShouldReturnBookId(url);
	}

	@Test
	public void getBookIdShouldReturnBookIdIfUrlHasLangParameter() {
		String url = new StringBuilder("https://app.edubase.ch/?lang=en#doc/").append(BOOK_ID).append("/1").toString();
		getBookIdShouldReturnBookId(url);
	}

	@Test
	public void getBookIdShouldReturnBookIdIfUrlHasHightPageNumber() {
		String url = new StringBuilder("https://app.edubase.ch/#doc/").append(BOOK_ID).append("/33").toString();
		getBookIdShouldReturnBookId(url);
	}

	@Test
	public void getBookIdShouldReturnBookIdIfUrlHasNoPageNumber() {
		String url = new StringBuilder("https://app.edubase.ch/#doc/").append(BOOK_ID).toString();
		getBookIdShouldReturnBookId(url);
	}

	@Test
	public void getBookIdShouldReturnBookIdIfUrlHasNoProtocol() {
		String url = new StringBuilder("app.edubase.ch/#doc/").append(BOOK_ID).append("/1").toString();
		getBookIdShouldReturnBookId(url);
	}

	@Test
	public void getBookIdShouldReturnBookIdIfUrlIsBookId() {
		String url = new StringBuilder(BOOK_ID).toString();
		getBookIdShouldReturnBookId(url);
	}

	private void getBookIdShouldReturnBookId(String url) {
		assertThat(sut.parseBookId(url)).isEqualTo(BOOK_ID);
	}

	@Test
	public void getBookIdShouldReturnInputfUrlIsNull() {
		String url = null;
		getBookIdShouldReturnInput(url);
	}

	@Test
	public void getBookIdShouldReturnInputIfUrlIsEmptyString() {
		String url = "";
		getBookIdShouldReturnInput(url);
	}

	@Test
	public void getBookIdShouldReturnInputIfUrlHasNoBookId() {
		String url = new StringBuilder("https://app.edubase.ch/").toString();
		getBookIdShouldReturnInput(url);
	}

	@Test
	public void getBookIdShouldReturnInputIfUrlHasNoDocPart() {
		String url = new StringBuilder("https://app.edubase.ch/").append(BOOK_ID).append("/1").toString();
		getBookIdShouldReturnInput(url);
	}

	@Test
	public void getBookIdShouldReturnInputIfUrlIsCompletleyDifferent() {
		String url = "https://www.openolat.com/";
		getBookIdShouldReturnInput(url);
	}

	private void getBookIdShouldReturnInput(String url) {
		assertThat(sut.parseBookId(url)).isEqualTo(url);
	}

	@Test
	public void getUserIdShouldConcatIssuerAndIdentityName() {
		String userName = "user";
		IdentityImpl identity = new IdentityImpl();
		identity.setName(userName);
		String providerName = "OLAT";
		String issuerIdentifier = "https://issuer.abc";

		when(identityEnvironmentMock.getIdentity()).thenReturn(identity);
		when(identityEnvironmentMock.getAttributes().get(AuthHelper.ATTRIBUTE_AUTHPROVIDER)).thenReturn(providerName);
		when(authenticationProviderMock.getIssuerIdentifier(identityEnvironmentMock))
				.thenReturn(issuerIdentifier);
		when(loginModulMock.getAuthenticationProvider(providerName)).thenReturn(authenticationProviderMock);

		String userID = sut.getUserId(identityEnvironmentMock);

		String expectedUserId = issuerIdentifier.concat("#").concat(userName);
		assertThat(userID).isEqualTo(expectedUserId);
	}

	@Test
	public void getLtiLaunchUrlShoudAppendBookIdAndMandatoryPageFrom() {
		BookSection bookSection = new BookSectionImpl();
		bookSection.setBookId(BOOK_ID);
		String baseUrl = "https://edubase.ch";
		when(edubaseModuleMock.getLtiLaunchUrl()).thenReturn(baseUrl);

		String generatedLtiUrl = sut.getLtiLaunchUrl(bookSection);

		String expectedLtiUrl = baseUrl + "/" + BOOK_ID + "/1";
		assertThat(generatedLtiUrl).isEqualTo(expectedLtiUrl);
	}

	@Test
	public void getLtiLaunchUrlShoudAppendPageFrom() {
		BookSection bookSection = new BookSectionImpl();
		bookSection.setBookId(BOOK_ID);
		Integer pageFrom = 9898;
		bookSection.setPageFrom(pageFrom);
		String baseUrl = "https://edubase.ch";
		when(edubaseModuleMock.getLtiLaunchUrl()).thenReturn(baseUrl);

		String generatedLtiUrl = sut.getLtiLaunchUrl(bookSection);

		String expectedLtiUrl = baseUrl + "/" + BOOK_ID + "/" + pageFrom;
		assertThat(generatedLtiUrl).isEqualTo(expectedLtiUrl);
	}
	
	@Test
	public void shouldGetApplicationUrl() {
		IdentityImpl identityImpl = new IdentityImpl();
		String readerUrl = "https://reader.openolat.com";
		when(edubaseModuleMock.getReaderUrl()).thenReturn(readerUrl);
		when(edubaseModuleMock.isReaderUrlUnique()).thenReturn(Boolean.FALSE);
		
		String applicationUrl = sut.getApplicationUrl(identityImpl);
		
		assertThat(applicationUrl).isEqualTo(readerUrl);
	}
	
	@Test
	public void shouldGetApplicationUrlWithToken() {
		IdentityImpl identityImpl = new IdentityImpl();
		identityImpl.setKey(Long.valueOf("1"));
		String readerUrl = "https://reader.openolat.com";
		when(edubaseModuleMock.getReaderUrl()).thenReturn(readerUrl);
		when(edubaseModuleMock.isReaderUrlUnique()).thenReturn(Boolean.TRUE);
		
		String applicationUrl = sut.getApplicationUrl(identityImpl);
		
		assertThat(applicationUrl).startsWith("https://").endsWith("reader.openolat.com");
		assertThat(applicationUrl.length()).isGreaterThan(readerUrl.length());
	}
	
	@Test
	public void shouldGetAllwaysSameApplicationUrlForAUser() {
		IdentityImpl identityImpl = new IdentityImpl();
		identityImpl.setKey(Long.valueOf("1"));
		String readerUrl = "https://reader.openolat.com";
		when(edubaseModuleMock.getReaderUrl()).thenReturn(readerUrl);
		when(edubaseModuleMock.isReaderUrlUnique()).thenReturn(Boolean.TRUE);
		
		String applicationUrl1 = sut.getApplicationUrl(identityImpl);
		String applicationUrl2 = sut.getApplicationUrl(identityImpl);
		
		assertThat(applicationUrl1).isEqualTo(applicationUrl2);
	}
	
	@Test
	public void shouldGetDifferentApplicationUrlForDifferentUsers() {
		IdentityImpl identityImpl1 = new IdentityImpl();
		identityImpl1.setKey(Long.valueOf("1"));
		IdentityImpl identityImpl2 = new IdentityImpl();
		identityImpl2.setKey(Long.valueOf("2"));
		String readerUrl = "https://reader.openolat.com";
		when(edubaseModuleMock.getReaderUrl()).thenReturn(readerUrl);
		when(edubaseModuleMock.isReaderUrlUnique()).thenReturn(Boolean.TRUE);
		
		String applicationUrl1 = sut.getApplicationUrl(identityImpl1);
		String applicationUrl2 = sut.getApplicationUrl(identityImpl2);
		
		assertThat(applicationUrl1).isNotEqualTo(applicationUrl2);
	}

}
