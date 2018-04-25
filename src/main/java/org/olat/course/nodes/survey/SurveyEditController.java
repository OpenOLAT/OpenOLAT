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
import org.olat.core.gui.components.tabbedpane.TabbedPane;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.ControllerEventListener;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.tabbable.ActivateableTabbableDefaultController;
import org.olat.course.ICourse;
import org.olat.course.assessment.AssessmentHelper;
import org.olat.course.condition.Condition;
import org.olat.course.condition.ConditionEditController;
import org.olat.course.nodes.SurveyCourseNode;
import org.olat.course.run.userview.UserCourseEnvironment;

/**
 * 
 * Initial date: 24.04.2018<br>
 * 
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class SurveyEditController extends ActivateableTabbableDefaultController implements ControllerEventListener {

	private static final String PANE_TAB_ACCESSIBILITY = "pane.tab.accessibility";
	public static final String PANE_TAB_CONFIG = "pane.tab.config";
	private final static String[] paneKeys = { PANE_TAB_CONFIG, PANE_TAB_ACCESSIBILITY };

	private VelocityContainer editVc;
	private SurveyConfigController surveyConfigController;
	private ConditionEditController accessibilityCondContr;
	private TabbedPane tabPane;

	public SurveyEditController(UserRequest ureq, WindowControl wControl, SurveyCourseNode surveyCourseNode,
			ICourse course, UserCourseEnvironment euce) {
		super(ureq, wControl);
		
		surveyConfigController = new SurveyConfigController(ureq, wControl, course, surveyCourseNode);
		listenTo(surveyConfigController);

		Condition accessCondition = surveyCourseNode.getPreConditionAccess();
		accessibilityCondContr = new ConditionEditController(ureq, wControl, euce, accessCondition,
				AssessmentHelper.getAssessableNodes(course.getEditorTreeModel(), surveyCourseNode));
		listenTo(accessibilityCondContr);

		editVc = createVelocityContainer("edit");
		editVc.put("config", surveyConfigController.getInitialComponent());
	}

	@Override
	public void addTabs(TabbedPane tabbedPane) {
		tabPane = tabbedPane;
		tabbedPane.addTab(translate(PANE_TAB_ACCESSIBILITY),
				accessibilityCondContr.getWrappedDefaultAccessConditionVC(translate("condition.accessibility.title")));
		tabbedPane.addTab(translate(PANE_TAB_CONFIG), editVc);
	}

	@Override
	public String[] getPaneKeys() {
		return paneKeys;
	}

	@Override
	public TabbedPane getTabbedPane() {
		return tabPane;
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
