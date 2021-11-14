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
* Copyright (c) since 2004 at Multimedia- & E-Learning Services (MELS),<br>
* University of Zurich, Switzerland.
* <hr>
* <a href="http://www.openolat.org">
* OpenOLAT - Online Learning and Training</a><br>
* This file has been modified by the OpenOLAT community. Changes are licensed
* under the Apache 2.0 license as the original file.
*/

package org.olat.course.nodes.basiclti;

import org.olat.core.commons.fullWebApp.LayoutMain3ColsPreviewController;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.tabbedpane.TabbedPane;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.tabbable.ActivateableTabbableDefaultController;
import org.olat.course.ICourse;
import org.olat.course.editor.NodeEditController;
import org.olat.course.highscore.ui.HighScoreEditController;
import org.olat.course.nodeaccess.NodeAccessType;
import org.olat.course.nodes.BasicLTICourseNode;
import org.olat.course.nodes.MSCourseNode;
import org.olat.course.run.environment.CourseEnvironment;
import org.olat.course.run.userview.UserCourseEnvironment;
import org.olat.modules.ModuleConfiguration;
import org.olat.repository.RepositoryEntry;

/**
 * Description:<BR/>
 * This edit controller is used to edit a course building block of type basic lti
 * <P/>
 * Initial Date:  march 2010
 *
 * @author guido
 * @author Charles Severance
 */
public class LTIEditController extends ActivateableTabbableDefaultController {

	public static final String PANE_TAB_LTCONFIG = "pane.tab.ltconfig";
	public static final String PANE_TAB_HIGHSCORE = "pane.tab.highscore";
	
	private static final String[] paneKeys = {PANE_TAB_LTCONFIG};

	private ModuleConfiguration config;
	private final VelocityContainer myContent;
	private final CourseEnvironment editCourseEnv;
	private final UserCourseEnvironment userCourseEnv;
	private HighScoreEditController highScoreNodeConfigController;

	private LTIConfigForm ltConfigForm;	
	private BasicLTICourseNode courseNode;
	private TabbedPane myTabbedPane;
	private Link previewButton;
	private LayoutMain3ColsPreviewController previewLayoutCtr;

	/**
	 * Constructor for tunneling editor controller 
	 * @param config The node module configuration
	 * @param ureq The user request
	 * @param wControl The window controller
	 * @param tuCourseNode The current single page course node
	 * @param course
	 */
	public LTIEditController(ModuleConfiguration config, UserRequest ureq, WindowControl wControl, 
			BasicLTICourseNode ltCourseNode, ICourse course, UserCourseEnvironment euce) {
		super(ureq, wControl);
		
		this.config = config;
		this.courseNode = ltCourseNode;
		this.userCourseEnv = euce;
		this.editCourseEnv = course.getCourseEnvironment();
		
		myContent = createVelocityContainer("edit");
		previewButton = LinkFactory.createButtonSmall("command.preview", myContent, this);
		previewButton.setIconLeftCSS("o_icon o_icon_preview");
		
		highScoreNodeConfigController = new HighScoreEditController(ureq, wControl, config, course);
		listenTo(highScoreNodeConfigController);
		
		RepositoryEntry courseEntry = course.getCourseEnvironment().getCourseGroupManager().getCourseEntry();
		ltConfigForm = new LTIConfigForm(ureq, wControl, config, NodeAccessType.of(course), courseEntry, ltCourseNode.getIdent());
		listenTo(ltConfigForm);
		
		myContent.put("ltConfigForm", ltConfigForm.getInitialComponent());

		// Enable preview button only if node configuration is valid
		if (!(ltCourseNode.isConfigValid().isError())) myContent.contextPut("showPreviewButton", Boolean.TRUE);
		else myContent.contextPut("showPreviewButton", Boolean.FALSE);
		
	}

	@Override
	public void event(UserRequest ureq, Component source, Event event) {
		if (source == previewButton) {
			Controller runCtr = new LTIRunController(getWindowControl(), config, ureq, courseNode, userCourseEnv, editCourseEnv);
			previewLayoutCtr = new LayoutMain3ColsPreviewController(ureq, getWindowControl(), null, runCtr.getInitialComponent(), null);
			previewLayoutCtr.addDisposableChildController(runCtr);
			previewLayoutCtr.activate();
			listenTo(previewLayoutCtr);
		}
	}

	@Override
	public void event(UserRequest ureq, Controller source, Event event) {
		if (source == ltConfigForm) {
			if (event == Event.CANCELLED_EVENT) {
				// do nothing
			} else if (event == Event.DONE_EVENT) {
				config = ltConfigForm.getUpdatedConfig();
				updateHighscoreTab();
				fireEvent(ureq, NodeEditController.NODECONFIG_CHANGED_EVENT);
				fireEvent(ureq, NodeEditController.REMINDER_VISIBILITY_EVENT);
				// form valid -> node config valid -> show preview button
				myContent.contextPut("showPreviewButton", Boolean.TRUE);
			}
		} else if (source == highScoreNodeConfigController){
			if (event == Event.DONE_EVENT) {
				fireEvent(ureq, NodeEditController.NODECONFIG_CHANGED_EVENT);
			}
		} else if (source == previewLayoutCtr) {
			removeAsListenerAndDispose(previewLayoutCtr);
		}
	}

	private void updateHighscoreTab() {
		Boolean sf = courseNode.getModuleConfiguration().getBooleanSafe(MSCourseNode.CONFIG_KEY_HAS_SCORE_FIELD,false);
		myTabbedPane.setEnabled(myTabbedPane.indexOfTab(highScoreNodeConfigController.getInitialComponent()), sf);
	}

	@Override
	public void addTabs(TabbedPane tabbedPane) {
		myTabbedPane = tabbedPane;
		tabbedPane.addTab(translate(PANE_TAB_LTCONFIG), myContent);
		tabbedPane.addTab(translate(PANE_TAB_HIGHSCORE) , highScoreNodeConfigController.getInitialComponent());
		updateHighscoreTab();
	}

	@Override
	public String[] getPaneKeys() {
		return paneKeys;
	}

	@Override
	public TabbedPane getTabbedPane() {
		return myTabbedPane;
	}
	
}
