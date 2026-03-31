/**
 * <p>
 * Copyright (c) frentix GmbH<br>
 * http://www.frentix.com<br>
 */
package org.olat.selenium;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.List;
import java.util.UUID;

import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.olat.modules.selectus.model.PositionRole;
import org.olat.modules.selectus.model.PositionStatus;
import org.olat.selenium.page.LoginPage;
import org.olat.selenium.page.NavigationPage;
import org.olat.selenium.page.graphene.OOGraphene;
import org.olat.selenium.page.selectus.ApplicationWizardPage;
import org.olat.selenium.page.selectus.CommitteePage;
import org.olat.selenium.page.selectus.DecisionToolDefinitionPage.Type;
import org.olat.selenium.page.selectus.DecisionToolPage;
import org.olat.selenium.page.selectus.EditPositionPage;
import org.olat.selenium.page.selectus.PositionListPage;
import org.olat.selenium.page.selectus.PositionPage;
import org.olat.test.rest.UserRestClient;
import org.olat.user.restapi.UserVO;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

import com.dumbster.smtp.SmtpMessage;

/**
 * Test high level features in a customer neutral way.
 * 
 * Initial date: 15 janv. 2018<br>
 * @author srosse, stephane.rosse@frentix.com, https://www.frentix.com
 *
 */
@RunWith(Arquillian.class)
public class SelectusTest extends Deployments {
	
	private WebDriver browser = getWebDriver(0);
	@ArquillianResource
	private URL deploymentUrl;

	/**
	 * The administrator create a new position, publish it. In a second
	 * browser, an user apply to the position.<br/>
	 * After the user has finished the wizard, the administrator checks
	 * that the application is visible in the list of the position.
	 * 
	 * @param loginPage
	 * @param applicantBrowser
	 * @throws URISyntaxException 
	 * @throws IOException 
	 */
	@Test
	@RunAsClient
	public void applicationWizard() throws IOException, URISyntaxException {
		UserVO administrator = new UserRestClient(deploymentUrl).createSelectusManager("Selecta", true);
		
		WebDriver applicantBrowser = getWebDriver(1);
		
		LoginPage.load(browser, deploymentUrl)
			.assertOnLoginPage()
			.loginAs(administrator)
			.resume();
		
		String title = "Fac - 1 " + UUID.randomUUID();
		NavigationPage navBar = NavigationPage.load(browser);
		PositionListPage positions = navBar
			.openPositionList();
		positions
			.addPosition()
			.editProfile()
			.selectLanguages(true, false, false)
			.fillMLTitles(title, title, "en")
			.fillId("AC-234")
			.fillMLDepartmentAndHomepage("Départment de paléontologie", null, "https://www.frentix.com")
			.fillMLDescription("Very interessant job", "en")
			.savePositionProfile()
			.editStatus()
			.selectStatus(PositionStatus.published)
			.savePositionStatus()
			.clickToolbarBack()
			.back();
		PositionPage position = positions
			.selectPositionInList(title);
		 String positionUrl = position
			.openAdminMenu()
			.editPosition()
			.editProfile()
			.getPositionURLAndClose();
		
		//applicant apply to the position
		String seleniumPositionUrl = ApplicationWizardPage.rewritePositionUrl(positionUrl, deploymentUrl);
		ApplicationWizardPage apply = ApplicationWizardPage.getWizard(applicantBrowser, seleniumPositionUrl);
		apply
			.assertPositionInstruction(title)
			.nextToDataProtection()
			.acceptDataProtectionDisclaimer()
			.nextToPersonalData()
			.fillPerson("Dr.", "f", "Yoko", "Schmid")
			.fillMaritalStatus("single")
			.fillBirthday(6, 3, 1987)
			.fillPhone("34853468")
			.fillEmail("yoko@frentix.com")
			.selectNationality("France")
			.fillBusinessInfos("Université Paris I", "Paléantologie", "Chargé de cours")
			.fillBusinessAddress("Rue de Flore 92", "98000", "Paris", "France")
			.fillPrivateAddress("Rue de Flore 92", "98000", "Paris", "France")
			.nextToAcademicalBackground()
			.fillHighestDegree("master", "2002", "Sorbonne")
			.fillWorkedInAcademiaSince("10")
			.nextToReview()
			.finish()
			.assertApplicationSend();
		
		// staff check the application
		position
			.back()
			.selectPositionInList(title)
			.selectApplications()
			.assertOnApplication("Yoko", "Schmid");
	}
	
	/**
	 * Administrator create a staff member, the staff member create
	 * an application.
	 * 
	 * @param loginPage
	 * @throws URISyntaxException 
	 * @throws IOException 
	 */
	@Test
	@RunAsClient
	public void createApplicationSelectusManager() throws IOException, URISyntaxException {

		UserVO user = new UserRestClient(deploymentUrl).createSelectusManager("Leonhard", false);// "fritz", "Fritz", "Haber", "Frt01#Secured"

		LoginPage loginPage = LoginPage.load(browser, deploymentUrl);
		loginPage
			.loginAs(user.getLogin(), user.getPassword())
			.resume()
			.assertLoggedIn(user);

		NavigationPage navBar = NavigationPage.load(browser);
		String title = "ETH - 2 " + UUID.randomUUID();
		PositionListPage positions = navBar
			.openPositionList();
		positions
			.addPosition()
			.editProfile()
			.selectLanguages(true, false, false)
			.fillMLTitles(title, title, "en")
			.fillId("AC-235")
			.fillMLDepartmentAndHomepage("Physics department", null, "https://www.frentix.com")
			.fillMLDescription("Very interessant job", "en")
			.savePositionProfile()
			.clickToolbarBack()
			.back();
		PositionPage position = positions
			.selectPositionInList(title);
		
		position
			.addApplication()
			.selectEditAcademicalBackground()
			.fillHighestDegree("pd", "2001", "University of Paris")
			.selectEditPerson()
			.fillPerson("Dr.", "f", "Marie", "Curie")
			.fillMaritalStatus("married")
			.selectNationality("France")
			.fillPrivateAddress("Rue de Flore 45", "3214", "Chambort", "France")
			.fillEmail("marie." + UUID.randomUUID() + "@curie.institut.fr")
			.selectEditStatus()
			.saveApplication();
		
		position
			.selectApplications()
			.assertOnApplication("Marie", "Curie");
	}
	
	/**
	 * Three applicants apply together and we check that the application ids
	 * are unique.
	 * 
	 * @param loginPage
	 * @param applicant1Browser
	 * @param applicant2Browser
	 * @param applicant3Browser
	 * @throws URISyntaxException 
	 * @throws IOException 
	 */
	@Test
	@RunAsClient
	public void concurrentApplicationWizard() throws IOException, URISyntaxException {
		UserVO administrator = new UserRestClient(deploymentUrl).createSelectusManager("Selecta-2", true);
		
		WebDriver applicant1Browser = getWebDriver(1);
		WebDriver applicant2Browser = getWebDriver(2);
		WebDriver applicant3Browser = getWebDriver(3);
		
		LoginPage.load(browser, deploymentUrl)
			.assertOnLoginPage()
			.loginAs(administrator)
			.resume();

		String title = "Fac - 2 " + UUID.randomUUID();
		NavigationPage navBar = NavigationPage.load(browser);
		PositionListPage positions = navBar
			.openPositionList();
		positions
			.addPosition()
			.editProfile()
			.selectLanguages(true, false, false)
			.fillMLTitles(title, title, "en")
			.fillId("AC-236")
			.fillMLDepartmentAndHomepage("IAs department", null, "https://www.frentix.com")
			.fillMLDescription("Study the effect of concurrency with new multi-core processors", "en")
			.savePositionProfile()
			.editStatus()
			.selectStatus(PositionStatus.published)
			.savePositionStatus()
			.clickToolbarBack()
			.back();
		PositionPage position = positions
			.selectPositionInList(title);
		 String positionUrl = position
			.openAdminMenu()
			.editPosition()
			.editProfile()
			.getPositionURLAndClose();
		 
		String seleniumPositionUrl = ApplicationWizardPage.rewritePositionUrl(positionUrl, deploymentUrl);
		 
		//applicant 1 apply to the position
		ApplicationWizardPage apply1 = ApplicationWizardPage.getWizard(applicant1Browser, seleniumPositionUrl);
		apply1
			.assertPositionInstruction(title)
			.nextToDataProtection()
			.acceptDataProtectionDisclaimer()
			.nextToPersonalData()
			.fillPerson("Dr.", "f", "Aika", "Dupond")
			.fillMaritalStatus("divorced")
			.fillBirthday(23, 11, 1992)
			.fillPhone("34853468")
			.fillEmail("aika.dupond@frentix.com")
			.fillBirthday(6, 3, 1987)
			.selectNationality("France")
			.fillBusinessInfos("Paris II", "IA Department", "Chargé de cours")
			.fillBusinessAddress("Rue de Flore 93", "98000", "Paris", "France")
			.fillPrivateAddress("Rue St. Jacques 45", "99022", "Montreuil", "France")
			.nextToAcademicalBackground()
			.fillHighestDegree("diplom", "2002", "Sorbonne")
			.fillWorkedInAcademiaSince("1")
			.nextToReview();
		
		//applicant 2
		ApplicationWizardPage apply2 = ApplicationWizardPage.getWizard(applicant2Browser, seleniumPositionUrl);
		apply2
			.assertPositionInstruction(title)
			.nextToDataProtection()
			.acceptDataProtectionDisclaimer()
			.nextToPersonalData()
			.fillPerson("Dr.", "f", "Aoi", "Dupont")
			.fillMaritalStatus("married")
			.fillBirthday(15, 9, 1990)
			.fillPhone("34853468")
			.fillEmail("aoi.dupont@frentix.com")
			.fillBirthday(6, 3, 1987)
			.selectNationality("France")
			.fillBusinessInfos("Paris I", "Hardware and silicon department", "Chargé de cours")
			.fillBusinessAddress("Rue de Flore 94", "98000", "Paris", "France")
			.fillPrivateAddress("Chemin de Moron 12", "45032", "Palavasse", "France")
			.nextToAcademicalBackground()
			.fillHighestDegree("prof", "2002", "Sorbonne")
			.fillWorkedInAcademiaSince("7")
			.nextToReview();
		
		//applicant 3
		ApplicationWizardPage apply3 = ApplicationWizardPage.getWizard(applicant3Browser, seleniumPositionUrl);
		apply3
			.assertPositionInstruction(title)
			.nextToDataProtection()
			.acceptDataProtectionDisclaimer()
			.nextToPersonalData()
			.fillPerson("Dr.", "f", "Ami", "Dupon")
			.fillMaritalStatus("single")
			.fillBirthday(6, 6, 1981)
			.fillPhone("34853468")
			.fillEmail("ami.dupon@frentix.com")
			.selectNationality("France")
			.fillBusinessInfos("Paris III", "Informatic and technology", "Chargé de cours")
			.fillBusinessAddress("Rue de Flore 95", "98000", "Paris", "France")
			.fillPrivateAddress("Avenue Charles de Gaulle 21", "32001", "Marseille", "France")
			.nextToAcademicalBackground()
			.fillHighestDegree("pd", "2002", "Sorbonne")
			.fillWorkedInAcademiaSince("3")
			.nextToReview();
		 
		//they
		apply1
			.finishNoWait();
		apply2
			.finishNoWait();
		apply3
			.finishNoWait();
		
		//wait
		apply1
			.waitFinish()
			.assertApplicationSend();
		apply2.waitFinish()
			.waitFinish()
			.assertApplicationSend();
		apply3
			.waitFinish()
			.assertApplicationSend();

		// staff check the application
		position
			.back()
			.selectPositionInList(title)
			.selectApplications()
			.assertOnApplication("Aoi", "Dupont")
			.assertOnApplicationId(1)
			.assertOnApplicationId(2)
			.assertOnApplicationId(3);
	}
	
	/**
	 * Administrator create a staff member and a committee member as users.
	 * The staff member logged in and create a new position and add the
	 * committee member to the committee.
	 * 
	 * @param loginPage
	 * @throws URISyntaxException 
	 * @throws IOException 
	 */
	@Test
	@RunAsClient
	public void manageCommitteeAddHead() throws IOException, URISyntaxException {
		
		UserVO committeeMember = new UserRestClient(deploymentUrl).createRandomUser("Richard");// "richard", "Richard", "Ernst", "Rich#2478268"
		UserVO selectusManager = new UserRestClient(deploymentUrl).createSelectusManager("Leonhard", false);// "fritz", "Fritz", "Haber", "Frt#z01Sured"

		LoginPage loginPage = LoginPage.load(browser, deploymentUrl);
		loginPage
			.loginAs(selectusManager.getLogin(), selectusManager.getPassword())
			.resume()
			.assertLoggedIn(selectusManager);
		
		NavigationPage navBar = NavigationPage.load(browser);
		
		String title = "Fac - 3 " + UUID.randomUUID();
		PositionListPage positions = navBar
			.openPositionList();
		positions
			.addPosition()
			.editProfile()
			.selectLanguages(true, false, false)
			.fillMLTitles(title, title, "en")
			.fillId("AC-237")
			.fillMLDepartmentAndHomepage("Ghost department", null, "https://www.frentix.com")
			.fillMLDescription("Very subtle job", "en")
			.savePositionProfile()
			.clickToolbarBack()
			.back();
		PositionPage position = positions
			.selectPositionInList(title);
		
		position
			.selectCommittee()
			.addCommitteeMember()
			.fillEmail(committeeMember.getEmail())
			.selectRole(PositionRole.head)
			.next();
		
		position
			.assertCommittee(committeeMember.getLastName() + ", " + committeeMember.getFirstName())
			.assertCommitteeHead(committeeMember.getLastName() + ", " + committeeMember.getFirstName());
	}
	

	/**
	 * An administrator create a position, add an application.
	 * It goes to the C-Decision tab, it set the C decision
	 * to the application it created. Than it send an E-mail.
	 * 
	 * @param loginPage
	 * @throws URISyntaxException 
	 * @throws IOException 
	 */
	@Test
	@RunAsClient
	public void rejectionProcess() throws IOException, URISyntaxException {
		UserVO administrator = new UserRestClient(deploymentUrl).createSelectusManager("Selecta-3", true);
		
		LoginPage.load(browser, deploymentUrl)
			.assertOnLoginPage()
			.loginAs(administrator)
			.resume();
	
		String title = "Fac 4 " + UUID.randomUUID();
		NavigationPage navBar = NavigationPage.load(browser);
		PositionListPage positions = navBar
			.openPositionList();
		positions
			.addPosition()
			.editProfile()
			.selectLanguages(true, false, false)
			.fillMLTitles(title, title, "en")
			.fillId("AC-238")
			.fillMLDepartmentAndHomepage("Physics department", null, "https://www.frentix.com")
			.fillMLDescription("Very interessant job but dangerous", "en")
			.savePositionProfile()
			.clickToolbarBack()
			.back();
		PositionPage position = positions
			.selectPositionInList(title);
		
		String uuid = UUID.randomUUID().toString();
		String lastname = "Curie-" + uuid; 
		String email = "pierre." + uuid.replace("-", "") + "@curie.institut.fr";
		
		position
			.addApplication()
			.selectEditAcademicalBackground()
			.fillHighestDegree("pd", "2001", "University of Paris")
			.selectEditPerson()
			.fillPerson("Dr.", "m", "Pierre", lastname)
			.fillMaritalStatus("married")
			.fillEmail(email)
			.fillBusinessAddress("Rue de Flore 45", "90001", "Paris", "France")
			.selectEditStatus()
			.saveApplication();
		
		position
			.selectApplications()
			.assertOnApplication("Pierre", "Curie")
		// set a c decision
			.selectAllApplications()
			.setCommitteeDecision("1");
		// send an E-mail to annonce the decision
		position
			.selectRejection()
			.openSendDecisionMailWizard()
			.selectRejectionLevel(1)
			.nextToSelect()
			.selectByEmail(email)
			.nextToOverview()
			.assertByEmail(email)
			.nextToTemplate()
			.selectTemplate("Crejection")
			.finish();
		
		position
			.assertIsInRejectionLog(lastname);

		List<SmtpMessage> messages = getSmtpServer().getReceivedEmails();
		Assert.assertEquals(1, messages.size());
		Assert.assertEquals(email, messages.get(0).getHeaderValue("To"));
	}
	

	/**
	 * A staff member prepare a position, load two applications,
	 * prepare the rubrics, set a committee member.<br>
	 * The committee member rate the two applications. Staff
	 * close the position (close and not rating), and set a value
	 * to a rubric. The committee member see the value without
	 * reloading the page.
	 * 
	 * @param loginPage The login page for the administrator
	 * @param committeeBrowser The browser for the committee member
	 * @throws URISyntaxException 
	 * @throws IOException 
	 */
	@Test
	@RunAsClient
	public void positionWithRubrics() throws IOException, URISyntaxException {
		WebDriver committeeBrowser = getWebDriver(1);
		
		UserVO committeeMember = new UserRestClient(deploymentUrl).createRandomUser("Daniel");// "daniel", "Daniel", "Mieville", "Miver#01Closed"
		UserVO selectusManager = new UserRestClient(deploymentUrl).createSelectusManager("Leonhard", false);// "leohard", "Leonhard", "Euler", "Baci01#Vault"
		
		LoginPage.load(browser, deploymentUrl)
			.assertOnLoginPage()
			.loginAs(selectusManager.getLogin(), selectusManager.getPassword())
			.resume()
			.assertLoggedIn(selectusManager);
		
		NavigationPage navBar = NavigationPage.load(browser);

		String title = "Fac rubrics - 5 " + UUID.randomUUID();
		PositionListPage positions = navBar
			.openPositionList()
			.addPosition()
			.editProfile()
			.selectLanguages(true, false, false)
			.fillMLTitles(title, title, "en")
			.fillId("AC-234")
			.fillMLDepartmentAndHomepage("Topology department", null, "https://www.frentix.com")
			.fillMLDescription("We open a position for a mathematicen specialised in rubrics", "en")
			.savePositionProfile()
			.clickToolbarBack()
			.back();
		
		PositionPage position = positions
			.selectPositionInList(title);
		// enable rubrics
		position
			.openAdminMenu()
			.editPosition()
			.editEvaluations()
			.editRubrics()
			.enableRubrics()
			.savePositionDecisionToolConfiguration()
			.clickToolbarBack();
		
		// go to the decision tool and prepare some rubrics
		position
			.selectDecisionTool()
			.manageRubric()
			.addRubric("Text", Type.text, 2, 0)
			.addRubric("Swiss rating", Type.oneSix, 1, 1)
			.saveAndClose();
		
		position
			.selectApplications();
		// add a first application
		position
			.addApplication()
			.selectEditAcademicalBackground()
			.fillHighestDegree("pd", "2003", "University of Paris")
			.selectEditPerson()
			.fillPerson("Dr.", "f", "Yvonne", "Montessori")
			.fillMaritalStatus("married")
			.fillEmail("yvonne." + UUID.randomUUID() + "@acone.it")
			.fillBusinessAddress("Via Giuseppe Verdi 29", "3100", "Pisa", "Italy")
			.selectEditStatus()
			.saveApplication();
		
		// add a second application
		position
			.addApplication()
			.selectEditAcademicalBackground()
			.fillHighestDegree("pd", "1990", "University of Cambridge")
			.selectEditPerson()
			.fillPerson("Dr.", "m", "Michael", "Marzoni")
			.fillMaritalStatus("unmarried")
			.fillBusinessAddress("Via Giuseppe Verdi 31", "3200", "Venedig", "Italy")
			.fillEmail("michael." + UUID.randomUUID() + "@acone.it")
			.selectEditStatus()
			.saveApplication();
		
		// add a committee member
		CommitteePage committee = position
			.selectCommittee();
		committee
			.addCommitteeMember()
			.fillEmail(committeeMember.getEmail())
			.selectRole(PositionRole.member)
			.nextToMemberData();
		OOGraphene.waitElementDisappears(By.cssSelector("div.o_layered_panel div.o_wizard"), 10, browser);
		// find the password
		committee
			.edit(committeeMember.getLastName());
		committee
			.cancel();//close the edit modal dialog
		
		position
			.selectApplications()
			.openAdminMenu()
			.editPosition()
			.editStatus()
			.selectStatus(PositionStatus.closedAndInScreening)
			.savePositionStatus()
			.clickToolbarBack()
			.back();
		
		// The committee member log in
		LoginPage committeeLogin = LoginPage.load(committeeBrowser, deploymentUrl);
		committeeLogin
			.assertOnLoginPage()
			.loginAs(committeeMember);
		
		NavigationPage.load(committeeBrowser)
			.openPositionList();

		PositionPage committeePosition = new PositionPage(committeeBrowser);
		committeePosition
			.acceptPositionDisclaimer();
		
		committeePosition
			.rate("Montessori", "A")
			.rate("Marzoni", "C");
		
		//Back to the staff
		position = positions
				.selectPositionInList(title);
		DecisionToolPage decisionTool = position
			.selectApplications()
			.openAdminMenu()
			.editPosition()
			.editStatus()
			.selectStatus(PositionStatus.closedAndNoRating)
			.savePositionStatus()
			.clickToolbarBack()
			.selectDecisionTool();
		
		// Committee reload the page
		committeePosition
			.back()
			.selectPositionInList(title);
		
		DecisionToolPage committeeDecisionTool = committeePosition
				.selectDecisionTool();
		
		// Staff set a value in the decision tool
		decisionTool
			.selectRubricValue("Marzoni", "abc", "5");
		
		// Committee must see it
		committeeDecisionTool
			.assertOnRubricValue("Marzoni", "5");
	}
	
	
	/**
	 * The test check different paths for staff:
	 * <ul>
	 *  <li>/auth/Positions/0/Position/{positionKey}
	 *  <li>/auth/Positions/0/Position/{positionKey}/Edit/0
	 * </ul>
	 * 
	 * And for applicant:
	 * <ul>
	 *  <li>/position/79560704
	 *  <li>/positions/0
	 * </ul>
	 * 
	 * @param loginPage
	 * @throws URISyntaxException 
	 * @throws IOException 
	 */
	@Test
	@RunAsClient
	public void businessPaths() throws IOException, URISyntaxException {
		UserVO administrator = new UserRestClient(deploymentUrl).createSelectusManager("Selecta-4", true);
		
		WebDriver applicantBrowser = getWebDriver(1);
		
		LoginPage.load(browser, deploymentUrl)
			.assertOnLoginPage()
			.loginAs(administrator)
			.resume();
	
		String title = "Fac 7 " + UUID.randomUUID();
		NavigationPage navBar = NavigationPage.load(browser);
		PositionListPage positions = navBar
			.openPositionList();
		positions
			.addPosition()
			.editProfile()
			.selectLanguages(true, false, false)
			.fillMLTitles(title, title, "en")
			.fillId("AC-238")
			.fillMLDepartmentAndHomepage("Astrophysic department", null, "https://www.frentix.com")
			.fillMLDescription("Very interessant job but very dangerous", "en")
			.savePositionProfile()
			.editStatus()
			.selectStatus(PositionStatus.published)
			.savePositionStatus()
			.clickToolbarBack()
			.back();
		PositionPage position = positions
			.selectPositionInList(title);
		String externUrl = position
			.openAdminMenu()
			.editPosition()
			.editProfile()
			.getPositionURLAndClose();
		 
		int index = externUrl.lastIndexOf('/');
		String positionKey = externUrl.substring(index + 1);
		String positionUrl = deploymentUrl.toString() + "auth/Positions/0/Position/" + positionKey;
		browser.navigate().to(positionUrl);
		
		PositionPage reloadedPosition = new PositionPage(browser);
		reloadedPosition.assertPosition();
		
		String editPositionUrl = positionUrl + "/Edit/0";
		browser.navigate().to(editPositionUrl);
		
		EditPositionPage reloadedEditPosition = new EditPositionPage(browser);
		reloadedEditPosition.assertEdit();
		
		// applicant use the direct standard link
		String seleniumPositionUrl = deploymentUrl.toString() + "position/" + positionKey;
		ApplicationWizardPage applyPosition = ApplicationWizardPage.getWizard(applicantBrowser, seleniumPositionUrl);
		applyPosition.assertPositionInstruction(title); 
		
		// try /positions/0 to see the list of positions
		String seleniumPositionsUrl = deploymentUrl.toString() + "positions/0";
		ApplicationWizardPage positionList = ApplicationWizardPage.getWizard(applicantBrowser, seleniumPositionsUrl);
		positionList.assertCanChoosePositionInList(title);
	}
	
	/**
	 * The administrator delete all positions but one.
	 * @throws URISyntaxException 
	 * @throws IOException 
	 */
	@Test
	@RunAsClient
	public void deletePositions() throws IOException, URISyntaxException {
		UserVO administrator = new UserRestClient(deploymentUrl).createSelectusManager("Selecta-5", true);
		
		LoginPage.load(browser, deploymentUrl)
			.assertOnLoginPage()
			.loginAs(administrator)
			.resume();
		
		NavigationPage navBar = NavigationPage.load(browser);
		PositionListPage positions = navBar
			.openPositionList();
		
		String title = "DEL - 2 " + UUID.randomUUID();
		positions
			.addPosition()
			.editProfile()
			.selectLanguages(true, false, false)
			.fillMLTitles(title, title, "en")
			.fillId("AC-235")
			.fillMLDepartmentAndHomepage("Physics department", null, "https://www.frentix.com")
			.fillMLDescription("Very interessant job", "en")
			.savePositionProfile()
			.clickToolbarBack()
			.back();
		
		int numOfPositions = positions.numOfPositions();
		for(int i=1; i<numOfPositions; i++) {
			positions
				.deletePosition(1)
				.assertOnBackup()
				.confirmDeletePositionDefinitively();
			int afterNumOfPositions = positions.numOfPositions();
			Assert.assertEquals(numOfPositions, afterNumOfPositions + i);
		}
		numOfPositions = positions.numOfPositions();
		Assert.assertEquals(1, numOfPositions);
	}
}
