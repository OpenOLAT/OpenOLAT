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
 * Initial date: 29 nov. 2016<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class DecisionToolDefinitionPage {
	
	private WebDriver browser;
	
	public DecisionToolDefinitionPage(WebDriver browser) {
		this.browser = browser;
	}
	
	public DecisionToolDefinitionPage addRubric(String name, Type type, int weight, int position) {
		By addRubricBy = By.cssSelector("a.o_sel_decision_add_rubric");
		browser.findElement(addRubricBy).click();
		OOGraphene.waitBusy(browser);
		
		By textBy = By.xpath("//div[contains(@class,'o_sel_decision_definition_table')]//table//tr[contains(@id,'-" + position + "')]//input[@type='text']");
		OOGraphene.waitElement(textBy, browser);
		browser.findElement(textBy).sendKeys(name);
		
		By typeBy = By.xpath("//div[contains(@class,'o_sel_decision_definition_table')]//table//tr[contains(@id,'-" + position + "')]//select[contains(@id,'o_fiotype_')]");
		WebElement typeEl = browser.findElement(typeBy);
		new Select(typeEl).selectByValue(type.id);
		OOGraphene.waitBusy(browser);
		
		if(type != Type.text) {
			By weightBy = By.xpath("//div[contains(@class,'o_sel_decision_definition_table')]//table//tr[contains(@id,'-" + position + "')]//select[contains(@id,'o_fioweight_')]");
			WebElement weightEl = browser.findElement(weightBy);
			new Select(weightEl).selectByValue(Integer.toString(weight));
			OOGraphene.waitBusy(browser);
		}
		
		return this;
	}
	
	public DecisionToolPage saveAndClose() {
		By saveBy = By.cssSelector("button.o_sel_decision_definition_save");
		browser.findElement(saveBy).click();
		OOGraphene.waitBusy(browser);
		return new DecisionToolPage(browser);
	}
	
	public enum Type {
		stars("abc"),
		minusorPlus("-1-0-+1"),
		oneSix("1-6"),
		oneTen("1-10"),
		sixOne("6-1"),
		text("text");
		
		private final String id;
		
		private Type(String id) {
			this.id = id;
		}
		
		public String id() {
			return id;
		}
	}
}
