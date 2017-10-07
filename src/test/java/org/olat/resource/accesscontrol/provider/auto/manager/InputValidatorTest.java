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
package org.olat.resource.accesscontrol.provider.auto.manager;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.HashSet;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;
import org.olat.basesecurity.IdentityImpl;
import org.olat.resource.accesscontrol.provider.auto.AdvanceOrderInput;
import org.olat.resource.accesscontrol.provider.auto.IdentifierKey;
import org.olat.resource.accesscontrol.provider.auto.model.AutoAccessMethod;

/**
 *
 * Initial date: 17.08.2017<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class InputValidatorTest {

	private AdvanceOrderInput inputMock;

	private InputValidator sut = new InputValidator();

	@Before
	public void setUp() {
		inputMock = mock(AdvanceOrderInput.class);

		when(inputMock.getIdentity()).thenReturn(new IdentityImpl());
		Set<IdentifierKey> keys = new HashSet<>();
		keys.add(IdentifierKey.externalId);
		when(inputMock.getKeys()).thenReturn(keys);
		when(inputMock.getRawValues()).thenReturn("rawValues");
		doReturn(AutoAccessMethod.class).when(inputMock).getMethodClass();
	}

	@Test
	public void shouldNotBeValidIfInputIsNull() {
		boolean isValid = sut.isValid(null);

		assertThat(isValid).isFalse();
	}

	@Test
	public void shouldNotBeValidIfIdentityIsNull() {
		when(inputMock.getIdentity()).thenReturn(null);

		boolean isValid = sut.isValid(inputMock);

		assertThat(isValid).isFalse();
	}

	@Test
	public void shouldNotBeValidIfKeysIsNull() {
		when(inputMock.getKeys()).thenReturn(null);

		boolean isValid = sut.isValid(inputMock);

		assertThat(isValid).isFalse();
	}

	@Test
	public void shouldNotBeValidIfEmptyKeys() {
		when(inputMock.getKeys()).thenReturn(new HashSet<>(0));

		boolean isValid = sut.isValid(inputMock);

		assertThat(isValid).isFalse();
	}

	@Test
	public void shouldNotBeValidIfMethodtypeIsNull() {
		when(inputMock.getMethodClass()).thenReturn(null);

		boolean isValid = sut.isValid(inputMock);

		assertThat(isValid).isFalse();
	}

	@Test
	public void shouldNotBeValidIfRawValuesIsNull() {
		when(inputMock.getRawValues()).thenReturn(null);

		boolean isValid = sut.isValid(inputMock);

		assertThat(isValid).isFalse();
	}


	@Test
	public void shouldNotBeValidIfRawValuesIsEmptyString() {
		when(inputMock.getRawValues()).thenReturn("");

		boolean isValid = sut.isValid(inputMock);

		assertThat(isValid).isFalse();
	}

	@Test
	public void shouldNotBeValidIfMethodDoesNotExist() {
		when(inputMock.getIdentity()).thenReturn(null);

		boolean isValid = sut.isValid(inputMock);

		assertThat(isValid).isFalse();
	}

}
