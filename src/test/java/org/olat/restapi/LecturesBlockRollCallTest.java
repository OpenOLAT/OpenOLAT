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
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.UriBuilder;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.junit.Assert;
import org.junit.Test;
import org.olat.core.commons.persistence.DB;
import org.olat.core.id.Identity;
import org.olat.modules.lecture.LectureBlock;
import org.olat.modules.lecture.LectureBlockRollCall;
import org.olat.modules.lecture.LectureBlockRollCallSearchParameters;
import org.olat.modules.lecture.LectureBlockStatus;
import org.olat.modules.lecture.LectureRollCallStatus;
import org.olat.modules.lecture.manager.LectureBlockDAO;
import org.olat.modules.lecture.manager.LectureBlockRollCallDAO;
import org.olat.modules.lecture.restapi.LectureBlockRollCallVO;
import org.olat.repository.RepositoryEntry;
import org.olat.test.JunitTestHelper;
import org.olat.test.OlatRestTestCase;
import org.springframework.beans.factory.annotation.Autowired;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * 
 * Initial date: 28 août 2017<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class LecturesBlockRollCallTest extends OlatRestTestCase {
	
	@Autowired
	private DB dbInstance;
	@Autowired
	private LectureBlockDAO lectureBlockDao;
	@Autowired
	private LectureBlockRollCallDAO lectureBlockRollCallDao;
	
	@Test
	public void getRollCalls_searchParams_True()
	throws IOException, URISyntaxException {
		// an open lecture block
		LectureBlock openLectureBlock = createMinimalLectureBlock(3);
		Identity id1 = JunitTestHelper.createAndPersistIdentityAsRndUser("lecturer-1");
		Identity id2 = JunitTestHelper.createAndPersistIdentityAsRndUser("lecturer-2");
		dbInstance.commitAndCloseSession();

		// a closed lecture block
		LectureBlock closedLectureBlock = createMinimalLectureBlock(3);
		dbInstance.commitAndCloseSession();
		closedLectureBlock.setStatus(LectureBlockStatus.done);
		closedLectureBlock.setRollCallStatus(LectureRollCallStatus.closed);
		lectureBlockDao.update(closedLectureBlock);

		List<Integer> absences = Arrays.asList(1, 2);
		LectureBlockRollCall rollCall1 = lectureBlockRollCallDao.createAndPersistRollCall(closedLectureBlock, id1,
				null, null, null, null, null, Collections.emptyList());
		LectureBlockRollCall rollCall2 = lectureBlockRollCallDao.createAndPersistRollCall(closedLectureBlock, id2,
				null, null, null, null, null, absences);
		LectureBlockRollCall rollCall3 = lectureBlockRollCallDao.createAndPersistRollCall(openLectureBlock, id1,
				null, null, null, null, null, absences);
		LectureBlockRollCall rollCall4 = lectureBlockRollCallDao.createAndPersistRollCall(openLectureBlock, id2,
				null, null, null, null, null, Collections.emptyList());
		dbInstance.commit();
		
		rollCall2.setAbsenceSupervisorNotificationDate(new Date());
		rollCall2 = lectureBlockRollCallDao.update(rollCall2);
		dbInstance.commitAndCloseSession();
		
		// REST call
		RestConnection conn = new RestConnection();
		Assert.assertTrue(conn.login("administrator", "openolat"));

		URI uri = UriBuilder.fromUri(getContextURI()).path("repo").path("lectures").path("rollcalls")
				.queryParam("hasAbsences", "true")
				.queryParam("closed", "true")
				.queryParam("hasSupervisorNotificationDate", "true").build();
		HttpGet method = conn.createGet(uri, MediaType.APPLICATION_JSON, true);
		HttpResponse response = conn.execute(method);
		
		Assert.assertEquals(200, response.getStatusLine().getStatusCode());
		List<LectureBlockRollCallVO> voList = parseLectureBlockRollCallArray(response.getEntity().getContent());
		Assert.assertNotNull(voList);
		
		// check the return values
		List<Long> rollCallVoKeys = voList.stream().map(vo -> vo.getKey()).collect(Collectors.toList());
		Assert.assertFalse(rollCallVoKeys.contains(rollCall1.getKey()));
		Assert.assertTrue(rollCallVoKeys.contains(rollCall2.getKey()));
		Assert.assertFalse(rollCallVoKeys.contains(rollCall3.getKey()));
		Assert.assertFalse(rollCallVoKeys.contains(rollCall4.getKey()));
		
		
		//check that roll call 2 has a date
		boolean found = false;
		for(LectureBlockRollCallVO vo:voList) {
			if(vo.getKey().equals(rollCall2.getKey()) && vo.getAbsenceSupervisorNotificationDate() != null) {
				found = true;
			}	
		}
		Assert.assertTrue(found);
	}
	
	@Test
	public void getRollCallByKey()
	throws IOException, URISyntaxException {
		// a closed lecture block
		LectureBlock lectureBlock = createMinimalLectureBlock(3);
		Identity id = JunitTestHelper.createAndPersistIdentityAsRndUser("lecturer-1");
		dbInstance.commit();

		List<Integer> absences = Arrays.asList(1, 2);
		LectureBlockRollCall rollCall = lectureBlockRollCallDao.createAndPersistRollCall(lectureBlock, id,
				null, null, null, null, null, absences);
		dbInstance.commitAndCloseSession();

		// GET REST call
		RestConnection conn = new RestConnection();
		Assert.assertTrue(conn.login("administrator", "openolat"));

		URI uri = UriBuilder.fromUri(getContextURI()).path("repo").path("lectures").path("rollcalls")
				.path(rollCall.getKey().toString()).build();
		HttpGet method = conn.createGet(uri, MediaType.APPLICATION_JSON, true);
		HttpResponse response = conn.execute(method);

		Assert.assertEquals(200, response.getStatusLine().getStatusCode());
		LectureBlockRollCallVO rollCallVo = conn.parse(response, LectureBlockRollCallVO.class);
		Assert.assertNotNull(rollCallVo);
		Assert.assertEquals(rollCall.getKey(), rollCallVo.getKey());
	}
	
	@Test
	public void getAndUpdateSupervisorDate()
	throws IOException, URISyntaxException {
		// a closed lecture block
		LectureBlock lectureBlock = createMinimalLectureBlock(3);
		Identity id = JunitTestHelper.createAndPersistIdentityAsRndUser("lecturer-1");
		dbInstance.commit();

		List<Integer> absences = Arrays.asList(1, 2);
		LectureBlockRollCall rollCall = lectureBlockRollCallDao.createAndPersistRollCall(lectureBlock, id,
				null, null, null, null, null, absences);
		dbInstance.commitAndCloseSession();

		// POST REST call
		LectureBlockRollCallVO rollCallVo = new LectureBlockRollCallVO();
		rollCallVo.setKey(rollCall.getKey());
		rollCallVo.setLecturesAbsentNumber(rollCall.getLecturesAbsentNumber());
		rollCallVo.setLecturesAttendedNumber(rollCall.getLecturesAttendedNumber());
		
		rollCallVo.setComment(rollCall.getComment());
		rollCallVo.setAbsenceReason(rollCall.getAbsenceReason());
		rollCallVo.setAbsenceAuthorized(rollCall.getAbsenceAuthorized());
		rollCallVo.setAbsenceSupervisorNotificationDate(new Date());

		rollCallVo.setIdentityKey(id.getKey());
		rollCallVo.setLectureBlockKey(lectureBlock.getKey());

		RestConnection conn = new RestConnection();
		Assert.assertTrue(conn.login("administrator", "openolat"));

		URI uri = UriBuilder.fromUri(getContextURI()).path("repo").path("lectures").path("rollcalls").build();
		HttpPost postMethod = conn.createPost(uri, MediaType.APPLICATION_JSON);
		conn.addJsonEntity(postMethod, rollCallVo);
		
		HttpResponse response = conn.execute(postMethod);

		// check the response
		Assert.assertEquals(200, response.getStatusLine().getStatusCode());
		LectureBlockRollCallVO updatedRollCallVo = conn.parse(response, LectureBlockRollCallVO.class);
		Assert.assertNotNull(updatedRollCallVo);
		Assert.assertEquals(rollCall.getKey(), updatedRollCallVo.getKey());
		Assert.assertNotNull(updatedRollCallVo.getAbsenceSupervisorNotificationDate());
		
		
		// reload the roll call from the database
		LectureBlockRollCallSearchParameters searchParams = new LectureBlockRollCallSearchParameters();
		searchParams.setRollCallKey(rollCall.getKey());
		List<LectureBlockRollCall> rollCalls = lectureBlockRollCallDao.getRollCalls(searchParams);
		Assert.assertNotNull(rollCalls);
		Assert.assertEquals(1, rollCalls.size());
		LectureBlockRollCall updatedRollCall = rollCalls.get(0);
		Assert.assertEquals(rollCall, updatedRollCall);
		Assert.assertNotNull(updatedRollCall.getAbsenceSupervisorNotificationDate());
	}
	
	private LectureBlock createMinimalLectureBlock(int numOfLectures) {
		RepositoryEntry entry = JunitTestHelper.createAndPersistRepositoryEntry();
		LectureBlock lectureBlock = lectureBlockDao.createLectureBlock(entry);
		lectureBlock.setStartDate(new Date());
		lectureBlock.setEndDate(new Date());
		lectureBlock.setTitle("Hello REST lecturers");
		lectureBlock.setPlannedLecturesNumber(numOfLectures);
		lectureBlock.setEffectiveLecturesNumber(numOfLectures);
		return lectureBlockDao.update(lectureBlock);
	}
	
	protected List<LectureBlockRollCallVO> parseLectureBlockRollCallArray(InputStream body) {
		try {
			ObjectMapper mapper = new ObjectMapper(jsonFactory); 
			return mapper.readValue(body, new TypeReference<List<LectureBlockRollCallVO>>(){/* */});
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
}
