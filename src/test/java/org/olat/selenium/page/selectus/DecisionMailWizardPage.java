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
 * Initial date: 24.06.2015<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class DecisionMailWizardPage {

	public static final By nextBy = By.className("o_wizard_button_next");
	public static final By finishBy = By.className("o_wizard_button_finish");
	
	private WebDriver browser;
	
	public DecisionMailWizardPage(WebDriver browser) {
		this.browser = browser;
	}
	
	public DecisionMailWizardPage selectRejectionLevel(int level) {
		By filterBy = By.xpath("//fieldset[contains(@class,'o_sel_rejection_include_filter')]//input[@type='checkbox'][@value='" + level + "']");
		browser.findElement(filterBy).click();
		OOGraphene.waitBusy(browser);
		return this;
	}
	
	public DecisionMailWizardPage selectByEmail(String email) {
		By checkboxBy = By.xpath("//div[contains(@class,'o_sel_rejection_to_application_list')]//table[contains(@class,'table')]//tr[td[contains(text(),'" + email + "')]]/td/input[@type='checkbox'][@name='tb_ms']");
		WebElement checkEl = browser.findElement(checkboxBy);
		OOGraphene.check(checkEl, Boolean.TRUE);
		OOGraphene.waitBusy(browser);
		return this;
	}
	
	public DecisionMailWizardPage assertByEmail(String email) {
		By rowBy = By.xpath("//div[contains(@class,'o_sel_rejection_mail_to_overview')]//table[contains(@class,'table')]//tr/td[contains(text(),'" + email + "')]");
		List<WebElement> rows = browser.findElements(rowBy);
		Assert.assertEquals(1, rows.size());
		return this;
	}
	
	public DecisionMailWizardPage nextToSelect() {
		OOGraphene.nextStep(browser);
		By formBy = By.className("o_sel_rejection_to_application_list");
		OOGraphene.waitElement(formBy, browser);
		return this;
	}
	
	public DecisionMailWizardPage nextToOverview() {
		OOGraphene.nextStep(browser);
		By formBy = By.className("o_sel_rejection_mail_to_overview");
		OOGraphene.waitElement(formBy, browser);
		return this;
	}
	
	public DecisionMailWizardPage nextToTemplate() {
		OOGraphene.nextStep(browser);
		By formBy = By.className("o_sel_rejection_mail_to_template");
		OOGraphene.waitElement(formBy, browser);
		return this;
	}
	
	public DecisionMailWizardPage selectTemplate(String template) {
		By selectTemplateBy = By.id("o_fiomailtemplateform_templates_SELBOX");
		WebElement selectTemplateEl = browser.findElement(selectTemplateBy);
		new Select(selectTemplateEl).selectByValue(template);
		OOGraphene.waitBusy(browser);
		return this;
	}
	
	public DecisionMailWizardPage finish() {
		OOGraphene.finishStep(browser);
		OOGraphene.closeBlueMessageWindow(browser);
		return this;
	}
}
