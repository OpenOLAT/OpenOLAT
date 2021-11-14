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
import org.olat.core.commons.modules.bc.components.ListRenderer;
import org.olat.core.commons.services.vfs.ui.version.RevisionListController;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.gui.translator.Translator;
import org.olat.core.util.StringHelper;
import org.olat.core.util.vfs.VFSConstants;
import org.olat.core.util.vfs.VFSItem;
import org.olat.core.util.vfs.VFSLockApplicationType;
import org.olat.core.util.vfs.VFSLockManager;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Description:<br>
 * Open a panel with the list of revisions of the selected file
 * <br>
 * Events:
 * <ul>
 * 	<li>FOLDERCOMMAND_FINISHED</li>
 * </ul>
 * <P>
 * Initial Date:  21 sept. 2009 <br>
 *
 * @author srosse
 */
public class CmdViewRevisions extends BasicController implements FolderCommand {

	private int status = FolderCommandStatus.STATUS_SUCCESS;
	private RevisionListController revisionListCtr;
	private VFSItem currentItem;
	
	@Autowired
	private VFSLockManager vfsLockManager;

	public CmdViewRevisions(UserRequest ureq, WindowControl wControl) {
		super(ureq, wControl);
	}

	public Controller execute(FolderComponent folderComponent, UserRequest ureq, WindowControl wControl, Translator translator) {
		if (revisionListCtr != null) {
			removeAsListenerAndDispose(revisionListCtr);
		}
		
		String pos = ureq.getParameter(ListRenderer.PARAM_VERID);
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
		if (currentItem.canVersion() != VFSConstants.YES) {
			status = FolderCommandStatus.STATUS_FAILED;
			getWindowControl().setError(translator.translate("failed"));
			return null;
		}

		setTranslator(translator);
		
		boolean locked = vfsLockManager.isLockedForMe(currentItem, ureq.getIdentity(), VFSLockApplicationType.vfs, null);
		revisionListCtr = new RevisionListController(ureq, wControl, currentItem, locked);
		listenTo(revisionListCtr);
		putInitialPanel(revisionListCtr.getInitialComponent());
		return this;
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
		if(currentItem != null) {
			return translate("versions.revisions.of", new String[] { currentItem.getName() });
		}
		return translate("versions.revisions");
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
	// nothing to do here
	}

	@Override
	public void event(UserRequest ureq, Controller source, Event event) {
		if (source == revisionListCtr) {
			if (event == Event.DONE_EVENT) {
				fireEvent(ureq, FOLDERCOMMAND_FINISHED);
			} else if (event == Event.CANCELLED_EVENT) {
				fireEvent(ureq, FOLDERCOMMAND_FINISHED);
			} else if (event == FOLDERCOMMAND_FINISHED) {
				fireEvent(ureq, FOLDERCOMMAND_FINISHED);
			}
		}
	}
}