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
package org.olat.core.commons.services.webdav;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.UUID;

import junit.framework.Assert;

import org.apache.http.HttpResponse;
import org.apache.http.util.EntityUtils;
import org.apache.poi.util.IOUtils;
import org.junit.Test;
import org.olat.core.id.Identity;
import org.olat.test.JunitTestHelper;

/**
 * 
 * Test the commands against the WedDAV implementation of OpenOLAT
 * 
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class WebDAVCommandsTest extends WebDAVTestCase {
	
	
	@Test
	public void testPropFind()
	throws IOException, URISyntaxException {
		//create a user
		Identity user = JunitTestHelper.createAndPersistIdentityAsUser("webdav-1-" + UUID.randomUUID().toString());
		
		//list root content of its webdav folder
		WebDAVConnection conn = new WebDAVConnection();
		conn.setCredentials(user.getName(), "A6B7C8");
		URI uri = conn.getContextURI().build();
		HttpResponse response = conn.propfind(uri);
		assertEquals(207, response.getStatusLine().getStatusCode());
		
		String xml = EntityUtils.toString(response.getEntity());
		Assert.assertTrue(xml.indexOf("/webdav/coursefolders/") > 0);
		
		IOUtils.closeQuietly(conn);
	}

}
