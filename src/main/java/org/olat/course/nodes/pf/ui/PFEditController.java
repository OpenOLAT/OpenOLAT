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
package org.olat.course.nodes.pf.ui;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.tabbedpane.TabbedPane;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.ControllerEventListener;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.tabbable.ActivateableTabbableDefaultController;
import org.olat.course.ICourse;
import org.olat.course.assessment.AssessmentHelper;
import org.olat.course.condition.Condition;
import org.olat.course.condition.ConditionEditController;
import org.olat.course.editor.NodeEditController;
import org.olat.course.nodes.PFCourseNode;
import org.olat.course.run.userview.UserCourseEnvironment;
import org.olat.course.tree.CourseEditorTreeModel;
/**
*
* @author Fabian Kiefer, fabian.kiefer@frentix.com, http://www.frentix.com
*
*/
public class PFEditController extends ActivateableTabbableDefaultController implements ControllerEventListener {
	
	private static final String PANE_TAB_CONFIGURATION = "pane.tab.configuration";
	private static final String PANE_TAB_ACCESSIBILITY = "pane.tab.accessibility";	
	private static final String[] paneKeys = { PANE_TAB_CONFIGURATION, PANE_TAB_ACCESSIBILITY };

	private VelocityContainer configVC;
	private PFEditFormController modConfigCtr;
	private TabbedPane myTabbedPane;
	private ConditionEditController accessibilityCondCtr;
	private PFCourseNode pfNode;

	
	public PFEditController(UserRequest ureq, WindowControl wControl, 
			PFCourseNode pfNode, ICourse course, UserCourseEnvironment euce) {
		super(ureq, wControl);
		this.pfNode = pfNode;
		
		configVC = createVelocityContainer("edit");
		// Accessibility precondition
		CourseEditorTreeModel editorModel = course.getEditorTreeModel();
		Condition accessCondition = pfNode.getPreConditionAccess();
		accessibilityCondCtr = new ConditionEditController(ureq, getWindowControl(), euce, accessCondition,
				AssessmentHelper.getAssessableNodes(editorModel, pfNode));		
		listenTo(accessibilityCondCtr);
		
		modConfigCtr = new PFEditFormController(ureq, wControl, pfNode);
		listenTo(modConfigCtr);
		configVC.put("sfeditform", modConfigCtr.getInitialComponent());		
		
	}

	@Override
	public void addTabs(TabbedPane tabbedPane) {
		myTabbedPane = tabbedPane;
		tabbedPane.addTab(translate(PANE_TAB_ACCESSIBILITY), accessibilityCondCtr.getWrappedDefaultAccessConditionVC(translate("condition.accessibility.title")));
		tabbedPane.addTab(translate(PANE_TAB_CONFIGURATION), configVC);
	}

	@Override
	public String[] getPaneKeys() {
		return paneKeys;
	}

	@Override
	public TabbedPane getTabbedPane() {
		return myTabbedPane;
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		//
	}
	
	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if (source == accessibilityCondCtr) {
			if (event == Event.CHANGED_EVENT) {
				Condition cond = accessibilityCondCtr.getCondition();
				pfNode.setPreConditionAccess(cond);
				fireEvent(ureq, NodeEditController.NODECONFIG_CHANGED_EVENT);
			}
		} else 	if (source == modConfigCtr) {
			if (Event.DONE_EVENT.equals(event)) {
				fireEvent(ureq, NodeEditController.NODECONFIG_CHANGED_EVENT);	
			}
		}
		super.event(ureq, source, event);
	}

	@Override
	protected void doDispose() {
		//
	}

}
