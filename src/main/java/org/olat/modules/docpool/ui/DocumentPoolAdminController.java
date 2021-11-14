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
package org.olat.modules.docpool.ui;

import java.util.List;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.segmentedview.SegmentViewComponent;
import org.olat.core.gui.components.segmentedview.SegmentViewEvent;
import org.olat.core.gui.components.segmentedview.SegmentViewFactory;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.gui.control.generic.dtabs.Activateable2;
import org.olat.core.id.context.ContextEntry;
import org.olat.core.id.context.StateEntry;
import org.olat.core.util.StringHelper;
import org.olat.core.util.resource.OresHelper;
import org.olat.modules.docpool.DocumentPoolModule;
import org.olat.modules.taxonomy.Taxonomy;
import org.olat.modules.taxonomy.TaxonomyService;
import org.olat.modules.taxonomy.model.TaxonomyRefImpl;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 10 nov. 2017<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class DocumentPoolAdminController extends BasicController implements Activateable2 {
	
	private final VelocityContainer mainVC;
	private final SegmentViewComponent segmentView;
	private final Link configurationLink, permissionsLink, infosPageLink;
	
	private DocumentPoolInfoPageController infosPageCtrl;
	private DocumentPoolAdminPermissionsController permissionsCtrl;
	private DocumentPoolAdminConfigurationController configurationCtrl;
	
	@Autowired
	private TaxonomyService taxonomyService;
	@Autowired
	private DocumentPoolModule docPoolModule;
	
	public DocumentPoolAdminController(UserRequest ureq, WindowControl wControl) {
		super(ureq, wControl);
		
		mainVC = createVelocityContainer("document_pool_admin");
		
		segmentView = SegmentViewFactory.createSegmentView("segments", mainVC, this);
		segmentView.setDontShowSingleSegment(true);
		configurationLink = LinkFactory.createLink("document.pool.configuration", mainVC, this);
		segmentView.addSegment(configurationLink, true);
		doOpenConfiguration(ureq);
		permissionsLink = LinkFactory.createLink("document.pool.permissions", mainVC, this);
		infosPageLink = LinkFactory.createLink("document.pool.infos.page", mainVC, this);
		if(docPoolModule.isEnabled()) {
			segmentView.addSegment(permissionsLink, false);
			segmentView.addSegment(infosPageLink, false);
		}
		putInitialPanel(mainVC);
	}

	@Override
	public void activate(UserRequest ureq, List<ContextEntry> entries, StateEntry state) {
		if(entries == null || entries.isEmpty()) return;
		
		String type = entries.get(0).getOLATResourceable().getResourceableTypeName();
		if("Configuration".equalsIgnoreCase(type)) {
			doOpenConfiguration(ureq);
			segmentView.select(configurationLink);
		} else if("Types".equalsIgnoreCase(type)) {
			doOpenPermissions(ureq);
			segmentView.select(permissionsLink);
		} else if("InfosPage".equalsIgnoreCase(type)) {
			doOpenInfosPage(ureq);
			segmentView.select(infosPageLink);
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
				} else if (clickedLink == permissionsLink){
					doOpenPermissions(ureq);
				} else if(clickedLink == infosPageLink) {
					doOpenInfosPage(ureq);
				}
			}
		}
	}

	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if(source == configurationCtrl) {
			if(event == Event.DONE_EVENT || event == Event.CHANGED_EVENT) {
				segmentView.removeSegment(permissionsLink);
				segmentView.removeSegment(infosPageLink);
				if(docPoolModule.isEnabled()) {
					segmentView.addSegment(permissionsLink, false);
					segmentView.addSegment(infosPageLink, false);
				}
			}
		}
		super.event(ureq, source, event);
	}

	private void doOpenConfiguration(UserRequest ureq) {
		if(configurationCtrl == null) {
			WindowControl bwControl = addToHistory(ureq, OresHelper.createOLATResourceableType("Configuration"), null);
			configurationCtrl = new DocumentPoolAdminConfigurationController(ureq, bwControl);
			listenTo(configurationCtrl);
		}
		addToHistory(ureq, configurationCtrl);
		mainVC.put("segmentCmp", configurationCtrl.getInitialComponent());
	}
	
	private void doOpenPermissions(UserRequest ureq) {
		removeAsListenerAndDispose(permissionsCtrl);
		
		if(StringHelper.containsNonWhitespace(docPoolModule.getTaxonomyTreeKey())) {
			WindowControl bwControl = addToHistory(ureq, OresHelper.createOLATResourceableType("Types"), null);
			Taxonomy taxonomy = taxonomyService.getTaxonomy(new TaxonomyRefImpl(new Long(docPoolModule.getTaxonomyTreeKey())));
			permissionsCtrl = new DocumentPoolAdminPermissionsController(ureq, bwControl, taxonomy);
			listenTo(permissionsCtrl);
			mainVC.put("segmentCmp", permissionsCtrl.getInitialComponent());
		}
	}
	
	private void doOpenInfosPage(UserRequest ureq) {
		if(infosPageCtrl == null) {
			WindowControl bwControl = addToHistory(ureq, OresHelper.createOLATResourceableType("InfosPage"), null);
			infosPageCtrl = new DocumentPoolInfoPageController(ureq, bwControl);
			listenTo(infosPageCtrl);
		}
		addToHistory(ureq, infosPageCtrl);
		mainVC.put("segmentCmp", infosPageCtrl.getInitialComponent());
	}
}