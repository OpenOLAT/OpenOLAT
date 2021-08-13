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
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.List;
import java.util.concurrent.Callable;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriBuilder;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpHead;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.util.EntityUtils;
import org.junit.Assert;
import org.junit.Test;
import org.olat.core.commons.persistence.DB;
import org.olat.core.id.Identity;
import org.olat.core.util.vfs.VFSLeaf;
import org.olat.course.certificate.Certificate;
import org.olat.course.certificate.CertificateLight;
import org.olat.course.certificate.CertificateStatus;
import org.olat.course.certificate.CertificatesManager;
import org.olat.course.certificate.model.CertificateConfig;
import org.olat.course.certificate.model.CertificateInfos;
import org.olat.course.certificate.restapi.CertificateVO;
import org.olat.course.certificate.restapi.CertificateVOes;
import org.olat.repository.RepositoryEntry;
import org.olat.test.JunitTestHelper;
import org.olat.test.OlatRestTestCase;
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
	
	@Test
	public void getUserCertificatesInformations() throws IOException, URISyntaxException {
		RestConnection conn = new RestConnection();
		Assert.assertTrue(conn.login("administrator", "openolat"));

		Identity assessedIdentity = JunitTestHelper.createAndPersistIdentityAsRndUser("user-cert-1");
		Identity author = JunitTestHelper.createAndPersistIdentityAsAuthor("auth-cert-2");
		RepositoryEntry entry = JunitTestHelper.deployBasicCourse(author);
		dbInstance.commitAndCloseSession();

		CertificateInfos certificateInfos = new CertificateInfos(assessedIdentity, 2.0f, Float.valueOf(10), true,
				Double.valueOf(0.2));
		CertificateConfig config = CertificateConfig.builder().withSendEmailBcc(false).build();
		Certificate certificate = certificatesManager.generateCertificate(certificateInfos, entry, null, config);
		dbInstance.commitAndCloseSession();
		Assert.assertNotNull(certificate);
		sleep(1000);
		
		//wait until the certificate is created
		waitCertificate(certificate.getKey());
		
		URI uri = UriBuilder.fromUri(getContextURI()).path("users")
				.path(assessedIdentity.getKey().toString())
				.path("certificates").build();
		HttpGet method = conn.createGet(uri, MediaType.APPLICATION_JSON, true);
		HttpResponse response = conn.execute(method);
		Assert.assertEquals(200, response.getStatusLine().getStatusCode());
		
		CertificateVOes certificateVoes = conn.parse(response, CertificateVOes.class);
		Assert.assertNotNull(certificateVoes);
		Assert.assertNotNull(certificateVoes.getCertificates());
		Assert.assertEquals(1, certificateVoes.getCertificates().size());
		
		CertificateVO certificateVo = certificateVoes.getCertificates().get(0);
		Assert.assertNotNull(certificateVo);
		Assert.assertEquals(certificate.getKey(), certificateVo.getKey());
		Assert.assertEquals(assessedIdentity.getKey(), certificateVo.getIdentityKey());
		Assert.assertEquals(certificate.getUuid(), certificateVo.getUuid());

		conn.shutdown();
	}
	
	@Test
	public void getUserCertificate() throws IOException, URISyntaxException {
		RestConnection conn = new RestConnection();
		Assert.assertTrue(conn.login("administrator", "openolat"));

		Identity assessedIdentity = JunitTestHelper.createAndPersistIdentityAsRndUser("user-cert-2");
		Identity author = JunitTestHelper.createAndPersistIdentityAsAuthor("auth-cert-3");
		RepositoryEntry entry = JunitTestHelper.deployBasicCourse(author);
		dbInstance.commitAndCloseSession();

		CertificateInfos certificateInfos = new CertificateInfos(assessedIdentity, 2.0f, Float.valueOf(10), true,
				Double.valueOf(0.2));
		CertificateConfig config = CertificateConfig.builder().withSendEmailBcc(false).build();
		Certificate certificate = certificatesManager.generateCertificate(certificateInfos, entry, null, config);
		dbInstance.commitAndCloseSession();
		Assert.assertNotNull(certificate);
		sleep(1000);
		
		//wait until the certificate is created
		waitCertificate(certificate.getKey());
		
		URI uri = UriBuilder.fromUri(getContextURI()).path("users")
				.path(assessedIdentity.getKey().toString())
				.path("certificates").path(certificate.getKey().toString()).build();
		HttpHead method = conn.createHead(uri, "application/pdf", true);
		HttpResponse response = conn.execute(method);
		Assert.assertEquals(200, response.getStatusLine().getStatusCode());
		EntityUtils.consume(response.getEntity());
		
		//check  with a stupid number
		HttpGet getMethod = conn.createGet(uri, "application/pdf", true);
		HttpResponse getResponse = conn.execute(getMethod);
		Assert.assertEquals(200, getResponse.getStatusLine().getStatusCode());
		EntityUtils.consume(getResponse.getEntity());

		conn.shutdown();
	}
	
	
	@Test
	public void uploadCertificateStandalone() throws IOException, URISyntaxException {
		RestConnection conn = new RestConnection();
		Assert.assertTrue(conn.login("administrator", "openolat"));

		Identity assessedIdentity = JunitTestHelper.createAndPersistIdentityAsRndUser("cert-4");
		dbInstance.commitAndCloseSession();

		URI uri = UriBuilder.fromUri(getContextURI()).path("users")
				.path(assessedIdentity.getKey().toString())
				.path("certificates").build();

		URL certificateUrl = CertificationTest.class.getResource("certificate.pdf");
		Assert.assertNotNull(certificateUrl);
		File certificateFile = new File(certificateUrl.toURI());
		HttpPost method = conn.createPost(uri, MediaType.APPLICATION_JSON);
		
		MultipartEntityBuilder builder = MultipartEntityBuilder.create()
				.setMode(HttpMultipartMode.BROWSER_COMPATIBLE)
				.addTextBody("filename", certificateFile.getName())
				.addTextBody("archivedResourceKey", "726348723")
				.addBinaryBody("file", certificateFile, ContentType.APPLICATION_OCTET_STREAM, certificateFile.getName());
		method.setEntity(builder.build());

		HttpResponse response = conn.execute(method);
		Assert.assertEquals(200, response.getStatusLine().getStatusCode());
		EntityUtils.consume(response.getEntity());

		//check certificate
		List<CertificateLight> certificates = certificatesManager.getLastCertificates(assessedIdentity);
		Assert.assertNotNull(certificates);
		//check the certificate file
		Certificate certificate = certificatesManager.getCertificateById(certificates.get(0).getKey());
		VFSLeaf certificateLeaf = certificatesManager.getCertificateLeaf(certificate);
		Assert.assertNotNull(certificateLeaf);
		Assert.assertEquals(certificateFile.length(), certificateLeaf.getSize());
	}
	
	
	private void waitCertificate(Long certificateKey) {
		//wait until the certificate is created
		waitForCondition(new Callable<Boolean>() {
			@Override
			public Boolean call() throws Exception {
				Certificate reloadedCertificate = certificatesManager.getCertificateById(certificateKey);
				return CertificateStatus.ok.equals(reloadedCertificate.getStatus());
			}
		}, 30000);
	}

}
