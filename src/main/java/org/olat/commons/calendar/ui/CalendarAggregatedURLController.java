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
package org.olat.commons.calendar.ui;

import org.olat.commons.calendar.CalendarManager;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;

/**
 * 
 * Initial date: 27.08.2015<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class CalendarAggregatedURLController extends BasicController {

	public CalendarAggregatedURLController(UserRequest ureq, WindowControl wControl, String icalFeedLink, String icalAggregatedFeedLink) {
		super(ureq, wControl);
		setTranslator(Util.createPackageTranslator(CalendarManager.class, getLocale(), getTranslator()));
		
		VelocityContainer mainVC = createVelocityContainer("calAggregatedFeed");
		if(StringHelper.containsNonWhitespace(icalFeedLink)) {
			mainVC.contextPut("icalFeedLink", icalFeedLink);
		}
		if(StringHelper.containsNonWhitespace(icalAggregatedFeedLink)) {
			mainVC.contextPut("icalAggregatedFeedLink", icalAggregatedFeedLink);
		}
		putInitialPanel(mainVC);
	}
	
	@Override
	public void event(UserRequest ureq, Component source, Event event) {
		//
	}

}
