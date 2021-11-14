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

import org.olat.core.commons.modules.bc.FileCopyController;
import org.olat.core.commons.modules.bc.components.FolderComponent;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.gui.translator.Translator;
import org.olat.core.logging.AssertException;
import org.olat.core.util.WebappHelper;
import org.olat.core.util.vfs.VFSConstants;
import org.olat.core.util.vfs.VFSContainer;
import org.olat.core.util.vfs.VFSManager;

/**
 * 
 * Description:<br>
 * Copy a file from an external folder
 * 
 * <P>
 * Initial Date:  17 mars 2011 <br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
public class CmdCopyFile extends BasicController implements FolderCommand {

	private int status = FolderCommandStatus.STATUS_SUCCESS;
	
	protected CmdCopyFile(UserRequest ureq, WindowControl wControl) {
		super(ureq, wControl);		
	}

	@Override
	public Controller execute(FolderComponent folderComponent, UserRequest ureq, WindowControl wControl, Translator translator) {
		setTranslator(translator);
		if (folderComponent.getCurrentContainer().canWrite() != VFSConstants.YES) {
			throw new AssertException("Illegal attempt to create file in: " + folderComponent.getCurrentContainerPath());
		}
		
		//check for quota
		long quotaLeft = VFSManager.getQuotaLeftKB(folderComponent.getCurrentContainer());
		if (quotaLeft < -2) {
			String supportAddr = WebappHelper.getMailConfig("mailQuota");
			String msg = translate("QuotaExceededSupport", new String[] { supportAddr });
			getWindowControl().setError(msg);
			return null;
		}
		
		VFSContainer cContainer = folderComponent.getExternContainerForCopy();
		return new FileCopyController(ureq, wControl, cContainer, folderComponent);
	}

	@Override
	public void event(UserRequest ureq, Component source, Event event) {
		//empty
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
		return null;
	}
}
