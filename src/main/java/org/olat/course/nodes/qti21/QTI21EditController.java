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
package org.olat.course.nodes.qti21;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.stack.BreadcrumbPanel;
import org.olat.core.gui.components.tabbedpane.TabbedPane;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.tabbable.ActivateableTabbableDefaultController;
import org.olat.course.ICourse;
import org.olat.course.assessment.AssessmentHelper;
import org.olat.course.condition.Condition;
import org.olat.course.condition.ConditionEditController;
import org.olat.course.editor.NodeEditController;
import org.olat.course.nodes.QTI21AssessmentCourseNode;
import org.olat.course.run.userview.UserCourseEnvironment;
import org.olat.course.tree.CourseEditorTreeModel;

/**
 * 
 * Initial date: 19.05.2015<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class QTI21EditController extends ActivateableTabbableDefaultController {
	

	public static final String PANE_TAB_CONFIG_RE = "pane.tab.config.entry";
	public static final String PANE_TAB_ACCESSIBILITY = "pane.tab.accessibility";
	
	public static final String[] paneKeys = {
		PANE_TAB_ACCESSIBILITY, PANE_TAB_CONFIG_RE
	};
	
	private TabbedPane myTabbedPane;
	private final QTI21AssessmentCourseNode qtiNode;
	
	private final ConditionEditController accessibilityCondCtrl;
	private final QTI21ReferenceConfigurationController referenceCtrl;
	
	public QTI21EditController(UserRequest ureq, WindowControl wControl, BreadcrumbPanel stackPanel,
			QTI21AssessmentCourseNode qtiNode, ICourse course, UserCourseEnvironment euce) {
		super(ureq, wControl);
		
		this.qtiNode = qtiNode;
		
		// Accessibility precondition
		Condition accessCondition = qtiNode.getPreConditionAccess();
		CourseEditorTreeModel editorModel = course.getEditorTreeModel();
		accessibilityCondCtrl = new ConditionEditController(ureq, getWindowControl(),
				accessCondition, AssessmentHelper.getAssessableNodes(editorModel, qtiNode), euce);		
		listenTo(accessibilityCondCtrl);
		
		referenceCtrl = new QTI21ReferenceConfigurationController(ureq, getWindowControl(), stackPanel, qtiNode);
		listenTo(referenceCtrl);
	}
	
	@Override
	protected void doDispose() {
		//
	}

	@Override
	public void addTabs(TabbedPane tabbedPane) {
		myTabbedPane = tabbedPane;
		tabbedPane.addTab(translate(PANE_TAB_ACCESSIBILITY), accessibilityCondCtrl.getWrappedDefaultAccessConditionVC(translate("condition.accessibility.title")));
		tabbedPane.addTab(translate(PANE_TAB_CONFIG_RE), referenceCtrl.getInitialComponent());
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
		if (source == accessibilityCondCtrl) {
			if (event == Event.CHANGED_EVENT) {
				Condition cond = accessibilityCondCtrl.getCondition();
				qtiNode.setPreConditionAccess(cond);
				fireEvent(ureq, NodeEditController.NODECONFIG_CHANGED_EVENT);
			}
		} else if(referenceCtrl == source) {
			if (event == Event.CHANGED_EVENT) {
				fireEvent(ureq, NodeEditController.NODECONFIG_CHANGED_EVENT);
			}
		}

		super.event(ureq, source, event);
	}
}
