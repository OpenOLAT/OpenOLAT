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

import org.jcodec.common.Assert;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Test;


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
}
