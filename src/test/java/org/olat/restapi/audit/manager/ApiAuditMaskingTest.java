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
package org.olat.restapi.audit.manager;

import org.assertj.core.api.Assertions;
import org.junit.Test;

/**
 * Initial date: 16 sept. 2026<br>
 * @author srosse, stephane.rosse@frentix.com, https://www.frentix.com
 */
public class ApiAuditMaskingTest {
	
	@Test
	public void maskJsonTopLevelPassword() {
		String masked = ApiAuditMasking.maskJson("{\"login\":\"alice\",\"password\":\"s3cret\"}");
		Assertions.assertThat(masked)
			.contains("\"password\":\"***\"")
			.contains("\"login\":\"alice\"")
			.doesNotContain("s3cret");
	}
	
	@Test
	public void maskJsonNestedAndArray() {
		String json = "{\"users\":[{\"name\":\"a\",\"credential\":\"x\"}],\"auth\":{\"clientSecret\":\"y\",\"secret\":\"z\"}}";
		String masked = ApiAuditMasking.maskJson(json);
		Assertions.assertThat(masked)
			.doesNotContain("\"x\"").doesNotContain("\"y\"").doesNotContain("\"z\"")
			.contains("\"name\":\"a\"");
	}
	
	@Test
	public void maskJsonCaseInsensitiveKey() {
		String masked = ApiAuditMasking.maskJson("{\"Password\":\"p\",\"CLIENTSECRET\":\"c\"}");
		Assertions.assertThat(masked)
			.doesNotContain("\"p\"").doesNotContain("\"c\"")
			.contains("\"Password\":\"***\"")
			.contains("\"CLIENTSECRET\":\"***\"");
	}
	
	@Test
	public void maskJsonInvalidJsonUsesRegexFallback() {
		String truncated = "{\"login\":\"alice\",\"password\":\"s3cret\",\"desc\":\"lo...[truncated]";
		String masked = ApiAuditMasking.maskJson(truncated);
		Assertions.assertThat(masked)
			.doesNotContain("s3cret")
			.contains("\"password\":\"***\"")
			.contains("...[truncated]");
	}
	
	@Test
	public void maskJsonNonSecretUnchanged() {
		String json = "{\"a\":1,\"b\":[true,null],\"c\":{\"d\":\"e\"}}";
		Assertions.assertThat(ApiAuditMasking.maskJson(json)).isEqualTo(json);
	}
	
	@Test
	public void maskJsonNullAndEmpty() {
		Assertions.assertThat(ApiAuditMasking.maskJson(null)).isNull();
		Assertions.assertThat(ApiAuditMasking.maskJson("")).isEmpty();
	}
	
	@Test
	public void maskQuerySecretParams() {
		String masked = ApiAuditMasking.maskQuery("username=alice&password=s3cret&x=1&secret=abc");
		Assertions.assertThat(masked).isEqualTo("username=alice&password=***&x=1&secret=***");
	}
	
	@Test
	public void maskQueryNoSecrets() {
		Assertions.assertThat(ApiAuditMasking.maskQuery("start=0&limit=25")).isEqualTo("start=0&limit=25");
		Assertions.assertThat(ApiAuditMasking.maskQuery(null)).isNull();
		Assertions.assertThat(ApiAuditMasking.maskQuery("")).isEmpty();
	}
	
	@Test
	public void maskQueryFlagWithoutValue() {
		Assertions.assertThat(ApiAuditMasking.maskQuery("password&x=1")).isEqualTo("password&x=1");
	}
}
