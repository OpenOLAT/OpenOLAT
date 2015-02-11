package org.olat.selenium.page.qti;

import java.util.List;

import org.junit.Assert;
import org.olat.selenium.page.graphene.OOGraphene;
import org.openqa.selenium.Alert;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

/**
 * 
 * Drive the test QTI 1.2 from OpenOLAT
 * 
 * Initial date: 11.02.2015<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class QTI12Page {
	
	private WebDriver browser;
	
	private QTI12Page(WebDriver browser) {
		this.browser = browser;
	}
	
	public static QTI12Page getQTI12Page(WebDriver browser) {
		WebElement main = browser.findElement(By.id("o_main_wrapper"));
		Assert.assertTrue(main.isDisplayed());
		return new QTI12Page(browser);
	}
	
	public QTI12Page start() {
		By startBy = By.cssSelector("a.o_sel_start_qti12_test");
		WebElement startButton = browser.findElement(startBy);
		startButton.click();
		OOGraphene.waitBusy();
		return this;
	}
	
	public QTI12Page selectItem(int position) {
		By itemsBy = By.cssSelector("a.o_sel_qti_menu_item");
		List<WebElement> itemList = browser.findElements(itemsBy);
		Assert.assertTrue(itemList.size() > position);
		WebElement itemEl = itemList.get(position);
		itemEl.click();
		OOGraphene.waitBusy();
		return this;
	}
	
	public QTI12Page answerSingleChoice(int selectPosition) {
		By itemsBy = By.cssSelector("div.o_qti_item_choice_option input[type='radio']");
		List<WebElement> optionList = browser.findElements(itemsBy);
		Assert.assertTrue(optionList.size() > selectPosition);
		WebElement optionEl = optionList.get(selectPosition);
		optionEl.click();
		OOGraphene.waitBusy();
		return this;
	}
	
	public QTI12Page answerMultipleChoice(int... selectPositions) {
		By itemsBy = By.cssSelector("div.o_qti_item_choice_option input[type='checkbox']");
		List<WebElement> optionList = browser.findElements(itemsBy);
		for(int selectPosition:selectPositions) {
			Assert.assertTrue(optionList.size() > selectPosition);
			optionList.get(selectPosition).click();
		}
		OOGraphene.waitBusy();
		return this;
	}
	
	public QTI12Page answerKPrim(boolean... choices) {
		By itemsBy = By.cssSelector("table.o_qti_item_kprim input[type='radio']");
		List<WebElement> optionList = browser.findElements(itemsBy);
		Assert.assertEquals(choices.length * 2, optionList.size());
		for(int i=0; i<choices.length; i++) {
			WebElement optionTrueEl = optionList.get(i * 2);
			WebElement optionFalseEl = optionList.get((i * 2) + 1);
			if(choices[i]) {
				optionTrueEl.click();
			} else {
				optionFalseEl.click();
			}
		}
		OOGraphene.waitBusy();
		return this;
	}
	
	public QTI12Page answerFillin(String... texts) {
		By holesBy = By.cssSelector("div.o_qti_item input[type='text']");
		List<WebElement> holesList = browser.findElements(holesBy);
		Assert.assertEquals(texts.length, holesList.size());
		for(int i=0; i<texts.length; i++) {
			holesList.get(i).sendKeys(texts[i]);
		}
		OOGraphene.waitBusy();
		return this;
	}
	
	public QTI12Page saveAnswer() {
		By saveAnswerBy = By.name("olat_fosm");
		WebElement saveAnswerButton = browser.findElement(saveAnswerBy);
		saveAnswerButton.click();
		OOGraphene.waitBusy();
		return this;
	}
	
	public QTI12Page endTest() {
		By endBy = By.cssSelector("div.o_button_group.o_button_group_right a");
		WebElement endButton = browser.findElement(endBy);
		endButton.click();
		//accept and go further
		Alert alert = browser.switchTo().alert();
		alert.accept();
		OOGraphene.waitAndCloseBlueMessageWindow(browser);
		return this;
	}
	
	public QTI12Page closeTest() {
		By endBy = By.cssSelector("div.o_button_group.o_button_group_right a");
		WebElement endButton = browser.findElement(endBy);
		endButton.click();
		OOGraphene.waitBusy();
		return this;
	}
}
