package org.olat.selenium.page.portfolio;

import java.util.List;

import org.junit.Assert;
import org.olat.selenium.page.graphene.OOGraphene;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;


/**
 * 
 * Initial date: 09.09.2016<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class MediaCenterPage {
	
	private final WebDriver browser;

	public MediaCenterPage(WebDriver browser) {
		this.browser = browser;
	}
	
	public MediaCenterPage assertOnMedia(String name) {
		By nameBy = By.xpath("//div[contains(@class,'o_portfolio_media')]//a/span[contains(text(),'" + name + "')]");
		OOGraphene.waitElement(nameBy, 5, browser);
		List<WebElement> nameEls = browser.findElements(nameBy);
		Assert.assertFalse(nameEls.isEmpty());
		return this;
	}
	
	public MediaDetailsPage selectMedia(String name) {
		By nameBy = By.xpath("//div[contains(@class,'o_portfolio_media')]//a[span[contains(text(),'" + name + "')]]");
		OOGraphene.waitElement(nameBy, 5, browser);
		browser.findElement(nameBy).click();
		OOGraphene.waitBusy(browser);
		return new MediaDetailsPage(browser);
	}
}
