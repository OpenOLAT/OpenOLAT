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
package org.olat.course.nodes.feed;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.stack.BreadcrumbPanel;
import org.olat.core.gui.components.tabbedpane.TabbedPane;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.ControllerEventListener;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.tabbable.ActivateableTabbableDefaultController;
import org.olat.core.util.Util;
import org.olat.course.ICourse;
import org.olat.course.assessment.AssessmentHelper;
import org.olat.course.condition.Condition;
import org.olat.course.condition.ConditionEditController;
import org.olat.course.condition.ConditionRemoveController;
import org.olat.course.editor.NodeEditController;
import org.olat.course.nodes.AbstractFeedCourseNode;
import org.olat.course.run.userview.UserCourseEnvironment;
import org.olat.course.tree.CourseEditorTreeModel;
import org.olat.modules.webFeed.ui.FeedUIFactory;

/**
 * The abstract feed course node edit controller.
 * 
 * <P>
 * Initial Date: Mar 31, 2009 <br>
 * 
 * @author gwassmann
 */
public class FeedNodeEditController extends ActivateableTabbableDefaultController implements ControllerEventListener {

	public static final String PANE_TAB_CONFIG = "pane.tab.feed";
	private static final String PANE_TAB_ACCESS = "pane.tab.access";
	private static final String[] paneKeys = { PANE_TAB_ACCESS, PANE_TAB_CONFIG };

	private TabbedPane tabbedPane;
	private VelocityContainer accessVC;

	private ConditionRemoveController conditionRemoveCtrl;
	private ConditionEditController readerCtr;
	private ConditionEditController posterCtr;
	private ConditionEditController moderatroCtr;
	private Controller configCtrl;

	private AbstractFeedCourseNode courseNode;

	public FeedNodeEditController(UserRequest ureq, WindowControl wControl, BreadcrumbPanel stackPanel,
			String translatorPackage, ICourse course, AbstractFeedCourseNode courseNode, UserCourseEnvironment uce,
			FeedUIFactory uiFactory, String resourceTypeName, String helpUrl) {
		super(ureq, wControl);
		setTranslator(Util.createPackageTranslator(translatorPackage, getLocale(), getTranslator()));
		this.courseNode = courseNode;
		
		if (courseNode.hasCustomPreConditions()) {
			// Accessibility tab
			accessVC = new VelocityContainer("accessVC", FeedNodeEditController.class, "access", getTranslator(), this);
			CourseEditorTreeModel editorModel = course.getEditorTreeModel();
			
			conditionRemoveCtrl = new ConditionRemoveController(ureq, getWindowControl());
			listenTo(conditionRemoveCtrl);
			accessVC.put("remove", conditionRemoveCtrl.getInitialComponent());
			
			// Moderator precondition
			Condition moderatorCondition = courseNode.getPreConditionModerator();
			moderatroCtr = new ConditionEditController(ureq, getWindowControl(), uce, moderatorCondition,
					AssessmentHelper.getAssessableNodes(editorModel, courseNode));
			this.listenTo(moderatroCtr);
			accessVC.put("moderatorCondition", moderatroCtr.getInitialComponent());
			
			// Poster precondition
			Condition posterCondition = courseNode.getPreConditionPoster();
			posterCtr = new ConditionEditController(ureq, getWindowControl(), uce, posterCondition,
					AssessmentHelper.getAssessableNodes(editorModel, courseNode));
			this.listenTo(posterCtr);
			accessVC.put("posterCondition", posterCtr.getInitialComponent());
			
			// Reader precondition
			Condition readerCondition = courseNode.getPreConditionReader();
			readerCtr = new ConditionEditController(ureq, getWindowControl(), uce, readerCondition,
					AssessmentHelper.getAssessableNodes(editorModel, courseNode));
			this.listenTo(readerCtr);
			accessVC.put("readerCondition", readerCtr.getInitialComponent());
		}

		configCtrl = new FeedNodeConfigsController(ureq, wControl, stackPanel, translatorPackage, course, courseNode,
				uiFactory, resourceTypeName, helpUrl);
		listenTo(configCtrl);
	}

	@Override
	public String[] getPaneKeys() {
		return paneKeys;
	}

	@Override
	public TabbedPane getTabbedPane() {
		return tabbedPane;
	}

	@Override
	protected void doDispose() {
		//
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		//
	}

	@Override
	public void event(UserRequest ureq, Controller source, Event event) {
		if (source == moderatroCtr) {
			if (event == Event.CHANGED_EVENT) {
				Condition cond = moderatroCtr.getCondition();
				courseNode.setPreConditionModerator(cond);
				fireEvent(ureq, NodeEditController.NODECONFIG_CHANGED_EVENT);
			}
		} else if (source == posterCtr) {
			if (event == Event.CHANGED_EVENT) {
				Condition cond = posterCtr.getCondition();
				courseNode.setPreConditionPoster(cond);
				fireEvent(ureq, NodeEditController.NODECONFIG_CHANGED_EVENT);
			}
		} else if (source == readerCtr) {
			if (event == Event.CHANGED_EVENT) {
				Condition cond = readerCtr.getCondition();
				courseNode.setPreConditionReader(cond);
				fireEvent(ureq, NodeEditController.NODECONFIG_CHANGED_EVENT);
			}
		} else if (source == configCtrl) {
			fireEvent(ureq, event);
		} else if (source == conditionRemoveCtrl && event == ConditionRemoveController.REMOVE) {
			courseNode.removeCustomPreconditions();
			fireEvent(ureq, NodeEditController.NODECONFIG_CHANGED_REFRESH_EVENT);
		}
	}

	@Override
	public void addTabs(TabbedPane tabbedPane) {
		this.tabbedPane = tabbedPane;
		if (accessVC != null) {
			tabbedPane.addTab(translate(PANE_TAB_ACCESS), accessVC);
		}
		if (configCtrl != null) {
			tabbedPane.addTab(translate(PANE_TAB_CONFIG), configCtrl.getInitialComponent());
		}
	}
}
