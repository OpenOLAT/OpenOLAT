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
package org.olat.modules.video.ui.editor;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;

/**
 * Initial date: 2023-02-09<br>
 *
 * @author cpfranger, christoph.pfranger@frentix.com, <a href="https://www.frentix.com">https://www.frentix.com</a>
 */
public class HeaderCommandsController extends BasicController {
	public static final Event DELETE_EVENT = new Event("header.commands.delete");
	private final Link deleteLink;

	protected HeaderCommandsController(UserRequest ureq, WindowControl wControl) {
		super(ureq, wControl);

		VelocityContainer mainVC = createVelocityContainer("header_commands");
		deleteLink = LinkFactory.createLink("delete", "delete", getTranslator(), mainVC, this,
				Link.LINK);
		deleteLink.setIconLeftCSS("o_icon o_icon-fw o_icon_delete");
		mainVC.put("delete", deleteLink);

		putInitialPanel(mainVC);

	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		if (deleteLink == source) {
			fireEvent(ureq, DELETE_EVENT);
		}
	}
}
