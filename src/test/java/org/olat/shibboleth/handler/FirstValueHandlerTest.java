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
public class FirstValueHandlerTest {

	FirstValueHandler sut = new FirstValueHandler();

	@Test
	public void parseShoudReturnFirstValueIfMultipleVales() {
		String expected = "abc";
		String input = expected + ";abc;222;a;erer;sdfsfd";

		String parsed = sut.parse(input);

		assertThat(parsed).isEqualTo(expected);
	}

	@Test
	public void parseShoudReturnFirstValueIfOneValue() {
		String input = "abc";

		String parsed = sut.parse(input);

		assertThat(parsed).isEqualTo(input);
	}

	@Test
	public void parseShoudReturnNoValueIfNull() {
		String parsed = sut.parse(null);

		assertThat(parsed).isNull();
	}
	

	@Test
	public void parseShoudReturnNoValueIfEmpty() {
		String parsed = sut.parse("");

		assertThat(parsed).isNull();
	}

	@Test
	public void parseShoudReturnNoValueIfOnlySemicolon() {
		String parsed = sut.parse(";");

		assertThat(parsed).isNull();
	}
	
	@Test
	public void parseShoudReturnSecondValueIfFirstIsEmpty() {
		String expected = "abc";
		String input = ";" + expected + ";abc;222;a;erer;sdfsfd";

		String parsed = sut.parse(input);

		assertThat(parsed).isEqualTo(expected);
	}
	
	@Test
	public void parseShoudReturnFourthValueIfFirstThreeAreEmpty() {
		String expected = "abc";
		String input = ";  ;;" + expected + ";abc;222;a;erer;sdfsfd";

		String parsed = sut.parse(input);

		assertThat(parsed).isEqualTo(expected);
	}
}
