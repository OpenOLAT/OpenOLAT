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

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.UriBuilder;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.util.EntityUtils;
import org.junit.Assert;
import org.junit.Test;
import org.olat.core.commons.persistence.DB;
import org.olat.core.id.Identity;
import org.olat.modules.lecture.LectureBlock;
import org.olat.modules.lecture.LectureService;
import org.olat.modules.lecture.RepositoryEntryLectureConfiguration;
import org.olat.modules.lecture.restapi.LectureBlockVO;
import org.olat.repository.RepositoryEntry;
import org.olat.test.JunitTestHelper;
import org.olat.test.JunitTestHelper.IdentityWithLogin;
import org.olat.test.OlatRestTestCase;
import org.springframework.beans.factory.annotation.Autowired;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * 
 * Initial date: 13 sept. 2017<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class LecturesBlocksRootTest extends OlatRestTestCase {
	
	@Autowired
	private DB dbInstance;
	@Autowired
	private LectureService lectureService;
	
	/**
	 * Only administrator and lecture managers have access to this REST API
	 * 
	 * @throws IOException
	 * @throws URISyntaxException
	 */
	@Test
	public void getLecturesBlock_administrator()
	throws IOException, URISyntaxException {
		Identity author = JunitTestHelper.createAndPersistIdentityAsRndAuthor("lect-root-all");
		
		RepositoryEntry entry = deployCourseWithLecturesEnabled(author);
		LectureBlock block = createLectureBlock(entry);
		dbInstance.commit();
		lectureService.addTeacher(block, author);
		dbInstance.commit();

		RestConnection conn = new RestConnection();
		Assert.assertTrue(conn.login("administrator", "openolat"));

		URI uri = UriBuilder.fromUri(getContextURI()).path("repo").path("lectures").build();
		HttpGet method = conn.createGet(uri, MediaType.APPLICATION_JSON, true);
		HttpResponse response = conn.execute(method);
		
		Assert.assertEquals(200, response.getStatusLine().getStatusCode());
		List<LectureBlockVO> voList = parseLectureBlockArray(response.getEntity().getContent());
		Assert.assertNotNull(voList);
		Assert.assertFalse(voList.isEmpty());
		
		LectureBlockVO lectureBlockVo = null;
		for(LectureBlockVO vo:voList) {
			if(vo.getKey().equals(block.getKey())) {
				lectureBlockVo = vo;
			}
		}
		
		Assert.assertNotNull(lectureBlockVo);
		Assert.assertEquals(block.getKey(), lectureBlockVo.getKey());
		Assert.assertEquals(entry.getKey(), lectureBlockVo.getRepoEntryKey());
	}
	
	/**
	 * Only administrator and lecture managers have access to this REST API
	 * 
	 * @throws IOException
	 * @throws URISyntaxException
	 */
	@Test
	public void getLecturesBlock_permissionDenied()
	throws IOException, URISyntaxException {
		Identity author = JunitTestHelper.createAndPersistIdentityAsRndAuthor("lect-root-all");
		IdentityWithLogin user = JunitTestHelper.createAndPersistRndAuthor("lect-root-hacker");
		
		RepositoryEntry entry = deployCourseWithLecturesEnabled(author);
		LectureBlock block = createLectureBlock(entry);
		dbInstance.commit();
		lectureService.addTeacher(block, author);
		dbInstance.commit();

		RestConnection conn = new RestConnection();
		Assert.assertTrue(conn.login(user));

		URI uri = UriBuilder.fromUri(getContextURI()).path("repo").path("lectures").build();
		HttpGet method = conn.createGet(uri, MediaType.APPLICATION_JSON, true);
		HttpResponse response = conn.execute(method);
		Assert.assertEquals(401, response.getStatusLine().getStatusCode());
		EntityUtils.consumeQuietly(response.getEntity());
	}
	
	@Test
	public void getLecturesBlock_date()
	throws IOException, URISyntaxException {
		Identity author = JunitTestHelper.createAndPersistIdentityAsRndAuthor("lect-root-1");
		RepositoryEntry entry = deployCourseWithLecturesEnabled(author);
		LectureBlock block = createLectureBlock(entry);
		dbInstance.commit();
		lectureService.addTeacher(block, author);
		dbInstance.commit();

		RestConnection conn = new RestConnection();
		Assert.assertTrue(conn.login("administrator", "openolat"));

		String date = new SimpleDateFormat("yyyy-MM-dd'T'hh:mm:ss").format(new Date());
		URI uri = UriBuilder.fromUri(getContextURI()).path("repo").path("lectures").queryParam("date", date).build();
		HttpGet method = conn.createGet(uri, MediaType.APPLICATION_JSON, true);
		HttpResponse response = conn.execute(method);
		
		Assert.assertEquals(200, response.getStatusLine().getStatusCode());
		List<LectureBlockVO> voList = parseLectureBlockArray(response.getEntity().getContent());
		Assert.assertNotNull(voList);
		Assert.assertFalse(voList.isEmpty());
		
		LectureBlockVO lectureBlockVo = null;
		for(LectureBlockVO vo:voList) {
			if(vo.getKey().equals(block.getKey())) {
				lectureBlockVo = vo;
			}
		}
		
		Assert.assertNotNull(lectureBlockVo);
		Assert.assertEquals(block.getKey(), lectureBlockVo.getKey());
		Assert.assertEquals(entry.getKey(), lectureBlockVo.getRepoEntryKey());
	}
	
	private RepositoryEntry deployCourseWithLecturesEnabled(Identity author) {
		RepositoryEntry entry = JunitTestHelper.deployBasicCourse(author);
		RepositoryEntryLectureConfiguration config = lectureService.getRepositoryEntryLectureConfiguration(entry);
		config.setLectureEnabled(true);
		lectureService.updateRepositoryEntryLectureConfiguration(config);
		dbInstance.commit();
		return entry;
	}
	
	private LectureBlock createLectureBlock(RepositoryEntry entry) {
		LectureBlock lectureBlock = lectureService.createLectureBlock(entry);
		lectureBlock.setStartDate(new Date());
		lectureBlock.setEndDate(new Date());
		lectureBlock.setTitle("Hello lecturers");
		lectureBlock.setPlannedLecturesNumber(4);
		return lectureService.save(lectureBlock, null);
	}
	
	protected List<LectureBlockVO> parseLectureBlockArray(InputStream body) {
		try {
			ObjectMapper mapper = new ObjectMapper(jsonFactory); 
			return mapper.readValue(body, new TypeReference<List<LectureBlockVO>>(){/* */});
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
}
