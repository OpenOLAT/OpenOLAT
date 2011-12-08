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
package de.bps.course.nodes.ll;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

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
import org.olat.course.editor.NodeEditController;
import org.olat.course.run.userview.UserCourseEnvironment;
import org.olat.modules.ModuleConfiguration;

import de.bps.course.nodes.LLCourseNode;

/**
 * Description:<br>
 * Edit controller for link list course nodes.
 *
 * <P>
 * Initial Date: 05.11.2008 <br>
 *
 * @author Marcel Karras (toka@freebits.de)
 */
public class LLEditController extends ActivateableTabbableDefaultController implements ControllerEventListener {

	public static final String PANE_TAB_LLCONFIG = "pane.tab.llconfig";
	private static final String PANE_TAB_ACCESSIBILITY = "pane.tab.accessibility";
	private ModuleConfiguration moduleConfiguration;
	private LLCourseNode courseNode;
	private VelocityContainer editVc;
	private ConditionEditController accessibilityCondContr;
	final static String[] paneKeys = { PANE_TAB_LLCONFIG, PANE_TAB_ACCESSIBILITY };
	private TabbedPane tabPane;
	private LLEditForm llFormContr;

	public LLEditController(ModuleConfiguration moduleConfiguration, UserRequest ureq, WindowControl wControl, LLCourseNode courseNode,
			ICourse course, UserCourseEnvironment userCourseEnv) {
		super(ureq, wControl);

		this.moduleConfiguration = moduleConfiguration;
		this.courseNode = courseNode;

		editVc = this.createVelocityContainer("edit");

		Condition accessCondition = courseNode.getPreConditionAccess();
		accessibilityCondContr = new ConditionEditController(ureq, wControl, course.getCourseEnvironment().getCourseGroupManager(),
				accessCondition, "accessabilityConditionForm", AssessmentHelper.getAssessableNodes(course.getEditorTreeModel(), courseNode),
				userCourseEnv);
		this.listenTo(accessibilityCondContr);

		llFormContr = new LLEditForm(ureq, getWindowControl(), this.moduleConfiguration, course.getCourseEnvironment());
		llFormContr.addControllerListener(this);
		editVc.put("llEditForm", llFormContr.getInitialComponent());

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
	 * {@inheritDoc}
	 */
	@Override
	protected void doDispose() {
	// nothing to dispose

	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
	// TODO Auto-generated method stub

	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if (source == accessibilityCondContr) {
			if (event == Event.CHANGED_EVENT) {
				Condition cond = accessibilityCondContr.getCondition();
				courseNode.setPreConditionAccess(cond);
				fireEvent(ureq, NodeEditController.NODECONFIG_CHANGED_EVENT);
			}
		} else if (source == llFormContr) {
			moduleConfiguration = llFormContr.getModuleConfiguration();
			fireEvent(ureq, NodeEditController.NODECONFIG_CHANGED_EVENT);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public void addTabs(TabbedPane tabbedPane) {
		tabPane = tabbedPane;
		tabbedPane.addTab(translate(PANE_TAB_ACCESSIBILITY), accessibilityCondContr
				.getWrappedDefaultAccessConditionVC(translate("condition.accessibility.title")));
		tabbedPane.addTab(translate(PANE_TAB_LLCONFIG), editVc);

	}

	/**
	 * {@inheritDoc}
	 */
	public static boolean isConfigValid(ModuleConfiguration moduleConfig) {
		List<LLModel> linkList = (List<LLModel>) moduleConfig.get(LLCourseNode.CONF_LINKLIST);
		if (linkList != null) {
			for (LLModel link : linkList) {
				if (link.isIntern() && StringHelper.containsNonWhitespace(link.getTarget()) && 
						StringHelper.containsNonWhitespace(link.getDescription())) {
					return true;
				}
				if (link.getTarget().isEmpty() || link.getDescription().isEmpty()) { return false; }
				URL target = null;
				try {
					target = new URL(link.getTarget());
				} catch (MalformedURLException e) {
					target = null;
				}
				if (target == null) {  return false; }
			}
			return true;
		}
		return false;
	}
}
