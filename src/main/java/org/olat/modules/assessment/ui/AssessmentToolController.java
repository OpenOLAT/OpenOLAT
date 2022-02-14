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
package org.olat.modules.assessment.ui;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.stack.TooledStackedPanel;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.MainLayoutBasicController;
import org.olat.core.gui.control.generic.dtabs.Activateable2;
import org.olat.core.id.OLATResourceable;
import org.olat.core.id.context.BusinessControlFactory;
import org.olat.core.id.context.ContextEntry;
import org.olat.core.id.context.StateEntry;
import org.olat.core.util.Util;
import org.olat.core.util.resource.OresHelper;
import org.olat.course.assessment.AssessmentModule;
import org.olat.course.assessment.ui.tool.AssessmentEventToState;
import org.olat.modules.assessment.ui.event.UserSelectionEvent;
import org.olat.repository.RepositoryEntry;

/**
 * 
 * Initial date: 24.05.2016<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class AssessmentToolController extends MainLayoutBasicController implements Activateable2 {

	private RepositoryEntry testEntry;
	private final AssessableResource element;
	private final AssessmentToolContainer toolContainer;
	private final AssessmentToolSecurityCallback assessmentCallback;
	private final AssessmentEventToState assessmentEventToState;
	
	private Link usersLink;
	private final TooledStackedPanel stackPanel;
	
	private AssessedIdentityListController currentCtl;
	private AssessmentOverviewController overviewCtrl;
	
	public AssessmentToolController(UserRequest ureq, WindowControl wControl, TooledStackedPanel stackPanel,
			RepositoryEntry testEntry, AssessableResource element, AssessmentToolSecurityCallback assessmentCallback) {
		super(ureq, wControl);
		setTranslator(Util.createPackageTranslator(AssessmentModule.class, getLocale(), getTranslator()));
		this.element = element;
		this.testEntry = testEntry;
		this.stackPanel = stackPanel;
		this.assessmentCallback = assessmentCallback;
		toolContainer = new AssessmentToolContainer();
		toolContainer.setCertificateMap(new ConcurrentHashMap<>());
		
		overviewCtrl = new AssessmentOverviewController(ureq, getWindowControl(), testEntry, element, assessmentCallback);
		listenTo(overviewCtrl);
		putInitialPanel(overviewCtrl.getInitialComponent());
		assessmentEventToState = new AssessmentEventToState(overviewCtrl);
	}
	
	public void initToolbar() {
		usersLink = LinkFactory.createToolLink("users", translate("users"), this, "o_icon_user");
		usersLink.setElementCssClass("o_sel_assessment_tool_users");
		stackPanel.addTool(usersLink);
	}
	
	@Override
	public void activate(UserRequest ureq, List<ContextEntry> entries, StateEntry state) {
		if(entries == null || entries.size() == 0) {
			return;
		}
		
		String resName = entries.get(0).getOLATResourceable().getResourceableTypeName();
		if ("Users".equalsIgnoreCase(resName)) {
			doSelectUsersView(ureq);
		}
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		if (source == usersLink) {
			doSelectUsersView(ureq);
		}
	}

	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if (assessmentEventToState != null && assessmentEventToState.handlesEvent(source, event)) {
			doSelectUsersView(ureq).activate(ureq, null, assessmentEventToState.getState(event));
		} else if (event instanceof UserSelectionEvent) {
			UserSelectionEvent use = (UserSelectionEvent)event;
			OLATResourceable resource = OresHelper.createOLATResourceableInstance("Identity", use.getIdentityKey());
			List<ContextEntry> entries = BusinessControlFactory.getInstance().createCEListFromString(resource);
			doSelectUsersView(ureq).activate(ureq, entries, null);
		}
		super.event(ureq, source, event);
	}
	
	private Activateable2 doSelectUsersView(UserRequest ureq) {
		if(currentCtl != null) {
			stackPanel.popController(currentCtl);
		}
		
		OLATResourceable ores = OresHelper.createOLATResourceableInstance("Users", 0l);
		WindowControl bwControl = BusinessControlFactory.getInstance().createBusinessWindowControl(ores, null, getWindowControl());
		addToHistory(ureq, bwControl);
		currentCtl = element.createIdentityList(ureq, bwControl, stackPanel, testEntry, assessmentCallback);
		listenTo(currentCtl);
		stackPanel.pushController(translate("users"), currentCtl);
		return currentCtl;
	}
}
