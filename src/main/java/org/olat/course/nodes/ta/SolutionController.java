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

package org.olat.course.nodes.ta;

import org.olat.core.commons.modules.bc.FolderRunController;
import org.olat.core.commons.services.notifications.SubscriptionContext;
import org.olat.core.commons.services.notifications.ui.ContextualSubscriptionController;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.util.vfs.NamedContainerImpl;
import org.olat.core.util.vfs.VFSContainer;
import org.olat.core.util.vfs.VFSManager;
import org.olat.core.util.vfs.callbacks.ReadOnlyCallback;
import org.olat.course.nodes.CourseNode;
import org.olat.course.nodes.TACourseNode;
import org.olat.course.run.environment.CourseEnvironment;

/**
 *  
 * @author Mike Stock
 */

public class SolutionController extends BasicController {
	
	private VelocityContainer myContent;
	private FolderRunController solutionFolderRunController;
	private SubscriptionContext subsContext;
	private ContextualSubscriptionController contextualSubscriptionCtr;

	
	/**
	 * Implements a dropbox.
	 * @param ureq
	 * @param wControl
	 * @param config
	 * @param node
	 * @param courseEnv
	 * @param previewMode
	 */
	public SolutionController(UserRequest ureq, WindowControl wControl, CourseNode node, CourseEnvironment courseEnv, boolean previewMode) {
		super(ureq, wControl);
		
		myContent = createVelocityContainer("solutionRun");
		
		// returnbox display
		String solutionPath = SolutionController.getSolutionPathRelToFolderRoot(courseEnv, node);
		VFSContainer rootFolder = VFSManager.olatRootContainer(solutionPath, null);
		VFSContainer namedContainer = new NamedContainerImpl("solutions", rootFolder); 
		namedContainer.setLocalSecurityCallback(new ReadOnlyCallback());
		solutionFolderRunController = new FolderRunController(namedContainer, false, ureq, wControl);
		solutionFolderRunController.addControllerListener(this);
		myContent.put("solutionbox", solutionFolderRunController.getInitialComponent());
		if (!previewMode) {
			// offer subscription, but not to guests
			subsContext = SolutionFileUploadNotificationHandler.getSubscriptionContext(courseEnv, node);
			if (subsContext != null) {
				contextualSubscriptionCtr = AbstractTaskNotificationHandler.createContextualSubscriptionController(ureq, wControl, solutionPath, subsContext, SolutionController.class);
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
	
	@Override
	public void event(UserRequest ureq, Component source, Event event) {
		//
	}
}
