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
package org.olat.shibboleth.handler;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;

/**
 *
 * Initial date: 21.07.2017<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class SchacGenderHandlerTest {

	private static final String OLAT_DEFAULT = "-";
	private SchacGenderHandler sut = new SchacGenderHandler();

	@Test
	public void parseShouldHandleMale() {
		String schacMale = "1";
		String olatMale = "male";

		String parsed = sut.parse(schacMale);

		assertThat(parsed).isEqualTo(olatMale);
	}

	@Test
	public void parseShouldHandleFemale() {
		String schacFemale = "2";
		String olatFemale = "female";

		String parsed = sut.parse(schacFemale);

		assertThat(parsed).isEqualTo(olatFemale);
	}

	@Test
	public void parseShouldHandleOther() {
		String randomValue = "abc";

		String parsed = sut.parse(randomValue);

		assertThat(parsed).isEqualTo(OLAT_DEFAULT);
	}

	@Test
	public void parseShouldHandleNull() {
		String parsed = sut.parse(null);

		assertThat(parsed).isEqualTo(OLAT_DEFAULT);
	}
}
