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


import org.olat.core.commons.modules.bc.FolderEvent;
import org.olat.core.commons.modules.bc.components.FolderComponent;
import org.olat.core.commons.services.vfs.VFSMetadata;
import org.olat.core.commons.services.vfs.VFSRepositoryService;
import org.olat.core.commons.services.vfs.model.VFSMetadataImpl;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.translator.Translator;
import org.olat.core.logging.AssertException;
import org.olat.core.util.FileUtils;
import org.olat.core.util.vfs.VFSConstants;
import org.olat.core.util.vfs.VFSContainer;
import org.olat.core.util.vfs.VFSItem;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * A panel with a FolderComponent and a CreateFolderForm.
 * 
 * @author Lavinia Dumitrescu
 *
 */
public class CmdCreateFolder extends FormBasicController implements FolderCommand {

	private int status = FolderCommandStatus.STATUS_SUCCESS;
	private FolderComponent folderComponent;	

	private String folderName;
	private String target;
	private TextElement textElement;
	
	@Autowired
	private VFSRepositoryService vfsRepositoryService;
  
	public CmdCreateFolder(UserRequest ureq, WindowControl wControl) {			
		super(ureq, wControl);
	}

	/**
	 * Create a folder.
	 * @param cContainer
	 * @param folderName
	 * @return Status object.
	 */
	@Override
	public Controller execute(FolderComponent fc, UserRequest ureq, WindowControl wControl, Translator trans) {
		this.setTranslator(trans);
		if (fc.getCurrentContainer().canWrite() != VFSConstants.YES) {
			throw new AssertException("Illegal attempt to create folder in: " + fc.getCurrentContainerPath());
		}
		this.folderComponent = fc;
		target = folderComponent.getRootContainer().getName() + folderComponent.getCurrentContainerPath();
		target = target.replace("/", " / ");
		setTranslator(trans);
		initForm(ureq);
		return this;
	}
	
	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		formLayout.setElementCssClass("o_sel_folder_new_folder");
		uifactory.addStaticTextElement("cf.createin", target, formLayout);

		textElement = uifactory.addTextElement("fileName", "cf.name", -1, "", formLayout);
		textElement.setDisplaySize(20);
		textElement.setMandatory(true);
		textElement.setElementCssClass("o_sel_folder_new_folder_name");
		
		FormLayoutContainer formButtons = FormLayoutContainer.createButtonLayout("formButton", getTranslator());
		formLayout.add(formButtons);
		uifactory.addFormSubmitButton("submit", "cf.button", formButtons);
		uifactory.addFormCancelButton("cancel", formButtons, ureq, getWindowControl());		
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
		return translate("cf.header");
	}

	public String getFolderName() {
		return textElement.getValue();
	}

	@Override
	protected void formOK(UserRequest ureq) {			
		//create the folder
		String name = textElement.getValue();	
		VFSContainer currentContainer = folderComponent.getCurrentContainer();
		VFSItem item = currentContainer.createChildContainer(name);
		if (item instanceof VFSContainer && item.canMeta() == VFSConstants.YES) {
			// update meta data
			VFSMetadata meta = item.getMetaInfo();
			if (meta instanceof VFSMetadataImpl) {
				((VFSMetadataImpl)meta).setFileInitializedBy(ureq.getIdentity());
				vfsRepositoryService.updateMetadata(meta);
			}
			status = FolderCommandStatus.STATUS_SUCCESS;
			
			fireEvent(ureq, new FolderEvent(FolderEvent.NEW_FOLDER_EVENT, folderName));	
			fireEvent(ureq, FolderCommand.FOLDERCOMMAND_FINISHED);
		}	else {
			status = FolderCommandStatus.STATUS_FAILED;
			fireEvent(ureq, FolderCommand.FOLDERCOMMAND_FINISHED);
		}
	}

	@Override
	protected void formCancelled(UserRequest ureq) {
		status = FolderCommandStatus.STATUS_CANCELED;
		fireEvent(ureq, FolderCommand.FOLDERCOMMAND_FINISHED);
	}

	@Override
	protected boolean validateFormLogic(UserRequest ureq) {
		boolean isInputValid = true;
		String name = textElement.getValue();		
		if(name==null || name.trim().equals("")) {
			textElement.setErrorKey("cf.empty", new String[0]);
			isInputValid = false;
		} else {				
			if (!validateFolderName(name)) {
				textElement.setErrorKey("cf.name.notvalid", new String[0]);
				isInputValid = false;
				return isInputValid;
			} 
      //ok, folder name is sanitized, let's see if a folder with this name already exists
			VFSContainer currentContainer = folderComponent.getCurrentContainer();				
			VFSItem item = currentContainer.resolve(name);
			if (item != null) {
				textElement.setErrorKey("cf.exists", new String[] {name});
				isInputValid = false;
			} else {
				isInputValid = true;
			}
		}
		return isInputValid;			
	}
		
	/**
	 * Checks if the input name is a valid folder name.
	 * @return true if valid
	 */
	private boolean validateFolderName(String name) {
		return FileUtils.validateFilename(name);
	}	
}