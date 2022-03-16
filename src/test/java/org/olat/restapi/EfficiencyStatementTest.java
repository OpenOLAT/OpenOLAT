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

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriBuilder;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.util.EntityUtils;
import org.junit.Assert;
import org.junit.Test;
import org.olat.core.commons.persistence.DB;
import org.olat.core.id.Identity;
import org.olat.course.CourseFactory;
import org.olat.course.ICourse;
import org.olat.course.assessment.UserEfficiencyStatement;
import org.olat.course.assessment.manager.EfficiencyStatementManager;
import org.olat.course.assessment.model.EfficiencyStatementVO;
import org.olat.repository.RepositoryEntry;
import org.olat.resource.OLATResource;
import org.olat.test.JunitTestHelper;
import org.olat.test.OlatRestTestCase;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 17.11.2014<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class EfficiencyStatementTest extends OlatRestTestCase {

	@Autowired
	private DB dbInstance;
	@Autowired
	private EfficiencyStatementManager efficiencyStatementManager;
	
	@Test
	public void getEfficiencyStatement() throws IOException, URISyntaxException {
		// create a standalone efficiency statement
		Identity admin = JunitTestHelper.findIdentityByLogin("administrator");
		Identity assessedIdentity = JunitTestHelper.createAndPersistIdentityAsRndUser("eff-1");
		RepositoryEntry courseEntry = JunitTestHelper.deployBasicCourse(admin);
		ICourse course = CourseFactory.loadCourse(courseEntry);
		dbInstance.commitAndCloseSession();
		
		OLATResource resource = course.getCourseEnvironment().getCourseGroupManager().getCourseResource();
		UserEfficiencyStatement statement = efficiencyStatementManager
				.createUserEfficiencyStatement(new Date(), 5.0f, "g1", "pc1", true, assessedIdentity, resource);
		dbInstance.commitAndCloseSession();
		Assert.assertNotNull(statement);
		
		// get the statement
		RestConnection conn = new RestConnection();
		Assert.assertTrue(conn.login("administrator", "openolat"));
		
		URI uri = UriBuilder.fromUri(getContextURI()).path("repo").path("courses")
				.path(resource.getKey().toString())
				.path("statements").path(assessedIdentity.getKey().toString()).build();
		HttpGet getStatement = conn.createGet(uri, MediaType.APPLICATION_JSON, true);
		HttpResponse response = conn.execute(getStatement);
		assertEquals(200, response.getStatusLine().getStatusCode());
		EfficiencyStatementVO statementVO = conn.parse(response, EfficiencyStatementVO.class);
		Assert.assertNotNull(statementVO);
		Assert.assertEquals(5.0f, statementVO.getScore(), 0.0001);
		Assert.assertEquals("g1", statementVO.getGrade());
		Assert.assertEquals("pc1", statementVO.getPerformanceClassIdent());
		Assert.assertEquals(true, statementVO.getPassed());
		Assert.assertEquals(assessedIdentity.getKey(), statementVO.getIdentityKey());
	}

	@Test
	public void putEfficiencyStatement() throws IOException, URISyntaxException {
		RestConnection conn = new RestConnection();
		Assert.assertTrue(conn.login("administrator", "openolat"));

		Identity assessedIdentity = JunitTestHelper.createAndPersistIdentityAsRndUser("cert-3");
		Identity author = JunitTestHelper.createAndPersistIdentityAsAuthor("cert-4");
		RepositoryEntry entry = JunitTestHelper.deployBasicCourse(author);
		dbInstance.commitAndCloseSession();

		URI uri = UriBuilder.fromUri(getContextURI()).path("repo").path("courses")
				.path(entry.getOlatResource().getKey().toString())
				.path("statements").path(assessedIdentity.getKey().toString()).build();

		EfficiencyStatementVO statement = new EfficiencyStatementVO();
		statement.setCreationDate(new Date());
		statement.setPassed(Boolean.TRUE);
		statement.setScore(2.5f);

		HttpPut method = conn.createPut(uri, MediaType.APPLICATION_JSON, true);
		conn.addJsonEntity(method, statement);

		HttpResponse response = conn.execute(method);
		assertEquals(200, response.getStatusLine().getStatusCode());
		EntityUtils.consume(response.getEntity());

		UserEfficiencyStatement efficiencyStatement = efficiencyStatementManager
				.getUserEfficiencyStatementFull(entry, assessedIdentity);

		Assert.assertNotNull(efficiencyStatement);
		Assert.assertNotNull(efficiencyStatement.getCourseRepoKey());
		Assert.assertEquals(entry.getKey(), efficiencyStatement.getCourseRepoKey());
		Assert.assertEquals(2.5f, efficiencyStatement.getScore(), 0.001);
		Assert.assertEquals(Boolean.TRUE, efficiencyStatement.getPassed());
		Assert.assertEquals(assessedIdentity, efficiencyStatement.getIdentity());
	}
	
	@Test
	public void putEfficiencyStatement_standalone() throws IOException, URISyntaxException {
		RestConnection conn = new RestConnection();
		Assert.assertTrue(conn.login("administrator", "openolat"));

		Identity assessedIdentity = JunitTestHelper.createAndPersistIdentityAsRndUser("cert-3");
		dbInstance.commitAndCloseSession();
		
		Long resourceKey = 3495783497l;

		URI uri = UriBuilder.fromUri(getContextURI()).path("repo").path("courses")
				.path(resourceKey.toString())
				.path("statements").path(assessedIdentity.getKey().toString()).build();

		EfficiencyStatementVO statement = new EfficiencyStatementVO();
		statement.setCreationDate(new Date());
		statement.setPassed(Boolean.TRUE);
		statement.setScore(8.5f);
		statement.setCourseTitle("Standalone");

		HttpPut method = conn.createPut(uri, MediaType.APPLICATION_JSON, true);
		conn.addJsonEntity(method, statement);

		HttpResponse response = conn.execute(method);
		assertEquals(200, response.getStatusLine().getStatusCode());
		EntityUtils.consume(response.getEntity());

		//check the efficiency statement
		List<UserEfficiencyStatement> efficiencyStatements = efficiencyStatementManager
				.getUserEfficiencyStatementLight(assessedIdentity);
		Assert.assertNotNull(efficiencyStatements);
		Assert.assertEquals(1, efficiencyStatements.size());
		
		UserEfficiencyStatement efficiencyStatement = efficiencyStatements.get(0);
		Assert.assertEquals(8.5f, efficiencyStatement.getScore(), 0.001);
		Assert.assertEquals(Boolean.TRUE, efficiencyStatement.getPassed());
		Assert.assertEquals("Standalone", efficiencyStatement.getShortTitle());
		Assert.assertEquals(assessedIdentity, efficiencyStatement.getIdentity());
	}
	
	/**
	 * Don't give the identity key in URL but in the JSON object
	 * 
	 * @throws IOException
	 * @throws URISyntaxException
	 */
	@Test
	public void putEfficiencyStatement_standalone_alt() throws IOException, URISyntaxException {
		RestConnection conn = new RestConnection();
		Assert.assertTrue(conn.login("administrator", "openolat"));

		Identity assessedIdentity = JunitTestHelper.createAndPersistIdentityAsRndUser("cert-3");
		dbInstance.commitAndCloseSession();
		
		Long resourceKey = 3495783497l;

		URI uri = UriBuilder.fromUri(getContextURI()).path("repo").path("courses")
				.path(resourceKey.toString())
				.path("statements").build();

		EfficiencyStatementVO statement = new EfficiencyStatementVO();
		statement.setIdentityKey(assessedIdentity.getKey());
		statement.setCreationDate(new Date());
		statement.setPassed(Boolean.TRUE);
		statement.setScore(8.5f);
		statement.setCourseTitle("Standalone");

		HttpPut method = conn.createPut(uri, MediaType.APPLICATION_JSON, true);
		conn.addJsonEntity(method, statement);

		HttpResponse response = conn.execute(method);
		assertEquals(200, response.getStatusLine().getStatusCode());
		EntityUtils.consume(response.getEntity());

		//check the efficiency statement
		List<UserEfficiencyStatement> efficiencyStatements = efficiencyStatementManager
				.getUserEfficiencyStatementLight(assessedIdentity);
		Assert.assertNotNull(efficiencyStatements);
		Assert.assertEquals(1, efficiencyStatements.size());
		
		UserEfficiencyStatement efficiencyStatement = efficiencyStatements.get(0);
		Assert.assertEquals(8.5f, efficiencyStatement.getScore(), 0.001);
		Assert.assertEquals(Boolean.TRUE, efficiencyStatement.getPassed());
		Assert.assertEquals("Standalone", efficiencyStatement.getShortTitle());
		Assert.assertEquals(assessedIdentity, efficiencyStatement.getIdentity());
	}
}
