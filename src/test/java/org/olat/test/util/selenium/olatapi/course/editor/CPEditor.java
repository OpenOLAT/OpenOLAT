package org.olat.test.util.selenium.olatapi.course.editor;

import com.thoughtworks.selenium.Selenium;

public class CPEditor extends CourseElementEditor {

	public CPEditor(Selenium selenium) {
		super(selenium);
		// TODO Auto-generated constructor stub
	}

	public void select(String title) {
		if(selenium.isElementPresent("ui=courseEditor::content_bbCP_tabLearningContentCP()")) {
			selenium.click("ui=courseEditor::content_bbCP_tabLearningContentCP()");
			selenium.waitForPageToLoad("30000");
		}		
		selenium.click("ui=courseEditor::content_bbCP_selectCreateImportCP()");
		selenium.waitForPageToLoad("30000");
		selenium.click("ui=courseEditor::commons_chooseLr_myEntries()");
		selenium.waitForPageToLoad("30000");
		selenium.click("ui=courseEditor::commons_chooseLr_chooseCP(name=" + title + ")");
		selenium.waitForPageToLoad("30000");	
	}
}
