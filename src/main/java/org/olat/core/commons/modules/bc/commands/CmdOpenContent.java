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
package org.olat.core.commons.modules.bc.commands;

import java.util.List;

import org.olat.core.CoreSpringFactory;
import org.olat.core.commons.editor.htmleditor.HTMLEditorConfig;
import org.olat.core.commons.modules.bc.components.FolderComponent;
import org.olat.core.commons.modules.bc.components.ListRenderer;
import org.olat.core.commons.services.doceditor.DocEditor;
import org.olat.core.commons.services.doceditor.DocEditor.Mode;
import org.olat.core.commons.services.doceditor.DocEditorConfigs;
import org.olat.core.commons.services.doceditor.DocEditorService;
import org.olat.core.commons.services.notifications.NotificationsManager;
import org.olat.core.commons.services.notifications.SubscriptionContext;
import org.olat.core.commons.services.vfs.VFSRepositoryService;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.gui.control.winmgr.CommandFactory;
import org.olat.core.gui.translator.Translator;
import org.olat.core.util.StringHelper;
import org.olat.core.util.vfs.VFSConstants;
import org.olat.core.util.vfs.VFSContainer;
import org.olat.core.util.vfs.VFSItem;
import org.olat.core.util.vfs.VFSLeaf;
import org.olat.core.util.vfs.VFSManager;
import org.olat.core.util.vfs.callbacks.VFSSecurityCallback;
import org.olat.core.util.vfs.util.ContainerAndFile;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 18 Mar 2019<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class CmdOpenContent extends BasicController implements FolderCommand {

	private VFSItem currentItem;
	private int status = FolderCommandStatus.STATUS_SUCCESS;
	
	private FolderComponent folderComponent;
	
	@Autowired
	private DocEditorService docEditorService;
	
	protected CmdOpenContent(UserRequest ureq, WindowControl wControl) {
		super(ureq, wControl);
	}

	@Override
	public Controller execute(FolderComponent folderCmp, UserRequest ureq, WindowControl wControl,
			Translator translator) {
		this.folderComponent = folderCmp;
		String pos = ureq.getParameter(ListRenderer.PARAM_CONTENT_EDIT_ID);
		if (!StringHelper.containsNonWhitespace(pos)) {
			// somehow parameter did not make it to us
			status = FolderCommandStatus.STATUS_FAILED;
			getWindowControl().setError(translator.translate("failed"));
			return null;
		}
		
		status = FolderCommandHelper.sanityCheck(wControl, folderComponent);
		if(status == FolderCommandStatus.STATUS_SUCCESS) {
			int index = Integer.parseInt(pos);
			List<VFSItem> children = folderComponent.getCurrentContainerChildren();
			if(index >= 0 && index < children.size()) {
				currentItem = folderComponent.getCurrentContainerChildren().get(index);
				status = FolderCommandHelper.sanityCheck2(wControl, folderComponent, currentItem);
			} else {
				status = FolderCommandStatus.STATUS_FAILED;
				getWindowControl().setError(translator.translate("failed"));
				return null;
			}	
		}
		if(status == FolderCommandStatus.STATUS_FAILED) {
			return null;
		}
		
		status = FolderCommandHelper.fileEditSanityCheck(currentItem);
		if (status == FolderCommandStatus.STATUS_FAILED) {
			// this should no longer happen, since folderComponent -> ListRenderer does not display edit-link for folders
			logWarn("Given VFSItem is not a file, can't edit it: "+folderComponent.getCurrentContainerPath() + "/" + currentItem.getName(),null);
			getWindowControl().setError(translator.translate("FileEditFailed"));
			return null;
		}
		
		if (!(currentItem instanceof VFSLeaf)) {
			status = FolderCommandStatus.STATUS_FAILED;
			getWindowControl().setError(translator.translate("failed"));
			return null;
		}
		
		VFSLeaf vfsLeaf = (VFSLeaf) currentItem;
		boolean metaAvailable = vfsLeaf.canMeta() == VFSConstants.YES;
		if (metaAvailable) {
			CoreSpringFactory.getImpl(VFSRepositoryService.class).increaseDownloadCount(vfsLeaf);
		}
		
		Mode mode = getMode(ureq, vfsLeaf, metaAvailable);
		HTMLEditorConfig htmlEditorConfig = getHtmlEditorConfig(vfsLeaf);
		DocEditorConfigs configs = DocEditorConfigs.builder()
				.withMode(mode)
				.withVersionControlled(true)
				.addConfig(htmlEditorConfig)
				.build(vfsLeaf);
		
		String url = docEditorService.prepareDocumentUrl(ureq.getUserSession(), configs);
		getWindowControl().getWindowBackOffice().sendCommandTo(CommandFactory.createNewWindowRedirectTo(url));

		if (DocEditor.Mode.EDIT == mode) {
			markNews(folderCmp.getRootContainer());
		}
		
		return null;
	}
	
	private DocEditor.Mode getMode(UserRequest ureq, VFSLeaf vfsLeaf, boolean metaAvailable) {
		VFSContainer currentContainer = folderComponent.getCurrentContainer();
		VFSContainer container = VFSManager.findInheritingSecurityCallbackContainer(currentContainer);
		boolean canWrite = container.getLocalSecurityCallback().canWrite();
		if (canWrite && docEditorService.hasEditor(getIdentity(), ureq.getUserSession().getRoles(), vfsLeaf, DocEditor.Mode.EDIT, metaAvailable)) {
			return DocEditor.Mode.EDIT;
		}
		return DocEditor.Mode.VIEW;
	}
	
	private HTMLEditorConfig getHtmlEditorConfig(VFSLeaf vfsLeaf) {
		// start HTML editor with the folders root folder as base and the file
		// path as a relative path from the root directory. But first check if the 
		// root directory is wirtable at all (e.g. not the case in users personal 
		// briefcase), and seach for the next higher directory that is writable.
		String relFilePath = "/" + vfsLeaf.getName();
		// add current container path if not at root level
		if (!folderComponent.getCurrentContainerPath().equals("/")) { 
			relFilePath = folderComponent.getCurrentContainerPath() + relFilePath;
		}
		VFSContainer writableRootContainer = folderComponent.getRootContainer();
		ContainerAndFile result = VFSManager.findWritableRootFolderFor(writableRootContainer, relFilePath);
		if (result != null) {
			if(vfsLeaf.getParentContainer() != null) {
				writableRootContainer = vfsLeaf.getParentContainer();
				relFilePath = vfsLeaf.getName();
			} else {
				writableRootContainer = result.getContainer();
			}
		} else {
			// use fallback that always work: current directory and current file
			relFilePath = vfsLeaf.getName();
			writableRootContainer = folderComponent.getCurrentContainer(); 
		}
		return HTMLEditorConfig.builder(writableRootContainer, relFilePath)
				.withCustomLinkTreeModel(folderComponent.getCustomLinkTreeModel())
				.build();
	}
	
	private void markNews(VFSItem rootContainer) {
		VFSContainer container = VFSManager.findInheritingSecurityCallbackContainer(rootContainer);
		VFSSecurityCallback secCallback = container.getLocalSecurityCallback();
		if(secCallback != null) {
			SubscriptionContext subsContext = secCallback.getSubscriptionContext();
			if (subsContext != null) {
				NotificationsManager notificationsManager = CoreSpringFactory.getImpl(NotificationsManager.class);
				notificationsManager.markPublisherNews(subsContext, getIdentity(), true);
			}
		}
	}

	@Override
	public int getStatus() {
		return status;
	}

	@Override
	public boolean runsModal() {
		return true;
	}

	@Override
	public String getModalTitle() {
		return null;
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		//
	}

	public String getFileName() {
		return currentItem.getName();
	}

}
