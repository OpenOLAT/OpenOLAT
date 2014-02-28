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

package org.olat.ims.cp.ui;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.control.ControllerEventListener;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.ims.cp.CPManager;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.controllers.IAddController;
import org.olat.repository.controllers.RepositoryAddCallback;
import org.olat.resource.OLATResource;
import org.olat.resource.OLATResourceManager;

/**
 * 
 * Description:<br>
 * Implementation of the repository add controller for IMS ContentPackages
 * 
 * <P>
 * Initial Date:  11.09.2008 <br>
 * @author Sergio Trentini
 */
public class CreateNewCPController extends BasicController implements IAddController, ControllerEventListener {

	
	private OLATResource newCPResource;
	
	public CreateNewCPController(RepositoryAddCallback addCallback, UserRequest ureq, WindowControl wControl) {
		super(ureq,wControl);
		// do prepare course now
		newCPResource = OLATResourceManager.getInstance().createOLATResourceInstance("FileResource.IMSCP");
		
		if (addCallback != null) {
			addCallback.setResourceable(newCPResource);
			addCallback.setDisplayName(translate("FileResource.IMSCP"));
			addCallback.setResourceName("-");
			addCallback.finished(ureq);
		}
	}

	@Override
	protected void doDispose() {
	// nothing to dispose
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
	// nothing to listen to...
	}

	public Component getTransactionComponent() {
		return getInitialComponent();
	}

	public void repositoryEntryCreated(RepositoryEntry re) {
		//
	}

	@Override
	public void repositoryEntryCopied(RepositoryEntry sourceEntry, RepositoryEntry newEntry) {
		//
	}

	public void transactionAborted() {
		// Don't do nothing!
	}

	public boolean transactionFinishBeforeCreate() {
		CPManager cpMmg = CPManager.getInstance();
		String initialPageTitle = translate("cptreecontroller.newpage.title");
		cpMmg.createNewCP(newCPResource, initialPageTitle);
		return true;
	}

}
