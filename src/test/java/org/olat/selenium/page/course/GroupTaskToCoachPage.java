package org.olat.selenium.page.course;

import java.util.List;

import org.jboss.arquillian.drone.api.annotation.Drone;
import org.junit.Assert;
import org.olat.selenium.page.graphene.OOGraphene;
import org.olat.user.restapi.UserVO;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

/**
 * 
 * Initial date: 26.05.2015<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class GroupTaskToCoachPage {
	
	@Drone
	private WebDriver browser;
	
	public GroupTaskToCoachPage() {
		//
	}
	
	public GroupTaskToCoachPage(WebDriver browser) {
		this.browser = browser;
	}
	
	public GroupTaskToCoachPage selectBusinessGroupToCoach(String name) {
		By tableRowBy = By.cssSelector(".table tr");
		By selectLinkBy = By.xpath("//td//a[contains(@href,'select')]");
		
		List<WebElement> rows = browser.findElements(tableRowBy);
		WebElement selectLinkEl = null;
		for(WebElement row:rows) {
			if(row.getText().contains(name)) {
				selectLinkEl = row.findElement(selectLinkBy);
			}
		}
		Assert.assertNotNull(selectLinkEl);
		selectLinkEl.click();
		OOGraphene.waitBusy(browser);
		return this;
	}
	
	public GroupTaskToCoachPage selectIdentityToCoach(UserVO user) {
		By tableRowBy = By.cssSelector(".table tr");
		By selectLinkBy = By.xpath("//td//a[contains(@href,'sel3ect')]");
		
		List<WebElement> rows = browser.findElements(tableRowBy);
		WebElement selectLinkEl = null;
		for(WebElement row:rows) {
			String firstName = user.getFirstName();
			String text = row.getText();
			if(row.getText().contains(user.getFirstName())) {
				selectLinkEl = row.findElement(selectLinkBy);
			}
		}
		Assert.assertNotNull(selectLinkEl);
		selectLinkEl.click();
		OOGraphene.waitBusy(browser);
		return this;
	}
	
	public GroupTaskToCoachPage assertSubmittedDocument(String title) {
		By selectLinkBy = By.xpath("//div[@id='o_step_submit_content']//ul//a//span[contains(text(),'" + title + "')]");
		List<WebElement> documentLinkEls = browser.findElements(selectLinkBy);
		Assert.assertFalse(documentLinkEls.isEmpty());
		return this;
	}
	
	public GroupTaskToCoachPage reviewed() {
		By reviewBy = By.cssSelector("#o_step_review_content .o_sel_course_gta_reviewed");
		browser.findElement(reviewBy).click();
		OOGraphene.waitBusy(browser);
		return this;
	}
	
	public GroupTaskToCoachPage openGroupAssessment() {
		By assessmentButtonBy = By.cssSelector("#o_step_grading_content .o_sel_course_gta_assessment_button");
		browser.findElement(assessmentButtonBy).click();
		OOGraphene.waitBusy(browser);
		
		return this;
	}
	
	/**
	 * Apply passed/score to all members of the group
	 * @param passed
	 * @param score
	 * @return
	 */
	public GroupTaskToCoachPage groupAssessment(Boolean passed, Float score) {
		By applyToAllBy = By.cssSelector(".o_sel_course_gta_group_assessment_form .o_sel_course_gta_apply_to_all input[type='checkbox']");
		WebElement applyToAllEl = browser.findElement(applyToAllBy);
		OOGraphene.check(applyToAllEl, Boolean.TRUE);
		OOGraphene.waitBusy(browser);
		
		if(passed != null) {
			By passedBy = By.cssSelector(".o_sel_course_gta_group_assessment_form .o_sel_course_gta_group_passed input[type='checkbox']");
			WebElement passedEl = browser.findElement(passedBy);
			OOGraphene.check(passedEl, Boolean.TRUE);
			OOGraphene.waitBusy(browser);
		}
		
		if(score != null) {
			By scoreBy = By.cssSelector(".o_sel_course_gta_group_assessment_form .o_sel_course_gta_group_score input[type='text']");
			WebElement scoreEl = browser.findElement(scoreBy);
			scoreEl.clear();
			scoreEl.sendKeys(score.toString());
			OOGraphene.waitBusy(browser);
		}
		
		By saveBy = By.cssSelector(".o_sel_course_gta_group_assessment_form button.btn-primary");
		browser.findElement(saveBy).click();
		OOGraphene.waitBusy(browser);
		return this;
	}

}
