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
package org.olat.modules.taxonomy.ui;

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
import org.olat.modules.taxonomy.Taxonomy;

/**
 * 
 * Initial date: 10 nov. 2017<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class TaxonomyOverviewController extends BasicController implements BreadcrumbPanelAware {
	
	private BreadcrumbPanel stackPanel;
	private final VelocityContainer mainVC;
	private final SegmentViewComponent segmentView;
	private final Link metadataLink, typesLink, levelsLink, oldTreeLink;
	
	private TaxonomyTreeController oldTreeCtrl;
	private EditTaxonomyController metadataCtrl;
	private TaxonomyTreeTableController taxonomyCtrl;
	private TaxonomyLevelTypesEditController typeListCtrl;
	
	private Taxonomy taxonomy;
	
	public TaxonomyOverviewController(UserRequest ureq, WindowControl wControl, Taxonomy taxonomy) {
		super(ureq, wControl);
		this.taxonomy = taxonomy;
		
		mainVC = createVelocityContainer("taxonomy_overview");
		
		segmentView = SegmentViewFactory.createSegmentView("segments", mainVC, this);
		metadataLink = LinkFactory.createLink("taxonomy.metadata", mainVC, this);
		segmentView.addSegment(metadataLink, true);
		doOpenMetadata(ureq);
		
		typesLink = LinkFactory.createLink("taxonomy.types", mainVC, this);
		segmentView.addSegment(typesLink, false);
		levelsLink = LinkFactory.createLink("taxonomy.levels", mainVC, this);
		segmentView.addSegment(levelsLink, false);
		oldTreeLink = LinkFactory.createLink("taxonomy.levels.tree", mainVC, this);
		segmentView.addSegment(oldTreeLink, false);
		
		putInitialPanel(mainVC);
	}

	@Override
	public void setBreadcrumbPanel(BreadcrumbPanel stackPanel) {
		this.stackPanel = stackPanel;
		if(taxonomyCtrl != null) {
			taxonomyCtrl.setBreadcrumbPanel(stackPanel);
		}	
	}

	@Override
	protected void doDispose() {
		//
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		if(source == segmentView) {
			if(event instanceof SegmentViewEvent) {
				SegmentViewEvent sve = (SegmentViewEvent)event;
				String segmentCName = sve.getComponentName();
				Component clickedLink = mainVC.getComponent(segmentCName);
				if (clickedLink == metadataLink) {
					doOpenMetadata(ureq);
				} else if (clickedLink == typesLink){
					doOpenTypes(ureq);
				} else if (clickedLink == levelsLink){
					doOpenTaxonomyLevels(ureq);
				} else if (clickedLink == oldTreeLink){
					doOpenTaxonomyLevelsOldTree(ureq);
				}
			}
		}
	}
	
	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if(metadataCtrl == source) {
			if(event == Event.CANCELLED_EVENT) {
				fireEvent(ureq, event);//propagate cancel
			}
		}
		super.event(ureq, source, event);
	}

	private void doOpenMetadata(UserRequest ureq) {
		if(metadataCtrl == null) {
			metadataCtrl = new EditTaxonomyController(ureq, getWindowControl(), taxonomy);
			listenTo(metadataCtrl);
		}
		mainVC.put("segmentCmp", metadataCtrl.getInitialComponent());
	}
	
	private void doOpenTypes(UserRequest ureq) {
		if(typeListCtrl == null) {
			typeListCtrl = new TaxonomyLevelTypesEditController(ureq, getWindowControl(), taxonomy);
			listenTo(typeListCtrl);
		}
		mainVC.put("segmentCmp", typeListCtrl.getInitialComponent());
	}
	
	private void doOpenTaxonomyLevels(UserRequest ureq) {
		if(taxonomyCtrl == null) {
			taxonomyCtrl = new TaxonomyTreeTableController(ureq, getWindowControl(), taxonomy);
			taxonomyCtrl.setBreadcrumbPanel(stackPanel);
			listenTo(taxonomyCtrl);
		}
		mainVC.put("segmentCmp", taxonomyCtrl.getInitialComponent());
	}
	
	private void doOpenTaxonomyLevelsOldTree(UserRequest ureq) {
		if(oldTreeCtrl == null) {
			oldTreeCtrl = new TaxonomyTreeController(ureq, getWindowControl(), taxonomy);
			listenTo(oldTreeCtrl);
		}
		mainVC.put("segmentCmp", oldTreeCtrl.getInitialComponent());
	}
}