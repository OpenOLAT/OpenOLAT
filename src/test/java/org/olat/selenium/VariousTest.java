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
package org.olat.selenium;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.UUID;

import org.jboss.arquillian.drone.api.annotation.Drone;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.olat.modules.library.LibraryManagerTest;
import org.olat.selenium.page.LoginPage;
import org.olat.selenium.page.NavigationPage;
import org.olat.selenium.page.core.FolderPage;
import org.olat.test.JunitTestHelper;
import org.olat.test.rest.UserRestClient;
import org.olat.user.restapi.UserVO;
import org.openqa.selenium.WebDriver;

/**
 * 
 * Initial date: 19.06.2014<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
@RunWith(Arquillian.class)
public class VariousTest extends Deployments {

	@Drone
	private WebDriver browser;
	@ArquillianResource
	private URL deploymentUrl;

	/**
	 * An administrator setup a library with a shared folder,
	 * upload and unzip a bunch of documents for it. A user
	 * use the library, browses some folders and comments a document.
	 * 
	 * @throws IOException
	 * @throws URISyntaxException
	 */
	@Test
	public void library()
	throws IOException, URISyntaxException {

		UserVO user = new UserRestClient(deploymentUrl).createRandomUser("kanu");
		
		LoginPage loginPage = LoginPage.load(browser, deploymentUrl);
		loginPage
			.loginAs("administrator", "openolat")
			.resume();
		
		// Administrator setup the library
		String library = "Library " + UUID.randomUUID();
		NavigationPage navigation = NavigationPage.load(browser);
		navigation
			.openAuthoringEnvironment()
			.createSharedFolder(library)
			.clickToolbarBack();
		
		// open the return box of the participant and upload a file
		URL documentsUrl = LibraryManagerTest.class.getResource("Library.zip");
		File documentsFile = new File(documentsUrl.toURI());
		
		new FolderPage(browser)
			.assertOnFolderCmp()
			.uploadFile(documentsFile)
			.selectFile(documentsFile.getName())
			.unzipFile(documentsFile.getName());
		
		navigation
			.openAdministration()
			.openLibrarySettings()
			.addSharedFolder(library);
		
		// A user visits the library
		LoginPage userLoginPage = LoginPage.load(browser, deploymentUrl);
		userLoginPage.loginAs(user.getLogin(), user.getPassword());
		
		navigation
			.openLibrary(browser)
			.assertOnMenuFolder("Library")
			.selectFolder("Library")
			.selectFolder("Positions")
			.assertOnPdfFile("DocPosition_1.pdf");
	}
	
	
	/**
	 * An administrator setup a new shared folder, configures it as
	 * library with a folder. A user proposes a new document. The administrator
	 * reviews the document, accept it through the wizard and go back to the
	 * library to see it as new document, and in the selected folder.
	 * 
	 * @throws IOException
	 * @throws URISyntaxException
	 */
	@Test
	public void libraryWorkflow()
	throws IOException, URISyntaxException {
		UserVO user = new UserRestClient(deploymentUrl).createRandomUser("kanu");
		
		LoginPage loginPage = LoginPage.load(browser, deploymentUrl);
		loginPage
			.loginAs("administrator", "openolat")
			.resume();
		
		// Administrator setup the library with a folder
		String library = "Library " + UUID.randomUUID();
		NavigationPage navigation = NavigationPage.load(browser);
		navigation
			.openAuthoringEnvironment()
			.createSharedFolder(library)
			.clickToolbarBack();
		
		new FolderPage(browser)
			.assertOnFolderCmp()
			.createDirectory("Topics");

		navigation
			.openAdministration()
			.openLibrarySettings()
			.addSharedFolder(library);
		
		// A user visits the library
		LoginPage userLoginPage = LoginPage.load(browser, deploymentUrl);
		userLoginPage.loginAs(user.getLogin(), user.getPassword());
		
		URL documentUrl = JunitTestHelper.class.getResource("file_resources/handInTopic1.pdf");
		File documentFile = new File(documentUrl.toURI());
		navigation
			.openLibrary(browser)
			.uploadDocument(documentFile);
		
		// Administrator review the document
		loginPage = LoginPage.load(browser, deploymentUrl);
		loginPage
			.loginAs("administrator", "openolat")
			.resume();
		
		navigation = NavigationPage.load(browser);
		navigation
			.openLibrary(browser)
			.reviewDocuments()
			.assertOnDocumentToReview(documentFile.getName())
			.acceptDocument(documentFile.getName())
			.assertOnMetadata()
			.nextFolders("Topics")
			.nextNotifications()
			.finish()
			.back()
			.assertOnNewDocument("handInTopic1.pdf")
			.selectFolder("Topics")
			.assertOnPdfFile("handInTopic1.pdf");
	}
}
