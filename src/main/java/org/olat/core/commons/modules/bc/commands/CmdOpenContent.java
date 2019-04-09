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

import org.olat.core.commons.editor.htmleditor.HTMLEditorConfig;
import org.olat.core.commons.modules.bc.components.FolderComponent;
import org.olat.core.commons.modules.bc.components.ListRenderer;
import org.olat.core.commons.services.doceditor.DocEditor;
import org.olat.core.commons.services.doceditor.DocEditorConfigs;
import org.olat.core.commons.services.doceditor.DocEditorSecurityCallback;
import org.olat.core.commons.services.doceditor.DocEditorSecurityCallbackBuilder;
import org.olat.core.commons.services.doceditor.DocumentEditorService;
import org.olat.core.commons.services.doceditor.ui.DocEditorFullscreenController;
import org.olat.core.commons.services.notifications.NotificationsManager;
import org.olat.core.commons.services.notifications.SubscriptionContext;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
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
	private Controller editCtrl;
	
	@Autowired
	private DocumentEditorService docEditorService;
	
	protected CmdOpenContent(UserRequest ureq, WindowControl wControl) {
		super(ureq, wControl);
	}

	@Override
	public Controller execute(FolderComponent folderComponent, UserRequest ureq, WindowControl wControl,
			Translator translator) {
		this.folderComponent = folderComponent;
		String pos = ureq.getParameter(ListRenderer.PARAM_CONTENT_EDIT_ID);
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
		
		DocEditorSecurityCallback secCallback = getDocEditorSecCallback(vfsLeaf);
		HTMLEditorConfig htmlEditorConfig = getHtmlEditorConfig(vfsLeaf);
		DocEditorConfigs configs = DocEditorConfigs.builder()
				.addConfig(htmlEditorConfig)
				.build();
		editCtrl = new DocEditorFullscreenController(ureq, getWindowControl(), vfsLeaf, secCallback, configs);
		listenTo(editCtrl);
		
		return this;
	}
	
	private DocEditorSecurityCallback getDocEditorSecCallback(VFSLeaf vfsLeaf) {
		VFSContainer currentContainer = folderComponent.getCurrentContainer();
		boolean hasMeta = currentContainer.canMeta() == VFSConstants.YES;
		VFSContainer container = VFSManager.findInheritingSecurityCallbackContainer(currentContainer);
		boolean canWrite = container.getLocalSecurityCallback().canWrite();
		
		DocEditorSecurityCallbackBuilder secCallbackBuilder = DocEditorSecurityCallbackBuilder.builder()
				.withVersionControlled(true)
				.withHasMeta(hasMeta);
		DocEditor.Mode mode = getMode(vfsLeaf, canWrite, secCallbackBuilder);
		secCallbackBuilder.withMode(mode);
		return secCallbackBuilder.build();
	}
	
	private DocEditor.Mode getMode(VFSLeaf vfsLeaf, boolean canWrite, DocEditorSecurityCallbackBuilder secCallbackBuilder) {
		if (canWrite) {
			DocEditorSecurityCallback editSecCallback = secCallbackBuilder.withMode(DocEditor.Mode.EDIT).build();
			if (docEditorService.hasEditor(vfsLeaf, getIdentity(), editSecCallback)) {
				return DocEditor.Mode.EDIT;
			}
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
	
	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if (source == editCtrl) {
			if (event == Event.DONE_EVENT) {
				notifyFinished(ureq);
			}
			cleanUp();
			fireEvent(ureq, FOLDERCOMMAND_FINISHED);
		}
		super.event(ureq, source, event);
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
	}
	
	private void cleanUp() {
		removeAsListenerAndDispose(editCtrl);
		editCtrl = null;
	}

	@Override
	protected void doDispose() {
		//
	}

	public String getFileName() {
		return currentItem.getName();
	}

}
