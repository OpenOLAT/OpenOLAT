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

package org.olat.modules.fo;

import org.olat.core.commons.fullWebApp.LayoutMain3ColsController;
import org.olat.core.commons.fullWebApp.popup.BaseFullWebappPopupLayoutFactory;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.creator.ControllerCreator;
import org.olat.core.gui.control.generic.popup.PopupBrowserWindow;
import org.olat.core.gui.control.generic.title.TitleInfo;
import org.olat.core.gui.control.generic.title.TitledWrapperController;
import org.olat.core.util.StringHelper;
import org.olat.modules.fo.ui.ForumController;


/**
 * 
 * Description:<br>
 * Factory for a Titled <code>ForumController</code>, either in a popup or not.
 * 
 * <P>
 * Initial Date:  25.06.2007 <br>
 * @author Lavinia Dumitrescu
 */
public class ForumUIFactory {
	
	public static final String CSS_ICON_CLASS_FORUM = "o_fo_icon";
	public static final String CSS_ICON_CLASS_MESSAGE = "o_forum_message_icon";
	
	/**
	 * Provides a popable ForumController wrapped in a titled controller.
	 * @param ureq
	 * @param forum
	 * @param forumCallback
	 * @param title
	 * @return a ChiefController
	 */
	public static PopupBrowserWindow getPopupableForumController(UserRequest ureq, WindowControl wControl, final Forum forum, final ForumCallback forumCallback, final TitleInfo titleInfo) {		
		ControllerCreator ctrlCreator = new ControllerCreator() {
			@Override
			public Controller createController(UserRequest lureq, WindowControl lwControl) {
				Controller forumWrapperController = getTitledForumController(lureq, lwControl, forum,  forumCallback, titleInfo);
				// use on column layout
				LayoutMain3ColsController layoutCtr = new LayoutMain3ColsController(lureq, lwControl, forumWrapperController);
				layoutCtr.addDisposableChildController(forumWrapperController); // dispose content on layout dispose
				return layoutCtr;
			}					
		};
		//wrap the content controller into a full header layout
		ControllerCreator layoutCtrlr = BaseFullWebappPopupLayoutFactory.createAuthMinimalPopupLayout(ureq, ctrlCreator);
		return wControl.getWindowBackOffice().getWindowManager().createNewPopupBrowserWindowFor(ureq, layoutCtrlr);						
	}
	
	/**
	 * Provides a ForumController wrapped in a titled controller.
	 * @param ureq
	 * @param forum
	 * @param forumCallback
	 * @param title
	 * @return a TitledWrapperController
	 */
  public static Controller getTitledForumController(UserRequest ureq, WindowControl wControl, Forum forum, ForumCallback forumCallback, TitleInfo titleInfo) {			
		ForumController popupFoCtr = new ForumController(ureq, wControl, forum, forumCallback, true);												
		TitledWrapperController forumWrapperController = new TitledWrapperController(ureq, wControl, popupFoCtr, "o_course_run", titleInfo);
		// Set CSS values to default forum icons if no values are set in the title info
		if (!StringHelper.containsNonWhitespace(titleInfo.getIconCssClass())) {
			forumWrapperController.setIconCssClass(CSS_ICON_CLASS_FORUM);
		}
		return forumWrapperController;							
	}

  /**
   * Provides a standard forum controller without a title element
   * @param ureq
   * @param wControl
   * @param forum
   * @param forumCallback
   * @return
   */
	public static ForumController getStandardForumController(UserRequest ureq, WindowControl wControl, Forum forum,	ForumCallback forumCallback) {
		return new ForumController(ureq, wControl, forum, forumCallback, true);												
	}
  
}
