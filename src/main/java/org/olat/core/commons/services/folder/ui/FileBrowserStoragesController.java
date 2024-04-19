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

import java.util.List;

import org.olat.core.commons.services.vfs.VFSMetadata;
import org.olat.core.commons.services.vfs.VFSRepositoryService;
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
import org.olat.core.util.vfs.VFSItem;
import org.olat.modules.cemedia.MediaVersion;
import org.olat.modules.cemedia.model.SearchMediaParameters.Access;
import org.olat.modules.cemedia.ui.MediaCenterConfig;
import org.olat.modules.cemedia.ui.MediaCenterController;
import org.olat.modules.cemedia.ui.event.MediaSelectionEvent;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 19 Apr 2024<br>
 * @author uhensler, urs.hensler@frentix.com, https://www.frentix.com
 *
 */
public class FileBrowserStoragesController extends BasicController {
	
	private static final MediaCenterConfig MEDIA_CENTER_CONFIG = new MediaCenterConfig(true, false, false, false, false,
			false, null, MediaCenterController.ALL_TAB_ID, Access.DIRECT, null);
	private static final String CMD_MEDIA_CENTER = "media";
	
	private final VelocityContainer mainVC;
	private final TooledStackedPanel stackedPanel;

	private MediaCenterController mediaCenterCtrl;

	private int counter = 0;
	
	@Autowired
	private VFSRepositoryService vfsRepositoryService;
	
	public FileBrowserStoragesController(UserRequest ureq, WindowControl wControl, TooledStackedPanel stackedPanel) {
		super(ureq, wControl);
		this.stackedPanel = stackedPanel;
		
		mainVC = createVelocityContainer("browser_mega_buttons");
		putInitialPanel(mainVC);
		
		List<Link> links = List.of(
				createLink(CMD_MEDIA_CENTER, "o_icon_media", translate("browser.storages.media"), "")
			);
		mainVC.contextPut("links", links);
	}
	
	private Link createLink(String cmd, String iconCSS, String name, String description) {
		Link link = LinkFactory.createCustomLink("cont_" + counter++, cmd, null, Link.LINK_CUSTOM_CSS + Link.NONTRANSLATED, mainVC, this);
		link.setElementCssClass("btn btn-default o_button_mega");
		link.setIconLeftCSS("o_icon o_icon-xl " + iconCSS);
		String text = "<div class=\"o_mega_headline\">" + name + "</div>";
		text += "<div class=\"o_mega_subline\">" + description + "</div>";
		link.setCustomDisplayText(text);
		return link;
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		if (source instanceof Link link) {
			String command = link.getCommand();
			if (CMD_MEDIA_CENTER.equals(command)) {
				doOpenMediaCenter(ureq);
			}
		}
	}

	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if (source == mediaCenterCtrl) {
			if (event instanceof MediaSelectionEvent mediaSelectionEvent) {
				doFireMediaSelectionEvent(ureq, mediaSelectionEvent);
			}
		}
		super.event(ureq, source, event);
	}

	private void doOpenMediaCenter(UserRequest ureq) {
		String title = translate("browser.storages.media");
		mediaCenterCtrl = new MediaCenterController(ureq, getWindowControl(), stackedPanel, MEDIA_CENTER_CONFIG);
		mediaCenterCtrl.setFormTranslatedTitle(title);
		listenTo(mediaCenterCtrl);
		
		stackedPanel.pushController(title, mediaCenterCtrl);
	}

	private void doFireMediaSelectionEvent(UserRequest ureq, MediaSelectionEvent mediaSelectionEvent) {
		List<MediaVersion> versions = mediaSelectionEvent.getMedia().getVersions();
		if (!versions.isEmpty()) {
			VFSMetadata vfsMetadata = versions.get(0).getMetadata();
			if (vfsMetadata != null) {
				VFSItem vfsItem = vfsRepositoryService.getItemFor(vfsMetadata);
				if (vfsItem != null) {
					fireEvent(ureq, new FileBrowserSelectionEvent(List.of(vfsItem)));
					return;
				}
			}
		}
		
		fireEvent(ureq, Event.CANCELLED_EVENT);
	}

}
