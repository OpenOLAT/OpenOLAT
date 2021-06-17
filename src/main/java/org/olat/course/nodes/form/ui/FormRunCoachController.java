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
package org.olat.course.nodes.form.ui;

import java.util.List;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.stack.TooledStackedPanel;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.gui.control.generic.dtabs.Activateable2;
import org.olat.core.id.context.ContextEntry;
import org.olat.core.id.context.StateEntry;
import org.olat.course.nodes.FormCourseNode;
import org.olat.course.nodes.form.FormSecurityCallback;
import org.olat.course.run.userview.UserCourseEnvironment;

/**
 * 
 * Initial date: 26 Apr 2021<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class FormRunCoachController extends BasicController implements Activateable2 {
	
	private final FormParticipationListController participationCtrl;

	public FormRunCoachController(UserRequest ureq, WindowControl wControl, FormCourseNode formCourseNode,
			UserCourseEnvironment caochCourseEnv, FormSecurityCallback secCallback) {
		super(ureq, wControl);
		
		TooledStackedPanel stackPanel = new TooledStackedPanel("coachStackPanel", getTranslator(), this);
		stackPanel.setToolbarAutoEnabled(false);
		stackPanel.setToolbarEnabled(false);
		stackPanel.setShowCloseLink(true, false);
		stackPanel.setCssClass("o_identity_list_stack");
		putInitialPanel(stackPanel);
		
		participationCtrl = new FormParticipationListController(ureq, wControl, stackPanel, formCourseNode, caochCourseEnv, secCallback);
		listenTo(participationCtrl);
		participationCtrl.activate(ureq, null, null);
		
		stackPanel.pushController(translate("breadcrumb.users"), participationCtrl);
	}

	@Override
	public void activate(UserRequest ureq, List<ContextEntry> entries, StateEntry state) {
		participationCtrl.activate(ureq, entries, state);
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
