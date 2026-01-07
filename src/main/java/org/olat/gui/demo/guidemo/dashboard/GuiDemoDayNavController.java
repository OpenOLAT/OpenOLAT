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

import java.util.Date;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.daynav.DayNavComponent;
import org.olat.core.gui.components.daynav.DayNavFactory;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.util.DateUtils;
import org.olat.core.util.Formatter;

/**
 * 
 * Initial date: Jan 6, 2026<br>
 * @author uhensler, urs.hensler@frentix.com, https://www.frentix.com
 *
 */
public class GuiDemoDayNavController extends BasicController {

	private VelocityContainer mainVC;
	private DayNavComponent dayNav1;
	private DayNavComponent dayNav2;
	private DayNavComponent dayNav3;
	
	public GuiDemoDayNavController(UserRequest ureq, WindowControl wControl) {
		super(ureq, wControl);
		
		mainVC = createVelocityContainer("day_nav");
		putInitialPanel(mainVC);
		
		dayNav1 = DayNavFactory.createComponent("day.nav.1", mainVC);
		
		dayNav2 = DayNavFactory.createComponent("day.nav.2", mainVC);
		dayNav2.setStartDate(DateUtils.addDays(new Date(), -1));
		dayNav2.setSelectedDate(DateUtils.addDays(new Date(), 1));
		
		dayNav3 = DayNavFactory.createComponent("day.nav.3", mainVC);
		dayNav3.setStartDate(DateUtils.addDays(new Date(), 100));
		dayNav3.setSelectedDate(DateUtils.addDays(new Date(), 105));
	}
	
	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		if (source == dayNav1) {
			doShowDay(dayNav1.getSelectedDate());
		} else if (source == dayNav2) {
			doShowDay(dayNav2.getSelectedDate());
		} else if (source == dayNav3) {
			doShowDay(dayNav3.getSelectedDate());
		}
	}
	
	private void doShowDay(Date date) {
		showInfo("show.label.value", new String[] { Formatter.getInstance(getLocale()).formatDate(date) } );
	}

}
