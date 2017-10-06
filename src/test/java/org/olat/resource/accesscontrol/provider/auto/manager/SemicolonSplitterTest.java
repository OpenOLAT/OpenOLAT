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

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.junit.Test;


/**
 *
 * Initial date: 23.08.2017<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class SemicolonSplitterTest {

	private final static String SEMICOLON = ";";

	private SemicolonSplitter sut = new SemicolonSplitter();

	@Test
	public void shouldReturnMultipleValues() {
		String value1 = "single,3";
		String value2 = "multiple";
		String value3 = "value3";
		String rawValue = value1 + SEMICOLON + value2 + SEMICOLON + value3 + SEMICOLON;
		List<String> rawValues = Arrays.asList(value1, value2, value3);

		Collection<String> values = sut.split(rawValue);

		assertThat(values).hasSize(rawValues.size()).hasSameElementsAs(rawValues);
	}

	@Test
	public void shouldReturnSingleValue() {
		String singleValue = "single,3";
		List<String> rawValues = Arrays.asList(singleValue);

		Collection<String> values = sut.split(singleValue);

		assertThat(values).hasSize(rawValues.size()).hasSameElementsAs(rawValues);
	}

	@Test
	public void shouldReturnEmptyCollectionIfNullInput() {
		Collection<String> values = sut.split(null);

		assertThat(values).hasSize(0);
	}
	@Test
	public void shouldReturnEmptyCollectionIfEmptyStringInput() {
		Collection<String> values = sut.split("");

		assertThat(values).hasSize(0);
	}
}
