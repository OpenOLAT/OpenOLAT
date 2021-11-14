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

package org.olat.course.nodes.cal;

import java.util.Date;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.tabbedpane.TabbedPane;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.ControllerEventListener;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.tabbable.ActivateableTabbableDefaultController;
import org.olat.core.util.StringHelper;
import org.olat.course.ICourse;
import org.olat.course.assessment.AssessmentHelper;
import org.olat.course.condition.Condition;
import org.olat.course.condition.ConditionEditController;
import org.olat.course.condition.ConditionRemoveController;
import org.olat.course.editor.NodeEditController;
import org.olat.course.nodes.CalCourseNode;
import org.olat.course.run.calendar.CourseCalendarController;
import org.olat.course.run.userview.UserCourseEnvironment;
import org.olat.course.tree.CourseEditorTreeModel;
import org.olat.modules.ModuleConfiguration;

/**
 * 
 * <h3>Description:</h3> Edit controller for calendar course nodes<br/>
 * <p>
 * Initial Date: 4 nov. 2009 <br>
 * 
 * @author srosse, stephane.rosse@frentix.com, www.frentix.com
 */
public class CalEditController extends ActivateableTabbableDefaultController implements ControllerEventListener {
	private static final String PANE_TAB_ACCESSIBILITY = "pane.tab.accessibility";
	private static final String PANE_TAB_CALCONFIG = "pane.tab.calconfig";

	private static final String[] paneKeys = { PANE_TAB_CALCONFIG, PANE_TAB_ACCESSIBILITY };

	private ConditionRemoveController conditionRemoveCtrl;
	private ConditionEditController accessCondContr;
	private Controller configCtrl;
	private TabbedPane tabs;
	private CourseCalendarController calCtr;
	private VelocityContainer editAccessVc;
	private ConditionEditController editCondContr;
	
	private CalCourseNode calCourseNode;

	public CalEditController(UserRequest ureq, WindowControl wControl, CalCourseNode calCourseNode, ICourse course,
			UserCourseEnvironment euce) {
		super(ureq, wControl);
		this.calCourseNode = calCourseNode;

		configCtrl = new CalConfigsController(ureq, wControl, course, calCourseNode);
		listenTo(configCtrl);

		if (calCourseNode.hasCustomPreConditions()) {
			editAccessVc = createVelocityContainer("edit_access");
			CourseEditorTreeModel editorModel = course.getEditorTreeModel();
			
			conditionRemoveCtrl = new ConditionRemoveController(ureq, getWindowControl());
			listenTo(conditionRemoveCtrl);
			editAccessVc.put("remove", conditionRemoveCtrl.getInitialComponent());
			
			// Accessibility precondition
			Condition accessCondition = calCourseNode.getPreConditionAccess();
			accessCondContr = new ConditionEditController(ureq, getWindowControl(), euce, accessCondition,
					AssessmentHelper.getAssessableNodes(editorModel, calCourseNode));
			this.listenTo(accessCondContr);
			editAccessVc.put("readerCondition", accessCondContr.getInitialComponent());

			// cal read / write preconditions
			Condition editCondition = calCourseNode.getPreConditionEdit();
			editCondContr = new ConditionEditController(ureq, getWindowControl(), euce, editCondition,
					AssessmentHelper.getAssessableNodes(editorModel, calCourseNode));
			listenTo(editCondContr);
			editAccessVc.put("editCondition", editCondContr.getInitialComponent());
		}
	}

	@Override
	public void event(UserRequest ureq, Component source, Event event) {
		//
	}

	@Override
	public void event(UserRequest ureq, Controller source, Event event) {
		if (source == configCtrl) {
			fireEvent(ureq, event);
		} else if (source == accessCondContr) {
			if (event == Event.CHANGED_EVENT) {
				Condition cond = accessCondContr.getCondition();
				calCourseNode.setPreConditionAccess(cond);
				fireEvent(ureq, NodeEditController.NODECONFIG_CHANGED_EVENT);
			}
		} else if (source == editCondContr) {
			if (event == Event.CHANGED_EVENT) {
				Condition cond = editCondContr.getCondition();
				calCourseNode.setPreConditionEdit(cond);
				fireEvent(ureq, NodeEditController.NODECONFIG_CHANGED_EVENT);
			}
		} else if (source == conditionRemoveCtrl && event == ConditionRemoveController.REMOVE) {
			calCourseNode.removeCustomPreconditions();
			fireEvent(ureq, NodeEditController.NODECONFIG_CHANGED_REFRESH_EVENT);
		}
	}

	@Override
	public void addTabs(TabbedPane tabbedPane) {
		tabs = tabbedPane;
		if (editAccessVc != null) {
			tabbedPane.addTab(translate(PANE_TAB_ACCESSIBILITY), editAccessVc);
		}
		tabbedPane.addTab(translate(PANE_TAB_CALCONFIG), configCtrl.getInitialComponent());
	}

	@Override
	protected void doDispose() {
		if (calCtr != null) {
			calCtr.dispose();
			calCtr = null;
		}
        super.doDispose();
	}

	@Override
	public String[] getPaneKeys() {
		return paneKeys;
	}

	@Override
	public TabbedPane getTabbedPane() {
		return tabs;
	}

	public static Date getStartDate(ModuleConfiguration config) {
		String timeStr = config.getStringValue(CalCourseNode.CONFIG_START_DATE);
		if (StringHelper.containsNonWhitespace(timeStr)) {
			try {
				Long time = Long.parseLong(timeStr);
				return new Date(time);
			} catch (Exception e) {
				return null;
			}
		}
		return null;
	}

	public static void setStartDate(ModuleConfiguration config, Date startDate) {
		if (startDate == null) config.setStringValue(CalCourseNode.CONFIG_START_DATE, "");
		else {
			String timeStr = String.valueOf(startDate.getTime());
			config.setStringValue(CalCourseNode.CONFIG_START_DATE, timeStr);
		}
	}

	public static boolean getAutoDate(ModuleConfiguration config) {
		String autoStr = config.getStringValue(CalCourseNode.CONFIG_AUTO_DATE);
		if (StringHelper.containsNonWhitespace(autoStr)) { return Boolean.valueOf(autoStr); }
		return Boolean.FALSE;
	}

	public static void setAutoDate(ModuleConfiguration config, boolean autoDate) {
		config.setStringValue(CalCourseNode.CONFIG_AUTO_DATE, Boolean.toString(autoDate));
	}
}