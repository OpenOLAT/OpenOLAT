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
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.UUID;

import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.UriBuilder;

import org.assertj.core.api.Assertions;
import org.junit.Assert;
import org.junit.Test;
import org.olat.core.commons.persistence.DB;
import org.olat.modules.bigbluebutton.BigBlueButtonMeetingTemplate;
import org.olat.modules.bigbluebutton.manager.BigBlueButtonMeetingTemplateDAO;
import org.olat.modules.bigbluebutton.restapi.BigBlueButtonMeetingTemplateVO;
import org.olat.modules.bigbluebutton.restapi.BigBlueButtonTemplatesStatisticsVO;
import org.olat.test.OlatRestTestCase;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 29 oct. 2020<br>
 * @author srosse, stephane.rosse@frentix.com, https://www.frentix.com
 *
 */
public class BigBlueButtonTemplatesWebServiceTest extends OlatRestTestCase {

	@Autowired
	private DB dbInstance;
	@Autowired
	private BigBlueButtonMeetingTemplateDAO bigBlueButtonMeetingTemplateDao;
	
	@Test
	public void getTemplates()
	throws IOException, URISyntaxException, InterruptedException  {
		String externalId = UUID.randomUUID().toString();
		BigBlueButtonMeetingTemplate template = bigBlueButtonMeetingTemplateDao.createTemplate("A new template to update", externalId, false);
		dbInstance.commit();
		Assert.assertNotNull(template);
		
		RestConnection conn = new RestConnection("administrator", "openolat");
		
		URI request = UriBuilder.fromUri(getContextURI()).path("bigbluebutton").path("templates").build();
		HttpRequest method = conn.createGet(request, MediaType.APPLICATION_JSON);
		HttpResponse<InputStream> response = conn.execute(method);
		Assert.assertEquals(200, response.statusCode());
		
		List<BigBlueButtonMeetingTemplateVO> templates = conn
				.parseList(response, BigBlueButtonMeetingTemplateVO.class);
		Assertions.assertThat(templates)
			.hasSizeGreaterThanOrEqualTo(1)
			.map(BigBlueButtonMeetingTemplateVO::getExternalId)
			.containsAnyOf(externalId);
		conn.close();
	}
	
	@Test
	public void getStatistics()
	throws IOException, URISyntaxException, InterruptedException  {
		
		RestConnection conn = new RestConnection("administrator", "openolat");			
		
		URI request = UriBuilder.fromUri(getContextURI()).path("bigbluebutton").path("templates").path("statistics").build();
		HttpRequest method = conn.createGet(request, MediaType.APPLICATION_JSON);
		HttpResponse<InputStream> response = conn.execute(method);
		Assert.assertEquals(200, response.statusCode());
		
		BigBlueButtonTemplatesStatisticsVO statistics = conn
				.parse(response, BigBlueButtonTemplatesStatisticsVO.class);
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
		conn.close();
	}
}
