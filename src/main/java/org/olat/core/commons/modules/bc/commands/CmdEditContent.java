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

import java.util.Collections;
import java.util.List;

import org.olat.core.commons.controllers.linkchooser.CustomLinkTreeModel;
import org.olat.core.commons.editor.htmleditor.HTMLEditorController;
import org.olat.core.commons.editor.htmleditor.WysiwygFactory;
import org.olat.core.commons.editor.plaintexteditor.PlainTextEditorController;
import org.olat.core.commons.modules.bc.components.FolderComponent;
import org.olat.core.commons.modules.bc.components.ListRenderer;
import org.olat.core.commons.modules.bc.version.VersionCommentController;
import org.olat.core.commons.services.notifications.NotificationsManager;
import org.olat.core.commons.services.notifications.SubscriptionContext;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.gui.control.generic.closablewrapper.CloseableModalController;
import org.olat.core.gui.control.generic.modal.DialogBoxController;
import org.olat.core.gui.translator.Translator;
import org.olat.core.util.StringHelper;
import org.olat.core.util.vfs.VFSContainer;
import org.olat.core.util.vfs.VFSItem;
import org.olat.core.util.vfs.VFSLeaf;
import org.olat.core.util.vfs.VFSLockApplicationType;
import org.olat.core.util.vfs.VFSLockManager;
import org.olat.core.util.vfs.VFSManager;
import org.olat.core.util.vfs.callbacks.VFSSecurityCallback;
import org.olat.core.util.vfs.util.ContainerAndFile;
import org.springframework.beans.factory.annotation.Autowired;

public class CmdEditContent extends BasicController implements FolderCommand {

	private int status = FolderCommandStatus.STATUS_SUCCESS;
	private VFSItem currentItem;
	private FolderComponent folderComponent;
	private Controller editorc;
	private DialogBoxController lockedFiledCtr;

	private VersionCommentController unlockCtr;
	private CloseableModalController unlockDialogBox;
	
	@Autowired
	private VFSLockManager vfsLockManager;
	
	protected CmdEditContent(UserRequest ureq, WindowControl wControl) {
		super(ureq, wControl);
	}

	@Override
	public Controller execute(FolderComponent folderComponent, UserRequest ureq, WindowControl wControl, Translator translator) {
		this.folderComponent = folderComponent;
		String pos = ureq.getParameter(ListRenderer.PARAM_CONTENTEDITID);
		if (!StringHelper.containsNonWhitespace(pos)) {
			// somehow parameter did not make it to us
			status = FolderCommandStatus.STATUS_FAILED;
			getWindowControl().setError(translator.translate("failed"));
			return null;
		}
		
		status = FolderCommandHelper.sanityCheck(wControl, folderComponent);
		if(status == FolderCommandStatus.STATUS_SUCCESS) {
			currentItem = folderComponent.getCurrentContainerChildren().get(Integer.parseInt(pos));
			status = FolderCommandHelper.sanityCheck2(wControl, folderComponent, currentItem);
		}
		if(status == FolderCommandStatus.STATUS_FAILED) {
			return null;
		}
		
		// do fileEditSanityCheck
		// OO-57
		status = FolderCommandHelper.fileEditSanityCheck(currentItem);
		if (status == FolderCommandStatus.STATUS_FAILED) {
			// this should no longer happen, since folderComponent -> ListRenderer does not display edit-link for folders
			logWarn("Given VFSItem is not a file, can't edit it: "+folderComponent.getCurrentContainerPath() + "/" + currentItem.getName(),null);
			getWindowControl().setError(translator.translate("FileEditFailed"));
			return null;
		}
		
		if(vfsLockManager.isLockedForMe(currentItem, ureq.getIdentity(), ureq.getUserSession().getRoles(), VFSLockApplicationType.vfs)) {
			List<String> lockedFiles = Collections.singletonList(currentItem.getName());
			String msg = FolderCommandHelper.renderLockedMessageAsHtml(translator, lockedFiles);
			List<String> buttonLabels = Collections.singletonList(translator.translate("ok"));
			lockedFiledCtr = activateGenericDialog(ureq, translator.translate("lock.title"), msg, buttonLabels, lockedFiledCtr);
			return null;
		}
		

		// start HTML editor with the folders root folder as base and the file
		// path as a relative path from the root directory. But first check if the 
		// root directory is wirtable at all (e.g. not the case in users personal 
		// briefcase), and seach for the next higher directory that is writable.
		String relFilePath = "/" + currentItem.getName();
		// add current container path if not at root level
		if (!folderComponent.getCurrentContainerPath().equals("/")) { 
			relFilePath = folderComponent.getCurrentContainerPath() + relFilePath;
		}
		VFSContainer writableRootContainer = folderComponent.getRootContainer();
		ContainerAndFile result = VFSManager.findWritableRootFolderFor(writableRootContainer, relFilePath);
		if (result != null) {
			if(currentItem.getParentContainer() != null) {
				writableRootContainer = currentItem.getParentContainer();
				relFilePath = currentItem.getName();
			} else {
				writableRootContainer = result.getContainer();
			}
		} else {
			// use fallback that always work: current directory and current file
			relFilePath = currentItem.getName();
			writableRootContainer = folderComponent.getCurrentContainer(); 
		}
		// launch plaintext or html editor depending on file type
		if (relFilePath.endsWith(".html") || relFilePath.endsWith(".htm")) {
			CustomLinkTreeModel customLinkTreeModel = folderComponent.getCustomLinkTreeModel(); 
			if (customLinkTreeModel != null) {
				editorc = WysiwygFactory.createWysiwygControllerWithInternalLink(ureq, getWindowControl(), writableRootContainer, relFilePath, true, customLinkTreeModel);
				((HTMLEditorController)editorc).setNewFile(false);				
			} else {				
				editorc = WysiwygFactory.createWysiwygController(ureq, getWindowControl(), writableRootContainer, relFilePath, true, true);
				((HTMLEditorController)editorc).setNewFile(false);				
			}
		}
		else {
			editorc = new PlainTextEditorController(ureq, getWindowControl(), (VFSLeaf)currentItem, "utf-8", true, false, null);
		}
		listenTo(editorc);
		putInitialPanel(editorc.getInitialComponent());
		return this;
	}

	@Override
	public int getStatus() {
		return status;
	}

	@Override
	public boolean runsModal() {
		return false;
	}
	
	@Override
	public String getModalTitle() {
		return null;
	}

	public String getFileName() {
		return currentItem.getName();
	}

	@Override
	public void event(UserRequest ureq, Component source, Event event) {
		// nothing to do here
	}

	@Override
	public void event(UserRequest ureq, Controller source, Event event) {
		if (source == editorc) {
			if (event == Event.DONE_EVENT) {
				boolean lock = vfsLockManager.isLocked(currentItem, VFSLockApplicationType.vfs);
				if(lock) {
					unlockCtr = new VersionCommentController(ureq,getWindowControl(), true, false);
					listenTo(unlockCtr);
					unlockDialogBox = new CloseableModalController(getWindowControl(), translate("ok"), unlockCtr.getInitialComponent());
					unlockDialogBox.activate();
				} else {
					notifyFinished(ureq);
				}
				// cleanup editor
				removeAsListenerAndDispose(editorc);
				editorc = null;
			}
			else if(event == Event.CANCELLED_EVENT) {
				fireEvent(ureq, FOLDERCOMMAND_FINISHED);
				// cleanup editor
				removeAsListenerAndDispose(editorc);
				editorc = null;
			}
		} else if (source == lockedFiledCtr) {
			fireEvent(ureq, FOLDERCOMMAND_FINISHED);
		} else if (source == unlockCtr) {
			if(!unlockCtr.keepLocked()) {
				vfsLockManager.unlock(currentItem, getIdentity(), ureq.getUserSession().getRoles(), VFSLockApplicationType.vfs);
			}
			cleanUpUnlockDialog();
			fireEvent(ureq, FOLDERCOMMAND_FINISHED);
		}
	}
	
	private void notifyFinished(UserRequest ureq) {
		VFSContainer container = VFSManager.findInheritingSecurityCallbackContainer(folderComponent.getRootContainer());
		VFSSecurityCallback secCallback = container.getLocalSecurityCallback();
		if(secCallback != null) {
			SubscriptionContext subsContext = secCallback.getSubscriptionContext();
			if (subsContext != null) {
				NotificationsManager.getInstance().markPublisherNews(subsContext, ureq.getIdentity(), true);
			}
		}
		fireEvent(ureq, FOLDERCOMMAND_FINISHED);
	}
	
	private void cleanUpUnlockDialog() {
		if(unlockDialogBox != null) {
			unlockDialogBox.deactivate();
			removeAsListenerAndDispose(unlockCtr);
			unlockDialogBox = null;
			unlockCtr = null;
		}
	}
	
	/**
	 * @see org.olat.core.gui.control.DefaultController#doDispose(boolean)
	 */
	protected void doDispose() {
		// auto dispose by basic controller
	}
}