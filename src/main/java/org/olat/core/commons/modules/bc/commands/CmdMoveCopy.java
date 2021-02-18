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
import java.util.List;

import org.olat.core.commons.modules.bc.FileSelection;
import org.olat.core.commons.modules.bc.FolderEvent;
import org.olat.core.commons.modules.bc.components.FolderComponent;
import org.olat.core.commons.services.notifications.NotificationsManager;
import org.olat.core.commons.services.notifications.SubscriptionContext;
import org.olat.core.commons.services.vfs.VFSRepositoryService;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.tree.MenuTree;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.DefaultController;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.folder.FolderTreeModel;
import org.olat.core.gui.translator.Translator;
import org.olat.core.util.Util;
import org.olat.core.util.vfs.VFSConstants;
import org.olat.core.util.vfs.VFSContainer;
import org.olat.core.util.vfs.VFSItem;
import org.olat.core.util.vfs.VFSLeaf;
import org.olat.core.util.vfs.VFSLockApplicationType;
import org.olat.core.util.vfs.VFSLockManager;
import org.olat.core.util.vfs.VFSManager;
import org.olat.core.util.vfs.VFSStatus;
import org.olat.core.util.vfs.callbacks.VFSSecurityCallback;
import org.olat.core.util.vfs.filters.VFSItemFilter;
import org.springframework.beans.factory.annotation.Autowired;

public class CmdMoveCopy extends DefaultController implements FolderCommand {

	private static final String VELOCITY_ROOT = Util.getPackageVelocityRoot(CmdMoveCopy.class);
	private static int status = FolderCommandStatus.STATUS_SUCCESS;

	private Translator translator;

	private MenuTree selTree;
	private FileSelection fileSelection;
	private Link selectButton, cancelButton;
	private FolderComponent folderComponent;
	private final boolean move;
	
	@Autowired
	private VFSLockManager vfsLockManager;
	@Autowired
	private VFSRepositoryService vfsRepositoryService;
	@Autowired
	private NotificationsManager notificationsManager;

	protected CmdMoveCopy(WindowControl wControl, boolean move) {
		super(wControl);
		this.move = move;
	}
	
	@Override
	public Controller execute(FolderComponent fc, UserRequest ureq, WindowControl windowControl, Translator trans) {
		this.folderComponent = fc;
		this.translator = trans;
		this.fileSelection = new FileSelection(ureq, fc.getCurrentContainerPath());
				
		VelocityContainer main = new VelocityContainer("mc", VELOCITY_ROOT + "/movecopy.html", translator, this);
		main.contextPut("fileselection", fileSelection);
		
		//check if command is executed on a file list containing invalid filenames or paths
		if(!fileSelection.getInvalidFileNames().isEmpty()) {		
			main.contextPut("invalidFileNames", fileSelection.getInvalidFileNames());
		}		

		selTree = new MenuTree(null, "seltree", this);
		FolderTreeModel ftm = new FolderTreeModel(ureq.getLocale(), fc.getRootContainer(),
				true, false, true, fc.getRootContainer().canWrite() == VFSConstants.YES, new EditableFilter());
		selTree.setTreeModel(ftm);
		selectButton = LinkFactory.createButton(move ? "move" : "copy", main, this);
		cancelButton = LinkFactory.createButton("cancel", main, this);

		main.put("seltree", selTree);
		if (move) {
			main.contextPut("move", Boolean.TRUE);
		}

		setInitialComponent(main);
		return this;
	}
	
	public boolean isMoved() {
		return move;
	}
	
	public FileSelection getFileSelection() {
		return fileSelection;
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

	public String getTarget() {
		FolderTreeModel ftm = (FolderTreeModel) selTree.getTreeModel();
		return ftm.getSelectedPath(selTree.getSelectedNode());
	}

	@Override
	public void event(UserRequest ureq, Component source, Event event) {
		if(cancelButton == source) {
			status = FolderCommandStatus.STATUS_CANCELED;
			fireEvent(ureq, FOLDERCOMMAND_FINISHED);
		} else if (selectButton == source) {
			doMove(ureq);
		}
	}
	
	private void doMove(UserRequest ureq) {
		FolderTreeModel ftm = (FolderTreeModel) selTree.getTreeModel();
		String selectedPath = ftm.getSelectedPath(selTree.getSelectedNode());
		if (selectedPath == null) {
			abortFailed(ureq, "failed");
			return;
		}
		VFSStatus vfsStatus = VFSConstants.SUCCESS;
		VFSContainer rootContainer = folderComponent.getRootContainer();
		VFSItem vfsItem = rootContainer.resolve(selectedPath);
		if (vfsItem == null || (vfsItem.canWrite() != VFSConstants.YES)) {
			abortFailed(ureq, "failed");
			return;
		}
		// copy the files
		VFSContainer target = (VFSContainer)vfsItem;
		List<VFSItem> sources = getSanityCheckedSourceItems(target, ureq);
		if (sources == null) return;
		
		for (VFSItem vfsSource:sources) {
			VFSItem targetFile = target.resolve(vfsSource.getName());
			if(vfsSource instanceof VFSLeaf && targetFile != null && targetFile.canVersion() == VFSConstants.YES) {
				//add a new version to the file
				VFSLeaf sourceLeaf = (VFSLeaf)vfsSource;
				vfsRepositoryService.addVersion(sourceLeaf, ureq.getIdentity(), "", sourceLeaf.getInputStream());
			} else {
				vfsStatus = target.copyFrom(vfsSource, ureq.getIdentity());
			}
			if (vfsStatus != VFSConstants.SUCCESS) {
				String errorKey = "failed";
				if (vfsStatus == VFSConstants.ERROR_QUOTA_EXCEEDED)
					errorKey = "QuotaExceeded";
				abortFailed(ureq, errorKey);
				return;
			}
			if (move) {
				// if move, delete the source. Note that meta source
				// has already been delete (i.e. moved)
				vfsSource.delete();
			}
		}
		
		// after a copy or a move, notify the subscribers
		VFSSecurityCallback secCallback = VFSManager.findInheritedSecurityCallback(folderComponent.getCurrentContainer());
		if (secCallback != null) {
			SubscriptionContext subsContext = secCallback.getSubscriptionContext();
			if (subsContext != null) {
				notificationsManager.markPublisherNews(subsContext, ureq.getIdentity(), true);
			}
		}
		fireEvent(ureq, new FolderEvent(move ? FolderEvent.MOVE_EVENT : FolderEvent.COPY_EVENT, fileSelection.renderAsHtml()));
		notifyFinished(ureq);
	}
	
	private void notifyFinished(UserRequest ureq) {
		VFSContainer container = VFSManager.findInheritingSecurityCallbackContainer(folderComponent.getRootContainer());
		VFSSecurityCallback secCallback = container.getLocalSecurityCallback();
		if(secCallback != null) {
			SubscriptionContext subsContext = secCallback.getSubscriptionContext();
			if (subsContext != null) {
				notificationsManager.markPublisherNews(subsContext, ureq.getIdentity(), true);
			}
		}
		fireEvent(ureq, FOLDERCOMMAND_FINISHED);
	}

	/**
	 * Get the list of source files. Sanity check if resolveable, overlapping or
	 * a target with the same name already exists. In such cases, set the error message, fire
	 * the abort event and return null.
	 * 
	 * @param target
	 * @param ureq
	 * @return
	 */
	private List<VFSItem> getSanityCheckedSourceItems(VFSContainer target, UserRequest ureq) {
		// collect all source files first
		
		List<VFSItem> sources = new ArrayList<>();
		for (String sourceRelPath:fileSelection.getFiles()) {
			VFSItem vfsSource = folderComponent.getCurrentContainer().resolve(sourceRelPath);
			if (vfsSource == null) {
				abortFailed(ureq, "FileDoesNotExist");
				return null;
			}
			if (vfsSource instanceof VFSContainer) {
				// if a folder... check if they are overlapping
				if (VFSManager.isContainerDescendantOrSelf(target, (VFSContainer)vfsSource)) {
					abortFailed(ureq, "OverlappingTarget");
					return null;
				}
			}
			if (vfsLockManager.isLockedForMe(vfsSource, ureq.getIdentity(), VFSLockApplicationType.vfs, null)) {
				abortFailed(ureq, "lock.title");
				return null;
			}

			// check for existence... this will also prevent to copy item over itself
			VFSItem item = target.resolve(vfsSource.getName());
			if (item != null) {
				abortFailed(ureq, "TargetNameAlreadyUsed");
				return null;
			}
			
			if (vfsSource.canCopy() != VFSConstants.YES) {
				getWindowControl().setError(translator.translate("FileMoveCopyFailed", new String[] {vfsSource.getName()}));
				status = FolderCommandStatus.STATUS_FAILED;
				fireEvent(ureq, FOLDERCOMMAND_FINISHED);
				return null;
			}
			sources.add(vfsSource);
		}
		return sources;
	}
	
	private void abortFailed(UserRequest ureq, String errorMessageKey) {
		getWindowControl().setError(translator.translate(errorMessageKey));
		status = FolderCommandStatus.STATUS_FAILED;
		fireEvent(ureq, FOLDERCOMMAND_FINISHED);
	}
	
	@Override
	protected void doDispose() {
		//
	}
	
	private static final class EditableFilter implements VFSItemFilter {
		
		@Override
		public boolean accept(VFSItem vfsItem) {
			VFSSecurityCallback secCallback = vfsItem.getLocalSecurityCallback();
			if(secCallback != null && !secCallback.canWrite()) {
				return false;
			}
			return true;
		}
	}
}