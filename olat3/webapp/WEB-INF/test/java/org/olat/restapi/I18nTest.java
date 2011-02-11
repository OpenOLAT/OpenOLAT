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
 * <p>
 */

package org.olat.restapi;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.net.URI;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriBuilder;

import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.methods.GetMethod;
import org.junit.Test;
import org.olat.test.OlatJerseyTestCase;

/**
 * Description:<br>
 * Test the translation service
 * 
 * <P>
 * Initial Date:  14 apr. 2010 <br>
 * @author srosse, stephane.rosse@frentix.com
 */
public class I18nTest extends OlatJerseyTestCase {

	public I18nTest() {
		super();
  }	
	
	@Test
	public void testExecuteService() throws HttpException, IOException {
		URI uri = UriBuilder.fromUri(getContextURI()).path("i18n").path("org.olat.core").path("ok").build();
		GetMethod method = createGet(uri, MediaType.TEXT_PLAIN, false);
		int code = getHttpClient().executeMethod(method);
		assertEquals(200, code);
		String response = method.getResponseBodyAsString();
		assertEquals("OK", response);
  }
}
