/**
 * <a href="https://www.openolat.org">
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
 * frentix GmbH, https://www.frentix.com
 * <p>
 */
package org.olat.selenium.page.course;

import java.util.List;

import org.olat.selenium.page.graphene.OOGraphene;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.Select;

/**
 * 
 * Initial date: 11 sept. 2023<br>
 * @author srosse, stephane.rosse@frentix.com, https://www.frentix.com
 *
 */
public class BadgeClassesPage {
	
	private WebDriver browser;
	
	public BadgeClassesPage(WebDriver browser) {
		this.browser = browser;
	}
	
	public BadgeClassesPage assertOnBadgesConfiguration() {
		By classesBy = By.cssSelector("fieldset.o_badge_classes");
		OOGraphene.waitElement(classesBy, browser);
		return this;
	}
	
	/**
	 * Create a new badge if there isn't any in the course.
	 * 
	 * @return Itself
	 */
	public BadgeClassesPage createBadgeClass() {
		By createClassBy = By.cssSelector("fieldset.o_badge_classes a.o_sel_badge_classes_create");
		OOGraphene.waitElement(createClassBy, browser);
		browser.findElement(createClassBy).click();
		
		OOGraphene.waitModalDialog(browser);
		return this;
	}
	
	/**
	 * If new badge available, 
	 * 
	 * @return Itself
	 */
	public BadgeClassesPage startingWithNewBadgeClass() {
		By startBy = By.xpath("//div[contains(@class,'o_badge_wiz_starting_point_step')]//input[@name='form.starting.point'][@value='form.create.from.scratch.title']");
		OOGraphene.waitElement(startBy, browser);
		browser.findElement(startBy).click();
		
		By tableBy = By.cssSelector(".o_badge_wiz_starting_point_step table td div.o_image");
		OOGraphene.waitElementDisappears(tableBy, 5, browser);
		return this;
	}
	
	public BadgeClassesPage nextToClasses() {
		OOGraphene.nextStep(browser);
		
		By templateBy = By.className("o_template_card");
		OOGraphene.waitElement(templateBy, browser);
		return this;
	}
	
	public BadgeClassesPage selectClass(String className) {
		By classBy = By.xpath("//div[contains(@class,'o_template_card')][div[text()[contains(.,'" + className + "')]]]");
		OOGraphene.waitElement(classBy, browser);
		browser.findElement(classBy).click();
		
		By classSelectedBy = By.xpath("//div[contains(@class,'o_template_card')][contains(@class,'o_selected')][div[text()[contains(.,'" + className + "')]]]");
		OOGraphene.waitElement(classSelectedBy, browser);
		return this;
	}
	
	public BadgeClassesPage selectClassByType(String type) {
		By classBy = By.xpath("//div[contains(@class,'o_template_card')][img[contains(@src,'" + type + "')]]");
		OOGraphene.waitElement(classBy, browser);
		browser.findElement(classBy).click();
		
		By classSelectedBy = By.xpath("//div[contains(@class,'o_template_card')][contains(@class,'o_selected')][img[contains(@src,'" + type + "')]]");
		OOGraphene.waitElement(classSelectedBy, browser);
		return this;
	}
	
	public BadgeClassesPage nextToCustomization() {
		OOGraphene.nextStep(browser);
		
		By customizationBy = By.className("o_badge_wiz_customize_step");
		OOGraphene.waitElement(customizationBy, browser);
		return this;
	}
	
	public BadgeClassesPage customize(String name) {
		By titleEl = By.cssSelector(".o_badge_wiz_customize_step .o_sel_badge_title input[type='text']");
		browser.findElement(titleEl).clear();
		OOGraphene.waitBusy(browser);
		By imageEmptyAltEl = By.cssSelector(".o_badge_wiz_customize_step img[alt='']");
		OOGraphene.waitElement(imageEmptyAltEl, browser);
		
		browser.findElement(titleEl).sendKeys(name);
		OOGraphene.waitBusy(browser);
		
		By applyEl = By.cssSelector(".o_badge_wiz_customize_step a.o_sel_badge_apply");
		browser.findElement(applyEl).click();
		
		By imageAltEl = By.cssSelector(".o_badge_wiz_customize_step img[alt='" + name + "']");
		OOGraphene.waitElement(imageAltEl, browser);
		
		return this;
	}
	
	public BadgeClassesPage nextToDetails() {
		OOGraphene.nextStep(browser);
		
		By customizationBy = By.cssSelector("div.o_wizard_steps_current_content .o_sel_badge_name input");
		OOGraphene.waitElement(customizationBy, browser);
		return this;
	}
	
	public BadgeClassesPage details(String nameSuffix, String description) {
		details(description);
		By nameBy = By.cssSelector("div.o_wizard_steps_current_content .o_sel_badge_name input[type='text']");
		browser.findElement(nameBy).sendKeys(nameSuffix);
		return this;
	}
	
	public BadgeClassesPage details(String description) {
		By descriptionmarkDownBy = By.cssSelector("div.o_wizard_steps_current_content .o_sel_badge_description div.milkdown div.ProseMirror.editor[contenteditable=true]");
		OOGraphene.waitElement(descriptionmarkDownBy, browser);
		By descriptionBy = By.cssSelector("div.o_wizard_steps_current_content .o_sel_badge_description");
		OOGraphene.markdown(descriptionBy, description, browser);
		return this;
	}
	
	public BadgeClassesPage nextToCriteria() {
		OOGraphene.nextStep(browser);
		
		By criteriaBy = By.cssSelector("div.o_badge_wiz_criteria_step");
		OOGraphene.waitElement(criteriaBy, browser);
		return this;
	}
	
	public BadgeClassesPage criteria(String summary) {
		By criteriaMarkdownBy = By.cssSelector("div.o_badge_wiz_criteria_step .o_sel_badge_criteria_summary div.milkdown div.ProseMirror.editor[contenteditable=true]");
		OOGraphene.waitElement(criteriaMarkdownBy, browser);
		By criteriaBy = By.cssSelector("div.o_badge_wiz_criteria_step .o_sel_badge_criteria_summary");
		OOGraphene.markdown(criteriaBy, summary, browser);
		return this;
	}
	
	public BadgeClassesPage criteriaAuto() {
		By automaticBy = By.cssSelector("#o_fioform_award_procedure_wr input[name='form.award.procedure'][value='automatic'][checked='checked']");
		List<WebElement> automaticEls = browser.findElements(automaticBy);
		if(automaticEls.isEmpty()) {
			By criteriaBy = By.cssSelector("#o_fioform_award_procedure_wr input[name='form.award.procedure'][value='automatic']");
			browser.findElement(criteriaBy).click();
		}
		
		By selectBy = By.id("o_fioform_condition_0_SELBOX");
		OOGraphene.waitElement(selectBy, browser);
		OOGraphene.scrollBottom(selectBy, browser);
		return this;
	}
		
	public BadgeClassesPage criteraCoursePassedAsFirstRule() {
		By selectBy = By.id("o_fioform_condition_0_SELBOX");
		WebElement selectEl = browser.findElement(selectBy);
		new Select(selectEl).selectByValue("coursePassed");
		
		By deleteBy = By.xpath("//div[contains(@class,'o_badge_wiz_criteria_step')]//a[i[contains(@class,'o_icon_delete_item')]]");
		OOGraphene.waitElement(deleteBy, browser);
		return this;
	}
	
	public BadgeClassesPage criteriaPassedCourseElementAsFirstRule(String courseNode) {
		By selectBy = By.id("o_fioform_condition_0_SELBOX");
		WebElement selectEl = browser.findElement(selectBy);
		new Select(selectEl).selectByValue("completionCriterionMet");
		
		By courseElementBy = By.id("o_fioform_condition_courseElements_0_SELBOX");
		OOGraphene.waitElement(courseElementBy, browser);
		OOGraphene.scrollBottom(courseElementBy, browser);
		WebElement courseElementEl = browser.findElement(courseElementBy);
		new Select(courseElementEl).selectByVisibleText(courseNode);
		OOGraphene.waitBusy(browser);
		return this;
	}
	
	public BadgeClassesPage criteriaPassedBadgeAsAdditionalRule(String badgeClass) {
		By selectBy = By.id("o_fioform_condition_new_SELBOX");
		WebElement selectEl = browser.findElement(selectBy);
		new Select(selectEl).selectByValue("otherBadgeEarned");
		
		By badgeBy = By.id("o_fioform_condition_badges_1_SELBOX");
		OOGraphene.waitElement(badgeBy, browser);
		WebElement badgeEl = browser.findElement(badgeBy);
		new Select(badgeEl).selectByVisibleText(badgeClass);
		OOGraphene.waitBusy(browser);
		return this;
	}
	
	public BadgeClassesPage criteriaGlobalAuto() {
		By automaticBy = By.cssSelector("#o_fioform_award_procedure_wr input[name='form.award.procedure'][value='automatic'][checked='checked']");
		List<WebElement> automaticEls = browser.findElements(automaticBy);
		if(automaticEls.isEmpty()) {
			By criteriaBy = By.cssSelector("#o_fioform_award_procedure_wr input[name='form.award.procedure'][value='automatic']");
			browser.findElement(criteriaBy).click();
		}
		
		By selectBy = By.id("o_fioform_condition_new_SELBOX");
		OOGraphene.waitElement(selectBy, browser);
		OOGraphene.scrollBottom(selectBy, browser);
		return this;
	}
	
	public BadgeClassesPage criteriaGlobalPassedCourseAsFirstRule(String course) {
		By selectBy = By.id("o_fioform_condition_new_SELBOX");
		WebElement selectEl = browser.findElement(selectBy);
		new Select(selectEl).selectByValue("coursesPassed");
		
		By courseElementBy = By.cssSelector(".o_badge_wiz_criteria_step a.btn.o_badge_course_selector_button");
		OOGraphene.waitElement(courseElementBy, browser);
		OOGraphene.scrollBottom(courseElementBy, browser);
		browser.findElement(courseElementBy).click();
		OOGraphene.waitCallout(browser, ".o_badge_course_selector");
		
		By quickSearchBy = By.cssSelector("div.o_badge_course_selector_search input.o_quick_search[type='text']");
		OOGraphene.waitElement(quickSearchBy, browser);
		browser.findElement(quickSearchBy).sendKeys(course);
		
		By courseSelectBy = By.xpath("//label[div[contains(@class,'o_badge_course_selector_option')]/div[text()[contains(.,'" + course + "')]]]/input[@type='checkbox']");
		OOGraphene.waitElement(courseSelectBy, browser);
		browser.findElement(courseSelectBy).click();
		
		By selectCoursesBy = By.cssSelector(".o_badge_course_selector .o_button_group a.btn.btn-primary");
		browser.findElement(selectCoursesBy).click();
		
		OOGraphene.waitCalloutDisappears(browser, ".o_badge_course_selector");
		return this;
	}
	
	public BadgeClassesPage nextToSummary() {
		OOGraphene.nextStep(browser);
		
		By criteriaBy = By.cssSelector("div.o_badge_wiz_summary_step");
		OOGraphene.waitElement(criteriaBy, browser);
		return this;
	}
	
	public BadgeClassesPage assertOnSummary(String name) {
		try {
			By nameBy = By.xpath("//div[contains(@class,'o_badge_wiz_summary_step')]//p[text()[contains(.,'" + name + "')]]");
			OOGraphene.waitElement(nameBy, browser);
		} catch (Exception e) {
			OOGraphene.takeScreenshot("badgesummary", browser);
			throw e;
		}
		return this;
	}
	
	public BadgeClassesPage nextToRecipients() {
		OOGraphene.nextStep(browser);
		By criteriaBy = By.cssSelector("fieldset.o_sel_badge_recipients");
		OOGraphene.waitElement(criteriaBy, browser);
		return this;
	}
	
	public BadgeClassesPage finish() {
		OOGraphene.finishStep(browser, false);
		return this;
	}
	
	public BadgeClassesPage assertOnTable(String badgeName) {
		By badgeBy = By.xpath("//fieldset[contains(@class,'o_badge_classes')]//table//td/a[text()[contains(.,'" + badgeName + "')]]");
		OOGraphene.waitElement(badgeBy, browser);
		return this;
	}

}
