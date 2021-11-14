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
package org.olat.modules.wiki;

import org.olat.core.commons.modules.bc.FileUploadController;
import org.olat.core.commons.modules.bc.FolderConfig;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.util.vfs.Quota;
import org.olat.core.util.vfs.VFSContainer;

/**
 * Only a wrapper to show some informations around the file upload controller.
 * 
 * 
 * Initial date: 17.07.2015<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class WikiFileUploadController extends BasicController {
	
	private final FileUploadController fileUplCtr;
	
	public WikiFileUploadController(UserRequest ureq, WindowControl wControl, VFSContainer mediaFolder) {
		super(ureq, wControl);
		
		VelocityContainer mainVC = this.createVelocityContainer("upload_file");
		
		fileUplCtr = new FileUploadController(getWindowControl(), mediaFolder, ureq,
				FolderConfig.getLimitULKB(), Quota.UNLIMITED, null, false, false, false, true, true, false);
		listenTo(fileUplCtr);
		mainVC.put("fileUpload", fileUplCtr.getInitialComponent());
		putInitialPanel(mainVC);
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		//
	}

	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		fireEvent(ureq, event);
	}
}
