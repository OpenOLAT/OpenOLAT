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
package org.olat.selenium.page.qti;

import java.io.File;

import org.olat.selenium.page.graphene.OOGraphene;
import org.openqa.selenium.By;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;

import uk.ac.ed.ph.jqtiplus.value.Cardinality;

/**
 * 
 * Initial date: 23 oct. 2017<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class QTI21HotspotEditorPage extends QTI21AssessmentItemEditorPage {
	
	public QTI21HotspotEditorPage(WebDriver browser) {
		super(browser);
	}
	
	public QTI21HotspotEditorPage updloadBackground(File image) {
		By inputBy = By.cssSelector(".o_fileinput input[type='file']");
		OOGraphene.uploadFile(inputBy, image, browser);
		OOGraphene.waitBusy(browser);
		String name = image.getName().substring(0, image.getName().indexOf('.'));
		By imageBy = By.xpath("//div[@class='o_filemeta'][text()[contains(.,'" + name + "')]]");
		OOGraphene.waitElement(imageBy, browser);
		return this;
	}
	
	public QTI21HotspotEditorPage setCardinality(Cardinality cardinality) {
		By cardinalityBy = By.xpath("//div[contains(@class,'o_sel_assessment_item_cardinality')]//input[@value='" + cardinality.name() + "']");
		browser.findElement(cardinalityBy).click();
		return this;
	}
	
	public QTI21HotspotEditorPage setCorrect(String name, String indexName, boolean correct) {
		By correctCheckboxBy = By.xpath("//div[contains(@class,'o_sel_assessment_item_correct_spots')]//label[contains(text(),'" + name+ "')]/input[@name='form.imd.correct.spots']");
		WebElement correctCheckboxEl = browser.findElement(correctCheckboxBy);
		OOGraphene.check(correctCheckboxEl, Boolean.valueOf(correct));
		if(correct) {
			By setCorrectBy = By.xpath("//div[contains(@class,'o_qti_hotspot_correct')]/span[@class='o_qti_hotspot_label'][text()[contains(.,'" + indexName + "')]]");
			OOGraphene.waitElementSlowly(setCorrectBy, 5, browser);
		} else {
			OOGraphene.waitBusy(browser);
		}
		return this;
	}
	
	/**
	 * Add a rectangle hotspot.
	 * 
	 * @return Itself
	 */
	public QTI21HotspotEditorPage addRectangle() {
		By addRectBy = By.xpath("//a[contains(@class,'btn-default')][i[contains(@class,'o_icon_rectangle')]]");
		browser.findElement(addRectBy).click();
		By rectangleBy = By.cssSelector("div.o_draw_rectangle");
		OOGraphene.waitElementPresence(rectangleBy, 5, browser);
		return moveToHotspotEditor();
	}
	
	public QTI21HotspotEditorPage moveToHotspotEditor() {
		OOGraphene.waitingLong();//SEL wait focus jump
		OOGraphene.moveTo(By.cssSelector("div div.o_sel_assessment_item_correct_spots"), browser);
		return this;
	}
	
	/**
	 * Resize the default circle
	 * @return Itself
	 */
	public QTI21HotspotEditorPage resizeCircle() {
		By circleBy = By.cssSelector("div.o_draw_circle");
		OOGraphene.waitElement(circleBy, browser);
		
		WebElement circleEl = browser.findElement(circleBy);
		Dimension dim = circleEl.getSize();// 20 20
		new Actions(browser)
			.moveToElement(circleEl, (dim.getWidth() / 2) - 2, 0)// 0 0
			.clickAndHold()
			.moveByOffset(40, 0)
			.release()
			.perform();
		return this;
	}
	
	/**
	 * Move a circle hotspot by the specified offsets.
	 * 
	 * @param xOffset
	 * @param yOffset
	 * @return
	 */
	public QTI21HotspotEditorPage moveCircle(int xOffset, int yOffset) {
		By circleBy = By.cssSelector("div.o_draw_circle");
		return moveElement(circleBy, xOffset, yOffset);
	}
	
	/**
	 * Move a rectangle hotspot by the specified offsets.
	 * 
	 * @param xOffset
	 * @param yOffset
	 * @return Itself
	 */
	public QTI21HotspotEditorPage moveRectangle(int xOffset, int yOffset) {
		By rectangleBy = By.cssSelector("div.o_draw_rectangle");
		return moveElement(rectangleBy, xOffset, yOffset);
	}
	
	private QTI21HotspotEditorPage moveElement(By elementBy, int xOffset, int yOffset) {
		OOGraphene.waitElement(elementBy, browser);
		OOGraphene.scrollTo(By.id("o_fiohotspot_layout_SELBOX"), browser);
		WebElement element = browser.findElement(elementBy);
		try {
			new Actions(browser)
				.moveToElement(element, 0, 0)
				.clickAndHold()
				.moveByOffset(xOffset, yOffset)
				.perform();
		} catch (Exception e) {
			OOGraphene.takeScreenshot("Move element", browser);
			throw e;
		}
		
		new Actions(browser)
			.release()
			.perform();
		return this;
	}

	public QTI21HotspotEditorPage save() {
		By saveBy = By.cssSelector("div.o_sel_hotspots_save>button.btn.btn-primary");
		OOGraphene.moveAndClick(saveBy, browser);
		OOGraphene.waitBusy(browser);
		// waits are needed for chrome on our test server
		OOGraphene.waitTinymce(browser);
		OOGraphene.scrollTop(browser);
		return this;
	}
	
	public QTI21HotspotScoreEditorPage selectScores() {
		By tabLinkBy = By.xpath("//ul[contains(@class,'o_sel_assessment_item_config')]/li[2]/a");
		By scorePanelBy = By.className("o_sel_assessment_item_options");
		try {
			OOGraphene.waitElement(tabLinkBy, browser);
			browser.findElement(tabLinkBy).click();
			OOGraphene.waitElementSlowly(scorePanelBy, 5, browser);
		} catch (Exception e) {
			try {
				OOGraphene.waitingLong();
				browser.findElement(tabLinkBy).click();
				OOGraphene.waitElementSlowly(scorePanelBy, 5, browser);
			} catch (Exception e1) {
				OOGraphene.takeScreenshot("Select scores", browser);
				OOGraphene.logs(browser);
				throw e1;
			}
		}
		return new QTI21HotspotScoreEditorPage(browser);
	}
	
	public QTI21FeedbacksEditorPage selectFeedbacks() {
		selectTab(By.className("o_sel_assessment_item_feedbacks"));
		return new QTI21FeedbacksEditorPage(browser);
	}
}
