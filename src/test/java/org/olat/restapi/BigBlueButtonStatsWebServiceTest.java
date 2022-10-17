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
import org.olat.restapi.system.vo.BigBlueButtonStatisticsVO;
import org.olat.test.OlatRestTestCase;

/**
 * 
 * Initial date: 13 jul. 2020<br>
 * @author mjenny, moritz.jenny@frentix.com, http://www.frentix.com
 *
 */

public class BigBlueButtonStatsWebServiceTest extends OlatRestTestCase {
	
	
	@Test
	public void bigbluebuttonStatistics()
	throws IOException, URISyntaxException {
		
		RestConnection conn = new RestConnection();
		Assert.assertTrue(conn.login("administrator", "openolat"));			
		
		URI request = UriBuilder.fromUri(getContextURI()).path("system").path("monitoring").path("bigbluebutton").build();
		HttpGet method = conn.createGet(request, MediaType.APPLICATION_JSON, true);
		HttpResponse response = conn.execute(method);
		Assert.assertEquals(200, response.getStatusLine().getStatusCode());
				
		BigBlueButtonStatisticsVO bigBlueButtonStatisticsVO = conn.parse(response, BigBlueButtonStatisticsVO.class);		
		
		Assert.assertEquals(0, bigBlueButtonStatisticsVO.getAttendeesCount());
		Assert.assertEquals(0, bigBlueButtonStatisticsVO.getCapacity());
		Assert.assertEquals(0, bigBlueButtonStatisticsVO.getMeetingsCount());
		Assert.assertEquals(0, bigBlueButtonStatisticsVO.getRecordingCount());
		Assert.assertEquals(0, bigBlueButtonStatisticsVO.getVideoCount());
	}	
}
