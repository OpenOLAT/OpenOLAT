package org.olat.test.util.selenium.olatapi.course.run;

import org.olat.test.util.selenium.olatapi.OLATSeleniumWrapper;

import com.thoughtworks.selenium.Selenium;

/**
 * This is the disposed course run.
 * One could get to this, if the course run gets disposed either by a course publish,
 * or by modify course properties.
 * 
 * @author Lavinia Dumitrescu
 *
 */
public class DisposedCourseRun extends OLATSeleniumWrapper {

	public DisposedCourseRun(Selenium selenium) {
		super(selenium);

    if(!selenium.isElementPresent("ui=course::disposed_closeAndRestart()")) {
    	//click anywhere, the course run was disposed anyway
    	selenium.click("ui=course::toolbox_generalTools_detailedView()");
  		selenium.waitForPageToLoad("30000");
    }
	}

	public CourseRun closeCourseAndRestart() {
		if(selenium.isElementPresent("ui=course::disposed_closeAndRestart()")) {
		  selenium.click("ui=course::disposed_closeAndRestart()");
		  selenium.waitForPageToLoad("30000");
		  return new CourseRun(selenium);
		}
		throw new IllegalStateException("There is no - Close and restart course - button present!");
	}
}
