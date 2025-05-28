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
import org.olat.core.gui.components.stack.BreadcrumbPanel;
import org.olat.core.gui.components.stack.BreadcrumbPanelAware;
import org.olat.core.gui.components.stack.BreadcrumbedStackedPanel;
import org.olat.core.gui.components.stack.PopEvent;
import org.olat.core.gui.components.tabbedpane.TabbedPane;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.gui.control.generic.dtabs.Activateable2;
import org.olat.core.id.context.BusinessControlFactory;
import org.olat.core.id.context.ContextEntry;
import org.olat.core.id.context.StateEntry;
import org.olat.core.util.resource.OresHelper;
import org.olat.modules.taxonomy.Taxonomy;
import org.olat.modules.taxonomy.TaxonomyLevel;
import org.olat.modules.taxonomy.TaxonomySecurityCallback;
import org.olat.modules.taxonomy.ui.events.OpenTaxonomyLevelEvent;

/**
 * 
 * Initial date: 10 nov. 2017<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class TaxonomyOverviewController extends BasicController implements BreadcrumbPanelAware, Activateable2 {
	
	private int metadataTab;
	private int typeListTab;
	private int levelsTab;
	private int lostFoundTab;
	
	private BreadcrumbedStackedPanel stackPanel;
	private final VelocityContainer mainVC;
	private final TabbedPane tabPane;
	
	private EditTaxonomyController metadataCtrl;
	private TaxonomyTreeTableController taxonomyLevelsCtrl;
	private TaxonomyLevelTypesEditController typeListCtrl;
	private TaxonomyLostAndfoundDocumentsController lostFoundCtrl;
	
	private Taxonomy taxonomy;
	private TaxonomySecurityCallback secCallback;
	
	public TaxonomyOverviewController(UserRequest ureq, WindowControl wControl, TaxonomySecurityCallback secCallback, Taxonomy taxonomy) {
		super(ureq, wControl);
		this.taxonomy = taxonomy;
		this.secCallback = secCallback;
		
		mainVC = createVelocityContainer("taxonomy_overview");
		
		tabPane = new TabbedPane("tabs", ureq.getLocale());
		tabPane.setElementCssClass("o_sel_taxonomy_abs");
		tabPane.addListener(this);
		initTabPane(ureq);
		mainVC.put("tabs", tabPane);

		putInitialPanel(mainVC);
		updateProperties();
	}
	
	public Taxonomy getTaxonomy() {
		return taxonomy;
	}
	
	private void initTabPane(UserRequest ureq) {
		levelsTab = tabPane.addTab(ureq, translate("taxonomy.levels.tab"), "o_sel_taxonomy_levels", uureq -> {
			removeAsListenerAndDispose(taxonomyLevelsCtrl);
			WindowControl bwControl = addToHistory(uureq, OresHelper.createOLATResourceableType("Levels"), null);
			taxonomyLevelsCtrl = new TaxonomyTreeTableController(uureq, bwControl, secCallback, taxonomy, null);
			taxonomyLevelsCtrl.setBreadcrumbPanel(stackPanel);
			listenTo(taxonomyLevelsCtrl);
			return taxonomyLevelsCtrl.getInitialComponent();
		}, true);
		
		metadataTab = tabPane.addTab(ureq, translate("taxonomy.metadata"), "o_sel_taxonomy_metadata", uureq -> {
			removeAsListenerAndDispose(metadataCtrl);
			WindowControl bwControl = addToHistory(uureq, OresHelper.createOLATResourceableType("Metadata"), null);
			metadataCtrl = new EditTaxonomyController(uureq, bwControl, secCallback, taxonomy);
			listenTo(metadataCtrl);
			return metadataCtrl.getInitialComponent();
		}, true);
		
		if (secCallback.canViewLevelTypes()) {
			typeListTab = tabPane.addTab(ureq, translate("taxonomy.types"), "o_sel_taxonomy_levels", uureq -> {
				removeAsListenerAndDispose(typeListCtrl);
				WindowControl bwControl = addToHistory(uureq, OresHelper.createOLATResourceableType("Types"), null);
				typeListCtrl = new TaxonomyLevelTypesEditController(uureq, bwControl, taxonomy);
				listenTo(typeListCtrl);
				return typeListCtrl.getInitialComponent();
			}, true);
		}
		
		if (secCallback.canViewLostFound()) {
			lostFoundTab = tabPane.addTab(ureq, translate("taxonomy.lost.found"), "o_sel_taxonomy_lost_found", uureq -> {
				removeAsListenerAndDispose(lostFoundCtrl);
				WindowControl bwControl = addToHistory(uureq, OresHelper.createOLATResourceableType("Lostfound"), null);
				lostFoundCtrl = new TaxonomyLostAndfoundDocumentsController(uureq, bwControl, taxonomy);
				listenTo(lostFoundCtrl);
				return lostFoundCtrl.getInitialComponent();
			}, true);
		}
	}
	
	private void updateProperties() {
		mainVC.contextPut("title", taxonomy.getDisplayName());
		mainVC.contextPut("identifier", taxonomy.getIdentifier());
	}
	
	@Override
	public void setBreadcrumbPanel(BreadcrumbPanel stackPanel) {
		if (stackPanel instanceof BreadcrumbedStackedPanel breadcrumbedStackedPanel) {
			this.stackPanel = breadcrumbedStackedPanel;
			this.stackPanel.addListener(this);
		}
		if(taxonomyLevelsCtrl != null) {
			taxonomyLevelsCtrl.setBreadcrumbPanel(stackPanel);
		}
	}
	
	@Override
	public synchronized void dispose() {
		if (stackPanel != null) {
			stackPanel.removeListener(this);
		}
		super.dispose();
	}

	@Override
	public void activate(UserRequest ureq, List<ContextEntry> entries, StateEntry state) {
		if(entries == null || entries.isEmpty()) return;
		
		String type = entries.get(0).getOLATResourceable().getResourceableTypeName();
		if("Metadata".equalsIgnoreCase(type)) {
			tabPane.setSelectedPane(ureq, metadataTab);
		} else if("Types".equalsIgnoreCase(type)) {
			tabPane.setSelectedPane(ureq, typeListTab);
			List<ContextEntry> subEntries = entries.subList(1, entries.size());
			typeListCtrl.activate(ureq, subEntries, entries.get(0).getTransientState());
		} else if("Levels".equalsIgnoreCase(type)) {
			tabPane.setSelectedPane(ureq, levelsTab);
			List<ContextEntry> subEntries = entries.subList(1, entries.size());
			taxonomyLevelsCtrl.activate(ureq, subEntries, entries.get(0).getTransientState());
		} else if("Lostfound".equalsIgnoreCase(type)) {
			tabPane.setSelectedPane(ureq, lostFoundTab);
		}
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		if (stackPanel == source) {
			if(event instanceof PopEvent pe) {
				doProcessPopEvent(ureq, pe);
			}
		}
	}
	
	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if (source == taxonomyLevelsCtrl) {
			if (event instanceof OpenTaxonomyLevelEvent openEvent) {
				doOpenTaxonomyLevel(ureq, openEvent.getTaxonomyLevel());
			}
		} else if(metadataCtrl == source) {
			if (event == Event.DONE_EVENT) {
				taxonomy = metadataCtrl.getTaxonomy();
				updateProperties();
			} else if (event == Event.CANCELLED_EVENT) {
				fireEvent(ureq, event);//propagate cancel
			}
		}
		super.event(ureq, source, event);
	}
	
	private void doProcessPopEvent(UserRequest ureq, PopEvent pe) {
		if (stackPanel.getLastController() == this) {
			// Only pop up to this
		} else if (pe.getUserObject() instanceof TaxonomyLevel taxonomyLevel) {
			// Click on intermediate crumb
			doOpenTaxonomyLevel(ureq, taxonomyLevel.getParent());
		}
	}
	
	private void doOpenTaxonomyLevel(UserRequest ureq, TaxonomyLevel taxonomyLevel) {
		stackPanel.popUpToController(this);
		
		tabPane.setSelectedPane(ureq, levelsTab);
		if (taxonomyLevelsCtrl != null) {
			List<ContextEntry> entries = BusinessControlFactory.getInstance()
					.createCEListFromResourceable(taxonomyLevel, null);
			taxonomyLevelsCtrl.activate(ureq, entries, null);
		}
	}

}