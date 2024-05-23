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
package org.olat.home;

import java.util.List;

import org.olat.core.commons.services.folder.ui.FolderUIFactory;
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
import org.olat.core.gui.control.generic.dtabs.Activateable2;
import org.olat.core.id.OLATResourceable;
import org.olat.core.id.Roles;
import org.olat.core.id.context.ContextEntry;
import org.olat.core.id.context.StateEntry;
import org.olat.core.util.Util;
import org.olat.core.util.resource.OresHelper;
import org.olat.modules.cemedia.ui.MediaCenterConfig;
import org.olat.modules.cemedia.ui.MediaCenterController;
import org.olat.modules.cemedia.ui.MediaCentersController;

/**
 * 
 * Initial date: 19 Apr 2024<br>
 * @author uhensler, urs.hensler@frentix.com, https://www.frentix.com
 *
 */
public class PersonalFileHubStoragesController extends BasicController implements Activateable2 {
	
	private static final String CMD_MEDIA_CENTER = "media";
	
	private final VelocityContainer mainVC;
	private final TooledStackedPanel stackedPanel;

	private Controller mediaCenterCtrl;

	private int counter = 0;
	
	public PersonalFileHubStoragesController(UserRequest ureq, WindowControl wControl, TooledStackedPanel stackedPanel) {
		super(ureq, wControl, Util.createPackageTranslator(FolderUIFactory.class, ureq.getLocale()));
		setTranslator(Util.createPackageTranslator(MediaCenterController.class, getLocale(), getTranslator()));
		this.stackedPanel = stackedPanel;
		
		velocity_root = Util.getPackageVelocityRoot(FolderUIFactory.class);
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
	public void activate(UserRequest ureq, List<ContextEntry> entries, StateEntry state) {
		if(entries == null || entries.isEmpty()) return;

		String resName = entries.get(0).getOLATResourceable().getResourceableTypeName();
		if("MediaCenter".equalsIgnoreCase(resName) || "Media".equalsIgnoreCase(resName)) {
			doOpenMediaCenter(ureq);
			if (mediaCenterCtrl instanceof Activateable2 activateable2) {
				List<ContextEntry> subEntries = entries.subList(1, entries.size());
				activateable2.activate(ureq, subEntries, entries.get(0).getTransientState());
				
			}
		} else if("Media".equalsIgnoreCase(resName)) {
			doOpenMediaCenter(ureq);
			if (mediaCenterCtrl instanceof Activateable2 activateable2) {
				activateable2.activate(ureq, entries, state);
			}
		}
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
		
		Roles roles = ureq.getUserSession().getRoles();
		if (roles.isAdministrator() || roles.isLearnResourceManager()) {
			OLATResourceable ores = OresHelper.createOLATResourceableInstance("MediaCenter", 0l);
			WindowControl swControl = addToHistory(ureq, ores, null, getWindowControl(), true);
			MediaCentersController mediaCentersCtrl = new MediaCentersController(ureq, swControl, stackedPanel);
			listenTo(mediaCentersCtrl);
			mediaCenterCtrl = mediaCentersCtrl;
		} else {
			OLATResourceable ores = OresHelper.createOLATResourceableInstance("MediaCenter", 0l);
			WindowControl swControl = addToHistory(ureq, ores, null, getWindowControl(), true);
			MediaCenterController myMediaCenterCtrl = new MediaCenterController(ureq, swControl,
					stackedPanel, MediaCenterConfig.valueOfMy());
			myMediaCenterCtrl.setFormTranslatedTitle(translate("media.center.my.title"));
			listenTo(myMediaCenterCtrl);
			mediaCenterCtrl = myMediaCenterCtrl;
		}
		stackedPanel.pushController(title, mediaCenterCtrl);
	}

}
