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
package org.olat.modules.bigbluebutton.manager;

import java.net.URI;

import org.junit.Assert;
import org.junit.Test;

/**
 * 
 * Initial date: 18 mars 2020<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class BigBlueButtonUriBuilderTest {
	
	@Test
	public void createCheckSummedUrl() {
		// createname=Test+Meeting&meetingID=abc123&attendeePW=111222&moderatorPW=333444
		String sharedSecret = "639259d4-9dd8-4b25-bf01-95f9567eaf4b";
		BigBlueButtonUriBuilder builder = BigBlueButtonUriBuilder
				.fromUri(URI.create("https://bbb.openolat.org/bigbluebutton/"), sharedSecret);
		
		builder.operation("create")
		       .parameter("name", "Test Meeting")
		       .parameter("meetingID", "abc123")
		       .parameter("attendeePW", "111222")
		       .parameter("moderatorPW", "333444");
		
		String url = builder.build().toString();
		Assert.assertEquals("https://bbb.openolat.org/bigbluebutton/api/create?name=Test+Meeting&meetingID=abc123&attendeePW=111222&moderatorPW=333444&checksum=da9185f7f333cfdfcd6eeac32dca3777510c4c436020d8b887ba5515bd1d189e" , url);
	}
	
	@Test
	public void createCheckSummedSpecialCharactersUrl() {
		String sharedSecret = "639259d4-9dd8-4b25-bf01-95f9567eaf4b";
		BigBlueButtonUriBuilder builder = BigBlueButtonUriBuilder
				.fromUri(URI.create("https://bbb.openolat.org/bigbluebutton/"), sharedSecret);
		
		builder.operation("create")
		       .parameter("name", "Test \u00E9v\u00E9nement")
		       .parameter("meetingID", "abc123")
		       .parameter("attendeePW", "111222")
		       .parameter("moderatorPW", "333444");
		
		String url = builder.build().toString();
		Assert.assertEquals("https://bbb.openolat.org/bigbluebutton/api/create?name=Test+%C3%A9v%C3%A9nement&meetingID=abc123&attendeePW=111222&moderatorPW=333444&checksum=bedc6c6bf6edf42c5ac0f2f3c34cbc8283435029edfa590f43c670e18c4324f9", url);
	}

}
