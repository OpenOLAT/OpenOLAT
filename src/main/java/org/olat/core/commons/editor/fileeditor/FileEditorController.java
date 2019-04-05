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

import org.olat.core.commons.controllers.linkchooser.CustomLinkTreeModel;
import org.olat.core.commons.editor.htmleditor.HTMLEditorConfig;
import org.olat.core.commons.editor.htmleditor.HTMLEditorController;
import org.olat.core.commons.editor.htmleditor.HTMLReadOnlyController;
import org.olat.core.commons.editor.htmleditor.WysiwygFactory;
import org.olat.core.commons.editor.plaintexteditor.TextEditorController;
import org.olat.core.commons.services.vfs.VFSLeafEditor.Mode;
import org.olat.core.commons.services.vfs.VFSLeafEditorConfigs;
import org.olat.core.commons.services.vfs.VFSLeafEditorConfigs.Config;
import org.olat.core.commons.services.vfs.VFSLeafEditorSecurityCallback;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.gui.control.controller.BlankController;
import org.olat.core.logging.OLog;
import org.olat.core.logging.Tracing;
import org.olat.core.util.vfs.VFSLeaf;
import org.olat.core.util.vfs.VFSLockApplicationType;
import org.olat.core.util.vfs.VFSLockManager;
import org.springframework.beans.factory.annotation.Autowired;

public class FileEditorController extends BasicController {

	private static final OLog log = Tracing.createLoggerFor(FileEditorController.class);

	private Controller editCtrl;

	private VFSLeaf vfsLeaf;
	private boolean temporaryLock;
	
	@Autowired
	private VFSLockManager vfsLockManager;

	protected FileEditorController(UserRequest ureq, WindowControl wControl, VFSLeaf vfsLeaf,
			VFSLeafEditorSecurityCallback secCallback, VFSLeafEditorConfigs configs) {
		super(ureq, wControl);
		this.vfsLeaf = vfsLeaf;
		
		boolean isEdit = Mode.EDIT.equals(secCallback.getMode());
		if (isEdit) {
			if(vfsLockManager.isLockedForMe(vfsLeaf, ureq.getIdentity(), VFSLockApplicationType.vfs, null)) {
				// It the file is locked by someone other, show it in preview mode
				isEdit = false;
			} else {
				boolean notLocked = !vfsLockManager.isLocked(vfsLeaf, VFSLockApplicationType.vfs, null);
				if (notLocked) {
					vfsLockManager.lock(vfsLeaf, getIdentity(), VFSLockApplicationType.vfs, null);
					temporaryLock = true;
				}
			}
		}
		
		if (vfsLeaf.getName().endsWith(".html") || vfsLeaf.getName().endsWith(".htm")) {
			Config configObj = configs.getConfig(HTMLEditorConfig.TYPE);
			HTMLEditorConfig config = null;
			if (!(configObj instanceof HTMLEditorConfig)) {
				log.error("FileEditor started without configuration! Displayd blank page. File: " + vfsLeaf + ", Identity: "
					+ getIdentity());
				editCtrl = new BlankController(ureq, wControl);
			}
			config = (HTMLEditorConfig) configObj;
			if (isEdit) {
				HTMLEditorController htmlCtrl;
				CustomLinkTreeModel customLinkTreeModel = config.getCustomLinkTreeModel();
				if (customLinkTreeModel != null) {
					htmlCtrl = WysiwygFactory.createWysiwygControllerWithInternalLink(ureq, getWindowControl(),
							config.getVfsContainer(), config.getFilePath(), true, secCallback.isVersionControlled(), customLinkTreeModel,
							config.getEdusharingProvider());
				} else {
					htmlCtrl = WysiwygFactory.createWysiwygController(ureq, getWindowControl(), config.getVfsContainer(),
							config.getFilePath(), config.getMediaPath(), true, secCallback.isVersionControlled(), config.getEdusharingProvider());
				}
				
				htmlCtrl.setNewFile(false);
				htmlCtrl.getRichTextConfiguration().setAllowCustomMediaFactory(config.isAllowCustomMediaFactory());
				if (config.isDisableMedia()) {
					htmlCtrl.getRichTextConfiguration().disableMedia();
				}
				
				editCtrl = htmlCtrl;
			} else {
				editCtrl = new HTMLReadOnlyController(ureq, getWindowControl(), vfsLeaf.getParentContainer(), vfsLeaf.getName(), secCallback.canClose());
			}
		}
		else {
			editCtrl = new TextEditorController(ureq, getWindowControl(), vfsLeaf, "utf-8", !isEdit, secCallback.isVersionControlled());
		}
		listenTo(editCtrl);
		
		VelocityContainer mainVC = createVelocityContainer("file_editor");
		mainVC.put("editor", editCtrl.getInitialComponent());
		putInitialPanel(mainVC);
	}

	@Override
	public void event(UserRequest ureq, Component source, Event event) {
		// nothing to do here
	}

	@Override
	public void event(UserRequest ureq, Controller source, Event event) {
		if (source == editCtrl) {
			fireEvent(ureq, event);
		}
	}

	@Override
	protected void doDispose() {
		doUnlock();
		removeAsListenerAndDispose(editCtrl);
		editCtrl = null;
	}
	
	private void doUnlock() {
		if (temporaryLock) {
			vfsLockManager.unlock(vfsLeaf, VFSLockApplicationType.vfs);
		}
	}
}