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

import org.olat.admin.quota.QuotaController;
import org.olat.core.commons.modules.bc.components.FolderComponent;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.ControllerEventListener;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.gui.translator.Translator;
import org.olat.core.util.Util;
import org.olat.core.util.vfs.Quota;
import org.olat.core.util.vfs.QuotaManager;
import org.olat.core.util.vfs.VFSContainer;
import org.olat.core.util.vfs.VFSManager;
import org.olat.core.util.vfs.callbacks.VFSSecurityCallback;
import org.springframework.beans.factory.annotation.Autowired;

public class CmdEditQuota extends BasicController implements FolderCommand, ControllerEventListener {

	private int status = FolderCommandStatus.STATUS_SUCCESS;
	private Controller quotaEditController;
	private VFSSecurityCallback currentSecCallback = null;
	
	@Autowired
	private QuotaManager quotaManager;
	
	protected CmdEditQuota(UserRequest ureq, WindowControl wControl) {
		super(ureq, wControl, Util.createPackageTranslator(QuotaController.class, ureq.getLocale()));
	}

	@Override
	public Controller execute(FolderComponent folderComponent, UserRequest ureq, WindowControl wControl, Translator translator) {
		VFSContainer inheritingContainer = VFSManager.findInheritingSecurityCallbackContainer(folderComponent.getCurrentContainer());
		if (inheritingContainer == null || inheritingContainer.getLocalSecurityCallback().getQuota() == null) {
			getWindowControl().setWarning(translator.translate("editQuota.nop"));
			return null;
		}
		
		currentSecCallback = inheritingContainer.getLocalSecurityCallback();
		// cleanup old controller first
		if (quotaEditController != null) quotaEditController.dispose();
		// create a edit controller
		quotaEditController = quotaManager.getQuotaEditorInstance(ureq, wControl, currentSecCallback.getQuota().getPath(), false, true);
		quotaEditController.addControllerListener(this);
		if (quotaEditController != null) {
			putInitialPanel(quotaEditController.getInitialComponent());
			return this;
		}
		// do nothing, quota can't be edited
		wControl.setWarning("No quota editor available in briefcase, can't use this function!");
		return null;
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
		return translate("qf.edit");
	}

	@Override
	public void event(UserRequest ureq, Controller source, Event event) {
		if (source == quotaEditController) {
			if (event == Event.CHANGED_EVENT) {
				// update quota
				Quota newQuota = quotaManager.getCustomQuota(currentSecCallback.getQuota().getPath());
				if (newQuota != null) currentSecCallback.setQuota(newQuota);
			} else if (event == Event.CANCELLED_EVENT) {
				// do nothing
			}
			fireEvent(ureq, FOLDERCOMMAND_FINISHED);
		}
	}

	@Override
	public void event(UserRequest ureq, Component source, Event event) {
		//
	}

	@Override
	protected void doDispose() {
		if (quotaEditController != null) {
			quotaEditController.dispose();
			quotaEditController = null;
		}
        super.doDispose();
	}
}