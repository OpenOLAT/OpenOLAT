package org.olat.test.util.selenium.olatapi.group;

import org.olat.test.util.selenium.olatapi.OLATSeleniumWrapper;
import org.olat.test.util.selenium.olatapi.course.run.CourseRun;

/**
 * Wrapper for Rights Management, triggered from Course Run
 * 
 * @author Thomas Linowsky
 */

import com.thoughtworks.selenium.Selenium;

public class RightsManagement extends OLATSeleniumWrapper{

	/**
	 * Constructor
	 * @param selenium
	 */
	public RightsManagement(Selenium selenium) {
		super(selenium);
		if(!selenium.isTextPresent("Rights management")) {
			throw new IllegalStateException("This is not the - Rights management - page");
		}
	}
	
	/**
	 * create a new rights group with given title and description
	 * @param title The title of the new rights group
	 * @param description The description of the new rights group
	 * @return The GroupAdmin object of the newly created rights group
	 */

	public RightsAdmin createRightsGroup(String title, String description) {
		selenium.click("ui=rightsManagement::createRightsGroup()");
		selenium.waitForPageToLoad("30000");
		selenium.type("ui=group::content_description_groupDetails_groupName()", title);		
		if(description!=null) {
			//uses a Rich text element
		  selenium.type("ui=commons::tinyMce_styledTextArea()", description);
		}		
		selenium.click("ui=groupManagement::toolbox_groupManagement_formNewLearningGroup_save()");		
		selenium.waitForPageToLoad("30000");
		
		return new RightsAdmin(selenium);
	}
	
	/**
	 * close the rights group and show Course run the group was inited by
	 * @return The courserun where we came from
	 */

	public CourseRun closeRightsManagement() {
		selenium.click("ui=rightsManagement::rightGroups_close()");
		selenium.waitForPageToLoad("30000");
		return new CourseRun(selenium);
	}

}
