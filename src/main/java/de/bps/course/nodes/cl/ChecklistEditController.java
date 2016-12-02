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
 * BPS Bildungsportal Sachsen GmbH, http://www.bps-system.de
 * <p>
 */
package de.bps.course.nodes.cl;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.tabbedpane.TabbedPane;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.ControllerEventListener;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.closablewrapper.CloseableModalController;
import org.olat.core.gui.control.generic.tabbable.ActivateableTabbableDefaultController;
import org.olat.core.gui.translator.Translator;
import org.olat.core.util.Util;
import org.olat.course.ICourse;
import org.olat.course.assessment.AssessmentHelper;
import org.olat.course.condition.Condition;
import org.olat.course.condition.ConditionEditController;
import org.olat.course.editor.NodeEditController;
import org.olat.course.run.userview.UserCourseEnvironment;

import de.bps.course.nodes.ChecklistCourseNode;
import de.bps.olat.modules.cl.Checklist;
import de.bps.olat.modules.cl.ChecklistManager;
import de.bps.olat.modules.cl.ChecklistUIFactory;

/**
 * Description:<br>
 * Edit controller for checklist course node
 * 
 * <P>
 * Initial Date:  23.07.2009 <br>
 * @author bja <bja@bps-system.de>
 */
public class ChecklistEditController extends ActivateableTabbableDefaultController implements ControllerEventListener {
	
	public static final String PANE_TAB_CLCONFIG = "pane.tab.clconfig";
	private static final String PANE_TAB_ACCESSIBILITY = "pane.tab.accessibility";
	final static String[] paneKeys = { PANE_TAB_CLCONFIG, PANE_TAB_ACCESSIBILITY };
	
	// GUI
	private ConditionEditController accessibilityCondContr;
	private CloseableModalController cmcManage;
	private Controller checklistFormContr, manageController;
	private VelocityContainer editVc;
	private TabbedPane tabPane;
	private Link manageCheckpointsButton;
	
	// data
	private ICourse course;
	private ChecklistCourseNode courseNode;
	private Checklist checklist;
	
	public ChecklistEditController(UserRequest ureq, WindowControl wControl, ChecklistCourseNode checklistCourseNode, ICourse course, UserCourseEnvironment euce) {
		super(ureq, wControl);
		this.course = course;
		this.courseNode = checklistCourseNode;
		checklist = courseNode.loadOrCreateChecklist(course.getCourseEnvironment().getCoursePropertyManager());

		Condition accessCondition = courseNode.getPreConditionAccess();
		accessibilityCondContr = new ConditionEditController(ureq, wControl, euce,
				accessCondition, AssessmentHelper.getAssessableNodes(course.getEditorTreeModel(), courseNode));
		listenTo(accessibilityCondContr);
		
		editVc = createVelocityContainer("edit");
		manageCheckpointsButton = LinkFactory.createButton("manage", editVc, this);
		checklistFormContr = ChecklistUIFactory.getInstance().createEditCheckpointsController(ureq, getWindowControl(), checklist, "cl.save", ChecklistUIFactory.comparatorTitleAsc);
		checklistFormContr.addControllerListener(this);
		editVc.put("checklistEditForm", checklistFormContr.getInitialComponent());
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String[] getPaneKeys() {
		return paneKeys;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public TabbedPane getTabbedPane() {
		return tabPane;
	}

	/**
	 * @see org.olat.core.gui.control.DefaultController#doDispose()
	 */
	@Override
	protected void doDispose() {
		if(accessibilityCondContr != null) {
			accessibilityCondContr.dispose();
			accessibilityCondContr = null;
		}
		if(checklistFormContr != null) {
			checklistFormContr.dispose();
			checklistFormContr = null;
		}
		if(manageController != null) {
			manageController.dispose();
			manageController = null;
		}
		if(cmcManage != null) {
			cmcManage.dispose();
			cmcManage = null;
		}
	}

	/**
	 * @see org.olat.core.gui.control.DefaultController#event(org.olat.core.gui.UserRequest, org.olat.core.gui.components.Component, org.olat.core.gui.control.Event)
	 */
	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		if(source.equals(manageCheckpointsButton)) {
			manageController = ChecklistUIFactory.getInstance().createManageCheckpointsController(ureq, getWindowControl(), checklist, course, true);
			manageController.addControllerListener(this);
			Translator clTranslator = Util.createPackageTranslator(Checklist.class, ureq.getLocale());
			cmcManage = new CloseableModalController(getWindowControl(), clTranslator.translate("cl.close"), manageController.getInitialComponent(), true, clTranslator.translate("cl.manage.title"));
			cmcManage.addControllerListener(this);
			cmcManage.activate();
		}
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if(source == accessibilityCondContr) {
			if (event == Event.CHANGED_EVENT) {
				Condition cond = accessibilityCondContr.getCondition();
				courseNode.setPreConditionAccess(cond);
				fireEvent(ureq, NodeEditController.NODECONFIG_CHANGED_EVENT);
			}
		} else if(source == checklistFormContr && event == Event.CHANGED_EVENT) {
			//checklist = ChecklistManager.getInstance().saveChecklist(checklist);
			fireEvent(ureq, NodeEditController.NODECONFIG_CHANGED_EVENT);
		} else if(source == manageController && event == Event.DONE_EVENT) {
			cmcManage.deactivate();
		} else if(event == NodeEditController.NODECONFIG_CHANGED_EVENT) {
			// update title and description according to the course node
			Checklist cl = ChecklistManager.getInstance().loadChecklist(checklist);
			cl.setTitle(courseNode.getShortTitle());
			cl.setDescription(courseNode.getLongTitle());
			checklist = ChecklistManager.getInstance().saveChecklist(cl);
		}
	}

	/**
	 * @see org.olat.core.gui.control.generic.tabbable.TabbableController#addTabs(org.olat.core.gui.components.tabbedpane.TabbedPane)
	 */
	public void addTabs(TabbedPane tabbedPane) {
		tabPane = tabbedPane;
		tabbedPane.addTab(translate(PANE_TAB_ACCESSIBILITY), accessibilityCondContr.getWrappedDefaultAccessConditionVC(translate("condition.accessibility.title")));
		tabbedPane.addTab(translate(PANE_TAB_CLCONFIG), editVc);
	}

}
