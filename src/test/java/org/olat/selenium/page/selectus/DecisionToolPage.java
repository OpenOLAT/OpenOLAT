/**
 * <p>
 * Copyright (c) frentix GmbH<br>
 * http://www.frentix.com<br>
 */
package org.olat.selenium.page.selectus;

import org.olat.selenium.page.graphene.OOGraphene;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.Select;

/**
 * 
 * Initial date: 28 nov. 2016<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class DecisionToolPage {
	
	private WebDriver browser;
	
	public DecisionToolPage(WebDriver browser) {
		this.browser = browser;
	}
	
	public DecisionToolDefinitionPage manageRubric() {
		By addRubricBy = By.cssSelector("a.o_sel_decision_manage_rubrics");
		OOGraphene.waitElement(addRubricBy, browser);
		browser.findElement(addRubricBy).click();
		OOGraphene.waitBusy(browser);
		OOGraphene.waitModalDialog(browser);
		return new DecisionToolDefinitionPage(browser);
	}
	
	public DecisionToolPage selectRubricValue(String lastName, String rubricType, String value) {
		By selectBy = By.xpath("//div[contains(@class,'o_table_flexi')]//table//tr[td[text()='" + lastName + "']]/td/div/select[starts-with(@id,'o_fio" + rubricType + "')]");
		WebElement selectEl = browser.findElement(selectBy);
		new Select(selectEl).selectByValue(value);
		OOGraphene.waitBusy(browser);
		return this;
	}
	
	public DecisionToolPage assertOnRubricValue(String lastName, String value) {
		By selectBy = By.xpath("//div[contains(@class,'o_table_flexi')]//table//tr[td[text()='" + lastName + "']]/td[text()='" + value + "']");
		OOGraphene.waitElement(selectBy, 10, browser);
		return this;
	}

}
