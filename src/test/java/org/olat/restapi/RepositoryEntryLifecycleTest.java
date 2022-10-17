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
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.UriBuilder;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.junit.Assert;
import org.junit.Test;
import org.olat.core.commons.persistence.DB;
import org.apache.logging.log4j.Logger;
import org.olat.core.logging.Tracing;
import org.olat.repository.manager.RepositoryEntryLifecycleDAO;
import org.olat.repository.model.RepositoryEntryLifecycle;
import org.olat.restapi.support.vo.RepositoryEntryLifecycleVO;
import org.olat.test.OlatRestTestCase;
import org.springframework.beans.factory.annotation.Autowired;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * 
 * Initial date: 10.06.2013<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class RepositoryEntryLifecycleTest extends OlatRestTestCase  {
	
	private static final Logger log = Tracing.createLoggerFor(RepositoryEntryLifecycleTest.class);
	
	@Autowired
	private RepositoryEntryLifecycleDAO reLifeCycleDao;
	@Autowired
	private DB dbInstance;
	
	@Test
	public void testGetEntries() throws IOException, URISyntaxException {
		// create a public life cycle
		RepositoryEntryLifecycle reLifeCycle = reLifeCycleDao.create("REST life cycle", "Unique", false, null, null);
		Calendar cal = Calendar.getInstance();
		cal.setTime(new Date());
		cal.add(Calendar.DATE, -2);
		Date from = cal.getTime();
		cal.add(Calendar.DATE, 5);
		Date to = cal.getTime();
		RepositoryEntryLifecycle limitLifeCycle = reLifeCycleDao.create("REST life cycle", "Unique", false, from, to);
		dbInstance.commitAndCloseSession();
		
		//retrieve the public life cycles
		RestConnection conn = new RestConnection();
		assertTrue(conn.login("administrator", "openolat"));
		
		URI request = UriBuilder.fromUri(getContextURI()).path("repo/lifecycle").build();
		HttpGet method = conn.createGet(request, MediaType.APPLICATION_JSON, true);
		HttpResponse response = conn.execute(method);
		assertEquals(200, response.getStatusLine().getStatusCode());
		List<RepositoryEntryLifecycleVO> entryVoes = parseRepoArray(response.getEntity());
		assertNotNull(entryVoes);
		
		int found = 0;
		for(RepositoryEntryLifecycleVO entryVo:entryVoes) {
			if(reLifeCycle.getKey().equals(entryVo.getKey())) {
				found++;
			} else if(limitLifeCycle.getKey().equals(entryVo.getKey())) {
				found++;
			}
		}

		conn.shutdown();
		Assert.assertEquals(2, found);
	}


	private List<RepositoryEntryLifecycleVO> parseRepoArray(HttpEntity entity) {
		try(InputStream in=entity.getContent()) {
			ObjectMapper mapper = new ObjectMapper(jsonFactory); 
			return mapper.readValue(in, new TypeReference<List<RepositoryEntryLifecycleVO>>(){/* */});
		} catch (Exception e) {
			log.error("", e);
			return null;
		}
	}
}
