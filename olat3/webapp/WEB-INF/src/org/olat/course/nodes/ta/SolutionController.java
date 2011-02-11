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

package org.olat.course.nodes.ta;

import org.olat.commons.file.filechooser.FileChooserController;
import org.olat.core.commons.modules.bc.FolderRunController;
import org.olat.core.commons.modules.bc.vfs.OlatNamedContainerImpl;
import org.olat.core.commons.modules.bc.vfs.OlatRootFolderImpl;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.ControllerEventListener;
import org.olat.core.gui.control.DefaultController;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.util.Util;
import org.olat.core.util.notifications.ContextualSubscriptionController;
import org.olat.core.util.notifications.SubscriptionContext;
import org.olat.core.util.vfs.callbacks.ReadOnlyCallback;
import org.olat.course.nodes.CourseNode;
import org.olat.course.nodes.TACourseNode;
import org.olat.course.run.environment.CourseEnvironment;
import org.olat.course.run.userview.UserCourseEnvironment;

/**
 *  
 * @author Mike Stock
 */

public class SolutionController extends BasicController {
	
	private VelocityContainer myContent;
	private FileChooserController fileChooserController;
	private FolderRunController solutionFolderRunController;
	private SubscriptionContext subsContext;
	private ContextualSubscriptionController contextualSubscriptionCtr;

	
	/**
	 * Implements a dropbox.
	 * @param ureq
	 * @param wControl
	 * @param config
	 * @param node
	 * @param userCourseEnv
	 * @param previewMode
	 */
	public SolutionController(UserRequest ureq, WindowControl wControl, CourseNode node, UserCourseEnvironment userCourseEnv, boolean previewMode) {
		super(ureq, wControl);
		
		myContent = createVelocityContainer("solutionRun");
		
		// returnbox display
		OlatRootFolderImpl rootFolder = new OlatRootFolderImpl(
			SolutionController.getSolutionPathRelToFolderRoot(userCourseEnv.getCourseEnvironment(), node), null);
		OlatNamedContainerImpl namedContainer = new OlatNamedContainerImpl("solutions", rootFolder); 
		namedContainer.setLocalSecurityCallback(new ReadOnlyCallback());
		solutionFolderRunController = new FolderRunController(namedContainer, false, ureq, wControl);
		solutionFolderRunController.addControllerListener(this);
		myContent.put("solutionbox", solutionFolderRunController.getInitialComponent());
		if ( !previewMode) {
			// offer subscription, but not to guests
			subsContext = SolutionFileUploadNotificationHandler.getSubscriptionContext(userCourseEnv, node);
			if (subsContext != null) {
				contextualSubscriptionCtr = AbstractTaskNotificationHandler.createContextualSubscriptionController(ureq, wControl, getSolutionPathRelToFolderRoot(
						userCourseEnv.getCourseEnvironment(), node), subsContext, SolutionController.class);
				myContent.put("subscription", contextualSubscriptionCtr.getInitialComponent());
				myContent.contextPut("hasNotification", Boolean.TRUE);
			}
		}
		putInitialPanel(myContent);
	}
	
	
	/**
	 * Returnbox path relative to folder root.
	 * @param courseEnv
	 * @param cNode
	 * @return Returnbox path relative to folder root.
	 */
	public static String getSolutionPathRelToFolderRoot(CourseEnvironment courseEnv, CourseNode cNode) {
		return courseEnv.getCourseBaseContainer().getRelPath() + "/" + TACourseNode.SOLUTION_FOLDER_NAME + "/" + cNode.getIdent();
	}
	
	
	/**
	 * @see org.olat.core.gui.control.DefaultController#event(org.olat.core.gui.UserRequest, org.olat.core.gui.components.Component, org.olat.core.gui.control.Event)
	 */
	public void event(UserRequest ureq, Component source, Event event) {

	}

	/**
	 * @see org.olat.core.gui.control.DefaultController#event(org.olat.core.gui.UserRequest, org.olat.core.gui.control.Controller, org.olat.core.gui.control.Event)
	 */
	public void event(UserRequest ureq, Controller source, Event event) {

	}
		
	/**
	 * 
	 * @see org.olat.core.gui.control.DefaultController#doDispose(boolean)
	 */
	protected void doDispose() {
		if (fileChooserController != null) {
			fileChooserController.dispose();
			fileChooserController = null;
		}
	}
}
