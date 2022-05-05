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

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriBuilder;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.logging.log4j.Logger;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.olat.core.commons.persistence.DB;
import org.olat.core.id.Identity;
import org.olat.core.logging.Tracing;
import org.olat.core.util.DateUtils;
import org.olat.course.assessment.AssessmentMode;
import org.olat.course.assessment.AssessmentMode.Target;
import org.olat.course.assessment.AssessmentModeManager;
import org.olat.course.assessment.restapi.AssessmentModeVO;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryEntryStatusEnum;
import org.olat.test.JunitTestHelper;
import org.olat.test.OlatRestTestCase;
import org.springframework.beans.factory.annotation.Autowired;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * 
 * Initial date: 4 mai 2022<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class CourseAssessmentModeWebServiceTest extends OlatRestTestCase {
	
	private static final Logger log = Tracing.createLoggerFor(AssessmentModeWebServiceTest.class);

	private static RepositoryEntry courseEntry;
	
	@Autowired
	private DB dbInstance;
	@Autowired
	private AssessmentModeManager assessmentModeMgr;
	
	
	@Before
	public void setUp() throws Exception {
		try {
			if(courseEntry == null) {
				Identity admin = JunitTestHelper.findIdentityByLogin("administrator");
				courseEntry = JunitTestHelper.deployBasicCourse(admin, RepositoryEntryStatusEnum.preparation, false, false);
			}
			dbInstance.closeSession();
		} catch (Exception e) {
			log.error("Exception in setUp(): " + e);
		}
	}
	
	@Test
	public void getAssessmentModes()
	throws IOException, URISyntaxException {
		
		AssessmentMode mode = assessmentModeMgr.createAssessmentMode(courseEntry);
		mode.setName("Assessment to load");
		mode.setBegin(new Date());
		mode.setEnd(new Date());
		mode.setTargetAudience(Target.course);
		AssessmentMode savedMode = assessmentModeMgr.persist(mode);
		dbInstance.commitAndCloseSession();
		Assert.assertNotNull(savedMode);
		
		RestConnection conn = new RestConnection();
		Assert.assertTrue(conn.login("administrator", "openolat"));			
		
		URI request = UriBuilder.fromUri(getContextURI()).path("repo").path("courses").path(courseEntry.getOlatResource().getResourceableId().toString())
				.path("assessmentmodes").build();
		HttpGet method = conn.createGet(request, MediaType.APPLICATION_JSON, true);
		HttpResponse response = conn.execute(method);
		Assert.assertEquals(200, response.getStatusLine().getStatusCode());
		
		List<AssessmentModeVO> modeVoes = parseAssessmentModeArray(response.getEntity());
		assertThat(modeVoes)
			.isNotNull()
			.isNotEmpty()
			.extracting(vo -> vo.getKey())
			.containsAnyOf(savedMode.getKey());	
		
		for(AssessmentModeVO modeVo:modeVoes) {
			Assert.assertEquals(courseEntry.getKey(), modeVo.getRepositoryEntryKey());
		}
	}
	
	@Test
	public void createAssessmentMode()
	throws IOException, URISyntaxException {

		AssessmentModeVO assessmentModeVo = new AssessmentModeVO();
		assessmentModeVo.setRepositoryEntryKey(courseEntry.getKey());
		assessmentModeVo.setName("My course mode");
		String externalId = UUID.randomUUID().toString();
		assessmentModeVo.setExternalId(externalId);
		assessmentModeVo.setManagedFlagsString("all");
		assessmentModeVo.setTargetAudience(Target.course.name());
		
		Date begin = DateUtils.addDays(new Date(), 3);
		assessmentModeVo.setBegin(begin);
		assessmentModeVo.setLeadTime(7);
		assessmentModeVo.setEnd(DateUtils.addHours(begin, 2));
		assessmentModeVo.setFollowupTime(14);
		
		assessmentModeVo.setIpList("192.168.1.1");
		assessmentModeVo.setRestrictAccessIps(Boolean.TRUE);
		
		RestConnection conn = new RestConnection();
		Assert.assertTrue(conn.login("administrator", "openolat"));	
		
		URI request = UriBuilder.fromUri(getContextURI()).path("repo").path("courses")
				.path(courseEntry.getOlatResource().getResourceableId().toString())
				.path("assessmentmodes").build();
		HttpPut method = conn.createPut(request, MediaType.APPLICATION_JSON, true);
		conn.addJsonEntity(method, assessmentModeVo);
		HttpResponse response = conn.execute(method);
		Assert.assertEquals(200, response.getStatusLine().getStatusCode());
		
		AssessmentModeVO savedAssessmentModeVo = conn.parse(response, AssessmentModeVO.class);
		Assert.assertNotNull(savedAssessmentModeVo);
		Assert.assertNotNull(savedAssessmentModeVo.getCreationDate());
		Assert.assertNotNull(savedAssessmentModeVo.getBegin());
		Assert.assertNotNull(savedAssessmentModeVo.getEnd());
		Assert.assertEquals("My course mode", savedAssessmentModeVo.getName());
		
		AssessmentMode savedAssessmentMode = assessmentModeMgr.getAssessmentModeById(savedAssessmentModeVo.getKey());
		Assert.assertNotNull(savedAssessmentMode);
		Assert.assertNotNull(savedAssessmentMode.getCreationDate());
		Assert.assertEquals("My course mode", savedAssessmentMode.getName());
		Assert.assertNotNull(savedAssessmentMode.getBegin());
		Assert.assertEquals(7, savedAssessmentMode.getLeadTime());
		Assert.assertNotNull(savedAssessmentMode.getEnd());
		Assert.assertEquals(14, savedAssessmentMode.getFollowupTime());
		
		Assert.assertEquals(externalId, savedAssessmentMode.getExternalId());
		Assert.assertEquals("all", savedAssessmentMode.getManagedFlagsString());
		
		Assert.assertTrue(savedAssessmentMode.isRestrictAccessIps());
		Assert.assertEquals("192.168.1.1", savedAssessmentMode.getIpList());
		
		conn.shutdown();
	}
	
	@Test
	public void updateAssessmentMode()
	throws IOException, URISyntaxException {
		
		AssessmentMode mode = assessmentModeMgr.createAssessmentMode(courseEntry);
		mode.setName("Course assessment to update");
		mode.setBegin(new Date());
		mode.setEnd(new Date());
		mode.setTargetAudience(Target.course);
		AssessmentMode savedMode = assessmentModeMgr.persist(mode);
		dbInstance.commitAndCloseSession();
		Assert.assertNotNull(savedMode);
		
		// Update per REST
		AssessmentModeVO assessmentModeVo = new AssessmentModeVO();
		assessmentModeVo.setKey(savedMode.getKey());
		assessmentModeVo.setRepositoryEntryKey(courseEntry.getKey());
		assessmentModeVo.setName("Updated course assessment");
		String externalId = UUID.randomUUID().toString();
		assessmentModeVo.setExternalId(externalId);
		assessmentModeVo.setManagedFlagsString("all");
		
		Date begin = DateUtils.addDays(new Date(), 3);
		assessmentModeVo.setBegin(begin);
		assessmentModeVo.setLeadTime(5);
		assessmentModeVo.setEnd(DateUtils.addHours(begin, 2));
		assessmentModeVo.setFollowupTime(15);
		
		assessmentModeVo.setRestrictAccessElements(Boolean.TRUE);
		assessmentModeVo.setElementList("90604173081887,90604173081892");
		assessmentModeVo.setStartElement("90604173081892");
		
		RestConnection conn = new RestConnection();
		Assert.assertTrue(conn.login("administrator", "openolat"));	
		
		URI request = UriBuilder.fromUri(getContextURI()).path("repo").path("assessmentmodes").build();
		HttpPost method = conn.createPost(request, MediaType.APPLICATION_JSON);
		conn.addJsonEntity(method, assessmentModeVo);
		HttpResponse response = conn.execute(method);
		Assert.assertEquals(200, response.getStatusLine().getStatusCode());
		
		AssessmentModeVO updatedAssessmentModeVo = conn.parse(response, AssessmentModeVO.class);
		Assert.assertNotNull(updatedAssessmentModeVo);
		Assert.assertNotNull(updatedAssessmentModeVo.getCreationDate());
		Assert.assertNotNull(updatedAssessmentModeVo.getBegin());
		Assert.assertNotNull(updatedAssessmentModeVo.getEnd());
		Assert.assertEquals("Updated course assessment", updatedAssessmentModeVo.getName());
		
		AssessmentMode updatedAssessmentMode = assessmentModeMgr.getAssessmentModeById(updatedAssessmentModeVo.getKey());
		Assert.assertNotNull(updatedAssessmentMode);
		Assert.assertNotNull(updatedAssessmentMode.getCreationDate());
		Assert.assertEquals("Updated course assessment", updatedAssessmentMode.getName());
		Assert.assertNotNull(updatedAssessmentMode.getBegin());
		Assert.assertEquals(5, updatedAssessmentMode.getLeadTime());
		Assert.assertNotNull(updatedAssessmentMode.getEnd());
		Assert.assertEquals(15, updatedAssessmentMode.getFollowupTime());
		
		Assert.assertEquals(externalId, updatedAssessmentMode.getExternalId());
		Assert.assertEquals("all", updatedAssessmentMode.getManagedFlagsString());
		
		Assert.assertTrue(updatedAssessmentMode.isRestrictAccessElements());
		Assert.assertEquals("90604173081887,90604173081892", updatedAssessmentMode.getElementList());
		Assert.assertEquals("90604173081892", updatedAssessmentMode.getStartElement());
		
		conn.shutdown();
	}

	
	protected List<AssessmentModeVO> parseAssessmentModeArray(HttpEntity entity) {
		try(InputStream in=entity.getContent()) {
			ObjectMapper mapper = new ObjectMapper(jsonFactory);
			return mapper.readValue(in, new TypeReference<List<AssessmentModeVO>>(){/* */});
		} catch (Exception e) {
			log.error("", e);
			return null;
		}
	}

}
