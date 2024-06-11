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
package org.olat.modules.openbadges;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import org.olat.core.util.StringHelper;

/**
 * Initial date: 2024-06-10<br>
 *
 * @author cpfranger, christoph.pfranger@frentix.com, <a href="https://www.frentix.com">https://www.frentix.com</a>
 */
public class LinkedInUrl {

	public enum Field {
		name,
		organizationName,
		issueYear,
		issueMonth,
		expirationYear,
		expirationMonth,
		certId,
		certUrl,
	}

	private final Map<Field, String> fields = new HashMap<>();

	private void add(Field field, String value) {
		fields.put(field, StringHelper.xssScan(value));
	}

	private String asUrl() {
		return fields.keySet().stream()
				.map(f -> f.name() + "=" + StringHelper.urlEncodeUTF8(fields.get(f))).collect(Collectors.joining("&"));
	}

	public static class LinkedInUrlBuilder {
		private static final String URL_BASE = "https://www.linkedin.com/profile/add?startTask=CERTIFICATION_NAME&";

		private final LinkedInUrl linkedInUrl = new LinkedInUrl();

		public LinkedInUrlBuilder add(Field field, String value) {
			linkedInUrl.add(field, value);
			return this;
		}

		public String asUrl() {
			return URL_BASE + linkedInUrl.asUrl();
		}
	}
}
