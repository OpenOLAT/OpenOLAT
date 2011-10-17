package org.olat.test.util.selenium.olatapi.course.run;

import org.olat.test.util.selenium.olatapi.OLATSeleniumWrapper;

import com.thoughtworks.selenium.Selenium;

/**
 * Generic course element. Provides access to the CourseRun.
 * 
 * @author lavinia
 *
 */
public class CourseElement extends OLATSeleniumWrapper{

  public CourseElement(Selenium selenium) {
    super(selenium);
    // TODO Auto-generated constructor stub
  }

  public CourseRun getCourseRun() {
    return new CourseRun(selenium);
  }
}
