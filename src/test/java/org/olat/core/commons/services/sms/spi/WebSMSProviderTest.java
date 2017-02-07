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
package org.olat.core.commons.services.sms.spi;

import java.util.UUID;

import org.junit.Assert;
import org.junit.Test;
import org.olat.test.OlatTestCase;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 3 févr. 2017<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class WebSMSProviderTest extends OlatTestCase {
	
	@Autowired
	private WebSMSProvider webSmsProvider;
	
	@Test
	public void send() {
		//webSmsProvider.setCredentials("stephane.rosse@frentix.com", "secret");
		webSmsProvider.setTest(true);
		boolean ok = webSmsProvider.send(UUID.randomUUID().toString(), "Hello from jenkins", "41797346615");
		Assert.assertTrue(ok);
	}

}
