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
*/

package org.olat.ims.qti.editor;

import java.io.File;
import java.util.Locale;

import org.olat.core.commons.modules.bc.FolderConfig;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.control.ControllerEventListener;
import org.olat.core.gui.control.DefaultController;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.translator.PackageTranslator;
import org.olat.core.gui.translator.Translator;
import org.olat.core.logging.Tracing;
import org.olat.core.util.CodeHelper;
import org.olat.core.util.Util;
import org.olat.fileresource.FileResourceManager;
import org.olat.fileresource.types.AddingResourceException;
import org.olat.fileresource.types.FileResource;
import org.olat.ims.qti.fileresource.SurveyFileResource;
import org.olat.ims.qti.fileresource.TestFileResource;
import org.olat.ims.qti.process.AssessmentInstance;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryManager;
import org.olat.repository.controllers.IAddController;
import org.olat.repository.controllers.RepositoryAddCallback;

/**
 * Initial Date:  14.01.2005
 *
 * @author Mike Stock
 */
public class AddNewQTIDocumentController extends DefaultController implements IAddController, ControllerEventListener {

	private static final String PACKAGE_REPOSITORY = Util.getPackageName(RepositoryManager.class);
	private static final String DUMMY_TITLE = "__DUMMYTITLE__";
	private String type;
	private FileResource resource;
	private Locale locale;
	private Translator translator;
	QTIEditorPackage tmpPackage;
	
	/**
	 * @param type
	 * @param addCallback
	 * @param ureq
	 * @param wControl
	 */
	public AddNewQTIDocumentController(String type, RepositoryAddCallback addCallback, UserRequest ureq, WindowControl wControl) {
		super(wControl);		
		this.type = type;
		this.translator = new PackageTranslator(PACKAGE_REPOSITORY, ureq.getLocale());
		this.locale = ureq.getLocale();
		if (type.equals(AssessmentInstance.QMD_ENTRY_TYPE_ASSESS)) {
			resource = new TestFileResource();
		} else if (type.equals(AssessmentInstance.QMD_ENTRY_TYPE_SURVEY)) {
			resource = new SurveyFileResource();
		}
		if (addCallback != null) {
			addCallback.setResourceable(resource);
			addCallback.setDisplayName(translator.translate(resource.getResourceableTypeName()));
			addCallback.setResourceName("-");
			addCallback.finished(ureq);
		}
	}
	
	/**
	 * @see org.olat.repository.controllers.IAddController#getTransactionComponent()
	 */
	public Component getTransactionComponent() {
		return null;
	}

	/**
	 * @see org.olat.repository.controllers.IAddController#transactionFinishBeforeCreate()
	 */
	public boolean transactionFinishBeforeCreate() {
		File fTempQTI = new File(FolderConfig.getCanonicalTmpDir() + "/" + CodeHelper.getGlobalForeverUniqueID() + ".zip");
		tmpPackage = new QTIEditorPackage(DUMMY_TITLE, type, locale);
		// we need to save the package in order to be able to create a file resource entry.
		// package will be created again after changing title.
		if (!tmpPackage.savePackageTo(fTempQTI)) return false;
		try {
			return (FileResourceManager.getInstance().addFileResource(fTempQTI, "qti.zip", resource) != null);
		} catch (AddingResourceException e) {
			Tracing.logWarn("Error while adding new qti.zip resource", e, AddNewQTIDocumentController.class);
			return false;
		}
	}

	/**
	 * @see org.olat.repository.controllers.IAddController#repositoryEntryCreated(org.olat.repository.RepositoryEntry)
	 */
	public void repositoryEntryCreated(RepositoryEntry re) {
		// change title
		tmpPackage.getQTIDocument().getAssessment().setTitle(re.getDisplayname());
		// re-save package into the repository, entry has not been defalted yet, so
		// saving the entry is all it needs to update.
		File fRepositoryQTI = new File(FileResourceManager.getInstance().getFileResourceRoot(re.getOlatResource()), "qti.zip");
		fRepositoryQTI.delete();
		tmpPackage.savePackageTo(fRepositoryQTI);
		// cleanup temp files
		tmpPackage.cleanupTmpPackageDir();
	}

	/**
	 * @see org.olat.repository.controllers.IAddController#transactionAborted()
	 */
	public void transactionAborted() {
		// Nothing to do here... no file has been created yet.
	}

	/**
	 * @see org.olat.core.gui.control.DefaultController#event(org.olat.core.gui.UserRequest, org.olat.core.gui.components.Component, org.olat.core.gui.control.Event)
	 */
	public void event(UserRequest ureq, Component source, Event event) {
		//
	}

	/**
	 * @see org.olat.core.gui.control.DefaultController#doDispose(boolean)
	 */
	protected void doDispose() {
		//
	}

}
