/**
 * <p>
 * Copyright (c) frentix GmbH<br>
 * http://www.frentix.com<br>
 */
package org.olat.selenium.page.selectus;

import java.util.List;

import org.olat.core.util.StringHelper;
import org.olat.selenium.page.graphene.OOGraphene;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

/**
 * 
 * Initial date: 9 janv. 2019<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class OrganisationUnitsPage {
	
	private final WebDriver browser;
	
	public OrganisationUnitsPage(WebDriver browser) {
		this.browser = browser;
	}
	
	public OrganisationUnitsPage assertOnOrgUnits() {
		By tableBy = By.cssSelector("div.o_sel_org_units_admin");
		OOGraphene.waitElement(tableBy, browser);
		return this;
	}
	
	private boolean exists(String name) {
		By exists = By.xpath("//div[contains(@class,'o_sel_org_units')]/table//td[text()[contains(.,'" + name + "')]]");
		List<WebElement> rows = browser.findElements(exists);
		return !rows.isEmpty();
	}
	
	public OrganisationUnitsPage createOrgUnitML(String name, String nameDe) {
		By addButtonBy = By.cssSelector("a.o_sel_org_unit_add");
		OOGraphene.waitElement(addButtonBy, browser);
		browser.findElement(addButtonBy).click();
		OOGraphene.waitModalDialog(browser);
		
		By nameEnBy = By.cssSelector("fieldset.o_sel_org_unit_form .o_sel_org_unit_name_en input[type='text']");
		OOGraphene.waitElement(nameEnBy, browser);
		browser.findElement(nameEnBy).sendKeys(name);
		
		if(StringHelper.containsNonWhitespace(nameDe)) {
			By nameDeBy = By.cssSelector("fieldset.o_sel_org_unit_form .o_sel_org_unit_name_de input[type='text']");
			browser.findElement(nameDeBy).sendKeys(nameDe);
		}
		
		By saveBy = By.cssSelector("fieldset.o_sel_org_unit_form button.btn-primary");
		OOGraphene.click(saveBy, browser);
		
		OOGraphene.waitModalDialogDisappears(browser);
		return this;
	}
	
	public OrganisationUnitsPage createIfNotExists(String name, String nameDe) {
		assertOnOrgUnits();// make sure the table is loaded
		if(exists(name)) {
			return this;
		}
		return createOrgUnitML(name, nameDe);
	}

}
