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
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Answers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.olat.shibboleth.ShibbolethModule;

/**
 *
 * Initial date: 10.08.2017<br>
 *
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class DifferenceCheckerTest {

	private static final String ATTRIBUTE_NAME = "attributeName";
	private static final String THE_SAME = "theSame";
	private static final String SHIBBOLETH_ONLY = "shibbolethOnly";
	private static final String OLAT_ONLY = "olatOnly";

	@Mock(answer = Answers.RETURNS_DEEP_STUBS)
	ShibbolethModule shibbolethModuleMock;

	@InjectMocks
	private DifferenceChecker sut;

	@Before
	public void setUp() {
		MockitoAnnotations.initMocks(this);
	}

	@Test
	public void shouldBeDifferentWhenShibOlatFalse() {
		shouldReturnDifferent(SHIBBOLETH_ONLY, OLAT_ONLY, false);
	}

	@Test
	public void shouldBeDifferentWhenShibOlatTrue() {
		shouldReturnDifferent(SHIBBOLETH_ONLY, OLAT_ONLY, true);
	}

	@Test
	public void shouldBeDifferentWhenShibNullFalse() {
		shouldReturnDifferent(SHIBBOLETH_ONLY, null, false);
	}

	@Test
	public void shouldBeDifferentWhenShibNullTrue() {
		shouldReturnDifferent(SHIBBOLETH_ONLY, null, true);
	}

	@Test
	public void shouldBeDifferentWhenNullOlatTrue() {
		shouldReturnDifferent(null, OLAT_ONLY, true);
	}

	@Test
	public void shouldBeNotDifferentWhenNullOlatfalse() {
		shouldReturnNotDifferent(null, OLAT_ONLY, false);
	}

	@Test
	public void shouldBeNotDifferentWhenSameSameFalse() {
		shouldReturnNotDifferent(THE_SAME, THE_SAME, false);
	}

	@Test
	public void shouldBeNotDifferentWhenSameSameTrue() {
		shouldReturnNotDifferent(THE_SAME, THE_SAME, false);
	}

	@Test
	public void shouldBeNotDifferentWhenNullNullFalse() {
		shouldReturnNotDifferent(null, null, false);
	}

	@Test
	public void shouldBeNotDifferentWhenNullNullTrue() {
		shouldReturnNotDifferent(null, null, true);
	}

	private void shouldReturnDifferent(String shibbolethAttributeName, String userPropertyValue, boolean deleteIfNull) {
		boolean isDiferent = prepareAndRunChceck(shibbolethAttributeName, userPropertyValue, deleteIfNull);

		assertThat(isDiferent).isTrue();
	}

	private void shouldReturnNotDifferent(String shibbolethAttributeName, String userPropertyValue, boolean deleteIfNull) {
		boolean isDiferent = prepareAndRunChceck(shibbolethAttributeName, userPropertyValue, deleteIfNull);

		assertThat(isDiferent).isFalse();
	}

	private boolean prepareAndRunChceck(String shibbolethAttributeName, String userPropertyValue, boolean deleteIfNull) {
		when(shibbolethModuleMock.getDeleteIfNull().get(ATTRIBUTE_NAME)).thenReturn(deleteIfNull);

		return sut.isDifferent(ATTRIBUTE_NAME, shibbolethAttributeName, userPropertyValue);
	}

	@Test
	public void shouldDeleteNullIfNotConfigured() {
		when(shibbolethModuleMock.getDeleteIfNull().get(ATTRIBUTE_NAME)).thenThrow(new RuntimeException());

		boolean isDifferent = sut.isDifferent(ATTRIBUTE_NAME, null, OLAT_ONLY);

		assertThat(isDifferent).isTrue();
	}
}
