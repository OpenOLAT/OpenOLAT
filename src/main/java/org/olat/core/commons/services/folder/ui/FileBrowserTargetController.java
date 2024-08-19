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

import org.olat.core.commons.services.folder.ui.event.FileBrowserSelectionEvent;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.segmentedview.SegmentViewComponent;
import org.olat.core.gui.components.segmentedview.SegmentViewEvent;
import org.olat.core.gui.components.segmentedview.SegmentViewFactory;
import org.olat.core.gui.components.stack.TooledStackedPanel;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.util.vfs.VFSContainer;

/**
 * 
 * Initial date: 12 Aug 2024<br>
 * @author uhensler, urs.hensler@frentix.com, https://www.frentix.com
 *
 */
public class FileBrowserTargetController extends BasicController {

	private final VelocityContainer mainVC;
	private final SegmentViewComponent segmentView;
	private final Link currentFolderLink;
	private Link fileHubLink;
	
	private FolderTargetController currentFolderCtrl;
	private TooledStackedPanel stackedPanel;
	private FileBrowserMainController fileBrowserCtrl;
	
	private final VFSContainer rootContainer;
	private final VFSContainer currentContainer;
	private final String submitButtonText;
	private VFSContainer selectedContainer;
	
	public FileBrowserTargetController(UserRequest ureq, WindowControl wControl, VFSContainer rootContainer,
			VFSContainer currentContainer, String submitButtonText, boolean showFileHub) {
		super(ureq, wControl);
		this.rootContainer = rootContainer;
		this.currentContainer = currentContainer;
		this.submitButtonText = submitButtonText;
		
		mainVC = createVelocityContainer("browser_target");
		putInitialPanel(mainVC);
		
		segmentView = SegmentViewFactory.createSegmentView("segments", mainVC, this);
		segmentView.setElementCssClass("o_block_bottom");
		segmentView.setDontShowSingleSegment(true);
		
		currentFolderLink = LinkFactory.createLink("browser.current.folder", mainVC, this);
		segmentView.addSegment(currentFolderLink, true);
		
		if (showFileHub) {
			fileHubLink = LinkFactory.createLink("browser.file.hub", mainVC, this);
			segmentView.addSegment(fileHubLink, true);
		}
		
		doOpenCurrentFolder(ureq);
	}
	
	public Object getUserObject() {
		return currentFolderCtrl.getUserObject();
	}
	
	public void setUserObject(Object userObject) {
		currentFolderCtrl.setUserObject(userObject);
	}
	
	public VFSContainer getSelectedContainer() {
		return selectedContainer;
	}

	private void setSelectedContainer(VFSContainer selectedContainer) {
		this.selectedContainer = selectedContainer;
	}

	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if (source == currentFolderCtrl) {
			if (event == Event.DONE_EVENT) {
				selectedContainer = currentFolderCtrl.getSelectedContainer();
			}
			fireEvent(ureq, event);
		} else if (source == fileBrowserCtrl) {
			if (event instanceof FileBrowserSelectionEvent selectionEvent) {
				selectionEvent.getVfsItems().stream()
						.filter(item -> item instanceof VFSContainer)
						.findFirst()
						.ifPresent(item -> setSelectedContainer((VFSContainer)item));
				if (getSelectedContainer() != null) {
					fireEvent(ureq, Event.DONE_EVENT);
				} else {
					fireEvent(ureq, Event.CANCELLED_EVENT);
				}
			} else if (event == Event.CANCELLED_EVENT) {
				fireEvent(ureq, event);
			}
		}
		super.event(ureq, source, event);
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		if (source == segmentView) {
			if (event instanceof SegmentViewEvent) {
				SegmentViewEvent sve = (SegmentViewEvent)event;
				String segmentCName = sve.getComponentName();
				Component clickedLink = mainVC.getComponent(segmentCName);
				if (clickedLink == currentFolderLink) {
					doOpenCurrentFolder(ureq);
				} else if (clickedLink == fileHubLink) {
					doOpenFileHub(ureq);
				}
			}
		} 
	}
	
	private void doOpenCurrentFolder(UserRequest ureq) {
		if (currentFolderCtrl == null) {
			currentFolderCtrl = new FolderTargetController(ureq, getWindowControl(), rootContainer, currentContainer, submitButtonText);
			listenTo(currentFolderCtrl);
		}
		mainVC.put("segmentCmp", currentFolderCtrl.getInitialComponent());
		segmentView.select(currentFolderLink);
	}
	
	private void doOpenFileHub(UserRequest ureq) {
		if (fileBrowserCtrl == null) {
			stackedPanel = new TooledStackedPanel("fileHubBreadcrumb", getTranslator(), this);
			stackedPanel.setToolbarEnabled(false);
			
			fileBrowserCtrl = new FileBrowserMainController(ureq, getWindowControl(), stackedPanel,
					FileBrowserSelectionMode.targetSingle, null, submitButtonText);
			listenTo(fileBrowserCtrl);
			stackedPanel.pushController(translate("browser.file.hub"), fileBrowserCtrl);
		}
		
		mainVC.put("segmentCmp", stackedPanel);
		segmentView.select(fileHubLink);
	}

}
