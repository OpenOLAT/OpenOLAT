/**
* OLAT - Online Learning and Training<br>
* http://www.olat.org
* <p>
* Licensed under the Apache License, Version 2.0 (the "License"); <br>
* you may not use this file except in compliance with the License.<br>
* You may obtain a copy of the License at
* <p>
* http://www.apache.org/licenses/LICENSE-2.0
* <p>
* Unless required by applicable law or agreed to in writing,<br>
* software distributed under the License is distributed on an "AS IS" BASIS, <br>
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
* See the License for the specific language governing permissions and <br>
* limitations under the License.
* <p>
* Copyright (c) since 2004 at Multimedia- & E-Learning Services (MELS),<br>
* University of Zurich, Switzerland.
*/
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
