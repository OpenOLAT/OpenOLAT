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
* <hr>
* <a href="http://www.openolat.org">
* OpenOLAT - Online Learning and Training</a><br>
* This file has been modified by the OpenOLAT community. Changes are licensed
* under the Apache 2.0 license as the original file.  
* <p>
*/
package org.olat.test.util.selenium.olatapi.qti;

import com.thoughtworks.selenium.Selenium;

import org.olat.test.util.selenium.olatapi.OLATSeleniumWrapper;
import org.olat.test.util.selenium.olatapi.lr.LRDetailedView;
import org.olat.test.util.selenium.olatapi.qti.QuestionEditor.QUESTION_TYPES;

/**
 * This is the TestEditor abstraction. <p>
 * It allows to add section nodes or question nodes of the type: SINGLE_CHOICE, MULTIPLE_CHOICE, KPRIM, GAP_TEXT.
 * 
 * @author Lavinia Dumitrescu
 *
 */
public class TestEditor extends OLATSeleniumWrapper {

	
	
	/**
	 * @param selenium
	 */
	public TestEditor(Selenium selenium) {
		super(selenium);

    //Check that we're on the right place
		if(!selenium.isElementPresent("ui=testEditor::toolbox_add_addSection()")) {
			//it must have an Add Section link.
			throw new IllegalStateException("This is not the - Test editor - page");
		}
	}
	
	/**
	 * Close editor and save changes.
	 * @return
	 */
	public LRDetailedView close() {
		selenium.click("ui=testEditor::toolbox_editorTools_closeEditor()");
		selenium.waitForPageToLoad("30000");
		selenium.click("ui=testEditor::dialog_clickSave()");
		selenium.waitForPageToLoad("30000");
		
		return new LRDetailedView(selenium);
	}
	
	/**
	 * Sets the passing score for the curent test.
	 * @param score
	 */
	public void setNecessaryPassingScore(double score) {
		selenium.type("ui=testEditor::content_test_necessaryPassingScore()", String.valueOf(score));
		selenium.click("ui=testEditor::content_test_save()");
		selenium.waitForPageToLoad("30000");
	}
	
	/**
	 * Adds question of the specified type with the given title.
	 * @param type
	 * @param title
	 */
	public QuestionEditor addQuestion(QUESTION_TYPES type, String title) {
		clickAddQuestion(type);
		
		selenium.waitForPageToLoad("30000");
		selenium.click("ui=testEditor::toolbox_add_insertAsRootsFirstChild()");
		selenium.click("ui=testEditor::toolbox_add_save()");
		selenium.waitForPageToLoad("30000");		
		selenium.type("ui=commons::flexiForm_labeledTextInput(formElementLabel=Title)", title);
		selenium.click("ui=commons::flexiForm_saveButton()");
		selenium.waitForPageToLoad("30000");
		
		return returnQuestionEditor(type);
	}
	
	/**
	 * QUESTION_TYPES: SINGLE_CHOICE, MULTIPLE_CHOICE, KPRIM, GAP_TEXT
	 * @param type
	 */
	protected void clickAddQuestion(QUESTION_TYPES type) {
		if(QUESTION_TYPES.SINGLE_CHOICE.equals(type)) {
		  selenium.click("ui=testEditor::toolbox_add_addSingleChoice()");
		} else if(QUESTION_TYPES.MULTIPLE_CHOICE.equals(type)) {
			selenium.click("ui=testEditor::toolbox_add_addMultipleChoice()");
		} else if(QUESTION_TYPES.KPRIM.equals(type)) {
			selenium.click("ui=testEditor::toolbox_add_addKprim()");
		} else if(QUESTION_TYPES.GAP_TEXT.equals(type)) {
			selenium.click("ui=testEditor::toolbox_add_addGapText()");
		}
	}
	
	/**
	 * 
	 * @param type
	 * @return Returns a SCQuestionEditor, MCQuestionEditor, KPrimQuestionEditor or a FIBQuestionEditor.
	 */
	protected QuestionEditor returnQuestionEditor(QUESTION_TYPES type) {
		if(QUESTION_TYPES.SINGLE_CHOICE.equals(type)) {
			return new SCQuestionEditor(selenium);
		} else if(QUESTION_TYPES.MULTIPLE_CHOICE.equals(type)) {
			return new MCQuestionEditor(selenium);
		} else if(QUESTION_TYPES.KPRIM.equals(type)) {
			return new KPrimQuestionEditor(selenium);
		} else if(QUESTION_TYPES.GAP_TEXT.equals(type)) {
			return new FIBQuestionEditor(selenium);
		}
		return new QuestionEditor(selenium);
	}
	
	/**
	 * Adds new section with the specified title.
	 * @param title
	 */
	public SectionEditor addSection(String title) {
		selenium.click("ui=testEditor::toolbox_add_addSection()");
		selenium.waitForPageToLoad("30000");
		selenium.click("ui=courseEditor::toolbox_insertCourseElements_insertAsRootsFirstChild()");
		selenium.click("ui=testEditor::toolbox_add_save()");
		selenium.waitForPageToLoad("30000");
		selenium.click("ui=testEditor::content_section_title()");
		selenium.type("ui=testEditor::content_section_title()", title);
		selenium.click("ui=testEditor::content_section_save()");
		selenium.waitForPageToLoad("30000");
		return new SectionEditor(selenium);
	}
	
	/**
	 * Select question with the given title.
	 * @param title
	 */
	public QuestionEditor selectQuestion(String title) {
		selenium.click("ui=testEditor::menu_link(link=" + title + ")");
		selenium.waitForPageToLoad("30000");
		
		return getCurrentQuestion();
	}
	
	/**
	 * Checks the question type.
	 * QUESTION_TYPES: SINGLE_CHOICE, MULTIPLE_CHOICE, KPRIM, GAP_TEXT
	 * @return Returns null if no question type identified.
	 */
	protected QuestionEditor getCurrentQuestion() {
		if(selenium.isElementPresent("ui=testEditor::content_metadata_scType()")) {
			return new SCQuestionEditor(selenium);
		} else if(selenium.isElementPresent("ui=testEditor::content_metadata_mcType()")) {
			return new MCQuestionEditor(selenium);
		} else if(selenium.isElementPresent("ui=testEditor::content_metadata_gapType()")) {
			return new FIBQuestionEditor(selenium);
		} else if(selenium.isElementPresent("ui=testEditor::content_metadata_kprimType()")) {
			return new KPrimQuestionEditor(selenium);
		}  
		throw new IllegalStateException("This is not a - QuestionEditor - page!");
	}
	
	public SectionEditor selectSection(String title) {
		selenium.click("ui=testEditor::menu_link(link=" + title + ")");
		selenium.waitForPageToLoad("30000");
		return new SectionEditor(selenium);
	}
	
	/**
	 * Deletes the current selected node.
	 *
	 */
	public void deleteCurrentNode(boolean confirmDeletion) {
		selenium.click("ui=testEditor::toolbox_Change_changeDelete()");
		selenium.waitForPageToLoad("30000");
		if(confirmDeletion) {
		  selenium.click("ui=testEditor::dialog_clickYes()");
		  selenium.waitForPageToLoad("30000");
		}
	}
	
	/**
	 * Try to delete the current selected section/question node but not possible. 
	 * A Test must contain at least a section node.
	 * @throws Exception
	 */
	public void deleteUndeleteable(boolean isSection) throws Exception {
		String cannotBeDeteledMsg = "Section cannot be deleted.";
		if(!isSection) {
			cannotBeDeteledMsg = "Question cannot be deleted.";
		}
		selenium.click("ui=testEditor::toolbox_Change_changeDelete()");
		selenium.waitForPageToLoad("30000");
		for (int second = 0;; second++) {
			if (second >= 60) fail("timeout");
			try { if (selenium.isTextPresent(cannotBeDeteledMsg)) break; } catch (Exception e) {}
			Thread.sleep(1000);
		}
		selenium.click("ui=testEditor::dialog_clickOk()");
	}
	
	/**
	 * Copy the current selected node and sets the newTitle as title.
	 * @param newTitle
	 */
	public QuestionEditor copyCurrentQuestion(String newTitle) {
		selenium.click("ui=testEditor::toolbox_Change_changeCopy()");
		selenium.waitForPageToLoad("30000");
		selenium.click("ui=courseEditor::toolbox_insertCourseElements_insertAsRootsFirstChild()");
		selenium.click("ui=testEditor::toolbox_add_save()");
		selenium.waitForPageToLoad("30000");		
		selenium.type("ui=commons::flexiForm_labeledTextInput(formElementLabel=Title)", newTitle);
		selenium.click("ui=commons::flexiForm_saveButton()");
		selenium.waitForPageToLoad("30000");
		return new QuestionEditor(selenium);
	}
	
	/**
	 * Clicks OK on a dialog.
	 *
	 */
	public void dialogOK() {
		selenium.click("ui=testEditor::dialog_clickOk()");
	}
}
