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
import org.olat.core.util.vfs.VFSContainer;

/**
 * 
 * Initial date: 18 Apr 2024<br>
 * @author uhensler, urs.hensler@frentix.com, https://www.frentix.com
 *
 */
public class FolderTargetController extends BasicController {
	
	private final TooledStackedPanel stackedPanel;
	
	private final FolderSelectionController selectionCtrl;
	
	private Object userObject;
	private VFSContainer selectedContainer;

	public FolderTargetController(UserRequest ureq, WindowControl wControl, VFSContainer rootContainer,
			VFSContainer currentContainer, String submitButtonText) {
		super(ureq, wControl);
		stackedPanel = new TooledStackedPanel("folderBreadcrumb", getTranslator(), this);
		stackedPanel.setToolbarEnabled(false);
		putInitialPanel(stackedPanel);
		
		selectionCtrl = new FolderSelectionController(ureq, wControl, stackedPanel, rootContainer, FileBrowserSelectionMode.targetSingle,
				submitButtonText);
		listenTo(selectionCtrl);
		stackedPanel.pushController(rootContainer.getName(), selectionCtrl);
		selectionCtrl.updateCurrentContainer(ureq, currentContainer);
	}
	
	public Object getUserObject() {
		return userObject;
	}
	
	public void setUserObject(Object userObject) {
		this.userObject = userObject;
	}
	
	public VFSContainer getSelectedContainer() {
		return selectedContainer;
	}
	
	private void setSelectdContainer(VFSContainer selectedContainer) {
		this.selectedContainer = selectedContainer;
	}
	
	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		//
	}
	
	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if (source == selectionCtrl) {
			if (event instanceof FileBrowserSelectionEvent selectionEvent) {
				selectionEvent.getVfsItems().stream()
						.filter(item -> item instanceof VFSContainer)
						.findFirst()
						.ifPresent(item -> setSelectdContainer((VFSContainer)item));
				if (getSelectedContainer() != null) {
					fireEvent(ureq, Event.DONE_EVENT);
				} else {
					fireEvent(ureq, Event.CANCELLED_EVENT);
				}
			} else if (event instanceof FileBrowserPushEvent) {
				// Do not fire to avoid closing the modal.
			} else {
				fireEvent(ureq, event);
			}
		}
		super.event(ureq, source, event);
	}

}
