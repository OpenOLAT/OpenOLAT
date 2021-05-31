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

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.util.StringHelper;


public class CalendarColorChooserController extends BasicController {

	private String choosenColor;
	private CalendarPersonalConfigurationRow row;

	private static final String[] colors = new String[]{
		"o_cal_green", "o_cal_lime", "o_cal_blue", "o_cal_orange", "o_cal_fuchsia",
		"o_cal_yellow", "o_cal_red", "o_cal_rebeccapurple", "o_cal_navy", "o_cal_olive",
		"o_cal_maroon", "o_cal_grey"
	};
	
	public static boolean colorExists(String color) {
		if (StringHelper.containsNonWhitespace(color)) {
			for (String colorClass : colors) {
				if (colorClass.equalsIgnoreCase(color)) {
					return true;
				}
			}
		}
		return false;
	}

	public CalendarColorChooserController(UserRequest ureq, WindowControl wControl,
			CalendarPersonalConfigurationRow row) {
		super(ureq, wControl);
		this.row = row;
		init(row.getCssClass());
	}

	public CalendarColorChooserController(UserRequest ureq, WindowControl wControl, String currentColor) {
		super(ureq, wControl);
		init(currentColor);
	}
	
	private void init(String currentColor) {
		VelocityContainer colorVC = createVelocityContainer("calEdit", "calColor");
		for(String color:colors) {
			addColor(color, currentColor, colorVC);
		}
		putInitialPanel(colorVC);
	}
	
	private void addColor(String css, String currentCssSelection, VelocityContainer colorVC) {
		Link colorLink = LinkFactory.createCustomLink(css, "selc", "", Link.NONTRANSLATED, colorVC, this);
		if (css.equals(currentCssSelection)){
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
	
	public CalendarPersonalConfigurationRow getRow() {
		return row;
	}

	@Override
	protected void doDispose() {
		// nothing to dispose
	}
}