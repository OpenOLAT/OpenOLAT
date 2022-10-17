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
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Date;
import java.util.List;

import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.UriBuilder;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.util.EntityUtils;
import org.junit.Assert;
import org.junit.Test;
import org.olat.core.commons.persistence.DB;
import org.olat.core.id.Identity;
import org.olat.course.CourseFactory;
import org.olat.course.ICourse;
import org.olat.course.assessment.UserEfficiencyStatement;
import org.olat.course.assessment.manager.EfficiencyStatementManager;
import org.olat.course.assessment.model.UserEfficiencyStatementImpl;
import org.olat.course.assessment.restapi.UserEfficiencyStatementVO;
import org.olat.course.assessment.restapi.UserEfficiencyStatementVOes;
import org.olat.repository.RepositoryEntry;
import org.olat.resource.OLATResource;
import org.olat.test.JunitTestHelper;
import org.olat.test.OlatRestTestCase;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 13 août 2021<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class UserEfficiencyStatementWebServiceTest extends OlatRestTestCase {
	
	@Autowired
	private DB dbInstance;
	@Autowired
	private EfficiencyStatementManager efficiencyStatementManager;
	
	@Test
	public void getUserEfficiencyStatements() throws IOException, URISyntaxException {
		// create a standalone efficiency statement
		Identity admin = JunitTestHelper.findIdentityByLogin("administrator");
		Identity assessedIdentity = JunitTestHelper.createAndPersistIdentityAsRndUser("user-eff-1");
		RepositoryEntry courseEntry = JunitTestHelper.deployBasicCourse(admin);
		ICourse course = CourseFactory.loadCourse(courseEntry);
		dbInstance.commitAndCloseSession();
		
		OLATResource resource = course.getCourseEnvironment().getCourseGroupManager().getCourseResource();
		UserEfficiencyStatement statement = efficiencyStatementManager
				.createUserEfficiencyStatement(new Date(), 5.0f, "g1", "gs1", "pc1", true, assessedIdentity, resource);
		dbInstance.commitAndCloseSession();
		Assert.assertNotNull(statement);
		
		
		// get the statement
		RestConnection conn = new RestConnection();
		Assert.assertTrue(conn.login("administrator", "openolat"));
		
		URI uri = UriBuilder.fromUri(getContextURI()).path("users")
				.path(assessedIdentity.getKey().toString())
				.path("statements").build();
		HttpGet getStatement = conn.createGet(uri, MediaType.APPLICATION_JSON, true);
		HttpResponse response = conn.execute(getStatement);
		assertEquals(200, response.getStatusLine().getStatusCode());
		UserEfficiencyStatementVOes statementVOes = conn.parse(response, UserEfficiencyStatementVOes.class);
		Assert.assertNotNull(statementVOes);
		Assert.assertNotNull(statementVOes.getStatements());
		Assert.assertEquals(1, statementVOes.getStatements().size());
		UserEfficiencyStatementVO statementVO = statementVOes.getStatements().get(0);
		Assert.assertEquals(5.0f, statementVO.getScore(), 0.0001);
		Assert.assertEquals("g1", statementVO.getGrade());
		Assert.assertEquals("gs1", statementVO.getGradeSystemIdent());
		Assert.assertEquals("pc1", statementVO.getPerfromanceClassIdent());
		Assert.assertEquals(Boolean.TRUE, statementVO.getPassed());
		Assert.assertEquals(assessedIdentity.getKey(), statementVO.getIdentityKey());
	}
	
	@Test
	public void putUserEfficiencyStatements() throws IOException, URISyntaxException {
		Identity assessedIdentity = JunitTestHelper.createAndPersistIdentityAsRndUser("user-eff-2");
		
		UserEfficiencyStatementVO statementVO = new UserEfficiencyStatementVO();
		statementVO.setAttemptedNodes(2);
		statementVO.setPassedNodes(1);
		statementVO.setTotalNodes(3);
		statementVO.setScore(8.0f);
		statementVO.setGrade("g1");
		statementVO.setPerfromanceClassIdent("pc1");
		statementVO.setPassed(Boolean.TRUE);
		statementVO.setIdentityKey(assessedIdentity.getKey());
		statementVO.setStatementXml("<org.olat.course.assessment.EfficiencyStatement></org.olat.course.assessment.EfficiencyStatement>");
		
		// create the statement
		RestConnection conn = new RestConnection();
		Assert.assertTrue(conn.login("administrator", "openolat"));
		
		URI uri = UriBuilder.fromUri(getContextURI()).path("users")
				.path(assessedIdentity.getKey().toString())
				.path("statements").build();

		HttpPost method = conn.createPost(uri, MediaType.APPLICATION_JSON);
		conn.addJsonEntity(method, statementVO);
		HttpResponse response = conn.execute(method);
		Assert.assertEquals(200, response.getStatusLine().getStatusCode());
		EntityUtils.consume(response.getEntity());
		
		// get the statement
		List<UserEfficiencyStatementImpl> statements = efficiencyStatementManager.getUserEfficiencyStatementFull(assessedIdentity);
		Assert.assertNotNull(statements);
		Assert.assertEquals(1, statements.size());
		
		UserEfficiencyStatementImpl statement = statements.get(0);
		Assert.assertEquals(Integer.valueOf(2), statement.getAttemptedNodes());
		Assert.assertEquals(Integer.valueOf(1), statement.getPassedNodes());
		Assert.assertEquals(Integer.valueOf(3), statement.getTotalNodes());
		Assert.assertEquals(8.0f, statement.getScore(), 0.0001);
		Assert.assertEquals("g1", statementVO.getGrade());
		Assert.assertEquals("pc1", statementVO.getPerfromanceClassIdent());
		Assert.assertEquals(Boolean.TRUE, statement.getPassed());
		Assert.assertNotNull(statement.getStatementXml());
		Assert.assertEquals(assessedIdentity, statement.getIdentity());
	}
	
	/**
	 * The REST API cannot replace an existing efficiency statement with a valid course.
	 * 
	 * @throws IOException
	 * @throws URISyntaxException
	 */
	@Test
	public void putUserEfficiencyStatementExistingStatement() throws IOException, URISyntaxException {
		Identity admin = JunitTestHelper.findIdentityByLogin("administrator");
		Identity assessedIdentity = JunitTestHelper.createAndPersistIdentityAsRndUser("user-eff-1");
		RepositoryEntry courseEntry = JunitTestHelper.deployBasicCourse(admin);
		ICourse course = CourseFactory.loadCourse(courseEntry);
		dbInstance.commitAndCloseSession();
		
		OLATResource resource = course.getCourseEnvironment().getCourseGroupManager().getCourseResource();
		UserEfficiencyStatement statement = efficiencyStatementManager
				.createUserEfficiencyStatement(new Date(), 5.0f, "g1", "gs1", "pc1", true, assessedIdentity, resource);
		dbInstance.commitAndCloseSession();
		Assert.assertNotNull(statement);
		
		UserEfficiencyStatementVO statementVO = new UserEfficiencyStatementVO();
		statementVO.setScore(8.0f);
		statementVO.setPassed(Boolean.TRUE);
		statementVO.setCourseRepoKey(courseEntry.getKey());
		statementVO.setIdentityKey(assessedIdentity.getKey());
		
		// create the statement
		RestConnection conn = new RestConnection();
		Assert.assertTrue(conn.login("administrator", "openolat"));
		
		URI uri = UriBuilder.fromUri(getContextURI()).path("users")
				.path(assessedIdentity.getKey().toString())
				.path("statements").build();

		HttpPost method = conn.createPost(uri, MediaType.APPLICATION_JSON);
		conn.addJsonEntity(method, statementVO);
		HttpResponse response = conn.execute(method);
		Assert.assertEquals(409, response.getStatusLine().getStatusCode());
		EntityUtils.consume(response.getEntity());
	}
	
	/**
	 * The REST API can append an efficiency statement to an existing course.
	 * 
	 * @throws IOException
	 * @throws URISyntaxException
	 */
	@Test
	public void putUserEfficiencyStatementExistingCourse() throws IOException, URISyntaxException {
		Identity admin = JunitTestHelper.findIdentityByLogin("administrator");
		Identity assessedIdentity = JunitTestHelper.createAndPersistIdentityAsRndUser("user-eff-1");
		RepositoryEntry courseEntry = JunitTestHelper.deployBasicCourse(admin);
		dbInstance.commitAndCloseSession();

		UserEfficiencyStatementVO statementVO = new UserEfficiencyStatementVO();
		statementVO.setScore(8.0f);
		statementVO.setGrade("g1");
		statementVO.setPerfromanceClassIdent("pc1");
		statementVO.setPassed(Boolean.TRUE);
		statementVO.setCourseRepoKey(courseEntry.getKey());
		statementVO.setIdentityKey(assessedIdentity.getKey());
		
		// create the statement
		RestConnection conn = new RestConnection();
		Assert.assertTrue(conn.login("administrator", "openolat"));
		
		URI uri = UriBuilder.fromUri(getContextURI()).path("users")
				.path(assessedIdentity.getKey().toString())
				.path("statements").build();

		HttpPost method = conn.createPost(uri, MediaType.APPLICATION_JSON);
		conn.addJsonEntity(method, statementVO);
		HttpResponse response = conn.execute(method);
		Assert.assertEquals(200, response.getStatusLine().getStatusCode());
		EntityUtils.consume(response.getEntity());
		
		// get the statement
		UserEfficiencyStatementImpl statement = efficiencyStatementManager.getUserEfficiencyStatementFull(courseEntry, assessedIdentity);
		Assert.assertNotNull(statement);
		Assert.assertEquals(8.0f, statement.getScore(), 0.0001);
		Assert.assertEquals("g1", statementVO.getGrade());
		Assert.assertEquals("pc1", statementVO.getPerfromanceClassIdent());
		Assert.assertEquals(Boolean.TRUE, statement.getPassed());
		Assert.assertEquals(courseEntry.getOlatResource(), statement.getResource());
		Assert.assertEquals(assessedIdentity, statement.getIdentity());
	}

}
