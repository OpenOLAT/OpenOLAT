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
package org.olat.modules.project.ui;

import java.util.List;

import org.olat.core.commons.services.doceditor.DocEditor.Mode;
import org.olat.core.commons.services.doceditor.DocEditorConfigs;
import org.olat.core.commons.services.doceditor.DocEditorService;
import org.olat.core.commons.services.doceditor.DocumentSavedEvent;
import org.olat.core.commons.services.doceditor.drawio.DrawioEditor;
import org.olat.core.commons.services.doceditor.drawio.DrawioEditorConfig;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.dropdown.Dropdown;
import org.olat.core.gui.components.dropdown.DropdownOrientation;
import org.olat.core.gui.components.emptystate.EmptyState;
import org.olat.core.gui.components.emptystate.EmptyStateFactory;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.gui.control.generic.closablewrapper.CloseableModalController;
import org.olat.core.gui.control.winmgr.CommandFactory;
import org.olat.core.util.Formatter;
import org.olat.core.util.coordinate.CoordinatorManager;
import org.olat.core.util.event.GenericEventListener;
import org.olat.core.util.vfs.VFSLeaf;
import org.olat.core.util.vfs.VFSMediaMapper;
import org.olat.modules.project.ProjActivity;
import org.olat.modules.project.ProjActivity.Action;
import org.olat.modules.project.ProjActivitySearchParams;
import org.olat.modules.project.ProjProject;
import org.olat.modules.project.ProjProjectSecurityCallback;
import org.olat.modules.project.ProjWhiteboardFileType;
import org.olat.modules.project.ProjectService;
import org.olat.user.UserManager;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 28 Aug 2023<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class ProjWhiteboardController extends BasicController implements GenericEventListener {
	
	private static final List<Action> MODIFIED_ACTIONS = List.of(Action.whiteboardCreate, Action.whiteboardEdit);
	
	private VelocityContainer mainVC;
	private Dropdown cmdsDropDown;
	private Link viewLink;
	private Link editLink;
	private Link exportLink;
	private Link resetLink;
	private EmptyState emptyState;

	private CloseableModalController cmc;
	private ProjConfirmationController resetConfirmationCtrl;

	private final ProjProject project;
	private final ProjProjectSecurityCallback secCallback;
	private final Formatter formatter;
	private VFSLeaf whiteboardLeaf;
	
	@Autowired
	private ProjectService projectService;
	@Autowired
	private DocEditorService docEditorService;
	@Autowired
	private DrawioEditor drawioEditor;
	@Autowired
	private UserManager userManager;

	public ProjWhiteboardController(UserRequest ureq, WindowControl wControl, ProjProject project, ProjProjectSecurityCallback secCallback) {
		super(ureq, wControl);
		this.project = project;
		this.secCallback = secCallback;
		this.formatter = Formatter.getInstance(getLocale());
		this.whiteboardLeaf = projectService.getWhiteboard(project, ProjWhiteboardFileType.board);
		
		mainVC = createVelocityContainer("whiteboard");
		putInitialPanel(mainVC);
		
		//Commands
		cmdsDropDown = new Dropdown("cmds", null, false, getTranslator());
		cmdsDropDown.setCarretIconCSS("o_icon o_icon_commands");
		cmdsDropDown.setEmbbeded(true);
		cmdsDropDown.setOrientation(DropdownOrientation.right);
		mainVC.put("cmds", cmdsDropDown);
		
		if (secCallback.canEditWhiteboard()) {
			editLink = LinkFactory.createToolLink("whiteboard.edit", translate("whiteboard.edit"), this, "o_icon_edit");
			editLink.setNewWindow(true, true);
			cmdsDropDown.addComponent(editLink);
			
			exportLink = LinkFactory.createToolLink("whiteboard.export", translate("whiteboard.export"), this, "o_icon_export");
			cmdsDropDown.addComponent(exportLink);
			
			cmdsDropDown.addComponent(new Dropdown.Spacer("whitebard.spacer"));
			
			resetLink = LinkFactory.createToolLink("whiteboard.reset", translate("whiteboard.reset"), this, "o_icon_reset");
			cmdsDropDown.addComponent(resetLink);
		} else {
			viewLink = LinkFactory.createToolLink("whiteboard.view", translate("whiteboard.view"), this, "o_icon_view");
			viewLink.setNewWindow(true, true);
			cmdsDropDown.addComponent(viewLink);
		}
		
		// Empty state
		emptyState = EmptyStateFactory.create("whiteboard.empty.state", mainVC, this);
		emptyState.setIconCss("o_icon o_icon_proj_whiteboard");
		emptyState.setIndicatorIconCss("o_no_indicator");
		emptyState.setMessageI18nKey("whiteboard.empty.message");
		emptyState.setButtonI18nKey("whiteboard.empty.button");
		emptyState.getButton().setNewWindow(true, true);
		
		reload(ureq);
	}

	@Override
	public void event(Event event) {
		if (event instanceof DocumentSavedEvent dccEvent) {
			if (whiteboardLeaf != null && whiteboardLeaf.getMetaInfo().getKey().equals(dccEvent.getVfsMetadatKey())) {
				reload(null);
			}
		}
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		if ("open".equals(event.getCommand())) {
			doOpenWhiteboard(ureq);
		} else if (source == emptyState) {
			doCreateWhiteboard(ureq);
		} else if (source == viewLink) {
			doOpenWhiteboard(ureq);
		} else if (source == editLink) {
			doOpenWhiteboard(ureq);
		} else if (source == exportLink) {
			doExportWhiteboard(ureq);
		} else if (source == resetLink) {
			doConfirmResetWhiteboard(ureq);
		}
	}
	
	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if (resetConfirmationCtrl == source) {
			if (event == Event.DONE_EVENT) {
				doResetWhiteboard(ureq);
			}
			cmc.deactivate();
			cleanUp();
		}
		super.event(ureq, source, event);
	}


	private void cleanUp() {
		removeAsListenerAndDispose(resetConfirmationCtrl);
		removeAsListenerAndDispose(cmc);
		resetConfirmationCtrl = null;
		cmc = null;
	}

	@Override
	public void doDispose() {
		CoordinatorManager.getInstance().getCoordinator().getEventBus().deregisterFor(this, DocEditorService.DOCUMENT_SAVED_EVENT_CHANNEL);
		super.doDispose();
	}

	public void reload(UserRequest ureq) {
		VFSLeaf previewLeaf = projectService.getWhiteboard(project, ProjWhiteboardFileType.preview);
		if (whiteboardLeaf != null && previewLeaf != null) {
			VFSMediaMapper whiteboardMapper = new VFSMediaMapper(previewLeaf);
			String mapperId = "proj-whiteboard" + project.getKey() + previewLeaf.getLastModified();
			String whiteboardUrl = registerCacheableMapper(ureq, mapperId, whiteboardMapper, 3600);
			mainVC.contextPut("whiteboardUrl", whiteboardUrl);
			
			ProjActivitySearchParams searchParams = new ProjActivitySearchParams();
			searchParams.setProject(project);
			searchParams.setActions(MODIFIED_ACTIONS);
			ProjActivity lastActivity = projectService.getProjectKeyToLastActivity(searchParams).getOrDefault(project.getKey(), null);
			
			if (lastActivity != null) {
				String modifiedDate = formatter.formatDateRelative(lastActivity.getCreationDate());
				String modifiedBy = userManager.getUserDisplayName(lastActivity.getDoer().getKey());
				String modified = translate("date.by", modifiedDate, modifiedBy);
				mainVC.contextPut("modified", modified);
			}
		}
		
		boolean whiteboardAvailable = whiteboardLeaf != null;
		cmdsDropDown.setVisible(whiteboardAvailable);
		emptyState.setVisible(!whiteboardAvailable);
		mainVC.setDirty(true);
	}

	private void doCreateWhiteboard(UserRequest ureq) {
		projectService.createWhiteboard(getIdentity(), project, getLocale());
		whiteboardLeaf = projectService.getWhiteboard(project, ProjWhiteboardFileType.board);
		reload(ureq);
		doOpenWhiteboard(ureq);
	}
	
	private void doOpenWhiteboard(UserRequest ureq) {
		// Only the editor needs automatic refresh of the whiteboard.
		CoordinatorManager.getInstance().getCoordinator().getEventBus().registerFor(this, null, DocEditorService.DOCUMENT_SAVED_EVENT_CHANNEL);
		
		// Reload. Maybe someone else has reset the whiteboard.
		whiteboardLeaf = projectService.getWhiteboard(project, ProjWhiteboardFileType.board);
		if (whiteboardLeaf == null) {
			reload(ureq);
			getWindowControl().getWindowBackOffice().sendCommandTo(CommandFactory.createNewWindowCancelRedirectTo());
			return;
		}
		
		VFSLeaf previewLeaf = projectService.getWhiteboard(project, ProjWhiteboardFileType.preview);
		DocEditorConfigs configs = DocEditorConfigs.builder()
				.withMode(secCallback.canEditWhiteboard()? Mode.EDIT: Mode.VIEW)
				.withFireSavedEvent(true)
				.addConfig(DrawioEditorConfig.builder().withSvgPreviewLeaf(previewLeaf).build())
				.build(whiteboardLeaf);
		// Use explicitly the draw.io editor because the common image editor is read only
		String url = docEditorService.prepareDocumentUrl(ureq.getUserSession(), drawioEditor, configs);
		getWindowControl().getWindowBackOffice().sendCommandTo(CommandFactory.createNewWindowRedirectTo(url));
	}

	private void doExportWhiteboard(UserRequest ureq) {
		projectService.copyWhiteboardToFiles(getIdentity(), project);
		fireEvent(ureq, Event.CHANGED_EVENT);
	}

	private void doConfirmResetWhiteboard(UserRequest ureq) {
		if (guardModalController(resetConfirmationCtrl)) return;
		if (guardResetLocked()) return;
		
		String message = translate("whiteboard.reset.message");
		resetConfirmationCtrl = new ProjConfirmationController(ureq, getWindowControl(), message,
				"whiteboard.reset.confirm", "whiteboard.reset.button", false);
		listenTo(resetConfirmationCtrl);
		
		cmc = new CloseableModalController(getWindowControl(), translate("close"),
				resetConfirmationCtrl.getInitialComponent(), true, translate("whiteboard.reset"), true);
		listenTo(cmc);
		cmc.activate();
	}

	private void doResetWhiteboard(UserRequest ureq) {
		if (guardResetLocked()) return;
		
		projectService.resetWhiteboard(getIdentity(), project);
		whiteboardLeaf = null;
		reload(ureq);
		fireEvent(ureq, Event.CHANGED_EVENT);
	}
	
	private boolean guardResetLocked() {
		if (drawioEditor.isLockedForMe(whiteboardLeaf, getIdentity(), Mode.EDIT)) {
			showInfo("whiteboard.reset.locked");
			return true;
		}
		return false;
	}
	
}
