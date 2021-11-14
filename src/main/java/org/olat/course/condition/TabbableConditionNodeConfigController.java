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
package org.olat.course.condition;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.tabbedpane.TabbedPane;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.tabbable.ActivateableTabbableDefaultController;
import org.olat.core.util.Util;
import org.olat.course.editor.AccessEditController;
import org.olat.course.editor.ConditionAccessEditConfig;
import org.olat.course.editor.NodeEditController;
import org.olat.course.editor.VisibilityEditController;
import org.olat.course.nodes.AbstractAccessableCourseNode;
import org.olat.course.nodes.CourseNode;
import org.olat.course.run.userview.UserCourseEnvironment;
import org.olat.course.tree.CourseEditorTreeModel;

/**
 * 
 * Initial date: 3 Sep 2019<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class TabbableConditionNodeConfigController extends ActivateableTabbableDefaultController {

	private static final String PANE_TAB_VISIBILITY = "pane.tab.visibility";
	private static final String PANE_TAB_ACCESSIBILITY = "pane.tab.accessibility";
	private final static String[] paneKeys = { PANE_TAB_VISIBILITY, PANE_TAB_ACCESSIBILITY };
	
	private final VisibilityEditController visibilityCtrl;
	private AccessEditController accessCtrl;
	private TabbedPane tabPane;
	
	public TabbableConditionNodeConfigController(UserRequest ureq, WindowControl wControl, CourseNode courseNode,
			UserCourseEnvironment userCourseEnvironment, CourseEditorTreeModel editorModel,
			ConditionAccessEditConfig accessEditConfig) {
		super(ureq, wControl);
		setTranslator(Util.createPackageTranslator(NodeEditController.class, getLocale(), getTranslator()));
		
		visibilityCtrl = new VisibilityEditController(ureq, getWindowControl(), courseNode, userCourseEnvironment,
				editorModel);
		listenTo(visibilityCtrl);

		if (courseNode instanceof AbstractAccessableCourseNode && !accessEditConfig.isCustomAccessConditionController()) {
			accessCtrl = new AccessEditController(ureq, getWindowControl(), (AbstractAccessableCourseNode) courseNode,
					userCourseEnvironment, editorModel, accessEditConfig);
			listenTo(accessCtrl);
		}
	}

	@Override
	public void addTabs(TabbedPane tabbedPane) {
		tabPane = tabbedPane;
		tabbedPane.addTab(translate(PANE_TAB_VISIBILITY), visibilityCtrl.getInitialComponent());
		if (accessCtrl != null) {
			tabbedPane.addTab(translate(PANE_TAB_ACCESSIBILITY), accessCtrl.getInitialComponent());
		}
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
	public void event(UserRequest ureq, Controller source, Event event) {
		if (source == visibilityCtrl) {
			if (event == NodeEditController.NODECONFIG_CHANGED_EVENT) {
				fireEvent(ureq, NodeEditController.NODECONFIG_CHANGED_EVENT);
			}
		} else if (source == accessCtrl) {
			if (event == NodeEditController.NODECONFIG_CHANGED_EVENT) {
				fireEvent(ureq, NodeEditController.NODECONFIG_CHANGED_EVENT);
			}
		} 
	}
	
	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		//
	}

}
