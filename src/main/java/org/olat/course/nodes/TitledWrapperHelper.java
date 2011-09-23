package org.olat.course.nodes;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.title.TitleInfo;
import org.olat.core.gui.control.generic.title.TitledWrapperController;
import org.olat.core.util.StringHelper;

public class TitledWrapperHelper {

	
	public static Controller getWrapper(UserRequest ureq, WindowControl wControl,
			Controller controller, CourseNode courseNode, String iconCssClass) {
		
		String displayOption = courseNode.getDisplayOption();
		if(CourseNode.DISPLAY_OPTS_CONTENT.equals(displayOption)) {
			return controller;
		} else if (CourseNode.DISPLAY_OPTS_TITLE_CONTENT.equals(displayOption)) {
			TitleInfo titleInfo = new TitleInfo(null, courseNode.getShortTitle(), null, courseNode.getIdent());
			titleInfo.setDescriptionCssClass("o_course_run_objectives");
			TitledWrapperController titledController = new TitledWrapperController(ureq, wControl, controller, "o_course_run", titleInfo);
			if (StringHelper.containsNonWhitespace(iconCssClass)) {
				titledController.setTitleCssClass(" b_with_small_icon_left " + iconCssClass + " ");
			}
			return titledController; 
		} else if (CourseNode.DISPLAY_OPTS_TITLE_DESCRIPTION_CONTENT.equals(displayOption)) {

			String longTitle = courseNode.getLongTitle();
			String description = null;
			if (StringHelper.containsNonWhitespace(courseNode.getLearningObjectives())) {
				if (StringHelper.containsNonWhitespace(longTitle)) {
					description = "<h4>" + longTitle + "</h4>" + courseNode.getLearningObjectives();
				} else {
					description = courseNode.getLearningObjectives();
				}
			}
			
			TitleInfo titleInfo = new TitleInfo(null, courseNode.getShortTitle(), description, courseNode.getIdent());
			titleInfo.setDescriptionCssClass("o_course_run_objectives");
			TitledWrapperController titledController = new TitledWrapperController(ureq, wControl, controller, "o_course_run", titleInfo);
			if (StringHelper.containsNonWhitespace(iconCssClass)) {
				titledController.setTitleCssClass(" b_with_small_icon_left " + iconCssClass + " ");
			}
			return titledController; 
		} else {
			return controller;
		}
	}
}
