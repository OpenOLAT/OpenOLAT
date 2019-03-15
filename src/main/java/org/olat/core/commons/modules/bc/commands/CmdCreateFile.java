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

import org.olat.core.commons.editor.htmleditor.HTMLEditorController;
import org.olat.core.commons.editor.htmleditor.WysiwygFactory;
import org.olat.core.commons.editor.plaintexteditor.PlainTextEditorController;
import org.olat.core.commons.modules.bc.FolderEvent;
import org.olat.core.commons.modules.bc.FolderLicenseHandler;
import org.olat.core.commons.modules.bc.components.FolderComponent;
import org.olat.core.commons.services.license.License;
import org.olat.core.commons.services.license.LicenseModule;
import org.olat.core.commons.services.license.LicenseService;
import org.olat.core.commons.services.license.ui.LicenseUIFactory;
import org.olat.core.commons.services.notifications.NotificationsManager;
import org.olat.core.commons.services.notifications.SubscriptionContext;
import org.olat.core.commons.services.vfs.VFSMetadata;
import org.olat.core.commons.services.vfs.VFSRepositoryService;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.translator.Translator;
import org.olat.core.logging.AssertException;
import org.olat.core.util.FileUtils;
import org.olat.core.util.WebappHelper;
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
 * Description:
 * A panel with a FolderComponent and a CreateFileForm.
 * 
 * Initial Date:  13.12.2005
 * @author Florian Gnägi
 */
public class CmdCreateFile extends FormBasicController implements FolderCommand {

	private int status = FolderCommandStatus.STATUS_SUCCESS;
	private String fileName;
	private String target;
	
	private Controller editorCtr;
	private TextElement textElement;
	private FolderComponent folderComponent;
	
	@Autowired
	private LicenseService licenseService;
	@Autowired
	private LicenseModule licenseModule;
	@Autowired
	private FolderLicenseHandler licenseHandler;
	@Autowired
	private VFSRepositoryService vfsRepositoryService;

	protected CmdCreateFile(UserRequest ureq, WindowControl wControl) {
		super(ureq, wControl);
	}

	@Override
	public Controller execute(FolderComponent folderComponent, UserRequest ureq, WindowControl wControl, Translator translator) {
		if (folderComponent.getCurrentContainer().canWrite() != VFSConstants.YES) {
			throw new AssertException("Illegal attempt to create file in: " + folderComponent.getCurrentContainerPath());
		}		
		setTranslator(translator);
		this.folderComponent = folderComponent;

		//check for quota
		long quotaLeft = VFSManager.getQuotaLeftKB(folderComponent.getCurrentContainer());
		if (quotaLeft <= 0 && quotaLeft != -1 ) {
			String supportAddr = WebappHelper.getMailConfig("mailQuota");
			String msg = translate("QuotaExceededSupport", new String[] { supportAddr });
			getWindowControl().setError(msg);
			return null;
		}
		target = folderComponent.getRootContainer().getName() + folderComponent.getCurrentContainerPath();
		target = target.replace("/", " / ");
		initForm(ureq);
		return this;
	}
	
	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		formLayout.setElementCssClass("o_sel_folder_new_file");
		uifactory.addStaticTextElement("cf.createin", target, formLayout);
		
		textElement = uifactory.addTextElement("fileName", "cfile.name", -1, "", formLayout);
		textElement.setExampleKey("cfile.name.example", null);
		textElement.setDisplaySize(20);
		textElement.setMandatory(true);
		textElement.setElementCssClass("o_sel_folder_new_file_name");
		
		FormLayoutContainer formButtons = FormLayoutContainer.createButtonLayout("formButton", getTranslator());
		formLayout.add(formButtons);
		uifactory.addFormSubmitButton("submit", "cfile.create", formButtons);
		uifactory.addFormCancelButton("cancel", formButtons, ureq, getWindowControl());		
	}
	
	@Override
	protected void doDispose() {
				
	}
	
	public void event(UserRequest ureq, Controller source, Event event) {
		if (source == editorCtr) {
			if (event == Event.DONE_EVENT) {
				// we're done, notify listerers
				fireEvent(ureq, new FolderEvent(FolderEvent.NEW_FILE_EVENT, fileName));	
				notifyFinished(ureq);
			} else if(event == Event.CANCELLED_EVENT){
				fireEvent(ureq, FOLDERCOMMAND_FINISHED);
			}
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

	public String getFileName() {
		return fileName;
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
		return translate("cfile.header");
	}

	@Override
	protected void formCancelled(UserRequest ureq) {
		fireEvent(ureq, FOLDERCOMMAND_FINISHED);
	}

	@Override
	protected void formOK(UserRequest ureq) {			
		//create the file
		fileName = textElement.getValue();
		VFSContainer currentContainer = folderComponent.getCurrentContainer();
		VFSItem item = currentContainer.createChildLeaf(fileName);

		if(item == null) {
			status = FolderCommandStatus.STATUS_FAILED;
			notifyFinished(ureq);
		} else {
			if(item.canMeta() == VFSConstants.YES) {
				VFSMetadata meta = item.getMetaInfo();
				meta.setAuthor(ureq.getIdentity());
				if (licenseModule.isEnabled(licenseHandler)) {
					License license = licenseService.createDefaultLicense(licenseHandler, getIdentity());
					meta.setLicenseType(license.getLicenseType());
					meta.setLicenseTypeName(license.getLicenseType().getName());
					meta.setLicensor(license.getLicensor());
					meta.setLicenseText(LicenseUIFactory.getLicenseText(license));
				}
				vfsRepositoryService.updateMetadata(meta);
			}

			// start HTML editor with the folders root folder as base and the file
			// path as a relative path from the root directory. But first check if the 
			// root directory is wirtable at all (e.g. not the case in users personal 
			// briefcase), and seach for the next higher directory that is writable.
			String relFilePath = "/" + fileName;
			// add current container path if not at root level
			if (!folderComponent.getCurrentContainerPath().equals("/")) { 
				relFilePath = folderComponent.getCurrentContainerPath() + relFilePath;
			}
			VFSContainer writableRootContainer = folderComponent.getRootContainer();
			ContainerAndFile result = VFSManager.findWritableRootFolderFor(writableRootContainer, relFilePath);
			if (result != null) {
				writableRootContainer = result.getContainer();
				relFilePath = result.getFileName();
			} else {
				// use fallback that always work: current directory and current file
				relFilePath = fileName;
				writableRootContainer = folderComponent.getCurrentContainer(); 
			}
			if (relFilePath.endsWith(".html") || relFilePath.endsWith(".htm")) {
				editorCtr = WysiwygFactory.createWysiwygController(ureq, getWindowControl(), writableRootContainer, relFilePath, true, true);				
				((HTMLEditorController)editorCtr).setNewFile(true);
			} else {
				editorCtr = new PlainTextEditorController(ureq, getWindowControl(), (VFSLeaf)writableRootContainer.resolve(relFilePath), "utf-8", true, true, null);
			}

			listenTo(editorCtr);
			initialPanel.setContent(editorCtr.getInitialComponent());
		}
	}

	@Override
	protected boolean validateFormLogic(UserRequest ureq) {
		boolean isInputValid = true;
		String fileName = textElement.getValue();
		if(fileName==null || fileName.trim().equals("")) {
			textElement.setErrorKey("cfile.name.empty", new String[0]);
			isInputValid = false;
		} else {
			fileName = fileName.toLowerCase();
			// check if there are any unwanted path denominators in the name
			if (!validateFileName(fileName)) {
				textElement.setErrorKey("cfile.name.notvalid", new String[0]);
				isInputValid = false;
				return isInputValid;
			} else if (!fileName.endsWith(".html") && !fileName.endsWith(".htm") && !fileName.endsWith(".txt") && !fileName.endsWith(".css")) {
				//add html extension if missing
				fileName = fileName + ".html";
			}
			//ok, file name is sanitized, let's see if a file with this name already exists
			VFSContainer currentContainer = folderComponent.getCurrentContainer();
			VFSItem item = currentContainer.resolve(fileName);
			if (item != null) {
				textElement.setErrorKey("cfile.already.exists", new String[] {fileName});
				isInputValid = false;
			} else {
				isInputValid = true;
				textElement.setValue(fileName);
			}
		}
		return isInputValid;			
	}
	
	private boolean validateFileName(String name) {		
		return FileUtils.validateFilename(name);
	}
}