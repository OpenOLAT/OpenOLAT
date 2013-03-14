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
package org.olat.repository;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.List;
import java.util.UUID;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.drone.api.annotation.Drone;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.olat.restapi.support.vo.CourseVO;
import org.olat.restapi.support.vo.GroupVO;
import org.olat.restapi.support.vo.RepositoryEntryVO;
import org.olat.test.ArquillianDeployments;
import org.olat.user.restapi.UserVO;
import org.olat.util.FunctionalCourseUtil;
import org.olat.util.FunctionalCourseUtil.CourseEditorBlogTab;
import org.olat.util.FunctionalCourseUtil.CourseEditorCpTab;
import org.olat.util.FunctionalCourseUtil.CourseEditorIQTestTab;
import org.olat.util.FunctionalCourseUtil.CourseEditorPodcastTab;
import org.olat.util.FunctionalCourseUtil.CourseEditorPortfolioTaskTab;
import org.olat.util.FunctionalCourseUtil.CourseEditorWikiTab;
import org.olat.util.FunctionalCourseUtil.CourseNodeAlias;
import org.olat.util.FunctionalRepositorySiteUtil;
import org.olat.util.FunctionalUtil;
import org.olat.util.FunctionalVOUtil;
import org.olat.util.browser.Tutor1;

import com.thoughtworks.selenium.DefaultSelenium;

/**
 * 
 * @author jkraehemann, joel.kraehemann@frentix.com, frentix.com
 */
@Ignore
@RunWith(Arquillian.class)
public class FunctionalRepositoryTest {
	
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
			
			functionalCourseUtil = new FunctionalCourseUtil(functionalUtil, functionalRepositorySiteUtil);
			
			functionalVOUtil = new FunctionalVOUtil(functionalUtil.getUsername(), functionalUtil.getPassword());
			
			initialized = true;
		}
	}
	
	@Ignore
	@Test
	@RunAsClient
	public void checkCopyCourse(@Drone @Tutor1 DefaultSelenium tutor0) throws URISyntaxException, IOException{
		/*
		 * prerequisites for test created via REST
		 */
		/* create tutor */
		UserVO tutorVO = functionalVOUtil.createTestAuthors(deploymentUrl, 1).get(0);
		
		/* create groups */
		List<GroupVO> groupVO = functionalVOUtil.createTestCourseGroups(deploymentUrl, 3);
		
		/* import course */
		String courseName = UUID.randomUUID().toString();
		CourseVO courseVO = functionalVOUtil.importAllElementsCourse(deploymentUrl, courseName, courseName);
		
		RepositoryEntryVO repoEntryVO = functionalVOUtil.getRepositoryEntryByKey(deploymentUrl, courseVO.getRepoEntryKey());
		functionalVOUtil.addOwnerToRepositoryEntry(deploymentUrl, repoEntryVO, tutorVO);
		
		/* set rights */
		//TODO:JK: implement me
		
		/* create learning areas */
		//TODO:JK: implement me
		
		/*
		 * create or configure content
		 */
		/* login */
		functionalUtil.login(tutor0, tutorVO.getLogin(), tutorVO.getPassword(), true);
		
		/* open detailed view of course */
		functionalRepositorySiteUtil.openDetailedView(tutor0, courseName, 1);
		
		/* copy resource */
		String newCourseName = "Copy " + courseName;
		functionalRepositorySiteUtil.copyRepositoryEntry(tutor0, newCourseName, courseName);
		
		/*
		 * verify content
		 */
		/* open copied course */
		functionalRepositorySiteUtil.openCourse(tutor0, newCourseName, 0);
		
		/* open course editor */
		functionalCourseUtil.openCourseEditor(tutor0);
		
		/* click all course nodes */
		int count = functionalCourseUtil.count(tutor0, true, true, false);
		
		for(int i = 0; i < count; i++){
			/* open course node */
			CourseNodeAlias alias = functionalCourseUtil.open(tutor0, i);
			
			/**/
			switch(alias){
			case CP:
			{
				
				
				/* verify visibility */
				//TODO:JK: implement me
				
				/* verify access */
				//TODO:JK: implement me
				
				/* verify rules */
				//TODO:JK: implement me
			}
			break;
			case BLOG:
			{
				/* verify visibility */
				//TODO:JK: implement me
				
				/* verify access */
				//TODO:JK: implement me
				
				/* verify rules */
				//TODO:JK: implement me
			}
			break;
			case PODCAST:
			{
				/* verify visibility */
				//TODO:JK: implement me
				
				/* verify access */
				//TODO:JK: implement me
				
				/* verify rules */
				//TODO:JK: implement me
			}
			break;
			case WIKI:
			{
				/* verify visibility */
				//TODO:JK: implement me
				
				/* verify access */
				//TODO:JK: implement me
				
				/* verify rules */
				//TODO:JK: implement me
			}
			break;
			case PORTFOLIO_TASK:
			{
				/* verify visibility */
				//TODO:JK: implement me
				
				/* verify access */
				//TODO:JK: implement me
				
				/* verify rules */
				//TODO:JK: implement me
			}
			break;
			case IQ_TEST:
			{
				/* verify visibility */
				//TODO:JK: implement me
				
				/* verify access */
				//TODO:JK: implement me
				
				/* verify rules */
				//TODO:JK: implement me
			}
			break;
			case IQ_SELFTEST:
			{
				/* verify visibility */
				//TODO:JK: implement me
				
				/* verify access */
				//TODO:JK: implement me
				
				/* verify rules */
				//TODO:JK: implement me
			}
			break;
			case IQ_QUESTIONAIRE:
			{
				/* verify visibility */
				//TODO:JK: implement me
				
				/* verify access */
				//TODO:JK: implement me
				
				/* verify rules */
				//TODO:JK: implement me
			}
			break;
			}
			
		}
		
		/* click all learning resources */
		count = functionalCourseUtil.count(tutor0, true, false, false);
		
		for(int i = 0; i < count; i++){
			/* open learning resource */
			CourseNodeAlias alias = functionalCourseUtil.openLearningResource(tutor0, i);
			
			/* open learning content tab */
			switch(alias){
			case CP:
			{
				functionalCourseUtil.openCourseEditorCpTab(tutor0, CourseEditorCpTab.LEARNING_CONTENT);
				
				/* verify resource IDs */
				//TODO:JK: implement me
			}
			break;
			case BLOG:
			{
				functionalCourseUtil.openCourseEditorBlogTab(tutor0, CourseEditorBlogTab.LEARNING_CONTENT);
				
				/* verify resource IDs */
				//TODO:JK: implement me
			}
			break;
			case PODCAST:
			{
				functionalCourseUtil.openCourseEditorPodcastTab(tutor0, CourseEditorPodcastTab.LEARNING_CONTENT);
				
				/* verify resource IDs */
				//TODO:JK: implement me
			}
			break;
			case WIKI:
			{
				functionalCourseUtil.openCourseEditorWikiTab(tutor0, CourseEditorWikiTab.LEARNING_CONTENT);

				/* verify resource IDs */
				//TODO:JK: implement me
			}
			break;
			case PORTFOLIO_TASK:
			{
				functionalCourseUtil.openCourseEditorPortfolioTaskTab(tutor0, CourseEditorPortfolioTaskTab.LEARNING_CONTENT);

				/* verify resource IDs */
				//TODO:JK: implement me
			}
			break;
			case IQ_TEST:
			{
				functionalCourseUtil.openCourseEditorIQTestTab(tutor0, CourseEditorIQTestTab.TEST_CONFIGURATION);

				/* verify resource IDs */
				//TODO:JK: implement me
			}
			break;
			case IQ_SELFTEST:
			{
				functionalCourseUtil.openCourseEditorIQTestTab(tutor0, CourseEditorIQTestTab.TEST_CONFIGURATION);

				/* verify resource IDs */
				//TODO:JK: implement me
			}
			break;
			case IQ_QUESTIONAIRE:
			{
				functionalCourseUtil.openCourseEditorIQTestTab(tutor0, CourseEditorIQTestTab.TEST_CONFIGURATION);

				/* verify resource IDs */
				//TODO:JK: implement me
			}
			break;
			}
		}
		
		//TODO:JK: implement me
	}
}
