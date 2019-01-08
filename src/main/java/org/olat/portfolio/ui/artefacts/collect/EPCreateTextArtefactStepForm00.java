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

import java.io.ByteArrayInputStream;

import org.olat.core.CoreSpringFactory;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.RichTextElement;
import org.olat.core.gui.components.form.flexible.impl.Form;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.wizard.StepFormBasicController;
import org.olat.core.gui.control.generic.wizard.StepsEvent;
import org.olat.core.gui.control.generic.wizard.StepsRunContext;
import org.olat.core.gui.translator.Translator;
import org.olat.core.util.Util;
import org.olat.core.util.vfs.VFSContainer;
import org.olat.core.util.vfs.VFSItem;
import org.olat.core.util.vfs.VFSLeaf;
import org.olat.core.util.vfs.VFSManager;
import org.olat.portfolio.manager.EPArtefactManager;
import org.olat.portfolio.manager.EPFrontendManager;
import org.olat.portfolio.model.artefacts.AbstractArtefact;
import org.olat.portfolio.ui.artefacts.view.EPArtefactViewController;

/**
 * Description:<br>
 * first step for wizzard, when creating a text-artefact
 * can also be used as a separate form to edit an artefact 
 * 
 * <P>
 * Initial Date: 01.09.2010 <br>
 * 
 * @author Roman Haag, roman.haag@frentix.com, http://www.frentix.com
 */
public class EPCreateTextArtefactStepForm00 extends StepFormBasicController {

	private AbstractArtefact artefact;
	private RichTextElement content;
	private VFSContainer vfsTemp;
	private EPFrontendManager ePFMgr;
	private String artFulltextContent;

	// use this constructor to edit an already existing artefact
	public EPCreateTextArtefactStepForm00(UserRequest ureq, WindowControl wControl, AbstractArtefact artefact){
		super(ureq, wControl, FormBasicController.LAYOUT_VERTICAL);
		Translator pt = Util.createPackageTranslator(EPArtefactViewController.class, ureq.getLocale(), getTranslator());
		flc.setTranslator(pt);
		this.artefact = artefact;
		ePFMgr = (EPFrontendManager) CoreSpringFactory.getBean("epFrontendManager");
		this.artFulltextContent = ePFMgr.getArtefactFullTextContent(artefact);
		this.vfsTemp = ePFMgr.getArtefactContainer(artefact);
		initForm(flc, this, ureq);		
	}
	
	public EPCreateTextArtefactStepForm00(UserRequest ureq, WindowControl wControl, Form rootForm, StepsRunContext runContext, int layout,
			String customLayoutPageName, AbstractArtefact artefact, VFSContainer vfsTemp) {
		super(ureq, wControl, rootForm, runContext, layout, customLayoutPageName);
		// set fallback translator to re-use given strings
		Translator pt = Util.createPackageTranslator(EPArtefactViewController.class, ureq.getLocale(), getTranslator());
		flc.setTranslator(pt);
		this.artefact = artefact;
		this.artFulltextContent = artefact.getFulltextContent(); // during collection the fulltextcontent is not persisted and therefore might be longer than db-length restriction
		this.vfsTemp = vfsTemp;
		initForm(flc, this, ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {

		VFSItem contFile = vfsTemp.resolve(EPArtefactManager.ARTEFACT_CONTENT_FILENAME);
		if (contFile == null) {
			vfsTemp.createChildLeaf(EPArtefactManager.ARTEFACT_CONTENT_FILENAME);
		}
		content = uifactory.addRichTextElementForFileData("content", "artefact.content", artFulltextContent, 15, -1, vfsTemp,
				EPArtefactManager.ARTEFACT_CONTENT_FILENAME, null, formLayout, ureq.getUserSession(), getWindowControl());
		content.getEditorConfiguration().setFileBrowserUploadRelPath("media");
		content.setMandatory(true);
		content.setNotEmptyCheck("artefact.content.not.empty");

		if (!isUsedInStepWizzard()) {
			// add form buttons
			uifactory.addFormSubmitButton("stepform.submit", formLayout);
		}

	}

	@Override
	protected void formOK(UserRequest ureq) {

		// either save values to runContext or do persist them
		// directly, if form is used outside step-context
		if (isUsedInStepWizzard()) {
			// save fulltext to temp-file
			String fulltext = content.getValue();
			VFSLeaf contFile = (VFSLeaf) vfsTemp.resolve(EPArtefactManager.ARTEFACT_CONTENT_FILENAME);
			VFSManager.copyContent(new ByteArrayInputStream(fulltext.getBytes()), contFile);

			addToRunContext("artefact", artefact);
			addToRunContext("tempArtFolder", vfsTemp);
			fireEvent(ureq, StepsEvent.ACTIVATE_NEXT);
		} else {
			String fulltext = content.getValue();
			artefact.setFulltextContent(fulltext);
			ePFMgr.updateArtefact(artefact);
			
			// the content-file is not needed in this case!! remove it.
			VFSLeaf contFile = (VFSLeaf) vfsTemp.resolve(EPArtefactManager.ARTEFACT_CONTENT_FILENAME);
			if (contFile != null) contFile.delete();
			fireEvent(ureq, Event.DONE_EVENT);
		}
	}

	@Override
	protected void doDispose() {
		// nothing, temp-file is cleaned within calling controller!
	}

}
