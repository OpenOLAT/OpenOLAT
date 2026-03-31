/**
 * <p>
 * Copyright (c) frentix GmbH<br>
 * http://www.frentix.com<br>
 */
package org.olat.selenium.page.selectus;

import org.olat.modules.selectus.model.PositionRole;
import org.olat.selenium.page.graphene.OOGraphene;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.Select;


/**
 * 
 * Initial date: 24.06.2015<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class CommitteeMemberWizardPage {
	
	public static final By nextBy = By.className("o_wizard_button_next");
	public static final By finishBy = By.className("o_wizard_button_finish");
	
	private WebDriver browser;
	
	public CommitteeMemberWizardPage(WebDriver browser) {
		this.browser = browser;
	}
	
	public CommitteeMemberWizardPage fillEmail(String email) {
		By emailBy = By.cssSelector(".o_sel_position_committee_email_step .o_sel_committee_email input[type='text']");
		browser.findElement(emailBy).sendKeys(email);
		return this;
	}
	
	public CommitteeMemberWizardPage fillFirstNameLastName(String firstName, String lastName) {
		By firstNameBy = By.cssSelector(".o_sel_member_firstName input[type='text']");
		browser.findElement(firstNameBy).sendKeys(firstName);
		By lastNameBy = By.cssSelector(".o_sel_member_lastName input[type='text']");
		browser.findElement(lastNameBy).sendKeys(lastName);
		return this;
	}
	
	public CommitteeMemberWizardPage fillGenderAndTypeOfUser() {
		OOGraphene.waitAndScrollTo(By.cssSelector("div.o_sel_member_typeOfUser"), browser);
		
		By genderBy = By.cssSelector(".o_sel_member_gender input[type='radio'][name='gender'][value='female']");
		browser.findElement(genderBy).click();
		By typeOfUserBy = By.cssSelector(".o_sel_member_typeOfUser input[type='radio'][name='typeOfUser'][value='external']");
		browser.findElement(typeOfUserBy).click();
		return this;
	}
	
	/**
	 * the possible role
	 * 
	 * 
	 * @param role
	 * @return
	 */
	public CommitteeMemberWizardPage selectRole(PositionRole role) {
		By emailBy = By.cssSelector(".o_sel_position_committee_email_step .o_sel_committee_role select");
		WebElement roleEl = browser.findElement(emailBy);
		new Select(roleEl).selectByValue(role.role());
		OOGraphene.waitBusy(browser);
		return this;
	}
	
	public CommitteeMemberWizardPage nextToMemberData() {
		OOGraphene.nextStep(browser);
		return this;
	}
	
	public CommitteeMemberWizardPage next() {
		OOGraphene.waitingALittleLonger();
		By buttonsBy = By.cssSelector(".modal-footer");
		OOGraphene.waitAndScrollTo(buttonsBy, browser);
		OOGraphene.waitingALittleLonger();
		browser.findElement(OOGraphene.wizardNextBy).click();
		OOGraphene.waitBusy(browser);
		return this;
	}
	
	public CommitteeMemberWizardPage finish() {
		OOGraphene.finishStep(browser);
		return this;
	}
}
