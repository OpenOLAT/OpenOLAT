/**
 * <a href="https://www.openolat.org">
 * OpenOLAT - Online Learning and Training</a><br>
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); <br>
 * you may not use this file except in compliance with the License.<br>
 * You may obtain a copy of the License at the
 * <a href="https://www.apache.org/licenses/LICENSE-2.0">Apache homepage</a>
 * <p>
 * Unless required by applicable law or agreed to in writing,<br>
 * software distributed under the License is distributed on an "AS IS" BASIS, <br>
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
 * See the License for the specific language governing permissions and <br>
 * limitations under the License.
 * <p>
 * Initial code contributed and copyrighted by<br>
 * frentix GmbH, https://www.frentix.com
 * <p>
 */
package org.olat.restapi;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;

import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.UriBuilder;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.util.EntityUtils;
import org.junit.Assert;
import org.junit.Test;
import org.olat.basesecurity.GroupRoles;
import org.olat.core.commons.persistence.DB;
import org.olat.core.id.Roles;
import org.olat.course.CourseFactory;
import org.olat.course.config.CourseConfig;
import org.olat.course.disclaimer.CourseDisclaimerManager;
import org.olat.course.disclaimer.restapi.CourseDisclaimerTextVO;
import org.olat.course.disclaimer.restapi.CourseDisclaimerVO;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryService;
import org.olat.restapi.support.vo.ErrorVO;
import org.olat.test.JunitTestHelper;
import org.olat.test.JunitTestHelper.IdentityWithLogin;
import org.olat.test.OlatRestTestCase;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Test the REST API of the course disclaimer.
 *
 * Initial date: 2 nov. 2026<br>
 * @author srosse, stephane.rosse@frentix.com, https://www.frentix.com
 */
public class CourseDisclaimerTest extends OlatRestTestCase {

	@Autowired
	private DB dbInstance;
	@Autowired
	private RepositoryService repositoryService;
	@Autowired
	private CourseDisclaimerManager courseDisclaimerManager;

	@Test
	public void getDisclaimer()
	throws IOException, URISyntaxException {
		IdentityWithLogin owner = JunitTestHelper.createAndPersistRndAuthor("rest-disc-1");
		RepositoryEntry courseEntry = JunitTestHelper.deployBasicCourse(owner.getIdentity());
		initDisclaimer(courseEntry);
		dbInstance.commitAndCloseSession();

		RestConnection conn = new RestConnection(owner);
		HttpGet method = conn.createGet(getDisclaimerUri(courseEntry), MediaType.APPLICATION_JSON, true);
		HttpResponse response = conn.execute(method);
		Assert.assertEquals(200, response.getStatusLine().getStatusCode());

		CourseDisclaimerVO disclaimerVo = conn.parse(response, CourseDisclaimerVO.class);
		Assert.assertNotNull(disclaimerVo);
		Assert.assertEquals(Boolean.TRUE, disclaimerVo.getTermsOfUse().getEnabled());
		Assert.assertEquals("Terms of use", disclaimerVo.getTermsOfUse().getTitle());
		Assert.assertEquals("<p>Be nice</p>", disclaimerVo.getTermsOfUse().getTerms());
		Assert.assertEquals("I accept", disclaimerVo.getTermsOfUse().getLabel1());
		Assert.assertEquals(Boolean.FALSE, disclaimerVo.getDataProtection().getEnabled());
		Assert.assertEquals(Long.valueOf(0), disclaimerVo.getConsentCount());
		Assert.assertNotNull(disclaimerVo.getModuleEnabled());

		conn.shutdown();
	}

	@Test
	public void getDisclaimerForbidden()
	throws IOException, URISyntaxException {
		IdentityWithLogin owner = JunitTestHelper.createAndPersistRndAuthor("rest-disc-2");
		IdentityWithLogin participant = JunitTestHelper.createAndPersistRndUser("rest-disc-3");
		RepositoryEntry courseEntry = JunitTestHelper.deployBasicCourse(owner.getIdentity());
		repositoryService.addRole(participant.getIdentity(), courseEntry, GroupRoles.participant.name());
		dbInstance.commitAndCloseSession();

		RestConnection conn = new RestConnection(participant);
		HttpGet method = conn.createGet(getDisclaimerUri(courseEntry), MediaType.APPLICATION_JSON, true);
		HttpResponse response = conn.execute(method);
		Assert.assertEquals(403, response.getStatusLine().getStatusCode());
		EntityUtils.consume(response.getEntity());

		HttpPut putMethod = conn.createPut(getDisclaimerUri(courseEntry), MediaType.APPLICATION_JSON, true);
		conn.addJsonEntity(putMethod, createDisclaimerVO());
		HttpResponse putResponse = conn.execute(putMethod);
		Assert.assertEquals(403, putResponse.getStatusLine().getStatusCode());
		EntityUtils.consume(putResponse.getEntity());

		conn.shutdown();
	}

	@Test
	public void putDisclaimer()
	throws IOException, URISyntaxException {
		IdentityWithLogin owner = JunitTestHelper.createAndPersistRndAuthor("rest-disc-4");
		RepositoryEntry courseEntry = JunitTestHelper.deployBasicCourse(owner.getIdentity());
		dbInstance.commitAndCloseSession();

		RestConnection conn = new RestConnection(owner);
		HttpPut method = conn.createPut(getDisclaimerUri(courseEntry), MediaType.APPLICATION_JSON, true);
		conn.addJsonEntity(method, createDisclaimerVO());
		HttpResponse response = conn.execute(method);
		Assert.assertEquals(200, response.getStatusLine().getStatusCode());

		CourseDisclaimerVO savedVo = conn.parse(response, CourseDisclaimerVO.class);
		Assert.assertEquals(Boolean.TRUE, savedVo.getTermsOfUse().getEnabled());
		Assert.assertEquals("REST terms of use", savedVo.getTermsOfUse().getTitle());
		Assert.assertEquals(Boolean.FALSE, savedVo.getDataProtection().getEnabled());

		// check the course configuration
		CourseConfig config = CourseFactory.loadCourse(courseEntry).getCourseEnvironment().getCourseConfig();
		Assert.assertTrue(config.isDisclaimerEnabled(1));
		Assert.assertFalse(config.isDisclaimerEnabled(2));
		Assert.assertEquals("REST terms of use", config.getDisclaimerTitel(1));
		Assert.assertEquals("<p>Terms</p>", config.getDisclaimerTerms(1));
		Assert.assertEquals("I accept", config.getDisclaimerLabel(1, 1));
		Assert.assertEquals("I really accept", config.getDisclaimerLabel(1, 2));

		// round trip
		HttpGet getMethod = conn.createGet(getDisclaimerUri(courseEntry), MediaType.APPLICATION_JSON, true);
		HttpResponse getResponse = conn.execute(getMethod);
		Assert.assertEquals(200, getResponse.getStatusLine().getStatusCode());
		CourseDisclaimerVO disclaimerVo = conn.parse(getResponse, CourseDisclaimerVO.class);
		Assert.assertEquals("REST terms of use", disclaimerVo.getTermsOfUse().getTitle());
		Assert.assertEquals("<p>Terms</p>", disclaimerVo.getTermsOfUse().getTerms());

		conn.shutdown();
	}

	@Test
	public void putDisclaimerMissingTitle()
	throws IOException, URISyntaxException {
		IdentityWithLogin owner = JunitTestHelper.createAndPersistRndAuthor("rest-disc-6");
		RepositoryEntry courseEntry = JunitTestHelper.deployBasicCourse(owner.getIdentity());
		dbInstance.commitAndCloseSession();

		CourseDisclaimerVO disclaimerVo = createDisclaimerVO();
		disclaimerVo.getTermsOfUse().setTitle(null);

		RestConnection conn = new RestConnection(owner);
		HttpPut method = conn.createPut(getDisclaimerUri(courseEntry), MediaType.APPLICATION_JSON, true);
		conn.addJsonEntity(method, disclaimerVo);
		HttpResponse response = conn.execute(method);
		Assert.assertEquals(400, response.getStatusLine().getStatusCode());

		List<ErrorVO> errors = conn.parseList(response, ErrorVO.class);
		Assert.assertEquals(1, errors.size());
		Assert.assertEquals("termsOfUse.title:mandatory", errors.get(0).getCode());

		CourseConfig config = CourseFactory.loadCourse(courseEntry).getCourseEnvironment().getCourseConfig();
		Assert.assertFalse(config.isDisclaimerEnabled());

		conn.shutdown();
	}

	@Test
	public void putDisclaimerUnknownConsents()
	throws IOException, URISyntaxException {
		IdentityWithLogin owner = JunitTestHelper.createAndPersistRndAuthor("rest-disc-7");
		RepositoryEntry courseEntry = JunitTestHelper.deployBasicCourse(owner.getIdentity());
		dbInstance.commitAndCloseSession();

		RestConnection conn = new RestConnection(owner);
		URI uri = UriBuilder.fromUri(getDisclaimerUri(courseEntry)).queryParam("consents", "forget").build();
		HttpPut method = conn.createPut(uri, MediaType.APPLICATION_JSON, true);
		conn.addJsonEntity(method, createDisclaimerVO());
		HttpResponse response = conn.execute(method);
		Assert.assertEquals(400, response.getStatusLine().getStatusCode());
		EntityUtils.consume(response.getEntity());

		conn.shutdown();
	}

	@Test
	public void putDisclaimerRemoveWhileEnabled()
	throws IOException, URISyntaxException {
		IdentityWithLogin owner = JunitTestHelper.createAndPersistRndAuthor("rest-disc-8");
		RepositoryEntry courseEntry = JunitTestHelper.deployBasicCourse(owner.getIdentity());
		dbInstance.commitAndCloseSession();

		RestConnection conn = new RestConnection(owner);
		URI uri = UriBuilder.fromUri(getDisclaimerUri(courseEntry)).queryParam("consents", "remove").build();
		HttpPut method = conn.createPut(uri, MediaType.APPLICATION_JSON, true);
		conn.addJsonEntity(method, createDisclaimerVO());
		HttpResponse response = conn.execute(method);
		Assert.assertEquals(400, response.getStatusLine().getStatusCode());

		List<ErrorVO> errors = conn.parseList(response, ErrorVO.class);
		Assert.assertEquals(1, errors.size());
		Assert.assertEquals("consents", errors.get(0).getCode());

		conn.shutdown();
	}

	@Test
	public void putDisclaimerRevokeConsents()
	throws IOException, URISyntaxException {
		IdentityWithLogin owner = JunitTestHelper.createAndPersistRndAuthor("rest-disc-9");
		IdentityWithLogin participant = JunitTestHelper.createAndPersistRndUser("rest-disc-10");
		RepositoryEntry courseEntry = JunitTestHelper.deployBasicCourse(owner.getIdentity());
		repositoryService.addRole(participant.getIdentity(), courseEntry, GroupRoles.participant.name());
		initDisclaimer(courseEntry);
		Roles participantRoles = Roles.userRoles();
		courseDisclaimerManager.acceptDisclaimer(courseEntry, participant.getIdentity(), participantRoles, true, false);
		dbInstance.commitAndCloseSession();
		Assert.assertTrue(courseDisclaimerManager.isAccessGranted(courseEntry, participant.getIdentity(), participantRoles));

		RestConnection conn = new RestConnection(owner);
		URI uri = UriBuilder.fromUri(getDisclaimerUri(courseEntry)).queryParam("consents", "revoke").build();
		HttpPut method = conn.createPut(uri, MediaType.APPLICATION_JSON, true);
		conn.addJsonEntity(method, createDisclaimerVO());
		HttpResponse response = conn.execute(method);
		Assert.assertEquals(200, response.getStatusLine().getStatusCode());

		// revoked consents are still counted
		CourseDisclaimerVO savedVo = conn.parse(response, CourseDisclaimerVO.class);
		Assert.assertEquals(Long.valueOf(1), savedVo.getConsentCount());
		dbInstance.commitAndCloseSession();
		Assert.assertFalse(courseDisclaimerManager.isAccessGranted(courseEntry, participant.getIdentity(), participantRoles));

		conn.shutdown();
	}

	@Test
	public void putDisclaimerKeepsMissingText()
	throws IOException, URISyntaxException {
		IdentityWithLogin owner = JunitTestHelper.createAndPersistRndAuthor("rest-disc-11");
		RepositoryEntry courseEntry = JunitTestHelper.deployBasicCourse(owner.getIdentity());
		initDisclaimer(courseEntry);
		dbInstance.commitAndCloseSession();

		CourseDisclaimerVO disclaimerVo = new CourseDisclaimerVO();
		disclaimerVo.setDataProtection(createTextVO("Data protection"));

		RestConnection conn = new RestConnection(owner);
		HttpPut method = conn.createPut(getDisclaimerUri(courseEntry), MediaType.APPLICATION_JSON, true);
		conn.addJsonEntity(method, disclaimerVo);
		HttpResponse response = conn.execute(method);
		Assert.assertEquals(200, response.getStatusLine().getStatusCode());
		EntityUtils.consume(response.getEntity());

		CourseConfig config = CourseFactory.loadCourse(courseEntry).getCourseEnvironment().getCourseConfig();
		Assert.assertTrue(config.isDisclaimerEnabled(1));
		Assert.assertEquals("Terms of use", config.getDisclaimerTitel(1));
		Assert.assertTrue(config.isDisclaimerEnabled(2));
		Assert.assertEquals("Data protection", config.getDisclaimerTitel(2));

		conn.shutdown();
	}

	@Test
	public void putDisclaimerKeepsEnabled()
	throws IOException, URISyntaxException {
		IdentityWithLogin owner = JunitTestHelper.createAndPersistRndAuthor("rest-disc-13");
		RepositoryEntry courseEntry = JunitTestHelper.deployBasicCourse(owner.getIdentity());
		initDisclaimer(courseEntry);
		dbInstance.commitAndCloseSession();
		
		CourseDisclaimerVO disclaimerVo = new CourseDisclaimerVO();
		CourseDisclaimerTextVO termsOfUseVo = createTextVO("Changed title");
		termsOfUseVo.setEnabled(null);
		disclaimerVo.setTermsOfUse(termsOfUseVo);
		
		RestConnection conn = new RestConnection(owner);
		HttpPut method = conn.createPut(getDisclaimerUri(courseEntry), MediaType.APPLICATION_JSON, true);
		conn.addJsonEntity(method, disclaimerVo);
		HttpResponse response = conn.execute(method);
		Assert.assertEquals(200, response.getStatusLine().getStatusCode());
		EntityUtils.consume(response.getEntity());
		
		CourseConfig config = CourseFactory.loadCourse(courseEntry).getCourseEnvironment().getCourseConfig();
		Assert.assertTrue(config.isDisclaimerEnabled(1));
		Assert.assertEquals("Changed title", config.getDisclaimerTitel(1));
		
		conn.shutdown();
	}
	
	@Test
	public void putDisclaimerLocked()
	throws IOException, URISyntaxException {
		IdentityWithLogin owner = JunitTestHelper.createAndPersistRndAuthor("rest-disc-12");
		RepositoryEntry courseEntry = JunitTestHelper.deployBasicCourse(owner.getIdentity());
		dbInstance.commitAndCloseSession();

		Long courseResId = courseEntry.getOlatResource().getResourceableId();
		CourseFactory.openCourseEditSession(courseResId);
		try {
			RestConnection conn = new RestConnection(owner);
			HttpPut method = conn.createPut(getDisclaimerUri(courseEntry), MediaType.APPLICATION_JSON, true);
			conn.addJsonEntity(method, createDisclaimerVO());
			HttpResponse response = conn.execute(method);
			Assert.assertEquals(409, response.getStatusLine().getStatusCode());
			EntityUtils.consume(response.getEntity());
			conn.shutdown();
		} finally {
			CourseFactory.closeCourseEditSession(courseResId, true);
		}

		CourseConfig config = CourseFactory.loadCourse(courseEntry).getCourseEnvironment().getCourseConfig();
		Assert.assertFalse(config.isDisclaimerEnabled());
	}

	private URI getDisclaimerUri(RepositoryEntry courseEntry) {
		return UriBuilder.fromUri(getContextURI()).path("repo").path("courses")
				.path(courseEntry.getOlatResource().getResourceableId().toString()).path("disclaimer").build();
	}

	private void initDisclaimer(RepositoryEntry courseEntry) {
		Long courseResId = courseEntry.getOlatResource().getResourceableId();
		CourseFactory.openCourseEditSession(courseResId);
		CourseConfig config = CourseFactory.getCourseEditSession(courseResId).getCourseEnvironment().getCourseConfig();
		config.setDisclaimerEnabled(1, true);
		config.setDisclaimerTitle(1, "Terms of use");
		config.setDisclaimerTerms(1, "<p>Be nice</p>");
		config.setDisclaimerLabel(1, 1, "I accept");
		config.setDisclaimerLabel(1, 2, "");
		CourseFactory.setCourseConfig(courseResId, config);
		CourseFactory.saveCourse(courseResId);
		CourseFactory.closeCourseEditSession(courseResId, true);
	}

	private CourseDisclaimerVO createDisclaimerVO() {
		CourseDisclaimerVO disclaimerVo = new CourseDisclaimerVO();
		disclaimerVo.setTermsOfUse(createTextVO("REST terms of use"));
		CourseDisclaimerTextVO dataProtectionVo = new CourseDisclaimerTextVO();
		dataProtectionVo.setEnabled(Boolean.FALSE);
		disclaimerVo.setDataProtection(dataProtectionVo);
		return disclaimerVo;
	}

	private CourseDisclaimerTextVO createTextVO(String title) {
		CourseDisclaimerTextVO textVo = new CourseDisclaimerTextVO();
		textVo.setEnabled(Boolean.TRUE);
		textVo.setTitle(title);
		textVo.setTerms("<p>Terms</p>");
		textVo.setLabel1("I accept");
		textVo.setLabel2("I really accept");
		return textVo;
	}
}
