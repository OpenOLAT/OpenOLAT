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

import org.olat.core.commons.persistence.DB;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.tabbedpane.TabCreator;
import org.olat.core.gui.components.tabbedpane.TabbedPane;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.gui.control.generic.closablewrapper.CloseableCalloutWindowController;
import org.olat.core.gui.control.generic.closablewrapper.CloseableModalController;
import org.olat.core.gui.control.generic.dtabs.Activateable2;
import org.olat.core.gui.control.generic.modal.DialogBoxController;
import org.olat.core.gui.control.generic.modal.DialogBoxUIFactory;
import org.olat.core.id.context.ContextEntry;
import org.olat.core.id.context.StateEntry;
import org.olat.core.util.StringHelper;
import org.olat.modules.taxonomy.Taxonomy;
import org.olat.modules.taxonomy.TaxonomyLevel;
import org.olat.modules.taxonomy.TaxonomyLevelManagedFlag;
import org.olat.modules.taxonomy.TaxonomyService;
import org.olat.modules.taxonomy.ui.events.DeleteTaxonomyLevelEvent;
import org.olat.modules.taxonomy.ui.events.NewTaxonomyLevelEvent;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 14 nov. 2017<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class TaxonomyLevelOverviewController extends BasicController implements Activateable2 {

	private final TabbedPane tabPane;
	private final VelocityContainer mainVC;
	private final Link actionButton;
	
	private CloseableModalController cmc;
	private ActionsController actionsCtrl;
	private DialogBoxController confirmDeleteDialog;
	private EditTaxonomyLevelController metadataCtrl;
	private TaxonomyLevelRelationsController relationsCtrl;
	private TaxonomyLevelCompetenceController competencesCtrl;
	private CloseableCalloutWindowController actionsCalloutCtrl;
	private EditTaxonomyLevelController createTaxonomyLevelCtrl;
	
	private TaxonomyLevel taxonomyLevel;
	
	@Autowired
	private DB dbInstance;
	@Autowired
	private TaxonomyService taxonomyService;
	
	public TaxonomyLevelOverviewController(UserRequest ureq, WindowControl wControl, TaxonomyLevel taxonomyLevel) {
		super(ureq, wControl);
		
		this.taxonomyLevel = taxonomyLevel;

		mainVC = createVelocityContainer("taxonomy_level_overview");
		
		actionButton = LinkFactory.createButton("actions", mainVC, this);
		actionButton.setIconLeftCSS("o_icon o_icon_actions");
		
		tabPane = new TabbedPane("tabs", ureq.getLocale());
		tabPane.addListener(this);
		
		metadataCtrl = new EditTaxonomyLevelController(ureq, getWindowControl(), taxonomyLevel);
		listenTo(metadataCtrl);
		tabPane.addTab(translate("taxonomy.metadata"), metadataCtrl);
		initTabPane();
		
		mainVC.put("tabs", tabPane);
		
		putInitialPanel(mainVC);
		updateProperties();
	}
	
	private void initTabPane() {
		tabPane.addTab(translate("taxonomy.level.competences"), new TabCreator() {
			@Override
			public Component create(UserRequest uureq) {
				competencesCtrl = new TaxonomyLevelCompetenceController(uureq, getWindowControl(), taxonomyLevel);
				listenTo(competencesCtrl);
				return competencesCtrl.getInitialComponent();
			}
		});
		
		tabPane.addTab(translate("taxonomy.level.relations"), new TabCreator() {
			@Override
			public Component create(UserRequest uureq) {
				relationsCtrl = new TaxonomyLevelRelationsController(uureq, getWindowControl(), taxonomyLevel);
				listenTo(relationsCtrl);
				return relationsCtrl.getInitialComponent();
			}
		});
	}
	
	private void updateProperties() {
		mainVC.contextPut("id", taxonomyLevel.getKey());
		mainVC.contextPut("externalId", taxonomyLevel.getExternalId());
		mainVC.contextPut("path", taxonomyLevel.getMaterializedPathIdentifiers());
	}

	@Override
	protected void doDispose() {
		//
	}
	
	

	@Override
	public void activate(UserRequest ureq, List<ContextEntry> entries, StateEntry state) {
		if(entries == null || entries.isEmpty()) return;
		
		tabPane.activate(ureq, entries, state);
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		if(actionButton == source) {
			doOpenActions(ureq);
		} else if (source == tabPane) {
			tabPane.addToHistory(ureq, getWindowControl());
		}
	}
	
	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if(metadataCtrl == source) {
			if(event == Event.DONE_EVENT || event == Event.CHANGED_EVENT) {
				taxonomyLevel = metadataCtrl.getTaxonomyLevel();
				updateProperties();
				fireEvent(ureq, event);
			} else if(event == Event.CANCELLED_EVENT) {
				fireEvent(ureq, Event.CANCELLED_EVENT);
			}
		} else if(confirmDeleteDialog == source) {
			if (DialogBoxUIFactory.isOkEvent(event) || DialogBoxUIFactory.isYesEvent(event)) {
				doDelete(ureq);
			}
		} else if(createTaxonomyLevelCtrl == source) {
			if(event == Event.DONE_EVENT || event == Event.CHANGED_EVENT) {
				fireEvent(ureq, new NewTaxonomyLevelEvent(createTaxonomyLevelCtrl.getTaxonomyLevel()));
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
		removeAsListenerAndDispose(actionsCtrl);
		removeAsListenerAndDispose(cmc);
		createTaxonomyLevelCtrl = null;
		actionsCalloutCtrl = null;
		actionsCtrl = null;
		cmc = null;
	}
	
	private void doOpenActions(UserRequest ureq) {
		actionsCtrl = new ActionsController(ureq, getWindowControl());
		listenTo(actionsCtrl);
		actionsCalloutCtrl = new CloseableCalloutWindowController(ureq, getWindowControl(),
				actionsCtrl.getInitialComponent(), actionButton.getDispatchID(), "", true, "");
		listenTo(actionsCalloutCtrl);
		actionsCalloutCtrl.activate();
	}
	
	private void doConfirmDelete(UserRequest ureq) {
		if(taxonomyService.canDeleteTaxonomyLevel(taxonomyLevel)) {
			String title = translate("confirmation.delete.level.title");
			String text = translate("confirmation.delete.level", new String[] { StringHelper.escapeHtml(taxonomyLevel.getDisplayName()) });
			confirmDeleteDialog = activateOkCancelDialog(ureq, title, text, confirmDeleteDialog);
		} else {
			showWarning("warning.delete.level");
		}
	}
	
	private void doDelete(UserRequest ureq) {
		if(taxonomyService.deleteTaxonomyLevel(taxonomyLevel)) {
			dbInstance.commit();//commit before sending event
			fireEvent(ureq, new DeleteTaxonomyLevelEvent());
			showInfo("confirm.deleted.level", new String[] { StringHelper.escapeHtml(taxonomyLevel.getDisplayName()) });
		}
	}
	
	private void doMove() {
		//TODO taxonomy
		showWarning("not.implemented");
	}
	
	private void doCreateTaxonomyLevel(UserRequest ureq) {
		if(createTaxonomyLevelCtrl != null) return;
		
		taxonomyLevel = taxonomyService.getTaxonomyLevel(taxonomyLevel);
		Taxonomy taxonomy = taxonomyLevel.getTaxonomy();
		createTaxonomyLevelCtrl = new EditTaxonomyLevelController(ureq, getWindowControl(), taxonomyLevel, taxonomy);
		listenTo(createTaxonomyLevelCtrl);
		
		cmc = new CloseableModalController(getWindowControl(), "close", createTaxonomyLevelCtrl.getInitialComponent(), true, translate("add.taxonomy.level"));
		listenTo(cmc);
		cmc.activate();
	}

	private class ActionsController extends BasicController {

		private final VelocityContainer toolVC;
		private Link moveLink, newLink, deleteLink;

		public ActionsController(UserRequest ureq, WindowControl wControl) {
			super(ureq, wControl);
			
			toolVC = createVelocityContainer("level_actions");
			if(!TaxonomyLevelManagedFlag.isManaged(taxonomyLevel, TaxonomyLevelManagedFlag.move)) {
				moveLink = addLink("move.taxonomy.level", "o_icon_move");
			}
			newLink = addLink("add.taxonomy.level.under", "o_icon_taxonomy_levels");
			if(!TaxonomyLevelManagedFlag.isManaged(taxonomyLevel, TaxonomyLevelManagedFlag.delete)) {
				deleteLink = addLink("delete", "o_icon_delete_item");
			}
			putInitialPanel(toolVC);
		}
		
		private Link addLink(String name, String iconCss) {
			Link link = LinkFactory.createLink(name, name, getTranslator(), toolVC, this, Link.LINK);
			toolVC.put(name, link);
			link.setIconLeftCSS("o_icon " + iconCss);
			return link;
		}

		@Override
		protected void doDispose() {
			//
		}
		
		@Override
		protected void event(UserRequest ureq, Component source, Event event) {
			if(moveLink == source) {
				close();
				doMove();
			} else if(newLink == source) {
				close();
				doCreateTaxonomyLevel(ureq);
			} else if(deleteLink == source) {
				close();
				doConfirmDelete(ureq);
			}
		}
		
		private void close() {
			actionsCalloutCtrl.deactivate();
			cleanUp();
		}
	}
}