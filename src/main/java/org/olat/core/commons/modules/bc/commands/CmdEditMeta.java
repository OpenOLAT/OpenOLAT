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

import java.util.List;

import org.olat.core.commons.modules.bc.FolderEvent;
import org.olat.core.commons.modules.bc.components.FolderComponent;
import org.olat.core.commons.modules.bc.components.ListRenderer;
import org.olat.core.commons.modules.bc.meta.MetaInfoController;
import org.olat.core.commons.modules.bc.meta.MetaInfoFormController;
import org.olat.core.commons.services.notifications.NotificationsManager;
import org.olat.core.commons.services.notifications.SubscriptionContext;
import org.olat.core.commons.services.vfs.VFSMetadata;
import org.olat.core.commons.services.vfs.VFSRepositoryService;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.gui.translator.Translator;
import org.olat.core.id.OLATResourceable;
import org.olat.core.id.context.BusinessControlFactory;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
import org.olat.core.util.resource.OresHelper;
import org.olat.core.util.vfs.VFSConstants;
import org.olat.core.util.vfs.VFSContainer;
import org.olat.core.util.vfs.VFSItem;
import org.olat.core.util.vfs.VFSLockApplicationType;
import org.olat.core.util.vfs.VFSLockManager;
import org.olat.core.util.vfs.VFSManager;
import org.olat.core.util.vfs.callbacks.VFSSecurityCallback;
import org.springframework.beans.factory.annotation.Autowired;

public class CmdEditMeta extends BasicController implements FolderCommand {

	private int status = FolderCommandStatus.STATUS_SUCCESS;
	
	private MetaInfoController metaCtr;
	private MetaInfoFormController metaInfoCtr;
	private VFSItem currentItem;
	private FolderComponent folderComponent;
	private Translator translator;

	@Autowired
	private VFSLockManager vfsLockManager;
	@Autowired
	private VFSRepositoryService vfsRepositoryService;
	@Autowired
	private NotificationsManager notificationsManager;

	protected CmdEditMeta(UserRequest ureq, WindowControl wControl) {
		super(ureq, wControl, Util.createPackageTranslator(MetaInfoController.class, ureq.getLocale()));
	}

	/**
	 * Checks if the file/folder name is not null and valid,
	 * checks if the FolderComponent is ok,
	 * checks if the item exists and is not locked.
	 * 
	 * @see org.olat.core.commons.modules.bc.commands.FolderCommand#execute(org.olat.core.commons.modules.bc.components.FolderComponent, org.olat.core.gui.UserRequest, org.olat.core.gui.control.WindowControl, org.olat.core.gui.translator.Translator)
	 */
	@Override
	public Controller execute(FolderComponent fComponent,
			UserRequest ureq, WindowControl wControl, Translator trans) {
		this.translator = trans;
		this.folderComponent = fComponent;
		String posString = ureq.getParameter(ListRenderer.PARAM_EDTID);
		if (!StringHelper.isLong(posString)) {
			// somehow parameter did not make it to us
			status = FolderCommandStatus.STATUS_FAILED;
			getWindowControl().setError(translator.translate("failed"));
			return null;
		}
		
		status = FolderCommandHelper.sanityCheck(wControl, fComponent);
		if(status == FolderCommandStatus.STATUS_SUCCESS) {
			int pos = Integer.parseInt(posString);
			List<VFSItem> children = fComponent.getCurrentContainerChildren();
			if(pos >= 0 && pos < children.size()) {
				currentItem = children.get(pos);
			} else {
				status = FolderCommandStatus.STATUS_FAILED;
				getWindowControl().setWarning(translator.translate("warning.file.not.found"));
				fComponent.updateChildren();
				return null;
			}
		}
		if(status == FolderCommandStatus.STATUS_FAILED) {
			return null;
		}	

		removeAsListenerAndDispose(metaCtr);
		removeAsListenerAndDispose(metaInfoCtr);
		if(vfsLockManager.isLockedForMe(currentItem, getIdentity(), VFSLockApplicationType.vfs, null)) {
			//readonly
			String resourceUrl = getResourceURL(wControl);
			metaCtr = new MetaInfoController(ureq, wControl, currentItem, resourceUrl);
			listenTo(metaCtr);
			putInitialPanel(metaCtr.getInitialComponent());
		} else {
			String resourceUrl = getResourceURL(wControl);
			metaInfoCtr = new MetaInfoFormController(ureq, wControl, currentItem, resourceUrl);
			listenTo(metaInfoCtr);
			putInitialPanel(metaInfoCtr.getInitialComponent());
		}
		return this;
	}
	
	private String getResourceURL(WindowControl wControl) {
		String path = "path=" + folderComponent.getCurrentContainerPath();
		if(currentItem != null) {
			if(path.charAt(path.length() - 1) != '/') {
				path += "/";
			}
			path += currentItem.getName();
		}
		OLATResourceable ores = OresHelper.createOLATResourceableTypeWithoutCheck(path);
		WindowControl bwControl = BusinessControlFactory.getInstance().createBusinessWindowControl(ores, null, wControl);
		return BusinessControlFactory.getInstance().getAsURIString(bwControl.getBusinessControl(), false);
	}

	@Override
	public int getStatus() {
		return status;
	}

	@Override
	public String getModalTitle() {
		return translate("mf.metadata.title");
	}

	@Override
	public void event(UserRequest ureq, Component source, Event event) {
		// nothing to do here
	}

	@Override
	public void event(UserRequest ureq, Controller source, Event event) {
		if (source == metaInfoCtr && event == Event.DONE_EVENT) {
			VFSMetadata meta = metaInfoCtr.getMetaInfo();
			String fileName = metaInfoCtr.getFilename();
			if(meta != null) {
				vfsRepositoryService.updateMetadata(meta);
				if (metaInfoCtr.isFileRenamed()) {
					// IMPORTANT: First rename the meta data because underlying file
					// has to exist in order to work properly on it's meta data.
					VFSContainer container = currentItem.getParentContainer();
					if(container.resolve(fileName) != null) {
						getWindowControl().setError(translator.translate("TargetNameAlreadyUsed"));
						status = FolderCommandStatus.STATUS_FAILED;
					} else {
						if(VFSConstants.NO.equals(currentItem.rename(fileName))) {
							getWindowControl().setError(translator.translate("FileRenameFailed", new String[]{fileName}));
							status = FolderCommandStatus.STATUS_FAILED;
						}
					}
				}
			}
			fireEvent(ureq, new FolderEvent(FolderEvent.EDIT_EVENT, fileName));
			notifyFinished(ureq);
		} else if (event == Event.CANCELLED_EVENT) {
			fireEvent(ureq, FOLDERCOMMAND_FINISHED);
		}
	}
	
	private void notifyFinished(UserRequest ureq) {
		VFSContainer container = VFSManager.findInheritingSecurityCallbackContainer(folderComponent.getRootContainer());
		VFSSecurityCallback secCallback = container.getLocalSecurityCallback();
		if(secCallback != null) {
			SubscriptionContext subsContext = secCallback.getSubscriptionContext();
			if (subsContext != null) {
				notificationsManager.markPublisherNews(subsContext, ureq.getIdentity(), true);
			}
		}
		fireEvent(ureq, FOLDERCOMMAND_FINISHED);
	}

	@Override
	public boolean runsModal() {
		return false;
	}
}
