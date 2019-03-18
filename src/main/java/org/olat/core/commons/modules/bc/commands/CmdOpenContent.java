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
package org.olat.core.commons.modules.bc.commands;

import org.olat.core.commons.editor.fileeditor.FileEditor;
import org.olat.core.commons.modules.bc.components.FolderComponent;
import org.olat.core.commons.modules.bc.components.ListRenderer;
import org.olat.core.commons.services.notifications.NotificationsManager;
import org.olat.core.commons.services.notifications.SubscriptionContext;
import org.olat.core.commons.services.vfs.VFSLeafEditor;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.control.ChiefController;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.ScreenMode.Mode;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.gui.translator.Translator;
import org.olat.core.util.StringHelper;
import org.olat.core.util.vfs.VFSContainer;
import org.olat.core.util.vfs.VFSItem;
import org.olat.core.util.vfs.VFSLeaf;
import org.olat.core.util.vfs.VFSManager;
import org.olat.core.util.vfs.callbacks.VFSSecurityCallback;

/**
 * 
 * Initial date: 18 Mar 2019<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class CmdOpenContent extends BasicController implements FolderCommand {

	private VFSItem currentItem;
	private int status = FolderCommandStatus.STATUS_SUCCESS;
	
	private FolderComponent folderComponent;
	private Controller editCtrl;

	protected CmdOpenContent(UserRequest ureq, WindowControl wControl) {
		super(ureq, wControl);
	}

	@Override
	@SuppressWarnings("deprecation")
	public Controller execute(FolderComponent folderComponent, UserRequest ureq, WindowControl wControl,
			Translator translator) {
		this.folderComponent = folderComponent;
		String pos = ureq.getParameter(ListRenderer.PARAM_CONTENTEDITID);
		if (!StringHelper.containsNonWhitespace(pos)) {
			// somehow parameter did not make it to us
			status = FolderCommandStatus.STATUS_FAILED;
			getWindowControl().setError(translator.translate("failed"));
			return null;
		}
		
		status = FolderCommandHelper.sanityCheck(wControl, folderComponent);
		if(status == FolderCommandStatus.STATUS_SUCCESS) {
			currentItem = folderComponent.getCurrentContainerChildren().get(Integer.parseInt(pos));
			status = FolderCommandHelper.sanityCheck2(wControl, folderComponent, currentItem);
		}
		if(status == FolderCommandStatus.STATUS_FAILED) {
			return null;
		}
		
		status = FolderCommandHelper.fileEditSanityCheck(currentItem);
		if (status == FolderCommandStatus.STATUS_FAILED) {
			// this should no longer happen, since folderComponent -> ListRenderer does not display edit-link for folders
			logWarn("Given VFSItem is not a file, can't edit it: "+folderComponent.getCurrentContainerPath() + "/" + currentItem.getName(),null);
			getWindowControl().setError(translator.translate("FileEditFailed"));
			return null;
		}
		
		if (!(currentItem instanceof VFSLeaf)) {
			status = FolderCommandStatus.STATUS_FAILED;
			getWindowControl().setError(translator.translate("failed"));
			return null;
		}
		
		VFSLeaf vfsLeaf = (VFSLeaf)currentItem;
		VFSLeafEditor editor = new FileEditor();
		editCtrl = editor.getRunController(ureq, wControl, vfsLeaf, folderComponent, getIdentity());
		listenTo(editCtrl);
		
		ChiefController cc = getWindowControl().getWindowBackOffice().getChiefController();
		String businessPath = editCtrl.getWindowControlForDebug().getBusinessControl().getAsString();
		cc.getScreenMode().setMode(Mode.full, businessPath);
		getWindowControl().pushToMainArea(editCtrl.getInitialComponent());
		return this;
	}

	@Override
	public int getStatus() {
		return status;
	}

	@Override
	public boolean runsModal() {
		return true;
	}

	@Override
	public String getModalTitle() {
		return null;
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		//
	}
	
	
	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if (source == editCtrl) {
			if (event == Event.DONE_EVENT) {
				notifyFinished(ureq);
			}
			doCloseEditor();
			fireEvent(ureq, FOLDERCOMMAND_FINISHED);
		}
		super.event(ureq, source, event);
	}

	private void notifyFinished(UserRequest ureq) {
		VFSContainer container = VFSManager.findInheritingSecurityCallbackContainer(folderComponent.getRootContainer());
		VFSSecurityCallback secCallback = container.getLocalSecurityCallback();
		if(secCallback != null) {
			SubscriptionContext subsContext = secCallback.getSubscriptionContext();
			if (subsContext != null) {
				NotificationsManager.getInstance().markPublisherNews(subsContext, ureq.getIdentity(), true);
			}
		}
	}
	
	private void doCloseEditor() {
		getWindowControl().pop();
		String businessPath = getWindowControl().getBusinessControl().getAsString();
		getWindowControl().getWindowBackOffice().getChiefController().getScreenMode().setMode(Mode.standard, businessPath);
		cleanUp();
	}
	
	private void cleanUp() {
		removeAsListenerAndDispose(editCtrl);
		editCtrl = null;
	}

	@Override
	protected void doDispose() {
		//
	}

	public String getFileName() {
		return currentItem.getName();
	}

}
