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
package org.olat.course.nodes.opencast.ui;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.winmgr.CommandFactory;
import org.olat.core.util.Formatter;
import org.olat.course.nodes.OpencastCourseNode;
import org.olat.course.run.userview.UserCourseEnvironment;
import org.olat.modules.ModuleConfiguration;
import org.olat.modules.opencast.OpencastEvent;
import org.olat.modules.opencast.OpencastModule;
import org.olat.modules.opencast.OpencastSeries;
import org.olat.modules.opencast.OpencastService;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 12 Aug 2020<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class OpencastDisplayController extends FormBasicController {
	
	private FormLink seriesButton;
	private FormLink eventButton;

	private final ModuleConfiguration config;
	private final String roles;
	
	@Autowired
	private OpencastModule opencastModule;
	@Autowired
	private OpencastService opencastService;

	public OpencastDisplayController(UserRequest ureq, WindowControl wControl, OpencastCourseNode courseNode,
			UserCourseEnvironment userCourseEnv) {
		super(ureq, wControl, "display");
		config = courseNode.getModuleConfiguration();
		roles = initRoles(userCourseEnv);
		
		initForm(ureq);
		doStartImmediately(ureq);
	}

	private String initRoles(UserCourseEnvironment userCourseEnv) {
		if (userCourseEnv.isAdmin()) {
			return opencastModule.getRolesAdmin();
		} else if (userCourseEnv.isCoach()) {
			return opencastModule.getRolesCoach();
		}
		return opencastModule.getRolesParticipant();
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		if (formLayout instanceof FormLayoutContainer) {
			FormLayoutContainer layoutCont = (FormLayoutContainer)formLayout;
			
			String title = config.getStringValue(OpencastCourseNode.CONFIG_TITLE);
			layoutCont.contextPut("title", title);
			
			if (config.has(OpencastCourseNode.CONFIG_SERIES_IDENTIFIER)) {
				String seriesIdentifier = config.getStringValue(OpencastCourseNode.CONFIG_SERIES_IDENTIFIER);
				OpencastSeries series = opencastService.getSeries(seriesIdentifier);
				if (series != null) {
					layoutCont.contextPut("title", series.getTitle());
					seriesButton = uifactory.addFormLink("start.series", formLayout, Link.BUTTON_LARGE);
					seriesButton.setNewWindow(true, true);
					seriesButton.setUserObject(series);
				} else {
					layoutCont.contextPut("error", translate("error.series.not.found", new String[] {title}));
				}
			} else {
				String eventIdentifier = config.getStringValue(OpencastCourseNode.CONFIG_EVENT_IDENTIFIER);
				OpencastEvent event = opencastService.getEvent(eventIdentifier);
				if (event != null) {
					layoutCont.contextPut("title", event.getTitle());
					String start = Formatter.getInstance(getLocale()).formatDateAndTime(event.getStart());
					layoutCont.contextPut("start", start);
					layoutCont.contextPut("creator", event.getCreator());
					
					eventButton = uifactory.addFormLink("start.event", formLayout, Link.BUTTON_LARGE);
					eventButton.setNewWindow(true, true);
					eventButton.setUserObject(event);
				} else {
					layoutCont.contextPut("error", translate("error.event.not.found", new String[] {title}));
				}
			}
		}
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if (source == seriesButton) {
			OpencastSeries opencastSeries = (OpencastSeries)seriesButton.getUserObject();
			doStartSeries(ureq, opencastSeries);
		} else if (source == eventButton) {
			OpencastEvent opencastEvent = (OpencastEvent)eventButton.getUserObject();
			doStartEvent(ureq, opencastEvent);
		}
		super.formInnerEvent(ureq, source, event);
	}

	private void doStartImmediately(UserRequest ureq) {
		if (opencastModule.isStartImmediately()) {
			if (seriesButton != null) {
				OpencastSeries opencastSeries = (OpencastSeries)seriesButton.getUserObject();
				doStartSeries(ureq, opencastSeries);
			} else if (eventButton != null) {
				OpencastEvent opencastEvent = (OpencastEvent)eventButton.getUserObject();
				doStartEvent(ureq, opencastEvent);
			}
		}
	}

	private void doStartSeries(UserRequest ureq, OpencastSeries opencastSeries) {
		String url = opencastService.getLtiSeriesMapperUrl(ureq.getUserSession(), opencastSeries, roles);
		getWindowControl().getWindowBackOffice().sendCommandTo(CommandFactory.createNewWindowRedirectTo(url));
	}

	private void doStartEvent(UserRequest ureq, OpencastEvent opencastEvent) {
		String url = opencastService.getLtiEventMapperUrl(ureq.getUserSession(), opencastEvent.getIdentifier(), roles);
		getWindowControl().getWindowBackOffice().sendCommandTo(CommandFactory.createNewWindowRedirectTo(url));
	}

	@Override
	protected void formOK(UserRequest ureq) {
		//
	}

	@Override
	protected void doDispose() {
		//
	}

}
