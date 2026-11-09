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

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.UriBuilder;

import org.apache.commons.lang3.time.DateUtils;
import org.assertj.core.api.Assertions;
import org.junit.Assert;
import org.junit.Test;
import org.olat.core.commons.persistence.DB;
import org.olat.core.id.Identity;
import org.olat.core.util.httpclient.ConnectionUtilities.NameValuePair;
import org.olat.core.util.vfs.VFSLeaf;
import org.olat.course.certificate.Certificate;
import org.olat.course.certificate.CertificateLight;
import org.olat.course.certificate.CertificateManagedFlag;
import org.olat.course.certificate.CertificatesManager;
import org.olat.course.certificate.manager.CertificatesManagerTest;
import org.olat.course.certificate.model.CertificateConfig;
import org.olat.course.certificate.model.CertificateInfos;
import org.olat.course.certificate.restapi.CertificateVO;
import org.olat.course.certificate.restapi.CertificateVOes;
import org.olat.modules.certificationprogram.CertificationProgram;
import org.olat.modules.certificationprogram.CertificationProgramService;
import org.olat.modules.certificationprogram.RecertificationMode;
import org.olat.modules.certificationprogram.ui.component.DurationType;
import org.olat.repository.RepositoryEntry;
import org.olat.restapi.support.ObjectFactory;
import org.olat.test.JunitTestHelper;
import org.olat.test.OlatRestTestCase;
import org.olat.user.restapi.UserVO;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 13 août 2021<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class UserCertificationWebServiceTest extends OlatRestTestCase {

	@Autowired
	private DB dbInstance;
	@Autowired
	private CertificatesManager certificatesManager;
	@Autowired
	private CertificationProgramService certificationProgramService;
	
	@Test
	public void getUserCertificatesInformations() throws IOException, URISyntaxException, InterruptedException {
		RestConnection conn = new RestConnection("administrator", "openolat");

		Identity assessedIdentity = JunitTestHelper.createAndPersistIdentityAsRndUser("user-cert-1");
		Identity author = JunitTestHelper.getDefaultAuthor();
		RepositoryEntry entry = JunitTestHelper.deployBasicCourse(author);
		dbInstance.commitAndCloseSession();

		CertificateInfos certificateInfos = new CertificateInfos(assessedIdentity, 2.0f, Float.valueOf(10), true,
				Double.valueOf(0.2), "", null);
		CertificateConfig config = CertificateConfig.builder()
				.withSendEmail(true)
				.build();
		Certificate certificate = certificatesManager.generateCertificate(certificateInfos, entry, null, config);
		dbInstance.commitAndCloseSession();
		Assert.assertNotNull(certificate);
		//wait until the certificate is created
		triggerAndWaitMessageAreConsumed(CertificatesManager.GENERATION_JOB_TRIGGER_KEY);
		
		URI uri = UriBuilder.fromUri(getContextURI()).path("users")
				.path(assessedIdentity.getKey().toString())
				.path("certificates").build();
		HttpRequest method = conn.createGet(uri, MediaType.APPLICATION_JSON);
		HttpResponse<InputStream> response = conn.execute(method);
		Assert.assertEquals(200, response.statusCode());
		
		CertificateVOes certificateVoes = conn.parse(response, CertificateVOes.class);
		Assert.assertNotNull(certificateVoes);
		Assert.assertNotNull(certificateVoes.getCertificates());
		Assert.assertEquals(1, certificateVoes.getCertificates().size());
		
		CertificateVO certificateVo = certificateVoes.getCertificates().get(0);
		Assert.assertNotNull(certificateVo);
		Assert.assertEquals(certificate.getKey(), certificateVo.getKey());
		Assert.assertEquals(assessedIdentity.getKey(), certificateVo.getIdentityKey());
		Assert.assertEquals(certificate.getUuid(), certificateVo.getUuid());
		
		UserVO user = certificateVo.getUser();
		Assert.assertNotNull(user);
		Assert.assertEquals(assessedIdentity.getUser().getFirstName(), user.getFirstName());
		Assert.assertEquals(assessedIdentity.getUser().getLastName(), user.getLastName());
		conn.close();

	}
	
	@Test
	public void getUserManagedCertificatesInfos() throws IOException, URISyntaxException, InterruptedException {
		RestConnection conn = new RestConnection("administrator", "openolat");

		Identity identity = JunitTestHelper.createAndPersistIdentityAsRndUser("user-cert-6");
		URL certificateUrl = CertificatesManagerTest.class.getResource("template.pdf");
		Assert.assertNotNull(certificateUrl);
		File certificateFile = new File(certificateUrl.toURI());
		CertificateManagedFlag[] managedFlags = { CertificateManagedFlag.delete }; 
		Certificate managedCertificate = certificatesManager
				.uploadStandaloneCertificate(identity, new Date(), "DM-234", managedFlags, "My floating course", -1l, null, null, certificateFile, identity);
		Certificate certificate = certificatesManager
				.uploadStandaloneCertificate(identity, new Date(), null, null, "My floating course", -1l, null, null, certificateFile, identity);
		dbInstance.commitAndCloseSession();
		
		// Get managed
		URI uri = UriBuilder.fromUri(getContextURI()).path("users")
				.path(identity.getKey().toString())
				.path("certificates")
				.queryParam("managed", "true")
				.queryParam("last", "true")
				.build();
		HttpRequest method = conn.createGet(uri, MediaType.APPLICATION_JSON);
		HttpResponse<InputStream> response = conn.execute(method);
		Assert.assertEquals(200, response.statusCode());
		
		CertificateVOes certificateVoes = conn.parse(response, CertificateVOes.class);
		Assert.assertNotNull(certificateVoes);
		Assert.assertNotNull(certificateVoes.getCertificates());
		Assert.assertEquals(1, certificateVoes.getCertificates().size());
		
		CertificateVO certificateVo = certificateVoes.getCertificates().get(0);
		Assert.assertNotNull(certificateVo);
		Assert.assertEquals(managedCertificate.getKey(), certificateVo.getKey());
		
		// Get all 
		URI allUri = UriBuilder.fromUri(getContextURI()).path("users")
				.path(identity.getKey().toString())
				.path("certificates")
				.queryParam("last", "true")
				.build();
		HttpRequest allMethod = conn.createGet(allUri, MediaType.APPLICATION_JSON);
		HttpResponse<InputStream> allResponse = conn.execute(allMethod);
		Assert.assertEquals(200, allResponse.statusCode());
		
		CertificateVOes allCertificateVoes = conn.parse(allResponse, CertificateVOes.class);
		Assert.assertNotNull(allCertificateVoes);
		Assert.assertNotNull(allCertificateVoes.getCertificates());

		Assertions.assertThat(allCertificateVoes.getCertificates())
			.hasSize(2)
			.map(CertificateVO::getKey)
			.containsExactlyInAnyOrder(certificate.getKey(), managedCertificate.getKey());
		conn.close();

	}
	
	@Test
	public void getUserManagedCertificatesInfosByExternalId() throws IOException, URISyntaxException, InterruptedException {
		RestConnection conn = new RestConnection("administrator", "openolat");

		Identity identity = JunitTestHelper.createAndPersistIdentityAsRndUser("user-cert-6");
		URL certificateUrl = CertificatesManagerTest.class.getResource("template.pdf");
		Assert.assertNotNull(certificateUrl);
		File certificateFile = new File(certificateUrl.toURI());
		String externalId = UUID.randomUUID().toString();
		Certificate managedCertificate = certificatesManager
				.uploadStandaloneCertificate(identity, new Date(), externalId, null, "My floating course", -1l, null, null, certificateFile, identity);
		dbInstance.commitAndCloseSession();
		
		// Get managed
		URI uri = UriBuilder.fromUri(getContextURI()).path("users")
				.path(identity.getKey().toString())
				.path("certificates")
				.queryParam("externalId", externalId)
				.build();
		HttpRequest method = conn.createGet(uri, MediaType.APPLICATION_JSON);
		HttpResponse<InputStream> response = conn.execute(method);
		Assert.assertEquals(200, response.statusCode());
		
		CertificateVOes certificateVoes = conn.parse(response, CertificateVOes.class);
		Assert.assertNotNull(certificateVoes);
		Assert.assertNotNull(certificateVoes.getCertificates());
		Assert.assertEquals(1, certificateVoes.getCertificates().size());
		
		CertificateVO certificateVo = certificateVoes.getCertificates().get(0);
		Assert.assertNotNull(certificateVo);
		Assert.assertEquals(managedCertificate.getKey(), certificateVo.getKey());
		conn.close();

	}
	
	@Test
	public void getUserCertificatePdf() throws IOException, URISyntaxException, InterruptedException {
		RestConnection conn = new RestConnection("administrator", "openolat");

		Identity assessedIdentity = JunitTestHelper.createAndPersistIdentityAsRndUser("user-cert-2");
		Identity author = JunitTestHelper.getDefaultAuthor();
		RepositoryEntry entry = JunitTestHelper.deployBasicCourse(author);
		dbInstance.commitAndCloseSession();

		CertificateInfos certificateInfos = new CertificateInfos(assessedIdentity, 2.0f, Float.valueOf(10), true,
				Double.valueOf(0.2), "", null);
		CertificateConfig config = CertificateConfig.builder()
				.withSendEmail(false)
				.build();
		Certificate certificate = certificatesManager.generateCertificate(certificateInfos, entry, null, config);
		dbInstance.commitAndCloseSession();
		Assert.assertNotNull(certificate);
		//wait until the certificate is created
		triggerAndWaitMessageAreConsumed(CertificatesManager.GENERATION_JOB_TRIGGER_KEY);
		
		URI uri = UriBuilder.fromUri(getContextURI()).path("users")
				.path(assessedIdentity.getKey().toString())
				.path("certificates").path(certificate.getKey().toString()).build();
		HttpRequest method = conn.createHead(uri, "application/pdf");
		HttpResponse<InputStream> response = conn.execute(method);
		Assert.assertEquals(200, response.statusCode());
		RestConnection.consume(response);
		
		//check  with a stupid number
		HttpRequest getMethod = conn.createGet(uri, "application/pdf");
		HttpResponse<InputStream> getResponse = conn.execute(getMethod);
		Assert.assertEquals(200, getResponse.statusCode());
		RestConnection.consume(getResponse);
		conn.close();

	}
	
	@Test
	public void getUserCertificateInfos() throws IOException, URISyntaxException, InterruptedException {
		RestConnection conn = new RestConnection("administrator", "openolat");

		Identity identity = JunitTestHelper.createAndPersistIdentityAsRndUser("user-cert-5");
		URL certificateUrl = CertificatesManagerTest.class.getResource("template.pdf");
		Assert.assertNotNull(certificateUrl);
		File certificateFile = new File(certificateUrl.toURI());
		CertificateManagedFlag[] managedFlags = { CertificateManagedFlag.delete };
		
		Calendar cal = Calendar.getInstance();
		cal.set(Calendar.MINUTE, 0);
		cal.set(Calendar.SECOND, 0);
		cal.set(Calendar.MILLISECOND, 0);
		cal.add(Calendar.DATE, 3);
		Date nextCertificationDate = cal.getTime();
		
		Certificate certificate = certificatesManager
				.uploadStandaloneCertificate(identity, new Date(), "DM-234", managedFlags, "My floating course", -1l, null, nextCertificationDate, certificateFile, identity);
		dbInstance.commitAndCloseSession();
		Assert.assertNotNull(certificate);
		
		URI uri = UriBuilder.fromUri(getContextURI()).path("users")
				.path(identity.getKey().toString())
				.path("certificates").path(certificate.getKey().toString()).build();
		HttpRequest method = conn.createGet(uri, MediaType.APPLICATION_JSON);
		HttpResponse<InputStream> response = conn.execute(method);
		Assert.assertEquals(200, response.statusCode());
		CertificateVO certificateVo = conn.parse(response, CertificateVO.class);
		
		Assert.assertNotNull(certificateVo);
		Assert.assertNotNull(certificateVo.getCreationDate());
		Assert.assertEquals("My floating course", certificateVo.getCourseTitle());
		Assert.assertEquals("DM-234", certificateVo.getExternalId());
		Assert.assertEquals("delete", certificateVo.getManagedFlags());
		Assert.assertEquals(Long.valueOf(-1l), certificateVo.getCourseResourceKey());
		Assert.assertEquals(nextCertificationDate, certificateVo.getNextCertificationDate());
		conn.close();

	}
	
	@Test
	public void uploadCertificateStandalone() throws IOException, URISyntaxException, InterruptedException {
		RestConnection conn = new RestConnection("administrator", "openolat");

		Identity assessedIdentity = JunitTestHelper.createAndPersistIdentityAsRndUser("cert-4");
		dbInstance.commitAndCloseSession();

		URI uri = UriBuilder.fromUri(getContextURI()).path("users")
				.path(assessedIdentity.getKey().toString())
				.path("certificates").build();

		URL certificateUrl = CourseCertificationTest.class.getResource("certificate.pdf");
		Assert.assertNotNull(certificateUrl);
		File certificateFile = new File(certificateUrl.toURI());
		
		Calendar cal = Calendar.getInstance();
		cal.set(Calendar.MINUTE, 0);
		cal.set(Calendar.SECOND, 0);
		cal.set(Calendar.MILLISECOND, 0);
		Date creationDate = cal.getTime();
		cal.add(Calendar.DATE, 3);
		Date nextCertificationDate = cal.getTime();
		
		List<NameValuePair> formParameters = List.of(
				new NameValuePair("archivedResourceKey", "726348723"),
				new NameValuePair("externalId", "DM-726348723"),
				new NameValuePair("managedFlags", "delete"),
				new NameValuePair("creationDate", ObjectFactory.formatDate(creationDate)),
				new NameValuePair("nextRecertificationDate", ObjectFactory.formatDate(nextCertificationDate)));
		HttpRequest method = conn.createPost(uri, certificateFile, certificateFile.getName(), formParameters, MediaType.APPLICATION_JSON);
		HttpResponse<InputStream> response = conn.execute(method);
		Assert.assertEquals(200, response.statusCode());
		RestConnection.consume(response);

		//Check certificate
		List<CertificateLight> certificates = certificatesManager.getLastCertificates(assessedIdentity);
		Assert.assertNotNull(certificates);
		//Check the certificate file
		Certificate certificate = certificatesManager.getCertificateById(certificates.get(0).getKey());
		VFSLeaf certificateLeaf = certificatesManager.getCertificateLeaf(certificate);
		Assert.assertNotNull(certificateLeaf);
		Assert.assertEquals(certificateFile.length(), certificateLeaf.getSize());
		// Check data
		Assert.assertEquals(Long.valueOf(726348723l), certificate.getArchivedResourceKey());
		Assert.assertEquals("DM-726348723", certificate.getExternalId());
		Assert.assertNotNull(certificate.getManagedFlags());
		Assert.assertEquals(1, certificate.getManagedFlags().length);
		Assert.assertEquals(CertificateManagedFlag.delete, certificate.getManagedFlags()[0]);
		Assert.assertEquals(creationDate, certificate.getCreationDate());
		Assert.assertEquals(nextCertificationDate, certificate.getNextRecertificationDate());
		conn.close();
	}
	
	@Test
	public void deleteUserCertificate() throws IOException, URISyntaxException, InterruptedException {
		RestConnection conn = new RestConnection("administrator", "openolat");

		Identity identity = JunitTestHelper.createAndPersistIdentityAsRndUser("user-cert-5");
		URL certificateUrl = CertificatesManagerTest.class.getResource("template.pdf");
		Assert.assertNotNull(certificateUrl);
		File certificateFile = new File(certificateUrl.toURI());
		CertificateManagedFlag[] managedFlags = { CertificateManagedFlag.delete }; 
		Certificate certificate = certificatesManager
				.uploadStandaloneCertificate(identity, new Date(), "DM-234", managedFlags, "My floating course", -1l, null, null, certificateFile, identity);
		dbInstance.commitAndCloseSession();
		Assert.assertNotNull(certificate);
		
		URI uri = UriBuilder.fromUri(getContextURI()).path("users")
				.path(identity.getKey().toString())
				.path("certificates").path(certificate.getKey().toString()).build();
		HttpRequest method = conn.createDelete(uri, MediaType.APPLICATION_JSON);
		HttpResponse<InputStream> response = conn.execute(method);
		Assert.assertEquals(200, response.statusCode());
		
		Certificate deletedCertificate = certificatesManager.getCertificateById(certificate.getKey());
		Assert.assertNull(deletedCertificate);
		conn.close();

	}
	
	@Test
	public void deleteUserCourseCertificate() throws IOException, URISyntaxException, InterruptedException {
		RestConnection conn = new RestConnection("administrator", "openolat");

		Identity identity = JunitTestHelper.createAndPersistIdentityAsRndUser("user-cert-5");
		RepositoryEntry entry = JunitTestHelper.deployBasicCourse(identity);
		
		URL certificateUrl = CertificatesManagerTest.class.getResource("template.pdf");
		Assert.assertNotNull(certificateUrl);
		File certificateFile = new File(certificateUrl.toURI()); 
		Certificate certificate = certificatesManager.uploadCertificate(identity, new Date(), null, null,
				null, entry.getOlatResource(), null, certificateFile, JunitTestHelper.getDefaultActor());
		dbInstance.commitAndCloseSession();
		Assert.assertNotNull(certificate);
		
		URI uri = UriBuilder.fromUri(getContextURI()).path("users")
				.path(identity.getKey().toString())
				.path("certificates").path(certificate.getKey().toString()).build();
		HttpRequest method = conn.createDelete(uri, MediaType.APPLICATION_JSON);
		HttpResponse<InputStream> response = conn.execute(method);
		Assert.assertEquals(200, response.statusCode());
		
		Certificate deletedCertificate = certificatesManager.getCertificateById(certificate.getKey());
		Assert.assertNull(deletedCertificate);
		conn.close();

	}
	
	@Test
	public void updateCertificate() throws IOException, URISyntaxException, InterruptedException {
		Identity participant = JunitTestHelper.createAndPersistIdentityAsRndUser("prog-participant-4", Locale.ENGLISH);
		CertificationProgram program = certificationProgramService.createCertificationProgram("program-to-curriculum-5", "CP5", null);
		program.setRecertificationEnabled(true);
		program.setValidityEnabled(true);
		program.setValidityTimelapse(7);
		program.setValidityTimelapseUnit(DurationType.day);
		program.setRecertificationMode(RecertificationMode.automatic);
		program.setRecertificationWindowEnabled(true);
		program.setRecertificationWindow(7);
		program.setRecertificationWindowUnit(DurationType.day);
		certificationProgramService.updateCertificationProgram(program);
		dbInstance.commit();
		
		CertificateInfos certificateInfos = new CertificateInfos(participant, 2.0f, Float.valueOf(10), true,
				Double.valueOf(0.2), "", null);
		CertificateConfig config = CertificateConfig.builder()
				.withSendEmail(true)
				.withSendEmailBcc(false)
				.build();
		Certificate certificate = certificatesManager.generateCertificate(certificateInfos, program, null, config);
		dbInstance.commitAndCloseSession();
		//wait until the certificate is created
		waitMessageAreConsumed();
		
		RestConnection conn = new RestConnection("administrator", "openolat");
		
		URI uri = UriBuilder.fromUri(getContextURI()).path("users")
				.path(participant.getKey().toString())
				.path("certificates").path(certificate.getKey().toString()).build();
		HttpRequest method = conn.createGet(uri, MediaType.APPLICATION_JSON);
		HttpResponse<InputStream> response = conn.execute(method);
		Assert.assertEquals(200, response.statusCode());
		CertificateVO certificateVo = conn.parse(response, CertificateVO.class);
		
		Assert.assertNotNull(certificateVo);
		Assert.assertEquals(program.getKey(), certificateVo.getCertificationProgramKey());
		Assert.assertNotNull(certificateVo.getNextCertificationDate());
		Assert.assertNotNull(certificateVo.getRecertificationWindowDate());
		
		certificateVo.setExternalId("controlled-certificate");
		certificateVo.setNextCertificationDate(DateUtils.addDays(certificateVo.getNextCertificationDate(), -21));
		// Let it recalculate the time frame
		certificateVo.setRecertificationWindowDate(null);
		
		URI updateUri = UriBuilder.fromUri(getContextURI()).path("users")
				.path(participant.getKey().toString())
				.path("certificates").build();
		HttpRequest putMethod = conn.createPut(updateUri, certificateVo, MediaType.APPLICATION_JSON);
		HttpResponse<InputStream> putResponse = conn.execute(putMethod);
		Assert.assertEquals(200, putResponse.statusCode());
		CertificateVO updatedCertificateVo = conn.parse(putResponse, CertificateVO.class);
		
		Assert.assertNotNull(updatedCertificateVo);
		Assert.assertEquals(program.getKey(), updatedCertificateVo.getCertificationProgramKey());
		Assert.assertNotNull(updatedCertificateVo.getNextCertificationDate());
		Assert.assertNotNull(updatedCertificateVo.getRecertificationWindowDate());
		// Make sure the dates are pushed in the past
		Assert.assertTrue(updatedCertificateVo.getNextCertificationDate().before(new Date()));
		Assert.assertTrue(updatedCertificateVo.getRecertificationWindowDate().before(new Date()));
		conn.close();
	}
}
