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

package org.olat.course.nodes.bc;

import org.olat.core.commons.services.folder.ui.FolderController;
import org.olat.core.commons.services.folder.ui.FolderControllerConfig;
import org.olat.core.commons.services.folder.ui.FolderEmailFilter;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.util.vfs.Quota;
import org.olat.core.util.vfs.VFSContainer;
import org.olat.core.util.vfs.callbacks.ReadOnlyCallback;
import org.olat.core.util.vfs.callbacks.VFSSecurityCallback;
import org.olat.course.nodes.BCCourseNode;
import org.olat.course.run.userview.NodeEvaluation;
import org.olat.course.run.userview.UserCourseEnvironment;

/**
 * Description: <br>
 * Initial Date: 10.02.2005 <br>
 * @author Mike Stock
 */
public class BCPreviewController extends BasicController {
	
	private static final FolderControllerConfig FOLDER_CONFIG = FolderControllerConfig.builder()
			.withDisplayWebDAVLinkEnabled(false)
			.withSearchEnabled(false)
			.withMail(FolderEmailFilter.never)
			.build();
	
	public BCPreviewController(UserRequest ureq, WindowControl wControl, BCCourseNode node,
			UserCourseEnvironment userCourseEnv, NodeEvaluation ne) {
		super(ureq, wControl);
		VelocityContainer previewVC = createVelocityContainer("preview");
		putInitialPanel(previewVC);
		
		VFSContainer namedContainer = BCCourseNode.getNodeFolderContainer(node, userCourseEnv.getCourseEnvironment());
		namedContainer.setLocalSecurityCallback(new ReadOnlyCallback());
		FolderController folderCtrl = new FolderController(ureq, wControl, namedContainer, FOLDER_CONFIG);
		listenTo(folderCtrl);
		previewVC.put("folder", folderCtrl.getInitialComponent());
		
		// get additional infos
		boolean canDownload = node.canDownload(ne);
		boolean canUpload = node.canUpload(userCourseEnv, ne);
		VFSSecurityCallback secCallback = new FolderNodeCallback(namedContainer.getRelPath(), canDownload, canUpload, false, false, null);
		previewVC.contextPut("canUpload", Boolean.valueOf(secCallback.canWrite()));
		previewVC.contextPut("canDownload", Boolean.valueOf(secCallback.canRead()));
		Quota q = secCallback.getQuota();
		previewVC.contextPut("quotaKB", (q != null) ? q.getQuotaKB().toString() : "-");
	}

	@Override
	public void event(UserRequest ureq, Component source, Event event) {
		//
	}
}
