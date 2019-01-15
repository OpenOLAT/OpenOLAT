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
package org.olat.modules.gotomeeting.manager;

import java.util.Date;

import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Test;
import org.olat.modules.gotomeeting.model.GoToOrganizerG2T;


/**
 * 
 * This mostly test that the date returned by the GoTo server are parseable.
 * 
 * Initial date: 23.03.2016<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class GoToJsonUtilTest {
	
	@Test
	public void createTraining() throws JSONException {
		JSONObject trainingJson = GoToJsonUtil.training(3457498l, "New training", "Very important", "Europ/Amsterdam", new Date(), new Date());
		String content = trainingJson.toString(2);
		System.out.println(content);
	}
	
	@Test
	public void formatDateTime() throws JSONException {
		Date now = new Date();
		String formattedDate = GoToJsonUtil.formatDateTimeForPost(now);
		System.out.println(formattedDate);
		Assert.assertNotNull(formattedDate);
	}
	
	@Test
	public void parseDateTime() throws JSONException {
		Date formattedDate = GoToJsonUtil.parseDateTime("2016-04-07T10:00:00Z");
		System.out.println(formattedDate);
		Assert.assertNotNull(formattedDate);
	}
	
	@Test
	public void parseRecordingDateTime() throws JSONException {
		Date formattedDate = GoToJsonUtil.parseRecordingDateTime("2016-03-31T17:07:49+02:00");
		System.out.println(formattedDate);
		Assert.assertNotNull(formattedDate);
	}
	
	@Test
	public void parseToken() throws JSONException {
		String rawResponse = "{\"access_token\":\"VwrR3YJ5KP3k7sHjMPGMlf7FS22G\",\"token_type\":\"Bearer\",\"refresh_token\":\"tn4E4Dnd9B2UCIi6ZNhxZLY9KrrfyXlN\",\"expires_in\":3600,\"account_key\":\"2627181176136903948\",\"account_type\":\"\",\"email\":\"sross@frentix.ch\",\"firstName\":\"Stefen\",\"lastName\":\"Ross\",\"organizer_key\":\"6511479779650939910\",\"version\":\"3\"}";
		GoToOrganizerG2T organizer = GoToJsonUtil.parseToken(rawResponse);
		Assert.assertNotNull(organizer);
		Assert.assertEquals("VwrR3YJ5KP3k7sHjMPGMlf7FS22G", organizer.getAccessToken());
		Assert.assertEquals("tn4E4Dnd9B2UCIi6ZNhxZLY9KrrfyXlN", organizer.getRefreshToken());
		Assert.assertEquals("2627181176136903948", organizer.getAccountKey());
		Assert.assertEquals("6511479779650939910", organizer.getOrganizerKey());
		Assert.assertEquals("sross@frentix.ch", organizer.getEmail());
		Assert.assertEquals("Stefen", organizer.getFirstName());
		Assert.assertEquals("Ross", organizer.getLastName());
	}
}
