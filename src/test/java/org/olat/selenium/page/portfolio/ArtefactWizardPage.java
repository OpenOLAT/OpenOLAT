/**
 * <a href="http://www.openolat.org">
 * OpenOLAT - Online Learning and Training</a><br>
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); <br>
 * you may not use this file except in compliance with the License.<br>
 * You may obtain a copy of the License at the
 * <a href="http://www.apache.org/licenses/LICENSE-2.0">Apache homepage</a>
 * <p>
 * Unless required by applicable law or agreed to in writing,<br>
 * software distributed under the License is distributed on an "AS IS" BASIS, <br>
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
 * See the License for the specific language governing permissions and <br>
 * limitations under the License.
 * <p>
 * Initial code contributed and copyrighted by<br>
 * frentix GmbH, http://www.frentix.com
 * <p>
 */
package org.olat.selenium.page.portfolio;

import java.io.File;
import java.util.List;

import org.jboss.arquillian.drone.api.annotation.Drone;
import org.jboss.arquillian.graphene.Graphene;
import org.junit.Assert;
import org.olat.selenium.page.graphene.OOGraphene;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

/**
 * Drive the wizard to add artefacts
 * 
 * 
 * Initial date: 01.07.2014<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class ArtefactWizardPage {
	
	public static final By nextBy = By.className("o_wizard_button_next");
	public static final By finishBy = By.className("o_wizard_button_finish");
	
	@Drone
	private WebDriver browser;
	
	public static ArtefactWizardPage getWizard(WebDriver browser) {
		By modalBy = By.className("modal-content");
		WebElement modal = browser.findElement(modalBy);
		return Graphene.createPageFragment(ArtefactWizardPage.class, modal);
	}
	
	/**
	 * Next
	 * @return this
	 */
	public ArtefactWizardPage next() {
		WebElement next = browser.findElement(nextBy);
		Assert.assertTrue(next.isDisplayed());
		Assert.assertTrue(next.isEnabled());
		next.click();
		OOGraphene.waitBusy(browser);
		OOGraphene.closeBlueMessageWindow(browser);
		return this;
	}
	
	/**
	 * Finish the wizard, wait and close the blue info box.
	 * @return this
	 */
	public ArtefactWizardPage finish() {
		WebElement finish = browser.findElement(finishBy);
		Assert.assertTrue(finish.isDisplayed());
		Assert.assertTrue(finish.isEnabled());
		finish.click();
		OOGraphene.waitBusy(browser);
		OOGraphene.waitAndCloseBlueMessageWindow(browser);
		return this;
	}
	
	public ArtefactWizardPage fillTextArtefactContent(String content) {
		OOGraphene.tinymce(content, browser);
		return this;
	}
	
	public ArtefactWizardPage uploadFile(File file) {
		By inputBy = By.cssSelector(".o_fileinput input[type='file']");
		OOGraphene.uploadFile(inputBy, file, browser);
		return this;
	}
	
	public ArtefactWizardPage fillArtefactMetadatas(String title, String description) {
		By titleBy = By.cssSelector(".o_sel_ep_artefact_metadata_title input");
		WebElement titleEl = browser.findElement(titleBy);
		titleEl.sendKeys(title);
		
		OOGraphene.tinymce(description, browser);		
		return this;
	}
	
	/**
	 * !! Doesn't work
	 * @param tags
	 * @return this
	 */
	public ArtefactWizardPage tags(String... tags) {
		By tagBy = By.cssSelector("div.o_sel_artefact_add_wizard div.bootstrap-tagsinput>input");
		WebElement tagEl = browser.findElement(tagBy);
		if(tags != null && tags.length > 0 && tags[0] != null) {
			for(String tag:tags) {
				tagEl.sendKeys(tag);
			}
		}
		return this;
	}
	
	/**
	 * Select the map > page > structure by their respective title
	 * @param mapTitle
	 * @param pageTitle
	 * @param structureTitle
	 * @return this
	 */
	public ArtefactWizardPage selectMap(String mapTitle, String pageTitle, String structureTitle) {
		select(mapTitle, By.cssSelector("span.o_tree_l1>a"));
		select(pageTitle, By.cssSelector("span.o_tree_l2>a"));
		select(structureTitle, By.cssSelector("span.o_tree_l3>a"));
		return this;
	}
	
	private void select(String title, By selector) {
		WebElement linkToClick = null;
		List<WebElement> links = browser.findElements(selector);
		for(WebElement link:links) {
			if(link.getText().contains(title)) {
				linkToClick = link;
			}
		}
		Assert.assertNotNull(linkToClick);
		linkToClick.click();
		OOGraphene.waitBusy(browser);
	}
}