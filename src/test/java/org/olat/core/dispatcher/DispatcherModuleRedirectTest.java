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
package org.olat.core.dispatcher;

import java.util.Arrays;
import java.util.Collection;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import org.springframework.mock.web.MockHttpServletResponse;

/**
 * 
 * Initial date: 6 sept. 2021<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
@RunWith(Parameterized.class)
public class DispatcherModuleRedirectTest {
	
	@Parameters
    public static Collection<Object[]> data() {
        return Arrays.asList(new Object[][] {
                { "http://localhost:8080/auth/_version_/images/pixel.gif", 302, Boolean.TRUE },
                { "http://localhost:8080/auth/_version_/images/'<script>alert(0)-'", 404, Boolean.FALSE },
                // standard business path
                { "http://localhost:8080/auth/RepositoryEntry/23547/Entry/265746527", 302, Boolean.TRUE },
                // business path with a path
                { "http://localhost:8080/url/RepositoryEntry/163741700/path%3D~~Test%2Ehtml/0", 302, Boolean.TRUE }
        });
    }
	
	private Boolean valid;
	private int status;
	private String url;
	
	public DispatcherModuleRedirectTest(String url, Integer status, Boolean valid) {
		this.url = url;
		this.status = status;
		this.valid = valid;
	}
	
	@Test
	public void redirectSecureTo() {
		MockHttpServletResponse response = new MockHttpServletResponse();
		boolean result = DispatcherModule.redirectSecureTo(response, url);
		Assert.assertEquals(status, response.getStatus());
		Assert.assertEquals(valid, Boolean.valueOf(result));
	}
}
