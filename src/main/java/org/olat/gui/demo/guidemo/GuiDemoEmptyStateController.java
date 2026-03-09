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

	private GuiDemoEmptyStateFormController emptyStateFormCtrl;

	public GuiDemoEmptyStateController(UserRequest ureq, WindowControl wControl) {
		super(ureq, wControl);

		mainVC = createVelocityContainer("guidemo-empty-state");

		initStandard1();
		initStandard2();
		initStandardForm3(ureq);

		putInitialPanel(mainVC);
	}

	private void initStandard1() {
		EmptyStateConfig emptyStateConfig = EmptyStateConfig.builder()
				.withIconCss("o_icon_globe")
				.withMessageI18nKey("empty.state.message.nothing")
				.withHintI18nKey("empty.state.hint.additional")
				.withHelp(translate("empty.state.help.additional"), "release_notes/")
				.withPrimaryButton("o_icon_bolt_lightning", "empty.state.button.primary.action", null)
				.withSecondaryButton("o_icon_bolt_lightning", "empty.state.button.secondary.action.1", null, "#1")
				.withSecondaryButton("o_icon_bolt_lightning", "empty.state.button.secondary.action.2", null,"#2")
				.withSecondaryButton("o_icon_bolt_lightning", "empty.state.button.secondary.action.3", null,"#3")
				.build();
		EmptyState emptyState = EmptyStateFactory.create("emptyStateStandard1", mainVC, this, emptyStateConfig);
		emptyState.setTranslator(getTranslator());
	}

	private void initStandardForm3(UserRequest ureq) {
		emptyStateFormCtrl = new GuiDemoEmptyStateFormController(ureq, getWindowControl());
		listenTo(emptyStateFormCtrl);
		mainVC.put("emptyStateForm", emptyStateFormCtrl.getInitialComponent());
	}

	private void initStandard2() {
		EmptyStateConfig emptyStateConfig = EmptyStateConfig.builder()
				.withIconCss("o_icon_search")
				.withMessageI18nKey("empty.state.message.learning")
				.withHintI18nKey("empty.state.hint.modify")
				.build();
		EmptyState emptyState = EmptyStateFactory.create("emptyStateStandard2", mainVC, this, emptyStateConfig);
		emptyState.setTranslator(getTranslator());
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		if (source instanceof EmptyState) {
			if (event instanceof EmptyState.PrimaryEvent) {
				showInfo("empty.state.event.primary");
			} else if (event instanceof EmptyState.SecondaryEvent secondaryEvent) {
				showInfo("empty.state.event.secondary", secondaryEvent.getAction());
			}
		}
	}
}