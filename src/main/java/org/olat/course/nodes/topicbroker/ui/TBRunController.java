/**
 * <a href="https://www.openolat.org">
 * OpenOLAT - Online Learning and Training</a><br>
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); <br>
 * you may not use this file except in compliance with the License.<br>
 * You may obtain a copy of the License at the
 * <a href="https://www.apache.org/licenses/LICENSE-2.0">Apache homepage</a>
 * <p>
 * Unless required by applicable law or agreed to in writing,<br>
 * software distributed under the License is distributed on an "AS IS" BASIS, <br>
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
 * See the License for the specific language governing permissions and <br>
 * limitations under the License.
 * <p>
 * Initial code contributed and copyrighted by<br>
 * frentix GmbH, https://www.frentix.com
 * <p>
 */
package org.olat.course.nodes.topicbroker.ui;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.course.nodes.TopicBrokerCourseNode;
import org.olat.course.run.userview.UserCourseEnvironment;
import org.olat.modules.topicbroker.TBBroker;
import org.olat.modules.topicbroker.TopicBrokerService;
import org.olat.modules.topicbroker.ui.TBSelectionController;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 27 May 2024<br>
 * @author uhensler, urs.hensler@frentix.com, https://www.frentix.com
 *
 */
public class TBRunController extends BasicController {
	
	private final TBSelectionController selectionCtrl;

	@Autowired
	private TopicBrokerService topicBrokerService;

	public TBRunController(UserRequest ureq, WindowControl wControl, TopicBrokerCourseNode courseNode, UserCourseEnvironment userCourseEnv) {
		super(ureq, wControl);
		VelocityContainer mainVC = createVelocityContainer("run");
		putInitialPanel(mainVC);
		
		TBBroker broker = topicBrokerService.getOrCreateBroker(getIdentity(),
				userCourseEnv.getCourseEnvironment().getCourseGroupManager().getCourseEntry(), courseNode.getIdent());
		selectionCtrl = new TBSelectionController(ureq, wControl, broker);
		listenTo(selectionCtrl);
		mainVC.put("selection", selectionCtrl.getInitialComponent());
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		//
	}

}
