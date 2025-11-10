/**
 * <a href="https://www.openolat.org">
 * OpenOLAT - Online Learning and Training</a><br>
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); <br>
 * you may not use this file except in compliance with the License.<br>
 * You may obtain a copy of the License at the
 * <a href="https://www.apache.org/licenses/LICENSE-2.0">Apache homepage</a>
 * <p>
 * Unless required by applicable law or agreed to in writing,<br>
 * software distributed under the License is distributed on an "AS IS" BASIS, <br>
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
 * See the License for the specific language governing permissions and <br>
 * limitations under the License.
 * <p>
 * Initial code contributed and copyrighted by<br>
 * frentix GmbH, https://www.frentix.com
 * <p>
 */
package org.olat.modules.assessment;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;

/**
 * 
 * Initial date: Nov 7, 2025<br>
 * @author uhensler, urs.hensler@frentix.com, https://www.frentix.com
 *
 */
public class UserDisplayIdentifierGeneratorTest {

	@Test
	public void shouldGenerate_format() {
		String identifier = UserDisplayIdentifierGenerator.generate();
		
		assertThat(identifier).hasSize(7);
		assertThat(identifier.substring(0, 2)).isAlphabetic();
		assertThat(identifier.substring(3, 4)).isEqualTo("-");
		assertThat(identifier.substring(4, 6)).isAlphabetic();
	}

}
