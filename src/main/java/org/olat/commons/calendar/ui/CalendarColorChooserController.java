/**
* OLAT - Online Learning and Training<br>
* http://www.olat.org
* <p>
* Licensed under the Apache License, Version 2.0 (the "License"); <br>
* you may not use this file except in compliance with the License.<br>
* You may obtain a copy of the License at
* <p>
* http://www.apache.org/licenses/LICENSE-2.0
* <p>
* Unless required by applicable law or agreed to in writing,<br>
* software distributed under the License is distributed on an "AS IS" BASIS, <br>
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
* See the License for the specific language governing permissions and <br>
* limitations under the License.
* <p>
* Copyright (c) since 2004 at Multimedia- & E-Learning Services (MELS),<br>
* University of Zurich, Switzerland.
* <hr>
* <a href="http://www.openolat.org">
* OpenOLAT - Online Learning and Training</a><br>
* This file has been modified by the OpenOLAT community. Changes are licensed
* under the Apache 2.0 license as the original file.
*/

package org.olat.commons.calendar.ui;

import org.olat.commons.calendar.ui.components.KalendarRenderWrapper;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;


public class CalendarColorChooserController extends BasicController {

	private VelocityContainer colorVC;
	private String choosenColor;
	private final KalendarRenderWrapper calendarWrapper;

	private static final String[] colors = new String[]{
		"o_cal_green", "o_cal_blue", "o_cal_orange",
		"o_cal_yellow", "o_cal_red", "o_cal_rebeccapurple", "o_cal_grey"
	};

	public CalendarColorChooserController(UserRequest ureq, WindowControl wControl,
			KalendarRenderWrapper calendarWrapper, String currentCssSelection) {
		super(ureq, wControl);
		
		this.calendarWrapper = calendarWrapper;

		colorVC = createVelocityContainer("calEdit", "calColor");
		
		for(String color:colors) {
			addColor(color, currentCssSelection);
		}
		putInitialPanel(colorVC);
	}
	
	private void addColor(String css, String currentCssSelection) {
		Link colorLink = LinkFactory.createCustomLink(css, "selc", "", Link.NONTRANSLATED, colorVC, this);
		if (currentCssSelection.equals(css)){
			colorLink.setIconLeftCSS("o_icon o_cal_colorchooser_selected");
		} else {
			colorLink.setIconLeftCSS("o_icon");
		}
		colorLink.setUserObject(css);
	}
	
	@Override
	public void event(UserRequest ureq, Component source, Event event) {
		if (source instanceof Link){
			Link colorLink = (Link) source;
			choosenColor = (String)colorLink.getUserObject();
			fireEvent(ureq, Event.DONE_EVENT);
		}
	}

	public String getChoosenColor() {
		return choosenColor;
	}
	
	public KalendarRenderWrapper getCalendarWrapper() {
		return calendarWrapper;
	}

	@Override
	protected void doDispose() {
		// nothing to dispose
	}
}