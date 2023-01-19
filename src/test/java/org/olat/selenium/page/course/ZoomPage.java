package org.olat.selenium.page.course;

import org.olat.selenium.page.graphene.OOGraphene;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

/**
 * 
 * Initial date: 19 janv. 2023<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class ZoomPage {

	private final WebDriver browser;
	
	public ZoomPage(WebDriver browser) {
		this.browser = browser;
	}
	
	public ZoomPage assertOnZoomPanel() {
		By zoomPanelBy = By.id("zoom-run-panel");
		OOGraphene.waitElement(zoomPanelBy, browser);
		return this;
	}
}
