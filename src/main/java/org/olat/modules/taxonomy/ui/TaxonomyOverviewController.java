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

import java.util.List;

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
import org.olat.modules.taxonomy.Taxonomy;

/**
 * 
 * Initial date: 10 nov. 2017<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class TaxonomyOverviewController extends BasicController implements BreadcrumbPanelAware, Activateable2 {
	
	private BreadcrumbPanel stackPanel;
	private final VelocityContainer mainVC;
	private final SegmentViewComponent segmentView;
	private final Link metadataLink, typesLink, levelsLink, lostFoundLink;
	
	private EditTaxonomyController metadataCtrl;
	private TaxonomyTreeTableController taxonomyCtrl;
	private TaxonomyLevelTypesEditController typeListCtrl;
	private TaxonomyLostAndfoundDocumentsController lostFoundCtrl;
	
	private Taxonomy taxonomy;
	
	public TaxonomyOverviewController(UserRequest ureq, WindowControl wControl, Taxonomy taxonomy) {
		super(ureq, wControl);
		this.taxonomy = taxonomy;
		
		mainVC = createVelocityContainer("taxonomy_overview");
		
		segmentView = SegmentViewFactory.createSegmentView("segments", mainVC, this);
		metadataLink = LinkFactory.createLink("taxonomy.metadata", mainVC, this);
		metadataLink.setElementCssClass("o_sel_taxonomy_metadata");
		segmentView.addSegment(metadataLink, true);
		doOpenMetadata(ureq);
		
		typesLink = LinkFactory.createLink("taxonomy.types", mainVC, this);
		typesLink.setElementCssClass("o_sel_taxonomy_types");
		segmentView.addSegment(typesLink, false);
		levelsLink = LinkFactory.createLink("taxonomy.levels", mainVC, this);
		levelsLink.setElementCssClass("o_sel_taxonomy_levels");
		segmentView.addSegment(levelsLink, false);
		lostFoundLink = LinkFactory.createLink("taxonomy.lost.found", mainVC, this);
		lostFoundLink.setElementCssClass("o_sel_taxonomy_lost_found");
		segmentView.addSegment(lostFoundLink, false);

		putInitialPanel(mainVC);
	}
	
	public Taxonomy getTaxonomy() {
		return taxonomy;
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
	public void activate(UserRequest ureq, List<ContextEntry> entries, StateEntry state) {
		if(entries == null || entries.isEmpty()) return;
		
		String type = entries.get(0).getOLATResourceable().getResourceableTypeName();
		if("Metadata".equalsIgnoreCase(type)) {
			doOpenMetadata(ureq);
			segmentView.select(metadataLink);
		} else if("Types".equalsIgnoreCase(type)) {
			List<ContextEntry> subEntries = entries.subList(1, entries.size());
			doOpenTypes(ureq).activate(ureq, subEntries, entries.get(0).getTransientState());
			segmentView.select(typesLink);
		} else if("Levels".equalsIgnoreCase(type)) {
			List<ContextEntry> subEntries = entries.subList(1, entries.size());
			doOpenTaxonomyLevels(ureq).activate(ureq, subEntries, entries.get(0).getTransientState());
			segmentView.select(levelsLink);
		} else if("Lostfound".equalsIgnoreCase(type)) {
			doOpenLostFound(ureq);
			segmentView.select(lostFoundLink);
		}
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
				} else if (clickedLink == typesLink) {
					doOpenTypes(ureq);
				} else if (clickedLink == levelsLink) {
					doOpenTaxonomyLevels(ureq);
				} else if(clickedLink == lostFoundLink) {
					doOpenLostFound(ureq);
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
			WindowControl bwControl = addToHistory(ureq, OresHelper.createOLATResourceableType("Metadata"), null);
			metadataCtrl = new EditTaxonomyController(ureq, bwControl, taxonomy);
			listenTo(metadataCtrl);
		}
		mainVC.put("segmentCmp", metadataCtrl.getInitialComponent());
	}
	
	private TaxonomyLevelTypesEditController doOpenTypes(UserRequest ureq) {
		if(typeListCtrl == null) {
			WindowControl bwControl = addToHistory(ureq, OresHelper.createOLATResourceableType("Types"), null);
			typeListCtrl = new TaxonomyLevelTypesEditController(ureq, bwControl, taxonomy);
			listenTo(typeListCtrl);
		} else {
			typeListCtrl.loadModel();
		}
		mainVC.put("segmentCmp", typeListCtrl.getInitialComponent());
		return typeListCtrl;
	}
	
	private TaxonomyTreeTableController doOpenTaxonomyLevels(UserRequest ureq) {
		if(taxonomyCtrl == null) {
			WindowControl bwControl = addToHistory(ureq, OresHelper.createOLATResourceableType("Levels"), null);
			taxonomyCtrl = new TaxonomyTreeTableController(ureq, bwControl, taxonomy);
			taxonomyCtrl.setBreadcrumbPanel(stackPanel);
			listenTo(taxonomyCtrl);
		}
		mainVC.put("segmentCmp", taxonomyCtrl.getInitialComponent());
		return taxonomyCtrl;
	}
	
	private void doOpenLostFound(UserRequest ureq) {
		if(lostFoundCtrl == null) {
			WindowControl bwControl = addToHistory(ureq, OresHelper.createOLATResourceableType("Lostfound"), null);
			lostFoundCtrl = new TaxonomyLostAndfoundDocumentsController(ureq, bwControl, taxonomy);
			listenTo(lostFoundCtrl);
		}
		mainVC.put("segmentCmp", lostFoundCtrl.getInitialComponent());
	}
}