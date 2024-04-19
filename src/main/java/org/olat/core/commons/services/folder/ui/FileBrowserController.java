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
public class FileBrowserController extends BasicController {

	private final TooledStackedPanel stackedPanel;
	
	private final FileBrowserMainController mainCtrl;
	
	private Object userObject;

	public FileBrowserController(UserRequest ureq, WindowControl wControl, FileBrowserSelectionMode selectionMode,
			FolderQuota folderQuota, String submitButtonText) {
		super(ureq, wControl);
		
		stackedPanel = new TooledStackedPanel("folderBreadcrumb", getTranslator(), this);
		stackedPanel.setToolbarEnabled(false);
		putInitialPanel(stackedPanel);
		
		mainCtrl = new FileBrowserMainController(ureq, wControl, stackedPanel, selectionMode, folderQuota, submitButtonText);
		listenTo(mainCtrl);
		stackedPanel.pushController(translate("browser.main"), mainCtrl);
	}

	public Object getUserObject() {
		return userObject;
	}

	public void setUserObject(Object userObject) {
		this.userObject = userObject;
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		//
	}
	
	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if (source == mainCtrl) {
			fireEvent(ureq, event);
		}
		super.event(ureq, source, event);
	}

}
