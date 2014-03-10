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

import org.olat.core.CoreSpringFactory;
import org.olat.core.commons.modules.bc.FolderEvent;
import org.olat.core.commons.modules.bc.components.FolderComponent;
import org.olat.core.commons.modules.bc.components.ListRenderer;
import org.olat.core.commons.modules.bc.meta.MetaInfo;
import org.olat.core.commons.modules.bc.meta.MetaInfoController;
import org.olat.core.commons.modules.bc.meta.MetaInfoFormController;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.gui.translator.Translator;
import org.olat.core.util.StringHelper;
import org.olat.core.util.vfs.VFSConstants;
import org.olat.core.util.vfs.VFSContainer;
import org.olat.core.util.vfs.VFSItem;
import org.olat.core.util.vfs.VFSLockManager;

public class CmdEditMeta extends BasicController implements FolderCommand {

	private int status = FolderCommandStatus.STATUS_SUCCESS;
	
	private MetaInfoController metaCtr;
	private MetaInfoFormController metaInfoCtr;
	private VFSItem currentItem;
	private Translator translator;

	private final VFSLockManager vfsLockManager;

	protected CmdEditMeta(UserRequest ureq, WindowControl wControl) {
		super(ureq, wControl);
		vfsLockManager = CoreSpringFactory.getImpl(VFSLockManager.class);
	}

	/**
	 * Checks if the file/folder name is not null and valid,
	 * checks if the FolderComponent is ok,
	 * checks if the item exists and is not locked.
	 * 
	 * @see org.olat.core.commons.modules.bc.commands.FolderCommand#execute(org.olat.core.commons.modules.bc.components.FolderComponent, org.olat.core.gui.UserRequest, org.olat.core.gui.control.WindowControl, org.olat.core.gui.translator.Translator)
	 */
	public Controller execute(FolderComponent folderComponent,
			UserRequest ureq, WindowControl wControl, Translator trans) {
		this.translator = trans;

		String pos = ureq.getParameter(ListRenderer.PARAM_EDTID);
		if (!StringHelper.containsNonWhitespace(pos)) {
			// somehow parameter did not make it to us
			status = FolderCommandStatus.STATUS_FAILED;
			getWindowControl().setError(translator.translate("failed"));
			return null;
		}
		
		status = FolderCommandHelper.sanityCheck(wControl, folderComponent);
		if(status == FolderCommandStatus.STATUS_SUCCESS) {
			currentItem = folderComponent.getCurrentContainerChildren().get(Integer.parseInt(pos));
		}
		if(status == FolderCommandStatus.STATUS_FAILED) {
			return null;
		}	

		removeAsListenerAndDispose(metaCtr);
		removeAsListenerAndDispose(metaInfoCtr);

		if(vfsLockManager.isLockedForMe(currentItem, getIdentity(), ureq.getUserSession().getRoles())) {
			//readonly
			metaCtr = new MetaInfoController(ureq, wControl, currentItem);
			listenTo(metaCtr);
			putInitialPanel(metaCtr.getInitialComponent());
		} else {
			metaInfoCtr = new MetaInfoFormController(ureq, wControl, currentItem);
			listenTo(metaInfoCtr);
			putInitialPanel(metaInfoCtr.getInitialComponent());
		}
		return this;
	}

	public int getStatus() {
		return status;
	}

	/**
	 * @see org.olat.core.gui.control.DefaultController#event(org.olat.core.gui.UserRequest,
	 *      org.olat.core.gui.components.Component,
	 *      org.olat.core.gui.control.Event)
	 */
	public void event(UserRequest ureq, Component source, Event event) {
		// nothing to do here
	}

	/**
	 * @see org.olat.core.gui.control.DefaultController#event(org.olat.core.gui.UserRequest,
	 *      org.olat.core.gui.control.Controller,
	 *      org.olat.core.gui.control.Event)
	 */
	public void event(UserRequest ureq, Controller source, Event event) {
		if (source == metaInfoCtr && event == Event.DONE_EVENT) {
			MetaInfo meta = metaInfoCtr.getMetaInfo();
			String fileName = metaInfoCtr.getFilename();
			if(meta != null) {
				meta.write();
				if (metaInfoCtr.isFileRenamed()) {
					// IMPORTANT: First rename the meta data because underlying file
					// has to exist in order to work properly on it's meta data.
					VFSContainer container = currentItem.getParentContainer();
					if(container.resolve(fileName) != null) {
						getWindowControl().setError(translator.translate("TargetNameAlreadyUsed"));
						status = FolderCommandStatus.STATUS_FAILED;
					} else {
						if (meta != null) {
							meta.rename(fileName);
						}
						if(VFSConstants.NO.equals(currentItem.rename(fileName))) {
							getWindowControl().setError(translator.translate("FileRenameFailed", new String[]{fileName}));
							status = FolderCommandStatus.STATUS_FAILED;
						}
					}
				}
			}
			fireEvent(ureq, new FolderEvent(FolderEvent.EDIT_EVENT, fileName));
			fireEvent(ureq, FOLDERCOMMAND_FINISHED);
		} else if (event == Event.CANCELLED_EVENT) {
			fireEvent(ureq, FOLDERCOMMAND_FINISHED);
		}
	}

	protected void doDispose() {
		// metaInfoCtr should be auto-disposed
	}

	public boolean runsModal() {
		return false;
	}
}
