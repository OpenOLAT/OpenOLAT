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
package org.olat.core.util.http;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

/**
 * 
 * Initial date: 9 oct. 2026<br>
 * @author srosse, stephane.rosse@frentix.com, https://www.frentix.com
 *
 */
@RunWith(Parameterized.class)
public class OriginValidatorTest {
	
	private static final List<String> DOMAINS = List.of("olat.example.com", " *.Frentix.com ", "");
	
	@Parameters
	public static Collection<Object[]> data() {
		return Arrays.asList(new Object[][] {
			// origin, allowed with HTTPS required, allowed with HTTP permitted
			{ "https://olat.example.com", Boolean.TRUE, Boolean.TRUE },
			{ "https://olat.example.com/", Boolean.TRUE, Boolean.TRUE },
			{ "https://OLAT.Example.COM", Boolean.TRUE, Boolean.TRUE },
			{ "HTTPS://olat.example.com", Boolean.TRUE, Boolean.TRUE },
			{ " https://olat.example.com ", Boolean.TRUE, Boolean.TRUE },
			{ "https://olat.example.com.", Boolean.TRUE, Boolean.TRUE },
			// Referer like values
			{ "https://olat.example.com/auth/RepositoryEntry/1234?x=1#frag", Boolean.TRUE, Boolean.TRUE },
			// Scheme
			{ "http://olat.example.com", Boolean.FALSE, Boolean.TRUE },
			{ "ftp://olat.example.com", Boolean.FALSE, Boolean.FALSE },
			{ "ws://olat.example.com", Boolean.FALSE, Boolean.FALSE },
			{ "javascript:alert(1)", Boolean.FALSE, Boolean.FALSE },
			{ "olat.example.com", Boolean.FALSE, Boolean.FALSE },
			{ "//olat.example.com", Boolean.FALSE, Boolean.FALSE },
			// Other hosts
			{ "https://evil.com", Boolean.FALSE, Boolean.FALSE },
			{ "https://example.com", Boolean.FALSE, Boolean.FALSE },
			{ "https://evilolat.example.com", Boolean.FALSE, Boolean.FALSE },
			{ "https://sub.olat.example.com", Boolean.FALSE, Boolean.FALSE },
			{ "https://olat.example.com.evil.com", Boolean.FALSE, Boolean.FALSE },
			{ "https://olat_example.com", Boolean.FALSE, Boolean.FALSE },
			{ "https://[::1]", Boolean.FALSE, Boolean.FALSE },
			// User info
			{ "https://olat.example.com@evil.com", Boolean.FALSE, Boolean.FALSE },
			{ "https://evil.com@olat.example.com", Boolean.FALSE, Boolean.FALSE },
			{ "https://user:pwd@olat.example.com", Boolean.FALSE, Boolean.FALSE },
			// Wildcard
			{ "https://www.frentix.com", Boolean.TRUE, Boolean.TRUE },
			{ "https://a.b.frentix.com", Boolean.TRUE, Boolean.TRUE },
			{ "http://www.frentix.com", Boolean.FALSE, Boolean.TRUE },
			{ "https://frentix.com", Boolean.FALSE, Boolean.FALSE },
			{ "https://evilfrentix.com", Boolean.FALSE, Boolean.FALSE },
			{ "https://www.frentix.com.evil.com", Boolean.FALSE, Boolean.FALSE },
			// Garbage
			{ null, Boolean.FALSE, Boolean.FALSE },
			{ "", Boolean.FALSE, Boolean.FALSE },
			{ "   ", Boolean.FALSE, Boolean.FALSE },
			{ "null", Boolean.FALSE, Boolean.FALSE },
			{ "https://olat.example.com\r\nX-Evil: 1", Boolean.FALSE, Boolean.FALSE },
			{ "https://", Boolean.FALSE, Boolean.FALSE }
		});
	}
	
	private final String origin;
	private final Boolean allowedHttpsOnly;
	private final Boolean allowedHttp;
	
	public OriginValidatorTest(String origin, Boolean allowedHttpsOnly, Boolean allowedHttp) {
		this.origin = origin;
		this.allowedHttpsOnly = allowedHttpsOnly;
		this.allowedHttp = allowedHttp;
	}
	
	@Test
	public void isAllowedHttpsRequired() {
		OriginValidator validator = new OriginValidator(DOMAINS, true);
		Assert.assertEquals(allowedHttpsOnly, Boolean.valueOf(validator.isAllowed(origin)));
	}
	
	@Test
	public void isAllowedHttpPermitted() {
		OriginValidator validator = new OriginValidator(DOMAINS, false);
		Assert.assertEquals(allowedHttp, Boolean.valueOf(validator.isAllowed(origin)));
	}
}
