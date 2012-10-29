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
package org.olat.course;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.SecureRandom;
import java.util.List;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.IOUtils;
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
import org.olat.core.logging.OLog;
import org.olat.core.logging.Tracing;
import org.olat.restapi.support.vo.GroupVO;
import org.olat.restapi.support.vo.RepositoryEntryVO;
import org.olat.test.ArquillianDeployments;
import org.olat.util.FunctionalCourseUtil;
import org.olat.util.FunctionalCourseUtil.CourseEditorCourseTab;
import org.olat.util.FunctionalCourseUtil.CourseNodeAlias;
import org.olat.util.FunctionalHtmlUtil;
import org.olat.util.FunctionalRepositorySiteUtil;
import org.olat.util.FunctionalRepositorySiteUtil.CourseWizardAccess;
import org.olat.util.FunctionalRepositorySiteUtil.CourseWizardElement;
import org.olat.util.FunctionalUtil;
import org.olat.util.FunctionalUtil.OlatSite;
import org.olat.util.FunctionalVOUtil;

import com.google.common.io.Files;
import com.thoughtworks.selenium.DefaultSelenium;

/**
 * 
 * @author jkraehemann, joel.kraehemann@frentix.com, frentix.com
 */
@RunWith(Arquillian.class)
public class FunctionalCourseTest {

	private final static OLog log = Tracing.createLoggerFor(FunctionalCourseTest.class);
	
	public final static String WIZARD_COURSE_TITLE = "wizard-course";
	public final static String WIZARD_COURSE_DESCRIPTION = "course created by wizard";
	
	public final static String EDITOR_COURSE_TITLE = "editor-course";
	public final static String EDITOR_COURSE_CHANGED_TITLE = "editor-course-renamed";
	public final static String EDITOR_COURSE_DESCRIPTION = "course create within editor";
	public final static String EDITOR_COURSE_OVERVIEW_FILE = "/org/olat/course/overview_comprehensive_guide_to_c_programming.html";

	public final static int LARGE_COURSE_FILE_COUNT = 20;
	public final static long LARGE_COURSE_FILE_SIZE = 50000;
	public final static int LARGE_COURSE_TEST_COUNT = 200;
	public final static int LARGE_COURSE_GROUP_COUNT = 150;
	public final static String LARGE_COURSE_IQ_TEST_SHORT_TITLE = "QTI";
	public final static String LARGE_COURSE_IQ_TEST_LONG_TITLE = "generated test No. ";
	public final static String LARGE_COURSE_IQ_TEST_DESCRIPTION_0 = "generated within a loop: test#";
	public final static String LARGE_COURSE_IQ_TEST_DESCRIPTION_1 = " of totally " + LARGE_COURSE_TEST_COUNT;
	
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
	
	static FunctionalHtmlUtil functionalHtmlUtil;
	
	static boolean initialized = false;
	
	@Before
	public void setup() throws IOException, URISyntaxException{
		if(!initialized){
			functionalUtil = new FunctionalUtil();
			functionalUtil.setDeploymentUrl(deploymentUrl.toString());			
			functionalRepositorySiteUtil = functionalUtil.getFunctionalRepositorySiteUtil();
			functionalCourseUtil = functionalRepositorySiteUtil.getFunctionalCourseUtil();

			functionalVOUtil = new FunctionalVOUtil(functionalUtil.getUsername(), functionalUtil.getPassword());
			
			functionalHtmlUtil = functionalUtil.getFunctionalHtmlUtil();

			initialized = true;
		}
	}

	@Ignore
	@Test
	@RunAsClient
	public void checkCreateUsingWizard(){
		/* login */
		Assert.assertTrue(functionalUtil.login(browser, functionalUtil.getUsername(), functionalUtil.getPassword(), true));
		
		/* open repository site */
		Assert.assertTrue(functionalUtil.openSite(browser, OlatSite.LEARNING_RESOURCES));
		
		/* create course using wizard */
		CourseWizardElement[] elementArray = new CourseWizardElement[]{
				CourseWizardElement.INFO_PAGE,
				CourseWizardElement.FORUM,
				CourseWizardElement.ENROLLMENT,
				CourseWizardElement.DOWNLOAD_FOLDER,
				CourseWizardElement.EMAIL};
		
		Assert.assertTrue(functionalRepositorySiteUtil.createCourseUsingWizard(browser, WIZARD_COURSE_TITLE, WIZARD_COURSE_DESCRIPTION,
				elementArray, null, true, CourseWizardAccess.USERS));
		
		/* click each node once */
		for(int i = 0; i < elementArray.length; i++){
			functionalCourseUtil.open(browser, i);
		}
		
		functionalUtil.logout(browser);
	}

	@Ignore
	@Test
	@RunAsClient
	public void checkCreateUsingEditor() throws FileNotFoundException, IOException, URISyntaxException{
		/* login */
		Assert.assertTrue(functionalUtil.login(browser, functionalUtil.getUsername(), functionalUtil.getPassword(), true));
		
		/* open repository site */
		Assert.assertTrue(functionalUtil.openSite(browser, OlatSite.LEARNING_RESOURCES));
		
		/* create course */
		Assert.assertTrue(functionalRepositorySiteUtil.createCourseUsingEditor(browser, EDITOR_COURSE_TITLE, EDITOR_COURSE_DESCRIPTION));
		
		/* set single page as overview */
		Assert.assertTrue(functionalCourseUtil.uploadOverviewPage(browser, FunctionalCourseTest.class.getResource(EDITOR_COURSE_OVERVIEW_FILE).toURI()));
		
		/* publish entire course */
		Assert.assertTrue(functionalCourseUtil.publishEntireCourse(browser, null, null));
		
		/* change short title and save */
		Assert.assertTrue(functionalCourseUtil.openCourseEditorCourseTab(browser, CourseEditorCourseTab.TITLE_AND_DESCRIPTION));
		
		StringBuffer selectorBuffer = new StringBuffer();
		
		selectorBuffer.append("xpath=(//div[contains(@class, 'o_editor')]//form//input[@type='text'])[1]");
		
		browser.type(selectorBuffer.toString(), EDITOR_COURSE_CHANGED_TITLE);
		
		selectorBuffer = new StringBuffer();
		
		selectorBuffer.append("xpath=(//div[contains(@class, 'o_editor')]//form//button[contains(@class, '")
		.append(functionalUtil.getButtonDirtyCss())
		.append("')])[last()]");
		
		functionalUtil.waitForPageToLoadElement(browser, selectorBuffer.toString());
		browser.click(selectorBuffer.toString());
		functionalUtil.waitForPageToUnloadElement(browser, selectorBuffer.toString());
		
		/* publish entire course */
		Assert.assertTrue(functionalCourseUtil.publishEntireCourse(browser, null, null));
		
		/*
		 * Test for content i.e. approve if changes were applied.
		 */
		String courseLink = null;
		
		Assert.assertNotNull(courseLink = functionalCourseUtil.readExternalLink(browser));
		Assert.assertTrue(functionalCourseUtil.closeActiveTab(browser));
		
		browser.open(courseLink);
		functionalUtil.waitForPageToLoad(browser);

		String originalText = functionalHtmlUtil.stripTags(IOUtils.toString(FunctionalCourseTest.class.getResourceAsStream(EDITOR_COURSE_OVERVIEW_FILE)), true);

		//TODO:JK: probably you want to replace the following code with functionalUtil.waitForPageToLoadContent
		String spIFrameSelector = "dom=document.getElementsByClassName('b_module_singlepage_wrapper')[0].getElementsByTagName('iframe')[0]";
		functionalUtil.waitForPageToLoadElement(browser, spIFrameSelector);
		browser.selectFrame(spIFrameSelector);
		String source = browser.getHtmlSource();
		String currentText = functionalHtmlUtil.stripTags(source, true);
		browser.selectFrame("relative=up");
		
		
		Assert.assertTrue(originalText.equals(currentText));
		
		selectorBuffer = new StringBuffer();
		
		selectorBuffer.append("xpath=//a[contains(@class, '")
		.append(functionalCourseUtil.getStructureIconCss())
		.append("')]")
		.append("//span[contains(text(), '")
		.append(EDITOR_COURSE_CHANGED_TITLE)
		.append("')]");
		
		Assert.assertTrue(browser.isElementPresent(selectorBuffer.toString()));
		
		functionalUtil.logout(browser);
	}
	
	@Test
	@RunAsClient
	public void checkCreateLargeCourse() throws URISyntaxException, IOException, NoSuchAlgorithmException, NoSuchProviderException{
		File[] largeFile = new File[LARGE_COURSE_FILE_COUNT];
		
		for(int i = 0; i < largeFile.length; i++){
			File currentFile =
					largeFile[i] = new File(Files.createTempDir(), "largeText" + i + ".txt");

			if(currentFile.exists()){
				currentFile.delete();
			}

			currentFile.createNewFile();
			OutputStream out = new FileOutputStream(currentFile);

			log.info("creating large file: " + LARGE_COURSE_FILE_SIZE + "bytes");

			for(int j = 0; currentFile.length() < LARGE_COURSE_FILE_SIZE; j++){
				ByteArrayOutputStream dataOut = new ByteArrayOutputStream();
				
				dataOut.write(new String("Line number #" + j + ": ").getBytes());
				dataOut.write(Base64.encodeBase64(SecureRandom.getInstance("SHA1PRNG", "SUN").generateSeed(512), false));
				dataOut.write("\n".getBytes());
				IOUtils.write(dataOut.toByteArray(), out);
				out.flush();
			}

			out.close();
		}
		
		/* create groups via REST-API */
		List<GroupVO> group = functionalVOUtil.createTestCourseGroups(deploymentUrl, LARGE_COURSE_GROUP_COUNT);
		
		/* login */
		Assert.assertTrue(functionalUtil.login(browser, functionalUtil.getUsername(), functionalUtil.getPassword(), true));
		
		/* open repository site */
		Assert.assertTrue(functionalUtil.openSite(browser, OlatSite.LEARNING_RESOURCES));
		
		/* create course using wizard */
		CourseWizardElement[] elementArray = new CourseWizardElement[]{
				CourseWizardElement.INFO_PAGE,
				CourseWizardElement.DOWNLOAD_FOLDER};
		
		Assert.assertTrue(functionalRepositorySiteUtil.createCourseUsingWizard(browser, WIZARD_COURSE_TITLE, WIZARD_COURSE_DESCRIPTION,
				elementArray, null, true, CourseWizardAccess.USERS));
		
		Assert.assertTrue(functionalCourseUtil.open(browser, 1));
		
		for(File currentFile: largeFile){
			Assert.assertTrue(functionalCourseUtil.uploadFileToCourseFolder(browser, currentFile.toURI()));
		}
		
		/* click course node and open editor */
		functionalCourseUtil.open(browser, -1);
		Assert.assertTrue(functionalCourseUtil.openCourseEditor(browser));
		String businessPath = functionalCourseUtil.readExternalLink(browser);
		
		/* create tests */
		for(int i = 0; i < LARGE_COURSE_TEST_COUNT; i++){
			String title = LARGE_COURSE_IQ_TEST_SHORT_TITLE + i;
			String description = LARGE_COURSE_IQ_TEST_DESCRIPTION_0 + i + LARGE_COURSE_IQ_TEST_DESCRIPTION_1;
			
			/* create course node and assign qti test to it */
			Assert.assertTrue(functionalCourseUtil.createCourseNode(browser,
					CourseNodeAlias.IQ_TEST,
					title, LARGE_COURSE_IQ_TEST_LONG_TITLE, description,
					2 * i + 4));
			Assert.assertTrue(functionalCourseUtil.createQTITest(browser, title, description));
		}
		
		functionalUtil.logout(browser);
		
		/* assign groups to course */
		long repoKey = functionalCourseUtil.extractRepositoryEntryKey(businessPath);
		RepositoryEntryVO repoEntryVO = functionalVOUtil.getRepositoryEntryByKey(deploymentUrl, repoKey);
		long courseId = repoEntryVO.getOlatResourceId();
		
		functionalVOUtil.addGroupToCourse(deploymentUrl, courseId, group);
	}
}
