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
package org.olat.modules.lecture.ui;

import java.util.List;

import org.olat.core.commons.fullWebApp.LayoutMain3ColsController;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.stack.TooledStackedPanel;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.MainLayoutBasicController;
import org.olat.core.gui.control.generic.dtabs.Activateable2;
import org.olat.core.id.context.ContextEntry;
import org.olat.core.id.context.StateEntry;
import org.olat.modules.lecture.ui.coach.LecturesCoachingController;

/**
 * 
 * Initial date: 31 juil. 2019<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class LecturesMainController extends MainLayoutBasicController implements Activateable2 {

	private final TooledStackedPanel content;
	
	private LayoutMain3ColsController columnLayoutCtr;
	private final LecturesCoachingController lectureCoachingCtrl;
	
	public LecturesMainController(UserRequest ureq, WindowControl wControl) {
		super(ureq, wControl);
		
		content = new TooledStackedPanel("lecture-mgmt-stack", getTranslator(), this);
		content.setNeverDisposeRootController(true);
		content.setToolbarAutoEnabled(true);
		
		// TODO principal
		LecturesSecurityCallback secCallback = LecturesSecurityCallbackFactory
				.getSecurityCallback(true, false, false, LectureRoles.lecturemanager);
		lectureCoachingCtrl = new LecturesCoachingController(ureq, getWindowControl(), content, secCallback);
		listenTo(lectureCoachingCtrl);
		
		columnLayoutCtr = new LayoutMain3ColsController(ureq, getWindowControl(), null, content, "lectures");
		columnLayoutCtr.addCssClassToMain("o_lectures");
		listenTo(columnLayoutCtr); // auto dispose later
		putInitialPanel(columnLayoutCtr.getInitialComponent());
		
		String title = translate("coach.lectures");
		content.rootController(title, lectureCoachingCtrl);
	}

	@Override
	public void activate(UserRequest ureq, List<ContextEntry> entries, StateEntry state) {
		//
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		//
	}
	
	

}
