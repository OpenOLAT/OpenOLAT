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
package org.olat.course.nodes.edusharing.ui;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.course.nodes.EdusharingCourseNode;
import org.olat.modules.ModuleConfiguration;

/**
 * 
 * Initial date: 20 May 2020<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class EdusharingRunController extends BasicController {

	public EdusharingRunController(UserRequest ureq, WindowControl wControl, EdusharingCourseNode courseNode) {
		super(ureq, wControl);
		ModuleConfiguration config = courseNode.getModuleConfiguration();
		
		VelocityContainer mainVC = createVelocityContainer("run");
		
		mainVC.contextPut("identifier", config.getStringValue(EdusharingCourseNode.CONFIG_IDENTIFIER));
		
		mainVC.contextPut("objecturl", config.getStringValue(EdusharingCourseNode.CONFIG_ES_OBJECT_URL));
		
		String versionConfig = config.getStringValue(EdusharingCourseNode.CONFIG_VERSION);
		String version = EdusharingCourseNode.CONFIG_VERSION_VALUE_CURRENT.equals(versionConfig)
				? config.getStringValue(EdusharingCourseNode.CONFIG_ES_WINDOW_VERISON)
				: "0";
		mainVC.contextPut("version", version);
		
		String showLicense = config.getBooleanSafe(EdusharingCourseNode.CONFIG_SHOW_LICENSE)? "show": "hide";
		mainVC.contextPut("showLicense", showLicense);
		
		String showMetadata = config.getBooleanSafe(EdusharingCourseNode.CONFIG_SHOW_METADATA)? "show": "hide";
		mainVC.contextPut("showMetadata", showMetadata);
		
		putInitialPanel(mainVC);
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		//
	}
}
