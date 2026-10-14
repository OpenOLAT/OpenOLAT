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
package org.olat.modules.catalog;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;
import org.springframework.mock.web.MockHttpServletRequest;

/**
 *
 * Initial date: 14 Oct 2026<br>
 * @author uhensler, urs.hensler@frentix.com, https://www.frentix.com
 *
 */
public class WebCatalogDispatcherTest {

	private final WebCatalogDispatcher sut = new WebCatalogDispatcher();

	@Test
	public void sitemapRequiresNoSession() {
		assertThat(sut.isSessionRequired(request("/catalog/sitemap.xml"))).isFalse();
	}

	@Test
	public void sitemapIsCaseInsensitive() {
		assertThat(sut.isSessionRequired(request("/catalog/Sitemap.XML"))).isFalse();
	}

	@Test
	public void pagesRequireSession() {
		assertThat(sut.isSessionRequired(request("/catalog/"))).isTrue();
		assertThat(sut.isSessionRequired(request("/catalog/Infos/0"))).isTrue();
	}

	private MockHttpServletRequest request(String pathInfo) {
		MockHttpServletRequest request = new MockHttpServletRequest("GET", "/olat" + pathInfo);
		request.setContextPath("/olat");
		request.setPathInfo(pathInfo);
		return request;
	}
}
