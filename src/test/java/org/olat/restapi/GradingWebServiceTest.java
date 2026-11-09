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

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Date;
import java.util.List;

import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.UriBuilder;

import org.junit.Assert;
import org.junit.Test;
import org.olat.core.commons.persistence.DB;
import org.olat.core.id.Identity;
import org.olat.modules.assessment.AssessmentEntry;
import org.olat.modules.assessment.manager.AssessmentEntryDAO;
import org.olat.modules.assessment.restapi.AssessmentEntryVO;
import org.olat.modules.grading.GraderToIdentity;
import org.olat.modules.grading.GradingAssignment;
import org.olat.modules.grading.manager.GraderToIdentityDAO;
import org.olat.modules.grading.manager.GradingAssignmentDAO;
import org.olat.modules.grading.restapi.GradingAssignmentUserVisibilityVO;
import org.olat.modules.grading.restapi.GradingAssignmentWithInfosVO;
import org.olat.repository.RepositoryEntry;
import org.olat.restapi.support.vo.RepositoryEntryVO;
import org.olat.test.JunitTestHelper;
import org.olat.test.OlatRestTestCase;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 8 juil. 2020<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class GradingWebServiceTest extends OlatRestTestCase {
	
	@Autowired
	private DB dbInstance;
	@Autowired
	private AssessmentEntryDAO assessmentEntryDao;
	@Autowired
	private GraderToIdentityDAO gradedToIdentityDao;
	@Autowired
	private GradingAssignmentDAO gradingAssignmentDao;
	
	@Test
	public void testWithGrading() throws IOException, URISyntaxException, InterruptedException {
		RestConnection conn = new RestConnection("administrator", "openolat");
		
		URI uri = getGradingUriBuilder().path("assignments").path("tests").build();
		HttpRequest method = conn.createGet(uri, MediaType.APPLICATION_JSON);
		HttpResponse<InputStream> response = conn.execute(method);
		assertEquals(200, response.statusCode());
		List<RepositoryEntryVO> entriesVo = conn.parseList(response, RepositoryEntryVO.class);
		Assert.assertNotNull(entriesVo);
		conn.close();
	}
	
	@Test
	public void testAssignmentInfos() throws IOException, URISyntaxException, InterruptedException {
		Identity author = JunitTestHelper.createAndPersistIdentityAsRndUser("assignment-author1");
		Identity grader = JunitTestHelper.createAndPersistIdentityAsRndUser("assignment-grader-2");
		Identity student = JunitTestHelper.createAndPersistIdentityAsRndUser("assignment-student-3");
		RepositoryEntry entry = JunitTestHelper.createRandomRepositoryEntry(author);
		GraderToIdentity relation = gradedToIdentityDao.createRelation(entry, grader);	
		AssessmentEntry assessment = assessmentEntryDao.createAssessmentEntry(student, null, entry, null, false, entry);
		GradingAssignment assignment = gradingAssignmentDao.createGradingAssignment(relation, entry, assessment, new Date(), new Date());
		dbInstance.commit();

		RestConnection conn = new RestConnection("administrator", "openolat");
		
		URI uri = getGradingUriBuilder().path("test").path(entry.getKey().toString()).path("assignments").build();
		HttpRequest method = conn.createGet(uri, MediaType.APPLICATION_JSON);
		HttpResponse<InputStream> response = conn.execute(method);
		assertEquals(200, response.statusCode());
		List<GradingAssignmentWithInfosVO> infosVoes = conn.parseList(response, GradingAssignmentWithInfosVO.class);
		Assert.assertNotNull(infosVoes);
		Assert.assertEquals(1, infosVoes.size());
		
		GradingAssignmentWithInfosVO infoVo = infosVoes.get(0);
		Assert.assertEquals(student.getKey(), infoVo.getAssessedIdentityKey());
		Assert.assertEquals(assignment.getAssignmentStatus().name(), infoVo.getAssignmentStatus());
		Assert.assertNotNull(infoVo.getAssessmentEntry());
		
		AssessmentEntryVO assessmentEntryVo = infoVo.getAssessmentEntry();
		
		// still not set
		Assert.assertNull(assessmentEntryVo.getAssessmentStatus());
		Assert.assertEquals(assessment.getKey(), assessmentEntryVo.getKey());
		Assert.assertEquals(entry.getKey(), assessmentEntryVo.getReferenceEntryKey());
		Assert.assertEquals(entry.getKey(), assessmentEntryVo.getRepositoryEntryKey());
		Assert.assertNotNull(infoVo.getAssessmentEntry());
		conn.close();
	}
	
	@Test
	public void getTestUserVisibility() throws IOException, URISyntaxException, InterruptedException {
		Identity author = JunitTestHelper.createAndPersistIdentityAsRndUser("assignment-author1");
		Identity grader = JunitTestHelper.createAndPersistIdentityAsRndUser("assignment-grader-2");
		Identity student = JunitTestHelper.createAndPersistIdentityAsRndUser("assignment-student-3");
		RepositoryEntry entry = JunitTestHelper.createRandomRepositoryEntry(author);
		GraderToIdentity relation = gradedToIdentityDao.createRelation(entry, grader);	
		AssessmentEntry assessment = assessmentEntryDao.createAssessmentEntry(student, null, entry, null, false, entry);
		GradingAssignment assignment = gradingAssignmentDao.createGradingAssignment(relation, entry, assessment, new Date(), new Date());
		dbInstance.commit();

		RestConnection conn = new RestConnection("administrator", "openolat");
		
		URI uri = getGradingUriBuilder().path("test").path(entry.getKey().toString()).path("assignments")
				.path(assignment.getKey().toString()).path("uservisibility").build();
		HttpRequest method = conn.createGet(uri, MediaType.APPLICATION_JSON);
		HttpResponse<InputStream> response = conn.execute(method);
		assertEquals(200, response.statusCode());
		GradingAssignmentUserVisibilityVO userVisibility = conn.parse(response, GradingAssignmentUserVisibilityVO.class);
		Assert.assertNotNull(userVisibility);
		Assert.assertEquals(assignment.getKey(), userVisibility.getAssignmentKey());
		conn.close();
	}
	
	private UriBuilder getGradingUriBuilder() {
		return UriBuilder.fromUri(getContextURI()).path("grading");
	}
}
