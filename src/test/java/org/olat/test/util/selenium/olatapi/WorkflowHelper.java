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
package org.olat.test.util.selenium.olatapi;

import java.io.File;
import java.net.MalformedURLException;

import org.olat.test.util.selenium.OlatLoginHelper;
import org.olat.test.util.selenium.olatapi.course.editor.CourseEditor;
import org.olat.test.util.selenium.olatapi.lr.LRDetailedView;
import org.olat.test.util.selenium.olatapi.lr.LearningResources;
import org.olat.test.util.setup.OlatLoginInfos;
import org.olat.test.util.setup.context.Context;

import com.thoughtworks.selenium.Selenium;

/**
 * This class contains helper methods used by tests in order to setup olat
 * server(s) as single vm, cluster mode-single node, cluster-mode-multiple
 * nodes.
 * <p>
 * 
 * @author Stefan
 * 
 */
public class WorkflowHelper {

	/**
	 * Find a file inside the seleniumtests dir.
	 * Returns file if any found, null otherwise.
	 * 
	 * @param srcRelativeFilePath
	 * @return
	 */
	public static File locateFile(String srcRelativeFilePath) {
		File f = new File("target/test-classes/" + srcRelativeFilePath);
		if (f.exists()) {
			return f;
		}
		f = new File("target/test-classes/" + Context.FILE_RESOURCES_PATH + srcRelativeFilePath);
		if (f.exists()) {
			return f;
		}
		return null;
	}
	
	public static void deleteAllGroupsFromAuthor(OlatLoginInfos loginInfos) {
		System.out.println("=====================================");
		System.out.println("DELETE ALL GROUPS FROM: " + loginInfos.getUsername());
		System.out.println("                                START");
		System.out.println("=====================================");
		
		Selenium selenium = Context.getContext().createSeleniumAndLogin(loginInfos);
		selenium.click("ui=tabs::groups()");
		selenium.waitForPageToLoad("30000");
		selenium.click("ui=groups::menu_myProjectGroups()");
		selenium.waitForPageToLoad("30000");
		while(selenium.isElementPresent("ui=groups::content_deleteFirstGroup()")) {
			selenium.click("ui=groups::content_deleteFirstGroup()");
			selenium.waitForPageToLoad("30000");
			selenium.click("ui=groups::content_deleteYes()");
			selenium.waitForPageToLoad("30000");
			System.out.println("Yes, we deleted a group!");
		}
		
		System.out.println("=====================================");
		System.out.println("DELETE ALL GROUPS FROM: " + loginInfos.getUsername());
		System.out.println("                                END");
		System.out.println("=====================================");
	}
	
	public static void deleteAllLearningResourcesFromAuthor(String author) {
		final long maxEnd = System.currentTimeMillis() + 900*1000 /*15 min*/;
		System.out.println("=====================================");
		System.out.println("DELETE ALL LEARNING RESOURCES FROM: " + author);
		System.out.println("                                START");
		System.out.println("=====================================");
		
		// get the list of all learning resources of the author
		Selenium selenium = Context.getContext().createSeleniumAndLogin();
		while(System.currentTimeMillis()<maxEnd) {
			selenium.click("ui=tabs::learningResources()");
			selenium.waitForPageToLoad("30000");
			selenium.click("ui=learningResources::menu_searchForm()");
			selenium.waitForPageToLoad("30000");			
			selenium.type("ui=commons::flexiForm_labeledTextInput(formElementLabel=Author)", author);
			selenium.click("ui=commons::flexiForm_genericLink(buttonLabel=Search)");
			selenium.waitForPageToLoad("30000");
		
			// now loop through the list until there is no learning resource left to be deleted
			if (!selenium.isElementPresent("ui=learningResources::content_firstLearningResourceInTable()")) {
				break;
			}		
			selenium.click("ui=learningResources::content_firstLearningResourceInTable()");
			selenium.waitForPageToLoad("30000");
			selenium.click("ui=learningResources::toolbox_learningResource_delete()");
			selenium.waitForPageToLoad("30000");
			selenium.click("ui=groups::content_deleteYes()");
			selenium.waitForPageToLoad("30000");
			System.out.println("YESSS, WE DELETED A RESOURCE!!! ");
			final String bodyText = selenium.getBodyText(); 
			if (bodyText.contains("cannot be deleted")) {
				// fetch 'Used in course "CourseImportTestCourse-1227697859073"'
				String nameOfReferredCourse = bodyText.substring(bodyText.indexOf("Used in course")+16);
				nameOfReferredCourse = nameOfReferredCourse.substring(0, nameOfReferredCourse.indexOf("\""));
				System.out.println("Cannot delete resource since referenced in: " + nameOfReferredCourse);
				selenium.click("ui=dialog::OK()");
				selenium.click("ui=tabs::learningResources()");
				selenium.waitForPageToLoad("30000");
				selenium.click("ui=learningResources::menu_searchForm()");
				selenium.waitForPageToLoad("30000");				
				selenium.type("ui=commons::flexiForm_labeledTextInput(formElementLabel=Title of learning resource)", nameOfReferredCourse);					
				selenium.type("ui=commons::flexiForm_labeledTextInput(formElementLabel=Author)", author);
				selenium.click("ui=commons::flexiForm_genericLink(buttonLabel=Search)");
				selenium.waitForPageToLoad("30000");
				if (!selenium.isElementPresent("ui=learningResources::content_firstLearningResourceInTable()")) {
					//throw new AssertionError("Got an ERROR box saying that the course is referenced. Tried to delete the referred course. But can't find it. Other author?");
					System.out.println("No Referred Course found! Maybe already deleted. Course deletion takes longer since 14.01.2010");
				} else {
				  selenium.click("ui=learningResources::content_firstLearningResourceInTable()");
				  selenium.waitForPageToLoad("30000");
				  selenium.click("ui=learningResources::toolbox_learningResource_delete()");
				  selenium.waitForPageToLoad("30000");
				  selenium.click("ui=groups::content_deleteYes()");
				  selenium.waitForPageToLoad("30000");
				  System.out.println("Referred Course deleted!");
				}
				
			}
		}
		selenium.click("ui=tabs::logOut()");
		selenium.waitForPageToLoad("30000");
		selenium.close();
		selenium.stop();
		
		System.out.println("=====================================");
		System.out.println("DELETE ALL LEARNING RESOURCES FROM: " + author);
		System.out.println("                                 END");
		System.out.println("=====================================");
	}
	
	/**
	 * Deletes all learning resources found with the author + resourceTitle search filter.
	 * @param author
	 * @param resourceTitle
	 */
	public static void deleteLearningResources(String author, String resourceTitle) {
		System.out.println("=====================================");
		System.out.println("DELETE LEARNING RESOURCES FROM: " + author + " WITH TITLE: " + resourceTitle);
		System.out.println("                                START");
		System.out.println("=====================================");
		
		// get the list of all learning resources of the author
		Selenium selenium = Context.getContext().createSeleniumAndLogin();
		selenium.click("ui=tabs::learningResources()");
		selenium.waitForPageToLoad("30000");
		selenium.click("ui=learningResources::menu_searchForm()");
		selenium.waitForPageToLoad("30000");
		selenium.type("ui=commons::flexiForm_labeledTextInput(formElementLabel=Author)", author);		
		selenium.type("ui=commons::flexiForm_labeledTextInput(formElementLabel=Title of learning resource)", resourceTitle);		
		selenium.click("ui=commons::flexiForm_genericLink(buttonLabel=Search)");
		selenium.waitForPageToLoad("30000");
		
		// now loop through the list until there is no learning resource left to be deleted
		while(selenium.isElementPresent("ui=learningResources::content_firstLearningResourceInTable()")) {
			selenium.click("ui=learningResources::content_firstLearningResourceInTable()");
			selenium.waitForPageToLoad("30000");
			selenium.click("ui=learningResources::toolbox_learningResource_delete()");
			selenium.waitForPageToLoad("30000");
			selenium.click("ui=groups::content_deleteYes()");
			selenium.waitForPageToLoad("30000");
		}
		selenium.click("ui=tabs::logOut()");
		selenium.waitForPageToLoad("30000");
		selenium.close();
		selenium.stop();
		
		System.out.println("=====================================");
		System.out.println("DELETE ALL LEARNING RESOURCES FROM: " + author + " WITH TITLE: " + resourceTitle);
		System.out.println("                                 END");
		System.out.println("=====================================");
	}
	

	public static void deleteAllCoursesNamed(String str) {
		System.out.println("=====================================");
		System.out.println("DELETE ALL COURSES NAMED: " + str);
		System.out.println("                                START");
		System.out.println("=====================================");
		Selenium selenium = Context.getContext().createSeleniumAndLogin();
		int cnt = 0;
		while (true) {
			selenium.click("ui=tabs::learningResources()");
			selenium.waitForPageToLoad("30000");
			selenium.click("ui=learningResources::menu_searchForm()");
			selenium.waitForPageToLoad("30000");			
			selenium.type("ui=commons::flexiForm_labeledTextInput(formElementLabel=Title of learning resource)", str);			
			selenium.click("ui=commons::flexiForm_genericLink(buttonLabel=Search)");
			selenium.waitForPageToLoad("30000");
			if (!selenium.isElementPresent("ui=learningResources::content_clickLearningResource(nameOfLearningResource=" + str + ")")) {
				break;
			}
			selenium.click("ui=learningResources::content_showDetailedView(nameOfLearningResource=" + str + ")");			
			selenium.waitForPageToLoad("30000");
			selenium.click("ui=learningResources::toolbox_learningResource_delete()");
			selenium.waitForPageToLoad("30000");
			selenium.click("ui=groups::content_deleteYes()");
			selenium.waitForPageToLoad("30000");
			cnt++;
		}
		selenium.close();
		selenium.stop();
		System.out.println("=====================================");
		System.out.println("DELETE ALL COURSES NAMED: " + str);
		System.out.println(" (deleted " + cnt + " courses)");
		System.out.println("                                 DONE");
		System.out.println("=====================================");
	}

	public static OlatLoginInfos createUserIfNotExists(
			OlatLoginInfos loginInfos, String username, String password,
			boolean isSystemUser, boolean userManagementRole,
			boolean groupManagementRole, boolean authorRole,
			boolean systemAdminRole) throws InterruptedException {

		if (Context.getContext().getStandardAdminOlatLoginInfos(1)
				.getUsername().equals(username)) {
			throw new IllegalArgumentException(
					"Thou shall not use an existing username! (" + username
							+ ")");
		}
		if (Context.getContext().getStandardAuthorOlatLoginInfos(1)
				.getUsername().equals(username)) {
			throw new IllegalArgumentException(
					"Thou shall not use an existing username! (" + username
							+ ")");
		}
		if (Context.getContext().getStandardStudentOlatLoginInfos(1)
				.getUsername().equals(username)) {
			throw new IllegalArgumentException(
					"Thou shall not use an existing username! (" + username
							+ ")");
		}
		if (Context.getContext().getStandardGuestOlatLoginInfos(1)
				.getUsername().equals(username)) {
			throw new IllegalArgumentException(
					"Thou shall not use an existing username! (" + username
							+ ")");
		}

		if (password.length() < 4) {
			throw new IllegalArgumentException(
					"password must be at least 4 characters");
		}
		if (!password.matches(".*[0-9].*")) {
			throw new IllegalArgumentException(
					"password must contain an numerical value as well mate");
		}
		if (username.matches(".*[A-Z].*")) {
			throw new IllegalArgumentException(
					"username must not contain CAPITAL letters!");
		}
		System.out.println("connecting to " + loginInfos.getSeleniumHostname()
				+ "...");

		Selenium selenium = Context.getContext().createSelenium(loginInfos);
		System.out.println("connected.");

		System.out.println("Logging in to " + loginInfos.getFullOlatServerUrl()
				+ "...");
		OlatLoginHelper.olatLogin(selenium, loginInfos);
		System.out.println("Logged in to " + loginInfos.getFullOlatServerUrl()
				+ ".");

		selenium.click("ui=tabs::userManagement()");
		selenium.waitForPageToLoad("30000");		
		selenium.type("ui=commons::flexiForm_labeledTextInput(formElementLabel=User name)", username);
		selenium.click("ui=commons::flexiForm_genericLink(buttonLabel=Search)");
		selenium.waitForPageToLoad("30000");

		if (selenium.isTextPresent("No user was found with these attributes.")) {
			// then create the user
			System.out.println("Create the user '" + username
					+ "', couldn't find it.");
			selenium.click("ui=userManagement::menu_createUser()");
			selenium.waitForPageToLoad("30000");

			selenium.type("ui=commons::flexiForm_labeledTextInput(formElementLabel=User name)", username);	
			selenium.type("ui=commons::flexiForm_labeledTextInput(formElementLabel=First name)", username);
			selenium.type("ui=commons::flexiForm_labeledTextInput(formElementLabel=Last name)", "GenByOlatServerSetupHelper");			
			selenium.type("ui=commons::flexiForm_labeledTextInput(formElementLabel=E-mail)", username + "" + System.currentTimeMillis() + "_seleniumuser@olat.uzh.ch");
			selenium.type("ui=commons::flexiForm_labeledPasswordInput(formElementLabel=Password)", password);
			selenium.type("ui=commons::flexiForm_labeledPasswordInput(formElementLabel=Verify password)", password);
			selenium.click("ui=userManagement::content_createUser_save()");
			selenium.waitForPageToLoad("30000");
			selenium.click("ui=tabs::userManagement()");
			selenium.waitForPageToLoad("30000");
			selenium.type("ui=commons::flexiForm_labeledTextInput(formElementLabel=User name)", username);
			selenium.click("ui=commons::flexiForm_genericLink(buttonLabel=Search)");
			selenium.waitForPageToLoad("30000");

			if (selenium
					.isTextPresent("No user was found with these attributes.")) {
				throw new IllegalStateException("Couldnt create user "
						+ username);
			}
			// perfect!
		} else {
			// otherwise select the user and make sure the rights are set
			// correctly
			System.out.println("User '" + username + "' already exists.");
		}

		// now adjust the role if necessary
		selenium.click("ui=tabs::userManagement()");
		selenium.waitForPageToLoad("30000");
		selenium.type("ui=commons::flexiForm_labeledTextInput(formElementLabel=User name)", username);		
		selenium.click("ui=commons::flexiForm_genericLink(buttonLabel=Search)");
		selenium.waitForPageToLoad("30000");
		selenium
				.click("ui=commons::usertable_userlist_selectUserName(nameOfUser="
						+ username + ")");
		selenium.waitForPageToLoad("30000");

		selenium.click("ui=userManagement::content_userdetail_roles()");
		selenium.waitForPageToLoad("30000");

		if (userManagementRole != selenium
				.isChecked("ui=userManagement::content_userdetail_roles_isUsermanager()")) {
			if (userManagementRole) {
				selenium
						.check("ui=userManagement::content_userdetail_roles_isUsermanager()");
			} else {
				selenium
						.uncheck("ui=userManagement::content_userdetail_roles_isUsermanager()");
			}
		}
		if (groupManagementRole != selenium
				.isChecked("ui=userManagement::content_userdetail_roles_isGroupmanager()")) {
			if (groupManagementRole) {
				selenium
						.check("ui=userManagement::content_userdetail_roles_isGroupmanager()");
			} else {
				selenium
						.uncheck("ui=userManagement::content_userdetail_roles_isGroupmanager()");
			}
		}
		if (authorRole != selenium
				.isChecked("ui=userManagement::content_userdetail_roles_isAuthor()")) {
			if (authorRole) {
				selenium
						.check("ui=userManagement::content_userdetail_roles_isAuthor()");
			} else {
				selenium
						.uncheck("ui=userManagement::content_userdetail_roles_isAuthor()");
			}
		}
		if (systemAdminRole != selenium
				.isChecked("ui=userManagement::content_userdetail_roles_isAdmin()")) {
			if (systemAdminRole) {
				selenium
						.check("ui=userManagement::content_userdetail_roles_isAdmin()");
			} else {
				selenium
						.uncheck("ui=userManagement::content_userdetail_roles_isAdmin()");
			}
		}

		selenium.click("ui=commons::flexiForm_saveButton()");
		selenium.waitForPageToLoad("30000");
		selenium.close();
		selenium.stop();

		try {
			return new OlatLoginInfos(loginInfos.getSeleniumHostname(),
					loginInfos.getSeleniumBrowserId(), loginInfos
							.getFullOlatServerUrl(), username, password);
		} catch (MalformedURLException e) {
			throw new RuntimeException(e);
		}

	}

	/**
	 * 1. Imports course if not already imported 2. Modifies access - change
	 * course access to "All registered OLAT users" 3. Publishes course
	 * 
	 * @param zippedCourse
	 * @param newTitleOfCourse
	 * @param newDescriptionOfCourse
	 * @throws InterruptedException
	 */
	public static void importCourse(File zippedCourse, String newTitleOfCourse,
			String newDescriptionOfCourse) throws InterruptedException {
		// check if course with newTitleOfCourse exists
		boolean checkIfExists = false;
		Selenium selenium_0 = Context.getContext().createSeleniumAndLogin();
		selenium_0.click("ui=tabs::learningResources()");
		selenium_0.waitForPageToLoad("30000");
		selenium_0.click("ui=learningResources::menu_searchForm()");
		selenium_0.waitForPageToLoad("30000");		
		selenium_0.type("ui=commons::flexiForm_labeledTextInput(formElementLabel=Title of learning resource)", newTitleOfCourse);		
		selenium_0.click("ui=commons::flexiForm_genericLink(buttonLabel=Search)");
		selenium_0.waitForPageToLoad("30000");
		checkIfExists = (selenium_0.isTextPresent(newTitleOfCourse));
		selenium_0.close();
		selenium_0.stop();
		if (checkIfExists) {
			System.out.println("Course with title " + newTitleOfCourse
					+ " already exists, no need to import it!");
			return;
		}

		String remoteFile = Context.getContext().provideFileRemotely(zippedCourse);
		
		System.out.println("===================");
		System.out.println("Course Import Start");
		System.out.println("       Course: " + newTitleOfCourse);
		System.out.println("       File:   " + zippedCourse.getAbsolutePath());
		System.out.println("       Remote: " + remoteFile);
		System.out.println("===================");

		Selenium selenium = Context.getContext().createSeleniumAndLogin();
		selenium.click("ui=tabs::learningResources()");
		selenium.waitForPageToLoad("30000");
		selenium.click("ui=learningResources::toolbox_import_course()");
		selenium.waitForPageToLoad("30000");
		selenium.click("ui=learningResources::courseImport_uploadFile()");
		selenium.waitForPageToLoad("30000");
		selenium.type("ui=upload::fileChooser()",remoteFile);
		selenium.click("ui=upload::submit()");
		selenium.waitForPageToLoad("60000");
		
		while (!selenium.isElementPresent("ui=learningResources::dialog_title()")) {
			for (int second = 0;; second++) {
				if (second >= 120)
					break;
				try {
					if (selenium
							.isTextPresent("How do you wish to proceed?"))
						break;
				} catch (Exception e) {
				}
				Thread.sleep(500);
			}
			Thread.sleep(2000);
			selenium.click("ui=learningResources::courseImport_importReferencesImport()");
			selenium.waitForPageToLoad("30000");
			selenium.click("ui=learningResources::courseImport_importReferencesContinue()");
			selenium.waitForPageToLoad("30000");
		}
		// until the import is done
		selenium.type("ui=learningResources::dialog_title()", newTitleOfCourse);
		//the description shows up in an iframe
		//selenium.selectFrame("//iframe[contains(@src,'javascript:\"\"')]");
		selenium.click("ui=learningResources::dialog_description()");
		selenium.type("ui=learningResources::dialog_description()",	newDescriptionOfCourse);
		//selenium.selectFrame("relative=top");			
		selenium.click("ui=commons::save()");
		selenium.waitForPageToLoad("60000");
		Thread.sleep(5000);
		selenium.click("ui=courseEditor::publishDialog_next()");
		selenium.waitForPageToLoad("30000");
		selenium.click("ui=learningResources::dialog_startNo()");
		//selenium.waitForPageToLoad("30000");
		
		selenium.click("ui=tabs::home()");
		selenium.waitForPageToLoad("30000");
		selenium.close();
		selenium.stop();
		System.out.println("=================");
		System.out.println("Course Import End");
		System.out.println("=================");

		// modify property - change course access to "All registered OLAT users"
		Selenium selenium_1 = Context.getContext().createSeleniumAndLogin();
		selenium_1.click("ui=tabs::learningResources()");
		selenium_1.waitForPageToLoad("30000");
		selenium_1.click("ui=learningResources::menu_searchForm()");
		selenium_1.waitForPageToLoad("30000");		
		selenium_1.type("ui=commons::flexiForm_labeledTextInput(formElementLabel=Title of learning resource)", newTitleOfCourse);	
		selenium_1.click("ui=commons::flexiForm_genericLink(buttonLabel=Search)");
		selenium_1.waitForPageToLoad("30000");
		selenium_1.click("ui=learningResources::content_showDetailedView(nameOfLearningResource=" + newTitleOfCourse + ")");		
		selenium_1.waitForPageToLoad("30000");
		selenium_1
				.click("ui=learningResources::toolbox_learningResource_modifyProperties()");
		selenium_1.waitForPageToLoad("30000");
		selenium_1.click("ui=learningResourcesModifieProperties::accessAllRegistered()");
		selenium_1.click("ui=commons::flexiForm_saveButton()");
		selenium_1.waitForPageToLoad("30000");
		
		//select evidence of achievement and enable it if not already enabled
		selenium_1.click("ui=learningResourcesModifieProperties::evidenceOfAchievement()");
		selenium_1.waitForPageToLoad("30000");
		boolean isEfficiencyStatementEnabled = selenium_1.isChecked("ui=learningResourcesModifieProperties::evidenceOfAchievementEnabled()");
		if(!isEfficiencyStatementEnabled) {			
			selenium_1.click("ui=learningResourcesModifieProperties::evidenceOfAchievementEnabled()");
			selenium_1.click("ui=commons::flexiForm_saveButton()");
			selenium_1.waitForPageToLoad("30000");
			selenium_1.click("ui=learningResources::dialog_yes()");
			selenium_1.waitForPageToLoad("30000");
		}
		
		selenium_1.click("ui=overlay::overlayClose()");
		selenium_1.waitForPageToLoad("30000");
		//new step in modify properties 
		selenium_1.click("ui=learningResources::dialog_yes()");
		selenium_1.waitForPageToLoad("30000");
		
		// publish course
		selenium_1.click("ui=learningResources::content_showContent()");
		Thread.sleep(3000);
		selenium_1.waitForPageToLoad("60000");
		selenium_1.click("ui=course::toolbox_courseTools_courseEditor()");
		Thread.sleep(1000);
		selenium_1.waitForPageToLoad("30000");
		selenium_1.click("ui=courseEditor::toolbox_editorTools_publish()");
		selenium_1.waitForPageToLoad("30000");
		selenium_1.click("ui=courseEditor::publishDialog_selectall()");
		selenium_1.waitForPageToLoad("30000");
		selenium_1.click("ui=courseEditor::publishDialog_next()");
		selenium_1.waitForPageToLoad("30000");
		selenium_1.click("ui=courseEditor::publishDialog_finish()");
		selenium_1.waitForPageToLoad("30000");
		selenium_1.click("ui=courseEditor::toolbox_editorTools_closeEditor()");
		selenium_1.waitForPageToLoad("30000");
		selenium_1.close();
		selenium_1.stop();
	}

	/**
	 * Administrator adds user with username as owner of the course with
	 * courseName.
	 * 
	 * @param username
	 * @param courseName
	 * @throws Exception
	 */
	public static void addOwnerToLearningResource(String username, String courseName) throws Exception {
		Selenium selenium = Context.getContext().createSeleniumAndLogin(
				Context.getContext().getStandardAdminOlatLoginInfos(1));

		selenium.click("ui=tabs::learningResources()");
		selenium.waitForPageToLoad("30000");
		selenium.click("ui=learningResources::menu_searchForm()");
		selenium.waitForPageToLoad("30000");		
		selenium.type("ui=commons::flexiForm_labeledTextInput(formElementLabel=Title of learning resource)", courseName);
		selenium.click("ui=commons::flexiForm_genericLink(buttonLabel=Search)");
		selenium.waitForPageToLoad("30000");
		selenium.click("ui=learningResources::content_showDetailedView(nameOfLearningResource=" + courseName + ")");
		selenium.waitForPageToLoad("30000");
		selenium
				.click("ui=learningResources::toolbox_learningResource_assignOwners()");
		selenium.waitForPageToLoad("30000");
		selenium
				.click("ui=learningResources::toolbox_learningResource_assignOwners_addOwner()");
		selenium.waitForPageToLoad("30000");		
		selenium.type("ui=commons::flexiForm_labeledTextInput(formElementLabel=User name)", username);
		selenium.click("ui=commons::flexiForm_genericLink(buttonLabel=Search)");
		selenium.waitForPageToLoad("30000");
		selenium
				.click("ui=commons::usertable_adduser_checkUsername(nameOfUser="
						+ username + ")");
		selenium.click("ui=commons::usertable_adduser_choose()");
		selenium.click("ui=overlay::overlayClose()");
		selenium.click("ui=tabs::logOut()");
		selenium.waitForPageToLoad("30000");
	}

	/**
	 * admnistrator adds user as tutor of the group
	 * 
	 */
	public static void addTutorToGroup(String tutorUsername, String courseName,
			String groupName) {
		Selenium selenium = Context.getContext().createSeleniumAndLogin(
				Context.getContext().getStandardAdminOlatLoginInfos(1));

		openCourseAfterLogin(selenium, courseName);
		selenium.click("ui=course::toolbox_courseTools_groupManagement()");
		selenium.waitForPageToLoad("30000");
		selenium.click("ui=groupManagement::menu_allLearningGroups()");
		selenium.waitForPageToLoad("30000");
		selenium
				.click("ui=groupManagement::content_learningGroupTable_selectGroup(nameOfGroup="
						+ groupName + ")");
		selenium.waitForPageToLoad("30000");
		selenium.click("ui=group::menu_administration()");
		selenium.waitForPageToLoad("30000");
		selenium.click("ui=group::content_members_tabMembers()");
		selenium.waitForPageToLoad("30000");
		selenium
				.click("ui=groupManagement::content_learningGroupsEditor_addMembers_addTutor()");
		selenium.waitForPageToLoad("30000");		
		selenium.type("ui=commons::flexiForm_labeledTextInput(formElementLabel=User name)", tutorUsername);
		selenium.click("ui=commons::flexiForm_genericLink(buttonLabel=Search)");
		selenium.waitForPageToLoad("30000");
		selenium
				.click("ui=groupManagement::content_learningGroupsEditor_addMembers_checkUser(username="
						+ tutorUsername + ")");
		selenium.click("ui=commons::usertable_adduser_choose()");
		selenium.waitForPageToLoad("30000");
		//per default is "Send e-mail" not checked
		//selenium.check("ui=commons::flexiForm_labeledCheckbox(formElementLabel=" + Send e-mail + ")");
		selenium.click("ui=commons::usertable_adduser_finish()");		
		selenium.click("ui=tabs::logOut()");
		selenium.waitForPageToLoad("30000");
	}

	/**
	 * Enter "assessmentTool" course.
	 * 
	 * @param selenium_
	 */
	public static void openCourseAfterLogin(Selenium selenium_, String courseName) {
		selenium_.click("ui=tabs::learningResources()");
		selenium_.waitForPageToLoad("30000");
		selenium_.click("ui=learningResources::menu_searchForm()");
		selenium_.waitForPageToLoad("30000");		
		selenium_.type("ui=commons::flexiForm_labeledTextInput(formElementLabel=Title of learning resource)", courseName);
		selenium_.click("ui=commons::flexiForm_genericLink(buttonLabel=Search)");
		selenium_.waitForPageToLoad("30000");
		selenium_.click("ui=learningResources::content_clickLearningResource(nameOfLearningResource=" + courseName + ")");		
		selenium_.waitForPageToLoad("30000");		
	}
	
	/**
	 * Delete evidence of achievement for any course containing courseName and user.
	 * @param courseName
	 * @param userName
	 */
	public static void deleteEvidencesOfAchievement(String courseName, OlatLoginInfos olatLoginInfos) {
		System.out.println("OlatServerSetupHelper - START deleteEvidenceOfAchievement");
		OLATWorkflowHelper workflow = Context.getContext().getOLATWorkflowHelper(olatLoginInfos);
		workflow.getHome().getEvidencesOfAchievement().deleteAll(courseName);		
		System.out.println("OlatServerSetupHelper - END deleteEvidenceOfAchievement");
	}
	
	/**
	 * Helper method.
	 * Makes a copy of the course courseName, with the given name, and publishes as public visible.
	 * @param context
	 * @param courseName
	 * @param cloneCourseName
	 * @return returns true if course was successfully cloned.
	 */
	public static boolean cloneCourse(Context context, String courseName, String cloneCourseName) {
		OLATWorkflowHelper workflow = context.getOLATWorkflowHelper(context.getStandardAdminOlatLoginInfos(1));
		LRDetailedView lRDetailedView = workflow.getLearningResources().searchResource(courseName, null);
		LearningResources learningResources = lRDetailedView.copyLR(cloneCourseName, cloneCourseName + " description");
		LRDetailedView clonedLRDetailedView = learningResources.searchMyResource(cloneCourseName);
		CourseEditor courseEditor = clonedLRDetailedView.showCourseContent().getCourseEditor();
		courseEditor.publishCourse();
		//close course editor to release the lock
		courseEditor.closeToCourseRun().close(cloneCourseName);		
		return true;
	}
	
	/*public static void deleteAllFromMyEntriesStartingWith(OlatLoginInfos loginInfos, String str) {
	System.out.println("=====================================");
	System.out.println("DELETE ALL FROM MYENTRIES STARTING WITH: " + str + " for user "+loginInfos.getUsername());
	System.out.println("                                START");
	System.out.println("=====================================");
	Selenium selenium = Context.getContext().createSeleniumAndLogin(loginInfos);
	int cnt = 0;
	boolean all = false;
	while (true) {
		System.out.println("COUNTER: "+cnt);
		selenium.click("ui=tabs::learningResources()");
		selenium.waitForPageToLoad("30000");
		selenium.click("ui=learningResources::menu_myEntries()");
		selenium.waitForPageToLoad("30000");
		selenium.type("ui=learningResources::content_searchInTableField()", str);
		try{
			System.out.println("Waiting 1 sec...");
			Thread.sleep(1000);
		} catch(InterruptedException ie) {
			ie.printStackTrace(System.out);
		}
		selenium.submit("ui=learningResources::content_searchInTableForm()");
		selenium.waitForPageToLoad("30000");
		
		
		// now loop through the list until there is no learning resource left to be deleted
		if (!selenium.isElementPresent("ui=learningResources::content_firstLearningResourceInTable()")) {
			all = true;
			break;
		}		
		selenium.click("ui=learningResources::content_firstLearningResourceInTable()");
		selenium.waitForPageToLoad("30000");
		String resourceTitle = selenium.getText("//div[span/a/span/text()='Back']//h4");
		System.out.println("Resource title: "+resourceTitle);
		if (!resourceTitle.startsWith(str)) {
			throw new IllegalStateException("resource title not what we were looking for: "+resourceTitle+". search str: "+str);
		}
		selenium.click("ui=learningResources::toolbox_learningResource_delete()");
		selenium.waitForPageToLoad("30000");
		selenium.click("ui=groups::content_deleteYes()");
		selenium.waitForPageToLoad("30000");
		final String bodyText = selenium.getBodyText(); 
		if (bodyText.contains("cannot be deleted")) {
			System.out.println("DELETION FAILED FOR SOME REASON... GIVING UP");
			System.out.println(bodyText);
			break;
		} else if (bodyText.contains("Entry deleted.")){
			System.out.println("YESSS, WE DELETED A RESOURCE!!! ");
			cnt++;
			continue;
		} else {
			System.out.println("COULDNT FIGURE OUT WHETHER DELETION WAS SUCCESSFUL OR NOT, GIVING UP...");
			System.out.println(bodyText);
			break;
		}
	}
	selenium.close();
	selenium.stop();
	System.out.println("=====================================");
	System.out.println("DELETE ALL FROM MYENTRIES STARTING WITH: " + str);
	System.out.println(" (deleted " + cnt + " entries, all="+all+")");
	System.out.println("                                 DONE");
	System.out.println("=====================================");

}

public static void deleteAllCoursesStartingWith(String str) {
	System.out.println("=====================================");
	System.out.println("DELETE ALL COURSES STARTING WITH: " + str);
	System.out.println("                                START");
	System.out.println("=====================================");
	Selenium selenium = Context.getContext().createSeleniumAndLogin();
	int cnt = 0;
	boolean all = false;
	while (true) {
		System.out.println("COUNTER: "+cnt);
		selenium.click("ui=tabs::learningResources()");
		selenium.waitForPageToLoad("30000");
		selenium.click("ui=learningResources::menu_searchForm()");
		selenium.waitForPageToLoad("30000");
		selenium.type("ui=learningResources::content_searchForm_titleField()",str);
		selenium.click("ui=learningResources::content_searchForm_search()");
		selenium.waitForPageToLoad("30000");
		
		
		// now loop through the list until there is no learning resource left to be deleted
		if (!selenium.isElementPresent("ui=learningResources::content_firstLearningResourceInTable()")) {
			all = true;
			break;
		}		
		selenium.click("ui=learningResources::content_firstLearningResourceInTable()");
		selenium.waitForPageToLoad("30000");
		selenium.click("ui=learningResources::toolbox_learningResource_delete()");
		selenium.waitForPageToLoad("30000");
		selenium.click("ui=groups::content_deleteYes()");
		selenium.waitForPageToLoad("30000");
		final String bodyText = selenium.getBodyText(); 
		if (bodyText.contains("cannot be deleted")) {
			System.out.println("DELETION FAILED FOR SOME REASON... GIVING UP");
			break;
		} else if (bodyText.contains("Entry deleted.")){
			System.out.println("YESSS, WE DELETED A RESOURCE!!! ");
			cnt++;
			continue;
		} else {
			System.out.println("COULDNT FIGURE OUT WHETHER DELETION WAS SUCCESSFUL OR NOT, GIVING UP...");
			System.out.println(bodyText);
			break;
		}
	}
	selenium.close();
	selenium.stop();
	System.out.println("=====================================");
	System.out.println("DELETE ALL COURSES STARTING WITH: " + str);
	System.out.println(" (deleted " + cnt + " courses, all="+all+")");
	System.out.println("                                 DONE");
	System.out.println("=====================================");
}*/


}
