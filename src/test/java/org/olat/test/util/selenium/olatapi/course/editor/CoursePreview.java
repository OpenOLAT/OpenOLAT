package org.olat.test.util.selenium.olatapi.course.editor;

import org.olat.test.util.selenium.olatapi.OLATSeleniumWrapper;

import com.thoughtworks.selenium.Selenium;

public class CoursePreview extends OLATSeleniumWrapper {
	
	public enum Role {REGISTERED_USER, GUEST, TUTOR, COURSE_OWNER, AUTHOR}

	public CoursePreview(Selenium selenium) {
		super(selenium);
		// TODO Auto-generated constructor stub
	}

  public CourseEditor closePreview() {
	  selenium.click("ui=courseEditor::preview_closePreview()");
		selenium.waitForPageToLoad("30000");
		return new CourseEditor(selenium);
  }
  
  public void showPreview() {
		selenium.click("ui=courseEditor::preview_showCoursePreview()");
		selenium.waitForPageToLoad("30000");
 }
  
  public void changeRole(Role role) {  	
  	if(Role.REGISTERED_USER.equals(role)) {  		
  		selenium.click("ui=courseEditor::preview_selectRole(role=role.student)");
  	} else if(Role.GUEST.equals(role)) {
  		selenium.click("ui=courseEditor::preview_selectRole(role=role.guest)");
  	} else if (Role.TUTOR.equals(role)) {
  		selenium.click("ui=courseEditor::preview_selectRole(role=role.coursecoach)");
  	} else if (Role.COURSE_OWNER.equals(role)) {
  		selenium.click("ui=courseEditor::preview_selectRole(role=role.courseadmin)");
  	} else if (Role.AUTHOR.equals(role)) {
  		selenium.click("ui=courseEditor::preview_selectRole(role=role.globalauthor)");
  	} 
  }
  
  public void selectCourseElement(String title) {
		selenium.click("ui=courseEditor::menu_link(link=" + title + ")");
		selenium.waitForPageToLoad("60000");
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block			
		}		
	}
}
