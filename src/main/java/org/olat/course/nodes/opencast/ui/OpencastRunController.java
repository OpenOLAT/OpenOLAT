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

import java.util.stream.Collectors;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.htmlheader.jscss.JSAndCSSComponent;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
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
 * Initial date: 13 Aug 2020<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class OpencastRunController extends BasicController {
	
	private final ModuleConfiguration config;
	private final String roles;
	
	@Autowired
	private OpencastModule opencastModule;
	@Autowired
	private OpencastService opencastService;

	public OpencastRunController(UserRequest ureq, WindowControl wControl, OpencastCourseNode courseNode, UserCourseEnvironment userCourseEnv) {
		super(ureq, wControl);
		config = courseNode.getModuleConfiguration();
		roles = initRoles(userCourseEnv);
		
		VelocityContainer mainVC = createVelocityContainer("run");
		
		String url = null;
		String title = config.getStringValue(OpencastCourseNode.CONFIG_TITLE);
		mainVC.contextPut("title", title);
		
		if (config.has(OpencastCourseNode.CONFIG_SERIES_IDENTIFIER)) {
			String seriesIdentifier = config.getStringValue(OpencastCourseNode.CONFIG_SERIES_IDENTIFIER);
			OpencastSeries series = opencastService.getSeries(seriesIdentifier);
			if (series != null) {
				mainVC.contextPut("title", series.getTitle());
				url = opencastService.getLtiSeriesMapperUrl(ureq.getUserSession(), series, roles);
			} else {
				mainVC.contextPut("error", translate("error.series.not.found", new String[] {title}));
			}
		} else {
			String eventIdentifier = config.getStringValue(OpencastCourseNode.CONFIG_EVENT_IDENTIFIER);
			OpencastEvent event = opencastService.getEvent(eventIdentifier);
			if (event != null) {
				mainVC.contextPut("title", event.getTitle());
				String start = Formatter.getInstance(getLocale()).formatDateAndTime(event.getStart());
				mainVC.contextPut("start", start);
				String presenter = event.getPresenters().stream().collect(Collectors.joining(", "));
				mainVC.contextPut("presenter", presenter);
				url = opencastService.getLtiEventMapperUrl(ureq.getUserSession(), event.getIdentifier(), roles);
			} else {
				mainVC.contextPut("error", translate("error.event.not.found", new String[] {title}));
			}
		}
		
		if (url != null) {
			mainVC.contextPut("mapperUri", url + "/");
			
			JSAndCSSComponent js = new JSAndCSSComponent("js", new String[] { "js/openolat/iFrameResizerHelper.js" }, null);
			mainVC.put("js", js);
		}
		
		putInitialPanel(mainVC);
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
	protected void event(UserRequest ureq, Component source, Event event) {
		//
	}

	@Override
	protected void doDispose() {
		//
	}

}
