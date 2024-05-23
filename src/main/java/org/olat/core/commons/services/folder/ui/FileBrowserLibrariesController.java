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

/**
 * 
 * Initial date: 19 Apr 2024<br>
 * @author uhensler, urs.hensler@frentix.com, https://www.frentix.com
 *
 */
public class FileBrowserLibrariesController extends BasicController {
	
	private static final String CMD_MEDIA_CENTER = "media";
	
	private final VelocityContainer mainVC;
	private final TooledStackedPanel stackedPanel;

	private FileBrowserMediaCenterController mediaCenterCtrl;

	private int counter = 0;
	
	public FileBrowserLibrariesController(UserRequest ureq, WindowControl wControl, TooledStackedPanel stackedPanel) {
		super(ureq, wControl);
		this.stackedPanel = stackedPanel;
		
		mainVC = createVelocityContainer("browser_mega_buttons");
		putInitialPanel(mainVC);
		
		List<Link> links = List.of(
				createLink(CMD_MEDIA_CENTER, "o_icon_media", translate("browser.storages.media"))
			);
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

}
