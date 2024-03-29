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
package org.olat.course.nodes.document.ui;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.stack.BreadcrumbPanel;
import org.olat.core.gui.components.tabbedpane.TabbedPane;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.tabbable.ActivateableTabbableDefaultController;
import org.olat.core.util.vfs.VFSContainer;
import org.olat.course.ICourse;
import org.olat.course.nodes.DocumentCourseNode;

/**
 * 
 * Initial date: 13 Jul 2020<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class DocumentEditController extends ActivateableTabbableDefaultController {

	public static final String PANE_TAB_CONFIG = "pane.tab.config";
	private final static String[] paneKeys = { PANE_TAB_CONFIG };

	private DocumentConfigController configController;
	private TabbedPane tabPane;

	public DocumentEditController(UserRequest ureq, WindowControl wControl, BreadcrumbPanel stackPanel,
			ICourse course, DocumentCourseNode documentCourseNode, VFSContainer courseFolderCont) {
		super(ureq, wControl);

		configController = new DocumentConfigController(ureq, wControl, stackPanel, course, documentCourseNode, courseFolderCont);
		listenTo(configController);
	}

	@Override
	public void addTabs(TabbedPane tabbedPane) {
		tabPane = tabbedPane;
		tabbedPane.addTab(translate(PANE_TAB_CONFIG), configController.getInitialComponent());
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
		if (source == configController) {
			fireEvent(ureq, event);
		}
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		//
	}

}
