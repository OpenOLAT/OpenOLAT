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
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.olat.modules.fo.portfolio.ForumArtefact;
import org.olat.modules.wiki.portfolio.WikiArtefact;
import org.olat.portfolio.FunctionalArtefactTest.Binder.Page;
import org.olat.portfolio.FunctionalArtefactTest.Binder.Page.Artefact;
import org.olat.portfolio.FunctionalArtefactTest.Binder.Page.BlogArtefact;
import org.olat.portfolio.FunctionalArtefactTest.Binder.Page.JournalArtefact;
import org.olat.portfolio.FunctionalArtefactTest.Binder.Page.TextArtefact;
import org.olat.portfolio.model.artefacts.FileArtefact;
import org.olat.restapi.support.vo.CourseVO;
import org.olat.restapi.support.vo.RepositoryEntryVO;
import org.olat.test.ArquillianDeployments;
import org.olat.user.restapi.UserVO;
import org.olat.util.FunctionalCourseUtil;
import org.olat.util.FunctionalEPortfolioUtil;
import org.olat.util.FunctionalEPortfolioUtil.ArtefactAlias;
import org.olat.util.FunctionalUtil.WaitLimitAttribute;
import org.olat.util.FunctionalUtil.WaitForContentFlag;
import org.olat.util.FunctionalHomeSiteUtil;
import org.olat.util.FunctionalRepositorySiteUtil;
import org.olat.util.FunctionalUtil;
import org.olat.util.FunctionalVOUtil;

import com.thoughtworks.selenium.DefaultSelenium;
import com.thoughtworks.selenium.Selenium;

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
	public final static String BLOG_POST_CONTENT = "Operating Systems: Design and Implementation (by Andrew S. Tanenbaum)";
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

	public final static String TEXT_ARTEFACT_CREATED_WITHIN_BINDER_CONTENT = "1. Two threads\n----------------\n - Keep in mind to sync two threads you need in each thread a conditional lock and method call to wake up the other thread\n";
	public final static String TEXT_ARTEFACT_CREATED_WITHIN_BINDER_TITLE = "syncing threads";
	public final static String TEXT_ARTEFACT_CREATED_WITHIN_BINDER_DESCRIPTION = "Notes on using conditional locks.";
	public final static String[] TEXT_ARTEFACT_CREATED_WITHIN_BINDER_TAGS = {"programming", "threads", "thread safety", "conditional lock"};
	public final static String TEXT_ARTEFACT_CREATED_WITHIN_BINDER_BINDER = BINDER_PROGRAMMING_THEORIE;
	public final static String TEXT_ARTEFACT_CREATED_WITHIN_BINDER_PAGE = "thread safety";
	public final static String TEXT_ARTEFACT_CREATED_WITHIN_BINDER_STRUCTURE = "issue 4";
	
	public final static String FILE_ARTEFACT_CREATED_WITHIN_BINDER_PATH = "/org/olat/portfolio/syncing_threads.c";
	public final static String FILE_ARTEFACT_CREATED_WITHIN_BINDER_TITLE = "conditional locks";
	public final static String FILE_ARTEFACT_CREATED_WITHIN_BINDER_DESCRIPTION = "Syncing two posix threads using conditional locks";
	public final static String[] FILE_ARTEFACT_CREATED_WITHIN_BINDER_TAGS = {"programming", "c", "mutex", "thread", "condition", "signal", "wait"};
	public final static String FILE_ARTEFACT_CREATED_WITHIN_BINDER_BINDER = BINDER_PROGRAMMING_SAMPLES;
	public final static String FILE_ARTEFACT_CREATED_WITHIN_BINDER_PAGE = "thread safety";
	public final static String FILE_ARTEFACT_CREATED_WITHIN_BINDER_STRUCTURE = "issue 5";
	
	public final static String LEARNING_JOURNAL_CREATED_WITHIN_BINDER_TITLE = "Threading Journal";
	public final static String LEARNING_JOURNAL_CREATED_WITHIN_BINDER_DESCRIPTION = "My experiences with thread safety";
	public final static String[] LEARNING_JOURNAL_CREATED_WITHIN_BINDER_TAGS = {"programming", "threads", "thread safety"};
	public final static String LEARNING_JOURNAL_CREATED_WITHIN_BINDER_BINDER = BINDER_PROGRAMMING_THEORIE;
	public final static String LEARNING_JOURNAL_CREATED_WITHIN_BINDER_PAGE = "thread safety";
	public final static String LEARNING_JOURNAL_CREATED_WITHIN_BINDER_STRUCTURE = null;
	
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
			functionalHomeSiteUtil = functionalUtil.getFunctionalHomeSiteUtil();

			functionalRepositorySiteUtil = functionalUtil.getFunctionalRepositorySiteUtil();
			functionalCourseUtil = functionalRepositorySiteUtil.getFunctionalCourseUtil();
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
			Class<?> artefactClass, String artefactName, String artefactDescription, String[] artefactTags, String[] artefactContent){
		Binder binder = findBinderByName(this.map, binderName);
		
		if(binder == null){
			binder = new Binder(binderName, binderDescription);
			this.map.add(binder);
		}
		
		Binder.Page page = findPageByName(binder.page, pageName);
		
		if(page == null){
			page = binder.new Page(pageName, pageDescription);
			page.parent = binder;
			binder.page.add(page);
		}
		
		Binder.Page.Structure structure = findStructureByName(page.child, structureName);
		
		if(structure == null && structureName != null){
			structure = page.new Structure(structureName, structureDescription);
			structure.parent = page;
			page.child.add(structure);
		}
		
		Binder.Page.Artefact artefact = findArtefactByName(page.child, artefactName);
		
		if(artefact == null && structure != null){
			artefact = findArtefactByName(structure.child, artefactName);
		}
		
		if(artefact == null){
			artefact = ArtefactFactory.newArtefact(page, artefactClass, artefactName, artefactDescription, artefactTags, artefactContent);
			
			if(structure != null){
				artefact.parent = structure;
				structure.child.add(artefact);
			}else{
				artefact.parent = page;
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
			if(name.equals(current.binderName)){
				return(current);
			}
		}	
		
		return(null);
	}
	
	Binder.Page findPageByName(List<Binder.Page> page, String name){
		if(name == null)
			return(null);
		
		for(Binder.Page current: page){
			if(name.equals(current.pageName)){
				return(current);
			}
		}	
		
		return(null);
	}
	
	Binder.Page.Artefact findArtefactByName(List<?> list, String name){
		if(name == null)
			return(null);
		
		for(Object current: list){
			if(current instanceof Binder.Page.Artefact && name.equals(((Binder.Page.Artefact) current).artefactName)){
				return((Binder.Page.Artefact) current);
			}
		}	
		
		return(null);
	}
	
	Binder.Page.Structure findStructureByName(List<?> list, String name){
		if(name == null)
			return(null);
		
		for(Object current: list){
			if(current instanceof Binder.Page.Structure && name.equals(((Binder.Page.Structure) current).structureName)){
				return((Binder.Page.Structure) current);
			}
		}	
		
		return(null);
	}
	
	/**
	 * verifies the the tags and content
	 * 
	 * @param artefact
	 * @return
	 */
	boolean checkArtefact(Binder.Page.Artefact artefact){
		if(artefact instanceof Binder.Page.JournalArtefact)
			return(true);
		
		if(artefact.parent instanceof Binder.Page.Structure){
			if(!functionalEportfolioUtil.openArtefact(browser,
					((Binder)((Binder.Page)((Binder.Page.Structure) artefact.parent).parent).parent).binderName,
					((Binder.Page)((Binder.Page.Structure) artefact.parent).parent).pageName, 
					((Binder.Page.Structure) artefact.parent).structureName,
					artefact.artefactName)){
				return(false);
			}
		}else{
			if(!functionalEportfolioUtil.openArtefact(browser,
					((Binder)((Binder.Page)((Binder.Page.Structure) artefact.parent).parent).parent).binderName,
					((Binder.Page)((Binder.Page.Structure) artefact.parent).parent).pageName, 
					null,
					artefact.artefactName)){
				return(false);
			}
		}
		
		/* check tags */
		StringBuffer selectorBuffer = new StringBuffer();
		
		selectorBuffer.append("xpath=//div[contains(@class, '")
		.append(functionalEportfolioUtil.getArtefactCss())
		.append("')]//div[contains(@class, '")
		.append(functionalEportfolioUtil.getTagIconCss())
		.append("')]//div[");
		
		boolean hasPrev = false;
		
		for(String currentTag: artefact.tags){
			if(hasPrev){
				selectorBuffer.append(" and ");
			}else{
				hasPrev = true;
			}
			
			selectorBuffer.append("contains(text(), '")
			.append(currentTag)
			.append("')");
		}
		
		selectorBuffer.append("]");
		
		if(!browser.isElementPresent(selectorBuffer.toString())){
			return(false);
		}
		
		if(!functionalEportfolioUtil.closeArtefact(browser)){
			return(false);
		}
		
		/* compare business paths */
		//TODO:JK: uncomment this code
		/* business paths aren't reliable, yet */
//		if(!functionalEportfolioUtil.clickArtefactContent(browser)){
//			return(false);
//		}
//		
//		String path0 = functionalUtil.currentBusinessPath(browser);
//		
//		if(!artefact.open(browser, deploymentUrl)){
//			return(false);
//		}
//		
//		String path1 = functionalUtil.currentBusinessPath(browser);
//		
//		if(path0 == null || !path0.equals(path1)){
//			return(false);
//		}
		
		/* verify content */
		artefact.open(browser, deploymentUrl);

		String currentContent = null;

		while((currentContent = artefact.nextContent()) != null){
			if(!functionalUtil.waitForPageToLoadContent(browser, null,
					currentContent,
					WaitLimitAttribute.VERY_SAVE, null,
					false)){
				return(false);
			}
		}
		
		return(true);
	}
	
	/**
	 * verifies the specified binder
	 * 
	 * @param binder
	 * @return
	 */
	boolean checkMap(Binder binder){
		if(!functionalEportfolioUtil.openBinder(browser, binder.binderName)){
			return(false);
		}
		
		/* check binder structure */
		for(Binder.Page currentPage: binder.page){
			if(currentPage.ignore){
				continue;
			}
			
			/* check page */
			if(!functionalEportfolioUtil.pageExists(browser, binder.binderName, currentPage.pageName)){
				return(false);
			}
			
			//TODO:JK: doesn't check the page's description
			
			/* traverse tree */
			for(Object currentPageChild: currentPage.child){
				if(currentPageChild instanceof Binder.Page.Artefact){
					Binder.Page.Artefact currentArtefact = (Binder.Page.Artefact) currentPageChild;
					
					if(currentArtefact.ignore){
						continue;
					}
					
					/* check artefact */
					if(!functionalEportfolioUtil.artefactExists(browser, binder.binderName, currentPage.pageName, null, currentArtefact.artefactName)){
						return(false);
					}

					//TODO:JK: doesn't check the artefact's description
				}else{
					Binder.Page.Structure currentStructure = (Binder.Page.Structure) currentPageChild;
			
					if(currentStructure.ignore){
						continue;
					}
					
					/*  check structure */
					if(!functionalEportfolioUtil.structureExists(browser, binder.binderName, currentPage.pageName, currentStructure.structureName)){
						return(false);
					}
					
					//TODO:JK: doesn't check the structure's description
					
					/* traverse tree */
					for(Binder.Page.Artefact currentArtefact: currentStructure.child){
						if(currentArtefact.ignore){
							continue;
						}
						
						/* check artefact */
						if(!functionalEportfolioUtil.artefactExists(browser, binder.binderName, currentPage.pageName, currentStructure.structureName, currentArtefact.artefactName)){
							return(false);
						}
						
						//TODO:JK: doesn't check the artefact's description
					}
				}
			}	
		}
		
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
				ForumArtefact.class, FORUM_ARTEFACT_TITLE, FORUM_ARTEFACT_DESCRIPTION, FORUM_TAGS, null);
		
		Binder binder = (Binder) retval[0];
		Binder.Page page = (Binder.Page) retval[1];
		Binder.Page.Structure structure = (Binder.Page.Structure) retval[2];
		Binder.Page.Artefact artefact = (Binder.Page.Artefact) retval[3];
		((Binder.Page.ForumArtefact) artefact).postTitle = FORUM_POST_TITLE;
		((Binder.Page.ForumArtefact) artefact).postContent = FORUM_POST_MESSAGE;
		
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
		artefact.businessPath = functionalUtil.currentBusinessPath(browser);
		
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

		artefact.ignore = false;
		
		/* verify */
		Assert.assertTrue(checkArtefact(artefact));
		Assert.assertTrue(checkMap(binder));
		
		functionalUtil.logout(browser);
	}

	@Test
	@RunAsClient
	public void checkCollectWikiArticle() throws URISyntaxException, IOException{
		/*
		 * Prepare for verification
		 */		
		Object[] retval = prepareVerification(WIKI_BINDER, null,
				WIKI_PAGE, null,
				WIKI_STRUCTURE, null,
				WikiArtefact.class, WIKI_ARTEFACT_TITLE, WIKI_ARTEFACT_DESCRIPTION, WIKI_TAGS, null);
		
		Binder binder = (Binder) retval[0];
		Binder.Page page = (Binder.Page) retval[1];
		Binder.Page.Structure structure = (Binder.Page.Structure) retval[2];
		Binder.Page.Artefact artefact = (Binder.Page.Artefact) retval[3];
		((Binder.Page.WikiArtefact) artefact).article = WIKI_ARTICLE_CONTENT;
		
		/*
		 * Test case
		 */
		/* import wiki via rest */
		RepositoryEntryVO vo = functionalVOUtil.importWiki(deploymentUrl);

		/* login for test setup */
		Assert.assertTrue(functionalUtil.login(browser, user.getLogin(), user.getPassword(), true));

		/* create binder, page or structure if necessary */
		Assert.assertTrue(functionalEportfolioUtil.createElements(browser, WIKI_BINDER, WIKI_PAGE, WIKI_STRUCTURE));

		/* create an article for the wiki */
		Assert.assertTrue(functionalCourseUtil.createWikiArticle(browser, vo.getKey(), WIKI_ARTICLE_PAGENAME, WIKI_ARTICLE_CONTENT));
		artefact.businessPath = functionalUtil.currentBusinessPath(browser);

		/* add artefact */
		Assert.assertTrue(functionalCourseUtil.addToEportfolio(browser, WIKI_BINDER, WIKI_PAGE, WIKI_STRUCTURE,
				WIKI_ARTEFACT_TITLE, WIKI_ARTEFACT_DESCRIPTION, WIKI_TAGS,
				functionalEportfolioUtil));
		
		/*
		 * Test for content and make assumptions if the changes were applied.
		 * Keep it simple use quick access with business paths.
		 */
		binder.ignore = false;
		
		page.ignore = false;
		
		structure.ignore = false;

		artefact.ignore = false;
		
		/* verify */
		Assert.assertTrue(checkArtefact(artefact));
		Assert.assertTrue(checkMap(binder));
		
		functionalUtil.logout(browser);
	}

	@Test
	@RunAsClient
	public void checkCollectBlogPost() throws URISyntaxException, IOException{
		/*
		 * Prepare for verification
		 */		
		Object[] retval = prepareVerification(BLOG_BINDER, null,
				BLOG_PAGE, null,
				BLOG_STRUCTURE, null,
				BlogArtefact.class, BLOG_ARTEFACT_TITLE, BLOG_ARTEFACT_DESCRIPTION, BLOG_TAGS, null);
		
		Binder binder = (Binder) retval[0];
		Binder.Page page = (Binder.Page) retval[1];
		Binder.Page.Structure structure = (Binder.Page.Structure) retval[2];
		Binder.Page.Artefact artefact = (Binder.Page.Artefact) retval[3];
		((Binder.Page.BlogArtefact) artefact).postTitle = BLOG_POST_TITLE;
		((Binder.Page.BlogArtefact) artefact).postContent = BLOG_POST_CONTENT;
		
		/*
		 * Test case
		 */
		/* deploy course with REST */
		CourseVO course = functionalVOUtil.importCourseIncludingBlog(deploymentUrl);	

		/* login for test setup */
		Assert.assertTrue(functionalUtil.login(browser, user.getLogin(), user.getPassword(), true));

		/* create binder, page or structure if necessary */
		Assert.assertTrue(functionalEportfolioUtil.createElements(browser, BLOG_BINDER, BLOG_PAGE, BLOG_STRUCTURE));

		/* blog */
		Assert.assertTrue(functionalCourseUtil.createBlogEntry(browser, course.getRepoEntryKey(), 0,
				BLOG_POST_TITLE, BLOG_POST_DESCRIPTION, BLOG_POST_CONTENT));
		artefact.businessPath = functionalUtil.currentBusinessPath(browser);

		/* add artefact */
		Assert.assertTrue(functionalCourseUtil.addToEportfolio(browser, BLOG_BINDER, BLOG_PAGE, BLOG_STRUCTURE,
				BLOG_ARTEFACT_TITLE, BLOG_ARTEFACT_DESCRIPTION, BLOG_TAGS,
				functionalEportfolioUtil));
		
		/*
		 * Test for content and make assumptions if the changes were applied.
		 * Keep it simple use quick access with business paths.
		 */
		binder.ignore = false;
		
		page.ignore = false;
		
		structure.ignore = false;

		artefact.ignore = false;
		
		/* verify */
		Assert.assertTrue(checkArtefact(artefact));
		Assert.assertTrue(checkMap(binder));
		
		functionalUtil.logout(browser);
	}

	@Test
	@RunAsClient
	public void checkAddTextArtefact(){
		/*
		 * Prepare for verification
		 */		
		Object[] retval = prepareVerification(TEXT_ARTEFACT_BINDER, null,
				TEXT_ARTEFACT_PAGE, null,
				TEXT_ARTEFACT_STRUCTURE, null,
				TextArtefact.class, TEXT_ARTEFACT_TITLE, TEXT_ARTEFACT_DESCRIPTION, TEXT_ARTEFACT_TAGS, null);
		
		Binder binder = (Binder) retval[0];
		Binder.Page page = (Binder.Page) retval[1];
		Binder.Page.Structure structure = (Binder.Page.Structure) retval[2];
		Binder.Page.Artefact artefact = (Binder.Page.Artefact) retval[3];
		
		/*
		 * Test case
		 */
		/* login for test setup */
		Assert.assertTrue(functionalUtil.login(browser, user.getLogin(), user.getPassword(), true));

		/* create binder, page or structure if necessary */
		Assert.assertTrue(functionalEportfolioUtil.createElements(browser, TEXT_ARTEFACT_BINDER, TEXT_ARTEFACT_PAGE, TEXT_ARTEFACT_STRUCTURE));
		
		/* add text artefact */
		Assert.assertTrue(functionalEportfolioUtil.addTextArtefact(browser, TEXT_ARTEFACT_BINDER, TEXT_ARTEFACT_PAGE, TEXT_ARTEFACT_STRUCTURE,
				TEXT_ARTEFACT_CONTENT,
				TEXT_ARTEFACT_TITLE, TEXT_ARTEFACT_DESCRIPTION,
				TEXT_ARTEFACT_TAGS));
		
		/*
		 * Test for content and make assumptions if the changes were applied.
		 * Keep it simple use quick access with business paths.
		 */
		binder.ignore = false;
		
		page.ignore = false;
		
		structure.ignore = false;

		artefact.ignore = false;
		
		/* verify */
		Assert.assertTrue(checkArtefact(artefact));
		Assert.assertTrue(checkMap(binder));

		functionalUtil.logout(browser);
	}

	@Test
	@RunAsClient
	public void checkUploadFileArtefact() throws URISyntaxException, MalformedURLException{
		/*
		 * Prepare for verification
		 */		
		Object[] retval = prepareVerification(FILE_ARTEFACT_BINDER, null,
				FILE_ARTEFACT_PAGE, null,
				FILE_ARTEFACT_STRUCTURE, null,
				FileArtefact.class, FILE_ARTEFACT_TITLE, FILE_ARTEFACT_DESCRIPTION, FILE_ARTEFACT_TAGS, null);
		
		Binder binder = (Binder) retval[0];
		Binder.Page page = (Binder.Page) retval[1];
		Binder.Page.Structure structure = (Binder.Page.Structure) retval[2];
		Binder.Page.Artefact artefact = (Binder.Page.Artefact) retval[3];
		
		/*
		 * Test case
		 */
		/* login for test setup */
		Assert.assertTrue(functionalUtil.login(browser, user.getLogin(), user.getPassword(), true));
		
		/* create binder, page or structure if necessary */
		Assert.assertTrue(functionalEportfolioUtil.createElements(browser, FILE_ARTEFACT_BINDER, FILE_ARTEFACT_PAGE, FILE_ARTEFACT_STRUCTURE));
		
		/* upload file artefact */
		Assert.assertTrue(functionalEportfolioUtil.uploadFileArtefact(browser, FILE_ARTEFACT_BINDER, FILE_ARTEFACT_PAGE, FILE_ARTEFACT_STRUCTURE,
				FunctionalArtefactTest.class.getResource(FILE_ARTEFACT_PATH).toURI(),
				FILE_ARTEFACT_TITLE, FILE_ARTEFACT_DESCRIPTION,
				FILE_ARTEFACT_TAGS));
		
		/*
		 * Test for content and make assumptions if the changes were applied.
		 * Keep it simple use quick access with business paths.
		 */
		binder.ignore = false;
		
		page.ignore = false;
		
		structure.ignore = false;

		artefact.ignore = false;
		
		/* verify */
		Assert.assertTrue(checkArtefact(artefact));
		Assert.assertTrue(checkMap(binder));
		
		functionalUtil.logout(browser);
	}

	@Test
	@RunAsClient
	public void checkCreateLearningJournal(){
		/*
		 * Prepare for verification
		 */		
		Object[] retval = prepareVerification(LEARNING_JOURNAL_BINDER, null,
				LEARNING_JOURNAL_PAGE, null,
				LEARNING_JOURNAL_STRUCTURE, null,
				JournalArtefact.class, LEARNING_JOURNAL_TITLE, LEARNING_JOURNAL_DESCRIPTION, LEARNING_JOURNAL_TAGS, null);
		
		Binder binder = (Binder) retval[0];
		Binder.Page page = (Binder.Page) retval[1];
		Binder.Page.Structure structure = (Binder.Page.Structure) retval[2];
		Binder.Page.Artefact artefact = (Binder.Page.Artefact) retval[3];
		
		/*
		 * Test case 
		 */
		/* login for test setup */
		Assert.assertTrue(functionalUtil.login(browser, user.getLogin(), user.getPassword(), true));

		/* create binder, page or structure if necessary */
		Assert.assertTrue(functionalEportfolioUtil.createElements(browser, LEARNING_JOURNAL_BINDER, LEARNING_JOURNAL_PAGE, LEARNING_JOURNAL_STRUCTURE));
		
		/* create learning journal */
		Assert.assertTrue(functionalEportfolioUtil.createLearningJournal(browser, LEARNING_JOURNAL_BINDER, LEARNING_JOURNAL_PAGE, LEARNING_JOURNAL_STRUCTURE,
				LEARNING_JOURNAL_TITLE, LEARNING_JOURNAL_DESCRIPTION,
				LEARNING_JOURNAL_TAGS));
		
		/*
		 * Test for content and make assumptions if the changes were applied.
		 * Keep it simple use quick access with business paths.
		 */
		binder.ignore = false;
		
		page.ignore = false;
		
		structure.ignore = false;

		artefact.ignore = false;
		
		/* verify */
		Assert.assertTrue(checkArtefact(artefact));
		//FIXME:JK: analyse why it always fails
		Assert.assertTrue(checkMap(binder));

		functionalUtil.logout(browser);
	}

	@Test
	@RunAsClient
	public void checkAddTextArtefactWithinBinder() throws MalformedURLException{
		/*
		 * Prepare for verification
		 */		
		Object[] retval = prepareVerification(TEXT_ARTEFACT_CREATED_WITHIN_BINDER_BINDER, null,
				TEXT_ARTEFACT_CREATED_WITHIN_BINDER_PAGE, null,
				TEXT_ARTEFACT_CREATED_WITHIN_BINDER_STRUCTURE, null,
				TextArtefact.class,
				TEXT_ARTEFACT_CREATED_WITHIN_BINDER_TITLE, TEXT_ARTEFACT_CREATED_WITHIN_BINDER_DESCRIPTION, TEXT_ARTEFACT_CREATED_WITHIN_BINDER_TAGS,
				null);
		
		Binder binder = (Binder) retval[0];
		Binder.Page page = (Binder.Page) retval[1];
		Binder.Page.Structure structure = (Binder.Page.Structure) retval[2];
		Binder.Page.Artefact artefact = (Binder.Page.Artefact) retval[3];
		
		/*
		 * Test case
		 */
		/* login for test setup */
		Assert.assertTrue(functionalUtil.login(browser, user.getLogin(), user.getPassword(), true));

		/* create binder, page or structure if necessary */
		Assert.assertTrue(functionalEportfolioUtil.createElements(browser,
				TEXT_ARTEFACT_CREATED_WITHIN_BINDER_BINDER, TEXT_ARTEFACT_CREATED_WITHIN_BINDER_PAGE, TEXT_ARTEFACT_CREATED_WITHIN_BINDER_STRUCTURE));
		
		/* add text artefact */
		Assert.assertTrue(functionalEportfolioUtil.createArtefact(browser,
				TEXT_ARTEFACT_CREATED_WITHIN_BINDER_BINDER, TEXT_ARTEFACT_CREATED_WITHIN_BINDER_PAGE, TEXT_ARTEFACT_CREATED_WITHIN_BINDER_STRUCTURE,
				ArtefactAlias.TEXT, TEXT_ARTEFACT_CREATED_WITHIN_BINDER_CONTENT,
				TEXT_ARTEFACT_CREATED_WITHIN_BINDER_TITLE, TEXT_ARTEFACT_CREATED_WITHIN_BINDER_DESCRIPTION,
				TEXT_ARTEFACT_CREATED_WITHIN_BINDER_TAGS));
		
		/*
		 * Test for content and make assumptions if the changes were applied.
		 * Keep it simple use quick access with business paths.
		 */
		binder.ignore = false;
		
		page.ignore = false;
		
		structure.ignore = false;

		artefact.ignore = false;
		
		/* verify */
		Assert.assertTrue(checkArtefact(artefact));
		Assert.assertTrue(checkMap(binder));
		
		functionalUtil.logout(browser);
	}
	
	@Test
	@RunAsClient
	public void checkUploadFileArtefactWithinBinder() throws MalformedURLException, URISyntaxException{
		/*
		 * Prepare for verification
		 */		
		Object[] retval = prepareVerification(FILE_ARTEFACT_CREATED_WITHIN_BINDER_BINDER, null,
				FILE_ARTEFACT_CREATED_WITHIN_BINDER_PAGE, null,
				FILE_ARTEFACT_CREATED_WITHIN_BINDER_STRUCTURE, null,
				FileArtefact.class,
				FILE_ARTEFACT_CREATED_WITHIN_BINDER_TITLE, FILE_ARTEFACT_CREATED_WITHIN_BINDER_DESCRIPTION, FILE_ARTEFACT_CREATED_WITHIN_BINDER_TAGS,
				null);
		
		Binder binder = (Binder) retval[0];
		Binder.Page page = (Binder.Page) retval[1];
		Binder.Page.Structure structure = (Binder.Page.Structure) retval[2];
		Binder.Page.Artefact artefact = (Binder.Page.Artefact) retval[3];
		
		/*
		 * Test case
		 */
		/* login for test setup */
		Assert.assertTrue(functionalUtil.login(browser, user.getLogin(), user.getPassword(), true));
		
		/* create binder, page or structure if necessary */
		Assert.assertTrue(functionalEportfolioUtil.createElements(browser,
				FILE_ARTEFACT_CREATED_WITHIN_BINDER_BINDER, FILE_ARTEFACT_CREATED_WITHIN_BINDER_PAGE, FILE_ARTEFACT_CREATED_WITHIN_BINDER_STRUCTURE));
		
		/* upload file artefact */
		Assert.assertTrue(functionalEportfolioUtil.createArtefact(browser,
				FILE_ARTEFACT_CREATED_WITHIN_BINDER_BINDER, FILE_ARTEFACT_CREATED_WITHIN_BINDER_PAGE, FILE_ARTEFACT_CREATED_WITHIN_BINDER_STRUCTURE,
				ArtefactAlias.FILE, FunctionalArtefactTest.class.getResource(FILE_ARTEFACT_CREATED_WITHIN_BINDER_PATH).toURI(),
				FILE_ARTEFACT_CREATED_WITHIN_BINDER_TITLE, FILE_ARTEFACT_CREATED_WITHIN_BINDER_DESCRIPTION,
				FILE_ARTEFACT_CREATED_WITHIN_BINDER_TAGS));
		
		/*
		 * Test for content and make assumptions if the changes were applied.
		 * Keep it simple use quick access with business paths.
		 */
		binder.ignore = false;
		
		page.ignore = false;
		
		structure.ignore = false;

		artefact.ignore = false;
		
		/* verify */
		Assert.assertTrue(checkArtefact(artefact));
		Assert.assertTrue(checkMap(binder));
		
		functionalUtil.logout(browser);
	}
	
	@Test
	@RunAsClient
	public void checkCreateLearningJournalWithinBinder() throws MalformedURLException{
		/*
		 * Prepare for verification
		 */		
		Object[] retval = prepareVerification(LEARNING_JOURNAL_CREATED_WITHIN_BINDER_BINDER, null,
				LEARNING_JOURNAL_CREATED_WITHIN_BINDER_PAGE, null,
				LEARNING_JOURNAL_CREATED_WITHIN_BINDER_STRUCTURE, null,
				JournalArtefact.class,
				LEARNING_JOURNAL_CREATED_WITHIN_BINDER_TITLE, LEARNING_JOURNAL_CREATED_WITHIN_BINDER_DESCRIPTION, LEARNING_JOURNAL_CREATED_WITHIN_BINDER_TAGS,
				null);
		
		Binder binder = (Binder) retval[0];
		Binder.Page page = (Binder.Page) retval[1];
		Binder.Page.Structure structure = (Binder.Page.Structure) retval[2];
		Binder.Page.Artefact artefact = (Binder.Page.Artefact) retval[3];
		
		/*
		 * Test case 
		 */
		/* login for test setup */
		Assert.assertTrue(functionalUtil.login(browser, user.getLogin(), user.getPassword(), true));

		/* create binder, page or structure if necessary */
		Assert.assertTrue(functionalEportfolioUtil.createElements(browser,
				LEARNING_JOURNAL_CREATED_WITHIN_BINDER_BINDER, LEARNING_JOURNAL_CREATED_WITHIN_BINDER_PAGE, LEARNING_JOURNAL_CREATED_WITHIN_BINDER_STRUCTURE));
		
		/* create learning journal */
		Assert.assertTrue(functionalEportfolioUtil.createArtefact(browser,
				LEARNING_JOURNAL_CREATED_WITHIN_BINDER_BINDER, LEARNING_JOURNAL_CREATED_WITHIN_BINDER_PAGE, LEARNING_JOURNAL_CREATED_WITHIN_BINDER_STRUCTURE,
				ArtefactAlias.LEARNING_JOURNAL, null,
				LEARNING_JOURNAL_CREATED_WITHIN_BINDER_TITLE, LEARNING_JOURNAL_CREATED_WITHIN_BINDER_DESCRIPTION, LEARNING_JOURNAL_CREATED_WITHIN_BINDER_TAGS));
		
		/*
		 * Test for content and make assumptions if the changes were applied.
		 * Keep it simple use quick access with business paths.
		 */
		binder.ignore = false;
		
		page.ignore = false;

		artefact.ignore = false;
		
		/* verify */
		Assert.assertTrue(checkArtefact(artefact));
		//FIXME:JK: analyse why it always fails
		Assert.assertTrue(checkMap(binder));
		
		functionalUtil.logout(browser);
	}
	
	/**
	 * Description:<br/>
	 * Helper classes to verify interactions with openolat.
	 * 
	 * @author jkraehemann, joel.kraehemann@frentix.com, frentix.com
	 */
	class Binder{
		String binderName;
		String description;
		List<Page> page = new ArrayList<Page>();
		boolean ignore = true;

		Binder(String name, String description){
			this.binderName = name;
			this.description = description;
		}
		
		class Page{
			Object parent = null;
			String pageName;
			String description;
			List child = new ArrayList();
			boolean ignore = true;
			
			Page(String name, String description){
				this.pageName = name;
				this.description = description;
			}
			
			class Structure{
				Object parent = null;
				String structureName;
				String description;
				List<Artefact> child = new ArrayList<Artefact>();
				boolean ignore = true;
				
				Structure(String name, String description){
					this.structureName = name;
					this.description = description;
				}
			}

			abstract class Artefact{
				Object parent = null;
				String artefactName;
				String description;
				String[] tags;
				String[] content;
				String businessPath;
				boolean ignore = true;
				
				Artefact(String name, String description, String[] tags, String[] content){
					this.artefactName = name;
					this.description = description;
					this.tags = tags;
					this.content = content;
				}
				
				boolean open(Selenium browser, URL deploymentUrl){
					functionalUtil.openBusinessPath(browser, businessPath);
					
					return(true);
				}
				
				abstract String nextContent();
			}
			
			class ForumArtefact extends Artefact{
				String postTitle;
				String postContent;
				int nthContent = 0;
				
				ForumArtefact(String name, String description, String[] tags, String[] content) {
					super(name, description, tags, content);
				}
				
				String nextContent(){
					if(nthContent == 0){
						nthContent++;
						
						return(postTitle);
					}else if(nthContent == 1){
						nthContent = -1;
						
						return(postContent);
					}else{
						return(null);
					}
				}
			}
			
			class BlogArtefact extends Artefact{
				String postTitle;
				String postContent;
				int nthContent = 0;
				
				BlogArtefact(String name, String description, String[] tags, String[] content) {
					super(name, description, tags, content);
				}
				
				String nextContent(){
					if(nthContent == 0){
						nthContent++;
						
						return(postTitle);
					}else if(nthContent == 1){
						nthContent = -1;
						
						return(null);//(postContent);
					}else{
						return(null);
					}
				}
			}
			
			class WikiArtefact extends Artefact{
				String article;
				int prevLine = 0;
				boolean initial = true;
				boolean passed = false;
				
				WikiArtefact(String name, String description, String[] tags, String[] content) {
					super(name, description, tags, content);
				}
				
				String nextContent(){
					int prevLine = this.prevLine;
					
					if(passed){
						return(null);
					}
					
					this.prevLine = article.indexOf('\n', prevLine + 1);
					
					if(initial){
						initial = false;
					}else{
						prevLine += 1;
					}
					
					if(this.prevLine == -1){
						passed = true;
						this.prevLine = article.length();
					}
					
					return(stripWikiSyntax(article.substring(prevLine, this.prevLine)));
				}
				
				String stripWikiSyntax(String line){
					//TODO:JK: this method is kept very simple
					line = line.replaceAll("==", "");
					
					return(line);
				}
			}
			
			class TextArtefact extends Artefact{
				String content;
				boolean passed = false;
				
				TextArtefact(String name, String description, String[] tags, String[] content) {
					super(name, description, tags, content);
				}

				boolean open(Selenium browser, URL deploymentUrl){
					/* empty */
					
					return(true);
				}
				
				String nextContent(){
					if(!passed){
						passed = true;
						
						return(content);
					}else{
						return(null);
					}
				}
			}
			
			class FileArtefact extends Artefact{
				String content;
				boolean passed = false;
				
				FileArtefact(String name, String description, String[] tags, String[] content) {
					super(name, description, tags, content);
				}

				boolean open(Selenium browser, URL deploymentUrl){
					/* empty */
					
					return(true);
				}
				
				String nextContent(){
					if(!passed){
						return(content);
					}else{					
						return(null);
					}
				}
			}
			
			class JournalArtefact extends Artefact{
				JournalArtefact(String name, String description, String[] tags, String[] content) {
					super(name, description, tags, content);
				}

				boolean open(Selenium browser, URL deploymentUrl){
					return(functionalEportfolioUtil.openArtefact(browser, binderName, pageName, ((parent instanceof Structure) ? null: ((Structure) parent).structureName), artefactName));
				}
				
				String nextContent(){
					return(null);
				}
			}
		}
	}
}

class ArtefactFactory{
	static Artefact newArtefact(Page page, Class<?> artefactClass, String name, String description, String[] tags, String[] content){
		if(artefactClass == null){
			return(null);
		}
		
		if(artefactClass.equals(ForumArtefact.class)){
			return(page.new ForumArtefact(name, description, tags, content));
		}else if(artefactClass.equals(BlogArtefact.class)){
			return(page.new BlogArtefact(name, description, tags, content));
		}else if(artefactClass.equals(WikiArtefact.class)){
			return(page.new WikiArtefact(name, description, tags, content));
		}else if(artefactClass.equals(TextArtefact.class)){
			return(page.new TextArtefact(name, description, tags, content));
		}else if(artefactClass.equals(FileArtefact.class)){
			return(page.new FileArtefact(name, description, tags, content));
		}else if(artefactClass.equals(JournalArtefact.class)){
			return(page.new JournalArtefact(name, description, tags, content));
		}
		
		return(null);
	}
}
