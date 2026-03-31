/**
 * <p>
 * Copyright (c) frentix GmbH<br>
 * http://www.frentix.com<br>
 */
package org.olat.selenium.page.selectus;

import java.net.URL;

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
public class ApplicationWizardPage {
	
	public static final String POSITION_URL_MARKER = "/position/";

	private WebDriver browser;
	private EditApplicationPage editApplication;
	
	public ApplicationWizardPage(WebDriver browser) {
		this.browser = browser;
	}
	
	public static String rewritePositionUrl(String url, URL deploymentUrl) {
		String rewritedUrl;
		int indexPosition = url.indexOf(POSITION_URL_MARKER);
		if(indexPosition > 0) {
			String relativePath = url.substring(indexPosition + 1);
			rewritedUrl = deploymentUrl + relativePath;
		} else {
			rewritedUrl = url;
		}
		return rewritedUrl;
	}
	
	public static ApplicationWizardPage getWizard(WebDriver browser, String positionUrl) {
		ApplicationWizardPage page = new ApplicationWizardPage(browser);
		page.browser.navigate().to(positionUrl);
		page.editApplication = new EditApplicationPage(browser);
		return page;
	}
	
	/**
	 * Can choose the position with the specified title in the position list?
	 * 
	 * @param title The title of a position
	 * @return Itself
	 */
	public ApplicationWizardPage assertCanChoosePositionInList(String title) {
		By optionTitleBy = By.xpath("//div[contains(@class,'o_position')]/h1[text()[contains(.,'" + title + "')]]");
		OOGraphene.waitElement(optionTitleBy, browser);
		return this;
	}
	
	/**
	 * Can select a position with the specified title in the first step
	 * of the application wizard.
	 * 
	 * @param title The title of a position
	 * @return Itself
	 */
	public ApplicationWizardPage assertCanSelectPosition(String title) {
		By titleBy = By.xpath("//div[contains(@class,'o_sel_appwizard_select_position')]//select/option[contains(text(),'" + title + "')]");
		OOGraphene.waitElement(titleBy, browser);
		return this;
	}
	
	public ApplicationWizardPage assertPositionInstruction(String title) {
		By titleBy = By.xpath("//fieldset[contains(@class,'fx_r_app_instructions')]//legend[text()[contains(.,'" + title + "')]]");
		OOGraphene.waitElement(titleBy, browser);
		return this;
	}
	
	/**
	 * 
	 * @param language The language of the user
	 * @return Itself
	 */
	public ApplicationWizardPage selectLanguage(String language) {
		By localeChooserBy = By.xpath("//select[@name='locale_chooser_SELBOX']");
		OOGraphene.waitElement(localeChooserBy, browser);
		new Select(browser.findElement(localeChooserBy)).selectByValue(language);
		OOGraphene.waitBusy(browser);
		By checkedLanguageBy = By.xpath("//select[@name='locale_chooser_SELBOX']/option[@selected='selected'][@value='" + language + "']");
		OOGraphene.waitElement(checkedLanguageBy, browser);
		return this;
	}
	
	public ApplicationWizardPage fillPerson(String title, String gender, String firsName, String lastName) {
		editApplication.fillPerson(title, gender, firsName, lastName);
		return this;
	}
	
	public ApplicationWizardPage fillNationality(String nationality) {
		editApplication.fillNationality(nationality);
		return this;
	}
	
	public ApplicationWizardPage selectNationality(String nationality) {
		editApplication.selectNationality(nationality);
		return this;
	}
	
	public ApplicationWizardPage fillAddress(boolean business, String line1, String zipCode, String city, String country) {
		editApplication.fillAddress(business, line1, country);
		editApplication.fillCity(zipCode, city);
		return this;
	}
	
	public ApplicationWizardPage fillPrivateAddress(String line1, String zipCode, String city, String country) {
		editApplication.fillPrivateAddress(line1, zipCode, city, country);
		return this;
	}
	
	public ApplicationWizardPage fillBusinessAddress(String line1, String zipCode, String city, String country) {
		editApplication.fillBusinessAddress(line1, zipCode, city, country);
		return this;
	}
	
	public ApplicationWizardPage fillPhone(String phone) {
		editApplication.fillPhone(phone);
		return this;
	}
	
	public ApplicationWizardPage fillEmail(String email) {
		editApplication.fillEmail(email);
		return this;
	}
	
	public ApplicationWizardPage fillEmail(String phone, String mobile, String email) {
		editApplication.fillEmail(email);
		editApplication.fillPhone(phone);
		editApplication.fillMobilePhone(mobile);
		return this;
	}
	
	public ApplicationWizardPage fillMaritalStatus(String status) {
		editApplication.fillMaritalStatus(status);
		return this;
	}
	
	public ApplicationWizardPage fillBirthday(int day, int month, int year) {
		editApplication.fillBirthday(day, month, year);
		return this;
	}
	
	public ApplicationWizardPage fillGender(String gender) {
		editApplication.fillGender(gender);
		return this;
	}
	
	public ApplicationWizardPage fillWorkedInAcademiaSince(String years) {
		editApplication.fillWorkedInAcademiaSince(years);
		return this;
	}
	
	public ApplicationWizardPage fillBusinessInfos(String organization, String unit, String position) {
		editApplication.fillBusinessInfos(organization, unit, position);
		return this;
	}
	
	public ApplicationWizardPage fillDissertation(String title, String date, String institution) {
		editApplication.fillDissertation(title, date, institution);
		return this;
	}
	
	public ApplicationWizardPage fillHabilitation(String title, String date, String institution) {
		editApplication.fillHabilitation(title, date, institution);
		return this;
	}
	
	public ApplicationWizardPage fillHighestDegree(String type, String year, String institution) {
		editApplication.fillHighestDegree(type, year, institution);
		return this;
	}
	
	public ApplicationWizardPage fillPublications(Integer originalPublications, Integer firstAuthorships, Integer lastAuthorships, Integer citations) {
		editApplication.fillPublications(originalPublications, firstAuthorships, lastAuthorships, citations);
		return this;
	}
	
	public ApplicationWizardPage fillFactors(Integer impactFactor, Integer hFactor) {
		editApplication.fillFactors(impactFactor, hFactor);
		return this;
	}
	
	public ApplicationWizardPage fillProjectMetadata(String acronym, String keywords, String disciplines) {
		editApplication.fillProjectMetadata(acronym, keywords, disciplines);
		return this;
	}
	
	public ApplicationWizardPage fillProjectFinancialImpacts(String impact1, String impact2, String impact3, String impact4, String impact5) {
		editApplication.fillProjectFinancialImpacts(impact1, impact2, impact3, impact4, impact5);
		return this;
	}
	
	public ApplicationWizardPage assertProjectFinancialImpactSum(int pos, String value) {
		editApplication.assertProjectFinancialImpactSum(pos, value);
		return this;
	}
	
	public ApplicationWizardPage fillProjectTitle(String title) {
		editApplication.fillProjectTitle(title);
		return this;
	}
	
	public ApplicationWizardPage fillProjectStartDate(int day) {
		editApplication.fillProjectStartDate(day);
		return this;
	}
	
	public ApplicationWizardPage fillProjectDuration(String duration) {
		editApplication.fillProjectDuration(duration);
		return this;
	}
	
	public ApplicationWizardPage fillProjectDescription(String description) {
		editApplication.fillProjectDescription(description);
		return this;
	}
	
	public ApplicationWizardPage acceptDataProtectionDisclaimer() {
		By acceptTermsBy = By.xpath("//div[contains(@class,'o_sel_accept_terms')]//input[@type='checkbox']");
		OOGraphene.waitElement(acceptTermsBy, browser);
		WebElement checkboxEl = browser.findElement(acceptTermsBy);
		OOGraphene.scrollTo(acceptTermsBy, browser);
		OOGraphene.check(checkboxEl, Boolean.TRUE);
		return this;
	}
	
	public ApplicationWizardPage acceptTerms() {
		return acceptDataProtectionDisclaimer();
	}
	
	private ApplicationWizardPage next() {
		By nextBy = By.className("o_wizard_button_next");
		OOGraphene.clickAndWait(nextBy, browser);
		return this;
	}
	
	public ApplicationWizardPage nextToInstructions() {
		next();
		By instructionsBy = By.className("fx_r_app_instructions");
		OOGraphene.waitElement(instructionsBy, browser);
		return this;
	}
	
	public ApplicationWizardPage nextToDataProtection() {
		next();
		By privacyBy = By.className("fx_r_app_dataprotection");
		OOGraphene.waitElement(privacyBy, browser);
		return this;
	}
	
	public ApplicationWizardPage nextToPersonalData() {
		next();
		By personalDataBy = By.className("o_sel_edit_person");
		OOGraphene.waitElement(personalDataBy, browser);
		return this;
	}
	
	public ApplicationWizardPage nextToAcademicalBackground() {
		next();
		By backgroundBy = By.className("o_sel_academical_background");
		OOGraphene.waitElement(backgroundBy, browser);
		return this;
	}
	
	public ApplicationWizardPage nextToProject() {
		next();
		By projectBy = By.className("o_sel_edit_project");
		OOGraphene.waitElement(projectBy, browser);
		return this;
	}
	
	public ApplicationWizardPage nextToReview() {
		next();
		By detailsBy = By.className("fx_r_app_details");
		OOGraphene.waitElement(detailsBy, browser);
		return this;
	}
	
	public ApplicationWizardPage finish() {
		OOGraphene.waitingALittleLonger();
		By finishBy = By.className("o_wizard_button_finish");
		OOGraphene.click(finishBy, browser);
		return this;
	}
	
	public ApplicationWizardPage finishNoWait() {
		By finishBy = By.className("o_wizard_button_finish");
		WebElement finishEl = browser.findElement(finishBy);
		boolean move = finishEl.getLocation().getY() > 669;
		if(move) {
			OOGraphene.scrollTo(finishBy, browser);
		}
		browser.findElement(finishBy).click();
		return this;
	}
	
	public ApplicationWizardPage waitFinish() {
		OOGraphene.waitBusy(browser);
		return this;
	}
	
	public ApplicationWizardPage assertApplicationSend() {
		By sendMsgBy = By.cssSelector(".o_success");
		OOGraphene.waitElement(sendMsgBy, browser);
		browser.findElement(sendMsgBy).click();
		return this;
	}
}
