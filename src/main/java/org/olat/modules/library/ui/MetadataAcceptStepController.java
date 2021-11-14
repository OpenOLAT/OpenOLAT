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
package org.olat.modules.library.ui;

import org.olat.core.commons.modules.bc.meta.MetaInfoFormController;
import org.olat.core.commons.services.vfs.VFSMetadata;
import org.olat.core.commons.services.vfs.VFSRepositoryService;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.impl.Form;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.gui.control.generic.wizard.StepFormController;
import org.olat.core.gui.control.generic.wizard.StepsEvent;
import org.olat.core.gui.control.generic.wizard.StepsRunContext;
import org.olat.core.util.Util;
import org.olat.core.util.vfs.LocalFileImpl;
import org.olat.core.util.vfs.VFSContainer;
import org.olat.core.util.vfs.VFSManager;
import org.olat.modules.library.LibraryManager;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Description:<br>
 * Review the meta-datas of the document submitted to the library
 * <P>
 * Events fired:
 * <ul>
 * 	<li>ACTIVATE_NEXT</li>
 * </ul>
 * 
 * Initial Date:  5 oct. 2009 <br>
 *
 * @author twuersch, srosse
 */
public class MetadataAcceptStepController extends BasicController implements StepFormController {
	
	public static final String STEPS_RUN_CONTEXT_METADATA_KEY = "metadata";
	public static final String STEPS_RUN_CONTEXT_FILENAME_KEY = "filename";
	public static final String STEPS_RUN_CONTEXT_NEW_FILENAME_KEY = "new_filename";
	
	private StepsRunContext stepsRunContext;
	private MetaInfoFormController metaInfoFormController;
	private MetadataAcceptFilenameController filenameController;
	private FormLayoutContainer mainC;
	
	@Autowired
	private LibraryManager libraryManager;
	@Autowired
	private VFSRepositoryService vfsRepositoryService;
	
	public MetadataAcceptStepController(UserRequest ureq, WindowControl wControl, StepsRunContext stepsRunContext, Form rootForm) {
		super(ureq, wControl);
		this.stepsRunContext = stepsRunContext;
		
		VFSContainer uploadFolder = libraryManager.getUploadFolder();
		LocalFileImpl uploadFile = VFSManager.olatRootLeaf("/" + uploadFolder.getName() + "/" + (String)stepsRunContext.get(STEPS_RUN_CONTEXT_FILENAME_KEY));
		metaInfoFormController = new MetaInfoFormController(ureq, wControl, rootForm, uploadFile);
		
		VFSMetadata metaInfo = vfsRepositoryService.getMetadataFor(uploadFile);
		metaInfoFormController.getMetaInfo(metaInfo);
		listenTo(metaInfoFormController);
		
		filenameController = new MetadataAcceptFilenameController(ureq, wControl, rootForm, uploadFile);
		listenTo(filenameController);
		
		String page = Util.getPackageVelocityRoot(this.getClass()) + "/metainfos_step.html";
		mainC = FormLayoutContainer.createCustomFormLayout("infoContainer", getTranslator(), page);
		mainC.setRootForm(rootForm);
		mainC.add("filename", filenameController.getFormItem());
		mainC.add("metainfos", metaInfoFormController.getFormItem());
		putInitialPanel(mainC.getComponent());
	}
	
	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		// Nothing to do.
	}

	@Override
	public void back() {
		//
	}

	public FormItem getStepFormItem() {
		return mainC;
	}
	
	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if (source == metaInfoFormController || source == filenameController) {
			if (event == Event.DONE_EVENT) {
				stepsRunContext.put(STEPS_RUN_CONTEXT_NEW_FILENAME_KEY, filenameController.getNewFilename());
				stepsRunContext.put(STEPS_RUN_CONTEXT_METADATA_KEY, metaInfoFormController.getMetaInfo());
				fireEvent(ureq, StepsEvent.ACTIVATE_NEXT);
			}
		}
	}
}
