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
package org.olat.modules.opencast;

import org.assertj.core.api.SoftAssertions;
import org.junit.Test;

/**
 * 
 * Initial date: 19 Aug 2020<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class WildcardFilterTest {
	
	@Test
	public void shouldFilterWithoutWildcard() {
		WildcardFilter sut = new WildcardFilter("abc", o -> (String)o);
		
		SoftAssertions softly = new SoftAssertions();
		assertThat(softly, sut, "abc", true);
		assertThat(softly, sut, "abc1", true);
		assertThat(softly, sut, "1abc", true);
		assertThat(softly, sut, "ABC", true);
		assertThat(softly, sut, "Abc", true);
		assertThat(softly, sut, "UaBc%", true);
		assertThat(softly, sut, "a1bc", false);
		assertThat(softly, sut, "ab2c", false);
		softly.assertAll();
	}
	
	@Test
	public void shouldFilterWithWildcard() {
		WildcardFilter sut = new WildcardFilter("ab*c", o -> (String)o);
		
		SoftAssertions softly = new SoftAssertions();
		assertThat(softly, sut, "abc", true);
		assertThat(softly, sut, "abc1", true);
		assertThat(softly, sut, "1abc", true);
		assertThat(softly, sut, "ABC", true);
		assertThat(softly, sut, "Abc", true);
		assertThat(softly, sut, "UaBc%", true);
		assertThat(softly, sut, "a1bc", false);
		assertThat(softly, sut, "ab2c", true);
		softly.assertAll();
	}
	
	@Test
	public void shouldFilterWithWildcardOnly() {
		WildcardFilter sut = new WildcardFilter("*", o -> (String)o);
		
		SoftAssertions softly = new SoftAssertions();
		assertThat(softly, sut, "abc", true);
		assertThat(softly, sut, "abc1", true);
		assertThat(softly, sut, "1abc", true);
		assertThat(softly, sut, "ABC", true);
		assertThat(softly, sut, "Abc", true);
		assertThat(softly, sut, "UaBc%", true);
		assertThat(softly, sut, "a1bc", true);
		assertThat(softly, sut, "ab2c", true);
		softly.assertAll();
	}
	
	private void assertThat(SoftAssertions softly, WildcardFilter sut, String value, boolean expected) {
		softly.assertThat(sut.test(value)).as(value).isEqualTo(expected);
	}

}
