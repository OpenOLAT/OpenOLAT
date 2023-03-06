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
import org.olat.core.id.context.BusinessControlFactory;
import org.olat.core.id.context.ContextEntry;
import org.olat.core.id.context.StateEntry;
import org.olat.modules.video.ui.editor.EditQuestionController;
import org.olat.modules.video.ui.editor.EditQuestionEvent;
import org.olat.modules.video.ui.editor.VideoEditorController;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryEntrySecurity;
import org.olat.repository.ui.RepositoryEntryRuntimeController;
import org.olat.repository.ui.RepositoryEntrySettingsController;

/**
 *
 * The runtime add configuration management and delivery options.
 *
 * Initial date: 01.04.2015<br>
 * @author dfurrer, dirk.furrer@frentix.com, http://www.frentix.com
 *
 */
public class VideoRuntimeController extends RepositoryEntryRuntimeController {

	private Link changeVideoLink;
	private Link editVideoLink;
	private VideoEditorController videoEditorController;

	private RepositoryEntry repositoryEntry;
	private EditQuestionController editQuestionController;

	public VideoRuntimeController(UserRequest ureq, WindowControl wControl,
			RepositoryEntry re, RepositoryEntrySecurity reSecurity, RuntimeControllerCreator runtimeControllerCreator) {
		super(ureq, wControl, re, reSecurity, runtimeControllerCreator);
		this.repositoryEntry = re;
	}
	
	@Override
	protected void initToolsMenuEditor(Dropdown toolsDropdown) {
		if (reSecurity.isEntryAdmin()) {
			toolsDropdown.addComponent(new Spacer("video-editor"));
			
			editVideoLink = LinkFactory.createToolLink("editVideo", translate("tab.video.editor"), this);
			editVideoLink.setUrl(BusinessControlFactory.getInstance()
					.getAuthenticatedURLFromBusinessPathStrings(businessPathEntry, "[Editor:0]"));
			editVideoLink.setIconLeftCSS("o_icon o_icon-fw o_icon_edit");
			toolsDropdown.addComponent(editVideoLink);
		}
	}

	@Override
	protected void initToolsMenuReplaceItem(Dropdown toolsDropdown) {
		if (reSecurity.isEntryAdmin()) {
			changeVideoLink = LinkFactory.createToolLink("changeVideo", translate("tab.video.exchange"), this);
			changeVideoLink.setIconLeftCSS("o_icon o_icon_refresh o_icon-fw");
			toolsDropdown.addComponent(changeVideoLink);
		}
	}

	@Override
	public void activate(UserRequest ureq, List<ContextEntry> entries, StateEntry state) {
		entries = removeRepositoryEntry(entries);
		if (entries != null && !entries.isEmpty()) {
			String type = entries.get(0).getOLATResourceable().getResourceableTypeName();
			if ("Editor".equalsIgnoreCase(type)) {
				doEditVideo(ureq);
				return;
			}
		}
		super.activate(ureq, entries, state);
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		if (changeVideoLink == source) {
			doReplaceVideo(ureq);
		} else if (editVideoLink == source) {
			doEditVideo(ureq);
		} else {
			if (event instanceof RootEvent || event instanceof PopEvent) {
				// reload the video, maybe some new transcoded files available
				VideoDisplayController videoDisplayCtr = (VideoDisplayController)getRuntimeController();
				videoDisplayCtr.reloadVideo(ureq);
				if (event instanceof PopEvent popEvent && popEvent.getController() == videoEditorController) {
					toolbarPanel.removeCssClass("o_edit_mode");
					toolbarPanel.setToolbarEnabled(true);
				}
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
		} else if (source instanceof VideoEditorController) {
			if (event instanceof EditQuestionEvent editQuestionEvent) {
				if (editQuestionController != null) {
					removeAsListenerAndDispose(editQuestionController);
				}
				editQuestionController = new EditQuestionController(ureq, getWindowControl(),
						editQuestionEvent.getQuestionId(), editQuestionEvent.getRepositoryEntry());
				listenTo(editQuestionController);
				toolbarPanel.pushController(translate("video.question.title"), editQuestionController);
			}
		} else if (editQuestionController == source) {
			String questionId = editQuestionController.getQuestionId();
			toolbarPanel.popUpToController(videoEditorController);
			videoEditorController.updateQuestion(questionId);
		}
		super.event(ureq, source, event);
	}
	
	@Override
	protected RepositoryEntrySettingsController createSettingsController(UserRequest ureq, WindowControl bwControl, RepositoryEntry refreshedEntry) {
		return new VideoSettingsController(ureq, addToHistory(ureq, bwControl), toolbarPanel, refreshedEntry);
	}
	
	private void doReplaceVideo (UserRequest ureq) {
		VideoResourceEditController resourceCtrl = new VideoResourceEditController(ureq, getWindowControl(), repositoryEntry);
		listenTo(resourceCtrl);
		pushController(ureq, translate("tab.video.settings"), resourceCtrl);
		setActiveTool(changeVideoLink);
	}

	private void doEditVideo(UserRequest ureq) {
		WindowControl windowControl = getSubWindowControl("Editor");
		videoEditorController = new VideoEditorController(ureq, addToHistory(ureq, windowControl), repositoryEntry);
		listenTo(videoEditorController);
		pushController(ureq, translate("tab.video.editor.breadcrumb"), videoEditorController);
		currentToolCtr = videoEditorController;
		setActiveTool(editVideoLink);
		toolbarPanel.setToolbarEnabled(false);
	}

	@Override
	protected <T extends Controller> T pushController(UserRequest ureq, String name, T controller) {
		toolbarPanel.removeCssClass("o_edit_mode");
		toolbarPanel.setToolbarEnabled(!(controller instanceof VideoEditorController));
		return super.pushController(ureq, name, controller);
	}

	private void doRefreshVideoPosterIfEntryAdmin() {
		if (reSecurity.isEntryAdmin()){
			VideoDisplayController videoDisplayCtr = (VideoDisplayController)getRuntimeController();
			videoDisplayCtr.reloadVideoPoster();
		}
	}
}