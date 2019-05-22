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

import java.util.List;

import org.olat.core.commons.editor.htmleditor.HTMLEditorConfig;
import org.olat.core.commons.modules.bc.FolderEvent;
import org.olat.core.commons.modules.bc.components.FolderComponent;
import org.olat.core.commons.services.doceditor.DocEditor;
import org.olat.core.commons.services.doceditor.DocEditorConfigs;
import org.olat.core.commons.services.doceditor.DocEditorSecurityCallback;
import org.olat.core.commons.services.doceditor.DocEditorSecurityCallbackBuilder;
import org.olat.core.commons.services.doceditor.DocTemplates;
import org.olat.core.commons.services.doceditor.DocumentEditorService;
import org.olat.core.commons.services.doceditor.ui.CreateDocumentController;
import org.olat.core.commons.services.doceditor.ui.DocEditorFullscreenController;
import org.olat.core.commons.services.notifications.NotificationsManager;
import org.olat.core.commons.services.notifications.SubscriptionContext;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.gui.control.generic.closablewrapper.CloseableModalController;
import org.olat.core.gui.translator.Translator;
import org.olat.core.id.Identity;
import org.olat.core.logging.AssertException;
import org.olat.core.util.FileUtils;
import org.olat.core.util.WebappHelper;
import org.olat.core.util.resource.OresHelper;
import org.olat.core.util.vfs.VFSConstants;
import org.olat.core.util.vfs.VFSContainer;
import org.olat.core.util.vfs.VFSLeaf;
import org.olat.core.util.vfs.VFSManager;
import org.olat.core.util.vfs.callbacks.VFSSecurityCallback;
import org.olat.core.util.vfs.util.ContainerAndFile;
import org.springframework.beans.factory.annotation.Autowired;

/**
 *
 * Initial Date:  13.12.2005
 * @author Florian Gnägi
 * @author Urs Hensler
 */
public class CmdCreateFile extends BasicController implements FolderCommand {

	private int status = FolderCommandStatus.STATUS_SUCCESS;
	private String fileName;
	
	private CloseableModalController cmc;
	private CreateDocumentController createCtrl;
	private Controller editorCtr;
	private FolderComponent folderComponent;
	
	private VFSLeaf vfsLeaf;
	
	@Autowired
	private DocumentEditorService docEditorService;

	protected CmdCreateFile(UserRequest ureq, WindowControl wControl) {
		super(ureq, wControl);
	}

	@Override
	public Controller execute(FolderComponent folderCmp, UserRequest ureq, WindowControl wControl, Translator translator) {
		if (folderCmp.getCurrentContainer().canWrite() != VFSConstants.YES) {
			throw new AssertException("Illegal attempt to create file in: " + folderCmp.getCurrentContainerPath());
		}		
		setTranslator(translator);
		this.folderComponent = folderCmp;

		//check for quota
		long quotaLeft = VFSManager.getQuotaLeftKB(folderCmp.getCurrentContainer());
		if (quotaLeft <= 0 && quotaLeft != -1 ) {
			String supportAddr = WebappHelper.getMailConfig("mailQuota");
			String msg = translate("QuotaExceededSupport", new String[] { supportAddr });
			getWindowControl().setError(msg);
			return null;
		}
		
		boolean hasMeta = folderCmp.getCurrentContainer().canMeta() == VFSConstants.YES;
		Identity identity = getIdentity();
		DocTemplates docTemplates = DocTemplates.editables(identity, ureq.getUserSession().getRoles(), getLocale(), hasMeta).build();
		createCtrl = new CreateDocumentController(ureq, wControl, folderCmp.getCurrentContainer(), docTemplates);
		listenTo(createCtrl);
		
		cmc = new CloseableModalController(getWindowControl(), translate("close"), createCtrl.getInitialComponent(),
				true, translate("cfile.header"));
		cmc.activate();
		listenTo(cmc);
		return this;
	}
	
	@Override
	protected void doDispose() {
		//
	}
	
	@Override
	public void event(UserRequest ureq, Controller source, Event event) {
		if (source == createCtrl) {
			cmc.deactivate();
			if (event == Event.DONE_EVENT) {
				vfsLeaf = createCtrl.getCreatedLeaf();
				if (vfsLeaf == null) {
					status = FolderCommandStatus.STATUS_FAILED;
				} else {
					fileName = vfsLeaf.getName();
					doEdit(ureq);
				}
			} else {
				fireEvent(ureq, FOLDERCOMMAND_FINISHED);
			}
		} else if (source == editorCtr) {
			fireEvent(ureq, new FolderEvent(FolderEvent.NEW_FILE_EVENT, fileName));
			fireEvent(ureq, FOLDERCOMMAND_FINISHED);
			notifyFinished(ureq);
			cleanUp();
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
	}
	
	private void doEdit(UserRequest ureq) {
		VFSContainer currentContainer = folderComponent.getCurrentContainer();
		boolean hasMeta = currentContainer.canMeta() == VFSConstants.YES;
		
		String suffix = FileUtils.getFileSuffix(vfsLeaf.getName());
		List<DocEditor> editors = docEditorService.getEditors(getIdentity(), ureq.getUserSession().getRoles(), suffix,
				DocEditor.Mode.EDIT, hasMeta);
		// Not able to decide which editor to use -> show the folder list
		if (editors.size() != 1) {
			fireEvent(ureq, new FolderEvent(FolderEvent.NEW_FILE_EVENT, fileName));
			fireEvent(ureq, FOLDERCOMMAND_FINISHED);
			return;
		}
		
		DocEditorSecurityCallback secCallback = DocEditorSecurityCallbackBuilder.builder()
				.withMode(DocEditor.Mode.EDIT)
				.withHasMeta(hasMeta)
				.withVersionControlled(true)
				.build();
		HTMLEditorConfig htmlEditorConfig = getHtmlEditorConfig();
		DocEditorConfigs configs = DocEditorConfigs.builder()
				.addConfig(htmlEditorConfig)
				.build();
		WindowControl swb = addToHistory(ureq, OresHelper.createOLATResourceableType("DocEditor"), null);
		editorCtr = new DocEditorFullscreenController(ureq, swb, vfsLeaf, secCallback, configs);
		listenTo(editorCtr);
	}

	private HTMLEditorConfig getHtmlEditorConfig() {
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
	
	private void cleanUp() {
		removeAsListenerAndDispose(createCtrl);
		removeAsListenerAndDispose(editorCtr);
		createCtrl = null;
		editorCtr = null;
	}

	public String getFileName() {
		return fileName;
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
}