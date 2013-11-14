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

import java.util.HashMap;
import java.util.Map;

import org.olat.core.CoreSpringFactory;
import org.olat.core.commons.editor.htmleditor.HTMLEditorController;
import org.olat.core.commons.editor.htmleditor.WysiwygFactory;
import org.olat.core.commons.editor.plaintexteditor.PlainTextEditorController;
import org.olat.core.commons.modules.bc.FolderEvent;
import org.olat.core.commons.modules.bc.components.FolderComponent;
import org.olat.core.commons.modules.bc.meta.MetaInfo;
import org.olat.core.commons.modules.bc.meta.MetaInfoFactory;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.panel.Panel;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.gui.translator.Translator;
import org.olat.core.logging.AssertException;
import org.olat.core.util.FileUtils;
import org.olat.core.util.WebappHelper;
import org.olat.core.util.vfs.OlatRelPathImpl;
import org.olat.core.util.vfs.VFSConstants;
import org.olat.core.util.vfs.VFSContainer;
import org.olat.core.util.vfs.VFSItem;
import org.olat.core.util.vfs.VFSLeaf;
import org.olat.core.util.vfs.VFSManager;
import org.olat.core.util.vfs.util.ContainerAndFile;

/**
* Initial Date:  13.12.2005
*
* @author Florian Gnägi
*
* Description:
* A panel with a FolderComponent and a CreateFileForm.
* TODO: LD: check status to show if an error occurred.
*/
public class CmdCreateFile extends BasicController implements FolderCommand {

	private int status = FolderCommandStatus.STATUS_SUCCESS;
	private FolderComponent folderComponent;
	private VelocityContainer mainVC;
	private Panel mainPanel;
	
	private CreateFileForm createFileForm;
	private Controller editorCtr;
	private String fileName;
	
	private static Map<String,String> i18nkeyMap;
	
	static {
		i18nkeyMap = new HashMap<String,String>();
		i18nkeyMap.put(CreateFileForm.TEXT_ELEM_I18N_KEY, "cfile.name");
		i18nkeyMap.put(CreateFileForm.SUBMIT_ELEM_I18N_KEY,"cfile.create");
		i18nkeyMap.put(CreateFileForm.RESET_ELEM_I18N_KEY,"cancel");
	}
	
	
	protected CmdCreateFile(UserRequest ureq, WindowControl wControl) {
		super(ureq, wControl);		
	}

	public Controller execute(FolderComponent folderComponent, UserRequest ureq, WindowControl wControl, Translator translator) {
		this.setTranslator(translator);
		if (folderComponent.getCurrentContainer().canWrite() != VFSConstants.YES) {
			throw new AssertException("Illegal attempt to create file in: " + folderComponent.getCurrentContainerPath());
		}		
		
		mainVC = createVelocityContainer("createFilePanel");		
		mainPanel = putInitialPanel(mainVC);
		
		this.folderComponent = folderComponent;
		mainVC.put("foldercomp", folderComponent);
		
		createFileForm = new CreateFileForm(ureq, wControl, translator);
		listenTo(createFileForm);		
		mainVC.put("createFileForm", createFileForm.getInitialComponent());
		
		//check for quota
		long quotaLeft = VFSManager.getQuotaLeftKB(folderComponent.getCurrentContainer());
		if (quotaLeft <= 0 && quotaLeft != -1 ) {
			String supportAddr = WebappHelper.getMailConfig("mailQuota");
			String msg = translate("QuotaExceededSupport", new String[] { supportAddr });
			this.getWindowControl().setError(msg);
			return null;
		}
						
		return this;
	}

	
	@Override
	protected void doDispose() {
				
	}

	@Override
	public void event(UserRequest ureq, Component source, Event event) {
		//empty
	}
	
	
	public void event(UserRequest ureq, Controller source, Event event) {
		if (source == editorCtr) {
			if (event == Event.DONE_EVENT) {
				// we're done, notify listerers
				fireEvent(ureq, new FolderEvent(FolderEvent.NEW_FILE_EVENT, fileName));	
				fireEvent(ureq, FolderCommand.FOLDERCOMMAND_FINISHED);
			} else if(event == Event.CANCELLED_EVENT){
				fireEvent(ureq, FolderCommand.FOLDERCOMMAND_FINISHED);
			}
		} else if(source == createFileForm) {
			if(event == Event.CANCELLED_EVENT){
				fireEvent(ureq, FolderCommand.FOLDERCOMMAND_FINISHED);
			} else if (event == Event.FAILED_EVENT) {				
				status = FolderCommandStatus.STATUS_FAILED;
				fireEvent(ureq, FolderCommand.FOLDERCOMMAND_FINISHED);
			}
			else if (event == Event.DONE_EVENT) {
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
				}
				else {
					editorCtr = new PlainTextEditorController(ureq, getWindowControl(), (VFSLeaf)writableRootContainer.resolve(relFilePath), "utf-8", true, true, null);
				}

				listenTo(editorCtr);
				
				mainPanel.setContent(editorCtr.getInitialComponent());
			}
		}
	}

	public String getFileName() {
		return fileName;
	}

	public int getStatus() { 
		return status; 
	}
		
	/**
	 * 
	 * Description:<br>
	 * CreateFileForm implementation.
	 * 
	 * <P>
	 * Initial Date:  28.01.2008 <br>
	 * @author Lavinia Dumitrescu
	 */
	private class CreateFileForm extends AbstractCreateItemForm {			
		
		private final MetaInfoFactory metaInfoFactory;
				
		public CreateFileForm(UserRequest ureq, WindowControl wControl, Translator translator) {			
			super(ureq, wControl, translator, i18nkeyMap);	
			textElement.setExampleKey("cfile.name.example", null);
			metaInfoFactory = CoreSpringFactory.getImpl(MetaInfoFactory.class);
		}		
						
		@Override
		protected void formOK(UserRequest ureq) {			
	    //create the file
			VFSContainer currentContainer = folderComponent.getCurrentContainer();
			VFSItem item = currentContainer.createChildLeaf(getItemName());
			if (item == null) {				
				fireEvent(ureq, Event.FAILED_EVENT);
				return;
			}
			if (item instanceof OlatRelPathImpl) {
				// update meta data
				MetaInfo meta = metaInfoFactory.createMetaInfoFor((OlatRelPathImpl)item);
				meta.setAuthor(ureq.getIdentity());
				meta.write();
			}	
			fileName = getItemName();			
			fireEvent(ureq, Event.DONE_EVENT);  
		}
		
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
					setItemName(fileName);
				}
			}
			return isInputValid;			
		}
		
		private boolean validateFileName(String name) {		
			return FileUtils.validateFilename(name);
		}			
	}
	public boolean runsModal() {
		return false;
	}

}
