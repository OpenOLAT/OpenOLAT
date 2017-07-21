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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.olat.core.id.User;
import org.olat.core.id.UserConstants;
import org.olat.shibboleth.ShibbolethModule;
import org.olat.test.OlatTestCase;
import org.olat.user.UserImpl;

/**
 *
 * Initial date: 19.07.2017<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class ShibbolethManagerImplTest extends OlatTestCase {

	private final String DEFAULT_SHIBBOLETH_ATTRIBUTE_HANDLER = "DoNothingHandler";
	private final String LAST_NAME_SHIB_KEY = "lastNameShibKey";
	private final String LAST_NAME_USER_KEY = UserConstants.LASTNAME;
	private final String LAST_NAME_VALUE = "lastName";
	private final String USER_PROPERTY_SHIB_KEY = "userPropertyShibKey";
	private final String USER_PROPERTY_USER_KEY = "city";
	private final String USER_PROPERTY_VALUE = "userProperty";
	private final String EXISTING_VALUE = "existingValue";
	private final Map<String, String> userMapping = new HashMap<>();
	private final Map<String, String> shibbolethAttributes = new HashMap<>();

	@Mock
	private ShibbolethModule shibbolethModuleMock;

	@InjectMocks
	private ShibbolethManagerImpl sut;

	@Before
	public void setUp() {
		// init mocks
		MockitoAnnotations.initMocks(this);
		when(shibbolethModuleMock.getUserMapping()).thenReturn(userMapping);
		when(shibbolethModuleMock.getShibbolethAttributeHandlerName(any(String.class)))
				.thenReturn(DEFAULT_SHIBBOLETH_ATTRIBUTE_HANDLER);

		// init mapping
		userMapping.put(LAST_NAME_SHIB_KEY, LAST_NAME_USER_KEY);
		userMapping.put(USER_PROPERTY_SHIB_KEY, USER_PROPERTY_USER_KEY);
	}

	@Test
	public void syncAttributesShouldAddAttribute() {
		User user = new UserImpl();
		shibbolethAttributes.put(LAST_NAME_SHIB_KEY, LAST_NAME_VALUE);
		shibbolethAttributes.put(USER_PROPERTY_SHIB_KEY, USER_PROPERTY_VALUE);

		user = sut.syncAttributes(user, shibbolethAttributes);

		assertThat(user.getLastName()).isEqualTo(LAST_NAME_VALUE);
		assertThat(user.getProperty(USER_PROPERTY_USER_KEY, null)).isEqualTo(USER_PROPERTY_VALUE);
	}

	@Test
	public void syncAttributesShouldRemovePropertyIfAttributeHasNoValue() {
		User user = new UserImpl();
		user.setProperty(USER_PROPERTY_USER_KEY, EXISTING_VALUE);

		user = sut.syncAttributes(user, shibbolethAttributes);

		assertThat(user.getProperty(USER_PROPERTY_USER_KEY, null)).isNull();
	}

}
