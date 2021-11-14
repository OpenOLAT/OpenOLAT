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
package org.olat.ims.qti21.ui.report;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;

/**
 * 
 * Initial date: 11 juin 2020<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class QuestionOriginReportController extends BasicController {
	
	private final QuestionOriginReportTableController tableCtrl;
	private final QuestionOriginReportSearchController searchCtrl;
	
	public QuestionOriginReportController(UserRequest ureq, WindowControl wControl) {
		super(ureq, wControl);

		searchCtrl = new QuestionOriginReportSearchController(ureq, wControl);
		listenTo(searchCtrl);
		tableCtrl = new QuestionOriginReportTableController(ureq, wControl);
		listenTo(tableCtrl);
		
		VelocityContainer mainVC = createVelocityContainer("reports");
		mainVC.put("search", searchCtrl.getInitialComponent());
		mainVC.put("table", tableCtrl.getInitialComponent());
		putInitialPanel(mainVC);
	}
	
	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if(searchCtrl == source) {
			if(event instanceof SearchEvent) {
				doSearch(ureq, (SearchEvent)event);
			}
		}
		super.event(ureq, source, event);
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		//
	}
	
	private void doSearch(UserRequest ureq, SearchEvent searchEvent) {
		tableCtrl.loadModel(ureq, searchEvent.getSearchString(), searchEvent.getAuthor());
	}
}
