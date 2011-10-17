package org.olat.test.util.selenium.olatapi.course.run;

import org.olat.test.util.selenium.olatapi.OLATSeleniumWrapper;

import com.thoughtworks.selenium.Selenium;

/**
 * Basic LTI run.
 * 
 * @author lavinia
 *
 */
public class LTIRun extends OLATSeleniumWrapper {

  public LTIRun(Selenium selenium) {
    super(selenium);
    // TODO Auto-generated constructor stub
  }
  
  public void launch() {
    selenium.selectFrame("//iframe[@id='IMSBasicLTIFrame']");
    if(selenium.isElementPresent("ui=course::content_lti_launch()")) {
      selenium.click("ui=course::content_lti_launch()");
      //TODO: LD: add security warning confirmation step!
    } else {
      throw new IllegalStateException("Cannot launch LTI, button missing!");
    }
    selenium.selectFrame("relative=top"); 
  }
}
