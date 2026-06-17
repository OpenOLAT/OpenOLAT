/**
 * <p>
 * Copyright (c) frentix GmbH<br>
 * http://www.frentix.com<br>
 */
package org.olat.selenium.page.selectus;

import org.olat.selenium.page.graphene.OOGraphene;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

/**
 * 
 * Initial date: 21 nov. 2019<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class ApplicationPage {
	
	private final WebDriver browser;
	
	public ApplicationPage(WebDriver browser) {
		this.browser = browser;
	}
	
	public ApplicationPage assertOnApplication(String lastName) {
		By lastNameBy = By.xpath("//div[contains(@class,'o_app_details_person')]//p[text()[contains(.,'" + lastName + "')]]");
		OOGraphene.waitElement(lastNameBy, browser);
		return this;
	}
	
	public ApplicationPage assertOnApplicationStatus(String status) {
		By statusBy = By.xpath("//div[contains(@class,'fx_r_app_details')]//div[contains(@class,'o_sel_application_status_infos')]/div/p[text()[contains(.,'" + status + "')]]");
		OOGraphene.waitElement(statusBy, browser);
		return this;
	}
	
	public ApplicationPage assertOnApplicationStatusComment(String comment) {
		By statusBy = By.xpath("//div[contains(@class,'fx_r_app_details')]//div[contains(@class,'o_sel_application_status_infos')]//p[text()[contains(.,'" + comment + "')]]");
		OOGraphene.waitElement(statusBy, browser);
		return this;
	}

}
