package org.olat.selenium.page.course;

import java.util.List;

import org.junit.Assert;
import org.olat.selenium.page.graphene.OOGraphene;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

/**
 * Attention! The browser instance is dependent of the location. Jump
 * to the iframe need a jump back to OpenOLAT.
 * 
 * 
 * Initial date: 21 août 2017<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class LTIPage {
	
	private WebDriver browser;
	
	public LTIPage(WebDriver browser) {
		this.browser = browser;
	}
	
	public LTIPage start() {
		By startBy = By.xpath("//div[contains(@class,'o_button_group')]/a[contains(@onclick,'start')]");
		OOGraphene.waitElement(startBy, browser);
		browser.findElement(startBy).click();
		
		By iframeBy = By.cssSelector(".o_iframedisplay iframe");
		OOGraphene.waitElement(iframeBy, browser);
		
		List<WebElement> iframes = browser.findElements(iframeBy);
		browser = browser.switchTo().frame(iframes.get(0));
		
		By launchedBy = By.xpath("//p[contains(text(),'Launch Validated.')]");
		OOGraphene.waitElement(launchedBy, browser);
		
		return this;
	}
	
	public LTIPage outcomeToolProvider() {
		By outcomeToolLinkBy = By.xpath("//a[contains(@href,'tool_provider_outcome.php')]");
		OOGraphene.waitElement(outcomeToolLinkBy, browser);
		browser.findElement(outcomeToolLinkBy).click();
		return this;
	}
	
	public LTIPage sendGrade(double grade) {
		By gradeBy = By.cssSelector("input[type=text][name=grade]");
		OOGraphene.waitElement(gradeBy, browser);
		browser.findElement(gradeBy).sendKeys(Double.toString(grade));
		
		By sendGradeBy = By.cssSelector("input[type=submit][value='Send Grade']");
		browser.findElement(sendGradeBy).click();
		
		By preBy = By.tagName("pre");
		OOGraphene.waitElement(preBy, browser);
		List<WebElement> preEls = browser.findElements(preBy);
		boolean success = true;
		for(WebElement preEl:preEls) {
			String text = preEl.getText();
			if(text.contains("<imsx_codeMajor>success</imsx_codeMajor>")) {
				success |= true;
			}
		}
		Assert.assertTrue(success);
		return this;
	}

}
