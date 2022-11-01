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
package org.olat.selenium.page.course;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;
import org.olat.selenium.page.graphene.OOGraphene;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

/**
 * Drive the group task course element.
 * 
 * Initial date: 22.05.2015<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class GroupTaskPage {

	private final WebDriver browser;
	
	public GroupTaskPage(WebDriver browser) {
		this.browser = browser;
	}
	
	public GroupTaskPage assertAssignmentAvailable() {
		By assignmentBy = By.id("o_step_assignement_content");
		OOGraphene.waitElement(assignmentBy, browser);
		List<WebElement> assignementEls = browser.findElements(assignmentBy);
		Assert.assertEquals(1, assignementEls.size());
		return this;
	}
	
	public GroupTaskPage assertTask(String taskName) {
		By selectLinkBy = By.xpath("//div[@id='o_step_assignement_content']//h5//span[contains(text(),'" + taskName + "')]");
		OOGraphene.waitElement(selectLinkBy, browser);
		List<WebElement> selectLinkEls = browser.findElements(selectLinkBy);
		Assert.assertFalse(selectLinkEls.isEmpty());
		return this;
	}
	
	public GroupTaskPage assertSubmissionAvailable() {
		By assignmentBy = By.id("o_step_submit_content");
		OOGraphene.waitElement(assignmentBy, browser);
		List<WebElement> assignementEls = browser.findElements(assignmentBy);
		Assert.assertEquals(1, assignementEls.size());
		return this;
	}
	
	public GroupTaskPage confirmOptionalTask() {
		OOGraphene.waitModalDialog(browser);
		
		By confirmBy = By.cssSelector("div.o_sel_course_gta_confirm_optional_task button.btn-primary");
		OOGraphene.waitElement(confirmBy, browser);
		browser.findElement(confirmBy).click();
		OOGraphene.waitModalDialogDisappears(browser);
		OOGraphene.waitAndCloseBlueMessageWindow(browser);
		return this;
	}
	
	public GroupTaskPage selectTask(int pos) {
		By taskBy = By.xpath("//div[@id='o_step_assignement_content']//table//td//a[contains(@onclick,'select')]");
		List<WebElement> assignementEls = browser.findElements(taskBy);
		Assert.assertTrue(pos < assignementEls.size());
		assignementEls.get(pos).click();
		OOGraphene.waitAndCloseBlueMessageWindow(browser);
		return this;
	}
	
	public GroupTaskPage selectTask(String name) {
		By taskBy = By.xpath("//div[@id='o_step_assignement_content']//table//tr[td[contains(text(),'" + name + "')]]/td//a[contains(@onclick,'select')]");
		OOGraphene.waitElement(taskBy, browser);
		// Firefox seems to need double scrollTo to understand
		OOGraphene.scrollTo(By.cssSelector("#o_step_assignement_content table>tbody"), browser);
		OOGraphene.clickAndWait(taskBy, browser);
		OOGraphene.waitAndCloseBlueMessageWindow(browser);
		return this;
	}
	
	public GroupTaskPage submitFile(File file) {
		moveToSubmit(By.id("o_step_submit_content"));
		return uploadFile("o_step_submit_content", file);
	}
	
	public GroupTaskPage submitRevisedFile(File file) {
		moveToSubmit(By.id("o_step_revision_content"));
		return uploadFile("o_step_revision_content", file);
	}
	
	/**
	 * Firefox need to wait a little longer, but why?
	 * 
	 * @param submitBy The step to move by
	 */
	private void moveToSubmit(By submitBy) {
		OOGraphene.waitElement(submitBy, browser);
		OOGraphene.moveTo(submitBy, browser);
		OOGraphene.waitingALittleLonger();
	}
	
	private GroupTaskPage uploadFile(String stepId, File file) {
		By uploadButtonBy = By.cssSelector("#" + stepId + " .o_sel_course_gta_submit_file");
		OOGraphene.waitElement(uploadButtonBy, browser);
		browser.findElement(uploadButtonBy).click();
		OOGraphene.waitModalDialog(browser);
		
		By inputBy = By.cssSelector(".o_fileinput input[type='file']");
		OOGraphene.uploadFile(inputBy, file, browser);
		OOGraphene.waitBusy(browser);
		By uploadedBy = By.cssSelector(".o_sel_course_gta_upload_form .o_sel_file_uploaded");
		OOGraphene.waitElement(uploadedBy, browser);
		
		By saveButtonBy = By.cssSelector(".o_sel_course_gta_upload_form button.btn-primary");
		OOGraphene.clickAndWait(saveButtonBy, browser);
		OOGraphene.waitModalDialogDisappears(browser);
		return this;
	}
	
	public GroupTaskPage submitText(String filename, String text) {
		String startWindow = browser.getWindowHandle();
		
		By createButtonBy = By.cssSelector("#o_step_submit_content .o_sel_course_gta_create_doc");
		OOGraphene.click(createButtonBy, browser);
		OOGraphene.waitModalDialog(browser);
		
		By filenameBy = By.cssSelector(".o_sel_course_gta_doc_filename input[type='text']");
		browser.findElement(filenameBy).sendKeys(filename);
		By saveBy = By.cssSelector(".o_sel_course_gta_new_doc_form button.btn-primary");
		browser.findElement(saveBy).click();
		OOGraphene.waitBusy(browser);
		
		List<String> allHandles = new ArrayList<>(browser.getWindowHandles());
		allHandles.remove(startWindow);
		String editorHandle = allHandles.get(0);
		
		browser.switchTo().window(editorHandle);
		OOGraphene.tinymceExec(text, browser);

		By saveAndCloseDirtyBy = By.cssSelector(".o_htmleditor #o_button_saveclose a.btn.o_button_dirty");
		OOGraphene.waitElement(saveAndCloseDirtyBy, browser);
		browser.findElement(saveAndCloseDirtyBy).click();
		
		browser.switchTo().window(startWindow);
		
		OOGraphene.waitElement(By.className("o_process"), browser);
		return this;
	}
	
	public GroupTaskPage submitDocuments() {
		By submitBy = By.cssSelector("#o_step_submit_content .o_sel_course_gta_submit_docs");
		OOGraphene.clickAndWait(submitBy, browser);
		
		//confirm
		confirmDialog();
		OOGraphene.waitAndCloseBlueMessageWindow(browser);
		return this;
	}
	
	public GroupTaskPage submitRevision() {
		By submitBy = By.cssSelector("#o_step_revision_content .o_sel_course_gta_submit_revisions");
		browser.findElement(submitBy).click();
		return confirmDialog();
	}
	
	public GroupTaskPage openSolutions() {
		By solutionStep = By.cssSelector("div.o_step_solution");
		OOGraphene.waitElementPresence(solutionStep, 5, browser);
		
		By solutionLinkBy = By.cssSelector("div.o_step_solution li.o_gta_document h5>a");
		List<WebElement> buttons = browser.findElements(solutionLinkBy);
		if(buttons.isEmpty() || !buttons.get(0).isDisplayed()) {
			//open grading tab
			By collpaseBy = By.xpath("//div[contains(@class,'o_step_solution')]//button[contains(@class,'o_button_details')]");
			OOGraphene.moveTo(collpaseBy, browser);
			browser.findElement(collpaseBy).click();
		}
		OOGraphene.waitElement(solutionLinkBy, browser);
		return this;
	}
	
	public GroupTaskPage assertSolution(String solution) {
		By solutionLinkBy = By.xpath("//div[@id='o_step_solution_content']//li[contains(@class,'o_gta_document')]//h5/a/span[contains(text(),'" + solution + "')]");
		OOGraphene.waitElement(solutionLinkBy, browser);
		return this;
	}
	
	/**
	 * Confirm a yes / no dialog box
	 */
	private GroupTaskPage confirmDialog() {
		OOGraphene.waitModalDialog(browser);
		
		By confirmButtonBy = By.cssSelector("div.modal-dialog div.modal-footer a");
		browser.findElement(confirmButtonBy).click();
		OOGraphene.waitBusy(browser);
		return this;
	}
	
	public GroupTaskPage assertPassed() {
		By passedBy = By.cssSelector("#o_step_grading_content table div.o_state.o_passed");
		OOGraphene.waitElement(passedBy, browser);
		return this;
	}
}
