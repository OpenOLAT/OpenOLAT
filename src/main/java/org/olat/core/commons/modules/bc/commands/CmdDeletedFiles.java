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

import org.olat.core.commons.modules.bc.components.FolderComponent;
import org.olat.core.commons.services.vfs.ui.version.DeletedFileListController;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.gui.translator.Translator;
import org.olat.core.util.Util;
import org.olat.core.util.vfs.VFSContainer;
import org.olat.core.util.vfs.VFSManager;

/**
 * 
 * Description:<br>
 * Open a panel with the list of deleted files of the selected container. The panel
 * can delete definitively a deleted and versioned file and restore them.
 * 
 * <P>
 * Initial Date:  21 sept. 2009 <br>
 *
 * @author srosse
 */
public class CmdDeletedFiles extends BasicController implements FolderCommand {

	private int status = FolderCommandStatus.STATUS_SUCCESS;
	private DeletedFileListController deletedFileListCtr;
	
	public CmdDeletedFiles(UserRequest ureq, WindowControl wControl) {
		super(ureq, wControl, Util.createPackageTranslator(DeletedFileListController.class, ureq.getLocale()));
	}

	@Override
	public Controller execute(FolderComponent folderComponent, UserRequest ureq, WindowControl wControl, Translator translator) {
		if(deletedFileListCtr != null) {
			removeAsListenerAndDispose(deletedFileListCtr);
		}

		VFSContainer currentContainer = folderComponent.getCurrentContainer();
		if(!VFSManager.exists(currentContainer)) {
			status = FolderCommandStatus.STATUS_FAILED;
			getWindowControl().setError(translator.translate("FileDoesNotExist"));
			return null;
		}
		
		deletedFileListCtr = new DeletedFileListController(ureq,wControl,currentContainer);
		listenTo(deletedFileListCtr);
		putInitialPanel(deletedFileListCtr.getInitialComponent());
		return deletedFileListCtr;
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		//
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
		return translate("version.deletedFiles");
	}
}
