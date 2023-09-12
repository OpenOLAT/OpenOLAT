package org.olat.selenium.page.course;

import org.olat.selenium.page.graphene.OOGraphene;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

/**
 * 
 * Initial date: 12 sept. 2023<br>
 * @author srosse, stephane.rosse@frentix.com, https://www.frentix.com
 *
 */
public class BadgesPage {
	
	private final WebDriver browser;
	
	public BadgesPage(WebDriver browser) {
		this.browser = browser;
	}
	
	public BadgesPage assertOnBadge(String badgeName) {
		By badgeBy = By.xpath("//div[@class='o_badge_tool_row']//legend[text()[contains(.,'" + badgeName + "')]]");
		OOGraphene.waitElement(badgeBy, browser);
		return this;
	}

}
