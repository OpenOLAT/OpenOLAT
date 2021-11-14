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
package org.olat.modules.qpool.ui;

import java.util.List;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.stack.TooledStackedPanel;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.MainLayoutBasicController;
import org.olat.core.gui.control.generic.dtabs.Activateable2;
import org.olat.core.id.context.ContextEntry;
import org.olat.core.id.context.StateEntry;

/**
 * 
 * Initial date: 21.01.2013<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class QuestionPoolSiteMainController extends MainLayoutBasicController implements Activateable2 {

	private final QuestionPoolMainEditorController qpoolMainController;
	
	public QuestionPoolSiteMainController(UserRequest ureq, WindowControl wControl) {
		super(ureq, wControl);

		TooledStackedPanel stackPanel = new TooledStackedPanel("qpoolStackPanel", getTranslator(), this);
		stackPanel.setToolbarAutoEnabled(true);
		stackPanel.setShowCloseLink(true, false);
		stackPanel.setInvisibleCrumb(0);
		putInitialPanel(stackPanel);
		
		qpoolMainController = new QuestionPoolMainEditorController(ureq, wControl, stackPanel);
		listenTo(qpoolMainController);
		
		stackPanel.pushController(translate("topnav.qpool"), qpoolMainController);
	}
	
	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		//
	}

	@Override
	public void activate(UserRequest ureq, List<ContextEntry> entries, StateEntry state) {
		qpoolMainController.activate(ureq, entries, state);
	}
}
