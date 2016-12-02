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

package org.olat.course.nodes.projectbroker;

import java.io.File;

import org.olat.core.CoreSpringFactory;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.translator.Translator;
import org.olat.core.util.Util;
import org.olat.core.util.vfs.callbacks.ReadOnlyCallback;
import org.olat.core.util.vfs.callbacks.VFSSecurityCallback;
import org.olat.course.nodes.CourseNode;
import org.olat.course.nodes.projectbroker.datamodel.Project;
import org.olat.course.nodes.projectbroker.service.ProjectGroupManager;
import org.olat.course.nodes.ta.DropboxController;
import org.olat.course.nodes.ta.DropboxScoringViewController;
import org.olat.course.nodes.ta.ReturnboxController;
import org.olat.course.run.userview.UserCourseEnvironment;

/**
 * @author Christian Guretzki
 */

public class ProjectBrokerDropboxScoringViewController extends DropboxScoringViewController {

	private Project project;
	private final ProjectGroupManager projectGroupManager;
	
	/**
	 * Scoring view of the dropbox.
	 * 
	 * @param ureq
	 * @param wControl
	 * @param node
	 * @param userCourseEnv
	 */
	public ProjectBrokerDropboxScoringViewController(Project project, UserRequest ureq, WindowControl wControl, CourseNode node, UserCourseEnvironment userCourseEnv) { 
		super(ureq, wControl, node, userCourseEnv, false);	
		this.project = project;
		projectGroupManager = CoreSpringFactory.getImpl(ProjectGroupManager.class);
		this.setVelocityRoot(Util.getPackageVelocityRoot(DropboxScoringViewController.class));
		Translator fallbackTranslator = Util.createPackageTranslator(this.getClass(), ureq.getLocale());
		Translator myTranslator = Util.createPackageTranslator(DropboxScoringViewController.class, ureq.getLocale(), fallbackTranslator);
		setTranslator(myTranslator);
		boolean hasNotification = projectGroupManager.isProjectManagerOrAdministrator(ureq, userCourseEnv.getCourseEnvironment(), project);
		init(ureq, hasNotification);
	}
	
	@Override
	protected String getDropboxFilePath(String assesseeName) {
		return DropboxController.getDropboxPathRelToFolderRoot(userCourseEnv.getCourseEnvironment(), node)
		+ File.separator + project.getKey();
	}

	@Override
	protected String getReturnboxFilePath(String assesseeName) {
		return ReturnboxController.getReturnboxPathRelToFolderRoot(userCourseEnv.getCourseEnvironment(), node) 
		+ File.separator + project.getKey();
	}

	@Override
	protected VFSSecurityCallback getDropboxVfsSecurityCallback() {
		return new ReadOnlyCallback();
	}
}