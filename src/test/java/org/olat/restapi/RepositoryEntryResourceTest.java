package org.olat.restapi;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Locale;

import javax.ws.rs.core.UriBuilder;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.util.EntityUtils;
import org.junit.Assert;
import org.junit.Test;
import org.olat.basesecurity.OrganisationService;
import org.olat.core.commons.persistence.DB;
import org.olat.core.id.Identity;
import org.olat.core.id.Organisation;
import org.olat.fileresource.types.ImsQTI21Resource;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.handlers.RepositoryHandler;
import org.olat.repository.handlers.RepositoryHandlerFactory;
import org.olat.test.JunitTestHelper;
import org.olat.test.OlatJerseyTestCase;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 2 mai 2018<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class RepositoryEntryResourceTest extends OlatJerseyTestCase {
	
	@Autowired
	private DB dbInstance;
	@Autowired
	private OrganisationService organisationService;
	
	@Test
	public void exportCourse()
	throws IOException, URISyntaxException {
		RestConnection conn = new RestConnection();
		Assert.assertTrue(conn.login("administrator", "openolat"));
		Identity author = JunitTestHelper.createAndPersistIdentityAsRndUser("course-owner");
		RepositoryEntry course = JunitTestHelper.deployBasicCourse(author);
		dbInstance.closeSession();
		
		URI request = UriBuilder.fromUri(getContextURI()).path("repo").path("entries")
				.path(course.getKey().toString()).path("file").build();
		HttpGet method = conn.createGet(request, "application/zip", true);
		HttpResponse response = conn.execute(method);

		Assert.assertEquals(200, response.getStatusLine().getStatusCode());
		byte[] exportedFile = EntityUtils.toByteArray(response.getEntity());
		Assert.assertTrue(exportedFile.length > 1000);	
	}
	
	@Test
	public void exportQTI21Test()
	throws IOException, URISyntaxException {
		//deploy QTI 2.1 test
		Identity author = JunitTestHelper.createAndPersistIdentityAsRndUser("test-owner");
		URL testUrl = JunitTestHelper.class.getResource("file_resources/qti21/simple_QTI_21_hotspot.zip");
		File testFile = new File(testUrl.toURI());		
		RepositoryHandler courseHandler = RepositoryHandlerFactory.getInstance()
						.getRepositoryHandler(ImsQTI21Resource.TYPE_NAME);
		Organisation defOrganisation = organisationService.getDefaultOrganisation();
		RepositoryEntry testEntry = courseHandler.importResource(author, null, "Test QTI 2.1", "", true, defOrganisation, Locale.ENGLISH, testFile, null);
		dbInstance.closeSession();
		
		RestConnection conn = new RestConnection();
		Assert.assertTrue(conn.login("administrator", "openolat"));
		
		URI request = UriBuilder.fromUri(getContextURI()).path("repo").path("entries")
				.path(testEntry.getKey().toString()).path("file").build();
		HttpGet method = conn.createGet(request, "application/zip", true);
		HttpResponse response = conn.execute(method);

		Assert.assertEquals(200, response.getStatusLine().getStatusCode());
		byte[] exportedFile = EntityUtils.toByteArray(response.getEntity());
		Assert.assertTrue(exportedFile.length > 1000);
	}
}
