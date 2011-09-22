/**
 * 
 * BPS Bildungsportal Sachsen GmbH<br>
 * Bahnhofstrasse 6<br>
 * 09111 Chemnitz<br>
 * Germany<br>
 * 
 * Copyright (c) 2005-2009 by BPS Bildungsportal Sachsen GmbH<br>
 * http://www.bps-system.de<br>
 *
 * All rights reserved.
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
import org.olat.core.gui.translator.PackageTranslator;
import org.olat.core.gui.translator.Translator;
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
		this.checklist = courseNode.loadOrCreateChecklist(this.course.getCourseEnvironment().getCoursePropertyManager());

		Condition accessCondition = courseNode.getPreConditionAccess();
		accessibilityCondContr = new ConditionEditController(ureq, wControl, course.getCourseEnvironment().getCourseGroupManager(),
				accessCondition, "accessabilityConditionForm", AssessmentHelper.getAssessableNodes(course.getEditorTreeModel(), courseNode), euce);
		this.listenTo(accessibilityCondContr);
		
		editVc = this.createVelocityContainer("edit");
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
			manageController = ChecklistUIFactory.getInstance().createManageCheckpointsController(ureq, getWindowControl(), checklist, course);
			manageController.addControllerListener(this);
			Translator clTranslator = new PackageTranslator(Checklist.class.getPackage().getName(), ureq.getLocale());
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
			ChecklistManager.getInstance().saveChecklist(this.checklist);
			fireEvent(ureq, NodeEditController.NODECONFIG_CHANGED_EVENT);
		} else if(source == manageController && event == Event.DONE_EVENT) {
			cmcManage.deactivate();
		} else if(event == NodeEditController.NODECONFIG_CHANGED_EVENT) {
			// update title and description according to the course node
			Checklist cl = ChecklistManager.getInstance().loadChecklist(this.checklist);
			cl.setTitle(this.courseNode.getShortTitle());
			cl.setDescription(this.courseNode.getLongTitle());
			ChecklistManager.getInstance().saveChecklist(cl);
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
