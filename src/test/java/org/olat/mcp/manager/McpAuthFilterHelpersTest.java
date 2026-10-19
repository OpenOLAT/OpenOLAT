/**
 * <a href="https://www.openolat.org">
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
 * frentix GmbH, https://www.frentix.com
 * <p>
 */
package org.olat.mcp.manager;

import org.assertj.core.api.Assertions;
import org.junit.Test;
import org.olat.core.helpers.Settings;
import org.olat.test.OlatTestCase;
import org.springframework.mock.web.MockHttpServletRequest;

/**
 * The static helpers of the filter: bearer parsing and the Origin check.
 *
 * Initial date: 2 Oct 2026<br>
 * @author gnaegi, gnaegi@frentix.com, https://www.frentix.com
 *
 */
public class McpAuthFilterHelpersTest extends OlatTestCase {

	@Test
	public void bearerOf() {
		MockHttpServletRequest request = new MockHttpServletRequest();
		Assertions.assertThat(McpAuthFilter.bearerOf(request)).isNull();

		request.addHeader("Authorization", "Bearer oomcp_abc");
		Assertions.assertThat(McpAuthFilter.bearerOf(request)).isEqualTo("oomcp_abc");

		MockHttpServletRequest lower = new MockHttpServletRequest();
		lower.addHeader("Authorization", "  bearer   oomcp_xyz  ");
		Assertions.assertThat(McpAuthFilter.bearerOf(lower)).isEqualTo("oomcp_xyz");

		MockHttpServletRequest basic = new MockHttpServletRequest();
		basic.addHeader("Authorization", "Basic dXNlcjpwdw==");
		Assertions.assertThat(McpAuthFilter.bearerOf(basic)).isNull();

		MockHttpServletRequest empty = new MockHttpServletRequest();
		empty.addHeader("Authorization", "Bearer ");
		Assertions.assertThat(McpAuthFilter.bearerOf(empty)).isNull();
	}

	@Test
	public void originAllowed() {
		String own = Settings.getServerContextPathURI();
		Assertions.assertThat(own).isNotBlank();

		MockHttpServletRequest none = new MockHttpServletRequest();
		Assertions.assertThat(McpAuthFilter.isOriginAllowed(none)).isTrue();

		MockHttpServletRequest same = new MockHttpServletRequest();
		same.addHeader("Origin", own.replaceFirst("(https?://[^/]+).*", "$1"));
		Assertions.assertThat(McpAuthFilter.isOriginAllowed(same)).isTrue();

		MockHttpServletRequest foreign = new MockHttpServletRequest();
		foreign.addHeader("Origin", "https://evil.example.org");
		Assertions.assertThat(McpAuthFilter.isOriginAllowed(foreign)).isFalse();

		MockHttpServletRequest nullOrigin = new MockHttpServletRequest();
		nullOrigin.addHeader("Origin", "null");
		Assertions.assertThat(McpAuthFilter.isOriginAllowed(nullOrigin)).isFalse();

		MockHttpServletRequest garbage = new MockHttpServletRequest();
		garbage.addHeader("Origin", "::not a uri::");
		Assertions.assertThat(McpAuthFilter.isOriginAllowed(garbage)).isFalse();
	}
}
