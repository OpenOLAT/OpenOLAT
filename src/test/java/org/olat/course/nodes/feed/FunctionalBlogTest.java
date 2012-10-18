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
import org.olat.test.ArquillianDeployments;
import org.olat.user.restapi.UserVO;
import org.olat.util.FunctionalCourseUtil;
import org.olat.util.FunctionalCourseUtil.BlogEdit;
import org.olat.util.FunctionalRepositorySiteUtil;
import org.olat.util.FunctionalUtil;
import org.olat.util.FunctionalUtil.WaitLimitAttribute;
import org.olat.util.FunctionalVOUtil;
import org.olat.util.FunctionalCourseUtil.CourseNodeAlias;
import org.olat.util.browser.Browser1;
import org.olat.util.browser.Browser2;
import org.olat.util.browser.Student1;
import org.olat.util.browser.Student2;

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
	static FunctionalVOUtil functionalVOUtil;

	static boolean initialized = false;
	
	@Before
	public void setup() throws IOException, URISyntaxException{
		if(!initialized){
			functionalUtil = new FunctionalUtil();
			functionalUtil.setDeploymentUrl(deploymentUrl.toString());

			functionalRepositorySiteUtil = functionalUtil.getFunctionalRepositorySiteUtil();
			functionalCourseUtil = functionalRepositorySiteUtil.getFunctionalCourseUtil();

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
		
		/*  */
		Assert.assertTrue(functionalRepositorySiteUtil.openCourse(browser, course.getRepoEntryKey()));
		Assert.assertTrue(functionalCourseUtil.openCourseEditor(browser));
		
		Assert.assertTrue(functionalCourseUtil.createCourseNode(browser, CourseNodeAlias.BLOG, BLOG_SHORT_TITLE, BLOG_LONG_TITLE, BLOG_DESCRIPTION, 0));
		Assert.assertTrue(functionalCourseUtil.createBlog(browser, BLOG_SHORT_TITLE, BLOG_DESCRIPTION));
		
		Assert.assertTrue(functionalCourseUtil.publishEntireCourse(browser, null, null));
		
		Assert.assertTrue(functionalCourseUtil.open(browser, course.getRepoEntryKey(), 0));
		Assert.assertTrue(functionalCourseUtil.importBlogFeed(browser, BLOG_FEED));
		
		Assert.assertTrue(functionalCourseUtil.open(browser, course.getRepoEntryKey(), 0));
		

		Assert.assertTrue(functionalUtil.logout(browser));
	}
	
	@Ignore
	@Test
	@RunAsClient
	public void checkConcurrentClearCache(){
		
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
		Arrays.fill(success, false);
		
		List<Thread> threads = new ArrayList<Thread>();
		
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
						// success[i] = false;
					}
				}
				
			});
			
			thread.start();
			threads.add(thread);
		}
		
		/* wait for browsers to be ready */
		for(Thread currentThread: threads){
			currentThread.join(60000);
		}
		
		/* edit blog as author */
		Assert.assertTrue(functionalCourseUtil.editBlogEntry(browser, course.getRepoEntryKey(), 0,
				null, null, CONCURRENT_RW_BLOG_NEW_CONTENT, 0, new BlogEdit[]{BlogEdit.CONTENT}));
		
		/* open entry by users */
		threads = new ArrayList<Thread>();
		
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
						functionalUtil.waitForPageToLoadContent(student[i], null, CONCURRENT_RW_BLOG_NEW_CONTENT, WaitLimitAttribute.VERY_SAVE, null, false);
						functionalUtil.logout(student[i]);
						success[i] = true;
					}catch(Exception e){
						// empty
					}
				}
				
			});
			
			thread.start();
			threads.add(thread);
		}
		
		/* wait for browsers to be logged out */
		for(Thread currentThread: threads){
			currentThread.join(60000);
		}
		
		Assert.assertFalse(ArrayUtils.contains(success, false));

		Assert.assertTrue(functionalUtil.logout(browser));
	}
}
