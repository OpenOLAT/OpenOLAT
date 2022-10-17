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

import static org.olat.test.JunitTestHelper.random;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Date;

import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.UriBuilder;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.junit.Assert;
import org.junit.Test;
import org.olat.core.commons.persistence.DB;
import org.olat.core.commons.services.doceditor.Access;
import org.olat.core.commons.services.doceditor.DocEditor.Mode;
import org.olat.core.commons.services.doceditor.manager.AccessDAO;
import org.olat.core.commons.services.vfs.VFSMetadata;
import org.olat.core.commons.services.vfs.manager.VFSMetadataDAO;
import org.olat.core.id.Identity;
import org.olat.restapi.system.vo.DocEditorStatisticsVO;
import org.olat.test.JunitTestHelper;
import org.olat.test.OlatRestTestCase;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 13 jul. 2020<br>
 * @author mjenny, moritz.jenny@frentix.com, http://www.frentix.com
 *
 */
public class DocEditorWebServiceTest extends OlatRestTestCase {
	
	@Autowired
	private DB dbInstance;
	@Autowired
	private VFSMetadataDAO vfsMetadataDAO;
	@Autowired
	private AccessDAO accessDao;

	@Test
	public void docEditorSessionQuery()
	throws IOException, URISyntaxException {
		
		RestConnection conn = new RestConnection();
		Assert.assertTrue(conn.login("administrator", "openolat"));
		
		String randomAppName = random();
		Identity identity = JunitTestHelper.createAndPersistIdentityAsRndUser("restuser1");
		Identity identity2 = JunitTestHelper.createAndPersistIdentityAsRndUser("restuser2");
		Access access1 = accessDao.createAccess(randomMetadata(), identity, randomAppName, Mode.EDIT, true, true, new Date());
		Access access2 = accessDao.createAccess(randomMetadata(), identity2, randomAppName, Mode.VIEW, false, true, new Date());
		dbInstance.commitAndCloseSession();	
		
		Assert.assertNotNull(access1);
		Assert.assertNotNull(access2);
		
		URI request = UriBuilder.fromUri(getContextURI()).path("system").path("monitoring").path("doceditor").path("sessions").path(randomAppName).build();
		HttpGet method = conn.createGet(request, MediaType.APPLICATION_JSON, true);
		HttpResponse response = conn.execute(method);
		Assert.assertEquals(200, response.getStatusLine().getStatusCode());
		
		DocEditorStatisticsVO docEditorStatisticsVO = conn.parse(response, DocEditorStatisticsVO.class);
		
		Assert.assertEquals(1, docEditorStatisticsVO.getOpenDocumentsRead());
		Assert.assertEquals(1, docEditorStatisticsVO.getOpenDocumentsWrite());
	}
	
	private VFSMetadata randomMetadata() {
		return vfsMetadataDAO.createMetadata(random(), random(), random(), new Date(), 1000l, false, "file://" + random(), "file", null);
	}
}
