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

import static org.assertj.core.api.Assertions.assertThat;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Date;
import java.util.List;

import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.UriBuilder;

import org.apache.commons.lang3.time.DateUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.util.EntityUtils;
import org.assertj.core.api.Assertions;
import org.junit.Assert;
import org.junit.Test;
import org.olat.basesecurity.OrganisationService;
import org.olat.core.commons.persistence.DB;
import org.olat.core.id.Identity;
import org.olat.core.id.Organisation;
import org.olat.course.certificate.Certificate;
import org.olat.course.certificate.CertificatesManager;
import org.olat.course.certificate.model.CertificateConfig;
import org.olat.course.certificate.model.CertificateInfos;
import org.olat.modules.certificationprogram.CertificationProgram;
import org.olat.modules.certificationprogram.CertificationProgramService;
import org.olat.modules.certificationprogram.restapi.CertificationProgramMemberVO;
import org.olat.modules.certificationprogram.restapi.CertificationProgramMemberVOes;
import org.olat.modules.certificationprogram.restapi.CertificationProgramVO;
import org.olat.modules.certificationprogram.restapi.CertificationProgramVOes;
import org.olat.restapi.support.ObjectFactory;
import org.olat.test.JunitTestHelper;
import org.olat.test.JunitTestHelper.IdentityWithLogin;
import org.olat.test.OlatRestTestCase;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 20 mai 2026<br>
 * @author srosse, stephane.rosse@frentix.com, https://www.frentix.com
 *
 */
public class CertificationProgramsWebServiceTest extends OlatRestTestCase {

	@Autowired
	private DB dbInstance;
	@Autowired
	private CertificatesManager certificatesManager;
	@Autowired
	private OrganisationService organisationService;
	@Autowired
	private CertificationProgramService certificationProgramService;

	@Test
	public void getPrograms() throws IOException, URISyntaxException {
		RestConnection conn = new RestConnection("administrator", "openolat");

		Organisation defaultOrganisation = organisationService.getDefaultOrganisation();
		CertificationProgram program = certificationProgramService.createCertificationProgram(
				"rest-prog-list-1", "REST List Program 1", null);
		certificationProgramService.updateCertificationProgram(program, List.of(defaultOrganisation));
		dbInstance.commitAndCloseSession();

		URI uri = UriBuilder.fromUri(getContextURI()).path("certificationprograms").build();
		HttpGet method = conn.createGet(uri, MediaType.APPLICATION_JSON, true);
		HttpResponse response = conn.execute(method);
		Assert.assertEquals(200, response.getStatusLine().getStatusCode());

		CertificationProgramVOes voEs = conn.parse(response, CertificationProgramVOes.class);
		Assert.assertNotNull(voEs);
		Assert.assertNotNull(voEs.getPrograms());

		List<Long> keys = voEs.getPrograms().stream()
				.map(CertificationProgramVO::getKey)
				.toList();
		assertThat(keys).contains(program.getKey());

		conn.shutdown();
	}

	@Test
	public void getProgramDetails() throws IOException, URISyntaxException {
		RestConnection conn = new RestConnection("administrator", "openolat");

		Organisation defaultOrganisation = organisationService.getDefaultOrganisation();
		CertificationProgram program = certificationProgramService.createCertificationProgram(
				"rest-prog-detail-1", "REST Detail Program", null);
		certificationProgramService.updateCertificationProgram(program, List.of(defaultOrganisation));
		dbInstance.commitAndCloseSession();

		URI uri = UriBuilder.fromUri(getContextURI())
				.path("certificationprograms")
				.path(program.getKey().toString())
				.build();
		HttpGet method = conn.createGet(uri, MediaType.APPLICATION_JSON, true);
		HttpResponse response = conn.execute(method);
		Assert.assertEquals(200, response.getStatusLine().getStatusCode());

		CertificationProgramVO vo = conn.parse(response, CertificationProgramVO.class);
		Assert.assertNotNull(vo);
		Assert.assertEquals(program.getKey(), vo.getKey());
		Assert.assertEquals("REST Detail Program", vo.getDisplayName());
		Assert.assertEquals("rest-prog-detail-1", vo.getIdentifier());

		conn.shutdown();
	}

	@Test
	public void getProgramNotFound() throws IOException, URISyntaxException {
		RestConnection conn = new RestConnection("administrator", "openolat");

		URI uri = UriBuilder.fromUri(getContextURI())
				.path("certificationprograms")
				.path("99999999999")
				.build();
		HttpGet method = conn.createGet(uri, MediaType.APPLICATION_JSON, true);
		HttpResponse response = conn.execute(method);
		Assert.assertEquals(404, response.getStatusLine().getStatusCode());
		EntityUtils.consume(response.getEntity());

		conn.shutdown();
	}

	/**
	 * List of all members of a certification program. Generate a certificate by upload.
	 * @throws IOException
	 * @throws URISyntaxException
	 */
	@Test
	public void getMembers() throws IOException, URISyntaxException {
		RestConnection conn = new RestConnection("administrator", "openolat");

		Organisation defaultOrganisation = organisationService.getDefaultOrganisation();
		Identity participant = JunitTestHelper.createAndPersistIdentityAsRndUser("cert-prog-member-1");
		CertificationProgram program = certificationProgramService.createCertificationProgram(
				"rest-prog-members-1", "REST Members Program", null);
		certificationProgramService.updateCertificationProgram(program, List.of(defaultOrganisation));
		dbInstance.commitAndCloseSession();

		URL certificateUrl = CertificationProgramsWebServiceTest.class.getResource("certificate.pdf");
		Assert.assertNotNull(certificateUrl);
		File certificateFile = new File(certificateUrl.toURI());

		Date issuedDate = new Date();
		Date nextRecertDate = DateUtils.addDays(issuedDate, 365);
		Certificate certificate = certificatesManager.uploadCertificate(participant, issuedDate,
				"EXT-REST-1", null, program, program.getResource(), nextRecertDate, certificateFile, JunitTestHelper.getDefaultActor());
		dbInstance.commitAndCloseSession();
		Assert.assertNotNull(certificate);

		URI uri = UriBuilder.fromUri(getContextURI())
				.path("certificationprograms")
				.path(program.getKey().toString())
				.path("certificates")
				.build();
		HttpGet method = conn.createGet(uri, MediaType.APPLICATION_JSON, true);
		HttpResponse response = conn.execute(method);
		Assert.assertEquals(200, response.getStatusLine().getStatusCode());

		CertificationProgramMemberVOes voEs = conn.parse(response, CertificationProgramMemberVOes.class);
		Assert.assertNotNull(voEs);
		Assertions.assertThat(voEs.getMembers())
			.hasSize(1)
			.map(CertificationProgramMemberVO::getCertificateKey)
			.containsExactly(certificate.getKey());

		conn.shutdown();
	}

	/**
	 * Get certificate informations of a specific user. Generate the certificate.
	 * 
	 * @throws IOException
	 * @throws URISyntaxException
	 */
	@Test
	public void getMembersByStatus() throws IOException, URISyntaxException {
		RestConnection conn = new RestConnection("administrator", "openolat");

		Organisation defaultOrganisation = organisationService.getDefaultOrganisation();
		Identity participant = JunitTestHelper.createAndPersistIdentityAsRndUser("cert-prog-status-1");
		CertificationProgram program = certificationProgramService.createCertificationProgram(
				"rest-prog-status-1", "REST Status Program", null);
		certificationProgramService.updateCertificationProgram(program, List.of(defaultOrganisation));
		dbInstance.commitAndCloseSession();

		CertificateInfos certificateInfos = new CertificateInfos(participant, 2.0f, Float.valueOf(10), true,
				Double.valueOf(0.2), "", null);
		CertificateConfig config = CertificateConfig.builder().withSendEmailBcc(false).build();
		
		Certificate activeCertificate = certificatesManager.generateCertificate(certificateInfos, program, null, config);
		dbInstance.commitAndCloseSession();
		Assert.assertNotNull(activeCertificate);
		dbInstance.commitAndCloseSession();
		//wait until the certificate is created
		waitMessageAreConsumed();

		// Filter by active status
		URI uri = UriBuilder.fromUri(getContextURI())
				.path("certificationprograms")
				.path(program.getKey().toString())
				.path("certificates")
				.queryParam("status", "active")
				.build();
		HttpGet method = conn.createGet(uri, MediaType.APPLICATION_JSON, true);
		HttpResponse response = conn.execute(method);
		Assert.assertEquals(200, response.getStatusLine().getStatusCode());

		CertificationProgramMemberVOes voEs = conn.parse(response, CertificationProgramMemberVOes.class);
		Assert.assertNotNull(voEs);
		Assert.assertNotNull(voEs.getMembers());
		Assertions.assertThat(voEs.getMembers())
			.hasSize(1)
			.allMatch(m -> "active".equals(m.getStatus()));

		conn.shutdown();
	}

	@Test
	public void getMemberByIdentity() throws IOException, URISyntaxException {
		RestConnection conn = new RestConnection("administrator", "openolat");

		Organisation defaultOrganisation = organisationService.getDefaultOrganisation();
		Identity participant = JunitTestHelper.createAndPersistIdentityAsRndUser("cert-prog-ident-1");
		CertificationProgram program = certificationProgramService.createCertificationProgram(
				"rest-prog-ident-1", "REST Identity Program", null);
		certificationProgramService.updateCertificationProgram(program, List.of(defaultOrganisation));
		dbInstance.commitAndCloseSession();

		URL certificateUrl = CertificationProgramsWebServiceTest.class.getResource("certificate.pdf");
		Assert.assertNotNull(certificateUrl);
		File certificateFile = new File(certificateUrl.toURI());

		Date issuedDate = new Date();
		Certificate certificate = certificatesManager.uploadCertificate(participant, issuedDate,
				"EXT-IDENT-1", null, program, program.getResource(), null, certificateFile, JunitTestHelper.getDefaultActor());
		dbInstance.commitAndCloseSession();
		Assert.assertNotNull(certificate);

		URI uri = UriBuilder.fromUri(getContextURI())
				.path("certificationprograms")
				.path(program.getKey().toString())
				.path("certificates")
				.path(participant.getKey().toString())
				.build();
		HttpGet method = conn.createGet(uri, MediaType.APPLICATION_JSON, true);
		HttpResponse response = conn.execute(method);
		Assert.assertEquals(200, response.getStatusLine().getStatusCode());

		CertificationProgramMemberVO vo = conn.parse(response, CertificationProgramMemberVO.class);
		Assert.assertNotNull(vo);
		Assert.assertEquals(participant.getKey(), vo.getIdentityKey());
		Assert.assertEquals(certificate.getKey(), vo.getCertificateKey());
		Assert.assertEquals("EXT-IDENT-1", vo.getExternalId());

		conn.shutdown();
	}

	@Test
	public void importCertificate() throws IOException, URISyntaxException {
		RestConnection conn = new RestConnection("administrator", "openolat");

		Organisation defaultOrganisation = organisationService.getDefaultOrganisation();
		Identity participant = JunitTestHelper.createAndPersistIdentityAsRndUser("cert-prog-import-1");
		CertificationProgram program = certificationProgramService.createCertificationProgram(
				"rest-prog-import-1", "REST Import Program", null);
		certificationProgramService.updateCertificationProgram(program, List.of(defaultOrganisation));
		dbInstance.commitAndCloseSession();

		URI uri = UriBuilder.fromUri(getContextURI())
				.path("certificationprograms")
				.path(program.getKey().toString())
				.path("certificates")
				.build();

		URL certificateUrl = CertificationProgramsWebServiceTest.class.getResource("certificate.pdf");
		Assert.assertNotNull(certificateUrl);
		File certificateFile = new File(certificateUrl.toURI());

		Date issuedDate = new Date();
		Date nextRecertDate = DateUtils.addDays(issuedDate, 180);

		HttpPost method = conn.createPost(uri, MediaType.APPLICATION_JSON);
		MultipartEntityBuilder builder = MultipartEntityBuilder.create()
				.setMode(HttpMultipartMode.BROWSER_COMPATIBLE)
				.addTextBody("identityKey", participant.getKey().toString())
				.addTextBody("issuedDate", ObjectFactory.formatDate(issuedDate))
				.addTextBody("nextRecertificationDate", ObjectFactory.formatDate(nextRecertDate))
				.addTextBody("externalId", "REST-IMPORT-001")
				.addBinaryBody("file", certificateFile, ContentType.APPLICATION_OCTET_STREAM, certificateFile.getName());
		method.setEntity(builder.build());

		HttpResponse response = conn.execute(method);
		Assert.assertEquals(200, response.getStatusLine().getStatusCode());

		CertificationProgramMemberVO vo = conn.parse(response, CertificationProgramMemberVO.class);
		Assert.assertNotNull(vo);
		Assert.assertEquals(participant.getKey(), vo.getIdentityKey());
		Assert.assertNotNull(vo.getCertificateKey());
		Assert.assertEquals("REST-IMPORT-001", vo.getExternalId());
		

		conn.shutdown();
	}

	@Test
	public void deleteCertificate() throws IOException, URISyntaxException {
		RestConnection conn = new RestConnection("administrator", "openolat");

		Organisation defaultOrganisation = organisationService.getDefaultOrganisation();
		Identity participant = JunitTestHelper.createAndPersistIdentityAsRndUser("cert-prog-delete-1");
		CertificationProgram program = certificationProgramService.createCertificationProgram(
				"rest-prog-delete-1", "REST Delete Program", null);
		certificationProgramService.updateCertificationProgram(program, List.of(defaultOrganisation));
		dbInstance.commitAndCloseSession();

		URL certificateUrl = CertificationProgramsWebServiceTest.class.getResource("certificate.pdf");
		Assert.assertNotNull(certificateUrl);
		File certificateFile = new File(certificateUrl.toURI());

		Certificate certificate = certificatesManager.uploadCertificate(participant, new Date(),
				null, null, program, program.getResource(), null, certificateFile, JunitTestHelper.getDefaultAdministrator());
		dbInstance.commitAndCloseSession();
		Assert.assertNotNull(certificate);

		URI uri = UriBuilder.fromUri(getContextURI())
				.path("certificationprograms")
				.path(program.getKey().toString())
				.path("certificates")
				.path(certificate.getKey().toString())
				.build();
		HttpDelete deleteMethod = conn.createDelete(uri, MediaType.APPLICATION_JSON);
		HttpResponse response = conn.execute(deleteMethod);
		Assert.assertEquals(200, response.getStatusLine().getStatusCode());
		EntityUtils.consume(response.getEntity());

		Certificate deleted = certificatesManager.getCertificateById(certificate.getKey());
		Assert.assertNull(deleted);

		conn.shutdown();
	}

	@Test
	public void deleteCertificateWrongProgram() throws IOException, URISyntaxException {
		RestConnection conn = new RestConnection("administrator", "openolat");

		Organisation defaultOrganisation = organisationService.getDefaultOrganisation();
		Identity participant = JunitTestHelper.createAndPersistIdentityAsRndUser("cert-prog-wrongdel-1");
		CertificationProgram program1 = certificationProgramService.createCertificationProgram(
				"rest-prog-wrongdel-1", "REST WrongDel Program 1", null);
		CertificationProgram program2 = certificationProgramService.createCertificationProgram(
				"rest-prog-wrongdel-2", "REST WrongDel Program 2", null);
		certificationProgramService.updateCertificationProgram(program1, List.of(defaultOrganisation));
		certificationProgramService.updateCertificationProgram(program2, List.of(defaultOrganisation));
		dbInstance.commitAndCloseSession();

		URL certificateUrl = CertificationProgramsWebServiceTest.class.getResource("certificate.pdf");
		Assert.assertNotNull(certificateUrl);
		File certificateFile = new File(certificateUrl.toURI());

		// Certificate belongs to program1
		Certificate certificate = certificatesManager.uploadCertificate(participant, new Date(),
				null, null, program1, program1.getResource(), null, certificateFile, JunitTestHelper.getDefaultAdministrator());
		dbInstance.commitAndCloseSession();
		Assert.assertNotNull(certificate);

		// Try to delete it via program2 — must return 409
		URI uri = UriBuilder.fromUri(getContextURI())
				.path("certificationprograms")
				.path(program2.getKey().toString())
				.path("certificates")
				.path(certificate.getKey().toString())
				.build();
		HttpDelete deleteMethod = conn.createDelete(uri, MediaType.APPLICATION_JSON);
		HttpResponse response = conn.execute(deleteMethod);
		Assert.assertEquals(409, response.getStatusLine().getStatusCode());
		EntityUtils.consume(response.getEntity());

		conn.shutdown();
	}

	@Test
	public void getProgramForbiddenForUnauthorizedUser() throws IOException, URISyntaxException {
		IdentityWithLogin unauthorizedUser = JunitTestHelper.createAndPersistRndUser("cert-prog-unauth-1");
		dbInstance.commitAndCloseSession();

		RestConnection conn = new RestConnection(unauthorizedUser);

		Organisation defaultOrganisation = organisationService.getDefaultOrganisation();
		CertificationProgram program = certificationProgramService.createCertificationProgram(
				"rest-prog-unauth-1", "REST Unauth Program", null);
		certificationProgramService.updateCertificationProgram(program, List.of(defaultOrganisation));
		dbInstance.commitAndCloseSession();

		URI uri = UriBuilder.fromUri(getContextURI())
				.path("certificationprograms")
				.path(program.getKey().toString())
				.build();
		HttpGet method = conn.createGet(uri, MediaType.APPLICATION_JSON, true);
		HttpResponse response = conn.execute(method);
		Assert.assertEquals(403, response.getStatusLine().getStatusCode());
		EntityUtils.consume(response.getEntity());

		conn.shutdown();
	}
}
