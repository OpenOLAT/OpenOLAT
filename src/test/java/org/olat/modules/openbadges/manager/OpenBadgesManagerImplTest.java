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
package org.olat.modules.openbadges.manager;

import org.olat.modules.openbadges.OpenBadgesManager;
import org.olat.test.OlatTestCase;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

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
public class OpenBadgesManagerImplTest extends OlatTestCase {

	@Autowired
	OpenBadgesManager openBadgesManager;

	@Test
	public void testBake() {
		if (openBadgesManager instanceof OpenBadgesManagerImpl managerImpl) {
			managerImpl.bakeBadge(OpenBadgesManager.FileType.png, "test.png");
		}
	}

	@Test
	public void testInsertingJsonIntoSvg() {
		String svg = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>\n" +
				"<!DOCTYPE svg PUBLIC \"-//W3C//DTD SVG 1.1//EN\" \"http://www.w3.org/Graphics/SVG/1.1/DTD/svg11.dtd\">\n" +
				"<svg width=\"100%\" height=\"100%\" viewBox=\"0 0 200 201\" version=\"1.1\"\n" +
				"     xmlns=\"http://www.w3.org/2000/svg\" xmlns:xlink=\"http://www.w3.org/1999/xlink\" xml:space=\"preserve\"\n" +
				"     xmlns:serif=\"http://www.serif.com/\"\n" +
				"     style=\"fill-rule:evenodd;clip-rule:evenodd;stroke-linecap:round;stroke-linejoin:round;stroke-miterlimit:1.5;\">\n" +
				"    <g transform=\"matrix(1,0,0,1,-651,-428)\">\n" +
				"    </g>\n" +
				"</svg>";

		String json = "{\n" +
				" \"@context\": \"https://w3id.org/openbadges/v2\",\n" +
				" \"id\": \"https://example.org/assertions/123\",\n" +
				" \"type\": \"Assertion\"\n" +
				"}";

		if (openBadgesManager instanceof OpenBadgesManagerImpl managerImpl) {
			String mergedSvg = managerImpl.mergeAssertionJson(svg, json, "https://test.openolat.org/badge/assertion/123");
			System.err.println(mergedSvg);
		}
	}
}