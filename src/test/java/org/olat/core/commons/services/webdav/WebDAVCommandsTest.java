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
package org.olat.core.commons.services.webdav;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.UUID;

import javax.ws.rs.core.UriBuilder;

import junit.framework.Assert;

import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpOptions;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.InputStreamEntity;
import org.apache.http.entity.StringEntity;
import org.apache.http.util.EntityUtils;
import org.apache.poi.util.IOUtils;
import org.junit.Test;
import org.olat.basesecurity.BaseSecurity;
import org.olat.core.commons.persistence.DB;
import org.olat.core.id.Identity;
import org.olat.course.CourseFactory;
import org.olat.repository.RepositoryEntry;
import org.olat.restapi.CoursePublishTest;
import org.olat.test.JunitTestHelper;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Test the commands against the WedDAV implementation of OpenOLAT
 * 
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class WebDAVCommandsTest extends WebDAVTestCase {
	
	@Autowired
	private DB dbInstance;
	@Autowired
	private BaseSecurity securityManager;
	
	/**
	 * Check the DAV, Ms-Author and Allow header
	 * @throws IOException
	 * @throws URISyntaxException
	 */
	@Test
	public void testOptions()
	throws IOException, URISyntaxException {
		//create a user
		Identity user = JunitTestHelper.createAndPersistIdentityAsUser("webdav-1-" + UUID.randomUUID().toString());
		
		//list root content of its webdav folder
		WebDAVConnection conn = new WebDAVConnection();
		conn.setCredentials(user.getName(), "A6B7C8");
		
		URI baseUri = conn.getBaseURI().build();
		HttpOptions optionsRoot = conn.createOptions(baseUri);
		HttpResponse optionsResponse = conn.execute(optionsRoot);
		Assert.assertEquals(200, optionsResponse.getStatusLine().getStatusCode());
		//check DAV header
		Header davHeader = optionsResponse.getFirstHeader("DAV");
		String davHeaderValue = davHeader.getValue();
		Assert.assertTrue(davHeaderValue.contains("1"));
		Assert.assertTrue(davHeaderValue.contains("2"));
		//check ms author
		Header msHeader = optionsResponse.getFirstHeader("MS-Author-Via");
		Assert.assertEquals("DAV", msHeader.getValue());
		//check methods
		Header allowHeader = optionsResponse.getFirstHeader("Allow");
		String allowValue = allowHeader.getValue();
		
		String[] allowedMethods = new String[] {
				"OPTIONS", "GET", "HEAD", "POST", "DELETE",
				"TRACE", "PROPPATCH", "COPY", "MOVE", "LOCK", "UNLOCK"
		};
		for(String allowedMethod:allowedMethods) {
			Assert.assertTrue(allowValue.contains(allowedMethod));
		}

		IOUtils.closeQuietly(conn);
	}
	
	@Test
	public void testPropFind()
	throws IOException, URISyntaxException {
		//create a user
		Identity user = JunitTestHelper.createAndPersistIdentityAsUser("webdav-2-" + UUID.randomUUID().toString());

		WebDAVConnection conn = new WebDAVConnection();
		conn.setCredentials(user.getName(), "A6B7C8");
		
		//list root content of its webdav folder
		URI uri = conn.getBaseURI().build();
		String xml = conn.propfind(uri, 1);
		Assert.assertTrue(xml.indexOf("<D:multistatus") > 0);//Windows need the D namespace
		Assert.assertTrue(xml.indexOf("<D:href>/</D:href>") > 0);//check the root
		Assert.assertTrue(xml.indexOf("<D:href>/webdav/</D:href>") > 0);//check the webdav folder

		//check public folder
		URI publicUri = conn.getBaseURI().path("webdav").path("home").path("public").build();
		String publicXml = conn.propfind(publicUri, 1);
		Assert.assertTrue(publicXml.indexOf("<D:multistatus") > 0);//Windows need the D namespace
		Assert.assertTrue(publicXml.indexOf("<D:href>/webdav/home/public/</D:href>") > 0);//check the root

		IOUtils.closeQuietly(conn);
	}
	
	@Test
	public void testPut_course()
	throws IOException, URISyntaxException {
		//create a user
		Identity author = JunitTestHelper.createAndPersistIdentityAsAuthor("webdav-3-" + UUID.randomUUID().toString());
		deployTestCourse(author, null);

		WebDAVConnection conn = new WebDAVConnection();
		conn.setCredentials(author.getName(), "A6B7C8");

		//author check course folder
		URI courseUri = conn.getBaseURI().path("webdav").path("coursefolders").build();
		String publicXml = conn.propfind(courseUri, 2);
		Assert.assertTrue(publicXml.indexOf("<D:href>/webdav/coursefolders/Kurs/_courseelementdata/</D:href>") > 0);

		//PUT in the folder
		URI putUri = UriBuilder.fromUri(courseUri).path("Kurs").path("test.txt").build();
		HttpPut put = conn.createPut(putUri);
		InputStream dataStream = WebDAVCommandsTest.class.getResourceAsStream("text.txt");
		InputStreamEntity entity = new InputStreamEntity(dataStream, -1);
		put.setEntity(entity);
		HttpResponse putResponse = conn.execute(put);
		Assert.assertEquals(201, putResponse.getStatusLine().getStatusCode());
		
		//GET
		HttpGet get = conn.createGet(putUri);
		HttpResponse getResponse = conn.execute(get);
		Assert.assertEquals(200, getResponse.getStatusLine().getStatusCode());
		String text = EntityUtils.toString(getResponse.getEntity());
		Assert.assertEquals("Small text", text);
	
		IOUtils.closeQuietly(conn);
	}
	
	/**
	 * PROPPATCH is essential for Windows
	 * @throws IOException
	 * @throws URISyntaxException
	 */
	@Test
	public void testPut_PropPatch_home()
	throws IOException, URISyntaxException {
		//create a user
		Identity author = JunitTestHelper.createAndPersistIdentityAsAuthor("webdav-3-" + UUID.randomUUID().toString());
		deployTestCourse(author, null);

		WebDAVConnection conn = new WebDAVConnection();
		conn.setCredentials(author.getName(), "A6B7C8");

		//author check course folder
		URI privateUri = conn.getBaseURI().path("webdav").path("home").path("private").build();
		conn.propfind(privateUri, 2);

		//PUT in the folder
		URI putUri = UriBuilder.fromUri(privateUri).path("test.txt").build();
		HttpPut put = conn.createPut(putUri);
		InputStream dataStream = WebDAVCommandsTest.class.getResourceAsStream("text.txt");
		InputStreamEntity entity = new InputStreamEntity(dataStream, -1);
		put.setEntity(entity);
		HttpResponse putResponse = conn.execute(put);
		Assert.assertEquals(201, putResponse.getStatusLine().getStatusCode());
		
		//PROPPATCH
		URI patchUri = UriBuilder.fromUri(privateUri).path("test.txt").build();
		HttpPropPatch patch = conn.createPropPatch(patchUri);
		
		StringBuilder sb = new StringBuilder();
		sb.append("<?xml version=\"1.0\" encoding=\"utf-8\" ?>")
		  .append("<D:propertyupdate xmlns:D=\"DAV:\"")
		  .append("  xmlns:Z=\"http://www.w3.com/standards/z39.50/\">")
		  .append("  <D:set>")
		  .append("      <D:prop>")
		  .append("           <Z:authors>")
		  .append("                <Z:Author>Jim Whitehead</Z:Author>")
		  .append("                <Z:Author>Roy Fielding</Z:Author>")
		  .append("           </Z:authors>")
		  .append("      </D:prop>")
		  .append("  </D:set>")
		  .append("  <D:remove>")
		  .append("      <D:prop><Z:Copyright-Owner/></D:prop>")
		  .append("   </D:remove>")
		  .append(" </D:propertyupdate>");
		
		patch.setEntity(new StringEntity(sb.toString()));
		
		HttpResponse patchResponse = conn.execute(patch);
		Assert.assertEquals(207, patchResponse.getStatusLine().getStatusCode());
	
		IOUtils.closeQuietly(conn);
	}
	
	@Test
	public void testLock()
	throws IOException, URISyntaxException {
		//create a user
		Identity author = JunitTestHelper.createAndPersistIdentityAsAuthor("webdav-3-" + UUID.randomUUID().toString());
		Identity assistant = JunitTestHelper.createAndPersistIdentityAsAuthor("webdav-4-" + UUID.randomUUID().toString());
		deployTestCourse(author, assistant);

		WebDAVConnection authorConn = new WebDAVConnection();
		authorConn.setCredentials(author.getName(), "A6B7C8");
		
		WebDAVConnection assistantConn = new WebDAVConnection();
		assistantConn.setCredentials(assistant.getName(), "A6B7C8");
		
		//author check course folder
		URI courseUri = authorConn.getBaseURI().path("webdav").path("coursefolders").build();
		String publicXml = authorConn.propfind(courseUri, 2);
		Assert.assertTrue(publicXml.indexOf("<D:href>/webdav/coursefolders/Kurs/_courseelementdata/</D:href>") > 0);

		//coauthor check course folder
		String assistantPublicXml = assistantConn.propfind(courseUri, 2);
		Assert.assertTrue(assistantPublicXml.indexOf("<D:href>/webdav/coursefolders/Kurs/_courseelementdata/</D:href>") > 0);

		//author lock the course folder
		
		
		
		IOUtils.closeQuietly(authorConn);
		IOUtils.closeQuietly(assistantConn);
	}
	
	private void deployTestCourse(Identity author, Identity coAuthor) throws URISyntaxException {
		URL courseWithForumsUrl = CoursePublishTest.class.getResource("myCourseWS.zip");
		Assert.assertNotNull(courseWithForumsUrl);
		File courseWithForums = new File(courseWithForumsUrl.toURI());
		String softKey = UUID.randomUUID().toString().replace("-", "").substring(0, 30);
		RepositoryEntry re = CourseFactory.deployCourseFromZIP(courseWithForums, author.getName(), softKey, 4);	
		securityManager.addIdentityToSecurityGroup(author, re.getOwnerGroup());
		if(coAuthor != null) {
			securityManager.addIdentityToSecurityGroup(coAuthor, re.getOwnerGroup());
		}
		
		dbInstance.commitAndCloseSession();
	}
}
