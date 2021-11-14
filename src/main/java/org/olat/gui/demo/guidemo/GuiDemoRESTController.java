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
package org.olat.gui.demo.guidemo;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.htmlheader.jscss.JSAndCSSComponent;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.restapi.RestModule;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * The purpose of this GUI demo is to show how you can add a custom JS library and do some REST queries via JavaScript
 * 
 * Initial date: 10.11.2017<br>
 * @author gnaegi, gnaegi@frentix.com, http://www.frentix.com
 *
 */
public class GuiDemoRESTController extends BasicController {
	@Autowired
	private RestModule restModule;
	
	private VelocityContainer vcMain;

	public GuiDemoRESTController(UserRequest ureq, WindowControl wControl) {
		super(ureq, wControl);
		vcMain = createVelocityContainer("guidemo-rest");		
		putInitialPanel(vcMain);
		
		// Push module state to velocty
		vcMain.contextPut("restEnabled", restModule.isEnabled());

		// Add the http://listjs.com jQuery plugin from a CDN.
		//
		// The JS and CSS component makes sure that the JS and CSS required by
		// this view is always in the DOM when the view is visible. The JS code
		// is loaded on-demand on first use.
		// If you want to load it from within the project, simply use a relative path notation like in this example: 
		// JSAndCSSComponent js = new JSAndCSSComponent("js", new String[] { "js/jquery/list.js/1.5.0/list.min.js" }, null);
		JSAndCSSComponent js = new JSAndCSSComponent("js", new String[] { "https://cdnjs.cloudflare.com/ajax/libs/list.js/1.5.0/list.min.js" }, null);
		vcMain.put("jsAdder", js);
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		// nothing to catch
	}
}
