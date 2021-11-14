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
package org.olat.user.ui.organisation;

import java.util.List;

import org.olat.basesecurity.OrganisationModule;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.segmentedview.SegmentViewComponent;
import org.olat.core.gui.components.segmentedview.SegmentViewEvent;
import org.olat.core.gui.components.segmentedview.SegmentViewFactory;
import org.olat.core.gui.components.stack.BreadcrumbPanel;
import org.olat.core.gui.components.stack.BreadcrumbPanelAware;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.gui.control.generic.dtabs.Activateable2;
import org.olat.core.id.context.ContextEntry;
import org.olat.core.id.context.StateEntry;
import org.olat.core.util.resource.OresHelper;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 9 févr. 2018<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class OrganisationAdminController extends BasicController implements Activateable2, BreadcrumbPanelAware {
	
	private final Link configurationLink;
	private final Link organisationListLink;
	private final Link organisationTypeListLink;
	
	private BreadcrumbPanel stackPanel;
	private final VelocityContainer mainVC;
	private final SegmentViewComponent segmentView;
	
	private OrganisationTypesAdminController typeListCtrl;
	private OrganisationAdminConfigrationController configurationCtrl;
	private OrganisationsStructureAdminController organisationListCtrl;
	
	@Autowired
	private OrganisationModule organisationModule;
	
	public OrganisationAdminController(UserRequest ureq, WindowControl wControl) {
		super(ureq, wControl);
		
		mainVC = createVelocityContainer("organisation_admin");
		
		segmentView = SegmentViewFactory.createSegmentView("segments", mainVC, this);
		segmentView.setDontShowSingleSegment(true);
		configurationLink = LinkFactory.createLink("organisation.configuration", mainVC, this);
		segmentView.addSegment(configurationLink, true);
		organisationListLink = LinkFactory.createLink("organisation.structure", mainVC, this);
		organisationTypeListLink = LinkFactory.createLink("organisation.types", mainVC, this);
		doOpenConfiguration(ureq);
		if(organisationModule.isEnabled()) {
			segmentView.addSegment(organisationListLink, false);
			segmentView.addSegment(organisationTypeListLink, false);
		}
		
		mainVC.put("segmentCmp", configurationCtrl.getInitialComponent());
		putInitialPanel(mainVC);
	}
	
	@Override
	public void setBreadcrumbPanel(BreadcrumbPanel stackPanel) {
		this.stackPanel = stackPanel;
		stackPanel.changeDisplayname(translate("admin.menu.title"));
		if(organisationListCtrl != null) {
			organisationListCtrl.setBreadcrumbPanel(stackPanel);
		}
	}

	@Override
	public void activate(UserRequest ureq, List<ContextEntry> entries, StateEntry state) {
		//
	}

	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if(configurationCtrl == source) {
			if(event == Event.CHANGED_EVENT) {
				segmentView.removeSegment(organisationListLink);
				segmentView.removeSegment(organisationTypeListLink);
				if(organisationModule.isEnabled()) {
					segmentView.addSegment(organisationListLink, false);
					segmentView.addSegment(organisationTypeListLink, false);
				}
			}
		}
		super.event(ureq, source, event);
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
				} else if (clickedLink == organisationListLink){
					doOpenOrganisationList(ureq);
				} else if (clickedLink == organisationTypeListLink) {
					doOpenOrganisationTypeList(ureq);
				}
			}
		}
	}
	
	private void doOpenConfiguration(UserRequest ureq) {
		if(configurationCtrl == null) {
			WindowControl bwControl = addToHistory(ureq, OresHelper.createOLATResourceableType("Configuration"), null);
			configurationCtrl = new OrganisationAdminConfigrationController(ureq, bwControl);
			listenTo(configurationCtrl);
		}
		addToHistory(ureq, configurationCtrl);
		mainVC.put("segmentCmp", configurationCtrl.getInitialComponent());
	}
	
	private void doOpenOrganisationList(UserRequest ureq) {
		if(organisationListCtrl == null) {
			WindowControl bwControl = addToHistory(ureq, OresHelper.createOLATResourceableType("Organisations"), null);
			organisationListCtrl = new OrganisationsStructureAdminController(ureq, bwControl);
			organisationListCtrl.setBreadcrumbPanel(stackPanel);
			listenTo(organisationListCtrl);
		}
		addToHistory(ureq, organisationListCtrl);
		mainVC.put("segmentCmp", organisationListCtrl.getInitialComponent());
	}

	private void doOpenOrganisationTypeList(UserRequest ureq) {
		if(typeListCtrl == null) {
			WindowControl bwControl = addToHistory(ureq, OresHelper.createOLATResourceableType("OrganisationTypes"), null);
			typeListCtrl = new OrganisationTypesAdminController(ureq, bwControl);
			listenTo(typeListCtrl);
		}
		addToHistory(ureq, typeListCtrl);
		mainVC.put("segmentCmp", typeListCtrl.getInitialComponent());
	}
}
