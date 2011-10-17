package org.olat.test.util.selenium.olatapi.course.run;

import org.olat.test.util.selenium.olatapi.OLATSeleniumWrapper;

import com.thoughtworks.selenium.Selenium;

/**
 * Represents a FileDialog course element in course run.
 * 
 * @author lavinia
 *
 */
public class FileDialog extends OLATSeleniumWrapper {

	public FileDialog(Selenium selenium) {
		super(selenium);
		// TODO: LD: add check - where am I?
	}

	/**
	 * Upload file.
	 * @param fileName
	 */
	public void uploadFile(String fileName) {
		selenium.click("ui=course::content_fileDialog_uploadFile()");
		selenium.waitForPageToLoad("30000");
		selenium.type("ui=upload::fileChooser()", fileName);
		selenium.click("ui=upload::submit()");
		selenium.waitForPageToLoad("30000");
	}
	
	/**
	 * If any file present, delete it.
	 * If more files, use deleteFile(fileName)
	 */
	public void deleteSingleFile() {
		selenium.click("ui=course::content_forum_delete()");
		selenium.waitForPageToLoad("30000");
		selenium.click("ui=groups::content_deleteYes()");
		selenium.waitForPageToLoad("30000");
	}
}
