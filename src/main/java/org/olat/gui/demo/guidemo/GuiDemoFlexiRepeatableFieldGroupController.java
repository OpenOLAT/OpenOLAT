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
import org.olat.core.gui.components.tabbedpane.TabbedPane;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.gui.demo.guidemo.GuiDemoFlexiRepeatableFieldGroupExampleController.Example;

/**
 * Initial date: 28 Oct 2026<br>
 * @author uhensler, urs.hensler@frentix.com, https://www.frentix.com
 */
public class GuiDemoFlexiRepeatableFieldGroupController extends BasicController {
	
	private Controller exampleCtrl;
	
	public GuiDemoFlexiRepeatableFieldGroupController(UserRequest ureq, WindowControl wControl) {
		super(ureq, wControl);
		TabbedPane tabPane = new TabbedPane("tabs", getLocale());
		for(Example example:Example.values()) {
			tabPane.addTab(ureq, translate("guidemo.rfg." + example.name() + ".tab"), null, uureq -> {
				removeAsListenerAndDispose(exampleCtrl);
				exampleCtrl = new GuiDemoFlexiRepeatableFieldGroupExampleController(uureq, getWindowControl(), example);
				listenTo(exampleCtrl);
				return exampleCtrl.getInitialComponent();
			}, true);
		}
		putInitialPanel(tabPane);
	}
	
	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		//
	}
	
}
