package org.olat.selenium.page.course;

import org.olat.selenium.page.graphene.OOGraphene;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.Select;

/**
 * 
 * Initial date: 19 janv. 2023<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class ZoomConfigurationPage {
	
	private final WebDriver browser;
	
	public ZoomConfigurationPage(WebDriver browser) {
		this.browser = browser;
	}
	
	public ZoomConfigurationPage selectConfiguration() {
		By tabBy = By.cssSelector("ul.o_node_config li.o_sel_zoom_options>a");
		OOGraphene.waitElement(tabBy, browser);
		browser.findElement(tabBy).click();
		OOGraphene.waitElement(By.className("o_sel_zoom_configuration_form"), browser);
		return this;
	}
	
	public ZoomConfigurationPage selectProfile(String profile) {
		By profileBy = By.cssSelector("fieldset.o_sel_zoom_configuration_form .o_sel_zoom_profile select");
		OOGraphene.waitElement(profileBy, browser);
		new Select(browser.findElement(profileBy))
			.selectByVisibleText(profile);
		OOGraphene.waitBusy(browser);
		return this;
	}
	
	public ZoomConfigurationPage saveConfiguration() {
		By saveBy = By.cssSelector("fieldset.o_sel_zoom_configuration_form button.btn.btn-primary");
		OOGraphene.waitElement(saveBy, browser);
		browser.findElement(saveBy).click();
		OOGraphene.waitBusy(browser);
		By dirtySaveBy = By.xpath("//fieldset[contains(@class,'o_sel_zoom_configuration_form')]//button[contains(@class,'btn-primary') and not(contains(@class,'o_button_dirty'))]");
		OOGraphene.waitElement(dirtySaveBy, browser);
		return this;
	}

}
