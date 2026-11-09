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
import java.util.UUID;
import java.util.concurrent.Callable;

import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.UriBuilder;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.olat.core.commons.persistence.DB;
import org.olat.core.id.Identity;
import org.olat.core.util.httpclient.ConnectionUtilities.NameValuePair;
import org.olat.core.util.vfs.VFSLeaf;
import org.olat.course.certificate.Certificate;
import org.olat.course.certificate.CertificateStatus;
import org.olat.course.certificate.CertificatesManager;
import org.olat.course.certificate.manager.CertificatesManagerTest;
import org.olat.course.certificate.model.AbstractCertificate;
import org.olat.course.certificate.model.CertificateConfig;
import org.olat.course.certificate.model.CertificateInfos;
import org.olat.course.certificate.restapi.CertificateVO;
import org.olat.course.certificate.restapi.CertificateVOes;
import org.olat.repository.RepositoryEntry;
import org.olat.restapi.support.ObjectFactory;
import org.olat.test.JunitTestHelper;
import org.olat.test.OlatRestTestCase;
import org.olat.user.restapi.UserVO;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 8 janv. 2026<br>
 * @author srosse, stephane.rosse@frentix.com, https://www.frentix.com
 *
 */
public class RepositoryEntryCertificationTest extends OlatRestTestCase {

	@Autowired
	private DB dbInstance;
	@Autowired
	private CertificatesManager certificatesManager;
	
	private static RepositoryEntry defaultEntry;
	
	@Before
	public void defaultCourse() {
		if(defaultEntry == null) {
			Identity author = JunitTestHelper.getDefaultAuthor();
			defaultEntry = JunitTestHelper.deployBasicCourse(author);
		}
	}
	
	@Test
	public void getCertificateFile() throws IOException, URISyntaxException, InterruptedException {
		RestConnection conn = new RestConnection("administrator", "openolat");

		Identity assessedIdentity = JunitTestHelper.createAndPersistIdentityAsRndUser("cert-1");
		dbInstance.commitAndCloseSession();

		CertificateInfos certificateInfos = new CertificateInfos(assessedIdentity, 2.0f, Float.valueOf(10), true,
				Double.valueOf(0.2), "", null);
		CertificateConfig config = CertificateConfig.builder()
				.withSendEmail(true)
				.withSendEmailBcc(false)
				.build();
		Certificate certificate = certificatesManager.generateCertificate(certificateInfos, defaultEntry, null, config);
		dbInstance.commitAndCloseSession();
		Assert.assertNotNull(certificate);

		triggerAndWaitCertificate(certificate.getKey());
		
		URI uri = UriBuilder.fromUri(getContextURI()).path("repo").path("entries")
				.path(defaultEntry.getKey().toString())
				.path("certificates").path(assessedIdentity.getKey().toString()).build();
		HttpRequest method = conn.createGet(uri, "application/pdf");
		HttpResponse<InputStream> response = conn.execute(method);
		Assert.assertEquals(200, response.statusCode());

		RestConnection.consume(response);
		conn.close();

	}
	
	@Test
	public void getCertificateHead() throws IOException, URISyntaxException, InterruptedException {
		RestConnection conn = new RestConnection("administrator", "openolat");

		Identity assessedIdentity = JunitTestHelper.createAndPersistIdentityAsRndUser("cert-11");
		Identity unassessedIdentity = JunitTestHelper.createAndPersistIdentityAsRndUser("cert-12");
		dbInstance.commitAndCloseSession();

		CertificateInfos certificateInfos = new CertificateInfos(assessedIdentity, 2.0f, Float.valueOf(10), true,
				Double.valueOf(0.2), "", null);
		CertificateConfig config = CertificateConfig.builder()
				.withSendEmail(true)
				.build();
		Certificate certificate = certificatesManager.generateCertificate(certificateInfos, defaultEntry, null, config);
		dbInstance.commitAndCloseSession();
		Assert.assertNotNull(certificate);

		triggerAndWaitCertificate(certificate.getKey());
		
		URI uri = UriBuilder.fromUri(getContextURI()).path("repo").path("entries")
				.path(defaultEntry.getKey().toString())
				.path("certificates").path(assessedIdentity.getKey().toString()).build();
		HttpRequest method = conn.createHead(uri, "application/pdf");
		HttpResponse<InputStream> response = conn.execute(method);
		Assert.assertEquals(200, response.statusCode());
		RestConnection.consume(response);
		
		//check  with a stupid number
		URI nonExistentUri = UriBuilder.fromUri(getContextURI()).path("repo").path("entries")
				.path(defaultEntry.getKey().toString())
				.path("certificates").path(unassessedIdentity.getKey().toString()).build();
		HttpRequest nonExistentMethod = conn.createHead(nonExistentUri, "application/pdf");
		HttpResponse<InputStream> nonExistentResponse = conn.execute(nonExistentMethod);
		Assert.assertEquals(404, nonExistentResponse.statusCode());
		RestConnection.consume(nonExistentResponse);
		conn.close();

	}
	
	@Test
	public void generateCertificate() throws IOException, URISyntaxException, InterruptedException {
		RestConnection conn = new RestConnection("administrator", "openolat");

		Identity assessedIdentity = JunitTestHelper.createAndPersistIdentityAsRndUser("cert-1");
		Identity author = JunitTestHelper.createAndPersistIdentityAsAuthor("cert-2");
		RepositoryEntry entry = JunitTestHelper.deployBasicCourse(author);
		dbInstance.commitAndCloseSession();

		Date creationDate = createDate(2014, 9, 9);
		URI uri = UriBuilder.fromUri(getContextURI()).path("repo").path("entries")
				.path(entry.getKey().toString())
				.path("certificates").path(assessedIdentity.getKey().toString())
				.queryParam("score", "3.2")
				.queryParam("passed", "true")
				.queryParam("creationDate", ObjectFactory.formatDate(creationDate)).build();

		HttpRequest method = conn.createPut(uri, MediaType.APPLICATION_JSON);
		HttpResponse<InputStream> response = conn.execute(method);
		Assert.assertEquals(200, response.statusCode());
		CertificateVO certificateVo = conn.parse(response, CertificateVO.class);
		Assert.assertNotNull(certificateVo);

		triggerAndWaitCertificate(certificateVo.getKey());
		
		//check certificate
		Certificate certificate = certificatesManager.getLastCertificate(assessedIdentity, entry.getOlatResource().getKey());
		Assert.assertNotNull(certificate);
		Assert.assertEquals(creationDate, certificate.getCreationDate());
		//check the certificate file
		VFSLeaf certificateLeaf = certificatesManager.getCertificateLeaf(certificate);
		Assert.assertNotNull(certificateLeaf);
		Assert.assertTrue(certificateLeaf.getSize() > 500);
		conn.close();
	}
	
	@Test
	public void getCertificateByExternalId() throws IOException, URISyntaxException, InterruptedException {
		RestConnection conn = new RestConnection("administrator", "openolat");

		Identity assessedIdentity = JunitTestHelper.createAndPersistIdentityAsRndUser("cert-1");
		String externalId = UUID.randomUUID().toString();
		
		URL certificateUrl = CertificatesManagerTest.class.getResource("template.pdf");
		Assert.assertNotNull(certificateUrl);
		File certificateFile = new File(certificateUrl.toURI()); 
		Certificate certificate = certificatesManager.uploadCertificate(assessedIdentity, new Date(), externalId, null,
				null, defaultEntry.getOlatResource(), null, certificateFile, JunitTestHelper.getDefaultAdministrator());
		dbInstance.commitAndCloseSession();

		URI uri = UriBuilder.fromUri(getContextURI()).path("repo").path("entries")
				.path(defaultEntry.getKey().toString())
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
		Assert.assertEquals(certificate.getKey(), certificateVo.getKey());
		

		UserVO user = certificateVo.getUser();
		Assert.assertNotNull(user);
		Assert.assertEquals(assessedIdentity.getUser().getFirstName(), user.getFirstName());
		Assert.assertEquals(assessedIdentity.getUser().getLastName(), user.getLastName());
		conn.close();

	}

	@Test
	public void uploadCertificate() throws IOException, URISyntaxException, InterruptedException {
		RestConnection conn = new RestConnection("administrator", "openolat");

		Identity assessedIdentity = JunitTestHelper.createAndPersistIdentityAsRndUser("cert-1");
		dbInstance.commitAndCloseSession();

		URI uri = UriBuilder.fromUri(getContextURI()).path("repo").path("entries")
				.path(defaultEntry.getKey().toString())
				.path("certificates").path(assessedIdentity.getKey().toString()).build();

		URL certificateUrl = RepositoryEntryCertificationTest.class.getResource("certificate.pdf");
		Assert.assertNotNull(certificateUrl);
		File certificateFile = new File(certificateUrl.toURI());
		Date creationDate = createDate(2014, 7, 1);
		Date nextCertificationDate = createDate(2036, 7, 1);

		List<NameValuePair> formParameters = List.of(
				new NameValuePair("externalId", "CWS-4543231"),
				new NameValuePair("managedFlags", "delete"),
				new NameValuePair("creationDate", ObjectFactory.formatDate(creationDate)),
				new NameValuePair("nextRecertificationDate", ObjectFactory.formatDate(nextCertificationDate)));
		HttpRequest method = conn.createPost(uri, certificateFile, certificateFile.getName(), formParameters, MediaType.APPLICATION_JSON);
		HttpResponse<InputStream> response = conn.execute(method);
		Assert.assertEquals(200, response.statusCode());
		RestConnection.consume(response);

		//check certificate
		Certificate certificate = certificatesManager.getLastCertificate(assessedIdentity, defaultEntry.getOlatResource().getKey());
		Assert.assertNotNull(certificate);
		Assert.assertEquals(creationDate, certificate.getCreationDate());
		Assert.assertEquals(nextCertificationDate, certificate.getNextRecertificationDate());
		Assert.assertEquals("CWS-4543231", certificate.getExternalId());
		Assert.assertEquals(1, certificate.getManagedFlags().length);
		
		//check the certificate file
		VFSLeaf certificateLeaf = certificatesManager.getCertificateLeaf(certificate);
		Assert.assertNotNull(certificateLeaf);
		Assert.assertEquals(certificateFile.length(), certificateLeaf.getSize());
		conn.close();
	}
	
	@Test
	public void deleteCertificate() throws IOException, URISyntaxException, InterruptedException {
		RestConnection conn = new RestConnection("administrator", "openolat");

		Identity assessedIdentity = JunitTestHelper.createAndPersistIdentityAsRndUser("cert-15");
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
		triggerAndWaitCertificate(certificate.getKey());
		
		// check that there is a real certificate with its file
		Certificate reloadedCertificate = certificatesManager.getCertificateById(certificate.getKey());
		Certificate lastCertificate = certificatesManager.getLastCertificate(assessedIdentity, entry.getOlatResource().getKey());
		Assert.assertEquals(reloadedCertificate, lastCertificate);
		VFSLeaf certificateFile = certificatesManager.getCertificateLeaf(reloadedCertificate);
		Assert.assertNotNull(certificateFile);
		Assert.assertTrue(certificateFile.exists());
		
		//delete the certificate
		URI uri = UriBuilder.fromUri(getContextURI()).path("repo").path("entries")
				.path(entry.getKey().toString())
				.path("certificates").path(assessedIdentity.getKey().toString()).build();
		HttpRequest method = conn.createDelete(uri, MediaType.APPLICATION_JSON);
		HttpResponse<InputStream> response = conn.execute(method);
		Assert.assertEquals(200, response.statusCode());
		RestConnection.consume(response);

		
		//check that the file and the database record are deleted
		VFSLeaf deletedFile = certificatesManager.getCertificateLeaf(reloadedCertificate);
		Assert.assertNull(deletedFile);
		Certificate deletedCertificate = certificatesManager.getCertificateById(certificate.getKey());
		Assert.assertNull(deletedCertificate);
		conn.close();
	}
	
	private Date createDate(int year, int month, int day) {
		Calendar cal = Calendar.getInstance();
		cal.set(Calendar.YEAR, year);
		cal.set(Calendar.MONTH, month);
		cal.set(Calendar.DATE, day);
		cal.set(Calendar.HOUR_OF_DAY, 0);
		cal.set(Calendar.MINUTE, 0);
		cal.set(Calendar.SECOND, 0);
		cal.set(Calendar.MILLISECOND, 0);
		return cal.getTime();
	}
	
	/**
	 * Wait that the certificate is generated, email sent, and flag last set.
	 * 
	 * @param certificateKey The primary key of the certificate
	 */
	private void triggerAndWaitCertificate(Long certificateKey) {
		certificatesManager.triggerGenerationJob();
		//wait until the certificate is created
		waitForCondition(new Callable<Boolean>() {
			@Override
			public Boolean call() throws Exception {
				Certificate reloadedCertificate = certificatesManager.getCertificateById(certificateKey);
				return CertificateStatus.ok.equals(reloadedCertificate.getStatus())
						&& ((AbstractCertificate)reloadedCertificate).isLast();
			}
		}, 30000);
	}
}
