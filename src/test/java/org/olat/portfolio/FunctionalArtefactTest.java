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

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
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
import org.olat.util.FunctionalCourseUtil.AccessOption;
import org.olat.util.FunctionalEPortfolioUtil;
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

	/* content */
	public final static String BINDER_PROGRAMMING_THEORIE = "programming (theorie)";
	public final static String BINDER_PROGRAMMING_SAMPLES = "programming (code samples)";

	public final static String FORUM_POST_TITLE = "question about multiplexing";
	public final static String FORUM_POST_MESSAGE = "What multiplexing exists in operating systems?";
	public final static String FORUM_ARTEFACT_TITLE = "multiplexing forum post";
	public final static String FORUM_ARTEFACT_DESCRIPTION = "Thread about multiplexing.";
	public final static String[] FORUM_TAGS = {"networking", "multiplexing", "operating systems", "virtual machine", "forum", "post"};
	public final static String FORUM_BINDER = BINDER_PROGRAMMING_THEORIE;
	public final static String FORUM_PAGE = "operating systems";
	public final static String FORUM_STRUCTURE = "issue 1";

	public final static String WIKI_ARTICLE_PAGENAME = "Multiplexing";
	public final static String WIKI_ARTICLE_CONTENT = "==Time Multiplexing==\nscheduling a serially-reusable resource among several users\n\n==Space multiplexing==\ndividing a multiple-use resource up among several users";
	public final static String WIKI_ARTEFACT_TITLE = "multiplexing wiki";
	public final static String WIKI_ARTEFACT_DESCRIPTION = "wiki page about multiplexing";
	public final static String[] WIKI_TAGS = {"networking", "multiplexing", "operating systems", "virtual machine", "wiki"};
	public final static String WIKI_BINDER = BINDER_PROGRAMMING_THEORIE;
	public final static String WIKI_PAGE = "operating systems";
	public final static String WIKI_STRUCTURE = "issue 2";

	public final static String BLOG_TITLE = "My Blog";
	public final static String BLOG_DESCRIPTION = "Blog created with Selenium";
	public final static String BLOG_POST_TITLE = "Multiplexing articles";
	public final static String BLOG_POST_DESCRIPTION = "Where you may find useful information about multiplexing.";
	public final static String BLOG_POST_CONTENT = "Operating Systems: Design & Implementation (by Andrew S. Tanenbaum)";
	public final static String BLOG_ARTEFACT_TITLE = "blog";
	public final static String BLOG_ARTEFACT_DESCRIPTION = "my personal blog";
	public final static String[] BLOG_TAGS = {"john smith", "blog"};
	public final static String BLOG_BINDER = BINDER_PROGRAMMING_THEORIE;
	public final static String BLOG_PAGE = "operating systems";
	public final static String BLOG_STRUCTURE = "issue 3";

	public final static String TEXT_ARTEFACT_CONTENT = "Bufferbloat is a phenomenon in a packet-switched computer network whereby excess buffering of packets inside the network causes high latency and jitter, as well as reducing the overall network throughput.";
	public final static String TEXT_ARTEFACT_TITLE = "Definition bufferbloat";
	public final static String TEXT_ARTEFACT_DESCRIPTION = "Definition bufferbloat";
	public final static String[] TEXT_ARTEFACT_TAGS = {"bufferbloat", "network", "latency", "jitter"};
	public final static String TEXT_ARTEFACT_BINDER = BINDER_PROGRAMMING_THEORIE;
	public final static String TEXT_ARTEFACT_PAGE = "networking";
	public final static String TEXT_ARTEFACT_STRUCTURE = "issue 1";

	public final static String FILE_ARTEFACT_PATH = "/org/olat/portfolio/sfqcodel.cc";
	public final static String FILE_ARTEFACT_TITLE = "CoDel";
	public final static String FILE_ARTEFACT_DESCRIPTION = "CoDel Algorithm";
	public final static String[] FILE_ARTEFACT_TAGS = {"codel", "sample code"};
	public final static String FILE_ARTEFACT_BINDER = BINDER_PROGRAMMING_SAMPLES;
	public final static String FILE_ARTEFACT_PAGE = "cpp";
	public final static String FILE_ARTEFACT_STRUCTURE = "issue 1";

	public final static String LEARNING_JOURNAL_TITLE = "Programming Topics";
	public final static String LEARNING_JOURNAL_DESCRIPTION = "Some hot programming topics";
	public final static String[] LEARNING_JOURNAL_TAGS = {"programming", "c", "c++"};
	public final static String LEARNING_JOURNAL_BINDER = BINDER_PROGRAMMING_THEORIE;
	public final static String LEARNING_JOURNAL_PAGE = "journal";
	public final static String LEARNING_JOURNAL_STRUCTURE = "2012/08/13";

	@Deployment(testable = false)
	public static WebArchive createDeployment() {
		return ArquillianDeployments.createDeployment();
	}

	@Drone
	DefaultSelenium browser;

	@ArquillianResource
	URL deploymentUrl;

	static FunctionalUtil functionalUtil;
	static FunctionalHomeSiteUtil functionalHomeSiteUtil;
	static FunctionalRepositorySiteUtil functionalRepositorySiteUtil;
	static FunctionalCourseUtil functionalCourseUtil;
	static FunctionalEPortfolioUtil functionalEportfolioUtil;
	static FunctionalVOUtil functionalVOUtil;

	static UserVO user;
	static List<Binder> map = new ArrayList<Binder>();

	static boolean initialized = false;

	@Before
	public void setup() throws IOException, URISyntaxException{
		if(!initialized){
			functionalUtil = new FunctionalUtil();
			functionalUtil.setDeploymentUrl(deploymentUrl.toString());
			functionalHomeSiteUtil = new FunctionalHomeSiteUtil(functionalUtil);

			functionalRepositorySiteUtil = new FunctionalRepositorySiteUtil(functionalUtil);
			functionalCourseUtil = new FunctionalCourseUtil(functionalUtil, functionalRepositorySiteUtil);
			functionalEportfolioUtil = new FunctionalEPortfolioUtil(functionalUtil, functionalHomeSiteUtil);

			functionalVOUtil = new FunctionalVOUtil(functionalUtil.getUsername(), functionalUtil.getPassword());

			/* create test user with REST */
			List<UserVO> userVO = functionalVOUtil.createTestUsers(deploymentUrl, 1);

			user = userVO.get(0);
			
			initialized = true;
		}
	}
	
	Object[] prepareVerification(String binderName, String binderDescription,
			String pageName, String pageDescription,
			String structureName, String structureDescription,
			String artefactName, String artefactDescription, String[] artefactTags, Object artefactContent){
		Binder binder = findBinderByName(this.map, binderName);
		
		if(binder == null){
			binder = new Binder(binderName, binderDescription);
			this.map.add(binder);
		}
		
		Binder.Page page = findPageByName(binder.page, pageName);
		
		if(page == null){
			page = binder.new Page(pageName, pageDescription);
			binder.page.add(page);
		}
		
		Binder.Page.Structure structure = findStructureByName(page.child, structureName);
		
		if(structure == null && structureName != null){
			structure = page.new Structure(structureName, structureDescription);
			page.child.add(structure);
		}
		
		Binder.Page.Artefact artefact = findArtefactByName(page.child, artefactName);
		
		if(artefact == null && structure != null){
			artefact = findArtefactByName(structure.child, artefactName);
		}
		
		if(artefact == null){
			artefact = page.new Artefact(artefactName, artefactDescription, artefactTags, artefactContent);
			
			if(structure != null){
				structure.child.add(artefact);
			}else{
				page.child.add(artefact);
			}
		}
		
		Object[] retval = new Object[]{binder, page, structure, artefact};
		
		return(retval);
	}
	
	Binder findBinderByName(List<Binder> binder, String name){
		if(name == null)
			return(null);
		
		for(Binder current: binder){
			if(name.equals(current.name)){
				return(current);
			}
		}	
		
		return(null);
	}
	
	Binder.Page findPageByName(List<Binder.Page> page, String name){
		if(name == null)
			return(null);
		
		for(Binder.Page current: page){
			if(name.equals(current.name)){
				return(current);
			}
		}	
		
		return(null);
	}
	
	Binder.Page.Artefact findArtefactByName(List<?> list, String name){
		if(name == null)
			return(null);
		
		for(Object current: list){
			if(current instanceof Binder.Page.Artefact && name.equals(((Binder.Page.Artefact) current).name)){
				return((Binder.Page.Artefact) current);
			}
		}	
		
		return(null);
	}
	
	Binder.Page.Structure findStructureByName(List<?> list, String name){
		if(name == null)
			return(null);
		
		for(Object current: list){
			if(current instanceof Binder.Page.Structure && name.equals(((Binder.Page.Structure) current).name)){
				return((Binder.Page.Structure) current);
			}
		}	
		
		return(null);
	}
	
	boolean checkArtefact(Binder.Page.Artefact artefact){
		
		if(!functionalEportfolioUtil.openArtefactDetails(browser, user.getKey(), artefact.name)){
			return(false);
		}
		
		//TODO:JK: implement me
		return(true);
	}
	
	boolean checkMap(Binder binder){
		//TODO:JK: implement me
		return(true);
	}

	@Test
	@RunAsClient
	public void checkCollectForumPost() throws IOException, URISyntaxException{
		/*
		 * Prepare for verification
		 */		
		Object[] retval = prepareVerification(FORUM_BINDER, null,
				FORUM_PAGE, null,
				FORUM_STRUCTURE, null,
				FORUM_ARTEFACT_TITLE, FORUM_ARTEFACT_DESCRIPTION, FORUM_TAGS, null);
		
		Binder binder = (Binder) retval[0];
		Binder.Page page = (Binder.Page) retval[1];
		Binder.Page.Structure structure = (Binder.Page.Structure) retval[2];
		Binder.Page.Artefact artefact = (Binder.Page.Artefact) retval[3];
		
		/*
		 * test case
		 */
		/* deploy course with REST */
		CourseVO course = functionalVOUtil.importCourseIncludingForum(deploymentUrl);

		/* login for test setup */
		Assert.assertTrue(functionalUtil.login(browser, user.getLogin(), user.getPassword(), true));

		/* create binder, page or structure if necessary */
		Assert.assertTrue(functionalEportfolioUtil.createElements(browser, FORUM_BINDER, FORUM_PAGE, FORUM_STRUCTURE));

		/* post message to forum */
		Assert.assertTrue(functionalCourseUtil.postForumMessage(browser, course.getRepoEntryKey(), 0, FORUM_POST_TITLE, FORUM_POST_MESSAGE));

		/* add artefact */
		Assert.assertTrue(functionalCourseUtil.addToEportfolio(browser, FORUM_BINDER, FORUM_PAGE, FORUM_STRUCTURE,
				FORUM_ARTEFACT_TITLE, FORUM_ARTEFACT_DESCRIPTION, FORUM_TAGS,
				functionalEportfolioUtil));

		/*
		 * Test for content and make assumptions if the changes were applied.
		 * Keep it simple use quick access with business paths.
		 */
		binder.ignore = false;
		
		page.ignore = false;
		
		structure.ignore = false;
		
		//TODO:JK: find a way to retrieve resourceable key
		//artefact.content = new String(deploymentUrl.toString() + "/url/RepositoryEntry/" + course.getRepoEntryKey() + "/CourseNode/");
		artefact.ignore = false;
		
		/* verify */
		Assert.assertTrue(checkArtefact(artefact));
		Assert.assertTrue(checkMap(binder));
	}

	@Test
	@RunAsClient
	public void checkCollectWikiArticle() throws URISyntaxException, IOException{
		/* import wiki via rest */
		RepositoryEntryVO vo = functionalVOUtil.importWiki(deploymentUrl);

		/* login for test setup */
		Assert.assertTrue(functionalUtil.login(browser, user.getLogin(), user.getPassword(), true));

		/* create binder, page or structure if necessary */
		Assert.assertTrue(functionalEportfolioUtil.createElements(browser, WIKI_BINDER, WIKI_PAGE, WIKI_STRUCTURE));

		/* create an article for the wiki */
		Assert.assertTrue(functionalCourseUtil.createWikiArticle(browser, vo.getKey(), WIKI_ARTICLE_PAGENAME, WIKI_ARTICLE_CONTENT));

		/* add artefact */
		Assert.assertTrue(functionalCourseUtil.addToEportfolio(browser, WIKI_BINDER, WIKI_PAGE, WIKI_STRUCTURE,
				WIKI_ARTEFACT_TITLE, WIKI_ARTEFACT_DESCRIPTION, WIKI_TAGS,
				functionalEportfolioUtil));
	}

	@Test
	@RunAsClient
	public void checkCollectBlogPost() throws URISyntaxException, IOException{
		/* deploy course with REST */
		CourseVO course = functionalVOUtil.importCourseIncludingBlog(deploymentUrl);	

		/* login for test setup */
		Assert.assertTrue(functionalUtil.login(browser, user.getLogin(), user.getPassword(), true));

		/* create binder, page or structure if necessary */
		Assert.assertTrue(functionalEportfolioUtil.createElements(browser, BLOG_BINDER, BLOG_PAGE, BLOG_STRUCTURE));

		/* blog */
		Assert.assertTrue(functionalCourseUtil.createBlogEntry(browser, course.getRepoEntryKey(), 0,
				BLOG_POST_TITLE, BLOG_POST_DESCRIPTION, BLOG_POST_CONTENT));

		/* add artefact */
		Assert.assertTrue(functionalCourseUtil.addToEportfolio(browser, BLOG_BINDER, BLOG_PAGE, BLOG_STRUCTURE,
				BLOG_ARTEFACT_TITLE, BLOG_ARTEFACT_DESCRIPTION, BLOG_TAGS,
				functionalEportfolioUtil));
	}

	@Test
	@RunAsClient
	public void checkAddTextArtefact(){
		/* login for test setup */
		Assert.assertTrue(functionalUtil.login(browser, user.getLogin(), user.getPassword(), true));

		/* add text artefact */
		Assert.assertTrue(functionalEportfolioUtil.addTextArtefact(browser, TEXT_ARTEFACT_BINDER, TEXT_ARTEFACT_PAGE, TEXT_ARTEFACT_STRUCTURE,
				TEXT_ARTEFACT_CONTENT,
				TEXT_ARTEFACT_TITLE, TEXT_ARTEFACT_DESCRIPTION,
				TEXT_ARTEFACT_TAGS));
	}

	@Test
	@RunAsClient
	public void checkUploadFileArtefact() throws URISyntaxException, MalformedURLException{
		/* login for test setup */
		Assert.assertTrue(functionalUtil.login(browser, user.getLogin(), user.getPassword(), true));

		/* upload file artefact */
		Assert.assertTrue(functionalEportfolioUtil.uploadFileArtefact(browser, FILE_ARTEFACT_BINDER, FILE_ARTEFACT_PAGE, FILE_ARTEFACT_STRUCTURE,
				FunctionalArtefactTest.class.getResource(FILE_ARTEFACT_PATH).toURI(),
				FILE_ARTEFACT_TITLE, FILE_ARTEFACT_DESCRIPTION,
				FILE_ARTEFACT_TAGS));
	}

	@Test
	@RunAsClient
	public void checkCreateLearningJournal(){
		/* login for test setup */
		Assert.assertTrue(functionalUtil.login(browser, user.getLogin(), user.getPassword(), true));

		/* create learning journal */
		Assert.assertTrue(functionalEportfolioUtil.createLearningJournal(browser, LEARNING_JOURNAL_BINDER, LEARNING_JOURNAL_PAGE, LEARNING_JOURNAL_STRUCTURE,
				LEARNING_JOURNAL_TITLE, LEARNING_JOURNAL_DESCRIPTION,
				LEARNING_JOURNAL_TAGS));

		System.out.println();
	}

	/**
	 * Description:<br/>
	 * Helper classes to verify interactions with openolat.
	 * 
	 * @author jkraehemann, joel.kraehemann@frentix.com, frentix.com
	 */
	class Binder{
		String name;
		String description;
		List<Page> page = new ArrayList<Page>();
		boolean ignore = true;

		Binder(String name, String description){
			this.name = name;
			this.description = description;
		}
		
		class Page{
			String name;
			String description;
			List child = new ArrayList();
			boolean ignore = true;
			
			Page(String name, String description){
				this.name = name;
				this.description = description;
			}
			
			class Structure{
				String name;
				String description;
				List<Artefact> child = new ArrayList<Artefact>();
				boolean ignore = true;
				
				Structure(String name, String description){
					this.name = name;
					this.description = description;
				}
			}

			class Artefact{
				String name;
				String description;
				String[] tags;
				Object content; /* in general a URL but for text artefact a String */
				boolean ignore = true;
				
				Artefact(String name, String description, String[] tags, Object content){
					this.name = name;
					this.description = description;
					this.tags = tags;
					this.content = content;
				}
			}
		}
	}
}
