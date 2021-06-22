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
package de.bps.olat.user;

import java.util.Map;

import org.junit.Assert;
import org.junit.Test;


/**
 * 
 * Initial date: 22 juin 2021<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class ChangeEMailExecuteControllerTest {
	
	@Test
	public void readEmailsAdrress() {
		String emailAdressXml ="<map>\n"
				+ " <entry>\n"
				+ "  <string>changedEMail</string>\n"
				+ "  <string>m@frentix.com</string>\n"
				+ " </entry> \n"
				+ " <entry>\n"
				+ "  <string>currentEMail</string>\n"
				+ "  <string>m@openolat.org</string>\n"
				+ " </entry>\n"
				+ "</map> ";
		
		Map<String,String> mails = ChangeEMailExecuteController.getMails(emailAdressXml);
		Assert.assertEquals("m@frentix.com", mails.get("changedEMail"));
		Assert.assertEquals("m@openolat.org", mails.get("currentEMail"));
	}
}
