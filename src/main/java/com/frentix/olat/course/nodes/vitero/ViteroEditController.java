/**
 * OLAT - Online Learning and Training<br>
 * http://www.olat.org
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); <br>
 * you may not use this file except in compliance with the License.<br>
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing,<br>
 * software distributed under the License is distributed on an "AS IS" BASIS, <br>
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
 * See the License for the specific language governing permissions and <br>
 * limitations under the License.
 * <p>
 * Copyright (c) frentix GmbH<br>
 * http://www.frentix.com<br>
 * <p>
 */
package com.frentix.olat.course.nodes.vitero;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.tabbedpane.TabbedPane;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.ControllerEventListener;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.modal.DialogBoxController;
import org.olat.core.gui.control.generic.modal.DialogBoxUIFactory;
import org.olat.core.gui.control.generic.tabbable.ActivateableTabbableDefaultController;
import org.olat.core.id.OLATResourceable;
import org.olat.core.util.resource.OresHelper;
import org.olat.course.ICourse;
import org.olat.course.assessment.AssessmentHelper;
import org.olat.course.condition.Condition;
import org.olat.course.condition.ConditionEditController;
import org.olat.course.editor.NodeEditController;
import org.olat.course.run.userview.UserCourseEnvironment;

import com.frentix.olat.course.nodes.ViteroCourseNode;
import com.frentix.olat.vitero.ui.ViteroBookingsEditController;

/**
 * 
 * Description:<br>
 * 
 * <P>
 * Initial Date:  6 oct. 2011 <br>
 *
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
public class ViteroEditController extends ActivateableTabbableDefaultController implements ControllerEventListener {

	private static final String PANE_TAB_ACCESSIBILITY = "pane.tab.accessibility";
	public static final String PANE_TAB_VCCONFIG = "pane.tab.vcconfig";
	final static String[] paneKeys = { PANE_TAB_VCCONFIG, PANE_TAB_ACCESSIBILITY };

	// GUI
	private VelocityContainer editVc;
	private ConditionEditController accessibilityCondContr;
	private TabbedPane tabPane;
	private ViteroBookingsEditController editForm;
	private DialogBoxController yesNoUpdate;
	private DialogBoxController yesNoDelete;
	
	// runtime data
	private ViteroCourseNode courseNode;

	public ViteroEditController(UserRequest ureq, WindowControl wControl, ViteroCourseNode courseNode,
			ICourse course, UserCourseEnvironment userCourseEnv) {
		super(ureq, wControl);
		this.courseNode = courseNode;

		editVc = this.createVelocityContainer("edit");

		Condition accessCondition = courseNode.getPreConditionAccess();
		accessibilityCondContr = new ConditionEditController(ureq, wControl, course.getCourseEnvironment().getCourseGroupManager(),
				accessCondition, "accessabilityConditionForm", AssessmentHelper.getAssessableNodes(course.getEditorTreeModel(), courseNode),
				userCourseEnv);
		listenTo(accessibilityCondContr);

		OLATResourceable ores = OresHelper.createOLATResourceableInstance(course.getResourceableTypeName(), course.getResourceableId());
		editForm = new ViteroBookingsEditController(ureq, wControl, null, ores);
		listenTo(editForm);
		editVc.put("editForm", editForm.getInitialComponent());
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
	protected void doDispose() {
		if(editForm != null) {
			removeAsListenerAndDispose(editForm);
			editForm = null;
		}
		if(yesNoDelete != null) {
			removeAsListenerAndDispose(yesNoDelete);
			yesNoDelete = null;
		}
		if(yesNoUpdate != null) {
			removeAsListenerAndDispose(yesNoUpdate);
			yesNoUpdate = null;
		}
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		// nothing to do
	}

	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if (source == accessibilityCondContr) {
			if (event == Event.CHANGED_EVENT) {
				Condition cond = accessibilityCondContr.getCondition();
				courseNode.setPreConditionAccess(cond);
				fireEvent(ureq, NodeEditController.NODECONFIG_CHANGED_EVENT);
			}
		} else if (source == editForm) {
			fireEvent(ureq, NodeEditController.NODECONFIG_CHANGED_EVENT);
		} else if (source == yesNoDelete) {
			if(DialogBoxUIFactory.isYesEvent(event)) {
				reset(ureq);
			}
		}
	}
	
	private void reset(UserRequest ureq) {
		removeAsListenerAndDispose(editForm);
		// prepare new edit view
/*
		config = provider.createNewConfiguration();
		// create room if configured to do it immediately
		if(config.isCreateMeetingImmediately()) {
			// here, the config is empty in any case, thus there are no start and end dates
			provider.createClassroom(roomId, courseNode.getShortName(), courseNode.getLongTitle(), null, null, config);
		}
		editForm = new ViteroEditForm(ureq, getWindowControl(), provider.getTemplates(), (DefaultVCConfiguration) config);
		listenTo(editForm);
		editVc.put("editForm", editForm.getInitialComponent());
		listenTo(configCtr);
		editVc.put("configCtr", configCtr.getInitialComponent());
		editVc.setDirty(true);
		// save the minimal config
		courseNode.getModuleConfiguration().set(VCCourseNode.CONF_VC_CONFIGURATION, config);
		
		fireEvent(ureq, NodeEditController.NODECONFIG_CHANGED_EVENT);
		*/
	}

	public void addTabs(TabbedPane tabbedPane) {
		tabPane = tabbedPane;
		tabbedPane.addTab(translate(PANE_TAB_ACCESSIBILITY),
				accessibilityCondContr.getWrappedDefaultAccessConditionVC(translate("condition.accessibility.title")));
		tabbedPane.addTab(translate(PANE_TAB_VCCONFIG), editVc);

	}
}