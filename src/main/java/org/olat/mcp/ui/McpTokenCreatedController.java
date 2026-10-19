/**
 * <a href="https://www.openolat.org">
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
 * frentix GmbH, https://www.frentix.com
 * <p>
 */
package org.olat.mcp.ui;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.mcp.McpTokenCreation;

/**
 * Shows the plaintext of a new token once, with the Claude Code command that
 * contains it. The plaintext lives only in the context of this controller,
 * the parent disposes it when the dialog closes.
 *
 * Initial date: 2 Oct 2026<br>
 * @author gnaegi, gnaegi@frentix.com, https://www.frentix.com
 *
 */
public class McpTokenCreatedController extends BasicController {

	private final Link closeButton;

	public McpTokenCreatedController(UserRequest ureq, WindowControl wControl, McpTokenCreation creation) {
		super(ureq, wControl);

		VelocityContainer mainVC = createVelocityContainer("token_created");
		mainVC.contextPut("token", creation.plaintext());
		mainVC.contextPut("label", creation.token().label());
		mainVC.contextPut("command", McpUIHelper.getClaudeCodeCommand(creation.plaintext()));
		closeButton = LinkFactory.createButton("user.created.close", mainVC, this);
		closeButton.setPrimary(true);
		closeButton.setElementCssClass("o_sel_mcp_token_created_close");
		putInitialPanel(mainVC);
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		if(source == closeButton) {
			fireEvent(ureq, Event.DONE_EVENT);
		}
	}
}
