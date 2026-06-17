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

/**
 * 
 * Initial date: 24.06.2015<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class CDecisionWizardPage {

	public static final By nextBy = By.className("o_wizard_button_next");
	public static final By finishBy = By.className("o_wizard_button_finish");
	
	private WebDriver browser;
	
	public CDecisionWizardPage(WebDriver browser) {
		this.browser = browser;
	}
	
	public CDecisionWizardPage selectByFirstAndLastName(String firstName, String lastName) {
		By linkBy = By.cssSelector("td");
		By checkBy = By.cssSelector("td input[type='checkbox'][name='tb_ms']");
		By rowsBy = By.cssSelector("div.o_sel_rejection_application_list table.table tr");
		List<WebElement> rows = browser.findElements(rowsBy);

		WebElement checkEl = null;
		for(WebElement row:rows) {
			boolean first = false;
			boolean last = false;
			List<WebElement> cells = row.findElements(linkBy);
			for(WebElement cell:cells) {
				if(cell.getText().contains(firstName)) {
					first = true;
				}
				if(cell.getText().contains(lastName)) {
					last = true;
				}
			}
			if(first && last) {
				checkEl = row.findElement(checkBy);
			}
		}
		
		Assert.assertNotNull(checkEl);
		checkEl.click();
		OOGraphene.waitBusy(browser);
		return this;
	}
	
	public CDecisionWizardPage assertByFirstAndLastName(String firstName, String lastName) {
		By linkBy = By.cssSelector("td");
		By rowsBy = By.cssSelector("div.o_sel_rejection_applications_overview table.table tr");
		List<WebElement> rows = browser.findElements(rowsBy);

		boolean found = false;
		for(WebElement row:rows) {
			boolean first = false;
			boolean last = false;
			List<WebElement> cells = row.findElements(linkBy);
			for(WebElement cell:cells) {
				if(cell.getText().contains(firstName)) {
					first = true;
				}
				if(cell.getText().contains(lastName)) {
					last = true;
				}
			}
			if(first && last) {
				found = true;
			}
		}
		
		Assert.assertTrue(found);
		return this;
	}
	
	public CDecisionWizardPage next() {
		WebElement next = browser.findElement(nextBy);
		Assert.assertTrue(next.isDisplayed());
		Assert.assertTrue(next.isEnabled());
		next.click();
		OOGraphene.waitBusy(browser);
		OOGraphene.closeBlueMessageWindow(browser);
		return this;
	}
	
	public CDecisionWizardPage finish() {
		WebElement finish = browser.findElement(finishBy);
		Assert.assertTrue(finish.isDisplayed());
		Assert.assertTrue(finish.isEnabled());
		finish.click();
		OOGraphene.waitBusy(browser);
		OOGraphene.closeBlueMessageWindow(browser);
		return this;
	}

}
