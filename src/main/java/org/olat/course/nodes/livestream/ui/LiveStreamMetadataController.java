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
package org.olat.course.nodes.livestream.ui;

import java.util.Date;
import java.util.Locale;

import org.apache.commons.lang.time.DateUtils;
import org.olat.commons.calendar.CalendarManager;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.util.Formatter;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
import org.olat.course.nodes.livestream.LiveStreamEvent;

/**
 * 
 * Initial date: 29 May 2019<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class LiveStreamMetadataController extends BasicController {
	
	private final VelocityContainer mainVC;
	
	private LiveStreamEvent currentEvent;

	protected LiveStreamMetadataController(UserRequest ureq, WindowControl wControl) {
		super(ureq, wControl, Util.createPackageTranslator(CalendarManager.class, ureq.getLocale()));
		mainVC = createVelocityContainer("metadata");
		updateUI(null);
		putInitialPanel(mainVC);
	}
	
	public void setEvent(LiveStreamEvent event) {
		updateUI(event);
	}

	private void updateUI(LiveStreamEvent event) {
		if (event == null || !event.equals(currentEvent)) {
			currentEvent = event;
			if (event != null) {
				mainVC.contextPut("id", event.getId());
				mainVC.contextPut("title", event.getSubject());
				addDateToMainVC(event);
				StringBuilder description = Formatter.stripTabsAndReturns(Formatter.formatURLsAsLinks(event.getDescription(), true));
				mainVC.contextPut("description", description.toString());
				if (StringHelper.containsNonWhitespace(event.getLocation())) {
					mainVC.contextPut("location", event.getLocation());
				}
			} else {
				mainVC.contextRemove("id");
			}
		}
	}
	
	private void addDateToMainVC(LiveStreamEvent calEvent) {
		Locale locale = getLocale();
		Date begin = calEvent.getBegin();
		Date end = calEvent.getEnd();
		
		boolean sameDay = DateUtils.isSameDay(begin, end);
		if (sameDay) {
			StringBuilder dateSb = new StringBuilder();
			dateSb.append(StringHelper.formatLocaleDateFull(begin.getTime(), locale));
			mainVC.contextPut("date", dateSb.toString());
			if (!calEvent.isAllDayEvent()) {
				StringBuilder timeSb = new StringBuilder();
				timeSb.append(StringHelper.formatLocaleTime(begin.getTime(), locale));
				timeSb.append(" - ");
				timeSb.append(StringHelper.formatLocaleTime(end.getTime(), locale));
				mainVC.contextPut("time", timeSb.toString());
			}
		} else {
			StringBuilder dateSb = new StringBuilder();
			dateSb.append(StringHelper.formatLocaleDateFull(begin.getTime(), locale));
			if (!calEvent.isAllDayEvent()) {
				dateSb.append(" ");
				dateSb.append(StringHelper.formatLocaleTime(begin.getTime(), locale));
			}
			dateSb.append(" - ");
			dateSb.append(StringHelper.formatLocaleDateFull(end.getTime(), locale));
			if (!calEvent.isAllDayEvent()) {
				dateSb.append(" ");
				dateSb.append(StringHelper.formatLocaleTime(end.getTime(), locale));
			}
			mainVC.contextPut("date", dateSb.toString());
		}
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		//
	}

	@Override
	protected void doDispose() {
		//
	}

}
