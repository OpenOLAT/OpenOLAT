/**
 * <a href="http://www.openolat.org">
 * OpenOLAT - Online Learning and Training</a><br>
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); <br>
 * you may not use this file except in compliance with the License.<br>
 * You may obtain a copy of the License at the
 * <a href="http://www.apache.org/licenses/LICENSE-2.0">Apache homepage</a>
 * <p>
 * Unless required by applicable law or agreed to in writing,<br>
 * software distributed under the License is distributed on an "AS IS" BASIS, <br>
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
 * See the License for the specific language governing permissions and <br>
 * limitations under the License.
 * <p>
 * Initial code contributed and copyrighted by<br>
 * frentix GmbH, http://www.frentix.com
 * <p>
 */
package org.olat.modules.video.ui;

import java.util.List;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.dropdown.Dropdown;
import org.olat.core.gui.components.dropdown.Dropdown.Spacer;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.stack.PopEvent;
import org.olat.core.gui.components.stack.RootEvent;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.dtabs.Activateable2;
import org.olat.core.id.OLATResourceable;
import org.olat.core.id.context.BusinessControlFactory;
import org.olat.core.id.context.ContextEntry;
import org.olat.core.id.context.StateEntry;
import org.olat.core.util.resource.OresHelper;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryEntrySecurity;
import org.olat.repository.ui.RepositoryEntryRuntimeController;

/**
 *
 * The runtime add configuration management and delivery options.
 *
 * Initial date: 01.04.2015<br>
 * @author dfurrer, dirk.furrer@frentix.com, http://www.frentix.com
 *
 */
public class VideoRuntimeController extends RepositoryEntryRuntimeController {

	private Link editorLink;
	private Link changeVideoLink;
	private VideoSettingsController videoSettingsCtr;
	private RepositoryEntry repositoryEntry;

	public VideoRuntimeController(UserRequest ureq, WindowControl wControl,
			RepositoryEntry re, RepositoryEntrySecurity reSecurity, RuntimeControllerCreator runtimeControllerCreator) {
		super(ureq, wControl, re, reSecurity, runtimeControllerCreator);
		this.repositoryEntry = re;
	}
	
	@Override
	protected void initToolsMenuEditor(Dropdown toolsDropdown) {
		if (reSecurity.isEntryAdmin()) {
			toolsDropdown.addComponent(new Spacer("video-editor"));
			
			editorLink = LinkFactory.createToolLink("metaDataConfig", translate("tab.video.settings"), this);
			editorLink.setUrl(BusinessControlFactory.getInstance()
					.getAuthenticatedURLFromBusinessPathStrings(businessPathEntry, "[Editor:0][metadata:0]"));
			editorLink.setIconLeftCSS("o_icon o_icon-fw o_icon_quota o_icon_settings");
			toolsDropdown.addComponent(editorLink);
			
			changeVideoLink = LinkFactory.createToolLink("changeVideo", translate("tab.video.exchange"), this);
			changeVideoLink.setIconLeftCSS("o_icon o_icon_refresh o_icon-fw");
			toolsDropdown.addComponent(changeVideoLink);
		}
	}

	@Override
	public void activate(UserRequest ureq, List<ContextEntry> entries, StateEntry state) {
		entries = removeRepositoryEntry(entries);
		if(entries != null && !entries.isEmpty()) {
			String type = entries.get(0).getOLATResourceable().getResourceableTypeName();
			if("Editor".equalsIgnoreCase(type)) {
				entries = entries.subList(1, entries.size());
				doEditor(ureq).activate(ureq, entries, null);
			}
		}
		super.activate(ureq, entries, state);
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		if(editorLink == source){
			doEditor(ureq);
		} else if (changeVideoLink == source) {
			doReplaceVideo(ureq);
		} else {
			if (event instanceof RootEvent || event instanceof PopEvent) {
				// reload the video, maybe some new transcoded files available
				VideoDisplayController videoDisplayCtr = (VideoDisplayController)getRuntimeController();
				videoDisplayCtr.reloadVideo(ureq);
			}
			// maybe something else needs to be done
			super.event(ureq, source, event);
		}
		doRefreshVideoPosterIfEntryAdmin();
	}
	
	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if(source instanceof VideoResourceEditController) {
			if(event == Event.CHANGED_EVENT) {
				VideoDisplayController videoDisplayCtr = (VideoDisplayController)getRuntimeController();
				videoDisplayCtr.reloadVideo(ureq);
			}
		}
		super.event(ureq, source, event);
	}
	
	private void doReplaceVideo (UserRequest ureq) {
		VideoResourceEditController resourceCtrl = new VideoResourceEditController(ureq, getWindowControl(), repositoryEntry);
		listenTo(resourceCtrl);
		pushController(ureq, translate("tab.video.settings"), resourceCtrl);
		setActiveTool(changeVideoLink);
	}

	protected Activateable2 doEditor(UserRequest ureq) {
		removeAsListenerAndDispose(videoSettingsCtr);

		RepositoryEntry entry = getRepositoryEntry();
		OLATResourceable ores = OresHelper.createOLATResourceableType("Editor");
		WindowControl swControl = addToHistory(ureq, ores, null);
		VideoSettingsController configCtrl = new VideoSettingsController(ureq, swControl, entry);
		listenTo(configCtrl);
		videoSettingsCtr = pushController(ureq, translate("tab.video.settings"), configCtrl);
		setActiveTool(editorLink);
		return videoSettingsCtr;
	}
	
	private void doRefreshVideoPosterIfEntryAdmin() {
		if (reSecurity.isEntryAdmin()){
			VideoDisplayController videoDisplayCtr = (VideoDisplayController)getRuntimeController();
			videoDisplayCtr.reloadVideoPoster();
		}
	}
}