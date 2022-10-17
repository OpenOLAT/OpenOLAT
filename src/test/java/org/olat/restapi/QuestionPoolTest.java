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

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.UriBuilder;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.util.EntityUtils;
import org.junit.Assert;
import org.junit.Test;
import org.olat.core.commons.persistence.DB;
import org.olat.core.id.Identity;
import org.olat.ims.qti21.QTI21Constants;
import org.olat.modules.qpool.QPoolService;
import org.olat.modules.qpool.QuestionItem;
import org.olat.modules.qpool.QuestionType;
import org.olat.modules.qpool.manager.QItemTypeDAO;
import org.olat.modules.qpool.manager.QuestionItemDAO;
import org.olat.modules.qpool.model.QItemType;
import org.olat.modules.qpool.restapi.QuestionItemVO;
import org.olat.modules.qpool.restapi.QuestionItemVOes;
import org.olat.test.JunitTestHelper;
import org.olat.test.OlatRestTestCase;
import org.olat.user.restapi.UserVO;
import org.springframework.beans.factory.annotation.Autowired;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * 
 * Initial date: 8 sept. 2017<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class QuestionPoolTest extends OlatRestTestCase {
	
	@Autowired
	private DB dbInstance;
	@Autowired
	private QPoolService qpoolService;
	@Autowired
	private QItemTypeDAO qItemTypeDao;
	@Autowired
	private QuestionItemDAO questionDao;
	@Autowired
	private QuestionItemDAO questionItemDao;

	@Test
	public void importQuestion()
	throws IOException, URISyntaxException {
		URL itemUrl = QuestionPoolTest.class.getResource("multiple_choice_per_answer.zip");
		assertNotNull(itemUrl);
		File itemFile = new File(itemUrl.toURI());

		RestConnection conn = new RestConnection();
		assertTrue(conn.login("administrator", "openolat"));
		
		URI request = UriBuilder.fromUri(getContextURI()).path("qpool/items").build();
		HttpPut method = conn.createPut(request, MediaType.APPLICATION_JSON, true);
		HttpEntity entity = MultipartEntityBuilder.create()
				.setMode(HttpMultipartMode.BROWSER_COMPATIBLE)
				.addBinaryBody("file", itemFile, ContentType.APPLICATION_OCTET_STREAM, itemFile.getName())
				.addTextBody("filename", "multiple_choice_per_answer.zip")
				.build();
		method.setEntity(entity);
		
		HttpResponse response = conn.execute(method);
		Assert.assertEquals(200, response.getStatusLine().getStatusCode());

		QuestionItemVOes voes = conn.parse(response, QuestionItemVOes.class);
		Assert.assertNotNull(voes);
		QuestionItemVO[] voArray = voes.getQuestionItems();
		Assert.assertNotNull(voArray);
		Assert.assertEquals(1, voArray.length);
		QuestionItemVO vo = voArray[0];
		Assert.assertNotNull(vo);
	}

	@Test
	public void getAuthors() throws IOException, URISyntaxException {
		QItemType mcType = qItemTypeDao.loadByType(QuestionType.MC.name());
		Identity author = JunitTestHelper.createAndPersistIdentityAsRndUser("item-author-1");
		QuestionItem item = questionDao.createAndPersist(author, "NGC 55", QTI21Constants.QTI_21_FORMAT, Locale.ENGLISH.getLanguage(), null, null, null, mcType);
		dbInstance.commitAndCloseSession();
		
		RestConnection conn = new RestConnection();
		Assert.assertTrue(conn.login("administrator", "openolat"));
		
		URI request = UriBuilder.fromUri(getContextURI()).path("/qpool/items/" + item.getKey() + "/authors/").build();
		HttpGet method = conn.createGet(request, MediaType.APPLICATION_JSON, true);
		HttpResponse response = conn.execute(method);
		Assert.assertEquals(200, response.getStatusLine().getStatusCode());
		List<UserVO> users = parseUserArray(response.getEntity().getContent());
		//check
		Assert.assertNotNull(users);
		Assert.assertEquals(1, users.size());
		Assert.assertTrue(author.getKey().equals(users.get(0).getKey()));
	}
	
	@Test
	public void getAuthor() throws IOException, URISyntaxException {
		QItemType mcType = qItemTypeDao.loadByType(QuestionType.MC.name());
		Identity author = JunitTestHelper.createAndPersistIdentityAsRndUser("item-author-2");
		QuestionItem item = questionDao.createAndPersist(author, "NGC 55", QTI21Constants.QTI_21_FORMAT, Locale.ENGLISH.getLanguage(), null, null, null, mcType);
		dbInstance.commitAndCloseSession();
		
		RestConnection conn = new RestConnection();
		Assert.assertTrue(conn.login("administrator", "openolat"));
		
		URI request = UriBuilder.fromUri(getContextURI()).path("/qpool/items/" + item.getKey() + "/authors/" + author.getKey()).build();
		HttpGet method = conn.createGet(request, MediaType.APPLICATION_JSON, true);
		HttpResponse response = conn.execute(method);
		Assert.assertEquals(200, response.getStatusLine().getStatusCode());
		UserVO user = conn.parse(response.getEntity(), UserVO.class);
		//check
		Assert.assertNotNull(user);
		Assert.assertTrue(author.getKey().equals(user.getKey()));
	}
	
	@Test
	public void addAuthor() throws IOException, URISyntaxException {
		QItemType mcType = qItemTypeDao.loadByType(QuestionType.MC.name());
		Identity author = JunitTestHelper.createAndPersistIdentityAsRndUser("item-author-1");
		QuestionItem item = questionDao.createAndPersist(author, "NGC 55", QTI21Constants.QTI_21_FORMAT, Locale.ENGLISH.getLanguage(), null, null, null, mcType);
		Identity coAuthor = JunitTestHelper.createAndPersistIdentityAsRndUser("item-author-1");

		RestConnection conn = new RestConnection();
		Assert.assertTrue(conn.login("administrator", "openolat"));
		
		URI request = UriBuilder.fromUri(getContextURI()).path("/qpool/items/" + item.getKey() + "/authors/" + coAuthor.getKey()).build();
		HttpPut method = conn.createPut(request, MediaType.APPLICATION_JSON, true);
		HttpResponse response = conn.execute(method);
		Assert.assertEquals(200, response.getStatusLine().getStatusCode());
		EntityUtils.consume(response.getEntity());

		//check
		List<Identity> authors = qpoolService.getAuthors(item);
		Assert.assertNotNull(authors);
		Assert.assertEquals(2, authors.size());
		Assert.assertTrue(authors.contains(author));
		Assert.assertTrue(authors.contains(coAuthor));
	}
	
	@Test
	public void removeAuthor() throws IOException, URISyntaxException {
		QItemType mcType = qItemTypeDao.loadByType(QuestionType.MC.name());
		Identity author = JunitTestHelper.createAndPersistIdentityAsRndUser("item-author-1");
		QuestionItem item = questionDao.createAndPersist(author, "NGC 55", QTI21Constants.QTI_21_FORMAT, Locale.ENGLISH.getLanguage(), null, null, null, mcType);
		Identity coAuthor = JunitTestHelper.createAndPersistIdentityAsRndUser("item-author-1");
		List<Identity> authors = Collections.singletonList(coAuthor);
		questionItemDao.addAuthors(authors, item);
		dbInstance.commit();

		RestConnection conn = new RestConnection();
		Assert.assertTrue(conn.login("administrator", "openolat"));
		
		URI request = UriBuilder.fromUri(getContextURI()).path("/qpool/items/" + item.getKey() + "/authors/" + coAuthor.getKey()).build();
		HttpDelete method = conn.createDelete(request, MediaType.APPLICATION_JSON);
		HttpResponse response = conn.execute(method);
		Assert.assertEquals(200, response.getStatusLine().getStatusCode());
		EntityUtils.consume(response.getEntity());

		//check
		List<Identity> itemsAuthors = qpoolService.getAuthors(item);
		Assert.assertNotNull(itemsAuthors);
		Assert.assertEquals(1, itemsAuthors.size());
		Assert.assertTrue(itemsAuthors.contains(author));
		Assert.assertFalse(itemsAuthors.contains(coAuthor));
	}

	protected List<UserVO> parseUserArray(InputStream body) {
		try {
			ObjectMapper mapper = new ObjectMapper(jsonFactory); 
			return mapper.readValue(body, new TypeReference<List<UserVO>>(){/* */});
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
}
