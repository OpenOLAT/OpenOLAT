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
package org.olat.course.nodes.survey;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;

/**
 * 
 * Initial date: 03.05.2018<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class SurveyRunCommandsController extends BasicController {
	
	static final String EVENT_DELETE_ALL_DATA = "delete.all.data";
	
	private final Link deleteAllDataLink;

	protected SurveyRunCommandsController(UserRequest ureq, WindowControl wControl) {
		super(ureq, wControl);
		
		VelocityContainer mainVC = createVelocityContainer("run_commands");
		deleteAllDataLink = LinkFactory.createLink("run.command.delete.data.all", mainVC, this);
		deleteAllDataLink.setIconLeftCSS("o_icon o_icon-fw o_icon_surv_delete_all_data");
		putInitialPanel(mainVC);
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		if (deleteAllDataLink == source) {
			fireEvent(ureq, new Event(EVENT_DELETE_ALL_DATA));
		}
	}

	@Override
	protected void doDispose() {
		//
	}

}
