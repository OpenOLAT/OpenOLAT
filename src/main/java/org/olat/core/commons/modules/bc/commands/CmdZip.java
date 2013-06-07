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
import org.olat.core.commons.modules.bc.meta.MetaInfo;
import org.olat.core.commons.modules.bc.meta.tagged.MetaTagged;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.form.flexible.impl.elements.FormReset;
import org.olat.core.gui.components.form.flexible.impl.elements.FormSubmit;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.gui.translator.Translator;
import org.olat.core.logging.AssertException;
import org.olat.core.util.FileUtils;
import org.olat.core.util.ZipUtil;
import org.olat.core.util.vfs.VFSConstants;
import org.olat.core.util.vfs.VFSContainer;
import org.olat.core.util.vfs.VFSItem;
import org.olat.core.util.vfs.VFSLeaf;

/**
 * 
 * Description:<br>
 * Provides a CreateItemForm and creates a zip file if input valid.
 * TODO: LD: check status to show if an error occured.
 * 
 * <P>
 * Initial Date:  30.01.2008 <br>
 * @author Lavinia Dumitrescu
 */
public class CmdZip extends BasicController implements FolderCommand {
	
	private int status = FolderCommandStatus.STATUS_SUCCESS;	
	
	private VelocityContainer mainVC;
	private CreateItemForm createItemForm;
	private VFSContainer currentContainer;
	private FileSelection selection;
	 
	
	protected CmdZip(UserRequest ureq, WindowControl wControl) {
		super(ureq, wControl);
	}
	
	public Controller execute(FolderComponent folderComponent, UserRequest ureq, WindowControl wControl, Translator trans) {
		this.setTranslator(trans);
		currentContainer = folderComponent.getCurrentContainer();
		if (currentContainer.canWrite() != VFSConstants.YES) {
			throw new AssertException("Cannot write to current folder.");
		}
		
		status = FolderCommandHelper.sanityCheck(wControl, folderComponent);
		if(status == FolderCommandStatus.STATUS_FAILED) {
			return null;
		}
	
		selection = new FileSelection(ureq, folderComponent.getCurrentContainerPath());
		status = FolderCommandHelper.sanityCheck3(wControl, folderComponent, selection);
		if(status == FolderCommandStatus.STATUS_FAILED) {
			return null;
		}
		
		mainVC = createVelocityContainer("createZipPanel");
		mainVC.contextPut("fileselection", selection);
		
		createItemForm = new CreateItemForm(ureq, wControl, trans);
		listenTo(createItemForm);		
		mainVC.put("createItemForm", createItemForm.getInitialComponent());
		putInitialPanel(mainVC);
		return this;
	}

	public int getStatus() {
		return status;
	}

	/**
	 * @see org.olat.core.gui.control.DefaultController#event(org.olat.core.gui.UserRequest, org.olat.core.gui.components.Component, org.olat.core.gui.control.Event)
	 */
	@Override
	public void event(UserRequest ureq, Component source, Event event) {
		//empty
	}
	
	public void event(UserRequest ureq, Controller source, Event event) {
		if(source == createItemForm) {
			if(event == Event.CANCELLED_EVENT){
				status = FolderCommandStatus.STATUS_CANCELED;
				fireEvent(ureq, FolderCommand.FOLDERCOMMAND_FINISHED);
			} else if (event == Event.FAILED_EVENT) {
        //abort
				status = FolderCommandStatus.STATUS_FAILED;
				fireEvent(ureq, FOLDERCOMMAND_FINISHED);
			} else if (event == Event.DONE_EVENT) {
        //we're done, notify listerers				
				fireEvent(ureq, new FolderEvent(FolderEvent.ZIP_EVENT, selection.renderAsHtml()));				
				fireEvent(ureq, FolderCommand.FOLDERCOMMAND_FINISHED);
			}
		}
	}

	/**
	 * @see org.olat.core.gui.control.DefaultController#doDispose(boolean)
	 */
	protected void doDispose() {
		// nothing to do
	}
	
	/**
	 * 
	 * Description:<br>
	 * Implementation of AbstractCreateItemForm.
	 * 
	 * <P>
	 * Initial Date:  30.01.2008 <br>
	 * @author Lavinia Dumitrescu
	 */
	private class CreateItemForm extends AbstractCreateItemForm {
		
		public CreateItemForm(UserRequest ureq, WindowControl wControl, Translator translator) {
			super(ureq, wControl, translator);
		}
		
		@Override
		protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {				
			
			FormLayoutContainer horizontalLayout = FormLayoutContainer.createHorizontalFormLayout("itemLayout", getTranslator());
			formLayout.add(horizontalLayout);
			textElement = uifactory.addTextElement("fileName", "zip.name", 20, "", horizontalLayout);
			textElement.setMandatory(true);			
			uifactory.addStaticTextElement("extension", null, translate("zip.extension"), horizontalLayout);
			
			FormLayoutContainer formButtons = FormLayoutContainer.createHorizontalFormLayout("formButton", getTranslator());
			formLayout.add(formButtons);
			createFile = new FormSubmit("submit","zip.button");
			formButtons.add(createFile);
			reset = new FormReset("reset","cancel");
			formButtons.add(reset);			
		}	

		/**
		 * Creates a zipFile by using ZipUtil and fires Event.DONE_EVENT if successful.
		 * 
		 * @see org.olat.core.commons.modules.bc.commands.AbstractCreateItemForm#formOK(org.olat.core.gui.UserRequest)
		 */
		protected void formOK(UserRequest ureq) {		
			VFSItem zipFile = currentContainer.createChildLeaf(getItemName());
			if (zipFile == null) {
				this.fireEvent(ureq, Event.FAILED_EVENT);
				return;				
			}
			
			List<VFSItem> vfsFiles = new ArrayList<VFSItem>();
			for (String fileName : selection.getFiles()) {
				VFSItem item = currentContainer.resolve(fileName);
				if (item != null)	vfsFiles.add(item);
			}
			if (!ZipUtil.zip(vfsFiles, (VFSLeaf)zipFile, true)) {
				// cleanup zip file
				zipFile.delete();				
				this.fireEvent(ureq, Event.FAILED_EVENT);
			} else {
				if(zipFile instanceof MetaTagged) {
					MetaInfo info = ((MetaTagged)zipFile).getMetaInfo();
					if(info != null) {
						info.setAuthor(ureq.getIdentity());
						info.write();
					}
				}
				
				fireEvent(ureq, Event.DONE_EVENT);								
			}
		}
		
		/**
		 * Checks if input valid.
		 * @see org.olat.core.commons.modules.bc.commands.AbstractCreateItemForm#validateFormLogic(org.olat.core.gui.UserRequest)
		 */
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
					setItemName(name);
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

	public boolean runsModal() {
		return false;
	}

}
