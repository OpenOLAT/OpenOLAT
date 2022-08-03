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
package org.olat.ims.lti13;

import java.util.Arrays;
import java.util.Collection;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import org.olat.ims.lti13.model.LTI13ToolDeploymentImpl;
import org.olat.ims.lti13.model.LTI13ToolImpl;


/**
 * 
 * Initial date: 3 août 2022<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
@RunWith(Parameterized.class)
public class LTI13PlatformDispatcherDelegateRedirectTest {
	
	@Parameters
    public static Collection<Object[]> data() {
        return Arrays.asList(new Object[][] {
                { "https://openolat.frentix.com/imathas/lti/launch.php\nhttps://openolat.frentix.com/imathas/lti/login.php", "https://openolat.frentix.com/imathas/lti/login.php", Boolean.TRUE },
                // With parameters both sides
                { "https://openolat.frentix.com/?lti-tool\r\nhttps://openolat.frentix.com/?lti-tool&keys", "https://openolat.frentix.com/?lti-tool", Boolean.TRUE },
                // With parameters only in configuration
                { "https://openolat.frentix.com/lti/auth/?lti-tool", "https://openolat.frentix.com/lti/auth/", Boolean.TRUE },
                { "https://openolat.frentix.com/lti\r\nhttps://openolat.frentix.com/lti13", "https://openolat.frentix.com/", Boolean.FALSE },
        });
    }
	
    private String url;
    private Boolean allowed;
    private String allowedRedirectUrls;
	private final LTI13PlatformDispatcherDelegate platformDispatcherDelegate = new LTI13PlatformDispatcherDelegate();
	
	public LTI13PlatformDispatcherDelegateRedirectTest(String allowedRedirectUrls, String url, Boolean allowed) {
		this.url = url;
		this.allowed = allowed;
		this.allowedRedirectUrls = allowedRedirectUrls;
	}
	
	@Test
	public void isRedirectUriAllowed() {
		LTI13ToolImpl tool = new LTI13ToolImpl();
		tool.setRedirectUrl(allowedRedirectUrls);
		
		LTI13ToolDeploymentImpl deployment = new LTI13ToolDeploymentImpl();
		deployment.setTool(tool);
		boolean redirectAllowed = platformDispatcherDelegate.isRedirectUriAllowed(url, deployment);
		Assert.assertEquals(allowed, Boolean.valueOf(redirectAllowed));
	}
}
