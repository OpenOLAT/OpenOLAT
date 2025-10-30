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
package org.olat.gui.demo.guidemo.dashboard;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;

/**
 * 
 * Initial date: Oct 29, 2025<br>
 * @author uhensler, urs.hensler@frentix.com, https://www.frentix.com
 *
 */
public class GuiDemoTableWidgetsController extends BasicController {

	protected GuiDemoTableWidgetsController(UserRequest ureq, WindowControl wControl) {
		super(ureq, wControl);
		VelocityContainer mainVC = createVelocityContainer("tables");
		putInitialPanel(mainVC);
		
		GuiDemoTableWidgetController table1Ctrl = new GuiDemoTableWidgetController(ureq, wControl,
				"<i class=\"o_icon o_icon_table\"> </i> " + translate("table.title"),
				true, false);
		listenTo(table1Ctrl);
		mainVC.put("table1", table1Ctrl.getInitialComponent());
		
		GuiDemoTableWidgetController table2Ctrl = new GuiDemoTableWidgetController(ureq, wControl,
				"<i class=\"o_icon o_icon_table\"> </i> " + translate("table.title.without.header"),
				false, false);
		listenTo(table2Ctrl);
		mainVC.put("table2", table2Ctrl.getInitialComponent());
		
		GuiDemoTableWidgetController table3Ctrl = new GuiDemoTableWidgetController(ureq, wControl,
				"<i class=\"o_icon o_icon_table\"> </i> " + translate("table.title.with.cell.links"),
				true, true);
		listenTo(table3Ctrl);
		mainVC.put("table3", table3Ctrl.getInitialComponent());
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		//
	}

}
