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
package org.olat.restapi;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.UriBuilder;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.junit.Assert;
import org.junit.Test;
import org.olat.modules.bigbluebutton.restapi.BigBlueButtonTemplatesStatisticsVO;
import org.olat.test.OlatRestTestCase;

/**
 * 
 * Initial date: 29 oct. 2020<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class BigBlueButtonTemplatesWebServiceTest extends OlatRestTestCase {
	
	@Test
	public void getStatistics()
	throws IOException, URISyntaxException  {
		
		RestConnection conn = new RestConnection("administrator", "openolat");			
		
		URI request = UriBuilder.fromUri(getContextURI()).path("bigbluebutton").path("templates").path("statistics").build();
		HttpGet method = conn.createGet(request, MediaType.APPLICATION_JSON, true);
		HttpResponse response = conn.execute(method);
		Assert.assertEquals(200, response.getStatusLine().getStatusCode());
		
		BigBlueButtonTemplatesStatisticsVO statistics = conn
				.parse(response.getEntity(), BigBlueButtonTemplatesStatisticsVO.class);
		Assert.assertNotNull(statistics);
		Assert.assertNotNull(statistics.getRooms());
		Assert.assertNotNull(statistics.getMaxParticipants());
		Assert.assertNotNull(statistics.getRoomsWithRecord());
		Assert.assertNotNull(statistics.getMaxParticipantsWithRecord());
		Assert.assertNotNull(statistics.getRoomsWithAutoStartRecording());
		Assert.assertNotNull(statistics.getMaxParticipantsWithAutoStartRecording());
		Assert.assertNotNull(statistics.getRoomsWithBreakout());
		Assert.assertNotNull(statistics.getMaxParticipantsWithBreakout());
		Assert.assertNotNull(statistics.getRoomsWithWebcamsOnlyForModerator());
		Assert.assertNotNull(statistics.getMaxParticipantsWithWebcamsOnlyForModerator());
	}
}
