/**
* OLAT - Online Learning and Training<br>
* http://www.olat.org
* <p>
* Licensed under the Apache License, Version 2.0 (the "License"); <br>
* you may not use this file except in compliance with the License.<br>
* You may obtain a copy of the License at
* <p>
* http://www.apache.org/licenses/LICENSE-2.0
* <p>
* Unless required by applicable law or agreed to in writing,<br>
* software distributed under the License is distributed on an "AS IS" BASIS, <br>
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
* See the License for the specific language governing permissions and <br>
* limitations under the License.
* <p>
* Copyright (c) since 2004 at Multimedia- & E-Learning Services (MELS),<br>
* University of Zurich, Switzerland.
* <hr>
* <a href="http://www.openolat.org">
* OpenOLAT - Online Learning and Training</a><br>
* This file has been modified by the OpenOLAT community. Changes are licensed
* under the Apache 2.0 license as the original file.  
* <p>
*/ 

package org.olat.core.commons.editor.fileeditor;

import java.util.List;

import org.apache.logging.log4j.Logger;
import org.olat.core.commons.controllers.linkchooser.CustomLinkTreeModel;
import org.olat.core.commons.editor.htmleditor.HTMLEditorConfig;
import org.olat.core.commons.editor.htmleditor.HTMLEditorController;
import org.olat.core.commons.editor.htmleditor.HTMLReadOnlyController;
import org.olat.core.commons.editor.htmleditor.WysiwygFactory;
import org.olat.core.commons.editor.plaintexteditor.TextEditorController;
import org.olat.core.commons.services.doceditor.Access;
import org.olat.core.commons.services.doceditor.DocEditor.Mode;
import org.olat.core.commons.services.doceditor.DocEditorConfigs;
import org.olat.core.commons.services.doceditor.DocEditorConfigs.Config;
import org.olat.core.commons.services.doceditor.DocEditorService;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.Window;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.gui.control.controller.BlankController;
import org.olat.core.gui.control.generic.dtabs.Activateable2;
import org.olat.core.gui.control.winmgr.CommandFactory;
import org.olat.core.id.context.ContextEntry;
import org.olat.core.id.context.StateEntry;
import org.olat.core.logging.Tracing;
import org.olat.core.util.vfs.VFSLeaf;
import org.olat.core.util.vfs.VFSLockApplicationType;
import org.olat.core.util.vfs.VFSLockManager;
import org.springframework.beans.factory.annotation.Autowired;

public class FileEditorController extends BasicController implements Activateable2 {

	private static final Logger log = Tracing.createLoggerFor(FileEditorController.class);
	
	private Controller editCtrl;

	private final VFSLeaf vfsLeaf;
	private final Access access;
	private boolean temporaryLock;
	
	@Autowired
	private VFSLockManager vfsLockManager;
	@Autowired
	private DocEditorService docEditorService;

	protected FileEditorController(UserRequest ureq, WindowControl wControl, VFSLeaf vfsLeaf,
			DocEditorConfigs configs, Access access) {
		super(ureq, wControl);
		this.vfsLeaf = vfsLeaf;
		this.access = access;
		
		wControl.getWindowBackOffice().getWindow().addListener(this);
		
		VelocityContainer mainVC = createVelocityContainer("file_editor");

		boolean isEdit = Mode.EDIT.equals(configs.getMode());
		if (isEdit) {
			if(vfsLockManager.isLockedForMe(vfsLeaf, ureq.getIdentity(), VFSLockApplicationType.exclusive, FileEditor.TYPE)) {
				isEdit = false;
				access = docEditorService.updateMode(access, Mode.VIEW);
				mainVC.contextPut("editInOtherWindow", Boolean.TRUE);
				mainVC.contextPut("fileName", vfsLeaf.getName());
			} else {
				vfsLockManager.lock(vfsLeaf, getIdentity(), VFSLockApplicationType.exclusive, FileEditor.TYPE);
				temporaryLock = true;
			}
		}
		
		if (vfsLeaf.getName().endsWith(".html") || vfsLeaf.getName().endsWith(".htm")) {
			Config configObj = configs.getConfig(HTMLEditorConfig.TYPE);
			HTMLEditorConfig config = null;
			if (!(configObj instanceof HTMLEditorConfig)) {
				log.error("FileEditor started without configuration! Displayd blank page. File: {}, Identity: {}",
						vfsLeaf, getIdentity());
				editCtrl = new BlankController(ureq, wControl);
			}
			config = (HTMLEditorConfig) configObj;
			if (isEdit) {
				HTMLEditorController htmlCtrl;
				CustomLinkTreeModel customLinkTreeModel = config.getCustomLinkTreeModel();
				if (customLinkTreeModel != null) {
					htmlCtrl = WysiwygFactory.createWysiwygControllerWithInternalLink(ureq, getWindowControl(),
							config.getVfsContainer(), config.getFilePath(), true, configs.isVersionControlled(), customLinkTreeModel,
							null, config.getEdusharingProvider());
				} else {
					htmlCtrl = WysiwygFactory.createWysiwygController(ureq, getWindowControl(), config.getVfsContainer(),
							config.getFilePath(), config.getMediaPath(), true, configs.isVersionControlled(), config.getEdusharingProvider());
				}
				
				htmlCtrl.setNewFile(false);
				htmlCtrl.getRichTextConfiguration().setAllowCustomMediaFactory(config.isAllowCustomMediaFactory());
				if (config.isDisableMedia()) {
					htmlCtrl.getRichTextConfiguration().disableMedia();
				}
				
				editCtrl = htmlCtrl;
			} else {
				editCtrl = new HTMLReadOnlyController(ureq, getWindowControl(), vfsLeaf.getParentContainer(), vfsLeaf.getName(), false);
			}
		}
		else {
			editCtrl = new TextEditorController(ureq, getWindowControl(), vfsLeaf, "utf-8", !isEdit, configs.isVersionControlled());
		}
		listenTo(editCtrl);
		
		mainVC.put("editor", editCtrl.getInitialComponent());
		putInitialPanel(mainVC);
	}

	@Override
	public void activate(UserRequest ureq, List<ContextEntry> entries, StateEntry state) {
		if(editCtrl instanceof Activateable2) {
			((Activateable2)editCtrl).activate(ureq, entries, state);
		}
	}

	@Override
	public void event(UserRequest ureq, Component source, Event event) {
		if(event == Window.CLOSE_WINDOW) {
			if(editCtrl != null) {
				editCtrl.dispatchEvent(ureq, source, event);
			}
			doUnlock();
		}
	}

	@Override
	public void event(UserRequest ureq, Controller source, Event event) {
		if (source == editCtrl) {
			if(event == Event.DONE_EVENT || event == Event.CANCELLED_EVENT) {
				fireEvent(ureq, Event.CLOSE_EVENT);
				doUnlock();
				getWindowControl().getWindowBackOffice().sendCommandTo(CommandFactory.createNewWindowCancelRedirectTo());
			} else {
				fireEvent(ureq, event);
			}
		}
		super.event(ureq, source, event);
	}

	@Override
	protected void doDispose() {
		doUnlock();
		removeAsListenerAndDispose(editCtrl);
		editCtrl = null;
	}
	
	private void doUnlock() {
		if (temporaryLock) {
			log.info("Unlock HTML editor: {}", vfsLeaf);
			vfsLockManager.unlock(vfsLeaf, VFSLockApplicationType.exclusive);
			temporaryLock = false;
		}
		if (access != null) {
			docEditorService.deleteAccess(access);
		}
	}
}