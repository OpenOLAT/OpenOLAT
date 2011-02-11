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
* Copyright (c) 1999-2006 at Multimedia- & E-Learning Services (MELS),<br>
* University of Zurich, Switzerland.
* <p>
*/ 

package org.olat.core.commons.modules.bc.commands;


import java.util.HashMap;
import java.util.Map;

import org.olat.core.commons.modules.bc.FolderEvent;
import org.olat.core.commons.modules.bc.components.FolderComponent;
import org.olat.core.commons.modules.bc.meta.MetaInfo;
import org.olat.core.commons.modules.bc.meta.MetaInfoFactory;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.gui.translator.Translator;
import org.olat.core.logging.AssertException;
import org.olat.core.util.FileUtils;
import org.olat.core.util.vfs.OlatRelPathImpl;
import org.olat.core.util.vfs.VFSConstants;
import org.olat.core.util.vfs.VFSContainer;
import org.olat.core.util.vfs.VFSItem;

/**
 * A panel with a FolderComponent and a CreateFolderForm. 
 * TODO: LD: check status to show if an error occured.
 * 
 * @author Lavinia Dumitrescu
 *
 */
public class CmdCreateFolder extends BasicController implements FolderCommand {

	private int status = FolderCommandStatus.STATUS_SUCCESS;
	private FolderComponent folderComponent;	
	private VelocityContainer mainVC;
	
	private CreateFolderForm createFolderForm;
	private String folderName;

private static Map<String,String> i18nkeyMap;
	
	static {
		i18nkeyMap = new HashMap<String,String>();
		i18nkeyMap.put(CreateFolderForm.TEXT_ELEM_I18N_KEY, "cf.name");
		i18nkeyMap.put(CreateFolderForm.SUBMIT_ELEM_I18N_KEY,"cf.button");
		i18nkeyMap.put(CreateFolderForm.RESET_ELEM_I18N_KEY,"cancel");
	}
	
		
	protected CmdCreateFolder(UserRequest ureq,WindowControl wControl) {
		super(ureq, wControl);		
	}

	/**
	 * Create a folder.
	 * @param cContainer
	 * @param folderName
	 * @return Status object.
	 */
	public Controller execute(FolderComponent fc, UserRequest ureq, WindowControl wControl, Translator trans) {
		this.setTranslator(trans);
		if (fc.getCurrentContainer().canWrite() != VFSConstants.YES) {
			throw new AssertException("Illegal attempt to create folder in: " + fc.getCurrentContainerPath());
		}
		this.folderComponent = fc;
				
		mainVC = this.createVelocityContainer("createFolderPanel");		
		mainVC.put("foldercomp", folderComponent);		
		
		createFolderForm = new CreateFolderForm(ureq, wControl, trans);
		this.listenTo(createFolderForm);		
		mainVC.put("createFolderForm", createFolderForm.getInitialComponent());
		this.putInitialPanel(mainVC);		
		
		return this;
	}

	public int getStatus() { 
		return status; 
	}
	
	public String getFolderName() {
		return createFolderForm.getItemName();
	}
	
	public void event(UserRequest ureq, Component source, Event event) {
		//empty
	}
	
	public void event(UserRequest ureq, Controller source, Event event) {
		if(source == createFolderForm) {
			if(event == Event.CANCELLED_EVENT){
				status = FolderCommandStatus.STATUS_CANCELED;
				fireEvent(ureq, FolderCommand.FOLDERCOMMAND_FINISHED);
			} else if (event == Event.FAILED_EVENT) {
				status = FolderCommandStatus.STATUS_FAILED;
				fireEvent(ureq, FolderCommand.FOLDERCOMMAND_FINISHED);
			} else if (event == Event.DONE_EVENT) {
        //we're done, notify listerers
				fireEvent(ureq, new FolderEvent(FolderEvent.NEW_FOLDER_EVENT, folderName));	
				fireEvent(ureq, FolderCommand.FOLDERCOMMAND_FINISHED);
			}
		}
	}

	
	protected void doDispose() {		
	}
	
	/**
	 * Form implementation.
	 * 
	 * @author Lavinia Dumitrescu
	 *
	 */
  private class CreateFolderForm extends AbstractCreateItemForm {			
				
		public CreateFolderForm(UserRequest ureq, WindowControl wControl, Translator translator) {			
			super(ureq, wControl, translator, i18nkeyMap);					
		}		
		
		@Override
		protected void formOK(UserRequest ureq) {			
      //create the folder
			VFSContainer currentContainer = folderComponent.getCurrentContainer();
			VFSItem item = currentContainer.createChildContainer(getItemName());
			if (item == null) {				
				this.fireEvent(ureq, Event.FAILED_EVENT);
				return;
			}
			if (item instanceof OlatRelPathImpl) {
				// update meta data
				MetaInfo meta = MetaInfoFactory.createMetaInfoFor((OlatRelPathImpl)item);
				meta.setAuthor(ureq.getIdentity().getName());
				meta.write();
			}		
			fireEvent(ureq, Event.DONE_EVENT);							
		}
		
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
					setItemName(name);
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

	public boolean runsModal() {
		return false;
	}

}
