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
package org.olat.core.gui.media;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import org.junit.Test;
import org.mockito.ArgumentCaptor;

import jakarta.servlet.http.HttpServletResponse;

/**
 *
 * Initial date: 14 Oct 2026<br>
 * @author uhensler, urs.hensler@frentix.com, https://www.frentix.com
 *
 */
public class ServletUtilTest {

	@Test
	public void setCacheHeadersOneDay() {
		HttpServletResponse response = mock(HttpServletResponse.class);
		ArgumentCaptor<Long> expires = ArgumentCaptor.forClass(Long.class);

		long before = System.currentTimeMillis();
		ServletUtil.setCacheHeaders(response, ServletUtil.CACHE_ONE_DAY);
		long after = System.currentTimeMillis();

		verify(response).addHeader("Cache-Control", "max-age=86400");
		verify(response).setDateHeader(eq("Expires"), expires.capture());
		assertThat(expires.getValue()).isBetween(before + 86_400_000l, after + 86_400_000l);
	}

	@Test
	public void setCacheHeadersNoCache() {
		HttpServletResponse response = mock(HttpServletResponse.class);

		ServletUtil.setCacheHeaders(response, ServletUtil.CACHE_NO_CACHE);

		verify(response).setHeader(eq("Cache-Control"), anyString());
		verify(response).setHeader("Pragma", "no-cache");
		verify(response).setDateHeader("Expires", 0);
	}

}
