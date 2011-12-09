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
package org.olat.test.util.selenium.olatapi.course.editor;

import org.olat.test.util.selenium.olatapi.OLATSeleniumWrapper;

import com.thoughtworks.selenium.Selenium;

public class LTIPageEditor extends CourseElementEditor {

  public LTIPageEditor(Selenium selenium) {
    super(selenium);
    // TODO Auto-generated constructor stub
  }

  private void selectPageContent() {
    if (selenium.isElementPresent("ui=courseEditor::content_lti_tabPageContent()")) {
      selenium.click("ui=courseEditor::content_lti_tabPageContent()");
      selenium.waitForPageToLoad("30000");
    } 
  }
  
  /**
   * Accepts null as input params, if null ignore.
   * If boolean false ignore, else click on checkbox (swich state).
   * @param url
   * @param key
   * @param password
   * @param sendNameToSupplierSwitch
   * @param sendEmailToSupplierSwitch
   * @param showInfoSentSwitch
   */
  public void configurePage(String url, String key, String password, boolean sendNameToSupplierSwitch, boolean sendEmailToSupplierSwitch ,boolean showInfoSentSwitch) {
    selectPageContent();
    if(url!=null) {
      selenium.type("ui=commons::flexiForm_labeledTextInput(formElementLabel=URL)", url);
    }
    if(key!=null) {
      selenium.type("ui=commons::flexiForm_labeledTextInput(formElementLabel=Key)", key);
    }
    if(password!=null) {
      selenium.type("ui=commons::flexiForm_labeledTextInput(formElementLabel=Password)", password);
    }
    if(sendNameToSupplierSwitch) {
      selenium.click("ui=commons::flexiForm_labeledCheckbox(formElementLabel=Send name to provider)");
    }
    if(sendEmailToSupplierSwitch) {
      selenium.click("ui=commons::flexiForm_labeledCheckbox(formElementLabel=Send e-mail address to provider)");
    }
    if(showInfoSentSwitch) {
      selenium.click("ui=commons::flexiForm_labeledCheckbox(formElementLabel=Show information sent)");
    }
    selenium.click("ui=commons::flexiForm_saveButton()");
    selenium.waitForPageToLoad("30000");
  }
  
  public LTIPreview showPreview(){
    selenium.click("ui=courseEditor::content_bbSinglePage_previewSinglePage()");
    selenium.waitForPageToLoad("30000");
    return new LTIPreview(this);
  }
  
  /**
   * Inner class since it could only be accessible via the parent class.
   * @author lavinia
   *
   */
  public class LTIPreview extends OLATSeleniumWrapper {

    private LTIPageEditor lTIPageEditor;
    
    private LTIPreview(LTIPageEditor lTIPageEditor_) {
      super(lTIPageEditor_.getSelenium());

      lTIPageEditor = lTIPageEditor_;
    }
    
    public boolean hasInfo(String info) {
      //select iframe
      selenium.selectFrame("//iframe[@id='IMSBasicLTIFrame']");
      boolean hasInfo = selenium.isTextPresent(info);
      selenium.selectFrame("relative=top"); 
      return hasInfo;
    }
    
    public LTIPageEditor closePreview() {
      if(selenium.isElementPresent("ui=courseEditor::preview_closePreview()")) {
        selenium.click("ui=courseEditor::preview_closePreview()");
        selenium.waitForPageToLoad("30000");
      } else {
        throw new IllegalStateException("Close preview - link not available");
      }
      return lTIPageEditor;
    }
    
  }
}
