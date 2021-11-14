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
package org.olat.core.commons.services.doceditor.ui;

import org.olat.core.commons.services.doceditor.DocEditor;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.user.UserManager;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 6 May 2019<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class DataTransferConfirmationController extends BasicController {

	private Link acceptLink;
	
	private final DocEditor editor;
	
	@Autowired
	private UserManager userManager;

	public DataTransferConfirmationController(UserRequest ureq, WindowControl wControl, DocEditor editor) {
		super(ureq, wControl);
		this.editor = editor;
		VelocityContainer mainVC = createVelocityContainer("data_transfer_confirmation");
		String[] args = new String[] {
				editor.getDisplayName(getLocale()),
				userManager.getUserDisplayName(getIdentity())
		};
		String intro = translate("data.transfer.intro", args);
		mainVC.contextPut("dataTransferIntro", intro);
		acceptLink = LinkFactory.createButton("data.transfer.accept", mainVC, this);
		acceptLink.setPrimary(true);
		putInitialPanel(mainVC);
	}

	public DocEditor getEditor() {
		return editor;
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		if (source == acceptLink) {
			fireEvent(ureq, Event.DONE_EVENT);
		}
	}
}
