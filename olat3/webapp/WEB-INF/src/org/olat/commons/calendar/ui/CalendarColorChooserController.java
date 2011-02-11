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
 * <p>
 */

package org.olat.commons.calendar.ui;

import java.util.HashMap;
import java.util.Locale;

import org.olat.commons.calendar.CalendarManager;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.DefaultController;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.translator.PackageTranslator;
import org.olat.core.gui.translator.Translator;
import org.olat.core.util.Util;


public class CalendarColorChooserController extends DefaultController {

	private static final String SELECTED_COLOR_CSS = "o_cal_colorchooser_selected";
	private static final String PACKAGE = Util.getPackageName(CalendarManager.class);
	private static final String VELOCITY_ROOT = Util.getPackageVelocityRoot(CalendarManager.class);

	private Translator translator;
	private VelocityContainer colorVC;
	private String choosenColor;
	private HashMap colorLinks;
	private Link cancelButton;

	public CalendarColorChooserController(Locale locale, WindowControl wControl, String currentCssSelection) {
		super(wControl);
		translator = new PackageTranslator(PACKAGE, locale);
		
		colorVC = new VelocityContainer("calEdit", VELOCITY_ROOT + "/calColor.html", translator, this);
		cancelButton = LinkFactory.createButton("cancel", colorVC, this);
		
		colorLinks = new HashMap();
		Link greenLink = LinkFactory.createCustomLink("greenLink", "selc", "", Link.NONTRANSLATED, colorVC, this);
		if (currentCssSelection.equals("o_cal_green")){
			greenLink.setCustomEnabledLinkCSS(SELECTED_COLOR_CSS);
			greenLink.setCustomDisabledLinkCSS(SELECTED_COLOR_CSS);
		}
		Link blueLink = LinkFactory.createCustomLink("blueLink", "selc", "", Link.NONTRANSLATED, colorVC, this);
		if (currentCssSelection.equals("o_cal_blue")){
			blueLink.setCustomEnabledLinkCSS(SELECTED_COLOR_CSS);
			blueLink.setCustomDisabledLinkCSS(SELECTED_COLOR_CSS);
		}
		Link orangeLink = LinkFactory.createCustomLink("orangeLink", "selc", "", Link.NONTRANSLATED, colorVC, this);
		if (currentCssSelection.equals("o_cal_orange")){
			orangeLink.setCustomEnabledLinkCSS(SELECTED_COLOR_CSS);
			orangeLink.setCustomDisabledLinkCSS(SELECTED_COLOR_CSS);
		}
		Link yellowLink = LinkFactory.createCustomLink("yellowLink", "selc", "", Link.NONTRANSLATED, colorVC, this);
		if (currentCssSelection.equals("o_cal_yellow")){
			yellowLink.setCustomEnabledLinkCSS(SELECTED_COLOR_CSS);
			yellowLink.setCustomDisabledLinkCSS(SELECTED_COLOR_CSS);
		}
		Link redLink = LinkFactory.createCustomLink("redLink", "selc", "", Link.NONTRANSLATED, colorVC, this);
		if (currentCssSelection.equals("o_cal_red")){
			redLink.setCustomEnabledLinkCSS(SELECTED_COLOR_CSS);
			redLink.setCustomDisabledLinkCSS(SELECTED_COLOR_CSS);
		}
		Link greyLink = LinkFactory.createCustomLink("greyLink", "selc", "", Link.NONTRANSLATED, colorVC, this);
		if (currentCssSelection.equals("o_cal_grey")){
			greyLink.setCustomEnabledLinkCSS(SELECTED_COLOR_CSS);
			greyLink.setCustomDisabledLinkCSS(SELECTED_COLOR_CSS);
		}
		
		colorLinks.put(greenLink, "o_cal_green");
		colorLinks.put(blueLink, "o_cal_blue");
		colorLinks.put(orangeLink,"o_cal_orange");
		colorLinks.put(yellowLink,"o_cal_yellow");
		colorLinks.put(redLink,"o_cal_red");
		colorLinks.put(greyLink,"o_cal_grey");

		setInitialComponent(colorVC);
	}
	
	public void event(UserRequest ureq, Component source, Event event) {
		if (source == cancelButton) {
			fireEvent(ureq, Event.CANCELLED_EVENT);
		} else if (colorLinks.containsKey(source)){
			choosenColor = (String)colorLinks.get(source);
			Link colorLink = (Link) source;
			colorLink.setCustomEnabledLinkCSS(choosenColor);
			colorLink.setCustomDisabledLinkCSS(choosenColor);
			fireEvent(ureq, Event.DONE_EVENT);
		}
	}

	public String getChoosenColor() {
		return choosenColor;
	}
	
	protected void doDispose() {
		// nothing to dispose
	}

}
