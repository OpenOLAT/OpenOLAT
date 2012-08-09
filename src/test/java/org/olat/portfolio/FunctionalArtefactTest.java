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
package org.olat.portfolio;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.List;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.drone.api.annotation.Drone;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.olat.restapi.support.vo.CourseVO;
import org.olat.restapi.support.vo.RepositoryEntryVO;
import org.olat.test.ArquillianDeployments;
import org.olat.user.restapi.UserVO;
import org.olat.util.FunctionalCourseUtil;
import org.olat.util.FunctionalEportfolioUtil;
import org.olat.util.FunctionalHomeSiteUtil;
import org.olat.util.FunctionalRepositorySiteUtil;
import org.olat.util.FunctionalUtil;
import org.olat.util.FunctionalVOUtil;

import com.thoughtworks.selenium.DefaultSelenium;

/**
 * 
 * @author jkraehemann, joel.kraehemann@frentix.com, frentix.com
 */
@RunWith(Arquillian.class)
public class FunctionalArtefactTest {
	public final static String FORUM_POST_TITLE = "question about multiplexing";
	public final static String FORUM_POST_MESSAGE = "What multiplexing exists in operating systems?";
	
	public final static String WIKI_ARTICLE_PAGENAME = "Multiplexing";
	public final static String WIKI_ARTICLE_CONTENT = "==Time Multiplexing==\nscheduling a serially-reusable resource among several users\n\n==Space multiplexing==\ndividing a multiple-use resource up among several users";
	
	public final static String BLOG_POST_TITLE = "Multiplexing articles";
	public final static String BLOG_POST_DESCRIPTION = "Where you may find useful information about multiplexing.";
	public final static String BLOG_POST_CONTENT = "Operating Systems: Design & Implementation (by Andrew S. Tanenbaum)";
	
	public final static String TEXT_ARTEFACT_CONTENT = "Bufferbloat is a phenomenon in a packet-switched computer network whereby excess buffering of packets inside the network causes high latency and jitter, as well as reducing the overall network throughput.";
	public final static String TEXT_ARTEFACT_TITLE = "Definition bufferbloat";
	public final static String TEXT_ARTEFACT_DESCRIPTION = "Definition bufferbloat";
	public final static String TEXT_ARTEFACT_TAGS = "bufferbloat network latency jitter";
	public final static String TEXT_ARTEFACT_BINDERPATH = "/My Binders";
	
	public final static String FILE_ARTEFACT_PATH = "/org/olat/portfolio/sfqcodel.cc";
	public final static String FILE_ARTEFACT_TITLE = "CoDel";
	public final static String FILE_ARTEFACT_DESCRIPTION = "CoDel Algorithm";
	public final static String FILE_ARTEFACT_TAGS = "codel";
	public final static String FILE_ARTEFACT_BINDERPATH = "/My Binders";

	public final static String LEARNING_JOURNAL_TITLE = "Programming Topics";
	public final static String LEARNING_JOURNAL_DESCRIPTION = "Some hot programming topics";
	public final static String LEARNING_JOURNAL_TAGS = "programming c c++";
	public final static String LEARNING_JOURNAL_BINDERPATH = "/My Binders";
	
	@Deployment(testable = false)
	public static WebArchive createDeployment() {
		return ArquillianDeployments.createDeployment();
	}

	@Drone
	DefaultSelenium browser;

	@ArquillianResource
	URL deploymentUrl;

	FunctionalUtil functionalUtil;
	FunctionalHomeSiteUtil functionalHomeSiteUtil;
	FunctionalRepositorySiteUtil functionalRepositorySiteUtil;
	FunctionalCourseUtil functionalCourseUtil;
	FunctionalEportfolioUtil functionalEportfolioUtil;
	FunctionalVOUtil functionalVOUtil;
	
	UserVO user;
	
	@Before
	public void setup() throws IOException, URISyntaxException{
		functionalUtil = new FunctionalUtil();
		functionalUtil.setDeploymentUrl(deploymentUrl.toString());
		functionalHomeSiteUtil = new FunctionalHomeSiteUtil(functionalUtil);

		functionalRepositorySiteUtil = new FunctionalRepositorySiteUtil(functionalUtil);
		functionalCourseUtil = new FunctionalCourseUtil(functionalUtil, functionalRepositorySiteUtil);
		functionalEportfolioUtil = new FunctionalEportfolioUtil(functionalUtil, functionalHomeSiteUtil);
		
		functionalVOUtil = new FunctionalVOUtil(functionalUtil.getUsername(), functionalUtil.getPassword());
		
		/* create test user with REST */
		List<UserVO> userVO = functionalVOUtil.createTestUsers(deploymentUrl, 1);
		
		user = userVO.get(0);
	}
	
	@Test
	@RunAsClient
	public void checkCollectForumPost() throws IOException, URISyntaxException{
		/* deploy course with REST */
		CourseVO course = functionalVOUtil.importAllElementsCourse(deploymentUrl);
		
		/* login for test setup */
		Assert.assertTrue(functionalUtil.login(browser, user.getLogin(), user.getPassword(), true));
		
		/* post message to forum */
		Assert.assertTrue(functionalCourseUtil.postForumMessage(browser, course.getRepoEntryKey(), 0, FORUM_POST_TITLE, FORUM_POST_MESSAGE));
		
		/* add artefact */
		Assert.assertTrue(functionalCourseUtil.addToEportfolio(browser));
	}
	
	@Test
	@RunAsClient
	public void checkCollectWikiArticle() throws URISyntaxException, IOException{
		/* import wiki via rest */
		RepositoryEntryVO vo = functionalVOUtil.importWiki(deploymentUrl);
		
		/* login for test setup */
		Assert.assertTrue(functionalUtil.login(browser, user.getLogin(), user.getPassword(), true));
		
		/* create an article for the wiki */
		Assert.assertTrue(functionalCourseUtil.createWikiArticle(browser, vo.getKey(), WIKI_ARTICLE_PAGENAME, WIKI_ARTICLE_CONTENT));
		
		/* add artefact */
		Assert.assertTrue(functionalCourseUtil.addToEportfolio(browser));
	}
	
	@Test
	@RunAsClient
	public void checkCollectBlogPost() throws URISyntaxException, IOException{
		/* import blog via rest */
		RepositoryEntryVO vo = functionalVOUtil.importBlog(deploymentUrl, user.getLogin(), user.getPassword());
		
		/* login for test setup */
		Assert.assertTrue(functionalUtil.login(browser, user.getLogin(), user.getPassword(), true));
		
		/* blog */
		Assert.assertTrue(functionalCourseUtil.createBlogEntry(browser, vo.getKey(), BLOG_POST_TITLE, BLOG_POST_DESCRIPTION, BLOG_POST_CONTENT));
		
		/* add artefact */
		Assert.assertTrue(functionalCourseUtil.addToEportfolio(browser));
	}
	
	@Test
	@RunAsClient
	public void checkAddTextArtefact(){
		/* login for test setup */
		Assert.assertTrue(functionalUtil.login(browser, user.getLogin(), user.getPassword(), true));
		
		/* add text artefact */
		Assert.assertTrue(functionalEportfolioUtil.addTextArtefact(browser, TEXT_ARTEFACT_CONTENT,
				TEXT_ARTEFACT_TITLE, TEXT_ARTEFACT_DESCRIPTION,
				TEXT_ARTEFACT_TAGS,
				TEXT_ARTEFACT_BINDERPATH));
	}
	
	@Test
	@RunAsClient
	public void checkUploadFileArtefact() throws URISyntaxException, MalformedURLException{
		/* login for test setup */
		Assert.assertTrue(functionalUtil.login(browser, user.getLogin(), user.getPassword(), true));

		/* upload file artefact */
		Assert.assertTrue(functionalEportfolioUtil.uploadFileArtefact(browser, FunctionalArtefactTest.class.getResource(FILE_ARTEFACT_PATH).toURI(),
				FILE_ARTEFACT_TITLE, FILE_ARTEFACT_DESCRIPTION,
				FILE_ARTEFACT_TAGS,
				FILE_ARTEFACT_BINDERPATH));
	}
	
	@Test
	@RunAsClient
	public void checkCreateLearningJournal(){
		/* login for test setup */
		Assert.assertTrue(functionalUtil.login(browser, user.getLogin(), user.getPassword(), true));
		
		/* create learning journal */
		Assert.assertTrue(functionalEportfolioUtil.createLearningJournal(browser,
				LEARNING_JOURNAL_TITLE, LEARNING_JOURNAL_DESCRIPTION,
				LEARNING_JOURNAL_TAGS,
				LEARNING_JOURNAL_BINDERPATH));
	}
}
