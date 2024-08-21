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

import java.util.ArrayList;
import java.util.List;

import org.olat.core.commons.services.folder.ui.event.FileBrowserPushEvent;
import org.olat.core.commons.services.folder.ui.event.FileBrowserTitleEvent;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.stack.TooledStackedPanel;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.util.vfs.VFSContainer;
import org.olat.modules.sharepoint.SharePointModule;
import org.olat.modules.sharepoint.SharePointService;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 19 Apr 2024<br>
 * @author uhensler, urs.hensler@frentix.com, https://www.frentix.com
 *
 */
public class FileBrowserLibrariesController extends BasicController {
	
	private static final String CMD_MEDIA_CENTER = "media";
	private static final String CMD_SHARE_POINT = "sharepoint";
	private static final String CMD_ONE_DRIVE = "onedrive";
	
	private final VelocityContainer mainVC;
	private final TooledStackedPanel stackedPanel;

	private FileBrowserMediaCenterController mediaCenterCtrl;
	private FolderSelectionController folderSelectionCtrl;
	
	private final FileBrowserSelectionMode selectionMode;
	private final String submitButtonText;
	private int counter = 0;
	
	@Autowired
	private SharePointModule sharePointModule;
	@Autowired
	private SharePointService sharePointService;
	
	public FileBrowserLibrariesController(UserRequest ureq, WindowControl wControl, TooledStackedPanel stackedPanel,
			FileBrowserSelectionMode selectionMode, String submitButtonText) {
		super(ureq, wControl);
		this.stackedPanel = stackedPanel;
		this.selectionMode = selectionMode;
		this.submitButtonText = submitButtonText;
		
		mainVC = createVelocityContainer("browser_mega_buttons");
		putInitialPanel(mainVC);
		
		List<Link> links = new ArrayList<>();
		if (FileBrowserSelectionMode.targetSingle != selectionMode) {
			links.add(createLink(CMD_MEDIA_CENTER, "o_icon_media", translate("browser.storages.media")));
		}
		// Temporary until save to SharePoint is implemented
		if((FileBrowserSelectionMode.targetSingle != selectionMode || sharePointModule.isSitesWriteEnabled())
				&& sharePointModule.canSharePoint(ureq.getUserSession())) {
			links.add(createLink(CMD_SHARE_POINT, "o_icon_provider_adfs", translate("browser.storages.share.point")));
		}
		if (sharePointModule.canOneDrive(ureq.getUserSession())) {
			links.add(createLink(CMD_ONE_DRIVE, "o_icon_onedrive", translate("browser.storages.one.drive")));
		}
		mainVC.contextPut("links", links);
	}
	
	private Link createLink(String cmd, String iconCSS, String name) {
		Link link = LinkFactory.createCustomLink("cont_" + counter++, cmd, null, Link.LINK_CUSTOM_CSS + Link.NONTRANSLATED, mainVC, this);
		link.setElementCssClass("btn btn-default o_button_mega");
		link.setIconLeftCSS("o_icon o_icon-xl " + iconCSS);
		String text = "<div class=\"o_mega_headline\">" + name + "</div>";
		text += "<div class=\"o_mega_subline\">" + "</div>";
		link.setCustomDisplayText(text);
		return link;
	}
	
	public boolean isLinkAvailable() {
		return counter > 0;
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		if (source instanceof Link link) {
			String command = link.getCommand();
			if (CMD_MEDIA_CENTER.equals(command)) {
				doOpenMediaCenter(ureq);
			} else if(CMD_SHARE_POINT.equals(command)) {
				doOpenSharePoint(ureq);
			} else if(CMD_ONE_DRIVE.equals(command)) {
				doOpenOneDrive(ureq);
			}
		}
	}

	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if (source == mediaCenterCtrl) {
			fireEvent(ureq, event);
		} else if (source == folderSelectionCtrl) {
			fireEvent(ureq, event);
		}
		super.event(ureq, source, event);
	}

	private void doOpenMediaCenter(UserRequest ureq) {
		String title = translate("browser.storages.media");
		mediaCenterCtrl = new FileBrowserMediaCenterController(ureq, getWindowControl(), title);
		listenTo(mediaCenterCtrl);
		
		stackedPanel.pushController(title, mediaCenterCtrl);
	}
	
	private void doOpenSharePoint(UserRequest ureq) {
		boolean readWrite = FileBrowserSelectionMode.targetSingle == selectionMode && sharePointModule.isSitesWriteEnabled();
		VFSContainer spContainer = sharePointService.getSharePointContainer(ureq.getUserSession(), readWrite);
		doOpenFolderSelection(ureq, spContainer);
	}
	
	private void doOpenOneDrive(UserRequest ureq) {
		VFSContainer oneDriveContainer = sharePointService.getOneDriveContainer(ureq.getUserSession());
		doOpenFolderSelection(ureq, oneDriveContainer);
	}

	private void doOpenFolderSelection(UserRequest ureq, VFSContainer spContainer) {
		folderSelectionCtrl = new FolderSelectionController(ureq, getWindowControl(), stackedPanel, spContainer,
				selectionMode, submitButtonText);
		listenTo(folderSelectionCtrl);
		
		String providerName = spContainer.getName();
		stackedPanel.pushController(providerName, folderSelectionCtrl);

		fireEvent(ureq, new FileBrowserTitleEvent(providerName));
		fireEvent(ureq, new FileBrowserPushEvent());
	}

}
