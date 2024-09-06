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
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.course.editor.NodeEditController;
import org.olat.course.groupsandrights.CourseGroupManager;
import org.olat.course.noderight.ui.NodeRightsController;
import org.olat.course.nodes.TopicBrokerCourseNode;

/**
 * 
 * Initial date: 27 May 2024<br>
 * @author uhensler, urs.hensler@frentix.com, https://www.frentix.com
 *
 */
public class TBConfigsController extends FormBasicController implements Controller {
	
	private TBConfigController configCtrl;
	private NodeRightsController nodeRightCtrl;

	private final TopicBrokerCourseNode courseNode;
	private final CourseGroupManager courseGroupManager;

	protected TBConfigsController(UserRequest ureq, WindowControl wControl, CourseGroupManager courseGroupManager,
			TopicBrokerCourseNode courseNode) {
		super(ureq, wControl, LAYOUT_BAREBONE);
		this.courseGroupManager = courseGroupManager;
		this.courseNode = courseNode;
		
		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		configCtrl = new TBConfigController(ureq, getWindowControl(), mainForm, courseGroupManager.getCourseEntry(), courseNode);
		listenTo(configCtrl);
		formLayout.add("configs", configCtrl.getInitialFormItem());
		
		nodeRightCtrl = new NodeRightsController(ureq, getWindowControl(), mainForm, courseGroupManager,
				TopicBrokerCourseNode.NODE_RIGHT_TYPES, courseNode.getModuleConfiguration(), null);
		listenTo(nodeRightCtrl);
		formLayout.add("rights", nodeRightCtrl.getInitialFormItem());
		
		FormLayoutContainer buttonsWrapperCont = FormLayoutContainer.createDefaultFormLayout("bottonsWrapper", getTranslator());
		buttonsWrapperCont.setElementCssClass("o_sel_tb_buttons o_block_top");
		buttonsWrapperCont.setRootForm(mainForm);
		formLayout.add(buttonsWrapperCont);
		FormLayoutContainer buttonCont = FormLayoutContainer.createButtonLayout("buttons", getTranslator());
		buttonCont.setRootForm(mainForm);
		buttonsWrapperCont.add(buttonCont);
		uifactory.addFormSubmitButton("save", "save", buttonCont);
	}

	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if (source == configCtrl) {
			if (event == NodeEditController.NODECONFIG_CHANGED_EVENT) {
				fireEvent(ureq, event);
			}
		}
		super.event(ureq, source, event);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		// See formOK() of the sub controllers.
	}

}
