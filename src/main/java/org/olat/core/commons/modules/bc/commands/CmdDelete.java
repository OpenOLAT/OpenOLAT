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

package org.olat.core.commons.modules.bc.commands;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.olat.core.CoreSpringFactory;
import org.olat.core.commons.modules.bc.FileSelection;
import org.olat.core.commons.modules.bc.FolderEvent;
import org.olat.core.commons.modules.bc.components.FolderComponent;
import org.olat.core.commons.services.vfs.VFSVersionModule;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.gui.control.generic.modal.DialogBoxController;
import org.olat.core.gui.control.generic.modal.DialogBoxUIFactory;
import org.olat.core.gui.translator.Translator;
import org.olat.core.util.vfs.VFSConstants;
import org.olat.core.util.vfs.VFSContainer;
import org.olat.core.util.vfs.VFSItem;
import org.olat.core.util.vfs.VFSLockApplicationType;
import org.olat.core.util.vfs.VFSLockManager;

public class CmdDelete extends BasicController implements FolderCommand {

	private static int status = FolderCommandStatus.STATUS_SUCCESS;

	private Translator translator;
	private FolderComponent folderComponent;
	private FileSelection fileSelection;

	private DialogBoxController dialogCtr;
	private DialogBoxController lockedFiledCtr;
	
	private final boolean versionsEnabled;
	private final VFSLockManager lockManager;
	
	protected CmdDelete(UserRequest ureq, WindowControl wControl) {
		super(ureq, wControl);
		versionsEnabled = CoreSpringFactory.getImpl(VFSVersionModule.class).isEnabled();
		lockManager = CoreSpringFactory.getImpl(VFSLockManager.class);
	}

	@Override
	public Controller execute(FolderComponent fc, UserRequest ureq, WindowControl wContr, Translator trans) {
		this.translator = trans;
		this.folderComponent = fc;
		this.fileSelection = new FileSelection(ureq, fc.getCurrentContainerPath());

		VFSContainer currentContainer = folderComponent.getCurrentContainer();
		List<String> lockedFiles = hasLockedFiles(currentContainer, fileSelection);
		if (lockedFiles.isEmpty()) {
			String msg = trans.translate("del.confirm") + "<p>" + fileSelection.renderAsHtml() + "</p>";		
			// create dialog controller
			dialogCtr = activateYesNoDialog(ureq, trans.translate("del.header"), msg, dialogCtr);
		} else {
			String msg = FolderCommandHelper.renderLockedMessageAsHtml(trans, lockedFiles);
			List<String> buttonLabels = Collections.singletonList(trans.translate("ok"));
			lockedFiledCtr = activateGenericDialog(ureq, trans.translate("lock.title"), msg, buttonLabels, lockedFiledCtr);
		}
		return this;
	}
	
	public List<String> hasLockedFiles(VFSContainer container, FileSelection selection) {
		List<String> lockedFiles = new ArrayList<>();
		for (String file : selection.getFiles()) {
			VFSItem item = container.resolve(file);
			if (lockManager.isLockedForMe(item, getIdentity(), VFSLockApplicationType.vfs, null)) {
				lockedFiles.add(file);
			}
		}
		return lockedFiles;
	}

	@Override
	public int getStatus() {
		return status;
	}

	@Override
	public boolean runsModal() {
		// this controller has its own modal dialog box
		return true;
	}
	
	@Override
	public String getModalTitle() {
		return null;
	}

	public FileSelection getFileSelection() {
		return fileSelection;
	}

	@Override
	public void event(UserRequest ureq, Controller source, Event event) {
		if (source == dialogCtr) {
			if (DialogBoxUIFactory.isYesEvent(event)) {				
				// do delete
				VFSContainer currentContainer = folderComponent.getCurrentContainer();
				List<String> files = fileSelection.getFiles();
				if (files.isEmpty()) {
					// sometimes, browser sends empty form data...
					getWindowControl().setError(translator.translate("failed"));
					status = FolderCommandStatus.STATUS_FAILED;
					fireEvent(ureq, FOLDERCOMMAND_FINISHED);
				}
				for (String file : files) {
					VFSItem item = currentContainer.resolve(file);
					if (item != null && (item.canDelete() == VFSConstants.YES)) {
						if (versionsEnabled && item.canVersion() == VFSConstants.YES) {
							// Move to pub
							item.delete();
						} else {
							item.deleteSilently();
						}
					} else {
						getWindowControl().setWarning(translator.translate("del.partial"));
					}
				}
				
				String confirmationText = fileSelection.renderAsHtml();
				fireEvent(ureq, new FolderEvent(FolderEvent.DELETE_EVENT, confirmationText));
				fireEvent(ureq, FOLDERCOMMAND_FINISHED);
			} else {
				// abort
				status = FolderCommandStatus.STATUS_CANCELED;
				fireEvent(ureq, FOLDERCOMMAND_FINISHED);
			}
		}

	}
	
	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		// no events to catch
	}

	@Override
	protected void doDispose() {
		// autodisposed by basic controller
	}
}
