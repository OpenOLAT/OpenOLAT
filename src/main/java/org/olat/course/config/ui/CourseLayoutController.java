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
* <p>
*/ 

package org.olat.course.config.ui;

import org.olat.core.commons.controllers.filechooser.FileChoosenEvent;
import org.olat.core.commons.controllers.filechooser.FileChooserController;
import org.olat.core.commons.controllers.filechooser.FileChooserUIFactory;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.gui.control.generic.closablewrapper.CloseableModalController;
import org.olat.core.logging.activity.ILoggingAction;
import org.olat.core.logging.activity.LearningResourceLoggingAction;
import org.olat.core.util.vfs.VFSContainer;
import org.olat.core.util.vfs.filters.VFSItemFileTypeFilter;
import org.olat.course.config.CourseConfig;

/**
 * Description: <br>
 * Configuration of course layout settings: standard or customized
 * 
 * Initial Date: Jun 21, 2005 <br>
 * @author patrick
 */
public class CourseLayoutController extends BasicController {
	
	private static final String LOG_COURSELAYOUT_DEFAULT_ADDED = "COURSELAYOUT_DEFAULT_ADDED";
	private static final String LOG_COURSELAYOUT_CUSTOM_ADDED = "COURSELAYOUT_CUSTOM_ADDED";
	private static final VFSItemFileTypeFilter cssTypeFilter = new VFSItemFileTypeFilter(new String[] { "css" });
	private VelocityContainer myContent;
	private FileChooserController fileChooserCtr;
	private Link changeCustomCSSButton;
	private Link chooseSystemCSSButton;
	private Link chooseCustomCSSButton;
	private CloseableModalController cmc;
	private VFSContainer vfsCourseRoot;
	private CourseConfig courseConfig;
	private ILoggingAction loggingAction;

	/**
	 * @param ureq
	 * @param control
	 * @param theCourse
	 */
	public CourseLayoutController(UserRequest ureq, WindowControl wControl, CourseConfig courseConfig, VFSContainer vfsCourseRoot) {
		super(ureq,wControl);
		
		this.courseConfig = courseConfig;
		this.vfsCourseRoot = vfsCourseRoot;
		
		myContent = this.createVelocityContainer("CourseLayout");
		changeCustomCSSButton = LinkFactory.createButton("form.layout.changecustomcss", myContent, this);
		chooseSystemCSSButton = LinkFactory.createButton("form.layout.choosesystemcss", myContent, this);
		chooseCustomCSSButton = LinkFactory.createButton("form.layout.choosecustomcss", myContent, this);
				
		String cssFileRef = courseConfig.getCssLayoutRef();
		myContent.contextPut("hasCustomCourseCSS", new Boolean(courseConfig.hasCustomCourseCSS()));
		myContent.contextPut("cssFileRef", cssFileRef);
		
		putInitialPanel(myContent);
	}

	/**
	 * @see org.olat.core.gui.control.DefaultController#event(org.olat.core.gui.UserRequest,
	 *      org.olat.core.gui.components.Component, org.olat.core.gui.control.Event)
	 */
	public void event(UserRequest ureq, Component source, Event event) {
		
		if (source == chooseSystemCSSButton){
			// applying the default layout
			myContent.contextPut("cssFileRef", CourseConfig.VALUE_EMPTY_CSS_FILEREF);			
			courseConfig.setCssLayoutRef(CourseConfig.VALUE_EMPTY_CSS_FILEREF);
			
			myContent.contextPut("hasCustomCourseCSS", new Boolean(courseConfig.hasCustomCourseCSS()));
      // log removing custom course layout
			loggingAction = LearningResourceLoggingAction.REPOSITORY_ENTRY_PROPERTIES_COURSELAYOUT_DEFAULT_ADDED;
			this.fireEvent(ureq, Event.CHANGED_EVENT);
			
		} else if (source == changeCustomCSSButton || source == chooseCustomCSSButton){
			
			removeAsListenerAndDispose(fileChooserCtr);
			fileChooserCtr = FileChooserUIFactory.createFileChooserController(ureq, getWindowControl(), vfsCourseRoot, cssTypeFilter, true);
			listenTo(fileChooserCtr);
			removeAsListenerAndDispose(cmc);
			cmc = new CloseableModalController(getWindowControl(), translate("close"), fileChooserCtr.getInitialComponent());
			listenTo(cmc);
			cmc.activate();
		}
	}
	
	/**
	 * @see org.olat.core.gui.control.DefaultController#event(org.olat.core.gui.UserRequest, org.olat.core.gui.control.Controller, org.olat.core.gui.control.Event)
	 */
	public void event(UserRequest ureq, Controller source, Event event) {
		if (source == fileChooserCtr) {
			// from file choosing, in any case remove modal dialog
			cmc.deactivate();
			
			if (event instanceof FileChoosenEvent) {
				
				String relPath = FileChooserUIFactory.getSelectedRelativeItemPath((FileChoosenEvent) event, vfsCourseRoot, null);				
				// user chose a file
				myContent.contextPut("cssFileRef", relPath);			
				courseConfig.setCssLayoutRef(relPath);				
				myContent.contextPut("hasCustomCourseCSS", new Boolean(courseConfig.hasCustomCourseCSS()));
								
        // log adding custom course layout
				loggingAction = LearningResourceLoggingAction.REPOSITORY_ENTRY_PROPERTIES_COURSELAYOUT_CUSTOM_ADDED;	
				this.fireEvent(ureq, Event.CHANGED_EVENT);
			}

		} // else user cancelled file selection
	}

	/**
	 * @see org.olat.core.gui.control.DefaultController#doDispose(boolean)
	 */
	protected void doDispose() {
		// controllers autodisposed by basic controller
	}

	/**
	 * 
	 * @return Returns a log message if the course layout was changed, else null.
	 */
	public ILoggingAction getLoggingAction() {
		return loggingAction;
	}

}