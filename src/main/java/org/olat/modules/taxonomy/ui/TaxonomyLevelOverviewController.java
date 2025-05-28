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

import java.util.Collections;
import java.util.List;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.dropdown.Dropdown;
import org.olat.core.gui.components.dropdown.DropdownUIFactory;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.stack.BreadcrumbPanel;
import org.olat.core.gui.components.tabbedpane.TabbedPane;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.gui.control.generic.closablewrapper.CloseableCalloutWindowController;
import org.olat.core.gui.control.generic.closablewrapper.CloseableModalController;
import org.olat.core.gui.control.generic.dtabs.Activateable2;
import org.olat.core.id.context.ContextEntry;
import org.olat.core.id.context.StateEntry;
import org.olat.core.util.StringHelper;
import org.olat.core.util.vfs.VFSLeaf;
import org.olat.core.util.vfs.VFSMediaMapper;
import org.olat.modules.catalog.CatalogV2Module;
import org.olat.modules.taxonomy.Taxonomy;
import org.olat.modules.taxonomy.TaxonomyLevel;
import org.olat.modules.taxonomy.TaxonomyLevelManagedFlag;
import org.olat.modules.taxonomy.TaxonomyLevelType;
import org.olat.modules.taxonomy.TaxonomySecurityCallback;
import org.olat.modules.taxonomy.TaxonomyService;
import org.olat.modules.taxonomy.ui.events.DeleteTaxonomyLevelEvent;
import org.olat.modules.taxonomy.ui.events.MoveTaxonomyLevelEvent;
import org.olat.modules.taxonomy.ui.events.NewTaxonomyLevelEvent;
import org.olat.modules.taxonomy.ui.events.OpenTaxonomyLevelEvent;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 14 nov. 2017<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class TaxonomyLevelOverviewController extends BasicController implements Activateable2 {

	private final BreadcrumbPanel stackPanel;
	private Dropdown cmdsDropDown;
	private Link moveLink;
	private Link newLink;
	private Link deleteLink;
	private final TabbedPane tabPane;
	private final VelocityContainer mainVC;
	
	private CloseableModalController cmc;
	private EditTaxonomyLevelController metadataCtrl;
	private MoveTaxonomyLevelController moveLevelCtrl;
	private TaxonomyTreeTableController taxonomyLevelsCtrl;
	private TaxonomyLevelRelationsController relationsCtrl;
	private DeleteTaxonomyLevelController confirmDeleteCtrl;
	private TaxonomyLevelManagementController managementCtrl;
	private TaxonomyLevelCompetenceController competencesCtrl;
	private CloseableCalloutWindowController actionsCalloutCtrl;
	private EditTaxonomyLevelController createTaxonomyLevelCtrl;
	
	private final TaxonomySecurityCallback secCallback;
	private TaxonomyLevel taxonomyLevel;

	@Autowired
	private TaxonomyService taxonomyService;
	@Autowired
	private CatalogV2Module catalogV2Module;
	
	public TaxonomyLevelOverviewController(UserRequest ureq, WindowControl wControl, BreadcrumbPanel stackPanel,
			TaxonomySecurityCallback secCallback, TaxonomyLevel taxonomyLevel) {
		super(ureq, wControl);
		this.stackPanel = stackPanel;
		this.secCallback = secCallback;
		this.taxonomyLevel = taxonomyLevel;

		mainVC = createVelocityContainer("taxonomy_level_overview");
		
		cmdsDropDown = DropdownUIFactory.createMoreDropdown("actions", getTranslator());
		cmdsDropDown.setButton(true);
		cmdsDropDown.setEmbbeded(true);
		mainVC.put("actions", cmdsDropDown);
		
		if(secCallback.canMove(taxonomyLevel) && !TaxonomyLevelManagedFlag.isManaged(taxonomyLevel, TaxonomyLevelManagedFlag.move)) {
			moveLink = LinkFactory.createToolLink("move", translate("move.taxonomy.level"), this, "o_icon_move");
			cmdsDropDown.addComponent(moveLink);
		}
		
		if (secCallback.canCreateChild(taxonomyLevel)) {
			newLink = LinkFactory.createToolLink("new", translate("add.taxonomy.level.under"), this, "o_icon_taxonomy_levels");
			cmdsDropDown.addComponent(newLink);
		}
		
		if(secCallback.canDelete(taxonomyLevel) && !TaxonomyLevelManagedFlag.isManaged(taxonomyLevel, TaxonomyLevelManagedFlag.delete)) {
			deleteLink = LinkFactory.createToolLink("delete", translate("delete"), this, "o_icon_delete_item");
			cmdsDropDown.addComponent(deleteLink);
		}
		cmdsDropDown.setVisible(cmdsDropDown.size() > 0);
		
		tabPane = new TabbedPane("tabs", ureq.getLocale());
		tabPane.setElementCssClass("o_sel_taxonomy_level_tabs");
		tabPane.addListener(this);
		initTabPane(ureq);
		mainVC.put("tabs", tabPane);
		
		putInitialPanel(mainVC);
		updateProperties(ureq);
	}
	
	private void initTabPane(UserRequest ureq) {
		tabPane.addTab(ureq, translate("taxonomy.levels.tab"), "o_sel_taxonomy_level_levels", uureq -> {
			removeAsListenerAndDispose(taxonomyLevelsCtrl);
			taxonomyLevelsCtrl = new TaxonomyTreeTableController(uureq, getWindowControl(), secCallback, taxonomyLevel.getTaxonomy(), taxonomyLevel);
			taxonomyLevelsCtrl.setBreadcrumbPanel(stackPanel);
			listenTo(taxonomyLevelsCtrl);
			return taxonomyLevelsCtrl.getInitialComponent();
		}, true);
		
		tabPane.addTab(ureq, translate("taxonomy.metadata"), "o_sel_taxonomy_level_edit_details", uureq -> {
			removeAsListenerAndDispose(metadataCtrl);
			metadataCtrl = new EditTaxonomyLevelController(uureq, getWindowControl(), secCallback, taxonomyLevel);
			listenTo(metadataCtrl);
			return metadataCtrl.getInitialComponent();
		}, true);
		
		if (secCallback.canViewManagement(taxonomyLevel)) {
			tabPane.addTab(ureq, translate("taxonomy.level.management"), "o_sel_taxonomy_level_management", uureq -> {
				removeAsListenerAndDispose(managementCtrl);
				managementCtrl = new TaxonomyLevelManagementController(uureq, getWindowControl(), taxonomyLevel);
				listenTo(managementCtrl);
				return managementCtrl.getInitialComponent();
			}, true);
		}
		
		if (secCallback.canViewCompetences()) {
			tabPane.addTab(ureq, translate("taxonomy.level.competences"), "o_sel_taxonomy_level_competences", uureq -> {
				removeAsListenerAndDispose(competencesCtrl);
				competencesCtrl = new TaxonomyLevelCompetenceController(uureq, getWindowControl(), taxonomyLevel);
				listenTo(competencesCtrl);
				return competencesCtrl.getInitialComponent();
			}, true);
		}
		
		tabPane.addTab(ureq, translate("taxonomy.level.relations"), "o_sel_taxonomy_level_edit_relations", uureq -> {
			removeAsListenerAndDispose(relationsCtrl);
			relationsCtrl = new TaxonomyLevelRelationsController(uureq, getWindowControl(), taxonomyLevel);
			listenTo(relationsCtrl);
			return relationsCtrl.getInitialComponent();
		}, true);
	}
	
	private void updateProperties(UserRequest ureq) {
		String teaserImageUrl = null;
		VFSLeaf teaserImage = taxonomyService.getTeaserImage(taxonomyLevel);
		if (teaserImage != null) {
			teaserImageUrl = registerMapper(ureq, new VFSMediaMapper(teaserImage));
		}
		mainVC.contextPut("imageUrl", teaserImageUrl);
		mainVC.contextPut("square", catalogV2Module.isEnabled() && CatalogV2Module.TAXONOMY_LEVEL_LAUNCHER_STYLE_SQUARE.equals(catalogV2Module.getLauncherTaxonomyLevelStyle()));
		
		mainVC.contextPut("title", TaxonomyUIFactory.translateDisplayName(getTranslator(), taxonomyLevel));
		mainVC.contextPut("identifier", taxonomyLevel.getIdentifier());
		TaxonomyLevelType type = taxonomyLevel.getType();
		String typeName = null;
		if (type != null) {
			typeName = type.getDisplayName();
		}
		mainVC.contextPut("typeName", typeName);
	}

	@Override
	public void activate(UserRequest ureq, List<ContextEntry> entries, StateEntry state) {
		if(entries == null || entries.isEmpty()) return;
		
		tabPane.activate(ureq, entries, state);
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		if(moveLink == source) {
			doMove(ureq);
		} else if(newLink == source) {
			doCreateTaxonomyLevel(ureq);
		} else if(deleteLink == source) {
			doConfirmDelete(ureq);
		} else if (source == tabPane) {
			tabPane.addToHistory(ureq, getWindowControl());
		}
	}
	
	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if (source == taxonomyLevelsCtrl) {
			if (event instanceof OpenTaxonomyLevelEvent) {
				fireEvent(ureq, event);
			}
		} else if(metadataCtrl == source) {
			if(event == Event.DONE_EVENT || event == Event.CHANGED_EVENT) {
				taxonomyLevel = metadataCtrl.getTaxonomyLevel();
				updateProperties(ureq);
				fireEvent(ureq, event);
			} else if(event == Event.CANCELLED_EVENT) {
				fireEvent(ureq, Event.CANCELLED_EVENT);
			}
		} else if(confirmDeleteCtrl == source) {
			if(event == Event.DONE_EVENT || event == Event.CHANGED_EVENT || event instanceof DeleteTaxonomyLevelEvent) {
				fireEvent(ureq, new DeleteTaxonomyLevelEvent());
			}
			cmc.deactivate();
			cleanUp();
		} else if(createTaxonomyLevelCtrl == source) {
			if(event == Event.DONE_EVENT || event == Event.CHANGED_EVENT) {
				fireEvent(ureq, new NewTaxonomyLevelEvent(createTaxonomyLevelCtrl.getTaxonomyLevel()));
			}
			cmc.deactivate();
			cleanUp();
		} else if(moveLevelCtrl == source) {
			if(event == Event.DONE_EVENT || event == Event.CHANGED_EVENT) {
				taxonomyLevel = moveLevelCtrl.getMovedTaxonomyLevel();
				fireEvent(ureq, new MoveTaxonomyLevelEvent(moveLevelCtrl.getMovedTaxonomyLevel()));
			}
			cmc.deactivate();
			cleanUp();
		} else if(cmc == source) {
			cleanUp();
		}
		super.event(ureq, source, event);
	}
	
	private void cleanUp() {
		removeAsListenerAndDispose(createTaxonomyLevelCtrl);
		removeAsListenerAndDispose(actionsCalloutCtrl);
		removeAsListenerAndDispose(confirmDeleteCtrl);
		removeAsListenerAndDispose(moveLevelCtrl);
		removeAsListenerAndDispose(cmc);
		createTaxonomyLevelCtrl = null;
		actionsCalloutCtrl = null;
		confirmDeleteCtrl = null;
		moveLevelCtrl = null;
		cmc = null;
	}
	
	private void doConfirmDelete(UserRequest ureq) {
		taxonomyLevel = taxonomyService.getTaxonomyLevel(taxonomyLevel);
		if(TaxonomyLevelManagedFlag.isManaged(taxonomyLevel.getManagedFlags(), TaxonomyLevelManagedFlag.delete)) {
			showWarning("warning.atleastone.level");
			return;
		}

		taxonomyLevel = taxonomyService.getTaxonomyLevel(taxonomyLevel);
		Taxonomy taxonomy = taxonomyLevel.getTaxonomy();
		List<TaxonomyLevel> levelToDelete = Collections.singletonList(taxonomyLevel);
		confirmDeleteCtrl = new DeleteTaxonomyLevelController(ureq, getWindowControl(), secCallback, levelToDelete, taxonomy);
		listenTo(confirmDeleteCtrl);

		String title = translate("confirmation.delete.level.title");
		cmc = new CloseableModalController(getWindowControl(), translate("close"), confirmDeleteCtrl.getInitialComponent(), true, title);
		listenTo(cmc);
		cmc.activate();
	}
	
	private void doMove(UserRequest ureq) {
		if(guardModalController(moveLevelCtrl)) return;
		
		taxonomyLevel = taxonomyService.getTaxonomyLevel(taxonomyLevel);
		Taxonomy taxonomy = taxonomyLevel.getTaxonomy();
		List<TaxonomyLevel> levelsToMove = Collections.singletonList(taxonomyLevel);
		moveLevelCtrl = new MoveTaxonomyLevelController(ureq, getWindowControl(), secCallback, levelsToMove, taxonomy);
		listenTo(moveLevelCtrl);
		
		String title = translate("move.taxonomy.level.title", new String[] {StringHelper.escapeHtml(TaxonomyUIFactory.translateDisplayName(getTranslator(), taxonomyLevel)) });
		cmc = new CloseableModalController(getWindowControl(), translate("close"), moveLevelCtrl.getInitialComponent(), true, title);
		listenTo(cmc);
		cmc.activate();
	}
	
	private void doCreateTaxonomyLevel(UserRequest ureq) {
		if(guardModalController(createTaxonomyLevelCtrl)) return;
		
		taxonomyLevel = taxonomyService.getTaxonomyLevel(taxonomyLevel);
		Taxonomy taxonomy = taxonomyLevel.getTaxonomy();
		createTaxonomyLevelCtrl = new EditTaxonomyLevelController(ureq, getWindowControl(), taxonomyLevel, taxonomy);
		listenTo(createTaxonomyLevelCtrl);
		
		cmc = new CloseableModalController(getWindowControl(), translate("close"), createTaxonomyLevelCtrl.getInitialComponent(), true, translate("add.taxonomy.level"));
		listenTo(cmc);
		cmc.activate();
	}
}