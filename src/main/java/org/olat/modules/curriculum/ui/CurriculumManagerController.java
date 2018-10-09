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
package org.olat.modules.curriculum.ui;

import java.util.List;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.stack.TooledStackedPanel;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.gui.control.generic.dtabs.Activateable2;
import org.olat.core.id.context.ContextEntry;
import org.olat.core.id.context.StateEntry;
import org.olat.core.util.UserSession;
import org.olat.modules.curriculum.CurriculumSecurityCallback;
import org.olat.modules.curriculum.CurriculumSecurityCallbackFactory;

/**
 * The root controller of the curriculum mamangement site
 * which holds the list controller and the bread crump / toolbar
 * 
 * Initial date: 15 févr. 2018<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class CurriculumManagerController extends BasicController implements Activateable2 {
	
	private final TooledStackedPanel toolbarPanel;
	private final CurriculumListManagerController curriculumListCtrl;
	
	public CurriculumManagerController(UserRequest ureq, WindowControl wControl) {
		super(ureq, wControl);
		
		toolbarPanel = new TooledStackedPanel("categoriesStackPanel", getTranslator(), this);
		toolbarPanel.setInvisibleCrumb(0); // show root level
		toolbarPanel.setShowCloseLink(false, false);
		putInitialPanel(toolbarPanel);
		
		UserSession usess = ureq.getUserSession();
		CurriculumSecurityCallback secCallback = CurriculumSecurityCallbackFactory.createCallback(usess.getRoles());
		
		curriculumListCtrl = new CurriculumListManagerController(ureq, getWindowControl(), toolbarPanel, secCallback);
		listenTo(curriculumListCtrl);
		toolbarPanel.pushController("Curriculums", curriculumListCtrl);
	}

	@Override
	protected void doDispose() {
		//
	}

	@Override
	public void activate(UserRequest ureq, List<ContextEntry> entries, StateEntry state) {
		if(entries == null || entries.isEmpty()) return;
		curriculumListCtrl.activate(ureq, entries, state);
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		//
	}
}
