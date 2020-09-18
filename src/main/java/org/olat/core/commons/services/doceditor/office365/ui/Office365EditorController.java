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
package org.olat.core.commons.services.doceditor.office365.ui;

import java.time.Duration;
import java.time.Instant;
import java.util.Date;

import org.olat.core.commons.services.doceditor.Access;
import org.olat.core.commons.services.doceditor.DocEditor.Mode;
import org.olat.core.commons.services.doceditor.DocEditorService;
import org.olat.core.commons.services.doceditor.office365.Office365Service;
import org.olat.core.commons.services.vfs.VFSMetadata;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.util.vfs.VFSLeaf;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 26 Apr 2019<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class Office365EditorController extends BasicController {

	private Access access;

	@Autowired
	private Office365Service office365Service;
	@Autowired
	private DocEditorService docEditorService;
	
	public Office365EditorController(UserRequest ureq, WindowControl wControl, VFSLeaf vfsLeaf,
			Access access) {
		super(ureq, wControl);
		this.access = access;
		
		if (office365Service.isLockNeeded(access.getMode())) {
			if (office365Service.isLockedForMe(vfsLeaf, getIdentity())) {
				this.access = docEditorService.updateMode(access, Mode.VIEW);
				showWarning("editor.warning.locked");
			}
		}
		VelocityContainer mainVC = createVelocityContainer("editor");
		
		VFSMetadata vfsMetadata = vfsLeaf.getMetaInfo();
		if (vfsMetadata == null) {
			mainVC.contextPut("warning", translate("editor.warning.no.metadata"));
		} else {
			String actionUrl = office365Service.getEditorActionUrl(vfsMetadata, access.getMode(), getLocale());
			if (actionUrl == null) {
				mainVC.contextPut("warning", translate("editor.warning.no.metadata"));
			} else {
				mainVC.contextPut("actionUrl", actionUrl);
				mainVC.contextPut("accessToken", access.getKey().toString());
				mainVC.contextPut("accessTokenTtl", access.getExpiresAt().getTime());
			}
		}
		
		putInitialPanel(mainVC);
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		//
	}

	@Override
	protected void doDispose() {
		Date expiresAt = Date.from(Instant.now().plus(Duration.ofMinutes(5)));
		docEditorService.updatetExpiresAt(access, expiresAt);
	}

}
