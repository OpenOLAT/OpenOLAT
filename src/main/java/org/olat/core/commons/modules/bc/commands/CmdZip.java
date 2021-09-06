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
import org.olat.core.util.ZipUtil;
import org.olat.core.util.vfs.VFSConstants;
import org.olat.core.util.vfs.VFSContainer;
import org.olat.core.util.vfs.VFSItem;
import org.olat.core.util.vfs.VFSLeaf;
import org.olat.core.util.vfs.filters.VFSSystemItemFilter;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Description:<br>
 * Provides a CreateItemForm and creates a zip file if input valid.
 * 
 * <P>
 * Initial Date:  30.01.2008 <br>
 * @author Lavinia Dumitrescu
 */
public class CmdZip extends FormBasicController implements FolderCommand {
	
	private int status = FolderCommandStatus.STATUS_SUCCESS;	

	private VFSContainer currentContainer;
	private FileSelection selection;
	private TextElement textElement;
	
	@Autowired
	private VFSRepositoryService vfsRepositoryService;
	
	protected CmdZip(UserRequest ureq, WindowControl wControl) {
		super(ureq, wControl);
	}
	
	@Override
	public Controller execute(FolderComponent folderComponent, UserRequest ureq, WindowControl wControl, Translator trans) {
		setTranslator(trans);
		currentContainer = folderComponent.getCurrentContainer();
		if (currentContainer.canWrite() != VFSConstants.YES) {
			throw new AssertException("Cannot write to current folder.");
		}
		
		status = FolderCommandHelper.sanityCheck(wControl, folderComponent);
		if(status == FolderCommandStatus.STATUS_FAILED) {
			return null;
		}
	
		selection = new FileSelection(ureq, folderComponent.getCurrentContainer(), folderComponent.getCurrentContainerPath());
		status = FolderCommandHelper.sanityCheck3(wControl, folderComponent, selection);
		if(status == FolderCommandStatus.STATUS_FAILED) {
			return null;
		}
		
		if(selection.getFiles().isEmpty()) {
			status = FolderCommandStatus.STATUS_FAILED;
			wControl.setWarning(trans.translate("warning.file.selection.empty"));
			return null;
		}
		
		initForm(ureq);
		return this;
	}
	
	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		String files = selection.renderAsHtml();
		uifactory.addStaticExampleText("zip.confirm", files, formLayout);
		
		textElement = uifactory.addTextElement("fileName", "zip.name", 20, "", formLayout);
		textElement.setMandatory(true);			
		uifactory.addStaticTextElement("extension", null, translate("zip.extension"), formLayout);
		
		FormLayoutContainer formButtons = FormLayoutContainer.createButtonLayout("formButton", getTranslator());
		formLayout.add(formButtons);
		uifactory.addFormSubmitButton("submit","zip.button", formButtons);
		uifactory.addFormCancelButton("cancel", formButtons, ureq, getWindowControl());	
	}

	@Override
	protected void doDispose() {
		// nothing to do
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
		return translate("zip.header");
	}

	@Override
	protected void formCancelled(UserRequest ureq) {
		status = FolderCommandStatus.STATUS_CANCELED;
		fireEvent(ureq, FolderCommand.FOLDERCOMMAND_FINISHED);
	}

	/**
	 * Creates a zipFile by using ZipUtil and fires Event.DONE_EVENT if successful.
	 */
	@Override
	protected void formOK(UserRequest ureq) {
		String name = textElement.getValue();
		if(!name.toLowerCase().endsWith(".zip")) {
			name += ".zip";
		}

		VFSLeaf zipFile = currentContainer.createChildLeaf(name);
		if (zipFile == null) {
			fireEvent(ureq, Event.FAILED_EVENT);
			return;				
		}
		
		List<VFSItem> vfsFiles = new ArrayList<>();
		for (String fileName : selection.getFiles()) {
			VFSItem item = currentContainer.resolve(fileName);
			if (item != null) {
				vfsFiles.add(item);
			}
		}
		if (!ZipUtil.zip(vfsFiles, zipFile, new VFSSystemItemFilter(), false)) {
			zipFile.delete();				
			status = FolderCommandStatus.STATUS_FAILED;
			fireEvent(ureq, FOLDERCOMMAND_FINISHED);
		} else {
			vfsRepositoryService.itemSaved(zipFile, ureq.getIdentity());
			
			fireEvent(ureq, new FolderEvent(FolderEvent.ZIP_EVENT, selection.renderAsHtml()));				
			fireEvent(ureq, FolderCommand.FOLDERCOMMAND_FINISHED);								
		}
	}
		
	/**
	 * Checks if input valid.
	 * @see org.olat.core.commons.modules.bc.commands.AbstractCreateItemForm#validateFormLogic(org.olat.core.gui.UserRequest)
	 */
	@Override
	protected boolean validateFormLogic(UserRequest ureq) {
		boolean isInputValid = true;
		String name = textElement.getValue();		
		if(name==null || name.trim().equals("")) {
			textElement.setErrorKey("zip.name.empty", new String[0]);
			isInputValid = false;
		} else {				
			if (!validateFileName(name)) {
				textElement.setErrorKey("zip.name.notvalid", new String[0]);
				isInputValid = false;
				return isInputValid;
			} 
    //Note: use java.io.File and not VFS to create a leaf. File must not exist upon ZipUtil.zip()
			name = name + ".zip";
			VFSItem zipFile = currentContainer.resolve(name);
			if (zipFile != null) {					
				textElement.setErrorKey("zip.alreadyexists", new String[] {name});
				isInputValid = false;
			} else {
				isInputValid = true;
			}
		}			
		return isInputValid;			
	}

	/**
	 * Checks if filename contains any prohibited chars.
	 * @param name
	 * @return true if file name valid.
	 */
	private boolean validateFileName(String name) {
		return FileUtils.validateFilename(name);
	}
}