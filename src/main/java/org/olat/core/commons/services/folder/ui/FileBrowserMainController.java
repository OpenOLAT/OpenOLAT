/**
 * <a href="https://www.openolat.org">
 * OpenOLAT - Online Learning and Training</a><br>
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); <br>
 * you may not use this file except in compliance with the License.<br>
 * You may obtain a copy of the License at the
 * <a href="https://www.apache.org/licenses/LICENSE-2.0">Apache homepage</a>
 * <p>
 * Unless required by applicable law or agreed to in writing,<br>
 * software distributed under the License is distributed on an "AS IS" BASIS, <br>
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
 * See the License for the specific language governing permissions and <br>
 * limitations under the License.
 * <p>
 * Initial code contributed and copyrighted by<br>
 * frentix GmbH, https://www.frentix.com
 * <p>
 */
package org.olat.core.commons.services.folder.ui;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.stack.TooledStackedPanel;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;

/**
 * 
 * Initial date: 17 Apr 2024<br>
 * @author uhensler, urs.hensler@frentix.com, https://www.frentix.com
 *
 */
public class FileBrowserMainController extends BasicController {

	private FileBrowserUploadController uploadCtrl;
	private FileBrowserMountPointsController vfsSourcesCtrl;

	public FileBrowserMainController(UserRequest ureq, WindowControl wControl, TooledStackedPanel stackedPanel,
			FileBrowserSelectionMode selectionMode, FolderQuota folderQuota, String submitButtonText) {
		super(ureq, wControl);
		
		VelocityContainer mainVC = createVelocityContainer("browser_main");
		putInitialPanel(mainVC);
		
		if (FileBrowserSelectionMode.sourceMulti == selectionMode || FileBrowserSelectionMode.sourceSingle == selectionMode) {
			mainVC.contextPut("uploadTitle", translate("browser.upload.title"));
			uploadCtrl = new FileBrowserUploadController(ureq, getWindowControl(), selectionMode, folderQuota, submitButtonText);
			listenTo(uploadCtrl);
			mainVC.put("upload", uploadCtrl.getInitialComponent());
		}
		
		mainVC.contextPut("vfsSourcesTitle", translate("browser.vfs.select.title"));
		vfsSourcesCtrl = new FileBrowserMountPointsController(ureq, wControl, stackedPanel, selectionMode, submitButtonText);
		listenTo(vfsSourcesCtrl);
		mainVC.put("vfsSources", vfsSourcesCtrl.getInitialComponent());
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		//
	}
	
	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if (source == uploadCtrl) {
			fireEvent(ureq, event);
		}
		if (source == vfsSourcesCtrl) {
			fireEvent(ureq, event);
		}
		super.event(ureq, source, event);
	}

}
