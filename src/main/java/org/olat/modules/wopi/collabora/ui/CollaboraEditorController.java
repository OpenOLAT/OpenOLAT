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
package org.olat.modules.wopi.collabora.ui;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.util.CodeHelper;
import org.olat.modules.wopi.Access;
import org.olat.modules.wopi.collabora.CollaboraService;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 10 Feb 2019<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class CollaboraEditorController extends BasicController {
	
	private final Access access;

	@Autowired
	private CollaboraService collaboraService;
	
	public CollaboraEditorController(UserRequest ureq, WindowControl wControl, Access access) {
		super(ureq, wControl);
		this.access = access;
		
		VelocityContainer mainVC = createVelocityContainer("collabora");
		
		String url = CollaboraEditorUrlBuilder
				.builder(access.getFileId(), access.getToken())
				.withLang(ureq.getLocale().getLanguage())
				.withCloseButton(access.canClose())
				.build();
		
		mainVC.contextPut("id", "o_" + CodeHelper.getRAMUniqueID());
		mainVC.contextPut("url", url);
		
		putInitialPanel(mainVC);
	}
	
	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		if ("close".equals(event.getCommand())) {
			// Suppress close event, because we can not hide close button
			if (access.canClose()) {
				fireEvent(ureq, Event.DONE_EVENT);
			}
		}
	}

	@Override
	protected void doDispose() {
		collaboraService.deleteAccess(access);
	}

}
