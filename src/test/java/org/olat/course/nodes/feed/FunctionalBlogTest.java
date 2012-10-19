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
package org.olat.course.nodes.feed;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

import org.apache.commons.lang.ArrayUtils;
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
import org.olat.restapi.support.vo.CourseVO;
import org.olat.restapi.support.vo.RepositoryEntryVO;
import org.olat.test.ArquillianDeployments;
import org.olat.user.restapi.UserVO;
import org.olat.util.FunctionalAdministrationSiteUtil;
import org.olat.util.FunctionalCourseUtil;
import org.olat.util.FunctionalCourseUtil.BlogEdit;
import org.olat.util.FunctionalRepositorySiteUtil;
import org.olat.util.FunctionalUtil;
import org.olat.util.FunctionalUtil.OlatSite;
import org.olat.util.FunctionalUtil.WaitLimitAttribute;
import org.olat.util.FunctionalVOUtil;
import org.olat.util.FunctionalCourseUtil.CourseNodeAlias;
import org.olat.util.browser.Browser1;
import org.olat.util.browser.Browser2;
import org.olat.util.browser.Student1;
import org.olat.util.browser.Student2;
import org.olat.util.browser.Tutor1;

import com.thoughtworks.selenium.DefaultSelenium;
import com.thoughtworks.selenium.Selenium;

/**
 * 
 * @author jkraehemann, joel.kraehemann@frentix.com, frentix.com
 */
@RunWith(Arquillian.class)
public class FunctionalBlogTest {
	
	public final static String BLOG_SHORT_TITLE = "blog";
	public final static String BLOG_LONG_TITLE = "test blog";
	public final static String BLOG_DESCRIPTION = "blog";
	public final static String BLOG_FEED = "http://blogs.frentix.com/blogs/frentix/rss.xml";
	
	public final static String DELETE_BLOG_SHORT_TITLE = "blog";
	public final static String DELETE_BLOG_LONG_TITLE = "blog for removal";
	public final static String DELETE_BLOG_DESCRIPTION = "The first blog entry will be deleted";
	public final static String DELETE_BLOG_CONTENT = "You should be able to choose to create or feed from existing blog.";
	
	public final static String CONCURRENT_CLEAR_CACHE_BLOG_PATH = "/org/olat/course/nodes/feed/blog.zip";
	public final static String CONCURRENT_CLEAR_CACHE_BLOG_FILE_NAME = "blog.zip";
	public final static String CONCURRENT_CLEAR_CACHE_BLOG_RESOURCE_NAME = "Blog";
	public final static String CONCURRENT_CLEAR_CACHE_BLOG_DISPLAY_NAME = "Parallel Computing Blog";
	public final static String CONCURRENT_CLEAR_CACHE_BLOG_POST1_TITLE = "Conditional locks with Java";
	public final static String CONCURRENT_CLEAR_CACHE_BLOG_POST1_DESCRIPTION = "Advanced thread safety in Java.";
	public final static String CONCURRENT_CLEAR_CACHE_BLOG_POST1_CONTENT = "Please take a look at ReentrantLock class in JavaSE package java.util.concurrent.locks for further information.";
	public final static String CONCURRENT_CLEAR_CACHE_BLOG_POST2_TITLE = "Creating conditions";
	public final static String CONCURRENT_CLEAR_CACHE_BLOG_POST2_DESCRIPTION = "Wait until condition is fulfilled.";
	public final static String CONCURRENT_CLEAR_CACHE_BLOG_POST2_CONTENT = "With the ReentrantLock class you may create new conditions like following:<br>\n<code>\nfinal Lock lock = new ReentrantLock();<br>\nfinal Condition cond  = lock.newCondition()<br>\n</code>\n";
	
	public final static String CONCURRENT_RW_BLOG_SHORT_TITLE = "blog";
	public final static String CONCURRENT_RW_BLOG_LONG_TITLE = "blog cleared cache";
	public final static String CONCURRENT_RW_BLOG_DESCRIPTION = "During open blog cache will be cleared";
	public final static String CONCURRENT_RW_BLOG_CONTENT = "New openolat release is outstanding.";
	public final static String CONCURRENT_RW_BLOG_NEW_CONTENT = "New openolat release is outstanding and is comming soon.";
	
	@Deployment(testable = false)
	public static WebArchive createDeployment() {
		return ArquillianDeployments.createDeployment();
	}

	@Drone
	DefaultSelenium browser;
	
	@ArquillianResource
	URL deploymentUrl;

	static FunctionalUtil functionalUtil;
	static FunctionalRepositorySiteUtil functionalRepositorySiteUtil;
	static FunctionalCourseUtil functionalCourseUtil;
	static FunctionalAdministrationSiteUtil functionalAdministrationSiteUtil;
	static FunctionalVOUtil functionalVOUtil;

	static boolean initialized = false;
	
	@Before
	public void setup() throws IOException, URISyntaxException{
		if(!initialized){
			functionalUtil = new FunctionalUtil();
			functionalUtil.setDeploymentUrl(deploymentUrl.toString());

			functionalRepositorySiteUtil = functionalUtil.getFunctionalRepositorySiteUtil();
			functionalCourseUtil = functionalRepositorySiteUtil.getFunctionalCourseUtil();
			
			functionalAdministrationSiteUtil = functionalUtil.getFunctionalAdministrationSiteUtil();
			
			functionalVOUtil = new FunctionalVOUtil(functionalUtil.getUsername(), functionalUtil.getPassword());

			initialized = true;
		}
	}
	
	@Test
	@RunAsClient
	public void checkCreate() throws URISyntaxException, IOException{
		CourseVO course = functionalVOUtil.importEmptyCourse(deploymentUrl);
		
		/* login for test setup */
		Assert.assertTrue(functionalUtil.login(browser, functionalUtil.getUsername(), functionalUtil.getPassword(), true));
		
		/* create a empty blog */
		Assert.assertTrue(functionalRepositorySiteUtil.openCourse(browser, course.getRepoEntryKey()));
		Assert.assertTrue(functionalCourseUtil.openCourseEditor(browser));
		
		Assert.assertTrue(functionalCourseUtil.createCourseNode(browser, CourseNodeAlias.BLOG, BLOG_SHORT_TITLE, BLOG_LONG_TITLE, BLOG_DESCRIPTION, 0));
		Assert.assertTrue(functionalCourseUtil.createBlog(browser, BLOG_SHORT_TITLE, BLOG_DESCRIPTION));
		
		/* publish */
		Assert.assertTrue(functionalCourseUtil.publishEntireCourse(browser, null, null));
		
		/* import empty feed */
		Assert.assertTrue(functionalCourseUtil.open(browser, course.getRepoEntryKey(), 0));
		Assert.assertTrue(functionalCourseUtil.importBlogFeed(browser, BLOG_FEED));
		
		/* blog should be accessible */
		Assert.assertTrue(functionalCourseUtil.open(browser, course.getRepoEntryKey(), 0));
		

		Assert.assertTrue(functionalUtil.logout(browser));
	}
	
	@Ignore
	@Test
	@RunAsClient
	public void checkDelete(){
		//TODO:JK: implement me
	}
	
	@Test
	@RunAsClient
	public void checkConcurrentClearCache(@Drone @Tutor1 DefaultSelenium tutor0, @Drone @Student1 DefaultSelenium student0) throws IOException, URISyntaxException{
		/*
		 * Setup
		 */
		/* create author */
		int tutorCount = 1;
			
		final UserVO[] tutors = new UserVO[tutorCount];
		functionalVOUtil.createTestAuthors(deploymentUrl, tutorCount).toArray(tutors);
		
		/* create user */
		int userCount = 1;
			
		final UserVO[] students = new UserVO[userCount];
		functionalVOUtil.createTestUsers(deploymentUrl, userCount).toArray(students);
		
		/* create blog and set tutor as owner */
		RepositoryEntryVO repoEntry = functionalVOUtil.importBlog(deploymentUrl,
				CONCURRENT_CLEAR_CACHE_BLOG_PATH,
				CONCURRENT_CLEAR_CACHE_BLOG_FILE_NAME, CONCURRENT_CLEAR_CACHE_BLOG_RESOURCE_NAME, CONCURRENT_CLEAR_CACHE_BLOG_DISPLAY_NAME);
		
		functionalVOUtil.addOwnerToRepositoryEntry(deploymentUrl, repoEntry, tutors[0]);
		
		/*
		 * Create content and visit it.
		 */
		/* tutor creates a new post */
		Assert.assertTrue(functionalUtil.login(tutor0, tutors[0].getLogin(), tutors[0].getPassword(), true));
		Assert.assertTrue(functionalCourseUtil.openBlog(tutor0, repoEntry.getKey()));
		Assert.assertTrue(functionalCourseUtil.editBlogEntry(tutor0,
				CONCURRENT_CLEAR_CACHE_BLOG_POST1_TITLE, CONCURRENT_CLEAR_CACHE_BLOG_POST1_DESCRIPTION, CONCURRENT_CLEAR_CACHE_BLOG_POST1_CONTENT,
				-1, null));
		
		/* student visits content */
		Assert.assertTrue(functionalUtil.login(student0, students[0].getLogin(), students[0].getPassword(), true));
		Assert.assertTrue(functionalCourseUtil.openBlog(student0, repoEntry.getKey()));

		
		/*
		 * Clear cache and verify content.
		 */
		/* admin clears cache */
		Assert.assertTrue(functionalUtil.login(browser, functionalUtil.getUsername(), functionalUtil.getPassword(), true));
		Assert.assertTrue(functionalAdministrationSiteUtil.clearCache(browser,
				new String[]{
				"org.olat.core.util.cache.n.impl.svm.SingleVMCacher@org.olat.modules.webFeed.dispatching.Path_feed__0",
				"org.olat.core.util.cache.n.impl.svm.SingleVMCacher@org.olat.modules.webFeed.managers.FeedManagerImpl_feed__0"
				}
		));

		Assert.assertTrue(functionalUtil.logout(browser));
		
		/* tutor adds a new post */
		//Assert.assertTrue(functionalCourseUtil.backBlogEntry(tutor0));
		Assert.assertTrue(functionalCourseUtil.editBlogEntry(tutor0,
				CONCURRENT_CLEAR_CACHE_BLOG_POST2_TITLE, CONCURRENT_CLEAR_CACHE_BLOG_POST2_DESCRIPTION, CONCURRENT_CLEAR_CACHE_BLOG_POST2_CONTENT,
				-1, null));
		
		/* student verifies title - month */
		functionalUtil.idle(student0);
		
		String[] titles = {
				CONCURRENT_CLEAR_CACHE_BLOG_POST1_TITLE,
				CONCURRENT_CLEAR_CACHE_BLOG_POST2_TITLE
		};
		
		StringBuffer selectorBuffer = new StringBuffer();
		
		selectorBuffer.append("//ul[contains(@class, '")
		.append(functionalCourseUtil.getBlogMonthCss())
		.append("')]//li//a");
		
		int iStop = student0.getXpathCount(selectorBuffer.toString()).intValue();
		
		boolean[] foundTitlesInMonth = new boolean[titles.length];
		Arrays.fill(foundTitlesInMonth, false);
		
		
		for(int i = 0; i < iStop; i++){
			functionalUtil.idle(student0);
			
			/* click month */
			StringBuffer currentBuffer = new StringBuffer();
			
			currentBuffer.append("xpath=(")
			.append(selectorBuffer)
			.append(")[")
			.append(i + 1)
			.append(']');
			
			functionalUtil.waitForPageToLoadElement(student0, currentBuffer.toString());
			student0.click(currentBuffer.toString());
			
			functionalUtil.idle(student0);
			
			/* it should be visible somewhere */
			int j = 0;
			
			for(String currentTitle: titles){
				if(student0.isTextPresent(currentTitle)){
					foundTitlesInMonth[j] = true;
					break;
				}
				
				j++;
			}
		}
		
		Assert.assertFalse(ArrayUtils.contains(foundTitlesInMonth, false));
		
		/* student verifies title - year */
		functionalUtil.idle(student0);
		
		selectorBuffer = new StringBuffer();
		
		selectorBuffer.append("//div//a[contains(@class, '")
		.append(functionalCourseUtil.getBlogYearCss())
		.append("')]");
		
		iStop = student0.getXpathCount(selectorBuffer.toString()).intValue();
		
		boolean[] foundTitlesInYear = new boolean[titles.length];
		Arrays.fill(foundTitlesInYear, false);
		
		for(int i = 0; i < iStop; i++){
			functionalUtil.idle(student0);
			
			/* click year */
			StringBuffer currentBuffer = new StringBuffer();
			
			currentBuffer.append("xpath=(")
			.append(selectorBuffer)
			.append(")[")
			.append(i + 1)
			.append(']');
			
			student0.click(currentBuffer.toString());
			
			functionalUtil.idle(student0);

			/* it should be visible somewhere */
			int j = 0;
			
			for(String currentTitle: titles){
				if(student0.isTextPresent(currentTitle)){
					foundTitlesInMonth[j] = true;
					break;
				}
				
				j++;
			}
		}
		
		Assert.assertFalse(ArrayUtils.contains(foundTitlesInYear, false));
		
		/* logout */
		Assert.assertTrue(functionalUtil.logout(tutor0));
		Assert.assertTrue(functionalUtil.logout(student0));
	}
	
	@Test
	@RunAsClient
	public void checkConcurrentRW(@Drone @Student1 DefaultSelenium student0, @Drone @Student2 DefaultSelenium student1) throws IOException, URISyntaxException, InterruptedException{
		/*
		 * Setup
		 */
		int userCount = 2;
			
		final UserVO[] users = new UserVO[userCount];
		functionalVOUtil.createTestUsers(deploymentUrl, userCount).toArray(users);
		
		final CourseVO course = functionalVOUtil.importEmptyCourse(deploymentUrl);
		
		/* create blog */
		Assert.assertTrue(functionalUtil.login(browser, functionalUtil.getUsername(), functionalUtil.getPassword(), true));
		
		Assert.assertTrue(functionalRepositorySiteUtil.openCourse(browser, course.getRepoEntryKey()));
		Assert.assertTrue(functionalCourseUtil.openCourseEditor(browser));
		
		Assert.assertTrue(functionalCourseUtil.createCourseNode(browser, CourseNodeAlias.BLOG, BLOG_SHORT_TITLE, BLOG_LONG_TITLE, BLOG_DESCRIPTION, 0));
		Assert.assertTrue(functionalCourseUtil.createBlog(browser, BLOG_SHORT_TITLE, BLOG_DESCRIPTION));
		
		Assert.assertTrue(functionalCourseUtil.publishEntireCourse(browser, null, null));
		
		/* create content */
		Assert.assertTrue(functionalCourseUtil.open(browser, course.getRepoEntryKey(), 0));
		Assert.assertTrue(functionalCourseUtil.createBlogEntry(browser, course.getRepoEntryKey(), 0,
				CONCURRENT_RW_BLOG_SHORT_TITLE, CONCURRENT_RW_BLOG_DESCRIPTION, CONCURRENT_RW_BLOG_CONTENT));
		
		/*
		 * do concurrent access read
		 */
		final Selenium[] student = new Selenium[userCount];
		
		student[0] = (Selenium) student0;
		student[1] = (Selenium) student1;
		
		final boolean[] success = new boolean[userCount];
		Arrays.fill(success, true);
		
		/* for syncing threads */
		final ReentrantLock lock = new ReentrantLock();
		final Condition cond = lock.newCondition();
		final boolean[] doSignal = new boolean[1];
		doSignal[0] = false;
		
		final boolean[] finished = new boolean[userCount];
		Arrays.fill(finished, false);
		
		/* students log in an visit blog */
		for(int i = 0; i < userCount; i++){
			final int index = i;
			
			Thread thread = new Thread(new Runnable(){
				int i;
				{
					i = index;
				}
				
				@Override
				public void run() {
					try{
						functionalUtil.login(student[i], users[i].getLogin(), users[i].getPassword(), true);
						functionalCourseUtil.openBlogWithoutBusinessPath(student[i], course.getRepoEntryKey(), 0);
						functionalCourseUtil.openBlogEntry(student[i], 0);
					}catch(Exception e){
						success[i] = false;
					}finally{
						finished[i] = true;
						
						lock.lock();
						if(doSignal[0]){
							cond.signal();
						}
						lock.unlock();
					}
				}
				
			});
			
			thread.start();
		}
		
		/* wait for browsers to be ready */
		lock.lock();
		doSignal[0] = true;
		
		try{
			while(ArrayUtils.contains(finished, false)){
				cond.await();
			}
		}finally{
			lock.unlock();
		}
		
		/* edit blog as author */
		Assert.assertTrue(functionalCourseUtil.editBlogEntry(browser, course.getRepoEntryKey(), 0,
				null, null, CONCURRENT_RW_BLOG_NEW_CONTENT, 0, new BlogEdit[]{BlogEdit.CONTENT}));
		
		/* open entry by users */
		Arrays.fill(finished, false);
		doSignal[0] = false;
		
		for(int i = 0; i < userCount; i++){
			final int index = i;
			
			Thread thread = new Thread(new Runnable(){
				int i;
				{
					i = index;
				}
				
				@Override
				public void run() {
					try{
						functionalCourseUtil.backBlogEntry(student[i]);
						functionalCourseUtil.openBlogEntry(student[i], 0);
						functionalUtil.waitForPageToLoadContent(student[i], null, CONCURRENT_RW_BLOG_NEW_CONTENT, WaitLimitAttribute.VERY_SAVE, null, true);
						functionalUtil.logout(student[i]);
					}catch(Exception e){
						success[i] = false;
					}finally{
						finished[i] = true;

						lock.lock();
						if(doSignal[0]){
							cond.signal();
						}
						lock.unlock();
					}
				}
				
			});
			
			thread.start();
		}
		
		/* wait for browsers to be logged out */
		lock.lock();
		doSignal[0] = true;
		
		try{
			while(ArrayUtils.contains(finished, false)){
				cond.await();
			}
		}finally{
			lock.unlock();
		}
		
		
		Assert.assertFalse(ArrayUtils.contains(success, false));

		Assert.assertTrue(functionalUtil.logout(browser));
	}
}
