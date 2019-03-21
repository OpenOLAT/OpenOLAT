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
package org.olat.portfolio.ui.artefacts.collect;

import org.olat.core.CoreSpringFactory;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FileElement;
import org.olat.core.gui.components.form.flexible.impl.Form;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.wizard.StepFormBasicController;
import org.olat.core.gui.control.generic.wizard.StepsEvent;
import org.olat.core.gui.control.generic.wizard.StepsRunContext;
import org.olat.core.util.StringHelper;
import org.olat.core.util.vfs.VFSContainer;
import org.olat.portfolio.manager.EPFrontendManager;
import org.olat.portfolio.model.artefacts.AbstractArtefact;

/**
 * Description:<br>
 * Upload an artefact-attachment/file to a temp-folder
 * 
 * <P>
 * Initial Date: 02.09.2010 <br>
 * 
 * @author Roman Haag, roman.haag@frentix.com, http://www.frentix.com
 */
public class EPCreateFileArtefactStepForm00 extends StepFormBasicController {

	private static final int MAX_UPLOADSIZE_KB = 10485;
	private FileElement fileupload;
	private VFSContainer vfsTemp;
	private AbstractArtefact artefact;
	private EPFrontendManager ePFMgr;
	
	public EPCreateFileArtefactStepForm00(UserRequest ureq, WindowControl wControl, AbstractArtefact artefact) {
		super(ureq, wControl, FormBasicController.LAYOUT_VERTICAL);
		this.artefact = artefact;		
		ePFMgr = (EPFrontendManager) CoreSpringFactory.getBean("epFrontendManager");
		this.vfsTemp = ePFMgr.getArtefactContainer(artefact);
		initForm(ureq);
	}

	public EPCreateFileArtefactStepForm00(UserRequest ureq, WindowControl wControl, Form rootForm, StepsRunContext runContext, int layout,
			String customLayoutPageName, AbstractArtefact artefact, VFSContainer vfsTemp) {
		super(ureq, wControl, rootForm, runContext, layout, customLayoutPageName);
		this.vfsTemp = vfsTemp;
		this.artefact = artefact;
		initForm(ureq);
	}

	/**
	 * @see org.olat.core.gui.control.generic.wizard.StepFormBasicController#initForm(org.olat.core.gui.components.form.flexible.FormItemContainer,
	 *      org.olat.core.gui.control.Controller, org.olat.core.gui.UserRequest)
	 */
	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		setFormDescription("step0.file.short.descr");

		fileupload = uifactory.addFileElement(getWindowControl(), "file.upload", formLayout);
		fileupload.setMandatory(true, "file.upload.no.file");
		fileupload.setMaxUploadSizeKB(MAX_UPLOADSIZE_KB, "file.upload.too.big", new String[] { String.valueOf(MAX_UPLOADSIZE_KB) });

		if (!isUsedInStepWizzard()) {
			// add form buttons
			uifactory.addFormSubmitButton("stepform.submit", formLayout);
		}
		
	}
	
	@Override
	protected boolean validateFormLogic(UserRequest ureq) {
		if (fileupload.isUploadSuccess()){
			String type = fileupload.getUploadMimeType();
			String fileName = fileupload.getUploadFileName(); 
			if (StringHelper.containsNonWhitespace(type) && StringHelper.containsNonWhitespace(fileName) && fileName.contains(".")) {
				return true;
			}
			fileupload.setErrorKey("unsupported.filetype", null);
		}
		return false;
	}

	/**
	 * @see org.olat.core.gui.control.generic.wizard.StepFormBasicController#formOK(org.olat.core.gui.UserRequest)
	 */
	@Override
	protected void formOK(UserRequest ureq) {

		// either save values to runContext or do persist them
		// directly, if form is used outside step-context
		if (isUsedInStepWizzard()) {
			// as with each further step form is validated again, do this only once!
			if (fileupload.isUploadSuccess() && getFromRunContext("tempArtFolder") == null) {
				saveUpload();
			}
			addToRunContext("artefact", artefact);
			addToRunContext("tempArtFolder", vfsTemp);
			fireEvent(ureq, StepsEvent.ACTIVATE_NEXT);
		} else if (fileupload.isUploadSuccess()){
			saveUpload();
			fireEvent(ureq, Event.DONE_EVENT);
		}
	}
	
	private void saveUpload(){
		//VFSLeaf contFile = vfsTemp.createChildLeaf(fileupload.getUploadFileName());
		//VFSManager.copyContent(fileupload.getUploadInputStream(), contFile);
		fileupload.moveUploadFileTo(vfsTemp);
	}

	/**
	 * @see org.olat.core.gui.control.generic.wizard.StepFormBasicController#doDispose()
	 */
	@Override
	protected void doDispose() {
		// nothing, temp-file is cleaned within calling controller!
	}
}
