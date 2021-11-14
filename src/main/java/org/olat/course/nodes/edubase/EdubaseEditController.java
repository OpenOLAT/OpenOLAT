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
package org.olat.course.nodes.edubase;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.tabbedpane.TabbedPane;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.ControllerEventListener;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.tabbable.ActivateableTabbableDefaultController;
import org.olat.course.editor.NodeEditController;
import org.olat.course.nodes.EdubaseCourseNode;

/**
 * 
 * Initial date: 21.06.2017<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class EdubaseEditController extends ActivateableTabbableDefaultController implements ControllerEventListener {

	private static final String PANE_TAB_VCCONFIG = "pane.tab.config";
	private static final String PANE_TAB_BOOK_SECTION_LIST = "pane.tab.book.section.list";
	private final static String[] paneKeys = { PANE_TAB_BOOK_SECTION_LIST, PANE_TAB_VCCONFIG };
	
	private TabbedPane tabPane;
	private EdubaseConfigController edubaseConfigController;
	private EdubaseBookSectionListController edubaseBookSectionListController;
	
	public EdubaseEditController(UserRequest ureq, WindowControl wControl, EdubaseCourseNode edubaseCourseNode) {
		super(ureq, wControl);

		edubaseConfigController = new EdubaseConfigController(ureq, wControl,
				edubaseCourseNode.getModuleConfiguration());
		listenTo(edubaseConfigController);

		edubaseBookSectionListController = new EdubaseBookSectionListController(ureq, wControl,
				edubaseCourseNode.getModuleConfiguration());
		listenTo(edubaseBookSectionListController);
	}

	@Override
	public void addTabs(TabbedPane tabbedPane) {
		tabPane = tabbedPane;
		tabbedPane.addTab(translate(PANE_TAB_VCCONFIG), edubaseConfigController.getInitialComponent());
		tabbedPane.addTab(translate(PANE_TAB_BOOK_SECTION_LIST), edubaseBookSectionListController.getInitialComponent());
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
		if (source == edubaseConfigController && event.equals(Event.DONE_EVENT)) {
			edubaseConfigController.getUpdatedConfig();
			fireEvent(ureq, NodeEditController.NODECONFIG_CHANGED_EVENT);
			edubaseBookSectionListController.dispatchEvent(ureq, edubaseConfigController, event);
		} else if (source == edubaseBookSectionListController && event.equals(Event.DONE_EVENT)) {
			edubaseBookSectionListController.getUpdatedConfig();
			fireEvent(ureq, NodeEditController.NODECONFIG_CHANGED_EVENT);
		}
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		//
	}
}
