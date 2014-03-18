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

package org.olat.modules.sharedfolder;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.control.DefaultController;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.translator.Translator;
import org.olat.core.util.Util;
import org.olat.fileresource.types.FileResource;
import org.olat.fileresource.types.SharedFolderFileResource;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryManager;
import org.olat.repository.controllers.IAddController;
import org.olat.repository.controllers.RepositoryAddCallback;

/**
 * Initial Date:  Aug 29, 2005 <br>
 * @author Alexander Schneider
 */
public class CreateNewSharedFolderController extends DefaultController implements IAddController {

	private FileResource newFileResource;

	/**
	 * @param addCallback
	 * @param ureq
	 */
	public CreateNewSharedFolderController(RepositoryAddCallback addCallback, UserRequest ureq, WindowControl wControl) {
		super(wControl);
		if (addCallback != null) {
			newFileResource = SharedFolderManager.getInstance().createSharedFolder();
			Translator trnsltr = Util.createPackageTranslator(RepositoryManager.class, ureq.getLocale());
			addCallback.setDisplayName(trnsltr.translate(newFileResource.getResourceableTypeName()));
			addCallback.setResourceable(newFileResource);
			addCallback.setResourceName(SharedFolderFileResource.RESOURCE_NAME);

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
		return true;
	}

	/**
	 * @see org.olat.repository.controllers.IAddController#transactionAborted()
	 */
	public void transactionAborted() {
		// File resource already created. Cleanup file resource on abort.
		if (newFileResource != null) SharedFolderManager.getInstance().deleteSharedFolder(newFileResource);
	}

	/**
	 * @see org.olat.core.gui.control.DefaultController#event(org.olat.core.gui.UserRequest,
	 *      org.olat.core.gui.components.Component, org.olat.core.gui.control.Event)
	 */
	public void event(UserRequest ureq, Component source, Event event) {
	// nothing to process here.
	}

	/**
	 * @see org.olat.repository.controllers.IAddController#repositoryEntryCreated(org.olat.repository.RepositoryEntry)
	 */
	public void repositoryEntryCreated(RepositoryEntry re) {
		return;
	} // nothing to do here.

	@Override
	public void repositoryEntryCopied(RepositoryEntry sourceEntry, RepositoryEntry newEntry) {
		//
	}

	/**
	 * @see org.olat.core.gui.control.DefaultController#doDispose(boolean)
	 */
	protected void doDispose() {
	//
	}
}
