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
package org.olat.gui.demo.guidemo;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.emptystate.EmptyState;
import org.olat.core.gui.components.emptystate.EmptyStateConfig;
import org.olat.core.gui.components.emptystate.EmptyStateFactory;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;

/**
 * Entry point for the GUI demo of empty states.
 * Delegates to EmptyStateMainController which provides the full demo layout.
 */
public class GuiDemoEmptyStateController extends BasicController {

	VelocityContainer mainVC;

	public GuiDemoEmptyStateController(UserRequest ureq, WindowControl wControl) {
		super(ureq, wControl);

		mainVC = createVelocityContainer("guidemo-empty-state");

		EmptyStateConfig emptyStateConfig = EmptyStateConfig.builder()
				.withIconCss("o_icon_globe")
				.withMessageI18nKey("empty.state.message.nothing")
				.withHintI18nKey("empty.state.hint.additional")
				.withHelp(translate("empty.state.help.additional"), "release_notes/")
				.build();
		EmptyState emptyState = EmptyStateFactory.create("emptyState", mainVC, this, emptyStateConfig);
		emptyState.setTranslator(getTranslator());
		
		putInitialPanel(mainVC);
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		//
	}
}