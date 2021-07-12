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
*/
package org.olat.course.nodes;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.title.TitleInfo;
import org.olat.core.gui.control.generic.title.TitledWrapperController;
import org.olat.core.util.StringHelper;
import org.olat.course.run.userview.UserCourseEnvironment;
import org.olat.course.style.ui.HeaderContentController;

public class TitledWrapperHelper {

	public static Controller getWrapper(UserRequest ureq, WindowControl wControl, Controller controller,
			UserCourseEnvironment userCourseEnv, CourseNode courseNode, String iconCssClass) {
		return new HeaderContentController(ureq, wControl, controller, userCourseEnv, courseNode, iconCssClass);
	}
	
	public static Controller getWrapper(UserRequest ureq, WindowControl wControl, Controller controller,
			CourseNode courseNode, String iconCssClass) {
		
		String displayOption = courseNode.getDisplayOption();
		if(CourseNode.DISPLAY_OPTS_CONTENT.equals(displayOption)) {
			//don't change anything
		} else if (CourseNode.DISPLAY_OPTS_SHORT_TITLE_CONTENT.equals(displayOption)) {
			if(StringHelper.containsNonWhitespace(courseNode.getShortTitle())) {
				TitleInfo titleInfo = new TitleInfo(null, courseNode.getShortTitle(), null, courseNode.getIdent());
				titleInfo.setDescriptionCssClass("o_objectives o_user_content_block");
				if (StringHelper.containsNonWhitespace(iconCssClass)) {
					titleInfo.setIconCssClass(iconCssClass);
				}
				controller = new TitledWrapperController(ureq, wControl, controller, null, titleInfo); 
			}
		} else if (CourseNode.DISPLAY_OPTS_TITLE_CONTENT.equals(displayOption)) {
			if(StringHelper.containsNonWhitespace(courseNode.getLongTitle())) {
				TitleInfo titleInfo = new TitleInfo(null, courseNode.getLongTitle(), null, courseNode.getIdent());
				titleInfo.setDescriptionCssClass("o_objectives o_user_content_block");
				if (StringHelper.containsNonWhitespace(iconCssClass)) {
					titleInfo.setIconCssClass(iconCssClass);
				}
				controller = new TitledWrapperController(ureq, wControl, controller, null, titleInfo);
			}
		} else if (CourseNode.DISPLAY_OPTS_SHORT_TITLE_DESCRIPTION_CONTENT.equals(displayOption)) {
			String title = courseNode.getShortTitle();
			String description = null;
			if (StringHelper.containsNonWhitespace(courseNode.getDescription())) {
				if (StringHelper.containsNonWhitespace(title)) {
					description = courseNode.getDescription();
				}
			}

			if(StringHelper.containsNonWhitespace(title) || StringHelper.containsNonWhitespace(description)) {
				TitleInfo titleInfo = new TitleInfo(null, title, description, courseNode.getIdent());
				titleInfo.setDescriptionCssClass("o_objectives o_user_content_block");
				if (StringHelper.containsNonWhitespace(iconCssClass)) {
					titleInfo.setIconCssClass(iconCssClass);
				}
				controller = new TitledWrapperController(ureq, wControl, controller, null, titleInfo);
			}
		} else if (CourseNode.DISPLAY_OPTS_TITLE_DESCRIPTION_CONTENT.equals(displayOption)) {

			String title = courseNode.getLongTitle();
			String description = null;
			if (StringHelper.containsNonWhitespace(courseNode.getDescription())) {
				description = courseNode.getDescription();
			}

			if(StringHelper.containsNonWhitespace(title) || StringHelper.containsNonWhitespace(description)) {
				TitleInfo titleInfo = new TitleInfo(null, title, description, courseNode.getIdent());
				titleInfo.setDescriptionCssClass("o_objectives o_user_content_block");
				if (StringHelper.containsNonWhitespace(iconCssClass)) {
					titleInfo.setIconCssClass(iconCssClass);
				}
				controller = new TitledWrapperController(ureq, wControl, controller, null, titleInfo);
			} 
		}
		
		return controller;
	}
}
