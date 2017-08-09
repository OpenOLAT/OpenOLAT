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

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.AdditionalAnswers.returnsFirstArg;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.olat.core.id.User;
import org.olat.core.id.UserConstants;
import org.olat.shibboleth.ShibbolethModule;
import org.olat.shibboleth.handler.ShibbolethAttributeHandler;
import org.olat.shibboleth.handler.ShibbolethAttributeHandlerFactory;
import org.olat.user.UserImpl;
import org.springframework.test.util.ReflectionTestUtils;

/**
 *
 * Initial date: 06.08.2017<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class ShibbolethAttributesTest {

	private static final String USER_EMAIL_KEY = UserConstants.EMAIL;
	private static final String SHIB_EMAIL_KEY = "shibEmailKey";
	private static final String SHIB_EMAIL_VALUE = "shibEmailValue";
	private static final String USER_NAME_KEY = UserConstants.LASTNAME;
	private static final String SHIB_NAME_KEY = "shibName";
	private static final String SHIB_NAME_VALUE = "Smith";
	private static final String USER_GENDER_KEY = UserConstants.GENDER;
	private static final String SHIB_GENDER_KEY = "shibGender";
	private static final String SHIB_GENDER_VALUE = "female";
	private static final String USER_CITY_KEY = UserConstants.CITY;
	private static final String SHIB_CITY_KEY = "shibCity";
	private static final String SHIB_CITY_VALUE = null;
	private static final String USER_OLD_VALUE = "old";

	@Mock
	private ShibbolethModule shibbolethModuleMock;
	@Mock
	private ShibbolethAttributeHandlerFactory shibbolethAttributeHandlerFactoryMock;
	@Mock
	private ShibbolethAttributeHandler shibbolethAttributeHandlerMock;

	private ShibbolethAttributes sut;

	@Before
	public void setUp() {
		MockitoAnnotations.initMocks(this);

		sut = new ShibbolethAttributes();

		ReflectionTestUtils.setField(sut, "shibbolethModule", shibbolethModuleMock);
		Map<String, String> shibbolethUserMapping = initUserMapping();
		when(shibbolethModuleMock.getUserMapping()).thenReturn(shibbolethUserMapping);

		// ShibbolethAttributeHandler.parse() does not modify the value
		ReflectionTestUtils.setField(sut, "shibbolethAttributeHandlerFactory", shibbolethAttributeHandlerFactoryMock);
		when(shibbolethAttributeHandlerFactoryMock.getHandler(anyString())).thenReturn(shibbolethAttributeHandlerMock);
		when(shibbolethAttributeHandlerFactoryMock.getHandler(isNull())).thenReturn(shibbolethAttributeHandlerMock);
		when(shibbolethAttributeHandlerMock.parse(anyString())).then(returnsFirstArg());

		Map<String, String> shibbolethKeysValues = initShibbolethMap();
		sut.init(shibbolethKeysValues);
	}

	private Map<String, String> initShibbolethMap() {
		Map<String, String> shibbolethMap = new HashMap<>();
		shibbolethMap.put(SHIB_EMAIL_KEY, SHIB_EMAIL_VALUE);
		shibbolethMap.put(SHIB_NAME_KEY, SHIB_NAME_VALUE);
		shibbolethMap.put(SHIB_GENDER_KEY, SHIB_GENDER_VALUE);
		shibbolethMap.put(SHIB_CITY_KEY, SHIB_CITY_VALUE);
		return shibbolethMap;
	}

	private Map<String, String> initUserMapping() {
		Map<String, String> shibbolethUserMapping = new HashMap<>();
		shibbolethUserMapping.put(SHIB_EMAIL_KEY, USER_EMAIL_KEY);
		shibbolethUserMapping.put(SHIB_NAME_KEY, USER_NAME_KEY);
		shibbolethUserMapping.put(SHIB_GENDER_KEY, USER_GENDER_KEY);
		shibbolethUserMapping.put(SHIB_CITY_KEY, USER_CITY_KEY);
		return shibbolethUserMapping;
	}

	private User getIdenticalOlatUser() {
		User user = new TestableUser();
		user.setProperty(USER_EMAIL_KEY, SHIB_EMAIL_VALUE);
		user.setProperty(USER_NAME_KEY, SHIB_NAME_VALUE);
		user.setProperty(USER_GENDER_KEY, SHIB_GENDER_VALUE);
		user.setProperty(USER_CITY_KEY, SHIB_CITY_VALUE);
		return user;
	}

	@Test
	public void shouldParseValuesWhenInit() {
		verify(shibbolethAttributeHandlerMock, times(3)).parse(anyString());
		verify(shibbolethAttributeHandlerMock, times(1)).parse(isNull());
	}

	@Test
	public void shouldReplaceValueByUserPropertyName() {
		String newValue = "newValue";

		sut.setValueForUserPropertyName(USER_NAME_KEY, newValue);

		assertThat(sut.getValueForUserPropertyName(USER_NAME_KEY)).isEqualTo(newValue);
	}

	@Test
	public void shouldNotSetPropertValueWhenNoMappingExits() {
		String keyNotPresent = "keyNotPresent";
		String newValue = "newValue";

		sut.setValueForUserPropertyName(keyNotPresent, newValue);

		assertThat(sut.getValueForUserPropertyName(keyNotPresent)).isNull();
	}

	@Test
	public void shouldNotParseValueWhenSet() {
		sut.setValueForUserPropertyName(USER_NAME_KEY, "newValue");

		// Only 3 times in the init().
		verify(shibbolethAttributeHandlerMock, times(3)).parse(anyString());
	}

	@Test
	public void shouldReturnACopyOfTheInternalMap() {
		Map<String, String> copiedttributes = sut.toMap();

		Map<String, String> initMap = initShibbolethMap();
		Set<String> initKeys = initMap.keySet();
		assertThat(copiedttributes).isNotSameAs(initMap).containsKeys(initKeys.toArray(new String[initKeys.size()]));
	}

	@Test
	public void shouldReturnValueForAShibbolethAttributeName() {
		String shibbolethValue = sut.getValueForAttributeName(SHIB_EMAIL_KEY);

		assertThat(shibbolethValue).isEqualTo(SHIB_EMAIL_VALUE);
	}

	@Test
	public void shouldReturnNullIfNoEntryPresentForShibbolethAttributeName() {
		String keyNotPresent = "keyNotPresent";

		String shibbolethValue = sut.getValueForAttributeName(keyNotPresent);

		assertThat(shibbolethValue).isNull();
	}

	@Test
	public void shouldReturnValueForAUserPropertyName() {
		String shibbolethValue = sut.getValueForUserPropertyName(USER_EMAIL_KEY);

		assertThat(shibbolethValue).isEqualTo(SHIB_EMAIL_VALUE);
	}

	@Test
	public void shouldReturnNullIfNoEntryPresentForUserPropertyName() {
		String keyNotPresent = "keyNotPresent";

		String shibbolethValue = sut.getValueForUserPropertyName(keyNotPresent);

		assertThat(shibbolethValue).isNull();
	}

	@Test
	public void shouldReturnTrueIfManyAttributesHaveChanged() {
		User user = getIdenticalOlatUser();
		user.setProperty(USER_NAME_KEY, USER_OLD_VALUE);
		user.setProperty(USER_GENDER_KEY, USER_OLD_VALUE);

		boolean hasDifference = sut.hasDifference(user);

		assertThat(hasDifference).isTrue();
	}

	@Test
	public void shouldReturnTrueIfOneAttributeHaveChanged() {
		User user = getIdenticalOlatUser();
		user.setProperty(USER_NAME_KEY, USER_OLD_VALUE);

		boolean hasDifference = sut.hasDifference(user);

		assertThat(hasDifference).isTrue();
	}

	@Test
	public void shouldReturnTrueIfAnAttributeIsNotNullAnymore() {
		User user = getIdenticalOlatUser();
		user.setProperty(USER_GENDER_KEY, null);

		boolean hasDifference = sut.hasDifference(user);

		assertThat(hasDifference).isTrue();
	}

	@Test
	public void shouldReturnTrueIfAnAttributeHasNoValueAnymore() {
		User user = getIdenticalOlatUser();
		user.setProperty(USER_CITY_KEY, USER_OLD_VALUE);

		boolean hasDifference = sut.hasDifference(user);

		assertThat(hasDifference).isTrue();
	}

	@Test
	public void shouldReturnFalseIfNoAttributeHasChanged() {
		User user = getIdenticalOlatUser();

		boolean hasDifference = sut.hasDifference(user);

		assertThat(hasDifference).isFalse();
	}

	@Test
	public void shouldNotChangeProperyOfSyncedUserIfSameValue() {
		User user = getIdenticalOlatUser();

		User syncedUser = sut.syncUser(user);

		assertThat(syncedUser.getProperty(USER_EMAIL_KEY, null)).isEqualTo(SHIB_EMAIL_VALUE);
	}

	@Test
	public void shouldChangeChangedPropertyOfSyncedUser() {
		User user = getIdenticalOlatUser();
		user.setProperty(USER_NAME_KEY, USER_OLD_VALUE);

		User syncedUser = sut.syncUser(user);

		assertThat(syncedUser.getProperty(USER_CITY_KEY, null)).isEqualTo(SHIB_CITY_VALUE);
	}

	@Test
	public void shouldChangePropertyOfSyncedUserIfItWasNotPresent() {
		User user = getIdenticalOlatUser();
		user.setProperty(USER_NAME_KEY, null);

		User syncedUser = sut.syncUser(user);

		assertThat(syncedUser.getProperty(USER_CITY_KEY, null)).isEqualTo(SHIB_CITY_VALUE);
	}

	@Test
	public void shouldNotChangeNullValueOfSyncedUser() {
		User user = getIdenticalOlatUser();

		User syncedUser = sut.syncUser(user);

		assertThat(syncedUser.getProperty(USER_CITY_KEY, null)).isEqualTo(SHIB_CITY_VALUE);
	}

	@Test
	public void shouldRemovePropertyFromSyncedUserIfNotPresentInShibboleth() {
		User user = getIdenticalOlatUser();
		user.setProperty(USER_CITY_KEY, USER_OLD_VALUE);

		User syncedUser = sut.syncUser(user);

		assertThat(syncedUser.getProperty(USER_CITY_KEY, null)).isEqualTo(SHIB_CITY_VALUE);
	}

	@Test
	public void shouldNotBeAnAuthorIfAuthorMappingIsDisabled() {
		when(shibbolethModuleMock.isAuthorMappingEnabled()).thenReturn(false);

		boolean isAuthor = sut.isAuthor();

		assertThat(isAuthor).isFalse();
	}

	@Test
	public void shouldNotBeAnAuthorIfAttributeDoesNotContainValue() {
		when(shibbolethModuleMock.isAuthorMappingEnabled()).thenReturn(true);
		when(shibbolethModuleMock.getAuthorMappingAttributeName()).thenReturn(SHIB_NAME_KEY);
		when(shibbolethModuleMock.getAuthorMappingContains()).thenReturn("notContained");

		boolean isAuthor = sut.isAuthor();

		assertThat(isAuthor).isFalse();
	}

	@Test
	public void shouldBeAnAuthorIfAttributeContainsValue() {
		when(shibbolethModuleMock.isAuthorMappingEnabled()).thenReturn(true);
		when(shibbolethModuleMock.getAuthorMappingAttributeName()).thenReturn(SHIB_NAME_KEY);
		when(shibbolethModuleMock.getAuthorMappingContains()).thenReturn("mi");

		boolean isAuthor = sut.isAuthor();

		assertThat(isAuthor).isTrue();
	}

	@SuppressWarnings("serial")
	private class TestableUser extends UserImpl {

		private Map<String, String> properties = new HashMap<>();

		@Override
		public String getProperty(String propertyName, Locale locale) {
			return properties.get(propertyName);
		}

		@Override
		public void setProperty(String propertyName, String propertyValue) {
			properties.put(propertyName, propertyValue);
		}

	}

}
