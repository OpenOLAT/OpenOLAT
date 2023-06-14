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
package org.olat.modules.project.ui;

import java.util.List;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.segmentedview.SegmentViewComponent;
import org.olat.core.gui.components.segmentedview.SegmentViewEvent;
import org.olat.core.gui.components.segmentedview.SegmentViewFactory;
import org.olat.core.gui.components.stack.BreadcrumbedStackedPanel;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.gui.control.generic.dtabs.Activateable2;
import org.olat.core.id.context.ContextEntry;
import org.olat.core.id.context.StateEntry;
import org.olat.core.util.resource.OresHelper;

/**
 * 
 * Initial date: 21 Nov 2022<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class ProjectsSiteController extends BasicController implements Activateable2 {
	
	private static final String ORES_TYPE_ADMIN = "Admin";
	
	private final VelocityContainer mainVC;
	private final SegmentViewComponent segmentView;
	private final Link myLink;
	private final Link templatesLink;
	private Link adminLink;
	
	private BreadcrumbedStackedPanel myStackPanel;
	private BreadcrumbedStackedPanel templatesStackPanel;
	private BreadcrumbedStackedPanel adminStackPanel;
	private ProjProjectMyController myCtrl;
	private ProjProjectTemplatesController templatesCtrl;
	private ProjProjectAdminController adminCtrl;

	public ProjectsSiteController(UserRequest ureq, WindowControl wControl) {
		super(ureq, wControl);
		
		mainVC = createVelocityContainer("main");
		putInitialPanel(mainVC);

		segmentView = SegmentViewFactory.createSegmentView("segments", mainVC, this);
		segmentView.setDontShowSingleSegment(true);
		
		myLink = LinkFactory.createLink("segment.my", mainVC, this);
		segmentView.addSegment(myLink, true);
		
		templatesLink = LinkFactory.createLink("segment.templates", mainVC, this);
		segmentView.addSegment(templatesLink, false);
		
		if (ureq.getUserSession().getRoles().isProjectManager() || ureq.getUserSession().getRoles().isAdministrator()) {
			adminLink = LinkFactory.createLink("segment.admin", mainVC, this);
			segmentView.addSegment(adminLink, false);
		}
		
		if (segmentView.getSegments().size() > 1) {
			mainVC.contextPut("cssClass", "o_block_top");
		}
	}

	@Override
	public void activate(UserRequest ureq, List<ContextEntry> entries, StateEntry state) {
		if (entries != null && entries.size() > 0) {
			String resName = entries.get(0).getOLATResourceable().getResourceableTypeName();
			if (ProjectBCFactory.TYPE_PROJECT.equalsIgnoreCase(resName)) {
				doOpenMy(ureq);
				myCtrl.activate(ureq, entries, state);
			}
		} else if (myCtrl == null) {
			doOpenMy(ureq);
		}
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		if (source == segmentView) {
			if (event instanceof SegmentViewEvent) {
				SegmentViewEvent sve = (SegmentViewEvent)event;
				String segmentCName = sve.getComponentName();
				Component clickedLink = mainVC.getComponent(segmentCName);
				if (clickedLink == myLink) {
					doOpenMy(ureq);
				} else if (clickedLink == templatesLink) {
					doOpenTemplates(ureq);
				} else if (clickedLink == adminLink) {
					doOpenAdmin(ureq);
				}
			}
		}
	}
	
	public void doOpenMy(UserRequest ureq) {
		removeAsListenerAndDispose(myCtrl);
		
		myStackPanel = new BreadcrumbedStackedPanel("mystack", getTranslator(), this);
		myCtrl = new ProjProjectMyController(ureq, getWindowControl(), myStackPanel);
		listenTo(myCtrl);
		myStackPanel.pushController(translate("project.list.title"), myCtrl);
		
		mainVC.put("segmentCmp", myStackPanel);
		segmentView.select(myLink);
	}
	
	public void doOpenTemplates(UserRequest ureq) {
		removeAsListenerAndDispose(templatesCtrl);
		
		templatesStackPanel = new BreadcrumbedStackedPanel("templatestack", getTranslator(), this);
		templatesCtrl = new ProjProjectTemplatesController(ureq, getWindowControl(), templatesStackPanel);
		listenTo(templatesCtrl);
		templatesStackPanel.pushController(translate("project.list.title"), templatesCtrl);
		
		mainVC.put("segmentCmp", templatesStackPanel);
		segmentView.select(templatesLink);
	}
	
	private void doOpenAdmin(UserRequest ureq) {
		removeAsListenerAndDispose(adminCtrl);
		
		adminStackPanel = new BreadcrumbedStackedPanel("adminstack", getTranslator(), this);
		WindowControl swControl = addToHistory(ureq, OresHelper.createOLATResourceableType(ORES_TYPE_ADMIN), null);
		adminCtrl = new ProjProjectAdminController(ureq, swControl, adminStackPanel);
		listenTo(adminCtrl);
		adminStackPanel.pushController(translate("project.list.title"), adminCtrl);
		
		mainVC.put("segmentCmp", adminStackPanel);
		segmentView.select(adminLink);
	}
	
}
