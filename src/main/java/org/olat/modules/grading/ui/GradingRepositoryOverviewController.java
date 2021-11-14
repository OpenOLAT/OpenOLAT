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
package org.olat.modules.grading.ui;

import java.util.List;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.segmentedview.SegmentViewComponent;
import org.olat.core.gui.components.segmentedview.SegmentViewEvent;
import org.olat.core.gui.components.segmentedview.SegmentViewFactory;
import org.olat.core.gui.components.stack.BreadcrumbPanel;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.gui.control.generic.dtabs.Activateable2;
import org.olat.core.id.context.ContextEntry;
import org.olat.core.id.context.StateEntry;
import org.olat.core.util.resource.OresHelper;
import org.olat.modules.grading.GradingSecurityCallback;
import org.olat.modules.grading.GradingSecurityCallbackFactory;
import org.olat.modules.grading.GradingService;
import org.olat.modules.grading.RepositoryEntryGradingConfiguration;
import org.olat.modules.grading.ui.event.OpenAssignmentsEvent;
import org.olat.repository.RepositoryEntry;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 13 janv. 2020<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class GradingRepositoryOverviewController extends BasicController implements Activateable2 {
	
	private final Link gradersLink;
	private final Link assignmentsLink;
	private final Link configurationLink;
	private final VelocityContainer mainVC;
	private final BreadcrumbPanel stackPanel;
	private final SegmentViewComponent segmentView;
	
	private GradersListController gradersCtrl;
	private GradingAssignmentsListController assignmentsCtrl;
	private GradingRepositoryEntryConfigurationController configurationCtrl;
	
	private RepositoryEntry entry;
	private RepositoryEntryGradingConfiguration configuration;
	
	@Autowired
	private GradingService gradingService;
	
	public GradingRepositoryOverviewController(UserRequest ureq, WindowControl wControl, BreadcrumbPanel stackPanel,
			RepositoryEntry entry) {
		super(ureq, wControl);
		this.entry = entry;
		this.stackPanel = stackPanel;
		configuration = gradingService.getOrCreateConfiguration(entry);
		
		mainVC = createVelocityContainer("overview");
		segmentView = SegmentViewFactory.createSegmentView("segments", mainVC, this);
		segmentView.setDontShowSingleSegment(true);
		
		configurationLink = LinkFactory.createLink("repository.configuration", mainVC, this);
		segmentView.addSegment(configurationLink, true);
		doOpenConfiguration(ureq);
		
		gradersLink = LinkFactory.createLink("repository.graders", mainVC, this);
		segmentView.addSegment(gradersLink, false);
		assignmentsLink = LinkFactory.createLink("repository.assignments", mainVC, this);
		segmentView.addSegment(assignmentsLink, false);
		
		putInitialPanel(mainVC);
	}

	@Override
	public void activate(UserRequest ureq, List<ContextEntry> entries, StateEntry state) {
		if(entries == null || entries.isEmpty()) return;
		
		String type = entries.get(0).getOLATResourceable().getResourceableTypeName();
		if("Configuration".equalsIgnoreCase(type)) {
			doOpenConfiguration(ureq);
			segmentView.select(configurationLink);
		} else if("Assignments".equalsIgnoreCase(type)) {
			doOpenAssignments(ureq);
			segmentView.select(assignmentsLink);
		} else if("Graders".equalsIgnoreCase(type)) {
			doOpenGraderList(ureq);
			segmentView.select(gradersLink);
		} 
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		if(source == segmentView) {
			if(event instanceof SegmentViewEvent) {
				SegmentViewEvent sve = (SegmentViewEvent)event;
				String segmentCName = sve.getComponentName();
				Component clickedLink = mainVC.getComponent(segmentCName);
				if (clickedLink == configurationLink) {
					doOpenConfiguration(ureq);
				} else if (clickedLink == gradersLink) {
					doOpenGraderList(ureq);
				} else if (clickedLink == assignmentsLink) {
					doOpenAssignments(ureq);
				}
			}
		}
	}
	
	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if(gradersCtrl == source) {
			if(event instanceof OpenAssignmentsEvent) {
				doOpenAssignments(ureq).activate((OpenAssignmentsEvent)event);
				segmentView.select(assignmentsLink);
			}
		} else if(configurationCtrl == source) {
			if(event == Event.CHANGED_EVENT) {
				reloadLists();
			}
		}
	}
	
	private void reloadLists() {
		removeAsListenerAndDispose(assignmentsCtrl);
		removeAsListenerAndDispose(gradersCtrl);
		assignmentsCtrl = null;
		gradersCtrl = null;
	}
	
	private GradingAssignmentsListController doOpenAssignments(UserRequest ureq) {
		if(assignmentsCtrl == null) {
			WindowControl swControl = addToHistory(ureq, OresHelper.createOLATResourceableType("Assignments"), null);
			GradingSecurityCallback secCallback = GradingSecurityCallbackFactory
					.getManagerCalllback(getIdentity(), ureq.getUserSession().getRoles());
			assignmentsCtrl = new GradingAssignmentsListController(ureq, swControl, entry, secCallback);
			listenTo(assignmentsCtrl);
			assignmentsCtrl.setBreadcrumbPanel(stackPanel);
		}
		mainVC.put("segmentCmp", assignmentsCtrl.getInitialComponent());
		addToHistory(ureq, assignmentsCtrl);
		return assignmentsCtrl;	
	}
	
	private GradersListController doOpenGraderList(UserRequest ureq) {
		if(gradersCtrl == null) {
			WindowControl swControl = addToHistory(ureq, OresHelper.createOLATResourceableType("Graders"), null);
			GradingSecurityCallback secCallback = GradingSecurityCallbackFactory
					.getManagerCalllback(getIdentity(), ureq.getUserSession().getRoles());
			gradersCtrl = new GradersListController(ureq, swControl, entry, secCallback);
			listenTo(gradersCtrl);
		}
		mainVC.put("segmentCmp", gradersCtrl.getInitialComponent());
		addToHistory(ureq, gradersCtrl);
		return gradersCtrl;
	}
	
	private GradingRepositoryEntryConfigurationController doOpenConfiguration(UserRequest ureq) {
		if(configurationCtrl == null) {
			WindowControl swControl = addToHistory(ureq, OresHelper.createOLATResourceableType("Configuration"), null);
			configurationCtrl = new GradingRepositoryEntryConfigurationController(ureq, swControl, entry, configuration);
			listenTo(configurationCtrl);
		}
		mainVC.put("segmentCmp", configurationCtrl.getInitialComponent());
		addToHistory(ureq, configurationCtrl);
		return configurationCtrl;
	}
}
