/**
 * <a href="https://www.openolat.org">
 * OpenOLAT - Online Learning and Training</a><br>
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); <br>
 * you may not use this file except in compliance with the License.<br>
 * You may obtain a copy of the License at the
 * <a href="https://www.apache.org/licenses/LICENSE-2.0">Apache homepage</a>
 * <p>
 * Unless required by applicable law or agreed to in writing,<br>
 * software distributed under the License is distributed on an "AS IS" BASIS, <br>
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
 * See the License for the specific language governing permissions and <br>
 * limitations under the License.
 * <p>
 * Initial code contributed and copyrighted by<br>
 * frentix GmbH, https://www.frentix.com
 * <p>
 */
package org.olat.core.commons.services.folder.ui;

import org.olat.core.commons.modules.bc.FileUploadController;
import org.olat.core.commons.services.folder.ui.component.QuotaBar;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.util.vfs.VFSConstants;
import org.olat.core.util.vfs.VFSContainer;

/**
 * 
 * Initial date: 28 Feb 2024<br>
 * @author uhensler, urs.hensler@frentix.com, https://www.frentix.com
 *
 */
public class UploadController extends BasicController {

	private final FileUploadController fileUploadCtr;

	public UploadController(UserRequest ureq, WindowControl wControl, VFSContainer currentContainer, FolderQuota folderQuota) {
		super(ureq, wControl);
		VelocityContainer mainVC = createVelocityContainer("upload");
		putInitialPanel(mainVC);

		QuotaBar quotaBar = new QuotaBar("quota", folderQuota, getLocale());
		mainVC.put(quotaBar.getComponentName(), quotaBar);
		
		// Upload
		fileUploadCtr = new FileUploadController(getWindowControl(), currentContainer, ureq,
				folderQuota.getUploadLimitKB(), folderQuota.getRemainingQuotaKB(), null, false, true,
				currentContainer.canMeta() == VFSConstants.YES, true, true, false);
		listenTo(fileUploadCtr);
		mainVC.put("upload", fileUploadCtr.getInitialComponent());
	}

	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if (source == fileUploadCtr) {
			fireEvent(ureq, event);
		}
		super.event(ureq, source, event);
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		//
	}

}
