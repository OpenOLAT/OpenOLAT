/**
 * <p>
 * Copyright (c) frentix GmbH<br>
 * http://www.frentix.com<br>
 */
package org.olat.selenium.page.selectus;

import java.util.List;

import org.junit.Assert;
import org.olat.selenium.page.graphene.OOGraphene;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.Select;

/**
 * 
 * Initial date: 23.06.2015<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class PositionPage {
	
	private WebDriver browser;
	
	public PositionPage(WebDriver browser) {
		this.browser = browser;
	}
	
	public PositionPage assertPosition() {
		OOGraphene.waitElement(By.className("o_sel_position_overview"), browser);
		return this;
	}
	
	public PositionPage acceptPositionDisclaimer() {
		By acceptBy = By.cssSelector("div.modal-dialog div.o_button_group button.btn-primary");
		OOGraphene.click(acceptBy, browser);
		OOGraphene.waitModalDialogDisappears(browser);
		return this;
	}
	
	public EditPositionPage editPosition() {
		By adminMenuBy = By.cssSelector("ul.o_sel_position_admin_menu");
		OOGraphene.waitElement(adminMenuBy, browser);
		By editLinkBy = By.cssSelector("ul.o_sel_position_admin_menu a.o_sel_edit_position");
		WebElement editLink = browser.findElement(editLinkBy);
		Assert.assertTrue(editLink.isDisplayed());
		editLink.click();
		OOGraphene.waitBusy(browser);
		return new EditPositionPage(browser);
	}
	
	public PositionPage openAdminMenu() {
		By adminMenuCaretBy = By.cssSelector("a.o_sel_position_admin_menu");
		OOGraphene.waitElement(adminMenuCaretBy, browser);
		WebElement adminMenuCaretEl = browser.findElement(adminMenuCaretBy);
		Assert.assertTrue(adminMenuCaretEl.isDisplayed());
		adminMenuCaretEl.click();
		return this;
	}
	
	public EditApplicationPage addApplication() {
		By addApplicationBy = By.cssSelector("a.o_sel_add_application");
		WebElement addApplicationEl = browser.findElement(addApplicationBy);
		addApplicationEl.click();
		OOGraphene.waitBusy(browser);
		return new EditApplicationPage(browser);
	}
	
	public PositionPage selectProfile() {
		By detailsLinkBy = By.className("o_sel_position_details");
		browser.findElement(detailsLinkBy).click();
		OOGraphene.waitBusy(browser);
		return this;
	}
	
	public PositionPage selectRatingPolicy() {
		By ratingPolicyBy = By.className("o_sel_position_rating_policy");
		browser.findElement(ratingPolicyBy).click();
		OOGraphene.waitBusy(browser);
		return this;
	}
	
	/**
	 * Check if an application with the specified first name and last name is in the
	 * list of applications.
	 * 
	 * @param firstName
	 * @param lastName
	 * @return
	 */
	public PositionPage assertOnApplication(String firstName, String lastName) {
		By lastNameBy = By.xpath("//div[contains(@class,'o_sel_position_application_list')]//table[contains(@class,'table')]//tr/td/a[contains(text(),'" + lastName + "')]");
		OOGraphene.waitElement(lastNameBy, browser);
		By firstNameBy = By.xpath("//div[contains(@class,'o_sel_position_application_list')]//table[contains(@class,'table')]//tr/td/a[contains(text(),'" + firstName + "')]");
		OOGraphene.waitElement(firstNameBy, browser);
		return this;
	}
	
	public ApplicationPage selectApplication(String lastName) {
		By lastNameBy = By.xpath("//div[contains(@class,'o_sel_position_application_list')]//table[contains(@class,'table')]//tr/td/a[contains(text(),'" + lastName + "')]");
		OOGraphene.waitElement(lastNameBy, browser);
		browser.findElement(lastNameBy).click();
		OOGraphene.waitBusy(browser);
		return new ApplicationPage(browser);
	}
	
	public PositionPage assertOnProject(String title) {
		By titleBy = By.xpath("//div[contains(@class,'o_sel_position_application_list')]//table[contains(@class,'table')]//tr/td/a[contains(text(),'" + title + "')]");
		OOGraphene.waitElement(titleBy, browser);
		By descriptionIconBy = By.xpath("//div[contains(@class,'o_sel_position_application_list')]//table[contains(@class,'table')]//tr/td/a/span/i[contains(@class,'o_icon_project_description')]");
		OOGraphene.waitElement(descriptionIconBy, browser);
		return this;
	}

	public PositionPage assertOnApplicationId(int id) {
		By applicationIdBy = By.xpath("//div[contains(@class,'o_sel_position_application_list')]//table//tr//td//a[text()='" + id + "']");
		List<WebElement> applicationIds = browser.findElements(applicationIdBy);
		Assert.assertEquals(1, applicationIds.size());
		return this;
	}
	
	public PositionPage selectApplications() {
		By applicationsLinkBy = By.className("o_sel_position_applications");
		browser.findElement(applicationsLinkBy).click();
		OOGraphene.waitBusy(browser);
		return this;
	}
	
	public PositionPage rate(String lastName, String value) {
		By rateBy = By.xpath("//div[contains(@class,'o_sel_position_application_list')]//table//tr[td/a[text()='" + lastName + "']]/td//div[contains(@class,'f_rating_items')]/a[@title='" + value + "']");
		OOGraphene.waitElement(rateBy, browser).click();
		By ratedBy = By.xpath("//div[contains(@class,'o_sel_position_application_list')]//table//tr[td/a[text()='" + lastName + "']]/td//div[contains(@class,'f_rating_items')]/a[contains(@class,'o_btn_rating_on')][@title='" + value + "']");
		OOGraphene.waitElement(ratedBy, browser);
		return this;
	}
	
	public PositionPage assertCommittee(String fullName) {
		By nameLinkBy = By.xpath("//div[contains(@class,'o_sel_position_committee_list')]//table[contains(@class,'table')]/tbody/tr/td/a[contains(text(),'" + fullName + "')]");
		OOGraphene.waitElement(nameLinkBy, browser);
		return this;
	}
	
	public PositionPage assertCommitteeHead(String fullName) {
		return assertCommitteeSpecialRoles(fullName, "o_sel_position_committee_head");
	}
	
	public PositionPage assertCommitteeSecretary(String fullName) {
		return assertCommitteeSpecialRoles(fullName, "o_sel_position_committee_secretary");
	}
	
	public PositionPage assertCommitteeExOfficio(String fullName) {
		return assertCommitteeSpecialRoles(fullName, "o_sel_position_committee_exofficio");
	}
	
	private PositionPage assertCommitteeSpecialRoles(String fullName, String roleClass) {
		By nameLinkBy = By.xpath("//div[contains(@class,'o_sel_position_committee_managers')]/table[@class='table']/tbody/tr[@class='" + roleClass + "']/td/span[contains(text(),'" + fullName + "')]");
		OOGraphene.waitElement(nameLinkBy, browser);
		return this;
	}
	
	public CommitteePage selectCommittee() {
		By committeeLinkBy = By.className("o_sel_position_committee");
		OOGraphene.waitElement(committeeLinkBy, 5, browser);
		browser.findElement(committeeLinkBy).click();
		OOGraphene.waitBusy(browser);
		return new CommitteePage(browser);
	}
	
	public DecisionToolPage selectDecisionTool() {
		By decisionToolkBy = By.className("o_sel_position_decision_tool");
		OOGraphene.waitElement(decisionToolkBy, browser);
		browser.findElement(decisionToolkBy).click();
		OOGraphene.waitBusy(browser);
		return new DecisionToolPage(browser);
	}
	
	/**
	 * Start the C-Decision wizard.
	 * 
	 * @return
	 */
	public CDecisionWizardPage openDecisionWizard() {
		By decisionLinkBy = By.className("o_sel_rejection_set_decision");
		OOGraphene.waitElement(decisionLinkBy, browser);
		browser.findElement(decisionLinkBy).click();
		OOGraphene.waitBusy(browser);
		return new CDecisionWizardPage(browser);
	}
	
	public DecisionMailWizardPage openSendDecisionMailWizard() {
		By decisionLinkBy = By.className("o_sel_rejection_sendmails");
		browser.findElement(decisionLinkBy).click();
		OOGraphene.waitBusy(browser);
		return new DecisionMailWizardPage(browser);
	}
	
	public PositionPage assertIsInRejectionLog(String lastName) {
		By rejectedBy = By.xpath("//fieldset[@class='o_sel_rejection_log']//table[contains(@class,'table')]//tr/td[text()[contains(normalize-space(.),'" + lastName+ "')]]");
		OOGraphene.waitElement(rejectedBy, browser);
		return this;
	}
	
	/**
	 * The select all of a flexi table
	 * @return Itself
	 */
	public PositionPage selectAllApplications() {
		OOGraphene.flexiTableSelectAll(browser);
		return this;
	}
	
	/**
	 * The button to set the committee decision.
	 * <ul>
	 * 	<li>0: no decision
	 *  <li>3: A decision
	 *  <li>2: B decision
	 *  <li>1: A decision
	 * </ul>
	 * 
	 * @return Itself
	 */
	public PositionPage setCommitteeDecision(String decision) {
		By committeeDecisionBy = By.cssSelector("a.o_sel_batch_decision");
		OOGraphene.waitElement(committeeDecisionBy, browser);
		browser.findElement(committeeDecisionBy).click();
		OOGraphene.waitModalDialog(browser);
		
		By decisionLevelBy = By.cssSelector("div.modal-content div.o_sel_committee_decision select");
		OOGraphene.waitElement(decisionLevelBy, browser);
		WebElement decisionLevelEl = browser.findElement(decisionLevelBy);
		new Select(decisionLevelEl).selectByValue(decision);

		By saveBy = By.cssSelector("div.modal-content .o_sel_batch_decision_form button.btn-primary");
		browser.findElement(saveBy).click();
		OOGraphene.waitBusy(browser);
		return this;
	}
	
	/**
	 * Select the C-Decision segment of the position.
	 * @return
	 */
	public PositionPage selectRejection() {
		By rejectionBy = By.className("o_sel_position_mail_center");
		browser.findElement(rejectionBy).click();
		OOGraphene.waitBusy(browser);
		return this;
	}
	
	public PositionListPage back() {
		By backBy = By.cssSelector("ol.breadcrumb li.o_breadcrumb_back a");
		browser.findElement(backBy).click();
		OOGraphene.waitBusy(browser);
		
		By positionListBy = By.cssSelector("div.o_sel_position_list");
		OOGraphene.waitElement(positionListBy, 5, browser);
		WebElement main = browser.findElement(positionListBy);
		Assert.assertTrue(main.isDisplayed());
		return new PositionListPage(browser);
	}
}
