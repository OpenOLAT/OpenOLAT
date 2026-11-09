/**
* OLAT - Online Learning and Training<br>
* http://www.olat.org
* <p>
* Licensed under the Apache License, Version 2.0 (the "License"); <br>
* you may not use this file except in compliance with the License.<br>
* You may obtain a copy of the License at
* <p>
* http://www.apache.org/licenses/LICENSE-2.0
* <p>
* Unless required by applicable law or agreed to in writing,<br>
* software distributed under the License is distributed on an "AS IS" BASIS, <br>
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
* See the License for the specific language governing permissions and <br>
* limitations under the License.
* <p>
* Copyright (c) since 2004 at Multimedia- & E-Learning Services (MELS),<br>
* University of Zurich, Switzerland.
* <hr>
* <a href="http://www.openolat.org">
* OpenOLAT - Online Learning and Training</a><br>
* This file has been modified by the OpenOLAT community. Changes are licensed
* under the Apache 2.0 license as the original file.  
* <p>
*/

package org.olat.restapi;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.UriBuilder;

import org.junit.Test;
import org.olat.test.OlatRestTestCase;

/**
 * Description:<br>
 * Test the translation service
 * 
 * <P>
 * Initial Date:  14 apr. 2010 <br>
 * @author srosse, stephane.rosse@frentix.com
 */
public class I18nTest extends OlatRestTestCase {
	
	@Test
	public void testExecuteService() throws IOException, URISyntaxException, InterruptedException {
		RestConnection conn = new RestConnection();
		
		URI uri = UriBuilder.fromUri(getContextURI()).path("i18n").path("org.olat.core").path("ok").build();
		HttpRequest method = conn.createGet(uri, MediaType.TEXT_PLAIN);
		HttpResponse<InputStream> response = conn.execute(method);
		assertEquals(200, response.statusCode());
		String out = RestConnection.toString(response);
		assertEquals("OK", out);
		conn.close();

  }
}
