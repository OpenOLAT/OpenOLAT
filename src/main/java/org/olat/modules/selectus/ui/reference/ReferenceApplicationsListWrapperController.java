/**
 * <a href="https://www.openolat.org">
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
 * frentix GmbH, https://www.frentix.com
 * <p>
 */
package org.olat.modules.selectus.ui.reference;

import java.util.List;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.stack.TooledStackedPanel;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.gui.control.generic.dtabs.Activateable2;
import org.olat.core.id.context.ContextEntry;
import org.olat.core.id.context.StateEntry;
import org.olat.core.util.Util;

import org.olat.modules.selectus.RecruitingPositionSecurityCallback;
import org.olat.modules.selectus.model.Application;
import org.olat.modules.selectus.model.Position;
import org.olat.modules.selectus.ui.PositionController;
import org.olat.modules.selectus.ui.events.SelectApplicationEvent;

/**
 * 
 * Initial date: 8 mars 2024<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class ReferenceApplicationsListWrapperController extends BasicController implements Activateable2 {
	
	private Link next;
	private Link previous;
	private Link applications;
	private TooledStackedPanel stackPanel;
	
	private int currentIndex = 0;
	private final Position position;
	private final int numOfApplications;
	private final RecruitingPositionSecurityCallback secCallback;
	
	private ReferenceApplicationController applicationCtrl;
	private final ReferenceApplicationsListController applicationsListCtrl;
	
	public ReferenceApplicationsListWrapperController(UserRequest ureq, WindowControl wControl,
			Position position, List<Application> applicationsList, RecruitingPositionSecurityCallback secCallback) {
		super(ureq, wControl, Util.createPackageTranslator(PositionController.class, ureq.getLocale()));
		this.position = position;
		this.secCallback = secCallback;
		numOfApplications = applicationsList.size();

		applicationsListCtrl = new ReferenceApplicationsListController(ureq, getWindowControl(), position, applicationsList);
		listenTo(applicationsListCtrl);
		
		stackPanel = new TooledStackedPanel("crumb.applications", getTranslator(), this);
		stackPanel.setShowCloseLink(false, false);
		stackPanel.setBreadcrumbEnabled(false);
		stackPanel.setToolbarEnabled(false);
		stackPanel.pushController("root", applicationsListCtrl);
		initTools();
		
		putInitialPanel(stackPanel);
	}

	private void initTools() {
		previous = LinkFactory.createToolLink("previous", "previous", translate("previous"), this);
		previous.setIconLeftCSS("o_icon o_icon-lg o_icon_previous_page");
		stackPanel.addTool(previous, true);
		
		String posTool = getPositionString(currentIndex);
		applications = LinkFactory.createToolLink("application.applications", posTool, this);
		stackPanel.addTool(applications, true);
		
		next = LinkFactory.createToolLink("next", translate("next"), this);
		next.setIconLeftCSS("o_icon o_icon-lg o_icon_next_page");
		stackPanel.addTool(next, true);
	}
	
	private String getPositionString(int currentPosition) {
		String pos = translate("application.currentPosition", new String[]{ Integer.toString((currentPosition + 1)), Integer.toString(numOfApplications) });
		return "<span class='o_text_icon'>" + pos + "</span>" + translate("application.applications");
	}

	@Override
	public void activate(UserRequest ureq, List<ContextEntry> entries, StateEntry state) {
		pop(ureq);
	}
	
	private void pop(UserRequest ureq) {
		stackPanel.setToolbarEnabled(false);
		stackPanel.popUpToRootController(ureq);
		removeAsListenerAndDispose(applicationCtrl);
		applicationCtrl = null;
	}

	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if(applicationsListCtrl == source) {
			if(event instanceof SelectApplicationEvent) {
				SelectApplicationEvent sae = (SelectApplicationEvent)event;
				doSelectApplication(ureq, sae.getApplication());
			}
		} else if(applicationCtrl == source) {
			if(event == Event.DONE_EVENT) {
				fireEvent(ureq, event);
			}
		}
	}
	
	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		if(source == applications) {
			pop(ureq);
		} else if(source == next) {
			doNextApplication(ureq);
		} else if(source == previous) {
			doPreviousApplication(ureq);
		}
	}
	
	private void doNextApplication(UserRequest ureq) {
		Application nextApplication = applicationsListCtrl.getApplication(currentIndex + 1);
		if(nextApplication != null) {
			pop(ureq);
			doSelectApplication(ureq, nextApplication);
		}
	}
	
	private void doPreviousApplication(UserRequest ureq) {
		Application previousApplication = applicationsListCtrl.getApplication(currentIndex - 1);
		if(previousApplication != null) {
			pop(ureq);
			doSelectApplication(ureq, previousApplication);
		}
	}
	
	private void doSelectApplication(UserRequest ureq, Application application) {
		currentIndex = applicationsListCtrl.indexOf(application);
		String posTool = getPositionString(currentIndex);
		applications.setCustomDisplayText(posTool);
		next.setEnabled(currentIndex + 1 < numOfApplications);
		previous.setEnabled(currentIndex > 0);
		
		removeAsListenerAndDispose(applicationCtrl);
		stackPanel.setToolbarEnabled(true);
		stackPanel.setDirty(true);
		
		applicationCtrl = new ReferenceApplicationController(ureq, getWindowControl(), position, application, secCallback, false);
		listenTo(applicationCtrl);
		stackPanel.pushController("", applicationCtrl);
	}
}
