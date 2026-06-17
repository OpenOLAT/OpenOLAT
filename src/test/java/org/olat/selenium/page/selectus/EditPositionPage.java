/**
 * <p>
 * Copyright (c) frentix GmbH<br>
 * http://www.frentix.com<br>
 */
package org.olat.selenium.page.selectus;

import org.apache.logging.log4j.Logger;
import org.olat.core.logging.Tracing;
import org.olat.modules.selectus.model.PositionStatus;
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
public class EditPositionPage {
	
	private static final Logger log = Tracing.createLoggerFor(EditPositionPage.class);

	private WebDriver browser;
	
	public EditPositionPage(WebDriver browser) {
		this.browser = browser;
	}
	
	public EditPositionPage assertEdit() {
		OOGraphene.waitElement(By.className("o_sel_edit_position_status_form"), browser);
		return this;
	}
	
	public String getPositionURL() {
		By urlBy = By.cssSelector("p.form-control-static.o_sel_position_url");
		OOGraphene.waitElement(urlBy, browser);
		String url = browser.findElement(urlBy).getText();
		return url.trim();
	}

	public String getPositionURLAndClose() {
		String url = getPositionURL();
		cancelPosition();
		return url;
	}
	
	public EditPositionPage editStatus() {
		By statusBy = By.cssSelector("a.o_sel_edit_position_status");
		browser.findElement(statusBy).click();
		OOGraphene.waitBusy(browser);
		
		By statusPageEl = By.cssSelector("fieldset.o_sel_edit_position_status_form");
		OOGraphene.waitElement(statusPageEl, browser);
		return this;
	}
	
	public EditPositionPage editProfile() {
		By statusBy = By.cssSelector("a.o_sel_edit_position_profile");
		browser.findElement(statusBy).click();
		OOGraphene.waitBusy(browser);
		
		By statusPageEl = By.cssSelector("fieldset.o_sel_edit_position_form");
		OOGraphene.waitElement(statusPageEl, browser);
		return this;
	} 
	
	public EditPositionPage selectStatus(PositionStatus status) {
		By statusBy = By.cssSelector(".o_sel_position_status select");
		WebElement statusEl = browser.findElement(statusBy);
		new Select(statusEl).selectByValue(status.name());
		OOGraphene.waitingALittleLonger();
		OOGraphene.waitingALittleLonger();
		By iconBy = By.cssSelector("i.o_position_status_filter." + status.getCss());
		OOGraphene.waitElement(iconBy, browser);
		return this;
	}
	
	public EditPositionPage editEvaluations() {
		By statusBy = By.cssSelector("a.o_sel_edit_position_evaluation");
		browser.findElement(statusBy).click();
		OOGraphene.waitBusy(browser);
		By evalulationTabBy = By.cssSelector("ul.o_sel_edit_position_evaluation_tab");
		OOGraphene.waitElement(evalulationTabBy, browser);
		return this;
	}
	
	public EditPositionPage editRubrics() {
		By formBy = By.cssSelector(".o_sel_edit_position_configuration_decision_tool_form");
		OOGraphene.selectTab("o_sel_edit_position_evaluation_tab", formBy, browser);
		return this;
	}
	
	public EditPositionPage enableRubrics() {
		By rubricBy = By.cssSelector(".o_sel_position_enable_rubric input[type='checkbox']");
		OOGraphene.waitElement(rubricBy, browser);
		browser.findElement(rubricBy).click();
		OOGraphene.waitBusy(browser);
		return this;
	}
	
	public EditPositionPage fillTitleAndDescription(String title, String shortTitle, String description) {
		OOGraphene.waitTinymce(browser);
		
		By titleBy = By.cssSelector(".o_sel_position_title input[type='text']");
		browser.findElement(titleBy).sendKeys(title);
		By shortTitleBy = By.cssSelector(".o_sel_position_shorttitle input[type='text']");
		browser.findElement(shortTitleBy).sendKeys(shortTitle);
	
		OOGraphene.tinymce(description, browser);
		return this;
	}
	
	public EditPositionPage selectLanguages(Boolean en, Boolean de, Boolean fr) {
		OOGraphene.waitTinymce(browser);
		
		By enBy = By.cssSelector(".o_sel_position_languages input[type='checkbox'][value='en']");
		WebElement enEl = browser.findElement(enBy);
		OOGraphene.check(enEl, en);
		OOGraphene.waitBusy(browser);
		
		if(de != null) {
			By deBy = By.cssSelector(".o_sel_position_languages input[type='checkbox'][value='de']");
			WebElement deEl = browser.findElement(deBy);
			OOGraphene.check(deEl, de);
			OOGraphene.waitBusy(browser);
		}

		if(fr != null) {
			By frBy = By.cssSelector(".o_sel_position_languages input[type='checkbox'][value='fr']");
			WebElement frEl = browser.findElement(frBy);
			OOGraphene.check(frEl, fr);
			OOGraphene.waitBusy(browser);
		}
		
		return this;
	}
	
	public EditPositionPage fillMLTitles(String title, String shortTitle, String language) {
		OOGraphene.waitTinymce(browser);
		
		By titleBy = By.cssSelector(".o_sel_position_title_" + language + " input[type='text']");
		browser.findElement(titleBy).sendKeys(title);
		OOGraphene.waitBusy(browser);
		
		By shortTitleBy = By.cssSelector(".o_sel_position_shorttitle_" + language + " input[type='text']");
		browser.findElement(shortTitleBy).sendKeys(shortTitle);
		OOGraphene.waitBusy(browser);
		return this;
	}
	
	public EditPositionPage fillMLDescription(String description, String language) {
		String cssSelector = ".o_sel_position_descritpion_" + language;
		OOGraphene.tinymce(description, cssSelector, browser);
		return this;
	}
	
	/**
	 * Set the planning id
	 * @param id The id to set
	 * @return Itself
	 */
	public EditPositionPage fillId(String id) {
		By idBy = By.cssSelector(".o_sel_position_id input[type='text']");
		browser.findElement(idBy).sendKeys(id);
		OOGraphene.waitBusy(browser);
		return this;
	}
	
	public EditPositionPage fillDepartmentAndHomepage(String department, String homepage) {
		if(department != null) {
			By departmentBy = By.cssSelector(".o_sel_position_departement input[type='text']");
			browser.findElement(departmentBy).sendKeys(department);
			OOGraphene.waitBusy(browser);
		}
		
		if(homepage != null) {
			By homepageBy = By.cssSelector(".o_sel_position_homepage input[type='text']");
			browser.findElement(homepageBy).sendKeys(homepage);
			OOGraphene.waitBusy(browser);
		}
		return this;
	}
	
	public EditPositionPage fillMLDepartmentAndHomepage(String departmentEn, String departmentDe, String homepage) {
		if(departmentEn != null) {
			By departmentBy = By.cssSelector(".o_sel_position_departement_en input[type='text']");
			browser.findElement(departmentBy).sendKeys(departmentEn);
			OOGraphene.waitBusy(browser);
		}
		
		if(departmentDe != null) {
			By departmentBy = By.cssSelector(".o_sel_position_departement_de input[type='text']");
			browser.findElement(departmentBy).sendKeys(departmentDe);
			OOGraphene.waitBusy(browser);
		}
		
		if(homepage != null) {
			By homepageBy = By.cssSelector(".o_sel_position_homepage input[type='text']");
			browser.findElement(homepageBy).sendKeys(homepage);
			OOGraphene.waitBusy(browser);
		}
		return this;
	}
	
	public EditPositionPage cancelPosition() {
		By cancelBy = By.xpath("//fieldset[contains(@class,'o_sel_edit_position_form')]//a[contains(@onclick,'cancel')]");
		browser.findElement(cancelBy).click();
		OOGraphene.waitBusy(browser);
		return this;
	}
	
	public EditPositionPage savePositionProfile() {
		By saveBy = By.cssSelector("fieldset.o_sel_edit_position_form button.btn.btn-primary");
		browser.findElement(saveBy).click();
		OOGraphene.waitBusy(browser);
		return this;
	}
	
	public EditPositionPage savePositionDecisionToolConfiguration() {
		By saveBy = By.cssSelector("fieldset.o_sel_edit_position_configuration_decision_tool_form button.btn.btn-primary");
		browser.findElement(saveBy).click();
		OOGraphene.waitBusy(browser);
		return this;
	}
	
	public EditPositionPage savePositionStatus() {
		By saveBy = By.xpath("//fieldset[contains(@class,'o_sel_edit_position_status_form')]//button[contains(@class,'btn-primary')]/span");
		browser.findElement(saveBy).click();
		OOGraphene.waitBusy(browser);
		return this;
	}
	
	public PositionPage clickToolbarBack() {
		OOGraphene.clickBreadcrumbBack(browser);
		try {
			By positionBy = By.cssSelector("div.o_sel_position_application_list");
			OOGraphene.waitElement(positionBy, browser);
		} catch (Exception e) {
			OOGraphene.takeScreenshot("Click toolbar back", browser);
			log.error("", e);
			throw e;
		}
		return new PositionPage(browser);
	}
}
